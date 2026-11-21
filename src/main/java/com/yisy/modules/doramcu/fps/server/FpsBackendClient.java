package com.yisy.modules.doramcu.fps.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST client for communication between the dedicated FPS server and the main backend.
 */
@Component
public class FpsBackendClient {

    private static final Logger log = LoggerFactory.getLogger(FpsBackendClient.class);

    private final FpsServerConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FpsBackendClient(FpsServerConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Register this server with the backend.
     */
    public boolean registerServer() {
        String url = config.getBackendUrl() + "/api/internal/fps/server/register";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("serverId", config.getServerId());
        payload.put("port", config.getServerPort());
        payload.put("maxPlayers", config.getMaxPlayers());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Failed to register server with backend: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send heartbeat to the backend.
     */
    public boolean sendHeartbeat() {
        String url = config.getBackendUrl() + "/api/internal/fps/server/heartbeat";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("serverId", config.getServerId());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Heartbeat failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate a player token with the backend.
     * @return true if the token is valid
     */
    public boolean validateToken(String token, String playerId) {
        String url = config.getBackendUrl() + "/api/internal/fps/server/validate-token";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", token);
        payload.put("playerId", playerId);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, payload, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode result = objectMapper.readTree(response.getBody());
                return result.path("valid").asBoolean(false);
            }
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Report match start to the backend.
     */
    public void reportMatchStart(String sessionId, String gameMode, String mapName, int playerCount) {
        String url = config.getBackendUrl() + "/api/internal/fps/server/match-start";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("serverId", config.getServerId());
        payload.put("sessionId", sessionId);
        payload.put("gameMode", gameMode);
        payload.put("mapName", mapName);
        payload.put("playerCount", playerCount);

        try {
            restTemplate.postForEntity(url, payload, String.class);
            log.info("Reported match start: {} ({}, {} players)", sessionId, gameMode, playerCount);
        } catch (Exception e) {
            log.warn("Failed to report match start: {}", e.getMessage());
        }
    }

    /**
     * Report match end to the backend.
     */
    public void reportMatchEnd(String sessionId, String winnerTeam, Map<String, Integer> scores, long durationSeconds) {
        String url = config.getBackendUrl() + "/api/internal/fps/server/match-end";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("serverId", config.getServerId());
        payload.put("sessionId", sessionId);
        if (winnerTeam != null) payload.put("winnerTeam", winnerTeam);
        payload.put("durationSeconds", durationSeconds);

        ObjectNode scoresNode = payload.putObject("scores");
        if (scores != null) {
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                scoresNode.put(entry.getKey(), entry.getValue());
            }
        }

        try {
            restTemplate.postForEntity(url, payload, String.class);
            log.info("Reported match end: {} (winner: {})", sessionId, winnerTeam);
        } catch (Exception e) {
            log.warn("Failed to report match end: {}", e.getMessage());
        }
    }
}
