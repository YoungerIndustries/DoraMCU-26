package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechComponent Model Tests")
class MechComponentTest {

    private MechComponent component;

    @BeforeEach
    void setUp() {
        component = new MechComponent(MechComponent.ComponentLocation.CENTER_TORSO, 30, 30);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertNotNull(component.getId());
            assertEquals(MechComponent.ComponentLocation.CENTER_TORSO, component.getLocation());
            assertEquals(30, component.getArmorCurrent());
            assertEquals(30, component.getArmorMax());
            assertEquals(30, component.getStructureCurrent());
            assertEquals(30, component.getStructureMax());
            assertFalse(component.isDestroyed());
            assertTrue(component.getMountedWeapons().isEmpty());
            assertTrue(component.getEquipmentSlots().isEmpty());
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyComponent() {
            MechComponent empty = new MechComponent();
            assertNull(empty.getId());
            assertNull(empty.getLocation());
            assertEquals(0, empty.getArmorCurrent());
            assertEquals(0, empty.getStructureCurrent());
        }

        @Test
        @DisplayName("should set armor current equal to max")
        void constructor_shouldSetArmorCurrentToMax() {
            MechComponent head = new MechComponent(MechComponent.ComponentLocation.HEAD, 5, 10);
            assertEquals(5, head.getArmorCurrent());
            assertEquals(5, head.getArmorMax());
        }

        @Test
        @DisplayName("should set structure current equal to max")
        void constructor_shouldSetStructureCurrentToMax() {
            MechComponent head = new MechComponent(MechComponent.ComponentLocation.HEAD, 5, 10);
            assertEquals(10, head.getStructureCurrent());
            assertEquals(10, head.getStructureMax());
        }
    }

    // ---- Component Locations ----

    @Nested
    @DisplayName("ComponentLocation Enum Tests")
    class ComponentLocationTests {

        @Test
        @DisplayName("should support all 8 component locations")
        void enum_shouldSupportAllLocations() {
            MechComponent.ComponentLocation[] values = MechComponent.ComponentLocation.values();
            assertEquals(8, values.length);
        }

        @Test
        @DisplayName("should create HEAD component")
        void constructor_shouldCreateHeadComponent() {
            MechComponent head = new MechComponent(MechComponent.ComponentLocation.HEAD, 5, 10);
            assertEquals(MechComponent.ComponentLocation.HEAD, head.getLocation());
        }

        @Test
        @DisplayName("should create CENTER_TORSO component")
        void constructor_shouldCreateCenterTorsoComponent() {
            MechComponent ct = new MechComponent(MechComponent.ComponentLocation.CENTER_TORSO, 30, 30);
            assertEquals(MechComponent.ComponentLocation.CENTER_TORSO, ct.getLocation());
        }

        @Test
        @DisplayName("should create LEFT_TORSO component")
        void constructor_shouldCreateLeftTorsoComponent() {
            MechComponent lt = new MechComponent(MechComponent.ComponentLocation.LEFT_TORSO, 20, 20);
            assertEquals(MechComponent.ComponentLocation.LEFT_TORSO, lt.getLocation());
        }

        @Test
        @DisplayName("should create RIGHT_TORSO component")
        void constructor_shouldCreateRightTorsoComponent() {
            MechComponent rt = new MechComponent(MechComponent.ComponentLocation.RIGHT_TORSO, 20, 20);
            assertEquals(MechComponent.ComponentLocation.RIGHT_TORSO, rt.getLocation());
        }

        @Test
        @DisplayName("should create LEFT_ARM component")
        void constructor_shouldCreateLeftArmComponent() {
            MechComponent la = new MechComponent(MechComponent.ComponentLocation.LEFT_ARM, 10, 10);
            assertEquals(MechComponent.ComponentLocation.LEFT_ARM, la.getLocation());
        }

        @Test
        @DisplayName("should create RIGHT_ARM component")
        void constructor_shouldCreateRightArmComponent() {
            MechComponent ra = new MechComponent(MechComponent.ComponentLocation.RIGHT_ARM, 10, 10);
            assertEquals(MechComponent.ComponentLocation.RIGHT_ARM, ra.getLocation());
        }

        @Test
        @DisplayName("should create LEFT_LEG component")
        void constructor_shouldCreateLeftLegComponent() {
            MechComponent ll = new MechComponent(MechComponent.ComponentLocation.LEFT_LEG, 7, 7);
            assertEquals(MechComponent.ComponentLocation.LEFT_LEG, ll.getLocation());
        }

