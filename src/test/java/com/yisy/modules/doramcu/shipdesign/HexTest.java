package com.yisy.modules.doramcu.shipdesign;

import java.util.*;

/**
 * Comprehensive tests for the Hex 3D class.
 */
public class HexTest {

    static int passed = 0;
    static int failed = 0;

    // --- Assertion helpers ---

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

    // --- Test methods ---

    static void test3DCoordinates() {
        System.out.println("Test: 3D cubic coordinates are stored correctly");
        Hex hex = new Hex(1, 2, 3);
        assertEquals(1, hex.getQ(), "Q coordinate should be 1");
        assertEquals(2, hex.getR(), "R coordinate should be 2");
        assertEquals(3, hex.getS(), "S coordinate should be 3");
    }

    static void testInitialState() {
        System.out.println("Test: Initial state is EMPTY");
        Hex hex = new Hex(0, 0, 0);
        assertEquals(Hex.State.EMPTY, hex.getState(), "Initial state should be EMPTY");
    }

    static void testOccupyTransition() {
        System.out.println("Test: EMPTY -> OCCUPIED via OCCUPY");
        Hex hex = new Hex(0, 0, 0);
        assertTrue(hex.fireEvent(Hex.Event.OCCUPY), "OCCUPY should succeed from EMPTY");
        assertEquals(Hex.State.OCCUPIED, hex.getState(), "State should be OCCUPIED");
    }

    static void testClearTransition() {
        System.out.println("Test: OCCUPIED -> EMPTY via CLEAR");
        Hex hex = new Hex(0, 0, 0);
        hex.fireEvent(Hex.Event.OCCUPY);
        assertTrue(hex.fireEvent(Hex.Event.CLEAR), "CLEAR should succeed from OCCUPIED");
        assertEquals(Hex.State.EMPTY, hex.getState(), "State should be EMPTY");
    }

    static void testSelectTransition() {
        System.out.println("Test: EMPTY -> SELECTED via SELECT");
        Hex hex = new Hex(0, 0, 0);
        assertTrue(hex.fireEvent(Hex.Event.SELECT), "SELECT should succeed from EMPTY");
        assertEquals(Hex.State.SELECTED, hex.getState(), "State should be SELECTED");
    }

    static void testDeselectTransition() {
        System.out.println("Test: SELECTED -> EMPTY via DESELECT");
        Hex hex = new Hex(0, 0, 0);
        hex.fireEvent(Hex.Event.SELECT);
        assertTrue(hex.fireEvent(Hex.Event.DESELECT), "DESELECT should succeed from SELECTED");
        assertEquals(Hex.State.EMPTY, hex.getState(), "State should be EMPTY");
    }

    static void testBlockTransition() {
        System.out.println("Test: EMPTY -> BLOCKED via BLOCK");
        Hex hex = new Hex(0, 0, 0);
        assertTrue(hex.fireEvent(Hex.Event.BLOCK), "BLOCK should succeed from EMPTY");
        assertEquals(Hex.State.BLOCKED, hex.getState(), "State should be BLOCKED");
    }

    static void testUnblockTransition() {
        System.out.println("Test: BLOCKED -> EMPTY via UNBLOCK");
        Hex hex = new Hex(0, 0, 0);
        hex.fireEvent(Hex.Event.BLOCK);
        assertTrue(hex.fireEvent(Hex.Event.UNBLOCK), "UNBLOCK should succeed from BLOCKED");
        assertEquals(Hex.State.EMPTY, hex.getState(), "State should be EMPTY");
    }

    static void testSelectedToOccupied() {
        System.out.println("Test: SELECTED -> OCCUPIED via OCCUPY");
        Hex hex = new Hex(0, 0, 0);
        hex.fireEvent(Hex.Event.SELECT);
        assertTrue(hex.fireEvent(Hex.Event.OCCUPY), "OCCUPY should succeed from SELECTED");
        assertEquals(Hex.State.OCCUPIED, hex.getState(), "State should be OCCUPIED");
    }

    static void testBlockFromAnyState() {
        System.out.println("Test: BLOCK transition from OCCUPIED and SELECTED");
        Hex hex1 = new Hex(0, 0, 0);
        hex1.fireEvent(Hex.Event.OCCUPY);
        assertTrue(hex1.fireEvent(Hex.Event.BLOCK), "BLOCK should succeed from OCCUPIED");
        assertEquals(Hex.State.BLOCKED, hex1.getState(), "State should be BLOCKED");

        Hex hex2 = new Hex(0, 0, 0);
        hex2.fireEvent(Hex.Event.SELECT);
        assertTrue(hex2.fireEvent(Hex.Event.BLOCK), "BLOCK should succeed from SELECTED");
        assertEquals(Hex.State.BLOCKED, hex2.getState(), "State should be BLOCKED");
    }

