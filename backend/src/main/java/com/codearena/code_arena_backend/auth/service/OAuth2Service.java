package com.codearena.code_arena_backend.auth.service;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.oauth2.OAuth2ProviderConfig;
import com.codearena.code_arena_backend.auth.oauth2.OAuth2UserInfo;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.codearena.code_arena_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final OAuth2ProviderConfig providerConfig;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Builds the authorization URL to redirect the user to the provider login page.
     */
    public String getAuthorizationUrl(String provider) {
        String clientId = providerConfig.getClientId(provider);
        String redirectUri = getRedirectUri(provider);

        if ("github".equalsIgnoreCase(provider)) {
            return "https://github.com/login/oauth/authorize" +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&scope=user:email";
        } else if ("42".equalsIgnoreCase(provider)) {
            return "https://api.intra.42.fr/oauth/authorize" +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code" +
                    "&scope=public";
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    /**
     * Exchanges the authorization code for an access token, fetches the user info,
     * links or creates the user in the database, and returns JWT tokens.
     */
    @Transactional
    public AuthResponse handleOAuth2Callback(String provider, String code) {
        String accessToken = exchangeCodeForToken(provider, code);
        OAuth2UserInfo userInfo = fetchUserInfo(provider, accessToken);
        User user = findOrCreateUser(provider, userInfo);

        // Make user online
        userService.goOnline(user);

        // Generate JWT response
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .build();
    }

    private String exchangeCodeForToken(String provider, String code) {
        String clientId = providerConfig.getClientId(provider);
        String clientSecret = providerConfig.getClientSecret(provider);
        String redirectUri = getRedirectUri(provider);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        String tokenUrl;
        if ("github".equalsIgnoreCase(provider)) {
            tokenUrl = "https://github.com/login/oauth/access_token";
        } else {
            tokenUrl = "https://api.intra.42.fr/oauth/token";
            body.add("grant_type", "authorization_code");
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String token = (String) response.getBody().get("access_token");
                if (token != null) {
                    return token;
                }
            }
            throw new IllegalStateException("Access token not found in provider response");
        } catch (Exception e) {
            log.error("Failed to exchange code for token with provider: {}", provider, e);
            throw new IllegalArgumentException("OAuth2 token exchange failed: " + e.getMessage());
        }
    }

    private OAuth2UserInfo fetchUserInfo(String provider, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        if ("github".equalsIgnoreCase(provider)) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET, request, Map.class);
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    throw new IllegalStateException("GitHub user info response was empty");
                }

                String id = String.valueOf(body.get("id"));
                String username = (String) body.get("login");
                String email = (String) body.get("email");
                String avatarUrl = (String) body.get("avatar_url");

                // If email is private/null, fetch via emails API
                if (email == null || email.trim().isEmpty()) {
                    email = fetchGitHubEmail(request);
                }

                return new OAuth2UserInfo(id, username, email, avatarUrl);
            } catch (Exception e) {
                log.error("Failed to fetch GitHub user info", e);
                throw new IllegalArgumentException("Failed to fetch user info from GitHub: " + e.getMessage());
            }
        } else if ("42".equalsIgnoreCase(provider)) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange("https://api.intra.42.fr/v2/me", HttpMethod.GET, request, Map.class);
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    throw new IllegalStateException("42 user info response was empty");
                }

                String id = String.valueOf(body.get("id"));
                String username = (String) body.get("login");
                String email = (String) body.get("email");

                // 42 avatar URL parsing: image -> link or image -> versions -> medium
                String avatarUrl = null;
                if (body.get("image") instanceof Map<?, ?> imageMap) {
                    avatarUrl = (String) imageMap.get("link");
                }

                return new OAuth2UserInfo(id, username, email, avatarUrl);
            } catch (Exception e) {
                log.error("Failed to fetch 42 user info", e);
                throw new IllegalArgumentException("Failed to fetch user info from 42: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private String fetchGitHubEmail(HttpEntity<Void> request) {
        try {
            ResponseEntity<List> response = restTemplate.exchange("https://api.github.com/user/emails", HttpMethod.GET, request, List.class);
            List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                // Find primary verified email, or first verified email, or first email
                for (Map<String, Object> emailObj : emails) {
                    Boolean primary = (Boolean) emailObj.get("primary");
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
                for (Map<String, Object> emailObj : emails) {
                    Boolean verified = (Boolean) emailObj.get("verified");
                    if (Boolean.TRUE.equals(verified)) {
                        return (String) emailObj.get("email");
                    }
                }
                if (!emails.isEmpty()) {
                    return (String) emails.getFirst().get("email");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch emails for GitHub user", e);
        }
        return null;
    }

    private User findOrCreateUser(String provider, OAuth2UserInfo userInfo) {
        // 1. Check if user already linked via this provider and providerId
        Optional<User> existingOauthUser = userRepository.findByOauthProviderAndOauthProviderId(provider, userInfo.id());
        if (existingOauthUser.isPresent()) {
            return existingOauthUser.get();
        }

        // 2. Check if user exists by email (link local account with matching email)
        if (userInfo.email() != null && !userInfo.email().trim().isEmpty()) {
            Optional<User> existingUserByEmail = userRepository.findByEmail(userInfo.email());
            if (existingUserByEmail.isPresent()) {
                User user = existingUserByEmail.get();
                log.info("Linking existing local user ID: {} with OAuth provider: {}", user.getId(), provider);
                user.setOauthProvider(provider);
                user.setOauthProviderId(userInfo.id());
                // Also update avatar if user doesn't have one
                if ((user.getAvatar() == null || user.getAvatar().contains("default-avatar")) && userInfo.avatarUrl() != null) {
                    user.setAvatar(userInfo.avatarUrl());
                }
                return userRepository.save(user);
            }
        }

        // 3. Create a brand new user
        log.info("Creating new user for OAuth provider: {} ID: {}", provider, userInfo.id());
        User user = new User();

        // Handle unique username constraint
        String baseUsername = userInfo.username();
        if (baseUsername == null || baseUsername.trim().isEmpty()) {
            baseUsername = provider + "_user";
        }
        String finalUsername = baseUsername;
        int count = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + (count++) + (UUID.randomUUID().toString().substring(0, 4));
            if (finalUsername.length() > 30) {
                finalUsername = finalUsername.substring(0, 25) + (count++);
            }
        }

        user.setUsername(finalUsername);
        user.setDisplayName(finalUsername);

        // Handle email (if provider didn't return one, generate a placeholder)
        String email = userInfo.email();
        if (email == null || email.trim().isEmpty()) {
            email = finalUsername + "@" + provider + ".codearena.internal";
        }
        user.setEmail(email);

        user.setOauthProvider(provider);
        user.setOauthProviderId(userInfo.id());
        user.setRole(User.Role.USER);

        if (userInfo.avatarUrl() != null) {
            user.setAvatar(userInfo.avatarUrl());
        } else {
            user.setAvatar("/api/users/avatars/default-avatar.svg");
        }

        // password_hash is nullable in entity now, so we leave it null.
        user.setPassword(null);

        return userRepository.save(user);
    }

    private String getRedirectUri(String provider) {
        // Callback URLs configured in OAuth provider dashboard:
        // https://localhost/api/auth/oauth2/callback/github
        // https://localhost/api/auth/oauth2/callback/42
        // Note: Spring security starter is not handling redirect, our controller is.
        // The domain is dynamic (e.g. host where the user is browsing), but in development/production
        // HTTPS is served on host, and Nginx forwards /api/auth/oauth2/callback/* to backend container.
        // So the redirect URI must match what's registered with the provider.
        // We will construct this redirect URI using the configured backend host, or just standard local callback path.
        // Usually, OAuth providers require a fixed redirect URI. So we can make this redirect URI configurable via properties if needed,
        // but default is pointing to /api/auth/oauth2/callback/{provider}.
        // Let's assume standard localhost/api/auth/oauth2/callback/{provider} for local dev, or make it customizable.
        // Let's make it customizable based on frontendCallbackUrl host, or simply /api/auth/oauth2/callback/{provider}.
        // Wait, the redirect URI registered with the OAuth provider is the path they redirect back to.
        // For CodeArena, Nginx reverse-proxies https://localhost:443 to the backend container.
        // So they will redirect to: https://localhost/api/auth/oauth2/callback/{provider}
        // Let's extract the origin of frontendCallbackUrl (e.g., https://localhost) and append "/api/auth/oauth2/callback/{provider}".
        // This is extremely elegant and works out-of-the-box for any domain (dev or prod)!
        String frontendUrl = providerConfig.getFrontendCallbackUrl();
        // frontendCallbackUrl is usually: https://localhost/oauth2/callback
        // We want: https://localhost/api/auth/oauth2/callback/{provider}
        String origin = frontendUrl.replace("/oauth2/callback", "");
        return origin + "/api/auth/oauth2/callback/" + provider;
    }
}
