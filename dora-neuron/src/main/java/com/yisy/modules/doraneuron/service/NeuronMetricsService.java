package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects and reports metrics for the Neuron monitoring dashboard.
 *
 * Tracks JVM memory, CPU usage, request counts, inference statistics,
 * and cluster connectivity. Provides a unified metrics endpoint for
 * monitoring systems and the dashboard.
 */
@Service
public class NeuronMetricsService {

    private static final Logger log = LoggerFactory.getLogger(NeuronMetricsService.class);

    private final NeuronConfig config;
    private final NeuronRegistryService registryService;

    /** Timestamp when this Neuron started (epoch ms). */
    private long startedAt;

    /** Total HTTP requests received. */
    private final AtomicLong totalRequests = new AtomicLong(0);

    /** Total HTTP errors. */
    private final AtomicLong totalErrors = new AtomicLong(0);

    /** Total inference requests. */
    private final AtomicLong totalInferences = new AtomicLong(0);

    /** Total inference errors. */
    private final AtomicLong totalInferenceErrors = new AtomicLong(0);

    /** Average inference time in milliseconds. */
    private volatile double avgInferenceTimeMs;

    /** Last known cluster status. */
    private volatile String clusterStatus = "UNKNOWN";

    public NeuronMetricsService(NeuronConfig config, NeuronRegistryService registryService) {
        this.config = config;
        this.registryService = registryService;
    }

    @PostConstruct
    public void init() {
        this.startedAt = System.currentTimeMillis();
        log.info("Neuron Metrics Service initialized");
    }

    /**
     * Record an HTTP request.
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * Record an HTTP error.
     */
    public void recordError() {
        totalErrors.incrementAndGet();
    }

    /**
     * Record an inference request with its duration.
     */
    public void recordInference(long durationMs) {
        totalInferences.incrementAndGet();
        // Exponential moving average for inference time
        double alpha = 0.1;
        avgInferenceTimeMs = alpha * durationMs + (1 - alpha) * avgInferenceTimeMs;
    }

    /**
     * Record an inference error.
     */
    public void recordInferenceError() {
        totalInferenceErrors.incrementAndGet();
    }

    /**
     * Update the cluster status from the coordinator.
     */
    public void setClusterStatus(String status) {
        this.clusterStatus = status;
    }

    /**
     * Get comprehensive metrics for the dashboard.
     */
    public Map<String, Object> getMetrics() {
        NeuronIdentity identity = registryService.getIdentity();

        // JVM memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

        // OS metrics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long availableProcessors = osBean.getAvailableProcessors();
        double cpuLoad = 0.0;
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            cpuLoad = sunOsBean.getSystemCpuLoad();
        }

        long uptime = System.currentTimeMillis() - startedAt;

        return Map.of(
                "identity", Map.of(
                        "nodeId", identity != null ? identity.getNodeId() : "unknown",
                        "nodeName", identity != null ? identity.getNodeName() : "unknown",
                        "region", identity != null ? identity.getRegion() : "unknown"
                ),
                "uptime", Map.of(
                        "milliseconds", uptime,
                        "seconds", uptime / 1000
                ),
                "jvm", Map.of(
                        "heapUsed", heapUsage.getUsed(),
                        "heapMax", heapUsage.getMax(),
                        "heapUsagePercent", (double) heapUsage.getUsed() / heapUsage.getMax() * 100
                ),
                "cpu", Map.of(
                        "load", cpuLoad,
                        "availableProcessors", availableProcessors
                ),
                "http", Map.of(
                        "totalRequests", totalRequests.get(),
                        "totalErrors", totalErrors.get(),
                        "errorRate", totalRequests.get() > 0
                                ? (double) totalErrors.get() / totalRequests.get() * 100
                                : 0.0
                ),
                "inference", Map.of(
                        "totalInferences", totalInferences.get(),
                        "totalErrors", totalInferenceErrors.get(),
                        "avgTimeMs", avgInferenceTimeMs
                ),
                "cluster", Map.of(
                        "status", clusterStatus,
                        "coordinatorUrl", config.getCoordinatorUrl()
                )
        );
    }

    /**
     * Get the Neuron uptime in milliseconds.
     */
    public long getUptimeMs() {
        return System.currentTimeMillis() - startedAt;
    }

    /**
     * Get the total number of HTTP requests.
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * Get the total number of HTTP errors.
     */
    public long getTotalErrors() {
        return totalErrors.get();
    }

    /**
     * Get the total number of inferences.
     */
    public long getTotalInferences() {
        return totalInferences.get();
    }

    /**
     * Get the average inference time in milliseconds.
     */
    public double getAvgInferenceTimeMs() {
        return avgInferenceTimeMs;
    }
}
