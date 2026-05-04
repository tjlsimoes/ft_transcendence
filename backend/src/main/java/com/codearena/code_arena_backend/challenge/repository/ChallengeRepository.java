package com.codearena.code_arena_backend.challenge.repository;

import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    Page<Challenge> findByDifficulty(ChallengeDifficulty difficulty, Pageable pageable);

    @Query(value = "SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = :difficulty", nativeQuery = true)
    Optional<Integer> findConfiguredTimeLimitByDifficulty(@Param("difficulty") String difficulty);
}
