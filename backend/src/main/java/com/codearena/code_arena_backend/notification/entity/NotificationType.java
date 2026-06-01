package com.codearena.code_arena_backend.notification.entity;

/**
 * Defines the supported types of notifications in the system.
 */
public enum NotificationType {
    /** Two players have been matched and a duel is starting. */
    MATCH_FOUND,

    /** A user has received a new friend request. */
    FRIEND_REQUEST,

    /** A previously sent friend request was accepted. */
    FRIEND_ACCEPTED,

    /** A duel has concluded (win/loss/draw). */
    DUEL_RESULT,

    /** General system-wide announcement. */
    SYSTEM_ANNOUNCEMENT,

	// A new chat message was received while recipient had no window open
	CHAT_MESSAGE
}
