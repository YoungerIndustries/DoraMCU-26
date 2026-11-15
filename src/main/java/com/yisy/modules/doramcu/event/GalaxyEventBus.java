package com.yisy.modules.doramcu.event;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Interface for distributed galaxy event publishing and subscription.
 * <p>
 * This interface abstracts the event distribution mechanism, allowing
 * different implementations for different deployment scenarios:
 * <ul>
 *   <li>{@code GalaxyEventService} - In-memory, single-process mode (default)</li>
 *   <li>{@code RedisGalaxyEventService} - Redis pub/sub, multi-process mode</li>
 *   <li>{@code KafkaGalaxyEventService} - Kafka topics, production-scale mode</li>
 * </ul>
 * </p>
 * <p>
 * Implementations should be thread-safe and handle failures gracefully.
 * </p>
 */
public interface GalaxyEventBus {

    /**
     * Publishes a galaxy event to all subscribed clients.
     * <p>
     * The event is distributed to all processes/subscribers that are
     * interested in the event's galaxy.
     * </p>
     *
     * @param event the event to publish
     */
    void publish(GalaxyEvent event);

    /**
     * Subscribes a client to galaxy events via SSE.
     * <p>
     * If galaxyNames is null or empty, the client receives events for all galaxies.
     * Otherwise, only events matching the specified galaxy names are delivered.
     * </p>
     *
     * @param clientId unique client identifier
     * @param galaxyNames list of galaxy names to filter by (null = all)
     * @return the registered SseEmitter
     */
    SseEmitter subscribe(String clientId, List<String> galaxyNames);

    /**
     * Subscribes a client to all galaxy events (no filter).
     *
     * @param clientId unique client identifier
     * @return the registered SseEmitter
     */
    default SseEmitter subscribeAll(String clientId) {
        return subscribe(clientId, null);
    }

    /**
     * Subscribes a client to events for a specific galaxy.
     *
     * @param clientId unique client identifier
     * @param galaxyName the galaxy name to filter by
     * @return the registered SseEmitter
     */
    default SseEmitter subscribeGalaxy(String clientId, String galaxyName) {
        return subscribe(clientId, List.of(galaxyName));
    }

    /**
     * Returns the recent event history, optionally filtered by galaxy name.
     *
     * @param galaxyName if non-null, only events for this galaxy
     * @return list of recent events
     */
    List<GalaxyEvent> getEventHistory(String galaxyName);

    /**
     * Returns the total number of connected subscribers.
     *
     * @return subscriber count
     */
    int getSubscriberCount();

    /**
     * Returns the name of the event bus implementation.
     *
     * @return implementation name (e.g., "in-memory", "redis", "kafka")
     */
    default String getImplementationName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns whether the event bus supports cross-process distribution.
     * <p>
     * In-memory implementations return false.
     * Redis and Kafka implementations return true.
     * </p>
     *
     * @return true if cross-process distribution is supported
     */
    default boolean supportsCrossProcess() {
        return false;
    }

    /**
     * Closes the event bus and releases resources.
     */
    default void close() {
        // No-op default
    }
}
