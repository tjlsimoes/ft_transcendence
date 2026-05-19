package com.codearena.code_arena_backend.message.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.codearena.code_arena_backend.message.dto.ChatMessageRequest;
import com.codearena.code_arena_backend.message.service.ChatService;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserRepository userRepository;

    @MessageMapping("/chat")
    public void sendMessage(Principal principal, ChatMessageRequest request) {
        User user = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        chatService.send(user.getId(), request);
    }
}
