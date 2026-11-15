package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.GameEvent;
import com.yisy.modules.doramcu.service.SpectatorService;
import com.yisy.modules.doramcu.service.SpectatorService.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for spectator mode — read-only observation of game sessions.
 * <p>
 * Provides endpoints for registering spectators, joining sessions,
 * retrieving redacted game state, and viewing spectator statistics.
 * Useful for streaming, tutorials, and game analysis.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/spectator}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/spectator")
public class SpectatorController {

    private final SpectatorService spectatorService;

    public SpectatorController(SpectatorService spectatorService) {
        this.spectatorService = spectatorService;
    }

    /**
     * Register a new spectator.
     *
     * @param spectatorId unique spectator identifier
     * @param name        optional display name
     * @return the registered spectator info
     */
    @PostMapping("/register/{spectatorId}")
    public ResponseEntity<?> registerSpectator(@PathVariable String spectatorId,
                                               @RequestParam(required = false) String name) {
        SpectatorInfo info = spectatorService.registerSpectator(spectatorId, name);
        return ResponseEntity.ok(Map.of(
                "spectatorId", info.getSpectatorId(),
                "name", info.getName(),
                "joinedAt", info.getJoinedAt().toString()
        ));
    }

    /**
     * Unregister a spectator.
     *
     * @param spectatorId the spectator to unregister
     * @return success message
     */
    @DeleteMapping("/unregister/{spectatorId}")
    public ResponseEntity<?> unregisterSpectator(@PathVariable String spectatorId) {
        if (spectatorService.getSpectator(spectatorId).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Spectator not found: " + spectatorId));
        }
        spectatorService.unregisterSpectator(spectatorId);
        return ResponseEntity.ok(Map.of("message", "Spectator unregistered: " + spectatorId));
    }

