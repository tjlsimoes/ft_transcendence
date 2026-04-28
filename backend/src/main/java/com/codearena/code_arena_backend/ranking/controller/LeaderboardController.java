package com.codearena.code_arena_backend.ranking.controller;

import com.codearena.code_arena_backend.ranking.dto.LeaderboardEntryResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint returning the top-N players ordered by ELO.
 * No authentication required — the leaderboard is visible to everyone.
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final UserRepository userRepository;

    /**
     * GET /api/leaderboard?page=X&size=Y&sort=Z
     * Returns the top players sorted by ELO descending.
     * Rank is assigned sequentially (1-based).
     */
    @GetMapping
    public ResponseEntity<Page<LeaderboardEntryResponse>> getLeaderboard
        (
            @RequestParam(required = false) User.League league,
            @PageableDefault(size = 50) Pageable pageable
        ) {

        Page<User> playersPage;
        if (league != null) {
            playersPage = userRepository.findByLeagueOrderByEloDesc(league, pageable);
        } else {
            playersPage = userRepository.findAllByOrderByEloDesc(pageable);
        }

        long offset = pageable.getOffset();

        // We use map to convert each User into a LeaderboardEntryResponse.
        // To assign sequential ranks, we can't just use a simple stream without tracking index,
        // so we can use a local counter array.
        int[] rankCounter = new int[] { (int) offset + 1 };

        Page<LeaderboardEntryResponse> response = playersPage.map(user ->
            LeaderboardEntryResponse.fromUser(rankCounter[0]++, user)
        );

        return ResponseEntity.ok(response);
    }

    // Why the weird int[] rankCounter array?
    // We use .map(user -> ...) to transform the entities into DTOs.
    // In Java, variables used inside a lambda function (like ->) must be "effectively final"
    // (meaning their value cannot change).
    // If we used int rankCounter = 51; and tried to do rankCounter++ inside the lambda,
    // Java would throw a compile error.
    // By putting it inside an array int[] rankCounter = new int[]{ 51 };
    // the array reference is final, but we are allowed to change the contents
    // inside the array (rankCounter[0]++).
}
