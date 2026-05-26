package com.codearena.code_arena_backend.duel.controller;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.duel.service.DuelSubmissionService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuelController — duel API surface")
class DuelControllerTest {

    @Mock
    private DuelSubmissionService submissionService;

    @Mock
    private DuelRepository duelRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private DuelController controller;

    private User user(Long id, String username, String displayName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(displayName);
        return user;
    }

    private Duel duel(Long id, Long challengerId, Long opponentId, Long challengeId, Duel.DuelStatus status) {
        Duel duel = new Duel();
        duel.setId(id);
        duel.setChallengerId(challengerId);
        duel.setOpponentId(opponentId);
        duel.setChallengeId(challengeId);
        duel.setStatus(status);
        duel.setStartedAt(LocalDateTime.now().minusSeconds(90));
        return duel;
    }

    private Challenge challenge(Long id, int timeLimitSecs) {
        Challenge challenge = new Challenge();
        challenge.setId(id);
        challenge.setTimeLimitSecs(timeLimitSecs);
        return challenge;
    }

    @Test
    @DisplayName("GET /api/duels/active includes EVALUATING in the active duel selection")
    void getActiveDuel_selectsEvaluatingDuel() {
        User me = user(1L, "player1", "Player One");
        User opponent = user(2L, "player2", "Rival");
        Duel activeDuel = duel(99L, 1L, 2L, 7L, Duel.DuelStatus.EVALUATING);
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(me));
        when(duelRepository.findActiveByUserId(1L, List.of(Duel.DuelStatus.MATCHED, Duel.DuelStatus.IN_PROGRESS, Duel.DuelStatus.EVALUATING)))
                .thenReturn(Optional.of(activeDuel));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));
        when(userDetails.getUsername()).thenReturn("player1");

        ResponseEntity<?> response = controller.getActiveDuel(userDetails);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("duelId")).isEqualTo(99L);
        assertThat(body.get("challengeId")).isEqualTo(7L);
        assertThat(body.get("opponentId")).isEqualTo(2L);
        assertThat(body.get("opponentName")).isEqualTo("Rival");
        assertThat(body.get("status")).isEqualTo(Duel.DuelStatus.EVALUATING);

        verify(duelRepository).findActiveByUserId(1L, List.of(Duel.DuelStatus.MATCHED, Duel.DuelStatus.IN_PROGRESS, Duel.DuelStatus.EVALUATING));
    }

    @Test
    @DisplayName("GET /api/duels/{id} returns 403 for non-participants")
    void getDuelStatus_deniesNonParticipant() {
        Duel duel = duel(42L, 1L, 2L, 7L, Duel.DuelStatus.IN_PROGRESS);
        User outsider = user(9L, "intruder", null);
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);

        when(duelRepository.findById(42L)).thenReturn(Optional.of(duel));
        when(userRepository.findByUsername("intruder")).thenReturn(Optional.of(outsider));
        when(userDetails.getUsername()).thenReturn("intruder");

        ResponseEntity<?> response = controller.getDuelStatus(42L, userDetails);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("error")).isEqualTo("You are not a participant of this duel");
        verify(challengeRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
        verify(submissionRepository, never()).findByDuelId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("GET /api/duels/{id} exposes EVALUATING state with zero remaining time")
    void getDuelStatus_handlesEvaluatingState() {
        Duel duel = duel(42L, 1L, 2L, 7L, Duel.DuelStatus.EVALUATING);
        Challenge challenge = challenge(7L, 600);
        User me = user(1L, "player1", null);
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        Submission mySubmission = new Submission();
        mySubmission.setUserId(1L);
        Submission opponentSubmission = new Submission();
        opponentSubmission.setUserId(2L);

        when(duelRepository.findById(42L)).thenReturn(Optional.of(duel));
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(me));
        when(userDetails.getUsername()).thenReturn("player1");
        when(challengeRepository.findById(7L)).thenReturn(Optional.of(challenge));
        when(submissionRepository.findByDuelIdAndUserId(42L, 1L)).thenReturn(Optional.of(mySubmission));
        when(submissionRepository.findByDuelId(42L)).thenReturn(List.of(mySubmission, opponentSubmission));

        ResponseEntity<?> response = controller.getDuelStatus(42L, userDetails);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("status")).isEqualTo(Duel.DuelStatus.EVALUATING);
        assertThat(body.get("timeLeftSecs")).isEqualTo(0L);
        assertThat(body.get("hasSubmitted")).isEqualTo(true);
        assertThat(body.get("opponentHasSubmitted")).isEqualTo(true);
    }
}
