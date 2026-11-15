package com.yisy.modules.doramcu.mechwarrior.ai;

import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnemyPilot Model Tests")
class EnemyPilotTest {

    private EnemyPilot enemy;

    @BeforeEach
    void setUp() {
        enemy = new EnemyPilot("Test Enemy", EnemyPilot.EnemyRole.ASSAULT, 3);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create with correct role")
        void constructor_shouldSetRole() {
            assertEquals(EnemyPilot.EnemyRole.ASSAULT, enemy.getRole());
        }

        @Test
        @DisplayName("should create with correct difficulty level")
        void constructor_shouldSetDifficulty() {
            assertEquals(3, enemy.getDifficultyLevel());
        }

        @Test
        @DisplayName("should create a pilot")
        void constructor_shouldCreatePilot() {
            assertNotNull(enemy.getPilot());
            assertNotNull(enemy.getPilot().getId());
            assertNotNull(enemy.getPilot().getName());
        }

        @Test
        @DisplayName("should create a chassis")
        void constructor_shouldCreateChassis() {
            assertNotNull(enemy.getChassis());
            assertNotNull(enemy.getChassis().getName());
        }

        @Test
        @DisplayName("should create weapons")
        void constructor_shouldCreateWeapons() {
            List<MechWeapon> weapons = enemy.getWeapons();
            assertNotNull(weapons);
            assertFalse(weapons.isEmpty());
        }

        @Test
        @DisplayName("should cap difficulty at 5")
        void constructor_shouldCapDifficulty() {
            EnemyPilot highDiff = new EnemyPilot("High", EnemyPilot.EnemyRole.SCOUT, 10);
            assertEquals(5, highDiff.getDifficultyLevel());
        }

        @Test
        @DisplayName("should floor difficulty at 1")
        void constructor_shouldFloorDifficulty() {
            EnemyPilot lowDiff = new EnemyPilot("Low", EnemyPilot.EnemyRole.SCOUT, 0);
            assertEquals(1, lowDiff.getDifficultyLevel());
        }
    }

    // ---- Enemy Roles ----

    @Nested
    @DisplayName("EnemyRole Enum Tests")
    class EnemyRoleTests {

        @Test
        @DisplayName("should support all 5 roles")
        void enum_shouldSupportAllRoles() {
            EnemyPilot.EnemyRole[] values = EnemyPilot.EnemyRole.values();
            assertEquals(5, values.length);
            assertNotNull(EnemyPilot.EnemyRole.SCOUT);
            assertNotNull(EnemyPilot.EnemyRole.ASSAULT);
            assertNotNull(EnemyPilot.EnemyRole.SNIPER);
            assertNotNull(EnemyPilot.EnemyRole.BRAWLER);
            assertNotNull(EnemyPilot.EnemyRole.SUPPORT);
        }

