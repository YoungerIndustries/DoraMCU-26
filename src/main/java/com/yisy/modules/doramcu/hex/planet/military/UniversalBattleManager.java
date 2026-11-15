package com.yisy.modules.doramcu.hex.planet.military;

import com.yisy.modules.doramcu.hex.planet.TerrainType;
import com.yisy.modules.doramcu.thirdreich.combat.ThirdReichCombatEngine;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Central battle coordinator that accepts units from any game module,
 * converts them to ThirdReichGroundUnit for resolution via the 3rd Reich
 * combat engine, then maps results back to UniversalBattleUnit.
 * <p>
 * This is the core of the universal battle system — any module can
 * participate in battles by implementing {@link ModuleBattleAdapter}.
 * </p>
 * <p>
 * Example flow:
 * <ol>
 *   <li>MechWarrior module creates UniversalBattleUnit via MechWarriorBattleAdapter</li>
 *   <li>Star Wars module creates UniversalBattleUnit via StarWarsBattleAdapter</li>
 *   <li>UniversalBattleManager converts both to ThirdReichGroundUnit</li>
 *   <li>3rd Reich engine resolves the battle</li>
 *   <li>Results are mapped back to UniversalBattleUnit with module metadata</li>
 *   <li>FPS renderer shows MechWarrior mech vs Star Wars AT-ST in 3D</li>
 * </ol>
 * </p>
 */
@Service
public class UniversalBattleManager {

    private static final Logger log = LoggerFactory.getLogger(UniversalBattleManager.class);

    private final Map<String, ModuleBattleAdapter> moduleAdapters;
    private final ThirdReichUnitAdapter thirdReichAdapter;
    private final ThirdReichCombatEngine combatEngine;
    private final Map<String, UniversalBattle> activeBattles;
    private final List<UniversalBattleRecord> battleHistory;
    private final AtomicInteger battleIdCounter;

    public UniversalBattleManager(List<ModuleBattleAdapter> adapters) {
        this.moduleAdapters = new HashMap<>();
        this.thirdReichAdapter = new ThirdReichUnitAdapter();
        this.combatEngine = new ThirdReichCombatEngine();
        this.activeBattles = new ConcurrentHashMap<>();
        this.battleHistory = new ArrayList<>();
        this.battleIdCounter = new AtomicInteger(1);

        // Register all module adapters
        for (ModuleBattleAdapter adapter : adapters) {
            moduleAdapters.put(adapter.getModuleId(), adapter);
            log.info("Registered battle adapter: {} ({})", adapter.getModuleName(), adapter.getModuleId());
        }
    }

    @PostConstruct
    public void init() {
        log.info("UniversalBattleManager initialized with {} module adapters: {}",
                moduleAdapters.size(), moduleAdapters.keySet());
    }

    /**
     * Request to start a universal battle.
     */
    public static class UniversalBattleRequest {
        final String attackerFaction;
        final String defenderFaction;
        final List<UniversalBattleUnit> attackerUnits;
        final List<UniversalBattleUnit> defenderUnits;
        final UnifiedBattleResolver.BattleType battleType;
        final int hexX, hexY, hexZ;
        final TerrainType terrain;

        public UniversalBattleRequest(String attackerFaction, String defenderFaction,
                                      List<UniversalBattleUnit> attackerUnits,
                                      List<UniversalBattleUnit> defenderUnits,
                                      UnifiedBattleResolver.BattleType battleType,
                                      int hexX, int hexY, int hexZ, TerrainType terrain) {
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.attackerUnits = attackerUnits;
            this.defenderUnits = defenderUnits;
            this.battleType = battleType;
            this.hexX = hexX;
            this.hexY = hexY;
            this.hexZ = hexZ;
            this.terrain = terrain;
        }

        public String getAttackerFaction() { return attackerFaction; }
        public String getDefenderFaction() { return defenderFaction; }
        public List<UniversalBattleUnit> getAttackerUnits() { return attackerUnits; }
        public List<UniversalBattleUnit> getDefenderUnits() { return defenderUnits; }
        public UnifiedBattleResolver.BattleType getBattleType() { return battleType; }
        public int getHexX() { return hexX; }
        public int getHexY() { return hexY; }
        public int getHexZ() { return hexZ; }
        public TerrainType getTerrain() { return terrain; }
    }

