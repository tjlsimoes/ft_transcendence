package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.service.JudgeService;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuelEvaluationService {

    private final DuelRepository duelRepository;
    private final SubmissionRepository submissionRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final JudgeService judgeService;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    @Autowired
    @Lazy
    private DuelLifecycleService lifecycleService;

    @Transactional
    public void evaluateDuel(Long duelId) {
        Duel duel = duelRepository.findById(duelId).orElseThrow();
        
        // Prevent double evaluation
        if (duel.getStatus() == Duel.DuelStatus.EVALUATING || 
            duel.getStatus() == Duel.DuelStatus.COMPLETED) {
            return;
        }

        // Cancel timer if it was triggered by submission
        lifecycleService.cancelTimer(duelId);

        duel.setStatus(Duel.DuelStatus.EVALUATING);
        duelRepository.save(duel);
        
        lifecycleService.broadcastEvent(duelId, "DUEL_EVALUATING", Map.of());

        // We run evaluation async so we don't block the calling thread (e.g. submit thread)
        CompletableFuture.runAsync(() -> doEvaluate(duelId));
    }

    private void doEvaluate(Long duelId) {
        try {
            Duel duel = duelRepository.findById(duelId).orElseThrow();
            Challenge challenge = challengeRepository.findById(duel.getChallengeId()).orElseThrow();
            
            // Parse test cases
            List<JudgeRequest.TestCaseInput> testCases = objectMapper.convertValue(
                    challenge.getTestCases(),
                    new TypeReference<>() {}
            );

            // Fetch submissions
            Submission sub1 = getOrCreateDummySubmission(duelId, duel.getChallengerId(), challenge.getTimeLimitSecs());
            Submission sub2 = getOrCreateDummySubmission(duelId, duel.getOpponentId(), challenge.getTimeLimitSecs());

            // Judge both sequentially
            JudgeResponse resp1 = judgeSubmission(sub1, testCases);
            JudgeResponse resp2 = judgeSubmission(sub2, testCases);

            // Calculate scores
            calculateScores(sub1, resp1, challenge.getTimeLimitSecs());
            calculateScores(sub2, resp2, challenge.getTimeLimitSecs());

            // Save submissions
            submissionRepository.save(sub1);
            submissionRepository.save(sub2);

            // Determine winner and calculate ELO
            transactionTemplate.executeWithoutResult(status -> finalizeDuel(duel, sub1, sub2));

        } catch (Exception e) {
            log.error("Error evaluating duel {}", duelId, e);
            // If evaluation fails, we should at least mark the duel as DRAW or COMPLETED with 0 scores
            // to avoid sticking in EVALUATING.
            transactionTemplate.executeWithoutResult(status -> handleEvaluationFailure(duelId));
        }
    }

    @Transactional
    protected void handleEvaluationFailure(Long duelId) {
        duelRepository.findById(duelId).ifPresent(duel -> {
            if (duel.getStatus() == Duel.DuelStatus.EVALUATING) {
                log.warn("Force completing duel {} due to evaluation failure", duelId);
                duel.setStatus(Duel.DuelStatus.DRAW);
                duel.setEndedAt(LocalDateTime.now());
                duelRepository.save(duel);
                lifecycleService.broadcastEvent(duelId, "DUEL_COMPLETED", Map.of(
                    "winnerId", "DRAW",
                    "challengerScore", 0,
                    "opponentScore", 0,
                    "challengerEloDelta", 0,
                    "opponentEloDelta", 0,
                    "reason", "ERROR"
                ));
            }
        });
    }

    private Submission getOrCreateDummySubmission(Long duelId, Long userId, int timeLimitSecs) {
        return submissionRepository.findByDuelIdAndUserId(duelId, userId)
                .orElseGet(() -> {
                    Submission dummy = new Submission();
                    dummy.setDuelId(duelId);
                    dummy.setUserId(userId);
                    dummy.setCode("");
                    dummy.setLanguage("C");
                    dummy.setTimeTakenSecs(timeLimitSecs); // Took max time
                    dummy.setScore(0);
                    dummy.setTimedOut(true);
                    return submissionRepository.save(dummy);
                });
    }

    private JudgeResponse judgeSubmission(Submission sub, List<JudgeRequest.TestCaseInput> testCases) {
        if (sub.getCode() == null || sub.getCode().isBlank()) {
            return new JudgeResponse(false, testCases.size(), 0, 0, 0, "No code submitted", new ArrayList<>());
        }
        
        JudgeRequest req = new JudgeRequest(sub.getCode(), sub.getLanguage(), testCases);
        return judgeService.judge(req);
    }

    private void calculateScores(Submission sub, JudgeResponse resp, int timeLimitSecs) {
        if (resp.passedTests() == 0) {
            sub.setScore(0);
            sub.setCorrectnessScore(0);
            sub.setPerfScore(0);
            sub.setTimeScore(0);
            sub.setQualityScore(0);
            sub.setRuntimeMs(0L);
            return;
        }

        double correctness = ((double) resp.passedTests() / resp.totalTests()) * 100.0;
        
        // Time score: 0 to 100
        double timeRatio = Math.max(0.0, (double) (timeLimitSecs - sub.getTimeTakenSecs()) / timeLimitSecs);
        double time = timeRatio * 100.0;

        // Perf score: base 100, lose points for slow runtime
        double perf = Math.max(0.0, 100.0 - (resp.runtimeMs() / 10.0));
        
        // Quality: flat 100 for now if it compiles and passes something
        double quality = 100.0;

        // Final formula: 0.40 * Correctness + 0.30 * Perf + 0.20 * Time + 0.10 * Quality
        double finalScore = (0.40 * correctness) + (0.30 * perf) + (0.20 * time) + (0.10 * quality);

        sub.setCorrectnessScore((int) Math.round(correctness));
        sub.setPerfScore((int) Math.round(perf));
        sub.setTimeScore((int) Math.round(time));
        sub.setQualityScore((int) Math.round(quality));
        sub.setRuntimeMs(resp.runtimeMs());
        sub.setScore((int) Math.round(finalScore));
    }

    @Transactional
    protected void finalizeDuel(Duel duel, Submission sub1, Submission sub2) {
        // Re-fetch user entities inside transaction
        User challenger = userRepository.findById(duel.getChallengerId()).orElseThrow();
        User opponent = userRepository.findById(duel.getOpponentId()).orElseThrow();

        Long winnerId = null;
        if (sub1.getScore() > sub2.getScore()) {
            winnerId = challenger.getId();
            duel.setStatus(Duel.DuelStatus.COMPLETED);
        } else if (sub2.getScore() > sub1.getScore()) {
            winnerId = opponent.getId();
            duel.setStatus(Duel.DuelStatus.COMPLETED);
        } else {
            // Scores are equal. Tie-break by time.
            if (sub1.getTimeTakenSecs() < sub2.getTimeTakenSecs()) {
                winnerId = challenger.getId();
                duel.setStatus(Duel.DuelStatus.COMPLETED);
                log.info("Duel {} - Challenger won by speed tie-break (score={})", duel.getId(), sub1.getScore());
            } else if (sub2.getTimeTakenSecs() < sub1.getTimeTakenSecs()) {
                winnerId = opponent.getId();
                duel.setStatus(Duel.DuelStatus.COMPLETED);
                log.info("Duel {} - Opponent won by speed tie-break (score={})", duel.getId(), sub2.getScore());
            } else {
                // Exactly same score and same time
                duel.setStatus(Duel.DuelStatus.DRAW);
                log.info("Duel {} - Draw (score={}, time={})", duel.getId(), sub1.getScore(), sub1.getTimeTakenSecs());
            }
        }

        duel.setWinnerId(winnerId);
        duel.setEndedAt(LocalDateTime.now());

        // Calculate ELO
        int[] eloChanges = calculateEloDelta(challenger.getElo(), opponent.getElo(), winnerId, challenger.getId(), opponent.getId());
        duel.setChallengerEloChange(eloChanges[0]);
        duel.setOpponentEloChange(eloChanges[1]);
        
        duelRepository.save(duel);

        // Update Users
        updateUserStats(challenger, eloChanges[0], winnerId != null && winnerId.equals(challenger.getId()), winnerId == null);
        updateUserStats(opponent, eloChanges[1], winnerId != null && winnerId.equals(opponent.getId()), winnerId == null);
        
        // Reset status
        challenger.setStatus(User.UserStatus.ONLINE);
        opponent.setStatus(User.UserStatus.ONLINE);
        
        userRepository.save(challenger);
        userRepository.save(opponent);

        log.info("Duel {} completed. Winner: {}", duel.getId(), winnerId);

        // Broadcast completion
        lifecycleService.broadcastEvent(duel.getId(), "DUEL_COMPLETED", Map.of(
            "winnerId", winnerId != null ? winnerId : "DRAW",
            "challengerScore", sub1.getScore(),
            "opponentScore", sub2.getScore(),
            "challengerEloDelta", eloChanges[0],
            "opponentEloDelta", eloChanges[1],
            "reason", "SCORE"
        ));
    }

    private int[] calculateEloDelta(int elo1, int elo2, Long winnerId, Long p1Id, Long p2Id) {
        double k = 32.0;
        double expected1 = 1.0 / (1.0 + Math.pow(10, (elo2 - elo1) / 400.0));
        double expected2 = 1.0 / (1.0 + Math.pow(10, (elo1 - elo2) / 400.0));

        double actual1 = 0.5, actual2 = 0.5;
        if (winnerId != null) {
            if (winnerId.equals(p1Id)) {
                actual1 = 1.0;
                actual2 = 0.0;
            } else {
                actual1 = 0.0;
                actual2 = 1.0;
            }
        }

        int delta1 = (int) Math.round(k * (actual1 - expected1));
        int delta2 = (int) Math.round(k * (actual2 - expected2));

        return new int[]{delta1, delta2};
    }

    private void updateUserStats(User user, int eloDelta, boolean won, boolean draw) {
        user.setElo(Math.max(0, user.getElo() + eloDelta));
        if (won) {
            user.setWins(user.getWins() + 1);
            user.setWinStreak(user.getWinStreak() + 1);
        } else if (!draw) {
            user.setLosses(user.getLosses() + 1);
            user.setWinStreak(0);
        }
        
        // Update League
        int e = user.getElo();
        if (e >= 3000) user.setLeague(User.League.MASTER);
        else if (e >= 2000) user.setLeague(User.League.GOLD);
        else if (e >= 1000) user.setLeague(User.League.SILVER);
        else user.setLeague(User.League.BRONZE);
    }
}
