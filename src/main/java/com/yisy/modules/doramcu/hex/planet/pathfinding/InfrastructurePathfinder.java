package com.yisy.modules.doramcu.hex.planet.pathfinding;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.TerrainData;
import com.yisy.modules.doramcu.hex.planet.VehicleType;
import com.yisy.modules.doramcu.hex.planet.pathfinding.dto.PathResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Finds shortest paths between stations along the infrastructure network.
 * <p>
 * Uses Dijkstra's algorithm on the {@link InfrastructureGraph} to find
 * the lowest-cost path between two stations. The path respects vehicle
 * infrastructure requirements (e.g., trains need rail, trucks need road).
 * </p>
 * <p>
 * The pathfinder:
 * <ul>
 *   <li>Builds the graph from terrain data</li>
 *   <li>Finds paths between any two hex coordinates</li>
 *   <li>Validates that a vehicle type can travel the path</li>
 *   <li>Returns the path as a list of {@link Hex3D} coordinates</li>
 * </ul>
 * </p>
 */
@Service
public class InfrastructurePathfinder {

    private static final Logger log = LoggerFactory.getLogger(InfrastructurePathfinder.class);

    /** Planet ID -> infrastructure graph */
    private final Map<Integer, InfrastructureGraph> planetGraphs;

    public InfrastructurePathfinder() {
        this.planetGraphs = new HashMap<>();
    }

    /**
     * Builds the infrastructure graph for a planet from its terrain map.
     *
     * @param planetId the planet ID
     * @param terrainMap hex key -> TerrainData mapping
     */
    public void buildGraph(int planetId, Map<String, TerrainData> terrainMap) {
        InfrastructureGraph graph = new InfrastructureGraph();
        graph.build(terrainMap);
        planetGraphs.put(planetId, graph);
        log.info("Built infrastructure graph for planet {}: {}", planetId, graph);
    }

    /**
     * Updates the graph when infrastructure changes at a hex.
     *
     * @param planetId the planet ID
     * @param hex the hex that changed
     * @param infraType the new infrastructure type
     */
    public void updateNode(int planetId, Hex3D hex, TerrainData.InfrastructureType infraType) {
        InfrastructureGraph graph = planetGraphs.get(planetId);
        if (graph == null) {
            log.warn("No graph for planet {}", planetId);
            return;
        }
        if (infraType == TerrainData.InfrastructureType.NONE) {
            graph.removeNode(hex);
        } else {
            graph.addNode(hex, infraType);
        }
    }

    /**
     * Finds the shortest path between two stations.
     *
     * @param planetId the planet ID
     * @param startStation the starting station
     * @param endStation the destination station
     * @return path result with the list of hex coordinates
     */
    public PathResultDto findPath(int planetId, Station startStation, Station endStation) {
        return findPath(planetId, startStation.getPosition(), endStation.getPosition(), null);
    }

    /**
     * Finds the shortest path between two stations, validating vehicle type.
     *
     * @param planetId the planet ID
     * @param startStation the starting station
     * @param endStation the destination station
     * @param vehicleType the vehicle type (optional, for infrastructure validation)
     * @return path result with the list of hex coordinates
     */
    public PathResultDto findPath(int planetId, Station startStation, Station endStation,
                                   VehicleType vehicleType) {
        return findPath(planetId, startStation.getPosition(), endStation.getPosition(), vehicleType);
    }

    /**
     * Finds the shortest path between two hex coordinates.
     *
     * @param planetId the planet ID
     * @param start the starting hex
     * @param end the destination hex
     * @param vehicleType the vehicle type (optional, for infrastructure validation)
     * @return path result with the list of hex coordinates
     */
    public PathResultDto findPath(int planetId, Hex3D start, Hex3D end,
                                   VehicleType vehicleType) {
        InfrastructureGraph graph = planetGraphs.get(planetId);
        if (graph == null) {
            return new PathResultDto("No infrastructure graph for planet " + planetId);
        }

        // Check if start and end are in the graph
        if (!graph.contains(start)) {
            return new PathResultDto("Start hex " + start + " has no infrastructure");
        }
        if (!graph.contains(end)) {
            return new PathResultDto("End hex " + end + " has no infrastructure");
        }

        // Run Dijkstra's algorithm
        List<Hex3D> path = dijkstra(graph, start, end, vehicleType);
        if (path == null || path.isEmpty()) {
            return new PathResultDto("No path between " + start + " and " + end);
        }

        // Calculate total cost
        double totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalCost += graph.getEdgeCost(path.get(i), path.get(i + 1));
        }

