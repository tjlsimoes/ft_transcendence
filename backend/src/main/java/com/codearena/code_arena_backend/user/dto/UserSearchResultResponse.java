package com.codearena.code_arena_backend.user.dto;

import com.codearena.code_arena_backend.user.entity.RelationshipStatus;
import com.codearena.code_arena_backend.user.entity.User;

public record UserSearchResultResponse(
    Long id,
	String username,
	String avatarUrl,
    RelationshipStatus relationshipStatus // NONE | PENDING_OUTGOING | PENDING_INCOMING | FRIENDS
) {

	public static UserSearchResultResponse from(User user, RelationshipStatus relationshipStatus) {
        return new UserSearchResultResponse(user.getId(), user.getUsername(), user.getAvatar(), relationshipStatus);
    }
}