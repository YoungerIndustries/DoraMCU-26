package com.yisy.modules.doramcu.mechwarrior.engine;

import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechComponent;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechWarriorMech Engine Tests")
class MechWarriorMechTest {

    private MechWarriorMech mech;
    private MechPilot pilot;
    private MechChassis chassis;

    @BeforeEach
    void setUp() {
        pilot = new MechPilot("Test Pilot");
        chassis = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
        mech = new MechWarriorMech(pilot, chassis);
    }

    // ---- Component Initialization ----

    @Nested
    @DisplayName("Component Initialization Tests")
    class ComponentInitializationTests {

        @Test
        @DisplayName("should create 8 components")
        void constructor_shouldCreate8Components() {
            assertEquals(8, mech.getAllComponents().size());
        }

        @Test
        @DisplayName("should initialize HEAD component")
        void constructor_shouldInitializeHead() {
            MechComponent head = mech.getComponent(MechComponent.ComponentLocation.HEAD);
            assertNotNull(head);
            assertEquals(5, head.getArmorMax());
            assertEquals(10, head.getStructureMax());
        }

        @Test
        @DisplayName("should initialize CENTER_TORSO component")
        void constructor_shouldInitializeCenterTorso() {
            MechComponent ct = mech.getComponent(MechComponent.ComponentLocation.CENTER_TORSO);
            assertNotNull(ct);
            assertEquals(33, ct.getArmorMax());
            assertEquals(33, ct.getStructureMax());
        }

        @Test
        @DisplayName("should initialize LEFT_TORSO component")
        void constructor_shouldInitializeLeftTorso() {
            MechComponent lt = mech.getComponent(MechComponent.ComponentLocation.LEFT_TORSO);
            assertNotNull(lt);
            assertEquals(20, lt.getArmorMax());
            assertEquals(20, lt.getStructureMax());
        }

        @Test
        @DisplayName("should initialize RIGHT_TORSO component")
        void constructor_shouldInitializeRightTorso() {
            MechComponent rt = mech.getComponent(MechComponent.ComponentLocation.RIGHT_TORSO);
            assertNotNull(rt);
            assertEquals(20, rt.getArmorMax());
            assertEquals(20, rt.getStructureMax());
        }

        @Test
        @DisplayName("should initialize LEFT_ARM component")
        void constructor_shouldInitializeLeftArm() {
            MechComponent la = mech.getComponent(MechComponent.ComponentLocation.LEFT_ARM);
            assertNotNull(la);
            assertEquals(10, la.getArmorMax());
            assertEquals(10, la.getStructureMax());
        }

        @Test
        @DisplayName("should initialize RIGHT_ARM component")
        void constructor_shouldInitializeRightArm() {
            MechComponent ra = mech.getComponent(MechComponent.ComponentLocation.RIGHT_ARM);
            assertNotNull(ra);
            assertEquals(10, ra.getArmorMax());
            assertEquals(10, ra.getStructureMax());
        }

        @Test
        @DisplayName("should initialize LEFT_LEG component")
        void constructor_shouldInitializeLeftLeg() {
            MechComponent ll = mech.getComponent(MechComponent.ComponentLocation.LEFT_LEG);
            assertNotNull(ll);
            assertEquals(7, ll.getArmorMax());
            assertEquals(7, ll.getStructureMax());
        }

        @Test
        @DisplayName("should initialize RIGHT_LEG component")
        void constructor_shouldInitializeRightLeg() {
            MechComponent rl = mech.getComponent(MechComponent.ComponentLocation.RIGHT_LEG);
            assertNotNull(rl);
            assertEquals(7, rl.getArmorMax());
            assertEquals(7, rl.getStructureMax());
        }

        @Test
        @DisplayName("should scale components by tonnage")
        void constructor_shouldScaleByTonnage() {
            MechChassis lightChassis = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            MechWarriorMech lightMech = new MechWarriorMech(new MechPilot("Light Pilot"), lightChassis);
            MechComponent ct = lightMech.getComponent(MechComponent.ComponentLocation.CENTER_TORSO);
            assertEquals(10, ct.getArmorMax());
            assertEquals(10, ct.getStructureMax());
        }
    }

