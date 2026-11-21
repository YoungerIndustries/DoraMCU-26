package com.yisy.modules.doramcu.fps.config;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.fps.FpsGameServerWrapper;
import com.yisy.modules.doramcu.fps.FpsWebSocketHandler;
import com.yisy.modules.doramcu.ai.server.service.VoiceCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures the FPS WebSocket endpoint at /ws/fps.
 * <p>
 * Registers the FpsWebSocketHandler to handle real-time multiplayer FPS connections.
 * </p>
 */
@Configuration
@EnableWebSocket
public class FpsWebSocketConfig implements WebSocketConfigurer {

    private final FpsGameServerWrapper fpsGameServer;
    private final VoiceCommandService voiceCommandService;

    public FpsWebSocketConfig(FpsGameServerWrapper fpsGameServer, VoiceCommandService voiceCommandService) {
        this.fpsGameServer = fpsGameServer;
        this.voiceCommandService = voiceCommandService;
    }

    @Bean
    public FpsWebSocketHandler fpsWebSocketHandler() {
        return new FpsWebSocketHandler(fpsGameServer, voiceCommandService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(fpsWebSocketHandler(), "/ws/fps")
                .setAllowedOrigins("*");
    }
}
