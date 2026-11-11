package com.yisy.modules.doramcu.hex;

/**
 * Combines multiple {@link ShapeDefinition} instances using boolean operations.
 * <p>
 * Supports union, intersection, and difference operations to create complex
 * shapes from simpler ones. For example, an L-shaped building can be created
 * by unioning two rectangular shapes.
 * </p>
 */
public class CompositeShape implements ShapeDefinition {

    private final ShapeDefinition shapeA;
    private final ShapeDefinition shapeB;
    private final Operation operation;

    /**
     * Boolean operation type.
     */
    public enum Operation {
        /** A OR B — cells in either shape */
        UNION,

        /** A AND B — cells in both shapes */
        INTERSECTION,

        /** A NOT B — cells in A but not in B */
        DIFFERENCE
    }

    /**
     * Creates a composite shape from two shapes and an operation.
     *
     * @param shapeA the first shape
     * @param shapeB the second shape
     * @param operation the boolean operation
     */
    public CompositeShape(ShapeDefinition shapeA, ShapeDefinition shapeB, Operation operation) {
        if (shapeA == null || shapeB == null || operation == null) {
            throw new IllegalArgumentException("Shapes and operation must not be null");
        }
        this.shapeA = shapeA;
        this.shapeB = shapeB;
        this.operation = operation;
    }

    /**
     * Creates a union of two shapes (A OR B).
     *
     * @param a the first shape
     * @param b the second shape
     * @return a new composite shape containing cells in either shape
     */
    public static ShapeDefinition union(ShapeDefinition a, ShapeDefinition b) {
        return new CompositeShape(a, b, Operation.UNION);
    }

    /**
     * Creates an intersection of two shapes (A AND B).
     *
     * @param a the first shape
     * @param b the second shape
     * @return a new composite shape containing cells in both shapes
     */
    public static ShapeDefinition intersection(ShapeDefinition a, ShapeDefinition b) {
        return new CompositeShape(a, b, Operation.INTERSECTION);
    }

    /**
     * Creates a difference of two shapes (A NOT B).
     *
     * @param a the first shape
     * @param b the second shape
     * @return a new composite shape containing cells in A but not in B
     */
    public static ShapeDefinition difference(ShapeDefinition a, ShapeDefinition b) {
        return new CompositeShape(a, b, Operation.DIFFERENCE);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        boolean a = shapeA.contains(x, y, z);
        boolean b = shapeB.contains(x, y, z);

        switch (operation) {
            case UNION:
                return a || b;
            case INTERSECTION:
                return a && b;
            case DIFFERENCE:
                return a && !b;
            default:
                return false;
        }
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
        return shapeA.estimatedCellCount() + shapeB.estimatedCellCount();
    }

    @Override
    public String toString() {
        return String.format("CompositeShape{%s %s %s}", shapeA, operation, shapeB);
    }
}
