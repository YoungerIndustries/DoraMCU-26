package com.yisy.modules.doramcu.earth2026.model;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Grid-based terrain elevation map for 3D battlefield representation.
 * <p>
 * Stores height data, slope angles, and vegetation density for a rectangular
 * area of the battlefield. Each cell represents a fixed-size grid square
 * (e.g., 10m x 10m) with elevation in meters above sea level.
 * </p>
 * <p>
 * This is a value object, not a JPA entity — it is assembled on demand from
 * Earth2026Location elevation data and generated height maps.
 * </p>
 */
public class TerrainElevationMap {

    /** Grid width in cells. */
    private final int width;

    /** Grid height in cells. */
    private final int height;

    /** Cell size in meters (e.g., 10.0 means each cell is 10m x 10m). */
    private final double cellSizeMeters;

    /** Base elevation of the location in meters above sea level. */
    private final double baseElevation;

    /** 2D elevation grid: elevations[row][col] in meters above base elevation. */
    private final double[][] elevations;

    /** Vegetation density per cell (0.0 = bare, 1.0 = dense). */
    private final double[][] vegetationDensity;

    /** Water mask per cell (true = water, false = land). */
    private final boolean[][] isWater;

    /**
     * Creates a new terrain elevation map.
     *
     * @param width the grid width in cells
     * @param height the grid height in cells
     * @param cellSizeMeters the size of each cell in meters
     * @param baseElevation the base elevation in meters above sea level
     */
    public TerrainElevationMap(int width, int height, double cellSizeMeters, double baseElevation) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        if (cellSizeMeters <= 0) {
            throw new IllegalArgumentException("Cell size must be positive");
        }
        this.width = width;
        this.height = height;
        this.cellSizeMeters = cellSizeMeters;
        this.baseElevation = baseElevation;
        this.elevations = new double[height][width];
        this.vegetationDensity = new double[height][width];
        this.isWater = new boolean[height][width];
    }

    // ---- Accessors ----

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getCellSizeMeters() { return cellSizeMeters; }
    public double getBaseElevation() { return baseElevation; }

    /**
     * Gets the elevation at a specific grid position relative to base elevation.
     *
     * @param row the row index
     * @param col the column index
     * @return elevation in meters above base elevation
     */
    public double getElevation(int row, int col) {
        return elevations[row][col];
    }

    /**
     * Gets the absolute elevation at a specific grid position.
     *
     * @param row the row index
     * @param col the column index
     * @return absolute elevation in meters above sea level
     */
    public double getAbsoluteElevation(int row, int col) {
        return baseElevation + elevations[row][col];
    }

    /**
     * Gets the vegetation density at a specific grid position.
     *
     * @param row the row index
     * @param col the column index
     * @return vegetation density (0.0 = bare, 1.0 = dense)
     */
    public double getVegetationDensity(int row, int col) {
        return vegetationDensity[row][col];
    }

    /**
     * Checks if a grid cell is water.
     *
     * @param row the row index
     * @param col the column index
     * @return true if the cell is water
     */
    public boolean isWater(int row, int col) {
        return isWater[row][col];
    }

    /**
     * Sets the elevation at a specific grid position.
     *
     * @param row the row index
     * @param col the column index
     * @param elevation elevation in meters above base elevation
     */
    public void setElevation(int row, int col, double elevation) {
        elevations[row][col] = elevation;
    }

    /**
     * Sets the vegetation density at a specific grid position.
     *
     * @param row the row index
     * @param col the column index
     * @param density vegetation density (0.0 = bare, 1.0 = dense)
     */
    public void setVegetationDensity(int row, int col, double density) {
        vegetationDensity[row][col] = Math.max(0.0, Math.min(1.0, density));
    }

    /**
     * Sets whether a grid cell is water.
     *
     * @param row the row index
     * @param col the column index
     * @param water true if the cell is water
     */
    public void setWater(int row, int col, boolean water) {
        isWater[row][col] = water;
    }

    // ---- Calculations ----

    /**
     * Calculates the slope angle at a grid cell in degrees.
     * Uses the central difference method for interior cells.
     *
     * @param row the row index
     * @param col the column index
     * @return slope angle in degrees (0 = flat, 90 = vertical)
     */
    public double getSlopeAngle(int row, int col) {
        double dx = 0.0;
        double dy = 0.0;

        if (col > 0 && col < width - 1) {
            dx = elevations[row][col + 1] - elevations[row][col - 1];
        } else if (col > 0) {
            dx = elevations[row][col] - elevations[row][col - 1];
        } else if (col < width - 1) {
            dx = elevations[row][col + 1] - elevations[row][col];
        }

        if (row > 0 && row < height - 1) {
            dy = elevations[row + 1][col] - elevations[row - 1][col];
        } else if (row > 0) {
            dy = elevations[row][col] - elevations[row - 1][col];
        } else if (row < height - 1) {
            dy = elevations[row + 1][col] - elevations[row][col];
        }

        double distance = cellSizeMeters * 2.0;
        double slope = Math.sqrt(dx * dx + dy * dy) / distance;
        return Math.toDegrees(Math.atan(slope));
    }

    /**
     * Gets the elevation at world coordinates (interpolated from grid).
     *
     * @param worldX world X coordinate in meters
     * @param worldY world Y coordinate in meters
     * @return interpolated elevation in meters above base elevation
     */
    public double getElevationAtWorldCoords(double worldX, double worldY) {
        double colF = worldX / cellSizeMeters;
        double rowF = worldY / cellSizeMeters;

        int col0 = (int) Math.floor(colF);
        int row0 = (int) Math.floor(rowF);
        int col1 = Math.min(col0 + 1, width - 1);
        int row1 = Math.min(row0 + 1, height - 1);

        col0 = Math.max(0, col0);
        row0 = Math.max(0, row0);

        double fx = colF - col0;
        double fy = rowF - row0;

        double z00 = elevations[row0][col0];
        double z10 = elevations[row0][col1];
        double z01 = elevations[row1][col0];
        double z11 = elevations[row1][col1];

        double z0 = z00 * (1 - fx) + z10 * fx;
        double z1 = z01 * (1 - fx) + z11 * fx;
        return z0 * (1 - fy) + z1 * fy;
    }

    // ---- Factory Methods ----

    /**
     * Generates a random terrain elevation map based on terrain type.
     *
     * @param width the grid width in cells
     * @param height the grid height in cells
     * @param cellSizeMeters the cell size in meters
     * @param baseElevation the base elevation
     * @param terrainType the terrain type string
     * @param vegetationDensity overall vegetation density
     * @return a generated terrain elevation map
     */
    public static TerrainElevationMap generateRandom(
            int width, int height, double cellSizeMeters, double baseElevation,
            String terrainType, double vegetationDensity) {
        TerrainElevationMap map = new TerrainElevationMap(width, height, cellSizeMeters, baseElevation);
        Random random = new Random(42); // Fixed seed for reproducibility

        double maxElevationVariation;
        double waterProbability;

        switch (terrainType != null ? terrainType.toUpperCase() : "URBAN") {
            case "PLAINS":
                maxElevationVariation = 5.0;
                waterProbability = 0.02;
                break;
            case "MOUNTAIN":
                maxElevationVariation = 100.0;
                waterProbability = 0.01;
                break;
            case "JUNGLE":
                maxElevationVariation = 15.0;
                waterProbability = 0.05;
                vegetationDensity = Math.max(vegetationDensity, 0.7);
                break;
            case "URBAN":
                maxElevationVariation = 10.0;
                waterProbability = 0.03;
                break;
            case "DESERT":
                maxElevationVariation = 20.0;
                waterProbability = 0.0;
                vegetationDensity = 0.0;
                break;
            case "ARCTIC":
                maxElevationVariation = 30.0;
                waterProbability = 0.1;
                vegetationDensity = 0.0;
                break;
            case "FOREST":
                maxElevationVariation = 20.0;
                waterProbability = 0.03;
                vegetationDensity = Math.max(vegetationDensity, 0.5);
                break;
            case "SWAMP":
                maxElevationVariation = 3.0;
                waterProbability = 0.3;
                vegetationDensity = Math.max(vegetationDensity, 0.4);
                break;
            default:
                maxElevationVariation = 10.0;
                waterProbability = 0.02;
                break;
        }

        // Generate elevation using simple noise
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // Simple multi-octave noise approximation
                double elevation = 0;
                elevation += (random.nextDouble() - 0.5) * maxElevationVariation;
                elevation += (random.nextDouble() - 0.5) * maxElevationVariation * 0.5;
                elevation += (random.nextDouble() - 0.5) * maxElevationVariation * 0.25;
                map.elevations[row][col] = elevation;

                // Vegetation with some variation
                map.vegetationDensity[row][col] = Math.max(0.0, Math.min(1.0,
                        vegetationDensity + (random.nextDouble() - 0.5) * 0.3));

                // Water cells
                map.isWater[row][col] = random.nextDouble() < waterProbability;
                if (map.isWater[row][col]) {
                    map.elevations[row][col] = Math.min(map.elevations[row][col], -1.0);
                    map.vegetationDensity[row][col] = 0.0;
                }
            }
        }

        return map;
    }

    /**
     * Creates a flat terrain map (all elevations at 0).
     */
    public static TerrainElevationMap createFlat(int width, int height, double cellSizeMeters, double baseElevation) {
        return generateRandom(width, height, cellSizeMeters, baseElevation, "PLAINS", 0.0);
    }

    @Override
    public String toString() {
        return "TerrainElevationMap{width=" + width + ", height=" + height +
                ", cellSize=" + cellSizeMeters + "m, baseElevation=" + baseElevation + "m}";
    }
}
