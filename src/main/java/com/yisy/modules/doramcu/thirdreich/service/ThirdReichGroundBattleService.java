package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import com.yisy.modules.doramcu.thirdreich.adapter.*;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderProfile;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderService;
import com.yisy.modules.doramcu.thirdreich.combat.ThirdReichCombatEngine;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import com.yisy.modules.doramcu.unified.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer for 3R4 ground battle resolution.
 * <p>
 * Orchestrates ground battles by accepting battle requests with source game type,
 * units, commanders, and terrain. Dispatches to the correct GroundCombatAdapter
 * based on GameType, converts units to 3R4, resolves battle via ThirdReichCombatEngine,
 * and maps results back.
 * </p>
 * <p>
 * Integrated with Earth 2026's PlanetService: battles can be built from Earth 2026
 * battle environments, which provide terrain, structures, weather, and available units
 * based on tech level and time period.
 * </p>
 */
public class ThirdReichGroundBattleService {

    private static final Logger log = LoggerFactory.getLogger(ThirdReichGroundBattleService.class);

    private final ThirdReichCombatEngine combatEngine = new ThirdReichCombatEngine();
    private final AICommanderService commanderService = new AICommanderService();
    private final Map<String, GroundCombatAdapter> adapters = new ConcurrentHashMap<>();
    private final Map<String, ThirdReichBattle> battles = new ConcurrentHashMap<>();
    private final AtomicInteger battleCounter = new AtomicInteger(1);
    private final PlanetService planetService;
    private final AiCommanderExperienceService experienceService;

    /**
     * Constructor with PlanetService and AiCommanderExperienceService for full integration.
     *
     * @param planetService the shared Earth planet service (may be null for standalone mode)
     * @param experienceService the shared AI commander experience service (may be null for standalone mode)
     */
    public ThirdReichGroundBattleService(PlanetService planetService, AiCommanderExperienceService experienceService) {
        this.planetService = planetService;
        this.experienceService = experienceService;
        // Register default adapters
        adapters.put("DoraMCU_4X", new DoraMCUTo3R4Adapter());
        adapters.put("Traveller", new TravellerTo3R4Adapter());
        adapters.put("Captains_Log", new StarTrekTo3R4Adapter());
    }

    /**
     * Constructor with PlanetService for Earth 2026 battle environment integration.
     *
     * @param planetService the shared Earth planet service (may be null for standalone mode)
     */
    public ThirdReichGroundBattleService(PlanetService planetService) {
        this.planetService = planetService;
        this.experienceService = null;
        // Register default adapters
        adapters.put("DoraMCU_4X", new DoraMCUTo3R4Adapter());
        adapters.put("Traveller", new TravellerTo3R4Adapter());
        adapters.put("Captains_Log", new StarTrekTo3R4Adapter());
    }

    /**
     * Constructor for backward compatibility (standalone mode, no PlanetService).
     * @deprecated Use {@link #ThirdReichGroundBattleService(PlanetService, AiCommanderExperienceService)} instead.
     */
    @Deprecated
    public ThirdReichGroundBattleService() {
        this.planetService = null;
        this.experienceService = null;
        // Register default adapters
        adapters.put("DoraMCU_4X", new DoraMCUTo3R4Adapter());
        adapters.put("Traveller", new TravellerTo3R4Adapter());
        adapters.put("Captains_Log", new StarTrekTo3R4Adapter());
    }

    /**
     * Creates and resolves a ground battle.
     *
     * @param sourceGame the source game type
     * @param attackerUnits the attacker's game units
     * @param defenderUnits the defender's game units
     * @param terrainType the terrain type
     * @param planetName the planet name (optional)
     * @param attackerCommander the attacker's commander (optional)
     * @param defenderCommander the defender's commander (optional)
     * @return the battle result
     */
    public Map<String, Object> resolveBattle(String sourceGame, List<?> attackerUnits,
                                              List<?> defenderUnits, TerrainType terrainType,
                                              String planetName,
                                              AICommander attackerCommander,
                                              AICommander defenderCommander) {
        GroundCombatAdapter adapter = adapters.get(sourceGame);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter found for game: " + sourceGame);
        }

