package com.codearena.code_arena_backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtService.
 *
 * We use @ExtendWith(MockitoExtension) instead of @SpringBootTest because
 * we only want to test this one class in isolation — no database, no context.
 *
 * ReflectionTestUtils.setField lets us inject @Value fields without starting
 * the full Spring environment.
 *
 * Testing strategy:
 *   - "happy path" first (valid token flows)
 *   - edge cases / failure paths marked as TODO for later
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService — token generation and validation")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    // A 256-bit (32 bytes) Base64-encoded secret — safe minimum for HS256.
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2";
    private static final long TEST_EXPIRATION_MS = 3_600_000L; // 1 hour

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually (no Spring context needed).
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION_MS);

        testUser = User.builder()
                .username("player1")
                .password("hashed")
                .authorities(List.of())
                .build();
    }

    @Test
    @DisplayName("generateToken – produces a non-null, non-blank token")
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername – returns the correct subject claim")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        String extracted = jwtService.extractUsername(token);
        assertThat(extracted).isEqualTo("player1");
    }

    @Test
    @DisplayName("isTokenValid – returns true for fresh token and correct user")
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid – returns false for token belonging to another user")
    void isTokenValid_returnsFalseForWrongUser() {
        String token = jwtService.generateToken(testUser);

        UserDetails anotherUser = User.builder()
                .username("player2")
                .password("hashed")
                .authorities(List.of())
                .build();

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    // TODO: test that an expired token returns false (needs a -1 ms expiration)
    // TODO: test that a tampered token throws JwtException
}
