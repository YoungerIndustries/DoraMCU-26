package com.yisy.modules.doramcu.thirdreich.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderProfile;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattle;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainType;
import com.yisy.modules.doramcu.thirdreich.service.ThirdReichGroundBattleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for 3R4 ground battle resolution.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/v1/thirdreich/ground/battles - create and resolve a ground battle</li>
 *   <li>POST /api/v1/thirdreich/ground/battles/ai - create battle with AI commanders</li>
 *   <li>POST /api/v1/thirdreich/ground/battles/environment - create battle from Earth 2026 environment</li>
 *   <li>POST /api/v1/thirdreich/ground/battles/environment/ai - create environment battle with AI commanders</li>
 *   <li>GET /api/v1/thirdreich/ground/battles/{id} - get battle status and results</li>
 *   <li>GET /api/v1/thirdreich/ground/units/{locationId} - get available units for a location/time period</li>
 *   <li>GET /api/v1/thirdreich/ground/structures/{locationId} - get structures at a location</li>
 *   <li>GET /api/v1/thirdreich/ground/commanders - list available AI commander profiles</li>
 *   <li>POST /api/v1/thirdreich/ground/commanders/generate - generate commander from a game character</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/thirdreich/ground")
public class ThirdReichGroundBattleController {

    private final ThirdReichGroundBattleService service;

    public ThirdReichGroundBattleController(PlanetService planetService,
                                            AiCommanderExperienceService experienceService) {
        this.service = new ThirdReichGroundBattleService(planetService, experienceService);
    }

