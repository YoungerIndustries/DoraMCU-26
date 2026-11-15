package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.service.SpectatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SpectatorWebSocketHandler Tests")
class SpectatorWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_SESSION_ID = "test-session-1";

    private SpectatorWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;
    @Mock
    private SpectatorService spectatorService;

    @BeforeEach
    void setUp() throws Exception {
        handler = new SpectatorWebSocketHandler(spectatorService);

        when(wsSession1.getId()).thenReturn("ws1abcdefg");
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/spectator"));
        when(wsSession1.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession1).sendMessage(any(TextMessage.class));

        when(wsSession2.getId()).thenReturn("ws2abcdefg");
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/spectator"));
        when(wsSession2.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession2).sendMessage(any(TextMessage.class));

        // Mock joinSession to return a valid game state so join succeeds
        SpectatorService.SpectatorGameState gameState = new SpectatorService.SpectatorGameState.Builder()
                .sessionId(TEST_SESSION_ID)
                .currentTurn(1)
                .playerCount(2)
                .timestamp(Instant.now())
                .build();
        when(spectatorService.joinSession(anyString(), anyString())).thenReturn(Optional.of(gameState));
        when(spectatorService.getSpectatorGameState(anyString())).thenReturn(Optional.of(gameState));
        when(spectatorService.getEventHistory(anyString(), any(int.class))).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Connection established sends available sessions")
    void connectionEstablished() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        // getSpectatorCount returns spectatorStates.size(), which is 0 until a join
        assertEquals(0, handler.getSpectatorCount());
        verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Connection closed removes session")
    void connectionClosed() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getSpectatorCount());
    }

    @Test
    @DisplayName("Join spectator session")
    void joinSpectatorSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getSpectatorCount());
        assertTrue(handler.getActiveSpectatorSessions().contains(TEST_SESSION_ID));
    }

    @Test
    @DisplayName("Leave spectator session")
    void leaveSpectatorSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getSpectatorCount());

        ObjectNode leave = objectMapper.createObjectNode();
        leave.put("type", "leave");
        handler.handleTextMessage(wsSession1, new TextMessage(leave.toString()));

        assertEquals(0, handler.getSpectatorCount());
    }

    @Test
    @DisplayName("Camera mode change")
    void cameraModeChange() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        ObjectNode camera = objectMapper.createObjectNode();
        camera.put("type", "camera_mode");
        camera.put("mode", "free");
        handler.handleTextMessage(wsSession1, new TextMessage(camera.toString()));

        assertEquals(1, handler.getSpectatorCount());
    }

    @Test
    @DisplayName("Multiple spectators on same session")
    void multipleSpectatorsSameSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        ObjectNode join1 = objectMapper.createObjectNode();
        join1.put("type", "join");
        join1.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join1.toString()));

        ObjectNode join2 = objectMapper.createObjectNode();
        join2.put("type", "join");
        join2.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession2, new TextMessage(join2.toString()));

        assertEquals(2, handler.getSpectatorCount());
    }

    @Test
    @DisplayName("List available sessions")
    void listSessions() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode list = objectMapper.createObjectNode();
        list.put("type", "list");
        handler.handleTextMessage(wsSession1, new TextMessage(list.toString()));

        verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Invalid message type is handled gracefully")
    void invalidMessageType() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode invalid = objectMapper.createObjectNode();
        invalid.put("type", "invalid_type");
        handler.handleTextMessage(wsSession1, new TextMessage(invalid.toString()));

        // Should not throw; no spectator state created
        assertEquals(0, handler.getSpectatorCount());
    }

    @Test
    @DisplayName("Disconnect removes spectator state")
    void disconnectRemovesState() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getSpectatorCount());
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getSpectatorCount());
    }
}
