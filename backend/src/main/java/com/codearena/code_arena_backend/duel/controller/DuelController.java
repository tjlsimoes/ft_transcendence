package com.codearena.code_arena_backend.duel.controller;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.duel.service.DuelSubmissionService;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/duels")
@RequiredArgsConstructor
public class DuelController {

    private final DuelSubmissionService submissionService;
    private final DuelRepository duelRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    @GetMapping("/{duelId}")
    public ResponseEntity<?> getDuelStatus(
            @PathVariable Long duelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found"));

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Challenge challenge = challengeRepository.findById(duel.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        boolean hasSubmitted = submissionRepository.findByDuelIdAndUserId(duel.getId(), user.getId()).isPresent();

        Map<String, Object> response = new java.util.HashMap<>(Map.of(
                "id", duel.getId(),
                "status", duel.getStatus(),
                "challengeId", duel.getChallengeId(),
                "challengerId", duel.getChallengerId(),
                "opponentId", duel.getOpponentId(),
                "hasSubmitted", hasSubmitted,
                "winnerId", duel.getWinnerId() != null ? duel.getWinnerId() : "NONE"
        ));

        long timeLeftSecs = 0;
        if (duel.getStatus() == Duel.DuelStatus.IN_PROGRESS && duel.getStartedAt() != null) {
            long elapsed = Duration.between(duel.getStartedAt(), LocalDateTime.now()).getSeconds();
            timeLeftSecs = Math.max(0, challenge.getTimeLimitSecs() - elapsed);
        } else if (duel.getStatus() == Duel.DuelStatus.MATCHED || duel.getStatus() == Duel.DuelStatus.WAITING) {
            timeLeftSecs = challenge.getTimeLimitSecs();
        }
        response.put("timeLeftSecs", timeLeftSecs);

        if (duel.getStatus() == Duel.DuelStatus.COMPLETED || duel.getStatus() == Duel.DuelStatus.DRAW) {
            List<Submission> submissions = submissionRepository.findByDuelId(duel.getId());
            Submission s1 = submissions.stream().filter(s -> s.getUserId().equals(duel.getChallengerId())).findFirst().orElse(null);
            Submission s2 = submissions.stream().filter(s -> s.getUserId().equals(duel.getOpponentId())).findFirst().orElse(null);
            
            response.put("challengerScore", s1 != null ? s1.getScore() : 0);
            response.put("opponentScore", s2 != null ? s2.getScore() : 0);
            response.put("challengerEloDelta", duel.getChallengerEloChange());
            response.put("opponentEloDelta", duel.getOpponentEloChange());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{duelId}/submit")
    public ResponseEntity<?> submitCode(
            @PathVariable Long duelId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String code = payload.get("code");
        String language = payload.getOrDefault("language", "C");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code cannot be empty"));
        }

        submissionService.submitCode(duelId, userDetails.getUsername(), code, language);
        return ResponseEntity.ok(Map.of("message", "Submission received"));
    }
}
