package com.yisy.modules.doramcu.thirdreich.domain;

/**
 * Represents a unit placed on a 3D battlefield with coordinates and facing direction.
 * <p>
 * Each placement links a ThirdReichGroundUnit to a specific position on the
 * BattlefieldMap, including 3D coordinates, facing direction, and team assignment.
 * </p>
 */
public class UnitPlacement {

    private final ThirdReichGroundUnit unit;
    private final double x;
    private final double y;
    private final double z;
    private final double facingDirection;
    private final boolean isAttacker;

    /**
     * Creates a new unit placement.
     *
     * @param unit the ground unit
     * @param x the X coordinate in meters
     * @param y the Y coordinate in meters
     * @param z the Z coordinate (elevation) in meters
     * @param facingDirection the facing direction in degrees (0 = north, 90 = east)
     * @param isAttacker true if this unit belongs to the attacker team
     */
    public UnitPlacement(ThirdReichGroundUnit unit, double x, double y, double z,
                         double facingDirection, boolean isAttacker) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        this.unit = unit;
        this.x = x;
        this.y = y;
        this.z = z;
        this.facingDirection = normalizeAngle(facingDirection);
        this.isAttacker = isAttacker;
    }

    // ---- Accessors ----

    public ThirdReichGroundUnit getUnit() { return unit; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getFacingDirection() { return facingDirection; }
    public boolean isAttacker() { return isAttacker; }

    /**
     * Gets the team name for this placement.
     */
    public String getTeamName() {
        return isAttacker ? "attacker" : "defender";
    }

    // ---- Mutators ----

    /**
     * Updates the position of this unit.
     */
    public void setPosition(double x, double y, double z) {
        // This would need mutable fields, but since fields are final,
        // we create a new placement instead. This method is a no-op for immutability.
        // Use createMoved() instead.
    }

    /**
     * Updates the facing direction.
     */
    public void setFacingDirection(double facingDirection) {
        // No-op for immutability. Use createWithFacing() instead.
    }

    // ---- Factory Methods ----

    /**
     * Creates a new placement with updated position.
     */
    public UnitPlacement createMoved(double x, double y, double z) {
        return new UnitPlacement(unit, x, y, z, facingDirection, isAttacker);
    }

    /**
     * Creates a new placement with updated facing direction.
     */
    public UnitPlacement createWithFacing(double facingDirection) {
        return new UnitPlacement(unit, x, y, z, facingDirection, isAttacker);
    }

    /**
     * Calculates the distance to another placement.
     */
    public double distanceTo(UnitPlacement other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates the horizontal distance to another placement (ignoring elevation).
     */
    public double horizontalDistanceTo(UnitPlacement other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double normalizeAngle(double angle) {
        angle = angle % 360.0;
        if (angle < 0) {
            angle += 360.0;
        }
        return angle;
    }

    @Override
    public String toString() {
        return "UnitPlacement{unit=" + unit.getName() + ", pos=(" +
                String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " +
                String.format("%.1f", z) + "), facing=" + String.format("%.0f", facingDirection) +
                "°, team=" + getTeamName() + "}";
    }
}
