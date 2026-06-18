package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.judge.dto.JudgeRequest;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.service.JudgeService;
import com.codearena.code_arena_backend.submission.entity.Submission;
import com.codearena.code_arena_backend.submission.repository.SubmissionRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused") // eq() kept for verify() call in handleEvaluationFailure tests

@ExtendWith(MockitoExtension.class)
@DisplayName("DuelEvaluationService")
class DuelEvaluationServiceTest {

    @Mock private DuelRepository duelRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;
    @Mock private JudgeService judgeService;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private DuelLifecycleService lifecycleService;

    @InjectMocks
    private DuelEvaluationService evaluationService;

    @BeforeEach
    void injectLazyField() {
        // lifecycleService is @Autowired @Lazy (not final), ensure it is set
        ReflectionTestUtils.setField(evaluationService, "lifecycleService", lifecycleService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Duel buildDuel(Long id, Long challengerId, Long opponentId,
                           Long challengeId, Duel.DuelStatus status) {
        Duel duel = new Duel();
        duel.setId(id);
        duel.setChallengerId(challengerId);
        duel.setOpponentId(opponentId);
        duel.setChallengeId(challengeId);
        duel.setStatus(status);
        return duel;
    }

    private User buildUser(Long id, int elo) {
        User u = new User();
        u.setId(id);
        u.setElo(elo);
        u.setWins(0);
        u.setLosses(0);
        u.setWinStreak(0);
        return u;
    }

    private Submission buildSubmission(Long duelId, Long userId,
                                       String code, int score, int timeTakenSecs) {
        Submission s = new Submission();
        s.setDuelId(duelId);
        s.setUserId(userId);
        s.setCode(code);
        s.setLanguage("C");
        s.setScore(score);
        s.setTimeTakenSecs(timeTakenSecs);
        return s;
    }

    private Challenge buildChallenge(Long id, int timeLimitSecs, String harness) throws Exception {
        Challenge c = new Challenge();
        c.setId(id);
        c.setTimeLimitSecs(timeLimitSecs);
        c.setTestHarness(harness);
        JsonNode testCasesNode = new ObjectMapper()
                .readTree("[{\"input\":\"in\",\"expected_output\":\"out\"}]");
        c.setTestCases(testCasesNode);
        return c;
    }

    /** Mocks transactionTemplate to execute its consumer synchronously. */
    @SuppressWarnings("unchecked")
    private void executeTransactionSynchronously() {
        doAnswer(inv -> {
            ((Consumer<org.springframework.transaction.TransactionStatus>)
                    inv.getArgument(0)).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    /** Returns a single-test passing JudgeResponse. */
    private JudgeResponse passingResponse() {
        return new JudgeResponse(true, 1, 1, 50L, 1024L, null,
                List.of(new JudgeResponse.TestCaseResult(0, true, "out", "out", null, 50L)));
    }

    // -------------------------------------------------------------------------
    // finalizeDuel — winner determination
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("finalizeDuel: challenger wins when challenger score is higher")
    void finalizeDuel_challengerHigherScore_challengerWins() {
        Duel duel = buildDuel(1L, 1L, 2L, 10L, Duel.DuelStatus.EVALUATING);
        Submission sub1 = buildSubmission(1L, 1L, "code", 80, 60);
        Submission sub2 = buildSubmission(1L, 2L, "code", 50, 90);
        User challenger = buildUser(1L, 1000);
        User opponent   = buildUser(2L, 1000);

        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        evaluationService.finalizeDuel(duel, sub1, sub2);

        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.COMPLETED);
        assertThat(duel.getWinnerId()).isEqualTo(1L);
        assertThat(duel.getChallengerEloChange()).isGreaterThan(0);
        assertThat(duel.getOpponentEloChange()).isLessThan(0);
        verify(duelRepository).save(duel);
        verify(userRepository).save(challenger);
        verify(userRepository).save(opponent);
    }

    @Test
    @DisplayName("finalizeDuel: opponent wins when opponent score is higher")
    void finalizeDuel_opponentHigherScore_opponentWins() {
        Duel duel = buildDuel(1L, 1L, 2L, 10L, Duel.DuelStatus.EVALUATING);
        Submission sub1 = buildSubmission(1L, 1L, "code", 30, 100);
        Submission sub2 = buildSubmission(1L, 2L, "code", 90, 45);
        User challenger = buildUser(1L, 1200);
        User opponent   = buildUser(2L, 800);

        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        evaluationService.finalizeDuel(duel, sub1, sub2);

        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.COMPLETED);
        assertThat(duel.getWinnerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("finalizeDuel: draw when scores are equal")
    void finalizeDuel_equalScore_draw() {
        Duel duel = buildDuel(1L, 1L, 2L, 10L, Duel.DuelStatus.EVALUATING);
        Submission sub1 = buildSubmission(1L, 1L, "code", 60, 120);
        Submission sub2 = buildSubmission(1L, 2L, "code", 60, 200);
        User challenger = buildUser(1L, 1000);
        User opponent   = buildUser(2L, 1000);

        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        evaluationService.finalizeDuel(duel, sub1, sub2);

        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.DRAW);
        assertThat(duel.getWinnerId()).isNull();
    }

    // -------------------------------------------------------------------------
    // finalizeDuel — ELO update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("finalizeDuel: winner ELO increases, loser ELO decreases")
    void finalizeDuel_winner_eloIncreases_loser_eloDecreases() {
        Duel duel = buildDuel(1L, 1L, 2L, 10L, Duel.DuelStatus.EVALUATING);
        Submission sub1 = buildSubmission(1L, 1L, "code", 100, 10);
        Submission sub2 = buildSubmission(1L, 2L, "code", 0,   300);
        User challenger = buildUser(1L, 1000);
        User opponent   = buildUser(2L, 1000);

        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        int eloBefore = challenger.getElo();

        evaluationService.finalizeDuel(duel, sub1, sub2);

        assertThat(challenger.getElo()).isGreaterThan(eloBefore);
        assertThat(opponent.getElo()).isLessThan(1000);
    }

    // -------------------------------------------------------------------------
    // handleEvaluationFailure
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleEvaluationFailure: sets EVALUATING duel to DRAW and broadcasts error")
    void handleEvaluationFailure_evaluatingDuel_markedAsDraw() {
        long duelId = 5L;
        Duel duel = buildDuel(duelId, 1L, 2L, 10L, Duel.DuelStatus.EVALUATING);
        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));

        evaluationService.handleEvaluationFailure(duelId);

        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.DRAW);
        assertThat(duel.getEndedAt()).isNotNull();
        verify(duelRepository).save(duel);
        verify(lifecycleService).broadcastEvent(eq(duelId), eq("DUEL_COMPLETED"), any());
    }

    @Test
    @DisplayName("handleEvaluationFailure: does nothing when duel is already COMPLETED")
    void handleEvaluationFailure_completedDuel_noOp() {
        long duelId = 6L;
        Duel duel = buildDuel(duelId, 1L, 2L, 10L, Duel.DuelStatus.COMPLETED);
        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));

