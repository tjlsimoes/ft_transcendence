package com.codearena.code_arena_backend.ranking.controller;

import com.codearena.code_arena_backend.ranking.dto.LeaderboardEntryResponse;
import com.codearena.code_arena_backend.ranking.util.LeagueUtils;
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

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final UserRepository userRepository;

    /**
     * GET /api/leaderboard?limit=50
     * Returns the top players sorted by ELO descending.
     *
     * League is computed on-the-fly from rank so that LEGEND/MASTER distinction
     * is always accurate — no bulk UPDATE required on login.
     * Legend = top 1% of ALL players AND elo >= 3000.
     */
    @GetMapping
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {

        int safeLimit = Math.min(Math.max(1, limit), MAX_LIMIT);
        List<User> players = userRepository.findTopPlayersByElo(safeLimit);

        // One COUNT query to determine the legend cutoff. Legend = top 1% of all players.
        long totalPlayers = userRepository.countAllPlayers();
        long legendCutoff = Math.max(1, (long) Math.ceil(totalPlayers * 0.01));

        List<LeaderboardEntryResponse> response = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            int rank = i + 1;
            User user = players.get(i);
            String league = LeagueUtils.computeLeague(user.getElo(), rank, legendCutoff);
            response.add(LeaderboardEntryResponse.fromUser(rank, user, league));
        }

        return ResponseEntity.ok(response);
    }
}
