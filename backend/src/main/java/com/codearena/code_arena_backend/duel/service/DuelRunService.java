package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.dto.RunCodeRequest;
import com.codearena.code_arena_backend.duel.dto.RunCodeResponse;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.service.JudgeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuelRunService {

    private final ChallengeRepository challengeRepository;
    private final JudgeService judgeService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RunCodeResponse runCode(Duel duel, RunCodeRequest request) {
        Challenge challenge = challengeRepository.findById(duel.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        // Build source code: user code + test harness (if exists)
        String sourceCode = request.code();
        if (challenge.getTestHarness() != null && !challenge.getTestHarness().isBlank()) {
            sourceCode = request.code() + "\n" + challenge.getTestHarness();
        }

        // Determine stdin and expectedOutput from first test case or user input
        String stdin = request.stdin();
        String expectedOutput = null;
        boolean usingCustomInput = stdin != null && !stdin.isBlank();

        if (!usingCustomInput) {
            JudgeRequest.TestCaseInput firstTestCase = getFirstTestCase(challenge);
            stdin = firstTestCase != null ? firstTestCase.stdin() : "";
            expectedOutput = firstTestCase != null ? firstTestCase.expectedOutput() : null;
        }

        // Build a single test case for the run (no expected output comparison in Judge0)
        List<JudgeRequest.TestCaseInput> testCases = List.of(
                new JudgeRequest.TestCaseInput(stdin, null)
        );

        JudgeRequest judgeRequest = new JudgeRequest(sourceCode, request.language(), testCases);
        JudgeResponse judgeResponse = judgeService.judge(judgeRequest);

        return mapToRunResponse(judgeResponse, expectedOutput);
    }

    private JudgeRequest.TestCaseInput getFirstTestCase(Challenge challenge) {
        if (challenge.getTestCases() == null) {
            return null;
        }
        try {
            List<JudgeRequest.TestCaseInput> testCases = objectMapper.convertValue(
                    challenge.getTestCases(),
                    new TypeReference<>() {}
            );
            if (!testCases.isEmpty()) {
                return testCases.get(0);
            }
        } catch (Exception e) {
            log.warn("Failed to parse test cases for fallback stdin", e);
        }
        return null;
    }

    private RunCodeResponse mapToRunResponse(JudgeResponse judgeResponse, String expectedOutput) {
        // Compilation error
        if (judgeResponse.compilationError() != null) {
            return RunCodeResponse.compileError(judgeResponse.compilationError());
        }

        // No test results (unexpected, but handle gracefully)
        if (judgeResponse.results().isEmpty()) {
            return RunCodeResponse.success("", null, 0, expectedOutput);
        }

        JudgeResponse.TestCaseResult result = judgeResponse.results().get(0);

        // Time limit exceeded
        if (result.error() != null && result.error().contains("Time limit exceeded")) {
            return RunCodeResponse.timeout(result.runtimeMs());
        }

        // Runtime error
        if (result.error() != null) {
            return RunCodeResponse.runtimeError(result.error(), result.runtimeMs());
        }

        // Success
        return RunCodeResponse.success(
                result.actualOutput(),
                null,
                result.runtimeMs(),
                expectedOutput
        );
    }
}
