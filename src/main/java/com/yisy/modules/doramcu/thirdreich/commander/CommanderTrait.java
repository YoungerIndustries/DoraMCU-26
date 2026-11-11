package com.yisy.modules.doramcu.thirdreich.commander;

/**
 * Commander traits that influence tactical decisions in 3R4 ground combat.
 * <p>
 * Each trait provides a modifier to combat resolution, affecting how a commander
 * leads their forces during battle.
 * </p>
 */
public enum CommanderTrait {

    /**
     * Aggressive commander. Pushes attacks harder, gains attack bonus but takes more risks.
     */
    AGGRESSIVE("Aggressive", 1, 0, "Pushes attacks harder, takes more risks"),

    /**
     * Defensive commander. Excels at holding positions and minimizing losses.
     */
    DEFENSIVE("Defensive", 0, 1, "Excels at holding positions and minimizing losses"),

    /**
     * Cautious commander. Avoids unnecessary risks, slower but more reliable.
     */
    CAUTIOUS("Cautious", -1, 1, "Avoids risks, slower but more reliable"),

    /**
     * Flanker. Specializes in outmaneuvering the enemy.
     */
    FLANKER("Flanker", 1, 0, "Outmaneuvers the enemy for tactical advantage"),

    /**
     * Artillery focus. Maximizes bombardment effectiveness.
     */
    ARTILLERY_FOCUS("Artillery Focus", 1, 0, "Maximizes artillery bombardment"),

    /**
     * Armor specialist. Expert in tank warfare and mechanized operations.
     */
    ARMOR_SPECIALIST("Armor Specialist", 1, 0, "Expert in tank and mechanized warfare"),

    /**
     * Infantry expert. Maximizes infantry combat effectiveness.
     */
    INFANTRY_EXPERT("Infantry Expert", 0, 1, "Maximizes infantry combat power"),

    /**
     * Logistician. Maintains supply lines and reduces attrition.
     */
    LOGISTICIAN("Logistician", 0, 1, "Maintains supply lines, reduces attrition"),

    /**
     * Inspirational leader. Boosts unit morale and resistance to rout.
     */
    INSPIRATIONAL("Inspirational", 0, 1, "Boosts morale and resistance to rout"),

    /**
     * Guerrilla specialist. Excels in irregular warfare and ambushes.
     */
    GUERRILLA("Guerrilla", 1, 1, "Excels in ambushes and irregular warfare");

    private final String displayName;
    private final int attackBonus;
    private final int defenseBonus;
    private final String description;

    CommanderTrait(String displayName, int attackBonus, int defenseBonus, String description) {
        this.displayName = displayName;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.description = description;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the attack bonus modifier. */
    public int getAttackBonus() {
        return attackBonus;
    }

    /** Returns the defense bonus modifier. */
    public int getDefenseBonus() {
        return defenseBonus;
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
