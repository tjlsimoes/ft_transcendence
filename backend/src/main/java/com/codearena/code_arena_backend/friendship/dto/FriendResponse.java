package com.codearena.code_arena_backend.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for returning a friend entry to the client.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {

    private Long id;
    private String username;
    private String avatarUrl;
    private String league;
    private String status; // ONLINE, OFFLINE, IN_QUEUE, IN_DUEL
}
