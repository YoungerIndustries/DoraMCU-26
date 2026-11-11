package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5ShipDesign Tests")
class B5ShipDesignTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Creates ship with valid tonnage")
        void validTonnage() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertEquals(1, ship.getId());
            assertEquals("Test Ship", ship.getName());
            assertEquals(10000, ship.getTotalTonnage());
        }

        @Test
        @DisplayName("Minimum tonnage is 1000")
        void minimumTonnage() {
            B5ShipDesign ship = new B5ShipDesign(1, "Small Ship", 1000);
            assertEquals(1000, ship.getTotalTonnage());
        }

        @Test
        @DisplayName("Throws on tonnage below 1000")
        void belowMinimumTonnage() {
            assertThrows(IllegalArgumentException.class, () ->
                new B5ShipDesign(1, "Too Small", 500)
            );
        }

        @Test
        @DisplayName("Throws on null name")
        void nullName() {
            assertThrows(NullPointerException.class, () ->
                new B5ShipDesign(1, null, 10000)
            );
        }

        @Test
        @DisplayName("Default values after construction")
        void defaults() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertEquals(B5ShipDesign.HullType.STANDARD, ship.getHullType());
            assertEquals(B5ShipDesign.JumpDrive.NONE, ship.getJumpDrive());
            assertEquals(B5ShipDesign.FusionDrive.NONE, ship.getFusionDrive());
            assertEquals(B5ShipDesign.PowerPlant.NONE, ship.getPowerPlant());
            assertEquals(B5ShipDesign.LifeSupport.NONE, ship.getLifeSupport());
            assertEquals(B5ShipDesign.ShipComputer.NONE, ship.getComputer());
            assertEquals(0, ship.getCargoTons());
            assertEquals(0, ship.getPassengerCount());
            assertEquals(B5ShipDesign.PassengerClass.THIRD, ship.getPassengerClass());
            assertTrue(ship.getWeapons().isEmpty());
            assertEquals(0, ship.getFuelTons());
            assertNull(ship.getFaction());
            assertNull(ship.getShipClass());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Set name")
        void setName() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setName("New Name");
            assertEquals("New Name", ship.getName());
        }

        @Test
        @DisplayName("Set name throws on null")
        void setNameNull() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertThrows(NullPointerException.class, () -> ship.setName(null));
        }

        @Test
        @DisplayName("Set hull type")
        void setHullType() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setHullType(B5ShipDesign.HullType.REINFORCED);
            assertEquals(B5ShipDesign.HullType.REINFORCED, ship.getHullType());
        }

        @Test
        @DisplayName("Set jump drive")
        void setJumpDrive() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setJumpDrive(B5ShipDesign.JumpDrive.J3);
            assertEquals(B5ShipDesign.JumpDrive.J3, ship.getJumpDrive());
        }

        @Test
        @DisplayName("Set fusion drive")
        void setFusionDrive() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setFusionDrive(B5ShipDesign.FusionDrive.M4);
            assertEquals(B5ShipDesign.FusionDrive.M4, ship.getFusionDrive());
        }

        @Test
        @DisplayName("Set cargo tons")
        void setCargoTons() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setCargoTons(500);
            assertEquals(500, ship.getCargoTons());
        }

        @Test
        @DisplayName("Set cargo tons throws on negative")
        void setCargoTonsNegative() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertThrows(IllegalArgumentException.class, () -> ship.setCargoTons(-1));
        }

        @Test
        @DisplayName("Set passenger count")
        void setPassengerCount() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setPassengerCount(100);
            assertEquals(100, ship.getPassengerCount());
        }

        @Test
        @DisplayName("Set passenger count throws on negative")
        void setPassengerCountNegative() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertThrows(IllegalArgumentException.class, () -> ship.setPassengerCount(-1));
        }

        @Test
        @DisplayName("Set fuel tons")
        void setFuelTons() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setFuelTons(200);
            assertEquals(200, ship.getFuelTons());
        }
    }

    @Nested
    @DisplayName("Weapons")
    class Weapons {

        @Test
        @DisplayName("Add weapon")
        void addWeapon() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            B5ShipDesign.B5ShipWeapon weapon = new B5ShipDesign.B5ShipWeapon(
                "Laser", "Laser", 2, 10, 2, 1);
            ship.addWeapon(weapon);
            assertEquals(1, ship.getWeapons().size());
        }

        @Test
        @DisplayName("Remove weapon")
        void removeWeapon() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            B5ShipDesign.B5ShipWeapon weapon = new B5ShipDesign.B5ShipWeapon(
                "Laser", "Laser", 2, 10, 2, 1);
            ship.addWeapon(weapon);
            ship.removeWeapon(weapon);
            assertTrue(ship.getWeapons().isEmpty());
        }

        @Test
        @DisplayName("Weapons list is unmodifiable")
        void unmodifiableList() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertThrows(UnsupportedOperationException.class, () ->
                ship.getWeapons().add(null)
            );
        }

        @Test
        @DisplayName("B5ShipWeapon total tons")
        void weaponTotalTons() {
            B5ShipDesign.B5ShipWeapon weapon = new B5ShipDesign.B5ShipWeapon(
                "Laser", "Laser", 3, 10, 2, 1);
            assertEquals(30, weapon.getTotalTons());
        }

        @Test
        @DisplayName("B5ShipWeapon average damage")
        void weaponAverageDamage() {
            B5ShipDesign.B5ShipWeapon weapon = new B5ShipDesign.B5ShipWeapon(
                "Laser", "Laser", 2, 10, 2, 1);
            assertEquals(14.0, weapon.getAverageDamage()); // 2 * 3.5 * 1 * 2
        }

        @Test
        @DisplayName("B5ShipWeapon missile detection")
        void weaponMissile() {
            B5ShipDesign.B5ShipWeapon missile = new B5ShipDesign.B5ShipWeapon(
                "Missile", "Missile", 1, 20, 3, 1);
            assertTrue(missile.isMissile());

            B5ShipDesign.B5ShipWeapon laser = new B5ShipDesign.B5ShipWeapon(
                "Laser", "Laser", 1, 10, 2, 1);
            assertFalse(laser.isMissile());
        }
    }

    @Nested
    @DisplayName("Calculated statistics")
    class CalculatedStatistics {

        @Test
        @DisplayName("Hull points calculation")
        void hullPoints() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setHullType(B5ShipDesign.HullType.STANDARD);
            assertEquals(100, ship.getHullPoints()); // (10000/1000) * 10

            ship.setHullType(B5ShipDesign.HullType.REINFORCED);
            assertEquals(150, ship.getHullPoints()); // (10000/1000) * 15
        }

        @Test
        @DisplayName("Estimated crew calculation")
        void estimatedCrew() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            int crew = ship.getEstimatedCrew();
            assertEquals(100, crew); // 10000/100 + 0 + 0 + 0
        }

        @Test
        @DisplayName("Construction cost is positive")
        void constructionCost() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertTrue(ship.getConstructionCost() > 0);
        }

        @Test
        @DisplayName("Annual maintenance is construction cost / 200")
        void annualMaintenance() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertEquals(ship.getConstructionCost() / 200, ship.getAnnualMaintenanceCost());
        }

        @Test
        @DisplayName("Remaining tons calculation")
        void remainingTons() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setCargoTons(100);
            assertEquals(9900, ship.getRemainingTons());
        }

        @Test
        @DisplayName("Valid design")
        void isValid() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertTrue(ship.isValid());
        }

        @Test
        @DisplayName("Invalid design when exceeding tonnage")
        void isInvalid() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setCargoTons(10001);
            assertFalse(ship.isValid());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Valid design passes validation")
        void validDesign() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            assertDoesNotThrow(() -> ship.validate());
        }

        @Test
        @DisplayName("Throws when exceeding tonnage")
        void exceedsTonnage() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setCargoTons(10001);
            assertThrows(IllegalStateException.class, () -> ship.validate());
        }

        @Test
        @DisplayName("Throws when jump drive without computer")
        void jumpWithoutComputer() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setJumpDrive(B5ShipDesign.JumpDrive.J1);
            assertThrows(IllegalStateException.class, () -> ship.validate());
        }

        @Test
        @DisplayName("Throws when weapons without power plant")
        void weaponsWithoutPower() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            B5ShipDesign.B5ShipWeapon weapon = new B5ShipDesign.B5ShipWeapon(
                "Laser", "Laser", 1, 10, 2, 1);
            ship.addWeapon(weapon);
            assertThrows(IllegalStateException.class, () -> ship.validate());
        }
    }

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("HullType values")
        void hullTypes() {
            assertEquals(4, B5ShipDesign.HullType.values().length);
            assertEquals("Standard", B5ShipDesign.HullType.STANDARD.getDisplayName());
            assertEquals(1.0, B5ShipDesign.HullType.STANDARD.getCostMultiplier());
            assertEquals(3.0, B5ShipDesign.HullType.SHADOW_GRADE.getCostMultiplier());
        }

        @Test
        @DisplayName("JumpDrive values")
        void jumpDrives() {
            assertEquals(7, B5ShipDesign.JumpDrive.values().length);
            assertEquals(0, B5ShipDesign.JumpDrive.NONE.getRating());
            assertEquals(6, B5ShipDesign.JumpDrive.J6.getRating());
        }

        @Test
        @DisplayName("FusionDrive values")
        void fusionDrives() {
            assertEquals(10, B5ShipDesign.FusionDrive.values().length);
            assertEquals(0, B5ShipDesign.FusionDrive.NONE.getRating());
            assertEquals(9, B5ShipDesign.FusionDrive.M9.getRating());
        }

        @Test
        @DisplayName("PowerPlant values")
        void powerPlants() {
            assertEquals(6, B5ShipDesign.PowerPlant.values().length);
        }

        @Test
        @DisplayName("LifeSupport values")
        void lifeSupport() {
            assertEquals(5, B5ShipDesign.LifeSupport.values().length);
        }

        @Test
        @DisplayName("ShipComputer values")
        void shipComputers() {
            assertEquals(8, B5ShipDesign.ShipComputer.values().length);
        }

        @Test
        @DisplayName("PassengerClass values")
        void passengerClasses() {
            assertEquals(4, B5ShipDesign.PassengerClass.values().length);
            assertEquals("First Class", B5ShipDesign.PassengerClass.FIRST.getDisplayName());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes key info")
        void toStringContent() {
            B5ShipDesign ship = new B5ShipDesign(1, "Test Ship", 10000);
            ship.setFaction("Earth Alliance");
            String result = ship.toString();
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("Test Ship"));
            assertTrue(result.contains("Earth Alliance"));
            assertTrue(result.contains("10000 tons"));
        }
    }
}
