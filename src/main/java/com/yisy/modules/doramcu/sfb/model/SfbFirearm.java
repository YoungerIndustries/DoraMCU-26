package com.yisy.modules.doramcu.sfb.model;

/**
 * Firearms available in SFB ground combat.
 * <p>
 * Each firearm has a range class (Near, Intermediate, Far) and damage value.
 * Weapons are themed around the Star Trek universe including
 * Federation phasers, Klingon disruptors, Romulan weapons, and more.
 * </p>
 */
public enum SfbFirearm {
    PHASER_PISTOL("Phaser Pistol", RangeClass.NEAR, 3, "Standard Federation phaser pistol"),
    PHASER_RIFLE("Phaser Rifle", RangeClass.INTERMEDIATE, 4, "Federation phaser rifle"),
    DISRUPTOR_PISTOL("Disruptor Pistol", RangeClass.NEAR, 4, "Klingon disruptor pistol"),
    DISRUPTOR_RIFLE("Disruptor Rifle", RangeClass.INTERMEDIATE, 5, "Klingon disruptor rifle"),
    PHOTON_TORPEDO_LAUNCHER("Photon Torpedo Launcher", RangeClass.FAR, 6, "Handheld photon torpedo launcher"),
    PLASMA_PISTOL("Plasma Pistol", RangeClass.NEAR, 3, "Romulan plasma pistol"),
    PLASMA_RIFLE("Plasma Rifle", RangeClass.INTERMEDIATE, 4, "Romulan plasma rifle"),
    MACRO_BERETTA("Macro-Beretta", RangeClass.NEAR, 5, "Federation macro-molecular blade pistol"),
    HEAVY_PHASER("Heavy Phaser", RangeClass.FAR, 6, "Federation heavy phaser weapon");

    private final String name;
    private final RangeClass rangeClass;
    private final int damage;
    private final String description;

    SfbFirearm(String name, RangeClass rangeClass, int damage, String description) {
        this.name = name;
        this.rangeClass = rangeClass;
        this.damage = damage;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public RangeClass getRangeClass() {
        return rangeClass;
    }

    public int getDamage() {
        return damage;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Range classes for firearms.
     */
    public enum RangeClass {
        NEAR("Near"),
        INTERMEDIATE("Intermediate"),
        FAR("Far");

        private final String name;

        RangeClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
