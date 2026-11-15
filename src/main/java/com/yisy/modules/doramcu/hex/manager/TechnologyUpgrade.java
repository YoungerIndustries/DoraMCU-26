package com.yisy.modules.doramcu.hex.manager;

/**
 * Technology upgrades available in the economic technology tree.
 * <p>
 * Each upgrade has a level, prerequisite technologies, and provides
 * bonuses to economic operations (mining, processing, storage, trade).
 * </p>
 */
public enum TechnologyUpgrade {

    /**
     * Mining Efficiency: +20% mining yield per level.
     */
    MINING_EFFICIENCY("Mining Efficiency", "Increases mining yield by 20% per level",
            null, 0),

    /**
     * Processing Speed: -1 turn processing time per level (min 1 turn).
     */
    PROCESSING_SPEED("Processing Speed", "Reduces processing time by 1 turn per level",
            null, 0),

    /**
     * Storage Expansion: +50% storage capacity per level.
     */
    STORAGE_EXPANSION("Storage Expansion", "Increases storage capacity by 50% per level",
            null, 0),

    /**
     * Trade Mastery: +10% trade profit per level.
     */
    TRADE_MASTERY("Trade Mastery", "Increases trade profit by 10% per level",
            null, 0),

    /**
     * Module Quality: Better stats per module type per level.
     */
    MODULE_QUALITY("Module Quality", "Improves module stats per level",
            null, 0),

    /**
     * Advanced Mining: Unlocks deeper mining operations.
     * Requires Mining Efficiency level 2.
     */
    ADVANCED_MINING("Advanced Mining", "Unlocks deeper mining operations",
            MINING_EFFICIENCY, 2),

    /**
     * Automation: Reduces processing time further.
     * Requires Processing Speed level 2.
     */
    AUTOMATION("Automation", "Further reduces processing time",
            PROCESSING_SPEED, 2),

    /**
     * Market Analysis: Better trade route discovery.
     * Requires Trade Mastery level 2.
     */
    MARKET_ANALYSIS("Market Analysis", "Improves trade route discovery",
            TRADE_MASTERY, 2),

    /**
     * Mass Production: Bulk module assembly.
     * Requires Module Quality level 2.
     */
    MASS_PRODUCTION("Mass Production", "Enables bulk module assembly",
            MODULE_QUALITY, 2),

    /**
     * Advanced Processing: Unlocks alternative processing recipes that reduce CRYSTAL dependency.
     * Requires Processing Speed level 1.
     */
    ADVANCED_PROCESSING("Advanced Processing", "Unlocks alternative processing recipes with reduced CRYSTAL dependency",
            PROCESSING_SPEED, 1),

    /**
     * Advanced Assembly: Unlocks alternative module assembly recipes with different resource requirements.
     * Requires Module Quality level 1.
     */
    ADVANCED_ASSEMBLY("Advanced Assembly", "Unlocks alternative module assembly recipes with flexible resource requirements",
            MODULE_QUALITY, 1);

    private final String displayName;
    private final String description;
    private final TechnologyUpgrade prerequisite;
    private final int prerequisiteLevel;

    TechnologyUpgrade(String displayName, String description,
                       TechnologyUpgrade prerequisite, int prerequisiteLevel) {
        this.displayName = displayName;
        this.description = description;
        this.prerequisite = prerequisite;
        this.prerequisiteLevel = prerequisiteLevel;
    }

    /** @return the display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the description */
    public String getDescription() {
        return description;
    }

    /** @return the prerequisite technology, or null if none */
    public TechnologyUpgrade getPrerequisite() {
        return prerequisite;
    }

    /** @return the required level of the prerequisite */
    public int getPrerequisiteLevel() {
        return prerequisiteLevel;
    }

    /**
     * Checks if this technology can be researched given the current levels.
     *
     * @param levels map of technology to current level
     * @return true if prerequisites are met
     */
    public boolean canResearch(java.util.Map<TechnologyUpgrade, Integer> levels) {
        if (prerequisite == null) {
            return true;
        }
        int currentLevel = levels.getOrDefault(prerequisite, 0);
        return currentLevel >= prerequisiteLevel;
    }
}
