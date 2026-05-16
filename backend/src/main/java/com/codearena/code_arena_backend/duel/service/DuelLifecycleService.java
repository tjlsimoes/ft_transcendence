package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Challenge challenge = challengeRepository.findById(duel.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + duel.getChallengeId()));

        int timeLimitSecs = challenge.getTimeLimitSecs();

        log.info("Started Duel #{} with time limit {}s", duelId, timeLimitSecs);

        broadcastEvent(duelId, "DUEL_STARTED", Map.of(
            "timeLimitSecs", timeLimitSecs,
            "status", duel.getStatus()
        ));

        // Start timer task
        scheduleTimer(duelId, timeLimitSecs);
    }

    private void scheduleTimer(Long duelId, int timeLimitSecs) {
        // We need a mutable reference for remaining time
        int[] remaining = { timeLimitSecs };

        Runnable tickTask = () -> {
            remaining[0] -= 10;
            
            if (remaining[0] <= 0) {
                // Time's up!
                log.info("Duel #{} timer expired", duelId);
                broadcastEvent(duelId, "DUEL_TIMEOUT", Map.of("timeLeftSecs", 0));
                
                // Stop the timer
                cancelTimer(duelId);
                
                // Trigger evaluation
                evaluationService.evaluateDuel(duelId);
            } else {
                // Tick
                broadcastEvent(duelId, "DUEL_TICK", Map.of("timeLeftSecs", remaining[0]));
            }
        };

        // Run every 10 seconds, starting after 10 seconds
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(tickTask, 10, 10, TimeUnit.SECONDS);
        activeTimers.put(duelId, future);
    }

    public void cancelTimer(Long duelId) {
        ScheduledFuture<?> future = activeTimers.remove(duelId);
        if (future != null) {
            future.cancel(false);
        }
    }

    public void broadcastEvent(Long duelId, String type, Map<String, Object> payload) {
        Map<String, Object> message = new java.util.HashMap<>(payload);
        message.put("type", type);
        message.put("duelId", duelId);
        message.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/duel/" + duelId, (Object) message);
    }
}
