package com.codearena.code_arena_backend.matchmaking.service;

import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchmakingScheduler — queue scan and pairing")
class MatchmakingSchedulerTest {

    @Mock
    private MatchmakingQueueService queueService;

    @Mock
    private MatchmakingService matchmakingService;

    @InjectMocks
    private MatchmakingScheduler scheduler;

    @Test
    @DisplayName("scanAndPair does nothing when queue has fewer than 2 players")
    void scanAndPair_emptyQueue_noOp() {
        when(queueService.getAllQueuedMembers()).thenReturn(Set.of("1:1000"));

        scheduler.scanAndPair();

        verify(matchmakingService, never()).createMatch(anyLong(), anyLong());
    }

    @Test
    @DisplayName("scanAndPair pairs two players within Elo window")
    void scanAndPair_pairsPlayers() {
        long now = System.currentTimeMillis();

        // Use LinkedHashSet to maintain insertion order for deterministic iteration
        Set<String> members = new LinkedHashSet<>();
        members.add("1:" + now);
        members.add("2:" + now);

        when(queueService.getAllQueuedMembers()).thenReturn(members);
        when(queueService.isQueued(1L)).thenReturn(true);
        when(queueService.getEnqueueTimestamp(1L)).thenReturn(now);
        when(queueService.getQueuedElo(1L)).thenReturn(1200);

        Set<String> candidates = new LinkedHashSet<>(members);
        when(queueService.findPlayersInEloRange(1000, 1400)).thenReturn(candidates);
        when(queueService.getQueuedElo(2L)).thenReturn(1150);

        scheduler.scanAndPair();

        verify(matchmakingService).createMatch(1L, 2L);
    }

    @Test
    @DisplayName("scanAndPair times out players after 60 seconds")
    void scanAndPair_timeoutPlayer() {
        long enqueuedAt = System.currentTimeMillis() - 65_000; // 65 seconds ago
        Set<String> members = Set.of("1:" + enqueuedAt, "2:" + enqueuedAt);

        when(queueService.getAllQueuedMembers()).thenReturn(members);
        when(queueService.isQueued(anyLong())).thenReturn(true);
        when(queueService.getEnqueueTimestamp(anyLong())).thenReturn(enqueuedAt);

        scheduler.scanAndPair();

        verify(matchmakingService, atLeastOnce()).handleTimeout(anyLong());
        verify(matchmakingService, never()).createMatch(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Elo→Difficulty mapping is correct")
    void eloToDifficulty_mapping() {
        // This tests the static method in MatchmakingService
        org.assertj.core.api.Assertions.assertThat(MatchmakingService.mapEloToDifficulty(500))
                .isEqualTo(ChallengeDifficulty.EASY);
        org.assertj.core.api.Assertions.assertThat(MatchmakingService.mapEloToDifficulty(1500))
                .isEqualTo(ChallengeDifficulty.MEDIUM);
        org.assertj.core.api.Assertions.assertThat(MatchmakingService.mapEloToDifficulty(2500))
                .isEqualTo(ChallengeDifficulty.HARD);
        org.assertj.core.api.Assertions.assertThat(MatchmakingService.mapEloToDifficulty(3500))
                .isEqualTo(ChallengeDifficulty.INSANE);
    }
}
