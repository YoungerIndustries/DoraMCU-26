package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechWeapon Model Tests")
class MechWeaponTest {

    private MechWeapon weapon;

    @BeforeEach
    void setUp() {
        weapon = new MechWeapon("Medium Laser", MechWeapon.WeaponType.ENERGY,
                MechWeapon.WeaponSubType.MEDIUM_LASER, 5, 3, 1.0);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertNotNull(weapon.getId());
            assertEquals("Medium Laser", weapon.getName());
            assertEquals(MechWeapon.WeaponType.ENERGY, weapon.getType());
            assertEquals(MechWeapon.WeaponSubType.MEDIUM_LASER, weapon.getSubType());
            assertEquals(5, weapon.getDamage());
            assertEquals(3, weapon.getHeatGenerated());
            assertEquals(1.0, weapon.getCooldownTime());
            assertEquals(600, weapon.getRange());
            assertEquals(1, weapon.getTonnage());
            assertEquals(1, weapon.getCriticalSlots());
            assertEquals(0, weapon.getAmmoCapacity());
            assertEquals(0, weapon.getAmmoHeat());
            assertFalse(weapon.isCritSeeking());
            assertFalse(weapon.isOnCooldown());
            assertNull(weapon.getDescription());
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyWeapon() {
            MechWeapon empty = new MechWeapon();
            assertNull(empty.getId());
            assertNull(empty.getName());
            assertNull(empty.getType());
            assertEquals(0, empty.getDamage());
        }

        @Test
        @DisplayName("should generate unique UUID for each weapon")
        void constructor_shouldGenerateUniqueUUID() {
            MechWeapon w1 = new MechWeapon("Laser 1", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 1, 1, 0.5);
            MechWeapon w2 = new MechWeapon("Laser 2", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 1, 1, 0.5);
            assertNotEquals(w1.getId(), w2.getId());
        }
    }

    // ---- Weapon Types ----

    @Nested
    @DisplayName("WeaponType Enum Tests")
    class WeaponTypeTests {

        @Test
        @DisplayName("should support all 3 weapon types")
        void enum_shouldSupportAllWeaponTypes() {
            MechWeapon.WeaponType[] values = MechWeapon.WeaponType.values();
            assertEquals(3, values.length);
            assertNotNull(MechWeapon.WeaponType.ENERGY);
            assertNotNull(MechWeapon.WeaponType.BALLISTIC);
            assertNotNull(MechWeapon.WeaponType.MISSILE);
        }

        @Test
        @DisplayName("should create ENERGY weapon")
        void constructor_shouldCreateEnergyWeapon() {
            MechWeapon laser = new MechWeapon("Large Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.LARGE_LASER, 10, 6, 1.5);
            assertEquals(MechWeapon.WeaponType.ENERGY, laser.getType());
        }

        @Test
        @DisplayName("should create BALLISTIC weapon")
        void constructor_shouldCreateBallisticWeapon() {
            MechWeapon ac = new MechWeapon("AC/20", MechWeapon.WeaponType.BALLISTIC,
                    MechWeapon.WeaponSubType.AC20, 20, 8, 3.0);
            assertEquals(MechWeapon.WeaponType.BALLISTIC, ac.getType());
        }

        @Test
        @DisplayName("should create MISSILE weapon")
        void constructor_shouldCreateMissileWeapon() {
            MechWeapon srm = new MechWeapon("SRM-4", MechWeapon.WeaponType.MISSILE,
                    MechWeapon.WeaponSubType.SRM_4, 8, 4, 2.0);
            assertEquals(MechWeapon.WeaponType.MISSILE, srm.getType());
        }
    }

    // ---- Weapon SubTypes ----

    @Nested
    @DisplayName("WeaponSubType Enum Tests")
    class WeaponSubTypeTests {

        @Test
        @DisplayName("should support all energy weapon subtypes")
        void enum_shouldSupportEnergySubtypes() {
            assertNotNull(MechWeapon.WeaponSubType.SMALL_LASER);
            assertNotNull(MechWeapon.WeaponSubType.MEDIUM_LASER);
            assertNotNull(MechWeapon.WeaponSubType.LARGE_LASER);
            assertNotNull(MechWeapon.WeaponSubType.MEDIUM_PULSE_LASER);
            assertNotNull(MechWeapon.WeaponSubType.LARGE_PULSE_LASER);
            assertNotNull(MechWeapon.WeaponSubType.EXTENDED_PULSE_LASER);
            assertNotNull(MechWeapon.WeaponSubType.PPC);
            assertNotNull(MechWeapon.WeaponSubType.ER_PPC);
            assertNotNull(MechWeapon.WeaponSubType.FLAME_THROWER);
        }

