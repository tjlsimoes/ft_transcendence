package com.codearena.code_arena_backend.challenge.controller;

import com.codearena.code_arena_backend.challenge.dto.ChallengeListItemResponse;
import com.codearena.code_arena_backend.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    public ResponseEntity<Page<ChallengeListItemResponse>> listChallenges(
            @RequestParam(required = false) String difficulty,
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<ChallengeListItemResponse> response = challengeService
                .listChallenges(difficulty, pageable)
                .map(ChallengeListItemResponse::from);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
