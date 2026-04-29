package com.codearena.code_arena_backend.matchmaking.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.matchmaking.dto.MatchmakingEvent;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * Orchestrates match creation after the scheduler finds a valid pair.
 *
 * Responsibilities:
 *   1. Dequeue both players from Redis
 *   2. Pick a random challenge based on the pair's average Elo
 *   3. Create and persist a Duel record
 *   4. Update both players' status to IN_DUEL
 *   5. Notify both players via WebSocket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final MatchmakingQueueService queueService;
    private final DuelRepository duelRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a match between two players.
     * Called by the scheduler when a valid Elo pair is found.
     */
    @Transactional
    public void createMatch(Long player1Id, Long player2Id) {
        User player1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + player1Id));
        User player2 = userRepository.findById(player2Id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + player2Id));

        // 1. Dequeue both players first to avoid duplicate matches while we prepare the duel
        queueService.dequeue(player1Id);
        queueService.dequeue(player2Id);

        // Prepare and create the duel. If anything fails after dequeueing we must
        // compensatingly re-enqueue both players and notify them so they aren't
        // silently removed from matchmaking.
        try {
            // 2. Pick challenge by difficulty (mapped from average Elo)
            int avgElo = (player1.getElo() + player2.getElo()) / 2;
            ChallengeDifficulty difficulty = mapEloToDifficulty(avgElo);
            Challenge challenge = challengeRepository
                .findRandomByDifficulty(difficulty.name())
                .orElseThrow(() -> new NoSuchElementException(
                    "No challenge available for difficulty: " + difficulty));

        // 3. Create Duel record
        Duel duel = new Duel();
        duel.setChallengerId(player1Id);
        duel.setOpponentId(player2Id);
        duel.setChallengeId(challenge.getId());
        duel.setStatus(Duel.DuelStatus.MATCHED);
        duel.setStartedAt(LocalDateTime.now());
        duel = duelRepository.save(duel);

        // 4. Update player statuses
        player1.setStatus(User.UserStatus.IN_DUEL);
        player2.setStatus(User.UserStatus.IN_DUEL);
        userRepository.save(player1);
        userRepository.save(player2);

        log.info("Match created: Duel #{} — {} vs {} (challenge={}, difficulty={})",
                duel.getId(), player1.getUsername(), player2.getUsername(),
                challenge.getId(), difficulty);

        // 5. Notify both players via WebSocket
        MatchmakingEvent event1 = MatchmakingEvent.matched(
                duel.getId(), player2Id, player2.getDisplayName(), challenge.getId());
        MatchmakingEvent event2 = MatchmakingEvent.matched(
                duel.getId(), player1Id, player1.getDisplayName(), challenge.getId());

            messagingTemplate.convertAndSend("/topic/matchmaking/" + player1Id, event1);
            messagingTemplate.convertAndSend("/topic/matchmaking/" + player2Id, event2);
        } catch (Exception e) {
            // Attempt to put players back into the queue and restore their status.
            try {
                queueService.enqueue(player1Id, player1.getElo());
            } catch (Exception ex) {
                log.warn("Failed to re-enqueue player {} after match creation error", player1Id, ex);
            }
            try {
                queueService.enqueue(player2Id, player2.getElo());
            } catch (Exception ex) {
                log.warn("Failed to re-enqueue player {} after match creation error", player2Id, ex);
            }

            // Ensure user statuses are back to ONLINE
            userRepository.findById(player1Id).ifPresent(u -> {
                u.setStatus(User.UserStatus.ONLINE);
                userRepository.save(u);
            });
            userRepository.findById(player2Id).ifPresent(u -> {
                u.setStatus(User.UserStatus.ONLINE);
                userRepository.save(u);
            });

            // Notify players they are back in queue
            messagingTemplate.convertAndSend(
                    "/topic/matchmaking/" + player1Id, MatchmakingEvent.queued());
            messagingTemplate.convertAndSend(
                    "/topic/matchmaking/" + player2Id, MatchmakingEvent.queued());

            log.error("Failed to create match between {} and {} — players re-enqueued", player1Id, player2Id, e);
            throw e;
        }
    }

    /**
     * Cancels a player's queue entry.
     */
    @Transactional
    public void cancelQueue(Long userId) {
        boolean removed = queueService.dequeue(userId);
        if (!removed) {
            throw new IllegalStateException("Player " + userId + " is not in the queue");
        }

        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(User.UserStatus.ONLINE);
            userRepository.save(user);
        });

        messagingTemplate.convertAndSend(
                "/topic/matchmaking/" + userId, MatchmakingEvent.cancelled());

        log.info("Player {} cancelled queue", userId);
    }

    /**
     * Notifies a player of a queue timeout and resets their status.
     */
    @Transactional
    public void handleTimeout(Long userId) {
        queueService.dequeue(userId);

        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(User.UserStatus.ONLINE);
            userRepository.save(user);
        });

        messagingTemplate.convertAndSend(
                "/topic/matchmaking/" + userId, MatchmakingEvent.timeout());

        log.info("Player {} timed out from queue", userId);
    }

    /**
     * Maps an Elo value to a challenge difficulty.
     *
     * Bronze  (0–999)   → EASY
     * Silver  (1000–1999) → MEDIUM
     * Gold    (2000–2999) → HARD
     * Master+ (3000+)    → INSANE
     */
    static ChallengeDifficulty mapEloToDifficulty(int elo) {
        if (elo < 1000) return ChallengeDifficulty.EASY;
        if (elo < 2000) return ChallengeDifficulty.MEDIUM;
        if (elo < 3000) return ChallengeDifficulty.HARD;
        return ChallengeDifficulty.INSANE;
    }
}
