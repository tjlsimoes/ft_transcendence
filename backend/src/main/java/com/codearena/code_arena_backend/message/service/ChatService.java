package com.codearena.code_arena_backend.message.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.codearena.code_arena_backend.message.dto.ChatMessageRequest;
import com.codearena.code_arena_backend.message.dto.ChatMessageResponse;
import com.codearena.code_arena_backend.message.entity.Message;
import com.codearena.code_arena_backend.message.repository.MessageRepository;
import com.codearena.code_arena_backend.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final MessageRepository msgRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public ChatMessageResponse send(Long senderId, ChatMessageRequest request) {
        userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("User (sender) not found"));
        userRepository.findById(request.getRecipientId()).orElseThrow(() -> new RuntimeException("User (recipient) not found"));
        if (senderId.equals(request.getRecipientId()))
            throw new RuntimeException("Sender and recipient cannot be the same user");
        Message msg = new Message(null, senderId, request.getRecipientId(), request.getContent(), null);
        msg = msgRepository.save(msg);

        ChatMessageResponse response = convertToResponse(msg);

        simpMessagingTemplate.convertAndSend(getTopicName(senderId, request.getRecipientId()), response);

        return response;
    }

    public Page<ChatMessageResponse> getConversation(Long user1Id, Long user2Id, Pageable pageable) {
        userRepository.findById(user1Id).orElseThrow(() -> new RuntimeException("User (user1) not found"));
        userRepository.findById(user2Id).orElseThrow(() -> new RuntimeException("User (user2) not found"));
        if (user1Id.equals(user2Id))
            throw new RuntimeException("User1 and user2 cannot be the same user");
        Page<Message> msgs = msgRepository.findBySenderAndRecipient(user1Id, user2Id, pageable);
        Page<ChatMessageResponse> responses = msgs.map(this::convertToResponse);
        return responses;
    }


    /**
     * Helper to generate a constant topic name for a pair of users,
     * e.g. /topic/chat/3-6, for users with ids 3 and 6, irregardless
     * of who's the sender or the recipient.
     * @param id1
     * @param id2
     * @return
     */
    private String getTopicName(Long id1, Long id2) {
        Long lowId = Math.min(id1, id2);
        Long highId = Math.max(id1, id2);
        return "/topic/chat/" + lowId + "-" + highId;
    }

    private ChatMessageResponse convertToResponse(Message msg) {
        return ChatMessageResponse.builder()
            .id(msg.getId())
            .senderId(msg.getSenderId())
            .recipientId(msg.getRecipientId())
            .content(msg.getContent())
            .createdAt(msg.getCreatedAt())
            .build();
    }
    
}
