package com.codearena.code_arena_backend.auth;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.auth.service.JwtService;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Integration Tests — registration, login, and refresh flows")
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private LoginRateLimiter rateLimiter;

        @Autowired
        private JwtService jwtService;

        @org.springframework.test.context.bean.override.mockito.MockitoBean
        private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

        private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUp() {
                userRepository.deleteAll();

                // Mock Redis behavior using a simple map
                java.util.Map<String, String> fakeRedis = new java.util.concurrent.ConcurrentHashMap<>();
                org.springframework.data.redis.core.ValueOperations<String, String> valueOps = org.mockito.Mockito
                                .mock(org.springframework.data.redis.core.ValueOperations.class);

                when(redisTemplate.opsForValue()).thenReturn(valueOps);

                // Stub hasKey: check if key exists in our fake map
                when(redisTemplate.hasKey(any())).thenAnswer(invocation -> {
                        String key = invocation.getArgument(0);
                        return fakeRedis.containsKey(key);
                });

                // Stub set: add to our fake map
                org.mockito.Mockito.doAnswer(invocation -> {
                        String key = invocation.getArgument(0);
                        String value = invocation.getArgument(1);
                        fakeRedis.put(key, value);
                        return null;
                }).when(valueOps).set(any(), any(), any(java.time.Duration.class));

                // Clear rate limiter
                org.springframework.test.util.ReflectionTestUtils.setField(rateLimiter, "attemptsCache",
                                new java.util.concurrent.ConcurrentHashMap<>());
        }

        @Test
        @DisplayName("Full auth flow: register -> login -> refresh")
        void fullAuthFlow() throws Exception {
                // 1. Register
                RegisterRequest regReq = new RegisterRequest("newuser", "new@arena.com", "SecurePass123");
                MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regReq)))
                                .andExpect(status().isCreated())
                                .andReturn();

                AuthResponse regResponse = objectMapper.readValue(regResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                assertThat(regResponse.getAccessToken()).isNotBlank();
                assertThat(regResponse.getRefreshToken()).isNotBlank();

                // 2. Login
                LoginRequest loginReq = new LoginRequest("newuser", "SecurePass123");
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                assertThat(loginResponse.getAccessToken()).isNotBlank();
                assertThat(loginResponse.getRefreshToken()).isNotBlank();

                // 3. Refresh
                Map<String, String> refreshReq = Map.of("refreshToken", loginResponse.getRefreshToken());
                MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshReq)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse refreshResponse = objectMapper.readValue(refreshResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                assertThat(refreshResponse.getAccessToken()).isNotBlank();
                assertThat(refreshResponse.getRefreshToken()).isNotBlank();
        }

        @Test
        @DisplayName("Token Rotation: using a refresh token twice fails")
        void tokenRotationTest() throws Exception {
                // 1. Register and Login
                RegisterRequest regReq = new RegisterRequest("rotateuser", "rotate@arena.com", "Pass123!");
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regReq)))
                                .andExpect(status().isCreated());

                LoginRequest loginReq = new LoginRequest("rotateuser", "Pass123!");
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse response1 = objectMapper.readValue(loginResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                String oldRefreshToken = response1.getRefreshToken();

                // 2. Refresh #1 (Success, rotates token)
                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                                .andExpect(status().isOk());

                // 3. Refresh #2 (Failure, old token is blacklisted)
                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefreshToken))))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Logout: revokes both tokens")
        void logoutRevokesTokensTest() throws Exception {
                // 1. Login
                RegisterRequest regReq = new RegisterRequest("logoutuser", "logout@arena.com", "Pass123!");
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regReq)))
                                .andExpect(status().isCreated());

                LoginRequest loginReq = new LoginRequest("logoutuser", "Pass123!");
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                                .andExpect(status().isOk())
                                .andReturn();

                AuthResponse response = objectMapper.readValue(loginResult.getResponse().getContentAsString(),
                                AuthResponse.class);
                String accessToken = response.getAccessToken();
                String refreshToken = response.getRefreshToken();

                // 2. Logout
                mockMvc.perform(post("/api/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                                .andExpect(status().isNoContent());

                // 3. Verify Refresh fails
                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Login fails with invalid credentials")
        void loginFailsWithInvalidCredentials() throws Exception {
                // Register first
                RegisterRequest regReq = new RegisterRequest("user1", "u1@arena.com", "SecurePass123");
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regReq)))
                                .andExpect(status().isCreated());

                // Login with wrong password
                LoginRequest loginReq = new LoginRequest("user1", "wrongpass");
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Rate limiting blocks excessive login attempts")
        void rateLimitingBlocksExcessiveAttempts() throws Exception {
                LoginRequest loginReq = new LoginRequest("anyuser", "anypass");

                // First 5 attempts (assuming MAX_ATTEMPTS=5 in LoginRateLimiter)
                for (int i = 0; i < 5; i++) {
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginReq)));
                }

                // 6th attempt should be blocked
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                                .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("Refresh fails when token is expired")
        void refreshTokenFailsWhenExpired() throws Exception {
                // 1. Register a user to have someone to "own" the token
                RegisterRequest regReq = new RegisterRequest("expireduser", "expired@arena.com", "Pass123!");
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(regReq)))
                                .andExpect(status().isCreated());

                org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername("expireduser")
                                .password("Pass123!")
                                .authorities("USER")
                                .build();

                // 2. Generate an already-expired token (-10 seconds)
                String expiredToken = jwtService.generateToken(
                                java.util.Map.of("jti", java.util.UUID.randomUUID().toString()),
                                userDetails,
                                -10000,
                                "refresh");

                // 3. Try to refresh
                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                                .writeValueAsString(java.util.Map.of("refreshToken", expiredToken))))
                                .andExpect(status().isBadRequest());
        }
}
