package com.yisy.modules.doramcu.star_trek_4x.controller;

import com.yisy.modules.doramcu.star_trek_4x.diplomacy.DiplomacyService;
import com.yisy.modules.doramcu.star_trek_4x.diplomacy.TreatyService;
import com.yisy.modules.doramcu.star_trek_4x.model.DiplomaticRelation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for the Diplomacy pillar.
 * <p>
 * Provides endpoints for diplomatic actions, treaties, and faction relations.
 * </p>
 */
@RestController
@RequestMapping("/api/star-trek-4x/diplomacy")
public class DiplomacyController {

    private final DiplomacyService diplomacyService;
    private final TreatyService treatyService;

    public DiplomacyController(DiplomacyService diplomacyService, TreatyService treatyService) {
        this.diplomacyService = diplomacyService;
        this.treatyService = treatyService;
    }

    /**
     * Get all relations for a faction.
     */
    @GetMapping("/relations/{factionId}")
    public ResponseEntity<List<DiplomaticRelation>> getRelations(
            @PathVariable Long factionId) {
        List<DiplomaticRelation> relations = diplomacyService.getRelations(factionId);
        return ResponseEntity.ok(relations);
    }

    /**
     * Modify relation between two factions.
     */
    @PostMapping("/modify-relation")
    public ResponseEntity<DiplomaticRelation> modifyRelation(
            @RequestParam Long faction1Id,
            @RequestParam Long faction2Id,
            @RequestParam int delta) {
        DiplomaticRelation relation = diplomacyService.modifyRelation(faction1Id, faction2Id, delta);
        return ResponseEntity.ok(relation);
    }

    /**
     * Declare war on another faction.
     */
    @PostMapping("/declare-war")
    public ResponseEntity<DiplomaticRelation> declareWar(
            @RequestParam Long attackerId,
            @RequestParam Long defenderId) {
        DiplomaticRelation relation = diplomacyService.declareWar(attackerId, defenderId);
        return ResponseEntity.ok(relation);
    }

    /**
     * Establish peace with another faction.
     */
    @PostMapping("/establish-peace")
    public ResponseEntity<DiplomaticRelation> establishPeace(
            @RequestParam Long faction1Id,
            @RequestParam Long faction2Id) {
        DiplomaticRelation relation = diplomacyService.establishPeace(faction1Id, faction2Id);
        return ResponseEntity.ok(relation);
    }

    /**
     * Check if two factions are at war.
     */
    @GetMapping("/at-war")
    public ResponseEntity<Map<String, Object>> isAtWar(
            @RequestParam Long faction1Id,
            @RequestParam Long faction2Id) {
        boolean atWar = diplomacyService.isAtWar(faction1Id, faction2Id);
        return ResponseEntity.ok(Map.of("atWar", atWar));
    }

    /**
     * Generate a diplomatic event.
     */
    @PostMapping("/generate-event/{factionId}")
    public ResponseEntity<Map<String, Object>> generateDiplomaticEvent(
            @PathVariable Long factionId) {
        Map<String, Object> event = diplomacyService.generateDiplomaticEvent(factionId);
        return ResponseEntity.ok(event);
    }

    /**
     * Create a treaty.
     */
    @PostMapping("/treaty")
    public ResponseEntity<Map<String, Object>> createTreaty(
            @RequestParam Long faction1Id,
            @RequestParam Long faction2Id,
            @RequestParam String treatyType) {
        Map<String, Object> result = treatyService.createTreaty(faction1Id, faction2Id, treatyType);
        return ResponseEntity.ok(result);
    }

    /**
     * Break a treaty.
     */
    @PostMapping("/break-treaty")
    public ResponseEntity<Map<String, Object>> breakTreaty(
            @RequestParam Long faction1Id,
            @RequestParam Long faction2Id) {
        Map<String, Object> result = treatyService.breakTreaty(faction1Id, faction2Id);
        return ResponseEntity.ok(result);
    }
}
