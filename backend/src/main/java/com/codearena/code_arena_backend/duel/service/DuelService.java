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
        // Get Duel and check if it's valid
        Duel duel = duelRepository.findById(duelId)
            .orElseThrow(() -> new RuntimeException("Duel not found"));
        
        if (duel.getStartedAt() == null) {
            throw new RuntimeException("Duel not started");
        }
        if (duel.getEndedAt() != null) {
            throw new RuntimeException("Duel already completed");
        }

        // Update Duel status
        duel.setEndedAt(LocalDateTime.now());
        if (isDraw) {
            duel.setStatus(Duel.DuelStatus.DRAW);
        } else {
            duel.setStatus(Duel.DuelStatus.COMPLETED);
            duel.setWinnerId(winnerId);
        }

        // Calculate and Update Ratings/Elo
        User winner = userRepository.findById(winnerId)
            .orElseThrow(() -> new RuntimeException("Winner not found"));
        
        User loser = duel.getChallengerId().equals(winnerId)
            ? userRepository.findById(duel.getOpponentId()).orElseThrow(() -> new RuntimeException("Loser not found"))
            : userRepository.findById(duel.getChallengerId()).orElseThrow(() -> new RuntimeException("Loser not found"));

        Integer winnerDelta = rankingService.calculateEloDelta(winner.getElo(), loser.getElo(), isDraw ? 0.5 : 1.0, winner.getWinStreak());
        Integer loserDelta = rankingService.calculateEloDelta(loser.getElo(), winner.getElo(), isDraw ? 0.5 : 0.0, loser.getWinStreak());

        // Update win streaks
        if (isDraw) {
            loser.setWinStreak(0);
            winner.setWinStreak(0);
        } else if (winnerId.equals(duel.getChallengerId())) {
            loser.setWinStreak(0);
            winner.setWinStreak(winner.getWinStreak() + 1);
        } else {
            loser.setWinStreak(loser.getWinStreak() + 1);
            winner.setWinStreak(0);
        }

        // Update win/loss counts
        if (!isDraw) {
            winner.setWins(winner.getWins() + 1);
            loser.setLosses(loser.getLosses() + 1);
        }

        winner.setElo(winner.getElo() + winnerDelta);
        loser.setElo(loser.getElo() + loserDelta);
        
        // Update user leagues based on new Elo (Bronze, Silver, Gold, Master)
        winner.setLeague(User.League.valueOf(rankingService.getLeagueFromElo(winner.getElo())));
        loser.setLeague(User.League.valueOf(rankingService.getLeagueFromElo(loser.getElo())));

        duel.setChallengerEloChange(winnerDelta);
        duel.setOpponentEloChange(loserDelta);

        duelRepository.save(duel);
        userRepository.save(winner);
        userRepository.save(loser);
        
        // Ensure Legend (Top 1% of Master+) is correctly recalculated globally
        userRepository.recalculateMasterLeagues();
    }
}

