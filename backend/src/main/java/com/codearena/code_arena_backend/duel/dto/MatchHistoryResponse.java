package com.codearena.code_arena_backend.duel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for returning match history entries to the client.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHistoryResponse {

    private Long id;
    private String result;       // "VICTORY" or "DEFEAT"
    private String opponent;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
