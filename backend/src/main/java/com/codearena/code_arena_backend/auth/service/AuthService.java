package com.codearena.code_arena_backend.auth.service;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService orchestrates registration and login.
 *
 * Register flow:
 *   1. Validate uniqueness of username and email.
 *   2. Hash the plain-text password with BCrypt.
 *   3. Persist the new User entity.
 *   4. Generate and return a JWT so the user is immediately logged in.
 *
 * Login flow:
 *   1. Delegate credential verification to AuthenticationManager
 *      (which uses UserDetailsService + PasswordEncoder under the hood).
 *   2. If valid, load UserDetails and generate a JWT.
 *
 * The service is intentionally thin: business rules (e.g. rate limiting,
 * email verification) will be added in later issues.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    /** JWT lifetime in ms – injected to calculate 'expiresIn' seconds. */
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${user.avatar.default-url:/api/users/avatars/default-avatar.svg}")
    private String defaultAvatarUrl;

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Registers a new user and returns a JWT.
     *
     * @throws IllegalArgumentException if username or email is already taken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Build and persist the new user.
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(User.Role.USER);
        user.setDisplayName(request.getUsername());
        user.setAvatar(defaultAvatarUrl);
        // Never store plain-text passwords: BCrypt hashes are one-way.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userService.save(user);
        // Sync league from elo and set status to ONLINE.
        userService.goOnline(user);
        // Generate tokens immediately — user is logged in after registration.
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        return buildResponse(accessToken, refreshToken);
    }

    /**
     * Authenticates an existing user and returns a JWT.
     *
     * AuthenticationManager.authenticate() throws BadCredentialsException
     * if the credentials are wrong — the controller will handle that.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // This call verifies username + hashed password via UserDetailsService.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Sync league from elo and set status to ONLINE.
        userService.findByUsername(request.getUsername())
                .ifPresent(userService::goOnline);

        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        return buildResponse(accessToken, refreshToken);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshToken the refresh token string
     * @return a new AuthResponse with access and refresh tokens
     * @throws IllegalArgumentException if the token is invalid or expired
     */
    public AuthResponse refreshToken(String refreshToken) {
        String username;
        String jti;
        try {
            username = jwtService.extractUsername(refreshToken);
            jti = jwtService.extractJti(refreshToken);
        } catch (io.jsonwebtoken.JwtException e) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        if (username == null || jti == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Check if this specific refresh token hash (jti) has been revoked.
        if (tokenBlacklistService.isBlacklisted(jti)) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(refreshToken, userDetails, "refresh")) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // ROTATION: Issue new tokens and blacklist the old refresh token jti
        // immediately.
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        Date expiration = jwtService.extractExpiration(refreshToken);
        long diff = expiration.getTime() - System.currentTimeMillis();
        tokenBlacklistService.blacklistToken(jti, java.time.Duration.ofMillis(diff));

        return buildResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Logs out the user by blacklisting their access token and optionally their
     * refresh token.
     *
     * @param accessToken  the Bearer access token string
     * @param refreshToken (optional) the refresh token string
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        String username = null;

        // 1. Blacklist Access Token (by full string)
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String jwt = accessToken.substring(7);
            try {
                username = jwtService.extractUsername(jwt);
                Date expiration = jwtService.extractExpiration(jwt);
                long diff = expiration.getTime() - System.currentTimeMillis();
                if (diff > 0) {
                    tokenBlacklistService.blacklistToken(jwt, java.time.Duration.ofMillis(diff));
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Token is already expired — no need to blacklist it, but we still
                // extract the username so we can set the user OFFLINE below.
                username = e.getClaims().getSubject();
            } catch (Exception e) {
                // Truly malformed token — try to recover username from refresh token.
            }
        }

        // 2. Blacklist Refresh Token (by jti for efficiency)
        if (refreshToken != null) {
            try {
                if (username == null) {
                    username = jwtService.extractUsername(refreshToken);
                }
                String jti = jwtService.extractJti(refreshToken);
                Date expiration = jwtService.extractExpiration(refreshToken);
                long diff = expiration.getTime() - System.currentTimeMillis();
                if (jti != null && diff > 0) {
                    tokenBlacklistService.blacklistToken(jti, java.time.Duration.ofMillis(diff));
                }
            } catch (Exception e) {
                // Ignore errors during refresh token extraction (e.g. malformed)
            }
        }

        // 3. Set user status to OFFLINE
        if (username != null) {
            userService.findByUsername(username).ifPresent(userService::goOffline);
        }
    }


    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    private AuthResponse buildResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000) // convert ms → seconds
                .build();
    }
}
