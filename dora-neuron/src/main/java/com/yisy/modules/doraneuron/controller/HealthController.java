package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import com.yisy.modules.doraneuron.protocol.ConsensusResult;
import com.yisy.modules.doraneuron.service.ConsensusService;
import com.yisy.modules.doraneuron.service.ExperienceCollectorService;
import com.yisy.modules.doraneuron.service.NeuronRegistryService;
import com.yisy.modules.doraneuron.service.NeuronWebSocketClient;
import com.yisy.modules.doraneuron.service.SatelliteAiService;
import com.yisy.modules.doraneuron.service.mesh.MeshConsensusService;
import com.yisy.modules.doraneuron.service.mesh.MeshExperienceSyncService;
import com.yisy.modules.doraneuron.service.mesh.NeuronMeshService;
import com.yisy.modules.doraneuron.service.multigame.GameRegistryService;
import com.yisy.modules.doraneuron.service.unity.UnityIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health and status endpoints for the Neuron node.
 *
 * Exposes basic health checks and node identity information for monitoring
 * and load balancer probes.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final NeuronConfig config;
    private final NeuronRegistryService registryService;
    private final ExperienceCollectorService experienceCollectorService;
    private final SatelliteAiService satelliteAiService;
    private final ConsensusService consensusService;
    private final NeuronWebSocketClient webSocketClient;
    private final NeuronMeshService meshService;
    private final MeshConsensusService meshConsensusService;
    private final MeshExperienceSyncService meshExperienceSyncService;
    private final UnityIntegrationService unityIntegrationService;
    private final GameRegistryService gameRegistryService;

    public HealthController(NeuronConfig config, NeuronRegistryService registryService,
                            ExperienceCollectorService experienceCollectorService,
                            @Autowired(required = false) SatelliteAiService satelliteAiService,
                            @Autowired(required = false) ConsensusService consensusService,
                            NeuronWebSocketClient webSocketClient,
                            @Autowired(required = false) NeuronMeshService meshService,
                            @Autowired(required = false) MeshConsensusService meshConsensusService,
                            @Autowired(required = false) MeshExperienceSyncService meshExperienceSyncService,
                            UnityIntegrationService unityIntegrationService,
                            GameRegistryService gameRegistryService) {
        this.config = config;
        this.registryService = registryService;
        this.experienceCollectorService = experienceCollectorService;
        this.satelliteAiService = satelliteAiService;
        this.consensusService = consensusService;
        this.webSocketClient = webSocketClient;
        this.meshService = meshService;
        this.meshConsensusService = meshConsensusService;
        this.meshExperienceSyncService = meshExperienceSyncService;
        this.unityIntegrationService = unityIntegrationService;
        this.gameRegistryService = gameRegistryService;
    }

    /**
     * Simple liveness probe — always returns 200 if the process is running.
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "UP");
        body.put("nodeId", config.getNodeId() != null ? config.getNodeId() : "not-yet-initialized");
        return ResponseEntity.ok(body);
    }

    /**
     * Readiness probe — checks that the Neuron has registered with the coordinator.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> body = new HashMap<>();
        NeuronIdentity identity = registryService.getIdentity();

        if (identity != null) {
            body.put("status", "UP");
            body.put("nodeId", identity.getNodeId());
            body.put("nodeName", identity.getNodeName());
            body.put("registeredAt", identity.getRegisteredAt());
            body.put("lastHeartbeat", identity.getLastHeartbeat());
        } else {
            body.put("status", "STARTING");
            body.put("message", "Registration in progress");
        }

        return ResponseEntity.ok(body);
    }

    /**
     * Detailed health information including configuration summary.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("service", "DoraMCU-Neuron");
        body.put("version", "0.1.0");

        NeuronIdentity identity = registryService.getIdentity();
        if (identity != null) {
            body.put("nodeId", identity.getNodeId());
            body.put("nodeName", identity.getNodeName());
            body.put("region", identity.getRegion());
            body.put("status", identity.getStatus().name());
            body.put("url", identity.getUrl());
            body.put("registeredAt", identity.getRegisteredAt());
            body.put("lastHeartbeat", identity.getLastHeartbeat());
        }

        Map<String, Object> configSummary = new HashMap<>();
        configSummary.put("coordinatorUrl", config.getCoordinatorUrl());
        configSummary.put("gameServerUrl", config.getGameServerUrl());
        configSummary.put("heartbeatIntervalMs", config.getHeartbeatIntervalMs());
        body.put("config", configSummary);

        // Add AI model status if available
        if (satelliteAiService != null) {
            Map<String, Object> aiStatus = new HashMap<>();
            aiStatus.put("aiReady", satelliteAiService.isReady());
            aiStatus.put("modelVersion", satelliteAiService.getCurrentModelVersion());
            aiStatus.put("modelVersioning", true);
            body.put("ai", aiStatus);
        }

        // Add experience collection status
        Map<String, Object> expStatus = new HashMap<>();
        expStatus.put("bufferSize", experienceCollectorService.getBufferSize());
        expStatus.put("totalCollected", experienceCollectorService.getTotalCollected());
        expStatus.put("totalFlushed", experienceCollectorService.getTotalFlushed());
        expStatus.put("flushFailures", experienceCollectorService.getFlushFailures());
        body.put("experience", expStatus);

        // Add consensus status if available
        if (consensusService != null) {
            Map<String, Object> consensusStatus = new HashMap<>();
            consensusStatus.put("enabled", config.isEnableConsensus());
            consensusStatus.put("consensusRequested", consensusService.getConsensusRequested());
            consensusStatus.put("votesCast", consensusService.getVotesCast());

            ConsensusResult lastResult = consensusService.getLastConsensusResult();
            if (lastResult != null) {
                Map<String, Object> lastResultMap = new HashMap<>();
                lastResultMap.put("voteCount", lastResult.getVoteCount());
                lastResultMap.put("totalWeight", lastResult.getTotalWeight());
                lastResultMap.put("durationMs", lastResult.getDurationMs());
                consensusStatus.put("lastResult", lastResultMap);
            }
            body.put("consensus", consensusStatus);
        }

        // Add WebSocket status
        Map<String, Object> wsStatus = new HashMap<>();
        wsStatus.put("connected", webSocketClient.isConnected());
        wsStatus.put("reconnectAttempts", webSocketClient.getReconnectAttempts());
        body.put("websocket", wsStatus);

        // Add P2P mesh status
        if (meshService != null) {
            Map<String, Object> meshStatus = new HashMap<>();
            meshStatus.put("enabled", config.isMeshEnabled());
            meshStatus.put("peerCount", meshService.getPeerCount());
            meshStatus.put("messagesSent", meshService.getMessagesSent());
            meshStatus.put("messagesReceived", meshService.getMessagesReceived());
            meshStatus.put("peersConnected", meshService.getPeersConnected());
            meshStatus.put("peersDisconnected", meshService.getPeersDisconnected());
            body.put("mesh", meshStatus);

            // Add mesh consensus status
            if (meshConsensusService != null) {
                Map<String, Object> meshConsensusStatus = new HashMap<>();
                meshConsensusStatus.put("enabled", config.isMeshConsensusEnabled());
                meshConsensusStatus.put("requested", meshConsensusService.getConsensusRequested());
                meshConsensusStatus.put("completed", meshConsensusService.getConsensusCompleted());
                meshConsensusStatus.put("failed", meshConsensusService.getConsensusFailed());
                body.put("meshConsensus", meshConsensusStatus);
            }

            // Add mesh experience sync status
            if (meshExperienceSyncService != null) {
                Map<String, Object> meshSyncStatus = new HashMap<>();
                meshSyncStatus.put("enabled", config.isMeshExperienceSyncEnabled());
                meshSyncStatus.put("samplesSent", meshExperienceSyncService.getSamplesSent());
                meshSyncStatus.put("samplesReceived", meshExperienceSyncService.getSamplesReceived());
                meshSyncStatus.put("syncRounds", meshExperienceSyncService.getSyncRounds());
                body.put("meshExperienceSync", meshSyncStatus);
            }
        }

        // Add Unity integration status
        Map<String, Object> unityStatus = unityIntegrationService.getStatus();
        body.put("unity", unityStatus);

        // Add federation failover status
        Map<String, Object> federationStatus = new HashMap<>();
        federationStatus.put("failoverEnabled", config.isFailoverEnabled());
        federationStatus.put("currentCoordinatorUrl", registryService.getCurrentCoordinatorUrl());
        federationStatus.put("federated", registryService.isFederated());
        federationStatus.put("registered", registryService.isRegistered());
        federationStatus.put("fallbackUrls", config.getCoordinatorFallbackUrlList());
        body.put("federation", federationStatus);

        // Add Kafka event-driven status
        Map<String, Object> kafkaStatus = new HashMap<>();
        kafkaStatus.put("enabled", config.isKafkaEnabled());
        kafkaStatus.put("bootstrapServers", config.getKafkaBootstrapServers());
        body.put("kafka", kafkaStatus);

        // Add multi-game status
        Map<String, Object> multiGameStatus = new HashMap<>();
        multiGameStatus.put("totalGames", gameRegistryService.getGameCount());
        List<GameProfile> activeGames = gameRegistryService.getActiveGames();
        multiGameStatus.put("activeGames", activeGames.size());
        multiGameStatus.put("games", activeGames.stream().map(p -> Map.of(
                "gameId", p.getGameId(),
                "gameName", p.getGameName(),
                "status", p.getStatus(),
                "modelVersion", p.getModelVersion(),
                "predictions", p.getTotalPredictions(),
                "experience", p.getTotalExperienceCollected()
        )).toList());
        body.put("multiGame", multiGameStatus);

        return ResponseEntity.ok(body);
    }
}
