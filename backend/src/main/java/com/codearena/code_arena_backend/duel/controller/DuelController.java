package com.codearena.code_arena_backend.duel.controller;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.duel.service.DuelSubmissionService;
import com.codearena.code_arena_backend.duel.service.DuelLifecycleService;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    private final DuelLifecycleService lifecycleService;
    private final DuelRepository duelRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    /**
     * GET /api/duels/active
     * Returns the authenticated user's current active duel (MATCHED or IN_PROGRESS).
     * Used by the lobby guard to detect and redirect users who are IN_DUEL.
     * Returns 404 if the user has no active duel.
     *
     * IMPORTANT: This endpoint must be declared BEFORE /{duelId} so that Spring
     * does not try to interpret "active" as a duelId path variable.
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveDuel(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return duelRepository.findActiveByUserId(
                        user.getId(),
                        List.of(Duel.DuelStatus.MATCHED, Duel.DuelStatus.IN_PROGRESS))
                .map(duel -> {
                    // Determine the opponent from the caller's perspective
                    Long opponentId = duel.getChallengerId().equals(user.getId())
                            ? duel.getOpponentId()
                            : duel.getChallengerId();

                    String opponentName = userRepository.findById(opponentId)
                            .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getUsername())
                            .orElse("Unknown");

                    Map<String, Object> body = Map.of(
                            "duelId",       duel.getId(),
                            "challengeId",  duel.getChallengeId(),
                            "opponentId",   opponentId,
                            "opponentName", opponentName,
                            "status",       duel.getStatus()
                    );
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{duelId}")
    public ResponseEntity<?> getDuelStatus(
            @PathVariable Long duelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found"));

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Security check: only participants can view their own duel.
        // Prevents any authenticated user from querying arbitrary duel IDs.
        if (!duel.getChallengerId().equals(user.getId()) && !duel.getOpponentId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not a participant of this duel"));
        }

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
        List<Submission> submissions = submissionRepository.findByDuelId(duel.getId());
        boolean opponentHasSubmitted = submissions.stream().anyMatch(s -> !s.getUserId().equals(user.getId()));
        response.put("opponentHasSubmitted", opponentHasSubmitted);

        if (duel.getStatus() == Duel.DuelStatus.IN_PROGRESS && duel.getStartedAt() != null) {
            long elapsedSinceStart = Duration.between(duel.getStartedAt(), LocalDateTime.now()).getSeconds();
            long standardTimeLeft = Math.max(0, challenge.getTimeLimitSecs() - elapsedSinceStart);

            if (submissions.size() == 1) {
                long elapsedSinceSub = Duration.between(submissions.get(0).getSubmittedAt(), LocalDateTime.now()).getSeconds();
                long reducedTimeLeft = Math.max(0, 60 - elapsedSinceSub);
                timeLeftSecs = Math.min(standardTimeLeft, reducedTimeLeft);
            } else {
                timeLeftSecs = standardTimeLeft;
            }
        } else if (duel.getStatus() == Duel.DuelStatus.MATCHED || duel.getStatus() == Duel.DuelStatus.WAITING) {
            timeLeftSecs = challenge.getTimeLimitSecs();
        }
        response.put("timeLeftSecs", timeLeftSecs);

        if (duel.getStatus() == Duel.DuelStatus.COMPLETED || duel.getStatus() == Duel.DuelStatus.DRAW) {
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

    // ------------------------------------------------------------------ //
    //  Exception handlers                                                  //
    // ------------------------------------------------------------------ //

    /**
     * User is not a participant of the requested duel, or a resource was not found.
     * Maps to 403 Forbidden so the caller cannot distinguish "not found" from "not yours".
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotParticipant(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Operation not allowed in the current duel state (e.g., already submitted, not IN_PROGRESS).
     * Maps to 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
