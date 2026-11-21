package com.yisy.modules.doramcu.config;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.controller.session.GameSessionWebSocketHandler;
import com.yisy.modules.doramcu.service.chat.ChatService;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures the 4X game session WebSocket endpoint at /ws/v1/4x/session.
 * <p>
 * Registers the {@link GameSessionWebSocketHandler} to handle real-time
 * bidirectional communication for 4X game sessions. This complements the
 * existing SSE endpoint ({@code /ws/v1/4x/game/{sessionId}}) by allowing
 * clients to send actions to the server, not just receive events.
 * </p>
 * <p>
 * Connection URL: {@code ws://host:port/ws/v1/4x/session?sessionId=ABC&playerId=1}
 * </p>
 */
@Configuration
@EnableWebSocket
public class GameSessionWebSocketConfig implements WebSocketConfigurer {

    private final GameSessionService gameSessionService;
    private final ChatService chatService;

    public GameSessionWebSocketConfig(GameSessionService gameSessionService, ChatService chatService) {
        this.gameSessionService = gameSessionService;
        this.chatService = chatService;
    }

    @Bean
    public GameSessionWebSocketHandler gameSessionWebSocketHandler() {
        return new GameSessionWebSocketHandler(gameSessionService, chatService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameSessionWebSocketHandler(), "/ws/v1/4x/session")
                .setAllowedOrigins("*");
    }
}
