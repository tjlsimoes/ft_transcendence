package com.codearena.code_arena_backend.judge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised configuration for the Judge0 code execution service.
 *
 * @param judge0Url      URL of the Judge0 server (e.g., http://judge0-server:2358)
 * @param languageId     Judge0 language ID (e.g., 50 for C GCC)
 * @param cpuTimeLimit   CPU time limit in seconds
 * @param memoryLimit    Memory limit in kilobytes
 */
@ConfigurationProperties(prefix = "judge")
public record JudgeProperties(
        String judge0Url,
        int languageId,
        float cpuTimeLimit,
        int memoryLimit
) {}
