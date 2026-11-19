package com.yisy.modules.doraneuron.controller.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.model.brainnode.BrainNodeStatus;
import com.yisy.modules.doraneuron.service.brainnode.BrainNodeService;
import com.yisy.modules.doraneuron.service.brainnode.ModelManagementService;
import com.yisy.modules.doraneuron.service.brainnode.SerialConsoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for Brain Node operations.
 * <p>
 * Provides endpoints for status, health, sync, failover, and serial console commands.
 */
@RestController
@RequestMapping("/api/v1/brain-node")
@ConditionalOnBean(BrainNodeConfig.class)
@EnableConfigurationProperties(BrainNodeConfig.class)
public class BrainNodeController {

    private static final Logger log = LoggerFactory.getLogger(BrainNodeController.class);

    private final BrainNodeService brainNodeService;
    private final SerialConsoleService serialConsoleService;
    private final BrainNodeConfig brainNodeConfig;
    private final ModelManagementService modelManagementService;

    public BrainNodeController(BrainNodeService brainNodeService,
                               SerialConsoleService serialConsoleService,
                               BrainNodeConfig brainNodeConfig,
                               @Autowired(required = false) ModelManagementService modelManagementService) {
        this.brainNodeService = brainNodeService;
        this.serialConsoleService = serialConsoleService;
        this.brainNodeConfig = brainNodeConfig;
        this.modelManagementService = modelManagementService;
    }

    /**
     * Get the current Brain Node status.
     *
     * @return comprehensive status information
     */
    @GetMapping("/status")
    public ResponseEntity<BrainNodeStatus> getStatus() {
        log.debug("Fetching Brain Node status");
        BrainNodeStatus status = brainNodeService.getStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Check if the Brain Node is healthy.
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.debug("Checking Brain Node health");
        Map<String, Object> health = new HashMap<>();
        health.put("status", brainNodeService.isInitialized() ? "UP" : "DOWN");
        health.put("initialized", brainNodeService.isInitialized());
        health.put("piReachable", brainNodeService.isPiReachable());
        health.put("serialConnected", serialConsoleService.isConnected());
        health.put("enabled", brainNodeConfig.isEnabled());
        return ResponseEntity.ok(health);
    }

    /**
     * Check if the Raspberry Pi is reachable.
     *
     * @return Pi reachability status
     */
    @GetMapping("/pi/health")
    public ResponseEntity<Map<String, Object>> checkPiHealth() {
        log.debug("Checking Raspberry Pi health");
        boolean reachable = brainNodeService.checkPiHealth();
        Map<String, Object> result = new HashMap<>();
        result.put("reachable", reachable);
        result.put("coordinatorUrl", brainNodeConfig.getPiCoordinatorUrl());
        result.put("serialConnected", serialConsoleService.isConnected());
        return ResponseEntity.ok(result);
    }

