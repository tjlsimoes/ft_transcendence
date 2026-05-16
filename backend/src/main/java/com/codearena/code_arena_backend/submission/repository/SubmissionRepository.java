package com.codearena.code_arena_backend.submission.repository;

import com.codearena.code_arena_backend.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByDuelId(Long duelId);
    Optional<Submission> findByDuelIdAndUserId(Long duelId, Long userId);
}
