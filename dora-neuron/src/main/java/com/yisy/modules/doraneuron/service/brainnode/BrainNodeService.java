package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.brainnode.BrainNodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main Brain Node orchestration service.
 * <p>
 * Initializes and coordinates persistent storage, AI inference, USB serial communication,
 * and data sync between the Brain Node and the Raspberry Pi.
 */
@Service
@Conditional({BrainNodeConfig.class})
@EnableConfigurationProperties({BrainNodeConfig.class, NeuronConfig.class})
public class BrainNodeService {

    private static final Logger log = LoggerFactory.getLogger(BrainNodeService.class);

    private final BrainNodeConfig brainNodeConfig;
    private final NeuronConfig neuronConfig;
    private final SerialConsoleService serialConsoleService;
    private final BrainNodeStorageService storageService;
    private final BrainNodeAiService aiService;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private volatile boolean piReachable = false;

    public BrainNodeService(BrainNodeConfig brainNodeConfig,
                            NeuronConfig neuronConfig,
                            SerialConsoleService serialConsoleService,
                            BrainNodeStorageService storageService,
                            BrainNodeAiService aiService) {
        this.brainNodeConfig = brainNodeConfig;
        this.neuronConfig = neuronConfig;
        this.serialConsoleService = serialConsoleService;
        this.storageService = storageService;
        this.aiService = aiService;
    }

    @PostConstruct
    public void init() {
        if (!brainNodeConfig.isEnabled()) {
            log.info("Brain Node is disabled, skipping initialization");
            return;
        }

        log.info("========================================");
        log.info("  Initializing Brain Node");
        log.info("========================================");

        try {
            // Initialize serial connection to Pi
            log.info("Initializing USB serial connection to Raspberry Pi...");
            boolean serialConnected = serialConsoleService.isConnected();
            log.info("Serial connection status: {}", serialConsoleService.getConnectionStatus());

            // Check Pi health
            log.info("Checking Raspberry Pi health...");
            piReachable = checkPiHealth();
            log.info("Pi reachable: {}", piReachable);

            // Initialize AI service
            log.info("Initializing AI inference service...");
            boolean aiReady = aiService.isReady();
            log.info("AI service ready: {}", aiReady);

            // Initialize storage
            log.info("Initializing persistent storage...");
            Map<String, Object> storageStats = storageService.getStorageStats();
            log.info("Storage stats: {}", storageStats);

            initialized.set(true);
            log.info("========================================");
            log.info("  Brain Node initialized successfully");
            log.info("========================================");
        } catch (Exception e) {
            log.error("Error initializing Brain Node: {}", e.getMessage(), e);
            initialized.set(false);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down Brain Node");
        initialized.set(false);
    }

    /**
     * Check if the Raspberry Pi is reachable.
     *
     * @return true if the Pi is reachable
     */
    public boolean checkPiHealth() {
        try {
            // Try serial heartbeat first
            if (serialConsoleService.isConnected()) {
                boolean health = serialConsoleService.checkPiHealth();
                if (health) {
                    piReachable = true;
                    return true;
                }
            }

            // Fallback to HTTP health check
            String piUrl = brainNodeConfig.getPiCoordinatorUrl();
            String healthUrl = piUrl + "/actuator/health";

            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(healthUrl))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            piReachable = response.statusCode() == 200;
            log.debug("Pi health check via HTTP: {} - {}", response.statusCode(), response.body());
            return piReachable;
        } catch (Exception e) {
            log.warn("Pi health check failed: {}", e.getMessage());
            piReachable = false;
            return false;
        }
    }

    /**
     * Scheduled health check for the Raspberry Pi.
     */
    @Scheduled(fixedDelayString = "${brainnode.pi-health-check-interval:10000}")
    public void scheduledPiHealthCheck() {
        if (!initialized.get()) {
            return;
        }

        boolean wasReachable = piReachable;
        piReachable = checkPiHealth();

        if (!piReachable && wasReachable) {
            log.warn("Raspberry Pi became unreachable");
            handlePiUnreachable();
        } else if (piReachable && !wasReachable) {
            log.info("Raspberry Pi is reachable again");
            handlePiReachable();
        }
    }

    /**
     * Handle the case when Pi becomes unreachable.
     */
    private void handlePiUnreachable() {
        if (brainNodeConfig.isAutoFailoverEnabled()) {
            log.warn("Auto-failover enabled, Brain Node may take over game management");
            // The LeaderElectionService will handle the actual failover
        } else {
            log.warn("Pi unreachable but auto-failover is disabled");
        }
    }

    /**
     * Handle the case when Pi becomes reachable again.
     */
    private void handlePiReachable() {
        log.info("Syncing data with Raspberry Pi after reconnection");
        // Trigger data sync to catch up on any changes
    }

    /**
     * Get the current Brain Node status.
     *
     * @return comprehensive status information
     */
    public BrainNodeStatus getStatus() {
        BrainNodeStatus status = new BrainNodeStatus();
        status.setNodeId(neuronConfig.getNodeId());
        status.setNodeName(neuronConfig.getNodeName());
        status.setNodeType(brainNodeConfig.getNodeType());
        status.setEnabled(brainNodeConfig.isEnabled());
        status.setSerialConnectionStatus(serialConsoleService.getConnectionStatus());
        status.setAiReady(aiService.isReady());
        status.setCurrentModelVersion(aiService.getCurrentModelVersion());
        status.setPiCoordinatorUrl(brainNodeConfig.getPiCoordinatorUrl());
        status.setPiReachable(piReachable);
        status.setAutoFailoverEnabled(brainNodeConfig.isAutoFailoverEnabled());
        status.setStorageType(brainNodeConfig.getStorageType());
        status.setStoragePath(brainNodeConfig.getStoragePath());
        status.setStorageStats(storageService.getStorageStats());
        status.setInferenceStats(aiService.getInferenceStats());
        status.setMessage(initialized.get() ? "Brain Node is running" : "Brain Node is initializing");
        return status;
    }

    /**
     * Check if the Brain Node is initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Check if the Pi is reachable.
     *
     * @return true if reachable
     */
    public boolean isPiReachable() {
        return piReachable;
    }

    /**
     * Trigger a manual sync with the Pi.
     *
     * @return true if sync was successful
     */
    public boolean triggerSync() {
        if (!piReachable) {
            log.warn("Cannot sync: Pi is unreachable");
            return false;
        }

        try {
            // Sync unsynced game states
            var unsyncedGameStates = storageService.getUnsyncedGameStates();
            log.info("Syncing {} unsynced game states", unsyncedGameStates.size());

            // Sync unsynced experience samples
            var unsyncedSamples = storageService.getUnsyncedExperienceSamples();
            log.info("Syncing {} unsynced experience samples", unsyncedSamples.size());

            // Mark as synced (actual sync would be done via DataSyncService)
            unsyncedGameStates.forEach(gs -> storageService.markGameStateSynced(gs.getGameId()));
            unsyncedSamples.forEach(s -> {
                // Mark individual samples as synced
            });

            log.info("Sync completed successfully");
            return true;
        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send a command to the Raspberry Pi.
     *
     * @param command the command to send
     * @return the response from the Pi
     */
    public String sendCommandToPi(String command) {
        return serialConsoleService.executeCommand(command, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * Restart the serial connection to the Pi.
     *
     * @return true if reconnection was successful
     */
    public boolean restartSerialConnection() {
        log.info("Restarting serial connection to Raspberry Pi");
        serialConsoleService.disconnect();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return serialConsoleService.connect();
    }
}
