package com.codearena.code_arena_backend.auth;

import com.codearena.code_arena_backend.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per HTTP request.
 *
 * Flow:
 *   1. Read the "Authorization" header.
 *   2. If it starts with "Bearer ", extract the token string.
 *   3. Parse the username from the token.
 *   4. If the user is not yet authenticated in this request:
 *        a. Load UserDetails from the database.
 *        b. Validate the token (correct user + not expired).
 *        c. If valid, create an Authentication object and store it
 *           in the SecurityContext — Spring Security will treat the
 *           request as authenticated from this point on.
 *   5. Continue the filter chain regardless.
 *
 * This is a "stub" implementation: the full logic is already wired,
 * but error handling (e.g. expired token response) will be refined later.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * UserDetailsService is implemented by UserService (see user package).
     * Spring injects it here by type.
     */
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If there is no Bearer token, skip JWT processing and move on.
        // Public endpoints (e.g. /api/auth/**, /api/health) will pass through.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix (7 characters) to get the raw token.
        final String jwt = authHeader.substring(7);
        final String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (JwtException e) {
            // Malformed, expired, or tampered token — reject with 401.
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired JWT");
            return;
        }

        // Only authenticate if not already authenticated in this request context.
        // SecurityContextHolder stores auth info per thread (request-scoped).
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Build a Spring Security authentication token (no credentials needed
                // because we already verified the JWT signature).
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                        // credentials – not needed post-auth
                                userDetails.getAuthorities()
                        );

                // Attach request metadata (IP, session) to the auth object.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store in the SecurityContext so downstream code can call
                // SecurityContextHolder.getContext().getAuthentication() to get the user.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
