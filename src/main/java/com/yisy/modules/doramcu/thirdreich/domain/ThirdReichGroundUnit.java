package com.yisy.modules.doramcu.thirdreich.domain;

/**
 * Represents a single ground unit in the Third Reich (3R4) ground combat engine.
 * <p>
 * Each unit has a type (infantry, armor, artillery, etc.), current strength, morale,
 * and combat values used in CRT (Combat Results Table) resolution.
 * </p>
 * <p>
 * This is the core domain model for 3R4 ground combat. Game-specific units from
 * other systems (Star Trek, Traveller, DoraMCU 4X) are adapted to this model
 * before battle resolution.
 * </p>
 */
public class ThirdReichGroundUnit {

    private final String id;
    private final String name;
    private final ThirdReichUnitType unitType;
    private int strength;
    private int maxStrength;
    private int morale;
    private int attackValue;
    private int defenseValue;
    private int mobility;
    private int supplyCost;
    private int terrainBonus;
    private String commanderId;
    private String sourceGame;

    /**
     * Creates a new ground unit.
     *
     * @param id the unique identifier for this unit
     * @param name the display name
     * @param unitType the type of unit (infantry, tank, etc.)
     */
    public ThirdReichGroundUnit(String id, String name, ThirdReichUnitType unitType) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Unit ID must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Unit name must not be null or blank");
        }
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type must not be null");
        }
        this.id = id;
        this.name = name;
        this.unitType = unitType;
        this.maxStrength = 100;
        this.strength = 100;
        this.morale = 100;
        this.attackValue = unitType.getBaseAttack();
        this.defenseValue = unitType.getBaseDefense();
        this.mobility = unitType.getMobility();
        this.supplyCost = unitType.getSupplyCost();
        this.terrainBonus = 0;
    }

    // ---- Accessors ----

    /** Returns the unique identifier. */
    public String getId() {
        return id;
    }

    /** Returns the display name. */
    public String getName() {
        return name;
    }

    /** Returns the unit type. */
    public ThirdReichUnitType getUnitType() {
        return unitType;
    }

    /** Returns the current strength (0-100). */
    public int getStrength() {
        return strength;
    }

    /** Sets the current strength. */
    public void setStrength(int strength) {
        this.strength = Math.max(0, Math.min(maxStrength, strength));
    }

    /** Returns the maximum strength. */
    public int getMaxStrength() {
        return maxStrength;
    }

    /** Sets the maximum strength. */
    public void setMaxStrength(int maxStrength) {
        this.maxStrength = maxStrength;
        this.strength = Math.min(strength, maxStrength);
    }

    /** Returns the morale (0-100). */
    public int getMorale() {
        return morale;
    }

    /** Sets the morale. */
    public void setMorale(int morale) {
        this.morale = Math.max(0, Math.min(100, morale));
    }

    /** Returns the attack value for CRT calculations. */
    public int getAttackValue() {
        return attackValue;
    }

    /** Sets the attack value. */
    public void setAttackValue(int attackValue) {
        this.attackValue = attackValue;
    }

    /** Returns the defense value for CRT calculations. */
    public int getDefenseValue() {
        return defenseValue;
    }

    /** Sets the defense value. */
    public void setDefenseValue(int defenseValue) {
        this.defenseValue = defenseValue;
    }

    /** Returns the mobility rating. */
    public int getMobility() {
        return mobility;
    }

    /** Sets the mobility rating. */
    public void setMobility(int mobility) {
        this.mobility = mobility;
    }

    /** Returns the per-turn supply cost. */
    public int getSupplyCost() {
        return supplyCost;
    }

    /** Sets the supply cost. */
    public void setSupplyCost(int supplyCost) {
        this.supplyCost = supplyCost;
    }

    /** Returns the terrain bonus modifier. */
    public int getTerrainBonus() {
        return terrainBonus;
    }

    /** Sets the terrain bonus modifier. */
    public void setTerrainBonus(int terrainBonus) {
        this.terrainBonus = terrainBonus;
    }

    /** Returns the commander ID, or null if no commander is assigned. */
    public String getCommanderId() {
        return commanderId;
    }

    /** Sets the commander ID. */
    public void setCommanderId(String commanderId) {
        this.commanderId = commanderId;
    }

    /** Returns the source game system, or null if native 3R4. */
    public String getSourceGame() {
        return sourceGame;
    }

    /** Sets the source game system. */
    public void setSourceGame(String sourceGame) {
        this.sourceGame = sourceGame;
    }

    // ---- Combat Calculations ----

    /**
     * Returns the effective attack value modified by strength and morale.
     */
    public int getEffectiveAttack() {
        double strengthRatio = strength / (double) maxStrength;
        double moraleFactor = Math.max(0.5, morale / 100.0);
        return Math.max(1, (int) (attackValue * strengthRatio * moraleFactor));
    }

    /**
     * Returns the effective defense value modified by strength, morale, and terrain bonus.
     */
    public int getEffectiveDefense() {
        double strengthRatio = strength / (double) maxStrength;
        double moraleFactor = Math.max(0.5, morale / 100.0);
        int base = (int) (defenseValue * strengthRatio * moraleFactor);
        return Math.max(1, base + terrainBonus);
    }

    // ---- Actions ----

    /**
     * Applies damage to this unit. Returns true if the unit is destroyed.
     *
     * @param damage the amount of damage to apply
     * @return true if the unit was destroyed
     */
    public boolean takeDamage(int damage) {
        strength = Math.max(0, strength - damage);
        if (strength <= 0) {
            morale = 0;
        }
        return strength <= 0;
    }

    /**
     * Adjusts morale.
     *
     * @param delta the morale change (positive or negative)
     */
    public void adjustMorale(int delta) {
        morale = Math.max(0, Math.min(100, morale + delta));
    }

    /**
     * Returns true if this unit is still combat-effective.
     */
    public boolean isCombatEffective() {
        return strength > 0 && morale > 0;
    }

    @Override
    public String toString() {
        return String.format("3R4Unit{id='%s', name='%s', type=%s, strength=%d/%d, morale=%d, atk=%d, def=%d}",
                id, name, unitType, strength, maxStrength, morale, attackValue, defenseValue);
    }
}
