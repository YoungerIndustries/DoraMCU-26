package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Periodic health monitor for all dynamically loaded modules.
 * <p>
 * Runs a scheduled task (default every 30 seconds) that checks each module's
 * Spring context health. If a module's context has crashed, it attempts automatic
 * recovery by reloading the module.
 * </p>
 */
@Component
public class ModuleHealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(ModuleHealthMonitor.class);

    private final ModuleLifecycleManager lifecycleManager;

    @Value("${doramcu.modules.health-check-interval:30000}")
    private long healthCheckInterval;

    public ModuleHealthMonitor(ModuleLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @PostConstruct
    public void init() {
        log.info("ModuleHealthMonitor initialized. Health check interval: {}ms", healthCheckInterval);
    }

    /**
     * Periodic health check for all dynamically loaded modules.
     * <p>
     * Verifies each module's Spring context is active. If crashed, attempts
     * automatic recovery by reloading the module from its JAR file.
     * </p>
     */
    @Scheduled(fixedRateString = "${doramcu.modules.health-check-interval:30000}")
    public void checkHealth() {
        Map<GameType, ModuleInfo> allModules = lifecycleManager.getAllModuleInfo();
        if (allModules.isEmpty()) {
            return;
        }

        int healthy = 0;
        int crashed = 0;

        for (Map.Entry<GameType, ModuleInfo> entry : allModules.entrySet()) {
            GameType gameType = entry.getKey();
            ModuleInfo info = entry.getValue();

            try {
                ModuleInfo checked = lifecycleManager.healthCheck(gameType);
                if (checked == null) continue;

                if (checked.getHealth() == ModuleHealthStatus.HEALTHY) {
                    healthy++;
                } else if (checked.getHealth() == ModuleHealthStatus.CRASHED) {
                    crashed++;
                    log.error("Module {} has crashed. Attempting recovery...", gameType);
                    attemptRecovery(gameType, checked);
                } else if (checked.getHealth() == ModuleHealthStatus.DISABLED) {
                    // Disabled modules are expected to be inactive
                    healthy++;
                }
            } catch (Exception e) {
                crashed++;
                log.error("Error checking health of module {}: {}", gameType, e.getMessage());
            }
        }

        if (crashed > 0) {
            log.warn("Health check: {} healthy, {} crashed out of {} modules",
                    healthy, crashed, allModules.size());
        }
    }

    /**
     * Attempt to recover a crashed module by reloading it.
     */
    private void attemptRecovery(GameType gameType, ModuleInfo info) {
        try {
            if (info.getJarPath() != null) {
                log.info("Reloading crashed module {} from {}", gameType, info.getJarPath());
                ModuleInfo reloaded = lifecycleManager.reloadModule(gameType, info.getJarPath());
                if (reloaded != null && reloaded.getHealth() == ModuleHealthStatus.HEALTHY) {
                    log.info("Module {} recovered successfully", gameType);
                } else {
                    log.error("Module {} recovery failed", gameType);
                }
            } else {
                log.error("Cannot recover module {}: no JAR path", gameType);
            }
        } catch (Exception e) {
            log.error("Module {} recovery threw exception: {}", gameType, e.getMessage(), e);
        }
    }
}
