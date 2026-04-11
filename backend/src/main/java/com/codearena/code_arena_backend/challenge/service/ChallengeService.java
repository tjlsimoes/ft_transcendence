package com.codearena.code_arena_backend.challenge.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public Page<Challenge> listChallenges(String difficulty, Pageable pageable) {
        if (difficulty == null || difficulty.isBlank()) {
            return challengeRepository.findAll(pageable);
        }

        ChallengeDifficulty parsedDifficulty;
        try {
            parsedDifficulty = ChallengeDifficulty.valueOf(difficulty.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid difficulty. Allowed values: EASY, MEDIUM, HARD, INSANE");
        }

        return challengeRepository.findByDifficulty(parsedDifficulty, pageable);
    }
}
