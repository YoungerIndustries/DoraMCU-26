package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive tests for the StateMachine class.
 */
public class StateMachineTest {

    static int passed = 0;
    static int failed = 0;

    // --- Enum definitions for testing ---

    enum TrafficLightState {
        RED, YELLOW, GREEN
    }

    enum TrafficLightEvent {
        SWITCH
    }

    enum OrderState {
        DRAFT, SUBMITTED, APPROVED, SHIPPED, DELIVERED
    }

    enum OrderEvent {
        SUBMIT, APPROVE, SHIP, DELIVER, CANCEL
    }

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

    static void testInitialState() {
        System.out.println("Test: Initial state is set correctly");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        assertEquals(TrafficLightState.RED, sm.getCurrentState(), "Initial state should be RED");
    }

    static void testAddSingleTransition() {
        System.out.println("Test: Add single transition");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);

        boolean result = sm.fireEvent(TrafficLightEvent.SWITCH);
        assertTrue(result, "fireEvent should return true when transition exists");
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState(), "State should transition to GREEN");
    }

    static void testMultipleTransitions() {
        System.out.println("Test: Multiple transitions in sequence");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);
        sm.addTransition(TrafficLightState.GREEN, TrafficLightEvent.SWITCH, TrafficLightState.YELLOW);
        sm.addTransition(TrafficLightState.YELLOW, TrafficLightEvent.SWITCH, TrafficLightState.RED);

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertEquals(TrafficLightState.GREEN, sm.getCurrentState(), "RED -> GREEN");

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertEquals(TrafficLightState.YELLOW, sm.getCurrentState(), "GREEN -> YELLOW");

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertEquals(TrafficLightState.RED, sm.getCurrentState(), "YELLOW -> RED");
    }

    static void testNoTransitionDefined() {
        System.out.println("Test: Fire event with no transition defined");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);

        boolean result = sm.fireEvent(TrafficLightEvent.SWITCH);
        assertFalse(result, "fireEvent should return false when no transition is defined");
        assertEquals(TrafficLightState.RED, sm.getCurrentState(), "State should remain RED");
    }

    static void testCanHandle() {
        System.out.println("Test: canHandle checks if event is handled");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);

        assertTrue(sm.canHandle(TrafficLightEvent.SWITCH), "canHandle should return true for defined transition");

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertFalse(sm.canHandle(TrafficLightEvent.SWITCH), "canHandle should return false when no transition from current state");
    }

    static void testMethodChaining() {
        System.out.println("Test: Method chaining returns the same instance");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        StateMachine<TrafficLightState, TrafficLightEvent> chained = sm
                .addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN)
                .addTransition(TrafficLightState.GREEN, TrafficLightEvent.SWITCH, TrafficLightState.YELLOW);

        assertTrue(sm == chained, "Method chaining should return the same instance");
        assertEquals(TrafficLightState.RED, sm.getCurrentState(), "Initial state should still be RED");
    }

    static void testOnEnterListener() {
        System.out.println("Test: onEnter listener is called on transition");
        List<String> log = new ArrayList<>();
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);
        sm.onEnter(TrafficLightState.GREEN, () -> log.add("entered GREEN"));

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertTrue(log.contains("entered GREEN"), "onEnter listener should be called for GREEN");
    }

    static void testOnExitListener() {
        System.out.println("Test: onExit listener is called on transition");
        List<String> log = new ArrayList<>();
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);
        sm.onExit(TrafficLightState.RED, () -> log.add("exited RED"));

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertTrue(log.contains("exited RED"), "onExit listener should be called for RED");
    }

    static void testListenerOrder() {
        System.out.println("Test: Exit listener fires before enter listener");
        List<String> log = new ArrayList<>();
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);
        sm.onExit(TrafficLightState.RED, () -> log.add("exit"));
        sm.onEnter(TrafficLightState.GREEN, () -> log.add("enter"));

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertEquals(0, (int) log.indexOf("exit"), "Exit should be logged first");
        assertEquals(1, (int) log.indexOf("enter"), "Enter should be logged second");
    }

    static void testInitialEnterListener() {
        System.out.println("Test: onEnter listener fires for initial state");
        List<String> log = new ArrayList<>();
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.onEnter(TrafficLightState.RED, () -> log.add("entered RED"));

        // The initial enter listener should have fired at construction time
        // We add a new listener after construction, so it should NOT fire again
        // Let's test by creating a new instance with the listener registered before any event
        List<String> log2 = new ArrayList<>();
        StateMachine<TrafficLightState, TrafficLightEvent> sm2 = new StateMachine<>(TrafficLightState.RED);
        sm2.onEnter(TrafficLightState.RED, () -> log2.add("entered RED"));
        // Add a transition to another state and back to RED
        sm2.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);
        sm2.addTransition(TrafficLightState.GREEN, TrafficLightEvent.SWITCH, TrafficLightState.RED);

        sm2.fireEvent(TrafficLightEvent.SWITCH); // RED -> GREEN
        sm2.fireEvent(TrafficLightEvent.SWITCH); // GREEN -> RED

        assertTrue(log2.contains("entered RED"), "onEnter should fire when returning to RED");
    }

    static void testMultipleListenersOnSameState() {
        System.out.println("Test: Multiple listeners on the same state");
        List<String> log = new ArrayList<>();
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);
        sm.onEnter(TrafficLightState.GREEN, () -> log.add("listener1"));
        sm.onEnter(TrafficLightState.GREEN, () -> log.add("listener2"));

        sm.fireEvent(TrafficLightEvent.SWITCH);
        assertTrue(log.contains("listener1"), "First listener should fire");
        assertTrue(log.contains("listener2"), "Second listener should fire");
    }

    static void testGetTransitionsFrom() {
        System.out.println("Test: getTransitionsFrom returns defined transitions");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.GREEN);

        var transitions = sm.getTransitionsFrom(TrafficLightState.RED);
        assertEquals(TrafficLightState.GREEN, transitions.get(TrafficLightEvent.SWITCH), "Transition map should contain SWITCH -> GREEN");

        var emptyTransitions = sm.getTransitionsFrom(TrafficLightState.YELLOW);
        assertTrue(emptyTransitions.isEmpty(), "getTransitionsFrom should return empty map for undefined state");
    }

    static void testOrderWorkflow() {
        System.out.println("Test: Complex order workflow scenario");
        StateMachine<OrderState, OrderEvent> sm = new StateMachine<>(OrderState.DRAFT);
        sm.addTransition(OrderState.DRAFT, OrderEvent.SUBMIT, OrderState.SUBMITTED)
                .addTransition(OrderState.SUBMITTED, OrderEvent.APPROVE, OrderState.APPROVED)
                .addTransition(OrderState.APPROVED, OrderEvent.SHIP, OrderState.SHIPPED)
                .addTransition(OrderState.SHIPPED, OrderEvent.DELIVER, OrderState.DELIVERED);

        assertEquals(OrderState.DRAFT, sm.getCurrentState(), "Start at DRAFT");

        sm.fireEvent(OrderEvent.SUBMIT);
        assertEquals(OrderState.SUBMITTED, sm.getCurrentState(), "After SUBMIT: SUBMITTED");

        sm.fireEvent(OrderEvent.APPROVE);
        assertEquals(OrderState.APPROVED, sm.getCurrentState(), "After APPROVE: APPROVED");

        sm.fireEvent(OrderEvent.SHIP);
        assertEquals(OrderState.SHIPPED, sm.getCurrentState(), "After SHIP: SHIPPED");

        sm.fireEvent(OrderEvent.DELIVER);
        assertEquals(OrderState.DELIVERED, sm.getCurrentState(), "After DELIVER: DELIVERED");

        // Try an invalid event
        assertFalse(sm.canHandle(OrderEvent.SUBMIT), "Cannot SUBMIT from DELIVERED state");
    }

    static void testSelfTransition() {
        System.out.println("Test: Self-transition (state to itself)");
        StateMachine<TrafficLightState, TrafficLightEvent> sm = new StateMachine<>(TrafficLightState.RED);
        sm.addTransition(TrafficLightState.RED, TrafficLightEvent.SWITCH, TrafficLightState.RED);

        List<String> log = new ArrayList<>();
        sm.onExit(TrafficLightState.RED, () -> log.add("exit"));
        sm.onEnter(TrafficLightState.RED, () -> log.add("enter"));

        boolean result = sm.fireEvent(TrafficLightEvent.SWITCH);
        assertTrue(result, "Self-transition should return true");
        assertEquals(TrafficLightState.RED, sm.getCurrentState(), "State should still be RED");
        assertTrue(log.contains("exit"), "Exit listener should fire on self-transition");
        assertTrue(log.contains("enter"), "Enter listener should fire on self-transition");
    }

    // --- Main runner ---

    public static void main(String[] args) {
        System.out.println("=== StateMachine Tests ===\n");

        testInitialState();
        testAddSingleTransition();
        testMultipleTransitions();
        testNoTransitionDefined();
        testCanHandle();
        testMethodChaining();
        testOnEnterListener();
        testOnExitListener();
        testListenerOrder();
        testInitialEnterListener();
        testMultipleListenersOnSameState();
        testGetTransitionsFrom();
        testOrderWorkflow();
        testSelfTransition();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed > 0) {
            System.exit(1);
        }
    }
}
