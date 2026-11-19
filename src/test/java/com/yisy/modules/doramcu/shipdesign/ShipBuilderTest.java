package com.yisy.modules.doramcu.shipdesign;

import java.util.List;

/**
 * Comprehensive tests for the ShipBuilder class.
 */
public class ShipBuilderTest {

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

    static void assertEquals(String expected, String actual, String message) {
        if (expected == null && actual == null) {
            passed++;
        } else if (expected != null && expected.equals(actual)) {
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

    static void testBuildMinimalShip() {
        System.out.println("Test: Build minimal ship with only size");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.SMALL)
                .build();

        assertEquals(Ship.Size.SMALL, ship.getSize(), "Size should be SMALL");
        assertEquals("Unnamed SMALL", ship.getName(), "Name should default to 'Unnamed SMALL'");
        assertEquals(8, ship.getSpeed(), "Speed should default to 8 for SMALL");
        assertEquals(1, ship.getArmor(), "Armor should default to 1 for SMALL");
        assertEquals(3, ship.getMaxHitPoints(), "HP should default to 3 for SMALL");
        assertEquals(2, ship.getSensorRange(), "Sensors should default to 2 for SMALL");
        assertEquals(3, ship.getCrewCapacity(), "Crew should default to 3 for SMALL");
        assertEquals(3, ship.getMaxFuel(), "Fuel should default to 3 for SMALL");
    }

    static void testBuildTinyShipDefaults() {
        System.out.println("Test: TINY ship defaults");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.TINY)
                .build();

        assertEquals(10, ship.getSpeed(), "TINY speed should be 10");
        assertEquals(0, ship.getArmor(), "TINY armor should be 0");
        assertEquals(1, ship.getMaxHitPoints(), "TINY HP should be 1");
        assertEquals(1, ship.getSensorRange(), "TINY sensors should be 1");
        assertEquals(1, ship.getCrewCapacity(), "TINY crew should be 1");
        assertEquals(1, ship.getMaxFuel(), "TINY fuel should be 1");
    }

    static void testBuildMediumShipDefaults() {
        System.out.println("Test: MEDIUM ship defaults");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.MEDIUM)
                .build();

