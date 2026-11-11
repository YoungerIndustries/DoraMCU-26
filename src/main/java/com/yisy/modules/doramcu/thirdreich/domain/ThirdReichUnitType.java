package com.yisy.modules.doramcu.thirdreich.domain;

/**
 * Types of ground units in the Third Reich (3R4) ground combat engine.
 * <p>
 * Based on the Avalon Hill "Rise and Decline of the Third Reich" board game unit types,
 * adapted for use as a generic ground combat system.
 * </p>
 * <p>
 * Each unit type has base attack and defense values used in Combat Results Table (CRT)
 * resolution, as well as mobility and supply characteristics.
 * </p>
 */
public enum ThirdReichUnitType {

    /**
     * Basic infantry unit. Forms the backbone of ground forces.
     * Effective in urban and jungle terrain.
     */
    INFANTRY("Infantry", 1, 1, 1, 1, "Basic foot soldier unit"),

    /**
     * Mechanized infantry with armored transport.
     * Balanced offense and defense with good mobility.
     */
    MECHANIZED("Mechanized Infantry", 2, 2, 2, 2, "Armored mobile infantry"),

    /**
     * Tank unit. High offense, moderate defense.
     * Devastating in open terrain, weak in mountains and urban areas.
     */
    TANK("Tank", 4, 3, 3, 3, "Heavy armored assault unit"),

    /**
     * Artillery battery. Long-range bombardment support.
     * High attack value but low mobility and defense.
     */
    ARTILLERY("Artillery", 3, 1, 1, 2, "Long-range bombardment support"),

    /**
     * Anti-aircraft unit. Defends against air support.
     * Provides defensive bonus against air attacks.
     */
    ANTI_AIR("Anti-Air", 1, 2, 1, 1, "Air defense battery"),

    /**
     * Reconnaissance unit. Fast movement, low combat value.
     * Provides intelligence and early warning.
     */
    RECON("Reconnaissance", 1, 1, 2, 1, "Fast scout unit"),

    /**
     * Engineer unit. Specialized in fortification and terrain modification.
     * Can improve defensive positions and repair infrastructure.
     */
    ENGINEER("Engineer", 1, 1, 1, 1, "Fortification and construction specialists"),

    /**
     * Air support unit. Close air support from aircraft.
     * Provides attack bonus to adjacent friendly units.
     */
    AIR_SUPPORT("Air Support", 3, 0, 4, 2, "Close air support from aircraft");

    private final String displayName;
    private final int baseAttack;
    private final int baseDefense;
    private final int mobility;
    private final int supplyCost;
    private final String description;

    ThirdReichUnitType(String displayName, int baseAttack, int baseDefense,
                       int mobility, int supplyCost, String description) {
        this.displayName = displayName;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.mobility = mobility;
        this.supplyCost = supplyCost;
        this.description = description;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the base attack value for CRT calculations. */
    public int getBaseAttack() {
        return baseAttack;
    }

    /** Returns the base defense value for CRT calculations. */
    public int getBaseDefense() {
        return baseDefense;
    }

    /** Returns the mobility rating (movement range in hexes). */
    public int getMobility() {
        return mobility;
    }

    /** Returns the per-turn supply cost. */
    public int getSupplyCost() {
        return supplyCost;
    }

    /** Returns the description. */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
