package com.codearena.code_arena_backend.notification.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.code_arena_backend.notification.NotificationService;
import com.codearena.code_arena_backend.notification.dto.NotificationResponse;
import com.codearena.code_arena_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping()
    public Page<NotificationResponse> getNotifications(@AuthenticationPrincipal User userDetails, Pageable pageable) {
        return notificationService.getNotifications(userDetails.getId(), pageable);
    }

    @GetMapping("unread")
    public List<NotificationResponse> getUnreadNotifications(@AuthenticationPrincipal User userDetails) {
        return notificationService.getUnreadNotifications(userDetails.getId());
    }

    @PatchMapping("{id}/read")
    public void markAsRead(@AuthenticationPrincipal User userDetails, @PathVariable Long id) {
        notificationService.markAsRead(id, userDetails.getUsername());
    }

}
