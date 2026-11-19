package com.yisy.modules.doramcu.shipdesign;

import java.util.List;

/**
 * Comprehensive tests for the Shipyard class.
 */
public class ShipyardTest {

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

    static void testEmptyShipyard() {
        System.out.println("Test: Empty shipyard state");
        Shipyard yard = new Shipyard();

        assertEquals(0, yard.getTotalOrderCount(), "Total orders should be 0");
        assertEquals(0, yard.getActiveOrderCount(), "Active orders should be 0");
        assertEquals(0, yard.getCompletedShips().size(), "Completed ships should be 0");
        assertEquals(0, yard.getAllOrders().size(), "All orders should be empty");
    }

    static void testOrderFromTemplate() {
        System.out.println("Test: Place order from template");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Star Hawk", ShipTemplate.scout());

        assertEquals(1, yard.getTotalOrderCount(), "Should have 1 order");
        assertEquals(1, yard.getActiveOrderCount(), "Should have 1 active order");
        assertEquals("Star Hawk", order.getShipName(), "Order ship name should match");
        assertEquals(Shipyard.Stage.PLANNING, order.getStage(), "Order should be in PLANNING stage");
        assertEquals("Scout", order.getTemplate().getName(), "Template should be Scout");
        assertTrue(order.getOrderId().startsWith("ORD-"), "Order ID should start with ORD-");
    }

    static void testOrderFromBuilder() {
        System.out.println("Test: Place order from builder");
        Shipyard yard = new Shipyard();
        ShipBuilder builder = new ShipBuilder()
                .name("Custom Ship")
                .size(Ship.Size.MEDIUM)
                .addBeam("Laser", 2, 5, 1);

        Shipyard.ShipOrder order = yard.orderFromBuilder(builder);

        assertEquals(1, yard.getTotalOrderCount(), "Should have 1 order");
        assertEquals(Shipyard.Stage.PLANNING, order.getStage(), "Order should be in PLANNING stage");
        assertTrue(order.getBuilder() != null, "Order should have a builder");
    }

    static void testMultipleOrders() {
        System.out.println("Test: Place multiple orders");
        Shipyard yard = new Shipyard();

        yard.orderFromTemplate("Alpha", ShipTemplate.scout());
        yard.orderFromTemplate("Beta", ShipTemplate.fighter());
        yard.orderFromTemplate("Gamma", ShipTemplate.cruiser());

        assertEquals(3, yard.getTotalOrderCount(), "Should have 3 orders");
        assertEquals(3, yard.getActiveOrderCount(), "Should have 3 active orders");
    }

    static void testOrderIDsAreUnique() {
        System.out.println("Test: Order IDs are unique");
        Shipyard yard = new Shipyard();

        Shipyard.ShipOrder order1 = yard.orderFromTemplate("Alpha", ShipTemplate.scout());
        Shipyard.ShipOrder order2 = yard.orderFromTemplate("Beta", ShipTemplate.fighter());

        assertFalse(order1.getOrderId().equals(order2.getOrderId()), "Order IDs should be unique");
        assertEquals("ORD-001", order1.getOrderId(), "First order should be ORD-001");
        assertEquals("ORD-002", order2.getOrderId(), "Second order should be ORD-002");
    }

    static void testValidateDesign() {
        System.out.println("Test: Validate design for template order");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Scout Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        boolean valid = yard.validateDesign(orderId);
        assertTrue(valid, "Scout template should be valid");

        assertTrue(order.getDesignPoints().isPresent(), "Should have design points after validation");
    }

    static void testStartConstruction() {
        System.out.println("Test: Start construction on order");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Test Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.startConstruction(orderId);

        assertEquals(Shipyard.Stage.UNDER_CONSTRUCTION, order.getStage(), "Stage should be UNDER_CONSTRUCTION");
        assertTrue(order.getNotes().get(0).contains("Construction started"), "Should have construction note");
    }

    static void testStartConstructionNotInPlanning() {
        System.out.println("Test: Cannot start construction if not in PLANNING");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Test Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.startConstruction(orderId);

        assertThrows(IllegalStateException.class, () -> {
            yard.startConstruction(orderId);
        }, "Should throw when already under construction");
    }

