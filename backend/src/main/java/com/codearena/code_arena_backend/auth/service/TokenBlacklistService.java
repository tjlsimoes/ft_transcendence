package com.codearena.code_arena_backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service to manage a blacklist of revoked JWT tokens using Redis.
 * When a user logs out, their access token is stored here until it naturally
 * expires.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt_blacklist:";

    /**
     * Adds a token to the blacklist with a specific time-to-live.
     *
     * @param token the JWT string
     * @param ttl   how long to keep it in the blacklist (remaining token life)
     */
    public void blacklistToken(String token, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "revoked", ttl);
    }

    /**
     * Checks if a token is in the blacklist.
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
