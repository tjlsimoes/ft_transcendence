package com.codearena.code_arena_backend.matchmaking.controller;

import com.codearena.code_arena_backend.matchmaking.service.MatchmakingQueueService;
import com.codearena.code_arena_backend.matchmaking.service.MatchmakingService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchmakingController — REST queue endpoints")
class MatchmakingControllerTest {

    @Mock
    private MatchmakingQueueService queueService;

    @Mock
    private MatchmakingService matchmakingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MatchmakingController controller;

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("player1");
        user.setElo(1200);
        user.setStatus(User.UserStatus.ONLINE);
        return user;
    }

    @Test
    @DisplayName("POST /queue returns 200 with QUEUED event")
    void enqueue_returns200() {
        User user = testUser();
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(queueService.enqueue(1L, 1200)).thenReturn(true);

        var auth = new TestingAuthenticationToken("player1", null);
        var response = controller.enqueue(auth);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().type()).isEqualTo("QUEUED");
        verify(userRepository).save(user);
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.IN_QUEUE);

        // Verify notification sent via user destination
        verify(messagingTemplate).convertAndSendToUser(
                eq("player1"),
                eq("/queue/matchmaking"),
                any()
        );
    }

    @Test
    @DisplayName("POST /queue is idempotent when already queued")
    void enqueue_idempotent() {
        User user = testUser();
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(queueService.enqueue(1L, 1200)).thenReturn(false);

        var auth = new TestingAuthenticationToken("player1", null);
        var response = controller.enqueue(auth);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().type()).isEqualTo("QUEUED");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /queue returns 409 when player is in a duel")
    void enqueue_conflictWhenInDuel() {
        User user = testUser();
        user.setStatus(User.UserStatus.IN_DUEL);
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));

        var auth = new TestingAuthenticationToken("player1", null);
        var response = controller.enqueue(auth);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().type()).isEqualTo("ERROR");
        assertThat(response.getBody().message()).contains("Cannot queue while in a duel");
    }

    @Test
    @DisplayName("DELETE /queue returns 204")
    void cancelQueue_returns204() {
        User user = testUser();
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));

        var auth = new TestingAuthenticationToken("player1", null);
        ResponseEntity<Void> response = controller.cancelQueue(auth);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(matchmakingService).cancelQueue(1L);
    }

    @Test
    @DisplayName("DELETE /queue returns 404 when not queued")
    void cancelQueue_returns404() {
        User user = testUser();
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        doThrow(new IllegalStateException("Not in queue")).when(matchmakingService).cancelQueue(1L);

        var auth = new TestingAuthenticationToken("player1", null);
        ResponseEntity<Void> response = controller.cancelQueue(auth);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("GET /queue/status returns queue state")
    void queueStatus_returnsState() {
        User user = testUser();
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(queueService.isQueued(1L)).thenReturn(true);

        var auth = new TestingAuthenticationToken("player1", null);
        ResponseEntity<Map<String, Object>> response = controller.queueStatus(auth);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().get("queued")).isEqualTo(true);
        assertThat(response.getBody().get("elo")).isEqualTo(1200);
    }
}
