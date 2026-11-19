package com.yisy.modules.doramcu.shipdesign;

import java.util.Objects;

/**
 * Represents a hexagonal cell with state management via StateMachine.
 */
public class Hex {
    public enum State {
        EMPTY, OCCUPIED, SELECTED, BLOCKED
    }

    public enum Event {
        OCCUPY, CLEAR, SELECT, DESELECT, BLOCK, UNBLOCK
    }

    private final int q;
    private final int r;
    private final int s;
    private final StateMachine<State, Event> stateMachine;

    /**
     * Creates a new Hex cell with cubic coordinates (q, r, s).
     * The initial state is EMPTY.
     *
     * @param q the q cubic coordinate
     * @param r the r cubic coordinate
     * @param s the s cubic coordinate (depth/layer)
     */
    public Hex(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
        this.stateMachine = new StateMachine<>(State.EMPTY);

        // Define transitions
        stateMachine.addTransition(State.EMPTY, Event.OCCUPY, State.OCCUPIED);
        stateMachine.addTransition(State.EMPTY, Event.SELECT, State.SELECTED);
        stateMachine.addTransition(State.EMPTY, Event.BLOCK, State.BLOCKED);

        stateMachine.addTransition(State.OCCUPIED, Event.CLEAR, State.EMPTY);
        stateMachine.addTransition(State.OCCUPIED, Event.BLOCK, State.BLOCKED);

        stateMachine.addTransition(State.SELECTED, Event.DESELECT, State.EMPTY);
        stateMachine.addTransition(State.SELECTED, Event.OCCUPY, State.OCCUPIED);
        stateMachine.addTransition(State.SELECTED, Event.BLOCK, State.BLOCKED);

        stateMachine.addTransition(State.BLOCKED, Event.UNBLOCK, State.EMPTY);
    }

    /**
     * Returns the q cubic coordinate.
     */
    public int getQ() {
        return q;
    }

    /**
     * Returns the r cubic coordinate.
     */
    public int getR() {
        return r;
    }

    /**
     * Returns the s cubic coordinate (depth/layer).
     */
    public int getS() {
        return s;
    }

    /**
     * Returns the current state of this hex cell.
     */
    public State getState() {
        return stateMachine.getCurrentState();
    }

    /**
     * Attempts to fire an event on this hex cell.
     *
     * @param event the event to fire
     * @return true if a state transition occurred, false otherwise
     */
    public boolean fireEvent(Event event) {
        return stateMachine.fireEvent(event);
    }

    /**
     * Checks if the given event can be handled in the current state.
     */
    public boolean canHandle(Event event) {
        return stateMachine.canHandle(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hex hex = (Hex) o;
        return q == hex.q && r == hex.r && s == hex.s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r, s);
    }

    @Override
    public String toString() {
        return "Hex{" + q + "," + r + "," + s + " state=" + getState() + "}";
    }
}