    /**
     * Get spectator info.
     *
     * @param spectatorId the spectator to look up
     * @return the spectator info
     */
    @GetMapping("/info/{spectatorId}")
    public ResponseEntity<?> getSpectatorInfo(@PathVariable String spectatorId) {
        return spectatorService.getSpectator(spectatorId)
                .map(info -> ResponseEntity.ok(Map.of(
                        "spectatorId", info.getSpectatorId(),
                        "name", info.getName(),
                        "currentSession", info.getCurrentSession(),
                        "active", info.isActive(),
                        "joinedAt", info.getJoinedAt().toString()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Join a game session as a spectator.
     *
     * @param spectatorId the spectator ID
     * @param sessionId   the game session to observe
     * @return redacted game state
     */
    @PostMapping("/join/{spectatorId}/{sessionId}")
    public ResponseEntity<?> joinSession(@PathVariable String spectatorId,
                                         @PathVariable String sessionId) {
        Optional<SpectatorGameState> result = spectatorService.joinSession(spectatorId, sessionId);
        if (result.isPresent()) {
            return ResponseEntity.ok(buildSpectatorGameStateResponse(result.get()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Leave a game session.
     *
     * @param spectatorId the spectator ID
     * @param sessionId   the session to leave
     * @return success message
     */
    @PostMapping("/leave/{spectatorId}/{sessionId}")
    public ResponseEntity<?> leaveSession(@PathVariable String spectatorId,
                                          @PathVariable String sessionId) {
        spectatorService.leaveSession(spectatorId, sessionId);
        return ResponseEntity.ok(Map.of("message", "Left session: " + sessionId));
    }

    /**
     * Get redacted game state for a session.
     *
     * @param sessionId the game session
     * @return redacted game state
     */
    @GetMapping("/state/{sessionId}")
    public ResponseEntity<?> getSpectatorGameState(@PathVariable String sessionId) {
        Optional<SpectatorGameState> result = spectatorService.getSpectatorGameState(sessionId);
        if (result.isPresent()) {
            return ResponseEntity.ok(buildSpectatorGameStateResponse(result.get()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get event history for a session.
     *
     * @param sessionId the game session
     * @param count     number of recent events (default 50)
     * @return list of events
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getEventHistory(@PathVariable String sessionId,
                                             @RequestParam(defaultValue = "50") int count) {
        List<GameEvent> events = spectatorService.getEventHistory(sessionId, count);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameEvent event : events) {
            result.add(Map.of(
                    "type", event.getType() != null ? event.getType().name() : "UNKNOWN",
                    "sessionId", event.getSessionId(),
                    "payload", event.getPayload() != null ? event.getPayload() : Collections.emptyMap()
            ));
        }
        return ResponseEntity.ok(Map.of("events", result, "count", result.size()));
    }

    /**
     * Get all spectators for a session.
     *
     * @param sessionId the game session
     * @return list of spectators
     */
    @GetMapping("/spectators/{sessionId}")
    public ResponseEntity<?> getSessionSpectators(@PathVariable String sessionId) {
        List<SpectatorInfo> spectators = spectatorService.getSessionSpectators(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SpectatorInfo info : spectators) {
            result.add(Map.of(
                    "spectatorId", info.getSpectatorId(),
                    "name", info.getName(),
                    "active", info.isActive()
            ));
        }
        return ResponseEntity.ok(Map.of("spectators", result, "count", result.size()));
    }

    /**
     * Get spectator count for a session.
     *
     * @param sessionId the game session
     * @return spectator count
     */
    @GetMapping("/count/{sessionId}")
    public ResponseEntity<?> getSpectatorCount(@PathVariable String sessionId) {
        int count = spectatorService.getSpectatorCount(sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "spectatorCount", count));
    }

    /**
     * Get all registered spectators.
     *
     * @return list of all spectators
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSpectators() {
        List<SpectatorInfo> spectators = spectatorService.getAllSpectators();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SpectatorInfo info : spectators) {
            result.add(Map.of(
                    "spectatorId", info.getSpectatorId(),
                    "name", info.getName(),
                    "currentSession", info.getCurrentSession(),
                    "active", info.isActive()
            ));
        }
        return ResponseEntity.ok(Map.of("spectators", result, "count", result.size()));
    }

    /**
     * Get all sessions with active spectators.
     *
     * @return list of session IDs
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getSessionsWithSpectators() {
        List<String> sessions = spectatorService.getSessionsWithSpectators();
        return ResponseEntity.ok(Map.of("sessions", sessions, "count", sessions.size()));
    }

    /**
     * Get global spectator statistics.
     *
     * @return statistics map
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(spectatorService.getStatistics());
    }

    /**
     * Get statistics for a specific session.
     *
     * @param sessionId the game session
     * @return statistics map
     */
    @GetMapping("/stats/{sessionId}")
    public ResponseEntity<?> getSessionStatistics(@PathVariable String sessionId) {
        return ResponseEntity.ok(spectatorService.getSessionStatistics(sessionId));
    }

    /**
     * Heartbeat for a spectator to keep connection alive.
     *
     * @param spectatorId the spectator ID
     * @return success message
     */
    @PostMapping("/heartbeat/{spectatorId}")
    public ResponseEntity<?> heartbeat(@PathVariable String spectatorId) {
        Optional<SpectatorInfo> opt = spectatorService.getSpectator(spectatorId);
        if (opt.isPresent()) {
            opt.get().heartbeat();
            return ResponseEntity.ok(Map.of("message", "Heartbeat received", "timestamp", opt.get().getLastHeartbeat().toString()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Remove stale spectators.
     *
     * @return number of removed spectators
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> removeStaleSpectators() {
        int removed = spectatorService.removeStaleSpectators();
        return ResponseEntity.ok(Map.of("removed", removed, "message", removed + " stale spectators removed"));
    }

    /**
     * Build a response map from SpectatorGameState.
     */
    private Map<String, Object> buildSpectatorGameStateResponse(SpectatorGameState state) {
        List<Map<String, Object>> players = new ArrayList<>();
        for (SpectatorPlayerSummary player : state.getPlayers()) {
            players.add(Map.of(
                    "name", player.getName(),
                    "fleetCount", player.getFleetCount(),
                    "planetCount", player.getPlanetCount()
            ));
        }

        List<Map<String, Object>> fleets = new ArrayList<>();
        for (SpectatorFleetSummary fleet : state.getFleets()) {
            fleets.add(Map.of(
                    "id", fleet.getId(),
                    "name", fleet.getPlayerName(),
                    "shipCount", fleet.getShipCount(),
                    "sector", fleet.getSector()
            ));
        }

        return Map.of(
                "sessionId", state.getSessionId(),
                "currentTurn", state.getCurrentTurn(),
                "playerCount", state.getPlayerCount(),
                "currentPlayerName", state.getCurrentPlayerName(),
                "players", players,
                "fleets", fleets,
                "fleetCount", state.getFleetCount(),
                "timestamp", state.getTimestamp().toString()
        );
    }
}
