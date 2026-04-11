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
                .requestMatchers("/api/auth/**", "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/challenges", "/api/challenges/**").permitAll()
                // Everything else requires a valid JWT.
                .anyRequest().authenticated()
            )

            // Never create or use an HTTP session — fully stateless.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our DaoAuthenticationProvider so Spring knows how
            // to verify username + hashed password during login.
            .authenticationProvider(authenticationProvider())

            // Run our JWT filter before Spring's default login filter.
            // This ensures the SecurityContext is populated from the token
            // before any authorisation checks happen.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Wires UserDetailsService + PasswordEncoder into Spring's auth machinery.
     * In Spring Security 6.x, DaoAuthenticationProvider requires UserDetailsService
     * in the constructor (the no-arg constructor was removed).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
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
        return new BCryptPasswordEncoder();
    }
}
