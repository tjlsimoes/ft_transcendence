package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.user.dto.ProfileResponse;
import com.codearena.code_arena_backend.user.dto.UpdateProfileRequest;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService — especially the UserDetailsService contract.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — UserDetailsService contract")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("loadUserByUsername – returns UserDetails for existing user")
    void loadUserByUsername_existingUser_returnsDetails() {
        // Arrange
        User user = new User();
        user.setUsername("player1");
        user.setEmail("p1@arena.com");
        user.setPassword("$2a$hashed");

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));

        // Act
        UserDetails details = userService.loadUserByUsername("player1");

        // Assert
        assertThat(details.getUsername()).isEqualTo("player1");
        assertThat(details.getPassword()).isEqualTo("$2a$hashed");
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

    @Test
    @DisplayName("getCurrentUserProfile – returns mapped profile response")
    void getCurrentUserProfile_returnsProfile() {
        User user = createUser(1L, "player1", "p1@arena.com");
        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));

        ProfileResponse response = userService.getCurrentUserProfile("player1");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("player1");
        assertThat(response.getEmail()).isEqualTo("p1@arena.com");
    }

    @Test
    @DisplayName("updateCurrentUserProfile – updates username, email, avatar")
    void updateCurrentUserProfile_updatesFields() {
        User user = createUser(1L, "player1", "p1@arena.com");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("playerRenamed");
        request.setEmail("new@arena.com");
        request.setAvatar("https://cdn.example/avatar.png");

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("playerRenamed")).thenReturn(false);
        when(userRepository.findByEmail("new@arena.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponse response = userService.updateCurrentUserProfile("player1", request);

        assertThat(response.getUsername()).isEqualTo("playerRenamed");
        assertThat(response.getEmail()).isEqualTo("new@arena.com");
        assertThat(response.getAvatar()).isEqualTo("https://cdn.example/avatar.png");
    }

    @Test
    @DisplayName("updateCurrentUserProfile – rejects duplicate username")
    void updateCurrentUserProfile_duplicateUsername_throws() {
        User user = createUser(1L, "player1", "p1@arena.com");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("takenName");

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("takenName")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCurrentUserProfile("player1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    @DisplayName("updateCurrentUserProfile – rejects duplicate email")
    void updateCurrentUserProfile_duplicateEmail_throws() {
        User user = createUser(1L, "player1", "p1@arena.com");
        User anotherUser = createUser(2L, "player2", "taken@arena.com");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("taken@arena.com");

        when(userRepository.findByUsername("player1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@arena.com")).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.updateCurrentUserProfile("player1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    private User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$hashed");
        return user;
    }

    // TODO: add tests for existsByEmail, save, findByEmail
}