    static void testInvalidTransitions() {
        System.out.println("Test: Invalid transitions return false");
        Hex hex = new Hex(0, 0, 0);
        assertFalse(hex.fireEvent(Hex.Event.CLEAR), "CLEAR should fail from EMPTY");
        assertFalse(hex.fireEvent(Hex.Event.DESELECT), "DESELECT should fail from EMPTY");
        assertFalse(hex.fireEvent(Hex.Event.UNBLOCK), "UNBLOCK should fail from EMPTY");
        assertEquals(Hex.State.EMPTY, hex.getState(), "State should remain EMPTY");
    }

    static void testCanHandle() {
        System.out.println("Test: canHandle checks event validity");
        Hex hex = new Hex(0, 0, 0);
        assertTrue(hex.canHandle(Hex.Event.OCCUPY), "canHandle should return true for OCCUPY from EMPTY");
        assertFalse(hex.canHandle(Hex.Event.CLEAR), "canHandle should return false for CLEAR from EMPTY");

        hex.fireEvent(Hex.Event.OCCUPY);
        assertTrue(hex.canHandle(Hex.Event.CLEAR), "canHandle should return true for CLEAR from OCCUPIED");
        assertFalse(hex.canHandle(Hex.Event.OCCUPY), "canHandle should return false for OCCUPY from OCCUPIED");
    }

    static void testEqualsWith3DCoordinates() {
        System.out.println("Test: equals compares all 3D coordinates");
        Hex hex1 = new Hex(1, 2, 3);
        Hex hex2 = new Hex(1, 2, 3);
        Hex hex3 = new Hex(1, 2, 4);
        Hex hex4 = new Hex(1, 3, 3);
        Hex hex5 = new Hex(2, 2, 3);

        assertTrue(hex1.equals(hex2), "Hex with same q, r, s should be equal");
        assertFalse(hex1.equals(hex3), "Hex with different s should not be equal");
        assertFalse(hex1.equals(hex4), "Hex with different r should not be equal");
        assertFalse(hex1.equals(hex5), "Hex with different q should not be equal");
        assertFalse(hex1.equals(null), "Hex should not equal null");
        assertTrue(hex1.equals(hex1), "Hex should equal itself");
    }

    static void testHashCodeWith3DCoordinates() {
        System.out.println("Test: hashCode is consistent with equals");
        Hex hex1 = new Hex(1, 2, 3);
        Hex hex2 = new Hex(1, 2, 3);
        Hex hex3 = new Hex(1, 2, 4);

        assertEquals(hex1.hashCode(), hex2.hashCode(), "Equal hexes should have equal hashCodes");
        // Different s coordinate should generally produce different hash
        // (not guaranteed, but highly likely with Objects.hash)
    }

    static void testToString() {
        System.out.println("Test: toString includes all 3D coordinates and state");
        Hex hex = new Hex(5, -3, 2);
        String str = hex.toString();
        assertTrue(str.contains("5"), "toString should contain q=5");
        assertTrue(str.contains("-3"), "toString should contain r=-3");
        assertTrue(str.contains("2"), "toString should contain s=2");
        assertTrue(str.contains("EMPTY"), "toString should contain state=EMPTY");
    }

    static void testDifferentLayersIndependent() {
        System.out.println("Test: Hexes on different layers have independent states");
        Hex layer0 = new Hex(0, 0, 0);
        Hex layer1 = new Hex(0, 0, 1);
        Hex layer2 = new Hex(0, 0, 2);

        layer0.fireEvent(Hex.Event.OCCUPY);
        layer1.fireEvent(Hex.Event.SELECT);
        layer2.fireEvent(Hex.Event.BLOCK);

        assertEquals(Hex.State.OCCUPIED, layer0.getState(), "Layer 0 should be OCCUPIED");
        assertEquals(Hex.State.SELECTED, layer1.getState(), "Layer 1 should be SELECTED");
        assertEquals(Hex.State.BLOCKED, layer2.getState(), "Layer 2 should be BLOCKED");
    }

    // --- Main runner ---

    public static void main(String[] args) {
        System.out.println("=== Hex 3D Tests ===\n");

        test3DCoordinates();
        testInitialState();
        testOccupyTransition();
        testClearTransition();
        testSelectTransition();
        testDeselectTransition();
        testBlockTransition();
        testUnblockTransition();
        testSelectedToOccupied();
        testBlockFromAnyState();
        testInvalidTransitions();
        testCanHandle();
        testEqualsWith3DCoordinates();
        testHashCodeWith3DCoordinates();
        testToString();
        testDifferentLayersIndependent();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.exit(1);
        }
    }
}
