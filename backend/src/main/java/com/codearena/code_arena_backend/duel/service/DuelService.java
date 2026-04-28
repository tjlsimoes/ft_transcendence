package com.codearena.code_arena_backend.duel.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.ranking.service.RankingService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DuelService {

    private final RankingService rankingService;
    private final DuelRepository duelRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void completeDuel(Long duelId, Long winnerId, boolean isDraw) {
        Duel duel = duelRepository.findById(duelId)
            .orElseThrow(() -> new RuntimeException("Duel not found"));
        
        if (duel.getStartedAt() == null) {
            throw new RuntimeException("Duel not started");
        }
        if (duel.getEndedAt() != null) {
            throw new RuntimeException("Duel already completed");
        }

        User challenger = userRepository.findById(duel.getChallengerId())
            .orElseThrow(() -> new RuntimeException("Challenger not found"));
        User opponent = userRepository.findById(duel.getOpponentId())
            .orElseThrow(() -> new RuntimeException("Opponent not found"));

        // Update Duel status
        duel.setEndedAt(LocalDateTime.now());
        if (isDraw) {
            duel.setStatus(Duel.DuelStatus.DRAW);
            duel.setWinnerId(null);
        } else {
            duel.setStatus(Duel.DuelStatus.COMPLETED);
            duel.setWinnerId(winnerId);
        }

        // Calculate Elo Deltas
        double challengerScore = isDraw ? 0.5 : (winnerId.equals(challenger.getId()) ? 1.0 : 0.0);
        double opponentScore = isDraw ? 0.5 : (winnerId.equals(opponent.getId()) ? 1.0 : 0.0);

        int challengerDelta = rankingService.calculateEloDelta(challenger.getElo(), opponent.getElo(), challengerScore, challenger.getWinStreak());
        int opponentDelta = rankingService.calculateEloDelta(opponent.getElo(), challenger.getElo(), opponentScore, opponent.getWinStreak());

        // Update Stats
        if (isDraw) {
            challenger.setWinStreak(0);
            opponent.setWinStreak(0);
        } else if (winnerId.equals(challenger.getId())) {
            challenger.setWins(challenger.getWins() + 1);
            challenger.setWinStreak(challenger.getWinStreak() + 1);
            opponent.setLosses(opponent.getLosses() + 1);
            opponent.setWinStreak(0);
        } else {
            opponent.setWins(opponent.getWins() + 1);
            opponent.setWinStreak(opponent.getWinStreak() + 1);
            challenger.setLosses(challenger.getLosses() + 1);
            challenger.setWinStreak(0);
        }

        // Apply Elo changes
        challenger.setElo(challenger.getElo() + challengerDelta);
        opponent.setElo(opponent.getElo() + opponentDelta);
        
        // Sync Leagues
        challenger.setLeague(User.League.valueOf(rankingService.getLeagueFromElo(challenger.getElo())));
        opponent.setLeague(User.League.valueOf(rankingService.getLeagueFromElo(opponent.getElo())));

        // Save Deltas to Duel
        duel.setChallengerEloChange(challengerDelta);
        duel.setOpponentEloChange(opponentDelta);

        // Save all
        duelRepository.save(duel);
        userRepository.save(challenger);
        userRepository.save(opponent);
        
        userRepository.recalculateMasterLeagues();
    }
}

