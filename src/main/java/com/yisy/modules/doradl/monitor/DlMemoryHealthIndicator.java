package com.yisy.modules.doradl.monitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Exposes DL memory status through the Spring Boot actuator health endpoint.
 * <p>
 * This allows monitoring tools (Prometheus, Grafana) to track memory pressure
 * and alert when DL training may cause OOM crashes.
 * </p>
 * <p>
 * Example health output:
 * <pre>
 * {
 *   "dl_memory": {
 *     "status": "UP",
 *     "details": {
 *       "enabled": true,
 *       "memoryLevel": "OK",
 *       "heapUsagePercent": 45.2,
 *       "javacppUsagePercent": 12.5,
 *       "freeMemoryMb": 2048,
 *       "minFreeMemoryMb": 1024
 *     }
 *   }
 * }
 * </pre>
 * </p>
 */
@Component("dlMemoryHealth")
@ConditionalOnBean(MemoryMonitor.class)
public class DlMemoryHealthIndicator implements HealthIndicator {

    private final MemoryMonitor memoryMonitor;

    public DlMemoryHealthIndicator(MemoryMonitor memoryMonitor) {
        this.memoryMonitor = memoryMonitor;
    }

    @Override
    public Health health() {
        MemoryMonitor.MemoryLevel level = memoryMonitor.getMemoryLevel();
        double heapUsage = memoryMonitor.getHeapUsageFraction() * 100;
        double javacppUsage = memoryMonitor.getJavacppUsageFraction() * 100;
        long freeMemoryMb = memoryMonitor.getFreeMemoryMb();

        // Always report UP — memory pressure is a warning for monitoring, not a service outage.
        // The details section still exposes the memory level so monitoring tools can alert.
        Health.Builder builder = Health.up();
        builder.withDetail("enabled", memoryMonitor.isEnabled());
        builder.withDetail("memoryLevel", level.name());
        builder.withDetail("heapUsagePercent", Math.round(heapUsage * 10.0) / 10.0);
        builder.withDetail("javacppUsagePercent", Math.round(javacppUsage * 10.0) / 10.0);
        builder.withDetail("freeMemoryMb", freeMemoryMb);
        builder.withDetail("minFreeMemoryMb", memoryMonitor.getMinFreeMemoryMb());

        return builder.build();
    }
}
