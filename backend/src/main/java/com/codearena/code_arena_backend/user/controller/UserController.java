package com.codearena.code_arena_backend.user.controller;

import com.codearena.code_arena_backend.duel.dto.MatchHistoryResponse;
import com.codearena.code_arena_backend.duel.entity.Duel;
import com.codearena.code_arena_backend.duel.entity.Duel.DuelStatus;
import com.codearena.code_arena_backend.duel.repository.DuelRepository;
import com.codearena.code_arena_backend.friendship.dto.FriendResponse;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for authenticated user operations.
 *
 * All endpoints require a valid JWT (no permitAll here).
 * The authenticated user is resolved via @AuthenticationPrincipal.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DuelRepository duelRepository;
    private final FriendshipRepository friendshipRepository;

    /**
     * GET /api/users/me
     * Returns the profile and game statistics of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        UserProfileResponse response = UserProfileResponse.fromEntity(user);
        userService.enrichWithRankingContext(response);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/me/matches
     * Returns the match history of the currently authenticated user.
     */
    @GetMapping("/me/matches")
    public ResponseEntity<List<MatchHistoryResponse>> getMyMatches(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<MatchHistoryResponse> history = duelRepository.findByUserId(user.getId())
                .stream()
                .map(duel -> {
                    boolean isChallenger = duel.getChallengerId().equals(user.getId());
                    Long opponentId = isChallenger ? duel.getOpponentId() : duel.getChallengerId();
                    String opponentName = userService.findById(opponentId)
                            .map(User::getUsername)
                            .orElse("Unknown");

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

    /**
     * GET /api/users/me/friends
     * Returns the friend list of the currently authenticated user.
     */
    @GetMapping("/me/friends")
    public ResponseEntity<List<FriendResponse>> getMyFriends(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<FriendResponse> friends = friendshipRepository.findAcceptedByUserId(user.getId())
                .stream()
                .map(friendship -> {
                    Long friendId = friendship.getUserId().equals(user.getId())
                            ? friendship.getFriendId()
                            : friendship.getUserId();
                    return userService.findById(friendId)
                            .map(friend -> FriendResponse.builder()
                                    .id(friend.getId())
                                    .username(friend.getUsername())
                                    .avatarUrl(friend.getAvatar())
                                    .league(UserProfileResponse.leagueFromElo(friend.getElo()))
                                    .status(friend.getStatus().name())
                                    .build())
                            .orElse(null);
                })
                .filter(f -> f != null)
                .toList();

        return ResponseEntity.ok(friends);
    }
}
