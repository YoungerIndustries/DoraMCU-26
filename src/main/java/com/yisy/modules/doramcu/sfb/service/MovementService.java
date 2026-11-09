package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbHexPosition;
import com.yisy.modules.doramcu.sfb.model.SfbShip;

/**
 * Handles proportional movement on the hex grid.
 * <p>
 * Manages speed calculation, turn modes, HET, sideslip, and acceleration.
 * </p>
 */
public class MovementService {

    /**
     * Move a ship by one hex in its current facing direction.
     */
    public void moveOneHex(SfbShip ship) {
        ship.move(1);
    }

    /**
     * Move a ship by the given number of hexes.
     */
    public void move(SfbShip ship, int hexes) {
        ship.move(hexes);
    }

    /**
     * Turn the ship by the given number of directions.
     * Positive = left, negative = right.
     */
    public void turn(SfbShip ship, int directions) {
        ship.turn(directions);
    }

    /**
     * Calculate the maximum hexes a ship can move based on its turn mode.
     * Turn mode 1: full movement
     * Turn mode 2: reduced movement for turning
     * Turn mode 3: minimal movement for sharp turns
     */
    public int getMaxMoveHexes(SfbShip ship) {
        int baseHexes = ship.getSpeed() / 4;
        switch (ship.getTurnMode()) {
            case 1: return baseHexes;
            case 2: return Math.max(1, baseHexes / 2);
            case 3: return 1;
            default: return baseHexes;
        }
    }

    /**
     * Calculate the turning allowance based on speed and turn mode.
     */
    public int getTurningAllowance(SfbShip ship) {
        int speed = ship.getSpeed();
        int turnMode = ship.getTurnMode();

        // Simplified turning rules
        if (turnMode == 1) {
            return speed >= 24 ? 1 : (speed >= 12 ? 2 : 3);
        } else if (turnMode == 2) {
            return speed >= 16 ? 2 : (speed >= 8 ? 3 : 4);
        } else {
            return 4;
        }
    }

    /**
     * Check if a ship can perform a High Energy Turn (HET).
     * HET requires speed 16+ and costs extra energy.
     */
    public boolean canPerformHET(SfbShip ship) {
        return ship.getSpeed() >= 16 && ship.isInCombat();
    }

    /**
     * Calculate the distance between two ships.
     */
    public int distanceBetween(SfbShip ship1, SfbShip ship2) {
        if (ship1.getPosition() == null || ship2.getPosition() == null) return 0;
        return ship1.getPosition().distanceTo(ship2.getPosition());
    }

    /**
     * Calculate the range band between two ships.
     * Range bands: 0=point blank, 1=1-2 hexes, 2=3-5 hexes, 3=6-8 hexes,
     * 4=9-12 hexes, 5=13-15 hexes, 6=16+ hexes
     */
    public int getRangeBand(SfbShip ship1, SfbShip ship2) {
        int distance = distanceBetween(ship1, ship2);
        if (distance == 0) return 0;
        if (distance <= 2) return 1;
        if (distance <= 5) return 2;
        if (distance <= 8) return 3;
        if (distance <= 12) return 4;
        if (distance <= 15) return 5;
        return 6;
    }

    /**
     * Get the direction from one ship to another.
     */
    public int getDirectionTo(SfbShip from, SfbShip to) {
        if (from.getPosition() == null || to.getPosition() == null) return 0;
        return from.getPosition().relativeDirection(to.getPosition());
    }
}