        @Test
        @DisplayName("should create RIGHT_LEG component")
        void constructor_shouldCreateRightLegComponent() {
            MechComponent rl = new MechComponent(MechComponent.ComponentLocation.RIGHT_LEG, 7, 7);
            assertEquals(MechComponent.ComponentLocation.RIGHT_LEG, rl.getLocation());
        }
    }

    // ---- Damage Application ----

    @Nested
    @DisplayName("Damage Application Tests")
    class DamageApplicationTests {

        @Test
        @DisplayName("should apply damage to armor first")
        void applyDamage_shouldApplyToArmorFirst() {
            int overflow = component.applyDamage(15);
            assertEquals(15, component.getArmorCurrent());
            assertEquals(30, component.getStructureCurrent());
            assertEquals(0, overflow);
        }

        @Test
        @DisplayName("should apply damage to structure after armor depleted")
        void applyDamage_shouldApplyToStructureAfterArmor() {
            component.applyDamage(30); // Deplete armor
            component.applyDamage(10); // Apply to structure
            assertEquals(0, component.getArmorCurrent());
            assertEquals(20, component.getStructureCurrent());
        }

        @Test
        @DisplayName("should return overflow damage when component fully destroyed")
        void applyDamage_shouldReturnOverflow() {
            int overflow = component.applyDamage(61); // 30 armor + 30 structure, 1 overflow
            assertEquals(0, component.getArmorCurrent());
            assertEquals(0, component.getStructureCurrent());
            assertTrue(component.isDestroyed());
            assertEquals(1, overflow);
        }

        @Test
        @DisplayName("should return all damage if component already destroyed")
        void applyDamage_shouldReturnAllDamageIfDestroyed() {
            component.applyDamage(60); // Destroy the component
            int overflow = component.applyDamage(10);
            assertEquals(10, overflow);
        }

        @Test
        @DisplayName("should destroy component when structure reaches zero")
        void applyDamage_shouldDestroyWhenStructureZero() {
            component.applyDamage(30); // Deplete armor
            component.applyDamage(30); // Deplete structure
            assertTrue(component.isDestroyed());
            assertEquals(0, component.getStructureCurrent());
        }

        @Test
        @DisplayName("should not destroy component when only armor depleted")
        void applyDamage_shouldNotDestroyWhenOnlyArmorDepleted() {
            component.applyDamage(30); // Deplete armor
            assertFalse(component.isDestroyed());
            assertEquals(0, component.getArmorCurrent());
            assertEquals(30, component.getStructureCurrent());
        }

        @Test
        @DisplayName("should handle partial damage to armor")
        void applyDamage_shouldHandlePartialArmorDamage() {
            component.applyDamage(10);
            assertEquals(20, component.getArmorCurrent());
            assertEquals(30, component.getStructureCurrent());
        }

        @Test
        @DisplayName("should handle damage that depletes armor and partially hits structure")
        void applyDamage_shouldDepleteArmorAndPartiallyHitStructure() {
            component.applyDamage(40); // 30 armor + 10 structure
            assertEquals(0, component.getArmorCurrent());
            assertEquals(20, component.getStructureCurrent());
            assertFalse(component.isDestroyed());
        }
    }

    // ---- Weapon Mounting ----

    @Nested
    @DisplayName("Weapon Mounting Tests")
    class WeaponMountingTests {

        @Test
        @DisplayName("should check if weapon can be mounted")
        void canMountWeapon_shouldReturnTrueWithSpace() {
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            weapon.setCriticalSlots(1);
            assertTrue(component.canMountWeapon(weapon));
        }

        @Test
        @DisplayName("should mount weapon successfully")
        void mountWeapon_shouldMountSuccessfully() {
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            weapon.setCriticalSlots(1);
            assertTrue(component.mountWeapon(weapon));
            assertEquals(1, component.getMountedWeapons().size());
            assertEquals(weapon, component.getMountedWeapons().get(0));
        }

        @Test
        @DisplayName("should not mount weapon on destroyed component")
        void mountWeapon_shouldFailOnDestroyedComponent() {
            component.applyDamage(60); // Destroy
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            assertFalse(component.canMountWeapon(weapon));
            assertFalse(component.mountWeapon(weapon));
        }

        @Test
        @DisplayName("should not mount weapon if no space")
        void mountWeapon_shouldFailIfNoSpace() {
            MechWeapon bigWeapon = new MechWeapon("Big Weapon", MechWeapon.WeaponType.BALLISTIC,
                    MechWeapon.WeaponSubType.GAUSS_RIFLE, 25, 10, 3.0);
            bigWeapon.setCriticalSlots(100); // Too large
            assertFalse(component.canMountWeapon(bigWeapon));
            assertFalse(component.mountWeapon(bigWeapon));
        }

