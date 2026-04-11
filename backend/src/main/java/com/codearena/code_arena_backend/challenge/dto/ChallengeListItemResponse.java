package com.codearena.code_arena_backend.challenge.dto;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;

public record ChallengeListItemResponse(
        Long id,
        String title,
        String description,
        ChallengeDifficulty difficulty,
        Integer timeLimitSecs
) {
    public static ChallengeListItemResponse from(Challenge challenge) {
        return new ChallengeListItemResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getDifficulty(),
                challenge.getTimeLimitSecs()
        );
    }
}
