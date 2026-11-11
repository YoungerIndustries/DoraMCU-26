package com.yisy.modules.doramcu.sfb.model;

/**
 * Melee weapons available in SFB ground combat.
 * <p>
 * Each weapon has a parry value (defense) and damage value (attack).
 * Weapons are themed around the Star Trek universe including
 * Federation, Klingon, Romulan, and other faction weapons.
 * </p>
 */
public enum SfbMeleeWeapon {
    FISTS("Fists", 0, 1, "Unarmed combat"),
    KNUCKLES("Knuckles", 1, 2, "Reinforced hand armor"),
    PHASER("Phaser", 2, 3, "Stun setting melee phaser"),
    KNIFE("Knife", 2, 3, "Standard combat knife"),
    SWORD("Sword", 3, 4, "Standard sword"),
    BAT_LETH("Bat'leth", 4, 5, "Klingon curved sword"),
    ROMULAN_KNIFE("Romulan Knife", 3, 4, "Romulan combat knife"),
    MACRO_BERETTA("Macro-Beretta", 2, 4, "Federation macro-molecular blade"),
    ENERGY_WHIP("Energy Whip", 3, 4, "Energy-based whip weapon"),
    PLASMA_EDGE("Plasma Edge", 4, 6, "Plasma-edged blade"),
    GORN_CLAW("Gorn Claw", 3, 5, "Gorn natural weapons"),
    SULIBAN_STAFF("Suliban Staff", 3, 4, "Suliban combat staff");

    private final String name;
    private final int parry;
    private final int damage;
    private final String description;

    SfbMeleeWeapon(String name, int parry, int damage, String description) {
        this.name = name;
        this.parry = parry;
        this.damage = damage;
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

    public String getDescription() {
        return description;
    }
}