    /**
     * Creates and resolves a ground battle.
     *
     * @param request the battle request containing source game, units, terrain, and commanders
     * @return the battle result
     */
    @PostMapping("/battles")
    public Map<String, Object> resolveBattle(@RequestBody Map<String, Object> request) {
        String sourceGame = (String) request.getOrDefault("sourceGame", "DoraMCU_4X");
        @SuppressWarnings("unchecked")
        List<?> attackerUnits = (List<?>) request.getOrDefault("attackerUnits", List.of());
        @SuppressWarnings("unchecked")
        List<?> defenderUnits = (List<?>) request.getOrDefault("defenderUnits", List.of());
        String terrainName = (String) request.getOrDefault("terrain", "PLAINS");
        String planetName = (String) request.get("planetName");

        TerrainType terrain = TerrainType.PLAINS;
        try {
            terrain = TerrainType.valueOf(terrainName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to plains
        }

        Map<String, Object> result = service.resolveBattle(
                sourceGame, attackerUnits, defenderUnits, terrain, planetName, null, null);

        // Add battle ID
        result.put("battleId", ((ThirdReichBattle) service.getBattle(
                "battle-" + (service.getBattle(null) != null ? 1 : 1))).getId());

        return result;
    }

    /**
     * Creates a battle with AI commanders.
     *
     * @param request the battle request with commander profiles
     * @return the battle result
     */
    @PostMapping("/battles/ai")
    public Map<String, Object> resolveAiBattle(@RequestBody Map<String, Object> request) {
        String sourceGame = (String) request.getOrDefault("sourceGame", "DoraMCU_4X");
        @SuppressWarnings("unchecked")
        List<?> attackerUnits = (List<?>) request.getOrDefault("attackerUnits", List.of());
        @SuppressWarnings("unchecked")
        List<?> defenderUnits = (List<?>) request.getOrDefault("defenderUnits", List.of());
        String terrainName = (String) request.getOrDefault("terrain", "PLAINS");
        String planetName = (String) request.get("planetName");
        String attackerProfileName = (String) request.get("attackerCommander");
        String defenderProfileName = (String) request.get("defenderCommander");

        TerrainType terrain = TerrainType.PLAINS;
        try {
            terrain = TerrainType.valueOf(terrainName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to plains
        }

        AICommanderProfile atkProfile = null;
        AICommanderProfile defProfile = null;

        if (attackerProfileName != null) {
            try {
                atkProfile = AICommanderProfile.valueOf(attackerProfileName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid profile
            }
        }
        if (defenderProfileName != null) {
            try {
                defProfile = AICommanderProfile.valueOf(defenderProfileName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid profile
            }
        }

        return service.resolveAiBattle(sourceGame, attackerUnits, defenderUnits, terrain, planetName, atkProfile, defProfile);
    }

    /**
     * Gets a battle by ID.
     *
     * @param battleId the battle ID
     * @return the battle details
     */
    @GetMapping("/battles/{id}")
    public Map<String, Object> getBattle(@PathVariable String battleId) {
        ThirdReichBattle battle = service.getBattle(battleId);
        if (battle == null) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("error", "Battle not found: " + battleId);
            return notFound;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", battle.getId());
        result.put("state", battle.getState().name());
        result.put("rounds", battle.getCurrentRound());
        result.put("events", battle.getEvents());
        if (battle.getResult() != null) {
            result.put("outcome", battle.getResult().getOutcome().name());
            result.put("narrative", battle.getResult().getNarrativeSummary());
            result.put("attackerCasualties", battle.getResult().getAttackerCasualties());
            result.put("defenderCasualties", battle.getResult().getDefenderCasualties());
        }
        return result;
    }

    /**
     * Lists all available AI commander profiles.
     *
     * @return list of commander profiles
     */
    @GetMapping("/commanders")
    public List<AICommanderProfile> getCommanders() {
        return service.getCommanderProfiles();
    }

    /**
     * Generates a commander from a game character.
     *
     * @param request the character stats and source game
     * @return the generated commander
     */
    @PostMapping("/commanders/generate")
    public Map<String, Object> generateCommander(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) request.getOrDefault("stats", new HashMap<>());
        String sourceGame = (String) request.getOrDefault("sourceGame", "DoraMCU_4X");

        AICommander commander = service.generateCommander(stats, sourceGame);
        if (commander == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate commander");
            return error;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", commander.getId());
        result.put("name", commander.getName());
        result.put("leadership", commander.getLeadership());
        result.put("traits", commander.getTraits());
        result.put("tacticalPreference", commander.getTacticalPreference().name());
        result.put("sourceGame", commander.getSourceGame());
        return result;
    }

    // ------------------------------------------------------------------
    // Earth 2026 Battle Environment Integration Endpoints
    // ------------------------------------------------------------------

    /**
     * Creates and resolves a ground battle using an Earth 2026 battle environment.
     * <p>
     * The battle environment is built from Earth 2026's PlanetService, providing
     * terrain, structures, weather, and available units based on the location
     * and time period.
     * </p>
     *
     * @param request the battle request with locationId, timePeriod, and units
     * @return the battle result with environment details
     */
    @PostMapping("/battles/environment")
    public ResponseEntity<?> resolveBattleFromEnvironment(@RequestBody Map<String, Object> request) {
        try {
            String sourceGame = (String) request.getOrDefault("sourceGame", "DoraMCU_4X");
            Long locationId = Long.valueOf(request.get("locationId").toString());
            String timePeriod = (String) request.getOrDefault("timePeriod", "WWII");
            @SuppressWarnings("unchecked")
            List<?> attackerUnits = (List<?>) request.getOrDefault("attackerUnits", List.of());
            @SuppressWarnings("unchecked")
            List<?> defenderUnits = (List<?>) request.getOrDefault("defenderUnits", List.of());

            Map<String, Object> result = service.resolveBattleFromEnvironment(
                    sourceGame, attackerUnits, defenderUnits, locationId, timePeriod, null, null);

            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PlanetService not available: " + e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid request: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Creates a battle with AI commanders using an Earth 2026 battle environment.
     *
     * @param request the battle request with locationId, timePeriod, units, and commander profiles
     * @return the battle result with environment details
     */
    @PostMapping("/battles/environment/ai")
    public ResponseEntity<?> resolveAiBattleFromEnvironment(@RequestBody Map<String, Object> request) {
        try {
            String sourceGame = (String) request.getOrDefault("sourceGame", "DoraMCU_4X");
            Long locationId = Long.valueOf(request.get("locationId").toString());
            String timePeriod = (String) request.getOrDefault("timePeriod", "WWII");
            @SuppressWarnings("unchecked")
            List<?> attackerUnits = (List<?>) request.getOrDefault("attackerUnits", List.of());
            @SuppressWarnings("unchecked")
            List<?> defenderUnits = (List<?>) request.getOrDefault("defenderUnits", List.of());
            String attackerProfileName = (String) request.get("attackerCommander");
            String defenderProfileName = (String) request.get("defenderCommander");

            AICommanderProfile atkProfile = null;
            AICommanderProfile defProfile = null;

            if (attackerProfileName != null) {
                try {
                    atkProfile = AICommanderProfile.valueOf(attackerProfileName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Ignore invalid profile
                }
            }
            if (defenderProfileName != null) {
                try {
                    defProfile = AICommanderProfile.valueOf(defenderProfileName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Ignore invalid profile
                }
            }

            Map<String, Object> result = service.resolveAiBattleFromEnvironment(
                    sourceGame, attackerUnits, defenderUnits, locationId, timePeriod, atkProfile, defProfile);

            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PlanetService not available: " + e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid request: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Gets the available unit types for a given Earth 2026 location and time period.
     *
     * @param locationId the Earth 2026 location ID
     * @param timePeriod the time period template name (query param, default: WWII)
     * @return list of available unit types
     */
    @GetMapping("/units/{locationId}")
    public ResponseEntity<?> getAvailableUnits(@PathVariable Long locationId,
                                                @RequestParam(defaultValue = "WWII") String timePeriod) {
        try {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units = service.getAvailableUnits(locationId, timePeriod);
            return ResponseEntity.ok(units);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PlanetService not available: " + e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid request: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Gets the structure list for a given Earth 2026 location.
     *
     * @param locationId the Earth 2026 location ID
     * @return list of structures
     */
    @GetMapping("/structures/{locationId}")
    public ResponseEntity<?> getStructures(@PathVariable Long locationId) {
        try {
            List<Earth2026BattleEnvironment.StructureInfo> structures = service.getStructures(locationId);
            return ResponseEntity.ok(structures);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PlanetService not available: " + e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid request: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
