package com.yisy.modules.doraneuron.service.edge;

import com.yisy.modules.doraneuron.config.edge.NeuronEdgeConfig;
import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

/**
 * Edge-specific metrics service that monitors resource-constrained device health.
 *
 * Extends the standard NeuronMetricsService with edge-specific metrics:
 * - CPU temperature (Linux sysfs)
 * - Swap space usage
 * - Thermal throttling status
 * - Memory pressure indicators
 * - Disk I/O wait time
 * - Model quantization status
 *
 * These metrics help operators understand why an edge neuron might be
 * underperforming and take corrective action.
 */
@Service
public class EdgeMetricsService {

    private static final Logger log = LoggerFactory.getLogger(EdgeMetricsService.class);

    private final NeuronEdgeConfig edgeConfig;
    private final SystemResourceDetector detector;
    private final ModelQuantizationService quantizationService;

    public EdgeMetricsService(NeuronEdgeConfig edgeConfig,
                               SystemResourceDetector detector,
                               ModelQuantizationService quantizationService) {
        this.edgeConfig = edgeConfig;
        this.detector = detector;
        this.quantizationService = quantizationService;
    }

    /** Last measured CPU temperature in Celsius. */
    private volatile double cpuTemperature = 0.0;

    /** Last measured swap usage in bytes. */
    private volatile long swapUsed = 0;

    /** Last measured swap total in bytes. */
    private volatile long swapTotal = 0;

    /** Whether the system is currently thermally throttled. */
    private volatile boolean isThermallyThrottled = false;

    /** Last measured memory pressure (0.0 to 1.0). */
    private volatile double memoryPressure = 0.0;

    @PostConstruct
    public void init() {
        log.info("Edge metrics service initialized: profile={}, thermalDetection={}, swapMonitoring={}",
                detector.getProfile(),
                edgeConfig.isThermalDetectionEnabled(),
                edgeConfig.isSwapMonitoringEnabled());
    }

    /**
     * Get comprehensive edge-specific metrics.
     */
    public Map<String, Object> getEdgeMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // System profile
        metrics.put("systemProfile", detector.getProfile().name());
        metrics.put("isEdgeDevice", detector.isEdgeDevice());
        metrics.put("isArmArchitecture", detector.isArmArchitecture());
        metrics.put("hasGpu", detector.hasGpu());
        metrics.put("architecture", detector.getArchitecture());
        metrics.put("availableCores", detector.getAvailableCores());
        metrics.put("totalMemoryMb", detector.getTotalMemoryMb());

        // CPU temperature (Linux-only)
        if (edgeConfig.isThermalDetectionEnabled()) {
            cpuTemperature = readCpuTemperature();
            metrics.put("cpuTemperature", cpuTemperature);
            metrics.put("thermalThrottleThreshold", edgeConfig.getThermalThrottleThreshold());
            metrics.put("isThermallyThrottled", isThermallyThrottled);
        }

        // Swap usage (Linux-only)
        if (edgeConfig.isSwapMonitoringEnabled()) {
            readSwapUsage();
            metrics.put("swapUsedMb", swapUsed / (1024 * 1024));
            metrics.put("swapTotalMb", swapTotal / (1024 * 1024));
            double swapUsagePercent = swapTotal > 0 ? (double) swapUsed / swapTotal * 100 : 0;
            metrics.put("swapUsagePercent", swapUsagePercent);
        }

        // Memory pressure
        memoryPressure = calculateMemoryPressure();
        metrics.put("memoryPressure", memoryPressure);

        // JVM memory
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        metrics.put("jvmHeapUsedMb", heapUsage.getUsed() / (1024 * 1024));
        metrics.put("jvmHeapMaxMb", heapUsage.getMax() / (1024 * 1024));
        metrics.put("jvmHeapUsagePercent", (double) heapUsage.getUsed() / heapUsage.getMax() * 100);

        // Quantization status
        metrics.put("quantization", quantizationService.getStats());

        // Edge optimization status
        metrics.put("autoOptimize", edgeConfig.isAutoOptimize());
        metrics.put("edgeOptimizationActive", edgeConfig.isEdgeOptimizationActive(detector));

        return metrics;
    }

    /**
     * Read CPU temperature from Linux sysfs.
     */
    private double readCpuTemperature() {
        // Try multiple thermal zone paths
        String[] thermalPaths = {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
        };

        for (String path : thermalPaths) {
            File tempFile = new File(path);
            if (tempFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                    String line = reader.readLine();
                    if (line != null) {
                        // Temperature is in millidegrees Celsius
                        double temp = Double.parseDouble(line.trim()) / 1000.0;

                        // Update thermal throttling status
                        if (temp > edgeConfig.getThermalThrottleThreshold()) {
                            if (!isThermallyThrottled) {
                                log.warn("CPU temperature {}°C exceeds threshold {}°C, thermal throttling activated",
                                        temp, edgeConfig.getThermalThrottleThreshold());
                            }
                            isThermallyThrottled = true;
                        } else if (temp < edgeConfig.getThermalThrottleThreshold() - 5.0) {
                            // Hysteresis to prevent flapping
                            if (isThermallyThrottled) {
                                log.info("CPU temperature {}°C below threshold, thermal throttling deactivated", temp);
                            }
                            isThermallyThrottled = false;
                        }
                        return temp;
                    }
                } catch (IOException | NumberFormatException e) {
                    log.debug("Failed to read CPU temperature from {}: {}", path, e.getMessage());
                }
            }
        }
        return 0.0;
    }

    /**
     * Read swap usage from /proc/meminfo (Linux).
     */
    private void readSwapUsage() {
        File meminfo = new File("/proc/meminfo");
        if (!meminfo.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(meminfo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SwapTotal:")) {
                    swapTotal = parseMeminfoValue(line);
                } else if (line.startsWith("SwapFree:")) {
                    long swapFree = parseMeminfoValue(line);
                    swapUsed = swapTotal - swapFree;
                    return;
                }
            }
        } catch (IOException e) {
            log.debug("Failed to read swap usage: {}", e.getMessage());
        }
    }

    /**
     * Parse a value from /proc/meminfo line (value is in kB).
     */
    private long parseMeminfoValue(String line) {
        try {
            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                // Value is in kB, convert to bytes
                return Long.parseLong(parts[1]) * 1024;
            }
        } catch (NumberFormatException e) {
            log.debug("Failed to parse meminfo line: {}", line);
        }
        return 0;
    }

    /**
     * Calculate memory pressure (0.0 to 1.0).
     * Higher values indicate more memory pressure.
     */
    private double calculateMemoryPressure() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

        double heapUsageRatio = (double) heapUsage.getUsed() / heapUsage.getMax();

        // Factor in swap usage if available
        double swapPressure = 0.0;
        if (swapTotal > 0) {
            swapPressure = (double) swapUsed / swapTotal;
        }

        // Weighted average: 70% heap, 30% swap
        return 0.7 * heapUsageRatio + 0.3 * swapPressure;
    }

    /**
     * Get the current CPU temperature.
     */
    public double getCpuTemperature() {
        return cpuTemperature;
    }

    /**
     * Check if the system is thermally throttled.
     */
    public boolean isThermallyThrottled() {
        return isThermallyThrottled;
    }

    /**
     * Get the current memory pressure (0.0 to 1.0).
     */
    public double getMemoryPressure() {
        return memoryPressure;
    }

    /**
     * Get swap usage in MB.
     */
    public long getSwapUsedMb() {
        return swapUsed / (1024 * 1024);
    }
}
