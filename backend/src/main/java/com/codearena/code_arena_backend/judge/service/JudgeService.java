package com.codearena.code_arena_backend.judge.service;

import com.codearena.code_arena_backend.judge.config.JudgeProperties;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest.TestCaseInput;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse.TestCaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Code execution service that delegates to Judge0.
 * Submits source code and test cases to Judge0's REST API and maps
 * the results back to our domain objects.
 */
@Service
public class JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private final RestClient restClient;
    private final JudgeProperties props;

    @Autowired
    public JudgeService(JudgeProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.judge0Url())
                .build();
    }

    // Visible for testing
    JudgeService(JudgeProperties props, RestClient restClient) {
        this.props = props;
        this.restClient = restClient;
    }

    /**
     * Judges a submission by sending each test case to Judge0 sequentially.
     */
    public JudgeResponse judge(JudgeRequest request) {
        int totalTests = request.testCases().size();
        List<TestCaseResult> results = new ArrayList<>();
        long totalRuntimeMs = 0;
        long peakMemoryKb = 0;
        int passedCount = 0;

        for (int i = 0; i < totalTests; i++) {
            TestCaseInput tc = request.testCases().get(i);
            Judge0Response j0Response = submitToJudge0(request.code(), tc);

            // Compilation Error (Status 6)
            if (j0Response.status() != null && j0Response.status().id() == 6) {
                log.info("Compilation failed in Judge0: {}", j0Response.compile_output());
                return JudgeResponse.compilationFailure(totalTests, j0Response.compile_output());
            }

            TestCaseResult result = mapResult(i, tc, j0Response);
            results.add(result);

            totalRuntimeMs += result.runtimeMs();
            // Best-effort memory tracking from Judge0
            if (j0Response.memory() != null) {
                peakMemoryKb = Math.max(peakMemoryKb, j0Response.memory().longValue());
            }

            if (result.passed()) {
                passedCount++;
            }
        }

        boolean allPassed = passedCount == totalTests;
        return new JudgeResponse(allPassed, totalTests, passedCount,
                totalRuntimeMs, peakMemoryKb, null, results);
    }

    private Judge0Response submitToJudge0(String sourceCode, TestCaseInput testCase) {
        Map<String, Object> body = new HashMap<>();
        body.put("source_code", sourceCode);
        body.put("language_id", props.languageId());
        
        if (testCase.stdin() != null && !testCase.stdin().isEmpty()) {
            body.put("stdin", testCase.stdin());
        }
        if (testCase.expectedOutput() != null && !testCase.expectedOutput().isEmpty()) {
            body.put("expected_output", testCase.expectedOutput());
        }
        
        body.put("cpu_time_limit", props.cpuTimeLimit());
        body.put("memory_limit", props.memoryLimit());

        try {
            return restClient.post()
                    .uri("/submissions?wait=true")
                    .body(body)
                    .retrieve()
                    .body(Judge0Response.class);
        } catch (Exception e) {
            log.error("Failed to call Judge0 API", e);
            // Return a synthesized internal error response
            return new Judge0Response(
                    null, null, null, "Internal Error calling Judge0: " + e.getMessage(), 
                    null, null, new Judge0Status(13, "Internal Error")
            );
        }
    }

    private TestCaseResult mapResult(int index, TestCaseInput testCase, Judge0Response response) {
        int statusId = response.status() != null ? response.status().id() : 13;
        boolean passed = (statusId == 3); // 3 = Accepted
        
        String errorMsg = null;
        if (!passed) {
            if (statusId == 5) {
                errorMsg = "Time limit exceeded";
            } else if (statusId >= 7 && statusId <= 12) {
                errorMsg = "Runtime error (" + response.status().description() + "): " + normalizeOutput(response.stderr());
            } else if (statusId == 13 || statusId == 14) {
                errorMsg = "Internal error: " + response.message();
            }
        }

        long runtimeMs = 0;
        if (response.time() != null) {
            runtimeMs = (long) (response.time() * 1000);
        }

        return new TestCaseResult(
                index,
                passed,
                normalizeOutput(response.stdout()),
                testCase.expectedOutput(),
                errorMsg,
                runtimeMs
        );
    }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        return output.stripTrailing();
    }

    // DTO for parsing Judge0 JSON response
    record Judge0Response(
            String stdout,
            String stderr,
            String compile_output,
            String message,
            Float time,
            Float memory,
            Judge0Status status
    ) {}

    record Judge0Status(
            int id,
            String description
    ) {}
}
