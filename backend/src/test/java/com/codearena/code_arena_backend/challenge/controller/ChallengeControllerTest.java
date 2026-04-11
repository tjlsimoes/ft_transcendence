package com.codearena.code_arena_backend.challenge.controller;

import com.codearena.code_arena_backend.challenge.dto.ChallengeListItemResponse;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.service.ChallengeService;
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
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeController — GET /api/challenges")
class ChallengeControllerTest {

    @Mock
    private ChallengeService challengeService;

    @InjectMocks
    private ChallengeController challengeController;

    @Test
    @DisplayName("listChallenges maps service page to response DTO page")
    void listChallenges_mapsToDtoPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Challenge challenge = new Challenge(7L, "ft_atoi", "desc", ChallengeDifficulty.MEDIUM, 600, "[]");
        Page<Challenge> servicePage = new PageImpl<>(List.of(challenge), pageable, 1);
        when(challengeService.listChallenges("MEDIUM", pageable)).thenReturn(servicePage);

        ResponseEntity<Page<ChallengeListItemResponse>> response =
                challengeController.listChallenges("MEDIUM", pageable);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        ChallengeListItemResponse item = response.getBody().getContent().getFirst();
        assertThat(item.id()).isEqualTo(7L);
        assertThat(item.title()).isEqualTo("ft_atoi");
        assertThat(item.difficulty()).isEqualTo(ChallengeDifficulty.MEDIUM);
        assertThat(item.timeLimitSecs()).isEqualTo(600);

        verify(challengeService).listChallenges("MEDIUM", pageable);
    }

    @Test
    @DisplayName("handleBadRequest returns HTTP 400 with error body")
    void handleBadRequest_returns400() {
        ResponseEntity<Map<String, String>> response =
                challengeController.handleBadRequest(new IllegalArgumentException("Invalid difficulty"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).contains("Invalid difficulty");
    }
}
