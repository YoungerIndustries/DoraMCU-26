package com.yisy.modules.doramcu.controller;
import com.yisy.modules.doramcu.controller.session.ReplayWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.model.GameActionLog;
import com.yisy.modules.doramcu.service.game.GameReplayService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReplayWebSocketHandler Tests")
class ReplayWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_SESSION_ID = "replay-demo-1";

    private ReplayWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;
    @Mock
    private GameReplayService gameReplayService;

    @BeforeEach
    void setUp() throws Exception {
        handler = new ReplayWebSocketHandler(gameReplayService);

        when(wsSession1.getId()).thenReturn("ws1");
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/replay"));
        when(wsSession1.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession1).sendMessage(any(TextMessage.class));

        when(wsSession2.getId()).thenReturn("ws2");
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/replay"));
        when(wsSession2.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession2).sendMessage(any(TextMessage.class));

        // Mock service to return a sample action log so join succeeds
        GameActionLog sampleLog = GameActionLog.builder(GameActionLog.ActionType.GAME_STARTED, TEST_SESSION_ID)
                .playerId(1L)
                .turnNumber(1)
                .timestamp(Instant.now())
                .build();
        when(gameReplayService.getSessionLogs(anyString())).thenReturn(Collections.singletonList(sampleLog));
    }

    @Test
    @DisplayName("Connection established sends available sessions")
    void connectionEstablished() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        // getConnectedClientCount returns replayStates.size(), which is 0 until a join
        assertEquals(0, handler.getConnectedClientCount());
        verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Connection closed removes session")
    void connectionClosed() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("Join replay session creates replay state")
    void joinReplaySession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("Leave replay session via disconnect")
    void leaveReplaySession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getConnectedClientCount());

        // Disconnect removes the replay state
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("Play command starts playback")
    void playCommand() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        ObjectNode play = objectMapper.createObjectNode();
        play.put("type", "play");
        handler.handleTextMessage(wsSession1, new TextMessage(play.toString()));

        // Should not throw; playback is started
        assertEquals(1, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("Pause command pauses playback")
    void pauseCommand() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        ObjectNode play = objectMapper.createObjectNode();
        play.put("type", "play");
        handler.handleTextMessage(wsSession1, new TextMessage(play.toString()));

        ObjectNode pause = objectMapper.createObjectNode();
        pause.put("type", "pause");
        handler.handleTextMessage(wsSession1, new TextMessage(pause.toString()));
    }

    @Test
    @DisplayName("Seek command changes playback position")
    void seekCommand() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        ObjectNode seek = objectMapper.createObjectNode();
        seek.put("type", "seek");
        seek.put("position", 0);
        handler.handleTextMessage(wsSession1, new TextMessage(seek.toString()));

        assertEquals(1, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("List sessions returns available replays")
    void listSessions() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode list = objectMapper.createObjectNode();
        list.put("type", "list_sessions");
        handler.handleTextMessage(wsSession1, new TextMessage(list.toString()));

        verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Multiple clients can join same replay")
    void multipleClientsSameReplay() throws Exception {
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

        assertEquals(2, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("Disconnect removes replay state")
    void disconnectRemovesState() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_SESSION_ID);
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getConnectedClientCount());
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedClientCount());
    }

    @Test
    @DisplayName("Invalid message type is handled gracefully")
    void invalidMessageType() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode invalid = objectMapper.createObjectNode();
        invalid.put("type", "invalid_type");
        handler.handleTextMessage(wsSession1, new TextMessage(invalid.toString()));

        // Should not throw; no replay state created
        assertEquals(0, handler.getConnectedClientCount());
    }
}