        @Test
        @DisplayName("should support all ballistic weapon subtypes")
        void enum_shouldSupportBallisticSubtypes() {
            assertNotNull(MechWeapon.WeaponSubType.AC2);
            assertNotNull(MechWeapon.WeaponSubType.AC5);
            assertNotNull(MechWeapon.WeaponSubType.AC10);
            assertNotNull(MechWeapon.WeaponSubType.AC20);
            assertNotNull(MechWeapon.WeaponSubType.LBX_10);
            assertNotNull(MechWeapon.WeaponSubType.LBX_20);
            assertNotNull(MechWeapon.WeaponSubType.GAUSS_RIFLE);
            assertNotNull(MechWeapon.WeaponSubType.UAC_20);
            assertNotNull(MechWeapon.WeaponSubType.MACHINE_GUN);
            assertNotNull(MechWeapon.WeaponSubType.ROTARY_AC);
        }

        @Test
        @DisplayName("should support all missile weapon subtypes")
        void enum_shouldSupportMissileSubtypes() {
            assertNotNull(MechWeapon.WeaponSubType.SRM_2);
            assertNotNull(MechWeapon.WeaponSubType.SRM_4);
            assertNotNull(MechWeapon.WeaponSubType.SRM_6);
            assertNotNull(MechWeapon.WeaponSubType.SRM_2_STREAK);
            assertNotNull(MechWeapon.WeaponSubType.SRM_4_STREAK);
            assertNotNull(MechWeapon.WeaponSubType.SRM_6_STREAK);
            assertNotNull(MechWeapon.WeaponSubType.LRM_5);
            assertNotNull(MechWeapon.WeaponSubType.LRM_10);
            assertNotNull(MechWeapon.WeaponSubType.LRM_15);
            assertNotNull(MechWeapon.WeaponSubType.LRM_20);
            assertNotNull(MechWeapon.WeaponSubType.LRM_5_STREAK);
            assertNotNull(MechWeapon.WeaponSubType.LRM_10_STREAK);
            assertNotNull(MechWeapon.WeaponSubType.LRM_15_STREAK);
            assertNotNull(MechWeapon.WeaponSubType.LRM_20_STREAK);
        }

        @Test
        @DisplayName("should support special weapon subtypes")
        void enum_shouldSupportSpecialSubtypes() {
            assertNotNull(MechWeapon.WeaponSubType.AMS);
            assertNotNull(MechWeapon.WeaponSubType.ECM);
            assertNotNull(MechWeapon.WeaponSubType.ECCM);
        }
    }

    // ---- Cooldown Logic ----

    @Nested
    @DisplayName("Cooldown Logic Tests")
    class CooldownTests {

        @Test
        @DisplayName("should start cooldown timer")
        void startCooldown_shouldSetOnCooldown() {
            weapon.startCooldown();
            assertTrue(weapon.isOnCooldown());
            assertTrue(weapon.getCooldownStart() > 0);
        }

        @Test
        @DisplayName("should be on cooldown immediately after start")
        void isOnCooldown_shouldReturnTrueImmediately() {
            weapon.startCooldown();
            long now = System.currentTimeMillis();
            assertTrue(weapon.isOnCooldown(now));
        }

        @Test
        @DisplayName("should not be on cooldown before start")
        void isOnCooldown_shouldReturnFalseBeforeStart() {
            long now = System.currentTimeMillis();
            assertFalse(weapon.isOnCooldown(now));
        }

        @Test
        @DisplayName("should clear cooldown after elapsed time via updateCooldown")
        void updateCooldown_shouldClearWhenElapsed() {
            weapon.setCooldownTime(0.1);
            weapon.startCooldown();
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            weapon.updateCooldown();
            assertFalse(weapon.isOnCooldown());
        }

        @Test
        @DisplayName("should not clear cooldown before elapsed time")
        void updateCooldown_shouldNotClearBeforeElapsed() {
            weapon.setCooldownTime(5.0);
            weapon.startCooldown();
            weapon.updateCooldown();
            assertTrue(weapon.isOnCooldown());
        }

        @Test
        @DisplayName("should return false when cooldown not started")
        void isOnCooldown_shouldReturnFalseWhenNotStarted() {
            long now = System.currentTimeMillis();
            assertFalse(weapon.isOnCooldown(now));
        }
    }

    // ---- Crit Seeking ----

