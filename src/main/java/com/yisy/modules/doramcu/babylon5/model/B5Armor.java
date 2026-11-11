package com.yisy.modules.doramcu.babylon5.model;

/**
 * Armor types available in the Babylon 5 universe.
 * <p>
 * Armor provides protection against melee and firearms attacks.
 * Different races have access to different armor types.
 * </p>
 */
public enum B5Armor {
    
    NONE("No Armor", 0, 0, "No armor"),
    LIGHT("Light Armor", 2, 1, "Light body armor"),
    MEDIUM("Medium Armor", 4, 2, "Standard combat armor"),
    HEAVY("Heavy Armor", 6, 3, "Heavy combat armor"),
    MINBARI("Minbari Armor", 5, 2, "Minbari warrior armor"),
    NARN("Narn Armor", 4, 1, "Narn combat armor"),
    ENERGY_SHIELD("Energy Shield", 8, 4, "Vorlon energy shield");

    private final String name;
    private final int protection;
    private final int encumbrance;
    private final String description;

    B5Armor(String name, int protection, int encumbrance, String description) {
        this.name = name;
        this.protection = protection;
        this.encumbrance = encumbrance;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getProtection() {
        return protection;
    }

    public int getEncumbrance() {
        return encumbrance;
    }

    public String getDescription() {
        return description;
    }
}
