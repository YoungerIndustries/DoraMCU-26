package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a 3D map of {@link M3space} cells arranged in a 10×10×10 volume.
 * <p>
 * A {@code HullModule} is a fixed-size 3D hex map used to model a ship hull module
 * or any bounded 3D space. It contains 1000 M3space cells arranged in a 10×10×10 cube,
 * providing O(1) lookup by coordinates.
 * <p>
 * The grid coordinates range from (0, 0, 0) to (9, 9, 9), covering all three axes.
 *
 * <pre>
 *   Z (depth)
 *   |
 *   |  Layer 9  +------------------+
 *   |           | .  .  .  .  .  . |
 *   |  Layer 8  | .  .  .  .  .  . |
 *   |           | ... (10 layers)  |
 *   |  Layer 1  | .  .  .  .  .  . |
 *   |           | .  .  .  .  .  . |
 *   |  Layer 0  +------------------+
 *   |           0  1  2  3  4  5  6  7  8  9  -> X
 *   +-> Y
 * </pre>
 */
public class HullModule {

    /** Default grid dimension (backward compatibility constant) */
    public static final int DEFAULT_WIDTH = 10;
    /** Default grid dimension (backward compatibility constant) */
    public static final int DEFAULT_HEIGHT = 10;
    /** Default grid dimension (backward compatibility constant) */
    public static final int DEFAULT_DEPTH = 10;
    /** Default total size (backward compatibility constant) */
    public static final int DEFAULT_SIZE = DEFAULT_WIDTH * DEFAULT_HEIGHT * DEFAULT_DEPTH;

    // Backward compatibility aliases — deprecated, use instance getters instead
    @Deprecated
    public static final int WIDTH = DEFAULT_WIDTH;
    @Deprecated
    public static final int HEIGHT = DEFAULT_HEIGHT;
    @Deprecated
    public static final int DEPTH = DEFAULT_DEPTH;
    @Deprecated
    public static final int SIZE = DEFAULT_SIZE;

    private final Map<M3space, M3space> cells;
    private final String name;
    private HullShape shape;
    private ShapeDefinition customShape;  // Optional custom shape overriding HullShape enum
    private final int width;
    private final int height;
    private final int depth;
    private final List<Compartment> compartments;  // Named functional compartments

    /**
     * Creates a new HullModule with the given name, initialized with a 10×10×10 grid of M3space cells.
     * Default shape is CUBE (all cells included).
     *
     * @param name the name of this hull module
     */
    public HullModule(String name) {
        this(name, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DEPTH);
    }

    /**
     * Creates a new unnamed HullModule with a 10×10×10 grid of M3space cells.
     */
    public HullModule() {
        this("HullModule");
    }

    /**
     * Creates a new HullModule with custom grid dimensions.
     *
     * @param name the name of this hull module
     * @param width the grid width (X axis)
     * @param height the grid height (Y axis)
     * @param depth the grid depth (Z axis)
     * @throws IllegalArgumentException if any dimension is less than 1
     */
    public HullModule(String name, int width, int height, int depth) {
        if (width < 1 || height < 1 || depth < 1) {
            throw new IllegalArgumentException("Dimensions must be at least 1: " + width + "x" + height + "x" + depth);
        }
        this.name = name;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.cells = new HashMap<>();
        this.shape = HullShape.CUBE;
        this.customShape = null;
        this.compartments = new ArrayList<>();
        initializeGrid();
    }

