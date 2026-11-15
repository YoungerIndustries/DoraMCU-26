package com.yisy.modules.doramcu.hex.planet.fps.dto;

/**
 * Ship state for the FPS 3D renderer.
 * <p>
 * Represents a space ship with position, type, hull points, and combat stats.
 * </p>
 */
public class FpsShipStateDto {

    private final String id;
    private final String name;
    private final String shipType; // "FRIGATE", "DESTROYER", "CRUISER", "BATTLESHIP", "CARRIER", "MOTHERSHIP"
    private final int x, y, z;
    private final int currentHullPoints;
    private final int maxHullPoints;
    private final int currentShieldPoints;
    private final int maxShieldPoints;
    private final int totalDamage;
    private final int mDriveSpeed;
    private final int jDriveSpeed;
    private final double evasion;
    private final String ownerId;
    private final boolean active;

    public FpsShipStateDto(String id, String name, String shipType,
                            int x, int y, int z,
                            int currentHullPoints, int maxHullPoints,
                            int currentShieldPoints, int maxShieldPoints,
                            int totalDamage, int mDriveSpeed, int jDriveSpeed,
                            double evasion, String ownerId, boolean active) {
        this.id = id;
        this.name = name;
        this.shipType = shipType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.currentHullPoints = currentHullPoints;
        this.maxHullPoints = maxHullPoints;
        this.currentShieldPoints = currentShieldPoints;
        this.maxShieldPoints = maxShieldPoints;
        this.totalDamage = totalDamage;
        this.mDriveSpeed = mDriveSpeed;
        this.jDriveSpeed = jDriveSpeed;
        this.evasion = evasion;
        this.ownerId = ownerId;
        this.active = active;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getShipType() { return shipType; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getCurrentHullPoints() { return currentHullPoints; }
    public int getMaxHullPoints() { return maxHullPoints; }
    public int getCurrentShieldPoints() { return currentShieldPoints; }
    public int getMaxShieldPoints() { return maxShieldPoints; }
    public int getTotalDamage() { return totalDamage; }
    public int getMDriveSpeed() { return mDriveSpeed; }
    public int getJDriveSpeed() { return jDriveSpeed; }
    public double getEvasion() { return evasion; }
    public String getOwnerId() { return ownerId; }
    public boolean isActive() { return active; }

    /** Returns the hull point ratio (0.0 to 1.0). */
    public double getHullRatio() {
        return maxHullPoints > 0 ? (double) currentHullPoints / maxHullPoints : 0.0;
    }

    /** Returns the shield point ratio (0.0 to 1.0). */
    public double getShieldRatio() {
        return maxShieldPoints > 0 ? (double) currentShieldPoints / maxShieldPoints : 0.0;
    }
}
