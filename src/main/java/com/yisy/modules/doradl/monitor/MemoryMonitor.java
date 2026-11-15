package com.yisy.modules.doradl.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Monitors JVM heap and native (JavaCPP) memory usage to prevent OOM crashes
 * during deep learning training.
 * <p>
 * This is the local DL4J implementation. When dl.rest-client.enabled=true,
 * memory monitoring is handled by the Python microservice.
 * </p>
 * <p>
 * Provides threshold-based warnings and a simple API for callers to check
 * whether it is safe to proceed with memory-intensive operations.
 * </p>
 * <p>
 * Configuration properties (all optional with sensible defaults):
 * <ul>
 *   <li>{@code dl.memory-guard.enabled} — Enable memory guard (default: true)</li>
 *   <li>{@code dl.memory-guard.heap-warning-threshold} — Heap warning threshold 0-1 (default: 0.70)</li>
 *   <li>{@code dl.memory-guard.heap-critical-threshold} — Heap critical threshold 0-1 (default: 0.80)</li>
 *   <li>{@code dl.memory-guard.javacpp-warning-threshold} — JavaCPP warning threshold 0-1 (default: 0.60)</li>
 *   <li>{@code dl.memory-guard.javacpp-critical-threshold} — JavaCPP critical threshold 0-1 (default: 0.75)</li>
 *   <li>{@code dl.memory-guard.min-free-memory-mb} — Min free memory MB before training (default: 1024)</li>
 *   <li>{@code dl.memory-guard.max-physical-memory-mb} — Max physical memory MB limit (default: 4096)</li>
 * </ul>
 * </p>
 */
@Component
@ConditionalOnProperty(name = "dl.rest-client.enabled", havingValue = "false", matchIfMissing = true)
public class MemoryMonitor {

    private static final Logger log = LoggerFactory.getLogger(MemoryMonitor.class);

    private final MemoryMXBean memoryMXBean;

    /** Configurable heap usage warning threshold: 70 % of max heap. */
    private final double heapWarningThreshold;

    /** Configurable heap usage critical threshold: 80 % of max heap. */
    private final double heapCriticalThreshold;

    /** Configurable JavaCPP physical memory warning threshold: 60 % of max physical bytes. */
    private final double javacppWarningThreshold;

    /** Configurable JavaCPP physical memory critical threshold: 75 % of max physical bytes. */
    private final double javacppCriticalThreshold;

    /** Minimum free memory in MB required before training. */
    private final long minFreeMemoryMb;

    /** Maximum physical memory in MB. */
    private final long maxPhysicalMemoryMb;

    /** JavaCPP max physical bytes property (bytes). */
    private static final String JAVACPP_MAX_PHYSICAL = "org.bytedeco.javacpp.maxphysicalbytes";

    /** Parsed max physical bytes from JavaCPP property; 0 means not configured. */
    private final long javacppMaxPhysicalBytes;

    /** Whether memory guard is enabled. */
    private final boolean enabled;

    public MemoryMonitor(
            @Value("${dl.memory-guard.enabled:true}") boolean enabled,
            @Value("${dl.memory-guard.heap-warning-threshold:0.70}") double heapWarningThreshold,
            @Value("${dl.memory-guard.heap-critical-threshold:0.80}") double heapCriticalThreshold,
            @Value("${dl.memory-guard.javacpp-warning-threshold:0.60}") double javacppWarningThreshold,
            @Value("${dl.memory-guard.javacpp-critical-threshold:0.75}") double javacppCriticalThreshold,
            @Value("${dl.memory-guard.min-free-memory-mb:1024}") long minFreeMemoryMb,
            @Value("${dl.memory-guard.max-physical-memory-mb:4096}") long maxPhysicalMemoryMb) {
        this.enabled = enabled;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.heapWarningThreshold = heapWarningThreshold;
        this.heapCriticalThreshold = heapCriticalThreshold;
        this.javacppWarningThreshold = javacppWarningThreshold;
        this.javacppCriticalThreshold = javacppCriticalThreshold;
        this.minFreeMemoryMb = minFreeMemoryMb;
        this.maxPhysicalMemoryMb = maxPhysicalMemoryMb;
        this.javacppMaxPhysicalBytes = parseJavaCppMaxPhysical();
        
        log.info("MemoryMonitor initialized — enabled: {}, heap thresholds: warn={}%, crit={}%, " +
                "JavaCPP thresholds: warn={}%, crit={}%, minFreeMemory: {}MB, maxPhysicalMemory: {}MB",
                enabled, heapWarningThreshold * 100, heapCriticalThreshold * 100,
                javacppWarningThreshold * 100, javacppCriticalThreshold * 100,
                minFreeMemoryMb, maxPhysicalMemoryMb);
    }

