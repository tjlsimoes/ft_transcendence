package com.codearena.code_arena_backend.user.controller;

import com.codearena.code_arena_backend.auth.LoginRateLimiter;
import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.challenge.entity.Challenge;
import com.codearena.code_arena_backend.challenge.entity.ChallengeDifficulty;
import com.codearena.code_arena_backend.challenge.repository.ChallengeRepository;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Account Deletion — Integration Tests")
class UserDeletionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private DuelRepository duelRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private LoginRateLimiter rateLimiter;

    @MockitoBean
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        duelRepository.deleteAll();
        challengeRepository.deleteAll();
        friendshipRepository.deleteAll();
        userRepository.deleteAll();

        ConcurrentHashMap<String, String> fakeRedis = new ConcurrentHashMap<>();
        org.springframework.data.redis.core.ValueOperations<String, String> valueOps =
                org.mockito.Mockito.mock(org.springframework.data.redis.core.ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.hasKey(any())).thenAnswer(inv -> fakeRedis.containsKey((String) inv.getArgument(0)));
        org.mockito.Mockito.doAnswer(inv -> {
            fakeRedis.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(valueOps).set(any(), any(), any(java.time.Duration.class));
        org.mockito.Mockito.doAnswer(inv -> {
            fakeRedis.remove((String) inv.getArgument(0));
            return true;
        }).when(redisTemplate).delete(any(String.class));

        ReflectionTestUtils.setField(rateLimiter, "attemptsCache", new ConcurrentHashMap<>());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                            //
    // ------------------------------------------------------------------ //

    private String registerAndLogin(String username, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(username, email, password))))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class)
                .getAccessToken();
    }

    private Challenge savedChallenge() {
        Challenge c = new Challenge();
        c.setTitle("Test Challenge");
        c.setDifficulty(ChallengeDifficulty.EASY);
        c.setTimeLimitSecs(60);
        c.setTestCases(objectMapper.createArrayNode());
        return challengeRepository.save(c);
    }

    private Duel savedDuel(Long challengerId, Long opponentId, Long challengeId) {
        Duel d = new Duel();
        d.setChallengerId(challengerId);
        d.setOpponentId(opponentId);
        d.setChallengeId(challengeId);
        d.setStatus(Duel.DuelStatus.COMPLETED);
        return duelRepository.save(d);
    }

    // ------------------------------------------------------------------ //
    //  Tests                                                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("No history – returns HTTP 204")
    void deleteAccount_noHistory_returns204() throws Exception {
        String token = registerAndLogin("alpha", "alpha@arena.com", "Password1!");

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("No history – user row removed from database")
    void deleteAccount_noHistory_userRemovedFromDb() throws Exception {
        String token = registerAndLogin("alpha", "alpha@arena.com", "Password1!");

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByUsername("alpha")).isEmpty();
    }

    @Test
    @DisplayName("With duel history – returns HTTP 204 (no FK violation from RESTRICT constraint)")
    void deleteAccount_withDuelHistory_returns204() throws Exception {
        String alphaToken = registerAndLogin("alpha", "alpha@arena.com", "Password1!");
        registerAndLogin("beta", "beta@arena.com", "Password2!");

        Long alphaId = userRepository.findByUsername("alpha").orElseThrow().getId();
        Long betaId  = userRepository.findByUsername("beta").orElseThrow().getId();
        savedDuel(alphaId, betaId, savedChallenge().getId());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + alphaToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("With duel history – duel preserved with null challengerId after deletion")
    void deleteAccount_withDuelHistory_duelPreservedWithNullChallengerId() throws Exception {
        String alphaToken = registerAndLogin("alpha", "alpha@arena.com", "Password1!");
        registerAndLogin("beta", "beta@arena.com", "Password2!");

        Long alphaId = userRepository.findByUsername("alpha").orElseThrow().getId();
        Long betaId  = userRepository.findByUsername("beta").orElseThrow().getId();
        Duel duel = savedDuel(alphaId, betaId, savedChallenge().getId());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + alphaToken))
                .andExpect(status().isNoContent());

        Duel surviving = duelRepository.findById(duel.getId()).orElseThrow();
        assertThat(surviving.getChallengerId()).isNull();
        assertThat(surviving.getOpponentId()).isEqualTo(betaId);
    }

    @Test
    @DisplayName("With duel history – survivor's match history shows 'Unknown' for deleted opponent")
    void deleteAccount_withDuelHistory_survivorMatchHistoryShowsUnknown() throws Exception {
        String alphaToken = registerAndLogin("alpha", "alpha@arena.com", "Password1!");
        String betaToken  = registerAndLogin("beta", "beta@arena.com", "Password2!");

        Long alphaId = userRepository.findByUsername("alpha").orElseThrow().getId();
        Long betaId  = userRepository.findByUsername("beta").orElseThrow().getId();
        savedDuel(alphaId, betaId, savedChallenge().getId());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + alphaToken))
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/api/users/me/matches")
                        .header("Authorization", "Bearer " + betaToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode matches = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(matches.isArray()).isTrue();
        assertThat(matches.size()).isGreaterThan(0);
        assertThat(matches.get(0).get("opponent").asText()).isEqualTo("Unknown");
    }

    @Test
    @DisplayName("With friendship – friendship rows cascade-removed on deletion")
    void deleteAccount_withFriendship_cascadeRemovesFriendshipRows() throws Exception {
        String alphaToken = registerAndLogin("alpha", "alpha@arena.com", "Password1!");
        registerAndLogin("beta", "beta@arena.com", "Password2!");

        Long alphaId = userRepository.findByUsername("alpha").orElseThrow().getId();
        Long betaId  = userRepository.findByUsername("beta").orElseThrow().getId();

        mockMvc.perform(post("/api/users/me/friends/" + betaId)
                        .header("Authorization", "Bearer " + alphaToken))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + alphaToken))
                .andExpect(status().isNoContent());

        assertThat(friendshipRepository.findAll())
                .noneMatch(f -> f.getUserId().equals(alphaId) || f.getFriendId().equals(alphaId));
    }

    @Test
    @DisplayName("With uploaded avatar – avatar file deleted from disk on account deletion")
    void deleteAccount_withAvatar_avatarFileDeletedFromDisk() throws Exception {
        String token = registerAndLogin("alpha", "alpha@arena.com", "Password1!");

        MvcResult uploadResult = mockMvc.perform(
                        multipart("/api/users/me/avatar")
                                .file(new MockMultipartFile("file", "avatar.png", "image/png",
                                        "fake-png-data".getBytes()))
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String avatarUrl = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .get("avatarUrl").asText();
        String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
        Path avatarPath = Paths.get("target/test-avatars", filename).toAbsolutePath();
        assertThat(avatarPath).exists();

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(avatarPath).doesNotExist();
    }
}
