package com.codearena.code_arena_backend.matchmaking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MatchmakingEvent schema and factory methods.
 * Verifies that all documented event types (QUEUED, MATCHED, TIMEOUT, CANCELLED, ERROR)
 * are properly created and contain expected fields.
 */
@DisplayName("MatchmakingEvent — schema and factory methods")
class MatchmakingEventTest {

    @Test
    @DisplayName("queued() creates event with type=QUEUED")
    void queued_createsValidEvent() {
        MatchmakingEvent event = MatchmakingEvent.queued();

        assertThat(event.type()).isEqualTo("QUEUED");
        assertThat(event.duelId()).isNull();
        assertThat(event.opponentId()).isNull();
        assertThat(event.opponentName()).isNull();
        assertThat(event.challengeId()).isNull();
        assertThat(event.message()).isNotBlank();
    }

    @Test
    @DisplayName("matched() creates event with type=MATCHED and all required fields")
    void matched_createsValidEvent() {
        MatchmakingEvent event = MatchmakingEvent.matched(1L, 2L, "Opponent", 10L);

        assertThat(event.type()).isEqualTo("MATCHED");
        assertThat(event.duelId()).isEqualTo(1L);
        assertThat(event.opponentId()).isEqualTo(2L);
        assertThat(event.opponentName()).isEqualTo("Opponent");
        assertThat(event.challengeId()).isEqualTo(10L);
        assertThat(event.message()).isNotBlank();
    }

    @Test
    @DisplayName("timeout() creates event with type=TIMEOUT")
    void timeout_createsValidEvent() {
        MatchmakingEvent event = MatchmakingEvent.timeout();

        assertThat(event.type()).isEqualTo("TIMEOUT");
        assertThat(event.duelId()).isNull();
        assertThat(event.opponentId()).isNull();
        assertThat(event.opponentName()).isNull();
        assertThat(event.challengeId()).isNull();
        assertThat(event.message()).isNotBlank();
    }

    @Test
    @DisplayName("cancelled() creates event with type=CANCELLED")
    void cancelled_createsValidEvent() {
        MatchmakingEvent event = MatchmakingEvent.cancelled();

        assertThat(event.type()).isEqualTo("CANCELLED");
        assertThat(event.duelId()).isNull();
        assertThat(event.opponentId()).isNull();
        assertThat(event.opponentName()).isNull();
        assertThat(event.challengeId()).isNull();
        assertThat(event.message()).isNotBlank();
    }

    @Test
    @DisplayName("error() creates event with type=ERROR and custom message")
    void error_createsValidEvent() {
        String errorMessage = "Cannot queue while in a duel.";
        MatchmakingEvent event = MatchmakingEvent.error(errorMessage);

        assertThat(event.type()).isEqualTo("ERROR");
        assertThat(event.duelId()).isNull();
        assertThat(event.opponentId()).isNull();
        assertThat(event.opponentName()).isNull();
        assertThat(event.challengeId()).isNull();
        assertThat(event.message()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("error() with different messages creates different events")
    void error_multipleMessages() {
        MatchmakingEvent error1 = MatchmakingEvent.error("Error 1");
        MatchmakingEvent error2 = MatchmakingEvent.error("Error 2");

        assertThat(error1.message()).isEqualTo("Error 1");
        assertThat(error2.message()).isEqualTo("Error 2");
        assertThat(error1).isNotEqualTo(error2); // Different messages
    }

    @Test
    @DisplayName("matched() message includes opponent name")
    void matched_messageIncludesOpponentName() {
        MatchmakingEvent event = MatchmakingEvent.matched(1L, 2L, "Alice", 10L);

        assertThat(event.message()).contains("Alice");
    }

    @Test
    @DisplayName("all factory-created events are records (immutable)")
    void events_areImmutable() {
        MatchmakingEvent event1 = MatchmakingEvent.queued();
        MatchmakingEvent event2 = MatchmakingEvent.queued();

        // Same values should produce equal records
        assertThat(event1).isEqualTo(event2);
    }
}