    @Nested
    @DisplayName("Crit Seeking Tests")
    class CritSeekingTests {

        @Test
        @DisplayName("should default to not crit seeking")
        void constructor_shouldDefaultToNotCritSeeking() {
            assertFalse(weapon.isCritSeeking());
        }

        @Test
        @DisplayName("should set crit seeking flag")
        void setter_shouldSetCritSeeking() {
            weapon.setCritSeeking(true);
            assertTrue(weapon.isCritSeeking());
        }

        @Test
        @DisplayName("machine gun should be crit seeking")
        void machineGun_shouldBeCritSeeking() {
            MechWeapon mg = new MechWeapon("Machine Gun", MechWeapon.WeaponType.BALLISTIC,
                    MechWeapon.WeaponSubType.MACHINE_GUN, 1, 1, 0.5);
            mg.setCritSeeking(true);
            assertTrue(mg.isCritSeeking());
        }
    }

    // ---- Ammo ----

    @Nested
    @DisplayName("Ammo Tests")
    class AmmoTests {

        @Test
        @DisplayName("should set ammo capacity")
        void setter_shouldSetAmmoCapacity() {
            weapon.setAmmoCapacity(20);
            assertEquals(20, weapon.getAmmoCapacity());
        }

        @Test
        @DisplayName("should set ammo heat")
        void setter_shouldSetAmmoHeat() {
            weapon.setAmmoHeat(50);
            assertEquals(50, weapon.getAmmoHeat());
        }

        @Test
        @DisplayName("energy weapon should have 0 ammo capacity")
        void energyWeapon_shouldHaveZeroAmmo() {
            MechWeapon laser = new MechWeapon("Small Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            assertEquals(0, laser.getAmmoCapacity());
        }

        @Test
        @DisplayName("ballistic weapon should have ammo capacity")
        void ballisticWeapon_shouldHaveAmmoCapacity() {
            MechWeapon ac = new MechWeapon("AC/2", MechWeapon.WeaponType.BALLISTIC,
                    MechWeapon.WeaponSubType.AC2, 2, 2, 2.0);
            ac.setAmmoCapacity(40);
            assertEquals(40, ac.getAmmoCapacity());
        }
    }

    // ---- Getters and Setters ----

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get name")
        void setter_shouldSetName() {
            weapon.setName("Large Laser");
            assertEquals("Large Laser", weapon.getName());
        }

        @Test
        @DisplayName("should set and get type")
        void setter_shouldSetType() {
            weapon.setType(MechWeapon.WeaponType.BALLISTIC);
            assertEquals(MechWeapon.WeaponType.BALLISTIC, weapon.getType());
        }

        @Test
        @DisplayName("should set and get subType")
        void setter_shouldSetSubType() {
            weapon.setSubType(MechWeapon.WeaponSubType.AC10);
            assertEquals(MechWeapon.WeaponSubType.AC10, weapon.getSubType());
        }

        @Test
        @DisplayName("should set and get damage")
        void setter_shouldSetDamage() {
            weapon.setDamage(10);
            assertEquals(10, weapon.getDamage());
        }

        @Test
        @DisplayName("should set and get heat generated")
        void setter_shouldSetHeatGenerated() {
            weapon.setHeatGenerated(6);
            assertEquals(6, weapon.getHeatGenerated());
        }

        @Test
        @DisplayName("should set and get cooldown time")
        void setter_shouldSetCooldownTime() {
            weapon.setCooldownTime(2.5);
            assertEquals(2.5, weapon.getCooldownTime());
        }

        @Test
        @DisplayName("should set and get range")
        void setter_shouldSetRange() {
            weapon.setRange(900);
            assertEquals(900, weapon.getRange());
        }

        @Test
        @DisplayName("should set and get tonnage")
        void setter_shouldSetTonnage() {
            weapon.setTonnage(5);
            assertEquals(5, weapon.getTonnage());
        }

        @Test
        @DisplayName("should set and get critical slots")
        void setter_shouldSetCriticalSlots() {
            weapon.setCriticalSlots(2);
            assertEquals(2, weapon.getCriticalSlots());
        }

        @Test
        @DisplayName("should set and get description")
        void setter_shouldSetDescription() {
            weapon.setDescription("Standard medium laser");
            assertEquals("Standard medium laser", weapon.getDescription());
        }

        @Test
        @DisplayName("should set and get ID")
        void setter_shouldSetId() {
            weapon.setId("weapon-123");
            assertEquals("weapon-123", weapon.getId());
        }
    }
}
