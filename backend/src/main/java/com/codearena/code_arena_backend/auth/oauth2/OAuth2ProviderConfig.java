package com.codearena.code_arena_backend.auth.oauth2;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class OAuth2ProviderConfig {

    @Value("${oauth2.github.client-id:}")
    private String githubClientId;

    @Value("${oauth2.github.client-secret:}")
    private String githubClientSecret;

    @Value("${oauth2.42.client-id:}")
    private String fortyTwoClientId;

    @Value("${oauth2.42.client-secret:}")
    private String fortyTwoClientSecret;

    @Value("${oauth2.frontend-callback-url:https://localhost/oauth2/callback}")
    private String frontendCallbackUrl;

    public String getClientId(String provider) {
        if ("github".equalsIgnoreCase(provider)) {
            return githubClientId;
        } else if ("42".equalsIgnoreCase(provider)) {
            return fortyTwoClientId;
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }

    public String getClientSecret(String provider) {
        if ("github".equalsIgnoreCase(provider)) {
            return githubClientSecret;
        } else if ("42".equalsIgnoreCase(provider)) {
            return fortyTwoClientSecret;
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }
}
