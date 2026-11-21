package com.yisy.modules.doramcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for DoraMCU.
 * <p>
 * Supports two modes controlled by {@code doramcu.launcher.mode}:
 * <ul>
 *   <li>{@code server} (default) — Headless server mode; Web UI at http://localhost:8080</li>
 *   <li>{@code desktop} — Auto-launches the Electron desktop app after server startup</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 *   ./gradlew bootRun                          # Server mode
 *   ./gradlew bootRun --args='--doramcu.launcher.mode=desktop'  # Desktop mode
 *   java -jar doraMCU.jar                      # Server mode (JAR)
 * </pre>
 * <p>
 * Configuration properties:
 * <ul>
 *   <li>{@code doramcu.launcher.mode} — "desktop" to auto-launch, "server" (default) for headless</li>
 *   <li>{@code doramcu.launcher.electron-dir} — path to dora-electron directory (default: dora-electron)</li>
 *   <li>{@code doramcu.launcher.server-url} — server URL to poll (default: http://localhost:8080)</li>
 *   <li>{@code doramcu.launcher.timeout-seconds} — max wait time for server (default: 120)</li>
 * </ul>
 */
@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "org.springframework.boot.autoconfigure.kafka.KafkaRepositoriesAutoConfiguration"
})
@EnableCaching
@EnableScheduling
@ComponentScan(
    basePackages = {"com.yisy.modules.doramcu", "com.yisy.modules.doradl", "com.yisy.modules.doraai"},
    excludeFilters = {
        // Exclude Neuron components from server scan
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.yisy\\.modules\\.doramcu\\.neuron\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.yisy\\.modules\\.doraneuron\\..*")
    }
)
public class DoraMcuLauncher {

    private static final Logger log = LoggerFactory.getLogger(DoraMcuLauncher.class);

    public static void main(String[] args) {
        log.info("Starting DoraMCU server — Web UI available at http://localhost:8080");
        SpringApplication app = new SpringApplication(DoraMcuLauncher.class);
        ConfigurableApplicationContext context = app.run(args);
        Environment env = context.getEnvironment();

        // Warn if running in dev profile without explicit opt-in
        String[] activeProfiles = env.getActiveProfiles();
        boolean isDev = java.util.Arrays.asList(activeProfiles).contains("dev");
        if (isDev) {
            String devOptIn = env.getProperty("doramcu.development");
            if (!"true".equalsIgnoreCase(devOptIn)) {
                log.warn("========================================");
                log.warn("Running in 'dev' profile without explicit opt-in!");
                log.warn("Set 'doramcu.development=true' to acknowledge dev mode.");
                log.warn("Do NOT use 'dev' profile in production/staging.");
                log.warn("========================================");
            }
        }
        log.info("DoraMCU server started successfully");

        // Check if desktop mode is enabled
        String launcherMode = env.getProperty("doramcu.launcher.mode", "server");
        if (!"desktop".equalsIgnoreCase(launcherMode)) {
            log.info("Launcher mode is '{}', skipping desktop app launch.", launcherMode);
            return;
        }

        // Check for headless environment
        boolean skipHeadlessCheck = "true".equalsIgnoreCase(System.getProperty("doramcu.launcher.skip-headless-check"));
        if (!skipHeadlessCheck && DesktopLauncher.isHeadless()) {
            log.warn("Running in headless mode — cannot launch desktop app.");
            log.info("Set system property 'doramcu.launcher.skip-headless-check=true' to override.");
            return;
        }

        // Delegate desktop launch to DesktopLauncher
        DesktopLauncher.launch(env);
    }
}
