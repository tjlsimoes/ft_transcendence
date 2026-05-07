package com.codearena.code_arena_backend.auth.controller;

import com.codearena.code_arena_backend.auth.LoginRateLimiter;
import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.LogoutRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.auth.dto.RefreshTokenRequest;
import com.codearena.code_arena_backend.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 *
 * All routes are under /api/auth/** which is explicitly whitelisted in
 * SecurityConfig — no JWT is required to reach these endpoints.
 *
 * @Valid triggers Bean Validation on the request body BEFORE the service
 * method is called. If validation fails, Spring returns 400 Bad Request
 * automatically with field error details.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    /**
     * POST /api/auth/register
     * Creates a new account and returns a JWT.
     * Returns 201 Created on success.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Authenticates a user and returns a dual-token pair (access + refresh).
     *
     * PROTECTIONS:
     * - In-memory rate limiting is applied based on the client's IP address.
     * - Threshold: 5 attempts within a 15-minute window.
     *
     * @param request        containing username and password
     * @param servletRequest used to extract the client's IP address
     * @return 200 OK with AuthResponse, or 429 Too Many Requests, or 401
     *         Unauthorized
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        String clientIp = resolveClientIp(servletRequest);

        if (!rateLimiter.isAllowed(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(null); // Or a specific error DTO
        }

        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            // Only count failures against the rate limit, not successful logins.
            rateLimiter.recordAttempt(clientIp);
            throw ex; // re-throw so the @ExceptionHandler below returns 401
        }
    }

    /**
     * Resolves the real client IP from the X-Forwarded-For header set by Nginx,
     * falling back to the direct remote address when no proxy header is present.
     * X-Forwarded-For can be a comma-separated list; the leftmost value is the
     * original client.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * POST /api/auth/refresh
     * Refreshes the access token using a refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * Logs out the user by blacklisting the current JWT.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) LogoutRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(token, refreshToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Global exception handler for this controller.
     * IllegalArgumentException covers "username taken", "email taken", etc.
     * BadCredentialsException is handled here too for a clean 401 response.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtException(io.jsonwebtoken.JwtException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid or expired token"));
    }
}
