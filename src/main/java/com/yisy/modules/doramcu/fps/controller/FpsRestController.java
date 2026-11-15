package com.yisy.modules.doramcu.fps.controller;

import com.yisy.modules.doramcu.fps.*;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for FPS matchmaking, content, and statistics.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/v1/fps/matches - Create a new match</li>
 *   <li>GET /api/v1/fps/matches - List active matches</li>
 *   <li>POST /api/v1/fps/matches/{id}/join - Join a match</li>
 *   <li>GET /api/v1/fps/weapons - List available weapons</li>
 *   <li>GET /api/v1/fps/maps - List available maps</li>
 *   <li>GET /api/v1/fps/stats/{playerId} - Get player statistics</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/fps")
public class FpsRestController {

    private static final Logger log = LoggerFactory.getLogger(FpsRestController.class);

    private final FpsMatchmakingService matchmakingService;
    private final FpsWeaponService weaponService;
    private final FpsMapService mapService;
    private final FpsGameServerWrapper gameServer;

    public FpsRestController(
            FpsMatchmakingService matchmakingService,
            FpsWeaponService weaponService,
            FpsMapService mapService,
            FpsGameServerWrapper gameServer) {
        this.matchmakingService = matchmakingService;
        this.weaponService = weaponService;
        this.mapService = mapService;
        this.gameServer = gameServer;
    }

    // ── Match Endpoints ───────────────────────────────────────────────────────

    /**
     * Create a new match/room.
     */
    @PostMapping("/matches")
    public ResponseEntity<Map<String, Object>> createMatch(
            @RequestParam(defaultValue = "player1") String playerId,
            @RequestParam(defaultValue = "Player") String playerName,
            @RequestParam(defaultValue = "DEATHMATCH") FpsMatch.GameMode mode,
            @RequestParam(required = false) String mapId,
            @RequestParam(defaultValue = "8") int maxPlayers) {

        FpsMatchmakingService.MatchmakingRoom room = matchmakingService.createRoom(
                playerId, playerName, mode, mapId, maxPlayers
        );

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getRoomId());
        response.put("mode", room.getMode().name());
        response.put("map", room.getMap() != null ? room.getMap().getName() : "default");
        response.put("maxPlayers", room.getMaxPlayers());
        response.put("playerCount", room.getPlayerCount());
        response.put("state", room.getState().name());
        response.put("serverUrl", room.getServerUrl());

