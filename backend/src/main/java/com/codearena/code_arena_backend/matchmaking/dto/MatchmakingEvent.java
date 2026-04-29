package com.codearena.code_arena_backend.matchmaking.dto;

/**
 * Event sent to players via WebSocket when matchmaking state changes.
 * Clients should subscribe to their user-scoped queue: /user/queue/matchmaking to receive these.
 */
public record MatchmakingEvent(
        /** Event type: QUEUED, MATCHED, TIMEOUT, CANCELLED */
        String type,
        /** Duel ID (non-null when type=MATCHED) */
        Long duelId,
        /** Opponent user ID (non-null when type=MATCHED) */
        Long opponentId,
        /** Opponent display name (non-null when type=MATCHED) */
        String opponentName,
        /** Challenge ID assigned to the duel (non-null when type=MATCHED) */
        Long challengeId,
        /** Human-readable message */
        String message
) {
    public static MatchmakingEvent queued() {
        return new MatchmakingEvent("QUEUED", null, null, null, null,
                "You have been added to the matchmaking queue.");
    }

    public static MatchmakingEvent matched(Long duelId, Long opponentId,
                                            String opponentName, Long challengeId) {
        return new MatchmakingEvent("MATCHED", duelId, opponentId, opponentName, challengeId,
                "Match found! You are dueling " + opponentName + ".");
    }

    public static MatchmakingEvent timeout() {
        return new MatchmakingEvent("TIMEOUT", null, null, null, null,
                "No opponent found. You have been removed from the queue.");
    }

    public static MatchmakingEvent cancelled() {
        return new MatchmakingEvent("CANCELLED", null, null, null, null,
                "You have left the matchmaking queue.");
    }
}
