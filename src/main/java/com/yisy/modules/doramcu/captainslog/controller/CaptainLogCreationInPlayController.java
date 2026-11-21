package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log Creation in Play endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides the quick character creation flow as an alternative to the full Lifepath system.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogCreationInPlayController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogCreationInPlayController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogCreationInPlayController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Starts a Creation in Play session for quick character creation.
     * <p>
     * A faster alternative to the full Lifepath system.
     * </p>
     */
    @PostMapping("/creation-in-play/start")
    public ResponseEntity<Map<String, Object>> startCreationInPlay(@RequestBody Map<String, String> body) {
        log.debug("POST /api/v1/captains-log/creation-in-play/start");
        String name = body.get("name");
        String species = body.get("species");
        String rank = body.get("rank");
        Map<String, Object> result = captainLogService.startCreationInPlay(name, species, rank);
        return ResponseEntity.ok(result);
    }

    /**
     * Makes a choice in a Creation in Play session.
     * <p>
     * Advances through the creation steps: Role → Background → Attributes → Disciplines → Values → Complete
     * </p>
     */
    @PostMapping("/creation-in-play/{sessionId}/choice")
    public ResponseEntity<Map<String, Object>> makeCreationChoice(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> choice) {
        log.debug("POST /api/v1/captains-log/creation-in-play/{}/choice", sessionId);
        Map<String, Object> result = captainLogService.makeCreationChoice(sessionId, choice);
        return ResponseEntity.ok(result);
    }

    /**
     * Lists all available character roles for Creation in Play.
     */
    @GetMapping("/creation-in-play/roles")
    public ResponseEntity<List<Map<String, String>>> listCreationRoles() {
        log.debug("GET /api/v1/captains-log/creation-in-play/roles");
        return ResponseEntity.ok(captainLogService.listCreationRoles());
    }

    /**
     * Lists all available background traits for Creation in Play.
     */
    @GetMapping("/creation-in-play/backgrounds")
    public ResponseEntity<List<Map<String, String>>> listCreationBackgrounds() {
        log.debug("GET /api/v1/captains-log/creation-in-play/backgrounds");
        return ResponseEntity.ok(captainLogService.listCreationBackgrounds());
    }

    /**
     * Lists all available core values for Creation in Play.
     */
    @GetMapping("/creation-in-play/values")
    public ResponseEntity<List<Map<String, String>>> listCreationValues() {
        log.debug("GET /api/v1/captains-log/creation-in-play/values");
        return ResponseEntity.ok(captainLogService.listCreationValues());
    }
}