    /**
     * Active battle tracking.
     */
    public static class UniversalBattle {
        private final String battleId;
        private final UnifiedBattleResolver.BattleType battleType;
        private final String attackerFaction;
        private final String defenderFaction;
        private final List<UniversalBattleUnit> attackerUnits;
        private final List<UniversalBattleUnit> defenderUnits;
        private int currentRound;
        private ThirdReichBattleResult.Outcome outcome;
        private final long startTime;

        public UniversalBattle(String battleId, UnifiedBattleResolver.BattleType battleType,
                               String attackerFaction, String defenderFaction,
                               List<UniversalBattleUnit> attackerUnits,
                               List<UniversalBattleUnit> defenderUnits) {
            this.battleId = battleId;
            this.battleType = battleType;
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.attackerUnits = new ArrayList<>(attackerUnits);
            this.defenderUnits = new ArrayList<>(defenderUnits);
            this.currentRound = 0;
            this.startTime = System.currentTimeMillis();
        }

        public String getBattleId() { return battleId; }
        public UnifiedBattleResolver.BattleType getBattleType() { return battleType; }
        public String getAttackerFaction() { return attackerFaction; }
        public String getDefenderFaction() { return defenderFaction; }
        public List<UniversalBattleUnit> getAttackerUnits() { return attackerUnits; }
        public List<UniversalBattleUnit> getDefenderUnits() { return defenderUnits; }
        public int getCurrentRound() { return currentRound; }
        public void setCurrentRound(int round) { this.currentRound = round; }
        public ThirdReichBattleResult.Outcome getOutcome() { return outcome; }
        public void setOutcome(ThirdReichBattleResult.Outcome outcome) { this.outcome = outcome; }
        public long getStartTime() { return startTime; }
    }

    /**
     * Record of a completed universal battle.
     */
    public static class UniversalBattleRecord {
        private final String battleId;
        private final UnifiedBattleResolver.BattleType battleType;
        private final String attackerFaction;
        private final String defenderFaction;
        private final ThirdReichBattleResult.Outcome outcome;
        private final int attackerCasualties;
        private final int defenderCasualties;
        private final int rounds;
        private final long timestamp;
        private final Set<String> modulesInvolved;

        public UniversalBattleRecord(String battleId, UnifiedBattleResolver.BattleType battleType,
                                     String attackerFaction, String defenderFaction,
                                     ThirdReichBattleResult.Outcome outcome,
                                     int attackerCasualties, int defenderCasualties,
                                     int rounds, long timestamp, Set<String> modulesInvolved) {
            this.battleId = battleId;
            this.battleType = battleType;
            this.attackerFaction = attackerFaction;
            this.defenderFaction = defenderFaction;
            this.outcome = outcome;
            this.attackerCasualties = attackerCasualties;
            this.defenderCasualties = defenderCasualties;
            this.rounds = rounds;
            this.timestamp = timestamp;
            this.modulesInvolved = modulesInvolved;
        }

        public String getBattleId() { return battleId; }
        public UnifiedBattleResolver.BattleType getBattleType() { return battleType; }
        public String getAttackerFaction() { return attackerFaction; }
        public String getDefenderFaction() { return defenderFaction; }
        public ThirdReichBattleResult.Outcome getOutcome() { return outcome; }
        public int getAttackerCasualties() { return attackerCasualties; }
        public int getDefenderCasualties() { return defenderCasualties; }
        public int getRounds() { return rounds; }
        public long getTimestamp() { return timestamp; }
        public Set<String> getModulesInvolved() { return modulesInvolved; }
    }

