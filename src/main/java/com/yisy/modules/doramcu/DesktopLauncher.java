package com.yisy.modules.doramcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Handles launching the Electron desktop application after the DoraMCU server starts.
 * <p>
 * This class is separated from {@link DoraMcuLauncher} to keep the production server
 * code free of desktop-specific dependencies. In production (server mode), this class
 * is never invoked.
 * <p>
 * Configuration properties:
 * <ul>
 *   <li>{@code doramcu.launcher.server-url} — server URL to poll (default: http://localhost:8080)</li>
 *   <li>{@code doramcu.launcher.timeout-seconds} — max wait time for server (default: 120)</li>
 *   <li>{@code doramcu.launcher.electron-dir} — path to dora-electron directory (default: dora-electron)</li>
 * </ul>
 */
public class DesktopLauncher {

    private static final Logger log = LoggerFactory.getLogger(DesktopLauncher.class);

    private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
    private static final long DEFAULT_TIMEOUT_SECONDS = 120;
    private static final long POLL_INTERVAL_MS = 2000;

    /**
     * Detects if the JVM is running in headless mode.
     */
    public static boolean isHeadless() {
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
     * Launches the Electron desktop app after waiting for the server to be ready.
     */
    public static void launch(Environment env) {
        String serverUrl = env.getProperty("doramcu.launcher.server-url", DEFAULT_SERVER_URL);
        long timeoutSeconds = Long.parseLong(env.getProperty("doramcu.launcher.timeout-seconds", String.valueOf(DEFAULT_TIMEOUT_SECONDS)));
        String electronDir = env.getProperty("doramcu.launcher.electron-dir", "dora-electron");

        // Wait for server readiness
        if (!waitForServer(serverUrl, timeoutSeconds)) {
            log.error("Aborting desktop app launch — server is not ready.");
            return;
        }

        launchElectron(serverUrl, electronDir);
    }

    /**
     * Waits for the server to be ready by polling the given URL.
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
     * Launches the Electron desktop app from the specified directory.
     */
    private static void launchElectron(String serverUrl, String electronDir) {
        log.info("Launching Electron desktop app from: {}", electronDir);

        File electronPath = new File(electronDir);
        if (!electronPath.exists() || !electronPath.isDirectory()) {
            log.error("Electron directory not found: {}", electronPath.getAbsolutePath());
            return;
        }

        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            File npmCmd = findExecutable("npm.cmd");
            if (npmCmd != null) {
                processBuilder = new ProcessBuilder(
                    npmCmd.getAbsolutePath(), "start", "--prefix", electronPath.getAbsolutePath()
                );
                log.info("Using npm to launch Electron");
            } else {
                File electronCmd = new File(electronPath, "node_modules/.bin/electron.cmd");
                if (electronCmd.exists()) {
                    processBuilder = new ProcessBuilder(electronCmd.getAbsolutePath(), ".");
                    processBuilder.directory(electronPath);
                    log.info("Using direct electron.cmd to launch");
                } else {
                    log.error("Neither npm nor electron.cmd found. Install dependencies with 'npm install' in dora-electron/");
                    return;
                }
            }
        } else if (os.contains("mac") || os.contains("darwin")) {
            processBuilder = new ProcessBuilder("npx", "electron", ".");
            processBuilder.directory(electronPath);
        } else {
            processBuilder = new ProcessBuilder("npx", "electron", ".");
            processBuilder.directory(electronPath);
        }

        processBuilder.inheritIO();
        processBuilder.environment().put("DORA_SERVER_URL", serverUrl);

        try {
            Process process = processBuilder.start();
            log.info("Electron desktop app launched (PID: {})", process.pid());
            log.info("Desktop app will connect to server at: {}", serverUrl);

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
}
