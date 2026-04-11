package com.codearena.code_arena_backend.challenge.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
}
