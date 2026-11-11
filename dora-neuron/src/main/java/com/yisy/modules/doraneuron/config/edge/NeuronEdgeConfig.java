package com.yisy.modules.doraneuron.config.edge;

import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Edge-specific configuration properties that override or supplement
 * the base {@code NeuronConfig} based on detected hardware capabilities.
 *
 * When running on an edge device (Raspberry Pi, Jetson Nano, etc.),
 * this configuration automatically adjusts resource-intensive settings
 * to prevent out-of-memory errors and CPU overload.
 *
 * Properties are prefixed with {@code neuron.edge} in application.properties.
 */
@ConfigurationProperties(prefix = "neuron.edge")
public class NeuronEdgeConfig {

    private static final Logger log = LoggerFactory.getLogger(NeuronEdgeConfig.class);

    /** Enable automatic edge optimization based on detected hardware. */
    private boolean autoOptimize = true;

    /** Force a specific system profile (overrides auto-detection). */
    private String forcedProfile;

    /** Enable model quantization for edge devices (INT8). */
    private boolean quantizationEnabled = true;

    /** Path to store quantized models. */
    private String quantizedModelDir = "/tmp/neuron-quantized-models";

    /** Enable thermal throttling detection (Linux-only). */
    private boolean thermalDetectionEnabled = true;

    /** CPU temperature threshold for throttling (Celsius). */
    private double thermalThrottleThreshold = 80.0;

    /** Reduce inference frequency when thermally throttled. */
    private boolean reduceInferenceOnThrottle = true;

    /** Enable swap space monitoring on Linux. */
    private boolean swapMonitoringEnabled = true;

    /** Maximum heap size recommendation for edge devices. */
    private long maxHeapMb = 512;

    /** JVM GC algorithm recommendation for edge devices. */
    private String recommendedGc = "G1GC";

    /**
     * Get the effective system profile, considering forced override.
     */
    public SystemResourceDetector.SystemProfile getEffectiveProfile(SystemResourceDetector detector) {
        if (forcedProfile != null && !forcedProfile.isEmpty()) {
            try {
                return SystemResourceDetector.SystemProfile.valueOf(forcedProfile.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid forced profile '{}', using auto-detected: {}", forcedProfile, detector.getProfile());
            }
        }
        return detector.getProfile();
    }

    /**
     * Check if auto-optimization is enabled and the system is an edge device.
     */
    public boolean isEdgeOptimizationActive(SystemResourceDetector detector) {
        return autoOptimize && detector.isEdgeDevice();
    }

    /**
     * Get the recommended inference threads, considering edge optimization.
     */
    public int getInferenceThreads(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return 2; // Default
        }
        return detector.getRecommendedInferenceThreads();
    }

    /**
     * Get the recommended experience buffer size, considering edge optimization.
     */
    public int getExperienceBufferSize(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return 500; // Default
        }
        return detector.getRecommendedExperienceBufferSize();
    }

    /**
     * Get the recommended flush interval, considering edge optimization.
     */
    public long getFlushIntervalMs(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return 30000; // Default
        }
        return detector.getRecommendedFlushIntervalMs();
    }

    /**
     * Get the recommended heartbeat interval, considering edge optimization.
     */
    public long getHeartbeatIntervalMs(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return 15000; // Default
        }
        return detector.getRecommendedHeartbeatIntervalMs();
    }

    /**
     * Check if mesh should be enabled, considering edge optimization.
     */
    public boolean isMeshEnabled(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return true; // Default
        }
        return detector.shouldEnableMesh();
    }

    /**
     * Check if consensus should be enabled, considering edge optimization.
     */
    public boolean isConsensusEnabled(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return true; // Default
        }
        return detector.shouldEnableConsensus();
    }

    /**
     * Get the recommended max mesh peers, considering edge optimization.
     */
    public int getMaxMeshPeers(SystemResourceDetector detector) {
        if (!autoOptimize) {
            return 5; // Default
        }
        return detector.getRecommendedMaxMeshPeers();
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public boolean isAutoOptimize() {
        return autoOptimize;
    }

    public void setAutoOptimize(boolean autoOptimize) {
        this.autoOptimize = autoOptimize;
    }

    public String getForcedProfile() {
        return forcedProfile;
    }

    public void setForcedProfile(String forcedProfile) {
        this.forcedProfile = forcedProfile;
    }

    public boolean isQuantizationEnabled() {
        return quantizationEnabled;
    }

    public void setQuantizationEnabled(boolean quantizationEnabled) {
        this.quantizationEnabled = quantizationEnabled;
    }

    public String getQuantizedModelDir() {
        return quantizedModelDir;
    }

    public void setQuantizedModelDir(String quantizedModelDir) {
        this.quantizedModelDir = quantizedModelDir;
    }

    public boolean isThermalDetectionEnabled() {
        return thermalDetectionEnabled;
    }

    public void setThermalDetectionEnabled(boolean thermalDetectionEnabled) {
        this.thermalDetectionEnabled = thermalDetectionEnabled;
    }

    public double getThermalThrottleThreshold() {
        return thermalThrottleThreshold;
    }

    public void setThermalThrottleThreshold(double thermalThrottleThreshold) {
        this.thermalThrottleThreshold = thermalThrottleThreshold;
    }

    public boolean isReduceInferenceOnThrottle() {
        return reduceInferenceOnThrottle;
    }

    public void setReduceInferenceOnThrottle(boolean reduceInferenceOnThrottle) {
        this.reduceInferenceOnThrottle = reduceInferenceOnThrottle;
    }

    public boolean isSwapMonitoringEnabled() {
        return swapMonitoringEnabled;
    }

    public void setSwapMonitoringEnabled(boolean swapMonitoringEnabled) {
        this.swapMonitoringEnabled = swapMonitoringEnabled;
    }

    public long getMaxHeapMb() {
        return maxHeapMb;
    }

    public void setMaxHeapMb(long maxHeapMb) {
        this.maxHeapMb = maxHeapMb;
    }

    public String getRecommendedGc() {
        return recommendedGc;
    }

    public void setRecommendedGc(String recommendedGc) {
        this.recommendedGc = recommendedGc;
    }
}
