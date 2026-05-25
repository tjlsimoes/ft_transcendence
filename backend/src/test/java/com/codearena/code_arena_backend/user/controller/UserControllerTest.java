package com.codearena.code_arena_backend.user.controller;

import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.entity.Duel.DuelStatus;
import com.codearena.code_arena_backend.duel.dto.MatchHistoryResponse;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdateUserProfileRequest;
import com.codearena.code_arena_backend.user.dto.UserAvatarResource;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserProfileService;
import com.codearena.code_arena_backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — user profile endpoints")
class UserControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UserService userService;

    @Mock
    private DuelRepository duelRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private UserController userController;

    private static UserProfileResponse testProfile(
            Long id, String username, String displayName, String bio,
            String avatarUrl, int wins, int losses, int elo,
            String league, String status
    ) {
        return new UserProfileResponse(
                id, username, null, displayName, bio, avatarUrl,
                wins, losses, 0, elo, league, status,
                LocalDateTime.now(),
                null, null, null
        );
    }

    @Test
    @DisplayName("getProfileById returns HTTP 200 with profile payload")
    void getProfileById_returnsProfile() {
        UserProfileResponse profile = testProfile(
                5L, "player5", "Player Five", "bio",
                "/api/users/avatars/default-avatar.svg",
                10, 2, 1200, "SILVER", "ONLINE"
        );
        when(userProfileService.getProfileById(5L)).thenReturn(profile);

        ResponseEntity<UserProfileResponse> response = userController.getProfileById(5L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("player5");
    }

    @Test
    @DisplayName("updateMyProfile uses authenticated username")
    void updateMyProfile_usesAuthenticationName() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("player1", null);
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("New Name", "email@email.com", "new bio");
        UserProfileResponse profile = testProfile(
                1L, "player1", "New Name", "new bio",
                "/api/users/avatars/default-avatar.svg",
                1, 1, 1000, "BRONZE", "OFFLINE"
        );

        when(userProfileService.updateMyProfile("player1", request)).thenReturn(profile);

        ResponseEntity<UserProfileResponse> response = userController.updateMyProfile(auth, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().displayName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("addFriend returns HTTP 201")
    void addFriend_returns201() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("player1", null);

        ResponseEntity<Void> response = userController.addFriend(auth, 7L);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        verify(userProfileService).addFriend("player1", 7L);
    }

    @Test
    @DisplayName("listOnlineFriends returns online friend list")
    void listOnlineFriends_returnsList() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("player1", null);
        when(userProfileService.listOnlineFriends("player1")).thenReturn(List.of(
                new FriendSummaryResponse(2L, "alice", "Alice", "/a.png", User.UserStatus.ONLINE, true)
        ));

        ResponseEntity<List<FriendSummaryResponse>> response = userController.listOnlineFriends(auth);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().online()).isTrue();
    }

    @Test
    @DisplayName("getAvatar returns resource and media type")
    void getAvatar_returnsFileResource() {
        UserAvatarResource avatar = new UserAvatarResource(
                new ByteArrayResource("svg".getBytes()),
                MediaType.valueOf("image/svg+xml")
        );
        when(userProfileService.loadAvatar("default-avatar.svg")).thenReturn(avatar);

        ResponseEntity<org.springframework.core.io.Resource> response =
                userController.getAvatar("default-avatar.svg");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("image/svg+xml"));
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleNotFound returns HTTP 404")
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, String>> response =
                userController.handleNotFound(new NoSuchElementException("User not found: 9"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).contains("9");
    }

    // ------------------------------------------------------------------ //
    //  deleteMyAccount                                                     //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("deleteMyAccount – returns HTTP 204 and delegates to userService")
    void deleteMyAccount_returns204AndDelegates() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("player1", null);

        ResponseEntity<Void> response = userController.deleteMyAccount(auth);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(userService).deleteAccount("player1");
    }

    // ------------------------------------------------------------------ //
    //  getMyMatches – null-safety after opponent/challenger deletion       //
    // ------------------------------------------------------------------ //

    private UserDetails userDetailsFor(String username) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username).password("pw").roles("USER").build();
    }

    private User entityFor(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setRole(User.Role.USER);
        return u;
    }

    @Test
    @DisplayName("getMyMatches – challenger deleted: current user identified as opponent, lpChange from opponentEloChange")
    void getMyMatches_challengerDeleted_currentUserIsOpponent() {
        Long currentUserId = 10L;
        User currentUser = entityFor(currentUserId, "beta");
        UserDetails principal = userDetailsFor("beta");

        // Challenger was deleted → challengerId is null; current user is the opponent
        Duel duel = new Duel(1L, null, currentUserId, 5L, null,
                DuelStatus.COMPLETED, null, null, null, 15);

        when(userService.findByUsername("beta")).thenReturn(Optional.of(currentUser));
        when(duelRepository.findByUserId(currentUserId)).thenReturn(List.of(duel));

        ResponseEntity<List<MatchHistoryResponse>> response = userController.getMyMatches(principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MatchHistoryResponse match = response.getBody().getFirst();
        assertThat(match.getOpponent()).isEqualTo("Unknown");
        assertThat(match.getLpChange()).isEqualTo(15);
        assertThat(match.getResult()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("getMyMatches – opponent deleted: current user identified as challenger, lpChange from challengerEloChange")
    void getMyMatches_opponentDeleted_currentUserIsChallenger() {
        Long currentUserId = 10L;
        User currentUser = entityFor(currentUserId, "alpha");
        UserDetails principal = userDetailsFor("alpha");

        // Opponent was deleted → opponentId is null; current user is the challenger and winner
        Duel duel = new Duel(2L, currentUserId, null, 5L, currentUserId,
                DuelStatus.COMPLETED, null, null, -10, null);

        when(userService.findByUsername("alpha")).thenReturn(Optional.of(currentUser));
        when(duelRepository.findByUserId(currentUserId)).thenReturn(List.of(duel));

        ResponseEntity<List<MatchHistoryResponse>> response = userController.getMyMatches(principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MatchHistoryResponse match = response.getBody().getFirst();
        assertThat(match.getOpponent()).isEqualTo("Unknown");
        assertThat(match.getResult()).isEqualTo("VICTORY");
        assertThat(match.getLpChange()).isEqualTo(-10);
    }

    @Test
    @DisplayName("getMyMatches – both participants present: resolves opponent username")
    void getMyMatches_bothPresent_resolvesOpponentName() {
        Long currentUserId = 10L;
        Long opponentId = 20L;
        User currentUser = entityFor(currentUserId, "alpha");
        User opponent = entityFor(opponentId, "beta");
        UserDetails principal = userDetailsFor("alpha");

        Duel duel = new Duel(3L, currentUserId, opponentId, 5L, null,
                DuelStatus.DRAW, null, null, 5, null);

        when(userService.findByUsername("alpha")).thenReturn(Optional.of(currentUser));
        when(duelRepository.findByUserId(currentUserId)).thenReturn(List.of(duel));
        when(userService.findById(opponentId)).thenReturn(Optional.of(opponent));

        ResponseEntity<List<MatchHistoryResponse>> response = userController.getMyMatches(principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MatchHistoryResponse match = response.getBody().getFirst();
        assertThat(match.getOpponent()).isEqualTo("beta");
        assertThat(match.getResult()).isEqualTo("DRAW");
    }
}
