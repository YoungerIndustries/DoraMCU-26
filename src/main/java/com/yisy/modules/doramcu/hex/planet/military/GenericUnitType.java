package com.yisy.modules.doramcu.hex.planet.military;

/**
 * Generic military unit type that all game modules can map to.
 * <p>
 * This enum provides a common abstraction layer so that any module's unit types
 * can be converted to a generic form for cross-module military production and
 * battle resolution. Each module implements {@link ModuleUnitMapper} to map
 * its specific unit types to these generic types.
 * </p>
 * <p>
 * Example mappings:
 * <ul>
 *   <li>Earth2026 Infantry → INFANTRY</li>
 *   <li>Star Wars Stormtrooper → INFANTRY</li>
 *   <li>DnD Knight → ARMORED</li>
 *   <li>MechWarrior BattleMech → ARMORED</li>
 *   <li>Star Trek Phaser Rifleman → INFANTRY</li>
 *   <li>007 Secret Agent → SPECIAL_FORCES</li>
 * </ul>
 * </p>
 */
public enum GenericUnitType {

    /** Basic foot soldier — the backbone of any ground force */
    INFANTRY("Infantry", 1, 1, 1, 1),

    /** Armored vehicle or heavily armored unit — tanks, mechs, knights */
    ARMORED("Armored", 4, 3, 3, 3),

    /** Artillery or long-range bombardment unit */
    ARTILLERY("Artillery", 3, 1, 1, 2),

    /** Aircraft or air support unit */
    AIRCRAFT("Aircraft", 3, 0, 4, 2),

    /** Naval vessel — ships, submarines, boats */
    NAVAL("Naval", 3, 3, 2, 3),

    /** Spacecraft — starships, fighters, capital ships */
    SPACECRAFT("Spacecraft", 5, 4, 3, 4),

    /** Special forces — spies, assassins, commandos, wizards */
    SPECIAL_FORCES("Special Forces", 2, 2, 2, 2),

    /** Engineer — construction, fortification, repair */
    ENGINEER("Engineer", 1, 1, 1, 1);

    private final String displayName;
    private final int baseAttack;
    private final int baseDefense;
    private final int mobility;
    private final int supplyCost;

    GenericUnitType(String displayName, int baseAttack, int baseDefense,
                    int mobility, int supplyCost) {
        this.displayName = displayName;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.mobility = mobility;
        this.supplyCost = supplyCost;
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

    @Override
    public String toString() {
        return displayName;
    }
}
