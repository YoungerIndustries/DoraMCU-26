package com.yisy.modules.doramcu.star_trek_4x.controller;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.star_trek_4x.model.MissionUpdate;
import com.yisy.modules.doramcu.star_trek_4x.rpg.CharacterImporter;
import com.yisy.modules.doramcu.star_trek_4x.rpg.MissionUpdateService;
import com.yisy.modules.doramcu.star_trek_4x.rpg.SfbCharacterCreator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for the RPG module.
 * <p>
 * Provides endpoints for character management, character import, and mission integration.
 * </p>
 */
@RestController("starTrek4xRpgController")
@RequestMapping("/api/star-trek-4x/rpg")
public class RpgController {

    private final CharacterImporter characterImporter;
    private final SfbCharacterCreator sfbCharacterCreator;
    private final MissionUpdateService missionUpdateService;

    public RpgController(CharacterImporter characterImporter,
                          SfbCharacterCreator sfbCharacterCreator,
                          MissionUpdateService missionUpdateService) {
        this.characterImporter = characterImporter;
        this.sfbCharacterCreator = sfbCharacterCreator;
        this.missionUpdateService = missionUpdateService;
    }

    /**
     * Import a Captain's Log character.
     */
    @PostMapping("/characters/import")
    public ResponseEntity<Map<String, Object>> importCharacter(
            @RequestParam Long characterId) {
        Map<String, Object> imported = characterImporter.importCharacter(characterId);
        return ResponseEntity.ok(imported);
    }

    /**
     * Get a Captain's Log character.
     */
    @GetMapping("/characters/{characterId}")
    public ResponseEntity<CaptainLogCharacter> getCharacter(
            @PathVariable Long characterId) {
        CaptainLogCharacter character = characterImporter.getCharacter(characterId);
        return ResponseEntity.ok(character);
    }

    /**
     * Create a new SFB character.
     */
    @PostMapping("/characters/create")
    public ResponseEntity<Map<String, Object>> createCharacter(
            @RequestParam String name,
            @RequestParam String empire,
            @RequestParam String species,
            @RequestParam String division) {
        Map<String, Object> character = sfbCharacterCreator.createCharacter(name, empire, species, division);
        return ResponseEntity.ok(character);
    }

    /**
     * Get available empires.
     */
    @GetMapping("/empires")
    public ResponseEntity<List<String>> getAvailableEmpires() {
        List<String> empires = sfbCharacterCreator.getAvailableEmpires();
        return ResponseEntity.ok(empires);
    }

    /**
     * Get available species.
     */
    @GetMapping("/species")
    public ResponseEntity<List<String>> getAvailableSpecies() {
        List<String> species = sfbCharacterCreator.getAvailableSpecies();
        return ResponseEntity.ok(species);
    }

    /**
     * Get available divisions.
     */
    @GetMapping("/divisions")
    public ResponseEntity<List<String>> getAvailableDivisions() {
        List<String> divisions = sfbCharacterCreator.getAvailableDivisions();
        return ResponseEntity.ok(divisions);
    }

    /**
     * Get mission updates for a mission.
     */
    @GetMapping("/missions/{missionId}/updates")
    public ResponseEntity<List<MissionUpdate>> getMissionUpdates(
            @PathVariable Long missionId) {
        List<MissionUpdate> updates = missionUpdateService.getMissionUpdates(missionId);
        return ResponseEntity.ok(updates);
    }

    /**
     * Process pending mission updates.
     */
    @PostMapping("/missions/process-updates")
    public ResponseEntity<Map<String, Object>> processPendingUpdates() {
        int processed = missionUpdateService.processPendingUpdates();
        return ResponseEntity.ok(Map.of(
                "processed", processed,
                "message", processed + " mission updates processed"
        ));
    }

    /**
     * Trigger a 4x event from mission completion.
     */
    @PostMapping("/missions/trigger-event")
    public ResponseEntity<Map<String, Object>> trigger4xEvent(
            @RequestParam Long missionId,
            @RequestParam String eventType) {
        Map<String, Object> result = missionUpdateService.trigger4xEvent(missionId, eventType);
        return ResponseEntity.ok(result);
    }
}
