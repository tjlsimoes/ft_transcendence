package com.codearena.code_arena_backend.judge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

/**
 * Request payload for the internal judge endpoint.
 * Contains the user's source code and test cases to evaluate against.
 */
public record JudgeRequest(
        @NotBlank String code,
        @NotBlank String language,
        @NotNull List<TestCaseInput> testCases
) {
    /**
     * A single test case: stdin piped to the compiled binary,
     * and expected stdout to compare against.
     */
    public record TestCaseInput(
            @JsonAlias({"input", "stdin"}) String stdin,
            @JsonAlias({"expected_output", "expectedOutput"}) String expectedOutput
    ) {}
}