    /**
     * Initializes the grid of M3space cells with the configured dimensions.
     * Exterior cells (on the shape's surface) default to Aluminum (material 1).
     * Interior cells (fully surrounded within the shape) default to Empty (material 0).
     * Cells outside the shape default to Empty.
     */
    private void initializeGrid() {
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Material material;
                    if (activeShape().contains(x, y, z)) {
                        if (activeShape().isExterior(x, y, z)) {
                            material = Material.Aluminum;
                        } else {
                            material = Material.Empty;
                        }
                    } else {
                        material = Material.Empty;
                    }
                    M3space cell = new M3space(x, y, z, material);
                    cells.put(cell, cell);
                }
            }
        }
    }

    /**
     * Returns the active shape definition: customShape if set, otherwise the HullShape enum.
     */
    private ShapeDefinition activeShape() {
        return customShape != null ? customShape : shape;
    }

    // ---- Name accessors ----

    /** @return the name of this hull module */
    public String getName() {
        return name;
    }

    // ---- Grid dimensions ----

    /** @return the width of the grid (X axis) */
    public int getWidth() {
        return width;
    }

    /** @return the height of the grid (Y axis) */
    public int getHeight() {
        return height;
    }

    /** @return the depth of the grid (Z axis) */
    public int getDepth() {
        return depth;
    }

    /** @return the total number of cells in the grid */
    public int getTotalCells() {
        return width * height * depth;
    }

    // ---- Shape operations ----

    /**
     * Returns the current shape of this hull module.
     *
     * @return the current shape (default is CUBE)
     */
    public HullShape getShape() {
        return shape;
    }

    /**
     * Sets the shape of this hull module using a predefined {@link HullShape} enum.
     * This clears any custom shape that was previously set.
     *
     * @param newShape the shape to apply
     */
    public void setShape(HullShape newShape) {
        if (newShape == null) {
            throw new IllegalArgumentException("Shape must not be null");
        }
        this.shape = newShape;
        this.customShape = null;
    }

    /**
     * Sets a custom shape definition for this hull module.
     * <p>
     * This allows arbitrary shapes (e.g., {@link MaskShape}, {@link CompositeShape})
     * to be used instead of the predefined {@link HullShape} enum values.
     * The custom shape takes precedence over the HullShape enum when querying
     * shape cells, exterior cells, and interior cells.
     * </p>
     *
     * @param newShape the custom shape definition to apply
     */
    public void setShape(ShapeDefinition newShape) {
        if (newShape == null) {
            throw new IllegalArgumentException("Shape must not be null");
        }
        this.customShape = newShape;
    }

    /**
     * Checks whether the cell at the given coordinates is within the current shape.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return true if the cell is within the current shape boundaries
     */
    public boolean isShapeCell(int x, int y, int z) {
        return activeShape().contains(x, y, z);
    }

    /**
     * Checks whether the given cell is within the current shape.
     *
     * @param space the cell to check
     * @return true if the cell is within the current shape boundaries
     */
    public boolean isShapeCell(M3space space) {
        return isShapeCell(space.getX(), space.getY(), space.getZ());
    }

    /**
     * Returns all cells that are within the current shape.
     *
     * @return list of cells inside the shape boundaries
     */
    public List<M3space> getShapeCells() {
        ShapeDefinition active = activeShape();
        return cells.values().stream()
                .filter(cell -> active.contains(cell.getX(), cell.getY(), cell.getZ()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of cells within the current shape.
     *
     * @return count of cells inside the shape boundaries
     */
    public int getShapeCellCount() {
        ShapeDefinition active = activeShape();
        return (int) cells.values().stream()
                .filter(cell -> active.contains(cell.getX(), cell.getY(), cell.getZ()))
                .count();
    }

    // ---- Exterior / Interior operations ----

    /**
     * Checks whether the cell at the given coordinates is on the exterior surface of the current shape.
     * A cell is exterior if it is within the shape and at least one of its 6 face neighbors is outside the shape.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return true if the cell is on the exterior surface of the current shape
     */
    public boolean isExteriorCell(int x, int y, int z) {
        return activeShape().isExterior(x, y, z);
    }

    /**
     * Checks whether the given cell is on the exterior surface of the current shape.
     *
     * @param space the cell to check
     * @return true if the cell is on the exterior surface of the current shape
     */
    public boolean isExteriorCell(M3space space) {
        return isExteriorCell(space.getX(), space.getY(), space.getZ());
    }

    /**
     * Checks whether the cell at the given coordinates is an interior cell of the current shape.
     * A cell is interior if it is within the shape and all 6 face neighbors are also within the shape.
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @param z the z coordinate (0-9)
     * @return true if the cell is an interior cell of the current shape
     */
    public boolean isInteriorCell(int x, int y, int z) {
        return activeShape().isInterior(x, y, z);
    }

    /**
     * Checks whether the given cell is an interior cell of the current shape.
     *
     * @param space the cell to check
     * @return true if the cell is an interior cell of the current shape
     */
    public boolean isInteriorCell(M3space space) {
        return isInteriorCell(space.getX(), space.getY(), space.getZ());
    }

    /**
     * Returns all cells on the exterior surface of the current shape.
     *
     * @return list of exterior cells
     */
    public List<M3space> getExteriorCells() {
        ShapeDefinition active = activeShape();
        return cells.values().stream()
                .filter(cell -> active.isExterior(cell.getX(), cell.getY(), cell.getZ()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of exterior cells in the current shape.
     *
     * @return count of exterior cells
     */
    public int getExteriorCellCount() {
        ShapeDefinition active = activeShape();
        return (int) cells.values().stream()
                .filter(cell -> active.isExterior(cell.getX(), cell.getY(), cell.getZ()))
                .count();
    }

    /**
     * Returns all interior cells of the current shape.
     *
     * @return list of interior cells
     */
    public List<M3space> getInteriorCells() {
        ShapeDefinition active = activeShape();
        return cells.values().stream()
                .filter(cell -> active.isInterior(cell.getX(), cell.getY(), cell.getZ()))
                .collect(Collectors.toList());
    }

    // ---- CRUD operations ----

    /**
     * Retrieves an M3space cell by its 3D coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the cell at the given position, or null if out of bounds
     */
    public M3space get(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return null;
        }
        return cells.get(new M3space(x, y, z));
    }

    /**
     * Retrieves an M3space cell by its 2D coordinates on the Z=0 layer (backward compatibility).
     *
     * @param x the x coordinate (0-9)
     * @param y the y coordinate (0-9)
     * @return the cell at the given position on layer 0, or null if out of bounds
     */
    public M3space get(int x, int y) {
        return get(x, y, 0);
    }

    /**
     * Retrieves an M3space cell by another M3space instance.
     *
     * @param space the space to look up
     * @return the stored cell, or null if not present
     */
    public M3space get(M3space space) {
        return cells.get(space);
    }

    /**
     * @return true if the grid contains a cell at the given 3D coordinates
     */
    public boolean contains(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth
                && cells.containsKey(new M3space(x, y, z));
    }

    /**
     * @return true if the grid contains a cell at the given 2D coordinates on layer Z=0
     */
    public boolean contains(int x, int y) {
        return contains(x, y, 0);
    }

    /**
     * @return all cells in the grid
     */
    public Collection<M3space> getAll() {
        return Collections.unmodifiableCollection(cells.values());
    }

    /**
     * @return the number of cells in the grid (always 1000)
     */
    public int size() {
        return cells.size();
    }

    // ---- Material operations ----

    /**
     * Changes the material of all exterior cells (on the shape's surface) to the specified material.
     * Exterior cells are those within the shape that have at least one face neighbor outside the shape.
     *
     * @param material the new material for exterior cells
     */
    public void setExteriorMaterial(Material material) {
        for (M3space cell : getExteriorCells()) {
            cell.setMaterial(material);
        }
    }

    /**
     * Changes the material of all interior cells to the specified material.
     * Interior cells are those within the shape that are fully surrounded by other shape cells.
     *
     * @param material the new material for interior cells
     */
    public void setInteriorMaterial(Material material) {
        for (M3space cell : getInteriorCells()) {
            cell.setMaterial(material);
        }
    }

    // ---- Compartment management ----

    /**
     * Adds a named compartment to this hull module.
     *
     * @param compartment the compartment to add
     */
    public void addCompartment(Compartment compartment) {
        if (compartment == null) {
            throw new IllegalArgumentException("Compartment must not be null");
        }
        compartments.add(compartment);
    }

    /**
     * Removes a compartment from this hull module.
     *
     * @param compartment the compartment to remove
     * @return true if removed
     */
    public boolean removeCompartment(Compartment compartment) {
        return compartments.remove(compartment);
    }

    /**
     * @return unmodifiable list of all compartments in this module
     */
    public List<Compartment> getCompartments() {
        return List.copyOf(compartments);
    }

    /**
     * Returns the compartment containing the given cell, or null if none.
     *
     * @param cell the cell to look up
     * @return the compartment containing this cell, or null
     */
    public Compartment getCompartmentAt(M3space cell) {
        if (cell == null) return null;
        for (Compartment compartment : compartments) {
            if (compartment.getCells().contains(cell)) {
                return compartment;
            }
        }
        return null;
    }

    /**
     * Alias for {@link #get(int, int, int)} — provides a clearer name for cell lookup.
     */
    public M3space getCell(int x, int y, int z) {
        return get(x, y, z);
    }

    // ---- Object contract ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HullModule that = (HullModule) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("HullModule{name='%s', size=%d}", name, cells.size());
    }
}
