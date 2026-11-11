package com.yisy.modules.doramcu.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a single galaxy lifecycle event.
 * <p>
 * Events are published by various services (BackgroundGalaxyBuilder,
 * GalaxyOverseerService, Earth2026Service) and broadcast via SSE to
 * connected admin dashboard clients.
 * </p>
 */
public class GalaxyEvent {

    private final GalaxyEventType type;
    private final String galaxyName;
    private final Long galaxyId;
    private final Instant timestamp;
    private final Map<String, Object> payload;

    /**
     * Creates a new galaxy event.
     *
     * @param type the event type
     * @param galaxyName the galaxy this event relates to
     * @param galaxyId the galaxy's database ID (may be null)
     * @param payload additional event-specific data (immutable map)
     */
    public GalaxyEvent(GalaxyEventType type, String galaxyName, Long galaxyId, Map<String, Object> payload) {
        this.type = type;
        this.galaxyName = galaxyName;
        this.galaxyId = galaxyId;
        this.timestamp = Instant.now();
        this.payload = payload != null ? Collections.unmodifiableMap(payload) : Collections.emptyMap();
    }

    /**
     * Convenience constructor with no payload.
     */
    public GalaxyEvent(GalaxyEventType type, String galaxyName, Long galaxyId) {
        this(type, galaxyName, galaxyId, null);
    }

    // -- Getters --

    public GalaxyEventType getType() {
        return type;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public Long getGalaxyId() {
        return galaxyId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    /**
     * Returns the payload value for the given key, or null if not present.
     */
    public Object getPayloadValue(String key) {
        return payload.get(key);
    }

    @Override
    public String toString() {
        return "GalaxyEvent{" +
                "type=" + type +
                ", galaxyName='" + galaxyName + '\'' +
                ", galaxyId=" + galaxyId +
                ", timestamp=" + timestamp +
                '}';
    }
}
