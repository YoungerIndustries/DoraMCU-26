package com.yisy.modules.doramcu.sfb.model;

/**
 * Armor types available in SFB ground combat.
 * <p>
 * Each armor type provides a protection value that reduces incoming damage.
 * Armor is themed around the Star Trek universe including
 * Federation, Klingon, Romulan, and other faction armor.
 * </p>
 */
public enum SfbArmor {
    NONE("None", 0, "No armor"),
    LIGHT("Light Armor", 2, "Light body armor"),
    MEDIUM("Medium Armor", 4, "Standard combat armor"),
    HEAVY("Heavy Armor", 6, "Heavy combat armor"),
    PHASER_SHIELD("Phaser Shield", 5, "Personal phaser shield"),
    DEFLECTOR_SHIELD("Deflector Shield", 8, "Personal deflector shield"),
    KLINGON_HEAVY("Klingon Heavy Armor", 7, "Klingon battle armor");

    private final String name;
    private final int protection;
    private final String description;

    SfbArmor(String name, int protection, String description) {
        this.name = name;
        this.protection = protection;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getProtection() {
        return protection;
    }

    public String getDescription() {
        return description;
    }
}
