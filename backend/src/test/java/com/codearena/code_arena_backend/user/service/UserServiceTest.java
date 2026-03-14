package com.codearena.code_arena_backend.user.service;

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

    // TODO: add tests for existsByEmail, save, findByEmail
}
