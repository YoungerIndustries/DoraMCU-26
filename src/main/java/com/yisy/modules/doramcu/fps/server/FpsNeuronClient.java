package com.yisy.modules.doramcu.fps.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Connects the FPS server to the Neuron AI network for intelligent bot behavior
 * and AI-assisted matchmaking.
 *
 * Features:
 * - Registers with the Neuron node as an FPS server peer
 * - Requests AI-assisted bot decisions for NPC behavior
 * - Submits match data for AI-driven matchmaking scoring
 * - Collects experience data from bot gameplay for model retraining
 * - Periodic heartbeat to maintain connection health
 */
@Component
public class FpsNeuronClient {

    private static final Logger log = LoggerFactory.getLogger(FpsNeuronClient.class);

    private final FpsServerConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /** Whether the Neuron connection is active. */
    private volatile boolean connected = false;

    /** Cached AI model version from Neuron. */
    private volatile long modelVersion = 0;

    /** Experience buffer for bot gameplay data. */
    private final List<Map<String, Object>> experienceBuffer = Collections.synchronizedList(new ArrayList<>());

    /** Total bot decisions requested from AI. */
    private final AtomicLong totalBotDecisions = new AtomicLong(0);

    /** Total successful AI responses. */
    private final AtomicLong totalAiResponses = new AtomicLong(0);

    /** Cache of bot decision results for replay scenarios. */
    private final ConcurrentHashMap<String, BotDecision> decisionCache = new ConcurrentHashMap<>();

    public FpsNeuronClient(FpsServerConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        log.info("FPS Neuron client initialized for server: {}", config.getServerId());
        registerWithNeuron();
    }

    /**
     * Register this FPS server with the Neuron node.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    public boolean registerWithNeuron() {
        String url = config.getBackendUrl() + "/api/v1/neuron/register";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("nodeId", config.getServerId() + "-neuron");
        payload.put("nodeName", "FPS Server " + config.getServerId());
        payload.put("clientType", "FPS_SERVER");
        payload.put("supportsInference", false);
        payload.put("canBeCoServer", false);
        payload.put("maxPlayers", config.getMaxPlayers());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                connected = true;
                JsonNode result = objectMapper.readTree(response.getBody());
                modelVersion = result.path("modelVersion").asLong(0);
                log.info("Registered with Neuron, model version: {}", modelVersion);
                return true;
            }
        } catch (Exception e) {
            if (connected) {
                log.warn("Failed to register with Neuron: {}", e.getMessage());
            }
            connected = false;
            return false;
        }
        return false;
    }

    /**
     * Send heartbeat to maintain Neuron connection.
     */
    @Scheduled(fixedDelayString = "${fps.server.heartbeatInterval:30000}")
    public void sendHeartbeat() {
        if (!connected) return;

        String url = config.getBackendUrl() + "/api/v1/neuron/heartbeat";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("nodeId", config.getServerId() + "-neuron");
        payload.put("modelVersion", modelVersion);

        // Add experience count if buffer has data
        int expCount;
        synchronized (experienceBuffer) {
            expCount = experienceBuffer.size();
        }
        payload.put("experienceCount", expCount);

        try {
            restTemplate.postForEntity(url, payload, String.class);
        } catch (Exception e) {
            log.debug("Heartbeat failed: {}", e.getMessage());
        }
    }

