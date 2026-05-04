package com.codearena.code_arena_backend.ranking.dto;

import com.codearena.code_arena_backend.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single entry in GET /api/leaderboard.
 * Computed from the User entity — no separate rankings table needed.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryResponse {

    private int rank;
    private String username;
    private String avatarUrl;
    private int elo;
    private String league;
    private String mark;
    private String tone;
    private int wins;
    private int losses;
    private int winStreak;
    private int totalDuels;
    private String winRate;

    public static LeaderboardEntryResponse fromUser(int rank, User user) {
        int total = user.getWins() + user.getLosses();
        String winRate = total > 0
                ? Math.round((user.getWins() * 100.0) / total) + "%"
                : "—";

        String leagueName = user.getLeague().name();
        return LeaderboardEntryResponse.builder()
                .rank(rank)
                .username(user.getUsername())
                .avatarUrl(user.getAvatar())
                .elo(user.getElo())
                .league(leagueName)
                .mark(String.valueOf(leagueName.charAt(0)))
                .tone("league-" + leagueName.toLowerCase())
                .wins(user.getWins())
                .losses(user.getLosses())
                .winStreak(user.getWinStreak())
                .totalDuels(total)
                .winRate(winRate)
                .build();
    }
}
