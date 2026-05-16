package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuelSubmissionService {

    private final DuelRepository duelRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final DuelEvaluationService evaluationService;
    private final DuelLifecycleService lifecycleService;

    @Transactional
    public void submitCode(Long duelId, String username, String code, String language) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found"));

        if (duel.getStatus() != Duel.DuelStatus.IN_PROGRESS) {
            throw new IllegalStateException("Duel is not in progress. Current status: " + duel.getStatus());
        }

        if (!duel.getChallengerId().equals(user.getId()) && !duel.getOpponentId().equals(user.getId())) {
            throw new IllegalArgumentException("User is not a participant of this duel");
        }

        // Check if user already submitted
        if (submissionRepository.findByDuelIdAndUserId(duelId, user.getId()).isPresent()) {
            throw new IllegalStateException("User already submitted code for this duel");
        }

        Submission submission = new Submission();
        submission.setDuelId(duelId);
        submission.setUserId(user.getId());
        submission.setCode(code);
        submission.setLanguage(language);

        long secondsTaken = Duration.between(duel.getStartedAt(), LocalDateTime.now()).getSeconds();
        submission.setTimeTakenSecs((int) secondsTaken);

        submissionRepository.save(submission);
        log.info("User {} submitted code for Duel {}", username, duelId);

        // Check if both players have submitted
        List<Submission> submissions = submissionRepository.findByDuelId(duelId);
        if (submissions.size() == 1) {
            log.info("First player submitted for Duel {}. Notifying opponent and reducing time if needed.", duelId);
            lifecycleService.reduceTimeLimitTo(duelId, 60);
            lifecycleService.broadcastEvent(duelId, "DUEL_OPPONENT_FINISHED", Map.of(
                "username", username
            ));
        } else if (submissions.size() >= 2) {
            log.info("Both players submitted for Duel {}. Triggering evaluation.", duelId);
            evaluationService.evaluateDuel(duelId);
        }
    }
}
