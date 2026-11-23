package com.yisy.modules.doramcu;

import com.yisy.modules.doramcu.gamecore.module.ModuleLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Autowired
    private ModuleLifecycleManager lifecycleManager;

    @Autowired
    private Environment env;

    /**
     * Scan the modules directory and load any existing JARs after Spring context is ready.
     * Runs in a background thread to not block the startup.
     */
    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String autoLoad = env.getProperty("doramcu.modules.auto-load", "true");
        if (!"true".equalsIgnoreCase(autoLoad)) {
            log.info("Module auto-load disabled by configuration");
            return;
        }

        String modulesDir = env.getProperty("doramcu.modules.dir", "/opt/doramcu/modules/");
        Path path = Paths.get(modulesDir);
        if (!Files.exists(path)) {
            log.info("Modules directory does not exist: {}. Skipping auto-load.", modulesDir);
            return;
        }

        // Run scan in background thread
        new Thread(() -> {
            try {
                int count = 0;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.jar")) {
                    for (Path jar : stream) {
                        log.info("Auto-loading module: {}", jar.getFileName());
                        lifecycleManager.loadModule(jar);
                        count++;
                    }
                }
                log.info("Auto-loaded {} modules from {}", count, modulesDir);
            } catch (IOException e) {
                log.error("Failed to scan modules directory {}: {}", modulesDir, e.getMessage(), e);
            }
        }, "module-auto-load").start();
    }

    /**
     * Graceful shutdown: unload all dynamic modules.
     */
    @EventListener(ContextClosedEvent.class)
    public void onContextClose() {
        log.info("Context closing, unloading all dynamic modules");
        lifecycleManager.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
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
            log.info("Launcher mode is '{}', keeping server running (main thread blocked).", launcherMode);
            // Block the main thread to keep the Spring context alive.
            // Without this, the main thread exits after SpringApplication.run() returns,
            // causing the entire application context (including Tomcat) to shut down.
            context.registerShutdownHook();
            Thread.currentThread().join();
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
