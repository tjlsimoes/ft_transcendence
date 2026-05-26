package com.codearena.code_arena_backend.duel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuelResultEvent {
    private Long duelId;
    private String result;       // "WIN", "LOSS", "DRAW"
    private Long opponentId;
    private String opponentName;
    private int eloChange;
    private int newElo;
}
