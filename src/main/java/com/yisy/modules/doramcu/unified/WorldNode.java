package com.yisy.modules.doramcu.unified;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a node in the unified world map.
 * <p>
 * Nodes can be locations, factions, planets, star systems, or any other
 * point of interest in the game world. Nodes are connected by edges.
 * </p>
 */
public class WorldNode {

    public enum NodeType {
        LOCATION,
        FACTION,
        PLANET,
        STAR_SYSTEM,
        DUNGEON,
        CITY,
        SPACE_STATION,
        TERRITORY,
        CUSTOM
    }

    private final String id;
    private final String name;
    private final NodeType type;
    private final String module;
    private final double x;
    private final double y;
    private final double z;
    private final String description;
    private boolean discovered;
    private final Map<String, Object> metadata;

    public WorldNode(String id, String name, NodeType type, String module,
                     double x, double y, double z, String description,
                     Map<String, Object> metadata) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.module = module;
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
        this.discovered = false;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public NodeType getType() { return type; }
    public String getModule() { return module; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getDescription() { return description; }
    public boolean isDiscovered() { return discovered; }
    public void setDiscovered(boolean discovered) { this.discovered = discovered; }
    public Map<String, Object> getMetadata() { return metadata; }
}