        evaluationService.handleEvaluationFailure(duelId);

        verify(duelRepository, never()).save(any());
        verify(lifecycleService, never()).broadcastEvent(any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // evaluateDuel — pre-condition guard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("evaluateDuel: returns immediately when duel is already EVALUATING")
    void evaluateDuel_alreadyEvaluating_returnsImmediately() {
        long duelId = 42L;
        Duel duel = buildDuel(duelId, 1L, 2L, 10L, Duel.DuelStatus.EVALUATING);
        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));

        evaluationService.evaluateDuel(duelId);

        verify(judgeService, never()).judge(any());
        verify(duelRepository, never()).save(any());
    }

    @Test
    @DisplayName("evaluateDuel: returns immediately when duel is already COMPLETED")
    void evaluateDuel_alreadyCompleted_returnsImmediately() {
        long duelId = 43L;
        Duel duel = buildDuel(duelId, 1L, 2L, 10L, Duel.DuelStatus.COMPLETED);
        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));

        evaluationService.evaluateDuel(duelId);

        verify(judgeService, never()).judge(any());
        verify(duelRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // evaluateDuel — test_harness concatenation (async)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("evaluateDuel: test_harness is appended to user code before judging")
    void evaluateDuel_withHarness_concatenatesCodeAndHarness() throws Exception {
        long duelId = 10L;
        String harness  = "int main(void){return 0;}";
        String userCode = "void my_func(void){}";

        Duel       duel       = buildDuel(duelId, 1L, 2L, 7L, Duel.DuelStatus.IN_PROGRESS);
        Challenge  challenge  = buildChallenge(7L, 300, harness);
        Submission sub1       = buildSubmission(duelId, 1L, userCode, 0, 0);
        Submission sub2       = buildSubmission(duelId, 2L, userCode, 0, 0);
        User       challenger = buildUser(1L, 1000);
        User       opponent   = buildUser(2L, 1000);

        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));
        when(challengeRepository.findById(7L)).thenReturn(Optional.of(challenge));
        when(submissionRepository.findByDuelIdAndUserId(duelId, 1L)).thenReturn(Optional.of(sub1));
        when(submissionRepository.findByDuelIdAndUserId(duelId, 2L)).thenReturn(Optional.of(sub2));
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        ArgumentCaptor<JudgeRequest> captor = ArgumentCaptor.forClass(JudgeRequest.class);
        when(judgeService.judge(captor.capture())).thenReturn(passingResponse());

        executeTransactionSynchronously();

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
            if ("DUEL_COMPLETED".equals(inv.getArgument(1))) latch.countDown();
            return null;
        }).when(lifecycleService).broadcastEvent(any(), any(), any());

        evaluationService.evaluateDuel(duelId);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "DUEL_COMPLETED was not broadcast within 5 s");

        List<JudgeRequest> captured = captor.getAllValues();
        assertThat(captured).hasSize(2);
        assertThat(captured.get(0).code()).isEqualTo(userCode + "\n" + harness);
        assertThat(captured.get(1).code()).isEqualTo(userCode + "\n" + harness);
    }

    @Test
    @DisplayName("evaluateDuel: user code is sent unchanged when challenge has no harness")
    void evaluateDuel_withoutHarness_usesUserCodeDirectly() throws Exception {
        long duelId   = 11L;
        String userCode = "int main(void){return 0;}";

        Duel       duel       = buildDuel(duelId, 1L, 2L, 8L, Duel.DuelStatus.IN_PROGRESS);
        Challenge  challenge  = buildChallenge(8L, 300, null);   // no harness
        Submission sub1       = buildSubmission(duelId, 1L, userCode, 0, 0);
        Submission sub2       = buildSubmission(duelId, 2L, userCode, 0, 0);
        User       challenger = buildUser(1L, 1000);
        User       opponent   = buildUser(2L, 1000);

        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));
        when(challengeRepository.findById(8L)).thenReturn(Optional.of(challenge));
        when(submissionRepository.findByDuelIdAndUserId(duelId, 1L)).thenReturn(Optional.of(sub1));
        when(submissionRepository.findByDuelIdAndUserId(duelId, 2L)).thenReturn(Optional.of(sub2));
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        ArgumentCaptor<JudgeRequest> captor = ArgumentCaptor.forClass(JudgeRequest.class);
        when(judgeService.judge(captor.capture())).thenReturn(passingResponse());

        executeTransactionSynchronously();

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
            if ("DUEL_COMPLETED".equals(inv.getArgument(1))) latch.countDown();
            return null;
        }).when(lifecycleService).broadcastEvent(any(), any(), any());

        evaluationService.evaluateDuel(duelId);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "DUEL_COMPLETED was not broadcast within 5 s");

        List<JudgeRequest> captured = captor.getAllValues();
        assertThat(captured).hasSize(2);
        // no harness → code passed to judge must equal the raw user code exactly
        assertThat(captured.get(0).code()).isEqualTo(userCode);
        assertThat(captured.get(1).code()).isEqualTo(userCode);
    }

    @Test
    @DisplayName("evaluateDuel: empty submission yields score 0 without calling judge")
    void evaluateDuel_emptyCode_scoreIsZeroWithoutCallingJudge() throws Exception {
        long duelId = 12L;

        Duel       duel       = buildDuel(duelId, 1L, 2L, 9L, Duel.DuelStatus.IN_PROGRESS);
        Challenge  challenge  = buildChallenge(9L, 300, "int main(){return 0;}");
        // both players submitted nothing
        Submission sub1       = buildSubmission(duelId, 1L, "", 0, 300);
        Submission sub2       = buildSubmission(duelId, 2L, "", 0, 300);
        User       challenger = buildUser(1L, 1000);
        User       opponent   = buildUser(2L, 1000);

        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));
        when(challengeRepository.findById(9L)).thenReturn(Optional.of(challenge));
        when(submissionRepository.findByDuelIdAndUserId(duelId, 1L)).thenReturn(Optional.of(sub1));
        when(submissionRepository.findByDuelIdAndUserId(duelId, 2L)).thenReturn(Optional.of(sub2));
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));

        executeTransactionSynchronously();

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
            if ("DUEL_COMPLETED".equals(inv.getArgument(1))) latch.countDown();
            return null;
        }).when(lifecycleService).broadcastEvent(any(), any(), any());

        evaluationService.evaluateDuel(duelId);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "DUEL_COMPLETED was not broadcast within 5 s");

        // judge must never be called for blank submissions
        verify(judgeService, never()).judge(any());
        // both scores remain 0 → draw
        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.DRAW);
    }
}
