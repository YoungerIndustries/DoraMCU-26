package com.yisy.modules.doramcu.shipdesign;

import java.util.List;

/**
 * Comprehensive tests for the ShipTemplate class.
 */
public class ShipTemplateTest {

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

    static void testScoutTemplate() {
        System.out.println("Test: Scout template properties");
        ShipTemplate template = ShipTemplate.scout();

        assertEquals("Scout", template.getName(), "Template name should be Scout");
        assertEquals(Ship.Size.SMALL, template.getSize(), "Scout size should be SMALL");
        assertEquals(10, template.getSpeed(), "Scout speed should be 10");
        assertEquals(1, template.getArmor(), "Scout armor should be 1");
        assertEquals(3, template.getHitPoints(), "Scout HP should be 3");
        assertEquals(5, template.getSensorRange(), "Scout sensors should be 5");
        assertEquals(2, template.getCrewCapacity(), "Scout crew should be 2");
        assertEquals(5, template.getMaxFuel(), "Scout fuel should be 5");
        assertEquals(1, template.getWeapons().size(), "Scout should have 1 weapon");
        assertTrue(template.getDescription().contains("reconnaissance"), "Scout description should mention reconnaissance");
    }

    static void testFighterTemplate() {
        System.out.println("Test: Fighter template properties");
        ShipTemplate template = ShipTemplate.fighter();

        assertEquals("Fighter", template.getName(), "Template name should be Fighter");
        assertEquals(Ship.Size.SMALL, template.getSize(), "Fighter size should be SMALL");
        assertEquals(8, template.getSpeed(), "Fighter speed should be 8");
        assertEquals(2, template.getArmor(), "Fighter armor should be 2");
        assertEquals(4, template.getHitPoints(), "Fighter HP should be 4");
        assertEquals(3, template.getWeapons().size(), "Fighter should have 3 weapons");
    }

    static void testCruiserTemplate() {
        System.out.println("Test: Cruiser template properties");
        ShipTemplate template = ShipTemplate.cruiser();

        assertEquals("Cruiser", template.getName(), "Template name should be Cruiser");
        assertEquals(Ship.Size.LARGE, template.getSize(), "Cruiser size should be LARGE");
        assertEquals(5, template.getSpeed(), "Cruiser speed should be 5");
        assertEquals(4, template.getArmor(), "Cruiser armor should be 4");
        assertEquals(12, template.getHitPoints(), "Cruiser HP should be 12");
        assertEquals(3, template.getWeapons().size(), "Cruiser should have 3 weapons");
    }

    static void testBattleshipTemplate() {
        System.out.println("Test: Battleship template properties");
        ShipTemplate template = ShipTemplate.battleship();

        assertEquals("Battleship", template.getName(), "Template name should be Battleship");
        assertEquals(Ship.Size.HUGE, template.getSize(), "Battleship size should be HUGE");
        assertEquals(3, template.getSpeed(), "Battleship speed should be 3");
        assertEquals(6, template.getArmor(), "Battleship armor should be 6");
        assertEquals(20, template.getHitPoints(), "Battleship HP should be 20");
        assertEquals(4, template.getWeapons().size(), "Battleship should have 4 weapons");
    }

    static void testTransportTemplate() {
        System.out.println("Test: Transport template properties");
        ShipTemplate template = ShipTemplate.transport();

        assertEquals("Transport", template.getName(), "Template name should be Transport");
        assertEquals(Ship.Size.LARGE, template.getSize(), "Transport size should be LARGE");
        assertEquals(25, template.getCrewCapacity(), "Transport crew should be 25");
        assertEquals(20, template.getMaxFuel(), "Transport fuel should be 20");
        assertEquals(1, template.getWeapons().size(), "Transport should have 1 weapon");
    }

    static void testBuildShipFromTemplate() {
        System.out.println("Test: Build ship from template");
        ShipTemplate template = ShipTemplate.scout();
        Ship ship = template.buildShip("Star Hawk");

        assertEquals("Star Hawk", ship.getName(), "Ship name should be Star Hawk");
        assertEquals(Ship.Size.SMALL, ship.getSize(), "Ship size should match template");
        assertEquals(10, ship.getSpeed(), "Ship speed should match template");
        assertEquals(1, ship.getWeapons().size(), "Ship should have template's weapons");
    }

    static void testToBuilder() {
        System.out.println("Test: Convert template to builder");
        ShipTemplate template = ShipTemplate.fighter();
        ShipBuilder builder = template.toBuilder();

        Ship ship = builder.name("Custom Fighter").speed(12).build();

        assertEquals("Custom Fighter", ship.getName(), "Ship name should be custom");
        assertEquals(12, ship.getSpeed(), "Speed should be overridden");
        assertEquals(2, ship.getArmor(), "Armor should come from template");
    }

