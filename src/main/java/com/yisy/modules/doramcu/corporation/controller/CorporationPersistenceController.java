package com.yisy.modules.doramcu.corporation.controller;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.corporation.model.CorporationSave;
import com.yisy.modules.doramcu.corporation.service.CorporationPersistenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for corporation persistence operations.
 * <p>
 * Provides endpoints for manual save/load of corporation state,
 * allowing players to save progress and restore it later.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/corporate/persistence")
public class CorporationPersistenceController {

    private final CorporationPersistenceService persistenceService;

    public CorporationPersistenceController(CorporationPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * Create a manual save snapshot of the corporation's current state.
     *
     * @param corporationId the corporation ID
     * @param request optional save name in request body
     * @return the created save
     */
    @PostMapping("/save")
    public ResponseEntity<CorporationSave> createSave(
            @RequestParam Long corporationId,
            @RequestBody(required = false) Map<String, String> request) {
        String saveName = (request != null) ? request.get("saveName") : null;
        CorporationSave save = persistenceService.createSave(corporationId, saveName);
        return ResponseEntity.ok(save);
    }

    /**
     * List all saves for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of saves
     */
    @GetMapping("/saves")
    public ResponseEntity<List<CorporationSave>> listSaves(@RequestParam Long corporationId) {
        List<CorporationSave> saves = persistenceService.listSaves(corporationId);
        return ResponseEntity.ok(saves);
    }

    /**
     * Load a save snapshot, restoring the corporation's state.
     *
     * @param corporationId the corporation ID
     * @param saveId the save ID
     * @return the loaded save with restoration details
     */
    @PostMapping("/save/{saveId}/load")
    public ResponseEntity<Map<String, Object>> loadSave(
            @RequestParam Long corporationId,
            @PathVariable Long saveId) {
        CorporationSave save = persistenceService.loadSave(corporationId, saveId);
        Map<String, Object> response = new HashMap<>();
        response.put("save", save);
        response.put("message", "Save '" + save.getSaveName() + "' loaded successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a save snapshot.
     *
     * @param corporationId the corporation ID
     * @param saveId the save ID
     * @return confirmation message
     */
    @DeleteMapping("/save/{saveId}")
    public ResponseEntity<Map<String, String>> deleteSave(
            @RequestParam Long corporationId,
            @PathVariable Long saveId) {
        persistenceService.deleteSave(corporationId, saveId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Save deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get the current office building level for a corporation.
     *
     * @param corporationId the corporation ID
     * @return the building level
     */
    @GetMapping("/office-level")
    public ResponseEntity<Map<String, Object>> getOfficeLevel(@RequestParam Long corporationId) {
        int level = persistenceService.getOfficeBuildingLevel(corporationId);
        Map<String, Object> response = new HashMap<>();
        response.put("corporationId", corporationId);
        response.put("officeBuildingLevel", level);
        return ResponseEntity.ok(response);
    }
}
