package com.codearena.code_arena_backend.ranking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.codearena.code_arena_backend.duel.dto.MatchHistoryResponse;
import com.codearena.code_arena_backend.duel.entity.Duel.DuelStatus;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {


    //  A high K-factor causes faster rating fluctuations for new
    //  or inconsistent players, while a low K-factor offers
    //  stability for established players.
    /**
     * The K-factor in ranking systems is a development coefficient
     * that determines how quickly a player's rating changes.
     */
    private static final int K_FACTOR = 32;

    private final DuelRepository duelRepository;
    private final UserService userService;

     // expectedScore - The probability of the player winning:
     // 
     //   - Equal Skill (1000 vs 1000):
     //     - The formula is 1 / (1+1) == 0.5.
     //     - The system expects you to have a 50% chance of winning.
     //   - You are the Underdog (1000 vs 1400):
     //     - The formula is 1 / (1+10^((1400-1000)/400)) == 1 / (1+10^((400)/400)) == 1
     //       / (1+10^1) == 1/11 == approx 0.09.
     //     - The system expects you only have a 9% chance to win.
     //   - You are the Favorite (1400 vs 1000):
     //     - The formula is 1 / (1+10^((1000-1400)/400)) == 1 / (1+10^(-400/400)) == 1 /
     //       (1+10^-1) == 1/1.1 == approx 0.91.
     //     - The system expects you to win 91% of the time.
     // 
     // actualScore - The actual score of the player (1 for win, 0 for loss, 0.5 for draw)
     //   - The Underdog win (1000 vs 1400 and 1000 wins)
     //      - ExpectedScore = 0.09
     //      - ActualScore = 1
     //      - Delta = (1 - 0.09) // 32 = 29 points gained
     //   - The Expected Result (1400 vs 1000 and 1400 wins)
     //      - ExpectedScore = 0.91
     //      - ActualScore = 1
     //      - Delta = (1 - 0.91) // 32 = 3 points gained
     //   - The Draw (1000 vs 1000 and 0.5)
     //      - ExpectedScore = 0.5
     //      - ActualScore = 0.5
     //      - Delta = (0.5 - 0.5) // 32 = 0
     //   
     // winStreak - The number of wins in a row limited to 3 wins
     //   - The bonus multiplier increases with the number of wins in a row:
     //     - 1 win: +10%
     //     - 2 wins: +20%
     //     - 3 wins: +30%
     //     - 4+ wins: +30% (maximum bonus)
     /**
     * @param playerElo   The player's current Elo rating.
     * @param opponentElo The opponent's current Elo rating.
     * @param actualScore The actual score of the player (1 for win, 0 for loss, 0.5 for draw).
     * @param winStreak   The number of wins in a row limited to 3 wins.
     * @return The change in the player's Elo rating.
     */
    public int calculateEloDelta(int playerElo, int opponentElo, double actualScore, int winStreak) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentElo - playerElo) / 400.0));
        double bonusMultiplier = 1.0 + (Math.min(winStreak, 3) * 0.1);
        double delta = (actualScore - expectedScore) * K_FACTOR;
        if (actualScore > 0.5)
            delta *= bonusMultiplier;
        return (int) delta;
    }

    public List<MatchHistoryResponse> getUserMatchHistory(User user) {
        List<MatchHistoryResponse> history = duelRepository.findByUserId(user.getId())
                .stream()
                .map(duel -> {
                    boolean isChallenger = duel.getChallengerId().equals(user.getId());
                    Long opponentId = isChallenger ? duel.getOpponentId() : duel.getChallengerId();
                    String opponentName = userService.findById(opponentId)
                            .map(User::getUsername)
                            .orElse("Unknown");

                    // Determine result from winnerId (set by the judge/duel service).
                    String result;
                    if (duel.getStatus() == DuelStatus.DRAW) {
                        result = "DRAW";
                    } else if (duel.getStatus() == DuelStatus.COMPLETED && duel.getWinnerId() != null) {
                        result = duel.getWinnerId().equals(user.getId()) ? "VICTORY" : "DEFEAT";
                    } else if (duel.getStatus() == DuelStatus.CANCELLED) {
                        result = "CANCELLED";
                    } else {
                        result = "PENDING";
                    }

                    // Determine LP change for the requesting user.
                    Integer lpChange = isChallenger
                            ? duel.getChallengerEloChange()
                            : duel.getOpponentEloChange();

                    return MatchHistoryResponse.builder()
                            .id(duel.getId())
                            .result(result)
                            .opponent(opponentName)
                            .status(duel.getStatus().name())
                            .lpChange(lpChange)
                            .startedAt(duel.getStartedAt())
                            .endedAt(duel.getEndedAt())
                            .build();
                })
                .toList();
            return history;
    }
}