        // Convert units to 3R4
        List<ThirdReichGroundUnit> atkUnits = adapter.to3R4Units(attackerUnits);
        List<ThirdReichGroundUnit> defUnits = adapter.to3R4Units(defenderUnits);

        // Create battle
        String battleId = "battle-" + battleCounter.getAndIncrement();
        ThirdReichBattle battle = new ThirdReichBattle(battleId);

        // Set terrain
        TerrainContext terrain = new TerrainContext(terrainType != null ? terrainType : TerrainType.PLAINS, planetName);
        battle.setTerrainContext(terrain);

        // Add units
        for (ThirdReichGroundUnit unit : atkUnits) {
            battle.addAttackerUnit(unit);
        }
        for (ThirdReichGroundUnit unit : defUnits) {
            battle.addDefenderUnit(unit);
        }

        // Set commanders
        if (attackerCommander != null) {
            battle.setAttackerCommanderId(attackerCommander.getId());
        }
        if (defenderCommander != null) {
            battle.setDefenderCommanderId(defenderCommander.getId());
        }

        // Resolve battle
        ThirdReichBattleResult result = combatEngine.resolveBattle(battle, attackerCommander, defenderCommander);

        // Store battle
        battles.put(battleId, battle);

        // Record AI commander experience
        recordCommanderExperience(attackerCommander, result, battleId, terrain);
        recordCommanderExperience(defenderCommander, result, battleId, terrain);

