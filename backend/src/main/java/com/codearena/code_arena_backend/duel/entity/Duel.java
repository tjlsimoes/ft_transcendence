package com.codearena.code_arena_backend.duel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "duels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Duel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenger_id", nullable = false)
    private Long challengerId;

    @Column(name = "opponent_id", nullable = false)
    private Long opponentId;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DuelStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "challenger_elo_change")
    private Integer challengerEloChange;

    @Column(name = "opponent_elo_change")
    private Integer opponentEloChange;

    public enum DuelStatus {
        WAITING,
        MATCHED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        DRAW
    }
}