    /**
     * Request AI-assisted bot decision for a given game state.
     *
     * @param botId unique bot identifier
     * @param gameState current game state vector (position, health, ammo, enemies, objectives)
     * @param availableActions list of available actions for this bot
     * @return bot decision with predicted action and confidence
     */
    public BotDecision requestBotDecision(String botId, double[] gameState, List<String> availableActions) {
        if (!connected) {
            log.debug("Neuron not connected, using fallback bot behavior");
            return createFallbackDecision(botId, availableActions);
        }

        // Check cache first for replay scenarios
        String cacheKey = botId + "-" + Arrays.hashCode(gameState);
        BotDecision cached = decisionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String url = config.getBackendUrl() + "/api/v1/neuron/ai-assist";

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("nodeId", config.getServerId() + "-neuron");
        payload.put("taskType", "bot_decision");

        // Encode game state
        ArrayNode stateArray = payload.putArray("inputState");
        for (double val : gameState) {
            stateArray.add(val);
        }

        // Encode available actions
        ArrayNode actionsArray = payload.putArray("availableActions");
        for (String action : availableActions) {
            actionsArray.add(action);
        }

        payload.put("botId", botId);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode result = objectMapper.readTree(response.getBody());
                totalAiResponses.incrementAndGet();

                BotDecision decision = new BotDecision();
                decision.botId = botId;
                decision.predictedAction = result.path("predictedAction").asInt(0);
                decision.predictedActionName = result.path("predictedActionName").asText("unknown");
                decision.confidence = result.path("confidence").asDouble(0.5);
                decision.modelVersion = result.path("modelVersion").asLong(modelVersion);
                decision.latencyMs = result.path("latencyMs").asLong(0);

                // Cache the decision
                decisionCache.put(cacheKey, decision);
                if (decisionCache.size() > 1000) {
                    decisionCache.clear();
                }

                totalBotDecisions.incrementAndGet();
                return decision;
            }
        } catch (Exception e) {
            log.debug("AI bot decision failed: {}", e.getMessage());
        }

        return createFallbackDecision(botId, availableActions);
    }

    /**
     * Request AI-assisted matchmaking score for player skill matching.
     *
     * @param playerStats map of player IDs to their statistics
     * @return matchmaking recommendations
     */
    public Map<String, Object> requestMatchmakingScore(Map<String, Map<String, Object>> playerStats) {
        if (!connected) {
            return Map.of("status", "neuron_not_connected", "message", "Using default matchmaking");
        }

        String url = config.getBackendUrl() + "/api/v1/neuron/matchmaking";

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("nodeId", config.getServerId() + "-neuron");

        ObjectNode playersNode = payload.putObject("playerStats");
        for (Map.Entry<String, Map<String, Object>> entry : playerStats.entrySet()) {
            ObjectNode playerNode = playersNode.putObject(entry.getKey());
            for (Map.Entry<String, Object> stat : entry.getValue().entrySet()) {
                if (stat.getValue() instanceof Number) {
                    playerNode.put(stat.getKey(), ((Number) stat.getValue()).doubleValue());
                } else {
                    playerNode.put(stat.getKey(), stat.getValue().toString());
                }
            }
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            log.debug("Matchmaking request failed: {}", e.getMessage());
        }

        return Map.of("status", "fallback", "message", "Using default matchmaking algorithm");
    }

    /**
     * Submit bot gameplay experience for AI model retraining.
     *
     * @param botId bot identifier
     * @param state game state at decision time
     * @param action action taken by the bot
     * @param reward outcome reward (positive = good, negative = bad)
     */
    public void submitBotExperience(String botId, double[] state, String action, double reward) {
        Map<String, Object> experience = new HashMap<>();
        experience.put("source", config.getServerId() + "-neuron");
        experience.put("clientId", botId);
        experience.put("action", action);
        experience.put("reward", reward);

        ArrayNode stateArray = objectMapper.createArrayNode();
        for (double val : state) {
            stateArray.add(val);
        }
        experience.put("state", stateArray);

        synchronized (experienceBuffer) {
            experienceBuffer.add(experience);
        }
    }

    /**
     * Flush experience buffer to Neuron server.
     */
    @Scheduled(fixedDelay = 30000)
    public void flushExperienceBuffer() {
        if (!connected || experienceBuffer.isEmpty()) return;

        List<Map<String, Object>> batch;
        synchronized (experienceBuffer) {
            batch = new ArrayList<>(experienceBuffer);
            experienceBuffer.clear();
        }

        if (batch.isEmpty()) return;

        String url = config.getBackendUrl() + "/api/v1/neuron/experience";

        ArrayNode array = objectMapper.createArrayNode();
        for (Map<String, Object> exp : batch) {
            array.add(objectMapper.valueToTree(exp));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(array.toString(), headers);

            restTemplate.postForEntity(url, entity, String.class);
            log.debug("Flushed {} experience samples to Neuron", batch.size());
        } catch (Exception e) {
            log.debug("Failed to flush experience buffer: {}", e.getMessage());
            // Re-add to buffer for next attempt
            synchronized (experienceBuffer) {
                experienceBuffer.addAll(0, batch);
            }
        }
    }

    /**
     * Create a fallback bot decision when AI is unavailable.
     */
    private BotDecision createFallbackDecision(String botId, List<String> availableActions) {
        BotDecision decision = new BotDecision();
        decision.botId = botId;

        if (availableActions != null && !availableActions.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(availableActions.size());
            decision.predictedAction = index;
            decision.predictedActionName = availableActions.get(index);
        } else {
            decision.predictedAction = 0;
            decision.predictedActionName = "idle";
        }

        decision.confidence = 0.3;
        decision.modelVersion = -1;
        decision.latencyMs = 0;
        return decision;
    }

    /**
     * Get connection status and statistics.
     */
    public Map<String, Object> getStatus() {
        int expCount;
        synchronized (experienceBuffer) {
            expCount = experienceBuffer.size();
        }

        return Map.of(
                "connected", connected,
                "modelVersion", modelVersion,
                "totalBotDecisions", totalBotDecisions.get(),
                "totalAiResponses", totalAiResponses.get(),
                "experienceBuffered", expCount,
                "decisionCacheSize", decisionCache.size()
        );
    }

    /**
     * Bot decision result from AI or fallback.
     */
    public static class BotDecision {
        public String botId;
        public int predictedAction;
        public String predictedActionName;
        public double confidence;
        public long modelVersion;
        public long latencyMs;

        @Override
        public String toString() {
            return "BotDecision{botId='" + botId + "', action='" + predictedActionName +
                    "', confidence=" + confidence + "}";
        }
    }
}
