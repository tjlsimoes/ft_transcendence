package com.codearena.code_arena_backend.challenge.controller;

import com.codearena.code_arena_backend.challenge.dto.ChallengeAdminResponse;
import com.codearena.code_arena_backend.challenge.dto.ChallengeListItemResponse;
import com.codearena.code_arena_backend.challenge.dto.ChallengeUpsertRequest;
import com.codearena.code_arena_backend.challenge.service.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.NoSuchElementException;

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

    @PostMapping
    public ResponseEntity<ChallengeAdminResponse> createChallenge(
            @Valid @RequestBody ChallengeUpsertRequest request
    ) {
        ChallengeAdminResponse response = ChallengeAdminResponse.from(
                challengeService.createChallenge(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChallengeAdminResponse> updateChallenge(
            @PathVariable Long id,
            @Valid @RequestBody ChallengeUpsertRequest request
    ) {
        ChallengeAdminResponse response = ChallengeAdminResponse.from(
                challengeService.updateChallenge(id, request)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable Long id) {
        challengeService.deleteChallenge(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
