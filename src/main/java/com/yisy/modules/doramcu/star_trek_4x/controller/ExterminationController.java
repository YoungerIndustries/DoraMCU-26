package com.yisy.modules.doramcu.star_trek_4x.controller;

import com.yisy.modules.doramcu.star_trek_4x.combat.*;
import com.yisy.modules.doramcu.star_trek_4x.model.PlanetaryBattle;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekGroundUnit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for the Extermination pillar.
 * <p>
 * Provides endpoints for fleet commands, battle initiation, planetary invasions,
 * and ground combat using Third Reich engine.
 * </p>
 */
@RestController
@RequestMapping("/api/star-trek-4x/extermination")
public class ExterminationController {

    private final FleetWarfareService fleetWarfareService;
    private final CombatBridgeService combatBridgeService;
    private final GroundCombatService groundCombatService;
    private final ConquestService conquestService;

    public ExterminationController(FleetWarfareService fleetWarfareService,
                                    CombatBridgeService combatBridgeService,
                                    GroundCombatService groundCombatService,
                                    ConquestService conquestService) {
        this.fleetWarfareService = fleetWarfareService;
        this.combatBridgeService = combatBridgeService;
        this.groundCombatService = groundCombatService;
        this.conquestService = conquestService;
    }

    /**
     * Deploy a fleet to a system.
     */
    @PostMapping("/fleet-deploy")
    public ResponseEntity<Map<String, Object>> deployFleet(
            @RequestParam Long factionId,
            @RequestParam Long systemId,
            @RequestParam int fleetSize) {
        Map<String, Object> result = fleetWarfareService.deployFleet(factionId, systemId, fleetSize);
        return ResponseEntity.ok(result);
    }

    /**
     * Move a fleet between systems.
     */
    @PostMapping("/fleet-move")
    public ResponseEntity<Map<String, Object>> moveFleet(
            @RequestParam Long factionId,
            @RequestParam Long fromSystemId,
            @RequestParam Long toSystemId,
            @RequestParam int fleetSize) {
        Map<String, Object> result = fleetWarfareService.moveFleet(factionId, fromSystemId, toSystemId, fleetSize);
        return ResponseEntity.ok(result);
    }

    /**
     * Engage an enemy fleet.
     */
    @PostMapping("/fleet-engage")
    public ResponseEntity<Map<String, Object>> engageFleet(
            @RequestParam Long attackerFactionId,
            @RequestParam Long defenderFactionId,
            @RequestParam Long systemId,
            @RequestParam int attackerFleetSize,
            @RequestParam int defenderFleetSize) {
        Map<String, Object> result = fleetWarfareService.engageFleet(
                attackerFactionId, defenderFactionId, systemId, attackerFleetSize, defenderFleetSize);
        return ResponseEntity.ok(result);
    }

    /**
     * Get fleet composition for a faction.
     */
    @GetMapping("/fleet-composition/{factionId}")
    public ResponseEntity<Map<String, Object>> getFleetComposition(
            @PathVariable Long factionId) {
        Map<String, Object> composition = fleetWarfareService.getFleetComposition(factionId);
        return ResponseEntity.ok(composition);
    }

    /**
     * Initiate tactical combat.
     */
    @PostMapping("/tactical-combat")
    public ResponseEntity<Map<String, Object>> initiateTacticalCombat(
            @RequestParam int attackerFleetSize,
            @RequestParam int defenderFleetSize,
            @RequestParam String systemName) {
        Map<String, Object> result = combatBridgeService.initiateTacticalCombat(
                attackerFleetSize, defenderFleetSize, systemName);
        return ResponseEntity.ok(result);
    }

    /**
     * Calculate combat odds.
     */
    @GetMapping("/combat-odds")
    public ResponseEntity<Map<String, Object>> calculateCombatOdds(
            @RequestParam int attackerFleetSize,
            @RequestParam int defenderFleetSize) {
        Map<String, Object> odds = combatBridgeService.calculateCombatOdds(attackerFleetSize, defenderFleetSize);
        return ResponseEntity.ok(odds);
    }

    /**
     * Initiate a planetary invasion.
     */
    @PostMapping("/planetary-invasion")
    public ResponseEntity<PlanetaryBattle> initiatePlanetaryInvasion(
            @RequestParam Long systemId,
            @RequestParam Long attackerFactionId,
            @RequestParam Long defenderFactionId) {
        PlanetaryBattle battle = groundCombatService.initiatePlanetaryInvasion(
                systemId, attackerFactionId, defenderFactionId);
        return ResponseEntity.ok(battle);
    }

    /**
     * Create a Star Trek ground unit.
     */
    @PostMapping("/ground-unit")
    public ResponseEntity<StarTrekGroundUnit> createGroundUnit(
            @RequestParam String name,
            @RequestParam StarTrekGroundUnit.UnitType unitType,
            @RequestParam Long characterId,
            @RequestParam Long ownerFactionId) {
        StarTrekGroundUnit unit = groundCombatService.createStarTrekGroundUnit(
                name, unitType, characterId, ownerFactionId);
        return ResponseEntity.ok(unit);
    }

    /**
     * Resolve a ground battle.
     */
    @PostMapping("/ground-battle/{battleId}")
    public ResponseEntity<PlanetaryBattle> resolveGroundBattle(
            @PathVariable Long battleId) {
        PlanetaryBattle result = groundCombatService.resolveGroundBattle(battleId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get battle result.
     */
    @GetMapping("/ground-battle/{battleId}/result")
    public ResponseEntity<PlanetaryBattle> getBattleResult(
            @PathVariable Long battleId) {
        PlanetaryBattle result = groundCombatService.getBattleResult(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found: " + battleId));
        return ResponseEntity.ok(result);
    }

    /**
     * Capture a system after battle.
     */
    @PostMapping("/capture-system")
    public ResponseEntity<Map<String, Object>> captureSystem(
            @RequestParam Long systemId,
            @RequestParam Long attackerFactionId,
            @RequestParam String battleOutcome) {
        Map<String, Object> result = conquestService.captureSystem(systemId, attackerFactionId, battleOutcome);
        return ResponseEntity.ok(result);
    }

    /**
     * Surrender a system.
     */
    @PostMapping("/surrender-system")
    public ResponseEntity<Map<String, Object>> surrenderSystem(
            @RequestParam Long systemId,
            @RequestParam Long defenderFactionId) {
        Map<String, Object> result = conquestService.surrenderSystem(systemId, defenderFactionId);
        return ResponseEntity.ok(result);
    }

    /**
     * Calculate conquest bonus.
     */
    @GetMapping("/conquest-bonus/{systemId}")
    public ResponseEntity<Map<String, Object>> calculateConquestBonus(
            @PathVariable Long systemId) {
        Map<String, Object> bonus = conquestService.calculateConquestBonus(systemId);
        return ResponseEntity.ok(bonus);
    }
}
