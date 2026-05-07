package com.codearena.code_arena_backend.matchmaking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Arrays;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * Manages the matchmaking queue in Redis.
 *
 * Data structures:
 *   - Sorted Set "matchmaking:queue": score = elo, member = "userId:timestampMs"
 *   - Hash "matchmaking:players": field = userId, value = "elo:timestampMs"
 *
 * The hash provides O(1) duplicate detection and O(1) timestamp/elo lookup,
 * while the sorted set enables efficient Elo-range queries via ZRANGEBYSCORE.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingQueueService {

    private static final String QUEUE_KEY = "matchmaking:queue";
    private static final String PLAYERS_KEY = "matchmaking:players";

    private final StringRedisTemplate redisTemplate;

    /**
     * Adds a player to the queue. Idempotent — if already queued, this is a no-op.
     *
     * @return true if newly enqueued, false if already in queue
     */
    public boolean enqueue(Long userId, int elo) {
        // Use a Redis Lua script to atomically check/insert into the hash and
        // add the sorted-set member. This prevents race conditions where two
        // concurrent enqueues create multiple ZSET entries for the same user.
        long timestamp = System.currentTimeMillis();
        String member = userId + ":" + timestamp;
        String field = userId.toString();
        String value = elo + ":" + timestamp;

        String lua = "if redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1 then return 0 end "
                + "redis.call('HSET', KEYS[1], ARGV[1], ARGV[2]) "
                + "redis.call('ZADD', KEYS[2], ARGV[4], ARGV[3]) "
                + "return 1";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(lua, Long.class);
        Long result = redisTemplate.execute(script,
                Arrays.asList(PLAYERS_KEY, QUEUE_KEY),
                field, value, member, String.valueOf(elo));

        if (result != null && result == 1L) {
            log.info("Player {} enqueued with elo={}", userId, elo);
            return true;
        } else {
            log.debug("Player {} already in queue, skipping enqueue", userId);
            return false;
        }
    }

    /**
     * Removes a player from the queue.
     *
     * @return true if the player was in the queue and removed
     */
    public boolean dequeue(Long userId) {
        String playerData = (String) redisTemplate.opsForHash().get(PLAYERS_KEY, userId.toString());
        if (playerData == null) {
            return false;
        }

        // Parse "elo:timestamp" to reconstruct the sorted set member
        String[] parts = playerData.split(":");
        String timestamp = parts[1];
        String member = userId + ":" + timestamp;

        redisTemplate.opsForZSet().remove(QUEUE_KEY, member);
        redisTemplate.opsForHash().delete(PLAYERS_KEY, userId.toString());

        log.info("Player {} dequeued", userId);
        return true;
    }

    /**
     * Checks if a player is currently in the queue.
     */
    public boolean isQueued(Long userId) {
        return redisTemplate.opsForHash().hasKey(PLAYERS_KEY, userId.toString());
    }

    /**
     * Returns the timestamp (epoch ms) when the player was enqueued, or -1 if not queued.
     */
    public long getEnqueueTimestamp(Long userId) {
        String playerData = (String) redisTemplate.opsForHash().get(PLAYERS_KEY, userId.toString());
        if (playerData == null) {
            return -1;
        }
        return Long.parseLong(playerData.split(":")[1]);
    }

    /**
     * Returns the elo stored for a queued player, or -1 if not queued.
     */
    public int getQueuedElo(Long userId) {
        String playerData = (String) redisTemplate.opsForHash().get(PLAYERS_KEY, userId.toString());
        if (playerData == null) {
            return -1;
        }
        return Integer.parseInt(playerData.split(":")[0]);
    }

    /**
     * Finds all queued player members whose Elo score falls within [minElo, maxElo].
     * Returns the sorted set members ("userId:timestamp" strings).
     */
    public Set<String> findPlayersInEloRange(int minElo, int maxElo) {
        Set<String> result = redisTemplate.opsForZSet().rangeByScore(QUEUE_KEY, minElo, maxElo);
        return result != null ? result : Collections.emptySet();
    }

    /**
     * Returns all queued player members in the sorted set.
     */
    public Set<String> getAllQueuedMembers() {
        Set<String> result = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
        return result != null ? result : Collections.emptySet();
    }

    /**
     * Extracts the userId from a sorted set member string "userId:timestamp".
     */
    public static Long extractUserId(String member) {
        return Long.parseLong(member.split(":")[0]);
    }
}
