package com.codearena.code_arena_backend.config;

import com.codearena.code_arena_backend.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * Key decisions:
 *
 * 1. STATELESS sessions — we never create an HTTP session.
 *    Every request must carry a valid JWT.  This is required for a
 *    scalable, container-friendly API (no sticky sessions needed).
 *
 * 2. CSRF disabled — CSRF attacks require a browser session cookie.
 *    Since we use Authorization headers (Bearer tokens) instead of
 *    cookies, there is nothing for CSRF to exploit.
 *
 * 3. JWT filter before UsernamePasswordAuthenticationFilter — our custom
 *    JwtAuthenticationFilter runs first, populates the SecurityContext,
 *    and then Spring's own filters see an already-authenticated request.
 *
 * 4. DaoAuthenticationProvider — connects the AuthenticationManager to
 *    our UserDetailsService (UserService) and our PasswordEncoder (BCrypt).
 *    AuthService calls authenticationManager.authenticate() during login.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * UserDetailsService is implemented by UserService (user package).
     * Spring resolves it by type — no circular dependency because
     * JwtAuthenticationFilter is injected at filter-chain build time.
     */
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs (see note above).
            .csrf(csrf -> csrf.disable())

            // Apply CORS rules defined in CorsConfig.
            .cors(Customizer.withDefaults())

            // Route authorisation rules.
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token required.
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/avatars/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/challenges", "/api/challenges/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/challenges", "/api/challenges/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/challenges", "/api/challenges/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/challenges", "/api/challenges/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/challenges", "/api/challenges/**").hasRole("ADMIN")
                // Everything else (including /api/auth/logout) requires a valid,
                // non-blacklisted JWT.
                .anyRequest().authenticated()
            )

                // Never create or use an HTTP session — fully stateless.
                // Every request must carry a valid JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /*
                 * NOTE ON AUTHENTICATION REFACTOR:
                 * We previously had a manual AuthenticationProvider bean. However, in Spring
                 * Boot 3.x/4.x,
                 * if we provide UserDetailsService and PasswordEncoder as beans, Spring
                 * Security
                 * automatically configures a DaoAuthenticationProvider for us.
                 *
                 * Manually exposing it as a bean was triggering a
                 * "Global AuthenticationManager"
                 * configuration warning at startup. We now rely on Spring's auto-wiring to keep
                 * the startup logs clean and follow modern standards.
                 */

                // Run our JWT filter before Spring's default login filter.
                // This ensures the SecurityContext is populated from the token
                // before any authorisation checks happen.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes Spring's AuthenticationManager as a bean so AuthService
     * can inject and call it directly during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
