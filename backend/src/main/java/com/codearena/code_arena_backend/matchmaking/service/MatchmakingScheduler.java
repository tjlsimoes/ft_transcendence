package com.codearena.code_arena_backend.matchmaking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs every 5 seconds to scan the matchmaking queue and pair players.
 *
 * Pairing rules:
 *   - Base Elo window: ±200
 *   - Window expands by +50 every 10 seconds a player has been waiting
 *   - Picks the closest-Elo candidate within the expanded window
 *   - Timeout after 60 seconds: player is dequeued and notified
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchmakingScheduler {

    private static final int BASE_ELO_WINDOW = 200;
    private static final int WINDOW_EXPANSION = 50;
    private static final int EXPANSION_INTERVAL_SECONDS = 10;
    private static final int TIMEOUT_SECONDS = 60;

    private final MatchmakingQueueService queueService;
    private final MatchmakingService matchmakingService;

    @Scheduled(fixedRate = 5000)
    public void scanAndPair() {
        Set<String> members = queueService.getAllQueuedMembers();
        if (members.size() < 2) {
            return;
        }

        long now = System.currentTimeMillis();
        Set<Long> matchedThisCycle = new HashSet<>();

        for (String member : members) {
            Long playerId = MatchmakingQueueService.extractUserId(member);

            // Skip if already matched this cycle or no longer in queue
            if (matchedThisCycle.contains(playerId) || !queueService.isQueued(playerId)) {
                continue;
            }

            long enqueuedAt = queueService.getEnqueueTimestamp(playerId);
            long waitingSeconds = (now - enqueuedAt) / 1000;

            // Timeout check
            if (waitingSeconds >= TIMEOUT_SECONDS) {
                log.info("Player {} timed out after {}s", playerId, waitingSeconds);
                matchmakingService.handleTimeout(playerId);
                continue;
            }

            // Calculate expanded window
            int playerElo = queueService.getQueuedElo(playerId);
            int expansion = WINDOW_EXPANSION * (int) (waitingSeconds / EXPANSION_INTERVAL_SECONDS);
            int window = BASE_ELO_WINDOW + expansion;

            // Find candidates within Elo window
            Set<String> candidates = queueService.findPlayersInEloRange(
                    playerElo - window, playerElo + window);

            // Find best match (closest Elo, not self, not already matched)
            Long bestMatchId = null;
            int bestEloDiff = Integer.MAX_VALUE;

            for (String candidateMember : candidates) {
                Long candidateId = MatchmakingQueueService.extractUserId(candidateMember);

                if (candidateId.equals(playerId) || matchedThisCycle.contains(candidateId)) {
                    continue;
                }

                int candidateElo = queueService.getQueuedElo(candidateId);
                int diff = Math.abs(playerElo - candidateElo);

                if (diff < bestEloDiff) {
                    bestEloDiff = diff;
                    bestMatchId = candidateId;
                }
            }

            if (bestMatchId != null) {
                log.info("Pairing player {} (elo={}) with player {} (eloDiff={})",
                        playerId, playerElo, bestMatchId, bestEloDiff);

                try {
                    matchmakingService.createMatch(playerId, bestMatchId);
                    matchedThisCycle.add(playerId);
                    matchedThisCycle.add(bestMatchId);
                } catch (Exception e) {
                    log.error("Failed to create match for {} vs {}: {}",
                            playerId, bestMatchId, e.getMessage(), e);
                }
            }
        }
    }
}
