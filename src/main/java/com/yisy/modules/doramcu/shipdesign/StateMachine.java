package com.yisy.modules.doramcu.shipdesign;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A generic State Machine implementation that manages states and transitions.
 *
 * @param <S> the type of states
 * @param <E> the type of events that trigger transitions
 */
public class StateMachine<S, E> {
    private final Map<S, Map<E, S>> transitionTable = new ConcurrentHashMap<>();
    private final Map<S, List<Runnable>> stateEnterListeners = new HashMap<>();
    private final Map<S, List<Runnable>> stateExitListeners = new HashMap<>();
    private volatile S currentState;

    /**
     * Creates a new StateMachine with the given initial state.
     *
     * @param initialState the initial state of the machine
     */
    public StateMachine(S initialState) {
        this.currentState = initialState;
        fireEnterListeners(initialState);
    }

    /**
     * Defines a transition from one state to another triggered by an event.
     *
     * @param fromState the source state
     * @param event     the triggering event
     * @param toState   the target state
     * @return this StateMachine for method chaining
     */
    public StateMachine<S, E> addTransition(S fromState, E event, S toState) {
        transitionTable.computeIfAbsent(fromState, k -> new ConcurrentHashMap<>())
                .put(event, toState);
        return this;
    }

    /**
     * Registers a callback to execute when entering a specific state.
     *
     * @param state    the state to listen for
     * @param listener the callback to execute on state entry
     * @return this StateMachine for method chaining
     */
    public StateMachine<S, E> onEnter(S state, Runnable listener) {
        stateEnterListeners.computeIfAbsent(state, k -> new ArrayList<>()).add(listener);
        return this;
    }

    /**
     * Registers a callback to execute when exiting a specific state.
     *
     * @param state    the state to listen for
     * @param listener the callback to execute on state exit
     * @return this StateMachine for method chaining
     */
    public StateMachine<S, E> onExit(S state, Runnable listener) {
        stateExitListeners.computeIfAbsent(state, k -> new ArrayList<>()).add(listener);
        return this;
    }

    /**
     * Attempts to fire an event, triggering a state transition if one is defined.
     *
     * @param event the event to fire
     * @return true if a transition occurred, false if no transition was defined
     */
    public boolean fireEvent(E event) {
        S nextState = getNextState(currentState, event);
        if (nextState == null) {
            return false;
        }
        transitionTo(nextState);
        return true;
    }

    /**
     * Returns the current state of the machine.
     *
     * @return the current state
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * Checks if the machine can handle the given event from the current state.
     *
     * @param event the event to check
     * @return true if a transition is defined for the event
     */
    public boolean canHandle(E event) {
        return getNextState(currentState, event) != null;
    }

    /**
     * Returns all defined transitions from a given state.
     *
     * @param state the state to query
     * @return a map of events to target states, or empty map if none defined
     */
    public Map<E, S> getTransitionsFrom(S state) {
        return Optional.ofNullable(transitionTable.get(state))
                .orElse(Collections.emptyMap());
    }

    private S getNextState(S fromState, E event) {
        Map<E, S> transitions = transitionTable.get(fromState);
        return transitions != null ? transitions.get(event) : null;
    }

    private void transitionTo(S nextState) {
        fireExitListeners(currentState);
        this.currentState = nextState;
        fireEnterListeners(nextState);
    }

    private void fireEnterListeners(S state) {
        List<Runnable> listeners = stateEnterListeners.get(state);
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }

    private void fireExitListeners(S state) {
        List<Runnable> listeners = stateExitListeners.get(state);
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }
}