    static void testCompleteOrder() {
        System.out.println("Test: Complete order builds ship");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Completed Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.startConstruction(orderId);
        Ship ship = yard.completeOrder(orderId);

        assertEquals("Completed Ship", ship.getName(), "Completed ship name should match");
        assertEquals(Shipyard.Stage.COMPLETE, order.getStage(), "Stage should be COMPLETE");
        assertEquals(ship, order.getCompletedShip(), "Order should reference completed ship");
        assertEquals(1, yard.getCompletedShips().size(), "Should have 1 completed ship");
        assertEquals(0, yard.getActiveOrderCount(), "Should have 0 active orders");
    }

    static void testCompleteOrderNotUnderConstruction() {
        System.out.println("Test: Cannot complete order not under construction");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Test Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        assertThrows(IllegalStateException.class, () -> {
            yard.completeOrder(orderId);
        }, "Should throw when not under construction");
    }

    static void testCancelOrder() {
        System.out.println("Test: Cancel order");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Cancelled Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.cancelOrder(orderId);

        assertEquals(Shipyard.Stage.CANCELLED, order.getStage(), "Stage should be CANCELLED");
        assertEquals(0, yard.getActiveOrderCount(), "Should have 0 active orders");
    }

    static void testCancelCompletedOrderThrows() {
        System.out.println("Test: Cannot cancel completed order");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Test Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.startConstruction(orderId);
        yard.completeOrder(orderId);

        assertThrows(IllegalStateException.class, () -> {
            yard.cancelOrder(orderId);
        }, "Should throw when cancelling completed order");
    }

    static void testAddNote() {
        System.out.println("Test: Add note to order");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Test Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.addNote(orderId, "Custom modification requested");

        assertEquals(1, order.getNotes().size(), "Should have 1 note");
        assertEquals("Custom modification requested", order.getNotes().get(0), "Note should match");
    }

    static void testGetOrderNotFound() {
        System.out.println("Test: Get order throws when not found");
        Shipyard yard = new Shipyard();

        assertThrows(IllegalArgumentException.class, () -> {
            yard.getOrder("ORD-999");
        }, "Should throw when order not found");
    }

    static void testGetOrdersByStage() {
        System.out.println("Test: Get orders by stage");
        Shipyard yard = new Shipyard();

        yard.orderFromTemplate("Alpha", ShipTemplate.scout());
        Shipyard.ShipOrder order2 = yard.orderFromTemplate("Beta", ShipTemplate.fighter());
        yard.orderFromTemplate("Gamma", ShipTemplate.cruiser());

        yard.startConstruction(order2.getOrderId());
        yard.completeOrder(order2.getOrderId());

        assertEquals(2, yard.getOrdersByStage(Shipyard.Stage.PLANNING).size(), "Should have 2 PLANNING orders");
        assertEquals(1, yard.getOrdersByStage(Shipyard.Stage.COMPLETE).size(), "Should have 1 COMPLETE order");
        assertEquals(0, yard.getOrdersByStage(Shipyard.Stage.CANCELLED).size(), "Should have 0 CANCELLED orders");
    }

    static void testCompleteFromBuilder() {
        System.out.println("Test: Complete order from custom builder");
        Shipyard yard = new Shipyard();
        ShipBuilder builder = new ShipBuilder()
                .name("Builder Ship")
                .size(Ship.Size.SMALL)
                .addBeam("Laser", 2, 5, 1);

        Shipyard.ShipOrder order = yard.orderFromBuilder(builder);
        String orderId = order.getOrderId();

        yard.startConstruction(orderId);
        Ship ship = yard.completeOrder(orderId);

        assertEquals("Builder Ship", ship.getName(), "Ship name should match builder");
        assertEquals(Ship.Size.SMALL, ship.getSize(), "Ship size should match builder");
        assertEquals(1, ship.getWeapons().size(), "Ship should have 1 weapon");
    }

