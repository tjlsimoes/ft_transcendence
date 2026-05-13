package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.ranking.util.LeagueUtils;
import com.codearena.code_arena_backend.user.entity.User;

import java.time.LocalDateTime;

/**
 * Public-facing profile DTO — returned by GET /api/users/{id}.
 *
 * Intentionally excludes PII fields (email) that must not be
 * visible to other authenticated users.
 */
public record PublicUserProfileResponse(
        Long id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        Integer wins,
        Integer losses,
        Integer winStreak,
        Integer elo,
        String league,
        String status,
        LocalDateTime createdAt
) {

    public static PublicUserProfileResponse from(User user) {
        String resolvedDisplayName = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName();

        return new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                resolvedDisplayName,
                user.getBio(),
                user.getAvatar(),
                user.getWins(),
                user.getLosses(),
                user.getWinStreak(),
                user.getElo(),
                LeagueUtils.leagueFromElo(user.getElo()),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
