package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.thirdreich.service.CountryCampaignService.CountryCampaign;
import com.yisy.modules.doramcu.hex.planet.military.PlayerMilitaryService.MilitaryBranch;
import com.yisy.modules.doramcu.hex.planet.military.PlayerMilitaryService.PlayerMilitaryRecord;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionChainService.ProductionChainSummary;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionChainService.ProductionTurnSummary;
import com.yisy.modules.doramcu.hex.planet.Industry;
import com.yisy.modules.doramcu.hex.planet.Settlement;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.TerrainType;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.fps.TycoonFpsIntegrationService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleService;
import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleType;
import com.yisy.modules.doramcu.hex.planet.hullmodule.dto.HullModuleBlueprintDto;
import com.yisy.modules.doramcu.hex.planet.hullmodule.dto.HullModuleDto;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionChainService;
import com.yisy.modules.doramcu.hex.planet.military.MilitaryProductionService;
import com.yisy.modules.doramcu.hex.planet.military.PlayerMilitaryService;
import com.yisy.modules.doramcu.hex.planet.military.UnifiedBattleResolver;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleManager;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import com.yisy.modules.doramcu.thirdreich.service.CountryCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for HullModule building, military production, battle resolution,
 * country campaigns, player military enlistment, production chains, and FPS integration.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexHullModuleController {

    private static final Logger log = LoggerFactory.getLogger(HexHullModuleController.class);

    private final HullModuleService hullModuleService;
    private final MilitaryProductionService militaryProductionService;
    private final MilitaryProductionChainService productionChainService;
    private final UnifiedBattleResolver battleResolver;
    private final UniversalBattleManager universalBattleManager;
    private final CountryCampaignService countryCampaignService;
    private final PlanetService planetService;
    private final PlayerMilitaryService playerMilitaryService;
    private final TycoonFpsIntegrationService tycoonFpsIntegrationService;
    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;

    public HexHullModuleController(
            HullModuleService hullModuleService,
            MilitaryProductionService militaryProductionService,
            MilitaryProductionChainService productionChainService,
            UnifiedBattleResolver battleResolver,
            UniversalBattleManager universalBattleManager,
            CountryCampaignService countryCampaignService,
            PlanetService planetService,
            PlayerMilitaryService playerMilitaryService,
            TycoonFpsIntegrationService tycoonFpsIntegrationService,
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder) {
        this.hullModuleService = hullModuleService;
        this.militaryProductionService = militaryProductionService;
        this.productionChainService = productionChainService;
        this.battleResolver = battleResolver;
        this.universalBattleManager = universalBattleManager;
        this.countryCampaignService = countryCampaignService;
        this.planetService = planetService;
        this.playerMilitaryService = playerMilitaryService;
        this.tycoonFpsIntegrationService = tycoonFpsIntegrationService;
        this.planetSurfaceBuilder = planetSurfaceBuilder;
    }

    // =====================================================================
    // HullModule Endpoints - Unified Building Construction System
    // =====================================================================

    /**
     * GET /api/v1/planets/hullmodule/types
     * List all available HullModule types.
     */
    @GetMapping("/hullmodule/types")
    public ResponseEntity<List<Map<String, Object>>> getHullModuleTypes() {
        return ResponseEntity.ok(hullModuleService.getModuleTypes());
    }

    /**
     * GET /api/v1/planets/{planetId}/hullmodules
     * List all hull modules on a planet.
     */
    @GetMapping("/{planetId}/hullmodules")
    public ResponseEntity<List<HullModuleDto>> getHullModules(@PathVariable Long planetId) {
        return ResponseEntity.ok(hullModuleService.getModulesByPlanet(planetId));
    }

    /**
     * GET /api/v1/planets/{planetId}/hullmodules/{moduleId}
     * Get a single hull module by ID.
     */
    @GetMapping("/{planetId}/hullmodules/{moduleId}")
    public ResponseEntity<HullModuleDto> getHullModule(@PathVariable Long planetId,
                                                        @PathVariable Long moduleId) {
        HullModuleDto module = hullModuleService.getModule(moduleId);
        if (module == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(module);
    }

    /**
     * POST /api/v1/planets/{planetId}/hullmodule/build
     * Build a new hull module using a predefined type.
     */
    @PostMapping("/{planetId}/hullmodule/build")
    public ResponseEntity<?> buildHullModule(@PathVariable Long planetId,
                                              @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String moduleTypeStr = (String) request.get("moduleType");
            int hexX = (int) request.getOrDefault("hexX", 0);
            int hexY = (int) request.getOrDefault("hexY", 0);
            int hexZ = (int) request.getOrDefault("hexZ", 0);
            String ownerId = (String) request.getOrDefault("ownerId", "SYSTEM");
            String terrainStr = (String) request.getOrDefault("terrain", "GRASSLAND");

            if (name == null || moduleTypeStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "name and moduleType are required"));
            }

            HullModuleType moduleType = HullModuleType.valueOf(moduleTypeStr);
            TerrainType terrain = TerrainType.valueOf(terrainStr);

            HullModuleDto module = hullModuleService.buildModule(
                    name, moduleType, planetId, hexX, hexY, hexZ, ownerId, terrain);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/{planetId}/hullmodule/build-blueprint
     * Build a new hull module using a player-created blueprint.
     */
    @PostMapping("/{planetId}/hullmodule/build-blueprint")
    public ResponseEntity<?> buildHullModuleFromBlueprint(@PathVariable Long planetId,
                                                           @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Long blueprintId = Long.valueOf(request.get("blueprintId").toString());
            int hexX = (int) request.getOrDefault("hexX", 0);
            int hexY = (int) request.getOrDefault("hexY", 0);
            int hexZ = (int) request.getOrDefault("hexZ", 0);
            String ownerId = (String) request.getOrDefault("ownerId", "SYSTEM");
            String terrainStr = (String) request.getOrDefault("terrain", "GRASSLAND");

            if (name == null || blueprintId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "name and blueprintId are required"));
            }

            TerrainType terrain = TerrainType.valueOf(terrainStr);

            HullModuleDto module = hullModuleService.buildFromBlueprint(
                    name, blueprintId, planetId, hexX, hexY, hexZ, ownerId, terrain);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/hullmodule/blueprint
     * Create a new player blueprint.
     */
    @PostMapping("/hullmodule/blueprint")
    public ResponseEntity<?> createBlueprint(@RequestBody Map<String, Object> request) {
        try {
            String blueprintCode = (String) request.get("blueprintCode");
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            String creatorId = (String) request.getOrDefault("creatorId", "SYSTEM");
            String categoryStr = (String) request.getOrDefault("category", "CUSTOM");
            int constructionCost = (int) request.getOrDefault("constructionCost", 1000);
            int maintenanceCost = (int) request.getOrDefault("maintenanceCost", 20);
            int floors = (int) request.getOrDefault("floors", 2);
            int basementLevels = (int) request.getOrDefault("basementLevels", 1);
            int totalCapacity = (int) request.getOrDefault("totalCapacity", 10);
            int techLevelRequired = (int) request.getOrDefault("techLevelRequired", 1);

            if (blueprintCode == null || name == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "blueprintCode and name are required"));
            }

            HullModuleType.Category category = HullModuleType.Category.valueOf(categoryStr);

            @SuppressWarnings("unchecked")
            Map<String, Integer> inputCargo = (Map<String, Integer>) request.getOrDefault("inputCargo", new HashMap<>());
            @SuppressWarnings("unchecked")
            Map<String, Integer> outputCargo = (Map<String, Integer>) request.getOrDefault("outputCargo", new HashMap<>());

            HullModuleBlueprintDto blueprint = hullModuleService.createBlueprint(
                    blueprintCode, name, description, creatorId, category,
                    constructionCost, maintenanceCost, floors, basementLevels,
                    totalCapacity, inputCargo, outputCargo, techLevelRequired);
            return ResponseEntity.ok(blueprint);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/hullmodule/blueprints
     * List all published blueprints.
     */
    @GetMapping("/hullmodule/blueprints")
    public ResponseEntity<List<HullModuleBlueprintDto>> getPublishedBlueprints() {
        return ResponseEntity.ok(hullModuleService.getPublishedBlueprints());
    }

    /**
     * POST /api/v1/planets/hullmodule/blueprint/{blueprintId}/publish
     * Publish a blueprint for public use.
     */
    @PostMapping("/hullmodule/blueprint/{blueprintId}/publish")
    public ResponseEntity<?> publishBlueprint(@PathVariable Long blueprintId,
                                               @RequestBody Map<String, Object> request) {
        try {
            Integer licenseFee = request.containsKey("licenseFee")
                    ? Integer.valueOf(request.get("licenseFee").toString()) : null;
            HullModuleBlueprintDto blueprint = hullModuleService.publishBlueprint(blueprintId, licenseFee);
            return ResponseEntity.ok(blueprint);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/{planetId}/hullmodule/maintenance
     * Calculate total maintenance cost for all modules on a planet.
     */
    @GetMapping("/{planetId}/hullmodule/maintenance")
    public ResponseEntity<Map<String, Object>> getMaintenanceCost(@PathVariable Long planetId) {
        int totalMaintenance = hullModuleService.calculateTotalMaintenance(planetId);
        return ResponseEntity.ok(Map.of(
                "planetId", planetId,
                "totalMaintenance", totalMaintenance
        ));
    }

    /**
     * POST /api/v1/planets/hullmodule/{moduleId}/deactivate
     * Deactivate (demolish) a hull module.
     */
    @PostMapping("/hullmodule/{moduleId}/deactivate")
    public ResponseEntity<?> deactivateModule(@PathVariable Long moduleId) {
        try {
            HullModuleDto module = hullModuleService.deactivateModule(moduleId);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/hullmodule/{moduleId}/production
     * Update production level for an industry module.
     */
    @PostMapping("/hullmodule/{moduleId}/production")
    public ResponseEntity<?> updateProductionLevel(@PathVariable Long moduleId,
                                                    @RequestBody Map<String, Object> request) {
        try {
            int productionLevel = Integer.valueOf(request.get("productionLevel").toString());
            HullModuleDto module = hullModuleService.updateProductionLevel(moduleId, productionLevel);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/hullmodule/{moduleId}/occupancy
     * Update occupancy for residential/commercial modules.
     */
    @PostMapping("/hullmodule/{moduleId}/occupancy")
    public ResponseEntity<?> updateOccupancy(@PathVariable Long moduleId,
                                              @RequestBody Map<String, Object> request) {
        try {
            int occupiedCapacity = Integer.valueOf(request.get("occupiedCapacity").toString());
            HullModuleDto module = hullModuleService.updateOccupancy(moduleId, occupiedCapacity);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Military Production Endpoints ----

    /**
     * POST /api/v1/planets/military/produce
     * Queue production of a military unit at a military structure.
     */
    @PostMapping("/military/produce")
    public ResponseEntity<?> produceMilitaryUnit(@RequestBody Map<String, Object> request) {
        try {
            String moduleId = (String) request.get("moduleId");
            String unitTypeName = (String) request.get("unitType");
            String factionId = (String) request.get("factionId");
            
            ThirdReichUnitType unitType = ThirdReichUnitType.valueOf(unitTypeName);
            boolean queued = militaryProductionService.queueProduction(moduleId, unitType, factionId);
            
            if (queued) {
                return ResponseEntity.ok(Map.of("status", "queued", "unitType", unitType.getDisplayName()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot produce this unit type at the given module"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/military/production-turn
     * Process one turn of military production.
     */
    @PostMapping("/military/production-turn")
    public ResponseEntity<?> processMilitaryProductionTurn(@RequestBody Map<String, Object> request) {
        try {
            Object planetIdObj = request.get("planetId");
            if (planetIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "planetId is required"));
            }
            Long planetId = Long.valueOf(planetIdObj.toString());
            List<ThirdReichGroundUnit> produced = militaryProductionService.processProductionTurn(planetId);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (ThirdReichGroundUnit unit : produced) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", unit.getId());
                map.put("name", unit.getName());
                map.put("unitType", unit.getUnitType().name());
                map.put("strength", unit.getStrength());
                map.put("attackValue", unit.getAttackValue());
                map.put("defenseValue", unit.getDefenseValue());
                result.add(map);
            }
            
            return ResponseEntity.ok(Map.of("produced", result, "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/military/units
     * List all military units.
     */
    @GetMapping("/military/units")
    public ResponseEntity<?> listMilitaryUnits(@RequestParam(required = false) String factionId) {
        try {
            List<ThirdReichGroundUnit> units;
            if (factionId != null) {
                units = militaryProductionService.getUnitsByFaction(factionId);
            } else {
                units = militaryProductionService.getAllUnits();
            }
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (ThirdReichGroundUnit unit : units) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", unit.getId());
                map.put("name", unit.getName());
                map.put("unitType", unit.getUnitType().name());
                map.put("strength", unit.getStrength());
                map.put("maxStrength", unit.getMaxStrength());
                map.put("morale", unit.getMorale());
                map.put("attackValue", unit.getAttackValue());
                map.put("defenseValue", unit.getDefenseValue());
                map.put("active", unit.isCombatEffective());
                result.add(map);
            }
            
            return ResponseEntity.ok(Map.of("units", result, "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/military/unit-types
     * List all producible unit types for a military module type.
     */
    @GetMapping("/military/unit-types")
    public ResponseEntity<?> listProducibleUnitTypes(@RequestParam String moduleType) {
        try {
            HullModuleType hmt = HullModuleType.valueOf(moduleType);
            List<ThirdReichUnitType> types = militaryProductionService.getProducibleUnitTypes(hmt);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (ThirdReichUnitType type : types) {
                Map<String, Object> map = new HashMap<>();
                map.put("unitType", type.name());
                map.put("displayName", type.getDisplayName());
                map.put("description", type.getDescription());
                map.put("productionTime", militaryProductionService.getProductionTime(type));
                map.put("productionCost", militaryProductionService.getProductionCost(type));
                result.add(map);
            }
            
            return ResponseEntity.ok(Map.of("moduleType", moduleType, "unitTypes", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown module type: " + moduleType));
        }
    }

    // ---- Battle Resolution Endpoints ----

    /**
     * POST /api/v1/planets/battle/resolve
     * Resolve a battle between two factions.
     */
    @PostMapping("/battle/resolve")
    public ResponseEntity<?> resolveBattle(@RequestBody Map<String, Object> request) {
        try {
            String attackerFaction = (String) request.get("attackerFaction");
            String defenderFaction = (String) request.get("defenderFaction");
            String battleTypeName = (String) request.get("battleType");
            Integer hexX = (Integer) request.get("hexX");
            Integer hexY = (Integer) request.get("hexY");
            Integer hexZ = (Integer) request.get("hexZ");
            
            // Get units for each faction - first from MilitaryProductionService, then from CountryCampaignService
            List<ThirdReichGroundUnit> attackerUnits = militaryProductionService.getUnitsByFaction(attackerFaction);
            if (attackerUnits.isEmpty()) {
                attackerUnits = countryCampaignService.getUnitsByIsoCode(attackerFaction);
            }
            List<ThirdReichGroundUnit> defenderUnits = militaryProductionService.getUnitsByFaction(defenderFaction);
            if (defenderUnits.isEmpty()) {
                defenderUnits = countryCampaignService.getUnitsByIsoCode(defenderFaction);
            }
            
            if (attackerUnits.isEmpty() || defenderUnits.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "One or both factions have no units"));
            }
            
            UnifiedBattleResolver.BattleType battleType = UnifiedBattleResolver.BattleType.valueOf(battleTypeName);
            TerrainType terrain = TerrainType.GRASSLAND;
            
            UnifiedBattleResolver.BattleRequest battleRequest = new UnifiedBattleResolver.BattleRequest(
                    attackerFaction, defenderFaction,
                    new ArrayList<>(attackerUnits), new ArrayList<>(defenderUnits),
                    battleType, hexX != null ? hexX : 0, hexY != null ? hexY : 0, hexZ != null ? hexZ : 0,
                    terrain
            );
            
            ThirdReichBattleResult result = battleResolver.resolveBattle(battleRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("outcome", result.getOutcome().name());
            response.put("attackerCasualties", result.getAttackerCasualties());
            response.put("defenderCasualties", result.getDefenderCasualties());
            response.put("attackerUnitsLost", result.getAttackerUnitsLost());
            response.put("defenderUnitsLost", result.getDefenderUnitsLost());
            response.put("roundsFought", result.getRoundsFought());
            response.put("events", result.getEvents());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/battle/history
     * Get recent battle history.
     */
    @GetMapping("/battle/history")
    public ResponseEntity<?> getBattleHistory(@RequestParam(defaultValue = "10") int count) {
        try {
            List<UnifiedBattleResolver.BattleRecord> records = battleResolver.getRecentBattles(count);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (UnifiedBattleResolver.BattleRecord record : records) {
                Map<String, Object> map = new HashMap<>();
                map.put("battleId", record.getBattleId());
                map.put("battleType", record.getBattleType().name());
                map.put("attackerFaction", record.getAttackerFaction());
                map.put("defenderFaction", record.getDefenderFaction());
                map.put("outcome", record.getOutcome().name());
                map.put("attackerCasualties", record.getAttackerCasualties());
                map.put("defenderCasualties", record.getDefenderCasualties());
                map.put("rounds", record.getRounds());
                result.add(map);
            }
            
            return ResponseEntity.ok(Map.of("battles", result, "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Country Campaign Endpoints ----

    /**
     * POST /api/v1/planets/{id}/country/{countryId}/campaign/init
     * Initialize a 3rd Reich campaign for a country.
     */
    @PostMapping("/{id}/country/{countryId}/campaign/init")
    public ResponseEntity<?> initializeCountryCampaign(
            @PathVariable Long id,
            @PathVariable Long countryId) {
        try {
            Optional<Earth2026Country> optional = planetService.getCountry(countryId);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Earth2026Country country = optional.get();
            CountryCampaign campaign = countryCampaignService.initializeCampaign(country);
            
            Map<String, Object> response = new HashMap<>();
            response.put("countryId", campaign.getCountryId());
            response.put("countryName", campaign.getCountryName());
            response.put("isoCode", campaign.getIsoCode());
            response.put("militaryStrength", campaign.getMilitaryStrength());
            response.put("units", campaign.getArmyUnits().size());
            response.put("combatPower", campaign.getTotalCombatPower());
            response.put("availableUnitTypes", campaign.getAvailableUnitTypes().size());
            response.put("productionBudget", campaign.getProductionBudget());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to initialize campaign: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/{id}/country/{countryId}/military
     * Get military forces for a country.
     */
    @GetMapping("/{id}/country/{countryId}/military")
    public ResponseEntity<?> getCountryMilitary(
            @PathVariable Long id,
            @PathVariable Long countryId) {
        try {
            CountryCampaign campaign = countryCampaignService.getCampaign(countryId);
            if (campaign == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<Map<String, Object>> units = new ArrayList<>();
            for (ThirdReichGroundUnit unit : campaign.getArmyUnits()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", unit.getId());
                map.put("name", unit.getName());
                map.put("unitType", unit.getUnitType().name());
                map.put("strength", unit.getStrength());
                map.put("maxStrength", unit.getMaxStrength());
                map.put("morale", unit.getMorale());
                map.put("attackValue", unit.getAttackValue());
                map.put("defenseValue", unit.getDefenseValue());
                map.put("active", unit.isCombatEffective());
                units.add(map);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("countryName", campaign.getCountryName());
            response.put("isoCode", campaign.getIsoCode());
            response.put("militaryStrength", campaign.getMilitaryStrength());
            response.put("combatPower", campaign.getTotalCombatPower());
            response.put("units", units);
            response.put("battlesWon", campaign.getBattlesWon());
            response.put("battlesLost", campaign.getBattlesLost());
            response.put("casualties", campaign.getTotalCasualties());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/{id}/campaigns
     * List all campaigns for a planet.
     */
    @GetMapping("/{id}/campaigns")
    public ResponseEntity<?> getPlanetCampaigns(@PathVariable Long id) {
        try {
            List<CountryCampaign> campaigns = countryCampaignService.getCampaignsForPlanet(id);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (CountryCampaign campaign : campaigns) {
                Map<String, Object> map = new HashMap<>();
                map.put("countryId", campaign.getCountryId());
                map.put("countryName", campaign.getCountryName());
                map.put("isoCode", campaign.getIsoCode());
                map.put("militaryStrength", campaign.getMilitaryStrength());
                map.put("units", campaign.getArmyUnits().size());
                map.put("combatPower", campaign.getTotalCombatPower());
                map.put("battlesWon", campaign.getBattlesWon());
                map.put("battlesLost", campaign.getBattlesLost());
                result.add(map);
            }
            
            return ResponseEntity.ok(Map.of("campaigns", result, "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Player Military Enlistment Endpoints ----

    /**
     * POST /api/v1/planets/{id}/military/enlist
     * Enlist a player in a country's military branch.
     */
    @PostMapping("/{id}/military/enlist")
    public ResponseEntity<?> enlistPlayer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String playerId = (String) request.get("playerId");
            Long countryId = Long.valueOf(request.get("countryId").toString());
            String branchName = (String) request.get("branch");

            MilitaryBranch branch = MilitaryBranch.valueOf(branchName);
            PlayerMilitaryRecord record = playerMilitaryService.enlist(playerId, id, countryId, branch);

            Map<String, Object> response = new HashMap<>();
            response.put("playerId", record.getPlayerId());
            response.put("countryName", record.getCountryName());
            response.put("isoCode", record.getIsoCode());
            response.put("branch", record.getBranch().getDisplayName());
            response.put("rank", record.getRank().getDisplayName());
            response.put("unit", record.getAssignedUnit() != null ? record.getAssignedUnit().getName() : null);
            response.put("enlistmentDate", record.getEnlistmentDate());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to enlist player: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/{id}/military/player/{playerId}
     * Get a player's military status.
     */
    @GetMapping("/{id}/military/player/{playerId}")
    public ResponseEntity<?> getPlayerMilitary(
            @PathVariable Long id,
            @PathVariable String playerId) {
        try {
            PlayerMilitaryRecord record = playerMilitaryService.getRecord(playerId);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("playerId", record.getPlayerId());
            response.put("countryName", record.getCountryName());
            response.put("isoCode", record.getIsoCode());
            response.put("branch", record.getBranch().getDisplayName());
            response.put("rank", record.getRank().getDisplayName());
            response.put("rankLevel", record.getRank().getLevel());
            response.put("unit", record.getAssignedUnit() != null ? record.getAssignedUnit().getName() : null);
            response.put("unitType", record.getAssignedUnit() != null ? record.getAssignedUnit().getUnitType().name() : null);
            response.put("battlesParticipated", record.getBattlesParticipated());
            response.put("kills", record.getKills());
            response.put("medals", record.getMedals());
            response.put("active", record.isActive());
            response.put("enlistmentDate", record.getEnlistmentDate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/{id}/military/player/{playerId}/promote
     * Promote a player to the next rank.
     */
    @PostMapping("/{id}/military/player/{playerId}/promote")
    public ResponseEntity<?> promotePlayer(
            @PathVariable Long id,
            @PathVariable String playerId) {
        try {
            PlayerMilitaryRecord record = playerMilitaryService.promote(playerId);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("playerId", record.getPlayerId());
            response.put("newRank", record.getRank().getDisplayName());
            response.put("rankLevel", record.getRank().getLevel());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/planets/{id}/military/player/{playerId}
     * Discharge a player from military service.
     */
    @DeleteMapping("/{id}/military/player/{playerId}")
    public ResponseEntity<?> dischargePlayer(
            @PathVariable Long id,
            @PathVariable String playerId) {
        try {
            PlayerMilitaryRecord record = playerMilitaryService.discharge(playerId);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                    "playerId", record.getPlayerId(),
                    "status", "discharged",
                    "countryName", record.getCountryName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Production Chain Endpoints ----

    /**
     * GET /api/v1/planets/{id}/military/production-chain
     * Get the military production chain for a planet.
     * Shows cargo flow from industries through stations to military bases.
     */
    @GetMapping("/{id}/military/production-chain")
    public ResponseEntity<?> getProductionChain(@PathVariable Long id) {
        try {
            Planet planet = new Planet(id.intValue());
            List<Industry> industries = planetSurfaceBuilder.getAllIndustries(planet);
            List<Station> stations = planetSurfaceBuilder.getAllStations(planet);

            ProductionChainSummary summary = productionChainService.getProductionChain(id, industries, stations);

            Map<String, Object> response = new HashMap<>();
            response.put("planetId", summary.getPlanetId());
            response.put("totalMilitaryUnits", summary.getTotalMilitaryUnits());
            response.put("activeProductionQueues", summary.getActiveProductionQueues());

            List<Map<String, Object>> rawIndustries = new ArrayList<>();
            for (var ind : summary.getRawMaterialIndustries()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ind.getId());
                map.put("name", ind.getName());
                map.put("type", ind.getType());
                map.put("x", ind.getX());
                map.put("y", ind.getY());
                map.put("z", ind.getZ());
                map.put("outputs", ind.getOutputs().stream().map(Enum::name).toArray(String[]::new));
                rawIndustries.add(map);
            }
            response.put("rawMaterialIndustries", rawIndustries);

            List<Map<String, Object>> procIndustries = new ArrayList<>();
            for (var ind : summary.getProcessingIndustries()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ind.getId());
                map.put("name", ind.getName());
                map.put("type", ind.getType());
                map.put("x", ind.getX());
                map.put("y", ind.getY());
                map.put("z", ind.getZ());
                map.put("inputs", ind.getInputs().stream().map(Enum::name).toArray(String[]::new));
                map.put("outputs", ind.getOutputs().stream().map(Enum::name).toArray(String[]::new));
                procIndustries.add(map);
            }
            response.put("processingIndustries", procIndustries);

            List<Map<String, Object>> bases = new ArrayList<>();
            for (var base : summary.getMilitaryBases()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", base.getId());
                map.put("name", base.getName());
                map.put("moduleType", base.getModuleType());
                map.put("x", base.getX());
                map.put("y", base.getY());
                map.put("z", base.getZ());
                bases.add(map);
            }
            response.put("militaryBases", bases);

            List<Map<String, Object>> flows = new ArrayList<>();
            for (var flow : summary.getCargoFlows()) {
                Map<String, Object> map = new HashMap<>();
                map.put("cargoType", flow.getCargoType());
                map.put("produced", flow.getProduced());
                map.put("consumed", flow.getConsumed());
                map.put("surplus", flow.getSurplus());
                flows.add(map);
            }
            response.put("cargoFlows", flows);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get production chain: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/planets/{id}/military/production-chain/turn
     * Process one turn of the military production chain.
     * Generates cargo from industries, consumes cargo for military production, produces units.
     */
    @PostMapping("/{id}/military/production-chain/turn")
    public ResponseEntity<?> processProductionChainTurn(@PathVariable Long id) {
        try {
            Planet planet = new Planet(id.intValue());
            List<Industry> industries = planetSurfaceBuilder.getAllIndustries(planet);
            List<Settlement> settlements = planetSurfaceBuilder.getAllSettlements(planet);
            List<Station> stations = planetSurfaceBuilder.getAllStations(planet);

            ProductionTurnSummary summary = productionChainService.processProductionTurn(id, industries, settlements, stations);

            Map<String, Object> response = new HashMap<>();
            response.put("planetId", summary.getPlanetId());
            response.put("cargoGenerated", summary.getCargoGenerated());
            response.put("unitsProduced", summary.getUnitsProduced());
            response.put("productionEvents", summary.getProductionEvents());
            response.put("availableCargo", summary.getAvailableCargo());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to process production turn: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Universal Battle Endpoints ----

    /**
     * POST /api/v1/planets/battle/universal
     * Start a universal battle between units from any modules.
     */
    @PostMapping("/battle/universal")
    public ResponseEntity<?> resolveUniversalBattle(@RequestBody Map<String, Object> request) {
        try {
            String attackerFaction = (String) request.get("attackerFaction");
            String defenderFaction = (String) request.get("defenderFaction");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attackerUnits = (List<Map<String, Object>>) request.get("attackerUnits");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> defenderUnits = (List<Map<String, Object>>) request.get("defenderUnits");
            String battleTypeStr = (String) request.getOrDefault("battleType", "GROUND");
            int hexX = (Integer) request.getOrDefault("hexX", 0);
            int hexY = (Integer) request.getOrDefault("hexY", 0);
            int hexZ = (Integer) request.getOrDefault("hexZ", 0);
            String terrainStr = (String) request.getOrDefault("terrain", "GRASSLAND");

            UnifiedBattleResolver.BattleType battleType = UnifiedBattleResolver.BattleType.valueOf(battleTypeStr);
            TerrainType terrain = TerrainType.valueOf(terrainStr);

            // Convert attacker units
            List<UniversalBattleUnit> attUnits = convertToUniversalUnits(attackerUnits, attackerFaction);
            List<UniversalBattleUnit> defUnits = convertToUniversalUnits(defenderUnits, defenderFaction);

            UniversalBattleManager.UniversalBattleRequest battleRequest =
                    new UniversalBattleManager.UniversalBattleRequest(
                            attackerFaction, defenderFaction,
                            attUnits, defUnits,
                            battleType, hexX, hexY, hexZ, terrain
                    );

            ThirdReichBattleResult result = universalBattleManager.resolveBattle(battleRequest);

            // Collect modules involved from the units
            Set<String> modulesInvolved = new HashSet<>();
            for (UniversalBattleUnit unit : attUnits) {
                modulesInvolved.add(unit.getModuleId());
            }
            for (UniversalBattleUnit unit : defUnits) {
                modulesInvolved.add(unit.getModuleId());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("outcome", result.getOutcome().name());
            response.put("attackerCasualties", result.getAttackerCasualties());
            response.put("defenderCasualties", result.getDefenderCasualties());
            response.put("rounds", result.getRoundsFought());
            response.put("modulesInvolved", modulesInvolved);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to resolve universal battle: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/battle/modules
     * List all available module unit types for universal battles.
     */
    @GetMapping("/battle/modules")
    public ResponseEntity<?> getModuleUnitTypes() {
        try {
            Map<String, Map<String, String>> allTypes = universalBattleManager.getAllModuleUnitTypes();
            Map<String, Object> response = new HashMap<>();
            response.put("modules", allTypes);
            response.put("adapterCount", allTypes.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get module unit types: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/planets/battle/universal/history
     * Get universal battle history.
     */
    @GetMapping("/battle/universal/history")
    public ResponseEntity<?> getUniversalBattleHistory(@RequestParam(defaultValue = "10") int count) {
        try {
            List<UniversalBattleManager.UniversalBattleRecord> records = universalBattleManager.getRecentBattles(count);
            List<Map<String, Object>> result = new ArrayList<>();
            for (UniversalBattleManager.UniversalBattleRecord record : records) {
                Map<String, Object> map = new HashMap<>();
                map.put("battleId", record.getBattleId());
                map.put("battleType", record.getBattleType().name());
                map.put("attackerFaction", record.getAttackerFaction());
                map.put("defenderFaction", record.getDefenderFaction());
                map.put("outcome", record.getOutcome().name());
                map.put("attackerCasualties", record.getAttackerCasualties());
                map.put("defenderCasualties", record.getDefenderCasualties());
                map.put("rounds", record.getRounds());
                map.put("modulesInvolved", record.getModulesInvolved());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get battle history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private List<UniversalBattleUnit> convertToUniversalUnits(List<Map<String, Object>> unitMaps, String faction) {
        List<UniversalBattleUnit> units = new ArrayList<>();
        for (Map<String, Object> unitMap : unitMaps) {
            String moduleId = (String) unitMap.get("moduleId");
            String moduleUnitType = (String) unitMap.get("moduleUnitType");
            String unitId = (String) unitMap.get("unitId");
            String unitName = (String) unitMap.getOrDefault("unitName", moduleUnitType);
            int techLevel = (Integer) unitMap.getOrDefault("techLevel", 6);

            Optional<com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter> adapterOpt =
                    universalBattleManager.getModuleAdapter(moduleId);

            if (adapterOpt.isPresent()) {
                UniversalBattleUnit unit = adapterOpt.get().createUnit(unitId, unitName, moduleUnitType, faction, techLevel);
                units.add(unit);
            } else {
                log.warn("No battle adapter found for module: {}", moduleId);
            }
        }
        return units;
    }

    // ---- FPS Battle Endpoints ----

    /**
     * Start an FPS battle on a planet's surface.
     * Converts planet terrain to an FPS map and places military units.
     */
    @PostMapping("/{planetId}/fps/start-battle")
    public ResponseEntity<Map<String, Object>> startFpsBattle(
            @PathVariable Long planetId,
            @RequestBody Map<String, String> request) {
        try {
            String attacker = request.getOrDefault("attackerFaction", "ATTACKER");
            String defender = request.getOrDefault("defenderFaction", "DEFENDER");

            Planet planet = new Planet(planetId.intValue());
            String sessionId = tycoonFpsIntegrationService.startBattle(planet, attacker, defender);

            return ResponseEntity.ok(Map.of(
                    "sessionId", sessionId,
                    "planetId", planetId,
                    "attackerFaction", attacker,
                    "defenderFaction", defender,
                    "status", "BATTLE_STARTED"
            ));
        } catch (Exception e) {
            log.error("Failed to start FPS battle: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get or create an FPS map for a planet.
     */
    @GetMapping("/{planetId}/fps/map")
    public ResponseEntity<Map<String, Object>> getFpsMap(@PathVariable Long planetId) {
        try {
            Planet planet = new Planet(planetId.intValue());
            com.yisy.modules.doramcu.fps.core.model.FpsMap map =
                    tycoonFpsIntegrationService.getOrCreateMap(planet);

            Map<String, Object> result = new HashMap<>();
            result.put("mapId", map.getId());
            result.put("name", map.getName());
            result.put("description", map.getDescription());
            result.put("theme", map.getTheme());
            result.put("mapWidth", map.getMapWidth());
            result.put("mapDepth", map.getMapDepth());
            result.put("mapHeight", map.getMapHeight());
            result.put("spawnPoints", map.getSpawnPoints().size());
            result.put("collisionBoxes", map.getCollisionBoxes().size());
            result.put("capturePoints", map.getCapturePoints().size());
            result.put("powerUpLocations", map.getPowerUpLocations().size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get FPS map: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== FPS Matchmaking Endpoints ==========

    /**
     * Join an FPS battle as an enlisted player.
     */
    @PostMapping("/{planetId}/fps/join-battle")
    public ResponseEntity<Map<String, Object>> joinFpsBattle(
            @PathVariable Long planetId,
            @RequestBody Map<String, String> request) {
        try {
            String playerId = request.getOrDefault("playerId", "PLAYER");
            String battleId = request.getOrDefault("battleId", "BATTLE-1");

            Map<String, Object> joinData = playerMilitaryService.joinFpsBattle(playerId, battleId);
            if (joinData == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Player not enlisted in any country"));
            }
            return ResponseEntity.ok(joinData);
        } catch (Exception e) {
            log.error("Failed to join FPS battle: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get FPS matchmaking data for a player.
     */
    @GetMapping("/{planetId}/fps/matchmaking/{playerId}")
    public ResponseEntity<Map<String, Object>> getFpsMatchmaking(
            @PathVariable Long planetId,
            @PathVariable String playerId) {
        Map<String, Object> data = playerMilitaryService.getFpsMatchmakingData(playerId);
        if (data == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Player not enlisted"));
        }
        return ResponseEntity.ok(data);
    }
}
