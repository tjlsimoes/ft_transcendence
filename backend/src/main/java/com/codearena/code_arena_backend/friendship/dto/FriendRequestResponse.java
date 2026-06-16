package com.codearena.code_arena_backend.friendship.dto;

import java.time.LocalDateTime;

public record FriendRequestResponse(
	Long requesterId,
	String username,
	String avatarUrl,
	LocalDateTime requestedAt
) {

}
