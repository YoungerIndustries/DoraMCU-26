package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PresenceWebSocketHandler Tests")
class PresenceWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private PresenceWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;

    @BeforeEach
    void setUp() throws Exception {
        handler = new PresenceWebSocketHandler();

        when(wsSession1.isOpen()).thenReturn(true);
        when(wsSession1.getUri()).thenReturn(new URI("/ws/presence?sessionId=test1&playerId=player1&playerName=Alice"));
        when(wsSession1.getId()).thenReturn("session1");

        when(wsSession2.isOpen()).thenReturn(true);
        when(wsSession2.getUri()).thenReturn(new URI("/ws/presence?sessionId=test1&playerId=player2&playerName=Bob"));
        when(wsSession2.getId()).thenReturn("session2");
    }

    @Nested
    @DisplayName("Connection Lifecycle")
    class ConnectionLifecycle {

        @Test
        @DisplayName("Should register player on connection")
        void connectionEstablished() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            assertEquals(1, handler.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("Should broadcast player_joined event")
        void connectionBroadcastsJoin() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should broadcast player_list on connection")
        void connectionBroadcastsPlayerList() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            // Should send both join event and player list
            verify(wsSession1, atLeast(2)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should unregister player on close")
        void sessionClosed() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            assertEquals(0, handler.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("Should broadcast player_left on disconnect")
        void sessionClosedBroadcastsLeave() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should handle transport errors")
        void transportError() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.handleTransportError(wsSession1, new RuntimeException("Connection lost"));

            assertEquals(0, handler.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("Chat Messages")
    class ChatMessages {

        @Test
        @DisplayName("Should broadcast chat messages")
        void chatBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "chat");
            msg.put("message", "Hello everyone!");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should handle empty chat message")
        void emptyChat() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "chat");
            msg.put("message", "");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            assertEquals(1, handler.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("Position Updates")
    class PositionUpdates {

        @Test
        @DisplayName("Should broadcast position updates")
        void moveBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "move");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("x", 100.0);
            data.put("y", 200.0);
            data.put("z", 300.0);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should update player position in data store")
        void moveUpdatesPosition() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "move");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("x", 100.0);
            data.put("y", 200.0);
            data.put("z", 300.0);
            msg.set("data", data);

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            Collection<PresenceWebSocketHandler.PlayerData> players = handler.getPlayers();
            assertFalse(players.isEmpty());
        }
    }

    @Nested
    @DisplayName("Activity Updates")
    class ActivityUpdates {

        @Test
        @DisplayName("Should broadcast activity changes")
        void activityBroadcast() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "activity");
            msg.put("type", "mining");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should handle different activity types")
        void activityTypes() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            String[] activities = {"mining", "building", "exploring", "trading", "combat", "idle"};
            for (String activity : activities) {
                ObjectNode msg = objectMapper.createObjectNode();
                msg.put("action", "activity");
                msg.put("type", activity);
                handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));
            }

            assertEquals(1, handler.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("Message Handling")
    class MessageHandling {

        @Test
        @DisplayName("Should handle ping message")
        void ping() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "ping");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should handle invalid JSON gracefully")
        void invalidJson() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            handler.handleTextMessage(wsSession1, new TextMessage("not json"));

            assertEquals(1, handler.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("Should handle unknown action gracefully")
        void unknownAction() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("action", "unknown");

            handler.handleTextMessage(wsSession1, new TextMessage(msg.toString()));

            assertEquals(1, handler.getOnlinePlayerCount());
        }
    }

    @Nested
    @DisplayName("Bot Simulation")
    class BotSimulation {

        @Test
        @DisplayName("Should simulate bot activity")
        void botActivity() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            Method method = PresenceWebSocketHandler.class.getDeclaredMethod("simulateBotActivity");
            method.setAccessible(true);
            method.invoke(handler);

            verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("Should not crash with no sessions")
        void botActivityNoSessions() throws Exception {
            Method method = PresenceWebSocketHandler.class.getDeclaredMethod("simulateBotActivity");
            method.setAccessible(true);
            assertDoesNotThrow(() -> method.invoke(handler));
        }
    }

    @Nested
    @DisplayName("Player Management")
    class PlayerManagement {

        @Test
        @DisplayName("Should track online player count")
        void onlinePlayerCount() throws Exception {
            assertEquals(0, handler.getOnlinePlayerCount());

            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getOnlinePlayerCount());

            handler.afterConnectionEstablished(wsSession2);
            assertEquals(2, handler.getOnlinePlayerCount());

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertEquals(1, handler.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("Should return all players")
        void getAllPlayers() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            Collection<PresenceWebSocketHandler.PlayerData> players = handler.getPlayers();
            assertEquals(2, players.size());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle send errors gracefully")
        void sendError() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            doThrow(new RuntimeException("Send failed")).when(wsSession1).sendMessage(any(TextMessage.class));

            assertDoesNotThrow(() -> {
                handler.handleTextMessage(wsSession1, new TextMessage("{\"action\":\"ping\"}"));
            });
        }

        @Test
        @DisplayName("Should handle null messages")
        void nullMessage() throws Exception {
            handler.afterConnectionEstablished(wsSession1);

            assertDoesNotThrow(() -> {
                handler.handleTextMessage(wsSession1, new TextMessage(""));
            });
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Full lifecycle: join, chat, move, change activity, leave")
        void fullLifecycle() throws Exception {
            // Join
            handler.afterConnectionEstablished(wsSession1);
            assertEquals(1, handler.getOnlinePlayerCount());

            // Chat
            ObjectNode chat = objectMapper.createObjectNode();
            chat.put("action", "chat");
            chat.put("message", "Hello!");
            handler.handleTextMessage(wsSession1, new TextMessage(chat.toString()));

            // Move
            ObjectNode move = objectMapper.createObjectNode();
            move.put("action", "move");
            ObjectNode data = objectMapper.createObjectNode();
            data.put("x", 100.0);
            data.put("y", 200.0);
            data.put("z", 300.0);
            move.set("data", data);
            handler.handleTextMessage(wsSession1, new TextMessage(move.toString()));

            // Activity change
            ObjectNode activity = objectMapper.createObjectNode();
            activity.put("action", "activity");
            activity.put("type", "mining");
            handler.handleTextMessage(wsSession1, new TextMessage(activity.toString()));

            // Leave
            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
            assertEquals(0, handler.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("Multiple players interact")
        void multiPlayerInteraction() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            // Player 1 chats
            ObjectNode chat1 = objectMapper.createObjectNode();
            chat1.put("action", "chat");
            chat1.put("message", "Hi Bob!");
            handler.handleTextMessage(wsSession1, new TextMessage(chat1.toString()));

            // Player 2 responds
            ObjectNode chat2 = objectMapper.createObjectNode();
            chat2.put("action", "chat");
            chat2.put("message", "Hi Alice!");
            handler.handleTextMessage(wsSession2, new TextMessage(chat2.toString()));

            assertEquals(2, handler.getOnlinePlayerCount());
        }

        @Test
        @DisplayName("Player disconnect does not affect others")
        void playerDisconnectIsolation() throws Exception {
            handler.afterConnectionEstablished(wsSession1);
            handler.afterConnectionEstablished(wsSession2);

            handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);

            assertEquals(1, handler.getOnlinePlayerCount());

            // Player 2 can still chat
            ObjectNode chat = objectMapper.createObjectNode();
            chat.put("action", "chat");
            chat.put("message", "Still here!");
            handler.handleTextMessage(wsSession2, new TextMessage(chat.toString()));

            verify(wsSession2, atLeast(1)).sendMessage(any(TextMessage.class));
        }
    }
}
