package com.yisy.modules.doramcu.sfb.model;

/**
 * Represents a position on the hex grid used in Star Fleet Battles.
 * <p>
 * Uses axial coordinates (q, r) for hex grid math. Facing is 0-5,
 * where 0 = Right, 1 = Right-Front, 2 = Left-Front, 3 = Left,
 * 4 = Left-Rear, 5 = Right-Rear.
 * </p>
 */
public class SfbHexPosition {

    private int q;
    private int r;
    private int facing;

    public SfbHexPosition() {
        this.q = 0;
        this.r = 0;
        this.facing = 0;
    }

    public SfbHexPosition(int q, int r, int facing) {
        this.q = q;
        this.r = r;
        this.facing = facing % 6;
    }

    public int getQ() { return q; }
    public void setQ(int q) { this.q = q; }

    public int getR() { return r; }
    public void setR(int r) { this.r = r; }

    public int getFacing() { return facing; }
    public void setFacing(int facing) { this.facing = facing % 6; }

    /**
     * Calculate the distance in hexes between this position and another.
     * Uses axial coordinate distance formula.
     */
    public int distanceTo(SfbHexPosition other) {
        return (Math.abs(this.q - other.q) + Math.abs(this.q + this.r - other.q - other.r) + Math.abs(this.r - other.r)) / 2;
    }

    /**
     * Calculate the relative direction from this ship to the target position.
     * Returns 0-5 corresponding to the hex directions.
     */
    public int relativeDirection(SfbHexPosition target) {
        int dq = target.q - this.q;
        int dr = target.r - this.r;

        if (dq > 0 && dr == 0) return 0; // Right
        if (dq > 0 && dr < 0) return 1; // Right-Front
        if (dq <= 0 && dr < 0) return 2; // Left-Front
        if (dq < 0 && dr == 0) return 3; // Left
        if (dq <= 0 && dr > 0) return 4; // Left-Rear
        return 5; // Right-Rear
    }

    /**
     * Move this position by the given number of hexes in the current facing direction.
     */
    public void move(int hexes) {
        switch (facing) {
            case 0: q += hexes; break; // Right
            case 1: q += hexes; r -= hexes; break; // Right-Front
            case 2: r -= hexes; break; // Left-Front
            case 3: q -= hexes; break; // Left
            case 4: q -= hexes; r += hexes; break; // Left-Rear
            case 5: r += hexes; break; // Right-Rear
        }
    }

    /**
     * Turn the ship by the given number of directions (positive = left, negative = right).
     */
    public void turn(int directions) {
        facing = (facing + directions + 6) % 6;
    }

    @Override
    public String toString() {
        return "SfbHexPosition{q=" + q + ", r=" + r + ", facing=" + facing + "}";
    }
}
