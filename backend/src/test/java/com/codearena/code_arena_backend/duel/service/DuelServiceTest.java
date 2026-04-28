package com.codearena.code_arena_backend.duel.service;

import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.ranking.service.RankingService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuelService — Match Resolution")
class DuelServiceTest {

    @Mock private DuelRepository duelRepository;
    @Mock private UserRepository userRepository;
    @Mock private RankingService rankingService;

    @InjectMocks private DuelService duelService;

    private User challenger;
    private User opponent;
    private Duel duel;

    @BeforeEach
    void setUp() {
        challenger = new User();
        challenger.setId(1L);
        challenger.setElo(1000);
        challenger.setWinStreak(2);
        challenger.setWins(5);

        opponent = new User();
        opponent.setId(2L);
        opponent.setElo(1200);
        opponent.setWinStreak(1);
        opponent.setLosses(3);

        duel = new Duel();
        duel.setId(10L);
        duel.setChallengerId(1L);
        duel.setOpponentId(2L);
        duel.setStartedAt(LocalDateTime.now());
        duel.setStatus(Duel.DuelStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Complete Duel: Challenger Wins (Underdog)")
    void completeDuel_ChallengerWins() {
        // Arrange
        when(duelRepository.findById(10L)).thenReturn(Optional.of(duel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));
        
        // Mock the Elo calculations
        when(rankingService.calculateEloDelta(1000, 1200, 1.0, 2)).thenReturn(30); // Winner gains 30
        when(rankingService.calculateEloDelta(1200, 1000, 0.0, 1)).thenReturn(-25); // Loser loses 25
        
        when(rankingService.getLeagueFromElo(1030)).thenReturn("SILVER");
        when(rankingService.getLeagueFromElo(1175)).thenReturn("SILVER");

        // Act
        duelService.completeDuel(10L, 1L, false);

        // Assert
        // 1. Check Duel Status
        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.COMPLETED);
        assertThat(duel.getWinnerId()).isEqualTo(1L);
        assertThat(duel.getChallengerEloChange()).isEqualTo(30);
        assertThat(duel.getOpponentEloChange()).isEqualTo(-25);

        // 2. Check User Stats Updated
        assertThat(challenger.getElo()).isEqualTo(1030);
        assertThat(challenger.getWinStreak()).isEqualTo(3);
        assertThat(challenger.getWins()).isEqualTo(6);

        assertThat(opponent.getElo()).isEqualTo(1175);
        assertThat(opponent.getWinStreak()).isEqualTo(0); // Reset!
        assertThat(opponent.getLosses()).isEqualTo(4);

        // 3. Verify Repositories were called to save
        verify(duelRepository).save(duel);
        verify(userRepository).save(challenger);
        verify(userRepository).save(opponent);
        verify(userRepository).recalculateMasterLeagues();
    }

    @Test
    @DisplayName("Complete Duel: Draw")
    void completeDuel_Draw() {
        // Arrange
        when(duelRepository.findById(10L)).thenReturn(Optional.of(duel));
        when(userRepository.findById(1L)).thenReturn(Optional.of(challenger));
        when(userRepository.findById(2L)).thenReturn(Optional.of(opponent));
        
        // Mock the Elo calculations
        when(rankingService.calculateEloDelta(1000, 1200, 0.5, 2)).thenReturn(0);
        when(rankingService.calculateEloDelta(1200, 1000, 0.5, 1)).thenReturn(0);
        
        when(rankingService.getLeagueFromElo(1000)).thenReturn("SILVER");
        when(rankingService.getLeagueFromElo(1200)).thenReturn("SILVER");

        // Act
        duelService.completeDuel(10L, 1L, true);

        // Assert
        // 1. Check Duel Status
        assertThat(duel.getStatus()).isEqualTo(Duel.DuelStatus.DRAW);
        assertThat(duel.getChallengerEloChange()).isEqualTo(0);
        assertThat(duel.getOpponentEloChange()).isEqualTo(0);

        // 2. Check User Stats Updated
        assertThat(challenger.getElo()).isEqualTo(1000);
        assertThat(challenger.getWinStreak()).isEqualTo(0);
        assertThat(challenger.getWins()).isEqualTo(5);

        assertThat(opponent.getElo()).isEqualTo(1200);
        assertThat(opponent.getWinStreak()).isEqualTo(0);
        assertThat(opponent.getLosses()).isEqualTo(3);

        // 3. Verify Repositories were called to save
        verify(duelRepository).save(duel);
        verify(userRepository).save(challenger);
        verify(userRepository).save(opponent);
        verify(userRepository).recalculateMasterLeagues();
    }
}
