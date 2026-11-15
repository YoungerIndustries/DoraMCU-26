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
    public ResponseEntity<byte[]> downloadClient(
            @PathVariable String platform,
            @PathVariable String os) {

        log.info("Client download requested: platform={}, os={}", platform, os);

        // Determine the file path based on platform and OS
        File file = resolveClientFile(platform, os);

        if (file == null || !file.exists()) {
            log.warn("Client file not found: platform={}, os={}", platform, os);
            return ResponseEntity.notFound().build();
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
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolves the file path for a given platform and OS.
     */
    private File resolveClientFile(String platform, String os) {
        switch (platform.toLowerCase()) {
            case "unity":
                return resolveUnityClientFile(os);
            case "electron":
                return resolveElectronClientFile(os);
            default:
                log.warn("Unknown platform: {}", platform);
                return null;
        }
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
