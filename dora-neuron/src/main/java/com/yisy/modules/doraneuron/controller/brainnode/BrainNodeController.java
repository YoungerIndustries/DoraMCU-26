package com.yisy.modules.doraneuron.controller.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.model.brainnode.BrainNodeStatus;
import com.yisy.modules.doraneuron.service.brainnode.BrainNodeService;
import com.yisy.modules.doraneuron.service.brainnode.SerialConsoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@Conditional({BrainNodeConfig.class})
@EnableConfigurationProperties(BrainNodeConfig.class)
public class BrainNodeController {

    private static final Logger log = LoggerFactory.getLogger(BrainNodeController.class);

    private final BrainNodeService brainNodeService;
    private final SerialConsoleService serialConsoleService;
    private final BrainNodeConfig brainNodeConfig;

    public BrainNodeController(BrainNodeService brainNodeService,
                               SerialConsoleService serialConsoleService,
                               BrainNodeConfig brainNodeConfig) {
        this.brainNodeService = brainNodeService;
        this.serialConsoleService = serialConsoleService;
        this.brainNodeConfig = brainNodeConfig;
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
}
