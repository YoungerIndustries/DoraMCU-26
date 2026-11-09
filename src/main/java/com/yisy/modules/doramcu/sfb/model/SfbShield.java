package com.yisy.modules.doramcu.sfb.model;

/**
 * Represents one of the 6 directional shields on an SFB ship.
 * <p>
 * Each shield has boxes that can be marked out, and can be reinforced
 * to absorb more damage. Shields are identified by direction:
 * Front-Right (0), Front-Left (1), Left (2), Right (3), Rear-Left (4), Rear-Right (5).
 * </p>
 */
public class SfbShield {

    private final int direction;
    private final int totalBoxes;
    private int boxesRemaining;
    private int reinforcementPoints;

    public SfbShield(int direction, int totalBoxes) {
        this.direction = direction;
        this.totalBoxes = totalBoxes;
        this.boxesRemaining = totalBoxes;
        this.reinforcementPoints = 0;
    }

    public int getDirection() { return direction; }
    public int getTotalBoxes() { return totalBoxes; }
    public int getBoxesRemaining() { return boxesRemaining; }
    public int getReinforcementPoints() { return reinforcementPoints; }

    /**
     * Absorb damage from this shield. Returns the amount of damage that penetrates.
     * Each point of damage removes one box (or reinforcement point).
     */
    public int absorbDamage(int damage) {
        int absorbed = 0;

        // First use reinforcement points
        while (damage > 0 && reinforcementPoints > 0) {
            reinforcementPoints--;
            damage--;
            absorbed++;
        }

        // Then use shield boxes
        while (damage > 0 && boxesRemaining > 0) {
            boxesRemaining--;
            damage--;
            absorbed++;
        }

        return damage; // remaining damage penetrates
    }

    /**
     * Reinforce the shield with the given number of points.
     */
    public void reinforce(int points) {
        this.reinforcementPoints += points;
    }

    /**
     * Repair shield boxes via DamCon.
     */
    public void repair(int boxes) {
        this.boxesRemaining = Math.min(totalBoxes, boxesRemaining + boxes);
    }

    /**
     * Check if the shield is down (no boxes remaining).
     */
    public boolean isDown() {
        return boxesRemaining <= 0 && reinforcementPoints <= 0;
    }

    /**
     * Get the total protection value (boxes + reinforcement).
     */
    public int getTotalProtection() {
        return boxesRemaining + reinforcementPoints;
    }

    @Override
    public String toString() {
        return "SfbShield{dir=" + direction + ", boxes=" + boxesRemaining + "/" + totalBoxes +
                ", reinforcement=" + reinforcementPoints + "}";
    }
}
