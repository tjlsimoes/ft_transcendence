package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.friendship.dto.FriendNotificationPayload;
import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.notification.NotificationService;
import com.codearena.code_arena_backend.notification.entity.NotificationType;
import com.codearena.code_arena_backend.ranking.service.RankingService;
import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdatePasswordRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RankingService rankingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserProfileService userProfileService;

    private Path tempAvatarDir;

    @BeforeEach
    void setUp() throws IOException {
        tempAvatarDir = Files.createTempDirectory("avatars-test-");
        ReflectionTestUtils.setField(userProfileService, "avatarStorageDir", tempAvatarDir.toString());
        ReflectionTestUtils.setField(userProfileService, "avatarBaseUrl", "/api/users/avatars");
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
        when(rankingService.getLeagueFromElo(1460)).thenReturn("SILVER");

        UserProfileResponse response = userProfileService.getProfileById(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("player7");
        assertThat(response.wins()).isEqualTo(12);
        assertThat(response.losses()).isEqualTo(5);
        assertThat(response.elo()).isEqualTo(1460);
        assertThat(response.league()).isEqualTo("SILVER");
    }

    @Test
    @DisplayName("updateMyProfile updates displayName and bio")
    void updateMyProfile_updatesFields() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(rankingService.getLeagueFromElo(0)).thenReturn("BRONZE");

        UserProfileResponse response = userProfileService.updateMyProfile(
                "player1",
                new UpdateUserProfileRequest("New Name", null, "Coder from 42")
        );

        assertThat(user.getDisplayName()).isEqualTo("New Name");
        assertThat(user.getBio()).isEqualTo("Coder from 42");
        assertThat(response.displayName()).isEqualTo("New Name");
        assertThat(response.bio()).isEqualTo("Coder from 42");
    }

    @Test
    @DisplayName("sendFriendRequest saves a single PENDING row and notifies the target")
    void sendFriendRequest_savesPendingRowAndNotifies() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User target = user(2L, "friend", User.UserStatus.ONLINE);

        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());

        userProfileService.sendFriendRequest("me", 2L);

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getFriendId()).isEqualTo(2L);
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");

        ArgumentCaptor<FriendNotificationPayload> payloadCaptor = ArgumentCaptor.forClass(FriendNotificationPayload.class);
        verify(notificationService).send(eq(2L), eq(NotificationType.FRIEND_REQUEST), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(payloadCaptor.getValue().getUsername()).isEqualTo("me");
    }

    @Test
    @DisplayName("sendFriendRequest rejects requesting yourself")
    void sendFriendRequest_throwsWhenTargetIsSelf() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> userProfileService.sendFriendRequest("me", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You cannot add yourself as a friend");
    }

    @Test
    @DisplayName("sendFriendRequest rejects a duplicate pending request")
    void sendFriendRequest_throwsWhenAlreadyPending() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User target = user(2L, "friend", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L))
                .thenReturn(Optional.of(new Friendship(1L, 2L, "PENDING", LocalDateTime.now())));

        assertThatThrownBy(() -> userProfileService.sendFriendRequest("me", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Friend request already sent");
    }

    @Test
    @DisplayName("sendFriendRequest rejects requesting an existing friend")
    void sendFriendRequest_throwsWhenAlreadyFriends() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User target = user(2L, "friend", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L))
                .thenReturn(Optional.of(new Friendship(1L, 2L, "ACCEPTED", LocalDateTime.now())));

        assertThatThrownBy(() -> userProfileService.sendFriendRequest("me", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You are already friend with this user");
    }

    @Test
    @DisplayName("sendFriendRequest auto-accepts when the target had already requested me")
    void sendFriendRequest_autoAcceptsOnMutualRequest() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User target = user(2L, "friend", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(friendshipRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L))
                .thenReturn(Optional.of(new Friendship(2L, 1L, "PENDING", LocalDateTime.now())));

        userProfileService.sendFriendRequest("me", 2L);

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Friendship::getUserId, Friendship::getFriendId, Friendship::getStatus)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(2L, 1L, "ACCEPTED"),
                        org.assertj.core.groups.Tuple.tuple(1L, 2L, "ACCEPTED")
                );
        verify(notificationService).send(eq(2L), eq(NotificationType.FRIEND_ACCEPTED), any());
        verify(notificationService, never()).send(eq(2L), eq(NotificationType.FRIEND_REQUEST), any());
    }

    @Test
    @DisplayName("acceptFriendRequest flips the row to ACCEPTED and creates the mirror row")
    void acceptFriendRequest_flipsStatusAndMirrors() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        Friendship pending = new Friendship(2L, 1L, "PENDING", LocalDateTime.now());
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(pending));

        userProfileService.acceptFriendRequest("me", 2L);

        assertThat(pending.getStatus()).isEqualTo("ACCEPTED");
        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Friendship::getUserId, Friendship::getFriendId, Friendship::getStatus)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(2L, 1L, "ACCEPTED"),
                        org.assertj.core.groups.Tuple.tuple(1L, 2L, "ACCEPTED")
                );
        verify(notificationService).send(eq(2L), eq(NotificationType.FRIEND_ACCEPTED), any());
    }

    @Test
    @DisplayName("acceptFriendRequest throws when there is no pending request from that user")
    void acceptFriendRequest_throwsWhenNoPendingRequest() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.acceptFriendRequest("me", 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No pending friend request from this user");
    }

    @Test
    @DisplayName("rejectFriendRequest deletes the pending row")
    void rejectFriendRequest_deletesPendingRow() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        Friendship pending = new Friendship(2L, 1L, "PENDING", LocalDateTime.now());
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.of(pending));

        userProfileService.rejectFriendRequest("me", 2L);

        verify(friendshipRepository).delete(pending);
    }

    @Test
    @DisplayName("rejectFriendRequest throws when there is no pending request from that user")
    void rejectFriendRequest_throwsWhenNoPendingRequest() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(friendshipRepository.findByUserIdAndFriendId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.rejectFriendRequest("me", 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No pending friend request from this user");
    }

    @Test
    @DisplayName("listOnlineFriends returns only non-offline friends")
    void listOnlineFriends_filtersOffline() {
        User me = user(1L, "me", User.UserStatus.ONLINE);
        User onlineFriend = user(2L, "alice", User.UserStatus.ONLINE);
        User offlineFriend = user(3L, "bob", User.UserStatus.OFFLINE);

        when(userRepository.findByUsername("me")).thenReturn(Optional.of(me));
        when(friendshipRepository.findByUserIdAndStatus(1L, "ACCEPTED")).thenReturn(List.of(
                new Friendship(1L, 2L, "ACCEPTED", LocalDateTime.now()),
                new Friendship(1L, 3L, "ACCEPTED", LocalDateTime.now())
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
        when(rankingService.getLeagueFromElo(0)).thenReturn("BRONZE");

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

    @Test
    @DisplayName("updateMyProfile updates email when it is unique")
    void updateMyProfile_updatesEmail_success() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        user.setEmail("old@email.com");

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null, "new@email.com", null);
        UserProfileResponse response = userProfileService.updateMyProfile("player1", request);

        assertThat(user.getEmail()).isEqualTo("new@email.com");
        assertThat(response.email()).isEqualTo("new@email.com");
    }

    @Test
    @DisplayName("updateMyProfile throws exception when new email already exists")
    void updateMyProfile_updatesEmail_alreadyExists() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        user.setEmail("old@email.com");
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@email.com")).thenReturn(true);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null, "new@email.com", null);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userProfileService.updateMyProfile("player1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }


    @Test
    @DisplayName("updatePassword updates password when current is correct")
    void updatesPassword_success() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        user.setPassword("old_hash");

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "old_hash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_hash");

        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword123");
        userProfileService.updatePassword("player1", request);

        assertThat(user.getPassword()).isEqualTo("new_hash");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updatePassword throws exception when current password is incorrect")
    void updatePassword_incorrectCurrentPassword() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        user.setPassword("old_hash");
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "old_hash")).thenReturn(false);

        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPassword", "newPassword123");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userProfileService.updatePassword("player1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Incorrect current password");
    }

    @Test
    @DisplayName("updatePassword throws exception when new password is the same as current password")
    void updatePassword_samePassword() {
        User user = user(1L, "player1", User.UserStatus.OFFLINE);
        user.setPassword("old_hash");
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("samePassword", "old_hash")).thenReturn(true);

        UpdatePasswordRequest request = new UpdatePasswordRequest("samePassword", "samePassword");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userProfileService.updatePassword("player1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password cannot be the same as current password");
    }

    private User user(Long id, String username, User.UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@arena.com");
        user.setPassword("hash");
        user.setDisplayName(username);
        user.setWins(0);
        user.setLosses(0);
        user.setElo(0);
        user.setLeague(User.League.BRONZE);
        user.setStatus(status);
        return user;
    }
}
