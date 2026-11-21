package com.yisy.modules.doramcu.hex.planet.fps.dto;
/**
 * Military unit state for the FPS 3D renderer.
 * <p>
 * Represents ground, naval, or space military units with combat stats and position.
 * </p>
 */
public class FpsMilitaryUnitStateDto {

    private final String id;
    private final String name;
    private final String unitType; // "INFANTRY", "TANK", "ARTILLERY", "DESTROYER", "FRIGATE", etc.
    private final String branch; // "ARMY", "NAVY", "SPACE_NAVY"
    private final int x, y, z; // Hex position
    private final int strength; // Current strength (0-100)
    private final int maxStrength;
    private final int morale; // Current morale (0-100)
    private final int attackValue;
    private final int defenseValue;
    private final String factionId; // Owning faction/player
    private final boolean active;
    private final boolean inCombat;

    public FpsMilitaryUnitStateDto(String id, String name, String unitType, String branch,
                                    int x, int y, int z,
                                    int strength, int maxStrength,
                                    int morale, int attackValue, int defenseValue,
                                    String factionId, boolean active, boolean inCombat) {
        this.id = id;
        this.name = name;
        this.unitType = unitType;
        this.branch = branch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.maxStrength = maxStrength;
        this.morale = morale;
        this.attackValue = attackValue;
        this.defenseValue = defenseValue;
        this.factionId = factionId;
        this.active = active;
        this.inCombat = inCombat;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUnitType() { return unitType; }
    public String getBranch() { return branch; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getStrength() { return strength; }
    public int getMaxStrength() { return maxStrength; }
    public int getMorale() { return morale; }
    public int getAttackValue() { return attackValue; }
    public int getDefenseValue() { return defenseValue; }
    public String getFactionId() { return factionId; }
    public boolean isActive() { return active; }
    public boolean isInCombat() { return inCombat; }

    /** Returns the strength ratio (0.0 to 1.0). */
    public double getStrengthRatio() {
        return maxStrength > 0 ? (double) strength / maxStrength : 0.0;
    }
}
