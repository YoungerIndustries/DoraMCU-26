package com.yisy.modules.doramcu.controller.session;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.service.game.GameSaveService;
import com.yisy.modules.doramcu.service.session.GameSnapshotScheduler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * REST controller for game session save/load functionality.
 * <p>
 * Provides endpoints for exporting game sessions as downloadable JSON files,
 * importing saved files to create new sessions, validating save files,
 * and listing saveable sessions.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/save}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/save")
public class GameSaveController {

    private static final Logger log = LoggerFactory.getLogger(GameSaveController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final GameSaveService gameSaveService;
    private final GameSnapshotScheduler gameSnapshotScheduler;

    public GameSaveController(GameSaveService gameSaveService, GameSnapshotScheduler gameSnapshotScheduler) {
        this.gameSaveService = gameSaveService;
        this.gameSnapshotScheduler = gameSnapshotScheduler;
    }

    /**
     * Export a game session as a downloadable JSON save file.
     *
     * @param sessionId the session ID to export
     * @return JSON save file with metadata and game state
     */
    @GetMapping("/export/{sessionId}")
    public ResponseEntity<byte[]> exportSession(@PathVariable String sessionId) {
        try {
            Map<String, Object> saveFile = gameSaveService.exportSession(sessionId);
            byte[] jsonBytes = objectMapper.writeValueAsBytes(saveFile);

            String fileName = "save_" + sessionId.substring(0, Math.min(8, sessionId.length()))
                    + "_" + Instant.now().getEpochSecond() + ".json";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize save file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Import a save file to create a new game session.
     * Accepts JSON body with the save file structure.
     *
     * @param body JSON save file
     * @return the new session ID and metadata
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importSession(@RequestBody Map<String, Object> body) {
        try {
            String newSessionId = gameSaveService.importSession(body);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("sessionId", newSessionId);
            result.put("message", "Save imported successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Failed to import save: {}", e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Failed to import save: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Import a save file uploaded as a multipart file.
     *
     * @param file the uploaded JSON save file
     * @return the new session ID and metadata
     */
    @PostMapping(value = "/import/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importSessionFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("success", false);
                error.put("error", "Uploaded file is empty");
                return ResponseEntity.badRequest().body(error);
            }

            // Parse the JSON file
            @SuppressWarnings("unchecked")
            Map<String, Object> saveFile = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<Map<String, Object>>() {}
            );

            String newSessionId = gameSaveService.importSession(saveFile);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("sessionId", newSessionId);
            result.put("message", "Save imported successfully from file: " + file.getOriginalFilename());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Failed to parse uploaded file: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Failed to import save file: {}", e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Failed to import save file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Validate a save file without importing it.
     *
     * @param body JSON save file to validate
     * @return validation result with valid flag, errors, and summary
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSaveFile(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = gameSaveService.validateSaveFile(body);
        return ResponseEntity.ok(result);
    }

    /**
     * List all sessions that can be exported (have saved game state).
     *
     * @return list of session metadata
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<Map<String, Object>>> listSaveableSessions() {
        List<Map<String, Object>> sessions = gameSaveService.listSaveableSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get save file format information.
     *
     * @return format version and structure description
     */
    @GetMapping("/format")
    public ResponseEntity<Map<String, Object>> getSaveFormat() {
        Map<String, Object> format = new LinkedHashMap<>();
        format.put("version", "1.0");
        format.put("description", "DoraMCU game session save file");
        
        Map<String, Object> structure = new LinkedHashMap<>();
        structure.put("version", "Save format version string");
        structure.put("timestamp", "ISO-8601 export timestamp");
        structure.put("sessionName", "Original session name");
        structure.put("sessionId", "Original session ID");
        structure.put("galaxyName", "Galaxy name (loaded from DB on import)");
        structure.put("playerCount", "Number of players in the game");
        structure.put("currentTurn", "Current turn number");
        structure.put("currentPlayerIndex", "Index of the current player");
        structure.put("gameState", "Serialized game state (players, planets, fleets)");
        
        format.put("structure", structure);
        format.put("notes", List.of(
                "Galaxy/solar system data is NOT included — loaded from database on import",
                "Ships are referenced by ID only — missing ships are skipped on import",
                "Save files can be shared between players for scenario replay"
        ));
        
        return ResponseEntity.ok(format);
    }

    /**
     * Trigger an immediate snapshot of all active game sessions.
     * Requires authentication.
     *
     * @return snapshot results with counts and timestamp
     */
    @PostMapping("/snapshot/now")
    public ResponseEntity<Map<String, Object>> snapshotNow() {
        try {
            Map<String, Object> result = gameSnapshotScheduler.snapshotNow();
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to trigger snapshot: {}", e.getMessage(), e);
            Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Failed to trigger snapshot: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Return snapshot status for all persisted sessions.
     * Shows last activity time and snapshot count per session.
     *
     * @return list of session snapshot statuses
     */
    @GetMapping("/snapshot/status")
    public ResponseEntity<java.util.List<Map<String, Object>>> getSnapshotStatus() {
        try {
            java.util.List<Map<String, Object>> allPersisted = gameSaveService.listSaveableSessions();
            java.util.List<Map<String, Object>> statuses = new java.util.ArrayList<>();
            for (Map<String, Object> session : allPersisted) {
                Map<String, Object> status = new java.util.LinkedHashMap<>();
                status.put("sessionId", session.get("sessionId"));
                status.put("sessionName", session.get("sessionName"));
                status.put("lastActivityAt", session.get("lastActivityAt"));
                status.put("updatedAt", session.get("updatedAt"));
                status.put("snapshotCount", session.get("snapshotCount"));
                statuses.add(status);
            }
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            log.error("Failed to get snapshot status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(java.util.Collections.emptyList());
        }
    }
}
