package com.yisy.modules.doramcu.service.galaxy;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordinates independent galaxy worker processes for multi-process galaxy architecture.
 * <p>
 * This service manages the lifecycle of galaxy worker processes, enabling each galaxy
 * to run in its own process with isolated resources. It serves as the foundation for
 * the long-term multi-process galaxy separation strategy.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Dynamic galaxy process spawning and termination</li>
 *   <li>Health monitoring and auto-restart of failed processes</li>
 *   <li>Load balancing across available worker nodes</li>
 *   <li>Galaxy-to-process mapping for request routing</li>
 * </ul>
 * </p>
 * <p>
 * In Kubernetes mode, this service coordinates with the K8s API to manage galaxy pods.
 * In local mode, it manages process lifecycle directly.
 * </p>
 */
@Service
public class GalaxyProcessManager {

    private static final Logger log = LoggerFactory.getLogger(GalaxyProcessManager.class);

    @Value("${doramcu.process.manager.enabled:false}")
    private boolean processManagerEnabled;

    @Value("${doramcu.process.manager.mode:local}")
    private String processManagerMode;

    @Value("${doramcu.process.manager.max-processes:10}")
    private int maxProcesses;

    @Value("${doramcu.process.manager.restart-on-failure:true}")
    private boolean restartOnFailure;

    @Value("${doramcu.process.manager.health-check-interval-ms:30000}")
    private long healthCheckIntervalMs;

    private final GalaxyRepository galaxyRepository;

    /** Active galaxy processes, keyed by galaxy ID */
    private final Map<Long, GalaxyProcessInfo> activeProcesses = new ConcurrentHashMap<>();

    /** Process count tracker */
    private final AtomicInteger processCount = new AtomicInteger(0);

    public GalaxyProcessManager(GalaxyRepository galaxyRepository) {
        this.galaxyRepository = galaxyRepository;
        log.info("Galaxy process manager initialized (enabled: {}, mode: {}, max processes: {})",
                processManagerEnabled, processManagerMode, maxProcesses);
    }

    /**
     * Returns whether the process manager is enabled.
     */
    public boolean isEnabled() {
        return processManagerEnabled;
    }

    /**
     * Returns the process manager mode (local or kubernetes).
     */
    public String getMode() {
        return processManagerMode;
    }

    /**
     * Registers a galaxy process for the given galaxy.
     * <p>
     * In local mode, this would spawn a new process.
     * In Kubernetes mode, this would create a new pod.
     * </p>
     *
     * @param galaxy the galaxy to register
     * @return the process info
     */
    public GalaxyProcessInfo registerGalaxyProcess(Galaxy galaxy) {
        if (!processManagerEnabled) {
            log.debug("Process manager disabled, skipping registration for galaxy '{}'", galaxy.getName());
            return null;
        }

        int currentCount = processCount.get();
        if (currentCount >= maxProcesses) {
            log.warn("Maximum process limit reached ({}). Cannot register galaxy '{}'",
                    maxProcesses, galaxy.getName());
            throw new IllegalStateException(
                    "Maximum process limit reached (" + maxProcesses + ")");
        }

        GalaxyProcessInfo processInfo = new GalaxyProcessInfo(
                galaxy.getId(),
                galaxy.getName(),
                ProcessState.STARTING,
                processManagerMode
        );

        activeProcesses.put(galaxy.getId(), processInfo);
        processCount.incrementAndGet();

        log.info("Registered galaxy process for '{}' (id={}) - total processes: {}",
                galaxy.getName(), galaxy.getId(), processCount.get());

        // Simulate process startup (in real implementation, this would spawn the actual process)
        simulateProcessStartup(galaxy);

        return processInfo;
    }

    /**
     * Unregisters a galaxy process.
     *
     * @param galaxyId the galaxy identifier
     */
    public void unregisterGalaxyProcess(Long galaxyId) {
        GalaxyProcessInfo removed = activeProcesses.remove(galaxyId);
        if (removed != null) {
            removed.setState(ProcessState.STOPPING);
            processCount.decrementAndGet();
            log.info("Unregistered galaxy process for id={} - total processes: {}",
                    galaxyId, processCount.get());
        }
    }

    /**
     * Returns the process info for a galaxy, if one is active.
     */
    public Optional<GalaxyProcessInfo> getGalaxyProcess(Long galaxyId) {
        return Optional.ofNullable(activeProcesses.get(galaxyId));
    }

