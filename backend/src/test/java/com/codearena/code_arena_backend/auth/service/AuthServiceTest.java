package com.codearena.code_arena_backend.auth.service;

import com.codearena.code_arena_backend.auth.dto.AuthResponse;
import com.codearena.code_arena_backend.auth.dto.LoginRequest;
import com.codearena.code_arena_backend.auth.dto.RegisterRequest;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * @Mock creates lightweight Mockito mocks — no Spring context is started.
 * @InjectMocks creates the real AuthService and injects the mocks into it.
 *
 * Pattern used: Arrange → Act → Assert (AAA).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — register and login flows")
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Inject @Value field — jwt expiration (ms)
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3_600_000L);
        ReflectionTestUtils.setField(authService, "defaultAvatarUrl", "/api/users/avatars/default-avatar.svg");
    }

    // ------------------------------------------------------------------ //
    //  register()                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("register – happy path returns a non-null token")
    void register_happyPath_returnsToken() {
        // Arrange
        RegisterRequest req = new RegisterRequest("player1", "p1@arena.com", "pass1234");
        UserDetails userDetails = mockUserDetails("player1");

        when(userService.existsByUsername("player1")).thenReturn(false);
        when(userService.existsByEmail("p1@arena.com")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$...");
        when(userService.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.loadUserByUsername("player1")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mock.jwt.token");

        // Act
        AuthResponse response = authService.register(req);

        // Assert
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getDisplayName()).isEqualTo("player1");
        assertThat(savedUser.getAvatar()).isEqualTo("/api/users/avatars/default-avatar.svg");
        assertThat(savedUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    @DisplayName("register – throws when username is already taken")
    void register_duplicateUsername_throwsIllegalArgument() {
        // Arrange
        RegisterRequest req = new RegisterRequest("player1", "p1@arena.com", "pass1234");
        when(userService.existsByUsername("player1")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    @DisplayName("register – throws when email is already registered")
    void register_duplicateEmail_throwsIllegalArgument() {
        // Arrange
        RegisterRequest req = new RegisterRequest("player1", "taken@arena.com", "pass1234");
        when(userService.existsByUsername("player1")).thenReturn(false);
        when(userService.existsByEmail("taken@arena.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    // ------------------------------------------------------------------ //
    //  login()                                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("login – valid credentials return a token")
    void login_validCredentials_returnsToken() {
        // Arrange
        LoginRequest req = new LoginRequest("player1", "pass1234");
        UserDetails userDetails = mockUserDetails("player1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authenticate() just needs to not throw
        when(userService.loadUserByUsername("player1")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mock.jwt.token");

        // Act
        AuthResponse response = authService.login(req);

        // Assert
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
    }

    @Test
    @DisplayName("login – bad credentials propagate BadCredentialsException")
    void login_badCredentials_throws() {
        // Arrange
        LoginRequest req = new LoginRequest("player1", "wrongpass");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private UserDetails mockUserDetails(String username) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("hashed")
                .authorities(List.of())
                .build();
    }
}
