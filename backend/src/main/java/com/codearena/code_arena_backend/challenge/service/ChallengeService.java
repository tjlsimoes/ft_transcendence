package com.codearena.code_arena_backend.challenge.service;

import com.codearena.code_arena_backend.challenge.dto.ChallengeUpsertRequest;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.service.JudgeService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final JudgeService judgeService;

    private static final int RUN_CODE_SAMPLE_TESTS = 3;
    private static final int MAX_CODE_SIZE_CHARS = 100_000;

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

    public Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Challenge not found: " + id));
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

    public JudgeResponse runCode(Long challengeId, String code, String requestedLanguage) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        if (code.length() > MAX_CODE_SIZE_CHARS) {
            throw new IllegalArgumentException("Code is too large");
        }

        String normalizedLanguage = normalizeLanguage(requestedLanguage);
        if (!"c".equals(normalizedLanguage)) {
            throw new IllegalArgumentException("Only C language is supported for RUN CODE");
        }

        Challenge challenge = getChallenge(challengeId);
        List<JudgeRequest.TestCaseInput> sampleTests = extractSampleTests(challenge.getTestCases());

        String finalCode = (challenge.getCodeWrapper() != null && !challenge.getCodeWrapper().isBlank())
                ? code + "\n" + challenge.getCodeWrapper()
                : code;

        JudgeRequest judgeRequest = new JudgeRequest(finalCode, normalizedLanguage, sampleTests);
        return judgeService.judge(judgeRequest);
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

    private String normalizeLanguage(String requestedLanguage) {
        if (requestedLanguage == null || requestedLanguage.isBlank()) {
            return "c";
        }
        return requestedLanguage.trim().toLowerCase(Locale.ROOT);
    }

    private List<JudgeRequest.TestCaseInput> extractSampleTests(JsonNode rawTestCases) {
        if (rawTestCases == null || !rawTestCases.isArray()) {
            throw new IllegalStateException("Challenge test cases are not configured correctly");
        }
        if (rawTestCases.size() < RUN_CODE_SAMPLE_TESTS) {
            throw new IllegalStateException("Challenge must have at least 3 sample test cases");
        }

        List<JudgeRequest.TestCaseInput> sampleTests = new ArrayList<>(RUN_CODE_SAMPLE_TESTS);
        for (int i = 0; i < RUN_CODE_SAMPLE_TESTS; i++) {
            JsonNode tc = rawTestCases.get(i);

            String stdin = textOrNull(tc, "stdin", "input");
            String expected = textOrNull(tc, "expected_output", "expectedOutput", "output");
            if (expected == null) {
                throw new IllegalStateException("Sample test case at index " + i + " is missing expected output");
            }

            sampleTests.add(new JudgeRequest.TestCaseInput(stdin == null ? "" : stdin, expected));
        }

        return sampleTests;
    }

    private String textOrNull(JsonNode node, String... fieldNames) {
        if (node == null || !node.isObject()) {
            return null;
        }

        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && !value.isNull()) {
                String text = value.asText();
                if (text != null) {
                    return text;
                }
            }
        }

        return null;
    }
}
