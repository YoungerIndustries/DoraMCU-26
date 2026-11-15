package com.yisy.modules.doramcu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.service.AchievementService;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AchievementWebSocketHandler Tests")
class AchievementWebSocketHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AchievementWebSocketHandler handler;

    @Mock
    private WebSocketSession wsSession1;
    @Mock
    private WebSocketSession wsSession2;
    @Mock
    private AchievementService achievementService;

    @BeforeEach
    void setUp() throws Exception {
        handler = new AchievementWebSocketHandler(achievementService);

        when(wsSession1.getId()).thenReturn("ws1");
        when(wsSession1.getUri()).thenReturn(new URI("ws://localhost/ws/achievements"));
        when(wsSession1.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession1).sendMessage(any(TextMessage.class));

        when(wsSession2.getId()).thenReturn("ws2");
        when(wsSession2.getUri()).thenReturn(new URI("ws://localhost/ws/achievements"));
        when(wsSession2.isOpen()).thenReturn(true);
        doAnswer(invocation -> null).when(wsSession2).sendMessage(any(TextMessage.class));

        // Mock achievement service methods called during subscribe
        when(achievementService.getPlayerAchievements(anyString())).thenReturn(Collections.emptyList());
        when(achievementService.getNotifications(anyString())).thenReturn(Collections.emptyList());
        when(achievementService.getAchievementDefinitions()).thenReturn(Collections.emptyMap());
        when(achievementService.getLeaderboard(anyString(), anyInt(), anyInt())).thenReturn(new AchievementService.LeaderboardEntry[0]);
        when(achievementService.unlockAchievement(anyString(), anyString())).thenReturn(false);
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
    @DisplayName("Subscribe with player ID")
    void subscribeWithPlayerId() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("playerId", "player-1");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        assertEquals(1, handler.getConnectedSessions());
    }

    @Test
    @DisplayName("Request leaderboard")
    void requestLeaderboard() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("playerId", "player-1");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        ObjectNode leaderboard = objectMapper.createObjectNode();
        leaderboard.put("type", "leaderboard");
        handler.handleTextMessage(wsSession1, new TextMessage(leaderboard.toString()));

        verify(wsSession1, atLeast(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Notify player of achievement")
    void notifyPlayer() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("playerId", "player-1");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        // Notify player of achievement (mocked to return false, so no broadcast)
        handler.notifyPlayer("player-1", "first_login");
    }

    @Test
    @DisplayName("Multiple players subscribe")
    void multiplePlayersSubscribe() throws Exception {
        handler.afterConnectionEstablished(wsSession1);
        handler.afterConnectionEstablished(wsSession2);

        ObjectNode subscribe1 = objectMapper.createObjectNode();
        subscribe1.put("type", "subscribe");
        subscribe1.put("playerId", "player-1");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe1.toString()));

        ObjectNode subscribe2 = objectMapper.createObjectNode();
        subscribe2.put("type", "subscribe");
        subscribe2.put("playerId", "player-2");
        handler.handleTextMessage(wsSession2, new TextMessage(subscribe2.toString()));

        assertEquals(2, handler.getConnectedSessions());
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
    @DisplayName("Disconnect removes player session")
    void disconnectRemovesPlayerSession() throws Exception {
        handler.afterConnectionEstablished(wsSession1);

        ObjectNode subscribe = objectMapper.createObjectNode();
        subscribe.put("type", "subscribe");
        subscribe.put("playerId", "player-1");
        handler.handleTextMessage(wsSession1, new TextMessage(subscribe.toString()));

        handler.afterConnectionClosed(wsSession1, CloseStatus.NORMAL);
        assertEquals(0, handler.getConnectedSessions());
    }
}
