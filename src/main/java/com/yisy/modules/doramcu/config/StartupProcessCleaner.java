package com.yisy.modules.doramcu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.lang.ProcessHandle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Detects and terminates any previously running DoraMCU server instances
 * before the new instance starts. Runs once during Spring context initialization.
 * <p>
 * Uses the Java 9+ {@link ProcessHandle} API to iterate all processes,
 * matching DoraMCU by main class name or JAR name in the command line.
 * The current process (own PID) is always excluded from the kill list.
 * </p>
 * <p>
 * Shutdown strategy: graceful {@code destroy()} first, wait 5 seconds,
 * then force-kill with {@code destroyForcibly()} if the process is still alive.
 * </p>
 */
@Component
public class StartupProcessCleaner {

    private static final Logger log = LoggerFactory.getLogger(StartupProcessCleaner.class);

    private final StartupProgressService startupProgressService;

    @Value("${doramcu.startup.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    public StartupProcessCleaner(StartupProgressService startupProgressService) {
        this.startupProgressService = startupProgressService;
    }

    @PostConstruct
    public void cleanUpPreviousProcesses() {
        if (!cleanupEnabled) {
            log.info("Process cleanup is disabled by configuration. Skipping.");
            startupProgressService.skipStep("process_cleanup", "Cleanup disabled by configuration");
            return;
        }

        startupProgressService.startStep("process_cleanup", "Scanning for previous DoraMCU processes");

        long currentPid = ProcessHandle.current().pid();
        log.info("Current process PID: {}", currentPid);

        final int[] killedCount = {0};
        try {
            ProcessHandle.allProcesses().forEach(process -> {
                long pid = process.pid();
                if (pid == currentPid) {
                    return; // Skip self
                }

                if (isDoraMcuProcess(process)) {
                    log.info("Found DoraMCU process (PID: {}) - attempting graceful shutdown", pid);
                    if (killProcess(process, pid)) {
                        killedCount[0]++;
                    }
                }
            });

            if (killedCount[0] > 0) {
                startupProgressService.completeStep(
                    "process_cleanup",
                    "Terminated " + killedCount[0] + " previous DoraMCU process(es)"
                );
                log.info("Process cleanup complete: terminated {} process(es)", killedCount[0]);
            } else {
                startupProgressService.completeStep(
                    "process_cleanup",
                    "No previous DoraMCU processes found"
                );
                log.info("Process cleanup complete: no previous processes found");
            }
        } catch (Exception e) {
            log.error("Error during process cleanup", e);
            startupProgressService.errorStep("process_cleanup", "Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Checks whether a process is a DoraMCU server instance by inspecting
     * its command line arguments for known markers.
     */
    private boolean isDoraMcuProcess(ProcessHandle process) {
        ProcessHandle.Info info = process.info();

        // Check command line for DoraMCU markers
        if (info.commandLine().isPresent()) {
            String commandLine = info.commandLine().orElse("").toLowerCase();
            return commandLine.contains("doramcu")
                || commandLine.contains("doramculauncher")
                || commandLine.contains("gamelauncher");
        }

        // Fallback: check process arguments
        if (info.arguments().isPresent()) {
            String[] args = info.arguments().orElseGet(() -> new String[0]);
            for (String arg : args) {
                String lower = arg.toLowerCase();
                if (lower.contains("doramcu")
                    || lower.contains("doramculauncher")
                    || lower.contains("gamelauncher")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Attempts to kill a process gracefully, then force-kills if it doesn't
     * respond within 5 seconds.
     *
     * @return true if the process was successfully terminated
     */
    private boolean killProcess(ProcessHandle process, long pid) {
        // Step 1: Graceful shutdown
        boolean destroyed = process.destroy();
        if (!destroyed) {
            log.warn("Failed to send graceful shutdown signal to PID: {}", pid);
        }

        // Step 2: Wait up to 5 seconds for graceful exit
        try {
            process.onExit().get(5, TimeUnit.SECONDS);
            log.info("PID {} stopped gracefully", pid);
            return true;
        } catch (TimeoutException e) {
            // Process didn't stop in time, will force kill below
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for PID {} to stop", pid);
        } catch (java.util.concurrent.ExecutionException e) {
            log.warn("Execution exception waiting for PID {}", pid, e);
        }

        // Step 3: Force kill
        log.warn("PID {} did not stop gracefully - force killing", pid);
        process.destroyForcibly();
        try {
            process.onExit().get(5, TimeUnit.SECONDS);
            log.info("PID {} force killed successfully", pid);
            return true;
        } catch (TimeoutException e) {
            log.error("PID {} did not terminate even after force kill", pid);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to force kill PID: {}", pid);
            return false;
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Execution exception force killing PID: {}", pid, e);
            return false;
        }
    }
}
