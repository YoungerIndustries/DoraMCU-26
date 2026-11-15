package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechPilot Model Tests")
class MechPilotTest {

    private MechPilot pilot;

    @BeforeEach
    void setUp() {
        pilot = new MechPilot("John Doe");
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertNotNull(pilot.getId());
            assertEquals("John Doe", pilot.getName());
            assertEquals(MechPilot.ExperienceLevel.GREEN, pilot.getExperienceLevel());
            assertEquals(0, pilot.getExperiencePoints());
            assertEquals(0, pilot.getStressLevel());
            assertFalse(pilot.isBroken());
            assertTrue(pilot.getTraits().isEmpty());
            assertEquals(0, pilot.getWins());
            assertEquals(0, pilot.getLosses());
            assertEquals(0, pilot.getKills());
            assertEquals(0, pilot.getMechsDestroyed());
            assertNull(pilot.getBiography());
        }

        @Test
        @DisplayName("should initialize all skills at 1")
        void constructor_shouldInitializeAllSkillsAt1() {
            assertEquals(1, pilot.getBallisticSkill());
            assertEquals(1, pilot.getEnergySkill());
            assertEquals(1, pilot.getMissileSkill());
            assertEquals(1, pilot.getShieldingSkill());
            assertEquals(1, pilot.getHeatManagementSkill());
            assertEquals(1, pilot.getEvasionSkill());
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyPilot() {
            MechPilot empty = new MechPilot();
            assertNull(empty.getId());
            assertNull(empty.getName());
            assertNull(empty.getExperienceLevel());
        }

        @Test
        @DisplayName("should generate unique UUID for each pilot")
        void constructor_shouldGenerateUniqueUUID() {
            MechPilot p1 = new MechPilot("Pilot 1");
            MechPilot p2 = new MechPilot("Pilot 2");
            assertNotEquals(p1.getId(), p2.getId());
        }
    }

    // ---- Experience Levels ----

    @Nested
    @DisplayName("Experience Level Tests")
    class ExperienceLevelTests {

        @Test
        @DisplayName("should support all 5 experience levels")
        void enum_shouldSupportAllLevels() {
            MechPilot.ExperienceLevel[] values = MechPilot.ExperienceLevel.values();
            assertEquals(5, values.length);
        }

        @Test
        @DisplayName("GREEN should require 0 points")
        void green_shouldRequireZeroPoints() {
            assertEquals(0, MechPilot.ExperienceLevel.GREEN.getRequiredPoints());
        }

        @Test
        @DisplayName("ROOKIE should require 100 points")
        void rookie_shouldRequire100Points() {
            assertEquals(100, MechPilot.ExperienceLevel.ROOKIE.getRequiredPoints());
        }

        @Test
        @DisplayName("VETERAN should require 500 points")
        void veteran_shouldRequire500Points() {
            assertEquals(500, MechPilot.ExperienceLevel.VETERAN.getRequiredPoints());
        }

        @Test
        @DisplayName("ELITE should require 1500 points")
        void elite_shouldRequire1500Points() {
            assertEquals(1500, MechPilot.ExperienceLevel.ELITE.getRequiredPoints());
        }

        @Test
        @DisplayName("LEGEND should require 5000 points")
        void legend_shouldRequire5000Points() {
            assertEquals(5000, MechPilot.ExperienceLevel.LEGEND.getRequiredPoints());
        }

        @Test
        @DisplayName("should gain experience points")
        void gainExperience_shouldAccumulatePoints() {
            pilot.gainExperience(50);
            assertEquals(50, pilot.getExperiencePoints());
            pilot.gainExperience(50);
            assertEquals(100, pilot.getExperiencePoints());
        }

        @Test
        @DisplayName("should level up from GREEN to ROOKIE at 100 points")
        void gainExperience_shouldLevelUpToRookie() {
            pilot.gainExperience(100);
            assertEquals(MechPilot.ExperienceLevel.ROOKIE, pilot.getExperienceLevel());
        }

        @Test
        @DisplayName("should level up one step at a time")
        void gainExperience_shouldLevelUpOneStepAtATime() {
            pilot.gainExperience(500);
            // Only levels up one step: GREEN -> ROOKIE (nextLevel checks only once)
            assertEquals(MechPilot.ExperienceLevel.ROOKIE, pilot.getExperienceLevel());
        }

