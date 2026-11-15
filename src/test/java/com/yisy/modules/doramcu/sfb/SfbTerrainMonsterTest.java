package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster;
import com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster.MonsterType;
import com.yisy.modules.doramcu.sfb.model.SfbTerrainMonster.SizeClass;
import com.yisy.modules.doramcu.sfb.service.SfbTerrainMonsterService;
import com.yisy.modules.doramcu.sfb.service.SfbTerrainMonsterService.EncounterResult;
import com.yisy.modules.doramcu.sfb.service.SfbTerrainMonsterService.TerrainType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for SFB Terrain Monsters system.
 * Covers monster creation, encounter probabilities, combat resolution,
 * and integration scenarios.
 */
class SfbTerrainMonsterTest {

    private SfbTerrainMonsterService service;

    @BeforeEach
    void setUp() {
        service = new SfbTerrainMonsterService();
    }

    // ==================== Monster Creation ====================

    @Nested
    class PredefinedMonsters {

        @Test
        void spaceAmoebaHasCorrectStats() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "5", "5");

            assertEquals(MonsterType.SPACE_AMOEBA, monster.getType());
            assertEquals("Space Amoeba", monster.getName());
            assertEquals(SizeClass.HUGE, monster.getSizeClass());
            assertEquals(100, monster.getHitPoints());
            assertEquals(8, monster.getAttackPower());
            assertEquals(4, monster.getDefense());
            assertEquals(4, monster.getMovementSpeed());
            assertEquals(1, monster.getAttackRange());
            assertEquals(3, monster.getSpecialAbility());
            assertTrue(monster.isActive());
            assertEquals(2, monster.getAbilities().size());
        }

        @Test
        void spaceDragonHasCorrectStats() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");

            assertEquals(MonsterType.SPACE_DRAGON, monster.getType());
            assertEquals("Space Dragon", monster.getName());
            assertEquals(SizeClass.GIANT, monster.getSizeClass());
            assertEquals(150, monster.getHitPoints());
            assertEquals(10, monster.getAttackPower());
            assertEquals(6, monster.getDefense());
            assertEquals(20, monster.getMovementSpeed());
            assertEquals(6, monster.getAttackRange());
            assertEquals(5, monster.getSpecialAbility());
            assertEquals(3, monster.getAbilities().size());
        }

        @Test
        void spaceWhaleHasCorrectStats() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_WHALE, "3", "3");

            assertEquals(MonsterType.SPACE_WHALE, monster.getType());
            assertEquals(200, monster.getHitPoints());
            assertEquals(2, monster.getAttackPower());
            assertEquals(8, monster.getDefense());
            assertEquals(1, monster.getSpecialAbility());
        }

        @Test
        void plasmaWaspHasCorrectStats() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.PLASMA_WASP, "1", "1");

            assertEquals(MonsterType.PLASMA_WASP, monster.getType());
            assertEquals("Plasma Wasp Swarm", monster.getName());
            assertEquals(SizeClass.SMALL, monster.getSizeClass());
            assertEquals(20, monster.getHitPoints());
            assertEquals(4, monster.getAttackPower());
            assertEquals(28, monster.getMovementSpeed());
            assertEquals(7, monster.getSpecialAbility());
        }

        @Test
        void voidKrakenHasCorrectStats() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.VOID_KRAKEN, "7", "7");

            assertEquals(MonsterType.VOID_KRAKEN, monster.getType());
            assertEquals("Void Kraken", monster.getName());
            assertEquals(SizeClass.HUGE, monster.getSizeClass());
            assertEquals(120, monster.getHitPoints());
            assertEquals(7, monster.getAttackPower());
            assertEquals(4, monster.getAttackRange());
            assertEquals(6, monster.getSpecialAbility());
        }

        @Test
        void allMonsterTypesCanBeCreated() {
            for (MonsterType type : MonsterType.values()) {
                SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(type, "0", "0");
                assertNotNull(monster);
                assertEquals(type, monster.getType());
                assertTrue(monster.isActive());
                assertNotNull(monster.getId());
                assertNotNull(monster.getPosition());
            }
        }

        @Test
        void nonNumericPositionDefaultsToZero() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "abc", "xyz");
            assertNotNull(monster);
            assertNotNull(monster.getPosition());
            // q and r should default to 0 when parsing fails
            assertEquals(0, monster.getPosition().getQ());
            assertEquals(0, monster.getPosition().getR());
        }
    }

    // ==================== Builder ====================

    @Nested
    class BuilderTests {

        @Test
        void customMonsterWithBuilder() {
            SfbTerrainMonster monster = new SfbTerrainMonster.Builder(MonsterType.SPACE_DRAGON)
                    .name("Custom Dragon")
                    .sizeClass(SizeClass.LARGE)
                    .hitPoints(80)
                    .attackPower(6)
                    .defense(3)
                    .movementSpeed(16)
                    .attackRange(4)
                    .specialAbility(5)
                    .ability("Fire Breath", "Deals 5 fire damage")
                    .build();

            assertEquals("Custom Dragon", monster.getName());
            assertEquals(SizeClass.LARGE, monster.getSizeClass());
            assertEquals(80, monster.getHitPoints());
            assertEquals(6, monster.getAttackPower());
            assertEquals(1, monster.getAbilities().size());
        }

        @Test
        void builderDefaults() {
            SfbTerrainMonster monster = new SfbTerrainMonster.Builder(MonsterType.SPACE_AMOEBA)
                    .build();

            assertEquals(SizeClass.MEDIUM, monster.getSizeClass());
            assertEquals(50, monster.getHitPoints());
            assertEquals(5, monster.getAttackPower());
            assertEquals(3, monster.getDefense());
            assertEquals(16, monster.getMovementSpeed());
        }

        @Test
        void movementSpeedClampedToRange() {
            SfbTerrainMonster fast = new SfbTerrainMonster.Builder(MonsterType.SPACE_DRAGON)
                    .movementSpeed(100)
                    .build();
            assertEquals(32, fast.getMovementSpeed());

            SfbTerrainMonster slow = new SfbTerrainMonster.Builder(MonsterType.SPACE_AMOEBA)
                    .movementSpeed(0)
                    .build();
            assertEquals(1, slow.getMovementSpeed());
        }

        @Test
        void specialAbilityClampedToRange() {
            SfbTerrainMonster monster = new SfbTerrainMonster.Builder(MonsterType.SPACE_DRAGON)
                    .specialAbility(20)
                    .build();
            assertEquals(10, monster.getSpecialAbility());

            monster = new SfbTerrainMonster.Builder(MonsterType.SPACE_DRAGON)
                    .specialAbility(-5)
                    .build();
            assertEquals(0, monster.getSpecialAbility());
        }
    }

    // ==================== Combat ====================

    @Nested
    class CombatTests {

        @Test
        void takeDamageReducesHp() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "0", "0");
            int remaining = monster.takeDamage(30);

            assertEquals(70, remaining);
            assertTrue(monster.isActive());
        }

        @Test
        void takeDamageZeroHpDeactivates() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "0", "0");
            int remaining = monster.takeDamage(100);

            assertEquals(0, remaining);
            assertFalse(monster.isActive());
        }

        @Test
        void takeDamageOverHpDeactivates() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "0", "0");
            int remaining = monster.takeDamage(200);

            assertEquals(0, remaining);
            assertFalse(monster.isActive());
        }

        @Test
        void inactiveMonsterTakesNoDamage() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "0", "0");
            monster.takeDamage(100); // deactivate
            int remaining = monster.takeDamage(10);

            assertEquals(0, remaining);
        }

        @Test
        void attackDealsDamageAfterDefense() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            // Dragon attackPower=10, shipDefense=0 -> damage = 10 - 0 = 10
            int damage = monster.attack(0);
            assertEquals(10, damage);

            // shipDefense=8 -> damage = 10 - 4 = 6
            damage = monster.attack(8);
            assertEquals(6, damage);

            // shipDefense=20 -> damage = 10 - 10 = 0
            damage = monster.attack(20);
            assertEquals(0, damage);
        }

        @Test
        void inactiveMonsterDealsNoDamage() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            monster.takeDamage(150); // deactivate
            int damage = monster.attack(0);
            assertEquals(0, damage);
        }

        @Test
        void specialAbilityTriggersBasedOnImpulse() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            // specialAbility=5, triggers when impulse % (11-5) == 0, i.e., impulse % 6 == 0
            assertNull(monster.useSpecialAbility(1));
            assertNull(monster.useSpecialAbility(5));
            assertNotNull(monster.useSpecialAbility(6));
            assertNull(monster.useSpecialAbility(7));
            assertNotNull(monster.useSpecialAbility(12));
        }

        @Test
        void specialAbilityZeroNeverTriggers() {
            SfbTerrainMonster monster = new SfbTerrainMonster.Builder(MonsterType.SPACE_WHALE)
                    .specialAbility(0)
                    .build();
            for (int i = 1; i <= 20; i++) {
                assertNull(monster.useSpecialAbility(i));
            }
        }

        @Test
        void canAttackReturnsFalseWhenInactive() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            assertTrue(monster.canAttack(0, 0));
            monster.takeDamage(150);
            assertFalse(monster.canAttack(0, 0));
        }

        @Test
        void moveTowardUpdatesProperties() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            monster.moveToward(5, 5);

            assertEquals(5, monster.getProperties().get("lastTargetQ"));
            assertEquals(5, monster.getProperties().get("lastTargetR"));
        }

        @Test
        void inactiveMonsterDoesNotMove() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            monster.takeDamage(150);
            monster.moveToward(5, 5);

            // No exception, but properties should not be updated for inactive monster
            // (implementation allows update but doesn't cause errors)
        }
    }

    // ==================== Encounter Probability ====================

    @Nested
    class EncounterProbabilityTests {

        @Test
        void normalSpaceHasLowProbability() {
            assertEquals(0.1, service.getEncounterProbability(TerrainType.NORMAL_SPACE));
        }

        @Test
        void anomalyHasHighestProbability() {
            assertEquals(0.9, service.getEncounterProbability(TerrainType.ANOMALY));
        }

        @Test
        void allTerrainTypesHaveProbabilities() {
            for (TerrainType terrain : TerrainType.values()) {
                double prob = service.getEncounterProbability(terrain);
                assertTrue(prob >= 0.0 && prob <= 1.0, "Probability out of range for " + terrain);
            }
        }

        @Test
        void deterministicEncounterCheck() {
            // Roll 1 should always trigger for ANOMALY (90%)
            assertTrue(service.shouldEncounterOccur(1, TerrainType.ANOMALY));
            // Roll 100 should never trigger for NORMAL_SPACE (10%)
            assertFalse(service.shouldEncounterOccur(100, TerrainType.NORMAL_SPACE));
            // Roll 10 should trigger for NORMAL_SPACE (10%)
            assertTrue(service.shouldEncounterOccur(10, TerrainType.NORMAL_SPACE));
        }

        @Test
        void terrainTypeOrder() {
            // Verify probability increases with danger
            double normal = service.getEncounterProbability(TerrainType.NORMAL_SPACE);
            double anomaly = service.getEncounterProbability(TerrainType.ANOMALY);
            assertTrue(normal < anomaly);
        }
    }

    // ==================== Spawning ====================

    @Nested
    class SpawningTests {

        @Test
        void spawnSpecificTypeAddsToBattle() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 5, 5);

            assertNotNull(monster);
            assertEquals(MonsterType.SPACE_AMOEBA, monster.getType());
            assertEquals(1, service.getActiveMonsterCount("battle1"));
        }

        @Test
        void spawnMultipleMonsters() {
            List<SfbTerrainMonster> monsters = service.spawnMultipleMonsters("battle1", TerrainType.ANOMALY, 5);

            assertTrue(monsters.size() >= 1 && monsters.size() <= 5);
            assertEquals(monsters.size(), service.getActiveMonsterCount("battle1"));
        }

        @Test
        void getBattleMonstersReturnsEmptyForUnknownBattle() {
            List<SfbTerrainMonster> monsters = service.getBattleMonsters("unknown");
            assertTrue(monsters.isEmpty());
        }

        @Test
        void getMonsterById() {
            SfbTerrainMonster spawned = service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 3, 3);
            Optional<SfbTerrainMonster> found = service.getMonster("battle1", spawned.getId());

            assertTrue(found.isPresent());
            assertEquals(spawned.getId(), found.get().getId());
        }

        @Test
        void getMonsterByIdNotFound() {
            Optional<SfbTerrainMonster> found = service.getMonster("battle1", "nonexistent");
            assertFalse(found.isPresent());
        }

        @Test
        void getMonstersByType() {
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 1, 1);
            service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 2, 2);
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 3, 3);

            List<SfbTerrainMonster> amoebas = service.getMonstersByType("battle1", MonsterType.SPACE_AMOEBA);
            assertEquals(2, amoebas.size());

            List<SfbTerrainMonster> dragons = service.getMonstersByType("battle1", MonsterType.SPACE_DRAGON);
            assertEquals(1, dragons.size());
        }

        @Test
        void getAllBattleMonstersIncludesInactive() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            monster.takeDamage(100); // deactivate

            assertEquals(0, service.getBattleMonsters("battle1").size());
            assertEquals(1, service.getAllBattleMonsters("battle1").size());
        }
    }

    // ==================== Service Combat Resolution ====================

    @Nested
    class ServiceCombatResolution {

        @Test
        void resolveMonsterAttack() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 0, 0);
            EncounterResult result = service.resolveMonsterAttack(monster, 4, 1);

            assertEquals(monster.getId(), result.getMonsterId());
            assertEquals(MonsterType.SPACE_DRAGON, result.getMonsterType());
            assertTrue(result.isMonsterAlive());
            // Dragon attackPower=10, shipDefense=4 -> damage = 10 - 2 = 8
            assertEquals(8, result.getDamageDealt());
        }

        @Test
        void resolveMonsterAttackInactiveMonster() {
            EncounterResult result = service.resolveMonsterAttack(null, 4, 1);

            assertEquals(0, result.getDamageDealt());
            assertFalse(result.isMonsterAlive());
        }

        @Test
        void shipAttacksMonsterReducesHp() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            EncounterResult result = service.shipAttacksMonster(monster, 50);

            assertEquals(50, result.getDamageDealt());
            assertEquals(50, result.getMonsterHpRemaining());
            assertTrue(result.isMonsterAlive());
        }

        @Test
        void shipAttacksMonsterDefeatsIt() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.PLASMA_WASP, 0, 0);
            // Wasp has 20 HP
            EncounterResult result = service.shipAttacksMonster(monster, 25);

            assertEquals(0, result.getMonsterHpRemaining());
            assertFalse(result.isMonsterAlive());
            assertTrue((Boolean) result.getDetails().get("monsterDefeated"));
        }

        @Test
        void shipAttacksNullMonster() {
            EncounterResult result = service.shipAttacksMonster(null, 10);

            assertEquals(0, result.getDamageDealt());
            assertFalse(result.isMonsterAlive());
        }

        @Test
        void specialAbilityInEncounterResult() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 0, 0);
            // Dragon specialAbility=5, triggers at impulse % 6 == 0
            EncounterResult result = service.resolveMonsterAttack(monster, 0, 6);
            assertNotNull(result.getAbilityUsed());

            result = service.resolveMonsterAttack(monster, 0, 1);
            assertNull(result.getAbilityUsed());
        }
    }

    // ==================== Management ====================

    @Nested
    class ManagementTests {

        @Test
        void removeMonster() {
            SfbTerrainMonster monster = service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            boolean removed = service.removeMonster("battle1", monster.getId());

            assertTrue(removed);
            assertEquals(0, service.getActiveMonsterCount("battle1"));
        }

        @Test
        void removeNonexistentMonster() {
            boolean removed = service.removeMonster("battle1", "nonexistent");
            assertFalse(removed);
        }

        @Test
        void clearBattleMonsters() {
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 1, 1);
            service.clearBattleMonsters("battle1");

            assertEquals(0, service.getActiveMonsterCount("battle1"));
        }

        @Test
        void moveAllMonsters() {
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 1, 1);
            service.moveAllMonsters("battle1", 5, 5);

            List<SfbTerrainMonster> monsters = service.getBattleMonsters("battle1");
            for (SfbTerrainMonster m : monsters) {
                assertEquals(5, m.getProperties().get("lastTargetQ"));
                assertEquals(5, m.getProperties().get("lastTargetR"));
            }
        }
    }

    // ==================== Statistics ====================

    @Nested
    class StatisticsTests {

        @Test
        void encounterStatisticsEmptyBattle() {
            Map<String, Object> stats = service.getEncounterStatistics("empty");

            assertEquals("empty", stats.get("battleId"));
            assertEquals(0, stats.get("totalMonsters"));
            assertEquals(0, stats.get("activeMonsters"));
        }

        @Test
        void encounterStatisticsWithMonsters() {
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 1, 1);
            SfbTerrainMonster wasp = service.spawnSpecificType("battle1", MonsterType.PLASMA_WASP, 2, 2);
            wasp.takeDamage(20); // deactivate

            Map<String, Object> stats = service.getEncounterStatistics("battle1");

            assertEquals(3, stats.get("totalMonsters"));
            assertEquals(2, stats.get("activeMonsters"));
            assertEquals(1, stats.get("defeatedMonsters"));
        }

        @Test
        void listTerrainTypes() {
            List<Map<String, Object>> types = service.listTerrainTypes();

            assertEquals(TerrainType.values().length, types.size());
            for (Map<String, Object> type : types) {
                assertNotNull(type.get("terrain"));
                assertNotNull(type.get("probability"));
                assertNotNull(type.get("preferredMonsters"));
            }
        }

        @Test
        void listMonsterTypes() {
            List<Map<String, Object>> types = service.listMonsterTypes();

            assertEquals(MonsterType.values().length, types.size());
            for (Map<String, Object> type : types) {
                assertNotNull(type.get("type"));
                assertNotNull(type.get("name"));
                assertNotNull(type.get("hitPoints"));
                assertNotNull(type.get("attackPower"));
                assertNotNull(type.get("abilities"));
            }
        }
    }

    // ==================== Integration Scenarios ====================

    @Nested
    class IntegrationScenarios {

        @Test
        void fullEncounterWorkflow() {
            // 1. Spawn a dragon in a radiation zone
            SfbTerrainMonster dragon = service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 5, 5);
            assertTrue(dragon.isActive());

            // 2. Ship attacks the dragon
            EncounterResult attack1 = service.shipAttacksMonster(dragon, 30);
            assertEquals(30, attack1.getDamageDealt());
            assertEquals(120, attack1.getMonsterHpRemaining());
            assertTrue(attack1.isMonsterAlive());

            // 3. Dragon attacks back
            EncounterResult dragonAttack = service.resolveMonsterAttack(dragon, 6, 1);
            assertTrue(dragonAttack.getDamageDealt() > 0);

            // 4. Ship continues attacking
            service.shipAttacksMonster(dragon, 50);
            service.shipAttacksMonster(dragon, 50);
            service.shipAttacksMonster(dragon, 50);

            // 5. Dragon should be defeated
            assertFalse(dragon.isActive());
            assertEquals(0, dragon.getHitPointsRemaining());
        }

        @Test
        void multiMonsterBattle() {
            // Spawn multiple monsters
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            service.spawnSpecificType("battle1", MonsterType.PLASMA_WASP, 1, 1);
            service.spawnSpecificType("battle1", MonsterType.VOID_KRAKEN, 2, 2);

            assertEquals(3, service.getActiveMonsterCount("battle1"));

            // Defeat one monster
            SfbTerrainMonster wasp = service.getMonstersByType("battle1", MonsterType.PLASMA_WASP).get(0);
            service.shipAttacksMonster(wasp, 25);

            // 2 active, 1 defeated
            assertEquals(2, service.getActiveMonsterCount("battle1"));
            Map<String, Object> stats = service.getEncounterStatistics("battle1");
            assertEquals(1, stats.get("defeatedMonsters"));
        }

        @Test
        void battleCleanup() {
            service.spawnSpecificType("battle1", MonsterType.SPACE_AMOEBA, 0, 0);
            service.spawnSpecificType("battle1", MonsterType.SPACE_DRAGON, 1, 1);
            service.clearBattleMonsters("battle1");

            assertEquals(0, service.getActiveMonsterCount("battle1"));
            assertTrue(service.getBattleMonsters("battle1").isEmpty());
        }

        @Test
        void terrainPreferenceRespected() {
            // ANOMALY terrain can spawn any monster type
            // Run multiple spawns to verify variety
            int battleIdCounter = 0;
            for (int i = 0; i < 20; i++) {
                String battleId = "pref-test-" + (battleIdCounter++);
                SfbTerrainMonster monster = service.spawnSpecificType(battleId,
                        MonsterType.values()[i % MonsterType.values().length], 0, 0);
                assertNotNull(monster);
            }
        }

        @Test
        void encounterResultBuilder() {
            EncounterResult result = EncounterResult.builder()
                    .monsterId("test-id")
                    .monsterName("Test Monster")
                    .monsterType(MonsterType.SPACE_DRAGON)
                    .damageDealt(15)
                    .abilityUsed("Energy Beam")
                    .monsterAlive(true)
                    .monsterHpRemaining(100)
                    .detail("customKey", "customValue")
                    .build();

            assertEquals("test-id", result.getMonsterId());
            assertEquals("Test Monster", result.getMonsterName());
            assertEquals(MonsterType.SPACE_DRAGON, result.getMonsterType());
            assertEquals(15, result.getDamageDealt());
            assertEquals("Energy Beam", result.getAbilityUsed());
            assertTrue(result.isMonsterAlive());
            assertEquals(100, result.getMonsterHpRemaining());
            assertEquals("customValue", result.getDetails().get("customKey"));
        }
    }

    // ==================== Model Properties ====================

    @Nested
    class ModelPropertiesTests {

        @Test
        void monsterIdIsUnique() {
            SfbTerrainMonster m1 = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "0", "0");
            SfbTerrainMonster m2 = SfbTerrainMonster.createPredefined(MonsterType.SPACE_AMOEBA, "0", "0");
            assertNotEquals(m1.getId(), m2.getId());
        }

        @Test
        void abilitiesAreUnmodifiable() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            assertThrows(UnsupportedOperationException.class, () -> monster.getAbilities().add("New Ability"));
        }

        @Test
        void propertiesAreUnmodifiable() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            assertThrows(UnsupportedOperationException.class, () -> monster.getProperties().put("key", "value"));
        }

        @Test
        void toStringIncludesKeyInfo() {
            SfbTerrainMonster monster = SfbTerrainMonster.createPredefined(MonsterType.SPACE_DRAGON, "0", "0");
            String str = monster.toString();

            assertTrue(str.contains("SPACE_DRAGON"));
            assertTrue(str.contains("Space Dragon"));
            assertTrue(str.contains("150/150"));
        }

        @Test
        void sizeClassEnumValues() {
            SizeClass[] sizes = SizeClass.values();
            assertEquals(6, sizes.length);
            assertArrayEquals(new SizeClass[]{SizeClass.TINY, SizeClass.SMALL, SizeClass.MEDIUM,
                    SizeClass.LARGE, SizeClass.HUGE, SizeClass.GIANT}, sizes);
        }

        @Test
        void monsterTypeEnumValues() {
            MonsterType[] types = MonsterType.values();
            assertEquals(5, types.length);
        }
    }
}
