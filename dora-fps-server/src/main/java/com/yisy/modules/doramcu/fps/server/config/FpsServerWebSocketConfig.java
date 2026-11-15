package com.yisy.modules.doramcu.fps.server.config;

import com.yisy.modules.doramcu.fps.server.FpsServerWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures the WebSocket endpoint for the dedicated FPS server.
 */
@Configuration
@EnableWebSocket
public class FpsServerWebSocketConfig implements WebSocketConfigurer {

    private final FpsServerWebSocketHandler handler;

    public FpsServerWebSocketConfig(FpsServerWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/fps").setAllowedOrigins("*");
    }
}