    static void testProcessAllOrders() {
        System.out.println("Test: Process all orders");
        Shipyard yard = new Shipyard();

        yard.orderFromTemplate("Scout 1", ShipTemplate.scout());
        yard.orderFromTemplate("Scout 2", ShipTemplate.scout());
        yard.orderFromTemplate("Transport 1", ShipTemplate.transport());

        int completed = yard.processAllOrders();

        assertEquals(3, completed, "Should complete 3 orders");
        assertEquals(3, yard.getCompletedShips().size(), "Should have 3 completed ships");
        assertEquals(0, yard.getActiveOrderCount(), "Should have 0 active orders");
    }

    static void testRemoveOrder() {
        System.out.println("Test: Remove order from shipyard");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Test Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.removeOrder(orderId);

        assertEquals(0, yard.getTotalOrderCount(), "Should have 0 orders after removal");
    }

    static void testFullLifecycle() {
        System.out.println("Test: Full order lifecycle");
        Shipyard yard = new Shipyard();

        // Place order
        Shipyard.ShipOrder order = yard.orderFromTemplate("Lifecycle Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();
        assertEquals(Shipyard.Stage.PLANNING, order.getStage(), "Should start in PLANNING");

        // Validate
        boolean valid = yard.validateDesign(orderId);
        assertTrue(valid, "Design should be valid");

        // Start construction
        yard.startConstruction(orderId);
        assertEquals(Shipyard.Stage.UNDER_CONSTRUCTION, order.getStage(), "Should be UNDER_CONSTRUCTION");

        // Complete
        Ship ship = yard.completeOrder(orderId);
        assertEquals("Lifecycle Ship", ship.getName(), "Ship name should match");
        assertEquals(Shipyard.Stage.COMPLETE, order.getStage(), "Should be COMPLETE");

        // Verify notes
        assertTrue(order.getNotes().size() >= 1, "Should have at least 1 note");
    }

    static void testShipyardToString() {
        System.out.println("Test: Shipyard toString");
        Shipyard yard = new Shipyard();
        yard.orderFromTemplate("Test", ShipTemplate.scout());

        String str = yard.toString();
        assertTrue(str.contains("Shipyard"), "toString should contain Shipyard");
        assertTrue(str.contains("totalOrders="), "toString should contain total orders");
    }

    static void testOrderToString() {
        System.out.println("Test: ShipOrder toString");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("Order Ship", ShipTemplate.scout());

        String str = order.toString();
        assertTrue(str.contains("Order Ship"), "toString should contain ship name");
        assertTrue(str.contains("PLANNING"), "toString should contain stage");
    }

    static void testDesignPointsOnOrder() {
        System.out.println("Test: Design points available on order after validation");
        Shipyard yard = new Shipyard();
        Shipyard.ShipOrder order = yard.orderFromTemplate("DP Ship", ShipTemplate.scout());
        String orderId = order.getOrderId();

        yard.validateDesign(orderId);

        assertTrue(order.getDesignPoints().isPresent(), "Order should have design points after validation");
        DesignPoints dp = order.getDesignPoints().get();
        assertEquals(Ship.Size.SMALL, dp.getSize(), "Design points size should match");
    }

    // --- Main ---

    public static void main(String[] args) {
        System.out.println("=== Shipyard Tests ===\n");

        testEmptyShipyard();
        testOrderFromTemplate();
        testOrderFromBuilder();
        testMultipleOrders();
        testOrderIDsAreUnique();
        testValidateDesign();
        testStartConstruction();
        testStartConstructionNotInPlanning();
        testCompleteOrder();
        testCompleteOrderNotUnderConstruction();
        testCancelOrder();
        testCancelCompletedOrderThrows();
        testAddNote();
        testGetOrderNotFound();
        testGetOrdersByStage();
        testCompleteFromBuilder();
        testProcessAllOrders();
        testRemoveOrder();
        testFullLifecycle();
        testShipyardToString();
        testOrderToString();
        testDesignPointsOnOrder();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed == 0) {
            System.out.println("All tests passed!");
        }
    }
}
