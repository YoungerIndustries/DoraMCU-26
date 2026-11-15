package com.yisy.modules.doramcu.unified;

import java.util.Collections;
import java.util.Map;

/**
 * Represents an edge (connection) between two world nodes.
 * <p>
 * Edges define travel routes, trade routes, diplomatic relations,
 * combat zones, and other connections between locations.
 * </p>
 */
public class WorldEdge {

    public enum EdgeType {
        TRAVEL,
        TRADE,
        DIPLOMATIC,
        COMBAT,
        WARP_GATE,
        HYPERLANE,
        CUSTOM
    }

    private final String id;
    private final String sourceId;
    private final String targetId;
    private final EdgeType type;
    private final double distance;
    private final String description;
    private final Map<String, Object> metadata;

    public WorldEdge(String sourceId, String targetId, EdgeType type,
                     double distance, String description, Map<String, Object> metadata) {
        this.id = sourceId + "-" + targetId;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.type = type;
        this.distance = distance;
        this.description = description;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
    }

    public String getId() { return id; }
    public String getSourceId() { return sourceId; }
    public String getTargetId() { return targetId; }
    public EdgeType getType() { return type; }
    public double getDistance() { return distance; }
    public String getDescription() { return description; }
    public Map<String, Object> getMetadata() { return metadata; }
}
