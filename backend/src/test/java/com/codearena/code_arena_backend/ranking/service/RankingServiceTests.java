package com.codearena.code_arena_backend.ranking.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RankingServiceTests {

    @InjectMocks
    private RankingService rankingService;

    @Test
    @DisplayName("Equal Elo Draw: 0 change in Elo")
    void calculateEloChange_equalElo_draw() {
        int elo1 = 1500;
        int elo2 = 1500;
        int result = rankingService.calculateEloDelta(elo1, elo2, 0.5, 0);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Underdog win Elo change: Win streak > Win")
    void calculateEloChange_equalElo_underdogWinStreakGreaterThanUnderdogWin() {
        int elo1 = 1000;
        int elo2 = 1500;
        int result = rankingService.calculateEloDelta(elo1, elo2, 1, 0);
        int resultWinStreak = rankingService.calculateEloDelta(elo1, elo2, 1, 3);
        assertTrue(resultWinStreak > result);
    }
    
    @Test
    @DisplayName("Equal Elo Draw with Win Streak: 0 change in Elo")
    void calculateEloChange_equalElo_drawStreakEqualToDraw() {
        int elo1 = 1500;
        int elo2 = 1500;
        int result = rankingService.calculateEloDelta(elo1, elo2, 0.5, 0);
        int resultDrawStreak = rankingService.calculateEloDelta(elo1, elo2, 0.5, 3);
        assertEquals(result, resultDrawStreak);
    }

    @Test
    @DisplayName("Underdog win Elo change > Favourite Win Elo change")
    void calculateEloChange_unequalElo_underdogWinGreaterThanFavouriteWin() {
        int elo1 = 1000;
        int elo2 = 1500;
        int resultWinFavourite = rankingService.calculateEloDelta(elo2, elo1, 1, 0);
        int resultWinUnderdog = rankingService.calculateEloDelta(elo1, elo2, 1, 0);
        assertTrue(resultWinUnderdog > resultWinFavourite);
    }

    @Test
    @DisplayName("Favourite loss Elo change is greater than Underdog loss Elo change")
    void calculateEloChange_unequalElo_favouriteLossLessThanUnderdogLoss() {
        int elo1 = 1000;
        int elo2 = 1500;
        int resultLossFavourite = rankingService.calculateEloDelta(elo2, elo1, 0, 0);
        int resultLossUnderdog = rankingService.calculateEloDelta(elo1, elo2, 0, 0);
        assertTrue(resultLossUnderdog > resultLossFavourite);
    }

    @Test
    @DisplayName("Win streak Elo change capped at 3")
    void calculateEloChange_WinStreakCapToThree() {
        int elo1 = 1000;
        int elo2 = 1500;
        int result4 = rankingService.calculateEloDelta(elo1, elo2, 1, 4);
        int result3 = rankingService.calculateEloDelta(elo1, elo2, 1, 3);
        assertTrue(result4 == result3);
    }


    // TO-DO: Add getUserMatchHistory test cases.

}
