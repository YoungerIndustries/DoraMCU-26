package com.yisy.modules.doramcu.sfb.controller;

import com.yisy.modules.doramcu.sfb.dto.SfbActionRequest;
import com.yisy.modules.doramcu.sfb.dto.SfbBattleLocationRequest;
import com.yisy.modules.doramcu.sfb.dto.SfbBuildShipRequest;
import com.yisy.modules.doramcu.sfb.dto.SfbCampaignStatusResponse;
import com.yisy.modules.doramcu.sfb.dto.SfbCharacterAssignmentRequest;
import com.yisy.modules.doramcu.sfb.dto.SfbCharacterModifiers;
import com.yisy.modules.doramcu.sfb.dto.SfbFleetRequest;
import com.yisy.modules.doramcu.sfb.dto.SfbRepairRequest;
import com.yisy.modules.doramcu.sfb.dto.SfbShipSnapshot;
import com.yisy.modules.doramcu.sfb.model.SfbBattleOutcome;
import com.yisy.modules.doramcu.sfb.model.SfbCharacterAssignment;
import com.yisy.modules.doramcu.sfb.model.SfbCrewStationType;
import com.yisy.modules.doramcu.sfb.model.SfbRole;
import com.yisy.modules.doramcu.sfb.model.SfbShip;
import com.yisy.modules.doramcu.sfb.service.SfbCampaignAIService;
import com.yisy.modules.doramcu.sfb.service.SfbCampaignEngine;
import com.yisy.modules.doramcu.sfb.service.SfbCharacterBridge;
import com.yisy.modules.doramcu.sfb.service.SfbCombatEngine;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService;
import com.yisy.modules.doramcu.sfb.service.SfbGalaxyIntegration;
import com.yisy.modules.doramcu.sfb.service.SfbRoleSystem;
import com.yisy.modules.doramcu.sfb.service.SfbTerrainMonsterService;
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
    private final SfbTerrainMonsterService terrainMonsterService;
    private final SfbDiplomacyService diplomacyService;
    private final SfbEconomyService economyService;
    private final SfbCampaignAIService campaignAIService;

    public SfbRestController() {
        this.combatEngine = new SfbCombatEngine();
        this.campaignEngine = new SfbCampaignEngine();
        this.characterBridge = new SfbCharacterBridge();
        this.roleSystem = new SfbRoleSystem(characterBridge);
        this.galaxyIntegration = new SfbGalaxyIntegration();
        this.terrainMonsterService = new SfbTerrainMonsterService();
        this.diplomacyService = new SfbDiplomacyService();
        this.economyService = new SfbEconomyService();
        this.campaignAIService = new SfbCampaignAIService(diplomacyService, economyService);
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

    // === Campaign Engine Endpoints ===

    @PostMapping("/campaign/fleets")
    public ResponseEntity<Map<String, String>> createFleet(@RequestBody SfbFleetRequest request) {
        campaignEngine.createFleet(request.getId(), request.getName(), request.getOwnerSide());
        return ResponseEntity.ok(Map.of(
                "fleetId", request.getId(),
                "name", request.getName(),
                "status", "created"));
    }

    @GetMapping("/campaign/fleets")
    public ResponseEntity<List<Map<String, Object>>> getAllFleets() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (SfbCampaignEngine.Fleet fleet : campaignEngine.getFleets(0)) {
            result.add(fleetToMap(fleet));
        }
        for (SfbCampaignEngine.Fleet fleet : campaignEngine.getFleets(1)) {
            result.add(fleetToMap(fleet));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/campaign/fleets/{fleetId}")
    public ResponseEntity<Map<String, Object>> getFleet(@PathVariable String fleetId) {
        SfbCampaignEngine.Fleet fleet = campaignEngine.getFleet(fleetId);
        if (fleet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fleetToMap(fleet));
    }

    @PostMapping("/campaign/fleets/{fleetId}/deploy")
    public ResponseEntity<Map<String, String>> deployFleet(
            @PathVariable String fleetId,
            @RequestBody Map<String, Integer> request) {
        campaignEngine.deployFleet(fleetId, request.get("sectorRow"), request.get("sectorCol"));
        return ResponseEntity.ok(Map.of("fleetId", fleetId, "status", "deployed"));
    }

    @PostMapping("/campaign/ships/build")
    public ResponseEntity<Map<String, String>> buildShip(@RequestBody SfbBuildShipRequest request) {
        com.yisy.modules.doramcu.sfb.model.SfbShip ship = campaignEngine.buildShip(request.getFleetId(), request.getEmpire(), request.getShipClass());
        return ResponseEntity.ok(Map.of(
                "shipId", ship.getId(),
                "shipClass", ship.getShipClass(),
                "status", "built"));
    }

    @PostMapping("/campaign/fleets/{fleetId}/repair")
    public ResponseEntity<Map<String, String>> repairFleet(
            @PathVariable String fleetId,
            @RequestBody(required = false) SfbRepairRequest request) {
        int dockyardBonus = (request != null) ? request.getDockyardBonus() : 0;
        campaignEngine.repairFleet(fleetId, dockyardBonus);
        return ResponseEntity.ok(Map.of("fleetId", fleetId, "status", "repaired"));
    }

    @GetMapping("/campaign/fleets/{fleet1Id}/can-engage/{fleet2Id}")
    public ResponseEntity<Map<String, Boolean>> canEngage(
            @PathVariable String fleet1Id,
            @PathVariable String fleet2Id) {
        boolean canEngage = campaignEngine.canEngage(fleet1Id, fleet2Id);
        return ResponseEntity.ok(Map.of("canEngage", canEngage));
    }

    @PostMapping("/campaign/fleets/{fleet1Id}/engage/{fleet2Id}")
    public ResponseEntity<Map<String, String>> startBattle(
            @PathVariable String fleet1Id,
            @PathVariable String fleet2Id) {
        String battleId = "battle-" + System.currentTimeMillis();
        campaignEngine.startBattle(battleId, fleet1Id, fleet2Id);
        return ResponseEntity.ok(Map.of("battleId", battleId, "status", "started"));
    }

    @PostMapping("/campaign/battles/{battleId}/complete")
    public ResponseEntity<Map<String, String>> completeBattle(
            @PathVariable String battleId,
            @RequestBody Map<String, Integer> request) {
        int winningSide = request.getOrDefault("winningSide", -1);
        campaignEngine.completeBattle(battleId, winningSide);
        return ResponseEntity.ok(Map.of("battleId", battleId, "status", "completed"));
    }

    @GetMapping("/campaign/status")
    public ResponseEntity<SfbCampaignStatusResponse> getCampaignStatus() {
        SfbCampaignStatusResponse response = new SfbCampaignStatusResponse(
                campaignEngine.getFleetCount(),
                campaignEngine.getActiveBattleCount(),
                campaignEngine.getShipyard().size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaign/shipyard")
    public ResponseEntity<List<SfbShipSnapshot>> getShipyard() {
        List<SfbShipSnapshot> snapshots = new java.util.ArrayList<>();
        for (com.yisy.modules.doramcu.sfb.model.SfbShip ship : campaignEngine.getShipyard().values()) {
            SfbShipSnapshot snapshot = new SfbShipSnapshot();
            snapshot.setId(ship.getId());
            snapshot.setName(ship.getShipClass());
            snapshot.setSide(ship.getSide());
            snapshot.setDestroyed(ship.isDestroyed());
            snapshot.setCrippled(ship.isCrippled());
            snapshot.setHullIntegrity(ship.getSsd().getExcessDamage());
            snapshots.add(snapshot);
        }
        return ResponseEntity.ok(snapshots);
    }

    // === Campaign History Endpoints ===

    @GetMapping("/campaign/history")
    public ResponseEntity<List<Map<String, Object>>> getCampaignHistory() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (SfbBattleOutcome outcome : campaignEngine.getCampaignHistory().getAllOutcomes()) {
            result.add(outcomeToMap(outcome));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/campaign/history/{battleId}")
    public ResponseEntity<Map<String, Object>> getBattleOutcome(@PathVariable String battleId) {
        SfbBattleOutcome outcome = campaignEngine.getCampaignHistory().getOutcome(battleId);
        if (outcome == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(outcomeToMap(outcome));
    }

    @GetMapping("/campaign/history/side/{side}")
    public ResponseEntity<List<Map<String, Object>>> getHistoryForSide(@PathVariable int side) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (SfbBattleOutcome outcome : campaignEngine.getCampaignHistory().getOutcomesForSide(side)) {
            result.add(outcomeToMap(outcome));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/campaign/statistics/side/{side}")
    public ResponseEntity<Map<String, Integer>> getSideStatistics(@PathVariable int side) {
        return ResponseEntity.ok(campaignEngine.getCampaignHistory().getSideStatistics(side));
    }

    // === Fleet Composition Endpoints ===

    @GetMapping("/campaign/fleets/{fleetId}/composition")
    public ResponseEntity<Map<String, Integer>> getFleetComposition(@PathVariable String fleetId) {
        return ResponseEntity.ok(campaignEngine.getFleetComposition(fleetId));
    }

    @GetMapping("/campaign/fleets/composition/side/{side}")
    public ResponseEntity<Map<String, Map<String, Integer>>> getAllFleetCompositions(@PathVariable int side) {
        return ResponseEntity.ok(campaignEngine.getAllFleetCompositions(side));
    }

    @PostMapping("/campaign/fleets/transfer")
    public ResponseEntity<Map<String, Integer>> transferShips(@RequestBody SfbTransferRequest request) {
        int transferred = campaignEngine.transferShips(
                request.getSourceFleetId(), request.getTargetFleetId(), request.getShipIds());
        return ResponseEntity.ok(Map.of("transferred", transferred));
    }

    @PostMapping("/campaign/fleets/{fleetId}/split")
    public ResponseEntity<Map<String, String>> splitFleet(
            @PathVariable String fleetId,
            @RequestBody SfbSplitFleetRequest request) {
        SfbCampaignEngine.Fleet newFleet = campaignEngine.splitFleet(
                fleetId, request.getNewFleetId(), request.getNewFleetName());
        return ResponseEntity.ok(Map.of("newFleetId", newFleet.getId(), "status", "split"));
    }

    @PostMapping("/campaign/fleets/{primaryFleetId}/merge/{secondaryFleetId}")
    public ResponseEntity<Map<String, String>> mergeFleets(
            @PathVariable String primaryFleetId,
            @PathVariable String secondaryFleetId) {
        SfbCampaignEngine.Fleet merged = campaignEngine.mergeFleets(primaryFleetId, secondaryFleetId);
        return ResponseEntity.ok(Map.of("mergedFleetId", merged.getId(), "status", "merged"));
    }

    private Map<String, Object> fleetToMap(SfbCampaignEngine.Fleet fleet) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", fleet.getId());
        map.put("name", fleet.getName());
        map.put("ownerSide", fleet.getOwnerSide());
        map.put("sectorRow", fleet.getSectorRow());
        map.put("sectorCol", fleet.getSectorCol());
        map.put("inDockyard", fleet.isInDockyard());
        map.put("shipIds", fleet.getShipIds());
        map.put("shipCount", fleet.getShipCount());
        return map;
    }

    private Map<String, Object> outcomeToMap(SfbBattleOutcome outcome) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("battleId", outcome.getBattleId());
        map.put("result", outcome.getResult().name());
        map.put("winningSide", outcome.getWinningSide());
        map.put("timestamp", outcome.getTimestamp().toString());
        map.put("sectorRow", outcome.getSectorRow());
        map.put("sectorCol", outcome.getSectorCol());
        map.put("terrainEffect", outcome.getTerrainEffect());
        map.put("destroyedShipIds", outcome.getDestroyedShipIds());
        map.put("shipsLostSide0", outcome.getShipsLostSide0());
        map.put("shipsLostSide1", outcome.getShipsLostSide1());
        return map;
    }

    // === Terrain Monsters (Phase 3) ===

    @GetMapping("/terrain-monsters/types")
    public ResponseEntity<List<Map<String, Object>>> getMonsterTypes() {
        return ResponseEntity.ok(terrainMonsterService.listMonsterTypes());
    }

    @GetMapping("/terrain-monsters/terrain-types")
    public ResponseEntity<List<Map<String, Object>>> getTerrainTypes() {
        return ResponseEntity.ok(terrainMonsterService.listTerrainTypes());
    }

    @PostMapping("/terrain-monsters/{battleId}/spawn")
    public ResponseEntity<Map<String, Object>> spawnMonster(
            @PathVariable String battleId,
            @RequestBody Map<String, String> request) {
        String terrain = request.getOrDefault("terrain", "NORMAL_SPACE");
        int q = parseIntOrDefault(request.get("q"), 5);
        int r = parseIntOrDefault(request.get("r"), 5);
        SfbTerrainMonsterService.TerrainType terrainType;
        try {
            terrainType = SfbTerrainMonsterService.TerrainType.valueOf(terrain);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid terrain type: " + terrain));
        }
        com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster monster =
                terrainMonsterService.spawnMonster(battleId, terrainType, q, r);
        if (monster == null) {
            return ResponseEntity.ok(Map.of("encountered", false, "terrain", terrain));
        }
        return ResponseEntity.ok(monsterToMap(monster));
    }

    @PostMapping("/terrain-monsters/{battleId}/spawn-specific")
    public ResponseEntity<Map<String, Object>> spawnSpecificMonster(
            @PathVariable String battleId,
            @RequestBody Map<String, String> request) {
        String type = request.getOrDefault("type", "SPACE_AMOEBA");
        int q = parseIntOrDefault(request.get("q"), 5);
        int r = parseIntOrDefault(request.get("r"), 5);
        com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster.MonsterType monsterType;
        try {
            monsterType = com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster.MonsterType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid monster type: " + type));
        }
        com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster monster =
                terrainMonsterService.spawnSpecificType(battleId, monsterType, q, r);
        return ResponseEntity.ok(monsterToMap(monster));
    }

    @GetMapping("/terrain-monsters/{battleId}")
    public ResponseEntity<List<Map<String, Object>>> getBattleMonsters(@PathVariable String battleId) {
        List<com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster> monsters =
                terrainMonsterService.getBattleMonsters(battleId);
        return ResponseEntity.ok(monsters.stream().map(this::monsterToMap).collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/terrain-monsters/{battleId}/{monsterId}")
    public ResponseEntity<Map<String, Object>> getMonster(
            @PathVariable String battleId,
            @PathVariable String monsterId) {
        return terrainMonsterService.getMonster(battleId, monsterId)
                .map(m -> ResponseEntity.ok(monsterToMap(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/terrain-monsters/{battleId}/{monsterId}/attack-ship")
    public ResponseEntity<Map<String, Object>> monsterAttacksShip(
            @PathVariable String battleId,
            @PathVariable String monsterId,
            @RequestBody Map<String, Integer> request) {
        int shipDefense = request.getOrDefault("shipDefense", 0);
        int impulse = request.getOrDefault("impulse", 1);
        com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster monster =
                terrainMonsterService.getMonster(battleId, monsterId).orElse(null);
        if (monster == null) {
            return ResponseEntity.notFound().build();
        }
        SfbTerrainMonsterService.EncounterResult result =
                terrainMonsterService.resolveMonsterAttack(monster, shipDefense, impulse);
        return ResponseEntity.ok(encounterResultToMap(result));
    }

    @PostMapping("/terrain-monsters/{battleId}/{monsterId}/ship-attacks")
    public ResponseEntity<Map<String, Object>> shipAttacksMonster(
            @PathVariable String battleId,
            @PathVariable String monsterId,
            @RequestBody Map<String, Integer> request) {
        int weaponDamage = request.getOrDefault("weaponDamage", 5);
        com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster monster =
                terrainMonsterService.getMonster(battleId, monsterId).orElse(null);
        if (monster == null) {
            return ResponseEntity.notFound().build();
        }
        SfbTerrainMonsterService.EncounterResult result =
                terrainMonsterService.shipAttacksMonster(monster, weaponDamage);
        return ResponseEntity.ok(encounterResultToMap(result));
    }

    @DeleteMapping("/terrain-monsters/{battleId}/{monsterId}")
    public ResponseEntity<Map<String, String>> removeMonster(
            @PathVariable String battleId,
            @PathVariable String monsterId) {
        boolean removed = terrainMonsterService.removeMonster(battleId, monsterId);
        if (removed) {
            return ResponseEntity.ok(Map.of("status", "removed", "monsterId", monsterId));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/terrain-monsters/{battleId}")
    public ResponseEntity<Map<String, String>> clearBattleMonsters(@PathVariable String battleId) {
        terrainMonsterService.clearBattleMonsters(battleId);
        return ResponseEntity.ok(Map.of("status", "cleared", "battleId", battleId));
    }

    @GetMapping("/terrain-monsters/{battleId}/statistics")
    public ResponseEntity<Map<String, Object>> getEncounterStatistics(@PathVariable String battleId) {
        return ResponseEntity.ok(terrainMonsterService.getEncounterStatistics(battleId));
    }

    @PostMapping("/terrain-monsters/{battleId}/move-to")
    public ResponseEntity<Map<String, String>> moveMonsters(
            @PathVariable String battleId,
            @RequestBody Map<String, String> request) {
        int q = parseIntOrDefault(request.get("q"), 0);
        int r = parseIntOrDefault(request.get("r"), 0);
        terrainMonsterService.moveAllMonsters(battleId, q, r);
        return ResponseEntity.ok(Map.of("status", "moved", "battleId", battleId));
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // === Terrain Monster Helpers ===

    private Map<String, Object> monsterToMap(com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster m) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", m.getId());
        map.put("type", m.getType().name());
        map.put("name", m.getName());
        map.put("sizeClass", m.getSizeClass().name());
        map.put("hitPoints", m.getHitPoints());
        map.put("hitPointsRemaining", m.getHitPointsRemaining());
        map.put("attackPower", m.getAttackPower());
        map.put("defense", m.getDefense());
        map.put("movementSpeed", m.getMovementSpeed());
        map.put("attackRange", m.getAttackRange());
        map.put("specialAbility", m.getSpecialAbility());
        map.put("abilities", m.getAbilities());
        map.put("active", m.isActive());
        map.put("position", m.getPosition() != null ?
                Map.of("q", m.getPosition().getQ(), "r", m.getPosition().getR()) : null);
        return map;
    }

    private Map<String, Object> encounterResultToMap(SfbTerrainMonsterService.EncounterResult result) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("monsterId", result.getMonsterId());
        map.put("monsterName", result.getMonsterName());
        map.put("monsterType", result.getMonsterType() != null ? result.getMonsterType().name() : null);
        map.put("damageDealt", result.getDamageDealt());
        map.put("abilityUsed", result.getAbilityUsed());
        map.put("monsterAlive", result.isMonsterAlive());
        map.put("monsterHpRemaining", result.getMonsterHpRemaining());
        map.put("details", result.getDetails());
        return map;
    }

    // === Campaign AI: Diplomacy ===

    @PostMapping("/diplomacy/factions/{name}")
    public ResponseEntity<Map<String, String>> registerFaction(@PathVariable String name,
            @RequestBody(required = false) Map<String, Integer> traits) {
        diplomacyService.registerFaction(name);
        return ResponseEntity.ok(Map.of("faction", name, "status", "registered"));
    }

    @GetMapping("/diplomacy/factions")
    public ResponseEntity<List<String>> listFactions() {
        return ResponseEntity.ok(diplomacyService.getAllFactions());
    }

    @GetMapping("/diplomacy/relation/{factionA}/{factionB}")
    public ResponseEntity<Map<String, Object>> getRelation(@PathVariable String factionA,
            @PathVariable String factionB) {
        return ResponseEntity.ok(Map.of(
                "factionA", factionA,
                "factionB", factionB,
                "score", diplomacyService.getRelationScore(factionA, factionB),
                "type", diplomacyService.getRelationType(factionA, factionB).name()
        ));
    }

    @PostMapping("/diplomacy/action")
    public ResponseEntity<Map<String, Object>> performDiplomaticAction(@RequestBody Map<String, String> request) {
        String factionA = request.get("factionA");
        String factionB = request.get("factionB");
        String action = request.get("action");
        SfbDiplomacyService.DiplomaticAction actionType = SfbDiplomacyService.DiplomaticAction.valueOf(action);
        SfbDiplomacyService.DiplomaticResult result = diplomacyService.performAction(factionA, factionB, actionType);
        return ResponseEntity.ok(Map.of(
                "action", result.getActionName(),
                "success", result.isSuccess(),
                "relationBefore", result.getRelationBefore(),
                "relationAfter", result.getRelationAfter(),
                "message", result.getMessage()
        ));
    }

    @PostMapping("/diplomacy/war/{aggressor}/{target}")
    public ResponseEntity<Map<String, String>> declareWar(@PathVariable String aggressor,
            @PathVariable String target) {
        diplomacyService.declareWar(aggressor, target);
        return ResponseEntity.ok(Map.of("status", "war_declared", "aggressor", aggressor, "target", target));
    }

    @PostMapping("/diplomacy/treaty")
    public ResponseEntity<Map<String, Object>> createTreaty(@RequestBody Map<String, Object> request) {
        String factionA = (String) request.get("factionA");
        String factionB = (String) request.get("factionB");
        String type = (String) request.get("type");
        int turns = (int) request.getOrDefault("turns", 10);
        SfbDiplomacyService.Treaty treaty = diplomacyService.createTreaty(
                factionA, factionB, SfbDiplomacyService.TreatyType.valueOf(type), turns);
        return ResponseEntity.ok(Map.of("treatyId", treaty.getId(), "type", treaty.getType().getName()));
    }

    @GetMapping("/diplomacy/treaties")
    public ResponseEntity<List<Map<String, Object>>> listTreaties() {
        List<Map<String, Object>> treaties = new java.util.ArrayList<>();
        for (SfbDiplomacyService.Treaty t : diplomacyService.getActiveTreaties()) {
            treaties.add(Map.of("id", t.getId(), "factionA", t.getFactionA(),
                    "factionB", t.getFactionB(), "type", t.getType().getName()));
        }
        return ResponseEntity.ok(treaties);
    }

    @GetMapping("/diplomacy/stats/{faction}")
    public ResponseEntity<Map<String, Object>> diplomacyStats(@PathVariable String faction) {
        return ResponseEntity.ok(diplomacyService.getDiplomacyStats(faction));
    }

    // === Campaign AI: Economy ===

    @PostMapping("/economy/factions/{name}")
    public ResponseEntity<Map<String, String>> registerEconomy(@PathVariable String name,
            @RequestBody Map<String, Integer> resources) {
        int credits = resources.getOrDefault("credits", 1000);
        int materials = resources.getOrDefault("materials", 500);
        int fuel = resources.getOrDefault("fuel", 200);
        int manpower = resources.getOrDefault("manpower", 100);
        economyService.registerEconomy(name, credits, materials, fuel, manpower);
        return ResponseEntity.ok(Map.of("faction", name, "status", "registered"));
    }

    @GetMapping("/economy/{faction}")
    public ResponseEntity<Map<String, Object>> getEconomy(@PathVariable String faction) {
        return ResponseEntity.ok(economyService.getEconomyStats(faction));
    }

    @GetMapping("/economy/build-costs")
    public ResponseEntity<List<Map<String, Object>>> getBuildCosts() {
        List<Map<String, Object>> costs = new java.util.ArrayList<>();
        for (SfbEconomyService.ShipClassCategory c : SfbEconomyService.ShipClassCategory.values()) {
            SfbEconomyService.ShipBuildCost cost = SfbEconomyService.ShipBuildCost.of(c);
            costs.add(Map.of("category", c.name(), "credits", cost.getCredits(),
                    "materials", cost.getMaterials(), "fuel", cost.getFuel(),
                    "manpower", cost.getManpower()));
        }
        return ResponseEntity.ok(costs);
    }

    @PostMapping("/economy/build/{faction}/{category}")
    public ResponseEntity<Map<String, Object>> buildShip(@PathVariable String faction,
            @PathVariable String category) {
        boolean built = economyService.buildShip(faction, SfbEconomyService.ShipClassCategory.valueOf(category));
        return ResponseEntity.ok(Map.of("built", built, "faction", faction, "category", category));
    }

    // === Campaign AI: Strategic Decisions ===

    @GetMapping("/ai/decisions/{faction}")
    public ResponseEntity<List<Map<String, Object>>> getDecisions(@PathVariable String faction) {
        List<Map<String, Object>> decisions = new java.util.ArrayList<>();
        for (SfbCampaignAIService.AIDecision d : campaignAIService.generateDecisions(faction)) {
            decisions.add(Map.of("action", d.getAction().getName(), "priority", d.getPriority(),
                    "target", d.getTarget(), "reasoning", d.getReasoning()));
        }
        return ResponseEntity.ok(decisions);
    }

    @PostMapping("/ai/execute/{faction}")
    public ResponseEntity<Map<String, Object>> executeDecision(@PathVariable String faction) {
        java.util.Optional<SfbCampaignAIService.AIDecision> decision = campaignAIService.executeTopDecision(faction);
        if (decision.isPresent()) {
            SfbCampaignAIService.AIDecision d = decision.get();
            return ResponseEntity.ok(Map.of("executed", true, "action", d.getAction().getName(),
                    "target", d.getTarget(), "reasoning", d.getReasoning()));
        }
        return ResponseEntity.ok(Map.of("executed", false, "reason", "No decisions available"));
    }

    @GetMapping("/ai/assessment/{faction}")
    public ResponseEntity<Map<String, Object>> getAssessment(@PathVariable String faction) {
        return ResponseEntity.ok(campaignAIService.getStrategicAssessment(faction));
    }

    @PostMapping("/ai/advance-turn")
    public ResponseEntity<Map<String, String>> advanceCampaignTurn() {
        campaignAIService.advanceCampaignTurn();
        return ResponseEntity.ok(Map.of("status", "campaign_turn_advanced"));
    }

    @GetMapping("/ai/actions")
    public ResponseEntity<List<Map<String, String>>> getAllActions() {
        return ResponseEntity.ok(campaignAIService.getAllActions());
    }

    // === DTOs for Phase 2 ===

    public static class SfbTransferRequest {
        private String sourceFleetId;
        private String targetFleetId;
        private List<String> shipIds;
        public String getSourceFleetId() { return sourceFleetId; }
        public void setSourceFleetId(String id) { this.sourceFleetId = id; }
        public String getTargetFleetId() { return targetFleetId; }
        public void setTargetFleetId(String id) { this.targetFleetId = id; }
        public List<String> getShipIds() { return shipIds; }
        public void setShipIds(List<String> ids) { this.shipIds = ids; }
    }

    public static class SfbSplitFleetRequest {
        private String newFleetId;
        private String newFleetName;
        public String getNewFleetId() { return newFleetId; }
        public void setNewFleetId(String id) { this.newFleetId = id; }
        public String getNewFleetName() { return newFleetName; }
        public void setNewFleetName(String name) { this.newFleetName = name; }
    }
}
