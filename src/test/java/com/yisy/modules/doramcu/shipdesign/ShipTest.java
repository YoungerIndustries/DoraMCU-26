package com.yisy.modules.doramcu.shipdesign;

import java.util.List;

/**
 * Comprehensive tests for the Ship class.
 */
public class ShipTest {

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

    // --- Helper to create a test ship ---

    static Ship createTestShip() {
        return new Ship("TestShip", Ship.Size.MEDIUM, 6, 3, 10, 8, 50, 100);
    }

    // --- Test methods ---

    static void testShipCreation() {
        System.out.println("Test: Ship creation with basic attributes");
        Ship ship = createTestShip();
        assertEquals("TestShip", ship.getName(), "Name should be TestShip");
        assertEquals(Ship.Size.MEDIUM, ship.getSize(), "Size should be MEDIUM");
        assertEquals(6, ship.getSpeed(), "Speed should be 6");
        assertEquals(3, ship.getArmor(), "Armor should be 3");
        assertEquals(10, ship.getHitPoints(), "Hit points should be 10");
        assertEquals(10, ship.getMaxHitPoints(), "Max hit points should be 10");
        assertEquals(8, ship.getSensorRange(), "Sensor range should be 8");
        assertEquals(50, ship.getCrewCapacity(), "Crew capacity should be 50");
        assertEquals(50, ship.getCurrentCrew(), "Current crew should start at capacity");
        assertEquals(100, ship.getMaxFuel(), "Max fuel should be 100");
        assertEquals(100, ship.getFuel(), "Fuel should start at max");
    }

    static void testShipSizeHexes() {
        System.out.println("Test: Ship size hex footprint");
        assertEquals(1, Ship.Size.TINY.getHexes(), "TINY should occupy 1 hex");
        assertEquals(2, Ship.Size.SMALL.getHexes(), "SMALL should occupy 2 hexes");
        assertEquals(3, Ship.Size.MEDIUM.getHexes(), "MEDIUM should occupy 3 hexes");
        assertEquals(4, Ship.Size.LARGE.getHexes(), "LARGE should occupy 4 hexes");
        assertEquals(5, Ship.Size.HUGE.getHexes(), "HUGE should occupy 5 hexes");
        assertEquals(6, Ship.Size.GIANT.getHexes(), "GIANT should occupy 6 hexes");

        Ship tiny = new Ship("Tiny", Ship.Size.TINY, 10, 1, 5, 5, 10, 50);
        assertEquals(1, tiny.getHexSize(), "TINY ship hex size should be 1");

        Ship giant = new Ship("Giant", Ship.Size.GIANT, 2, 6, 30, 12, 200, 300);
        assertEquals(6, giant.getHexSize(), "GIANT ship hex size should be 6");
    }

    static void testSetName() {
        System.out.println("Test: Setting ship name");
        Ship ship = createTestShip();
        ship.setName("RenamedShip");
        assertEquals("RenamedShip", ship.getName(), "Name should be updated");
    }

    static void testDamageWithArmor() {
        System.out.println("Test: Damage reduced by armor");
        Ship ship = createTestShip(); // armor=3, hp=10
        int taken = ship.takeDamage(5);
        assertEquals(2, taken, "Damage taken should be 5-3=2");
        assertEquals(8, ship.getHitPoints(), "Hit points should be 8");
    }

    static void testDamageLessThanArmor() {
        System.out.println("Test: Damage less than armor is fully absorbed");
        Ship ship = createTestShip(); // armor=3, hp=10
        int taken = ship.takeDamage(2);
        assertEquals(0, taken, "Damage taken should be 0");
        assertEquals(10, ship.getHitPoints(), "Hit points should remain 10");
    }

    static void testDamageEqualToArmor() {
        System.out.println("Test: Damage equal to armor is fully absorbed");
        Ship ship = createTestShip(); // armor=3, hp=10
        int taken = ship.takeDamage(3);
        assertEquals(0, taken, "Damage taken should be 0");
        assertEquals(10, ship.getHitPoints(), "Hit points should remain 10");
    }

    static void testDestroyShip() {
        System.out.println("Test: Ship destroyed when hit points reach 0");
        Ship ship = createTestShip(); // armor=3, hp=10
        ship.takeDamage(13); // 13-3=10 damage
        assertEquals(0, ship.getHitPoints(), "Hit points should be 0");
        assertTrue(ship.isDestroyed(), "Ship should be destroyed");
    }

