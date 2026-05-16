package com.codearena.code_arena_backend.ranking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "rankings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Ranking {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer elo = 0;

    @Column(nullable = false, length = 50)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.codearena.code_arena_backend.user.entity.User.League league = com.codearena.code_arena_backend.user.entity.User.League.BRONZE;

    @Column(name = "win_streak", nullable = false)
    private Integer winStreak = 0;
}
