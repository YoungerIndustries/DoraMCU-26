package com.yisy.modules.doramcu.hex.planet.fps.dto;

import java.util.Map;

/**
 * StarBase state for the FPS 3D renderer.
 * <p>
 * Represents a space station or orbital facility with modules, dock slots, and defense rating.
 * </p>
 */
public class FpsStarBaseStateDto {

    private final String id;
    private final String name;
    private final String starBaseType; // "OUTPOST", "STATION", "FORTRESS", "MEGASTRUCTURE"
    private final int x, y, z;
    private final int moduleCount;
    private final int dockSlots;
    private final int shipsDocked;
    private final int defenseRating;
    private final int currentHitPoints;
    private final int maxHitPoints;
    private final String ownerId;
    private final int planetId;
    private final boolean active;

    public FpsStarBaseStateDto(String id, String name, String starBaseType,
                                int x, int y, int z,
                                int moduleCount, int dockSlots, int shipsDocked,
                                int defenseRating, int currentHitPoints, int maxHitPoints,
                                String ownerId, int planetId, boolean active) {
        this.id = id;
        this.name = name;
        this.starBaseType = starBaseType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.moduleCount = moduleCount;
        this.dockSlots = dockSlots;
        this.shipsDocked = shipsDocked;
        this.defenseRating = defenseRating;
        this.currentHitPoints = currentHitPoints;
        this.maxHitPoints = maxHitPoints;
        this.ownerId = ownerId;
        this.planetId = planetId;
        this.active = active;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStarBaseType() { return starBaseType; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getModuleCount() { return moduleCount; }
    public int getDockSlots() { return dockSlots; }
    public int getShipsDocked() { return shipsDocked; }
    public int getDefenseRating() { return defenseRating; }
    public int getCurrentHitPoints() { return currentHitPoints; }
    public int getMaxHitPoints() { return maxHitPoints; }
    public String getOwnerId() { return ownerId; }
    public int getPlanetId() { return planetId; }
    public boolean isActive() { return active; }

    /** Returns the hit point ratio (0.0 to 1.0). */
    public double getHitPointRatio() {
        return maxHitPoints > 0 ? (double) currentHitPoints / maxHitPoints : 0.0;
    }

    /** Returns the available dock slots. */
    public int getAvailableDockSlots() {
        return dockSlots - shipsDocked;
    }
}
