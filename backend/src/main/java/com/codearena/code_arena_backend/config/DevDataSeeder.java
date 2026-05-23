package com.codearena.code_arena_backend.config;

import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Temporary database seeder for development.
 * This runs on application startup when the 'dev' profile is active.
 * It automatically creates a 'devuser' so the frontend can login without using the broken register mechanism.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String username = "devuser";
        if (!userService.existsByUsername(username)) {
            log.info("Creating default dev user: {} / password123", username);
            
            User devUser = new User();
            devUser.setUsername(username);
            devUser.setEmail("dev@codearena.dev");
            devUser.setDisplayName("Dev User");
            devUser.setRole(User.Role.USER);
            devUser.setAvatar("/api/users/avatars/default-avatar.svg");
            // Hash the password so it works with the standard login endpoint
            devUser.setPassword(passwordEncoder.encode("password123"));
            
            userService.save(devUser);
        } else {
            log.info("Dev user '{}' already exists. You can login with password 'password123'", username);
        }
    }
}
