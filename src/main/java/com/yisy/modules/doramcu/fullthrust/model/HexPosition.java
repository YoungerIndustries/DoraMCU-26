package com.yisy.modules.doramcu.fullthrust.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Represents a hex grid position on the game board.
 * Uses axial coordinates (q, r) for hex grid math.
 */
public class HexPosition {
    private int q; // column
    private int r; // row

    public HexPosition() {}

    public HexPosition(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int getQ() { return q; }
    public void setQ(int q) { this.q = q; }
    public int getR() { return r; }
    public void setR(int r) { this.r = r; }

    /**
     * Get the s coordinate (derived from q + r + s = 0)
     */
    public int getS() {
        return -q - r;
    }

    /**
     * Calculate distance to another hex position
     */
    public int distanceTo(HexPosition other) {
        return Math.max(
            Math.abs(this.q - other.q),
            Math.max(Math.abs(this.r - other.r), Math.abs(this.getS() - other.getS()))
        );
    }

    /**
     * Get all 6 neighboring hex positions
     */
    @JsonIgnore
    public List<HexPosition> getNeighbors() {
        // Directions for pointy-top hex grid
        int[] dq = {1, 1, 0, -1, -1, 0};
        int[] dr = {0, 1, 1, 0, -1, -1};
        
        List<HexPosition> neighbors = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            neighbors.add(new HexPosition(q + dq[i], r + dr[i]));
        }
        return neighbors;
    }

    /**
     * Get the direction (0-5) from this hex to another hex using cubical coordinates.
     * Returns the dominant hex direction based on the largest coordinate difference.
     * 0=East, 1=SouthEast, 2=SouthWest, 3=West, 4=NorthWest, 5=NorthEast
     */
    public int directionTo(HexPosition target) {
        int dq = target.q - this.q;
        int dr = target.r - this.r;
        int ds = target.getS() - this.getS();

        // Same position
        if (dq == 0 && dr == 0) return -1;

        // Use cubical coordinates to find the dominant axis
        // The direction is determined by which of |dq|, |dr|, |ds| is largest
        int absDq = Math.abs(dq);
        int absDr = Math.abs(dr);
        int absDs = Math.abs(ds);

        // Find the maximum absolute difference
        if (absDq >= absDr && absDq >= absDs) {
            // Q axis dominates
            return dq > 0 ? 0 : 3; // East or West
        } else if (absDr >= absDq && absDr >= absDs) {
            // R axis dominates
            return dr > 0 ? 2 : 5; // SouthWest or NorthEast
        } else {
            // S axis dominates (s = -q-r)
            return ds > 0 ? 4 : 1; // NorthWest or SouthEast
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HexPosition that = (HexPosition) o;
        return q == that.q && r == that.r;
    }

    @Override
    public int hashCode() {
        return 31 * q + r;
    }

    @Override
    public String toString() {
        return "(" + q + "," + r + ")";
    }
}
