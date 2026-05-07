package com.codearena.code_arena_backend.user.repository;

import com.codearena.code_arena_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /** Count all players (used as the base for the top-1 % Legend calculation). */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllPlayers();

    /**
     * Projection returned by {@link #findMasterRankingStats(int)}.
     * Carries all data needed by {@code enrichWithRankingContext} in a single DB round-trip.
     */
    interface MasterRankingStats {
        Long getTotalPlayers();
        Long getPlayersAbove();
        Integer getLegendThresholdElo();
        Integer getHighestElo();
    }

    /**
     * Single-query replacement for the four individual ranking queries.
     *
     * Returns in one DB round-trip:
     *   - total_players       : COUNT(*) of all users
     *   - players_above       : COUNT of users with elo > playerElo (determines global rank)
     *   - legend_threshold_elo: elo of the player at the legend cutoff position (top 1%)
     *   - highest_elo         : MAX elo across all players
     *
     * The OFFSET for the legend threshold mirrors the Java formula:
     *   GREATEST(1, CEIL(total * 0.01)) - 1
     */
    @Query(value = """
        WITH totals AS (
            SELECT COUNT(*) AS n FROM users
        )
        SELECT
            t.n                                                                           AS total_players,
            (SELECT COUNT(*) FROM users WHERE elo > :playerElo)                          AS players_above,
            COALESCE(
                (SELECT elo FROM users ORDER BY elo DESC
                 LIMIT 1 OFFSET (GREATEST(1::BIGINT, CEIL(t.n * 0.01)::BIGINT) - 1)),
                3000
            )                                                                             AS legend_threshold_elo,
            COALESCE((SELECT MAX(elo) FROM users), :playerElo)                            AS highest_elo
        FROM totals t
        """, nativeQuery = true)
    MasterRankingStats findMasterRankingStats(int playerElo);

    /** All players with elo >= 3000, ordered by elo descending. */
    @Query("SELECT u FROM User u WHERE u.elo >= 3000 ORDER BY u.elo DESC")
    List<User> findMasterPlusPlayers();

    /** Top N players ordered by elo descending (for leaderboard). */
    @Query(value = "SELECT * FROM users ORDER BY elo DESC LIMIT :limit", nativeQuery = true)
    List<User> findTopPlayersByElo(int limit);

    /**
     * Atomically recalculates LEGEND/MASTER for all players with elo >= 3000.
     * Players whose DENSE_RANK (by elo DESC) is within the legend cutoff get LEGEND;
     * everyone else with elo >= 3000 gets MASTER.
     * Uses DENSE_RANK so that tied elo values share the same rank.
     */
    @Modifying
    @Query(value = """
        UPDATE users SET league = CASE
            WHEN id IN (
                SELECT id FROM (
                    SELECT id, DENSE_RANK() OVER (ORDER BY elo DESC) AS rnk
                    FROM users WHERE elo >= 3000
                ) ranked
                WHERE rnk <= GREATEST(1, CEIL((SELECT COUNT(*) FROM users) * 0.01))
            ) THEN 'LEGEND'
            WHEN elo >= 3000 THEN 'MASTER'
            ELSE league
        END
        WHERE elo >= 3000
        """, nativeQuery = true)
    void recalculateMasterLeagues();
}
