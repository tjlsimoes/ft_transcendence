package com.codearena.code_arena_backend.user.repository;

import com.codearena.code_arena_backend.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Find the elo of the player at a given global rank position (0-based offset),
     * ordered by elo descending. Used to determine the Legend threshold (top 1% of all players).
     */
    @Query(value = "SELECT elo FROM users ORDER BY elo DESC LIMIT 1 OFFSET :offset", nativeQuery = true)
    Optional<Integer> findEloAtGlobalRank(long offset);

    /** Find the highest elo among all players. */
    @Query("SELECT MAX(u.elo) FROM User u")
    Optional<Integer> findHighestElo();

    /** Count how many players have elo strictly greater than the given value. */
    @Query("SELECT COUNT(u) FROM User u WHERE u.elo > :elo")
    long countPlayersWithEloAbove(int elo);

    /** All players with elo >= 3000, ordered by elo descending. */
    @Query("SELECT u FROM User u WHERE u.elo >= 3000 ORDER BY u.elo DESC")
    List<User> findMasterPlusPlayers();

    /** Paginated leaderboard of all players ordered by elo descending. */
    Page<User> findAllByOrderByEloDesc(Pageable pageable);

    /** Paginated leaderboard of players in a specific league ordered by elo descending. */
    Page<User> findByLeagueOrderByEloDesc(User.League league, Pageable pageable);

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
