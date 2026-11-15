package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.GameEngine;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GameStateWebSocketHandler Tests")
class GameStateWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TEST_GAME_ID = "game-session-1";

    private GameStateWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;
    @Mock
    private GameSessionService gameSessionService;

    @BeforeEach
    void setUp() throws Exception {
        handler = new GameStateWebSocketHandler(gameSessionService);

        when(wsSession1.getId()).thenReturn("ws1");
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/game-state"));
        when(wsSession1.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession1).sendMessage(any(TextMessage.class));

        when(wsSession2.getId()).thenReturn("ws2");
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/game-state"));
        when(wsSession2.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession2).sendMessage(any(TextMessage.class));

        // Mock a valid GameSession so join succeeds
        GameEngine mockEngine = mock(GameEngine.class);
        when(mockEngine.getGameType()).thenReturn(GameType.DORAMCU_4X);
        GameSession mockSession = new GameSession(TEST_GAME_ID, "Test Game", mockEngine);
        when(gameSessionService.getSession(anyString())).thenReturn(mockSession);
    }

    @Test
    @DisplayName("Connection established successfully")
    void connectionEstablished() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        assertEquals(1, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Connection closed removes session")
    void connectionClosed() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Join game session")
    void joinGameSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_GAME_ID);
        join.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getConnectedSessions());
        assertEquals(1, handler.getPlayerCount(TEST_GAME_ID));
    }

    @Test
    @DisplayName("Leave game session")
    void leaveGameSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_GAME_ID);
        join.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getPlayerCount(TEST_GAME_ID));

        ObjectNode leave = objectMapper.createObjectNode();
        leave.put("type", "leave");
        handler.handleTextMessage(wsSession1, new TextMessage(leave.toString()));

        assertEquals(0, handler.getPlayerCount(TEST_GAME_ID));
    }

    @Test
    @DisplayName("Request game state")
    void requestGameState() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_GAME_ID);
        join.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        ObjectNode state = objectMapper.createObjectNode();
        state.put("type", "state_request");
        handler.handleTextMessage(wsSession1, new TextMessage(state.toString()));

        verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Player action message")
    void playerAction() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_GAME_ID);
        join.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        ObjectNode action = objectMapper.createObjectNode();
        action.put("type", "action");
        action.put("actionType", "EXPLORE");
        handler.handleTextMessage(wsSession1, new TextMessage(action.toString()));
    }

    @Test
    @DisplayName("Multiple players in same game")
    void multiplePlayersSameGame() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        ObjectNode join1 = objectMapper.createObjectNode();
        join1.put("type", "join");
        join1.put("sessionId", TEST_GAME_ID);
        join1.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join1.toString()));

        ObjectNode join2 = objectMapper.createObjectNode();
        join2.put("type", "join");
        join2.put("sessionId", TEST_GAME_ID);
        join2.put("playerId", "2");
        handler.handleTextMessage(wsSession2, new TextMessage(join2.toString()));

        assertEquals(2, handler.getConnectedSessions());
        assertEquals(2, handler.getPlayerCount(TEST_GAME_ID));
    }

    @Test
    @DisplayName("List active sessions")
    void listActiveSessions() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_GAME_ID);
        join.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        var activeSessions = handler.getActiveSessions();
        assertFalse(activeSessions.isEmpty());
    }

    @Test
    @DisplayName("Invalid message type is handled gracefully")
    void invalidMessageType() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode invalid = objectMapper.createObjectNode();
        invalid.put("type", "invalid_type");
        handler.handleTextMessage(wsSession1, new TextMessage(invalid.toString()));

        assertEquals(1, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Disconnect removes player from game")
    void disconnectRemovesPlayer() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode join = objectMapper.createObjectNode();
        join.put("type", "join");
        join.put("sessionId", TEST_GAME_ID);
        join.put("playerId", "1");
        handler.handleTextMessage(wsSession1, new TextMessage(join.toString()));

        assertEquals(1, handler.getPlayerCount(TEST_GAME_ID));
        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedSessions());
        assertEquals(0, handler.getPlayerCount(TEST_GAME_ID));
    }
}
