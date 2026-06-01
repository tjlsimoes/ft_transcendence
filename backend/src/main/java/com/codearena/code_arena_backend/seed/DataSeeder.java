package com.codearena.code_arena_backend.seed;

import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "Password1!";

    @Override
    public void run(ApplicationArguments args) {
        log.info("DataSeeder: starting...");

        List<User> users = List.of(
            ensureUser("alice",   "alice@test.com"),
            ensureUser("bob",     "bob@test.com"),
            ensureUser("charlie", "charlie@test.com"),
            ensureUser("diana",   "diana@test.com"),
            ensureUser("eve",     "eve@test.com"),
            ensureUser("frank",   "frank@test.com")
        );

        // Wire every pair as mutual friends
        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                ensureFriendship(users.get(i).getId(), users.get(j).getId());
            }
        }

        log.info("DataSeeder: done. Users: {}",
            users.stream().map(User::getUsername).toList());
    }

    private User ensureUser(String username, String email) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(PASSWORD));
            User saved = userRepository.save(u);
            log.info("DataSeeder: created user '{}' (id={})", username, saved.getId());
            return saved;
        });
    }

    private void ensureFriendship(Long id1, Long id2) {
        if (!friendshipRepository.existsByUserIdAndFriendId(id1, id2)) {
            friendshipRepository.save(new Friendship(id1, id2, "ACCEPTED"));
            friendshipRepository.save(new Friendship(id2, id1, "ACCEPTED"));
            log.info("DataSeeder: friendship {} <-> {}", id1, id2);
        }
    }
}
