package com.codearena.code_arena_backend.judge.controller;

import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest.TestCaseInput;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse.TestCaseResult;
import com.codearena.code_arena_backend.judge.service.JudgeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JudgeController}.
 * Verifies response mapping from JudgeService to HTTP responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JudgeController — POST /internal/judge")
class JudgeControllerTest {

    @Mock
    private JudgeService judgeService;

    @InjectMocks
    private JudgeController judgeController;

    @Test
    @DisplayName("judge with valid request returns 200 with passed=true")
    void judge_validRequest_returns200() {
        JudgeRequest request = new JudgeRequest(
                "#include <stdio.h>\nint main(){printf(\"42\\n\");return 0;}",
                "c",
                List.of(new TestCaseInput("", "42\n"))
        );

        JudgeResponse mockResponse = new JudgeResponse(
                true, 1, 1, 42L, 1024L, null,
                List.of(new TestCaseResult(0, true, "42\n", "42\n", null, 42L))
        );
        when(judgeService.judge(request)).thenReturn(mockResponse);

        ResponseEntity<JudgeResponse> response = judgeController.judge(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isTrue();
        assertThat(response.getBody().totalTests()).isEqualTo(1);
        assertThat(response.getBody().passedTests()).isEqualTo(1);
        assertThat(response.getBody().compilationError()).isNull();
        assertThat(response.getBody().results()).hasSize(1);
        assertThat(response.getBody().results().getFirst().passed()).isTrue();

        verify(judgeService).judge(request);
    }

    @Test
    @DisplayName("judge with compilation error returns 200 with compilationError set")
    void judge_compilationError_returns200WithError() {
        JudgeRequest request = new JudgeRequest(
                "int main() { return }",  // invalid C
                "c",
                List.of(new TestCaseInput("", "hello"))
        );

        JudgeResponse mockResponse = JudgeResponse.compilationFailure(
                1, "error: expected ';' at end of declaration"
        );
        when(judgeService.judge(request)).thenReturn(mockResponse);

        ResponseEntity<JudgeResponse> response = judgeController.judge(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isFalse();
        assertThat(response.getBody().compilationError()).contains("expected ';'");
        assertThat(response.getBody().results()).isEmpty();
    }

    @Test
    @DisplayName("judge with failing tests returns 200 with passed=false and results")
    void judge_failingTests_returns200WithFailures() {
        JudgeRequest request = new JudgeRequest(
                "int main(){return 0;}",
                "c",
                List.of(
                        new TestCaseInput("", "hello\n"),
                        new TestCaseInput("", "world\n")
                )
        );

        JudgeResponse mockResponse = new JudgeResponse(
                false, 2, 0, 100L, 512L, null,
                List.of(
                        new TestCaseResult(0, false, "", "hello\n", null, 50L),
                        new TestCaseResult(1, false, "", "world\n", null, 50L)
                )
        );
        when(judgeService.judge(request)).thenReturn(mockResponse);

        ResponseEntity<JudgeResponse> response = judgeController.judge(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isFalse();
        assertThat(response.getBody().totalTests()).isEqualTo(2);
        assertThat(response.getBody().passedTests()).isEqualTo(0);
        assertThat(response.getBody().results()).hasSize(2);
    }

    @Test
    @DisplayName("judge delegates to JudgeService and returns its response unchanged")
    void judge_delegatesToService() {
        JudgeRequest request = new JudgeRequest(
                "code",
                "c",
                List.of(new TestCaseInput("input", "output"))
        );

        JudgeResponse expected = new JudgeResponse(
                true, 1, 1, 10L, 256L, null,
                List.of(new TestCaseResult(0, true, "output", "output", null, 10L))
        );
        when(judgeService.judge(request)).thenReturn(expected);

        ResponseEntity<JudgeResponse> response = judgeController.judge(request);

        assertThat(response.getBody()).isSameAs(expected);
        verify(judgeService).judge(request);
    }
}
