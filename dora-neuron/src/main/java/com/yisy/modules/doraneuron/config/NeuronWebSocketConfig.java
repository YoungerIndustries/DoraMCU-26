package com.yisy.modules.doraneuron.config;

import com.yisy.modules.doraneuron.service.NeuronWebSocketClient;
import com.yisy.modules.doraneuron.ws.GameSyncWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket configuration for the Neuron client and game sync endpoint.
 *
 * Creates the WebSocket client bean, the {@link NeuronWebSocketClient}
 * that manages the connection to the coordinator, and registers the
 * game sync endpoint for player game nodes.
 *
 * Only active when running the neuron standalone (not when integrated into DoraMCU).
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(prefix = "neuron", name = "standalone", havingValue = "true", matchIfMissing = false)
public class NeuronWebSocketConfig implements WebSocketConfigurer {

    @Bean
    public WebSocketClient webSocketClient() {
        // Use standard WebSocket client with SockJS fallback
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return new SockJsClient(transports);
    }

    @Bean
    public GameSyncWebSocketHandler gameSyncWebSocketHandler() {
        return new GameSyncWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameSyncWebSocketHandler(), "/ws/game-sync")
                .setAllowedOrigins("*");
    }

    // NeuronWebSocketClient is created as a @Service with constructor injection,
    // so we don't create it here — Spring manages it as a bean directly.
}