    /**
     * Trigger a manual sync with the Raspberry Pi.
     *
     * @return sync result
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerSync() {
        log.info("Manual sync triggered");
        boolean success = brainNodeService.triggerSync();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Sync completed successfully" : "Sync failed");
        return ResponseEntity.ok(result);
    }

    /**
     * Get the current sync status.
     * Returns DataSyncService stats if available, otherwise returns local storage sync status.
     * Works even when the Pi is unreachable.
     *
     * @return sync status information
     */
    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        log.debug("Fetching sync status");
        Map<String, Object> syncStatus = brainNodeService.getSyncStatus();
        return ResponseEntity.ok(syncStatus);
    }

    /**
     * Send a command to the Raspberry Pi via serial console.
     *
     * @param command the command to send
     * @return the response from the Pi
     */
    @PostMapping("/serial/command")
    public ResponseEntity<Map<String, Object>> sendSerialCommand(@RequestBody Map<String, String> request) {
        String command = request.get("command");
        if (command == null || command.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Command is required"));
        }

        log.info("Sending serial command to Pi: {}", command);
        String response = brainNodeService.sendCommandToPi(command);

        Map<String, Object> result = new HashMap<>();
        result.put("command", command);
        result.put("response", response != null ? response : "No response");
        result.put("serialConnected", serialConsoleService.isConnected());
        return ResponseEntity.ok(result);
    }

    /**
     * Get available serial ports.
     *
     * @return list of available serial ports
     */
    @GetMapping("/serial/ports")
    public ResponseEntity<List<String>> getAvailablePorts() {
        log.debug("Fetching available serial ports");
        List<String> ports = serialConsoleService.getAvailablePorts();
        return ResponseEntity.ok(ports);
    }

    /**
     * Restart the serial connection to the Raspberry Pi.
     *
     * @return reconnection result
     */
    @PostMapping("/serial/reconnect")
    public ResponseEntity<Map<String, Object>> restartSerial() {
        log.info("Restarting serial connection");
        boolean success = brainNodeService.restartSerialConnection();
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("status", serialConsoleService.getConnectionStatus());
        return ResponseEntity.ok(result);
    }

    /**
     * Get Brain Node configuration.
     *
     * @return configuration summary
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        log.debug("Fetching Brain Node configuration");
        Map<String, Object> config = new HashMap<>();
        config.put("nodeType", brainNodeConfig.getNodeType());
        config.put("enabled", brainNodeConfig.isEnabled());
        config.put("piCoordinatorUrl", brainNodeConfig.getPiCoordinatorUrl());
        config.put("serialEnabled", brainNodeConfig.isSerialEnabled());
        config.put("serialPortAutoDetect", brainNodeConfig.isSerialPortAutoDetect());
        config.put("inferenceThreads", brainNodeConfig.getInferenceThreads());
        config.put("aiModelSize", brainNodeConfig.getAiModelSize());
        config.put("confidenceThreshold", brainNodeConfig.getConfidenceThreshold());
        config.put("storageType", brainNodeConfig.getStorageType());
        config.put("storagePath", brainNodeConfig.getStoragePath());
        config.put("experienceBufferSize", brainNodeConfig.getExperienceBufferSize());
        config.put("autoFailoverEnabled", brainNodeConfig.isAutoFailoverEnabled());
        config.put("maxCpuUsagePercent", brainNodeConfig.getMaxCpuUsagePercent());
        config.put("maxMemoryMb", brainNodeConfig.getMaxMemoryMb());
        return ResponseEntity.ok(config);
    }

    /**
     * Trigger manual failover to take over game management.
     *
     * @return failover result
     */
    @PostMapping("/failover")
    public ResponseEntity<Map<String, Object>> triggerFailover() {
        log.warn("Manual failover triggered");
        Map<String, Object> result = new HashMap<>();

        if (!brainNodeConfig.isAutoFailoverEnabled()) {
            result.put("success", false);
            result.put("message", "Auto-failover is disabled in configuration");
            return ResponseEntity.ok(result);
        }

        // The actual failover is handled by LeaderElectionService
        result.put("success", true);
        result.put("message", "Failover initiated - LeaderElectionService will handle promotion");
        return ResponseEntity.ok(result);
    }

    // ── Model Management Endpoints ─────────────────────────────────────────────

    /**
     * Deploy a new ONNX model by uploading a file.
     *
     * @param file the ONNX model file
     * @param modelId optional model ID (generated if not provided)
     * @param version model version number
     * @param metadata optional metadata JSON
     * @return deployment result with model ID
     */
    @PostMapping(value = "/models/deploy", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> deployModel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "modelId", required = false) String modelId,
            @RequestParam(value = "version", defaultValue = "1") int version,
            @RequestParam(value = "metadata", required = false) String metadata) {
        log.info("Deploying model: {} (version: {})", modelId, version);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        try {
            String deployedId = modelManagementService.deployModel(file, modelId, version, metadata);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("modelId", deployedId);
            result.put("version", version);
            result.put("message", "Model deployed successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Model validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Model deployment failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to deploy model: " + e.getMessage()));
        }
    }

    /**
     * Deploy a new ONNX model from a URL.
     *
     * @param request containing the URL and optional model ID, version, metadata
     * @return deployment result with model ID
     */
    @PostMapping("/models/deploy/url")
    public ResponseEntity<Map<String, Object>> deployModelFromUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        String modelId = request.get("modelId");
        int version = 1;
        String metadata = request.get("metadata");
        if (request.containsKey("version")) {
            try {
                version = Integer.parseInt(request.get("version"));
            } catch (NumberFormatException e) {
                // ignore, use default
            }
        }

        log.info("Deploying model from URL: {}", url);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        try {
            String deployedId = modelManagementService.deployModelFromUrl(url, modelId, version, metadata);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("modelId", deployedId);
            result.put("version", version);
            result.put("message", "Model deployed successfully from URL");
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Model deployment from URL failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to deploy model: " + e.getMessage()));
        }
    }

    /**
     * List all cached models.
     *
     * @return list of model information
     */
    @GetMapping("/models")
    public ResponseEntity<List<Map<String, Object>>> listModels() {
        log.debug("Listing all cached models");

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(null);
        }

        List<Map<String, Object>> models = modelManagementService.listModels();
        return ResponseEntity.ok(models);
    }

    /**
     * Get details for a specific model.
     *
     * @param modelId the model ID
     * @return model details or 404 if not found
     */
    @GetMapping("/models/{modelId}")
    public ResponseEntity<Map<String, Object>> getModelDetails(@PathVariable String modelId) {
        log.debug("Fetching model details: {}", modelId);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(null);
        }

        Map<String, Object> details = modelManagementService.getModelDetails(modelId);
        if (details == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(details);
    }

    /**
     * Activate a model by swapping it into the inference engine.
     *
     * @param modelId the model ID to activate
     * @return activation result
     */
    @PostMapping("/models/{modelId}/activate")
    public ResponseEntity<Map<String, Object>> activateModel(@PathVariable String modelId) {
        log.info("Activating model: {}", modelId);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        boolean success = modelManagementService.activateModel(modelId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("modelId", modelId);
        result.put("message", success ? "Model activated successfully" : "Failed to activate model");
        return ResponseEntity.ok(result);
    }

    /**
     * Delete a cached model.
     *
     * @param modelId the model ID to delete
     * @return deletion result
     */
    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable String modelId) {
        log.info("Deleting model: {}", modelId);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        boolean success = modelManagementService.deleteModel(modelId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("modelId", modelId);
        result.put("message", success ? "Model deleted successfully" : "Failed to delete model");
        return ResponseEntity.ok(result);
    }

    /**
     * Create an A/B test configuration.
     *
     * @param request containing modelA, modelB, and trafficSplitA
     * @return creation result
     */
    @PostMapping("/models/ab-test")
    public ResponseEntity<Map<String, Object>> createAbTest(@RequestBody Map<String, Object> request) {
        String modelA = (String) request.get("modelA");
        String modelB = (String) request.get("modelB");
        if (modelA == null || modelB == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "modelA and modelB are required"));
        }

        double trafficSplitA = 50.0;
        if (request.containsKey("trafficSplitA")) {
            try {
                trafficSplitA = Double.parseDouble(request.get("trafficSplitA").toString());
            } catch (NumberFormatException e) {
                // ignore, use default
            }
        }

        log.info("Creating A/B test: A={}, B={}, split={}", modelA, modelB, trafficSplitA);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        boolean success = modelManagementService.createAbTest(modelA, modelB, trafficSplitA);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "A/B test created successfully" : "Failed to create A/B test");
        return ResponseEntity.ok(result);
    }

    /**
     * Get A/B test status and metrics.
     *
     * @return A/B test status or null if no active test
     */
    @GetMapping("/models/ab-test")
    public ResponseEntity<Map<String, Object>> getAbTestStatus() {
        log.debug("Fetching A/B test status");

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(null);
        }

        Map<String, Object> status = modelManagementService.getAbTestStatus();
        if (status == null) {
            return ResponseEntity.ok(Map.of("active", false, "message", "No active A/B test"));
        }
        return ResponseEntity.ok(status);
    }

    /**
     * Update A/B test traffic split.
     *
     * @param request containing trafficSplitA
     * @return update result
     */
    @PutMapping("/models/ab-test")
    public ResponseEntity<Map<String, Object>> updateAbTestTrafficSplit(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("trafficSplitA")) {
            return ResponseEntity.badRequest().body(Map.of("error", "trafficSplitA is required"));
        }

        double trafficSplitA;
        try {
            trafficSplitA = Double.parseDouble(request.get("trafficSplitA").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid trafficSplitA value"));
        }

        log.info("Updating A/B test traffic split: A={}", trafficSplitA);

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        boolean success = modelManagementService.updateAbTestTrafficSplit(trafficSplitA);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Traffic split updated" : "Failed to update traffic split");
        return ResponseEntity.ok(result);
    }

    /**
     * Stop the current A/B test.
     *
     * @return result
     */
    @DeleteMapping("/models/ab-test")
    public ResponseEntity<Map<String, Object>> stopAbTest() {
        log.info("Stopping A/B test");

        if (modelManagementService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Model management service not available"));
        }

        modelManagementService.stopAbTest();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "A/B test stopped");
        return ResponseEntity.ok(result);
    }
}
