package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codearena.code_arena_backend.user.dto.UserProfileResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Loads usernames for multiple user IDs in a single SELECT … WHERE id IN (…) query.
     * Use this instead of calling findById() inside a loop to avoid N+1 queries.
     *
     * @param ids the set of user IDs to resolve
     * @return map of userId → username; absent IDs are simply not present in the map
     */
    public Map<Long, String> getUsernamesByIds(Collection<Long> ids) {
        return userRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
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

    /**
     * Enriches a UserProfileResponse with ranking context for Master/Legend players.
     * - Determines if the player is in the top 1% of ALL players (Legend).
     * - A player must also have elo >= 3000 (Master) to qualify.
     * - For MASTER: sets legendThresholdLp (LP needed to reach Legend).
     * - For LEGEND: sets globalRank and highestLp.
     */
    public UserProfileResponse enrichWithRankingContext(UserProfileResponse response) {
        if (response.elo() < 3000) {
            return response;
        }

        long totalPlayers = userRepository.countAllPlayers();
        long legendCutoff = Math.max(1, (long) Math.ceil(totalPlayers * 0.01));

        // The player's global rank (0-based count of players above them)
        long playersAbove = userRepository.countPlayersWithEloAbove(response.elo());
        // 1-based rank
        long globalRank = playersAbove + 1;

        boolean isLegend = globalRank <= legendCutoff;

        // Find the Legend threshold: elo of the player at position legendCutoff
        Integer legendThreshold = userRepository.findEloAtGlobalRank(legendCutoff - 1).orElse(3000);

        if (isLegend) {
            int highestLp = userRepository.findHighestElo().orElse(response.elo());
            return response.withLegendContext((int) globalRank, highestLp);
        } else {
            return response.withMasterContext(legendThreshold);
        }
    }

    /**
     * Sets status to ONLINE and persists.
     * Does NOT touch the league column — league only changes when elo changes (match result).
     * Login never changes elo, so it can never change who is LEGEND vs MASTER.
     */
    public void goOnline(User user) {
        user.setStatus(User.UserStatus.ONLINE);
        userRepository.save(user);
    }

    /**
     * Recalculates the stored league for ALL Master+ players in a single atomic SQL UPDATE.
     * Uses DENSE_RANK so tied elo values share the same rank.
     * Call this after any elo change (match result), not on login.
     */
    @Transactional
    public void recalculateLeagues() {
        userRepository.recalculateMasterLeagues();
    }

    /**
     * Periodic safety net: keeps the stored league column in sync with the
     * current elo rankings every 5 minutes.
     * Runs asynchronously — the main request path is never blocked by this.
     */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void scheduledLeagueRecalculation() {
        userRepository.recalculateMasterLeagues();
    }

    /**
     * Sets the user status to OFFLINE (logout / session expiry).
     */
    public void goOffline(User user) {
        user.setStatus(User.UserStatus.OFFLINE);
        userRepository.save(user);
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
