package com.yisy.modules.doramcu.fullthrust.model;

import java.util.*;

/**
 * Represents the game board - a hex grid where ships move and fight.
 */
public class GameBoard {
    private int width; // Number of hexes in q direction
    private int height; // Number of hexes in r direction
    private Map<String, HexPosition> obstacles; // Asteroid fields, etc.

    public GameBoard() {
        this.width = 40;
        this.height = 40;
        this.obstacles = new HashMap<>();
    }

    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.obstacles = new HashMap<>();
    }

    // Getters and setters
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public Map<String, HexPosition> getObstacles() { return obstacles; }
    public void setObstacles(Map<String, HexPosition> obstacles) { this.obstacles = obstacles; }

    /**
     * Check if a position is within the board
     */
    public boolean isValidPosition(HexPosition pos) {
        return pos.getQ() >= 0 && pos.getQ() < width && pos.getR() >= 0 && pos.getR() < height;
    }

    /**
     * Check if a position has an obstacle
     */
    public boolean hasObstacle(HexPosition pos) {
        return obstacles.values().stream()
                .anyMatch(o -> o.getQ() == pos.getQ() && o.getR() == pos.getR());
    }

    /**
     * Add an obstacle at a position
     */
    public void addObstacle(String name, HexPosition pos) {
        obstacles.put(name, pos);
    }

    /**
     * Generate random asteroid field
     */
    public void generateAsteroidField(int numAsteroids, Random random) {
        for (int i = 0; i < numAsteroids; i++) {
            int q = random.nextInt(width);
            int r = random.nextInt(height);
            HexPosition pos = new HexPosition(q, r);
            addObstacle("asteroid_" + i, pos);
        }
    }
}
