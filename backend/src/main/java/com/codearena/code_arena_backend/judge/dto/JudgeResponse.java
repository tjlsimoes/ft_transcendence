package com.codearena.code_arena_backend.judge.dto;

import java.util.List;

/**
 * Response from the judge service after evaluating a submission.
 *
 * @param passed           true only if ALL test cases passed
 * @param totalTests       total number of test cases evaluated
 * @param passedTests      number of test cases that passed
 * @param runtimeMs        total wall-clock time across all test executions
 * @param memoryKb         peak memory usage (best-effort from Docker stats)
 * @param compilationError non-null if gcc compilation failed; test results will be empty
 * @param results          per-test-case results (empty when compilationError is set)
 */
public record JudgeResponse(
        boolean passed,
        int totalTests,
        int passedTests,
        long runtimeMs,
        long memoryKb,
        String compilationError,
        List<TestCaseResult> results
) {
    /**
     * Result of a single test case execution.
     */
    public record TestCaseResult(
            int index,
            boolean passed,
            String actualOutput,
            String expectedOutput,
            String error,
            long runtimeMs
    ) {}

    /**
     * Factory for a compilation-failure response (no tests executed).
     */
    public static JudgeResponse compilationFailure(int totalTests, String compileError) {
        return new JudgeResponse(false, totalTests, 0, 0, 0, compileError, List.of());
    }
}
