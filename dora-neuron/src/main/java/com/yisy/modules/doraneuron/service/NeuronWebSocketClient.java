package com.yisy.modules.doraneuron.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.ws.ConsensusVoteWsMessage;
import com.yisy.modules.doraneuron.protocol.ws.ExperienceStreamWsMessage;
import com.yisy.modules.doraneuron.protocol.ws.HeartbeatWsMessage;
import com.yisy.modules.doraneuron.protocol.ws.ModelSyncWsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebSocket client that connects the Neuron to the Coordinator.
 *
 * This client:
 * <ul>
 *   <li>Establishes a WebSocket connection to {@code ws://coordinator/ws/neuron}</li>
 *   <li>Sends periodic heartbeats over WebSocket</li>
 *   <li>Receives model sync broadcasts and triggers model downloads</li>
 *   <li>Receives consensus vote requests and responds with local predictions</li>
 *   <li>Streams experience samples to the coordinator</li>
 *   <li>Automatically reconnects on connection loss</li>
 * </ul>
 *
 * The WebSocket connection runs alongside the existing HTTP REST communication,
 * providing real-time bidirectional channels while HTTP serves as fallback.
 */
@Service
public class NeuronWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(NeuronWebSocketClient.class);

    private final WebSocketClient webSocketClient;
    private final NeuronConfig config;
    private final NeuronRegistryService registryService;
    private final NeuronPrometheusMetrics prometheusMetrics;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.port:8090}")
    private int serverPort;

    /** Current WebSocket session (volatile for thread safety). */
    private volatile WebSocketSession session;

    /** Connection state tracking. */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /** Reconnection attempt counter. */
    private final AtomicLong reconnectAttempts = new AtomicLong(0);

    /** Callbacks for incoming messages. */
    private List<ModelSyncCallback> modelSyncCallbacks = new ArrayList<>();
    private List<ConsensusCallback> consensusCallbacks = new ArrayList<>();

    public NeuronWebSocketClient(WebSocketClient webSocketClient,
                                  NeuronConfig config,
                                  NeuronRegistryService registryService,
                                  NeuronPrometheusMetrics prometheusMetrics) {
        this.webSocketClient = webSocketClient;
        this.config = config;
        this.registryService = registryService;
        this.prometheusMetrics = prometheusMetrics;
    }

    /**
     * Callback interface for model sync messages.
     */
    public interface ModelSyncCallback {
        void onModelSync(long version, String downloadUrl, String sha256Hash);
    }

    /**
     * Callback interface for consensus vote requests.
     */
    public interface ConsensusCallback {
        void onConsensusRequest(String requestId, double[] features, long timeoutMs);
    }

    /**
     * Register a callback for model sync messages.
     */
    public void addModelSyncCallback(ModelSyncCallback callback) {
        modelSyncCallbacks.add(callback);
    }

    /**
     * Register a callback for consensus vote requests.
     */
    public void addConsensusCallback(ConsensusCallback callback) {
        consensusCallbacks.add(callback);
    }

    @PostConstruct
    public void init() {
        log.info("Neuron WebSocket client initialized — coordinator: {}", config.getCoordinatorUrl());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connectOnStartup() {
        // Delay connection slightly to allow server to fully start
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                connect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Establish the WebSocket connection to the coordinator.
     */
    public void connect() {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            log.warn("Cannot connect WebSocket — neuron identity not yet available");
            return;
        }

        // Convert HTTP URL to WebSocket URL
        String coordinatorUrl = config.getCoordinatorUrl();
        String wsUrl = coordinatorUrl
                .replace("http://", "ws://")
                .replace("https://", "wss://")
                + "/ws/neuron?nodeId=" + identity.getNodeId()
                + "&nodeName=" + identity.getNodeName();

        log.info("Connecting WebSocket to: {}", wsUrl);

        try {
            CompletableFuture<WebSocketSession> future = webSocketClient.execute(new NeuronWebSocketHandler(), wsUrl);
            session = future.get();
            connected.set(true);
            reconnectAttempts.set(0);
            prometheusMetrics.setWebSocketConnected(true);
            log.info("WebSocket connected to coordinator");

        } catch (Exception e) {
            connected.set(false);
            prometheusMetrics.setWebSocketConnected(false);
            log.warn("WebSocket connection failed: {} — will retry", e.getMessage());
            scheduleReconnect();
        }
    }

    /**
     * Schedule a reconnection attempt with exponential backoff.
     */
    private void scheduleReconnect() {
        long attempts = reconnectAttempts.incrementAndGet();
        prometheusMetrics.recordWebSocketReconnect();
        long delay = Math.min(1000 * (1L << (int) Math.min(attempts - 1, 5)), 30000);

        log.info("Scheduling WebSocket reconnect in {}ms (attempt {})", delay, attempts);

        new Thread(() -> {
            try {
                Thread.sleep(delay);
                if (!connected.get()) {
                    connect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Send a message to the coordinator.
     */
    public void sendMessage(Object message) {
        if (!connected.get() || session == null || !session.isOpen()) {
            log.warn("Cannot send message — WebSocket not connected");
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Failed to send message: {}", e.getMessage());
            connected.set(false);
            scheduleReconnect();
        }
    }

    /**
     * Send periodic heartbeats over WebSocket.
     */
    @Scheduled(fixedDelayString = "${neuron.heartbeat-interval-ms:15000}")
    public void sendHeartbeat() {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            return;
        }

        HeartbeatWsMessage heartbeat = new HeartbeatWsMessage(
                identity.getNodeId(),
                identity.getStatus().name(),
                identity.getCurrentModelVersion(),
                identity.getExperienceCount()
        );
        heartbeat.setNodeId(identity.getNodeId());

        sendMessage(heartbeat);
    }

    /**
     * Stream experience samples to the coordinator.
     */
    public void streamExperience(List<ExperienceStreamWsMessage.ExperienceSampleDto> samples, long totalCollected) {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            return;
        }

        ExperienceStreamWsMessage message = new ExperienceStreamWsMessage(
                identity.getNodeId(), samples, totalCollected);
        sendMessage(message);
    }

    /**
     * Send a consensus vote response.
     */
    public void sendConsensusVote(String requestId, double[] probabilities,
                                   int actionIndex, double confidence, double voteWeight) {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            return;
        }

        ConsensusVoteWsMessage message = new ConsensusVoteWsMessage(
                identity.getNodeId(), requestId, probabilities,
                actionIndex, confidence, voteWeight);
        sendMessage(message);
    }

    /**
     * Check if the WebSocket connection is active.
     */
    public boolean isConnected() {
        return connected.get() && session != null && session.isOpen();
    }

    /**
     * Get the number of reconnect attempts.
     */
    public long getReconnectAttempts() {
        return reconnectAttempts.get();
    }

    /**
     * Gracefully close the connection.
     */
    @PreDestroy
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
                log.info("WebSocket disconnected gracefully");
            } catch (IOException e) {
                log.warn("Error closing WebSocket: {}", e.getMessage());
            }
        }
        connected.set(false);
    }

    /**
     * WebSocket handler for incoming messages from the coordinator.
     */
    private class NeuronWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession wsSession) {
            log.info("WebSocket connection established with coordinator");
        }

        @Override
        public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) {
            log.info("WebSocket connection closed: {} — {}", status.getCode(), status.getReason());
            connected.set(false);
            prometheusMetrics.setWebSocketConnected(false);
            scheduleReconnect();
        }

        @Override
        protected void handleTextMessage(WebSocketSession wsSession, TextMessage message) {
            try {
                JsonNode root = objectMapper.readTree(message.getPayload());
                String type = root.path("type").asText("");

                switch (type) {
                    case "CONNECT_ACK":
                        log.info("Coordinator acknowledged connection");
                        break;
                    case "MODEL_SYNC":
                        handleModelSync(root);
                        break;
                    case "CONSENSUS_VOTE_REQUEST":
                        handleConsensusRequest(root);
                        break;
                    case "EXPERIENCE_ACK":
                        int accepted = root.path("accepted").asInt(0);
                        log.debug("Experience ACK: {} samples accepted", accepted);
                        break;
                    default:
                        log.debug("Received WS message type: {}", type);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse WebSocket message: {}", e.getMessage());
            }
        }

        @Override
        public void handleTransportError(WebSocketSession wsSession, Throwable exception) {
            log.error("WebSocket transport error: {}", exception.getMessage());
            connected.set(false);
            prometheusMetrics.setWebSocketConnected(false);
            scheduleReconnect();
        }

        private void handleModelSync(JsonNode root) {
            long version = root.path("version").asLong(0);
            String downloadUrl = root.path("downloadUrl").asText("");
            String sha256Hash = root.path("sha256Hash").asText("");

            log.info("Received model sync: v{} from {}", version, downloadUrl);

            for (ModelSyncCallback callback : modelSyncCallbacks) {
                try {
                    callback.onModelSync(version, downloadUrl, sha256Hash);
                } catch (Exception e) {
                    log.error("Model sync callback error: {}", e.getMessage());
                }
            }
        }

        private void handleConsensusRequest(JsonNode root) {
            String requestId = root.path("requestId").asText("");
            long timeoutMs = root.path("timeoutMs").asLong(5000);

            // Extract features array
            JsonNode featuresNode = root.path("features");
            double[] features = null;
            if (featuresNode.isArray()) {
                features = new double[featuresNode.size()];
                for (int i = 0; i < featuresNode.size(); i++) {
                    features[i] = featuresNode.get(i).asDouble();
                }
            }

            log.info("Received consensus request: {} with {} features", requestId,
                    features != null ? features.length : 0);

            for (ConsensusCallback callback : consensusCallbacks) {
                try {
                    callback.onConsensusRequest(requestId, features, timeoutMs);
                } catch (Exception e) {
                    log.error("Consensus callback error: {}", e.getMessage());
                }
            }
        }
    }
}
