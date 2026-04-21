package com.codearena.code_arena_backend.auth.service;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
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

    /** JWT lifetime in ms – injected to calculate 'expiresIn' seconds. */
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

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
        // Never store plain-text passwords: BCrypt hashes are one-way.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userService.save(user);

        // Sync league from elo and set status to ONLINE.
        userService.goOnline(user);

        // Generate JWT immediately — user is logged in after registration.
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        return buildResponse(token);
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
                        request.getPassword()
                )
        );

        // Sync league from elo and set status to ONLINE.
        userService.findByUsername(request.getUsername())
                .ifPresent(userService::goOnline);

        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);
        return buildResponse(token);
    }

    /**
     * Marks the user as OFFLINE in the database.
     */
    @Transactional
    public void logout(String username) {
        userService.findByUsername(username)
                .ifPresent(userService::goOffline);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    private AuthResponse buildResponse(String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000) // convert ms → seconds
                .build();
    }
}
