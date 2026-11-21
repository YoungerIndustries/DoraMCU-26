package com.yisy.modules.doramcu.starshiptroopers.controller;

import com.yisy.modules.doramcu.starshiptroopers.model.Scenario;
import com.yisy.modules.doramcu.starshiptroopers.model.SpecialEquipment;
import com.yisy.modules.doramcu.starshiptroopers.model.Unit;
import com.yisy.modules.doramcu.starshiptroopers.service.StarshipTroopersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API for querying Starship Troopers universe data.
 * <p>
 * Exposes all Starship Troopers rules data: Terran/Arachnid/Humanoid units,
 * scenarios, special equipment, combat tables, combat resolution, and turn sequence.
 * </p>
 * Endpoints at /api/v1/starshiptroopers
 */
@RestController
@RequestMapping("/api/v1/starshiptroopers")
public class StarshipTroopersRestController {

    private final StarshipTroopersService service;

    public StarshipTroopersRestController(StarshipTroopersService service) {
        this.service = service;
    }

    // ==========================================
    // Terran Units
    // ==========================================

    @GetMapping("/units/terran")
    public List<Unit> getTerranUnits() {
        return service.getTerranUnits();
    }

    @GetMapping("/units/terran/{id}")
    public ResponseEntity<Unit> getTerranUnit(@PathVariable String id) {
        Optional<Unit> unit = service.getTerranUnitById(id);
        return unit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/units/terran/combat")
    public List<Unit> getTerranCombatUnits() {
        return service.getTerranCombatUnits();
    }

    // ==========================================
    // Arachnid Units
    // ==========================================

    @GetMapping("/units/arachnid")
    public List<Unit> getArachnidUnits() {
        return service.getArachnidUnits();
    }

    @GetMapping("/units/arachnid/{id}")
    public ResponseEntity<Unit> getArachnidUnit(@PathVariable String id) {
        Optional<Unit> unit = service.getArachnidUnitById(id);
        return unit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/units/arachnid/victory-points/{vp}")
    public List<Unit> getArachnidUnitsByVictoryPoints(@PathVariable int vp) {
        return service.getArachnidUnitsByVictoryPoints(vp);
    }

    // ==========================================
    // Humanoid Units
    // ==========================================

    @GetMapping("/units/humanoid")
    public List<Unit> getHumanoidUnits() {
        return service.getHumanoidUnits();
    }

    @GetMapping("/units/humanoid/{id}")
    public ResponseEntity<Unit> getHumanoidUnit(@PathVariable String id) {
        Optional<Unit> unit = service.getHumanoidUnitById(id);
        return unit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // Scenarios
    // ==========================================

    @GetMapping("/scenarios")
    public List<Scenario> getAllScenarios() {
        return service.getScenarios();
    }

    @GetMapping("/scenarios/{id}")
    public ResponseEntity<Scenario> getScenario(@PathVariable int id) {
        Optional<Scenario> scenario = service.getScenarioById(id);
        return scenario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/scenarios/by-difficulty")
    public List<Scenario> getScenariosByDifficulty() {
        return service.getScenariosByDifficulty();
    }

    // ==========================================
    // Special Equipment
    // ==========================================

    @GetMapping("/equipment")
    public List<SpecialEquipment> getAllEquipment() {
        return service.getSpecialEquipment();
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<SpecialEquipment> getEquipment(@PathVariable String id) {
        Optional<SpecialEquipment> equipment = service.getEquipmentById(id);
        return equipment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/equipment/explosive")
    public List<SpecialEquipment> getExplosiveEquipment() {
        return service.getExplosiveEquipment();
    }

    // ==========================================
    // Combat Tables
    // ==========================================

    @GetMapping("/combat/terran-table")
    public Map<String, Map<String, String>> getTerranCombatTable() {
        return service.getTerranCombatTable();
    }

    @GetMapping("/combat/alien-table")
    public Map<String, Map<String, String>> getAlienCombatTable() {
        return service.getAlienCombatTable();
    }

    @GetMapping("/combat/odds-ratios")
    public List<String> getOddsRatios() {
        return service.getOddsRatios();
    }

    /**
     * Resolves a combat roll using the Terran attack table.
     *
     * @param oddsRatio the attack-to-defense odds ratio (e.g., "2:1")
     * @param dieRoll   the die roll result (1-6)
     * @return the combat outcome code (M, D, W, K, H)
     */
    @GetMapping("/combat/resolve/terran")
    public ResponseEntity<String> resolveTerranCombat(
            @RequestParam String oddsRatio,
            @RequestParam int dieRoll) {
        String outcome = service.resolveTerranCombat(oddsRatio, dieRoll);
        return ResponseEntity.ok(outcome);
    }

    /**
     * Resolves a combat roll using the Alien attack table.
     *
     * @param oddsRatio the attack-to-defense odds ratio (e.g., "2:1")
     * @param dieRoll   the die roll result (1-6)
     * @return the combat outcome code (M, D, W, K, H)
     */
    @GetMapping("/combat/resolve/alien")
    public ResponseEntity<String> resolveAlienCombat(
            @RequestParam String oddsRatio,
            @RequestParam int dieRoll) {
        String outcome = service.resolveAlienCombat(oddsRatio, dieRoll);
        return ResponseEntity.ok(outcome);
    }

    /**
     * Calculates the attack-to-defense odds ratio.
     *
     * @param attackStrength  attacker's total attack strength
     * @param defenseStrength defender's total defense strength
     * @return the odds ratio string (e.g., "2:1", "1:1")
     */
    @GetMapping("/combat/odds")
    public ResponseEntity<String> calculateOdds(
            @RequestParam int attackStrength,
            @RequestParam int defenseStrength) {
        String odds = service.calculateOddsRatio(attackStrength, defenseStrength);
        return ResponseEntity.ok(odds);
    }

    @GetMapping("/combat/outcomes")
    public Map<String, String> getCombatOutcomes() {
        return service.getCombatOutcomes();
    }

    // ==========================================
    // Turn Sequence
    // ==========================================

    @GetMapping("/turn/terran-phases")
    public List<String> getTerranTurnPhases() {
        return service.getTerranTurnPhases();
    }

    @GetMapping("/turn/arachnid-phases")
    public List<String> getArachnidTurnPhases() {
        return service.getArachnidTurnPhases();
    }
}
