package com.yisy.modules.doramcu.hex.planet.pathfinding;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.TerrainData;

import java.util.*;

/**
 * Graph representation of connected infrastructure hexes on a planet surface.
 * <p>
 * The graph is built from {@link TerrainData} cells that have ROAD or RAIL
 * infrastructure. Each node is a {@link Hex3D} coordinate, and edges connect
 * adjacent hexes that share compatible infrastructure types.
 * </p>
 * <p>
 * The graph supports:
 * <ul>
 *   <li>Building from a terrain map</li>
 *   <li>Incremental updates when infrastructure changes</li>
 *   <li>Querying neighbors for pathfinding algorithms</li>
 * </ul>
 */
public class InfrastructureGraph {

    /** Hex -> list of connected neighbor hexes */
    private final Map<Hex3D, List<Hex3D>> adjacency;

    /** Hex -> infrastructure type at that node */
    private final Map<Hex3D, TerrainData.InfrastructureType> nodeInfrastructure;

    /**
     * Creates an empty infrastructure graph.
     */
    public InfrastructureGraph() {
        this.adjacency = new HashMap<>();
        this.nodeInfrastructure = new HashMap<>();
    }

    /**
     * Builds the graph from a terrain map.
     * <p>
     * Only hexes with ROAD, RAIL, or STATION infrastructure are included.
     * Two adjacent hexes are connected if both have compatible infrastructure.
     * </p>
     *
     * @param terrainMap hex key -> TerrainData mapping
     */
    public void build(Map<String, TerrainData> terrainMap) {
        adjacency.clear();
        nodeInfrastructure.clear();

        // Collect all infrastructure hexes
        Map<Hex3D, TerrainData> infraHexes = new HashMap<>();
        for (Map.Entry<String, TerrainData> entry : terrainMap.entrySet()) {
            TerrainData terrain = entry.getValue();
            TerrainData.InfrastructureType infra = terrain.getInfrastructure();
            if (infra == TerrainData.InfrastructureType.ROAD ||
                    infra == TerrainData.InfrastructureType.RAIL ||
                    infra == TerrainData.InfrastructureType.STATION) {
                // Parse hex key back to Hex3D
                Hex3D hex = parseHexKey(entry.getKey());
                if (hex != null) {
                    infraHexes.put(hex, terrain);
                    nodeInfrastructure.put(hex, infra);
                }
            }
        }

        // Build adjacency list
        for (Hex3D hex : infraHexes.keySet()) {
            List<Hex3D> neighbors = new ArrayList<>();
            for (Hex3D neighbor : hex.neighbors()) {
                if (infraHexes.containsKey(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
            adjacency.put(hex, neighbors);
        }
    }

    /**
     * Adds or updates a single node in the graph.
     *
     * @param hex the hex coordinate
     * @param infraType the infrastructure type
     */
    public void addNode(Hex3D hex, TerrainData.InfrastructureType infraType) {
        nodeInfrastructure.put(hex, infraType);
        rebuildAdjacencyFor(hex);
    }

    /**
     * Removes a node from the graph.
     *
     * @param hex the hex coordinate to remove
     */
    public void removeNode(Hex3D hex) {
        nodeInfrastructure.remove(hex);
        adjacency.remove(hex);
        // Remove from all neighbors' adjacency lists
        for (List<Hex3D> neighbors : adjacency.values()) {
            neighbors.remove(hex);
        }
    }

    /**
     * Returns the neighbors of a hex in the infrastructure graph.
     *
     * @param hex the hex coordinate
     * @return list of connected neighbors, or empty list if not in graph
     */
    public List<Hex3D> getNeighbors(Hex3D hex) {
        return Collections.unmodifiableList(adjacency.getOrDefault(hex, Collections.emptyList()));
    }

    /**
     * Returns the infrastructure type at a hex.
     *
     * @param hex the hex coordinate
     * @return the infrastructure type, or null if not in graph
     */
    public TerrainData.InfrastructureType getInfrastructure(Hex3D hex) {
        return nodeInfrastructure.get(hex);
    }

    /**
     * Checks if a hex is in the graph.
     *
     * @param hex the hex coordinate
     * @return true if the hex is a node in the graph
     */
    public boolean contains(Hex3D hex) {
        return nodeInfrastructure.containsKey(hex);
    }

    /**
     * Returns the number of nodes in the graph.
     */
    public int size() {
        return nodeInfrastructure.size();
    }

    /**
     * Returns all nodes in the graph.
     */
    public Set<Hex3D> nodes() {
        return Collections.unmodifiableSet(nodeInfrastructure.keySet());
    }

    /**
     * Gets the movement cost for traversing from one hex to another.
     *
     * @param from the source hex
     * @param to the destination hex
     * @return the movement cost, or Double.MAX_VALUE if not connected
     */
    public double getEdgeCost(Hex3D from, Hex3D to) {
        TerrainData.InfrastructureType fromInfra = nodeInfrastructure.get(from);
        TerrainData.InfrastructureType toInfra = nodeInfrastructure.get(to);
        if (fromInfra == null || toInfra == null) {
            return Double.MAX_VALUE;
        }
        // Base cost is 1.0 per hex step
        double cost = 1.0;
        // Rail is faster
        if (fromInfra == TerrainData.InfrastructureType.RAIL &&
                toInfra == TerrainData.InfrastructureType.RAIL) {
            cost = 0.5;
        }
        // Road is moderate
        if (fromInfra == TerrainData.InfrastructureType.ROAD &&
                toInfra == TerrainData.InfrastructureType.ROAD) {
            cost = 0.7;
        }
        return cost;
    }

    /**
     * Rebuilds the adjacency list for a single hex and its neighbors.
     *
     * @param hex the hex to rebuild
     */
    private void rebuildAdjacencyFor(Hex3D hex) {
        List<Hex3D> neighbors = new ArrayList<>();
        for (Hex3D neighbor : hex.neighbors()) {
            if (nodeInfrastructure.containsKey(neighbor)) {
                neighbors.add(neighbor);
            }
        }
        adjacency.put(hex, neighbors);

        // Update neighbors' adjacency lists
        for (Hex3D neighbor : neighbors) {
            List<Hex3D> neighborList = adjacency.computeIfAbsent(neighbor, k -> new ArrayList<>());
            if (!neighborList.contains(hex)) {
                neighborList.add(hex);
            }
        }
    }

    /**
     * Parses a hex key string back to a Hex3D object.
     * Expected format: "x,y,z"
     *
     * @param key the hex key string
     * @return the Hex3D, or null if parsing fails
     */
    private Hex3D parseHexKey(String key) {
        try {
            String[] parts = key.split(",");
            if (parts.length == 3) {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());
                return new Hex3D(x, y, z);
            }
        } catch (NumberFormatException ignored) {
            // Fall through
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("InfrastructureGraph{nodes=%d}", nodeInfrastructure.size());
    }
}