        @Test
        @DisplayName("should level up through all levels with multiple calls")
        void gainExperience_shouldLevelUpThroughAllLevels() {
            pilot.gainExperience(100);   // GREEN -> ROOKIE
            assertEquals(MechPilot.ExperienceLevel.ROOKIE, pilot.getExperienceLevel());
            pilot.gainExperience(400);   // ROOKIE -> VETERAN (500 total >= 500)
            assertEquals(MechPilot.ExperienceLevel.VETERAN, pilot.getExperienceLevel());
            pilot.gainExperience(1000);  // VETERAN -> ELITE (1500 total >= 1500)
            assertEquals(MechPilot.ExperienceLevel.ELITE, pilot.getExperienceLevel());
            pilot.gainExperience(3500);  // ELITE -> LEGEND (5000 total >= 5000)
            assertEquals(MechPilot.ExperienceLevel.LEGEND, pilot.getExperienceLevel());
        }

        @Test
        @DisplayName("should not level up beyond LEGEND")
        void gainExperience_shouldNotLevelUpBeyondLegend() {
            pilot.setExperienceLevel(MechPilot.ExperienceLevel.LEGEND);
            pilot.gainExperience(10000);
            assertEquals(MechPilot.ExperienceLevel.LEGEND, pilot.getExperienceLevel());
        }

        @Test
        @DisplayName("should not level up below threshold")
        void gainExperience_shouldNotLevelUpBelowThreshold() {
            pilot.gainExperience(99);
            assertEquals(MechPilot.ExperienceLevel.GREEN, pilot.getExperienceLevel());
        }

        @Test
        @DisplayName("getNextLevel should return null for LEGEND")
        void getNextLevel_shouldReturnNullForLegend() {
            assertNull(MechPilot.ExperienceLevel.LEGEND.getNextLevel());
        }

        @Test
        @DisplayName("getNextLevel should return next level for GREEN")
        void getNextLevel_shouldReturnRookieForGreen() {
            assertEquals(MechPilot.ExperienceLevel.ROOKIE, MechPilot.ExperienceLevel.GREEN.getNextLevel());
        }
    }

    // ---- Skill Increases ----

    @Nested
    @DisplayName("Skill Increase Tests")
    class SkillIncreaseTests {

        @Test
        @DisplayName("should increase BALLISTIC skill")
        void increaseSkill_shouldIncreaseBallistic() {
            pilot.increaseSkill(MechPilot.PilotSkill.BALLISTIC, 2);
            assertEquals(3, pilot.getBallisticSkill());
        }

        @Test
        @DisplayName("should increase ENERGY skill")
        void increaseSkill_shouldIncreaseEnergy() {
            pilot.increaseSkill(MechPilot.PilotSkill.ENERGY, 2);
            assertEquals(3, pilot.getEnergySkill());
        }

        @Test
        @DisplayName("should increase MISSILE skill")
        void increaseSkill_shouldIncreaseMissile() {
            pilot.increaseSkill(MechPilot.PilotSkill.MISSILE, 2);
            assertEquals(3, pilot.getMissileSkill());
        }

        @Test
        @DisplayName("should increase SHIELDING skill")
        void increaseSkill_shouldIncreaseShielding() {
            pilot.increaseSkill(MechPilot.PilotSkill.SHIELDING, 2);
            assertEquals(3, pilot.getShieldingSkill());
        }

        @Test
        @DisplayName("should increase HEAT_MANAGEMENT skill")
        void increaseSkill_shouldIncreaseHeatManagement() {
            pilot.increaseSkill(MechPilot.PilotSkill.HEAT_MANAGEMENT, 2);
            assertEquals(3, pilot.getHeatManagementSkill());
        }

        @Test
        @DisplayName("should increase EVASION skill")
        void increaseSkill_shouldIncreaseEvasion() {
            pilot.increaseSkill(MechPilot.PilotSkill.EVASION, 2);
            assertEquals(3, pilot.getEvasionSkill());
        }

        @Test
        @DisplayName("should cap skill at 6")
        void increaseSkill_shouldCapAt6() {
            pilot.increaseSkill(MechPilot.PilotSkill.BALLISTIC, 10);
            assertEquals(6, pilot.getBallisticSkill());
        }

        @Test
        @DisplayName("should not decrease skill below 1")
        void increaseSkill_shouldNotGoBelow1() {
            pilot.increaseSkill(MechPilot.PilotSkill.BALLISTIC, -5);
            assertEquals(1, pilot.getBallisticSkill());
        }

        @Test
        @DisplayName("should get skill level for each skill")
        void getSkillLevel_shouldReturnCorrectLevel() {
            pilot.increaseSkill(MechPilot.PilotSkill.BALLISTIC, 3);
            assertEquals(4, pilot.getSkillLevel(MechPilot.PilotSkill.BALLISTIC));
            assertEquals(1, pilot.getSkillLevel(MechPilot.PilotSkill.ENERGY));
        }
    }

