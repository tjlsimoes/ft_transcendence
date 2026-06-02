package com.codearena.code_arena_backend.challenge.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for challenge preview execution (Run Code button).
 */
public record ChallengeRunCodeRequest(
        @NotBlank String code,
        String language
) {
}