        log.info("Created match {} for player {}", room.getRoomId(), playerName);
        return ResponseEntity.ok(response);
    }

    /**
     * List all active matches/rooms.
     */
    @GetMapping("/matches")
    public ResponseEntity<List<Map<String, Object>>> listMatches() {
        List<Map<String, Object>> matches = new ArrayList<>();

        for (FpsMatchmakingService.MatchmakingRoom room : matchmakingService.getActiveRooms()) {
            Map<String, Object> m = new HashMap<>();
            m.put("roomId", room.getRoomId());
            m.put("mode", room.getMode().name());
            m.put("map", room.getMap() != null ? room.getMap().getName() : "default");
            m.put("maxPlayers", room.getMaxPlayers());
            m.put("playerCount", room.getPlayerCount());
            m.put("state", room.getState().name());
            matches.add(m);
        }

        // Also include in-progress game sessions
        for (FpsSession session : gameServer.getActiveSessions()) {
            Map<String, Object> m = new HashMap<>();
            m.put("roomId", session.getSessionId());
            m.put("mode", session.getGameMode().name());
            m.put("map", session.getMap() != null ? session.getMap().getName() : "default");
            m.put("maxPlayers", session.getMaxPlayers());
            m.put("playerCount", session.getPlayerCount());
            m.put("state", session.getState().name());
            matches.add(m);
        }

        return ResponseEntity.ok(matches);
    }

    /**
     * Join an existing match/room.
     */
    @PostMapping("/matches/{roomId}/join")
    public ResponseEntity<Map<String, Object>> joinMatch(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "player2") String playerId,
            @RequestParam(defaultValue = "Player") String playerName) {

        return matchmakingService.joinRoom(roomId, playerId, playerName)
                .map(room -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("roomId", room.getRoomId());
                    response.put("playerCount", room.getPlayerCount());
                    response.put("message", "Joined room successfully");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Room not found or full");
                    return ResponseEntity.badRequest().body(error);
                });
    }

    /**
     * Ready up for a match.
     */
    @PostMapping("/matches/{roomId}/ready")
    public ResponseEntity<Map<String, Object>> readyUp(
            @PathVariable String roomId,
            @RequestParam String playerId) {

        matchmakingService.readyUp(roomId, playerId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ready up sent");
        return ResponseEntity.ok(response);
    }

    // ── Weapon Endpoints ──────────────────────────────────────────────────────

    /**
     * List all available weapons.
     */
    @GetMapping("/weapons")
    public ResponseEntity<List<Map<String, Object>>> listWeapons(
            @RequestParam(required = false) String theme) {

        List<Map<String, Object>> weapons = new ArrayList<>();

        for (FpsWeapon weapon : weaponService.getAllWeapons()) {
            Map<String, Object> w = new HashMap<>();
            w.put("id", weapon.getId());
            w.put("name", weapon.getName());
            w.put("type", weapon.getType().name());
            w.put("damage", weapon.getDamage());
            w.put("fireRate", weapon.getFireRate());
            w.put("reloadTime", weapon.getReloadTime());
            w.put("magSize", weapon.getMagSize());
            w.put("reserveAmmo", weapon.getReserveAmmo());
            w.put("hitScan", weapon.isHitScan());
            w.put("range", weapon.getRange());
            weapons.add(w);
        }

        return ResponseEntity.ok(weapons);
    }

    // ── Map Endpoints ─────────────────────────────────────────────────────────

    /**
     * List all available maps.
     */
    @GetMapping("/maps")
    public ResponseEntity<List<Map<String, Object>>> listMaps(
            @RequestParam(required = false) String theme) {

        List<Map<String, Object>> maps = new ArrayList<>();

        for (FpsMap map : mapService.getAllMaps()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", map.getId());
            m.put("name", map.getName());
            m.put("description", map.getDescription());
            m.put("width", map.getMapWidth());
            m.put("depth", map.getMapDepth());
            m.put("height", map.getMapHeight());
            m.put("theme", map.getTheme());
            m.put("spawnPoints", map.getSpawnPoints().size());
            maps.add(m);
        }

        // Filter by theme if specified
        if (theme != null && !theme.isEmpty()) {
            maps.removeIf(m -> !theme.equals(m.get("theme")));
        }

        return ResponseEntity.ok(maps);
    }

    // ── Stats Endpoints ───────────────────────────────────────────────────────

    /**
     * Get player statistics.
     */
    @GetMapping("/stats/{playerId}")
    public ResponseEntity<Map<String, Object>> getPlayerStats(@PathVariable String playerId) {
        FpsMatchmakingService.PlayerStats stats = matchmakingService.getPlayerStats(playerId);

        Map<String, Object> response = new HashMap<>();
        response.put("playerId", playerId);
        response.put("matches", stats.matches());
        response.put("kills", stats.kills());
        response.put("deaths", stats.deaths());
        response.put("assists", stats.assists());
        response.put("wins", stats.wins());
        response.put("kdRatio", Math.round(stats.kdRatio() * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }

    /**
     * Get server status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeSessions", gameServer.getActiveSessions().size());
        status.put("activeRooms", matchmakingService.getActiveRooms().size());
        status.put("weapons", weaponService.getAllWeapons().size());
        status.put("maps", mapService.getAllMaps().size());
        status.put("completedMatches", matchmakingService.getCompletedMatches().size());
        return ResponseEntity.ok(status);
    }
}
