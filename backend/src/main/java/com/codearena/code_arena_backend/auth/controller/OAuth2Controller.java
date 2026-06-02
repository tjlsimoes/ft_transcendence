package com.codearena.code_arena_backend.auth.controller;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.oauth2.OAuth2ProviderConfig;
import com.codearena.code_arena_backend.auth.service.OAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;
    private final OAuth2ProviderConfig providerConfig;

    /**
     * Redirects the client browser to the OAuth2 provider authorization login page.
     * GET /api/auth/oauth2/{provider}
     */
    @GetMapping("/{provider}")
    public void redirectToProvider(@PathVariable String provider, HttpServletResponse response) throws IOException {
        try {
            String authUrl = oAuth2Service.getAuthorizationUrl(provider);
            log.info("Redirecting user to OAuth provider {} authorization URL: {}", provider, authUrl);
            response.sendRedirect(authUrl);
        } catch (IllegalArgumentException e) {
            log.error("Invalid provider requested: {}", provider, e);
            String errorUrl = providerConfig.getFrontendCallbackUrl() + "?error=" + URLEncoder.encode("Unsupported provider", StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        } catch (Exception e) {
            log.error("Error initiating OAuth redirect for provider: {}", provider, e);
            String errorUrl = providerConfig.getFrontendCallbackUrl() + "?error=" + URLEncoder.encode("Internal error", StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        }
    }

    /**
     * Receives the callback code from the OAuth2 provider, exchanges it, creates/links user,
     * and redirects back to the frontend with JWT tokens.
     * GET /api/auth/oauth2/callback/{provider}
     */
    @GetMapping("/callback/{provider}")
    public void handleCallback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDesc,
            HttpServletResponse response) throws IOException {

        String targetRedirectUrl = providerConfig.getFrontendCallbackUrl();

        if (error != null || code == null) {
            log.warn("OAuth authorization failed or denied from provider: {}. Error: {}, Description: {}", provider, error, errorDesc);
            String errorMessage = errorDesc != null ? errorDesc : "Access denied by user";
            response.sendRedirect(targetRedirectUrl + "?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
            return;
        }

        try {
            AuthResponse authResponse = oAuth2Service.handleOAuth2Callback(provider, code);
            log.info("OAuth login successful for provider: {}. Redirecting to frontend with tokens.", provider);
            
            String redirectUrl = targetRedirectUrl + 
                    "?token=" + authResponse.getAccessToken() + 
                    "&refreshToken=" + authResponse.getRefreshToken();
            
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Failed to complete OAuth callback handling for provider: {}", provider, e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "OAuth authentication failed";
            response.sendRedirect(targetRedirectUrl + "?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
        }
    }
}
