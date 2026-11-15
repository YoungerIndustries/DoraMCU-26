package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster;
import com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster.MonsterType;
import com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster.SizeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing terrain monster encounters in SFB battles.
 * <p>
 * Handles monster spawning based on terrain type, encounter probability,
 * combat resolution between monsters and ships, and monster lifecycle.
 * </p>
 * <p>
 * Terrain monsters can appear in battles based on the sector's terrain type.
 * Different terrain types have different encounter tables and probabilities.
 * </p>
 */
@Service
public class SfbTerrainMonsterService {

    private static final Logger log = LoggerFactory.getLogger(SfbTerrainMonsterService.class);

    /**
     * Terrain types that can spawn monsters.
     */
    public enum TerrainType {
        /** Normal space — low encounter rate. */
        NORMAL_SPACE,
        /** Nebula — medium encounter rate, favors Plasma Wasps. */
        NEBULA,
        /** Asteroid field — medium encounter rate, favors Space Amoebas. */
        ASTEROID_FIELD,
        /** Dark nebula — high encounter rate, favors Void Krakens. */
        DARK_NEBULA,
        /** Radiation zone — high encounter rate, favors Space Dragons. */
        RADIATION_ZONE,
        /** Gravity well — very high encounter rate, any monster. */
        GRAVITY_WELL,
        /** Anomaly — highest encounter rate, multiple monsters possible. */
        ANOMALY
    }

    /**
     * Encounter result after a monster attack.
     */
    public static class EncounterResult {
        private final String monsterId;
        private final String monsterName;
        private final MonsterType monsterType;
        private final int damageDealt;
        private final String abilityUsed;
        private final boolean monsterAlive;
        private final int monsterHpRemaining;
        private final Map<String, Object> details;

        private EncounterResult(Builder builder) {
            this.monsterId = builder.monsterId;
            this.monsterName = builder.monsterName;
            this.monsterType = builder.monsterType;
            this.damageDealt = builder.damageDealt;
            this.abilityUsed = builder.abilityUsed;
            this.monsterAlive = builder.monsterAlive;
            this.monsterHpRemaining = builder.monsterHpRemaining;
            this.details = Collections.unmodifiableMap(new HashMap<>(builder.details));
        }

        public String getMonsterId() { return monsterId; }
        public String getMonsterName() { return monsterName; }
        public MonsterType getMonsterType() { return monsterType; }
        public int getDamageDealt() { return damageDealt; }
        public String getAbilityUsed() { return abilityUsed; }
        public boolean isMonsterAlive() { return monsterAlive; }
        public int getMonsterHpRemaining() { return monsterHpRemaining; }
        public Map<String, Object> getDetails() { return details; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String monsterId;
            private String monsterName;
            private MonsterType monsterType;
            private int damageDealt;
            private String abilityUsed;
            private boolean monsterAlive;
            private int monsterHpRemaining;
            private final Map<String, Object> details = new HashMap<>();

            public Builder monsterId(String id) { this.monsterId = id; return this; }
            public Builder monsterName(String name) { this.monsterName = name; return this; }
            public Builder monsterType(MonsterType type) { this.monsterType = type; return this; }
            public Builder damageDealt(int damage) { this.damageDealt = damage; return this; }
            public Builder abilityUsed(String ability) { this.abilityUsed = ability; return this; }
            public Builder monsterAlive(boolean alive) { this.monsterAlive = alive; return this; }
            public Builder monsterHpRemaining(int hp) { this.monsterHpRemaining = hp; return this; }
            public Builder detail(String key, Object value) { this.details.put(key, value); return this; }
            public EncounterResult build() { return new EncounterResult(this); }
        }
    }

    /**
     * Per-battle monster tracking.
     */
    private final Map<String, List<SfbTerrainMonster>> battleMonsters = new ConcurrentHashMap<>();

