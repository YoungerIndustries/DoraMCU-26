package com.yisy.modules.doraneuron.service.unity;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.ExperienceSample;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.protocol.ConsensusResult;
import com.yisy.modules.doraneuron.protocol.unity.*;
import com.yisy.modules.doraneuron.service.ConsensusService;
import com.yisy.modules.doraneuron.service.ExperienceCollectorService;
import com.yisy.modules.doraneuron.service.SatelliteAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages Unity client connections, AI assistance requests, and experience collection.
 *
 * This service acts as the bridge between Unity game clients and the Neuron's
 * AI capabilities. It tracks connected clients, routes AI assistance requests
 * to local inference or consensus voting, and collects experience data from
 * game sessions for model retraining.
 */
@Service
public class UnityIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(UnityIntegrationService.class);

    /** Registry of connected Unity clients. */
    private final ConcurrentHashMap<String, UnityClientInfo> connectedClients = new ConcurrentHashMap<>();

    /** Scheduled executor for periodic client cleanup. */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private NeuronConfig config;

    @Autowired(required = false)
    private SatelliteAiService satelliteAiService;

    @Autowired(required = false)
    private ConsensusService consensusService;

    @Autowired
    private ExperienceCollectorService experienceCollectorService;

    @Autowired
    private NeuronPrometheusMetrics prometheusMetrics;

    /** Total AI assistance requests processed. */
    private int totalAssistanceRequests = 0;

    /** Total experience samples collected from Unity clients. */
    private int totalExperienceCollected = 0;

    @PostConstruct
    public void init() {
        // Periodically evict stale clients
        scheduler.scheduleAtFixedRate(this::evictStaleClients, 60, 60, TimeUnit.SECONDS);
        log.info("[UnityIntegrationService] Initialized — client eviction running every 60s");
    }

    /**
     * Registers a new Unity client connection.
     */
    public UnityClientInfo registerClient(String clientId, String clientType, String playerId) {
        prometheusMetrics.setUnityConnectedClients(connectedClients.size() + (connectedClients.containsKey(clientId) ? 0 : 1));
        UnityClientInfo info = connectedClients.compute(clientId, (key, existing) -> {
            if (existing != null) {
                // Update existing client
                existing.setClientType(clientType);
                existing.setPlayerId(playerId);
                existing.setLastHeartbeat(System.currentTimeMillis());
                return existing;
            } else {
                // New client
                UnityClientInfo newInfo = new UnityClientInfo();
                newInfo.setClientId(clientId);
                newInfo.setClientType(clientType);
                newInfo.setPlayerId(playerId);
                newInfo.setRegion(config.getNodeRegion());
                return newInfo;
            }
        });

        log.info("[UnityIntegrationService] Client registered: {} (player: {})", clientId, playerId);
        return info;
    }

    /**
     * Unregisters a Unity client.
     */
    public void unregisterClient(String clientId) {
        UnityClientInfo removed = connectedClients.remove(clientId);
        if (removed != null) {
            prometheusMetrics.setUnityConnectedClients(connectedClients.size());
            log.info("[UnityIntegrationService] Client unregistered: {}", clientId);
        }
    }

    /**
     * Updates the heartbeat for a client.
     */
    public void updateHeartbeat(String clientId) {
        UnityClientInfo info = connectedClients.get(clientId);
        if (info != null) {
            info.setLastHeartbeat(System.currentTimeMillis());
        }
    }

    /**
     * Processes an AI assistance request from a Unity client.
     *
     * If consensus is requested and available, routes to the consensus service.
     * Otherwise, uses local ONNX inference. Falls back to heuristic prediction
     * if no AI service is available.
     */
    public AiAssistResponse processAiAssist(AiAssistRequest request) {
        long startTime = System.currentTimeMillis();
        String clientId = request.getClientId();

        // Update client heartbeat
        updateHeartbeat(clientId);

        // Convert float[] to double[] for inference
        float[] features = request.getFeatures();
        if (features == null || features.length == 0) {
            return createErrorResponse("Invalid feature vector", startTime);
        }

        double[] doubleFeatures = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            doubleFeatures[i] = features[i];
        }

        NeuronDecision decision;
        boolean usedConsensus = false;

        try {
            if (request.isUseConsensus() && consensusService != null) {
                // Use distributed consensus
                ConsensusResult result = consensusService.requestConsensus(doubleFeatures, config.getConsensusTimeoutMs());
                if (result != null && result.getDecision() != null) {
                    decision = result.getDecision();
                } else {
                    decision = heuristicPredict(doubleFeatures);
                }
                usedConsensus = true;
            } else if (satelliteAiService != null) {
                // Use local ONNX inference
                decision = satelliteAiService.predict(doubleFeatures);
            } else {
                // Fallback to heuristic prediction
                decision = heuristicPredict(doubleFeatures);
                log.debug("[UnityIntegrationService] Fallback heuristic used for client: {}", clientId);
            }
        } catch (Exception e) {
            log.warn("[UnityIntegrationService] AI prediction failed for client {}: {}", clientId, e.getMessage());
            decision = heuristicPredict(doubleFeatures);
        }

        // Build response
        AiAssistResponse response = new AiAssistResponse();
        response.setPredictedAction(decision.getSelectedAction());
        response.setPredictedActionName(actionIndexToName(decision.getSelectedAction()));
        response.setConfidence(decision.getConfidence());
        response.setProbabilities(decision.getProbabilities());
        response.setModelVersion(decision.getModelVersion());
        response.setUsedConsensus(usedConsensus);
        response.setLatencyMs(System.currentTimeMillis() - startTime);

        // Generate explanation
        response.setExplanation(generateExplanation(decision, request.getContext()));

        // Update client stats
        UnityClientInfo info = connectedClients.get(clientId);
        if (info != null) {
            info.setPredictionCount(info.getPredictionCount() + 1);
        }

        totalAssistanceRequests++;
        prometheusMetrics.recordUnityPrediction();
        prometheusMetrics.setUnityConnectedClients(connectedClients.size());
        log.debug("[UnityIntegrationService] AI assist completed for {}: action={} confidence={} latency={}ms",
                clientId, response.getPredictedActionName(), response.getConfidence(), response.getLatencyMs());

        return response;
    }

    /**
     * Receives experience samples from a Unity client and forwards them
     * to the experience collector for aggregation.
     */
    public int collectExperience(UnityExperienceBatch batch) {
        String clientId = batch.getClientId();
        updateHeartbeat(clientId);

        List<ExperienceSample> samples = batch.getSamples();
        if (samples == null || samples.isEmpty()) {
            return 0;
        }

        int collected = 0;
        for (ExperienceSample sample : samples) {
            experienceCollectorService.collect(sample);
            collected++;
        }

        // Update client stats
        UnityClientInfo info = connectedClients.get(clientId);
        if (info != null) {
            info.setExperienceCount(info.getExperienceCount() + collected);
            info.setClientModelVersion(batch.getClientModelVersion());
        }

        totalExperienceCollected += collected;
        prometheusMetrics.recordUnityExperienceUploaded(collected);
        prometheusMetrics.setUnityConnectedClients(connectedClients.size());
        log.info("[UnityIntegrationService] Collected {} experience samples from client: {}", collected, clientId);
        return collected;
    }

    /**
     * Gets the current ONNX model info for a Unity client to check if update is needed.
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("available", satelliteAiService != null);

        if (satelliteAiService != null) {
            info.put("version", satelliteAiService.getCurrentModelVersion());
            info.put("inputSize", 64);
            info.put("outputSize", 17);
        }

        return info;
    }

    /**
     * Gets the status of the Unity integration service.
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connectedClients", connectedClients.size());
        status.put("totalAssistanceRequests", totalAssistanceRequests);
        status.put("totalExperienceCollected", totalExperienceCollected);
        status.put("aiServiceAvailable", satelliteAiService != null);
        status.put("consensusAvailable", consensusService != null);

        // Model info
        Map<String, Object> modelInfo = getModelInfo();
        status.put("modelInfo", modelInfo);

        return status;
    }

    /**
     * Gets info about a specific connected client.
     */
    public UnityClientInfo getClientInfo(String clientId) {
        return connectedClients.get(clientId);
    }

    /**
     * Gets all connected clients.
     */
    public List<UnityClientInfo> getAllClients() {
        return new ArrayList<>(connectedClients.values());
    }

    /**
     * Evicts clients that haven't sent a heartbeat in the configured timeout.
     */
    private void evictStaleClients() {
        long cutoff = System.currentTimeMillis() - config.getHeartbeatTimeoutMs() * 4;

        connectedClients.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastHeartbeat() < cutoff) {
                log.info("[UnityIntegrationService] Evicted stale client: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Generates a heuristic prediction when no AI service is available.
     */
    private NeuronDecision heuristicPredict(double[] features) {
        float[] probs = new float[17];
        float uniform = 1.0f / 17;

        for (int i = 0; i < 17; i++) {
            probs[i] = uniform;
        }

        // Simple heuristic: boost actions based on feature values
        if (features.length >= 64) {
            // Resources (features 0-7)
            float resourceScore = 0;
            for (int i = 0; i < 8; i++) resourceScore += (float) features[i];
            resourceScore /= 8;

            if (resourceScore > 0.5f) {
                probs[0] *= 2; // BUILD_SHIP
                probs[1] *= 1.5f; // BUILD_STATION
            }

            // Fleet strength (features 8-15)
            float fleetScore = 0;
            for (int i = 8; i < 16; i++) fleetScore += (float) features[i];
            fleetScore /= 8;

            if (fleetScore > 0.5f) {
                probs[2] *= 2; // ENGAGE_COMBAT
            }
        }

        // Normalize
        float sum = 0;
        for (float p : probs) sum += p;
        for (int i = 0; i < probs.length; i++) probs[i] /= sum;

        // Find best action
        int bestAction = 0;
        float maxProb = probs[0];
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                bestAction = i;
            }
        }

        return new NeuronDecision(probs, bestAction, maxProb, -1);
    }

    /**
     * Converts an action index to its human-readable name.
     */
    private String actionIndexToName(int index) {
        return switch (index) {
            case 0 -> "BUILD_SHIP";
            case 1 -> "BUILD_STATION";
            case 2 -> "ENGAGE_COMBAT";
            case 3 -> "MOVE_FLEET";
            case 4 -> "EXPAND_COLONY";
            case 5 -> "RESEARCH_TECH";
            case 6 -> "TRADE";
            case 7 -> "OPTIMIZE_MINING";
            case 8 -> "FORTIFY";
            case 9 -> "RETREAT";
            case 10 -> "FLANK_AMBUSH";
            case 11 -> "WAIT";
            case 12 -> "DIPLOMACY";
            case 13 -> "SCOUT";
            case 14 -> "RAID";
            case 15 -> "BLOCKADE";
            case 16 -> "SURRENDER";
            default -> "UNKNOWN";
        };
    }

    /**
     * Generates a brief explanation of the prediction.
     */
    private String generateExplanation(NeuronDecision decision, String context) {
        String actionName = actionIndexToName(decision.getSelectedAction());
        String confidenceStr = String.format("%.1f%%", decision.getConfidence() * 100);

        if (context != null) {
            return String.format("AI suggests %s with %s confidence in %s context",
                    actionName, confidenceStr, context);
        }
        return String.format("AI suggests %s with %s confidence", actionName, confidenceStr);
    }

    /**
     * Creates an error response when prediction fails.
     */
    private AiAssistResponse createErrorResponse(String error, long startTime) {
        AiAssistResponse response = new AiAssistResponse();
        response.setPredictedAction(11); // WAIT as default
        response.setPredictedActionName("WAIT");
        response.setConfidence(0f);
        response.setProbabilities(new float[17]);
        response.setModelVersion(-1);
        response.setUsedConsensus(false);
        response.setLatencyMs(System.currentTimeMillis() - startTime);
        response.setExplanation("Error: " + error);
        return response;
    }
}
