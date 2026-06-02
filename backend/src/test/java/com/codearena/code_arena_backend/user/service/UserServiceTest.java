package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.matchmaking.service.MatchmakingQueueService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService — especially the UserDetailsService contract.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @TempDir
    Path tempDir;

    @Mock private UserRepository userRepository;
    @Mock private MatchmakingQueueService matchmakingQueueService;
	@Mock private DuelRepository duelRepository;
	@Mock private FriendshipRepository friendshipRepository;
    @InjectMocks private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "avatarStorageDir", tempDir.toString());
    }

    @Test
    @DisplayName("loadUserByUsername – returns UserDetails for existing user")
    void loadUserByUsername_existingUser_returnsDetails() {
        // Arrange
        User user = new User();
        user.setUsername("player1");
        user.setEmail("p1@arena.com");
        user.setPassword("$2a$hashed");
        user.setRole(User.Role.USER);

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));

        // Act
        UserDetails details = userService.loadUserByUsername("player1");

        // Assert
        assertThat(details.getUsername()).isEqualTo("player1");
        assertThat(details.getPassword()).isEqualTo("$2a$hashed");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername – maps admin role to ROLE_ADMIN authority")
    void loadUserByUsername_adminRole_mapsAuthority() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@arena.com");
        admin.setPassword("$2a$hashed");
        admin.setRole(User.Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails details = userService.loadUserByUsername("admin");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername – throws UsernameNotFoundException for unknown user")
    void loadUserByUsername_unknownUser_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    @DisplayName("existsByUsername – delegates to repository")
    void existsByUsername_returnsRepositoryResult() {
        when(userRepository.existsByUsername("player1")).thenReturn(true);
        assertThat(userService.existsByUsername("player1")).isTrue();
    }

    // TODO: add tests for existsByEmail, save, findByEmail

    // ------------------------------------------------------------------ //
    //  deleteAccount                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("deleteAccount – throws NoSuchElementException when user not found")
    void deleteAccount_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount("ghost"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    @DisplayName("deleteAccount – no avatar: dequeues and deletes user without touching filesystem")
    void deleteAccount_noAvatar_deletesUserAndDequeues() {
        User user = new User();
        user.setId(42L);
        user.setUsername("player1");
        user.setAvatar(null);
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));

        userService.deleteAccount("player1");

        verify(matchmakingQueueService).dequeue(42L);
		verify(duelRepository).nullifyChallengerById(42L);
		verify(duelRepository).nullifyOpponentById(42L);
		verify(friendshipRepository).deleteAllByParticipant(42L);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteAccount – with avatar: deletes avatar file from disk before deleting user")
    void deleteAccount_withAvatar_deletesFileFromDisk() throws IOException {
        String filename = "test-avatar.png";
        Path avatarFile = tempDir.resolve(filename);
        Files.writeString(avatarFile, "fake-image-data");

        User user = new User();
        user.setId(7L);
        user.setUsername("player2");
        user.setAvatar("/api/users/avatars/" + filename);
        when(userRepository.findByUsername("player2")).thenReturn(Optional.of(user));

        userService.deleteAccount("player2");

        assertThat(avatarFile).doesNotExist();
		verify(duelRepository).nullifyChallengerById(7L);
		verify(duelRepository).nullifyOpponentById(7L);
		verify(friendshipRepository).deleteAllByParticipant(7L);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteAccount – missing avatar file on disk: does not throw, still deletes user")
    void deleteAccount_withMissingAvatarFile_stillDeletesUser() {
        User user = new User();
        user.setId(8L);
        user.setUsername("player3");
        user.setAvatar("/api/users/avatars/nonexistent-file.png");
        when(userRepository.findByUsername("player3")).thenReturn(Optional.of(user));

        assertThatCode(() -> userService.deleteAccount("player3")).doesNotThrowAnyException();
		verify(duelRepository).nullifyChallengerById(8L);
		verify(duelRepository).nullifyOpponentById(8L);
		verify(friendshipRepository).deleteAllByParticipant(8L);
        verify(userRepository).delete(user);
    }
}
