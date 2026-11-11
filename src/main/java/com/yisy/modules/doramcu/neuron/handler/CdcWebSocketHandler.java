package com.yisy.modules.doramcu.neuron.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.neuron.model.CdcEvent;
import com.yisy.modules.doramcu.neuron.service.CdcEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * WebSocket handler that broadcasts CDC events to connected game clients.
 * Each connected subscriber receives real-time entity change events.
 */
public class CdcWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CdcWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final List<WebSocketSession> subscribers = new CopyOnWriteArrayList<>();

    public CdcWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        subscribers.add(session);
        log.info("CDC WebSocket subscriber connected. Total subscribers: {}", subscribers.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        subscribers.remove(session);
        log.info("CDC WebSocket subscriber disconnected. Total subscribers: {}", subscribers.size());
    }

    /**
     * Broadcasts a CDC event to all connected subscribers.
     */
    public void broadcast(CdcEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            TextMessage message = new TextMessage(json);

            List<WebSocketSession> dead = new java.util.ArrayList<>();
            for (WebSocketSession session : subscribers) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    } else {
                        dead.add(session);
                    }
                } catch (IOException e) {
                    dead.add(session);
                    log.warn("Failed to send CDC event to subscriber: {}", e.getMessage());
                }
            }
            subscribers.removeAll(dead);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CDC event: {}", e.getMessage());
        }
    }

    public int getSubscriberCount() {
        return subscribers.size();
    }
}
