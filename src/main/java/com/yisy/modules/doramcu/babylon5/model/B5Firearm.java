package com.yisy.modules.doramcu.babylon5.model;

/**
 * Firearms weapons available in the Babylon 5 universe.
 * <p>
 * Weapons range from standard sidearms to heavy weapons used by
 * various B5 races including Earth Force, Narn, Centauri, and Minbari.
 * </p>
 */
public enum B5Firearm {
    
    // Sidearms
    PISTOL("Pistol", 4, 1, 2, "Standard sidearm", RangeClass.NEAR),
    LASER_PISTOL("Laser Pistol", 5, 2, 2, "Energy sidearm", RangeClass.NEAR),
    REVOLVER("Revolver", 4, 1, 3, "Heavy revolver", RangeClass.NEAR),
    
    // Rifles
    RIFLE("Rifle", 6, 2, 4, "Standard rifle", RangeClass.INTERMEDIATE),
    LASER_RIFLE("Laser Rifle", 7, 3, 4, "Energy rifle", RangeClass.INTERMEDIATE),
    ASSAULT_RIFLE("Assault Rifle", 6, 2, 5, "Automatic rifle", RangeClass.INTERMEDIATE),
    
    // Heavy weapons
    HEAVY_WEAPON("Heavy Weapon", 8, 4, 8, "Heavy weapon system", RangeClass.FAR),
    PLASMA_CANNON("Plasma Cannon", 9, 6, 10, "Shadow plasma cannon", RangeClass.FAR);

    private final String name;
    private final int attackValue;
    private final int damage;
    private final int encumbrance;
    private final String description;
    private final RangeClass rangeClass;

    B5Firearm(String name, int attackValue, int damage, int encumbrance, String description, RangeClass rangeClass) {
        this.name = name;
        this.attackValue = attackValue;
        this.damage = damage;
        this.encumbrance = encumbrance;
        this.description = description;
        this.rangeClass = rangeClass;
    }

    public String getName() {
        return name;
    }

    public int getAttackValue() {
        return attackValue;
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

    public RangeClass getRangeClass() {
        return rangeClass;
    }

    /**
     * Range classes for firearms.
     */
    public enum RangeClass {
        NEAR("Near", 0),
        INTERMEDIATE("Intermediate", -1),
        FAR("Far", -2);

        private final String name;
        private final int modifier;

        RangeClass(String name, int modifier) {
            this.name = name;
            this.modifier = modifier;
        }

        public String getName() {
            return name;
        }

        public int getModifier() {
            return modifier;
        }
    }
}
