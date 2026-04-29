package com.codearena.code_arena_backend.judge.service;

import com.codearena.code_arena_backend.judge.config.JudgeProperties;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest.TestCaseInput;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

@DisplayName("JudgeService")
class JudgeServiceTest {

    private MockRestServiceServer mockServer;
    private JudgeService judgeService;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        JudgeProperties props = new JudgeProperties("http://judge0-server:2358", 50, 10f, 128000);
        RestClient.Builder builder = RestClient.builder().baseUrl(props.judge0Url());
        
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        
        judgeService = new JudgeService(props, restClient);
        mapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Single passing test case returns passed=true")
    void judge_singlePassingTest_returnsPassed() throws Exception {
        JudgeService.Judge0Response mockResponse = new JudgeService.Judge0Response(
                "42\n", null, null, null, 0.05f, 1024f,
                new JudgeService.Judge0Status(3, "Accepted")
        );

        mockServer.expect(requestTo("http://judge0-server:2358/submissions?wait=true"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        JudgeRequest request = new JudgeRequest(
                "int main(){return 0;}", "c",
                List.of(new TestCaseInput("", "42"))
        );

        JudgeResponse response = judgeService.judge(request);

        assertThat(response.passed()).isTrue();
        assertThat(response.passedTests()).isEqualTo(1);
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).actualOutput()).isEqualTo("42");
        assertThat(response.results().get(0).expectedOutput()).isEqualTo("42");
        
        mockServer.verify();
    }

    @Test
    @DisplayName("Failing test case returns passed=false")
    void judge_failingTest_returnsNotPassed() throws Exception {
        JudgeService.Judge0Response mockResponse = new JudgeService.Judge0Response(
                "wrong\n", null, null, null, 0.05f, 1024f,
                new JudgeService.Judge0Status(4, "Wrong Answer")
        );

        mockServer.expect(requestTo("http://judge0-server:2358/submissions?wait=true"))
                .andRespond(withSuccess(mapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        JudgeRequest request = new JudgeRequest(
                "int main(){return 0;}", "c",
                List.of(new TestCaseInput("", "correct"))
        );

        JudgeResponse response = judgeService.judge(request);

        assertThat(response.passed()).isFalse();
        assertThat(response.passedTests()).isEqualTo(0);
        assertThat(response.results().get(0).actualOutput()).isEqualTo("wrong");
    }

    @Test
    @DisplayName("Compilation error short-circuits execution")
    void judge_compilationError_returnsError() throws Exception {
        JudgeService.Judge0Response mockResponse = new JudgeService.Judge0Response(
                null, null, "error: expected ';'", null, null, null,
                new JudgeService.Judge0Status(6, "Compilation Error")
        );

        mockServer.expect(requestTo("http://judge0-server:2358/submissions?wait=true"))
                .andRespond(withSuccess(mapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        JudgeRequest request = new JudgeRequest(
                "int main(){return 0}", "c",
                List.of(new TestCaseInput("", "out"))
        );

        JudgeResponse response = judgeService.judge(request);

        assertThat(response.passed()).isFalse();
        assertThat(response.compilationError()).contains("error: expected ';'");
        assertThat(response.results()).isEmpty();
    }

    @Test
    @DisplayName("Runtime error captures stderr")
    void judge_runtimeError_reportsError() throws Exception {
        JudgeService.Judge0Response mockResponse = new JudgeService.Judge0Response(
                "", "Segmentation fault", null, null, 0.01f, 1024f,
                new JudgeService.Judge0Status(11, "Runtime Error (NZEC)")
        );

        mockServer.expect(requestTo("http://judge0-server:2358/submissions?wait=true"))
                .andRespond(withSuccess(mapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        JudgeRequest request = new JudgeRequest(
                "int main(){return 0;}", "c",
                List.of(new TestCaseInput("", "out"))
        );

        JudgeResponse response = judgeService.judge(request);

        assertThat(response.passed()).isFalse();
        assertThat(response.results().get(0).error()).contains("Runtime error");
        assertThat(response.results().get(0).error()).contains("Segmentation fault");
    }

    @Test
    @DisplayName("Time Limit Exceeded sets correct error message")
    void judge_tle_reportsError() throws Exception {
        JudgeService.Judge0Response mockResponse = new JudgeService.Judge0Response(
                null, null, null, null, 10.0f, 1024f,
                new JudgeService.Judge0Status(5, "Time Limit Exceeded")
        );

        mockServer.expect(requestTo("http://judge0-server:2358/submissions?wait=true"))
                .andRespond(withSuccess(mapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        JudgeRequest request = new JudgeRequest(
                "int main(){while(1);return 0;}", "c",
                List.of(new TestCaseInput("", "out"))
        );

        JudgeResponse response = judgeService.judge(request);

        assertThat(response.passed()).isFalse();
        assertThat(response.results().get(0).error()).contains("Time limit exceeded");
    }

    @Test
    @DisplayName("Internal error from Judge0 API call sets internal error result")
    void judge_apiError_reportsInternalError() throws Exception {
        mockServer.expect(requestTo("http://judge0-server:2358/submissions?wait=true"))
                .andRespond(withServerError());

        JudgeRequest request = new JudgeRequest(
                "int main(){return 0;}", "c",
                List.of(new TestCaseInput("", "out"))
        );

        JudgeResponse response = judgeService.judge(request);

        assertThat(response.passed()).isFalse();
        assertThat(response.results().get(0).error()).contains("Internal Error calling Judge0");
    }
}
