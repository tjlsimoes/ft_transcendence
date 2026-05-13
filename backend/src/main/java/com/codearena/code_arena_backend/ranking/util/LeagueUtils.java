package com.codearena.code_arena_backend.ranking.util;

/**
 * Single source of truth for league tier derivation.
 *
 * All league-from-elo logic lives here so that thresholds only need
 * to be updated in one place.
 *
 * Tiers: BRONZE (0–999) | SILVER (1000–1999) | GOLD (2000–2999)
 *        MASTER (3000+)  | LEGEND (top 1% AND elo >= 3000)
 */
public final class LeagueUtils {

    private LeagueUtils() {}

    /**
     * Derives the league name from an elo value alone.
     * Does NOT account for LEGEND status — use {@link #computeLeague} for that.
     */
    public static String leagueFromElo(int elo) {
        if (elo >= 3000) return "MASTER";
        if (elo >= 2000) return "GOLD";
        if (elo >= 1000) return "SILVER";
        return "BRONZE";
    }

    /**
     * Derives the display league for a player given their elo and leaderboard rank.
     * LEGEND = top {@code legendCutoff} players AND elo >= 3000.
     *
     * @param elo          player's current elo
     * @param rank         1-based rank position on the leaderboard
     * @param legendCutoff maximum rank that qualifies for LEGEND (e.g. top 1%)
     */
    public static String computeLeague(int elo, int rank, long legendCutoff) {
        if (elo >= 3000) {
            return rank <= legendCutoff ? "LEGEND" : "MASTER";
        }
        return leagueFromElo(elo);
    }
}
