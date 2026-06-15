package com.codearena.code_arena_backend.duel.dto;

import jakarta.validation.constraints.NotBlank;

public record RunCodeRequest(
        @NotBlank(message = "Code cannot be empty") String code,
        @NotBlank(message = "Language cannot be empty") String language,
        String stdin
) {}
