package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdateUserProfileRequest;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService — profile/friends/avatar flows")
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private Path tempAvatarDir;

    @BeforeEach
    void setUp() throws IOException {
        tempAvatarDir = Files.createTempDirectory("avatars-test-");
        ReflectionTestUtils.setField(userProfileService, "avatarStorageDir", tempAvatarDir.toString());
        ReflectionTestUtils.setField(userProfileService, "avatarBaseUrl", "/api/users/avatars");
        ReflectionTestUtils.setField(userProfileService, "defaultAvatarFilename", "default-avatar.svg");
        ReflectionTestUtils.invokeMethod(userProfileService, "initAvatarStorage");
    }

    @Test
    @DisplayName("getProfileById returns profile with game stats")
    void getProfileById_returnsStats() {
        User user = user(7L, "player7", User.UserStatus.ONLINE);
        user.setWins(12);
        user.setLosses(5);
        user.setElo(1460);
        user.setLeague(User.League.SILVER);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userProfileService.getProfileById(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("player7");
        assertThat(response.wins()).isEqualTo(12);
        assertThat(response.losses()).isEqualTo(5);
        assertThat(response.elo()).isEqualTo(1460);
        assertThat(response.league()).isEqualTo(User.League.SILVER);
    }

    @Test
    @DisplayName("updateMyProfile updates displayName and bio")
    void updateMyProfile_updatesFields() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse response = userProfileService.updateMyProfile(
                "player1",
                new UpdateUserProfileRequest("New Name", "Coder from 42")
        );

        assertThat(user.getDisplayName()).isEqualTo("New Name");
        assertThat(user.getBio()).isEqualTo("Coder from 42");
        assertThat(response.displayName()).isEqualTo("New Name");
        assertThat(response.bio()).isEqualTo("Coder from 42");
    }

    @Test
    @DisplayName("addFriend creates reciprocal friendships when missing")
    void addFriend_createsBidirectionalRows() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User friend = user(2L, "friend", User.UserStatus.ONLINE);

        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendshipRepository.existsByUserIdAndFriendId(1L, 2L)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(2L, 1L)).thenReturn(false);

        userProfileService.addFriend("me", 2L);

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository, times(2)).save(captor.capture());
        List<Friendship> savedRows = captor.getAllValues();

        assertThat(savedRows)
                .extracting(Friendship::getUserId, Friendship::getFriendId, Friendship::getStatus)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(1L, 2L, "ACCEPTED"),
                        org.assertj.core.groups.Tuple.tuple(2L, 1L, "ACCEPTED")
                );
    }

    @Test
    @DisplayName("listOnlineFriends returns only non-offline friends")
    void listOnlineFriends_filtersOffline() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User onlineFriend = user(2L, "alice", User.UserStatus.ONLINE);
        User offlineFriend = user(3L, "bob", User.UserStatus.OFFLINE);

        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(friendshipRepository.findByUserIdAndStatus(1L, "ACCEPTED")).thenReturn(List.of(
                new Friendship(1L, 2L, "ACCEPTED"),
                new Friendship(1L, 3L, "ACCEPTED")
        ));
        when(userRepository.findAllById(List.of(2L, 3L))).thenReturn(List.of(onlineFriend, offlineFriend));

        List<FriendSummaryResponse> response = userProfileService.listOnlineFriends("me");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(2L);
        assertThat(response.getFirst().online()).isTrue();
    }

    @Test
    @DisplayName("uploadMyAvatar stores file and updates avatar URL")
    void uploadMyAvatar_storesFileAndPersistsUrl() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.save(me)).thenReturn(me);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "png-data".getBytes()
        );

        UserProfileResponse response = userProfileService.uploadMyAvatar("me", file);

        assertThat(response.avatarUrl()).startsWith("/api/users/avatars/");
        assertThat(response.avatarUrl()).endsWith(".png");

        String filename = response.avatarUrl().substring(response.avatarUrl().lastIndexOf('/') + 1);
        assertThat(Files.exists(tempAvatarDir.resolve(filename))).isTrue();
    }

    private User user(Long id, String username, User.UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@arena.com");
        user.setPassword("hash");
        user.setDisplayName(username);
        user.setAvatar("/api/users/avatars/default-avatar.svg");
        user.setWins(0);
        user.setLosses(0);
        user.setElo(0);
        user.setLeague(User.League.BRONZE);
        user.setStatus(status);
        return user;
    }
}