        @Test
        @DisplayName("should create SCOUT role")
        void constructor_shouldCreateScout() {
            EnemyPilot scout = new EnemyPilot("Scout", EnemyPilot.EnemyRole.SCOUT, 2);
            assertEquals(EnemyPilot.EnemyRole.SCOUT, scout.getRole());
            assertEquals(MechChassis.MechClass.LIGHT, scout.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create ASSAULT role")
        void constructor_shouldCreateAssault() {
            EnemyPilot assault = new EnemyPilot("Assault", EnemyPilot.EnemyRole.ASSAULT, 3);
            assertEquals(EnemyPilot.EnemyRole.ASSAULT, assault.getRole());
            assertEquals(MechChassis.MechClass.ASSAULT, assault.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create SNIPER role")
        void constructor_shouldCreateSniper() {
            EnemyPilot sniper = new EnemyPilot("Sniper", EnemyPilot.EnemyRole.SNIPER, 4);
            assertEquals(EnemyPilot.EnemyRole.SNIPER, sniper.getRole());
            assertEquals(MechChassis.MechClass.HEAVY, sniper.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create BRAWLER role")
        void constructor_shouldCreateBrawler() {
            EnemyPilot brawler = new EnemyPilot("Brawler", EnemyPilot.EnemyRole.BRAWLER, 2);
            assertEquals(EnemyPilot.EnemyRole.BRAWLER, brawler.getRole());
            assertEquals(MechChassis.MechClass.MEDIUM, brawler.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create SUPPORT role")
        void constructor_shouldCreateSupport() {
            EnemyPilot support = new EnemyPilot("Support", EnemyPilot.EnemyRole.SUPPORT, 3);
            assertEquals(EnemyPilot.EnemyRole.SUPPORT, support.getRole());
            assertEquals(MechChassis.MechClass.MEDIUM, support.getChassis().getMechClass());
        }
    }

    // ---- Pilot Skills ----

    @Nested
    @DisplayName("Pilot Skill Tests")
    class PilotSkillTests {

        @Test
        @DisplayName("should set pilot name")
        void constructor_shouldSetName() {
            assertEquals("Test Enemy", enemy.getPilot().getName());
        }

        @Test
        @DisplayName("should prefix ID with enemy-")
        void constructor_shouldPrefixId() {
            assertTrue(enemy.getPilot().getId().startsWith("enemy-"));
        }

        @Test
        @DisplayName("should scale skills by difficulty")
        void constructor_shouldScaleSkills() {
            EnemyPilot easy = new EnemyPilot("Easy", EnemyPilot.EnemyRole.ASSAULT, 1);
            EnemyPilot hard = new EnemyPilot("Hard", EnemyPilot.EnemyRole.ASSAULT, 5);

            // Hard enemy should have higher skills
            assertTrue(hard.getPilot().getBallisticSkill() >= easy.getPilot().getBallisticSkill());
        }

        @Test
        @DisplayName("should set experience level based on difficulty")
        void constructor_shouldSetExperienceLevel() {
            EnemyPilot green = new EnemyPilot("Green", EnemyPilot.EnemyRole.SCOUT, 1);
            assertEquals(MechPilot.ExperienceLevel.GREEN, green.getPilot().getExperienceLevel());

            EnemyPilot rookie = new EnemyPilot("Rookie", EnemyPilot.EnemyRole.SCOUT, 2);
            assertEquals(MechPilot.ExperienceLevel.ROOKIE, rookie.getPilot().getExperienceLevel());

            EnemyPilot veteran = new EnemyPilot("Veteran", EnemyPilot.EnemyRole.SCOUT, 3);
            assertEquals(MechPilot.ExperienceLevel.VETERAN, veteran.getPilot().getExperienceLevel());

            EnemyPilot elite = new EnemyPilot("Elite", EnemyPilot.EnemyRole.SCOUT, 4);
            assertEquals(MechPilot.ExperienceLevel.ELITE, elite.getPilot().getExperienceLevel());

            EnemyPilot legend = new EnemyPilot("Legend", EnemyPilot.EnemyRole.SCOUT, 5);
            assertEquals(MechPilot.ExperienceLevel.LEGEND, legend.getPilot().getExperienceLevel());
        }

        @Test
        @DisplayName("should give SCOUT bonus to energy and evasion")
        void scout_shouldHaveEnergyAndEvasionBonus() {
            EnemyPilot scout = new EnemyPilot("Scout", EnemyPilot.EnemyRole.SCOUT, 3);
            assertTrue(scout.getPilot().getEnergySkill() >= scout.getPilot().getBallisticSkill());
            assertTrue(scout.getPilot().getEvasionSkill() >= scout.getPilot().getBallisticSkill());
        }

        @Test
        @DisplayName("should give ASSAULT bonus to ballistic and shielding")
        void assault_shouldHaveBallisticAndShieldingBonus() {
            EnemyPilot assault = new EnemyPilot("Assault", EnemyPilot.EnemyRole.ASSAULT, 3);
            assertTrue(assault.getPilot().getBallisticSkill() >= assault.getPilot().getMissileSkill());
        }

        @Test
        @DisplayName("should give SNIPER bonus to ballistic and missile")
        void sniper_shouldHaveBallisticAndMissileBonus() {
            EnemyPilot sniper = new EnemyPilot("Sniper", EnemyPilot.EnemyRole.SNIPER, 3);
            assertTrue(sniper.getPilot().getBallisticSkill() >= sniper.getPilot().getEnergySkill());
        }

        @Test
        @DisplayName("should give BRAWLER bonus to energy and evasion")
        void brawler_shouldHaveEnergyAndEvasionBonus() {
            EnemyPilot brawler = new EnemyPilot("Brawler", EnemyPilot.EnemyRole.BRAWLER, 3);
            assertTrue(brawler.getPilot().getEnergySkill() >= brawler.getPilot().getMissileSkill());
        }

        @Test
        @DisplayName("should give SUPPORT bonus to missile and heat management")
        void support_shouldHaveMissileAndHeatBonus() {
            EnemyPilot support = new EnemyPilot("Support", EnemyPilot.EnemyRole.SUPPORT, 3);
            assertTrue(support.getPilot().getMissileSkill() >= support.getPilot().getBallisticSkill());
        }
    }

    // ---- Chassis ----

    @Nested
    @DisplayName("Chassis Tests")
    class ChassisTests {

        @Test
        @DisplayName("should create LIGHT chassis for SCOUT")
        void scout_shouldHaveLightChassis() {
            EnemyPilot scout = new EnemyPilot("Scout", EnemyPilot.EnemyRole.SCOUT, 3);
            assertEquals(MechChassis.MechClass.LIGHT, scout.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create ASSAULT chassis for ASSAULT")
        void assault_shouldHaveAssaultChassis() {
            EnemyPilot assault = new EnemyPilot("Assault", EnemyPilot.EnemyRole.ASSAULT, 3);
            assertEquals(MechChassis.MechClass.ASSAULT, assault.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create HEAVY chassis for SNIPER")
        void sniper_shouldHaveHeavyChassis() {
            EnemyPilot sniper = new EnemyPilot("Sniper", EnemyPilot.EnemyRole.SNIPER, 3);
            assertEquals(MechChassis.MechClass.HEAVY, sniper.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create MEDIUM chassis for BRAWLER")
        void brawler_shouldHaveMediumChassis() {
            EnemyPilot brawler = new EnemyPilot("Brawler", EnemyPilot.EnemyRole.BRAWLER, 3);
            assertEquals(MechChassis.MechClass.MEDIUM, brawler.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should create MEDIUM chassis for SUPPORT")
        void support_shouldHaveMediumChassis() {
            EnemyPilot support = new EnemyPilot("Support", EnemyPilot.EnemyRole.SUPPORT, 3);
            assertEquals(MechChassis.MechClass.MEDIUM, support.getChassis().getMechClass());
        }

        @Test
        @DisplayName("should scale tonnage by difficulty")
        void constructor_shouldScaleTonnage() {
            EnemyPilot easy = new EnemyPilot("Easy", EnemyPilot.EnemyRole.ASSAULT, 1);
            EnemyPilot hard = new EnemyPilot("Hard", EnemyPilot.EnemyRole.ASSAULT, 5);
            assertTrue(hard.getChassis().getTonnage() > easy.getChassis().getTonnage());
        }

        @Test
        @DisplayName("should set jump jets for SCOUT")
        void scout_shouldHaveJumpJets() {
            EnemyPilot scout = new EnemyPilot("Scout", EnemyPilot.EnemyRole.SCOUT, 3);
            assertTrue(scout.getChassis().getJumpJets() > 0);
        }

        @Test
        @DisplayName("should set higher speed for SCOUT")
        void scout_shouldHaveHigherSpeed() {
            EnemyPilot scout = new EnemyPilot("Scout", EnemyPilot.EnemyRole.SCOUT, 3);
            EnemyPilot assault = new EnemyPilot("Assault", EnemyPilot.EnemyRole.ASSAULT, 3);
            assertTrue(scout.getChassis().getMaxSpeed() > assault.getChassis().getMaxSpeed());
        }
    }

    // ---- Weapons ----

    @Nested
    @DisplayName("Weapon Loadout Tests")
    class WeaponLoadoutTests {

        @Test
        @DisplayName("should create energy weapons for SCOUT")
        void scout_shouldHaveEnergyWeapons() {
            EnemyPilot scout = new EnemyPilot("Scout", EnemyPilot.EnemyRole.SCOUT, 3);
            boolean hasEnergy = scout.getWeapons().stream()
                    .anyMatch(w -> w.getType() == MechWeapon.WeaponType.ENERGY);
            assertTrue(hasEnergy);
        }

        @Test
        @DisplayName("should create ballistic weapons for ASSAULT")
        void assault_shouldHaveBallisticWeapons() {
            EnemyPilot assault = new EnemyPilot("Assault", EnemyPilot.EnemyRole.ASSAULT, 3);
            boolean hasBallistic = assault.getWeapons().stream()
                    .anyMatch(w -> w.getType() == MechWeapon.WeaponType.BALLISTIC);
            assertTrue(hasBallistic);
        }

        @Test
        @DisplayName("should create missile weapons for SUPPORT")
        void support_shouldHaveMissileWeapons() {
            EnemyPilot support = new EnemyPilot("Support", EnemyPilot.EnemyRole.SUPPORT, 3);
            boolean hasMissile = support.getWeapons().stream()
                    .anyMatch(w -> w.getType() == MechWeapon.WeaponType.MISSILE);
            assertTrue(hasMissile);
        }

        @Test
        @DisplayName("should scale weapon damage by difficulty")
        void constructor_shouldScaleWeaponDamage() {
            EnemyPilot easy = new EnemyPilot("Easy", EnemyPilot.EnemyRole.ASSAULT, 1);
            EnemyPilot hard = new EnemyPilot("Hard", EnemyPilot.EnemyRole.ASSAULT, 5);

            // Hard enemy weapons should do more damage
            int easyTotal = easy.getWeapons().stream().mapToInt(MechWeapon::getDamage).sum();
            int hardTotal = hard.getWeapons().stream().mapToInt(MechWeapon::getDamage).sum();
            assertTrue(hardTotal > easyTotal);
        }

        @Test
        @DisplayName("should create multiple weapons")
        void constructor_shouldCreateMultipleWeapons() {
            EnemyPilot assault = new EnemyPilot("Assault", EnemyPilot.EnemyRole.ASSAULT, 3);
            assertTrue(assault.getWeapons().size() >= 3);
        }

        @Test
        @DisplayName("should create weapons with valid types")
        void constructor_shouldCreateValidWeapons() {
            for (MechWeapon weapon : enemy.getWeapons()) {
                assertNotNull(weapon.getType());
                assertNotNull(weapon.getSubType());
                assertTrue(weapon.getDamage() > 0);
            }
        }
    }
}