    // ---- Stress Management ----

    @Nested
    @DisplayName("Stress Management Tests")
    class StressTests {

        @Test
        @DisplayName("should add stress")
        void addStress_shouldAddStress() {
            pilot.addStress(30);
            assertEquals(30, pilot.getStressLevel());
        }

        @Test
        @DisplayName("should cap stress at 100")
        void addStress_shouldCapAt100() {
            pilot.addStress(150);
            assertEquals(100, pilot.getStressLevel());
        }

        @Test
        @DisplayName("should break pilot at 100 stress")
        void addStress_shouldBreakPilotAt100() {
            pilot.addStress(100);
            assertTrue(pilot.isBroken());
        }

        @Test
        @DisplayName("should not break pilot below 100 stress")
        void addStress_shouldNotBreakBelow100() {
            pilot.addStress(99);
            assertFalse(pilot.isBroken());
        }

        @Test
        @DisplayName("should reduce stress")
        void reduceStress_shouldReduceStress() {
            pilot.addStress(50);
            pilot.reduceStress(20);
            assertEquals(30, pilot.getStressLevel());
        }

        @Test
        @DisplayName("should not reduce stress below 0")
        void reduceStress_shouldNotGoBelow0() {
            pilot.reduceStress(50);
            assertEquals(0, pilot.getStressLevel());
        }

        @Test
        @DisplayName("should unbreak pilot when stress drops below 50")
        void reduceStress_shouldUnbreakBelow50() {
            pilot.addStress(100);
            assertTrue(pilot.isBroken());
            pilot.reduceStress(60);
            assertFalse(pilot.isBroken());
        }

        @Test
        @DisplayName("should keep pilot broken when stress is 50 or above")
        void reduceStress_shouldKeepBrokenAt50() {
            pilot.addStress(100);
            assertTrue(pilot.isBroken());
            pilot.reduceStress(49);
            assertEquals(51, pilot.getStressLevel());
            assertTrue(pilot.isBroken());
        }
    }

    // ---- Trait Management ----

    @Nested
    @DisplayName("Trait Management Tests")
    class TraitTests {

        @Test
        @DisplayName("should add trait")
        void addTrait_shouldAddTrait() {
            pilot.addTrait(MechPilot.PilotTrait.VETERAN);
            assertTrue(pilot.hasTrait(MechPilot.PilotTrait.VETERAN));
        }

        @Test
        @DisplayName("should not add duplicate trait")
        void addTrait_shouldNotAddDuplicate() {
            pilot.addTrait(MechPilot.PilotTrait.VETERAN);
            pilot.addTrait(MechPilot.PilotTrait.VETERAN);
            assertEquals(1, pilot.getTraits().size());
        }

        @Test
        @DisplayName("should add multiple traits")
        void addTrait_shouldAddMultipleTraits() {
            pilot.addTrait(MechPilot.PilotTrait.VETERAN);
            pilot.addTrait(MechPilot.PilotTrait.SNIPER);
            assertEquals(2, pilot.getTraits().size());
        }

        @Test
        @DisplayName("should return false for missing trait")
        void hasTrait_shouldReturnFalseForMissing() {
            assertFalse(pilot.hasTrait(MechPilot.PilotTrait.VETERAN));
        }

        @Test
        @DisplayName("should support all combat traits")
        void enum_shouldSupportCombatTraits() {
            assertNotNull(MechPilot.PilotTrait.VETERAN);
            assertNotNull(MechPilot.PilotTrait.SCOUT);
            assertNotNull(MechPilot.PilotTrait.BRAWLER);
            assertNotNull(MechPilot.PilotTrait.SNIPER);
            assertNotNull(MechPilot.PilotTrait.HEAT_MASTER);
            assertNotNull(MechPilot.PilotTrait.IRON_WILL);
        }

        @Test
        @DisplayName("should support all mech-specific traits")
        void enum_shouldSupportMechTraits() {
            assertNotNull(MechPilot.PilotTrait.LIGHT_MECH_PILOT);
            assertNotNull(MechPilot.PilotTrait.MEDIUM_MECH_PILOT);
            assertNotNull(MechPilot.PilotTrait.HEAVY_MECH_PILOT);
            assertNotNull(MechPilot.PilotTrait.ASSAULT_PILOT);
        }

        @Test
        @DisplayName("should support all special traits")
        void enum_shouldSupportSpecialTraits() {
            assertNotNull(MechPilot.PilotTrait.QUICK_REFLEXES);
            assertNotNull(MechPilot.PilotTrait.STEADY_HANDS);
            assertNotNull(MechPilot.PilotTrait.MECHANIC);
            assertNotNull(MechPilot.PilotTrait.LEADER);
        }
    }

