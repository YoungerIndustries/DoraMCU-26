package com.yisy.modules.doraneuron.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Receives state updates from game nodes and provides state synchronization.
 *
 * This controller handles HTTP-based fallback for state sync when WebSocket
 * is unavailable. The primary sync mechanism is WebSocket via GameSyncWebSocketHandler.
 */
@RestController
@RequestMapping("/api/v1/game-sync")
public class GameStateSyncController {

    private static final Logger log = LoggerFactory.getLogger(GameStateSyncController.class);

    /**
     * Receive a state update from a game node (HTTP fallback).
     */
    @PostMapping("/state")
    public ResponseEntity<Map<String, Object>> receiveStateUpdate(@RequestBody Map<String, Object> request) {
        String playerId = (String) request.get("playerId");
        String sessionId = (String) request.get("sessionId");
        Map<String, Object> state = (Map<String, Object>) request.get("state");
        Integer version = (Integer) request.get("version");

        if (playerId == null || sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "playerId and sessionId are required"));
        }

        log.info("Received state update from player {} (session: {}, version: {})", playerId, sessionId, version);

        // In production, this would persist the state to the database
        // and check for conflicts with the server's version

        // For now, return a simple ACK
        return ResponseEntity.ok(Map.of(
                "type", "SYNC_ACK",
                "playerId", playerId,
                "sessionId", sessionId,
                "version", version != null ? version + 1 : 1
        ));
    }

    /**
     * Request the current state from the server (HTTP fallback).
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> requestState(
            @RequestParam String playerId,
            @RequestParam String sessionId) {

        log.info("State request from player {} (session: {})", playerId, sessionId);

        // In production, this would fetch the latest state from the database
        // For now, return an empty state
        return ResponseEntity.ok(Map.of(
                "type", "STATE_UPDATE",
                "playerId", playerId,
                "sessionId", sessionId,
                "state", Map.of(),
                "version", 0
        ));
    }
}
