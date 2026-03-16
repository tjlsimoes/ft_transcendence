package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer elo;
    private Integer wins;
    private Integer losses;
    private Integer winStreak;
    private User.League league;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .elo(user.getElo())
                .wins(user.getWins())
                .losses(user.getLosses())
                .winStreak(user.getWinStreak())
                .league(user.getLeague())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
