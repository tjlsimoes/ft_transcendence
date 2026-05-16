package com.codearena.code_arena_backend.submission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "duel_id", nullable = false)
    private Long duelId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "time_score")
    private Integer timeScore = 0;

    @Column(name = "perf_score")
    private Integer perfScore = 0;

    @Column(name = "correctness_score")
    private Integer correctnessScore = 0;

    @Column(name = "quality_score")
    private Integer qualityScore = 0;

    @Column(name = "runtime_ms")
    private Long runtimeMs = 0L;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs = 0;

    @Column(name = "timed_out")
    private boolean timedOut = false;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;
}
