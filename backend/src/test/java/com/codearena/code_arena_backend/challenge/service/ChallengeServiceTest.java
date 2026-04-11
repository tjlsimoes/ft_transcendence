package com.codearena.code_arena_backend.challenge.service;

import com.codearena.code_arena_backend.challenge.dto.ChallengeUpsertRequest;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService — listChallenges")
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeService challengeService;

    @Test
    @DisplayName("listChallenges without difficulty returns paginated full list")
    void listChallenges_withoutDifficulty_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(challengeRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Challenge> result = challengeService.listChallenges(null, pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(challengeRepository).findAll(pageable);
    }

    @Test
    @DisplayName("listChallenges with blank difficulty returns paginated full list")
    void listChallenges_blankDifficulty_returnsPage() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Challenge> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(challengeRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Challenge> result = challengeService.listChallenges("   ", pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(challengeRepository).findAll(pageable);
    }

    @Test
    @DisplayName("listChallenges with difficulty filters by parsed difficulty")
    void listChallenges_withDifficulty_filtersByDifficulty() {
        Pageable pageable = PageRequest.of(0, 20);
        Challenge challenge = new Challenge(1L, "Example", "Desc", ChallengeDifficulty.MEDIUM, 600, "[]");
        Page<Challenge> expectedPage = new PageImpl<>(List.of(challenge), pageable, 1);

        when(challengeRepository.findByDifficulty(ChallengeDifficulty.MEDIUM, pageable)).thenReturn(expectedPage);

        Page<Challenge> result = challengeService.listChallenges("medium", pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(challengeRepository).findByDifficulty(ChallengeDifficulty.MEDIUM, pageable);
    }

    @Test
    @DisplayName("listChallenges with invalid difficulty throws clear bad request message")
    void listChallenges_invalidDifficulty_throws() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> challengeService.listChallenges("legendary", pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid difficulty");
    }

    @Test
    @DisplayName("createChallenge sets time limit from difficulty")
    void createChallenge_setsTimeFromDifficulty() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "ft_split",
                "desc",
                ChallengeDifficulty.INSANE,
                "[]"
        );

        when(challengeRepository.save(org.mockito.ArgumentMatchers.any(Challenge.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Challenge created = challengeService.createChallenge(request);

        assertThat(created.getDifficulty()).isEqualTo(ChallengeDifficulty.INSANE);
        assertThat(created.getTimeLimitSecs()).isEqualTo(1800);
        assertThat(created.getTitle()).isEqualTo("ft_split");
        verify(challengeRepository).save(org.mockito.ArgumentMatchers.any(Challenge.class));
    }

    @Test
    @DisplayName("updateChallenge updates existing challenge and recalculates time from difficulty")
    void updateChallenge_existing_updatesChallenge() {
        Challenge existing = new Challenge(3L, "old", "old", ChallengeDifficulty.EASY, 300, "[]");
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "new-title",
                "new-desc",
                ChallengeDifficulty.HARD,
                "[{\"input\":\"x\"}]"
        );

        when(challengeRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(challengeRepository.save(existing)).thenReturn(existing);

        Challenge updated = challengeService.updateChallenge(3L, request);

        assertThat(updated.getTitle()).isEqualTo("new-title");
        assertThat(updated.getDescription()).isEqualTo("new-desc");
        assertThat(updated.getDifficulty()).isEqualTo(ChallengeDifficulty.HARD);
        assertThat(updated.getTimeLimitSecs()).isEqualTo(1200);
        assertThat(updated.getTestCases()).isEqualTo("[{\"input\":\"x\"}]");
    }

    @Test
    @DisplayName("updateChallenge missing id throws NoSuchElementException")
    void updateChallenge_missing_throwsNotFound() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "title",
                null,
                ChallengeDifficulty.MEDIUM,
                "[]"
        );

        when(challengeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> challengeService.updateChallenge(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteChallenge existing id deletes challenge")
    void deleteChallenge_existing_deletes() {
        when(challengeRepository.existsById(5L)).thenReturn(true);

        challengeService.deleteChallenge(5L);

        verify(challengeRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteChallenge missing id throws and does not delete")
    void deleteChallenge_missing_throwsNotFound() {
        when(challengeRepository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> challengeService.deleteChallenge(77L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("77");

        verify(challengeRepository, never()).deleteById(77L);
    }
}
