package com.codearena.code_arena_backend.notification.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.code_arena_backend.notification.NotificationService;
import com.codearena.code_arena_backend.notification.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping()
    public Page<NotificationResponse> getNotifications(Authentication authentication, Pageable pageable) {
        return notificationService.getNotifications(authentication.getName(), pageable);
    }

    @GetMapping("unread")
    public List<NotificationResponse> getUnreadNotifications(Authentication authentication) {
        return notificationService.getUnreadNotifications(authentication.getName());
    }

    @PatchMapping("{id}/read")
    public void markAsRead(Authentication authentication, @PathVariable Long id) {
        notificationService.markAsRead(id, authentication.getName());
    }

}