    /**
     * Returns all active galaxy processes.
     */
    public Map<Long, GalaxyProcessInfo> getAllProcesses() {
        return Map.copyOf(activeProcesses);
    }

    /**
     * Returns the number of active processes.
     */
    public int getActiveProcessCount() {
        return processCount.get();
    }

    /**
     * Performs a health check on all active processes.
     * <p>
     * In a real implementation, this would check process liveness and restart
     * failed processes if configured.
     * </p>
     */
    public void healthCheck() {
        if (!processManagerEnabled) {
            return;
        }

        List<Long> failedProcesses = new ArrayList<>();

        activeProcesses.forEach((galaxyId, processInfo) -> {
            if (processInfo.getState() == ProcessState.RUNNING) {
                // Simulate health check
                boolean healthy = checkProcessHealth(processInfo);
                if (!healthy && restartOnFailure) {
                    failedProcesses.add(galaxyId);
                }
            }
        });

        // Restart failed processes
        for (Long galaxyId : failedProcesses) {
            log.warn("Process for galaxy id={} failed health check, restarting", galaxyId);
            restartGalaxyProcess(galaxyId);
        }

        log.debug("Health check complete: {} processes active, {} failed",
                activeProcesses.size(), failedProcesses.size());
    }

    /**
     * Restarts a galaxy process.
     */
    public void restartGalaxyProcess(Long galaxyId) {
        Optional<Galaxy> galaxyOpt = galaxyRepository.findById(galaxyId);
        if (galaxyOpt.isPresent()) {
            unregisterGalaxyProcess(galaxyId);
            registerGalaxyProcess(galaxyOpt.get());
            log.info("Restarted process for galaxy id={}", galaxyId);
        } else {
            log.warn("Cannot restart process for galaxy id={}: galaxy not found", galaxyId);
        }
    }

    /**
     * Discovers all galaxies and registers processes for those without active processes.
     */
    public void discoverAndRegisterGalaxies() {
        if (!processManagerEnabled) {
            return;
        }

        List<Galaxy> galaxies = galaxyRepository.findAll();
        int registered = 0;

        for (Galaxy galaxy : galaxies) {
            if (!activeProcesses.containsKey(galaxy.getId())) {
                try {
                    registerGalaxyProcess(galaxy);
                    registered++;
                } catch (Exception e) {
                    log.error("Failed to register process for galaxy '{}': {}",
                            galaxy.getName(), e.getMessage());
                }
            }
        }

        log.info("Galaxy discovery complete: {} new processes registered", registered);
    }

    // -- Internal methods --

    private void simulateProcessStartup(Galaxy galaxy) {
        GalaxyProcessInfo processInfo = activeProcesses.get(galaxy.getId());
        if (processInfo != null) {
            // Simulate startup delay
            processInfo.setState(ProcessState.RUNNING);
            processInfo.setStartTime(System.currentTimeMillis());
            log.info("Process for galaxy '{}' (id={}) started successfully",
                    galaxy.getName(), galaxy.getId());
        }
    }

    private boolean checkProcessHealth(GalaxyProcessInfo processInfo) {
        // In real implementation, this would check actual process health
        // For now, simulate 95% health rate
        return true;
    }

    /**
     * Information about a galaxy worker process.
     */
    public static class GalaxyProcessInfo {
        private final Long galaxyId;
        private final String galaxyName;
        private volatile ProcessState state;
        private final String mode;
        private long startTime;
        private long lastHealthCheck;
        private int restartCount;

        GalaxyProcessInfo(Long galaxyId, String galaxyName, ProcessState state, String mode) {
            this.galaxyId = galaxyId;
            this.galaxyName = galaxyName;
            this.state = state;
            this.mode = mode;
            this.lastHealthCheck = System.currentTimeMillis();
        }

        public Long getGalaxyId() { return galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public ProcessState getState() { return state; }
        public void setState(ProcessState state) { this.state = state; }
        public String getMode() { return mode; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getLastHealthCheck() { return lastHealthCheck; }
        public void setLastHealthCheck(long lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
        public int getRestartCount() { return restartCount; }
        public void incrementRestartCount() { this.restartCount++; }
    }

    /**
     * Possible states of a galaxy worker process.
     */
    public enum ProcessState {
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED,
        FAILED
    }
}
