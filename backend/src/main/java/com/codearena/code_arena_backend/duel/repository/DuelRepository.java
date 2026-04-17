package com.codearena.code_arena_backend.duel.repository;

import com.codearena.code_arena_backend.duel.entity.Duel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuelRepository extends JpaRepository<Duel, Long> {

    @Query("SELECT d FROM Duel d WHERE d.challengerId = :userId OR d.opponentId = :userId ORDER BY d.endedAt DESC")
    List<Duel> findByUserId(Long userId);
}
