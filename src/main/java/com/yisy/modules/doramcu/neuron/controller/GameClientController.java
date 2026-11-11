package com.yisy.modules.doramcu.neuron.controller;

import com.yisy.modules.doramcu.neuron.service.DatabaseSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for game client deployment.
 * Provides the game client executable for download and the database snapshot
 * for the game client's local replica initialization.
 */
@RestController
@RequestMapping("/api/v1/game-client")
public class GameClientController {

    private static final Logger log = LoggerFactory.getLogger(GameClientController.class);

    private final DatabaseSnapshotService snapshotService;

    @Value("${game.client.executable-path:}")
    private String executablePath;

    public GameClientController(DatabaseSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    /**
     * Serves the game client executable for download.
     * The executable path is configured via game.client.executable-path property.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadGameClient() throws IOException {
        if (executablePath == null || executablePath.isBlank()) {
            log.warn("Game client executable path not configured");
            return ResponseEntity.notFound().build();
        }

        File file = new File(executablePath);
        if (!file.exists()) {
            log.warn("Game client executable not found at: {}", executablePath);
            return ResponseEntity.notFound().build();
        }

        byte[] data = Files.readAllBytes(file.toPath());
        String fileName = file.getName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * Returns a full database snapshot as JSON Lines for the game client
     * to initialize its local database replica.
     */
    @GetMapping("/db-snapshot")
    public ResponseEntity<String> getDatabaseSnapshot() {
        log.info("Game client requesting database snapshot");
        String snapshot = snapshotService.exportSnapshot();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(snapshot);
    }

    // Track co-server status: clientId -> true if promoted
    private final Map<String, Boolean> coServerClients = new ConcurrentHashMap<>();

    /**
     * Promotes a game client to co-server role when the main server is unavailable.
     * Called by the Unity client's NeuronClientService when heartbeat failures exceed threshold.
     */
    @PostMapping("/neuron/co-server/promote")
    public ResponseEntity<Map<String, Object>> promoteToCoServer(@RequestBody Map<String, Object> request) {
        String clientId = (String) request.get("clientId");
        String playerId = (String) request.get("playerId");
        String reason = (String) request.get("reason");

        log.info("Game client requesting co-server promotion: clientId={}, playerId={}, reason={}",
                clientId, playerId, reason);

        coServerClients.put(clientId, true);
        log.info("Game client promoted to co-server: clientId={}", clientId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "clientId", clientId,
                "message", "Promoted to co-server"
        ));
    }

    /**
     * Resigns a game client from co-server role when the main server recovers.
     * Called by the Unity client's NeuronClientService when the server comes back online.
     */
    @PostMapping("/neuron/co-server/resign")
    public ResponseEntity<Map<String, Object>> resignCoServer(@RequestBody Map<String, Object> request) {
        String clientId = (String) request.get("clientId");

        log.info("Game client resigning from co-server role: clientId={}", clientId);

        coServerClients.remove(clientId);
        log.info("Game client resigned from co-server: clientId={}", clientId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "clientId", clientId,
                "message", "Resigned from co-server"
        ));
    }

    /**
     * Returns the list of currently promoted co-server clients.
     */
    @GetMapping("/neuron/co-server/status")
    public ResponseEntity<Map<String, Object>> getCoServerStatus() {
        return ResponseEntity.ok(Map.of(
                "coServerClients", coServerClients.keySet(),
                "count", coServerClients.size()
        ));
    }
}