    static void testCustomTemplate() {
        System.out.println("Test: Create custom template");
        ShipTemplate template = new ShipTemplate.Builder()
                .name("Interceptor")
                .size(Ship.Size.TINY)
                .speed(12)
                .armor(0)
                .hitPoints(1)
                .sensorRange(2)
                .crewCapacity(1)
                .maxFuel(2)
                .addMissile("Micro Missile", 2, 6, 1)
                .description("Fast interceptor with missile armament")
                .build();

        assertEquals("Interceptor", template.getName(), "Custom template name should match");
        assertEquals(Ship.Size.TINY, template.getSize(), "Custom template size should be TINY");
        assertEquals(12, template.getSpeed(), "Custom template speed should be 12");
        assertEquals(1, template.getWeapons().size(), "Custom template should have 1 weapon");
        assertEquals("Fast interceptor with missile armament", template.getDescription(), "Description should match");
    }

    static void testCustomTemplateWithMultipleWeapons() {
        System.out.println("Test: Custom template with multiple weapons");
        ShipTemplate template = new ShipTemplate.Builder()
                .name("Heavy Fighter")
                .size(Ship.Size.MEDIUM)
                .speed(6)
                .armor(2)
                .hitPoints(6)
                .sensorRange(3)
                .crewCapacity(4)
                .maxFuel(6)
                .addBeam("Laser", 2, 5, 1)
                .addMissile("Missile", 3, 8, 2)
                .addTorpedo("Torpedo", 4, 10, 1)
                .build();

        assertEquals(3, template.getWeapons().size(), "Should have 3 weapons");

        List<Ship.Weapon> beams = template.getWeapons();
        assertEquals(Ship.WeaponType.BEAM, beams.get(0).getType(), "First weapon should be beam");
    }

    static void testTemplateWithoutNameThrows() {
        System.out.println("Test: Template without name throws exception");
        assertThrows(IllegalStateException.class, () -> {
            new ShipTemplate.Builder()
                    .size(Ship.Size.SMALL)
                    .build();
        }, "Should throw when name is missing");
    }

    static void testTemplateWithoutSizeThrows() {
        System.out.println("Test: Template without size throws exception");
        assertThrows(IllegalStateException.class, () -> {
            new ShipTemplate.Builder()
                    .name("Test")
                    .build();
        }, "Should throw when size is missing");
    }

    static void testTemplateImmutability() {
        System.out.println("Test: Template weapons list is immutable");
        ShipTemplate template = ShipTemplate.fighter();
        List<Ship.Weapon> weapons1 = template.getWeapons();
        List<Ship.Weapon> weapons2 = template.getWeapons();

        assertFalse(weapons1 == weapons2, "Weapon lists should be different instances");
        assertEquals(weapons1.size(), weapons2.size(), "Weapon lists should have same size");
    }

    static void testTemplateEquals() {
        System.out.println("Test: Template equals and hashCode");
        ShipTemplate t1 = ShipTemplate.scout();
        ShipTemplate t2 = ShipTemplate.scout();
        ShipTemplate t3 = ShipTemplate.fighter();

        assertTrue(t1.equals(t2), "Same template type should be equal");
        assertFalse(t1.equals(t3), "Different template types should not be equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Same template type should have same hash code");
    }

    static void testTemplateToString() {
        System.out.println("Test: Template toString");
        ShipTemplate template = ShipTemplate.scout();
        String str = template.toString();

        assertTrue(str.contains("Scout"), "toString should contain name");
        assertTrue(str.contains("SMALL"), "toString should contain size");
        assertTrue(str.contains("speed=10"), "toString should contain speed");
    }

    static void testBuildMultipleShipsFromTemplate() {
        System.out.println("Test: Build multiple ships from same template");
        ShipTemplate template = ShipTemplate.fighter();

        Ship ship1 = template.buildShip("Alpha");
        Ship ship2 = template.buildShip("Beta");

        assertEquals("Alpha", ship1.getName(), "First ship should be named Alpha");
        assertEquals("Beta", ship2.getName(), "Second ship should be named Beta");
        assertEquals(ship1.getSpeed(), ship2.getSpeed(), "Both ships should have same speed");
        assertEquals(ship1.getWeapons().size(), ship2.getWeapons().size(), "Both ships should have same weapon count");
        assertFalse(ship1 == ship2, "Ships should be different instances");
    }

    // --- Main ---

    public static void main(String[] args) {
        System.out.println("=== ShipTemplate Tests ===\n");

        testScoutTemplate();
        testFighterTemplate();
        testCruiserTemplate();
        testBattleshipTemplate();
        testTransportTemplate();
        testBuildShipFromTemplate();
        testToBuilder();
        testCustomTemplate();
        testCustomTemplateWithMultipleWeapons();
        testTemplateWithoutNameThrows();
        testTemplateWithoutSizeThrows();
        testTemplateImmutability();
        testTemplateEquals();
        testTemplateToString();
        testBuildMultipleShipsFromTemplate();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed == 0) {
            System.out.println("All tests passed!");
        }
    }
}
