package com.codearena.code_arena_backend.notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.codearena.code_arena_backend.notification.dto.NotificationResponse;
import com.codearena.code_arena_backend.notification.entity.Notification;
import com.codearena.code_arena_backend.notification.entity.NotificationType;
import com.codearena.code_arena_backend.notification.repository.NotificationRepository;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void send(Long userId, NotificationType type, Object payload) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type.name());
        notification.setPayload(objectMapper.valueToTree(payload));
        notification.setRead(false);
        notificationRepository.save(notification);

        userRepository.findById(userId).ifPresent(user -> {
                NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .userId(userId)
                .type(notification.getType())
                .payload(payload)
                .read(false)
                .createdAt(notification.getCreatedAt())
                .build();

            messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                response
            );
        });
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            userRepository.findByUsername(username).ifPresent(user -> {
                if (!notification.getRead() && notification.getUserId().equals(user.getId())) {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                }
            });
        });
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::convertToResponse);
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private NotificationResponse convertToResponse(Notification notification) {
        Object payload = null;

        try {
            payload = objectMapper.convertValue(notification.getPayload(), Object.class);
        } catch (Exception e) {
            log.error("Failed to parse notification payload");
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .payload(payload)
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
