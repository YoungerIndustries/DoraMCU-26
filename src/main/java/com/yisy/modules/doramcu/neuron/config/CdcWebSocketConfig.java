package com.yisy.modules.doramcu.neuron.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.neuron.handler.CdcWebSocketHandler;
import com.yisy.modules.doramcu.neuron.service.CdcEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configures the CDC WebSocket endpoint at /ws/cdc.
 * Wires the CdcWebSocketHandler to receive CDC events from the publisher.
 */
@Configuration
@EnableWebSocket
public class CdcWebSocketConfig implements WebSocketConfigurer {

    private final ObjectMapper objectMapper;
    private final CdcEventPublisher cdcEventPublisher;

    public CdcWebSocketConfig(ObjectMapper objectMapper, CdcEventPublisher cdcEventPublisher) {
        this.objectMapper = objectMapper;
        this.cdcEventPublisher = cdcEventPublisher;
    }

    @Bean
    public CdcWebSocketHandler cdcWebSocketHandler() {
        CdcWebSocketHandler handler = new CdcWebSocketHandler(objectMapper);
        // Subscribe the handler to CDC events
        cdcEventPublisher.addListener(handler::broadcast);
        return handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cdcWebSocketHandler(), "/ws/cdc")
                .setAllowedOrigins("*");
    }
}