    /**
     * Encounter probability by terrain type (0.0 to 1.0).
     */
    private static final Map<TerrainType, Double> ENCOUNTER_PROBABILITY = Map.of(
            TerrainType.NORMAL_SPACE, 0.1,
            TerrainType.NEBULA, 0.3,
            TerrainType.ASTEROID_FIELD, 0.35,
            TerrainType.DARK_NEBULA, 0.5,
            TerrainType.RADIATION_ZONE, 0.5,
            TerrainType.GRAVITY_WELL, 0.7,
            TerrainType.ANOMALY, 0.9
    );

    /**
     * Preferred monster types by terrain.
     */
    private static final Map<TerrainType, List<MonsterType>> TERRAIN_PREFERENCES = Map.of(
            TerrainType.NORMAL_SPACE, List.of(MonsterType.SPACE_AMOEBA, MonsterType.PLASMA_WASP),
            TerrainType.NEBULA, List.of(MonsterType.PLASMA_WASP, MonsterType.VOID_KRAKEN),
            TerrainType.ASTEROID_FIELD, List.of(MonsterType.SPACE_AMOEBA, MonsterType.SPACE_DRAGON),
            TerrainType.DARK_NEBULA, List.of(MonsterType.VOID_KRAKEN, MonsterType.SPACE_DRAGON),
            TerrainType.RADIATION_ZONE, List.of(MonsterType.SPACE_DRAGON, MonsterType.PLASMA_WASP),
            TerrainType.GRAVITY_WELL, List.of(MonsterType.SPACE_DRAGON, MonsterType.VOID_KRAKEN, MonsterType.SPACE_WHALE),
            TerrainType.ANOMALY, List.of(MonsterType.SPACE_AMOEBA, MonsterType.SPACE_DRAGON, MonsterType.PLASMA_WASP, MonsterType.VOID_KRAKEN, MonsterType.SPACE_WHALE)
    );

    private static final Random RANDOM = new Random();

    /**
     * Checks if a monster encounter should occur based on terrain type.
     */
    public boolean shouldEncounterOccur(TerrainType terrain) {
        double probability = ENCOUNTER_PROBABILITY.getOrDefault(terrain, 0.2);
        return RANDOM.nextDouble() < probability;
    }

    /**
     * Checks if a monster encounter should occur using a roll (1-100).
     * @param roll a number 1-100
     * @param terrain the terrain type
     * @return true if encounter occurs
     */
    public boolean shouldEncounterOccur(int roll, TerrainType terrain) {
        double probability = ENCOUNTER_PROBABILITY.getOrDefault(terrain, 0.2);
        return roll <= (probability * 100);
    }

    /**
     * Gets the encounter probability for a terrain type.
     */
    public double getEncounterProbability(TerrainType terrain) {
        return ENCOUNTER_PROBABILITY.getOrDefault(terrain, 0.2);
    }

    /**
     * Spawns a random monster based on terrain type.
     * @param battleId the battle/session ID
     * @param terrain the terrain type
     * @param positionX hex X position
     * @param positionY hex Y position
     * @return the spawned monster, or null if no encounter
     */
    public SfbTerrainMonster spawnMonster(String battleId, TerrainType terrain, int positionQ, int positionR) {
        if (!shouldEncounterOccur(terrain)) {
            return null;
        }
        return spawnMonsterOfType(battleId, terrain, positionQ, positionR);
    }

    /**
     * Spawns a specific monster type.
     * @param battleId the battle/session ID
     * @param terrain the terrain type (affects which variant spawns)
     * @param positionX hex X position
     * @param positionY hex Y position
     * @return the spawned monster
     */
    public SfbTerrainMonster spawnMonsterOfType(String battleId, TerrainType terrain, int positionQ, int positionR) {
        List<MonsterType> preferences = TERRAIN_PREFERENCES.getOrDefault(terrain, List.of(MonsterType.SPACE_AMOEBA));
        MonsterType type = preferences.get(RANDOM.nextInt(preferences.size()));
        return spawnSpecificType(battleId, type, positionQ, positionR);
    }

