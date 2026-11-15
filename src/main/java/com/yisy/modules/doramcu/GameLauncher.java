package com.yisy.modules.doramcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * GameLauncher — starts the Spring Boot server, waits for it to be ready,
 * then automatically launches the Electron desktop app.
 * <p>
 * @deprecated Use {@link DoraMcuLauncher} instead, which now supports both server and desktop modes.
 * This class is kept for backward compatibility only and will be removed in a future release.
 * Use {@code doramcu.launcher.mode=desktop} with {@link DoraMcuLauncher} to auto-launch the Electron app.
 */
@Deprecated
@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "org.springframework.boot.autoconfigure.kafka.KafkaRepositoriesAutoConfiguration"
})
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {"com.yisy.modules.doramcu", "com.yisy.modules.doradl", "com.yisy.modules.doraai"})
public class GameLauncher {

    private static final Logger log = LoggerFactory.getLogger(GameLauncher.class);

    // Default configuration
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
    private static final long DEFAULT_TIMEOUT_SECONDS = 120;
    private static final long POLL_INTERVAL_MS = 2000;

    public static void main(String[] args) {
        log.info("========================================");
        log.info("DoraMCU GameLauncher starting");
        log.info("========================================");

        // Start Spring Boot context
        SpringApplication app = new SpringApplication(GameLauncher.class);
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
            log.info("Set 'doramcu.launcher.mode=desktop' to auto-launch the desktop app.");
            return;
        }

        // Check for headless environment
        // When desktop mode is explicitly set, we skip the headless check because
        // Gradle bootRun may report headless=true even on a normal desktop.
        boolean skipHeadlessCheck = "true".equalsIgnoreCase(System.getProperty("doramcu.launcher.skip-headless-check"));
        if (!skipHeadlessCheck && isHeadless()) {
            log.warn("Running in headless mode — cannot launch desktop app.");
            log.info("Set system property 'doramcu.launcher.skip-headless-check=true' to override.");
            return;
        }

        // Launch desktop app
        launchDesktopApp(env);
    }

    /**
     * Detects if the JVM is running in headless mode.
     */
    private static boolean isHeadless() {
        boolean headless = "true".equals(System.getProperty("java.awt.headless"));
        if (!headless) {
            try {
                headless = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
            } catch (java.awt.HeadlessException e) {
                headless = true;
            }
        }
        return headless;
    }

    /**
     * Waits for the server to be ready by polling the given URL.
     *
     * @param serverUrl the server URL to poll
     * @param timeoutSeconds maximum time to wait in seconds
     * @return true if server is ready, false if timed out
     */
    @SuppressWarnings("BusyWait")
    private static boolean waitForServer(String serverUrl, long timeoutSeconds) {
        log.info("Waiting for server at {} (timeout: {}s)...", serverUrl, timeoutSeconds);
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);

        while (System.currentTimeMillis() < deadline) {
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                if (responseCode >= 200 && responseCode < 500) {
                    log.info("Server is ready (HTTP {})", responseCode);
                    return true;
                }
                log.debug("Server returned {}, waiting...", responseCode);
            } catch (java.net.ConnectException e) {
                log.debug("Server not yet available, retrying in {}ms...", POLL_INTERVAL_MS);
            } catch (Exception e) {
                log.debug("Error polling server: {}, retrying...", e.getMessage());
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Server wait interrupted");
                return false;
            }
        }

        log.error("Server did not become ready within {}s", timeoutSeconds);
        return false;
    }

    /**
     * Launches the Electron desktop app.
     */
    private static void launchDesktopApp(Environment env) {
        String serverUrl = env.getProperty("doramcu.launcher.server-url", DEFAULT_SERVER_URL);
        long timeoutSeconds = Long.parseLong(env.getProperty("doramcu.launcher.timeout-seconds", String.valueOf(DEFAULT_TIMEOUT_SECONDS)));
        String electronDir = env.getProperty("doramcu.launcher.electron-dir", "dora-electron");

        // Wait for server readiness
        if (!waitForServer(serverUrl, timeoutSeconds)) {
            log.error("Aborting desktop app launch — server is not ready.");
            return;
        }

        log.info("Launching Electron desktop app from: {}", electronDir);

        // Resolve the electron directory path
        File electronPath = new File(electronDir);
        if (!electronPath.exists() || !electronPath.isDirectory()) {
            log.error("Electron directory not found: {}", electronPath.getAbsolutePath());
            return;
        }

        // Build the electron launch command based on OS
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            // Windows: use npm start or electron.cmd
            // Try npm first, fall back to direct electron
            File npmCmd = findExecutable("npm.cmd");
            if (npmCmd != null) {
                processBuilder = new ProcessBuilder(
                    npmCmd.getAbsolutePath(), "start", "--prefix", electronPath.getAbsolutePath()
                );
                log.info("Using npm to launch Electron");
            } else {
                // Try direct electron via node_modules
                File electronCmd = new File(electronPath, "node_modules/.bin/electron.cmd");
                if (electronCmd.exists()) {
                    processBuilder = new ProcessBuilder(
                        electronCmd.getAbsolutePath(), "."
                    );
                    processBuilder.directory(electronPath);
                    log.info("Using direct electron.cmd to launch");
                } else {
                    log.error("Neither npm nor electron.cmd found. Install dependencies with 'npm install' in dora-electron/");
                    return;
                }
            }
        } else if (os.contains("mac") || os.contains("darwin")) {
            // macOS
            processBuilder = new ProcessBuilder(
                "npx", "electron", "."
            );
            processBuilder.directory(electronPath);
        } else {
            // Linux
            processBuilder = new ProcessBuilder(
                "npx", "electron", "."
            );
            processBuilder.directory(electronPath);
        }

        // Inherit IO so the Electron console output is visible
        processBuilder.inheritIO();

        // Set environment variable for server URL so Electron can find the server
        processBuilder.environment().put("DORA_SERVER_URL", serverUrl);

        try {
            Process process = processBuilder.start();
            log.info("Electron desktop app launched (PID: {})", getProcessPid(process));
            log.info("Desktop app will connect to server at: {}", serverUrl);

            // Add shutdown hook to terminate Electron when server stops
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down Electron desktop app...");
                process.destroy();
                try {
                    if (process.waitFor(5, TimeUnit.SECONDS)) {
                        log.info("Electron desktop app stopped gracefully");
                    } else {
                        log.warn("Electron did not stop gracefully, forcing...");
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrupted while waiting for Electron to stop");
                    process.destroyForcibly();
                }
            }));

        } catch (Exception e) {
            log.error("Failed to launch Electron desktop app: {}", e.getMessage(), e);
            log.info("You can start it manually: cd {} && npm start", electronDir);
        }
    }

    /**
     * Finds an executable in the system PATH.
     */
    private static File findExecutable(String name) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }
        String[] paths = pathEnv.split(File.pathSeparator);
        for (String path : paths) {
            File file = new File(path, name);
            if (file.exists() && file.canExecute()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Attempts to get the process PID (for logging purposes).
     */
    private static String getProcessPid(Process process) {
        try {
            if (process instanceof ProcessHandle.Info) {
                return String.valueOf(process.pid());
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }
}
