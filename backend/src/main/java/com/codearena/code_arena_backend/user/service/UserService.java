package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * UserService implements Spring Security's UserDetailsService interface.
 *
 * Spring Security calls loadUserByUsername() during authentication to convert
 * a raw username string into a fully populated UserDetails object (which
 * carries the password hash and the user's authorities/roles).
 *
 * By having UserService implement this interface, we plug our JPA-backed
 * user store directly into the Spring Security machinery — no XML, no
 * separate adapter class needed.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Called by Spring Security (and by JwtAuthenticationFilter) to load a user.
     *
     * @param username the subject stored in the JWT ('sub' claim)
     * @throws UsernameNotFoundException if no user with that username exists
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // ------------------------------------------------------------------ //
    //  Additional query helpers (to be expanded in future issues)         //
    // ------------------------------------------------------------------ //

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Converts our User entity into a Spring Security UserDetails.
     *
     * We use the built-in org.springframework.security.core.userdetails.User
     * builder and expose the persisted role as a Spring authority
     * (e.g. USER -> ROLE_USER, ADMIN -> ROLE_ADMIN).
     */
    private UserDetails toUserDetails(User user) {
        String authority = "ROLE_" + user.getRole().name();
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // already BCrypt-hashed
            .authorities(List.<GrantedAuthority>of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
