package com.codearena.code_arena_backend.message.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.code_arena_backend.message.dto.ChatMessageResponse;
import com.codearena.code_arena_backend.message.service.ChatService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {
    private final ChatService chatService;

    @GetMapping("/{recipientId}")
    public Page<ChatMessageResponse> getConversation(
        Authentication authentication,
        @PathVariable Long recipientId,
        Pageable pageable) {
        return chatService.getConversation(authentication.getName(), recipientId, pageable);
    }

}
