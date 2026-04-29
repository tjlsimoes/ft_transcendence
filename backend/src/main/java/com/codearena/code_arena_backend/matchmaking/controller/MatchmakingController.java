package com.codearena.code_arena_backend.matchmaking.controller;

import com.codearena.code_arena_backend.matchmaking.dto.MatchmakingEvent;
import com.codearena.code_arena_backend.matchmaking.service.MatchmakingQueueService;
import com.codearena.code_arena_backend.matchmaking.service.MatchmakingService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST API for matchmaking queue management.
 *
 * POST   /api/matchmaking/queue        — join the ranked queue
 * DELETE /api/matchmaking/queue        — leave the queue
 * GET    /api/matchmaking/queue/status — check queue status
 */
@RestController
@RequestMapping("/api/matchmaking")
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingQueueService queueService;
    private final MatchmakingService matchmakingService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * POST /api/matchmaking/queue
     * Adds the authenticated player to the matchmaking queue.
     * Idempotent — returns 200 even if already queued.
     */
    @PostMapping("/queue")
    public ResponseEntity<MatchmakingEvent> enqueue(Principal principal) {

        User user = findUser(principal.getName());

        if (user.getStatus() == User.UserStatus.IN_DUEL) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MatchmakingEvent("ERROR", null, null, null, null,
                            "Cannot queue while in a duel."));
        }

        boolean newlyQueued = queueService.enqueue(user.getId(), user.getElo());

        if (newlyQueued) {
            user.setStatus(User.UserStatus.IN_QUEUE);
            userRepository.save(user);

            MatchmakingEvent event = MatchmakingEvent.queued();
            try {
                messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/matchmaking", event);
            } catch (Exception e) {
                // Fall back to logging if notification fails
                // (user will still be enqueued)
                // No need to expose transport failures to client here.

            }
            return ResponseEntity.ok(event);
        }

        // Already queued — return current status (idempotent)
        return ResponseEntity.ok(new MatchmakingEvent("QUEUED", null, null, null, null,
                "You are already in the matchmaking queue."));
    }

    /**
     * DELETE /api/matchmaking/queue
     * Removes the authenticated player from the queue.
     */
    @DeleteMapping("/queue")
    public ResponseEntity<Void> cancelQueue(Principal principal) {

        User user = findUser(principal.getName());

        try {
            matchmakingService.cancelQueue(user.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/matchmaking/queue/status
     * Returns whether the authenticated player is currently in the queue.
     */
    @GetMapping("/queue/status")
    public ResponseEntity<Map<String, Object>> queueStatus(Principal principal) {

        User user = findUser(principal.getName());
        boolean queued = queueService.isQueued(user.getId());

        Map<String, Object> body = Map.of(
                "queued", queued,
                "elo", user.getElo(),
                "status", user.getStatus().name()
        );

        return ResponseEntity.ok(body);
    }

    // ------------------------------------------------------------------ //
    //  Exception handlers                                                 //
    // ------------------------------------------------------------------ //

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                            //
    // ------------------------------------------------------------------ //

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }
}
