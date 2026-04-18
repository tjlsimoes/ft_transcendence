package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.user.entity.User;

public record FriendSummaryResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        User.UserStatus status,
        boolean online
) {
    public static FriendSummaryResponse from(User user) {
        String resolvedDisplayName = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName();
        boolean isOnline = user.getStatus() != User.UserStatus.OFFLINE;

        return new FriendSummaryResponse(
                user.getId(),
                user.getUsername(),
                resolvedDisplayName,
                user.getAvatar(),
                user.getStatus(),
                isOnline
        );
    }
}