    // ---- Bonus Calculations ----

    @Nested
    @DisplayName("Bonus Calculation Tests")
    class BonusTests {

        @Test
        @DisplayName("should calculate accuracy bonus for ENERGY weapons")
        void getAccuracyBonus_shouldCalculateForEnergy() {
            pilot.setEnergySkill(3);
            // (3 - 1) * 0.05 = 0.10
            assertEquals(0.10, pilot.getAccuracyBonus(MechWeapon.WeaponType.ENERGY));
        }

        @Test
        @DisplayName("should calculate accuracy bonus for BALLISTIC weapons")
        void getAccuracyBonus_shouldCalculateForBallistic() {
            pilot.setBallisticSkill(4);
            // (4 - 1) * 0.05 = 0.15
            assertEquals(0.15, pilot.getAccuracyBonus(MechWeapon.WeaponType.BALLISTIC), 0.001);
        }

        @Test
        @DisplayName("should calculate accuracy bonus for MISSILE weapons")
        void getAccuracyBonus_shouldCalculateForMissile() {
            pilot.setMissileSkill(5);
            // (5 - 1) * 0.05 = 0.20
            assertEquals(0.20, pilot.getAccuracyBonus(MechWeapon.WeaponType.MISSILE));
        }

        @Test
        @DisplayName("should return 0 accuracy bonus at skill level 1")
        void getAccuracyBonus_shouldReturnZeroAtLevel1() {
            assertEquals(0.0, pilot.getAccuracyBonus(MechWeapon.WeaponType.ENERGY));
        }

        @Test
        @DisplayName("should calculate heat reduction bonus")
        void getHeatReductionBonus_shouldCalculate() {
            pilot.setHeatManagementSkill(3);
            // (3 - 1) * 0.05 = 0.10
            assertEquals(0.10, pilot.getHeatReductionBonus());
        }

        @Test
        @DisplayName("should return 0 heat reduction at skill level 1")
        void getHeatReductionBonus_shouldReturnZeroAtLevel1() {
            assertEquals(0.0, pilot.getHeatReductionBonus());
        }

        @Test
        @DisplayName("should calculate damage reduction bonus")
        void getDamageReductionBonus_shouldCalculate() {
            pilot.setShieldingSkill(4);
            // (4 - 1) * 0.03 = 0.09
            assertEquals(0.09, pilot.getDamageReductionBonus());
        }

        @Test
        @DisplayName("should return 0 damage reduction at skill level 1")
        void getDamageReductionBonus_shouldReturnZeroAtLevel1() {
            assertEquals(0.0, pilot.getDamageReductionBonus());
        }

        @Test
        @DisplayName("should calculate stability bonus")
        void getStabilityBonus_shouldCalculate() {
            pilot.setEvasionSkill(5);
            // (5 - 1) * 0.05 = 0.20
            assertEquals(0.20, pilot.getStabilityBonus());
        }

        @Test
        @DisplayName("should return 0 stability bonus at skill level 1")
        void getStabilityBonus_shouldReturnZeroAtLevel1() {
            assertEquals(0.0, pilot.getStabilityBonus());
        }
    }

    // ---- Stats ----

    @Nested
    @DisplayName("Stats Tests")
    class StatsTests {

        @Test
        @DisplayName("should set and get wins")
        void setter_shouldSetWins() {
            pilot.setWins(10);
            assertEquals(10, pilot.getWins());
        }

        @Test
        @DisplayName("should set and get losses")
        void setter_shouldSetLosses() {
            pilot.setLosses(5);
            assertEquals(5, pilot.getLosses());
        }

        @Test
        @DisplayName("should set and get kills")
        void setter_shouldSetKills() {
            pilot.setKills(20);
            assertEquals(20, pilot.getKills());
        }

        @Test
        @DisplayName("should set and get mechs destroyed")
        void setter_shouldSetMechsDestroyed() {
            pilot.setMechsDestroyed(8);
            assertEquals(8, pilot.getMechsDestroyed());
        }

        @Test
        @DisplayName("should set and get biography")
        void setter_shouldSetBiography() {
            pilot.setBiography("Legendary pilot");
            assertEquals("Legendary pilot", pilot.getBiography());
        }

        @Test
        @DisplayName("should set and get name")
        void setter_shouldSetName() {
            pilot.setName("Jane Smith");
            assertEquals("Jane Smith", pilot.getName());
        }
    }
}