        @Test
        @DisplayName("should remove weapon by ID")
        void removeWeapon_shouldRemoveById() {
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            component.mountWeapon(weapon);
            assertTrue(component.removeWeapon(weapon.getId()));
            assertEquals(0, component.getMountedWeapons().size());
        }

        @Test
        @DisplayName("should return false when removing non-existent weapon")
        void removeWeapon_shouldReturnFalseForNonExistent() {
            assertFalse(component.removeWeapon("non-existent-id"));
        }

        @Test
        @DisplayName("should mount multiple weapons")
        void mountWeapon_shouldMountMultipleWeapons() {
            MechWeapon w1 = new MechWeapon("Laser 1", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            MechWeapon w2 = new MechWeapon("Laser 2", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            component.mountWeapon(w1);
            component.mountWeapon(w2);
            assertEquals(2, component.getMountedWeapons().size());
        }
    }

    // ---- Integrity Percentage ----

    @Nested
    @DisplayName("Integrity Percentage Tests")
    class IntegrityTests {

        @Test
        @DisplayName("should return 100% when fully intact")
        void getIntegrityPercentage_shouldReturn100WhenFull() {
            assertEquals(100.0, component.getIntegrityPercentage());
        }

        @Test
        @DisplayName("should return 0% when destroyed")
        void getIntegrityPercentage_shouldReturn0WhenDestroyed() {
            component.applyDamage(60);
            assertEquals(0.0, component.getIntegrityPercentage());
        }

        @Test
        @DisplayName("should return proportional percentage")
        void getIntegrityPercentage_shouldReturnProportional() {
            component.applyDamage(15); // Half armor depleted: armor 15/30
            // armorPct = 15/30 * 50 = 25, structurePct = 30/30 * 50 = 50
            assertEquals(75.0, component.getIntegrityPercentage());
        }

        @Test
        @DisplayName("should return 50% when armor fully depleted")
        void getIntegrityPercentage_shouldReturn50WhenArmorDepleted() {
            component.applyDamage(30); // All armor gone
            // armorPct = 0/30 * 50 = 0, structurePct = 30/30 * 50 = 50
            assertEquals(50.0, component.getIntegrityPercentage());
        }

        @Test
        @DisplayName("should return 25% when armor and half structure depleted")
        void getIntegrityPercentage_shouldReturn25WhenHalfStructure() {
            component.applyDamage(30); // All armor gone
            component.applyDamage(15); // Half structure gone
            // armorPct = 0, structurePct = 15/30 * 50 = 25
            assertEquals(25.0, component.getIntegrityPercentage());
        }
    }

    // ---- Equipment Slots ----

    @Nested
    @DisplayName("Equipment Slots Tests")
    class EquipmentSlotsTests {

        @Test
        @DisplayName("should add equipment slots")
        void equipmentSlots_shouldAddSlots() {
            component.getEquipmentSlots().add("Heat Sink");
            component.getEquipmentSlots().add("Ammo Bin");
            assertEquals(2, component.getEquipmentSlots().size());
        }

        @Test
        @DisplayName("should start with empty equipment slots")
        void constructor_shouldHaveEmptyEquipmentSlots() {
            assertTrue(component.getEquipmentSlots().isEmpty());
        }
    }

    // ---- Getters and Setters ----

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get ID")
        void setter_shouldSetId() {
            component.setId("comp-123");
            assertEquals("comp-123", component.getId());
        }

        @Test
        @DisplayName("should set and get location")
        void setter_shouldSetLocation() {
            component.setLocation(MechComponent.ComponentLocation.HEAD);
            assertEquals(MechComponent.ComponentLocation.HEAD, component.getLocation());
        }

        @Test
        @DisplayName("should set and get armor current")
        void setter_shouldSetArmorCurrent() {
            component.setArmorCurrent(20);
            assertEquals(20, component.getArmorCurrent());
        }

        @Test
        @DisplayName("should set and get armor max")
        void setter_shouldSetArmorMax() {
            component.setArmorMax(50);
            assertEquals(50, component.getArmorMax());
        }

        @Test
        @DisplayName("should set and get structure current")
        void setter_shouldSetStructureCurrent() {
            component.setStructureCurrent(20);
            assertEquals(20, component.getStructureCurrent());
        }

        @Test
        @DisplayName("should set and get structure max")
        void setter_shouldSetStructureMax() {
            component.setStructureMax(50);
            assertEquals(50, component.getStructureMax());
        }

        @Test
        @DisplayName("should set and get destroyed flag")
        void setter_shouldSetDestroyed() {
            component.setDestroyed(true);
            assertTrue(component.isDestroyed());
        }
    }
}
