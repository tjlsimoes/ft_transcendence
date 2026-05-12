package com.codearena.code_arena_backend.message.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.code_arena_backend.message.dto.ChatMessageResponse;
import com.codearena.code_arena_backend.message.service.ChatService;
import com.codearena.code_arena_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatService chatService;

    @GetMapping("/{recipientId}")
    public Page<ChatMessageResponse> getConversation(
        @AuthenticationPrincipal User user,
        @PathVariable Long recipientId,
        Pageable pageable) {
        return chatService.getConversation(user.getId(), recipientId, pageable);
    }

}
