package com.codearena.code_arena_backend.message.service;

import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.message.dto.ChatMessageRequest;
import com.codearena.code_arena_backend.message.dto.ChatMessageResponse;
import com.codearena.code_arena_backend.message.entity.Message;
import com.codearena.code_arena_backend.message.repository.MessageRepository;
import com.codearena.code_arena_backend.notification.NotificationService;
import com.codearena.code_arena_backend.notification.entity.NotificationType;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService — friends-only messaging")
class ChatServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository msgRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private ChatService chatService;

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    @Test
    @DisplayName("send saves the message and notifies the recipient when sender and recipient are friends")
    void send_savesMessageAndNotifies_whenFriends() {
        User sender = user(1L, "alice");
        User recipient = user(2L, "bob");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsByUserIdAndFriendIdAndStatus(1L, 2L, "ACCEPTED")).thenReturn(true);
        when(msgRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(99L);
            msg.setCreatedAt(LocalDateTime.now());
            return msg;
        });

        ChatMessageRequest request = new ChatMessageRequest(2L, "hello bob");
        ChatMessageResponse response = chatService.send(1L, request);

        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getRecipientId()).isEqualTo(2L);
        assertThat(response.getContent()).isEqualTo("hello bob");

        verify(simpMessagingTemplate).convertAndSend(eq("/topic/chat/1-2"), any(ChatMessageResponse.class));
        verify(notificationService).send(eq(2L), eq(NotificationType.CHAT_MESSAGE), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("send throws and never persists a message when sender and recipient are not friends")
    void send_throws_whenNotFriends() {
        User sender = user(1L, "alice");
        User recipient = user(2L, "bob");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsByUserIdAndFriendIdAndStatus(1L, 2L, "ACCEPTED")).thenReturn(false);

        ChatMessageRequest request = new ChatMessageRequest(2L, "hello bob");

        assertThatThrownBy(() -> chatService.send(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can only message your friends");

        verify(msgRepository, never()).save(any());
        verify(notificationService, never()).send(any(), any(), any());
    }
}
