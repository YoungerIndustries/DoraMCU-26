package com.yisy.modules.doramcu.event;

import com.yisy.modules.doramcu.service.GalaxyMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized event publisher and SSE broadcaster for galaxy lifecycle events.
 * <p>
 * Services publish events via {@link #publish(GalaxyEvent)}. Clients subscribe
 * via SSE to receive real-time event streams. Supports both global subscription
 * (all galaxies) and per-galaxy subscription.
 * </p>
 * <p>
 * Maintains an in-memory event history buffer (last 100 events) so new clients
 * can receive recent events on subscription.
 * </p>
 */
@Service
public class GalaxyEventService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyEventService.class);
    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final int EVENT_HISTORY_SIZE = 100;

    /**
     * All connected SSE emitters, keyed by client ID.
     * Each entry stores the emitter and the set of galaxy names it subscribes to
     * (empty set = subscribe to all galaxies).
     */
    private final Map<String, Subscriber> emitters = new ConcurrentHashMap<>();

    /**
     * Recent event history for new subscribers.
     */
    private final List<GalaxyEvent> eventHistory = new CopyOnWriteArrayList<>();

    /**
     * Metrics service for Prometheus event counters.
     */
    @Autowired(required = false)
    private GalaxyMetricsService galaxyMetricsService;

    /**
     * Holds an SSE emitter and its subscription filter.
     */
    static class Subscriber {
        final SseEmitter emitter;
        final List<String> galaxyFilters; // null or empty = all galaxies

        Subscriber(SseEmitter emitter, List<String> galaxyFilters) {
            this.emitter = emitter;
            this.galaxyFilters = galaxyFilters;
        }
    }

    /**
     * Publishes a galaxy event to all subscribed clients.
     * <p>
     * The event is stored in the history buffer and broadcast immediately
     * to clients whose subscription matches the event's galaxy.
     * </p>
     *
     * @param event the event to publish
     */
    public void publish(GalaxyEvent event) {
        // Add to history buffer
        eventHistory.add(event);
        if (eventHistory.size() > EVENT_HISTORY_SIZE) {
            eventHistory.remove(0);
        }

        log.debug("Publishing galaxy event: {} for galaxy '{}'", event.getType(), event.getGalaxyName());

        // Increment Prometheus event counter
        if (galaxyMetricsService != null) {
            galaxyMetricsService.incrementEvent(event.getType());
        }

        // Broadcast to matching subscribers
        broadcast(event);
    }

    /**
     * Convenience method to publish an event with a simple payload.
     */
    public void publish(GalaxyEventType type, String galaxyName, Long galaxyId, String... keyValuePairs) {
        Map<String, Object> payload = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                payload.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }
        publish(new GalaxyEvent(type, galaxyName, galaxyId, payload));
    }

    /**
     * Subscribes a client to galaxy events.
     * <p>
     * If galaxyNames is null or empty, the client receives events for all galaxies.
     * Otherwise, only events matching the specified galaxy names are delivered.
     * </p>
     *
     * @param clientId unique client identifier
     * @param galaxyNames list of galaxy names to filter by (null = all)
     * @return the registered SseEmitter
     */
    public SseEmitter subscribe(String clientId, List<String> galaxyNames) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emitters.put(clientId, new Subscriber(emitter, galaxyNames));

        emitter.onCompletion(() -> {
            log.debug("SSE event stream completed for client: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE event stream timed out for client: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onError((e) -> {
            log.warn("SSE event stream error for client: {}", clientId, e);
            emitters.remove(clientId);
        });

        // Send recent event history on subscription
        sendHistoryToClient(clientId, emitter, galaxyNames);

        return emitter;
    }

    /**
     * Subscribes a client to all galaxy events (no filter).
     */
    public SseEmitter subscribeAll(String clientId) {
        return subscribe(clientId, null);
    }

    /**
     * Subscribes a client to events for a specific galaxy.
     */
    public SseEmitter subscribeGalaxy(String clientId, String galaxyName) {
        return subscribe(clientId, List.of(galaxyName));
    }

    /**
     * Returns the recent event history, optionally filtered by galaxy name.
     *
     * @param galaxyName if non-null, only events for this galaxy
     * @return list of recent events
     */
    public List<GalaxyEvent> getEventHistory(String galaxyName) {
        if (galaxyName == null) {
            return new ArrayList<>(eventHistory);
        }
        return eventHistory.stream()
                .filter(e -> galaxyName.equals(e.getGalaxyName()))
                .toList();
    }

    /**
     * Returns the total number of connected subscribers.
     */
    public int getSubscriberCount() {
        return emitters.size();
    }

    // -- Internal methods --

    private void broadcast(GalaxyEvent event) {
        List<String> failedClients = new ArrayList<>();
        emitters.forEach((clientId, subscriber) -> {
            // Check if this subscriber matches the event's galaxy
            if (!matchesFilter(subscriber.galaxyFilters, event.getGalaxyName())) {
                return;
            }
            try {
                subscriber.emitter.send(SseEmitter.event()
                        .name(event.getType().name().toLowerCase())
                        .data(eventToMap(event)));
            } catch (IOException e) {
                log.warn("Failed to send event to client: {}", clientId);
                failedClients.add(clientId);
            }
        });
        // Remove failed clients
        failedClients.forEach(emitters::remove);
    }

    private boolean matchesFilter(List<String> filters, String galaxyName) {
        if (filters == null || filters.isEmpty()) {
            return true; // No filter = receive all
        }
        return filters.contains(galaxyName);
    }

    private void sendHistoryToClient(String clientId, SseEmitter emitter, List<String> galaxyNames) {
        try {
            // Send a subscription confirmation event
            emitter.send(SseEmitter.event()
                    .name("subscribed")
                    .data(Map.of(
                            "clientId", clientId,
                            "galaxyFilter", galaxyNames != null && !galaxyNames.isEmpty() ? galaxyNames : "all",
                            "historySize", eventHistory.size()
                    )));

            // Send recent events
            for (GalaxyEvent event : eventHistory) {
                if (matchesFilter(galaxyNames, event.getGalaxyName())) {
                    emitter.send(SseEmitter.event()
                            .name("history")
                            .data(eventToMap(event)));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to send history to client: {}", clientId, e);
        }
    }

    private Map<String, Object> eventToMap(GalaxyEvent event) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", event.getType().name());
        map.put("galaxyName", event.getGalaxyName());
        if (event.getGalaxyId() != null) {
            map.put("galaxyId", event.getGalaxyId());
        }
        map.put("timestamp", event.getTimestamp().toString());
        if (!event.getPayload().isEmpty()) {
            map.put("payload", event.getPayload());
        }
        return map;
    }
}
