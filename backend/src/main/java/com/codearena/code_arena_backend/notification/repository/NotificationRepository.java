package com.codearena.code_arena_backend.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codearena.code_arena_backend.notification.entity.Notification;
import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Unread notifications, newest first
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
