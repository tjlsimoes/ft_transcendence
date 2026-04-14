package com.codearena.code_arena_backend.health.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for HealthController.
 *
 * Spring Boot 4 removed @WebMvcTest. Because HealthController has no
 * dependencies we can instantiate it directly — no Spring context needed.
 *
 * Plain unit tests like this are the fastest possible: zero framework
 * startup cost, millisecond execution.
 */
@DisplayName("HealthController — GET /api/health")
class HealthControllerTest {

    // Directly instantiate: no IoC container needed for this controller.
    private final HealthController controller = new HealthController();

    @Test
    @DisplayName("health() – returns HTTP 200")
    void health_returns200() {
        ResponseEntity<Map<String, Object>> response = controller.health();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("health() – body contains 'status: UP'")
    void health_bodyContainsStatusUp() {
        Map<String, Object> body = controller.health().getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("health() – body contains timestamp and message")
    void health_bodyContainsExpectedFields() {
        Map<String, Object> body = controller.health().getBody();
        assertThat(body).isNotNull()
                .containsKey("timestamp")
                .containsKey("message");
    }

    // TODO: add @SpringBootTest integration test once a test DB profile is set up
}