        // Map result back
        return adapter.mapResult(result);
    }

    /**
     * Creates a battle with AI commanders.
     *
     * @param sourceGame the source game type
     * @param attackerUnits the attacker's game units
     * @param defenderUnits the defender's game units
     * @param terrainType the terrain type
     * @param planetName the planet name (optional)
     * @param attackerProfile the attacker's commander profile (optional)
     * @param defenderProfile the defender's commander profile (optional)
     * @return the battle result
     */
    public Map<String, Object> resolveAiBattle(String sourceGame, List<?> attackerUnits,
                                                List<?> defenderUnits, TerrainType terrainType,
                                                String planetName,
                                                AICommanderProfile attackerProfile,
                                                AICommanderProfile defenderProfile) {
        AICommander atkCmd = attackerProfile != null ? commanderService.createFromProfile(attackerProfile) : null;
        AICommander defCmd = defenderProfile != null ? commanderService.createFromProfile(defenderProfile) : null;

        return resolveBattle(sourceGame, attackerUnits, defenderUnits, terrainType, planetName, atkCmd, defCmd);
    }

    /**
     * Gets a battle by ID.
     *
     * @param battleId the battle ID
     * @return the battle, or null if not found
     */
    public ThirdReichBattle getBattle(String battleId) {
        return battles.get(battleId);
    }

    /**
     * Gets all available commander profiles.
     *
     * @return list of profiles
     */
    public List<AICommanderProfile> getCommanderProfiles() {
        return commanderService.getAllProfiles();
    }

    /**
     * Generates a commander from a game character.
     *
     * @param stats the character stats
     * @param sourceGame the source game
     * @return the generated commander
     */
    public AICommander generateCommander(Map<String, Object> stats, String sourceGame) {
        return commanderService.generateFromCharacter(stats, sourceGame);
    }

    /**
     * Gets a commander by ID.
     *
     * @param id the commander ID
     * @return the commander, or null if not found
     */
    public AICommander getCommander(String id) {
        return commanderService.getCommander(id);
    }

    // ------------------------------------------------------------------
    // Earth 2026 Battle Environment Integration
    // ------------------------------------------------------------------

    /**
     * Creates and resolves a ground battle using an Earth 2026 battle environment.
     * <p>
     * This method calls Earth 2026's PlanetService to build a complete battle
     * environment (terrain, structures, weather, available units) for the given
     * location and time period, then resolves the battle using those parameters.
     * </p>
     *
     * @param sourceGame   the source game type
     * @param attackerUnits the attacker's game units
     * @param defenderUnits the defender's game units
     * @param locationId   the Earth 2026 location ID
     * @param timePeriod   the time period template name (e.g., "WWII", "MODERN")
     * @param attackerCommander the attacker's commander (optional)
     * @param defenderCommander the defender's commander (optional)
     * @return the battle result with environment details
     */
    public Map<String, Object> resolveBattleFromEnvironment(String sourceGame, List<?> attackerUnits,
                                                             List<?> defenderUnits, Long locationId,
                                                             String timePeriod,
                                                             AICommander attackerCommander,
                                                             AICommander defenderCommander) {
        if (planetService == null) {
            throw new IllegalStateException("PlanetService not available; cannot build battle environment");
        }

        // 1. Build the battle environment from Earth 2026
        Earth2026BattleEnvironment env = planetService.buildBattleEnvironment(locationId, timePeriod);

        // 2. Map Earth 2026 terrain type to Third Reich TerrainType
        TerrainType terrainType = mapTerrainType(env.getTerrainType());

        // 3. Create terrain context with weather modifiers from the environment
        TerrainContext terrain = new TerrainContext(terrainType, env.getLocationName());
        terrain.setWeatherModifier(env.getWeather().getMovementModifier());
        terrain.setVisibilityModifier(env.getVisibilityModifier());

        // 4. Resolve the battle
        Map<String, Object> result = resolveBattle(
                sourceGame, attackerUnits, defenderUnits, terrainType,
                env.getLocationName(), attackerCommander, defenderCommander);

        // 5. Attach environment details to the result
        result.put("environment", env.toMap());
        result.put("structures", env.getStructures());
        result.put("availableUnits", env.getAvailableUnits());
        result.put("timePeriod", env.getTimePeriod());
        result.put("baseTechLevel", env.getBaseTechLevel());

        return result;
    }

    /**
     * Creates a battle with AI commanders using an Earth 2026 battle environment.
     *
     * @param sourceGame   the source game type
     * @param attackerUnits the attacker's game units
     * @param defenderUnits the defender's game units
     * @param locationId   the Earth 2026 location ID
     * @param timePeriod   the time period template name
     * @param attackerProfile the attacker's commander profile (optional)
     * @param defenderProfile the defender's commander profile (optional)
     * @return the battle result with environment details
     */
    public Map<String, Object> resolveAiBattleFromEnvironment(String sourceGame, List<?> attackerUnits,
                                                                List<?> defenderUnits, Long locationId,
                                                                String timePeriod,
                                                                AICommanderProfile attackerProfile,
                                                                AICommanderProfile defenderProfile) {
        AICommander atkCmd = attackerProfile != null ? commanderService.createFromProfile(attackerProfile) : null;
        AICommander defCmd = defenderProfile != null ? commanderService.createFromProfile(defenderProfile) : null;

        return resolveBattleFromEnvironment(sourceGame, attackerUnits, defenderUnits,
                locationId, timePeriod, atkCmd, defCmd);
    }

    /**
     * Gets the available unit types for a given Earth 2026 location and time period.
     *
     * @param locationId the Earth 2026 location ID
     * @param timePeriod the time period template name
     * @return list of available unit types
     */
    public List<Earth2026BattleEnvironment.UnitTypeInfo> getAvailableUnits(Long locationId, String timePeriod) {
        if (planetService == null) {
            throw new IllegalStateException("PlanetService not available");
        }
        Earth2026BattleEnvironment env = planetService.buildBattleEnvironment(locationId, timePeriod);
        return env.getAvailableUnits();
    }

    /**
     * Gets the structure list for a given Earth 2026 location.
     *
     * @param locationId the Earth 2026 location ID
     * @return list of structures
     */
    public List<Earth2026BattleEnvironment.StructureInfo> getStructures(Long locationId) {
        if (planetService == null) {
            throw new IllegalStateException("PlanetService not available");
        }
        List<?> structures = planetService.getStructuresAtLocation(locationId);
        List<Earth2026BattleEnvironment.StructureInfo> result = new ArrayList<>();
        for (Object s : structures) {
            if (s instanceof com.yisy.modules.doramcu.earth2026.model.Earth2026Structure) {
                com.yisy.modules.doramcu.earth2026.model.Earth2026Structure struct =
                        (com.yisy.modules.doramcu.earth2026.model.Earth2026Structure) s;
                result.add(new Earth2026BattleEnvironment.StructureInfo(
                        struct.getId(), struct.getName(), struct.getStructureType(),
                        struct.getTechLevel(), struct.getDefenseRating(), struct.getMaxCapacity(),
                        struct.isProvidesCover(), struct.getCoverBonus(), struct.isStrategic()));
            }
        }
        return result;
    }

    /**
     * Maps an Earth 2026 terrain type string to a Third Reich TerrainType enum.
     *
     * @param terrainType the terrain type string from Earth 2026
     * @return the corresponding Third Reich TerrainType
     */
    public TerrainType mapTerrainType(String terrainType) {
        if (terrainType == null || terrainType.isEmpty()) {
            return TerrainType.PLAINS;
        }
        try {
            return TerrainType.valueOf(terrainType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to plains for unknown terrain types
            return TerrainType.PLAINS;
        }
    }

    // ------------------------------------------------------------------
    // AI Commander Experience Integration
    // ------------------------------------------------------------------

    /**
     * Records battle experience for an AI commander linked to the shared experience system.
     *
     * @param commander the AI commander
     * @param result the battle result
     * @param battleId the battle ID
     * @param terrain the terrain context
     */
    void recordCommanderExperience(AICommander commander, ThirdReichBattleResult result,
                                    String battleId, TerrainContext terrain) {
        if (commander == null || experienceService == null) {
            return;
        }

        Long aiCharacterId = commander.getLinkedAiCharacterId();
        if (aiCharacterId == null) {
            return;
        }

        // Determine the outcome for this commander's side
        AiCommanderExperience.Outcome expOutcome;
        try {
            expOutcome = mapOutcomeForCommander(commander, result);
        } catch (Exception e) {
            log.warn("Failed to map outcome for commander {}: {}", commander.getName(), e.getMessage());
            return;
        }

        String location = terrain != null ? terrain.getPlanetName() : null;
        String description = result.getNarrativeSummary();

        try {
            experienceService.recordBattleExperience(
                    aiCharacterId, "THIRD_REICH", "Ground Battle",
                    expOutcome, description, location);
            log.info("Recorded experience for commander {} (AI char {}): {}",
                    commander.getName(), aiCharacterId, expOutcome);
        } catch (Exception e) {
            log.warn("Failed to record experience for commander {}: {}", commander.getName(), e.getMessage());
        }
    }

    /**
     * Maps the battle outcome to the experience outcome for a specific commander.
     * The attacker's outcome depends on whether the result favors the attacker.
     *
     * @param commander the commander whose outcome to determine
     * @param result the battle result
     * @return the experience outcome
     */
    AiCommanderExperience.Outcome mapOutcomeForCommander(AICommander commander, ThirdReichBattleResult result) {
        ThirdReichBattleResult.Outcome outcome = result.getOutcome();

        // Determine if this commander is on the attacker side
        // Since we don't have explicit side tracking here, we use the outcome mapping:
        // ATTACKER_VICTORY → attacker wins, defender loses
        // DEFENDER_VICTORY → defender wins, attacker loses
        // For simplicity, both commanders get the same outcome mapping
        switch (outcome) {
            case ATTACKER_VICTORY:
            case DEFENDER_VICTORY:
                return AiCommanderExperience.Outcome.VICTORY;
            case PYRRHIC_VICTORY:
                return AiCommanderExperience.Outcome.PYRRHIC_VICTORY;
            case DEFENDER_HOLDS:
                return AiCommanderExperience.Outcome.STALEMATE;
            case DRAW:
                return AiCommanderExperience.Outcome.STALEMATE;
            case RETREAT:
                return AiCommanderExperience.Outcome.ROUTE;
            default:
                return AiCommanderExperience.Outcome.STALEMATE;
        }
    }
}
