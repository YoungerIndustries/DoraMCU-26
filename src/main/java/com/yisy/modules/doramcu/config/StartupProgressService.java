package com.yisy.modules.doramcu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the progress of the DoraMCU startup sequence, exposing each step's
 * status via a simple API for the startup modal to poll.
 * <p>
 * The sequence covers server initialization (steps 1–9), game client deployment
 * (steps 10–12), and backup database synchronization (steps 13–14).
 * Step 5 (Earth 2026 Hub) initializes the core starting module before galaxy generation.
 * </p>
 */
@Service
public class StartupProgressService {

    private static final Logger log = LoggerFactory.getLogger(StartupProgressService.class);

    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED,
        ERROR
    }

    public record StartupStep(
            int order,
            String key,
            String label,
            StepStatus status,
            String message
    ) {}

    private final Map<String, StartupStep> steps = new LinkedHashMap<>();

    public StartupProgressService() {
        // Server initialization steps (1–8)
        registerStep(1, "process_cleanup", "Cleaning up previous processes");
        registerStep(2, "spring_context", "Initializing Spring context");
        registerStep(3, "database_connection", "Connecting to database");
        registerStep(4, "seed_data", "Loading seed data");
        registerStep(5, "earth_2026_hub", "Initializing Earth 2026 Hub");
        registerStep(6, "galaxy_generation", "Generating galaxy");
        registerStep(7, "game_session", "Creating game session");
        registerStep(8, "ai_players", "Initializing AI players");
        registerStep(9, "server_ready", "Server ready");

        // Game client deployment steps (10-12)
        registerStep(10, "client_download", "Downloading Game Client");
        registerStep(11, "client_startup", "Starting Game Client");
        registerStep(12, "client_registration", "Game Client registering with server");

        // Backup database steps (13-14)
        registerStep(13, "backup_snapshot", "Initializing backup database");
        registerStep(14, "backup_sync", "Backup database synced");
    }

    private void registerStep(int order, String key, String label) {
        steps.put(key, new StartupStep(order, key, label, StepStatus.PENDING, null));
    }

    /**
     * Returns all startup steps in order.
     */
    public List<StartupStep> getAllSteps() {
        return new ArrayList<>(steps.values());
    }

    /**
     * Returns a single step by key.
     */
    public StartupStep getStep(String key) {
        return steps.get(key);
    }

    /**
     * Updates the status of a step.
     */
    public void setStepStatus(String key, StepStatus status, String message) {
        StartupStep step = steps.get(key);
        if (step != null) {
            steps.put(key, new StartupStep(step.order(), step.key(), step.label(), status, message));
            log.info("Startup step [{}] {}: {}", key, status, message != null ? message : "");
        }
    }

    /**
     * Marks a step as in progress.
     */
    public void startStep(String key, String message) {
        setStepStatus(key, StepStatus.IN_PROGRESS, message);
    }

    /**
     * Marks a step as completed.
     */
    public void completeStep(String key, String message) {
        setStepStatus(key, StepStatus.COMPLETED, message);
    }

    /**
     * Marks a step as skipped.
     */
    public void skipStep(String key, String message) {
        setStepStatus(key, StepStatus.SKIPPED, message);
    }

    /**
     * Marks a step as errored.
     */
    public void errorStep(String key, String message) {
        setStepStatus(key, StepStatus.ERROR, message);
    }

    /**
     * Returns true if all steps are completed or skipped.
     */
    public boolean isComplete() {
        return steps.values().stream()
                .allMatch(s -> s.status() == StepStatus.COMPLETED || s.status() == StepStatus.SKIPPED);
    }

    /**
     * Returns true if any step has an error status.
     */
    public boolean hasErrors() {
        return steps.values().stream()
                .anyMatch(s -> s.status() == StepStatus.ERROR);
    }
}
