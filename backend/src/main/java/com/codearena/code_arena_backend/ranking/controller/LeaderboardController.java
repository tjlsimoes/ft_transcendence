package com.codearena.code_arena_backend.ranking.controller;

import com.codearena.code_arena_backend.ranking.dto.LeaderboardEntryResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Public endpoint returning the top-N players ordered by ELO.
 * No authentication required — the leaderboard is visible to everyone.
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private static final String DEFAULT_LIMIT = "50";
    private static final int MAX_LIMIT = 100;

    private final UserRepository userRepository;

    /**
     * GET /api/leaderboard?limit=50
     * Returns the top players sorted by ELO descending.
     * Rank is assigned sequentially (1-based).
     */
    @GetMapping
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
            @RequestParam(required = false) String league,
            @RequestParam(defaultValue = DEFAULT_LIMIT) int limit) {

        int safeLimit = Math.min(Math.max(1, limit), MAX_LIMIT);
        List<User> players;
        if (league != null) {
            players = userRepository.findTopPlayersLeagueAndEloDesc(league.toUpperCase(), safeLimit);
        } else {
            players = userRepository.findTopPlayersByElo(safeLimit);
        }

        List<LeaderboardEntryResponse> response = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            response.add(LeaderboardEntryResponse.fromUser(i + 1, players.get(i)));
        }

        return ResponseEntity.ok(response);
    }
}
