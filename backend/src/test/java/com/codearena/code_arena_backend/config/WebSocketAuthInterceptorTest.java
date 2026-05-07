package com.codearena.code_arena_backend.config;

import com.codearena.code_arena_backend.auth.service.JwtService;
import com.codearena.code_arena_backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for WebSocketAuthInterceptor security controls.
 * Verifies rejection of CONNECT without JWT and blocking of unauthorized SUBSCRIBE.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAuthInterceptor — STOMP authentication and authorization")
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    private Message<?> createStompMessage(StompCommand command, String destination, String authHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authHeader != null) {
            accessor.addNativeHeader("Authorization", authHeader);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    // --------- CONNECT TESTS ---------

    @Test
    @DisplayName("CONNECT without Authorization header is rejected")
    void connect_withoutAuthHeader_rejected() {
        Message<?> message = createStompMessage(StompCommand.CONNECT, null, null);
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("CONNECT with invalid Bearer format is rejected")
    void connect_invalidBearerFormat_rejected() {
        Message<?> message = createStompMessage(StompCommand.CONNECT, null, "Basic somecreds");
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("CONNECT with invalid token is rejected")
    void connect_invalidToken_rejected() {
        String token = "invalid.jwt.token";
        UserDetails userDetails = new User("player1", "pass", Collections.emptyList());
        when(jwtService.extractUsername(token)).thenReturn("player1");
        when(userService.loadUserByUsername("player1")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        Message<?> message = createStompMessage(StompCommand.CONNECT, null, "Bearer " + token);
        Message<?> result = interceptor.preSend(message, null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("CONNECT with JWT extraction error is rejected")
    void connect_jwtExtractionError_rejected() {
        String token = "malformed.token";
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        Message<?> message = createStompMessage(StompCommand.CONNECT, null, "Bearer " + token);
        Message<?> result = interceptor.preSend(message, null);

        assertThat(result).isNull();
    }

    // --------- SUBSCRIBE TESTS ---------

    @Test
    @DisplayName("SUBSCRIBE to public /topic/matchmaking/* is blocked")
    void subscribe_publicMatchmakingTopic_blocked() {
        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, "/topic/matchmaking/1", null);
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SUBSCRIBE to /queue/matchmaking without auth is blocked")
    void subscribe_queueMatchmakingUnauthenticated_blocked() {
        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, "/queue/matchmaking", null);
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SUBSCRIBE to /user/queue/matchmaking without auth is blocked")
    void subscribe_userQueueUnauthenticated_blocked() {
        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, "/user/queue/matchmaking", null);
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SUBSCRIBE to /user/queue/matchmaking with valid auth passes check")
    void subscribe_userQueueWithValidAuth() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/queue/matchmaking");
        accessor.setUser(new UsernamePasswordAuthenticationToken("player1", null));

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(message, null);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SUBSCRIBE without destination passes through")
    void subscribe_noDestination_allowed() {
        Message<?> message = createStompMessage(StompCommand.SUBSCRIBE, null, null);
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Non-STOMP messages pass through")
    void otherCommand_passesThrough() {
        Message<?> message = createStompMessage(StompCommand.SEND, null, null);
        Message<?> result = interceptor.preSend(message, null);
        assertThat(result).isNotNull();
    }
}
