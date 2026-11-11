package com.yisy.modules.doramcu.babylon5.model;

/**
 * Melee weapons available in the Babylon 5 universe.
 * <p>
 * Weapons range from primitive blades to advanced energy weapons used by
 * various B5 races including Earth Force, Narn, Centauri, and Minbari.
 * </p>
 */
public enum B5MeleeWeapon {
    
    // Unarmed
    FISTS("Fists", 0, 1, 0, "Unarmed combat"),
    
    // Blades
    KNIFE("Knife", 1, 2, 0, "Standard combat knife"),
    COMBAT_KNIFE("Combat Knife", 2, 3, 0, "Military-grade combat knife"),
    SWORD("Sword", 3, 4, 0, "Standard sword"),
    MINBARI_SWORD("Minbari Sword", 4, 5, 0, "Minbari warrior sword"),
    NARN_CLAW("Narn Claw", 3, 4, 0, "Narn combat claw weapon"),
    
    // Blunt weapons
    BATON("Baton", 2, 2, 0, "Standard baton"),
    STAFF("Staff", 2, 3, 0, "Wooden staff"),
    
    // Energy weapons
    ENERGY_WHIP("Energy Whip", 3, 5, 1, "Vorlon energy whip"),
    PLASMA_EDGE("Plasma Edge", 4, 6, 2, "Shadow plasma blade");

    private final String name;
    private final int parry;
    private final int damage;
    private final int encumbrance;
    private final String description;

    B5MeleeWeapon(String name, int parry, int damage, int encumbrance, String description) {
        this.name = name;
        this.parry = parry;
        this.damage = damage;
        this.encumbrance = encumbrance;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getParry() {
        return parry;
    }

    public int getDamage() {
        return damage;
    }

    public int getEncumbrance() {
        return encumbrance;
    }

    public String getDescription() {
        return description;
    }
}
