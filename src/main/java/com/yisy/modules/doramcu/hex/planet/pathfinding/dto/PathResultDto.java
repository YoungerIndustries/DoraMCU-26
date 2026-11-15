package com.yisy.modules.doramcu.hex.planet.pathfinding.dto;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a pathfinding query between two stations.
 * <p>
 * Contains the path as a list of hex coordinates, the total distance,
 * and whether a valid path was found.
 * </p>
 */
public class PathResultDto {

    private final boolean pathFound;
    private final List<Hex3D> path;
    private final double totalCost;
    private final String message;

    /**
     * Creates a successful path result.
     *
     * @param path the list of hex coordinates from start to destination
     * @param totalCost the total movement cost
     */
    public PathResultDto(List<Hex3D> path, double totalCost) {
        this.pathFound = path != null && !path.isEmpty();
        this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
        this.totalCost = totalCost;
        this.message = this.pathFound ?
                String.format("Path found with %d steps, cost %.1f", this.path.size(), totalCost) :
                "No path found";
    }

    /**
     * Creates a failure result when no path exists.
     *
     * @param message description of why no path was found
     */
    public PathResultDto(String message) {
        this.pathFound = false;
        this.path = new ArrayList<>();
        this.totalCost = -1;
        this.message = message;
    }

    /** @return true if a valid path was found */
    public boolean isPathFound() {
        return pathFound;
    }

    /** @return the path as a list of hex coordinates */
    public List<Hex3D> getPath() {
        return Collections.unmodifiableList(path);
    }

    /** @return the total movement cost of the path */
    public double getTotalCost() {
        return totalCost;
    }

    /** @return a human-readable message about the result */
    public String getMessage() {
        return message;
    }

    /** @return the number of steps (hexes) in the path */
    public int getStepCount() {
        return path.size();
    }

    @Override
    public String toString() {
        return String.format("PathResultDto{found=%s, steps=%d, cost=%.1f}",
                pathFound, path.size(), totalCost);
    }
}
