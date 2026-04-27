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

import java.util.Date;
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

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

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
        when(jwtService.generateToken(userDetails)).thenReturn("mock.access.token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("mock.refresh.token");

        // Act
        AuthResponse response = authService.register(req);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("mock.refresh.token");
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
        when(jwtService.generateToken(userDetails)).thenReturn("mock.access.token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("mock.refresh.token");

        // Act
        AuthResponse response = authService.login(req);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("mock.refresh.token");
    }

    @Test
    @DisplayName("refreshToken – rotates tokens and blacklists the old jti")
    void refreshToken_rotatesAndBlacklists() {
        // Arrange
        String oldRefreshToken = "old.refresh.token";
        UserDetails userDetails = mockUserDetails("player1");
        Date expiry = new Date(System.currentTimeMillis() + 100000);

        when(jwtService.extractUsername(oldRefreshToken)).thenReturn("player1");
        when(jwtService.extractJti(oldRefreshToken)).thenReturn("jti-123");
        when(tokenBlacklistService.isBlacklisted("jti-123")).thenReturn(false);
        when(userService.loadUserByUsername("player1")).thenReturn(userDetails);
        when(jwtService.isTokenValid(oldRefreshToken, userDetails, "refresh")).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("new.access.token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("new.refresh.token");
        when(jwtService.extractExpiration(oldRefreshToken)).thenReturn(expiry);

        // Act
        AuthResponse response = authService.refreshToken(oldRefreshToken);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
        verify(tokenBlacklistService).blacklistToken(eq("jti-123"), any());
    }

    @Test
    @DisplayName("logout – blacklists both tokens")
    void logout_blacklistsBoth() {
        // Arrange
        String accessToken = "Bearer access.token";
        String refreshToken = "refresh.token";
        Date accExpiry = new Date(System.currentTimeMillis() + 100000);
        Date refExpiry = new Date(System.currentTimeMillis() + 100000);

        when(jwtService.extractExpiration("access.token")).thenReturn(accExpiry);
        when(jwtService.extractJti(refreshToken)).thenReturn("jti-456");
        when(jwtService.extractExpiration(refreshToken)).thenReturn(refExpiry);

        // Act
        authService.logout(accessToken, refreshToken);

        // Assert
        verify(tokenBlacklistService).blacklistToken(eq("access.token"), any());
        verify(tokenBlacklistService).blacklistToken(eq("jti-456"), any());
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
