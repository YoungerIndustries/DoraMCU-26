package com.yisy.modules.doramcu.hex.planet;

import java.util.Objects;

/**
 * Represents a planet-level game event with metadata and payload.
 * <p>
 * Events are dispatched by {@link PlanetEventManager} to registered listeners.
 * Each event carries:
 * <ul>
 *   <li>The event type ({@link PlanetEventType})</li>
 *   <li>A timestamp (turn number when the event occurred)</li>
 *   <li>An optional source ID (settlement, industry, vehicle, etc.)</li>
 *   <li>An optional payload object with event-specific data</li>
 * </ul>
 */
public class PlanetEvent {

    private final PlanetEventType type;
    private final long timestamp;
    private final String sourceId;
    private final Object payload;

    /**
     * Creates a new planet event.
     *
     * @param type      the event type
     * @param timestamp the turn number when the event occurred
     * @param sourceId  the ID of the source entity, or null
     * @param payload   event-specific data, or null
     */
    public PlanetEvent(PlanetEventType type, long timestamp, String sourceId, Object payload) {
        if (type == null) {
            throw new IllegalArgumentException("Event type must not be null");
        }
        this.type = type;
        this.timestamp = timestamp;
        this.sourceId = sourceId;
        this.payload = payload;
    }

    /**
     * Creates a new planet event without a source.
     *
     * @param type      the event type
     * @param timestamp the turn number
     */
    public PlanetEvent(PlanetEventType type, long timestamp) {
        this(type, timestamp, null, null);
    }

    /** @return the event type */
    public PlanetEventType getType() {
        return type;
    }

    /** @return the turn number when the event occurred */
    public long getTimestamp() {
        return timestamp;
    }

    /** @return the source entity ID, or null */
    public String getSourceId() {
        return sourceId;
    }

    /** @return the event-specific payload, or null */
    public Object getPayload() {
        return payload;
    }

    /**
     * @return true if this event has a source ID
     */
    public boolean hasSource() {
        return sourceId != null;
    }

    /**
     * @return true if this event has a payload
     */
    public boolean hasPayload() {
        return payload != null;
    }

    /**
     * Casts the payload to the specified type.
     *
     * @param clazz the expected payload class
     * @param <T>   the payload type
     * @return the casted payload, or null if payload is null
     * @throws ClassCastException if the payload is not the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T getPayloadAs(Class<T> clazz) {
        if (payload == null) return null;
        return clazz.cast(payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanetEvent that = (PlanetEvent) o;
        return type == that.type && timestamp == that.timestamp &&
                Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, timestamp, sourceId);
    }

    @Override
    public String toString() {
        return String.format("PlanetEvent{type=%s, turn=%d, source=%s}",
                type, timestamp, sourceId);
    }
}
