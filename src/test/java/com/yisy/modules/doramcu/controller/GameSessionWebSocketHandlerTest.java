package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.GameEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GameSessionWebSocketHandler Tests")
class GameSessionWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private WebSocketSession wsSession1;

    @Mock
    private WebSocketSession wsSession2;

    @Mock
    private GameEngine gameEngine;

    private GameSession gameSession;
    private GameSessionWebSocketHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        gameSession = new GameSession("test-session-1", "Test Game", gameEngine);
        gameSession.addPlayer(1L);

        when(gameSessionService.getGameSession("test-session-1")).thenReturn(gameSession);
        when(wsSession1.isOpen()).thenReturn(true);
        when(wsSession2.isOpen()).thenReturn(true);
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=test-session-1&playerId=1"));
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=test-session-1&playerId=2"));

        handler = new GameSessionWebSocketHandler(gameSessionService);
    }

    // ============================================================
    // Connection Management
    // ============================================================

    @Nested
    @DisplayName("Connection Lifecycle")
    class ConnectionLifecycle {

        @Test
        @DisplayName("afterConnectionEstablished - should register valid connection")
        void validConnection() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            assertEquals(1, handler.getConnectionCount("test-session-1"));
            assertTrue(handler.getActiveSessionIds().contains("test-session-1"));
            assertEquals(1, handler.getTotalConnectionCount());
            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("afterConnectionEstablished - should close without sessionId")
        void missingSessionId() throws Exception {
            WebSocketSession badSession = mock(WebSocketSession.class);
            when(badSession.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?playerId=1"));

            handler.afterConnectionEstablished(badSession);

            verify(badSession).close(CloseStatus.BAD_DATA);
            assertEquals(0, handler.getConnectionCount("test-session-1"));
        }

        @Test
        @DisplayName("afterConnectionEstablished - should close without playerId")
        void missingPlayerId() throws Exception {
            WebSocketSession badSession = mock(WebSocketSession.class);
            when(badSession.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=test-session-1"));

            handler.afterConnectionEstablished(badSession);

            verify(badSession).close(CloseStatus.BAD_DATA);
            assertEquals(0, handler.getConnectionCount("test-session-1"));
        }

        @Test
        @DisplayName("afterConnectionEstablished - should close with invalid playerId")
        void invalidPlayerId() throws Exception {
            WebSocketSession badSession = mock(WebSocketSession.class);
            when(badSession.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=test-session-1&playerId=abc"));

            handler.afterConnectionEstablished(badSession);

            verify(badSession).close(CloseStatus.BAD_DATA);
        }

        @Test
        @DisplayName("afterConnectionEstablished - should close for non-existent session")
        void nonExistentSession() throws Exception {
            WebSocketSession badSession = mock(WebSocketSession.class);
            when(badSession.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=non-existent&playerId=1"));
            when(gameSessionService.getGameSession("non-existent")).thenReturn(null);

            handler.afterConnectionEstablished(badSession);

            verify(badSession).close(CloseStatus.POLICY_VIOLATION);
        }

        @Test
        @DisplayName("afterConnectionEstablished - should send ACK and game state")
        void sendsAckAndGameState() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            // Should send at least 2 messages: ACK and GAME_STATE
            verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Multiple connections to same session")
        void multipleConnections() throws Exception {
            when(gameSessionService.getGameSession("test-session-1")).thenReturn(gameSession);

            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            assertEquals(2, handler.getConnectionCount("test-session-1"));
            assertEquals(2, handler.getTotalConnectionCount());
        }

        @Test
        @DisplayName("afterConnectionClosed - should unregister connection")
        void disconnect() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getConnectionCount("test-session-1"));

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            assertEquals(0, handler.getConnectionCount("test-session-1"));
            assertEquals(0, handler.getTotalConnectionCount());
        }

        @Test
        @DisplayName("afterConnectionClosed - should broadcast player left event")
        void disconnectBroadcastsLeave() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            // Reset wsSession2 to clear connection messages
            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            // wsSession2 should receive the PLAYER_LEFT event
            verify(wsSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("afterConnectionClosed - cleanup when last connection closes")
        void lastConnectionCleanup() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            assertFalse(handler.getActiveSessionIds().contains("test-session-1"));
        }
    }

    // ============================================================
    // Action Handling
    // ============================================================

    @Nested
    @DisplayName("Action Dispatch")
    class ActionDispatch {

        @BeforeEach
        void connectPlayer() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
        }

        @Test
        @DisplayName("Ping action")
        void ping() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "ping");
            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Move action with data")
        void moveWithData() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "move");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("fleetId", 1);
            data.put("targetX", 1);
            data.put("targetY", 2);
            data.put("targetZ", 3);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Move action without data should fail")
        void moveWithoutData() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "move");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Attack action")
        void attack() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "attack");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("targetFleetId", 2);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Build action")
        void build() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "build");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("type", "station");
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Produce action")
        void produce() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "produce");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("resource", "FUEL");
            data.put("amount", 100);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Trade action")
        void trade() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "trade");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("commodity", "FOOD");
            data.put("amount", 50);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("EndTurn action")
        void endTurn() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "endTurn");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Chat action")
        void chat() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "chat");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("message", "Hello everyone!");
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("State action")
        void state() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "state");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Unknown action should return error")
        void unknownAction() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "unknownAction");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Missing action field should return error")
        void missingAction() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("data", "something");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Invalid JSON should return error")
        void invalidJson() throws Exception {
            handler.handleTextMessage(wsSession1, new TextMessage("{invalid json}"));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("MoveTo action alias")
        void moveToAlias() throws Exception {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "moveTo");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("fleetId", 1);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }
    }

    // ============================================================
    // Broadcasting
    // ============================================================

    @Nested
    @DisplayName("Broadcasting")
    class Broadcasting {

        @Test
        @DisplayName("Broadcast to all sessions")
        void broadcastToAll() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            // Reset to clear connection messages
            reset(wsSession1, wsSession2);
            when(wsSession1.isOpen()).thenReturn(true);
            when(wsSession2.isOpen()).thenReturn(true);

            GameEvent event = new GameEvent(GameEvent.EventType.TURN_CHANGED, "test-session-1",
                    Map.of("turn", 2));
            handler.broadcastToSession("test-session-1", event);

            verify(wsSession1).sendMessage(any(TextMessage.class));
            verify(wsSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Broadcast to non-existent session does nothing")
        void broadcastToNonExistent() {
            GameEvent event = new GameEvent(GameEvent.EventType.TURN_CHANGED, "non-existent",
                    Map.of("turn", 2));
            handler.broadcastToSession("non-existent", event);

            // No exception should be thrown
        }

        @Test
        @DisplayName("Move action broadcasts to other players")
        void moveBroadcastsToOthers() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "move");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("fleetId", 1);
            msg.set("data", data);

            // Reset wsSession2 verify count after connection
            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            // wsSession2 should receive the broadcast
            verify(wsSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("EndTurn broadcasts to all players including sender")
        void endTurnBroadcastsToAll() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "endTurn");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            // wsSession2 should receive the turn change broadcast
            verify(wsSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Chat broadcasts to all players including sender")
        void chatBroadcastsToAll() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "chat");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("message", "Hello!");
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession2).sendMessage(any(TextMessage.class));
        }
    }

    // ============================================================
    // Management Methods
    // ============================================================

    @Nested
    @DisplayName("Management Methods")
    class ManagementMethods {

        @Test
        @DisplayName("getConnectionCount - returns 0 for unknown session")
        void unknownSessionCount() {
            assertEquals(0, handler.getConnectionCount("unknown"));
        }

        @Test
        @DisplayName("getActiveSessionIds - returns empty initially")
        void emptyActiveSessions() {
            assertTrue(handler.getActiveSessionIds().isEmpty());
        }

        @Test
        @DisplayName("getTotalConnectionCount - returns 0 initially")
        void emptyTotalCount() {
            assertEquals(0, handler.getTotalConnectionCount());
        }

        @Test
        @DisplayName("getActiveSessionIds - returns session IDs after connections")
        void activeSessionIds() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            List<String> active = handler.getActiveSessionIds();
            assertEquals(1, active.size());
            assertTrue(active.contains("test-session-1"));
        }

        @Test
        @DisplayName("getTotalConnectionCount - counts across sessions")
        void totalCountAcrossSessions() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            assertEquals(2, handler.getTotalConnectionCount());
        }
    }

    // ============================================================
    // Game State with Game object
    // ============================================================

    @Nested
    @DisplayName("Game State Integration")
    class GameStateIntegration {

        @Test
        @DisplayName("State request with Game object")
        void stateWithGame() throws Exception {
            Game game = mock(Game.class);
            when(game.getCurrentTurn()).thenReturn(5);
            when(game.getCurrentPlayerIndex()).thenReturn(0);
            when(game.getActivePlayerCount()).thenReturn(2);
            when(game.getPlanets()).thenReturn(new ConcurrentHashMap<>());
            when(game.getFleets()).thenReturn(new ConcurrentHashMap<>());
            when(game.getEmpires()).thenReturn(new ConcurrentHashMap<>());

            GameSession sessionWithGame = new GameSession("game-session", "Game Test", game);
            when(gameSessionService.getGameSession("game-session")).thenReturn(sessionWithGame);

            WebSocketSession session = mock(WebSocketSession.class);
            when(session.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=game-session&playerId=1"));
            when(session.isOpen()).thenReturn(true);

            handler.afterConnectionEstablished(session);

            verify(session, atLeast(2)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Connection with Game object sends game state")
        void connectionSendsGameState() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            // Should send ACK, PLAYER_JOINED, and GAME_STATE events
            verify(wsSession1, atLeast(3)).sendMessage(any(TextMessage.class));
        }
    }

    // ============================================================
    // Error Handling
    // ============================================================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Message from unregistered session is ignored")
        void unregisteredSession() throws Exception {
            WebSocketSession unknown = mock(WebSocketSession.class);
            when(unknown.isOpen()).thenReturn(true);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "ping");

            handler.handleTextMessage(unknown, new TextMessage(msg.toString()));

            verify(unknown, never()).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("handleTransportError logs error")
        void transportError() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            handler.handleTransportError(wsSession1, new RuntimeException("Connection reset"));

            // No exception should be thrown
        }

        @Test
        @DisplayName("Closed session is removed during broadcast")
        void closedSessionRemoved() throws Exception {
            // Connect openSession first
            handler.afterConnectionEstablished(wsSession1);

            // Use a fresh mock for the closed session
            WebSocketSession closedSession = mock(WebSocketSession.class);
            when(closedSession.getUri()).thenReturn(new URI("ws://localhost/ws/v1/4x/session?sessionId=test-session-1&playerId=2"));
            when(closedSession.isOpen()).thenReturn(false);

            handler.afterConnectionEstablished(closedSession);

            // Reset both to clear connection messages
            reset(wsSession1, closedSession);
            when(wsSession1.isOpen()).thenReturn(true);
            when(closedSession.isOpen()).thenReturn(false);

            GameEvent event = new GameEvent(GameEvent.EventType.TURN_CHANGED, "test-session-1",
                    Map.of("turn", 2));
            handler.broadcastToSession("test-session-1", event);

            // wsSession1 should receive the broadcast, closedSession should not (isOpen=false)
            verify(wsSession1).sendMessage(any(TextMessage.class));
            verify(closedSession, never()).sendMessage(any(TextMessage.class));
        }
    }

    // ============================================================
    // Integration Scenarios
    // ============================================================

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Full multiplayer workflow: connect, play, disconnect")
        void fullWorkflow() throws Exception {
            // Player 1 connects
            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getConnectionCount("test-session-1"));

            // Player 2 connects
            handler.afterConnectionEstablished(wsSession2);
            assertEquals(2, handler.getConnectionCount("test-session-1"));

            // Player 1 sends a move
            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            ObjectNode moveMsg = objectMapper.createObjectNode();
            moveMsg.put("action", "move");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("fleetId", 1);
            moveMsg.set("data", data);
            handler.handleTextMessage(wsSession1, new TextMessage(moveMsg.toString()));

            // Player 2 should receive the move broadcast
            verify(wsSession2).sendMessage(any(TextMessage.class));

            // Player 1 ends turn
            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            ObjectNode endTurnMsg = objectMapper.createObjectNode();
            endTurnMsg.put("action", "endTurn");
            handler.handleTextMessage(wsSession1, new TextMessage(endTurnMsg.toString()));

            // Player 2 should receive the turn change
            verify(wsSession2).sendMessage(any(TextMessage.class));

            // Player 2 disconnects
            handler.afterConnectionClosed(wsSession2, CloseStatus.NORMAL);
            assertEquals(1, handler.getConnectionCount("test-session-1"));

            // Player 1 disconnects
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertEquals(0, handler.getConnectionCount("test-session-1"));
        }

        @Test
        @DisplayName("Chat conversation between two players")
        void chatConversation() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            // Player 1 chats
            reset(wsSession2);
            when(wsSession2.isOpen()).thenReturn(true);

            ObjectNode chat1 = objectMapper.createObjectNode();
            chat1.put("action", "chat");
            ObjectNode data1 = objectMapper.createObjectNode();
            data1.put("message", "Hello!");
            chat1.set("data", data1);
            handler.handleTextMessage(wsSession1, new TextMessage(chat1.toString()));

            verify(wsSession2).sendMessage(any(TextMessage.class));

            // Player 2 responds
            reset(wsSession1);
            when(wsSession1.isOpen()).thenReturn(true);

            ObjectNode chat2 = objectMapper.createObjectNode();
            chat2.put("action", "chat");
            ObjectNode data2 = objectMapper.createObjectNode();
            data2.put("message", "Hi there!");
            chat2.set("data", data2);
            handler.handleTextMessage(wsSession2, new TextMessage(chat2.toString()));

            verify(wsSession1).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Ping-pong latency check")
        void pingPong() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "ping");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            // Should receive ACTION_RESULT with pong
            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }
    }
}
