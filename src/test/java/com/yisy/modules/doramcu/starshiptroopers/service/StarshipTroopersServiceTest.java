package com.yisy.modules.doramcu.starshiptroopers.service;

import com.yisy.modules.doramcu.starshiptroopers.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarshipTroopersService Tests")
class StarshipTroopersServiceTest {

    private StarshipTroopersService service;

    @BeforeEach
    void setUp() {
        service = new StarshipTroopersService();
    }

    // ==================== Service Initialization ====================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("should load all JSON files without errors")
        void shouldLoadAllJsonFiles() {
            assertNotNull(service);
            assertFalse(service.getTerranUnits().isEmpty(), "Terran units should not be empty");
            assertFalse(service.getArachnidUnits().isEmpty(), "Arachnid units should not be empty");
            assertFalse(service.getHumanoidUnits().isEmpty(), "Humanoid units should not be empty");
            assertFalse(service.getScenarios().isEmpty(), "Scenarios should not be empty");
            assertFalse(service.getSpecialEquipment().isEmpty(), "Special equipment should not be empty");
            assertFalse(service.getTerranCombatTable().isEmpty(), "Terran combat table should not be empty");
            assertFalse(service.getAlienCombatTable().isEmpty(), "Alien combat table should not be empty");
        }

        @Test
        @DisplayName("should return unmodifiable collections")
        void shouldReturnUnmodifiableCollections() {
            assertThrows(UnsupportedOperationException.class, () -> service.getTerranUnits().add(null));
            assertThrows(UnsupportedOperationException.class, () -> service.getArachnidUnits().add(null));
            assertThrows(UnsupportedOperationException.class, () -> service.getScenarios().add(null));
            assertThrows(UnsupportedOperationException.class, () -> service.getSpecialEquipment().add(null));
        }
    }

    // ==================== Terran Units ====================

    @Nested
    @DisplayName("Terran Units Tests")
    class TerranUnitsTests {

        @Test
        @DisplayName("should load all 10 Terran unit types")
        void shouldLoadAllTerranUnitTypes() {
            assertEquals(10, service.getTerranUnits().size(),
                    "Should have 10 Terran unit types");
        }

        @Test
        @DisplayName("should have correct unit IDs")
        void shouldHaveCorrectUnitIds() {
            Set<String> expectedIds = Set.of(
                    "command", "marauder", "scout", "combat_engineer",
                    "air_car", "special_talent", "listening_device",
                    "heavy_nerve_gas", "retrieval_boat", "rocket_beacon"
            );
            Set<String> actualIds = service.getTerranUnits().stream()
                    .map(Unit::id)
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(expectedIds, actualIds, "All expected Terran unit IDs should be present");
        }

        @Test
        @DisplayName("should have correct Marauder stats")
        void shouldHaveCorrectMarauderStats() {
            Optional<Unit> marauder = service.getTerranUnitById("marauder");
            assertTrue(marauder.isPresent(), "Marauder unit should exist");
            Unit unit = marauder.get();
            assertEquals("Marauder", unit.name());
            assertEquals("Infantry Squad", unit.type());
            assertEquals(6, unit.attackStrength());
            assertEquals(4, unit.defenseStrength());
            assertEquals(6, unit.movementAllowance());
            assertTrue(unit.isCombatUnit());
        }

        @Test
        @DisplayName("should have correct Scout stats with higher movement")
        void shouldHaveCorrectScoutStats() {
            Optional<Unit> scout = service.getTerranUnitById("scout");
            assertTrue(scout.isPresent());
            Unit unit = scout.get();
            assertEquals(9, unit.movementAllowance(), "Scouts should have higher movement");
            assertTrue(unit.movementAllowance() > service.getTerranUnitById("marauder").get().movementAllowance(),
                    "Scout movement should exceed Marauder");
        }

        @Test
        @DisplayName("should identify immobile units correctly")
        void shouldIdentifyImmobileUnits() {
            Optional<Unit> listeningDevice = service.getTerranUnitById("listening_device");
            assertTrue(listeningDevice.isPresent());
            assertTrue(listeningDevice.get().isImmobile(), "Listening device should be immobile");
            assertEquals(0, listeningDevice.get().movementAllowance());

            Optional<Unit> rocketBeacon = service.getTerranUnitById("rocket_beacon");
            assertTrue(rocketBeacon.isPresent());
            assertTrue(rocketBeacon.get().isImmobile(), "Rocket beacon should be immobile");
        }

        @Test
        @DisplayName("should identify heavy weapons correctly")
        void shouldIdentifyHeavyWeapons() {
            Optional<Unit> heavyNerveGas = service.getTerranUnitById("heavy_nerve_gas");
            assertTrue(heavyNerveGas.isPresent());
            assertTrue(heavyNerveGas.get().isHeavyWeapon());
            assertEquals(9, heavyNerveGas.get().attackStrength());
            assertEquals(10, heavyNerveGas.get().range());
        }

        @Test
        @DisplayName("should return correct combat units")
        void shouldReturnCorrectCombatUnits() {
            List<Unit> combatUnits = service.getTerranCombatUnits();
            assertFalse(combatUnits.isEmpty());
            combatUnits.forEach(unit ->
                    assertTrue(unit.isCombatUnit(), "All returned units should be combat units"));
            // Command, Marauder, Scout, Combat Engineer, Heavy Nerve Gas
            assertTrue(combatUnits.size() >= 5, "Should have at least 5 combat-capable units");
        }

        @Test
        @DisplayName("should have Combat Engineer with equipment")
        void shouldHaveCombatEngineerWithEquipment() {
            Optional<Unit> engineer = service.getTerranUnitById("combat_engineer");
            assertTrue(engineer.isPresent());
            Unit unit = engineer.get();
            assertNotNull(unit.equipment(), "Combat engineer should have equipment");
            assertFalse(unit.equipment().isEmpty(), "Combat engineer equipment should not be empty");
            assertTrue(unit.equipment().stream()
                    .anyMatch(e -> e.contains("Nuclear")),
                    "Should have nuclear demolition munitions");
        }

        @Test
        @DisplayName("should have correct Air Car stats")
        void shouldHaveCorrectAirCarStats() {
            Optional<Unit> airCar = service.getTerranUnitById("air_car");
            assertTrue(airCar.isPresent());
            assertEquals(12, airCar.get().movementAllowance(), "Air Car should have high movement");
            assertEquals(0, airCar.get().attackStrength(), "Air Car should have no attack");
        }

        @Test
        @DisplayName("should find unit by ID")
        void shouldFindUnitById() {
            assertTrue(service.getTerranUnitById("command").isPresent());
            assertTrue(service.getTerranUnitById("marauder").isPresent());
            assertFalse(service.getTerranUnitById("nonexistent").isPresent());
        }
    }

    // ==================== Arachnid Units ====================

    @Nested
    @DisplayName("Arachnid Units Tests")
    class ArachnidUnitsTests {

        @Test
        @DisplayName("should load all 12 Arachnid unit types")
        void shouldLoadAllArachnidUnitTypes() {
            assertEquals(12, service.getArachnidUnits().size(),
                    "Should have 12 Arachnid unit types");
        }

        @Test
        @DisplayName("should have correct Warrior stats")
        void shouldHaveCorrectWarriorStats() {
            Optional<Unit> warrior = service.getArachnidUnitById("warrior");
            assertTrue(warrior.isPresent());
            Unit unit = warrior.get();
            assertEquals("Warrior Bug", unit.name());
            assertEquals(2, unit.attackStrength());
            assertEquals(1, unit.defenseStrength());
            assertEquals(3, unit.movementAllowance());
            assertEquals(1, unit.victoryPoints());
        }

        @Test
        @DisplayName("should have Brain Bug as high-value target")
        void shouldHaveBrainBugAsHighValueTarget() {
            Optional<Unit> brainBug = service.getArachnidUnitById("brain_bug");
            assertTrue(brainBug.isPresent());
            Unit unit = brainBug.get();
            assertEquals(10, unit.victoryPoints(), "Brain Bug should have highest VP");
            assertEquals(0, unit.attackStrength(), "Brain Bug should not attack");
            assertEquals(0, unit.movementAllowance(), "Brain Bug should be immobile");
            assertEquals(8, unit.defenseStrength(), "Brain Bug should have high defense");
            assertNotNull(unit.special(), "Brain Bug should have special rules");
        }

        @Test
        @DisplayName("should have Queen Bug with high VP")
        void shouldHaveQueenBugWithHighVp() {
            Optional<Unit> queen = service.getArachnidUnitById("queen");
            assertTrue(queen.isPresent());
            assertEquals(8, queen.get().victoryPoints());
            assertTrue(queen.get().isImmobile());
        }

        @Test
        @DisplayName("should have Crawler as siege unit")
        void shouldHaveCrawlerAsSiegeUnit() {
            Optional<Unit> crawler = service.getArachnidUnitById("crawler");
            assertTrue(crawler.isPresent());
            Unit unit = crawler.get();
            assertEquals("Siege", unit.type());
            assertEquals(8, unit.attackStrength());
            assertEquals(6, unit.defenseStrength());
            assertEquals(1, unit.movementAllowance(), "Crawler should be very slow");
            assertEquals(4, unit.victoryPoints());
        }

        @Test
        @DisplayName("should have Plasma Bug with highest attack")
        void shouldHavePlasmaBugWithHighestAttack() {
            Optional<Unit> plasma = service.getArachnidUnitById("plasma");
            assertTrue(plasma.isPresent());
            assertEquals(10, plasma.get().attackStrength(), "Plasma Bug should have highest attack");
            assertEquals(8, plasma.get().range());
        }

        @Test
        @DisplayName("should have Worker with no attack")
        void shouldHaveWorkerWithNoAttack() {
            Optional<Unit> worker = service.getArachnidUnitById("worker");
            assertTrue(worker.isPresent());
            assertEquals(0, worker.get().attackStrength());
            assertFalse(worker.get().isCombatUnit());
        }

        @Test
        @DisplayName("should have Decoy with zero stats")
        void shouldHaveDecoyWithZeroStats() {
            Optional<Unit> decoy = service.getArachnidUnitById("decoy");
            assertTrue(decoy.isPresent());
            assertEquals(0, decoy.get().attackStrength());
            assertEquals(0, decoy.get().defenseStrength());
            assertEquals(0, decoy.get().movementAllowance());
            assertEquals(0, decoy.get().victoryPoints());
        }

        @Test
        @DisplayName("should have Royal Warrior as elite unit")
        void shouldHaveRoyalWarriorAsEliteUnit() {
            Optional<Unit> royal = service.getArachnidUnitById("royal_warrior");
            assertTrue(royal.isPresent());
            assertEquals("Elite Infantry", royal.get().type());
            assertEquals(5, royal.get().attackStrength());
            assertEquals(4, royal.get().defenseStrength());
            assertEquals(3, royal.get().victoryPoints());
        }

        @Test
        @DisplayName("should have Warrior variants")
        void shouldHaveWarriorVariants() {
            Optional<Unit> warrior = service.getArachnidUnitById("warrior");
            assertTrue(warrior.isPresent());
            List<UnitVariant> variants = warrior.get().variants();
            assertNotNull(variants);
            assertEquals(2, variants.size(), "Warrior should have 2 variants");
            assertTrue(variants.stream().anyMatch(v -> v.name().contains("Desert")));
            assertTrue(variants.stream().anyMatch(v -> v.name().contains("Heavy")));
        }

        @Test
        @DisplayName("should filter units by victory points")
        void shouldFilterUnitsByVictoryPoints() {
            List<Unit> vp10Units = service.getArachnidUnitsByVictoryPoints(10);
            assertEquals(1, vp10Units.size());
            assertEquals("brain_bug", vp10Units.get(0).id());

            List<Unit> vp1Units = service.getArachnidUnitsByVictoryPoints(1);
            assertTrue(vp1Units.size() >= 2, "Warrior and Worker should have 1 VP");
        }

        @Test
        @DisplayName("should have Tanker with tunnel ability")
        void shouldHaveTankerWithTunnelAbility() {
            Optional<Unit> tanker = service.getArachnidUnitById("tanker");
            assertTrue(tanker.isPresent());
            assertTrue(tanker.get().abilities().stream()
                    .anyMatch(a -> a.toLowerCase().contains("tunnel")),
                    "Tanker should have tunnel ability");
        }

        @Test
        @DisplayName("should have Strong Point as fortification")
        void shouldHaveStrongPointAsFortification() {
            Optional<Unit> strongPoint = service.getArachnidUnitById("strong_point");
            assertTrue(strongPoint.isPresent());
            assertEquals("Fortification", strongPoint.get().type());
            assertEquals(6, strongPoint.get().defenseStrength());
            assertEquals(0, strongPoint.get().attackStrength());
        }
    }

    // ==================== Humanoid Units ====================

    @Nested
    @DisplayName("Humanoid Units Tests")
    class HumanoidUnitsTests {

        @Test
        @DisplayName("should load all 4 Humanoid unit types")
        void shouldLoadAllHumanoidUnitTypes() {
            assertEquals(4, service.getHumanoidUnits().size(),
                    "Should have 4 Humanoid unit types");
        }

        @Test
        @DisplayName("should have correct Humanoid Warrior stats")
        void shouldHaveCorrectHumanoidWarriorStats() {
            Optional<Unit> warrior = service.getHumanoidUnitById("humanoid_warrior");
            assertTrue(warrior.isPresent());
            assertEquals(4, warrior.get().attackStrength());
            assertEquals(4, warrior.get().defenseStrength());
            assertEquals(6, warrior.get().movementAllowance());
        }

        @Test
        @DisplayName("should have Humanoid Worker with no attack")
        void shouldHaveHumanoidWorkerWithNoAttack() {
            Optional<Unit> worker = service.getHumanoidUnitById("humanoid_worker");
            assertTrue(worker.isPresent());
            assertEquals(0, worker.get().attackStrength());
        }

        @Test
        @DisplayName("should have heavy weapons with high attack")
        void shouldHaveHeavyWeaponsWithHighAttack() {
            Optional<Unit> beam = service.getHumanoidUnitById("humanoid_heavy_beam");
            Optional<Unit> missile = service.getHumanoidUnitById("humanoid_heavy_missile");
            assertTrue(beam.isPresent());
            assertTrue(missile.isPresent());
            assertEquals(12, beam.get().attackStrength(), "Humanoid heavy weapons should have high attack");
            assertEquals(12, missile.get().attackStrength());
            assertEquals(6, beam.get().range());
            assertEquals(10, missile.get().range());
        }

        @Test
        @DisplayName("should have correct unit IDs")
        void shouldHaveCorrectUnitIds() {
            Set<String> expectedIds = Set.of(
                    "humanoid_warrior", "humanoid_worker",
                    "humanoid_heavy_beam", "humanoid_heavy_missile"
            );
            Set<String> actualIds = service.getHumanoidUnits().stream()
                    .map(Unit::id)
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(expectedIds, actualIds);
        }
    }

    // ==================== All Unit IDs ====================

    @Nested
    @DisplayName("All Unit IDs Tests")
    class AllUnitIdsTests {

        @Test
        @DisplayName("should return all unique unit IDs across factions")
        void shouldReturnAllUniqueUnitIds() {
            Set<String> allIds = service.getAllUnitIds();
            // 10 Terran + 12 Arachnid + 4 Humanoid = 26
            assertEquals(26, allIds.size(), "Should have 26 unique unit IDs");
        }

        @Test
        @DisplayName("should include IDs from all three factions")
        void shouldIncludeIdsFromAllFactions() {
            Set<String> allIds = service.getAllUnitIds();
            assertTrue(allIds.contains("marauder"), "Should include Terran IDs");
            assertTrue(allIds.contains("warrior"), "Should include Arachnid IDs");
            assertTrue(allIds.contains("humanoid_warrior"), "Should include Humanoid IDs");
        }
    }

    // ==================== Scenarios ====================

    @Nested
    @DisplayName("Scenarios Tests")
    class ScenariosTests {

        @Test
        @DisplayName("should load all 8 scenarios")
        void shouldLoadAllScenarios() {
            assertEquals(8, service.getScenarios().size(),
                    "Should have 8 scenarios");
        }

        @Test
        @DisplayName("should have correct scenario IDs 1-8")
        void shouldHaveCorrectScenarioIds() {
            for (int i = 1; i <= 8; i++) {
                assertTrue(service.getScenarioById(i).isPresent(),
                        "Scenario " + i + " should exist");
            }
        }

        @Test
        @DisplayName("should have Scenario 1 as introductory")
        void shouldHaveScenario1AsIntroductory() {
            Optional<Scenario> scenario = service.getScenarioById(1);
            assertTrue(scenario.isPresent());
            assertEquals("Introductory", scenario.get().difficulty());
            assertTrue(scenario.get().name().contains("Feint"));
        }

        @Test
        @DisplayName("should have Scenario 8 as expert difficulty")
        void shouldHaveScenario8AsExpert() {
            Optional<Scenario> scenario = service.getScenarioById(8);
            assertTrue(scenario.isPresent());
            assertEquals("Expert", scenario.get().difficulty());
            assertTrue(scenario.get().name().contains("Klendathu"));
        }

        @Test
        @DisplayName("should have increasing difficulty progression")
        void shouldHaveIncreasingDifficultyProgression() {
            List<Scenario> sorted = service.getScenariosByDifficulty();
            assertEquals(8, sorted.size());
            assertEquals("Introductory", sorted.get(0).difficulty(),
                    "First scenario should be Introductory");
            assertEquals("Expert", sorted.get(sorted.size() - 1).difficulty(),
                    "Last scenario should be Expert");
        }

        @Test
        @DisplayName("should have correct Klendathu forces")
        void shouldHaveCorrectKlendathuForces() {
            Optional<Scenario> klendathu = service.getScenarioById(8);
            assertTrue(klendathu.isPresent());
            Scenario scenario = klendathu.get();
            // Klendathu should have the most units
            assertTrue(scenario.totalTerranUnits() > 50,
                    "Klendathu should have many Terran units");
            assertTrue(scenario.totalArachnidUnits() > 100,
                    "Klendathu should have many Arachnid units");
        }

        @Test
        @DisplayName("should have victory conditions for all scenarios")
        void shouldHaveVictoryConditionsForAllScenarios() {
            service.getScenarios().forEach(scenario -> {
                assertNotNull(scenario.victoryConditions());
                assertFalse(scenario.victoryConditions().terranVictory().isBlank());
                assertFalse(scenario.victoryConditions().arachnidVictory().isBlank());
            });
        }

        @Test
        @DisplayName("should have Scenario 4 (Revolt) with humanoid forces")
        void shouldHaveRevoltWithHumanoidForces() {
            Optional<Scenario> revolt = service.getScenarioById(4);
            assertTrue(revolt.isPresent());
            Scenario scenario = revolt.get();
            assertTrue(scenario.humanoidForces() != null && !scenario.humanoidForces().isEmpty(),
                    "Revolt should have humanoid forces");
        }

        @Test
        @DisplayName("should have correct turn counts")
        void shouldHaveCorrectTurnCounts() {
            Optional<Scenario> feint = service.getScenarioById(1);
            assertTrue(feint.isPresent());
            assertEquals(10, feint.get().turns());

            Optional<Scenario> klendathu = service.getScenarioById(8);
            assertTrue(klendathu.isPresent());
            assertEquals(25, klendathu.get().turns());
        }

        @Test
        @DisplayName("should have special rules for complex scenarios")
        void shouldHaveSpecialRulesForComplexScenarios() {
            Optional<Scenario> revolt = service.getScenarioById(4);
            assertTrue(revolt.isPresent());
            assertFalse(revolt.get().specialRules().isEmpty(),
                    "Revolt should have special rules");
            assertTrue(revolt.get().specialRules().stream()
                    .anyMatch(r -> r.toLowerCase().contains("stack")),
                    "Revolt should mention stacking rules");
        }

        @Test
        @DisplayName("should calculate total units correctly")
        void shouldCalculateTotalUnitsCorrectly() {
            Optional<Scenario> scenario = service.getScenarioById(1);
            assertTrue(scenario.isPresent());
            // Scenario 1: 1 command + 6 marauder + 1 scout = 8 Terran
            assertEquals(8, scenario.get().totalTerranUnits());
        }

        @Test
        @DisplayName("should have Sheol scenarios with engineers")
        void shouldHaveSheolWithEngineers() {
            Optional<Scenario> sheol1 = service.getScenarioById(5);
            assertTrue(sheol1.isPresent());
            assertTrue(sheol1.get().name().contains("Sheol"));
            assertTrue(sheol1.get().terranForces().containsKey("combat_engineer"),
                    "Sheol should have combat engineers");
        }
    }

    // ==================== Special Equipment ====================

    @Nested
    @DisplayName("Special Equipment Tests")
    class SpecialEquipmentTests {

        @Test
        @DisplayName("should load all 8 equipment types")
        void shouldLoadAllEquipmentTypes() {
            assertEquals(8, service.getSpecialEquipment().size(),
                    "Should have 8 special equipment types");
        }

        @Test
        @DisplayName("should have correct equipment IDs")
        void shouldHaveCorrectEquipmentIds() {
            Set<String> expectedIds = Set.of(
                    "dap", "dar", "hedm", "ndm",
                    "listening_device", "rocket_beacon",
                    "heavy_nerve_gas", "heavy_explosive"
            );
            Set<String> actualIds = service.getSpecialEquipment().stream()
                    .map(SpecialEquipment::id)
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(expectedIds, actualIds);
        }

        @Test
        @DisplayName("should identify explosive equipment")
        void shouldIdentifyExplosiveEquipment() {
            List<SpecialEquipment> explosives = service.getExplosiveEquipment();
            assertEquals(4, explosives.size(),
                    "Should have 4 explosive types (DAP, DAR, HEDM, NDM)");
            explosives.forEach(e -> assertTrue(e.isExplosive()));
        }

        @Test
        @DisplayName("should have Nuclear Demolition Munitions with highest attack")
        void shouldHaveNuclearWithHighestAttack() {
            Optional<SpecialEquipment> ndm = service.getEquipmentById("ndm");
            assertTrue(ndm.isPresent());
            assertEquals(15, ndm.get().attackStrength(),
                    "Nuclear should have highest attack strength");
            assertTrue(ndm.get().isExplosive());
        }

        @Test
        @DisplayName("should have Listening Device as sensor")
        void shouldHaveListeningDeviceAsSensor() {
            Optional<SpecialEquipment> sensor = service.getEquipmentById("listening_device");
            assertTrue(sensor.isPresent());
            assertTrue(sensor.get().isSensor());
            assertEquals(0, sensor.get().attackStrength());
            assertEquals(3, sensor.get().range());
        }

        @Test
        @DisplayName("should have Heavy Nerve Gas as heavy weapon")
        void shouldHaveHeavyNerveGasAsHeavyWeapon() {
            Optional<SpecialEquipment> hng = service.getEquipmentById("heavy_nerve_gas");
            assertTrue(hng.isPresent());
            assertTrue(hng.get().isHeavyWeapon());
            assertEquals(10, hng.get().range());
            assertEquals(9, hng.get().attackStrength());
        }

        @Test
        @DisplayName("should have restrictions on explosive equipment")
        void shouldHaveRestrictionsOnExplosives() {
            List<SpecialEquipment> explosives = service.getExplosiveEquipment();
            explosives.forEach(e -> {
                assertNotNull(e.restrictions());
                assertFalse(e.restrictions().isEmpty(),
                        e.name() + " should have restrictions");
            });
        }

        @Test
        @DisplayName("should have special effects on all equipment")
        void shouldHaveSpecialEffectsOnAllEquipment() {
            service.getSpecialEquipment().forEach(e -> {
                assertNotNull(e.specialEffects());
                assertFalse(e.specialEffects().isEmpty(),
                        e.name() + " should have special effects");
            });
        }

        @Test
        @DisplayName("should find equipment by ID")
        void shouldFindEquipmentById() {
            assertTrue(service.getEquipmentById("dap").isPresent());
            assertTrue(service.getEquipmentById("ndm").isPresent());
            assertFalse(service.getEquipmentById("nonexistent").isPresent());
        }
    }

    // ==================== Combat Tables ====================

    @Nested
    @DisplayName("Combat Tables Tests")
    class CombatTablesTests {

        @Test
        @DisplayName("should have all 11 odds ratios")
        void shouldHaveAllOddsRatios() {
            List<String> ratios = service.getOddsRatios();
            assertEquals(11, ratios.size());
            assertTrue(ratios.contains("1:6"));
            assertTrue(ratios.contains("1:2"));
            assertTrue(ratios.contains("2:1"));
            assertTrue(ratios.contains("7:1+"));
        }

        @Test
        @DisplayName("should have 6 die outcomes per odds ratio in Terran table")
        void shouldHaveSixDieOutcomesInTerranTable() {
            service.getTerranCombatTable().forEach((odds, outcomes) ->
                    assertEquals(6, outcomes.size(),
                            "Odds " + odds + " should have 6 die outcomes"));
        }

        @Test
        @DisplayName("should have 6 die outcomes per odds ratio in Alien table")
        void shouldHaveSixDieOutcomesInAlienTable() {
            service.getAlienCombatTable().forEach((odds, outcomes) ->
                    assertEquals(6, outcomes.size(),
                            "Odds " + odds + " should have 6 die outcomes"));
        }

        @Test
        @DisplayName("should resolve Terran combat at 7:1+ as all KIA")
        void shouldResolveSevenToOneAsAllKia() {
            for (int roll = 1; roll <= 6; roll++) {
                assertEquals("K", service.resolveTerranCombat("7:1+", roll),
                        "7:1+ should always be KIA");
            }
        }

        @Test
        @DisplayName("should resolve Alien combat at 7:1+ as all KIA")
        void shouldResolveAlienSevenToOneAsAllKia() {
            for (int roll = 1; roll <= 6; roll++) {
                assertEquals("K", service.resolveAlienCombat("7:1+", roll),
                        "7:1+ should always be KIA");
            }
        }

        @Test
        @DisplayName("should resolve 1:6 as mostly misses")
        void shouldResolveOneToSixAsMostlyMisses() {
            int missCount = 0;
            for (int roll = 1; roll <= 6; roll++) {
                String outcome = service.resolveTerranCombat("1:6", roll);
                if ("M".equals(outcome)) missCount++;
            }
            assertEquals(5, missCount, "1:6 should have 5 misses out of 6 rolls");
        }

        @Test
        @DisplayName("should throw on invalid odds ratio")
        void shouldThrowOnInvalidOddsRatio() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.resolveTerranCombat("invalid", 1));
        }

        @Test
        @DisplayName("should throw on invalid die roll")
        void shouldThrowOnInvalidDieRoll() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.resolveTerranCombat("2:1", 7));
        }

        @Test
        @DisplayName("should have valid outcome codes only")
        void shouldHaveValidOutcomeCodesOnly() {
            Set<String> validCodes = Set.of("M", "D", "W", "K", "H");
            service.getTerranCombatTable().forEach((odds, outcomes) ->
                    outcomes.forEach((die, code) ->
                            assertTrue(validCodes.contains(code),
                                    "Outcome code " + code + " at " + odds + "/" + die + " is invalid")));
        }

        @Test
        @DisplayName("should have increasing severity with better odds")
        void shouldHaveIncreasingSeverityWithBetterOdds() {
            // At odds 6:1, KIA outcomes should be more frequent than at 1:2
            int kia6to1 = 0, kia1to2 = 0;
            for (int roll = 1; roll <= 6; roll++) {
                if ("K".equals(service.resolveTerranCombat("6:1", roll))) kia6to1++;
                if ("K".equals(service.resolveTerranCombat("1:2", roll))) kia1to2++;
            }
            assertTrue(kia6to1 > kia1to2,
                    "6:1 should have more KIA outcomes than 1:2");
        }

        @Test
        @DisplayName("Terran and Alien tables should be identical")
        void terranAndAlienTablesShouldBeIdentical() {
            assertEquals(service.getTerranCombatTable(), service.getAlienCombatTable(),
                    "Terran and Alien attack tables should have the same structure");
        }
    }

    // ==================== Combat Resolution ====================

    @Nested
    @DisplayName("Combat Resolution Tests")
    class CombatResolutionTests {

        @Test
        @DisplayName("should calculate odds ratio 2:1 for 4 vs 2")
        void shouldCalculateOddsRatio2to1() {
            assertEquals("2:1", service.calculateOddsRatio(4, 2));
        }

        @Test
        @DisplayName("should calculate odds ratio 1:2 for 2 vs 4")
        void shouldCalculateOddsRatio1to2() {
            assertEquals("1:2", service.calculateOddsRatio(2, 4));
        }

        @Test
        @DisplayName("should return 7:1+ for zero defense")
        void shouldReturnSevenToOneForZeroDefense() {
            assertEquals("7:1+", service.calculateOddsRatio(10, 0));
        }

        @Test
        @DisplayName("should calculate 3:1 for 9 vs 3")
        void shouldCalculate3to1() {
            assertEquals("3:1", service.calculateOddsRatio(9, 3));
        }

        @Test
        @DisplayName("should round down 1.5:1 to 1:1 (1:2)")
        void shouldRoundDownOdds() {
            // 3 vs 2 = 1.5:1, rounds down to 1:2
            String odds = service.calculateOddsRatio(3, 2);
            assertEquals("1:2", odds, "1.5:1 should round down to 1:2");
        }

        @Test
        @DisplayName("should resolve full combat: Marauder vs Warrior")
        void shouldResolveFullCombatMarauderVsWarrior() {
            // Marauder attack=6, Warrior defense=1 => 6:1
            String odds = service.calculateOddsRatio(6, 1);
            assertEquals("6:1", odds);
            // At 6:1, all rolls should be KIA
            for (int roll = 1; roll <= 6; roll++) {
                assertEquals("K", service.resolveTerranCombat(odds, roll));
            }
        }

        @Test
        @DisplayName("should resolve full combat: Warrior vs Marauder")
        void shouldResolveFullCombatWarriorVsMarauder() {
            // Warrior attack=2, Marauder defense=4 => 1:2
            String odds = service.calculateOddsRatio(2, 4);
            assertEquals("1:2", odds);
            // At 1:2, mostly misses and disruptions
            int missCount = 0;
            for (int roll = 1; roll <= 6; roll++) {
                String outcome = service.resolveAlienCombat(odds, roll);
                if ("M".equals(outcome)) missCount++;
            }
            assertTrue(missCount >= 1, "1:2 should have at least 1 miss");
        }
    }

    // ==================== Turn Sequence ====================

    @Nested
    @DisplayName("Turn Sequence Tests")
    class TurnSequenceTests {

        @Test
        @DisplayName("should have 5 Terran turn phases")
        void shouldHaveFiveTerranPhases() {
            assertEquals(5, service.getTerranTurnPhases().size());
        }

        @Test
        @DisplayName("should have 4 Arachnid turn phases")
        void shouldHaveFourArachnidPhases() {
            assertEquals(4, service.getArachnidTurnPhases().size());
        }

        @Test
        @DisplayName("should have Movement Phase in Terran turn")
        void shouldHaveMovementPhaseInTerranTurn() {
            List<String> phases = service.getTerranTurnPhases();
            assertTrue(phases.stream().anyMatch(p -> p.contains("Movement")),
                    "Terran turn should have a Movement Phase");
        }

        @Test
        @DisplayName("should have Close Combat Phase in both turns")
        void shouldHaveCloseCombatInBothTurns() {
            assertTrue(service.getTerranTurnPhases().stream()
                    .anyMatch(p -> p.contains("Close Combat")));
            assertTrue(service.getArachnidTurnPhases().stream()
                    .anyMatch(p -> p.contains("Close Combat")));
        }

        @Test
        @DisplayName("should have Tunnel Service in Arachnid turn")
        void shouldHaveTunnelServiceInArachnidTurn() {
            assertTrue(service.getArachnidTurnPhases().stream()
                    .anyMatch(p -> p.contains("Tunnel")),
                    "Arachnid turn should have Tunnel Service Movement");
        }

        @Test
        @DisplayName("should have Extended Jump in Terran turn")
        void shouldHaveExtendedJumpInTerranTurn() {
            assertTrue(service.getTerranTurnPhases().stream()
                    .anyMatch(p -> p.contains("Extended Jump")),
                    "Terran turn should have Extended Jump Phase");
        }

        @Test
        @DisplayName("should have 5 combat outcomes")
        void shouldHaveFiveCombatOutcomes() {
            assertEquals(5, service.getCombatOutcomes().size());
            assertTrue(service.getCombatOutcomes().containsKey("miss"));
            assertTrue(service.getCombatOutcomes().containsKey("disrupted"));
            assertTrue(service.getCombatOutcomes().containsKey("wia"));
            assertTrue(service.getCombatOutcomes().containsKey("kia"));
            assertTrue(service.getCombatOutcomes().containsKey("hvy"));
        }
    }

    // ==================== Cross-Reference Validation ====================

    @Nested
    @DisplayName("Cross-Reference Validation Tests")
    class CrossReferenceTests {

        @Test
        @DisplayName("scenario forces should reference valid unit IDs")
        void scenarioForcesShouldReferenceValidUnitIds() {
            Set<String> allUnitIds = service.getAllUnitIds();
            service.getScenarios().forEach(scenario -> {
                scenario.terranForces().keySet().forEach(id ->
                        assertTrue(allUnitIds.contains(id) || id.equals("wounded") ||
                                id.equals("demolition_munitions") || id.equals("nuclear_munitions") ||
                                id.equals("critical_installations"),
                                "Scenario " + scenario.name() + " references unknown Terran unit: " + id));
                scenario.arachnidForces().keySet().forEach(id ->
                        assertTrue(allUnitIds.contains(id) || id.equals("critical_installations"),
                                "Scenario " + scenario.name() + " references unknown Arachnid unit: " + id));
                if (scenario.humanoidForces() != null) {
                    scenario.humanoidForces().keySet().forEach(id ->
                            assertTrue(allUnitIds.contains(id),
                                    "Scenario " + scenario.name() + " references unknown Humanoid unit: " + id));
                }
            });
        }

        @Test
        @DisplayName("Brain Bug should only appear in later scenarios")
        void brainBugShouldOnlyAppearInLaterScenarios() {
            // Brain Bug should appear in Operation Royalty (7) and Klendathu (8)
            Optional<Scenario> royalty = service.getScenarioById(7);
            assertTrue(royalty.isPresent());
            assertTrue(royalty.get().arachnidForces().containsKey("brain_bug"),
                    "Operation Royalty should have Brain Bug");

            Optional<Scenario> klendathu = service.getScenarioById(8);
            assertTrue(klendathu.isPresent());
            assertTrue(klendathu.get().arachnidForces().containsKey("brain_bug"),
                    "Klendathu should have Brain Bug");
        }

        @Test
        @DisplayName("scenario difficulty should progress")
        void scenarioDifficultyShouldProgress() {
            List<Scenario> sorted = service.getScenariosByDifficulty();
            List<String> difficulties = List.of("Introductory", "Easy", "Medium", "Hard", "Very Hard", "Expert");
            int[] lastIndex = {-1};
            sorted.forEach(s -> {
                int idx = difficulties.indexOf(s.difficulty());
                assertTrue(idx >= lastIndex[0],
                        "Difficulty should not decrease: " + s.name() + " (" + s.difficulty() + ")");
                lastIndex[0] = idx;
            });
        }

        @Test
        @DisplayName("later scenarios should have more units")
        void laterScenariosShouldHaveMoreUnits() {
            Optional<Scenario> first = service.getScenarioById(1);
            Optional<Scenario> last = service.getScenarioById(8);
            assertTrue(first.isPresent());
            assertTrue(last.isPresent());
            assertTrue(last.get().totalTerranUnits() > first.get().totalTerranUnits(),
                    "Klendathu should have more Terran units than Feint");
            assertTrue(last.get().totalArachnidUnits() > first.get().totalArachnidUnits(),
                    "Klendathu should have more Arachnid units than Feint");
        }
    }

    // ==================== CombatOutcome Enum ====================

    @Nested
    @DisplayName("CombatOutcome Enum Tests")
    class CombatOutcomeTests {

        @Test
        @DisplayName("should parse all outcome codes")
        void shouldParseAllOutcomeCodes() {
            assertEquals(CombatOutcome.MISS, CombatOutcome.fromCode("M"));
            assertEquals(CombatOutcome.DISRUPTED, CombatOutcome.fromCode("D"));
            assertEquals(CombatOutcome.WIA, CombatOutcome.fromCode("W"));
            assertEquals(CombatOutcome.KIA, CombatOutcome.fromCode("K"));
            assertEquals(CombatOutcome.HVY, CombatOutcome.fromCode("H"));
        }

        @Test
        @DisplayName("should throw on invalid code")
        void shouldThrowOnInvalidCode() {
            assertThrows(IllegalArgumentException.class,
                    () -> CombatOutcome.fromCode("X"));
        }

        @Test
        @DisplayName("should have correct severity ordering")
        void shouldHaveCorrectSeverityOrdering() {
            assertEquals(0, CombatOutcome.MISS.severity());
            assertEquals(1, CombatOutcome.DISRUPTED.severity());
            assertEquals(2, CombatOutcome.WIA.severity());
            assertEquals(3, CombatOutcome.HVY.severity());
            assertEquals(4, CombatOutcome.KIA.severity());
        }

        @Test
        @DisplayName("KIA should have highest severity")
        void kiaShouldHaveHighestSeverity() {
            CombatOutcome.KIA.severity();
            assertTrue(CombatOutcome.KIA.severity() > CombatOutcome.MISS.severity());
            assertTrue(CombatOutcome.KIA.severity() > CombatOutcome.WIA.severity());
        }
    }

    // ==================== Model Validation ====================

    @Nested
    @DisplayName("Model Validation Tests")
    class ModelValidationTests {

        @Test
        @DisplayName("Unit should reject blank ID")
        void unitShouldRejectBlankId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Unit("", "Test", "Type", 1, 1, 1, "", List.of(), null, null, null, List.of(), List.of(), null));
        }

        @Test
        @DisplayName("Unit should reject negative attack strength")
        void unitShouldRejectNegativeAttack() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Unit("test", "Test", "Type", -1, 1, 1, "", List.of(), null, null, null, List.of(), List.of(), null));
        }

        @Test
        @DisplayName("Unit should reject negative defense strength")
        void unitShouldRejectNegativeDefense() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Unit("test", "Test", "Type", 1, -1, 1, "", List.of(), null, null, null, List.of(), List.of(), null));
        }

        @Test
        @DisplayName("Scenario should reject zero ID")
        void scenarioShouldRejectZeroId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Scenario(0, "Test", "", "", 1, List.of(), Map.of(), Map.of(), null,
                            new VictoryConditions("Win", "Win")));
        }

        @Test
        @DisplayName("VictoryConditions should reject blank conditions")
        void victoryConditionsShouldRejectBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new VictoryConditions("", "Win"));
            assertThrows(IllegalArgumentException.class,
                    () -> new VictoryConditions("Win", ""));
        }

        @Test
        @DisplayName("SpecialEquipment should reject blank ID")
        void equipmentShouldRejectBlankId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SpecialEquipment("", "Test", "Type", 0, 0, null, 0, List.of(), List.of(), ""));
        }
    }
}
