package com.codearena.code_arena_backend.challenge.service;

import com.codearena.code_arena_backend.challenge.dto.ChallengeUpsertRequest;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.NoSuchElementException;

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

    public Challenge createChallenge(ChallengeUpsertRequest request) {
        Challenge challenge = new Challenge();
        applyUpsert(challenge, request);
        return challengeRepository.save(challenge);
    }

    public Challenge updateChallenge(Long id, ChallengeUpsertRequest request) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Challenge not found: " + id));

        applyUpsert(challenge, request);
        return challengeRepository.save(challenge);
    }

    public void deleteChallenge(Long id) {
        if (!challengeRepository.existsById(id)) {
            throw new NoSuchElementException("Challenge not found: " + id);
        }
        challengeRepository.deleteById(id);
    }

    private void applyUpsert(Challenge challenge, ChallengeUpsertRequest request) {
        challenge.setTitle(request.getTitle().trim());
        challenge.setDescription(request.getDescription());
        challenge.setDifficulty(request.getDifficulty());
        challenge.setTimeLimitSecs(resolveTimeLimitFromSettings(request.getDifficulty()));
        challenge.setTestCases(request.getTestCases());
    }

    private Integer resolveTimeLimitFromSettings(ChallengeDifficulty difficulty) {
        return challengeRepository.findConfiguredTimeLimitByDifficulty(difficulty.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Missing difficulty settings for: " + difficulty.name()
                ));
    }
}
