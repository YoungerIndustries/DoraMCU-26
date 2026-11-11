package com.yisy.modules.doramcu.hex.planet;

import java.util.*;
import java.util.function.Consumer;

/**
 * Manages planet-level events: dispatches events to registered listeners
 * and maintains an event history.
 * <p>
 * Listeners can register for specific event types or receive all events.
 * Events are dispatched synchronously during turn processing.
 */
public class PlanetEventManager {

    private final Map<PlanetEventType, List<Consumer<PlanetEvent>>> listeners;
    private final List<Consumer<PlanetEvent>> globalListeners;
    private final List<PlanetEvent> eventHistory;
    private final int maxHistorySize;

    /**
     * Creates a new event manager with default history size of 1000.
     */
    public PlanetEventManager() {
        this(1000);
    }

    /**
     * Creates a new event manager with the given history size.
     *
     * @param maxHistorySize maximum number of events to keep in history
     */
    public PlanetEventManager(int maxHistorySize) {
        this.listeners = new EnumMap<>(PlanetEventType.class);
        this.globalListeners = new ArrayList<>();
        this.eventHistory = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Registers a listener for a specific event type.
     *
     * @param eventType the event type to listen for
     * @param listener  the callback function
     */
    public void registerListener(PlanetEventType eventType, Consumer<PlanetEvent> listener) {
        if (eventType == null || listener == null) {
            throw new IllegalArgumentException("Event type and listener must not be null");
        }
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Registers a global listener that receives all events.
     *
     * @param listener the callback function
     */
    public void registerGlobalListener(Consumer<PlanetEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        globalListeners.add(listener);
    }

    /**
     * Unregisters a listener for a specific event type.
     *
     * @param eventType the event type
     * @param listener  the callback to remove
     * @return true if the listener was removed
     */
    public boolean unregisterListener(PlanetEventType eventType, Consumer<PlanetEvent> listener) {
        List<Consumer<PlanetEvent>> typeListeners = listeners.get(eventType);
        if (typeListeners != null) {
            return typeListeners.remove(listener);
        }
        return false;
    }

    /**
     * Unregisters a global listener.
     *
     * @param listener the callback to remove
     * @return true if the listener was removed
     */
    public boolean unregisterGlobalListener(Consumer<PlanetEvent> listener) {
        return globalListeners.remove(listener);
    }

    /**
     * Dispatches an event to all registered listeners.
     *
     * @param event the event to dispatch
     */
    public void dispatch(PlanetEvent event) {
        if (event == null) return;

        // Add to history
        eventHistory.add(event);
        if (eventHistory.size() > maxHistorySize) {
            eventHistory.remove(0);
        }

        // Dispatch to type-specific listeners
        List<Consumer<PlanetEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<PlanetEvent> listener : new ArrayList<>(typeListeners)) {
                listener.accept(event);
            }
        }

        // Dispatch to global listeners
        for (Consumer<PlanetEvent> listener : new ArrayList<>(globalListeners)) {
            listener.accept(event);
        }
    }

    /**
     * Creates and dispatches an event in one call.
     *
     * @param type      the event type
     * @param timestamp the turn number
     * @param sourceId  the source entity ID, or null
     * @param payload   event-specific data, or null
     */
    public void fireEvent(PlanetEventType type, long timestamp, String sourceId, Object payload) {
        dispatch(new PlanetEvent(type, timestamp, sourceId, payload));
    }

    /**
     * Creates and dispatches an event without a source.
     *
     * @param type      the event type
     * @param timestamp the turn number
     */
    public void fireEvent(PlanetEventType type, long timestamp) {
        dispatch(new PlanetEvent(type, timestamp));
    }

    /**
     * @return the event history (oldest first)
     */
    public List<PlanetEvent> getEventHistory() {
        return Collections.unmodifiableList(eventHistory);
    }

    /**
     * Returns events of a specific type from history.
     *
     * @param eventType the event type to filter by
     * @return list of matching events
     */
    public List<PlanetEvent> getEventsByType(PlanetEventType eventType) {
        return eventHistory.stream()
                .filter(e -> e.getType() == eventType)
                .toList();
    }

    /**
     * Returns events from a specific source.
     *
     * @param sourceId the source entity ID
     * @return list of matching events
     */
    public List<PlanetEvent> getEventsBySource(String sourceId) {
        return eventHistory.stream()
                .filter(e -> sourceId.equals(e.getSourceId()))
                .toList();
    }

    /**
     * @return the number of registered type-specific listeners
     */
    public int getListenerCount() {
        int count = 0;
        for (List<Consumer<PlanetEvent>> list : listeners.values()) {
            count += list.size();
        }
        count += globalListeners.size();
        return count;
    }

    /**
     * Clears all event history.
     */
    public void clearHistory() {
        eventHistory.clear();
    }

    /**
     * Clears all registered listeners.
     */
    public void clearListeners() {
        listeners.clear();
        globalListeners.clear();
    }
}
