package com.codearena.code_arena_backend.user.repository;

import com.codearena.code_arena_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /** Count all players with elo >= 3000 (Master+ pool). */
    @Query("SELECT COUNT(u) FROM User u WHERE u.elo >= 3000")
    long countMasterPlusPlayers();

    /**
     * Find the elo of the player at a given rank position (1-based) among Master+ players,
     * ordered by elo descending. Used to determine the Legend threshold (top 1%).
     */
    @Query(value = "SELECT elo FROM users WHERE elo >= 3000 ORDER BY elo DESC LIMIT 1 OFFSET :offset", nativeQuery = true)
    Optional<Integer> findEloAtMasterPlusRank(long offset);

    /** Find the highest elo among all players. */
    @Query("SELECT MAX(u.elo) FROM User u")
    Optional<Integer> findHighestElo();

    /** Count how many players have elo strictly greater than the given value. */
    @Query("SELECT COUNT(u) FROM User u WHERE u.elo > :elo")
    long countPlayersWithEloAbove(int elo);
}
