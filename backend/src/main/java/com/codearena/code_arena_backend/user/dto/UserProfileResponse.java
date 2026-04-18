package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.user.entity.User;

public record UserProfileResponse(
        Long id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        Integer wins,
        Integer losses,
        Integer elo,
        User.League league,
        User.UserStatus status
) {
    public static UserProfileResponse from(User user) {
        String resolvedDisplayName = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName();

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                resolvedDisplayName,
                user.getBio(),
                user.getAvatar(),
                user.getWins(),
                user.getLosses(),
                user.getElo(),
                user.getLeague(),
                user.getStatus()
        );
    }
}
