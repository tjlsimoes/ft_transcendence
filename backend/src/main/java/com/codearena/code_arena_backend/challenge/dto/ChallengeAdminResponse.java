package com.codearena.code_arena_backend.challenge.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;

public record ChallengeAdminResponse(
        Long id,
        String title,
        String description,
        ChallengeDifficulty difficulty,
        Integer timeLimitSecs,
        JsonNode testCases
) {
    public static ChallengeAdminResponse from(Challenge challenge) {
        return new ChallengeAdminResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getDifficulty(),
                challenge.getTimeLimitSecs(),
                challenge.getTestCases()
        );
    }
}
