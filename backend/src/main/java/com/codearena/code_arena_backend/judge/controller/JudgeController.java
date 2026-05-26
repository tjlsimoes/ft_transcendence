package com.codearena.code_arena_backend.judge.controller;

import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.service.JudgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal-only endpoint for evaluating code submissions.
 *
 * <p>This endpoint is protected by {@code denyAll()} in the security config,
 * meaning it cannot be reached through the external HTTP proxy.  It is designed
 * to be called programmatically by other backend services (e.g. SubmissionService)
 * via direct method invocation on {@link JudgeService}.</p>
 *
 * <p>The HTTP endpoint exists as a secondary invocation path for potential future
 * microservice-to-microservice communication within the Docker network.</p>
 */
@RestController
@RequestMapping("/internal/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    @PostMapping
    public ResponseEntity<JudgeResponse> judge(
            @Valid @RequestBody JudgeRequest request) {
        JudgeResponse response = judgeService.judge(request);
        return ResponseEntity.ok(response);
    }
}
