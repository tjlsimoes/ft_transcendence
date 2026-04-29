package com.codearena.code_arena_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP over WebSocket for real-time features
 * (matchmaking notifications, duel events, chat).
 *
 * Clients connect via: wss://host/ws
 * Subscribe to user-scoped queue: /user/queue/matchmaking (server sends via convertAndSendToUser)
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages FROM client to server (e.g. /app/matchmaking/queue)
        config.setApplicationDestinationPrefixes("/app");
        // Prefix used for user-specific destinations (convertAndSendToUser)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Authenticate STOMP CONNECT frames using JWT
        registration.interceptors(webSocketAuthInterceptor);
    }
}
