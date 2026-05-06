package com.codearena.code_arena_backend.matchmaking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchmakingQueueService — Redis queue operations")
class MatchmakingQueueServiceTest {

    @InjectMocks
    private MatchmakingQueueService queueService;

    // Note: Most Redis operation tests have been moved to MatchmakingQueueServiceAtomicityTest,
    // which tests the atomic Lua script behavior for enqueue().
    // This class now contains only utility method tests.

    @Test
    @DisplayName("extractUserId parses member string correctly")
    void extractUserId_parsesCorrectly() {
        assertThat(MatchmakingQueueService.extractUserId("42:1713900000000")).isEqualTo(42L);
    }

    @Test
    @DisplayName("extractUserId parses negative userId")
    void extractUserId_negative() {
        assertThat(MatchmakingQueueService.extractUserId("999:9999999999")).isEqualTo(999L);
    }
}
