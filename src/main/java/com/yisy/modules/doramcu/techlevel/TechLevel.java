package com.yisy.modules.doramcu.techlevel;

/**
 * Unified technology level scale covering prehistoric to post-singularity eras.
 * <p>
 * Based on Traveller's tech system (TL0–TL12), extended for D&D fantasy and
 * high-tech universes like Star Trek and Babylon 5.
 * </p>
 */
public enum TechLevel {

    TL0(0, "Prehistoric", "Stone tools, no metal, hunter-gatherer societies"),
    TL1(1, "Ancient", "Bronze/iron age, basic agriculture, early civilizations"),
    TL2(2, "Medieval", "Castles, feudal society, knightly warfare"),
    TL3(3, "Renaissance", "Early gunpowder, exploration, printing press"),
    TL4(4, "Industrial", "Steam power, railroads, factories, mass production"),
    TL5(5, "Modern", "Combustion engines, electronics, global communication"),
    TL6(6, "Information Age", "Computers, space flight, nuclear power"),
    TL7(7, "Interplanetary", "Colonies on other worlds, fusion power, AI assistants"),
    TL8(8, "Interstellar", "FTL travel, energy weapons, Dyson swarms"),
    TL9(9, "Galactic", "Empire-scale civilization, advanced AI, wormhole networks"),
    TL10(10, "Post-Human", "Megastructures, reality engineering, mind uploading"),
    TL11(11, "Transcendent", "Dimensional manipulation, time travel, universe seeding"),
    TL12(12, "Post-Singularity", "God-like technology, multiverse awareness");

    private final int level;
    private final String name;
    private final String description;

    TechLevel(int level, String name, String description) {
        this.level = level;
        this.name = name;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Find a TechLevel by its numeric value.
     *
     * @param level the numeric tech level (0-12)
     * @return the matching TechLevel, or null if not found
     */
    public static TechLevel fromLevel(int level) {
        for (TechLevel tl : values()) {
            if (tl.level == level) {
                return tl;
            }
        }
        return null;
    }
}