    static void testDamageDoesNotGoBelowZero() {
        System.out.println("Test: Hit points do not go below 0");
        Ship ship = createTestShip(); // armor=3, hp=10
        ship.takeDamage(20); // 20-3=17 damage, but hp floors at 0
        assertEquals(0, ship.getHitPoints(), "Hit points should floor at 0");
        assertTrue(ship.isDestroyed(), "Ship should be destroyed");
    }

    static void testIsDamaged() {
        System.out.println("Test: isDamaged returns correct state");
        Ship ship = createTestShip();
        assertFalse(ship.isDamaged(), "New ship should not be damaged");
        ship.takeDamage(4); // 4-3=1 damage
        assertTrue(ship.isDamaged(), "Ship should be damaged after taking damage");
    }

    static void testRepair() {
        System.out.println("Test: Repair restores hit points");
        Ship ship = createTestShip(); // hp=10
        ship.takeDamage(5); // 5-3=2 damage, hp=8
        int repaired = ship.repair(3); // can only repair 2
        assertEquals(2, repaired, "Should repair 2 points");
        assertEquals(10, ship.getHitPoints(), "Hit points should be fully restored");
    }

    static void testRepairExceedsMax() {
        System.out.println("Test: Repair does not exceed max hit points");
        Ship ship = createTestShip(); // hp=10
        ship.takeDamage(5); // hp=8
        int repaired = ship.repair(5); // can only repair 2
        assertEquals(2, repaired, "Should repair only 2 points");
        assertEquals(10, ship.getHitPoints(), "Hit points should not exceed max");
    }

    static void testAddCrew() {
        System.out.println("Test: Add crew to ship");
        Ship ship = createTestShip(); // capacity=50, current=50
        ship.removeCrew(10); // current=40
        int added = ship.addCrew(5);
        assertEquals(5, added, "Should add 5 crew");
        assertEquals(45, ship.getCurrentCrew(), "Current crew should be 45");
    }

    static void testAddCrewExceedsCapacity() {
        System.out.println("Test: Add crew does not exceed capacity");
        Ship ship = createTestShip(); // capacity=50, current=50
        ship.removeCrew(10); // current=40
        int added = ship.addCrew(20); // can only add 10
        assertEquals(10, added, "Should add only 10 crew");
        assertEquals(50, ship.getCurrentCrew(), "Current crew should be at capacity");
    }

    static void testRemoveCrew() {
        System.out.println("Test: Remove crew from ship");
        Ship ship = createTestShip(); // current=50
        int removed = ship.removeCrew(15);
        assertEquals(15, removed, "Should remove 15 crew");
        assertEquals(35, ship.getCurrentCrew(), "Current crew should be 35");
    }

    static void testRemoveCrewExceedsCurrent() {
        System.out.println("Test: Remove crew does not go below 0");
        Ship ship = createTestShip(); // current=50
        int removed = ship.removeCrew(100); // can only remove 50
        assertEquals(50, removed, "Should remove only 50 crew");
        assertEquals(0, ship.getCurrentCrew(), "Current crew should be 0");
    }

    static void testAddWeapon() {
        System.out.println("Test: Add weapon to ship");
        Ship ship = createTestShip();
        Ship.Weapon beam = new Ship.Weapon(Ship.WeaponType.BEAM, "Laser", 4, 6, 1);
        ship.addWeapon(beam);
        List<Ship.Weapon> weapons = ship.getWeapons();
        assertEquals(1, weapons.size(), "Should have 1 weapon");
        assertEquals("Laser", weapons.get(0).getName(), "Weapon name should be Laser");
    }

    static void testGetWeaponsByType() {
        System.out.println("Test: Filter weapons by type");
        Ship ship = createTestShip();
        ship.addWeapon(new Ship.Weapon(Ship.WeaponType.BEAM, "Laser", 4, 6, 1));
        ship.addWeapon(new Ship.Weapon(Ship.WeaponType.MISSILE, "Missile Rack", 3, 10, 1));
        ship.addWeapon(new Ship.Weapon(Ship.WeaponType.BEAM, "Laser 2", 4, 6, 1));

        List<Ship.Weapon> beams = ship.getWeaponsByType(Ship.WeaponType.BEAM);
        assertEquals(2, beams.size(), "Should have 2 beam weapons");

        List<Ship.Weapon> missiles = ship.getWeaponsByType(Ship.WeaponType.MISSILE);
        assertEquals(1, missiles.size(), "Should have 1 missile weapon");

        List<Ship.Weapon> torpedoes = ship.getWeaponsByType(Ship.WeaponType.TORPEDO);
        assertEquals(0, torpedoes.size(), "Should have 0 torpedo weapons");
    }

