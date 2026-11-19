package com.yisy.modules.doramcu.shipdesign;

import java.util.List;

/**
 * Comprehensive tests for the DesignPoints class.
 */
public class DesignPointsTest {

    static int passed = 0;
    static int failed = 0;

    // --- Assertion helpers ---

    static void assertEquals(int expected, int actual, String message) {
        if (expected == actual) {
            passed++;
        } else {
            failed++;
            System.err.println("  FAIL: " + message + " - expected: " + expected + ", actual: " + actual);
        }
    }

    static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            passed++;
        } else if (expected != null && expected.equals(actual)) {
            passed++;
        } else {
            failed++;
            System.err.println("  FAIL: " + message + " - expected: " + expected + ", actual: " + actual);
        }
    }

    static void assertTrue(boolean condition, String message) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.err.println("  FAIL: " + message);
        }
    }

    static void assertFalse(boolean condition, String message) {
        if (!condition) {
            passed++;
        } else {
            failed++;
            System.err.println("  FAIL: " + message);
        }
    }

    static void assertThrows(Class<? extends Throwable> expectedType, Runnable action, String message) {
        try {
            action.run();
            failed++;
            System.err.println("  FAIL: " + message + " - no exception thrown");
        } catch (Throwable e) {
            if (expectedType.isInstance(e)) {
                passed++;
            } else {
                failed++;
                System.err.println("  FAIL: " + message + " - expected " + expectedType.getSimpleName() + ", got " + e.getClass().getSimpleName());
            }
        }
    }

    // --- Test methods ---

    static void testPointsBySize() {
        System.out.println("Test: Design points budget by size");
        assertEquals(15, DesignPoints.POINTS_BY_SIZE[Ship.Size.TINY.ordinal()], "TINY should have 15 points");
        assertEquals(30, DesignPoints.POINTS_BY_SIZE[Ship.Size.SMALL.ordinal()], "SMALL should have 30 points");
        assertEquals(50, DesignPoints.POINTS_BY_SIZE[Ship.Size.MEDIUM.ordinal()], "MEDIUM should have 50 points");
        assertEquals(80, DesignPoints.POINTS_BY_SIZE[Ship.Size.LARGE.ordinal()], "LARGE should have 80 points");
        assertEquals(120, DesignPoints.POINTS_BY_SIZE[Ship.Size.HUGE.ordinal()], "HUGE should have 120 points");
        assertEquals(160, DesignPoints.POINTS_BY_SIZE[Ship.Size.GIANT.ordinal()], "GIANT should have 160 points");
    }

    static void testDesignPointsConstructor() {
        System.out.println("Test: DesignPoints constructor sets size and max points");
        DesignPoints dp = new DesignPoints(Ship.Size.MEDIUM);

        assertEquals(Ship.Size.MEDIUM, dp.getSize(), "Size should be MEDIUM");
        assertEquals(50, dp.getMaxPoints(), "Max points should be 50 for MEDIUM");
        assertEquals(0, dp.getTotalPoints(), "Total points should start at 0");
        assertEquals(50, dp.getRemainingPoints(), "Remaining points should equal max");
    }

    static void testSpeedPoints() {
        System.out.println("Test: Speed point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        assertEquals(0, dp.calculateSpeedPoints(3), "Speed 3 should cost 0 points");
        assertEquals(0, dp.calculateSpeedPoints(2), "Speed 2 should cost 0 points");
        dp.reset();
        assertEquals(5, dp.calculateSpeedPoints(8), "Speed 8 should cost 5 points");
        dp.reset();
        assertEquals(10, dp.calculateSpeedPoints(13), "Speed 13 should cost 10 points");
    }

    static void testArmorPoints() {
        System.out.println("Test: Armor point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        assertEquals(0, dp.calculateArmorPoints(0), "Armor 0 should cost 0 points");
        assertEquals(2, dp.calculateArmorPoints(1), "Armor 1 should cost 2 points");
        assertEquals(6, dp.calculateArmorPoints(3), "Armor 3 should cost 6 points");
        assertEquals(10, dp.calculateArmorPoints(5), "Armor 5 should cost 10 points");
    }

    static void testHitPointsPoints() {
        System.out.println("Test: Hit points point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        assertEquals(0, dp.calculateHitPointsPoints(1), "HP 1 should cost 0 points");
        assertEquals(1, dp.calculateHitPointsPoints(2), "HP 2 should cost 1 point");
        assertEquals(3, dp.calculateHitPointsPoints(6), "HP 6 should cost 3 points");
        assertEquals(5, dp.calculateHitPointsPoints(10), "HP 10 should cost 5 points");
    }

    static void testSensorPoints() {
        System.out.println("Test: Sensor point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        assertEquals(0, dp.calculateSensorPoints(1), "Sensors 1 should cost 0 points");
        assertEquals(0, dp.calculateSensorPoints(0), "Sensors 0 should cost 0 points");
        dp.reset();
        assertEquals(2, dp.calculateSensorPoints(3), "Sensors 3 should cost 2 points");
        dp.reset();
        assertEquals(5, dp.calculateSensorPoints(6), "Sensors 6 should cost 5 points");
    }

    static void testCrewPoints() {
        System.out.println("Test: Crew point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        assertEquals(0, dp.calculateCrewPoints(4), "Crew 4 should cost 0 points");
        assertEquals(1, dp.calculateCrewPoints(5), "Crew 5 should cost 1 point");
        assertEquals(2, dp.calculateCrewPoints(10), "Crew 10 should cost 2 points");
        assertEquals(5, dp.calculateCrewPoints(25), "Crew 25 should cost 5 points");
    }

    static void testFuelPoints() {
        System.out.println("Test: Fuel point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        assertEquals(0, dp.calculateFuelPoints(2), "Fuel 2 should cost 0 points");
        assertEquals(1, dp.calculateFuelPoints(3), "Fuel 3 should cost 1 point");
        assertEquals(3, dp.calculateFuelPoints(9), "Fuel 9 should cost 3 points");
        assertEquals(6, dp.calculateFuelPoints(18), "Fuel 18 should cost 6 points");
    }

    static void testBeamWeaponPoints() {
        System.out.println("Test: Beam weapon point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        Ship.Weapon beam = new Ship.Weapon(Ship.WeaponType.BEAM, "Laser", 3, 5, 1);
        assertEquals(6, dp.calculateWeaponPoints(beam), "Beam strength 3 should cost 6 points");
    }

    static void testMissileWeaponPoints() {
        System.out.println("Test: Missile weapon point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        Ship.Weapon missile = new Ship.Weapon(Ship.WeaponType.MISSILE, "Rocket", 3, 8, 2);
        assertEquals(9, dp.calculateWeaponPoints(missile), "Missile strength 3 should cost 9 points");
    }

    static void testTorpedoWeaponPoints() {
        System.out.println("Test: Torpedo weapon point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        Ship.Weapon torpedo = new Ship.Weapon(Ship.WeaponType.TORPEDO, "Torpedo", 3, 10, 1);
        assertEquals(12, dp.calculateWeaponPoints(torpedo), "Torpedo strength 3 should cost 12 points");
    }

    static void testMultipleWeaponPoints() {
        System.out.println("Test: Multiple weapon point calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.LARGE);

        List<Ship.Weapon> weapons = List.of(
                new Ship.Weapon(Ship.WeaponType.BEAM, "Laser", 2, 5, 1),
                new Ship.Weapon(Ship.WeaponType.MISSILE, "Missile", 3, 8, 2)
        );

        assertEquals(13, dp.calculateWeaponPoints(weapons), "Two weapons should cost 13 points total");
    }

    static void testTotalPoints() {
        System.out.println("Test: Total points calculation");
        DesignPoints dp = new DesignPoints(Ship.Size.LARGE);

        dp.calculateSpeedPoints(8);
        dp.calculateArmorPoints(3);
        dp.calculateHitPointsPoints(10);
        dp.calculateSensorPoints(4);
        dp.calculateCrewPoints(10);
        dp.calculateFuelPoints(9);

        int expected = 5 + 6 + 5 + 3 + 2 + 3; // 24
        assertEquals(expected, dp.getTotalPoints(), "Total points should be sum of all components");
        assertEquals(80 - expected, dp.getRemainingPoints(), "Remaining should be max minus total");
    }

    static void testIndividualPointGetters() {
        System.out.println("Test: Individual point getters");
        DesignPoints dp = new DesignPoints(Ship.Size.MEDIUM);

        dp.calculateSpeedPoints(8);
        dp.calculateArmorPoints(2);
        dp.calculateHitPointsPoints(6);

        assertEquals(5, dp.getSpeedPoints(), "Speed points should be 5");
        assertEquals(4, dp.getArmorPoints(), "Armor points should be 4");
        assertEquals(3, dp.getHitPointsPoints(), "HP points should be 3");
        assertEquals(0, dp.getSensorPoints(), "Sensor points should be 0");
    }

    static void testValidateWithinBudget() {
        System.out.println("Test: Validation within budget");
        DesignPoints dp = new DesignPoints(Ship.Size.LARGE);

        dp.calculateSpeedPoints(5);
        dp.calculateArmorPoints(3);
        dp.calculateHitPointsPoints(10);

        assertTrue(dp.validate(), "Design within budget should be valid");
    }

    static void testValidateOverBudget() {
        System.out.println("Test: Validation over budget");
        DesignPoints dp = new DesignPoints(Ship.Size.TINY);

        dp.calculateSpeedPoints(10);
        dp.calculateArmorPoints(5);
        dp.calculateHitPointsPoints(10);

        assertFalse(dp.validate(), "Design over budget should be invalid");
        assertEquals(1, dp.getWarnings().size(), "Should have 1 warning");
        assertTrue(dp.getWarnings().get(0).contains("exceeds"), "Warning should mention exceeding budget");
    }

    static void testValidateNearBudget() {
        System.out.println("Test: Validation near budget limit");
        DesignPoints dp = new DesignPoints(Ship.Size.TINY);

        // TINY has 15 points, 90% = 13.5
        dp.calculateSpeedPoints(8);  // 5
        dp.calculateArmorPoints(4);  // 8
        // Total = 13, which is 86.7%
        dp.calculateHitPointsPoints(2); // 1, total = 14, which is 93.3%

        boolean valid = dp.validate();
        assertTrue(valid, "Design at 93% should still be valid");
        assertTrue(dp.getWarnings().size() > 0, "Should have near-budget warning");
    }

    static void testValidateNoWeaponsWarning() {
        System.out.println("Test: Validation warns about no weapons");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        dp.calculateSpeedPoints(5);
        dp.calculateArmorPoints(1);

        boolean valid = dp.validate();
        assertTrue(valid, "Design should be valid");
        boolean hasNoWeaponWarning = false;
        for (String warning : dp.getWarnings()) {
            if (warning.contains("no weapons")) {
                hasNoWeaponWarning = true;
                break;
            }
        }
        assertTrue(hasNoWeaponWarning, "Should warn about no weapons");
    }

    static void testValidateMinimalSpeedWarning() {
        System.out.println("Test: Validation warns about minimal speed");
        DesignPoints dp = new DesignPoints(Ship.Size.SMALL);

        dp.calculateSpeedPoints(2); // Speed 2, costs 0 points
        dp.calculateArmorPoints(1);

        boolean valid = dp.validate();
        assertTrue(valid, "Design should be valid");
        boolean hasSpeedWarning = false;
        for (String warning : dp.getWarnings()) {
            if (warning.contains("minimal speed")) {
                hasSpeedWarning = true;
                break;
            }
        }
        assertTrue(hasSpeedWarning, "Should warn about minimal speed");
    }

    static void testReset() {
        System.out.println("Test: Reset clears all points");
        DesignPoints dp = new DesignPoints(Ship.Size.MEDIUM);

        dp.calculateSpeedPoints(10);
        dp.calculateArmorPoints(5);
        dp.calculateHitPointsPoints(10);

        dp.reset();

        assertEquals(0, dp.getTotalPoints(), "Total should be 0 after reset");
        assertEquals(0, dp.getSpeedPoints(), "Speed points should be 0");
        assertEquals(0, dp.getArmorPoints(), "Armor points should be 0");
        assertEquals(50, dp.getRemainingPoints(), "Remaining should equal max after reset");
    }

    static void testEvaluateShip() {
        System.out.println("Test: Evaluate complete ship");
        Ship ship = new ShipBuilder()
                .name("Test Ship")
                .size(Ship.Size.MEDIUM)
                .speed(6)
                .armor(2)
                .hitPoints(6)
                .sensorRange(3)
                .crewCapacity(6)
                .maxFuel(6)
                .addBeam("Laser", 2, 5, 1)
                .build();

        DesignPoints dp = DesignPoints.evaluate(ship);

        assertEquals(Ship.Size.MEDIUM, dp.getSize(), "Evaluated size should match ship");
        assertEquals(3, dp.getSpeedPoints(), "Speed 6 should cost 3 points");
        assertEquals(4, dp.getArmorPoints(), "Armor 2 should cost 4 points");
        assertEquals(3, dp.getHitPointsPoints(), "HP 6 should cost 3 points");
        assertEquals(2, dp.getSensorPoints(), "Sensors 3 should cost 2 points");
        assertEquals(1, dp.getCrewPoints(), "Crew 6 should cost 1 point");
        assertEquals(2, dp.getFuelPoints(), "Fuel 6 should cost 2 points");
        assertEquals(4, dp.getWeaponPoints(), "Beam strength 2 should cost 4 points");
    }

    static void testEvaluateBuilder() {
        System.out.println("Test: Evaluate ShipBuilder");
        ShipBuilder builder = new ShipBuilder()
                .name("Builder Ship")
                .size(Ship.Size.SMALL)
                .speed(8)
                .armor(1)
                .hitPoints(3)
                .sensorRange(2)
                .crewCapacity(3)
                .maxFuel(3)
                .addMissile("Missile", 2, 6, 1);

        DesignPoints dp = DesignPoints.evaluate(builder);

        assertEquals(Ship.Size.SMALL, dp.getSize(), "Evaluated size should match builder");
        assertTrue(dp.getTotalPoints() > 0, "Total points should be greater than 0");
    }

    static void testToString() {
        System.out.println("Test: DesignPoints toString");
        DesignPoints dp = new DesignPoints(Ship.Size.MEDIUM);
        dp.calculateSpeedPoints(6);

        String str = dp.toString();
        assertTrue(str.contains("MEDIUM"), "toString should contain size");
        assertTrue(str.contains("total="), "toString should contain total");
        assertTrue(str.contains("remaining="), "toString should contain remaining");
    }

    static void testTemplateDesignValidation() {
        System.out.println("Test: Validate template-based ship design");
        ShipTemplate template = ShipTemplate.scout();
        Ship ship = template.buildShip("Test Scout");

        DesignPoints dp = DesignPoints.evaluate(ship);
        boolean valid = dp.validate();

        // Scout template: speed=10(7pts), armor=1(2pts), hp=3(1pt), sensors=5(4pts), crew=2(0pts), fuel=5(1pt), weapon=1 beam str 1(2pts)
        // Total = 17, SMALL budget = 30
        assertTrue(valid, "Scout template should be within budget");
        assertEquals(30, dp.getMaxPoints(), "Max points should be 30 for SMALL");
    }

    // --- Main ---

    public static void main(String[] args) {
        System.out.println("=== DesignPoints Tests ===\n");

        testPointsBySize();
        testDesignPointsConstructor();
        testSpeedPoints();
        testArmorPoints();
        testHitPointsPoints();
        testSensorPoints();
        testCrewPoints();
        testFuelPoints();
        testBeamWeaponPoints();
        testMissileWeaponPoints();
        testTorpedoWeaponPoints();
        testMultipleWeaponPoints();
        testTotalPoints();
        testIndividualPointGetters();
        testValidateWithinBudget();
        testValidateOverBudget();
        testValidateNearBudget();
        testValidateNoWeaponsWarning();
        testValidateMinimalSpeedWarning();
        testReset();
        testEvaluateShip();
        testEvaluateBuilder();
        testToString();
        testTemplateDesignValidation();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed == 0) {
            System.out.println("All tests passed!");
        }
    }
}
