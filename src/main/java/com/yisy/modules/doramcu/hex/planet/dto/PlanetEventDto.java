package com.yisy.modules.doramcu.hex.planet.dto;

/**
 * DTO for a planet-level game event.
 * <p>
 * Used by the event history API and SSE event streaming endpoints.
 * </p>
 */
public class PlanetEventDto {

    private String type;
    private long timestamp;
    private String sourceId;
    private Object payload;

    public PlanetEventDto() {
    }

    public PlanetEventDto(String type, long timestamp, String sourceId, Object payload) {
        this.type = type;
        this.timestamp = timestamp;
        this.sourceId = sourceId;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
