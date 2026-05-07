package com.codearena.code_arena_backend.matchmaking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for atomic enqueue() using Lua script to ensure idempotency
 * and prevent orphaned ZSET entries on concurrent enqueues.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchmakingQueueService.enqueue() atomicity (Lua script)")
class MatchmakingQueueServiceAtomicityTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private MatchmakingQueueService queueService;

    @Test
    @DisplayName("enqueue uses a Lua script to atomically add to hash and sorted set")
    void enqueue_usesLuaScript() {
        // When execute() is called with a Lua script, it should return 1L (success)
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(), any(), any(), any()
        )).thenReturn(1L);

        boolean result = queueService.enqueue(1L, 1200);

        assertThat(result).isTrue();
        // Verify that execute was called (indicating Lua script was used)
        verify(redisTemplate).execute(
                any(DefaultRedisScript.class),
                argThat(list -> list.size() == 2), // [PLAYERS_KEY, QUEUE_KEY]
                anyString(), // field (userId)
                anyString(), // value (elo:timestamp)
                anyString(), // member (userId:timestamp)
                anyString()  // elo as string
        );
    }

    @Test
    @DisplayName("enqueue returns false when Lua script returns 0 (already queued)")
    void enqueue_luaReturnsZero_idempotent() {
        // When execute() returns 0L (player already queued)
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(), any(), any(), any()
        )).thenReturn(0L);

        boolean result = queueService.enqueue(1L, 1200);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("enqueue passes correct arguments to Lua script")
    void enqueue_passesCorrectArgs() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(), any(), any(), any()
        )).thenReturn(1L);

        queueService.enqueue(5L, 1500);

        verify(redisTemplate).execute(
                any(DefaultRedisScript.class),
                argThat(list -> list.size() == 2),
                eq("5"),           // userId as field
                matches("1500:\\d+"), // elo:timestamp
                matches("5:\\d+"),    // userId:timestamp member
                eq("1500")         // elo as score
        );
    }

    @Test
    @DisplayName("enqueue handles null return from execute gracefully")
    void enqueue_nullReturn_treated_asFalse() {
        // Edge case: execute returns null
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(), any(), any(), any()
        )).thenReturn(null);

        boolean result = queueService.enqueue(1L, 1200);

        assertThat(result).isFalse();
    }
}
