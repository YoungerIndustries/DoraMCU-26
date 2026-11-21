package com.yisy.modules.doradl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Lightweight memory configuration for the AI module.
 * Replaces the heavy ND4J/DL4J memory manager with simple JVM memory monitoring.
 * No native libraries required — works on any platform including Raspberry Pi.
 */
@Configuration
public class Dl4jMemoryConfig {

    private static final Logger log = LoggerFactory.getLogger(Dl4jMemoryConfig.class);

    @PostConstruct
    public void init() {
        log.info("Lightweight AI memory configuration initialized (no native DL4J libraries)");
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