    /**
     * Start and resolve a universal battle.
     *
     * @param request the battle request containing units from any modules
     * @return the battle result
     */
    public ThirdReichBattleResult resolveBattle(UniversalBattleRequest request) {
        String battleId = "UBATTLE-" + battleIdCounter.getAndIncrement();
        log.info("Resolving universal {} battle: {} vs {} at ({},{},{})",
                request.battleType, request.attackerFaction, request.defenderFaction,
                request.hexX, request.hexY, request.hexZ);

        // Collect all modules involved
        Set<String> modulesInvolved = new HashSet<>();
        for (UniversalBattleUnit unit : request.attackerUnits) {
            modulesInvolved.add(unit.getModuleId());
        }
        for (UniversalBattleUnit unit : request.defenderUnits) {
            modulesInvolved.add(unit.getModuleId());
        }
        log.info("Modules involved in battle: {}", modulesInvolved);

        // Convert UniversalBattleUnit to ThirdReichGroundUnit
        List<ThirdReichGroundUnit> trAttackerUnits = convertToThirdReichUnits(request.attackerUnits);
        List<ThirdReichGroundUnit> trDefenderUnits = convertToThirdReichUnits(request.defenderUnits);

        // Create the battle
        ThirdReichBattle battle = new ThirdReichBattle(battleId);

        // Add attacker units
        for (ThirdReichGroundUnit unit : trAttackerUnits) {
            battle.addAttackerUnit(unit);
        }

        // Add defender units
        for (ThirdReichGroundUnit unit : trDefenderUnits) {
            battle.addDefenderUnit(unit);
        }

        // Set terrain context
        TerrainContext terrainContext = createTerrainContext(request.terrain, request.battleType);
        battle.setTerrainContext(terrainContext);

        // Apply battle type modifiers
        applyBattleTypeModifiers(battle, request.battleType);

        // Track as active battle
        UniversalBattle activeBattle = new UniversalBattle(
                battleId, request.battleType,
                request.attackerFaction, request.defenderFaction,
                request.attackerUnits, request.defenderUnits
        );
        activeBattles.put(battleId, activeBattle);

        // Resolve the battle
        ThirdReichBattleResult result = combatEngine.resolveBattle(battle, null, null);

        // Update active battle
        activeBattle.setCurrentRound(result.getRoundsFought());
        activeBattle.setOutcome(result.getOutcome());

        // Apply damage back to UniversalBattleUnits
        applyBattleResults(request.attackerUnits, trAttackerUnits, result);
        applyBattleResults(request.defenderUnits, trDefenderUnits, result);

        // Record the battle
        UniversalBattleRecord record = new UniversalBattleRecord(
                battleId, request.battleType,
                request.attackerFaction, request.defenderFaction,
                result.getOutcome(),
                result.getAttackerCasualties(), result.getDefenderCasualties(),
                result.getRoundsFought(), System.currentTimeMillis(), modulesInvolved
        );
        battleHistory.add(record);

        // Remove from active battles
        activeBattles.remove(battleId);

        log.info("Universal battle {} result: {} (attacker casualties: {}, defender casualties: {}, modules: {})",
                battleId, result.getOutcome(), result.getAttackerCasualties(),
                result.getDefenderCasualties(), modulesInvolved);

        return result;
    }

    /**
     * Convert UniversalBattleUnit list to ThirdReichGroundUnit list.
     */
    private List<ThirdReichGroundUnit> convertToThirdReichUnits(List<UniversalBattleUnit> units) {
        return units.stream().map(unit -> {
            // Map GenericUnitType to ThirdReichUnitType
            ThirdReichUnitType trType = thirdReichAdapter.mapToThirdReichUnit(unit.getGenericType());

            ThirdReichGroundUnit trUnit = new ThirdReichGroundUnit(
                    unit.getId(), unit.getName(), trType
            );
            trUnit.setStrength(unit.getStrength());
            trUnit.setMaxStrength(unit.getMaxStrength());
            trUnit.setMorale(unit.getMorale());
            trUnit.setAttackValue(unit.getAttackValue());
            trUnit.setDefenseValue(unit.getDefenseValue());
            trUnit.setMobility(unit.getMobility());
            trUnit.setSupplyCost(unit.getSupplyCost());
            trUnit.setCommanderId(unit.getCommanderId());
            trUnit.setSourceGame(unit.getModuleId());
            trUnit.setAiCoreBonus(unit.getAiCoreBonus());

            return trUnit;
        }).collect(Collectors.toList());
    }

