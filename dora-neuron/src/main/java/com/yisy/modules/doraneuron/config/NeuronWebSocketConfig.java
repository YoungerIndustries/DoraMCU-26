package com.yisy.modules.doraneuron.config;

import com.yisy.modules.doraneuron.service.NeuronWebSocketClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket configuration for the Neuron client.
 *
 * Creates the WebSocket client bean and the {@link NeuronWebSocketClient}
 * that manages the connection to the coordinator.
 */
@Configuration
public class NeuronWebSocketConfig {

    @Bean
    public WebSocketClient webSocketClient() {
        // Use standard WebSocket client with SockJS fallback
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return new SockJsClient(transports);
    }

    // NeuronWebSocketClient is created as a @Service with constructor injection,
    // so we don't create it here — Spring manages it as a bean directly.
}
