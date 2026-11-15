package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechChassis Model Tests")
class MechChassisTest {

    private MechChassis chassis;

    @BeforeEach
    void setUp() {
        chassis = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertEquals("Atlas", chassis.getName());
            assertEquals("ATL-6D", chassis.getDesignation());
            assertEquals(MechChassis.MechClass.ASSAULT, chassis.getMechClass());
            assertEquals(100, chassis.getTonnage());
            assertEquals(20, chassis.getHeatSinkSlots());
            assertEquals(200, chassis.getArmorPoints());
            assertEquals(100, chassis.getStructurePoints());
            assertEquals(200, chassis.getEngineRating());
            assertEquals(100, chassis.getCriticalSlots());
            assertEquals(0, chassis.getJumpJets());
            assertEquals(0.0, chassis.getMaxSpeed());
            assertEquals(0, chassis.getHardpointEnergy());
            assertEquals(0, chassis.getHardpointBallistic());
            assertEquals(0, chassis.getHardpointMissile());
            assertNull(chassis.getId());
            assertNull(chassis.getManufacturer());
            assertNull(chassis.getFaction());
            assertNull(chassis.getDescription());
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyChassis() {
            MechChassis empty = new MechChassis();
            assertNull(empty.getName());
            assertNull(empty.getId());
            assertNull(empty.getMechClass());
            assertEquals(0, empty.getTonnage());
        }

        @Test
        @DisplayName("should calculate heat sink slots as tonnage / 5")
        void constructor_shouldCalculateHeatSinkSlots() {
            MechChassis light = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            assertEquals(6, light.getHeatSinkSlots());

            MechChassis medium = new MechChassis("Medium", "CMD-2", MechChassis.MechClass.MEDIUM, 50);
            assertEquals(10, medium.getHeatSinkSlots());
        }

        @Test
        @DisplayName("should calculate armor points as tonnage * 2")
        void constructor_shouldCalculateArmorPoints() {
            MechChassis light = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            assertEquals(60, light.getArmorPoints());

            MechChassis heavy = new MechChassis("Heavy", "GRF-2", MechChassis.MechClass.HEAVY, 70);
            assertEquals(140, heavy.getArmorPoints());
        }

        @Test
        @DisplayName("should calculate structure points as tonnage")
        void constructor_shouldCalculateStructurePoints() {
            MechChassis light = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            assertEquals(30, light.getStructurePoints());

            MechChassis assault = new MechChassis("Assault", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            assertEquals(100, assault.getStructurePoints());
        }

        @Test
        @DisplayName("should calculate engine rating as tonnage * 2")
        void constructor_shouldCalculateEngineRating() {
            MechChassis light = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            assertEquals(60, light.getEngineRating());

            MechChassis assault = new MechChassis("Assault", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            assertEquals(200, assault.getEngineRating());
        }

        @Test
        @DisplayName("should calculate critical slots as tonnage")
        void constructor_shouldCalculateCriticalSlots() {
            MechChassis light = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            assertEquals(30, light.getCriticalSlots());

            MechChassis medium = new MechChassis("Medium", "CMD-2", MechChassis.MechClass.MEDIUM, 50);
            assertEquals(50, medium.getCriticalSlots());
        }
    }

    // ---- Mech Class Enum ----

    @Nested
    @DisplayName("MechClass Enum Tests")
    class MechClassTests {

        @Test
        @DisplayName("should support all 4 mech classes")
        void enum_shouldSupportAllMechClasses() {
            MechChassis.MechClass[] values = MechChassis.MechClass.values();
            assertEquals(4, values.length);
            assertNotNull(MechChassis.MechClass.LIGHT);
            assertNotNull(MechChassis.MechClass.MEDIUM);
            assertNotNull(MechChassis.MechClass.HEAVY);
            assertNotNull(MechChassis.MechClass.ASSAULT);
        }

        @Test
        @DisplayName("should create LIGHT mech (20-35 tons)")
        void constructor_shouldCreateLightMech() {
            MechChassis light = new MechChassis("Light", "LGT-1", MechChassis.MechClass.LIGHT, 30);
            assertEquals(MechChassis.MechClass.LIGHT, light.getMechClass());
            assertEquals(30, light.getTonnage());
        }

        @Test
        @DisplayName("should create MEDIUM mech (40-55 tons)")
        void constructor_shouldCreateMediumMech() {
            MechChassis medium = new MechChassis("Commando", "CMD-2", MechChassis.MechClass.MEDIUM, 50);
            assertEquals(MechChassis.MechClass.MEDIUM, medium.getMechClass());
            assertEquals(50, medium.getTonnage());
        }

        @Test
        @DisplayName("should create HEAVY mech (60-75 tons)")
        void constructor_shouldCreateHeavyMech() {
            MechChassis heavy = new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70);
            assertEquals(MechChassis.MechClass.HEAVY, heavy.getMechClass());
            assertEquals(70, heavy.getTonnage());
        }

        @Test
        @DisplayName("should create ASSAULT mech (80-100 tons)")
        void constructor_shouldCreateAssaultMech() {
            MechChassis assault = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            assertEquals(MechChassis.MechClass.ASSAULT, assault.getMechClass());
            assertEquals(100, assault.getTonnage());
        }
    }

    // ---- Manufacturer Enum ----

    @Nested
    @DisplayName("MechManufacturer Enum Tests")
    class ManufacturerTests {

        @Test
        @DisplayName("should support all 12 manufacturers")
        void enum_shouldSupportAllManufacturers() {
            MechChassis.MechManufacturer[] values = MechChassis.MechManufacturer.values();
            assertEquals(12, values.length);
        }

        @Test
        @DisplayName("should set IBC manufacturer")
        void setter_shouldSetIBCManufacturer() {
            chassis.setManufacturer(MechChassis.MechManufacturer.IBC);
            assertEquals(MechChassis.MechManufacturer.IBC, chassis.getManufacturer());
        }

        @Test
        @DisplayName("should set Kallon manufacturer")
        void setter_shouldSetKallonManufacturer() {
            chassis.setManufacturer(MechChassis.MechManufacturer.Kallon);
            assertEquals(MechChassis.MechManufacturer.Kallon, chassis.getManufacturer());
        }

        @Test
        @DisplayName("should set CLAN manufacturer")
        void setter_shouldSetClanManufacturer() {
            chassis.setManufacturer(MechChassis.MechManufacturer.CLAN);
            assertEquals(MechChassis.MechManufacturer.CLAN, chassis.getManufacturer());
        }
    }

    // ---- Getters and Setters ----

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get name")
        void setter_shouldSetName() {
            chassis.setName("Griffin");
            assertEquals("Griffin", chassis.getName());
        }

        @Test
        @DisplayName("should set and get designation")
        void setter_shouldSetDesignation() {
            chassis.setDesignation("GRF-2");
            assertEquals("GRF-2", chassis.getDesignation());
        }

        @Test
        @DisplayName("should set and get mech class")
        void setter_shouldSetMechClass() {
            chassis.setMechClass(MechChassis.MechClass.HEAVY);
            assertEquals(MechChassis.MechClass.HEAVY, chassis.getMechClass());
        }

        @Test
        @DisplayName("should set and get tonnage")
        void setter_shouldSetTonnage() {
            chassis.setTonnage(75);
            assertEquals(75, chassis.getTonnage());
        }

        @Test
        @DisplayName("should set and get max speed")
        void setter_shouldSetMaxSpeed() {
            chassis.setMaxSpeed(132.0);
            assertEquals(132.0, chassis.getMaxSpeed());
        }

        @Test
        @DisplayName("should set and get jump jets")
        void setter_shouldSetJumpJets() {
            chassis.setJumpJets(6);
            assertEquals(6, chassis.getJumpJets());
        }

        @Test
        @DisplayName("should set and get heat sink slots")
        void setter_shouldSetHeatSinkSlots() {
            chassis.setHeatSinkSlots(25);
            assertEquals(25, chassis.getHeatSinkSlots());
        }

        @Test
        @DisplayName("should set and get armor points")
        void setter_shouldSetArmorPoints() {
            chassis.setArmorPoints(250);
            assertEquals(250, chassis.getArmorPoints());
        }

        @Test
        @DisplayName("should set and get structure points")
        void setter_shouldSetStructurePoints() {
            chassis.setStructurePoints(120);
            assertEquals(120, chassis.getStructurePoints());
        }

        @Test
        @DisplayName("should set and get engine rating")
        void setter_shouldSetEngineRating() {
            chassis.setEngineRating(280);
            assertEquals(280, chassis.getEngineRating());
        }

        @Test
        @DisplayName("should set and get hardpoint energy")
        void setter_shouldSetHardpointEnergy() {
            chassis.setHardpointEnergy(4);
            assertEquals(4, chassis.getHardpointEnergy());
        }

        @Test
        @DisplayName("should set and get hardpoint ballistic")
        void setter_shouldSetHardpointBallistic() {
            chassis.setHardpointBallistic(3);
            assertEquals(3, chassis.getHardpointBallistic());
        }

        @Test
        @DisplayName("should set and get hardpoint missile")
        void setter_shouldSetHardpointMissile() {
            chassis.setHardpointMissile(2);
            assertEquals(2, chassis.getHardpointMissile());
        }

        @Test
        @DisplayName("should set and get critical slots")
        void setter_shouldSetCriticalSlots() {
            chassis.setCriticalSlots(80);
            assertEquals(80, chassis.getCriticalSlots());
        }

        @Test
        @DisplayName("should set and get faction")
        void setter_shouldSetFaction() {
            chassis.setFaction("Federated Suns");
            assertEquals("Federated Suns", chassis.getFaction());
        }

        @Test
        @DisplayName("should set and get description")
        void setter_shouldSetDescription() {
            chassis.setDescription("Heavy assault mech");
            assertEquals("Heavy assault mech", chassis.getDescription());
        }

        @Test
        @DisplayName("should set and get ID")
        void setter_shouldSetId() {
            chassis.setId("chassis-123");
            assertEquals("chassis-123", chassis.getId());
        }
    }
}
