package com.codearena.code_arena_backend.duel.dto;

public record RunCodeResponse(
        String status,
        String headline,
        String stdout,
        String stderr,
        String compilerMessage,
        Long executionTimeMs,
        String expectedOutput
) {
    public static RunCodeResponse success(String stdout, String stderr, long executionTimeMs, String expectedOutput) {
        return new RunCodeResponse("success", "Compiled & Executed", stdout, stderr, null, executionTimeMs, expectedOutput);
    }

    public static RunCodeResponse compileError(String compilerMessage) {
        return new RunCodeResponse("compile_error", "Compilation Error", null, null, compilerMessage, null, null);
    }

    public static RunCodeResponse runtimeError(String stderr, long executionTimeMs) {
        return new RunCodeResponse("runtime_error", "Runtime Error", null, stderr, null, executionTimeMs, null);
    }

    public static RunCodeResponse timeout(long executionTimeMs) {
        return new RunCodeResponse("timeout", "Time Limit Exceeded", null, null, null, executionTimeMs, null);
    }
}
