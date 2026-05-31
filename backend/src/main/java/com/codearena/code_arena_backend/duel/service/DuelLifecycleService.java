package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuelLifecycleService {

    private final DuelRepository duelRepository;
    private final ChallengeRepository challengeRepository;
    private final SubmissionRepository submissionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Inject Lazy to avoid circular dependency
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private DuelEvaluationService evaluationService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<Long, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    @Transactional
    public void startDuel(Long duelId) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found: " + duelId));

        if (duel.getStatus() != Duel.DuelStatus.MATCHED) {
            log.warn("Attempted to start duel {} but status is {}", duelId, duel.getStatus());
            return;
        }

        duel.setStatus(Duel.DuelStatus.IN_PROGRESS);
        duel.setStartedAt(LocalDateTime.now());
        duelRepository.save(duel);

        long timeLeftSecs = getCurrentTimeLeftSecs(duel);

        log.info("Started Duel #{} with time limit {}s", duelId, timeLeftSecs);

        broadcastEvent(duelId, "DUEL_STARTED", Map.of(
            "timeLeftSecs", timeLeftSecs,
            "status", duel.getStatus()
        ));

        // Start timer task
        scheduleTimer(duelId);
    }

    private void scheduleTimer(Long duelId) {
        Runnable tickTask = () -> {
            long current = getCurrentTimeLeftSecs(duelId);

            if (current <= 0) {
                // Time's up!
                log.info("Duel #{} timer expired", duelId);
                broadcastEvent(duelId, "DUEL_TIMEOUT", Map.of("timeLeftSecs", 0));

                // Stop the timer
                cancelTimer(duelId);

                // Trigger evaluation
                evaluationService.evaluateDuel(duelId);
            } else {
                // Tick
                broadcastEvent(duelId, "DUEL_TICK", Map.of("timeLeftSecs", current));
            }
        };

        // Run every second so all clients stay aligned with the server clock.
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(tickTask, 1, 1, TimeUnit.SECONDS);
        activeTimers.put(duelId, future);
    }

    public void reduceTimeLimitTo(Long duelId, int maxSeconds) {
        long current = getCurrentTimeLeftSecs(duelId);
        if (current > maxSeconds) {
            log.info("Duel #{} time reduced to {}s", duelId, maxSeconds);
            broadcastEvent(duelId, "DUEL_TIME_REDUCED", Map.of("timeLeftSecs", maxSeconds));
        }
    }

    public void cancelTimer(Long duelId) {
        ScheduledFuture<?> future = activeTimers.remove(duelId);
        if (future != null) {
            future.cancel(false);
        }
    }

    public long getRemainingTime(Long duelId) {
        return duelRepository.findById(duelId)
                .map(this::getCurrentTimeLeftSecs)
                .orElse(0L);
    }

    private long getCurrentTimeLeftSecs(Long duelId) {
        return duelRepository.findById(duelId)
                .map(this::getCurrentTimeLeftSecs)
                .orElse(0L);
    }

    private long getCurrentTimeLeftSecs(Duel duel) {
        if (duel.getStartedAt() == null || duel.getStatus() != Duel.DuelStatus.IN_PROGRESS) {
            return 0L;
        }

        Challenge challenge = challengeRepository.findById(duel.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + duel.getChallengeId()));

        long elapsedSinceStart = Duration.between(duel.getStartedAt(), LocalDateTime.now()).getSeconds();
        long standardTimeLeft = Math.max(0L, challenge.getTimeLimitSecs() - elapsedSinceStart);

        long reducedTimeLeft = standardTimeLeft;
        var submissions = submissionRepository.findByDuelId(duel.getId());
        if (submissions.size() == 1) {
            long elapsedSinceSubmission = Duration.between(submissions.get(0).getSubmittedAt(), LocalDateTime.now()).getSeconds();
            reducedTimeLeft = Math.max(0L, 60L - elapsedSinceSubmission);
        }

        return Math.min(standardTimeLeft, reducedTimeLeft);
    }

    public void broadcastEvent(Long duelId, String type, Map<String, Object> payload) {
        Map<String, Object> message = new java.util.HashMap<>(payload);
        message.put("type", type);
        message.put("duelId", duelId);
        message.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/duel/" + duelId, (Object) message);
    }
}
