package com.yisy.modules.doramcu.sfb.controller;

import com.yisy.modules.doramcu.sfb.dto.*;
import com.yisy.modules.doramcu.sfb.model.SfbCharacterAssignment;
import com.yisy.modules.doramcu.sfb.model.SfbRole;
import com.yisy.modules.doramcu.sfb.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for SFB scenario setup, ship selection, and campaign management.
 */
@RestController
@RequestMapping("/api/v1/sfb")
public class SfbRestController {

    private final SfbCombatEngine combatEngine;
    private final SfbCampaignEngine campaignEngine;
    private final SfbCharacterBridge characterBridge;
    private final SfbRoleSystem roleSystem;
    private final SfbGalaxyIntegration galaxyIntegration;

    public SfbRestController() {
        this.combatEngine = new SfbCombatEngine();
        this.campaignEngine = new SfbCampaignEngine();
        this.characterBridge = new SfbCharacterBridge();
        this.roleSystem = new SfbRoleSystem(characterBridge);
        this.galaxyIntegration = new SfbGalaxyIntegration();
    }

    @PostMapping("/battles")
    public ResponseEntity<Map<String, String>> createBattle(@RequestBody Map<String, String> request) {
        String battleId = request.getOrDefault("battleId", "battle-" + System.currentTimeMillis());
        return ResponseEntity.ok(Map.of("battleId", battleId, "status", "created"));
    }

    @PostMapping("/battles/{battleId}/assign-character")
    public ResponseEntity<SfbCharacterAssignment> assignCharacter(
            @PathVariable String battleId,
            @RequestBody SfbCharacterAssignmentRequest request) {
        SfbCharacterAssignment assignment = characterBridge.assignCharacterToBattle(
                battleId, request.getCharacterId(), request.getRole(),
                request.getShipId(), request.getStation());
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/battles/{battleId}/crew")
    public ResponseEntity<List<SfbCharacterAssignment>> getCrew(@PathVariable String battleId) {
        return ResponseEntity.ok(characterBridge.getBattleAssignments(battleId));
    }

    @GetMapping("/characters/{characterId}/modifiers")
    public ResponseEntity<SfbCharacterModifiers> getModifiers(
            @PathVariable Long characterId,
            @RequestParam SfbRole role) {
        SfbCharacterModifiers modifiers = roleSystem.getCharacterModifiers(characterId, role, null);
        if (modifiers == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(modifiers);
    }

    @PostMapping("/battles/{battleId}/location")
    public ResponseEntity<Map<String, Object>> setBattleLocation(
            @PathVariable String battleId,
            @RequestBody SfbBattleLocationRequest request) {
        var location = galaxyIntegration.setBattleLocation(
                battleId, request.getGalaxyId(),
                request.getSectorRow(), request.getSectorCol(),
                request.getSystemName());
        return ResponseEntity.ok(Map.of(
                "battleId", battleId,
                "sectorRow", location.getSectorRow(),
                "sectorCol", location.getSectorCol(),
                "terrainEffect", location.getTerrainEffect().name()));
    }

    @GetMapping("/galaxy/sectors/{sectorId}/battles")
    public ResponseEntity<List<String>> getBattlesInSector(@PathVariable String sectorId) {
        String[] parts = sectorId.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return ResponseEntity.ok(galaxyIntegration.getBattlesInSector(row, col));
    }
}
