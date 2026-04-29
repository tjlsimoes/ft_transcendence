package com.codearena.code_arena_backend.config;

import com.codearena.code_arena_backend.auth.service.JwtService;
import com.codearena.code_arena_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Intercepts STOMP CONNECT frames to authenticate WebSocket sessions via JWT.
 *
 * The client must send the JWT in a STOMP header:
 *   CONNECT
 *   Authorization: Bearer eyJhbGci...
 *
 * On success, a Spring Security Principal is set on the STOMP session,
 * making it available for user-specific topic subscriptions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Handle CONNECT: require a valid Bearer JWT; otherwise reject CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT missing Authorization header; rejecting connection");
                return null; // drop CONNECT -> connection will not be established
            }

            String token = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(token);
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    accessor.setUser(auth);
                    log.debug("WebSocket CONNECT authenticated for user: {}", username);
                } else {
                    log.warn("WebSocket CONNECT provided invalid token for user: {}", username);
                    return null;
                }
            } catch (Exception e) {
                log.warn("WebSocket authentication failed: {}", e.getMessage());
                return null;
            }
        }

        // Handle SUBSCRIBE: prevent subscriptions to public matchmaking topics and
        // require an authenticated principal for user-scoped queues.
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String dest = accessor.getDestination();
            if (dest != null) {
                // Deny old public matchmaking topics
                if (dest.startsWith("/topic/matchmaking") || dest.equals("/queue/matchmaking")) {
                    log.warn("Blocking subscription to public matchmaking destination: {}", dest);
                    return null;
                }

                // Require authenticated principal for user destinations (e.g. /user/queue/...)
                if (dest.startsWith("/user/") || dest.startsWith("/queue/")) {
                    if (accessor.getUser() == null) {
                        log.warn("Blocking subscription to {} because session is unauthenticated", dest);
                        return null;
                    }
                }
            }
        }

        return message;
    }
}