    /**
     * Apply battle results back to UniversalBattleUnits.
     */
    private void applyBattleResults(List<UniversalBattleUnit> universalUnits,
                                     List<ThirdReichGroundUnit> trUnits,
                                     ThirdReichBattleResult result) {
        for (int i = 0; i < universalUnits.size() && i < trUnits.size(); i++) {
            UniversalBattleUnit uUnit = universalUnits.get(i);
            ThirdReichGroundUnit trUnit = trUnits.get(i);

            // Copy back strength and morale from ThirdReich unit
            uUnit.setStrength(trUnit.getStrength());
            uUnit.setMorale(trUnit.getMorale());
        }
    }

    /**
     * Create a terrain context from planet terrain and battle type.
     */
    private TerrainContext createTerrainContext(TerrainType terrain, UnifiedBattleResolver.BattleType battleType) {
        com.yisy.modules.doramcu.thirdreich.domain.TerrainType trTerrain;

        if (battleType == UnifiedBattleResolver.BattleType.NAVAL) {
            trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.SWAMP;
        } else if (battleType == UnifiedBattleResolver.BattleType.SPACE) {
            trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.PLAINS;
        } else {
            switch (terrain) {
                case MOUNTAIN:
                case HIGHLAND:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.MOUNTAIN;
                    break;
                case FOREST:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.FOREST;
                    break;
                case DESERT:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.DESERT;
                    break;
                case SWAMP:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.SWAMP;
                    break;
                default:
                    trTerrain = com.yisy.modules.doramcu.thirdreich.domain.TerrainType.PLAINS;
                    break;
            }
        }

        return new TerrainContext(trTerrain);
    }

    /**
     * Apply battle type modifiers to units.
     */
    private void applyBattleTypeModifiers(ThirdReichBattle battle, UnifiedBattleResolver.BattleType battleType) {
        double attackModifier = 1.0;
        double defenseModifier = 1.0;

        switch (battleType) {
            case NAVAL:
                attackModifier = 0.9;
                defenseModifier = 1.2;
                break;
            case SPACE:
                attackModifier = 1.2;
                defenseModifier = 0.8;
                break;
            case GROUND:
            default:
                break;
        }

        for (ThirdReichGroundUnit unit : battle.getAttackerUnits()) {
            unit.setAttackValue((int) (unit.getAttackValue() * attackModifier));
            unit.setDefenseValue((int) (unit.getDefenseValue() * defenseModifier));
        }

        for (ThirdReichGroundUnit unit : battle.getDefenderUnits()) {
            unit.setAttackValue((int) (unit.getAttackValue() * attackModifier));
            unit.setDefenseValue((int) (unit.getDefenseValue() * defenseModifier));
        }
    }

    /**
     * Get an active battle by ID.
     */
    public Optional<UniversalBattle> getActiveBattle(String battleId) {
        return Optional.ofNullable(activeBattles.get(battleId));
    }

    /**
     * Get all active battles.
     */
    public Collection<UniversalBattle> getActiveBattles() {
        return Collections.unmodifiableCollection(activeBattles.values());
    }

    /**
     * Get battle history.
     */
    public List<UniversalBattleRecord> getBattleHistory() {
        return new ArrayList<>(battleHistory);
    }

    /**
     * Get recent battles (last N).
     */
    public List<UniversalBattleRecord> getRecentBattles(int count) {
        int size = battleHistory.size();
        int from = Math.max(0, size - count);
        return new ArrayList<>(battleHistory.subList(from, size));
    }

    /**
     * Get all registered module adapters.
     */
    public Map<String, ModuleBattleAdapter> getModuleAdapters() {
        return Collections.unmodifiableMap(moduleAdapters);
    }

    /**
     * Get a specific module adapter by ID.
     */
    public Optional<ModuleBattleAdapter> getModuleAdapter(String moduleId) {
        return Optional.ofNullable(moduleAdapters.get(moduleId));
    }

    /**
     * List all available unit types across all modules.
     */
    public Map<String, Map<String, String>> getAllModuleUnitTypes() {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map.Entry<String, ModuleBattleAdapter> entry : moduleAdapters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAvailableUnitTypes());
        }
        return result;
    }
}
