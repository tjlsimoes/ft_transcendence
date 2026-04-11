package com.codearena.code_arena_backend.challenge.dto;

import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeUpsertRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Difficulty is required")
    private ChallengeDifficulty difficulty;

    @NotBlank(message = "testCases is required")
    private String testCases;
}
