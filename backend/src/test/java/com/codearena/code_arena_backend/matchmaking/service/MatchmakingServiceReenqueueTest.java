package com.codearena.code_arena_backend.matchmaking.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.matchmaking.dto.MatchmakingEvent;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MatchmakingService compensating re-enqueue on failure.
 * Verifies that if match creation fails after dequeue, both players are
 * re-enqueued and notified.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchmakingService — compensating re-enqueue")
class MatchmakingServiceReenqueueTest {

    @Mock
    private MatchmakingQueueService queueService;

    @Mock
    private DuelRepository duelRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MatchmakingService matchmakingService;

    private User player1;
    private User player2;

    @BeforeEach
    void setUp() {
        player1 = new User();
        player1.setId(1L);
        player1.setUsername("player1");
        player1.setDisplayName("Player 1");
        player1.setElo(1200);
        player1.setStatus(User.UserStatus.IN_QUEUE);

        player2 = new User();
        player2.setId(2L);
        player2.setUsername("player2");
        player2.setDisplayName("Player 2");
        player2.setElo(1250);
        player2.setStatus(User.UserStatus.IN_QUEUE);
    }

    @Test
    @DisplayName("createMatch re-enqueues players if no challenge available")
    void createMatch_reEnqueueOnNoChallengeAvailable() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(challengeRepository.findRandomByDifficulty("MEDIUM")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchmakingService.createMatch(1L, 2L))
                .isInstanceOf(Exception.class);

        // Verify dequeue was called
        verify(queueService, times(2)).dequeue(anyLong());

        // Verify re-enqueue was called for both players
        verify(queueService).enqueue(1L, 1200);
        verify(queueService).enqueue(2L, 1250);

        // Verify players' statuses were restored to ONLINE
        assertThat(player1.getStatus()).isEqualTo(User.UserStatus.ONLINE);
        assertThat(player2.getStatus()).isEqualTo(User.UserStatus.ONLINE);

        // Verify notification was sent
        ArgumentCaptor<MatchmakingEvent> eventCaptor = ArgumentCaptor.forClass(MatchmakingEvent.class);
        verify(messagingTemplate, times(2)).convertAndSendToUser(anyString(), eq("/queue/matchmaking"), eventCaptor.capture());

        MatchmakingEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo("QUEUED");
    }

    @Test
    @DisplayName("createMatch re-enqueues players if duel save fails")
    void createMatch_reEnqueueOnDuelSaveFailure() {
        Challenge challenge = new Challenge();
        challenge.setId(1L);
        challenge.setDifficulty(ChallengeDifficulty.MEDIUM);

        when(userRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(challengeRepository.findRandomByDifficulty("MEDIUM")).thenReturn(Optional.of(challenge));
        when(duelRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> matchmakingService.createMatch(1L, 2L))
                .isInstanceOf(RuntimeException.class);

        // Verify re-enqueue was called
        verify(queueService).enqueue(1L, 1200);
        verify(queueService).enqueue(2L, 1250);

        // Verify notification was sent
        verify(messagingTemplate, times(2)).convertAndSendToUser(anyString(), eq("/queue/matchmaking"), any(MatchmakingEvent.class));
    }

    @Test
    @DisplayName("createMatch does not re-enqueue if everything succeeds")
    void createMatch_successfulMatch_noReenqueue() {
        Challenge challenge = new Challenge();
        challenge.setId(1L);
        challenge.setDifficulty(ChallengeDifficulty.MEDIUM);

        Duel duel = new Duel();
        duel.setId(1L);
        duel.setChallengerId(1L);
        duel.setOpponentId(2L);
        duel.setChallengeId(1L);
        duel.setStatus(Duel.DuelStatus.MATCHED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(challengeRepository.findRandomByDifficulty("MEDIUM")).thenReturn(Optional.of(challenge));
        when(duelRepository.save(any())).thenReturn(duel);

        matchmakingService.createMatch(1L, 2L);

        // Verify dequeue but NO re-enqueue
        verify(queueService, times(2)).dequeue(anyLong());
        verify(queueService, never()).enqueue(anyLong(), anyInt());

        // Verify MATCHED event was sent
        ArgumentCaptor<MatchmakingEvent> eventCaptor = ArgumentCaptor.forClass(MatchmakingEvent.class);
        verify(messagingTemplate, times(2)).convertAndSendToUser(anyString(), eq("/queue/matchmaking"), eventCaptor.capture());

        MatchmakingEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo("MATCHED");
    }

    @Test
    @DisplayName("createMatch handles re-enqueue failure gracefully")
    void createMatch_reEnqueueFailure_logsWarning() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(challengeRepository.findRandomByDifficulty("MEDIUM")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Redis down")).when(queueService).enqueue(anyLong(), anyInt());

        assertThatThrownBy(() -> matchmakingService.createMatch(1L, 2L))
                .isInstanceOf(Exception.class);

        // Verify re-enqueue was attempted despite failure
        verify(queueService).enqueue(1L, 1200);
        verify(queueService).enqueue(2L, 1250);

        // Verify notification was attempted (may fail but code tries)
        verify(messagingTemplate, times(2)).convertAndSendToUser(anyString(), eq("/queue/matchmaking"), any(MatchmakingEvent.class));
    }
}
