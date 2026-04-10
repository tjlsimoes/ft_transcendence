package com.codearena.code_arena_backend.auth;

import com.codearena.code_arena_backend.auth.service.JwtService;
import com.codearena.code_arena_backend.auth.service.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
 * If a valid Bearer token is present, the filter populates the
 * SecurityContext so downstream filters see an authenticated request.
 *
 * If the token is absent, invalid, or expired, the filter does NOT
 * short-circuit the response.  It simply continues the filter chain
 * without setting authentication.  The authorisation layer then
 * decides: permitAll endpoints proceed normally; protected endpoints
 * get 401/403 from Spring Security's default handling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

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

        // No Bearer token → skip JWT processing entirely.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.isTokenValid(jwt, userDetails) && !tokenBlacklistService.isBlacklisted(jwt)) {
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
        } catch (Exception e) {
            // Token processing failed (expired, malformed, user not found, etc.).
            // Do NOT short-circuit — let the filter chain continue.
            // Public endpoints (permitAll) will still be accessible.
            log.debug("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
