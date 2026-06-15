package com.codearena.code_arena_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration cors = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .allowCredentials(false)
                .maxAge(3600);

        // Keep strict configured origins and add patterns for Codespaces forwarded URLs.
        if (allowedOrigins != null && allowedOrigins.length > 0) {
            cors.allowedOrigins(allowedOrigins);
        }

        cors.allowedOriginPatterns(
                "https://*.app.github.dev",
                "https://*.app.github.dev:[*]",
                "http://localhost:[*]",
                "https://localhost:[*]");
    }
}
