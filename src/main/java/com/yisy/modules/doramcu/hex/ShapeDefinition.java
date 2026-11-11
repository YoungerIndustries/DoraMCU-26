package com.yisy.modules.doramcu.hex;

/**
 * Defines an arbitrary 3D shape for a {@link HullModule}.
 * <p>
 * This interface allows custom shapes beyond the predefined {@link HullShape} enum.
 * Implementations can represent arbitrary building footprints, complex ship hulls,
 * or any other 3D structure.
 * </p>
 * <p>
 * Shapes support boolean operations via {@link CompositeShape} for combining
 * multiple shapes into complex structures.
 * </p>
 */
public interface ShapeDefinition {

    /**
     * Checks whether the given cell coordinates fall within this shape.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the cell is part of this shape
     */
    boolean contains(int x, int y, int z);

    /**
     * Checks whether the given cell is on the exterior surface of this shape.
     * A cell is exterior if it is within the shape and at least one of its 6 face neighbors
     * (±X, ±Y, ±Z) is outside the shape.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the cell is part of the shape and on its exterior surface
     */
    boolean isExterior(int x, int y, int z);

    /**
     * Checks whether the given cell is an interior cell of this shape.
     * A cell is interior if it is within the shape and all 6 face neighbors
     * (±X, ±Y, ±Z) are also within the shape.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the cell is part of the shape and surrounded on all 6 face sides
     */
    boolean isInterior(int x, int y, int z);

    /**
     * Returns an estimate of the number of cells included in this shape.
     *
     * @return the approximate cell count
     */
    default int estimatedCellCount() {
        // Default implementation — subclasses may override for accuracy
        return 0;
    }

    /**
     * Returns a default implementation of isExterior based on contains().
     * Subclasses that override contains() can use this directly.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the cell is exterior
     */
    default boolean defaultIsExterior(int x, int y, int z) {
        if (!contains(x, y, z)) {
            return false;
        }
        if (!contains(x - 1, y, z)) return true;
        if (!contains(x + 1, y, z)) return true;
        if (!contains(x, y - 1, z)) return true;
        if (!contains(x, y + 1, z)) return true;
        if (!contains(x, y, z - 1)) return true;
        if (!contains(x, y, z + 1)) return true;
        return false;
    }

    /**
     * Returns a default implementation of isInterior based on contains().
     * Subclasses that override contains() can use this directly.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the cell is interior
     */
    default boolean defaultIsInterior(int x, int y, int z) {
        if (!contains(x, y, z)) {
            return false;
        }
        return contains(x - 1, y, z) && contains(x + 1, y, z) &&
               contains(x, y - 1, z) && contains(x, y + 1, z) &&
               contains(x, y, z - 1) && contains(x, y, z + 1);
    }
}
