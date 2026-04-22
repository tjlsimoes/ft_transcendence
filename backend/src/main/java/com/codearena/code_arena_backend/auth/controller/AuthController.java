package com.codearena.code_arena_backend.auth.controller;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
     * Validates credentials and returns a JWT.
     * Returns 200 OK on success, 401 Unauthorised on bad credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * Marks the user as OFFLINE. Requires a valid JWT.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
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
}
