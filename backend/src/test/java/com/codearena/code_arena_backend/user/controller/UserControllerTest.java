package com.codearena.code_arena_backend.user.controller;

import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdateUserProfileRequest;
import com.codearena.code_arena_backend.user.dto.UserAvatarResource;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserProfileService;
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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — user profile endpoints")
class UserControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("getProfileById returns HTTP 200 with profile payload")
    void getProfileById_returnsProfile() {
        UserProfileResponse profile = new UserProfileResponse(
                5L,
                "player5",
                "Player Five",
                "bio",
                "/api/users/avatars/default-avatar.svg",
                10,
                2,
                1200,
                User.League.SILVER,
                User.UserStatus.ONLINE
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
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("New Name", "new bio");
        UserProfileResponse profile = new UserProfileResponse(
                1L,
                "player1",
                "New Name",
                "new bio",
                "/api/users/avatars/default-avatar.svg",
                1,
                1,
                1000,
                User.League.BRONZE,
                User.UserStatus.OFFLINE
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
}
