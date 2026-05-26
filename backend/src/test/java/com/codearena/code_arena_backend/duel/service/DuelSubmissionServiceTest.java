package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuelSubmissionService — submit flow")
class DuelSubmissionServiceTest {

    @Mock
    private DuelRepository duelRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DuelEvaluationService evaluationService;

    @Mock
    private DuelLifecycleService lifecycleService;

    @InjectMocks
    private DuelSubmissionService submissionService;

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Duel duel(Long id) {
        Duel duel = new Duel();
        duel.setId(id);
        duel.setChallengerId(1L);
        duel.setOpponentId(2L);
        duel.setChallengeId(7L);
        duel.setStatus(Duel.DuelStatus.IN_PROGRESS);
        duel.setStartedAt(LocalDateTime.now().minusSeconds(15));
        return duel;
    }

    @Test
    @DisplayName("first submission reduces timer and notifies the opponent")
    void submitCode_firstSubmission_notifiesOpponent() {
        User me = user(1L, "player1");
        Duel duel = duel(10L);

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(me));
        when(duelRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(duel));
        when(submissionRepository.findByDuelIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(submissionRepository.save(org.mockito.ArgumentMatchers.any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(submissionRepository.countByDuelId(10L)).thenReturn(1L);

        submissionService.submitCode(10L, "player1", "int main() {}", "C");

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getDuelId()).isEqualTo(10L);
        assertThat(captor.getValue().getTimeTakenSecs()).isGreaterThanOrEqualTo(0);
        verify(lifecycleService).reduceTimeLimitTo(10L, 60);
        verify(lifecycleService).broadcastEvent(10L, "DUEL_OPPONENT_FINISHED", java.util.Map.of("username", "player1"));
        verify(evaluationService, never()).evaluateDuel(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("second submission triggers duel evaluation")
    void submitCode_secondSubmission_triggersEvaluation() {
        User me = user(2L, "player2");
        Duel duel = duel(10L);

        when(userRepository.findByUsername("player2")).thenReturn(Optional.of(me));
        when(duelRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(duel));
        when(submissionRepository.findByDuelIdAndUserId(10L, 2L)).thenReturn(Optional.empty());
        when(submissionRepository.save(org.mockito.ArgumentMatchers.any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(submissionRepository.countByDuelId(10L)).thenReturn(2L);

        submissionService.submitCode(10L, "player2", "int main() {}", "C");

        verify(evaluationService).evaluateDuel(10L);
        verify(lifecycleService, never()).broadcastEvent(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq("DUEL_OPPONENT_FINISHED"), org.mockito.ArgumentMatchers.anyMap());
        verify(lifecycleService, never()).reduceTimeLimitTo(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("duplicate submission is rejected before evaluation")
    void submitCode_duplicateSubmission_rejected() {
        User me = user(1L, "player1");
        Duel duel = duel(10L);

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(me));
        when(duelRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(duel));
        when(submissionRepository.findByDuelIdAndUserId(10L, 1L)).thenReturn(Optional.of(new Submission()));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> submissionService.submitCode(10L, "player1", "int main() {}", "C"));

        assertThat(exception.getMessage()).contains("already submitted");
        verify(submissionRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(evaluationService, never()).evaluateDuel(org.mockito.ArgumentMatchers.anyLong());
    }
}