    // ---- Weapon Mounting ----

    @Nested
    @DisplayName("Weapon Mounting Tests")
    class WeaponMountingTests {

        @Test
        @DisplayName("should mount weapon successfully")
        void mountWeapon_shouldMountSuccessfully() {
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            weapon.setCriticalSlots(1);
            assertTrue(mech.mountWeapon(weapon, MechComponent.ComponentLocation.LEFT_TORSO));
        }

        @Test
        @DisplayName("should not mount weapon if no space")
        void mountWeapon_shouldFailIfNoSpace() {
            MechWeapon bigWeapon = new MechWeapon("Big Weapon", MechWeapon.WeaponType.BALLISTIC,
                    MechWeapon.WeaponSubType.GAUSS_RIFLE, 25, 10, 3.0);
            bigWeapon.setCriticalSlots(100);
            assertFalse(mech.mountWeapon(bigWeapon, MechComponent.ComponentLocation.HEAD));
        }

        @Test
        @DisplayName("should get weapon by ID")
        void getWeapon_shouldReturnById() {
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            weapon.setCriticalSlots(1);
            mech.mountWeapon(weapon, MechComponent.ComponentLocation.LEFT_TORSO);
            assertEquals(weapon, mech.getWeapon(weapon.getId()));
        }

        @Test
        @DisplayName("should return null for non-existent weapon")
        void getWeapon_shouldReturnNull() {
            assertNull(mech.getWeapon("non-existent"));
        }

        @Test
        @DisplayName("should return all weapons")
        void getAllWeapons_shouldReturnAll() {
            MechWeapon w1 = new MechWeapon("Laser 1", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.5);
            MechWeapon w2 = new MechWeapon("Laser 2", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.MEDIUM_LASER, 5, 3, 1.0);
            w1.setCriticalSlots(1);
            w2.setCriticalSlots(1);
            mech.mountWeapon(w1, MechComponent.ComponentLocation.LEFT_TORSO);
            mech.mountWeapon(w2, MechComponent.ComponentLocation.RIGHT_TORSO);
            assertEquals(2, mech.getAllWeapons().size());
        }

        @Test
        @DisplayName("should start with no weapons")
        void constructor_shouldHaveNoWeapons() {
            assertTrue(mech.getAllWeapons().isEmpty());
        }
    }

    // ---- Operational Checks ----

    @Nested
    @DisplayName("Operational Check Tests")
    class OperationalTests {

        @Test
        @DisplayName("should be operational by default")
        void isOperational_shouldBeTrueByDefault() {
            assertTrue(mech.isOperational());
        }

        @Test
        @DisplayName("should not be operational if both legs destroyed")
        void isOperational_shouldBeFalseIfBothLegsDestroyed() {
            MechComponent leftLeg = mech.getComponent(MechComponent.ComponentLocation.LEFT_LEG);
            MechComponent rightLeg = mech.getComponent(MechComponent.ComponentLocation.RIGHT_LEG);
            leftLeg.applyDamage(leftLeg.getArmorMax() + leftLeg.getStructureMax());
            rightLeg.applyDamage(rightLeg.getArmorMax() + rightLeg.getStructureMax());
            assertFalse(mech.isOperational());
        }

        @Test
        @DisplayName("should be operational if one leg destroyed")
        void isOperational_shouldBeTrueIfOneLegDestroyed() {
            MechComponent leftLeg = mech.getComponent(MechComponent.ComponentLocation.LEFT_LEG);
            leftLeg.applyDamage(leftLeg.getArmorMax() + leftLeg.getStructureMax());
            assertTrue(mech.isOperational());
        }

        @Test
        @DisplayName("should not be operational if center torso destroyed")
        void isOperational_shouldBeFalseIfCenterTorsoDestroyed() {
            MechComponent ct = mech.getComponent(MechComponent.ComponentLocation.CENTER_TORSO);
            ct.applyDamage(ct.getArmorMax() + ct.getStructureMax());
            assertFalse(mech.isOperational());
        }

