package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for GET /api/users/me.
 *
 * Exposes the authenticated user's profile and game statistics
 * without leaking sensitive fields (password hash, internal IDs, etc.).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Integer elo;
    private Integer wins;
    private Integer losses;
    private Integer winStreak;
    private String league;
    private String status;
    private LocalDateTime createdAt;

    // Ranking context — populated for Master/Legend players
    /** For MASTER: the minimum LP to enter Legend (top 1% threshold). Null for other leagues. */
    private Integer legendThresholdLp;
    /** For LEGEND: the player's current global rank position (1-based). Null for other leagues. */
    private Integer globalRank;
    /** For LEGEND: the highest LP among all players. Null for other leagues. */
    private Integer highestLp;

    /**
     * Factory method — converts a User entity into a safe DTO.
     * League is derived from elo, not from the stored enum.
     * Does NOT populate ranking context fields — use {@link #withRankingContext} for that.
     */
    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatar())
                .elo(user.getElo())
                .wins(user.getWins())
                .losses(user.getLosses())
                .winStreak(user.getWinStreak())
                .league(leagueFromElo(user.getElo()))
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Derives league name from elo value.
     * Bronze 0-999 | Silver 1000-1999 | Gold 2000-2999 | Master 3000+
     * Legend status is determined separately (top 1% of Master+ players).
     */
    public static String leagueFromElo(int elo) {
        if (elo >= 3000) return "MASTER";
        if (elo >= 2000) return "GOLD";
        if (elo >= 1000) return "SILVER";
        return "BRONZE";
    }
}
