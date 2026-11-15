package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.model.GameActionLog;
import com.yisy.modules.doramcu.service.GameReplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * REST controller for game action replay and analysis.
 * <p>
 * Provides endpoints for querying action logs, getting statistics,
 * exporting/importing logs, and managing session logs.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/replay}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/replay")
public class GameReplayController {

    private final GameReplayService gameReplayService;

    public GameReplayController(GameReplayService gameReplayService) {
        this.gameReplayService = gameReplayService;
    }

    /**
     * Get all action logs for a session.
     *
     * @param sessionId the session ID
     * @return list of action log entries
     */
    @GetMapping("/logs/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> getSessionLogs(
            @PathVariable String sessionId) {
        List<GameActionLog> logs = gameReplayService.getSessionLogs(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameActionLog log : logs) {
            result.add(toMap(log));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get action logs for a specific player in a session.
     *
     * @param sessionId the session ID
     * @param playerId the player ID
     * @return list of action log entries for the player
     */
    @GetMapping("/logs/{sessionId}/player/{playerId}")
    public ResponseEntity<List<Map<String, Object>>> getPlayerLogs(
            @PathVariable String sessionId,
            @PathVariable Long playerId) {
        List<GameActionLog> logs = gameReplayService.getPlayerLogs(sessionId, playerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameActionLog log : logs) {
            result.add(toMap(log));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get action logs filtered by action type.
     *
     * @param sessionId the session ID
     * @param actionType the action type name
     * @return list of action log entries of the specified type
     */
    @GetMapping("/logs/{sessionId}/type/{actionType}")
    public ResponseEntity<List<Map<String, Object>>> getActionTypeLogs(
            @PathVariable String sessionId,
            @PathVariable String actionType) {
        try {
            GameActionLog.ActionType type = GameActionLog.ActionType.valueOf(actionType);
            List<GameActionLog> logs = gameReplayService.getActionTypeLogs(sessionId, type);
            List<Map<String, Object>> result = new ArrayList<>();
            for (GameActionLog log : logs) {
                result.add(toMap(log));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get action logs within a turn range.
     *
     * @param sessionId the session ID
     * @param fromTurn inclusive start turn
     * @param toTurn inclusive end turn
     * @return list of action log entries in the turn range
     */
    @GetMapping("/logs/{sessionId}/turns")
    public ResponseEntity<List<Map<String, Object>>> getTurnRangeLogs(
            @PathVariable String sessionId,
            @RequestParam int fromTurn,
            @RequestParam int toTurn) {
        List<GameActionLog> logs = gameReplayService.getTurnRangeLogs(sessionId, fromTurn, toTurn);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameActionLog log : logs) {
            result.add(toMap(log));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get the most recent N action logs for a session.
     *
     * @param sessionId the session ID
     * @param count number of recent logs to return (default 50)
     * @return list of the most recent action log entries
     */
    @GetMapping("/logs/{sessionId}/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentLogs(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "50") int count) {
        List<GameActionLog> logs = gameReplayService.getRecentLogs(sessionId, count);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameActionLog log : logs) {
            result.add(toMap(log));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get action statistics for a session.
     *
     * @param sessionId the session ID
     * @return statistics map with total actions, actions by type, actions by player, turn count
     */
    @GetMapping("/stats/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionStatistics(
            @PathVariable String sessionId) {
        Map<String, Object> stats = gameReplayService.getSessionStatistics(sessionId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get the number of action logs for a session.
     *
     * @param sessionId the session ID
     * @return count of logged actions
     */
    @GetMapping("/count/{sessionId}")
    public ResponseEntity<Map<String, Object>> getLogCount(
            @PathVariable String sessionId) {
        int count = gameReplayService.getLogCount(sessionId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("logCount", count);
        return ResponseEntity.ok(result);
    }

    /**
     * Export all action logs for a session as JSON.
     *
     * @param sessionId the session ID
     * @return list of action log maps for JSON serialization
     */
    @GetMapping("/export/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> exportSessionLogs(
            @PathVariable String sessionId) {
        List<Map<String, Object>> logs = gameReplayService.exportSessionLogs(sessionId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Import action logs for a session from JSON.
     *
     * @param sessionId the session ID
     * @param logMaps list of action log maps
     * @return import result with count
     */
    @PostMapping("/import/{sessionId}")
    public ResponseEntity<Map<String, Object>> importSessionLogs(
            @PathVariable String sessionId,
            @RequestBody List<Map<String, Object>> logMaps) {
        int count = gameReplayService.importSessionLogs(sessionId, logMaps);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("sessionId", sessionId);
        result.put("importedCount", count);
        return ResponseEntity.ok(result);
    }

    /**
     * Clear all action logs for a session.
     *
     * @param sessionId the session ID
     * @return confirmation result
     */
    @DeleteMapping("/logs/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSessionLogs(
            @PathVariable String sessionId) {
        gameReplayService.clearSessionLogs(sessionId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("sessionId", sessionId);
        result.put("message", "Logs cleared");
        return ResponseEntity.ok(result);
    }

    /**
     * List all sessions that have action logs.
     *
     * @return list of session metadata maps
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<Map<String, Object>>> listSessionsWithLogs() {
        List<Map<String, Object>> sessions = gameReplayService.listSessionsWithLogs();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get all available action types.
     *
     * @return list of action type names
     */
    @GetMapping("/action-types")
    public ResponseEntity<List<String>> getActionTypes() {
        List<String> types = new ArrayList<>();
        for (GameActionLog.ActionType type : GameActionLog.ActionType.values()) {
            types.add(type.name());
        }
        return ResponseEntity.ok(types);
    }

    private Map<String, Object> toMap(GameActionLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("actionType", log.getActionType().name());
        map.put("playerId", log.getPlayerId());
        map.put("sessionId", log.getSessionId());
        map.put("turnNumber", log.getTurnNumber());
        map.put("timestamp", log.getTimestamp().toString());
        map.put("data", log.getData());
        return map;
    }
}