    /**
     * Spawns a specific monster type at the given position.
     */
    public SfbTerrainMonster spawnSpecificType(String battleId, MonsterType type, int positionQ, int positionR) {
        SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(type, String.valueOf(positionQ), String.valueOf(positionR));
        battleMonsters.computeIfAbsent(battleId, k -> Collections.synchronizedList(new ArrayList<>()));
        battleMonsters.get(battleId).add(monster);
        log.info("Spawned {} in battle {} at ({}, {})", monster.getName(), battleId, positionQ, positionR);
        return monster;
    }

    /**
     * Spawns multiple monsters for high-danger terrain.
     * @param battleId the battle/session ID
     * @param terrain the terrain type
     * @param maxMonsters maximum number of monsters to spawn
     * @return list of spawned monsters
     */
    public List<SfbTerrainMonster> spawnMultipleMonsters(String battleId, TerrainType terrain, int maxMonsters) {
        List<SfbTerrainMonster> monsters = new ArrayList<>();
        int count = Math.min(maxMonsters, 1 + RANDOM.nextInt(3)); // 1-3 monsters
        for (int i = 0; i < count; i++) {
            int q = RANDOM.nextInt(10);
            int r = RANDOM.nextInt(10);
            SfbTerrainMonster monster = spawnMonsterOfType(battleId, terrain, q, r);
            if (monster != null) {
                monsters.add(monster);
            }
        }
        return monsters;
    }

