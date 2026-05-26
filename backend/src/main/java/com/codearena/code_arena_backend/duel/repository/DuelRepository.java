package com.codearena.code_arena_backend.duel.repository;

import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.entity.Duel.DuelStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DuelRepository extends JpaRepository<Duel, Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT d FROM Duel d WHERE d.id = :id")
        Optional<Duel> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT d FROM Duel d
            WHERE d.challengerId = :userId OR d.opponentId = :userId
            ORDER BY COALESCE(d.endedAt, d.startedAt) DESC NULLS LAST
            """)
    List<Duel> findByUserId(Long userId);

    /**
     * Returns the most recent active duel for a user with the given statuses.
     * Used by the lobby redirect guard to detect if the user has an ongoing game.
     *
     * The statuses are passed as a parameter to avoid JPQL issues with inner-class
     * enum literals (Duel.DuelStatus is a nested enum).
     */
    @Query("""
            SELECT d FROM Duel d
            WHERE (d.challengerId = :userId OR d.opponentId = :userId)
              AND d.status IN :statuses
            ORDER BY d.startedAt DESC
            """)
    Optional<Duel> findActiveByUserId(
            @Param("userId") Long userId,
            @Param("statuses") Collection<DuelStatus> statuses);
}
