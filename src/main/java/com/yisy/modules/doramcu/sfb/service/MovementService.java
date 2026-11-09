package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbHexPosition;
import com.yisy.modules.doramcu.sfb.model.SfbShip;

/**
 * Handles proportional movement on the hex grid.
 * <p>
 * Manages speed calculation, turn modes, HET, sideslip, and acceleration.
 * Uses the ImpulseManager to determine which impulses a ship moves on based on speed.
 * Each impulse move is 1 hex, so total hexes per turn equals the number of movement impulses.
 * </p>
 */
public class MovementService {

    private final ImpulseManager impulseManager;

    public MovementService() {
        this.impulseManager = new ImpulseManager();
    }

    /**
     * Constructor with injected ImpulseManager (for Spring/SFB config).
     */
    public MovementService(ImpulseManager impulseManager) {
        this.impulseManager = impulseManager;
    }

    /**
     * Move a ship by one hex in its current facing direction.
     * Records the hex moved for tracking purposes.
     */
    public void moveOneHex(SfbShip ship) {
        ship.move(1);
        ship.recordHexMoved();
    }

    /**
     * Move a ship by the given number of hexes.
     * Records each hex moved for tracking purposes.
     */
    public void move(SfbShip ship, int hexes) {
        ship.move(hexes);
        for (int i = 0; i < hexes; i++) {
            ship.recordHexMoved();
        }
    }

    /**
     * Move a ship based on its speed and the current impulse.
     * This is the main method used by the combat engine for proportional movement.
     * In SFB, each impulse move is 1 hex. The ship's speed determines which impulses
     * it moves on, so total hexes per turn equals the number of movement impulses.
     *
     * @param ship The ship to move
     * @param impulse The current impulse (1-32)
     * @return true if the ship moved this impulse, false otherwise
     */
    public boolean moveShip(SfbShip ship, int impulse) {
        if (!ship.isInCombat() || ship.getPosition() == null) {
            return false;
        }

        // Check if this ship moves on this impulse based on speed
        if (!impulseManager.movesOnImpulse(ship.getSpeed(), impulse)) {
            return false;
        }

        // Move 1 hex in current facing direction
        ship.move(1);
        ship.recordHexMoved();
        return true;
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
        int baseHexes = impulseManager.getHexesPerTurn(ship.getSpeed());
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
     * Validate and apply a speed change, respecting acceleration limits.
     * SFB rules: max 4 speed points change per turn.
     *
     * @param ship The ship changing speed
     * @param newSpeed The desired new speed
     * @return The actual speed set (may be clamped by acceleration limits)
     */
    public int setSpeedWithAcceleration(SfbShip ship, int newSpeed) {
        int validatedSpeed = impulseManager.validateSpeedChange(ship.getPreviousSpeed(), newSpeed);
        ship.setSpeed(validatedSpeed);
        return validatedSpeed;
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

    /**
     * Get the total hexes a ship can move per turn at its current speed.
     */
    public int getHexesPerTurn(SfbShip ship) {
        return impulseManager.getHexesPerTurn(ship.getSpeed());
    }

    /**
     * Get the ImpulseManager for access to movement chart data.
     */
    public ImpulseManager getImpulseManager() {
        return impulseManager;
    }
}
