package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.fourx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the generic 4X framework.
 * <p>
 * Provides endpoints for all four 4X pillars: Exploration, Expansion,
 * Exploitation, and Extermination, plus Diplomacy and Character Creation.
 * Each endpoint delegates to the corresponding service interface, which
 * game modules implement with their own theme-specific logic.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/fourx")
public class FourxController {

    private static final Logger log = LoggerFactory.getLogger(FourxController.class);

    private final FourxExplorationService explorationService;
    private final FourxCombatService combatService;
    private final FourxDiplomacyService diplomacyService;
    private final FourxEconomyService economyService;
    private final FourxCharacterService characterService;

    public FourxController(FourxExplorationService explorationService,
                           FourxCombatService combatService,
                           FourxDiplomacyService diplomacyService,
                           FourxEconomyService economyService,
                           FourxCharacterService characterService) {
        this.explorationService = explorationService;
        this.combatService = combatService;
        this.diplomacyService = diplomacyService;
        this.economyService = economyService;
        this.characterService = characterService;
    }

    // =====================================================================
    // Exploration Endpoints
    // =====================================================================

    @PostMapping("/exploration/scan/{sectorId}")
    public ResponseEntity<List<Map<String, Object>>> scanSector(@PathVariable String sectorId) {
        log.debug("Scanning sector: {}", sectorId);
        List<Map<String, Object>> result = explorationService.scanSector(sectorId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exploration/systems")
    public ResponseEntity<List<Map<String, Object>>> getDiscoveredSystems() {
        return ResponseEntity.ok(explorationService.getDiscoveredSystems());
    }

    @GetMapping("/exploration/systems/{systemId}")
    public ResponseEntity<Map<String, Object>> getExplorationReport(@PathVariable String systemId) {
        return ResponseEntity.ok(explorationService.getExplorationReport(systemId));
    }

    @PostMapping("/exploration/systems/{systemId}/events")
    public ResponseEntity<List<String>> generateExplorationEvents(@PathVariable String systemId) {
        return ResponseEntity.ok(explorationService.generateExplorationEvents(systemId));
    }

    @GetMapping("/exploration/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam String from, @RequestParam String to) {
        int distance = explorationService.calculateDistance(from, to);
        return ResponseEntity.ok(Map.of("from", from, "to", to, "distance", distance));
    }

    @GetMapping("/exploration/sectors")
    public ResponseEntity<List<String>> getAllSectors() {
        return ResponseEntity.ok(explorationService.getAllSectors());
    }

    @GetMapping("/exploration/count")
    public ResponseEntity<Map<String, Integer>> getDiscoveredSystemCount() {
        return ResponseEntity.ok(Map.of("count", explorationService.getDiscoveredSystemCount()));
    }

    // =====================================================================
    // Combat Endpoints
    // =====================================================================

    @PostMapping("/combat/initiate")
    public ResponseEntity<Map<String, Object>> initiateCombat(
            @RequestParam String attackerFleetId, @RequestParam String defenderFleetId) {
        log.debug("Initiating combat: {} vs {}", attackerFleetId, defenderFleetId);
        return ResponseEntity.ok(combatService.initiateCombat(attackerFleetId, defenderFleetId));
    }

    @PostMapping("/combat/{combatId}/round")
    public ResponseEntity<Map<String, Object>> executeCombatRound(@PathVariable String combatId) {
        return ResponseEntity.ok(combatService.executeCombatRound(combatId));
    }

    @GetMapping("/combat/{combatId}")
    public ResponseEntity<Map<String, Object>> getCombatStatus(@PathVariable String combatId) {
        return ResponseEntity.ok(combatService.getCombatStatus(combatId));
    }

    @GetMapping("/combat/fleets/{fleetId}/power")
    public ResponseEntity<Map<String, Integer>> calculateCombatPower(@PathVariable String fleetId) {
        return ResponseEntity.ok(Map.of("combatPower", combatService.calculateCombatPower(fleetId)));
    }

    @GetMapping("/combat/active")
    public ResponseEntity<List<String>> getActiveCombats() {
        return ResponseEntity.ok(combatService.getActiveCombats());
    }

    @GetMapping("/combat/ship-types/{factionId}")
    public ResponseEntity<List<Map<String, Object>>> getShipTypes(@PathVariable String factionId) {
        return ResponseEntity.ok(combatService.getShipTypes(factionId));
    }

    @PostMapping("/combat/fleets")
    public ResponseEntity<Map<String, Object>> createFleet(
            @RequestParam String factionId, @RequestParam String fleetName,
            @RequestParam String shipTypeId, @RequestParam int count) {
        log.debug("Creating fleet: {} for faction {}", fleetName, factionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(combatService.createFleet(factionId, fleetName, shipTypeId, count));
    }

    @GetMapping("/combat/fleets/{factionId}")
    public ResponseEntity<List<Map<String, Object>>> getFleets(@PathVariable String factionId) {
        return ResponseEntity.ok(combatService.getFleets(factionId));
    }

    // =====================================================================
    // Diplomacy Endpoints
    // =====================================================================

    @GetMapping("/diplomacy/factions")
    public ResponseEntity<List<FourxFaction>> getAllFactions() {
        return ResponseEntity.ok(diplomacyService.getAllFactions());
    }

    @GetMapping("/diplomacy/factions/{factionId}")
    public ResponseEntity<FourxFaction> getFaction(@PathVariable String factionId) {
        FourxFaction faction = diplomacyService.getFaction(factionId);
        if (faction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faction);
    }

    @GetMapping("/diplomacy/{factionId}/relations")
    public ResponseEntity<List<FourxDiplomaticRelation>> getRelations(@PathVariable String factionId) {
        return ResponseEntity.ok(diplomacyService.getRelations(factionId));
    }

    @PostMapping("/diplomacy/relation")
    public ResponseEntity<FourxDiplomaticRelation> modifyRelation(
            @RequestParam String faction1Id, @RequestParam String faction2Id,
            @RequestParam int delta) {
        return ResponseEntity.ok(diplomacyService.modifyRelation(faction1Id, faction2Id, delta));
    }

    @PostMapping("/diplomacy/war")
    public ResponseEntity<FourxDiplomaticRelation> declareWar(
            @RequestParam String attackerId, @RequestParam String defenderId) {
        log.info("War declared: {} vs {}", attackerId, defenderId);
        return ResponseEntity.ok(diplomacyService.declareWar(attackerId, defenderId));
    }

    @PostMapping("/diplomacy/peace")
    public ResponseEntity<FourxDiplomaticRelation> establishPeace(
            @RequestParam String faction1Id, @RequestParam String faction2Id) {
        return ResponseEntity.ok(diplomacyService.establishPeace(faction1Id, faction2Id));
    }

    @PostMapping("/diplomacy/treaty")
    public ResponseEntity<FourxDiplomaticRelation> addTreaty(
            @RequestParam String faction1Id, @RequestParam String faction2Id,
            @RequestParam String treatyType) {
        return ResponseEntity.ok(diplomacyService.addTreaty(faction1Id, faction2Id, treatyType));
    }

    @DeleteMapping("/diplomacy/treaty")
    public ResponseEntity<FourxDiplomaticRelation> removeTreaty(
            @RequestParam String faction1Id, @RequestParam String faction2Id,
            @RequestParam String treatyType) {
        return ResponseEntity.ok(diplomacyService.removeTreaty(faction1Id, faction2Id, treatyType));
    }

    @GetMapping("/diplomacy/war/check")
    public ResponseEntity<Map<String, Boolean>> isAtWar(
            @RequestParam String faction1Id, @RequestParam String faction2Id) {
        return ResponseEntity.ok(Map.of("atWar", diplomacyService.isAtWar(faction1Id, faction2Id)));
    }

    @PostMapping("/diplomacy/events/{factionId}")
    public ResponseEntity<Map<String, Object>> generateDiplomaticEvent(@PathVariable String factionId) {
        return ResponseEntity.ok(diplomacyService.generateDiplomaticEvent(factionId));
    }

    // =====================================================================
    // Economy Endpoints
    // =====================================================================

    @GetMapping("/economy/{factionId}/summary")
    public ResponseEntity<Map<String, Object>> getEconomySummary(@PathVariable String factionId) {
        return ResponseEntity.ok(economyService.calculateEconomySummary(factionId));
    }

    @PostMapping("/economy/turn")
    public ResponseEntity<Void> processEconomyTurn() {
        log.info("Processing economy turn for all factions");
        economyService.processEconomyTurn();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/economy/{factionId}/colonies")
    public ResponseEntity<List<FourxColony>> getColonies(@PathVariable String factionId) {
        return ResponseEntity.ok(economyService.getColonies(factionId));
    }

    @GetMapping("/economy/colonies/{colonyId}")
    public ResponseEntity<FourxColony> getColony(@PathVariable String colonyId) {
        FourxColony colony = economyService.getColony(colonyId);
        if (colony == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(colony);
    }

    @PostMapping("/economy/colonies")
    public ResponseEntity<FourxColony> createColony(
            @RequestParam String factionId, @RequestParam String colonyName,
            @RequestParam String starSystemId) {
        log.debug("Creating colony: {} in system {} for faction {}", colonyName, starSystemId, factionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(economyService.createColony(factionId, colonyName, starSystemId));
    }

    @PostMapping("/economy/colonies/{colonyId}/buildings")
    public ResponseEntity<FourxColony> addBuilding(
            @PathVariable String colonyId, @RequestParam String building) {
        return ResponseEntity.ok(economyService.addBuilding(colonyId, building));
    }

    @GetMapping("/economy/{factionId}/trade-routes")
    public ResponseEntity<List<FourxTradeRoute>> getTradeRoutes(@PathVariable String factionId) {
        return ResponseEntity.ok(economyService.getTradeRoutes(factionId));
    }

    @PostMapping("/economy/trade-routes")
    public ResponseEntity<FourxTradeRoute> createTradeRoute(
            @RequestParam String factionId, @RequestParam String originSystemId,
            @RequestParam String destinationSystemId,
            @RequestParam FourxTradeRoute.CargoType cargoType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(economyService.createTradeRoute(factionId, originSystemId, destinationSystemId, cargoType));
    }

    @GetMapping("/economy/{factionId}/trade-profit")
    public ResponseEntity<Map<String, Integer>> getTradeProfit(@PathVariable String factionId) {
        return ResponseEntity.ok(Map.of("profit", economyService.getTradeProfit(factionId)));
    }

    @PutMapping("/economy/trade-routes/{tradeRouteId}/active")
    public ResponseEntity<Void> setTradeRouteActive(
            @PathVariable String tradeRouteId, @RequestParam boolean active) {
        economyService.setTradeRouteActive(tradeRouteId, active);
        return ResponseEntity.ok().build();
    }

    // =====================================================================
    // Character Creation Endpoints
    // =====================================================================

    @PostMapping("/characters")
    public ResponseEntity<Map<String, Object>> createCharacter(
            @RequestParam String name, @RequestParam String race) {
        log.debug("Creating character: {} (race: {})", name, race);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(characterService.createCharacter(name, race));
    }

    @PostMapping("/characters/lifepath")
    public ResponseEntity<Map<String, Object>> createCharacterWithLifepath(
            @RequestParam String name, @RequestParam String race, @RequestParam String career) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(characterService.createCharacterWithLifepath(name, race, career));
    }

    @GetMapping("/characters/races")
    public ResponseEntity<List<String>> getAvailableRaces() {
        return ResponseEntity.ok(characterService.getAvailableRaces());
    }

    @GetMapping("/characters/careers")
    public ResponseEntity<List<String>> getAvailableCareers() {
        return ResponseEntity.ok(characterService.getAvailableCareers());
    }

    @GetMapping("/characters/careers/{career}/skills")
    public ResponseEntity<List<String>> getAvailableSkills(@PathVariable String career) {
        return ResponseEntity.ok(characterService.getAvailableSkills(career));
    }

    @GetMapping("/characters/races/{race}/modifiers")
    public ResponseEntity<Map<String, Integer>> getRaceModifiers(@PathVariable String race) {
        return ResponseEntity.ok(characterService.getRaceModifiers(race));
    }

    @GetMapping("/characters/careers/{career}/career-skills")
    public ResponseEntity<List<Map<String, Object>>> getCareerSkills(@PathVariable String career) {
        return ResponseEntity.ok(characterService.getCareerSkills(career));
    }

    @PostMapping("/characters/advance")
    public ResponseEntity<Map<String, Object>> advanceCareer(
            @RequestBody Map<String, Object> character, @RequestParam String career) {
        return ResponseEntity.ok(characterService.advanceCareer(character, career));
    }

    @GetMapping("/characters/all")
    public ResponseEntity<List<Map<String, Object>>> getAllCharacters() {
        return ResponseEntity.ok(characterService.getAllCharacters());
    }
}