    static void testTotalBeamStrength() {
        System.out.println("Test: Total beam strength calculation");
        Ship ship = createTestShip();
        ship.addWeapon(new Ship.Weapon(Ship.WeaponType.BEAM, "Laser 1", 4, 6, 1));
        ship.addWeapon(new Ship.Weapon(Ship.WeaponType.BEAM, "Laser 2", 6, 8, 1));
        ship.addWeapon(new Ship.Weapon(Ship.WeaponType.MISSILE, "Missile", 3, 10, 1));
        assertEquals(10, ship.getTotalBeamStrength(), "Total beam strength should be 10");
    }

    static void testConsumeFuel() {
        System.out.println("Test: Consume fuel");
        Ship ship = createTestShip(); // fuel=100
        int consumed = ship.consumeFuel(30);
        assertEquals(30, consumed, "Should consume 30 fuel");
        assertEquals(70, ship.getFuel(), "Fuel should be 70");
    }

    static void testConsumeFuelExceedsAvailable() {
        System.out.println("Test: Consume fuel does not go below 0");
        Ship ship = createTestShip(); // fuel=100
        int consumed = ship.consumeFuel(150);
        assertEquals(100, consumed, "Should consume only 100 fuel");
        assertEquals(0, ship.getFuel(), "Fuel should be 0");
        assertTrue(ship.isOutOfFuel(), "Ship should be out of fuel");
    }

    static void testRefuel() {
        System.out.println("Test: Refuel ship");
        Ship ship = createTestShip(); // fuel=100, max=100
        ship.consumeFuel(40); // fuel=60
        int added = ship.refuel(30);
        assertEquals(30, added, "Should add 30 fuel");
        assertEquals(90, ship.getFuel(), "Fuel should be 90");
    }

    static void testRefuelExceedsMax() {
        System.out.println("Test: Refuel does not exceed max fuel");
        Ship ship = createTestShip(); // fuel=100, max=100
        ship.consumeFuel(40); // fuel=60
        int added = ship.refuel(50); // can only add 40
        assertEquals(40, added, "Should add only 40 fuel");
        assertEquals(100, ship.getFuel(), "Fuel should be at max");
    }

    static void testWeaponProperties() {
        System.out.println("Test: Weapon properties");
        Ship.Weapon weapon = new Ship.Weapon(Ship.WeaponType.TORPEDO, "Photon Torpedo", 8, 12, 2);
        assertEquals(Ship.WeaponType.TORPEDO, weapon.getType(), "Type should be TORPEDO");
        assertEquals("Photon Torpedo", weapon.getName(), "Name should be Photon Torpedo");
        assertEquals(8, weapon.getStrength(), "Strength should be 8");
        assertEquals(12, weapon.getRange(), "Range should be 12");
        assertEquals(2, weapon.getArcs(), "Arcs should be 2");
    }

    static void testEqualsAndHashCode() {
        System.out.println("Test: Equals and hashCode");
        Ship ship1 = new Ship("Enterprise", Ship.Size.LARGE, 6, 4, 15, 10, 100, 200);
        Ship ship2 = new Ship("Enterprise", Ship.Size.LARGE, 6, 4, 15, 10, 100, 200);
        Ship ship3 = new Ship("Defiant", Ship.Size.SMALL, 8, 2, 8, 6, 30, 80);

        assertTrue(ship1.equals(ship2), "Ships with same name and size should be equal");
        assertFalse(ship1.equals(ship3), "Ships with different name should not be equal");
        assertEquals(ship1.hashCode(), ship2.hashCode(), "Equal ships should have same hashCode");
    }

    static void testToString() {
        System.out.println("Test: toString output");
        Ship ship = createTestShip();
        String str = ship.toString();
        assertTrue(str.contains("TestShip"), "toString should contain name");
        assertTrue(str.contains("MEDIUM"), "toString should contain size");
    }

    // --- Main runner ---

    public static void main(String[] args) {
        System.out.println("=== Ship Tests ===\n");

        testShipCreation();
        testShipSizeHexes();
        testSetName();
        testDamageWithArmor();
        testDamageLessThanArmor();
        testDamageEqualToArmor();
        testDestroyShip();
        testDamageDoesNotGoBelowZero();
        testIsDamaged();
        testRepair();
        testRepairExceedsMax();
        testAddCrew();
        testAddCrewExceedsCapacity();
        testRemoveCrew();
        testRemoveCrewExceedsCurrent();
        testAddWeapon();
        testGetWeaponsByType();
        testTotalBeamStrength();
        testConsumeFuel();
        testConsumeFuelExceedsAvailable();
        testRefuel();
        testRefuelExceedsMax();
        testWeaponProperties();
        testEqualsAndHashCode();
        testToString();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.exit(1);
        }
    }
}
