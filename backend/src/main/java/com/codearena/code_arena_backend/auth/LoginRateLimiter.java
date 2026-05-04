package com.codearena.code_arena_backend.auth;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple in-memory rate-limiter to prevent brute-force attacks on the login
 * endpoint.
 *
 * It tracks attempts per "key" (e.g., IP address or username).
 * If the number of attempts exceeds the threshold within the time window,
 * further attempts are blocked.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;

    private final Map<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Checks if the given key is currently allowed to make a login attempt.
     *
     * @param key the identifier (IP or username)
     * @return true if allowed, false if blocked
     */
    public boolean isAllowed(String key) {
        LoginAttempt attempt = attemptsCache.get(key);
        if (attempt == null) {
            return true;
        }

        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            if (attempt.getLastAttempt().plusMinutes(BLOCK_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
                return false;
            } else {
                // Block expired, reset.
                attemptsCache.remove(key);
                return true;
            }
        }

        return true;
    }

    /**
     * Records a login attempt (success or failure).
     * In a production system, you might only record failures, but for simple
     * rate-limiting of the endpoint itself, we record every hit.
     *
     * @param key the identifier
     */
    public void recordAttempt(String key) {
        attemptsCache.compute(key, (k, v) -> {
            if (v == null || v.getLastAttempt().plusMinutes(BLOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
                return new LoginAttempt(1, LocalDateTime.now());
            }
            return new LoginAttempt(v.getAttempts() + 1, LocalDateTime.now());
        });
    }

    private static class LoginAttempt {
        private final int attempts;
        private final LocalDateTime lastAttempt;

        public LoginAttempt(int attempts, LocalDateTime lastAttempt) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
    }
}
