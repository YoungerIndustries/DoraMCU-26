package com.yisy.modules.doramcu.controller.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EconomicProductionWebSocketHandler — Real-time economic production updates.
 *
 * WebSocket endpoint: /ws/economic-production
 *
 * Message types (server → client):
 * - contract_progress: Contract progress changed
 * - contract_status: Contract status changed (e.g., PENDING → ASSIGNED → IN_PROGRESS → COMPLETED/FAILED)
 * - chain_progress: Production chain progress changed
 * - chain_status: Production chain status changed
 * - snapshot: Initial state when subscribing
 * - pong: Response to ping
 * - error: Error message
 *
 * Client messages:
 * - subscribe: {"type":"subscribe","galaxyId":123} or {"type":"subscribe","contractId":456} or {"type":"subscribe","chainId":789}
 * - unsubscribe: {"type":"unsubscribe","galaxyId":123} or {"type":"unsubscribe","contractId":456} or {"type":"unsubscribe","chainId":789}
 * - ping: {"type":"ping"}
 */
public class EconomicProductionWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(EconomicProductionWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // All connected sessions
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // Galaxy-level subscriptions: galaxyId → sessions
    private final Map<Long, Set<WebSocketSession>> galaxySubscriptions = new ConcurrentHashMap<>();

    // Contract-level subscriptions: contractId → sessions
    private final Map<Long, Set<WebSocketSession>> contractSubscriptions = new ConcurrentHashMap<>();

    // Chain-level subscriptions: chainId → sessions
    private final Map<Long, Set<WebSocketSession>> chainSubscriptions = new ConcurrentHashMap<>();

    // Session → subscribed galaxy IDs
    private final Map<WebSocketSession, Set<Long>> sessionGalaxySubscriptions = new ConcurrentHashMap<>();

    // Session → subscribed contract IDs
    private final Map<WebSocketSession, Set<Long>> sessionContractSubscriptions = new ConcurrentHashMap<>();

    // Session → subscribed chain IDs
    private final Map<WebSocketSession, Set<Long>> sessionChainSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sessionGalaxySubscriptions.put(session, ConcurrentHashMap.newKeySet());
        sessionContractSubscriptions.put(session, ConcurrentHashMap.newKeySet());
        sessionChainSubscriptions.put(session, ConcurrentHashMap.newKeySet());
        log.info("Economic production WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // Unsubscribe from all galaxies
        Set<Long> galaxyIds = sessionGalaxySubscriptions.remove(session);
        if (galaxyIds != null) {
            for (Long galaxyId : galaxyIds) {
                removeSubscription(galaxySubscriptions, galaxyId, session);
            }
        }
        // Unsubscribe from all contracts
        Set<Long> contractIds = sessionContractSubscriptions.remove(session);
        if (contractIds != null) {
            for (Long contractId : contractIds) {
                removeSubscription(contractSubscriptions, contractId, session);
            }
        }
        // Unsubscribe from all chains
        Set<Long> chainIds = sessionChainSubscriptions.remove(session);
        if (chainIds != null) {
            for (Long chainId : chainIds) {
                removeSubscription(chainSubscriptions, chainId, session);
            }
        }
        log.info("Economic production WebSocket disconnected: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type) {
                case "subscribe":
                    handleSubscribe(session, json);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, json);
                    break;
                case "ping":
                    sendPong(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error handling economic production message: {}", e.getMessage());
            try {
                sendError(session, "Error: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending error message: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Handle subscribe message: client wants updates for galaxy, contract, or chain.
     */
    private void handleSubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (json.has("galaxyId")) {
            Long galaxyId = json.get("galaxyId").asLong();
            Set<WebSocketSession> galaxySessions = galaxySubscriptions.computeIfAbsent(galaxyId, k -> ConcurrentHashMap.newKeySet());
            galaxySessions.add(session);
            Set<Long> subscribedGalaxies = sessionGalaxySubscriptions.get(session);
            if (subscribedGalaxies != null) {
                subscribedGalaxies.add(galaxyId);
            }
            log.info("Session {} subscribed to galaxy {}", session.getId(), galaxyId);
        } else if (json.has("contractId")) {
            Long contractId = json.get("contractId").asLong();
            Set<WebSocketSession> contractSessions = contractSubscriptions.computeIfAbsent(contractId, k -> ConcurrentHashMap.newKeySet());
            contractSessions.add(session);
            Set<Long> subscribedContracts = sessionContractSubscriptions.get(session);
            if (subscribedContracts != null) {
                subscribedContracts.add(contractId);
            }
            log.info("Session {} subscribed to contract {}", session.getId(), contractId);
        } else if (json.has("chainId")) {
            Long chainId = json.get("chainId").asLong();
            Set<WebSocketSession> chainSessions = chainSubscriptions.computeIfAbsent(chainId, k -> ConcurrentHashMap.newKeySet());
            chainSessions.add(session);
            Set<Long> subscribedChains = sessionChainSubscriptions.get(session);
            if (subscribedChains != null) {
                subscribedChains.add(chainId);
            }
            log.info("Session {} subscribed to chain {}", session.getId(), chainId);
        } else {
            sendError(session, "Subscribe message must contain galaxyId, contractId, or chainId");
        }
    }

    /**
     * Handle unsubscribe message: client no longer wants updates.
     */
    private void handleUnsubscribe(WebSocketSession session, JsonNode json) throws IOException {
        if (json.has("galaxyId")) {
            Long galaxyId = json.get("galaxyId").asLong();
            removeSubscription(galaxySubscriptions, galaxyId, session);
            Set<Long> subscribedGalaxies = sessionGalaxySubscriptions.get(session);
            if (subscribedGalaxies != null) {
                subscribedGalaxies.remove(galaxyId);
            }
            log.info("Session {} unsubscribed from galaxy {}", session.getId(), galaxyId);
        } else if (json.has("contractId")) {
            Long contractId = json.get("contractId").asLong();
            removeSubscription(contractSubscriptions, contractId, session);
            Set<Long> subscribedContracts = sessionContractSubscriptions.get(session);
            if (subscribedContracts != null) {
                subscribedContracts.remove(contractId);
            }
            log.info("Session {} unsubscribed from contract {}", session.getId(), contractId);
        } else if (json.has("chainId")) {
            Long chainId = json.get("chainId").asLong();
            removeSubscription(chainSubscriptions, chainId, session);
            Set<Long> subscribedChains = sessionChainSubscriptions.get(session);
            if (subscribedChains != null) {
                subscribedChains.remove(chainId);
            }
            log.info("Session {} unsubscribed from chain {}", session.getId(), chainId);
        } else {
            sendError(session, "Unsubscribe message must contain galaxyId, contractId, or chainId");
        }
    }

    /**
     * Helper to remove a session from a subscription map.
     */
    private void removeSubscription(Map<Long, Set<WebSocketSession>> subscriptionMap, Long key, WebSocketSession session) {
        Set<WebSocketSession> subscribers = subscriptionMap.get(key);
        if (subscribers != null) {
            subscribers.remove(session);
            if (subscribers.isEmpty()) {
                subscriptionMap.remove(key);
            }
        }
    }

    /**
     * Broadcast contract progress update to all sessions subscribed to the contract or its galaxy.
     */
    public void broadcastContractProgress(Long contractId, Long galaxyId, int progressPercent) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "contract_progress");
        message.put("contractId", contractId);
        if (galaxyId != null) {
            message.put("galaxyId", galaxyId);
        }
        message.put("progressPercent", progressPercent);
        message.put("timestamp", System.currentTimeMillis());

        TextMessage textMessage = createTextMessage(message);
        if (textMessage == null) return;

        // Send to contract subscribers
        broadcastTo(contractSubscriptions.get(contractId), textMessage, contractId);
        // Send to galaxy subscribers
        if (galaxyId != null) {
            broadcastTo(galaxySubscriptions.get(galaxyId), textMessage, galaxyId);
        }
        log.debug("Broadcast contract progress for contract {} to subscribers", contractId);
    }

    /**
     * Broadcast contract status change to all sessions subscribed to the contract or its galaxy.
     */
    public void broadcastContractStatus(Long contractId, Long galaxyId, String newStatus) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "contract_status");
        message.put("contractId", contractId);
        if (galaxyId != null) {
            message.put("galaxyId", galaxyId);
        }
        message.put("status", newStatus);
        message.put("timestamp", System.currentTimeMillis());

        TextMessage textMessage = createTextMessage(message);
        if (textMessage == null) return;

        // Send to contract subscribers
        broadcastTo(contractSubscriptions.get(contractId), textMessage, contractId);
        // Send to galaxy subscribers
        if (galaxyId != null) {
            broadcastTo(galaxySubscriptions.get(galaxyId), textMessage, galaxyId);
        }
        log.debug("Broadcast contract status for contract {} to subscribers", contractId);
    }

    /**
     * Broadcast chain progress update to all sessions subscribed to the chain.
     */
    public void broadcastChainProgress(Long chainId, int progressPercent) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "chain_progress");
        message.put("chainId", chainId);
        message.put("progressPercent", progressPercent);
        message.put("timestamp", System.currentTimeMillis());

        TextMessage textMessage = createTextMessage(message);
        if (textMessage == null) return;

        broadcastTo(chainSubscriptions.get(chainId), textMessage, chainId);
        log.debug("Broadcast chain progress for chain {} to subscribers", chainId);
    }

    /**
     * Broadcast chain status change to all sessions subscribed to the chain.
     */
    public void broadcastChainStatus(Long chainId, String newStatus) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "chain_status");
        message.put("chainId", chainId);
        message.put("status", newStatus);
        message.put("timestamp", System.currentTimeMillis());

        TextMessage textMessage = createTextMessage(message);
        if (textMessage == null) return;

        broadcastTo(chainSubscriptions.get(chainId), textMessage, chainId);
        log.debug("Broadcast chain status for chain {} to subscribers", chainId);
    }

    /**
     * Create a TextMessage from an ObjectNode, returning null on serialization error.
     */
    private TextMessage createTextMessage(ObjectNode message) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("Error serializing WebSocket message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Broadcast a message to a set of sessions.
     */
    private void broadcastTo(Set<WebSocketSession> sessionSet, TextMessage textMessage, Long identifier) {
        if (sessionSet == null || sessionSet.isEmpty()) {
            log.debug("No subscribers for identifier {}", identifier);
            return;
        }
        sessionSet.forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("Error broadcasting to session {}: {}", s.getId(), e.getMessage());
            }
        });
    }

    /**
     * Send pong response.
     */
    private void sendPong(WebSocketSession session) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "pong");
        response.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Send error message.
     */
    private void sendError(WebSocketSession session, String error) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "error");
        response.put("message", error);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Get the number of connected sessions.
     */
    public int getConnectedSessions() {
        return sessions.size();
    }

    /**
     * Get the number of galaxies being monitored.
     */
    public int getMonitoredGalaxies() {
        return galaxySubscriptions.size();
    }

    /**
     * Get the number of contracts being monitored.
     */
    public int getMonitoredContracts() {
        return contractSubscriptions.size();
    }

    /**
     * Get the number of chains being monitored.
     */
    public int getMonitoredChains() {
        return chainSubscriptions.size();
    }
}
