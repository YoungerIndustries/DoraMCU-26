package com.yisy.modules.doramcu.traveller;

/**
 * Types of Traveller spacecraft for air/space combat.
 */
public enum TravellerSpacecraftType {
    FIGHTER("Fighter", 1, 2, 4, 100),
    BOMBER("Bomber", 2, 1, 2, 200),
    INTERCEPTOR("Interceptor", 1, 3, 5, 150),
    RECONNAISSANCE("Reconnaissance", 1, 1, 3, 80),
    CUSTOM("Custom", 1, 1, 3, 120);

    private final String displayName;
    private final int baseCrew;
    private final int baseArmor;
    private final int baseMovement;
    private final int baseCost;

    TravellerSpacecraftType(String displayName, int baseCrew, int baseArmor, int baseMovement, int baseCost) {
        this.displayName = displayName;
        this.baseCrew = baseCrew;
        this.baseArmor = baseArmor;
        this.baseMovement = baseMovement;
        this.baseCost = baseCost;
    }

    public String getDisplayName() { return displayName; }
    public int getBaseCrew() { return baseCrew; }
    public int getBaseArmor() { return baseArmor; }
    public int getBaseMovement() { return baseMovement; }
    public int getBaseCost() { return baseCost; }
}
