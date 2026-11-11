package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Structure;
import com.yisy.modules.doramcu.earth2026.model.TechLevel;
import com.yisy.modules.doramcu.earth2026.model.TimePeriodTemplate;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for Earth 2026 Tech Levels, Time Period Templates, Structures,
 * and Battle Environments.
 * <p>
 * Other game modules (e.g., Third Reich) call these endpoints to get
 * planet templates, structure lists, and battle environment data.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/planet")
public class Earth2026TechLevelController {

    private final Earth2026Service earth2026Service;

    public Earth2026TechLevelController(Earth2026Service earth2026Service) {
        this.earth2026Service = earth2026Service;
    }

    // ------------------------------------------------------------------
    // Tech Levels
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/planet/tech-levels
     * List all available tech levels.
     */
    @GetMapping("/tech-levels")
    public ResponseEntity<List<Map<String, Object>>> getAllTechLevels() {
        List<Map<String, Object>> levels = earth2026Service.getAllTechLevels().stream()
                .map(this::techLevelToMap)
                .toList();
        return ResponseEntity.ok(levels);
    }

    /**
     * GET /api/v1/earth2026/planet/tech-levels/{level}
     * Get a specific tech level by numeric value.
     */
    @GetMapping("/tech-levels/{level}")
    public ResponseEntity<?> getTechLevel(@PathVariable int level) {
        return earth2026Service.getTechLevel(level)
                .map(tl -> ResponseEntity.ok((Object) techLevelToMap(tl)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Time Period Templates
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/planet/time-periods
     * List all available time period templates.
     */
    @GetMapping("/time-periods")
    public ResponseEntity<List<Map<String, Object>>> getAllTimePeriods() {
        List<Map<String, Object>> periods = earth2026Service.getAllTimePeriodTemplates().stream()
                .map(this::timePeriodToMap)
                .toList();
        return ResponseEntity.ok(periods);
    }

    /**
     * GET /api/v1/earth2026/planet/time-periods/{name}
     * Get a specific time period template by name.
     */
    @GetMapping("/time-periods/{name}")
    public ResponseEntity<?> getTimePeriod(@PathVariable String name) {
        return earth2026Service.getTimePeriodTemplate(name)
                .map(tp -> ResponseEntity.ok((Object) timePeriodToMap(tp)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Structures
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/planet/locations/{locationId}/structures
     * List all structures at a location.
     */
    @GetMapping("/locations/{locationId}/structures")
    public ResponseEntity<List<Earth2026Structure>> getStructures(@PathVariable Long locationId) {
        return ResponseEntity.ok(earth2026Service.getStructuresAtLocation(locationId));
    }

    /**
     * GET /api/v1/earth2026/planet/structures/{id}
     * Get a specific structure by ID.
     */
    @GetMapping("/structures/{id}")
    public ResponseEntity<?> getStructure(@PathVariable Long id) {
        return earth2026Service.getStructure(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Battle Environments
    // ------------------------------------------------------------------

    /**
     * POST /api/v1/earth2026/planet/battle-environments
     * Build a complete battle environment for a location and time period.
     * <p>
     * Request body:
     * {
     *   "locationId": 1,
     *   "timePeriod": "WWII",
     *   "terrainType": "URBAN",  (optional, default "URBAN")
     *   "weather": "CLEAR"       (optional, default "CLEAR")
     * }
     * </p>
     */
    @PostMapping("/battle-environments")
    public ResponseEntity<?> buildBattleEnvironment(@RequestBody Map<String, Object> request) {
        try {
            Long locationId = Long.valueOf(request.get("locationId").toString());
            String timePeriod = request.get("timePeriod").toString();
            String terrainType = request.getOrDefault("terrainType", "URBAN").toString();
            String weather = request.getOrDefault("weather", "CLEAR").toString();

            if (locationId == null || timePeriod == null || timePeriod.isBlank()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "locationId and timePeriod are required");
                return ResponseEntity.badRequest().body(error);
            }

            Earth2026BattleEnvironment env = earth2026Service.buildBattleEnvironment(
                    locationId, timePeriod, terrainType, weather);
            return ResponseEntity.ok(env.toMap());
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to build battle environment: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * GET /api/v1/earth2026/planet/battle-environments/{locationId}/{timePeriod}
     * Build a battle environment with default terrain and weather.
     */
    @GetMapping("/battle-environments/{locationId}/{timePeriod}")
    public ResponseEntity<?> getBattleEnvironment(@PathVariable Long locationId,
                                                   @PathVariable String timePeriod) {
        try {
            Earth2026BattleEnvironment env =
                    earth2026Service.buildBattleEnvironment(locationId, timePeriod);
            return ResponseEntity.ok(env.toMap());
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GET /api/v1/earth2026/planet/units/{techLevel}
     * Get available unit types for a given tech level.
     */
    @GetMapping("/units/{techLevel}")
    public ResponseEntity<List<Map<String, Object>>> getUnitsForTechLevel(@PathVariable int techLevel) {
        List<Map<String, Object>> units = earth2026Service.getAvailableUnitsForTechLevel(techLevel).stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("unitType", u.getUnitType());
                    map.put("description", u.getDescription());
                    map.put("minTechLevel", u.getMinTechLevel());
                    map.put("attackPower", u.getAttackPower());
                    map.put("defensePower", u.getDefensePower());
                    map.put("movement", u.getMovement());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(units);
    }

    // ------------------------------------------------------------------
    // Rules Database: Species
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/planet/species
     * List all species from the rules database.
     */
    @GetMapping("/species")
    public ResponseEntity<List<Map<String, Object>>> getAllSpecies() {
        List<Map<String, Object>> species = earth2026Service.getAllSpecies().stream()
                .map(this::speciesToMap)
                .toList();
        return ResponseEntity.ok(species);
    }

    /**
     * GET /api/v1/earth2026/planet/species/{id}
     * Get a specific species by ID.
     */
    @GetMapping("/species/{id}")
    public ResponseEntity<?> getSpecies(@PathVariable String id) {
        return earth2026Service.getSpecies(id)
                .map(sp -> ResponseEntity.ok((Object) speciesToMap(sp)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/planet/species?gameModule=TRAVELLER
     * Get species by game module.
     */
    @GetMapping("/species/by-game-module")
    public ResponseEntity<List<Map<String, Object>>> getSpeciesByGameModule(@RequestParam String gameModule) {
        List<Map<String, Object>> species = earth2026Service.getSpeciesByGameModule(gameModule).stream()
                .map(this::speciesToMap)
                .toList();
        return ResponseEntity.ok(species);
    }

    // ------------------------------------------------------------------
    // Rules Database: AI Character Name Generation
    // ------------------------------------------------------------------

    /**
     * POST /api/v1/earth2026/planet/ai-names
     * Generate an AI character name for a given species and role.
     * <p>
     * Request body:
     * {
     *   "speciesId": "KLINGON",
     *   "role": "Leader"
     * }
     * </p>
     */
    @PostMapping("/ai-names")
    public ResponseEntity<?> generateAiName(@RequestBody Map<String, Object> request) {
        String speciesId = request.get("speciesId").toString();
        String role = request.getOrDefault("role", "Leader").toString();

        if (speciesId == null || speciesId.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "speciesId is required");
            return ResponseEntity.badRequest().body(error);
        }

        String name = earth2026Service.generateAiCharacterName(speciesId, role);
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("speciesId", speciesId);
        result.put("role", role);
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Rules Database: Galaxy/System/Planet Rules
    // ------------------------------------------------------------------

    /**
     * GET /api/v1/earth2026/planet/rules/galaxy
     * Get galaxy creation rules.
     */
    @GetMapping("/rules/galaxy")
    public ResponseEntity<?> getGalaxyRules() {
        com.yisy.modules.doramcu.earth2026.model.E26GalaxyRules rules = earth2026Service.getGalaxyRules();
        if (rules == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("defaultGalaxySeed", rules.getDefaultGalaxySeed());
        map.put("voidGalaxySeed", rules.getVoidGalaxySeed());
        map.put("earthGalaxySeed", rules.getEarthGalaxySeed());
        map.put("testGalaxySeed", rules.getTestGalaxySeed());
        map.put("maxSectorsPerGalaxy", rules.getMaxSectorsPerGalaxy());
        map.put("maxSystemsPerSector", rules.getMaxSystemsPerSector());
        map.put("maxPlanetsPerSystem", rules.getMaxPlanetsPerSystem());
        map.put("maxMoonsPerPlanet", rules.getMaxMoonsPerPlanet());
        map.put("defaultTechLevel", rules.getDefaultTechLevel());
        map.put("minTechLevel", rules.getMinTechLevel());
        map.put("maxTechLevel", rules.getMaxTechLevel());
        if (rules.getGalaxyTypes() != null) {
            map.put("galaxyTypes", rules.getGalaxyTypes().stream()
                    .map(gt -> {
                        Map<String, Object> gm = new HashMap<>();
                        gm.put("id", gt.getId());
                        gm.put("name", gt.getName());
                        gm.put("description", gt.getDescription());
                        return gm;
                    })
                    .toList());
        }
        return ResponseEntity.ok(map);
    }

    /**
     * GET /api/v1/earth2026/planet/rules/system
     * Get star system creation rules.
     */
    @GetMapping("/rules/system")
    public ResponseEntity<?> getSystemRules() {
        com.yisy.modules.doramcu.earth2026.model.E26SystemRules rules = earth2026Service.getSystemRules();
        if (rules == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> map = new HashMap<>();
        if (rules.getStarTypes() != null) {
            map.put("starTypes", rules.getStarTypes().stream()
                    .map(st -> {
                        Map<String, Object> sm = new HashMap<>();
                        sm.put("id", st.getId());
                        sm.put("name", st.getName());
                        sm.put("mass", st.getMass());
                        sm.put("lifespan", st.getLifespan());
                        sm.put("temperature", st.getTemperature());
                        return sm;
                    })
                    .toList());
        }
        if (rules.getPlanetTypes() != null) {
            map.put("planetTypes", rules.getPlanetTypes().stream()
                    .map(pt -> {
                        Map<String, Object> pm = new HashMap<>();
                        pm.put("id", pt.getId());
                        pm.put("name", pt.getName());
                        pm.put("description", pt.getDescription());
                        pm.put("minRadius", pt.getMinRadius());
                        pm.put("maxRadius", pt.getMaxRadius());
                        return pm;
                    })
                    .toList());
        }
        if (rules.getHabitableZone() != null) {
            com.yisy.modules.doramcu.earth2026.model.E26SystemRules.E26HabitableZone hz = rules.getHabitableZone();
            Map<String, Object> hzMap = new HashMap<>();
            hzMap.put("minDistance", hz.getMinDistance());
            hzMap.put("maxDistance", hz.getMaxDistance());
            hzMap.put("unit", hz.getUnit());
            map.put("habitableZone", hzMap);
        }
        return ResponseEntity.ok(map);
    }

    /**
     * GET /api/v1/earth2026/planet/rules/planet
     * Get planet creation rules.
     */
    @GetMapping("/rules/planet")
    public ResponseEntity<?> getPlanetRules() {
        com.yisy.modules.doramcu.earth2026.model.E26PlanetRules rules = earth2026Service.getPlanetRules();
        if (rules == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> map = new HashMap<>();
        if (rules.getGovernmentTypes() != null) {
            map.put("governmentTypes", rules.getGovernmentTypes().stream()
                    .map(gt -> {
                        Map<String, Object> gm = new HashMap<>();
                        gm.put("id", gt.getId());
                        gm.put("name", gt.getName());
                        gm.put("lawLevel", gt.getLawLevel());
                        gm.put("stability", gt.getStability());
                        return gm;
                    })
                    .toList());
        }
        if (rules.getLawLevels() != null) {
            com.yisy.modules.doramcu.earth2026.model.E26PlanetRules.E26LawLevels ll = rules.getLawLevels();
            Map<String, Object> llMap = new HashMap<>();
            llMap.put("min", ll.getMin());
            llMap.put("max", ll.getMax());
            llMap.put("descriptions", ll.getDescriptions());
            map.put("lawLevels", llMap);
        }
        if (rules.getWorldModifiers() != null) {
            map.put("worldModifiers", rules.getWorldModifiers().stream()
                    .map(wm -> {
                        Map<String, Object> wmMap = new HashMap<>();
                        wmMap.put("id", wm.getId());
                        wmMap.put("name", wm.getName());
                        wmMap.put("description", wm.getDescription());
                        return wmMap;
                    })
                    .toList());
        }
        return ResponseEntity.ok(map);
    }

    // ------------------------------------------------------------------
    // Planet Population from Game Data
    // ------------------------------------------------------------------

    /**
     * POST /api/v1/earth2026/planet/populate
     * Populate a planet using game-provided character data.
     * <p>
     * Other games call this endpoint to get a fully populated planet
     * with AI-controlled leadership. The game passes all existing characters
     * on the planet, and Earth 2026 reuses them or generates new ones as needed.
     * </p>
     * <p>
     * Uses the phased planet population API with a collecting progress callback
     * to build the response incrementally, avoiding memory overrun on large planets.
     * For real-time streaming, use {@code POST /api/v1/earth2026/regions/planets/stream} instead.
     * </p>
     */
    @PostMapping("/populate")
    public ResponseEntity<?> populatePlanetWithGameData(
            @RequestBody com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request) {

        if (request.getPlanetName() == null || request.getPlanetName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "planetName is required"));
        }
        if (request.getGameModuleName() == null || request.getGameModuleName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "gameModuleName is required"));
        }

        // Use the new phased API with a collecting progress callback
        com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse response =
                new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse();

        final java.util.List<com.yisy.modules.doramcu.earth2026.model.Earth2026Country> allCountries = new java.util.ArrayList<>();
        final java.util.List<com.yisy.modules.doramcu.earth2026.model.Earth2026Region> allRegions = new java.util.ArrayList<>();
        final java.util.List<com.yisy.modules.doramcu.eve.model.AiCharacter> newAiCharacters = new java.util.ArrayList<>();
        final java.util.Map<String, Long> reusedAiCharacterIds = new java.util.HashMap<>();
        final java.util.Map<String, String> registeredPlayerCharacters = new java.util.HashMap<>();
        final com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse.PopulationSummary summary =
                new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse.PopulationSummary();

        com.yisy.modules.doramcu.hex.PlanetPopulationProgress collectingProgress =
                new com.yisy.modules.doramcu.hex.PlanetPopulationProgress() {
            @Override
            public void onPlanetCreated(com.yisy.modules.doramcu.earth2026.model.Earth2026Planet planet) {
                response.setPlanet(planet);
            }

            @Override
            public void onCountryBatch(java.util.List<com.yisy.modules.doramcu.earth2026.model.Earth2026Country> countries, int countryIndex, int totalCountries) {
                allCountries.addAll(countries);
            }

            @Override
            public void onRegionBatch(java.util.List<com.yisy.modules.doramcu.earth2026.model.Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {
                allRegions.addAll(regions);
            }

            @Override
            public void onAiLeaderAssigned(com.yisy.modules.doramcu.eve.model.AiCharacter leader, Long countryId, boolean isNew) {
                if (isNew) {
                    newAiCharacters.add(leader);
                }
            }

            @Override
            public void onAiGovernorAssigned(com.yisy.modules.doramcu.eve.model.AiCharacter governor, Long regionId, boolean isNew) {
                if (isNew) {
                    newAiCharacters.add(governor);
                }
            }

            @Override
            public void onPlayerCharactersRegistered(java.util.Map<String, String> players) {
                registeredPlayerCharacters.putAll(players);
            }

            @Override
            public void onComplete(com.yisy.modules.doramcu.hex.PlanetPopulationProgress.PopulationSummary popSummary) {
                summary.setPlanetFound(popSummary.isPlanetFound());
                summary.setTotalGameCharacters(popSummary.getTotalGameCharacters());
                summary.setCountriesWithLeaders(popSummary.getCountriesWithLeaders());
                summary.setRegionsWithGovernors(popSummary.getRegionsWithGovernors());
                summary.setAiCharactersReused(popSummary.getAiCharactersReused());
                summary.setNewAiCharactersGenerated(popSummary.getNewAiCharactersGenerated());
                summary.setPlayerCharactersRegistered(popSummary.getPlayerCharactersRegistered());
            }
        };

        earth2026Service.populatePlanetWithGameData(request, collectingProgress);

        response.setCountries(allCountries);
        response.setRegions(allRegions);
        response.setNewAiCharacters(newAiCharacters);
        response.setReusedAiCharacterIds(reusedAiCharacterIds);
        response.setRegisteredPlayerCharacters(registeredPlayerCharacters);
        response.setSummary(summary);

        if (response.getPlanet() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // Serialization helpers
    // ------------------------------------------------------------------

    private Map<String, Object> speciesToMap(com.yisy.modules.doramcu.earth2026.model.E26Species sp) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", sp.getId());
        map.put("name", sp.getName());
        map.put("description", sp.getDescription());
        map.put("gameModule", sp.getGameModule());
        map.put("characteristics", sp.getCharacteristics());
        map.put("combatDM", sp.getCombatDM());
        map.put("socialDM", sp.getSocialDM());
        map.put("navigationDM", sp.getNavigationDM());
        map.put("armorBonus", sp.getArmorBonus());
        map.put("tradeBonusPercent", sp.getTradeBonusPercent());
        map.put("lifespan", sp.getLifespan());
        map.put("homeworld", sp.getHomeworld());
        if (sp.getNameDatabase() != null) {
            com.yisy.modules.doramcu.earth2026.model.E26NameDatabase db = sp.getNameDatabase();
            Map<String, Object> dbMap = new HashMap<>();
            dbMap.put("maleFirstNames", db.getMaleFirstNames());
            dbMap.put("femaleFirstNames", db.getFemaleFirstNames());
            dbMap.put("lastNames", db.getLastNames());
            dbMap.put("titlePrefixes", db.getTitlePrefixes());
            dbMap.put("titleSuffixes", db.getTitleSuffixes());
            dbMap.put("aiLeaderTitles", db.getAiLeaderTitles());
            dbMap.put("aiGovernorTitles", db.getAiGovernorTitles());
            dbMap.put("aiCaptainTitles", db.getAiCaptainTitles());
            map.put("nameDatabase", dbMap);
        }
        return map;
    }

    private Map<String, Object> techLevelToMap(TechLevel tl) {
        Map<String, Object> map = new HashMap<>();
        map.put("level", tl.getLevel());
        map.put("name", tl.getName());
        map.put("description", tl.getDescription());
        map.put("weaponLevel", tl.getWeaponLevel().name());
        map.put("transportLevel", tl.getTransportLevel().name());
        map.put("commsLevel", tl.getCommsLevel().name());
        map.put("computingLevel", tl.getComputingLevel().name());
        map.put("medicineLevel", tl.getMedicineLevel().name());
        map.put("industryLevel", tl.getIndustryLevel().name());
        map.put("preIndustrial", tl.isPreIndustrial());
        map.put("industrial", tl.isIndustrial());
        map.put("preStellar", tl.isPreStellar());
        map.put("stellar", tl.isStellar());
        return map;
    }

    private Map<String, Object> timePeriodToMap(TimePeriodTemplate tp) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", tp.getName());
        map.put("techLevel", tp.getBaseTechLevel().getLevel());
        map.put("techLevelName", tp.getBaseTechLevel().getName());
        map.put("description", tp.getDescription());
        map.put("dateRange", tp.getDateRange());
        map.put("typicalConflicts", tp.getTypicalConflicts());
        map.put("typicalStructures", tp.getTypicalStructures());
        map.put("structureTypes", tp.getStructureTypes());
        map.put("conflictTypes", tp.getConflictTypes());
        return map;
    }
}
