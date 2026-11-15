package com.yisy.modules.doramcu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * REST controller for client manifest and platform-specific client downloads.
 * Returns available game clients with platform compatibility info and serves
 * platform-specific client downloads.
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientManifestController {

    private static final Logger log = LoggerFactory.getLogger(ClientManifestController.class);

    @Value("${game.client.executable-path:}")
    private String gameClientExecutablePath;

    @Value("${doramcu.web-ui.external:false}")
    private boolean webUiExternal;

    @Value("${info.app.version:0.3.0}")
    private String appVersion;

    @Value("${doramcu.client-download.fallback-dir:clients/downloads}")
    private String clientDownloadFallbackDir;

    /**
     * Returns the manifest of available game clients with platform compatibility info.
     * This endpoint allows the web UI to query available clients without hardcoding.
     */
    @GetMapping("/manifest")
    public ResponseEntity<Map<String, Object>> getClientManifest() {
        log.debug("Returning client manifest");

        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("version", appVersion);

        List<Map<String, Object>> clients = new ArrayList<>();

        // Unity Desktop Client
        Map<String, Object> unityClient = createClientEntry(
                "unity",
                "DoraMCU Unity Client",
                "Full-featured 3D game client with Unity engine",
                "native"
        );

        List<Map<String, Object>> unityPlatforms = new ArrayList<>();

        // Windows
        Map<String, Object> windows = new LinkedHashMap<>();
        windows.put("os", "windows");
        windows.put("architecture", "x64");
        windows.put("downloadUrl", "/api/v1/clients/download/unity/windows");
        windows.put("filename", "DoraMCU-Setup.exe");
        windows.put("minimumSpecs", Map.of(
                "cores", 4,
                "memory", 4,
                "gpu", "DirectX 11"
        ));
        unityPlatforms.add(windows);

        // macOS
        Map<String, Object> macos = new LinkedHashMap<>();
        macos.put("os", "macos");
        macos.put("architecture", "x64");
        macos.put("downloadUrl", "/api/v1/clients/download/unity/macos");
        macos.put("filename", "DoraMCU.dmg");
        macos.put("minimumSpecs", Map.of(
                "cores", 4,
                "memory", 4,
                "gpu", "Metal"
        ));
        unityPlatforms.add(macos);

        // Linux
        Map<String, Object> linux = new LinkedHashMap<>();
        linux.put("os", "linux");
        linux.put("architecture", "x64");
        linux.put("downloadUrl", "/api/v1/clients/download/unity/linux");
        linux.put("filename", "DoraMCU.AppImage");
        linux.put("minimumSpecs", Map.of(
                "cores", 4,
                "memory", 4,
                "gpu", "OpenGL 4.0"
        ));
        unityPlatforms.add(linux);

        unityClient.put("platforms", unityPlatforms);
        clients.add(unityClient);

        // Electron Client
        Map<String, Object> electronClient = createClientEntry(
                "electron",
                "DoraMCU Electron",
                "Electron-based desktop launcher with embedded server",
                "hybrid"
        );

        List<Map<String, Object>> electronPlatforms = new ArrayList<>();
        Map<String, Object> electronWindows = new LinkedHashMap<>();
        electronWindows.put("os", "windows");
        electronWindows.put("architecture", "x64");
        electronWindows.put("downloadUrl", "/api/v1/clients/download/electron/windows");
        electronWindows.put("filename", "DoraMCU-Electron-Setup.exe");
        electronWindows.put("minimumSpecs", Map.of(
                "cores", 2,
                "memory", 2
        ));
        electronPlatforms.add(electronWindows);
        electronClient.put("platforms", electronPlatforms);
        clients.add(electronClient);

        // Web Client
        Map<String, Object> webClient = createClientEntry(
                "web",
                "DoraMCU Web",
                "Browser-based full game client",
                "web"
        );
        webClient.put("platforms", Arrays.asList("all"));
        webClient.put("url", "/clients/desktop/");
        webClient.put("minimumSpecs", Map.of(
                "cores", 2,
                "memory", 2
        ));
        clients.add(webClient);

        // Mobile PWA Client
        Map<String, Object> mobileClient = createClientEntry(
                "mobile",
                "DoraMCU Mobile",
                "Lightweight PWA game client for mobile devices",
                "pwa"
        );
        mobileClient.put("platforms", Arrays.asList("android", "ios"));
        mobileClient.put("url", "/clients/mobile/");
        mobileClient.put("minimumSpecs", Map.of(
                "cores", 2,
                "memory", 1
        ));
        clients.add(mobileClient);

        // Tablet Client
        Map<String, Object> tabletClient = createClientEntry(
                "tablet",
                "DoraMCU Tablet",
                "Tablet-optimized game client with touch-friendly UI",
                "web"
        );
        tabletClient.put("platforms", Arrays.asList("android", "ios"));
        tabletClient.put("url", "/clients/tablet/");
        tabletClient.put("minimumSpecs", Map.of(
                "cores", 4,
                "memory", 2
        ));
        clients.add(tabletClient);

        manifest.put("clients", clients);

        return ResponseEntity.ok(manifest);
    }

    /**
     * Downloads a platform-specific client.
     *
     * @param platform the client platform (unity, electron)
     * @param os the operating system (windows, macos, linux)
     * @return the client executable as a file download
     */
    @GetMapping("/download/{platform}/{os}")
    public ResponseEntity<?> downloadClient(
            @PathVariable String platform,
            @PathVariable String os) {

        log.info("Client download requested: platform={}, os={}", platform, os);

        // Determine the file path based on platform and OS
        File file = resolveClientFile(platform, os);

        if (file == null || !file.exists()) {
            log.warn("Client file not found: platform={}, os={}", platform, os);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Client not available");
            error.put("message", String.format(
                "The %s client for %s is not currently available for download. Please check back later or use the web client.",
                platform, os));
            error.put("platform", platform);
            error.put("os", os);
            error.put("suggestion", "Use the web client at /clients/desktop/ for immediate access.");
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String fileName = file.getName();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (IOException e) {
            log.error("Failed to read client file: platform={}, os={}", platform, os, e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Failed to read client file");
            error.put("platform", platform);
            error.put("os", os);
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    /**
     * Resolves the file path for a given platform and OS.
     * Tries the build directory first, then falls back to static resources.
     */
    private File resolveClientFile(String platform, String os) {
        File primary;
        switch (platform.toLowerCase()) {
            case "unity":
                primary = resolveUnityClientFile(os);
                if (primary != null && primary.exists()) {
                    return primary;
                }
                return resolveFallbackClientFile("unity", os);
            case "electron":
                primary = resolveElectronClientFile(os);
                if (primary != null && primary.exists()) {
                    return primary;
                }
                return resolveFallbackClientFile("electron", os);
            default:
                log.warn("Unknown platform: {}", platform);
                return null;
        }
    }

    /**
     * Resolves a fallback client file from the static resources directory.
     * Used when the build directory doesn't have the client file.
     */
    private File resolveFallbackClientFile(String platform, String os) {
        String osLower = os.toLowerCase();
        String fallbackPath;

        if ("unity".equals(platform)) {
            switch (osLower) {
                case "windows":
                    fallbackPath = clientDownloadFallbackDir + "/unity/windows/DoraMCU-Unity-Setup.exe";
                    break;
                case "macos":
                    fallbackPath = clientDownloadFallbackDir + "/unity/macos/DoraMCU-Unity.dmg";
                    break;
                case "linux":
                    fallbackPath = clientDownloadFallbackDir + "/unity/linux/DoraMCU-Unity.AppImage";
                    break;
                default:
                    return null;
            }
        } else {
            switch (osLower) {
                case "windows":
                    fallbackPath = clientDownloadFallbackDir + "/electron/windows/DoraMCU-Electron-Setup.exe";
                    break;
                case "macos":
                    fallbackPath = clientDownloadFallbackDir + "/electron/macos/DoraMCU-Electron.dmg";
                    break;
                case "linux":
                    fallbackPath = clientDownloadFallbackDir + "/electron/linux/DoraMCU-Electron.AppImage";
                    break;
                default:
                    return null;
            }
        }

        // Try as classpath resource first (static resources in JAR)
        try {
            java.net.URL resource = getClass().getClassLoader().getResource(fallbackPath);
            if (resource != null) {
                Path path = Path.of(resource.toURI());
                if (Files.exists(path)) {
                    log.info("Found fallback client file: {}", path);
                    return path.toFile();
                }
            }
        } catch (Exception e) {
            log.debug("Fallback classpath resource not found: {}", fallbackPath);
        }

        // Try as relative file path (development mode)
        File fallbackFile = new File("src/main/resources/static/" + fallbackPath);
        if (fallbackFile.exists()) {
            log.info("Found fallback client file: {}", fallbackFile.getAbsolutePath());
            return fallbackFile;
        }

        log.debug("Fallback client file not found: {}", fallbackPath);
        return null;
    }

    /**
     * Resolves the Unity client file for a given OS.
     */
    private File resolveUnityClientFile(String os) {
        switch (os.toLowerCase()) {
            case "windows":
                // Use the configured executable path or default
                if (gameClientExecutablePath != null && !gameClientExecutablePath.isBlank()) {
                    return new File(gameClientExecutablePath);
                }
                return new File("unity-client/Build/DoraMCU.Client.exe");
            case "macos":
                return new File("unity-client/Build/DoraMCU.dmg");
            case "linux":
                return new File("unity-client/Build/DoraMCU.AppImage");
            default:
                log.warn("Unknown OS for Unity client: {}", os);
                return null;
        }
    }

    /**
     * Resolves the Electron client file for a given OS.
     */
    private File resolveElectronClientFile(String os) {
        switch (os.toLowerCase()) {
            case "windows":
                return new File("dora-electron/build/DoraMCU-Electron-Setup.exe");
            case "macos":
                return new File("dora-electron/build/DoraMCU-Electron.dmg");
            case "linux":
                return new File("dora-electron/build/DoraMCU-Electron.AppImage");
            default:
                log.warn("Unknown OS for Electron client: {}", os);
                return null;
        }
    }

    /**
     * Returns the current server version for client version checking.
     * Clients use this to determine if an update is available.
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getVersion() {
        Map<String, Object> versionInfo = new LinkedHashMap<>();
        versionInfo.put("version", appVersion);
        versionInfo.put("timestamp", System.currentTimeMillis());
        
        // Include minimum client version required by this server
        versionInfo.put("minimumClientVersion", appVersion);
        
        // Include update availability for each client type
        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("unity", appVersion);
        updates.put("electron", appVersion);
        updates.put("web", appVersion);
        versionInfo.put("clientVersions", updates);
        
        log.debug("Returning version info: {}", appVersion);
        return ResponseEntity.ok(versionInfo);
    }

    /**
     * Returns platform-specific client capabilities package for PWA and browser extension delivery.
     * This endpoint is called after platform detection to determine what the client should download.
     *
     * @param platform the detected platform (desktop, mobile, tablet)
     * @param os the detected OS (windows, macos, linux, android, ios)
     * @return client distribution data with PWA manifest, extension URL, game engine bundle, and AI models
     */
    @GetMapping("/distribute")
    public ResponseEntity<Map<String, Object>> distributeClient(
            @RequestParam(value = "platform", defaultValue = "desktop") String platform,
            @RequestParam(value = "os", defaultValue = "windows") String os) {

        log.info("Client distribution requested: platform={}, os={}", platform, os);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("platform", platform);

        // Determine client type based on platform
        boolean isMobile = "mobile".equalsIgnoreCase(platform) || "android".equalsIgnoreCase(os) || "ios".equalsIgnoreCase(os);
        response.put("clientType", isMobile ? "pwa" : "pwa+extension");

        // PWA manifest URL
        response.put("pwaManifest", "/manifest.json");

        // Extension download URL (only for non-mobile platforms)
        if (!isMobile) {
            response.put("extensionDownloadUrl", "/api/v1/clients/extension/chrome");
        }

        // Game engine bundle URL
        response.put("gameEngineBundle", "/assets/game-engine/bundle.js");

        // AI models for local inference
        List<Map<String, Object>> aiModels = new ArrayList<>();
        Map<String, Object> travellerAi = new LinkedHashMap<>();
        travellerAi.put("name", "traveller-ai");
        travellerAi.put("url", "/models/traveller.onnx");
        travellerAi.put("version", 1);
        aiModels.add(travellerAi);

        Map<String, Object> earth2026Ai = new LinkedHashMap<>();
        earth2026Ai.put("name", "earth2026-ai");
        earth2026Ai.put("url", "/models/earth2026.onnx");
        earth2026Ai.put("version", 1);
        aiModels.add(earth2026Ai);
        response.put("aiModels", aiModels);

        // Game rules for local execution
        List<Map<String, Object>> gameRules = new ArrayList<>();
        Map<String, Object> travellerRules = new LinkedHashMap<>();
        travellerRules.put("system", "traveller");
        travellerRules.put("url", "/assets/rules/traveller.json");
        gameRules.add(travellerRules);

        Map<String, Object> earth2026Rules = new LinkedHashMap<>();
        earth2026Rules.put("system", "earth2026");
        earth2026Rules.put("url", "/assets/rules/earth2026.json");
        gameRules.add(earth2026Rules);
        response.put("gameRules", gameRules);

        // WebSocket sync endpoint
        response.put("syncEndpoint", "/ws/game-sync");

        return ResponseEntity.ok(response);
    }

    /**
     * Serves the browser extension download for Chrome/Chromium-based browsers.
     */
    @GetMapping(value = "/extension/chrome", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadChromeExtension() {
        log.info("Chrome extension download requested");
        // In production, this would serve the actual extension ZIP file
        // For now, return a placeholder
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Extension not yet available");
        error.put("message", "The browser extension is currently being built. The PWA will function without it.");
        return ResponseEntity.status(503).build();
    }

    /**
     * Creates a standard client entry map.
     */
    private Map<String, Object> createClientEntry(String id, String name, String description, String type) {
        Map<String, Object> client = new LinkedHashMap<>();
        client.put("id", id);
        client.put("name", name);
        client.put("description", description);
        client.put("type", type);
        return client;
    }
}
