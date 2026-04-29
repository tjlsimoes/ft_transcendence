package com.codearena.code_arena_backend.user.controller;

import com.codearena.code_arena_backend.duel.dto.MatchHistoryResponse;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.entity.Duel.DuelStatus;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.friendship.dto.FriendResponse;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdateUserProfileRequest;
import com.codearena.code_arena_backend.user.dto.UserAvatarResource;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserProfileService;
import com.codearena.code_arena_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for authenticated user operations.
 *
 * All endpoints require a valid JWT (no permitAll here).
 * The authenticated user is resolved via Authentication or @AuthenticationPrincipal.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserService userService;
    private final DuelRepository duelRepository;
    private final FriendshipRepository friendshipRepository;

    // ------------------------------------------------------------------ //
    //  Profile endpoints (from profile branch)                            //
    // ------------------------------------------------------------------ //

    /**
     * GET /api/users/me
     * Returns the profile and game statistics of the currently authenticated user,
     * enriched with ranking context for Master/Legend players.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        UserProfileResponse response = UserProfileResponse.from(user);
        response = userService.enrichWithRankingContext(response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getProfileById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(userProfileService.updateMyProfile(username, request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadMyAvatar(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(userProfileService.uploadMyAvatar(username, file));
    }

    // ------------------------------------------------------------------ //
    //  Friends endpoints                                                  //
    // ------------------------------------------------------------------ //

    @GetMapping("/me/friends")
    public ResponseEntity<List<FriendSummaryResponse>> listMyFriends(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userProfileService.listMyFriends(username));
    }

    @PostMapping("/me/friends/{id}")
    public ResponseEntity<Void> addFriend(Authentication authentication, @PathVariable Long id) {
        String username = authentication.getName();
        userProfileService.addFriend(username, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/me/friends/{id}")
    public ResponseEntity<Void> removeFriend(Authentication authentication, @PathVariable Long id) {
        String username = authentication.getName();
        userProfileService.removeFriend(username, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/online")
    public ResponseEntity<List<FriendSummaryResponse>> listOnlineFriends(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userProfileService.listOnlineFriends(username));
    }

    // ------------------------------------------------------------------ //
    //  Match history endpoint (from duel/ranking branch)                  //
    // ------------------------------------------------------------------ //

    /**
     * GET /api/users/me/matches
     * Returns the match history of the currently authenticated user.
     */
    @GetMapping("/me/matches")
    public ResponseEntity<List<MatchHistoryResponse>> getMyMatches(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<Duel> duels = duelRepository.findByUserId(user.getId());

        // Collect all opponent IDs, then resolve usernames in a single query (avoids N+1).
        Set<Long> opponentIds = duels.stream()
                .map(d -> d.getChallengerId().equals(user.getId()) ? d.getOpponentId() : d.getChallengerId())
                .collect(Collectors.toSet());
        Map<Long, String> usernameById = userService.getUsernamesByIds(opponentIds);

        List<MatchHistoryResponse> history = duels.stream()
                .map(duel -> {
                    boolean isChallenger = duel.getChallengerId().equals(user.getId());
                    Long opponentId = isChallenger ? duel.getOpponentId() : duel.getChallengerId();
                    String opponentName = usernameById.getOrDefault(opponentId, "Unknown");

                    // Determine result from winnerId (set by the judge/duel service).
                    String result;
                    if (duel.getStatus() == DuelStatus.DRAW) {
                        result = "DRAW";
                    } else if (duel.getStatus() == DuelStatus.COMPLETED && duel.getWinnerId() != null) {
                        result = duel.getWinnerId().equals(user.getId()) ? "VICTORY" : "DEFEAT";
                    } else if (duel.getStatus() == DuelStatus.CANCELLED) {
                        result = "CANCELLED";
                    } else {
                        result = "PENDING";
                    }

                    // Determine LP change for the requesting user.
                    Integer lpChange = isChallenger
                            ? duel.getChallengerEloChange()
                            : duel.getOpponentEloChange();

                    return MatchHistoryResponse.builder()
                            .id(duel.getId())
                            .result(result)
                            .opponent(opponentName)
                            .status(duel.getStatus().name())
                            .lpChange(lpChange)
                            .startedAt(duel.getStartedAt())
                            .endedAt(duel.getEndedAt())
                            .build();
                })
                .toList();

        return ResponseEntity.ok(history);
    }

    // ------------------------------------------------------------------ //
    //  Avatar serving                                                     //
    // ------------------------------------------------------------------ //

    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getAvatar(@PathVariable String filename) {
        UserAvatarResource avatar = userProfileService.loadAvatar(filename);
        return ResponseEntity.ok()
                .contentType(avatar.mediaType())
                .body(avatar.resource());
    }

    // ------------------------------------------------------------------ //
    //  Exception handlers                                                 //
    // ------------------------------------------------------------------ //

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleServerError(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }
}
