package com.yisy.modules.doramcu.hex;

/**
 * Priority levels for incremental galaxy sector generation.
 * <p>
 * When a galaxy is being built progressively, sectors are generated in priority
 * order so that players can join the Earth hub immediately while the rest of the
 * galaxy continues building in the background.
 * </p>
 * <p>
 * Priority order (highest to lowest):
 * <ol>
 *   <li>EARTH_HUB — The center sector (5,5) containing Earth, the character creation hub</li>
 *   <li>CORE_SYSTEMS — Sectors adjacent to the center (ring 1, sectors at distance 1 from center)</li>
 *   <li>SPIRAL_ARMS — Mid-range sectors (ring 2-3, sectors at distance 2-3 from center)</li>
 *   <li>FRONTIER — Edge sectors (ring 4, sectors at distance 4+ from center, outermost)</li>
 * </ol>
 * </p>
 */
public enum GalaxyBuildPriority {

    /**
     * The center sector (5,5) containing Earth — the character creation hub.
     * Generated FIRST so players can arrive immediately.
     */
    EARTH_HUB,

    /**
     * Sectors adjacent to the center (ring 1).
     * Generated SECOND to provide immediate expansion area around Earth.
     */
    CORE_SYSTEMS,

    /**
     * Mid-range sectors (ring 2-3).
     * Generated THIRD to fill out the spiral arms of the galaxy.
     */
    SPIRAL_ARMS,

    /**
     * Edge sectors (ring 4+, outermost).
     * Generated LAST as background work — frontier exploration zones.
     */
    FRONTIER;

    /**
     * Returns the build priority for a sector at the given grid coordinates.
     * <p>
     * The priority is determined by the Chebyshev distance from the center (5,5):
     * <ul>
     *   <li>Distance 0 → EARTH_HUB (center sector only)</li>
     *   <li>Distance 1 → CORE_SYSTEMS (8 adjacent sectors)</li>
     *   <li>Distance 2-3 → SPIRAL_ARMS</li>
     *   <li>Distance 4+ → FRONTIER</li>
     * </ul>
     * </p>
     *
     * @param row the row coordinate (0-9)
     * @param col the column coordinate (0-9)
     * @return the build priority for this sector position
     */
    public static GalaxyBuildPriority forSector(int row, int col) {
        int centerRow = Galaxy.GRID_SIZE / 2; // 5
        int centerCol = Galaxy.GRID_SIZE / 2; // 5
        int distance = Math.max(Math.abs(row - centerRow), Math.abs(col - centerCol));

        if (distance == 0) {
            return EARTH_HUB;
        } else if (distance == 1) {
            return CORE_SYSTEMS;
        } else if (distance <= 3) {
            return SPIRAL_ARMS;
        } else {
            return FRONTIER;
        }
    }

    /**
     * Returns the Chebyshev distance from the center for sorting sectors within
     * the same priority level.
     *
     * @param row the row coordinate (0-9)
     * @param col the column coordinate (0-9)
     * @return distance from center (5,5)
     */
    public static int distanceFromCenter(int row, int col) {
        int centerRow = Galaxy.GRID_SIZE / 2;
        int centerCol = Galaxy.GRID_SIZE / 2;
        return Math.max(Math.abs(row - centerRow), Math.abs(col - centerCol));
    }
}
