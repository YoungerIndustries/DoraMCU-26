package com.yisy.modules.doramcu.unified;

import com.yisy.modules.doramcu.hex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

/**
 * Event bus for cross-module events in the unified world.
 * <p>
 * Modules publish events when significant actions occur. Other modules
 * can register listeners to react to these events. Events are persisted
 * in memory for retrieval via the REST API.
 * </p>
 * <p>
 * Example: Captain's Log mission complete Ã¢â€ â€™ Star Wars faction unlocked.
 * </p>
 */
@Service
public class UnifiedEventBus {

    private static final Logger log = LoggerFactory.getLogger(UnifiedEventBus.class);

    private final List<UnifiedEvent> eventHistory = new ArrayList<>();
    private final Map<String, List<Consumer<UnifiedEvent>>> listeners = new java.util.HashMap<>();

    /**
     * Publishes an event to all registered listeners and stores it in history.
     *
     * @param event the event to publish
     */
    public void publish(UnifiedEvent event) {
        log.info("Publishing event: {}", event);
        eventHistory.add(event);

        // Notify listeners for this event type
        String typeKey = event.getType().name();
        listeners.getOrDefault(typeKey, Collections.emptyList()).forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.error("Error in event listener for {}: {}", typeKey, e.getMessage());
            }
        });

        // Notify wildcard listeners
        listeners.getOrDefault("*", Collections.emptyList()).forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.error("Error in wildcard event listener: {}", e.getMessage());
            }
        });
    }

    /**
     * Registers a listener for a specific event type.
     *
     * @param eventType the event type to listen for
     * @param listener  the callback
     */
    public void registerListener(UnifiedEvent.EventType eventType, Consumer<UnifiedEvent> listener) {
        listeners.computeIfAbsent(eventType.name(), k -> new CopyOnWriteArrayList<>()).add(listener);
        log.debug("Registered listener for {}", eventType);
    }

    /**
     * Registers a listener for all event types.
     *
     * @param listener the callback
     */
    public void registerWildcardListener(Consumer<UnifiedEvent> listener) {
        listeners.computeIfAbsent("*", k -> new CopyOnWriteArrayList<>()).add(listener);
        log.debug("Registered wildcard listener");
    }

    /**
     * Returns the event history, newest first.
     *
     * @return list of all events
     */
    public List<UnifiedEvent> getEventHistory() {
        List<UnifiedEvent> copy = new ArrayList<>(eventHistory);
        copy.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return Collections.unmodifiableList(copy);
    }

    /**
     * Returns recent events for a specific module.
     *
     * @param sourceModule the module name
     * @param limit        max events to return
     * @return filtered event list
     */
    public List<UnifiedEvent> getEventsByModule(String sourceModule, int limit) {
        return eventHistory.stream()
                .filter(e -> sourceModule == null || e.getSourceModule().equals(sourceModule))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    /**
     * Returns recent events (last N).
     *
     * @param limit max events to return
     * @return recent events
     */
    public List<UnifiedEvent> getRecentEvents(int limit) {
        return eventHistory.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    /**
     * Clears the event history.
     */
    public void clearHistory() {
        eventHistory.clear();
        log.info("Event history cleared");
    }
}
