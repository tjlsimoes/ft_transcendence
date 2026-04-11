package com.codearena.code_arena_backend.challenge.controller;

import com.codearena.code_arena_backend.challenge.dto.ChallengeListItemResponse;
import com.codearena.code_arena_backend.challenge.dto.ChallengeUpsertRequest;
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
import java.util.NoSuchElementException;

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

    @Test
    @DisplayName("createChallenge returns HTTP 201 and admin payload")
    void createChallenge_returns201() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "new",
                "desc",
                ChallengeDifficulty.HARD,
                "[]"
        );
        Challenge created = new Challenge(11L, "new", "desc", ChallengeDifficulty.HARD, 1200, "[]");
        when(challengeService.createChallenge(request)).thenReturn(created);

        ResponseEntity<com.codearena.code_arena_backend.challenge.dto.ChallengeAdminResponse> response =
                challengeController.createChallenge(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(11L);
        assertThat(response.getBody().difficulty()).isEqualTo(ChallengeDifficulty.HARD);
    }

    @Test
    @DisplayName("updateChallenge returns HTTP 200")
    void updateChallenge_returns200() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "updated",
                "desc",
                ChallengeDifficulty.MEDIUM,
                "[]"
        );
        Challenge updated = new Challenge(9L, "updated", "desc", ChallengeDifficulty.MEDIUM, 600, "[]");
        when(challengeService.updateChallenge(9L, request)).thenReturn(updated);

        ResponseEntity<com.codearena.code_arena_backend.challenge.dto.ChallengeAdminResponse> response =
                challengeController.updateChallenge(9L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(9L);
        assertThat(response.getBody().timeLimitSecs()).isEqualTo(600);
    }

    @Test
    @DisplayName("deleteChallenge returns HTTP 204")
    void deleteChallenge_returns204() {
        ResponseEntity<Void> response = challengeController.deleteChallenge(4L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(challengeService).deleteChallenge(4L);
    }

    @Test
    @DisplayName("handleNotFound returns HTTP 404 with error payload")
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, String>> response =
                challengeController.handleNotFound(new NoSuchElementException("Challenge not found: 13"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).contains("13");
    }
}
