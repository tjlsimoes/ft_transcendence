package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.user.entity.User;

import java.time.LocalDateTime;

/**
 * Response DTO for user profile endpoints.
 *
 * Exposes the user's profile and game statistics
 * without leaking sensitive fields (password hash, etc.).
 *
 * Ranking context fields (legendThresholdLp, globalRank, highestLp)
 * are populated separately via {@link #withRankingContext}.
 */
public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        Integer wins,
        Integer losses,
        Integer winStreak,
        Integer elo,
        String league,
        String status,
        LocalDateTime createdAt,
        // Ranking context — populated for Master/Legend players
        /** For MASTER: the minimum LP to enter Legend (top 1% of all players). Null for other leagues. */
        Integer legendThresholdLp,
        /** For LEGEND: the player's current global rank position (1-based). Null for other leagues. */
        Integer globalRank,
        /** For LEGEND: the highest LP among all players. Null for other leagues. */
        Integer highestLp
) {

    /**
     * Factory method — converts a User entity into a safe DTO.
     * League is derived from elo, not from the stored enum.
     * Does NOT populate ranking context fields — use {@link #withRankingContext} for that.
     */
    public static UserProfileResponse from(User user, String leagueName) {
        String resolvedDisplayName = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName();

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                resolvedDisplayName,
                user.getBio(),
                user.getAvatar(),
                user.getWins(),
                user.getLosses(),
                user.getWinStreak(),
                user.getElo(),
                leagueName,
                user.getStatus().name(),
                user.getCreatedAt(),
                null, // legendThresholdLp
                null, // globalRank
                null  // highestLp
        );
    }

    /**
     * Returns a copy with the league overridden to LEGEND and ranking context set.
     */
    public UserProfileResponse withLegendContext(int globalRank, int highestLp) {
        return new UserProfileResponse(
                id, username, email, displayName, bio, avatarUrl,
                wins, losses, winStreak, elo,
                "LEGEND", status, createdAt,
                null, globalRank, highestLp
        );
    }

    /**
     * Returns a copy with the legendThresholdLp set (for MASTER players).
     */
    public UserProfileResponse withMasterContext(int legendThresholdLp) {
        return new UserProfileResponse(
                id, username, email, displayName, bio, avatarUrl,
                wins, losses, winStreak, elo,
                league, status, createdAt,
                legendThresholdLp, null, null
        );
    }

}
