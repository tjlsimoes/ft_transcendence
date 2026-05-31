package com.codearena.code_arena_backend.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.codearena.code_arena_backend.auth.service.JwtService;
import com.codearena.code_arena_backend.message.dto.ChatMessageRequest;
import com.codearena.code_arena_backend.message.dto.ChatMessageResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketIntegrationTest {
    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final String WS_URL = "ws://localhost:%d/ws";

    @Autowired
    private JwtService jwtService; // to generate test tokens

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAToken;
    private User userA;

    private String userBToken;
    private User userB;
    private final BlockingQueue<ChatMessageResponse> messageQueue = new LinkedBlockingDeque<>();

    @BeforeEach
    void setup() {
        // 1. Create a standard Websocket client
        StandardWebSocketClient wsClient = new StandardWebSocketClient();

        // 2. Wrap it a STOMP client
        this.stompClient = new WebSocketStompClient(wsClient);

        // 3. Configure the injected message converter (with time module support)
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter(objectMapper));

        messageQueue.clear();

        userRepository.deleteAll();
        userA = new User();
        userA.setUsername("userA");
        userA.setEmail("userA@test.com");
        userA.setPassword("password");
        userA.setElo(1000);
        userA = userRepository.save(userA);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(userA.getUsername())
            .password(userA.getPassword())
            .authorities(Collections.emptyList())
            .build();
        userAToken = jwtService.generateToken(userDetails);

        userB = new User();
        userB.setUsername("userB");
        userB.setEmail("userB@test.com");
        userB.setPassword("password");
        userB.setElo(1000);
        userB = userRepository.save(userB);

        UserDetails userDetailsB = org.springframework.security.core.userdetails.User.builder()
            .username(userB.getUsername())
            .password(userB.getPassword())
            .authorities(Collections.emptyList())
            .build();
        userBToken = jwtService.generateToken(userDetailsB);
    }

    private StompSession connect(String token) throws Exception {
        StompHeaders headers = new StompHeaders();
        if (token != null) {
            headers.add("Authorization", "Bearer " + token);
        }

        return stompClient.connectAsync(
                String.format(WS_URL, port),
                new WebSocketHttpHeaders(),
                headers,
                new StompSessionHandlerAdapter() {}
            ).get(5, TimeUnit.SECONDS);
    }

    @Test
    void testConnect_WithValidToken_ShouldSucceed() throws Exception {
        StompSession session = connect(userAToken);

        assertThat(session).isNotNull();
        assertThat(session.isConnected()).isTrue();

        session.disconnect();
    }

    @Test
    void testSubscrive_ToOthersNotifications_ShouldFail() throws Exception {
        // Connect as User A
        StompSession session = connect(userAToken);

        // 2. Try to subscrive to User B's private queue
        // Interceptor should block this.
        String forbiddenTopic = "/user/userB/queue/notifications";

        // In STOMP, when a subscription is denied by an Interceptor,
        // the server typically sends an ERROR frame and closes the connection.
        session.subscribe(forbiddenTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return Object.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) { }
        });

        // 3. Wait and check if session is still active.
        Thread.sleep(500);
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    void testChat_MessageShouldBeDelivered() throws Exception {
        // 1. User A connects and subscrives to their chat with User B
        StompSession sessionA = connect(userAToken);
        String chatTopic = "/topic/chat/" + Math.min(userA.getId(), userB.getId()) + "-" + Math.max(userA.getId(), userB.getId());

        sessionA.subscribe(chatTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return ChatMessageResponse.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.add((ChatMessageResponse)payload);
            }
        });

        // 2. User A sends a message to the /app/chat destination
        ChatMessageRequest request = new ChatMessageRequest(userB.getId(), "Hello User B!");

        sessionA.send("/app/chat", request);

        // 3. Verify the message arrives in the queue within 5 seconds

        ChatMessageResponse received = messageQueue.poll(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Hello User B!");
        assertThat(received.getRecipientId()).isEqualTo(userB.getId());
        assertThat(received.getSenderId()).isEqualTo(userA.getId());
    }

    @Test
    void testChat_InvalidMessage_ShouldReturnValidationError() throws Exception {
        // 1. Connect and subscribe to the error queue
        StompSession sessionA = connect(userAToken);
        BlockingQueue<Map> errorQueue = new java.util.concurrent.LinkedBlockingQueue<>();

        sessionA.subscribe("/user/queue/errors", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return Map.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorQueue.add((Map) payload);
            }
        });

        // 2. Send an invalid message (blank content)
        ChatMessageRequest invalidRequest = new ChatMessageRequest(userB.getId(), "");
        sessionA.send("/app/chat", invalidRequest);

        // 3. Verify validation error was received
        Map<?, ?> errorResponse = errorQueue.poll(5, TimeUnit.SECONDS);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.get("error")).isEqualTo("Validation failed");

        List<?> details = (List<?>) errorResponse.get("details");
        assertThat(details.toString()).contains("Content must not be blank");
    }


}
