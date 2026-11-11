package com.yisy.modules.doramcu.hex;

import java.util.*;

/**
 * Defines a custom shape from an explicit set of {@link M3space} cells.
 * <p>
 * This implementation allows arbitrary shapes to be defined by specifying
 * exactly which cells are part of the shape. The exterior/interior distinction
 * is computed automatically based on face-neighbor relationships.
 * </p>
 * <p>
 * Useful for defining irregular building footprints, custom ship hulls,
 * or any shape that cannot be expressed by the predefined {@link HullShape} enum.
 * </p>
 */
public class MaskShape implements ShapeDefinition {

    private final Set<String> cells;
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    /**
     * Creates a MaskShape from a set of M3space cells.
     *
     * @param cellSet the cells that form the shape
     */
    public MaskShape(Set<M3space> cellSet) {
        if (cellSet == null || cellSet.isEmpty()) {
            throw new IllegalArgumentException("Cell set must not be null or empty");
        }
        this.cells = new HashSet<>(cellSet.size());
        int loX = Integer.MAX_VALUE, hiX = Integer.MIN_VALUE;
        int loY = Integer.MAX_VALUE, hiY = Integer.MIN_VALUE;
        int loZ = Integer.MAX_VALUE, hiZ = Integer.MIN_VALUE;

        for (M3space c : cellSet) {
            cells.add(key(c.getX(), c.getY(), c.getZ()));
            loX = Math.min(loX, c.getX());
            hiX = Math.max(hiX, c.getX());
            loY = Math.min(loY, c.getY());
            hiY = Math.max(hiY, c.getY());
            loZ = Math.min(loZ, c.getZ());
            hiZ = Math.max(hiZ, c.getZ());
        }
        this.minX = loX;
        this.maxX = hiX;
        this.minY = loY;
        this.maxY = hiY;
        this.minZ = loZ;
        this.maxZ = hiZ;
    }

    /**
     * Creates a MaskShape from a rectangular block.
     *
     * @param xMin minimum x (inclusive)
     * @param xMax maximum x (inclusive)
     * @param yMin minimum y (inclusive)
     * @param yMax maximum y (inclusive)
     * @param zMin minimum z (inclusive)
     * @param zMax maximum z (inclusive)
     */
    public MaskShape(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax) {
        this.cells = new HashSet<>();
        this.minX = xMin;
        this.maxX = xMax;
        this.minY = yMin;
        this.maxY = yMax;
        this.minZ = zMin;
        this.maxZ = zMax;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    cells.add(key(x, y, z));
                }
            }
        }
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return cells.contains(key(x, y, z));
    }

    @Override
    public boolean isExterior(int x, int y, int z) {
        return defaultIsExterior(x, y, z);
    }

    @Override
    public boolean isInterior(int x, int y, int z) {
        return defaultIsInterior(x, y, z);
    }

    @Override
    public int estimatedCellCount() {
        return cells.size();
    }

    /**
     * @return the number of cells in this shape
     */
    public int cellCount() {
        return cells.size();
    }

    /** @return minimum X coordinate */
    public int getMinX() { return minX; }
    /** @return maximum X coordinate */
    public int getMaxX() { return maxX; }
    /** @return minimum Y coordinate */
    public int getMinY() { return minY; }
    /** @return maximum Y coordinate */
    public int getMaxY() { return maxY; }
    /** @return minimum Z coordinate */
    public int getMinZ() { return minZ; }
    /** @return maximum Z coordinate */
    public int getMaxZ() { return maxZ; }

    /**
     * Returns all cells in this shape as M3space objects.
     *
     * @return list of cells
     */
    public List<M3space> getCells() {
        List<M3space> result = new ArrayList<>(cells.size());
        for (String k : cells) {
            String[] parts = k.split(",");
            result.add(new M3space(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
            ));
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("MaskShape{cells=%d, bounds=(%d-%d,%d-%d,%d-%d)}",
                cells.size(), minX, maxX, minY, maxY, minZ, maxZ);
    }
}
