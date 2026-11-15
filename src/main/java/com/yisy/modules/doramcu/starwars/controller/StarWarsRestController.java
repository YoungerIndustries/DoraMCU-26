package com.yisy.modules.doramcu.starwars.controller;

import com.yisy.modules.doramcu.starwars.model.*;
import com.yisy.modules.doramcu.starwars.service.StarWarsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API for querying Star Wars universe data.
 * <p>
 * Exposes all Star Wars rules data: factions, units (ground, starships, vehicles),
 * characters, scenarios, special abilities (Force powers), lore, and combat tables.
 * </p>
 * Endpoints at /api/v1/starwars
 */
@RestController
@RequestMapping("/api/v1/starwars")
public class StarWarsRestController {

    private final StarWarsService service;

    public StarWarsRestController(StarWarsService service) {
        this.service = service;
    }

    // ==========================================
    // Factions
    // ==========================================

    @GetMapping("/factions")
    public List<Faction> getAllFactions() {
        return service.getFactions();
    }

    @GetMapping("/factions/{id}")
    public ResponseEntity<Faction> getFaction(@PathVariable String id) {
        Optional<Faction> faction = service.getFactionById(id);
        return faction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/factions/{id}/allies")
    public List<Faction> getAlliesOf(@PathVariable String id) {
        return service.getAlliesOf(id);
    }

    // ==========================================
    // Units
    // ==========================================

    @GetMapping("/units")
    public List<Unit> getAllUnits() {
        return service.getAllUnits();
    }

    @GetMapping("/units/{id}")
    public ResponseEntity<Unit> getUnit(@PathVariable String id) {
        Optional<Unit> unit = service.getUnitById(id);
        return unit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/units/faction/{factionId}")
    public List<Unit> getUnitsByFaction(@PathVariable String factionId) {
        return service.getUnitsByFaction(factionId);
    }

    @GetMapping("/units/ground")
    public List<Unit> getGroundUnits() {
        return service.getGroundUnits();
    }

    @GetMapping("/units/starships")
    public List<Unit> getStarships() {
        return service.getStarships();
    }

    @GetMapping("/units/vehicles")
    public List<Unit> getGroundVehicles() {
        return service.getGroundVehicles();
    }

    @GetMapping("/units/combat")
    public List<Unit> getCombatUnits() {
        return service.getCombatUnits();
    }

    // ==========================================
    // Characters
    // ==========================================

    @GetMapping("/characters")
    public List<Unit> getAllCharacters() {
        return service.getCharacters();
    }

    @GetMapping("/characters/force-users")
    public List<Unit> getForceUsers() {
        return service.getForceUsers();
    }

    // ==========================================
    // Scenarios
    // ==========================================

    @GetMapping("/scenarios")
    public List<Scenario> getAllScenarios() {
        return service.getScenarios();
    }

    @GetMapping("/scenarios/{id}")
    public ResponseEntity<Scenario> getScenario(@PathVariable String id) {
        Optional<Scenario> scenario = service.getScenarioById(id);
        return scenario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/scenarios/by-difficulty")
    public List<Scenario> getScenariosByDifficulty() {
        return service.getScenariosByDifficulty();
    }

    @GetMapping("/scenarios/era/{era}")
    public List<Scenario> getScenariosByEra(@PathVariable String era) {
        return service.getScenariosByEra(era);
    }

    @GetMapping("/scenarios/faction/{faction}")
    public List<Scenario> getScenariosByFaction(@PathVariable String faction) {
        return service.getScenariosByFaction(faction);
    }

    /**
     * Returns detailed setup information for a scenario.
     *
     * @param id the scenario ID
     * @return a map with rebelForces, imperialForces, rebelObjective, imperialObjective,
     *         victoryConditions, map, players, playtime, and sourceGame
     */
    @GetMapping("/scenarios/{id}/setup")
    public ResponseEntity<Map<String, Object>> getScenarioSetup(@PathVariable String id) {
        Optional<Scenario> scenario = service.getScenarioById(id);
        if (scenario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Scenario s = scenario.get();
        Map<String, Object> setup = new java.util.LinkedHashMap<>();
        setup.put("id", s.id());
        setup.put("name", s.name());
        setup.put("map", s.map());
        setup.put("players", s.players());
        setup.put("playtime", s.playtime());
        setup.put("sourceGame", s.sourceGame());
        setup.put("player1Faction", s.player1Faction());
        setup.put("player2Faction", s.player2Faction());
        setup.put("rebelObjective", s.rebelObjective());
        setup.put("imperialObjective", s.imperialObjective());
        setup.put("rebelForces", s.rebelForces());
        setup.put("imperialForces", s.imperialForces());
        setup.put("victoryConditions", s.victoryConditions());
        return ResponseEntity.ok(setup);
    }

    // ==========================================
    // Special Abilities (Force Powers, Leader Abilities)
    // ==========================================

    @GetMapping("/abilities")
    public List<SpecialAbility> getAllAbilities() {
        return service.getSpecialAbilities();
    }

    @GetMapping("/abilities/{id}")
    public ResponseEntity<SpecialAbility> getAbility(@PathVariable String id) {
        Optional<SpecialAbility> ability = service.getAbilityById(id);
        return ability.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/abilities/light-side")
    public List<SpecialAbility> getLightSidePowers() {
        return service.getLightSidePowers();
    }

    @GetMapping("/abilities/dark-side")
    public List<SpecialAbility> getDarkSidePowers() {
        return service.getDarkSidePowers();
    }

    @GetMapping("/abilities/leader")
    public List<SpecialAbility> getLeaderAbilities() {
        return service.getLeaderAbilities();
    }

    @GetMapping("/abilities/leader/{faction}")
    public List<SpecialAbility> getLeaderAbilitiesByFaction(@PathVariable String faction) {
        return service.getLeaderAbilitiesByFaction(faction);
    }

    @GetMapping("/abilities/keywords")
    public List<SpecialAbility> getUnitKeywords() {
        return service.getUnitKeywords();
    }

    // ==========================================
    // Lore (Planets, Eras, Species, Events)
    // ==========================================

    @GetMapping("/lore")
    public List<Lore> getAllLore() {
        return service.getLore();
    }

    @GetMapping("/lore/{category}")
    public List<Lore> getLoreByCategory(@PathVariable String category) {
        return service.getLoreByCategory(category);
    }

    @GetMapping("/lore/planets")
    public List<Lore> getPlanets() {
        return service.getPlanets();
    }

    @GetMapping("/lore/eras")
    public List<Lore> getEras() {
        return service.getEras();
    }

    @GetMapping("/lore/species")
    public List<Lore> getSpecies() {
        return service.getSpecies();
    }

    @GetMapping("/lore/events")
    public List<Lore> getEvents() {
        return service.getEvents();
    }

    @GetMapping("/lore/force")
    public List<Lore> getForceInfo() {
        return service.getForceInfo();
    }

    @GetMapping("/lore/lightsabers")
    public List<Lore> getLightsaberInfo() {
        return service.getLightsaberInfo();
    }

    // ==========================================
    // Combat Tables
    // ==========================================

    @GetMapping("/combat/ground-table")
    public Map<String, Map<String, String>> getGroundCombatTable() {
        return service.getGroundCombatTable();
    }

    @GetMapping("/combat/space-table")
    public Map<String, Map<String, String>> getSpaceCombatTable() {
        return service.getSpaceCombatTable();
    }

    @GetMapping("/combat/outcomes")
    public Map<String, String> getCombatOutcomes() {
        return service.getCombatOutcomes();
    }

    /**
     * Resolves a ground combat roll using the ground combat table.
     *
     * @param oddsRatio the attacker-to-defender odds ratio (e.g., "2:1", "1:1")
     * @param dieRoll   the die roll result (2-12 for 2d6)
     * @return the combat outcome string
     */
    @GetMapping("/combat/resolve/ground")
    public ResponseEntity<String> resolveGroundCombat(
            @RequestParam String oddsRatio,
            @RequestParam int dieRoll) {
        String outcome = service.resolveGroundCombat(oddsRatio, dieRoll);
        return ResponseEntity.ok(outcome);
    }

    /**
     * Resolves a space combat roll using the space combat table.
     *
     * @param oddsRatio the attacker-to-defender odds ratio (e.g., "2:1", "1:1")
     * @param dieRoll   the die roll result (2-12 for 2d6)
     * @return the combat outcome string
     */
    @GetMapping("/combat/resolve/space")
    public ResponseEntity<String> resolveSpaceCombat(
            @RequestParam String oddsRatio,
            @RequestParam int dieRoll) {
        String outcome = service.resolveSpaceCombat(oddsRatio, dieRoll);
        return ResponseEntity.ok(outcome);
    }

    /**
     * Calculates the attack-to-defense odds ratio.
     *
     * @param attackerAttack attacker's attack stat
     * @param defenderDefense defender's defense stat
     * @return the odds ratio string (e.g., "2:1", "1:1", "1:2")
     */
    @GetMapping("/combat/odds")
    public ResponseEntity<String> calculateOdds(
            @RequestParam int attackerAttack,
            @RequestParam int defenderDefense) {
        String odds = service.calculateOddsRatio(attackerAttack, defenderDefense);
        return ResponseEntity.ok(odds);
    }
}