        @Test
        @DisplayName("should not be operational if head destroyed")
        void isOperational_shouldBeFalseIfHeadDestroyed() {
            MechComponent head = mech.getComponent(MechComponent.ComponentLocation.HEAD);
            head.applyDamage(head.getArmorMax() + head.getStructureMax());
            assertFalse(mech.isOperational());
        }

        @Test
        @DisplayName("should not be operational if destroyed")
        void isOperational_shouldBeFalseIfDestroyed() {
            mech.setDestroyed(true);
            assertFalse(mech.isOperational());
        }

        @Test
        @DisplayName("should not be operational if ejected")
        void isOperational_shouldBeFalseIfEjected() {
            mech.setEjected(true);
            assertFalse(mech.isOperational());
        }
    }

    // ---- Distance Calculation ----

    @Nested
    @DisplayName("Distance Calculation Tests")
    class DistanceTests {

        @Test
        @DisplayName("should calculate distance between two mechs")
        void getDistanceTo_shouldCalculateDistance() {
            mech.setPosX(0);
            mech.setPosY(0);
            mech.setPosZ(0);

            MechWarriorMech other = new MechWarriorMech(new MechPilot("Enemy"),
                    new MechChassis("Enemy", "ENM-1", MechChassis.MechClass.MEDIUM, 50));
            other.setPosX(3);
            other.setPosY(4);
            other.setPosZ(0);

            assertEquals(5.0, mech.getDistanceTo(other));
        }

        @Test
        @DisplayName("should return 0 for same position")
        void getDistanceTo_shouldReturnZeroForSamePosition() {
            MechWarriorMech other = new MechWarriorMech(new MechPilot("Enemy"),
                    new MechChassis("Enemy", "ENM-1", MechChassis.MechClass.MEDIUM, 50));
            assertEquals(0.0, mech.getDistanceTo(other));
        }

        @Test
        @DisplayName("should calculate 3D distance")
        void getDistanceTo_shouldCalculate3DDistance() {
            mech.setPosX(0);
            mech.setPosY(0);
            mech.setPosZ(0);

            MechWarriorMech other = new MechWarriorMech(new MechPilot("Enemy"),
                    new MechChassis("Enemy", "ENM-1", MechChassis.MechClass.MEDIUM, 50));
            other.setPosX(1);
            other.setPosY(2);
            other.setPosZ(2);

            assertEquals(3.0, mech.getDistanceTo(other));
        }
    }

    // ---- Stability Damage ----

    @Nested
    @DisplayName("Stability Damage Tests")
    class StabilityTests {

        @Test
        @DisplayName("should start with 0 stability damage")
        void constructor_shouldHaveZeroStabilityDamage() {
            assertEquals(0.0, mech.getStabilityDamage());
        }

        @Test
        @DisplayName("should add stability damage")
        void addStabilityDamage_shouldAddDamage() {
            mech.addStabilityDamage(10.0);
            assertEquals(10.0, mech.getStabilityDamage());
        }

        @Test
        @DisplayName("should accumulate stability damage")
        void addStabilityDamage_shouldAccumulate() {
            mech.addStabilityDamage(10.0);
            mech.addStabilityDamage(15.0);
            assertEquals(25.0, mech.getStabilityDamage());
        }

        @Test
        @DisplayName("should not be knocked down by default")
        void constructor_shouldNotBeKnockedDown() {
            assertFalse(mech.isKnockedDown());
        }
    }

    // ---- Position and Orientation ----

    @Nested
    @DisplayName("Position and Orientation Tests")
    class PositionTests {

        @Test
        @DisplayName("should set and get posX")
        void setter_shouldSetPosX() {
            mech.setPosX(10.0);
            assertEquals(10.0, mech.getPosX());
        }

        @Test
        @DisplayName("should set and get posY")
        void setter_shouldSetPosY() {
            mech.setPosY(20.0);
            assertEquals(20.0, mech.getPosY());
        }

        @Test
        @DisplayName("should set and get posZ")
        void setter_shouldSetPosZ() {
            mech.setPosZ(30.0);
            assertEquals(30.0, mech.getPosZ());
        }

        @Test
        @DisplayName("should set and get torso twist")
        void setter_shouldSetTorsoTwist() {
            mech.setTorsoTwist(45.0);
            assertEquals(45.0, mech.getTorsoTwist());
        }

