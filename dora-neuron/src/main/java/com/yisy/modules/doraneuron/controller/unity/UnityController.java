package com.yisy.modules.doraneuron.controller.unity;

import com.yisy.modules.doraneuron.protocol.unity.*;
import com.yisy.modules.doraneuron.service.unity.UnityIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Unity client integration with the Neuron.
 *
 * Provides endpoints for Unity clients to:
 * - Register and manage connections
 * - Request AI assistance for game decisions
 * - Upload experience data from game sessions
 * - Check model version and request updates
 */
@RestController
@RequestMapping("/api/v1/unity")
public class UnityController {

    private static final Logger log = LoggerFactory.getLogger(UnityController.class);

    @Autowired
    private UnityIntegrationService unityIntegrationService;

    // ── Client Management ────────────────────────────────────────────────────

    /**
     * Register a Unity client connection.
     *
     * @param request containing clientId, clientType, and playerId
     * @return registered client info
     */
    @PostMapping("/register")
    public ResponseEntity<UnityClientInfo> registerClient(@RequestBody UnityClientRegisterRequest request) {
        UnityClientInfo info = unityIntegrationService.registerClient(
                request.getClientId(),
                request.getClientType() != null ? request.getClientType() : "unity",
                request.getPlayerId()
        );
        return ResponseEntity.ok(info);
    }

    /**
     * Unregister a Unity client.
     */
    @DeleteMapping("/unregister")
    public ResponseEntity<Void> unregisterClient(@RequestParam String clientId) {
        unityIntegrationService.unregisterClient(clientId);
        return ResponseEntity.ok().build();
    }

    /**
     * Update client heartbeat.
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestParam String clientId) {
        unityIntegrationService.updateHeartbeat(clientId);
        return ResponseEntity.ok().build();
    }

    // ── AI Assistance ────────────────────────────────────────────────────────

    /**
     * Request AI assistance for a game decision.
     *
     * The client sends the current game state feature vector and optionally
     * requests consensus from the Neuron network.
     *
     * @param request containing features and optional consensus flag
     * @return AI prediction with confidence and explanation
     */
    @PostMapping("/ai/assist")
    public ResponseEntity<AiAssistResponse> requestAiAssist(@RequestBody AiAssistRequest request) {
        AiAssistResponse response = unityIntegrationService.processAiAssist(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Quick local prediction without consensus.
     *
     * Shortcut endpoint for fast local inference when consensus is not needed.
     */
    @PostMapping("/ai/predict")
    public ResponseEntity<AiAssistResponse> quickPredict(@RequestBody AiAssistRequest request) {
        // Force local inference
        request.setUseConsensus(false);
        AiAssistResponse response = unityIntegrationService.processAiAssist(request);
        return ResponseEntity.ok(response);
    }

    // ── Experience Collection ────────────────────────────────────────────────

    /**
     * Upload experience samples from a game session.
     *
     * Unity clients periodically upload collected (state, action, reward) tuples
     * for aggregation and eventual model retraining.
     *
     * @param batch containing experience samples
     * @return number of samples collected
     */
    @PostMapping("/experience/upload")
    public ResponseEntity<Map<String, Integer>> uploadExperience(@RequestBody UnityExperienceBatch batch) {
        int collected = unityIntegrationService.collectExperience(batch);
        return ResponseEntity.ok(Map.of("collected", collected));
    }

    // ── Model Management ─────────────────────────────────────────────────────

    /**
     * Get the current ONNX model info.
     *
     * Unity clients use this to check if their local model needs updating.
     */
    @GetMapping("/model/info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        return ResponseEntity.ok(unityIntegrationService.getModelInfo());
    }

    // ── Status and Monitoring ────────────────────────────────────────────────

    /**
     * Get the status of the Unity integration service.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(unityIntegrationService.getStatus());
    }

    /**
     * Get info about a specific connected client.
     */
    @GetMapping("/clients/{clientId}")
    public ResponseEntity<UnityClientInfo> getClientInfo(@PathVariable String clientId) {
        UnityClientInfo info = unityIntegrationService.getClientInfo(clientId);
        if (info != null) {
            return ResponseEntity.ok(info);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get all connected clients.
     */
    @GetMapping("/clients")
    public ResponseEntity<List<UnityClientInfo>> getAllClients() {
        return ResponseEntity.ok(unityIntegrationService.getAllClients());
    }

    /**
     * Simple request body for client registration.
     */
    public static class UnityClientRegisterRequest {
        private String clientId;
        private String clientType;
        private String playerId;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientType() {
            return clientType;
        }

        public void setClientType(String clientType) {
            this.clientType = clientType;
        }

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
    }
}