    /**
     * Gets all active monsters in a battle.
     */
    public List<SfbTerrainMonster> getBattleMonsters(String battleId) {
        List<SfbTerrainMonster> monsters = battleMonsters.get(battleId);
        if (monsters == null) {
            return Collections.emptyList();
        }
        return monsters.stream()
                .filter(SfbTerrainMonster::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Gets all monsters (including inactive) in a battle.
     */
    public List<SfbTerrainMonster> getAllBattleMonsters(String battleId) {
        List<SfbTerrainMonster> monsters = battleMonsters.get(battleId);
        if (monsters == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(monsters);
    }

    /**
     * Gets a monster by ID in a battle.
     */
    public Optional<SfbTerrainMonster> getMonster(String battleId, String monsterId) {
        List<SfbTerrainMonster> monsters = battleMonsters.get(battleId);
        if (monsters == null) {
            return Optional.empty();
        }
        return monsters.stream()
                .filter(m -> m.getId().equals(monsterId))
                .findFirst();
    }

    /**
     * Gets monsters by type in a battle.
     */
    public List<SfbTerrainMonster> getMonstersByType(String battleId, MonsterType type) {
        return getBattleMonsters(battleId).stream()
                .filter(m -> m.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Resolves a monster's attack on a ship.
     * @param monster the attacking monster
     * @param shipDefense the ship's shield value in the attacked direction
     * @param impulse current impulse number
     * @return the encounter result
     */
    public EncounterResult resolveMonsterAttack(SfbTerrainMonster monster, int shipDefense, int impulse) {
        if (monster == null || !monster.isActive()) {
            return EncounterResult.builder()
                    .monsterId("none")
                    .monsterName("No monster")
                    .damageDealt(0)
                    .monsterAlive(false)
                    .build();
        }

        int damage = monster.attack(shipDefense);
        String ability = monster.useSpecialAbility(impulse);

        return EncounterResult.builder()
                .monsterId(monster.getId())
                .monsterName(monster.getName())
                .monsterType(monster.getType())
                .damageDealt(damage)
                .abilityUsed(ability)
                .monsterAlive(monster.isActive())
                .monsterHpRemaining(monster.getHitPointsRemaining())
                .detail("impulse", impulse)
                .detail("shipDefense", shipDefense)
                .build();
    }

    /**
     * A ship attacks a monster.
     * @param monster the target monster
     * @param weaponDamage the weapon's damage value
     * @return the encounter result with monster's status
     */
    public EncounterResult shipAttacksMonster(SfbTerrainMonster monster, int weaponDamage) {
        if (monster == null) {
            return EncounterResult.builder()
                    .monsterId("none")
                    .monsterName("No monster")
                    .damageDealt(0)
                    .monsterAlive(false)
                    .build();
        }

        int remaining = monster.takeDamage(weaponDamage);
        boolean alive = monster.isActive();

        return EncounterResult.builder()
                .monsterId(monster.getId())
                .monsterName(monster.getName())
                .monsterType(monster.getType())
                .damageDealt(weaponDamage)
                .monsterAlive(alive)
                .monsterHpRemaining(remaining)
                .detail("weaponDamage", weaponDamage)
                .detail("monsterDefeated", !alive)
                .build();
    }

    /**
     * Moves all active monsters in a battle toward the nearest ship.
     * @param battleId the battle ID
     * @param shipX ship hex X
     * @param shipY ship hex Y
     */
    public void moveAllMonsters(String battleId, int shipQ, int shipR) {
        List<SfbTerrainMonster> monsters = battleMonsters.get(battleId);
        if (monsters == null) return;

        monsters.stream()
                .filter(SfbTerrainMonster::isActive)
                .forEach(m -> m.moveToward(shipQ, shipR));
    }

    /**
     * Removes a monster from a battle.
     */
    public boolean removeMonster(String battleId, String monsterId) {
        List<SfbTerrainMonster> monsters = battleMonsters.get(battleId);
        if (monsters == null) return false;
        return monsters.removeIf(m -> m.getId().equals(monsterId));
    }

    /**
     * Clears all monsters from a battle.
     */
    public void clearBattleMonsters(String battleId) {
        battleMonsters.remove(battleId);
    }

    /**
     * Gets the number of active monsters in a battle.
     */
    public int getActiveMonsterCount(String battleId) {
        return getBattleMonsters(battleId).size();
    }

    /**
     * Gets encounter statistics for a battle.
     */
    public Map<String, Object> getEncounterStatistics(String battleId) {
        List<SfbTerrainMonster> all = getAllBattleMonsters(battleId);
        List<SfbTerrainMonster> active = getBattleMonsters(battleId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("battleId", battleId);
        stats.put("totalMonsters", all.size());
        stats.put("activeMonsters", active.size());
        stats.put("defeatedMonsters", all.size() - active.size());

        // By type
        Map<String, Long> byType = active.stream()
                .collect(Collectors.groupingBy(m -> m.getType().name(), Collectors.counting()));
        stats.put("byType", byType);

        // By size
        Map<String, Long> bySize = active.stream()
                .collect(Collectors.groupingBy(m -> m.getSizeClass().name(), Collectors.counting()));
        stats.put("bySize", bySize);

        // Total HP
        int totalHp = active.stream().mapToInt(SfbTerrainMonster::getHitPointsRemaining).sum();
        stats.put("totalHpRemaining", totalHp);

        return stats;
    }

    /**
     * Lists all terrain types and their encounter probabilities.
     */
    public List<Map<String, Object>> listTerrainTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (TerrainType terrain : TerrainType.values()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("terrain", terrain.name());
            info.put("probability", getEncounterProbability(terrain));
            info.put("preferredMonsters", TERRAIN_PREFERENCES.getOrDefault(terrain, Collections.emptyList()));
            types.add(info);
        }
        return types;
    }

    /**
     * Lists all monster types with their stats.
     */
    public List<Map<String, Object>> listMonsterTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (MonsterType type : MonsterType.values()) {
            SfbTerrainMonster sample = SfbTerrainMonster.createPredefined(type, "0", "0");
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("type", type.name());
            info.put("name", sample.getName());
            info.put("sizeClass", sample.getSizeClass().name());
            info.put("hitPoints", sample.getHitPoints());
            info.put("attackPower", sample.getAttackPower());
            info.put("defense", sample.getDefense());
            info.put("movementSpeed", sample.getMovementSpeed());
            info.put("attackRange", sample.getAttackRange());
            info.put("specialAbility", sample.getSpecialAbility());
            info.put("abilities", sample.getAbilities());
            types.add(info);
        }
        return types;
    }
}