        return new PathResultDto(path, totalCost);
    }

    /**
     * Dijkstra's algorithm to find the shortest path.
     *
     * @param graph the infrastructure graph
     * @param start the starting hex
     * @param end the destination hex
     * @param vehicleType optional vehicle type for infrastructure validation
     * @return the path as a list of hexes, or null if no path exists
     */
    private List<Hex3D> dijkstra(InfrastructureGraph graph, Hex3D start, Hex3D end,
                                  VehicleType vehicleType) {
        // Distance map: hex -> minimum cost from start
        Map<Hex3D, Double> dist = new HashMap<>();
        // Previous map: hex -> previous hex in the path
        Map<Hex3D, Hex3D> prev = new HashMap<>();
        // Visited set
        Set<Hex3D> visited = new HashSet<>();
        // Priority queue: (cost, hex)
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(Node::getCost));

        dist.put(start, 0.0);
        queue.add(new Node(start, 0.0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Hex3D u = current.getHex();

            if (visited.contains(u)) continue;
            visited.add(u);

            // Reached the destination
            if (u.equals(end)) {
                return reconstructPath(prev, start, end);
            }

            double currentDist = dist.getOrDefault(u, Double.MAX_VALUE);

            for (Hex3D v : graph.getNeighbors(u)) {
                if (visited.contains(v)) continue;

                // Check infrastructure compatibility for vehicle type
                if (vehicleType != null) {
                    TerrainData.InfrastructureType infra = graph.getInfrastructure(v);
                    if (!isInfrastructureCompatible(vehicleType, infra)) {
                        continue;
                    }
                }

                double edgeCost = graph.getEdgeCost(u, v);
                double newDist = currentDist + edgeCost;

                if (newDist < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    queue.add(new Node(v, newDist));
                }
            }
        }

        // No path found
        return null;
    }

    /**
     * Checks if a vehicle type can use the given infrastructure.
     *
     * @param vehicleType the vehicle type
     * @param infra the infrastructure type
     * @return true if the vehicle can use this infrastructure
     */
    private boolean isInfrastructureCompatible(VehicleType vehicleType,
                                                TerrainData.InfrastructureType infra) {
        if (infra == null) return false;

        VehicleType.InfrastructureRequirement req = vehicleType.getRequiredInfrastructure();

        // STATION is always accessible (loading/unloading point)
        if (infra == TerrainData.InfrastructureType.STATION) return true;

        switch (req) {
            case ROAD:
                return infra == TerrainData.InfrastructureType.ROAD ||
                        infra == TerrainData.InfrastructureType.STATION;
            case RAIL:
                return infra == TerrainData.InfrastructureType.RAIL ||
                        infra == TerrainData.InfrastructureType.STATION;
            case WATER:
                return infra == TerrainData.InfrastructureType.PORT ||
                        infra == TerrainData.InfrastructureType.STATION;
            case AIRPORT:
                return infra == TerrainData.InfrastructureType.AIRPORT ||
                        infra == TerrainData.InfrastructureType.STATION;
            case NONE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Reconstructs the path from the previous map.
     *
     * @param prev the previous map from Dijkstra
     * @param start the starting hex
     * @param end the destination hex
     * @return the path from start to end
     */
    private List<Hex3D> reconstructPath(Map<Hex3D, Hex3D> prev, Hex3D start, Hex3D end) {
        List<Hex3D> path = new ArrayList<>();
        Hex3D current = end;

        while (current != null) {
            path.add(current);
            if (current.equals(start)) break;
            current = prev.get(current);
        }

        Collections.reverse(path);

        // Verify the path starts at the start hex
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return null;
        }

        return path;
    }

    /**
     * Finds all stations reachable from a given station.
     *
     * @param planetId the planet ID
     * @param startStation the starting station
     * @param allStations all stations on the planet
     * @return list of reachable stations
     */
    public List<Station> findReachableStations(int planetId, Station startStation,
                                                 List<Station> allStations) {
        InfrastructureGraph graph = planetGraphs.get(planetId);
        if (graph == null || !graph.contains(startStation.getPosition())) {
            return Collections.emptyList();
        }

        // BFS from start
        Set<Hex3D> visited = new HashSet<>();
        Queue<Hex3D> queue = new LinkedList<>();
        queue.add(startStation.getPosition());
        visited.add(startStation.getPosition());

        while (!queue.isEmpty()) {
            Hex3D current = queue.poll();
            for (Hex3D neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Filter stations whose position is in the visited set
        return allStations.stream()
                .filter(s -> visited.contains(s.getPosition()))
                .collect(Collectors.toList());
    }

    /**
     * Inner class for priority queue nodes.
     */
    private static class Node {
        private final Hex3D hex;
        private final double cost;

        Node(Hex3D hex, double cost) {
            this.hex = hex;
            this.cost = cost;
        }

        Hex3D getHex() {
            return hex;
        }

        double getCost() {
            return cost;
        }
    }
}