        @Test
        @DisplayName("should set and get facing angle")
        void setter_shouldSetFacingAngle() {
            mech.setFacingAngle(90.0);
            assertEquals(90.0, mech.getFacingAngle());
        }
    }

    // ---- Heat System ----

    @Nested
    @DisplayName("Heat System Tests")
    class HeatTests {

        @Test
        @DisplayName("should start with 0 heat")
        void constructor_shouldHaveZeroHeat() {
            assertEquals(0.0, mech.getHeatLevel());
        }

        @Test
        @DisplayName("should set and get heat level")
        void setter_shouldSetHeatLevel() {
            mech.setHeatLevel(50.0);
            assertEquals(50.0, mech.getHeatLevel());
        }

        @Test
        @DisplayName("should not have heat override by default")
        void constructor_shouldNotHaveHeatOverride() {
            assertFalse(mech.isHeatOverride());
        }

        @Test
        @DisplayName("should set and get heat override")
        void setter_shouldSetHeatOverride() {
            mech.setHeatOverride(true);
            assertTrue(mech.isHeatOverride());
        }

        @Test
        @DisplayName("should not be shutdown by default")
        void constructor_shouldNotBeShutdown() {
            assertFalse(mech.isShutdown());
        }

        @Test
        @DisplayName("should set and get shutdown")
        void setter_shouldSetShutdown() {
            mech.setShutdown(true);
            assertTrue(mech.isShutdown());
        }
    }

    // ---- Movement ----

    @Nested
    @DisplayName("Movement Tests")
    class MovementTests {

        @Test
        @DisplayName("should not be moving by default")
        void constructor_shouldNotBeMoving() {
            assertFalse(mech.isMoving());
        }

        @Test
        @DisplayName("should set and get moving")
        void setter_shouldSetMoving() {
            mech.setMoving(true);
            assertTrue(mech.isMoving());
        }

        @Test
        @DisplayName("should start with 0 speed")
        void constructor_shouldHaveZeroSpeed() {
            assertEquals(0.0, mech.getCurrentSpeed());
        }

        @Test
        @DisplayName("should set and get current speed")
        void setter_shouldSetCurrentSpeed() {
            mech.setCurrentSpeed(66.0);
            assertEquals(66.0, mech.getCurrentSpeed());
        }
    }

    // ---- Destroyed and Ejected States ----

    @Nested
    @DisplayName("Destroyed and Ejected State Tests")
    class StateTests {

        @Test
        @DisplayName("should not be destroyed by default")
        void constructor_shouldNotBeDestroyed() {
            assertFalse(mech.isDestroyed());
        }

        @Test
        @DisplayName("should set and get destroyed")
        void setter_shouldSetDestroyed() {
            mech.setDestroyed(true);
            assertTrue(mech.isDestroyed());
        }

        @Test
        @DisplayName("should not be ejected by default")
        void constructor_shouldNotBeEjected() {
            assertFalse(mech.isEjected());
        }

        @Test
        @DisplayName("should set and get ejected")
        void setter_shouldSetEjected() {
            mech.setEjected(true);
            assertTrue(mech.isEjected());
        }
    }

    // ---- Pilot and Chassis ----

    @Nested
    @DisplayName("Pilot and Chassis Tests")
    class PilotChassisTests {

        @Test
        @DisplayName("should return pilot")
        void getPilot_shouldReturnPilot() {
            assertEquals(pilot, mech.getPilot());
        }

        @Test
        @DisplayName("should return chassis")
        void getChassis_shouldReturnChassis() {
            assertEquals(chassis, mech.getChassis());
        }
    }

    // ---- Last Fire Time ----

    @Nested
    @DisplayName("Last Fire Time Tests")
    class LastFireTimeTests {

        @Test
        @DisplayName("should start with 0 last fire time")
        void constructor_shouldHaveZeroLastFireTime() {
            assertEquals(0, mech.getLastFireTime());
        }

        @Test
        @DisplayName("should set and get last fire time")
        void setter_shouldSetLastFireTime() {
            mech.setLastFireTime(1000);
            assertEquals(1000, mech.getLastFireTime());
        }
    }
}