        assertEquals(6, ship.getSpeed(), "MEDIUM speed should be 6");
        assertEquals(2, ship.getArmor(), "MEDIUM armor should be 2");
        assertEquals(6, ship.getMaxHitPoints(), "MEDIUM HP should be 6");
        assertEquals(3, ship.getSensorRange(), "MEDIUM sensors should be 3");
        assertEquals(6, ship.getCrewCapacity(), "MEDIUM crew should be 6");
        assertEquals(6, ship.getMaxFuel(), "MEDIUM fuel should be 6");
    }

    static void testBuildLargeShipDefaults() {
        System.out.println("Test: LARGE ship defaults");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.LARGE)
                .build();

        assertEquals(5, ship.getSpeed(), "LARGE speed should be 5");
        assertEquals(3, ship.getArmor(), "LARGE armor should be 3");
        assertEquals(10, ship.getMaxHitPoints(), "LARGE HP should be 10");
        assertEquals(4, ship.getSensorRange(), "LARGE sensors should be 4");
        assertEquals(10, ship.getCrewCapacity(), "LARGE crew should be 10");
        assertEquals(10, ship.getMaxFuel(), "LARGE fuel should be 10");
    }

    static void testBuildHugeShipDefaults() {
        System.out.println("Test: HUGE ship defaults");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.HUGE)
                .build();

        assertEquals(4, ship.getSpeed(), "HUGE speed should be 4");
        assertEquals(4, ship.getArmor(), "HUGE armor should be 4");
        assertEquals(15, ship.getMaxHitPoints(), "HUGE HP should be 15");
        assertEquals(5, ship.getSensorRange(), "HUGE sensors should be 5");
        assertEquals(15, ship.getCrewCapacity(), "HUGE crew should be 15");
        assertEquals(15, ship.getMaxFuel(), "HUGE fuel should be 15");
    }

    static void testBuildGiantShipDefaults() {
        System.out.println("Test: GIANT ship defaults");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.GIANT)
                .build();

        assertEquals(3, ship.getSpeed(), "GIANT speed should be 3");
        assertEquals(5, ship.getArmor(), "GIANT armor should be 5");
        assertEquals(21, ship.getMaxHitPoints(), "GIANT HP should be 21");
        assertEquals(6, ship.getSensorRange(), "GIANT sensors should be 6");
        assertEquals(21, ship.getCrewCapacity(), "GIANT crew should be 21");
        assertEquals(21, ship.getMaxFuel(), "GIANT fuel should be 21");
    }

    static void testOverrideDefaults() {
        System.out.println("Test: Override default values");
        Ship ship = new ShipBuilder()
                .name("Custom Ship")
                .size(Ship.Size.MEDIUM)
                .speed(12)
                .armor(5)
                .hitPoints(20)
                .sensorRange(10)
                .crewCapacity(30)
                .maxFuel(50)
                .build();

        assertEquals("Custom Ship", ship.getName(), "Name should be custom");
        assertEquals(12, ship.getSpeed(), "Speed should be overridden to 12");
        assertEquals(5, ship.getArmor(), "Armor should be overridden to 5");
        assertEquals(20, ship.getMaxHitPoints(), "HP should be overridden to 20");
        assertEquals(10, ship.getSensorRange(), "Sensors should be overridden to 10");
        assertEquals(30, ship.getCrewCapacity(), "Crew should be overridden to 30");
        assertEquals(50, ship.getMaxFuel(), "Fuel should be overridden to 50");
    }

    static void testAddBeamWeapon() {
        System.out.println("Test: Add beam weapon via builder");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.SMALL)
                .addBeam("Laser", 2, 4, 1)
                .build();

        List<Ship.Weapon> beams = ship.getWeaponsByType(Ship.WeaponType.BEAM);
        assertEquals(1, beams.size(), "Should have 1 beam weapon");
        assertEquals("Laser", beams.get(0).getName(), "Beam name should be Laser");
        assertEquals(2, beams.get(0).getStrength(), "Beam strength should be 2");
        assertEquals(4, beams.get(0).getRange(), "Beam range should be 4");
        assertEquals(1, beams.get(0).getArcs(), "Beam arcs should be 1");
    }

    static void testAddMissileWeapon() {
        System.out.println("Test: Add missile weapon via builder");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.SMALL)
                .addMissile("Rocket", 3, 8, 2)
                .build();

        List<Ship.Weapon> missiles = ship.getWeaponsByType(Ship.WeaponType.MISSILE);
        assertEquals(1, missiles.size(), "Should have 1 missile weapon");
        assertEquals("Rocket", missiles.get(0).getName(), "Missile name should be Rocket");
    }

    static void testAddTorpedoWeapon() {
        System.out.println("Test: Add torpedo weapon via builder");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.SMALL)
                .addTorpedo("Photon", 5, 10, 1)
                .build();

        List<Ship.Weapon> torpedoes = ship.getWeaponsByType(Ship.WeaponType.TORPEDO);
        assertEquals(1, torpedoes.size(), "Should have 1 torpedo weapon");
        assertEquals("Photon", torpedoes.get(0).getName(), "Torpedo name should be Photon");
    }

    static void testAddMultipleWeapons() {
        System.out.println("Test: Add multiple weapons of different types");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.MEDIUM)
                .addBeam("Main Laser", 3, 6, 1)
                .addBeam("Side Laser", 2, 4, 2)
                .addMissile("Rocket Pod", 4, 8, 1)
                .addTorpedo("Photon Torpedo", 6, 12, 1)
                .build();

        assertEquals(4, ship.getWeapons().size(), "Should have 4 total weapons");
        assertEquals(2, ship.getWeaponsByType(Ship.WeaponType.BEAM).size(), "Should have 2 beams");
        assertEquals(1, ship.getWeaponsByType(Ship.WeaponType.MISSILE).size(), "Should have 1 missile");
        assertEquals(1, ship.getWeaponsByType(Ship.WeaponType.TORPEDO).size(), "Should have 1 torpedo");
        assertEquals(5, ship.getTotalBeamStrength(), "Total beam strength should be 5");
    }

    static void testBuildWithoutSizeThrows() {
        System.out.println("Test: Build without size throws exception");
        assertThrows(IllegalStateException.class, () -> {
            new ShipBuilder().build();
        }, "Should throw IllegalStateException when size is not set");
    }

    static void testResetBuilder() {
        System.out.println("Test: Reset builder clears all settings");
        ShipBuilder builder = new ShipBuilder();
        builder.name("First").size(Ship.Size.SMALL).addBeam("Laser", 1, 2, 1);

        builder.reset();
        assertThrows(IllegalStateException.class, () -> {
            builder.build();
        }, "Reset builder should require size again");
    }

    static void testReuseBuilderAfterReset() {
        System.out.println("Test: Reuse builder after reset");
        ShipBuilder builder = new ShipBuilder();

        Ship ship1 = builder.name("First").size(Ship.Size.SMALL).build();
        assertEquals("First", ship1.getName(), "First ship name should be First");

        builder.reset();
        Ship ship2 = builder.name("Second").size(Ship.Size.LARGE).build();
        assertEquals("Second", ship2.getName(), "Second ship name should be Second");
        assertEquals(Ship.Size.LARGE, ship2.getSize(), "Second ship size should be LARGE");
    }

    static void testShipStartsFullCrewAndFuel() {
        System.out.println("Test: Built ship starts with full crew and fuel");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.MEDIUM)
                .build();

        assertEquals(6, ship.getCurrentCrew(), "Current crew should equal capacity");
        assertEquals(6, ship.getFuel(), "Current fuel should equal max fuel");
        assertFalse(ship.isOutOfFuel(), "Ship should not be out of fuel");
    }

    static void testPartialOverride() {
        System.out.println("Test: Partial override keeps some defaults");
        Ship ship = new ShipBuilder()
                .size(Ship.Size.MEDIUM)
                .speed(15)
                .build();

        assertEquals(15, ship.getSpeed(), "Speed should be overridden to 15");
        assertEquals(2, ship.getArmor(), "Armor should keep default 2");
        assertEquals(6, ship.getMaxHitPoints(), "HP should keep default 6");
    }

    static void testFluentChaining() {
        System.out.println("Test: Fluent chaining returns builder reference");
        ShipBuilder builder = new ShipBuilder();
        ShipBuilder result = builder.name("Test").size(Ship.Size.TINY).speed(5).armor(1);
        assertEquals(builder, result, "Chaining should return same builder instance");
    }

    static void testFullShipBuild() {
        System.out.println("Test: Full ship build with all options");
        Ship ship = new ShipBuilder()
                .name("Star Explorer")
                .size(Ship.Size.LARGE)
                .speed(8)
                .armor(5)
                .hitPoints(25)
                .sensorRange(8)
                .crewCapacity(20)
                .maxFuel(30)
                .addBeam("Forward Laser", 4, 8, 1)
                .addBeam("Aft Laser", 3, 6, 1)
                .addMissile("Missile Bank", 5, 12, 2)
                .addTorpedo("Quantum Torpedo", 8, 15, 1)
                .build();

        assertEquals("Star Explorer", ship.getName(), "Name should match");
        assertEquals(Ship.Size.LARGE, ship.getSize(), "Size should be LARGE");
        assertEquals(8, ship.getSpeed(), "Speed should be 8");
        assertEquals(5, ship.getArmor(), "Armor should be 5");
        assertEquals(25, ship.getMaxHitPoints(), "HP should be 25");
        assertEquals(8, ship.getSensorRange(), "Sensors should be 8");
        assertEquals(20, ship.getCrewCapacity(), "Crew should be 20");
        assertEquals(30, ship.getMaxFuel(), "Fuel should be 30");
        assertEquals(4, ship.getWeapons().size(), "Should have 4 weapons");
        assertEquals(7, ship.getTotalBeamStrength(), "Total beam strength should be 7");
        assertFalse(ship.isDestroyed(), "Ship should not be destroyed");
        assertFalse(ship.isDamaged(), "Ship should not be damaged");
    }

    // --- Main runner ---

    public static void main(String[] args) {
        System.out.println("=== ShipBuilder Tests ===\n");

        testBuildMinimalShip();
        testBuildTinyShipDefaults();
        testBuildMediumShipDefaults();
        testBuildLargeShipDefaults();
        testBuildHugeShipDefaults();
        testBuildGiantShipDefaults();
        testOverrideDefaults();
        testAddBeamWeapon();
        testAddMissileWeapon();
        testAddTorpedoWeapon();
        testAddMultipleWeapons();
        testBuildWithoutSizeThrows();
        testResetBuilder();
        testReuseBuilderAfterReset();
        testShipStartsFullCrewAndFuel();
        testPartialOverride();
        testFluentChaining();
        testFullShipBuild();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed == 0) {
            System.out.println("All tests passed!");
        }
    }
}
