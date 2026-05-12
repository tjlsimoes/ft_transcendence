package com.codearena.code_arena_backend.message.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.codearena.code_arena_backend.message.dto.ChatMessageRequest;
import com.codearena.code_arena_backend.message.service.ChatService;
import com.codearena.code_arena_backend.user.entity.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat")
    public void sendMessage(@AuthenticationPrincipal User user, ChatMessageRequest request) {
        chatService.send(user.getId(), request);
    }
}
