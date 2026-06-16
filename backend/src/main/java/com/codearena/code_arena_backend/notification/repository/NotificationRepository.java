package com.codearena.code_arena_backend.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codearena.code_arena_backend.notification.entity.Notification;
import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Unread notifications, newest first
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Marks unread CHAT_MESSAGE notifications from a specific sender as read.
    // Used when removing a friend, so messages they sent before the removal
    // don't leave a permanently stuck unread badge (the sender's avatar/entry
    // is gone from the friends list, so there's no other UI path to clear them).
    @Modifying
    @Query(value = "UPDATE notifications SET read = true WHERE user_id = :userId AND type = 'CHAT_MESSAGE' " +
                   "AND (payload->>'senderId')::bigint = :senderId AND read = false", nativeQuery = true)
    void markChatNotificationsReadFromSender(@Param("userId") Long userId, @Param("senderId") Long senderId);
}