    /**
     * Returns the current heap usage as a fraction of max heap (0..1).
     */
    public double getHeapUsageFraction() {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long max = heap.getMax();
        if (max <= 0) return 1.0;
        return (double) heap.getUsed() / max;
    }

    /**
     * Returns the current heap used bytes.
     */
    public long getHeapUsedBytes() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    /**
     * Returns the max heap bytes.
     */
    public long getHeapMaxBytes() {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    /**
     * Returns the JavaCPP max physical bytes configured via system property.
     */
    public long getJavacppMaxPhysicalBytes() {
        return javacppMaxPhysicalBytes;
    }

    /**
     * Estimates current JavaCPP physical memory usage by reading the
     * Pointer class statistics.  Falls back to 0 if the API is unavailable.
     */
    public long estimateJavacppPhysicalBytes() {
        try {
            Class<?> pointerClass = Class.forName("org.bytedeco.javacpp.Pointer");
            // qodana: disable=JavaReflectionMemberAccess for next line
            java.lang.reflect.Method method = pointerClass.getMethod("getPhysicalBytes");
            return (long) method.invoke(null);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns the JavaCPP physical usage as a fraction of the configured max.
     * Returns 0 if the max is not configured or the API is unavailable.
     */
    public double getJavacppUsageFraction() {
        if (javacppMaxPhysicalBytes <= 0) return 0;
        long used = estimateJavacppPhysicalBytes();
        return (double) used / javacppMaxPhysicalBytes;
    }

    /**
     * Memory level enum — used by {@link #getMemoryLevel()}.
     */
    public enum MemoryLevel {
        /** Memory is healthy — safe to train. */
        OK,
        /** Memory is elevated — reduce batch size or delay training. */
        WARNING,
        /** Memory is critical — skip training and force GC. */
        CRITICAL
    }

    /**
     * Returns the current memory pressure level based on both JVM heap
     * and JavaCPP physical memory usage.
     * <p>
     * JavaCPP memory uses separate, more conservative thresholds because
     * it is not managed by the JVM GC — leaks accumulate faster and are
     * harder to recover from.
     * </p>
     */
    public MemoryLevel getMemoryLevel() {
        if (!enabled) {
            return MemoryLevel.OK;
        }

        double heapFraction = getHeapUsageFraction();
        double javacppFraction = getJavacppUsageFraction();

        // Evaluate heap and JavaCPP independently with their own thresholds
        MemoryLevel heapLevel;
        if (heapFraction >= heapCriticalThreshold) {
            heapLevel = MemoryLevel.CRITICAL;
        } else if (heapFraction >= heapWarningThreshold) {
            heapLevel = MemoryLevel.WARNING;
        } else {
            heapLevel = MemoryLevel.OK;
        }

        MemoryLevel javacppLevel;
        if (javacppFraction >= javacppCriticalThreshold) {
            javacppLevel = MemoryLevel.CRITICAL;
        } else if (javacppFraction >= javacppWarningThreshold) {
            javacppLevel = MemoryLevel.WARNING;
        } else {
            javacppLevel = MemoryLevel.OK;
        }

        // Additional check: minimum free memory
        long freeMemoryMb = getFreeMemoryMb();
        if (freeMemoryMb < minFreeMemoryMb) {
            heapLevel = MemoryLevel.CRITICAL;
        }

        // Return the worst of the two levels
        if (heapLevel == MemoryLevel.CRITICAL || javacppLevel == MemoryLevel.CRITICAL) {
            return MemoryLevel.CRITICAL;
        }
        if (heapLevel == MemoryLevel.WARNING || javacppLevel == MemoryLevel.WARNING) {
            return MemoryLevel.WARNING;
        }
        return MemoryLevel.OK;
    }

    /**
     * Returns the current free memory in MB.
     */
    public long getFreeMemoryMb() {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        return (heap.getMax() - heap.getUsed()) / (1024 * 1024);
    }

    /**
     * Returns whether the memory guard is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the configured minimum free memory in MB.
     */
    public long getMinFreeMemoryMb() {
        return minFreeMemoryMb;
    }

    /**
     * Returns the configured maximum physical memory in MB.
     */
    public long getMaxPhysicalMemoryMb() {
        return maxPhysicalMemoryMb;
    }

    /**
     * Checks whether it is safe to proceed with a training operation.
     * Returns {@code true} when memory level is OK or WARNING.
     * Returns {@code false} when memory level is CRITICAL.
     */
    public boolean isTrainingSafe() {
        return getMemoryLevel() != MemoryLevel.CRITICAL;
    }

    /**
     * Suggests a reduced batch size based on current memory pressure.
     * When memory is WARNING, halves the requested batch size.
     * When memory is CRITICAL, returns 1 (minimum viable batch).
     */
    public int suggestBatchSize(int requestedBatchSize) {
        MemoryLevel level = getMemoryLevel();
        if (level == MemoryLevel.CRITICAL) {
            return Math.max(1, requestedBatchSize / 4);
        }
        if (level == MemoryLevel.WARNING) {
            return Math.max(1, requestedBatchSize / 2);
        }
        return requestedBatchSize;
    }

    /**
     * Forces a garbage collection and logs the current memory state.
     * Call this before training when memory is elevated.
     */
    public void forceGcAndLog() {
        logMemoryState("Before GC");
        log.info("Forcing garbage collection due to elevated memory usage");
        System.gc();
        // Give GC a moment to run
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logMemoryState("After GC");
    }

    /**
     * Logs the current memory state at DEBUG level.
     */
    public void logMemoryState() {
        logMemoryState("Manual check");
    }

    private void logMemoryState(String prefix) {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        double heapPct = heap.getMax() > 0 ? (heap.getUsed() * 100.0 / heap.getMax()) : 0;
        long javacppUsed = estimateJavacppPhysicalBytes();
        double javacppPct = javacppMaxPhysicalBytes > 0 ? (javacppUsed * 100.0 / javacppMaxPhysicalBytes) : 0;

        log.debug("{} — Heap: {}/{} ({}%), JavaCPP physical: {}/{} ({}%), Level: {}",
                prefix,
                toMiB(heap.getUsed()), toMiB(heap.getMax()), heapPct,
                toMiB(javacppUsed), toMiB(javacppMaxPhysicalBytes), javacppPct,
                getMemoryLevel());
    }

    /**
     * Triggers a memory warning log when usage crosses the warning threshold.
     */
    public void checkAndWarn() {
        MemoryLevel level = getMemoryLevel();
        if (level == MemoryLevel.CRITICAL) {
            log.warn("CRITICAL memory usage detected — heap {}%, JavaCPP {}% — training should be skipped",
                    getHeapUsageFraction() * 100, getJavacppUsageFraction() * 100);
        } else if (level == MemoryLevel.WARNING) {
            log.warn("ELEVATED memory usage — heap {}%, JavaCPP {}% — consider reducing batch size",
                    getHeapUsageFraction() * 100, getJavacppUsageFraction() * 100);
        }
    }

    private long parseJavaCppMaxPhysical() {
        String val = System.getProperty(JAVACPP_MAX_PHYSICAL, "0");
        try {
            return parseBytes(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseBytes(String val) {
        val = val.trim();
        if (val.isEmpty()) return 0;

        char last = val.charAt(val.length() - 1);
        long multiplier;
        String numPart;

        switch (Character.toUpperCase(last)) {
            case 'G':
                multiplier = 1024L * 1024 * 1024;
                numPart = val.substring(0, val.length() - 1);
                break;
            case 'M':
                multiplier = 1024L * 1024;
                numPart = val.substring(0, val.length() - 1);
                break;
            case 'K':
                multiplier = 1024L;
                numPart = val.substring(0, val.length() - 1);
                break;
            default:
                multiplier = 1;
                numPart = val;
                break;
        }
        return Long.parseLong(numPart.trim()) * multiplier;
    }

    private static String toMiB(long bytes) {
        return String.format("%.2f MiB", bytes / (1024.0 * 1024));
    }
}
