package com.codearena.code_arena_backend.matchmaking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchmakingQueueService — Redis queue operations")
class MatchmakingQueueServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOps;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @InjectMocks
    private MatchmakingQueueService queueService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    @DisplayName("enqueue adds player to sorted set and hash")
    void enqueue_addsToRedis() {
        when(hashOps.hasKey("matchmaking:players", "1")).thenReturn(false);

        boolean result = queueService.enqueue(1L, 1200);

        assertThat(result).isTrue();
        verify(zSetOps).add(eq("matchmaking:queue"), contains("1:"), eq(1200.0));
        verify(hashOps).put(eq("matchmaking:players"), eq("1"), contains("1200:"));
    }

    @Test
    @DisplayName("enqueue is idempotent — returns false if already queued")
    void enqueue_idempotent() {
        when(hashOps.hasKey("matchmaking:players", "1")).thenReturn(true);

        boolean result = queueService.enqueue(1L, 1200);

        assertThat(result).isFalse();
        verify(zSetOps, never()).add(any(), any(), anyDouble());
    }

    @Test
    @DisplayName("dequeue removes from sorted set and hash")
    void dequeue_removesFromRedis() {
        when(hashOps.get("matchmaking:players", "1")).thenReturn("1200:1000000");

        boolean result = queueService.dequeue(1L);

        assertThat(result).isTrue();
        verify(zSetOps).remove("matchmaking:queue", "1:1000000");
        verify(hashOps).delete("matchmaking:players", "1");
    }

    @Test
    @DisplayName("dequeue returns false when player is not queued")
    void dequeue_notQueued() {
        when(hashOps.get("matchmaking:players", "1")).thenReturn(null);

        boolean result = queueService.dequeue(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isQueued returns true when player exists in hash")
    void isQueued_returnsTrue() {
        when(hashOps.hasKey("matchmaking:players", "5")).thenReturn(true);

        assertThat(queueService.isQueued(5L)).isTrue();
    }

    @Test
    @DisplayName("findPlayersInEloRange delegates to ZRANGEBYSCORE")
    void findPlayersInEloRange_delegatesToRedis() {
        Set<String> expected = Set.of("1:1000", "2:2000");
        when(zSetOps.rangeByScore("matchmaking:queue", 1000, 1400)).thenReturn(expected);

        Set<String> result = queueService.findPlayersInEloRange(1000, 1400);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("extractUserId parses member string correctly")
    void extractUserId_parsesCorrectly() {
        assertThat(MatchmakingQueueService.extractUserId("42:1713900000000")).isEqualTo(42L);
    }

    @Test
    @DisplayName("getEnqueueTimestamp returns timestamp from hash")
    void getEnqueueTimestamp_returnsTimestamp() {
        when(hashOps.get("matchmaking:players", "3")).thenReturn("1500:1713900000000");

        assertThat(queueService.getEnqueueTimestamp(3L)).isEqualTo(1713900000000L);
    }

    @Test
    @DisplayName("getEnqueueTimestamp returns -1 when not queued")
    void getEnqueueTimestamp_notQueued() {
        when(hashOps.get("matchmaking:players", "99")).thenReturn(null);

        assertThat(queueService.getEnqueueTimestamp(99L)).isEqualTo(-1);
    }
}
