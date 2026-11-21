package com.yisy.modules.doramcu.fps.controller;
import com.yisy.modules.doramcu.model.players.Player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal REST API for communication between the dedicated FPS server and the main backend.
 * Endpoints are at /api/internal/fps/server.
 */
@RestController
@RequestMapping("/api/internal/fps/server")
public class FpsServerController {

    private static final Logger log = LoggerFactory.getLogger(FpsServerController.class);

    /** Registered dedicated servers: serverId → server info. */
    private final Map<String, ServerInfo> registeredServers = new ConcurrentHashMap<>();

    /**
     * Register a dedicated FPS server.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerServer(@RequestBody Map<String, Object> payload) {
        String serverId = (String) payload.get("serverId");
        Integer port = (Integer) payload.get("port");
        Integer maxPlayers = (Integer) payload.get("maxPlayers");

        if (serverId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "serverId is required"));
        }

        ServerInfo info = new ServerInfo(serverId, port != null ? port : 9000,
                maxPlayers != null ? maxPlayers : 16);
        registeredServers.put(serverId, info);
        log.info("FPS server registered: {} on port {}", serverId, info.port());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "serverId", serverId,
                "websocketUrl", "ws://localhost:" + info.port() + "/ws/fps"
        ));
    }

    /**
     * Heartbeat from a dedicated server.
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> payload) {
        String serverId = (String) payload.get("serverId");
        if (serverId != null && registeredServers.containsKey(serverId)) {
            log.debug("Heartbeat from server {}", serverId);
            return ResponseEntity.ok(Map.of("status", "ok"));
        }
        return ResponseEntity.ok(Map.of("status", "unknown", "serverId", serverId));
    }

    /**
     * Validate a player token.
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, Object> payload) {
        String token = (String) payload.get("token");
        String playerId = (String) payload.get("playerId");

        // Simple token validation — in production, this would check JWT/session
        boolean valid = token != null && !token.isEmpty() && playerId != null;
        log.debug("Token validation: playerId={}, valid={}", playerId, valid);

        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "playerId", playerId
        ));
    }

    /**
     * Report match start.
     */
    @PostMapping("/match-start")
    public ResponseEntity<Map<String, Object>> matchStart(@RequestBody Map<String, Object> payload) {
        String serverId = (String) payload.get("serverId");
        String sessionId = (String) payload.get("sessionId");
        String gameMode = (String) payload.get("gameMode");
        String mapName = (String) payload.get("mapName");
        Integer playerCount = (Integer) payload.get("playerCount");

        log.info("Match started on server {}: {} ({}, {} players)",
                serverId, sessionId, gameMode, playerCount);

        return ResponseEntity.ok(Map.of("status", "recorded", "sessionId", sessionId));
    }

    /**
     * Report match end.
     */
    @PostMapping("/match-end")
    public ResponseEntity<Map<String, Object>> matchEnd(@RequestBody Map<String, Object> payload) {
        String serverId = (String) payload.get("serverId");
        String sessionId = (String) payload.get("sessionId");
        String winnerTeam = (String) payload.get("winnerTeam");
        Long durationSeconds = (Long) payload.get("durationSeconds");

        log.info("Match ended on server {}: {} (winner: {}, duration: {}s)",
                serverId, sessionId, winnerTeam, durationSeconds);

        return ResponseEntity.ok(Map.of("status", "recorded", "sessionId", sessionId));
    }

    /**
     * Get list of registered servers.
     */
    @GetMapping("/servers")
    public ResponseEntity<Map<String, Object>> getServers() {
        return ResponseEntity.ok(Map.of(
                "servers", registeredServers.values(),
                "count", registeredServers.size()
        ));
    }

    /**
     * Server info record.
     */
    public record ServerInfo(String serverId, int port, int maxPlayers) {
    }
}
