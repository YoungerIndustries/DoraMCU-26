package com.yisy.modules.doradl.config;

import org.bytedeco.javacpp.Pointer;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Initializes ND4J memory management settings at application startup.
 * <p>
 * This configuration addresses the confirmed DL4J OOM crashes caused by
 * JavaCPP physical memory leaking to 4097 MB (exceeding the 4096 MB cap).
 * The root cause was disabled periodic GC and unbounded memory growth.
 * </p>
 * <p>
 * Key settings applied:
 * <ul>
 *   <li>Enable periodic GC so JavaCPP automatically releases native memory</li>
 *   <li>Set auto GC window to reclaim memory more aggressively</li>
 * </ul>
 * </p>
 * <p>
 * Workspace mode is configured per-network in {@code NeuralNetConfiguration.Builder}
 * via {@code trainingWorkspaceMode(WorkspaceMode.ENABLED)} and
 * {@code inferenceWorkspaceMode(WorkspaceMode.ENABLED)}, which is the default.
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "false", matchIfMissing = true)
public class Dl4jMemoryConfig {

    private static final Logger log = LoggerFactory.getLogger(Dl4jMemoryConfig.class);

    @PostConstruct
    public void init() {
        // Enable periodic GC to prevent JavaCPP native memory leaks
        // This was the primary cause of the OOM crashes: "Periodic GC Enabled: false"
        Nd4j.getMemoryManager().togglePeriodicGc(true);
        log.info("ND4J periodic GC enabled");

        // Reduced auto GC window from 10s to 5s for more frequent memory reclamation
        // This reduces memory spikes between GC cycles
        Nd4j.getMemoryManager().setAutoGcWindow(5000);
        log.info("ND4J auto GC window set to 5 seconds");

        // Log current memory manager state
        long maxPhysical = Pointer.maxPhysicalBytes();
        log.info("JavaCPP maxPhysicalBytes: {} MB", maxPhysical / (1024L * 1024));

        // Log JVM memory info for monitoring
        logJvmMemoryInfo();
    }

    /**
     * Logs current JVM memory usage for monitoring purposes.
     */
    private void logJvmMemoryInfo() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            log.info("JVM Heap Memory - Used: {} MB, Committed: {} MB, Max: {} MB",
                    heapUsage.getUsed() / (1024L * 1024),
                    heapUsage.getCommitted() / (1024L * 1024),
                    heapUsage.getMax() / (1024L * 1024));
            log.info("JVM Non-Heap Memory - Used: {} MB, Committed: {} MB",
                    nonHeapUsage.getUsed() / (1024L * 1024),
                    nonHeapUsage.getCommitted() / (1024L * 1024));
        } catch (Exception e) {
            log.warn("Failed to log JVM memory info: {}", e.getMessage());
        }
    }
}
