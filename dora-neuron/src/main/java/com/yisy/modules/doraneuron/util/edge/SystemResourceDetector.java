package com.yisy.modules.doraneuron.util.edge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.Properties;

/**
 * Detects the hardware capabilities of the host system to determine
 * whether this Neuron is running on an edge device.
 *
 * Edge devices are characterized by:
 * - Limited CPU cores (≤ 4)
 * - Limited RAM (≤ 2 GB)
 * - ARM architecture (Raspberry Pi, Jetson Nano)
 * - No GPU or limited GPU (Jetson Nano has integrated GPU)
 *
 * The detection results drive automatic configuration adjustments
 * for inference threads, buffer sizes, and feature toggles.
 */
public class SystemResourceDetector {

    private static final Logger log = LoggerFactory.getLogger(SystemResourceDetector.class);

    /** Detected system profile. */
    public enum SystemProfile {
        /** Full-featured server with abundant resources. */
        SERVER,
        /** Mid-range device; some optimizations applied. */
        MID_RANGE,
        /** Resource-constrained edge device (Raspberry Pi, Jetson Nano, etc.). */
        EDGE
    }

    private final SystemProfile profile;
    private final int availableCores;
    private final long totalMemoryBytes;
    private final boolean isArmArchitecture;
    private final boolean hasGpu;
    private final String architecture;
    private final String osName;

    public SystemResourceDetector() {
        this.availableCores = detectCores();
        this.totalMemoryBytes = detectMemory();
        this.isArmArchitecture = detectArm();
        this.hasGpu = detectGpu();
        this.architecture = System.getProperty("os.arch", "unknown");
        this.osName = System.getProperty("os.name", "unknown");

        this.profile = determineProfile();
        log.info("System resource detection complete: profile={}, cores={}, memory={}MB, arm={}, gpu={}, arch={}",
                profile, availableCores, totalMemoryBytes / (1024 * 1024),
                isArmArchitecture, hasGpu, architecture);
    }

    /**
     * Determine the system profile based on detected resources.
     */
    private SystemProfile determineProfile() {
        if (isArmArchitecture && totalMemoryBytes <= 2L * 1024 * 1024 * 1024) {
            return SystemProfile.EDGE;
        }
        if (availableCores <= 4 && totalMemoryBytes <= 4L * 1024 * 1024 * 1024) {
            return SystemProfile.MID_RANGE;
        }
        return SystemProfile.SERVER;
    }

    /**
     * Detect the number of available CPU cores.
     */
    private int detectCores() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getAvailableProcessors();
    }

    /**
     * Detect total system memory in bytes.
     */
    private long detectMemory() {
        // Try OS-level detection first
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            long total = sunOsBean.getTotalPhysicalMemorySize();
            if (total > 0) {
                return total;
            }
        }

        // Fallback to JVM max memory
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        if (nonHeap != null && nonHeap.getMax() > 0) {
            return nonHeap.getMax();
        }
        return 0;
    }

    /**
     * Detect if running on ARM architecture.
     */
    private boolean detectArm() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return arch.contains("arm") || arch.contains("aarch");
    }

    /**
     * Detect GPU availability.
     * Checks for NVIDIA GPU via environment variables and system properties.
     */
    private boolean detectGpu() {
        // Check for NVIDIA GPU
        String nvidiaVisibleDevices = System.getenv("NVIDIA_VISIBLE_DEVICES");
        if (nvidiaVisibleDevices != null && !nvidiaVisibleDevices.isEmpty()) {
            return true;
        }

        // Check for CUDA
        String cudaPath = System.getenv("CUDA_PATH");
        if (cudaPath != null && !cudaPath.isEmpty()) {
            return true;
        }

        // Check for Jetson-specific indicators
        String jetsonModel = System.getenv("JETSON_MODEL");
        if (jetsonModel != null && !jetsonModel.isEmpty()) {
            return true;
        }

        // Check /proc/device-tree/model for Jetson
        File jetsonModelFile = new File("/proc/device-tree/model");
        if (jetsonModelFile.exists()) {
            return true;
        }

        return false;
    }

    /**
     * Get the recommended number of inference threads for this system.
     */
    public int getRecommendedInferenceThreads() {
        return switch (profile) {
            case EDGE -> Math.min(availableCores, 1);
            case MID_RANGE -> Math.min(availableCores, 2);
            case SERVER -> Math.min(availableCores, 4);
        };
    }

    /**
     * Get the recommended experience buffer size for this system.
     */
    public int getRecommendedExperienceBufferSize() {
        return switch (profile) {
            case EDGE -> 100;
            case MID_RANGE -> 250;
            case SERVER -> 500;
        };
    }

    /**
     * Get the recommended experience flush interval in milliseconds.
     */
    public long getRecommendedFlushIntervalMs() {
        return switch (profile) {
            case EDGE -> 60000;  // Less frequent flushes to save bandwidth
            case MID_RANGE -> 45000;
            case SERVER -> 30000;
        };
    }

    /**
     * Check if mesh networking should be enabled for this system.
     * Edge devices may disable mesh to conserve resources.
     */
    public boolean shouldEnableMesh() {
        return profile != SystemProfile.EDGE;
    }

    /**
     * Check if consensus voting should be enabled for this system.
     */
    public boolean shouldEnableConsensus() {
        return profile != SystemProfile.EDGE;
    }

    /**
     * Check if model quantization is recommended.
     * Edge devices benefit from INT8 quantization for faster inference.
     */
    public boolean shouldUseQuantizedModel() {
        return profile == SystemProfile.EDGE;
    }

    /**
     * Get the recommended maximum mesh peers for this system.
     */
    public int getRecommendedMaxMeshPeers() {
        return switch (profile) {
            case EDGE -> 2;
            case MID_RANGE -> 3;
            case SERVER -> 5;
        };
    }

    /**
     * Get the recommended heartbeat interval in milliseconds.
     */
    public long getRecommendedHeartbeatIntervalMs() {
        return switch (profile) {
            case EDGE -> 30000;  // Less frequent heartbeats
            case MID_RANGE -> 20000;
            case SERVER -> 15000;
        };
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public SystemProfile getProfile() {
        return profile;
    }

    public int getAvailableCores() {
        return availableCores;
    }

    public long getTotalMemoryBytes() {
        return totalMemoryBytes;
    }

    public long getTotalMemoryMb() {
        return totalMemoryBytes / (1024 * 1024);
    }

    public boolean isArmArchitecture() {
        return isArmArchitecture;
    }

    public boolean hasGpu() {
        return hasGpu;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getOsName() {
        return osName;
    }

    /**
     * Check if this is an edge device.
     */
    public boolean isEdgeDevice() {
        return profile == SystemProfile.EDGE;
    }

    /**
     * Check if this is a mid-range device.
     */
    public boolean isMidRange() {
        return profile == SystemProfile.MID_RANGE;
    }

    /**
     * Check if this is a full-featured server.
     */
    public boolean isServer() {
        return profile == SystemProfile.SERVER;
    }
}
