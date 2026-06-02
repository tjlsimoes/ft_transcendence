package com.codearena.code_arena_backend.challenge.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.codearena.code_arena_backend.challenge.dto.ChallengeUpsertRequest;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.judge.dto.JudgeResponse;
import com.codearena.code_arena_backend.judge.service.JudgeService;
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

    @Mock
    private JudgeService judgeService;

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
        Challenge challenge = new Challenge(
            1L,
            "Example",
            "Desc",
            ChallengeDifficulty.MEDIUM,
            600,
            JsonNodeFactory.instance.arrayNode(),
            null
        );
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
    @DisplayName("createChallenge sets time limit from DB difficulty settings")
    void createChallenge_setsTimeFromDifficulty() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "ft_split",
                "desc",
                ChallengeDifficulty.INSANE,
                JsonNodeFactory.instance.arrayNode()
        );

        when(challengeRepository.findConfiguredTimeLimitByDifficulty("INSANE"))
                .thenReturn(Optional.of(1800));
        when(challengeRepository.save(org.mockito.ArgumentMatchers.any(Challenge.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Challenge created = challengeService.createChallenge(request);

        assertThat(created.getDifficulty()).isEqualTo(ChallengeDifficulty.INSANE);
        assertThat(created.getTimeLimitSecs()).isEqualTo(1800);
        assertThat(created.getTitle()).isEqualTo("ft_split");
        verify(challengeRepository).findConfiguredTimeLimitByDifficulty("INSANE");
        verify(challengeRepository).save(org.mockito.ArgumentMatchers.any(Challenge.class));
    }

    @Test
    @DisplayName("createChallenge throws when DB settings for difficulty are missing")
    void createChallenge_missingDifficultySettings_throws() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "ft_split",
                "desc",
                ChallengeDifficulty.INSANE,
                JsonNodeFactory.instance.arrayNode()
        );

        when(challengeRepository.findConfiguredTimeLimitByDifficulty("INSANE"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> challengeService.createChallenge(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INSANE");
    }

    @Test
    @DisplayName("updateChallenge updates existing challenge and recalculates time from difficulty")
    void updateChallenge_existing_updatesChallenge() {
        Challenge existing = new Challenge(
            3L,
            "old",
            "old",
            ChallengeDifficulty.EASY,
            300,
            JsonNodeFactory.instance.arrayNode(),
            null
        );
        var expectedTestCases = JsonNodeFactory.instance.arrayNode().addObject().put("input", "x");
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "new-title",
                "new-desc",
                ChallengeDifficulty.HARD,
                expectedTestCases
        );

        when(challengeRepository.findConfiguredTimeLimitByDifficulty("HARD"))
                .thenReturn(Optional.of(1200));
        when(challengeRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(challengeRepository.save(existing)).thenReturn(existing);

        Challenge updated = challengeService.updateChallenge(3L, request);

        assertThat(updated.getTitle()).isEqualTo("new-title");
        assertThat(updated.getDescription()).isEqualTo("new-desc");
        assertThat(updated.getDifficulty()).isEqualTo(ChallengeDifficulty.HARD);
        assertThat(updated.getTimeLimitSecs()).isEqualTo(1200);
        assertThat(updated.getTestCases()).isEqualTo(expectedTestCases);
        verify(challengeRepository).findConfiguredTimeLimitByDifficulty("HARD");
    }

    @Test
    @DisplayName("updateChallenge missing id throws NoSuchElementException")
    void updateChallenge_missing_throwsNotFound() {
        ChallengeUpsertRequest request = new ChallengeUpsertRequest(
                "title",
                null,
                ChallengeDifficulty.MEDIUM,
                JsonNodeFactory.instance.arrayNode()
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

        @Test
        @DisplayName("runCode uses exactly first 3 sample test cases")
        void runCode_usesExactlyThreeSampleCases() {
        var testCases = JsonNodeFactory.instance.arrayNode();
        testCases.addObject().put("input", "1 2").put("expected_output", "3");
        testCases.addObject().put("stdin", "2 3").put("expectedOutput", "5");
        testCases.addObject().put("input", "4 5").put("output", "9");
        testCases.addObject().put("input", "9 9").put("expected_output", "18");

        Challenge challenge = new Challenge(
            12L,
            "sum",
            "desc",
            ChallengeDifficulty.EASY,
            300,
            testCases,
            null
        );

        when(challengeRepository.findById(12L)).thenReturn(Optional.of(challenge));
        JudgeResponse expected = new JudgeResponse(true, 3, 3, 10, 1000, null, List.of());
        when(judgeService.judge(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        JudgeResponse response = challengeService.runCode(12L, "int main(){return 0;}", "C");

        assertThat(response).isSameAs(expected);
        var requestCaptor = org.mockito.ArgumentCaptor.forClass(com.codearena.code_arena_backend.judge.dto.JudgeRequest.class);
        verify(judgeService).judge(requestCaptor.capture());
        assertThat(requestCaptor.getValue().language()).isEqualTo("c");
        assertThat(requestCaptor.getValue().testCases()).hasSize(3);
        assertThat(requestCaptor.getValue().testCases().get(0).stdin()).isEqualTo("1 2");
        assertThat(requestCaptor.getValue().testCases().get(1).stdin()).isEqualTo("2 3");
        assertThat(requestCaptor.getValue().testCases().get(2).expectedOutput()).isEqualTo("9");
        }

        @Test
        @DisplayName("runCode rejects non-C language")
        void runCode_nonCLanguage_throws() {
        assertThatThrownBy(() -> challengeService.runCode(1L, "int main(){return 0;}", "python"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Only C language");
        }

        @Test
        @DisplayName("runCode requires at least 3 sample tests")
        void runCode_requiresThreeTests() {
        var testCases = JsonNodeFactory.instance.arrayNode();
        testCases.addObject().put("input", "1").put("expected_output", "1");
        testCases.addObject().put("input", "2").put("expected_output", "2");

        Challenge challenge = new Challenge(
            22L,
            "few-tests",
            "desc",
            ChallengeDifficulty.EASY,
            300,
            testCases,
            null
        );
        when(challengeRepository.findById(22L)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> challengeService.runCode(22L, "int main(){return 0;}", "c"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("at least 3");
        }
}
