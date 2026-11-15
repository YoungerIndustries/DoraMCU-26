package com.yisy.modules.doramcu.hex.manager;

import java.util.*;

/**
 * Manages the technology tree for an empire.
 * <p>
 * Tracks research levels, handles research actions, and provides
 * bonus calculations for economic operations.
 * </p>
 */
public class TechnologyTree {

    private final int empireId;

    /** Current research level for each technology. */
    private final Map<TechnologyUpgrade, Integer> levels = new EnumMap<>(TechnologyUpgrade.class);

    /** Research points available for spending. */
    private int researchPoints;

    /** Maximum level for any technology. */
    public static final int MAX_LEVEL = 5;

    /** Research cost per level (increases with each level). */
    public static final int BASE_RESEARCH_COST = 100;

    /**
     * Creates a new technology tree for the given empire.
     *
     * @param empireId the empire ID
     */
    public TechnologyTree(int empireId) {
        this.empireId = empireId;
        for (TechnologyUpgrade tech : TechnologyUpgrade.values()) {
            levels.put(tech, 0);
        }
        this.researchPoints = 0;
    }

    /** @return the empire ID */
    public int getEmpireId() {
        return empireId;
    }

    /** @return the current research points */
    public int getResearchPoints() {
        return researchPoints;
    }

    /**
     * Adds research points.
     *
     * @param points the points to add
     */
    public void addResearchPoints(int points) {
        if (points > 0) {
            researchPoints += points;
        }
    }

    /**
     * Returns the current level of a technology.
     *
     * @param tech the technology
     * @return the current level (0 = not researched)
     */
    public int getLevel(TechnologyUpgrade tech) {
        return levels.getOrDefault(tech, 0);
    }

    /**
     * Returns all technology levels (immutable).
     *
     * @return map of technology to level
     */
    public Map<TechnologyUpgrade, Integer> getLevels() {
        return Collections.unmodifiableMap(levels);
    }

    /**
     * Checks if a technology can be researched.
     *
     * @param tech the technology
     * @return true if prerequisites are met and not at max level
     */
    public boolean canResearch(TechnologyUpgrade tech) {
        if (getLevel(tech) >= MAX_LEVEL) {
            return false;
        }
        return tech.canResearch(levels);
    }

    /**
     * Researches a technology, spending research points.
     *
     * @param tech the technology to research
     * @return true if research was successful
     */
    public boolean research(TechnologyUpgrade tech) {
        if (!canResearch(tech)) {
            return false;
        }
        int cost = getResearchCost(tech);
        if (researchPoints < cost) {
            return false;
        }
        researchPoints -= cost;
        levels.merge(tech, 1, Integer::sum);
        return true;
    }

    /**
     * Returns the research cost for the next level of a technology.
     *
     * @param tech the technology
     * @return the cost in research points
     */
    public int getResearchCost(TechnologyUpgrade tech) {
        int currentLevel = getLevel(tech);
        return BASE_RESEARCH_COST * (currentLevel + 1);
    }

    // ---- Bonus Calculations ----

    /**
     * Returns the mining yield multiplier based on Mining Efficiency level.
     *
     * @return the multiplier (1.0 = no bonus)
     */
    public double getMiningYieldMultiplier() {
        int level = getLevel(TechnologyUpgrade.MINING_EFFICIENCY);
        return 1.0 + (0.2 * level);
    }

    /**
     * Returns the processing speed bonus (turns reduced).
     *
     * @return the number of turns reduced
     */
    public int getProcessingSpeedBonus() {
        int level = getLevel(TechnologyUpgrade.PROCESSING_SPEED);
        int bonus = level;
        if (getLevel(TechnologyUpgrade.AUTOMATION) > 0) {
            bonus += getLevel(TechnologyUpgrade.AUTOMATION);
        }
        return bonus;
    }

    /**
     * Returns the storage capacity multiplier based on Storage Expansion level.
     *
     * @return the multiplier (1.0 = no bonus)
     */
    public double getStorageCapacityMultiplier() {
        int level = getLevel(TechnologyUpgrade.STORAGE_EXPANSION);
        return 1.0 + (0.5 * level);
    }

    /**
     * Returns the trade profit bonus percentage based on Trade Mastery level.
     *
     * @return the bonus percentage
     */
    public double getTradeProfitBonus() {
        int level = getLevel(TechnologyUpgrade.TRADE_MASTERY);
        double bonus = 10.0 * level;
        if (getLevel(TechnologyUpgrade.MARKET_ANALYSIS) > 0) {
            bonus += 5.0 * getLevel(TechnologyUpgrade.MARKET_ANALYSIS);
        }
        return bonus;
    }

    /**
     * Returns the module quality bonus based on Module Quality level.
     *
     * @return the bonus multiplier (1.0 = no bonus)
     */
    public double getModuleQualityBonus() {
        int level = getLevel(TechnologyUpgrade.MODULE_QUALITY);
        double bonus = 1.0 + (0.1 * level);
        if (getLevel(TechnologyUpgrade.MASS_PRODUCTION) > 0) {
            bonus += 0.05 * getLevel(TechnologyUpgrade.MASS_PRODUCTION);
        }
        return bonus;
    }

    /**
     * Checks if alternative processing recipes are unlocked.
     * Requires Advanced Processing technology at level 1 or higher.
     *
     * @return true if alternative processing recipes are available
     */
    public boolean hasAdvancedProcessing() {
        return getLevel(TechnologyUpgrade.ADVANCED_PROCESSING) > 0;
    }

    /**
     * Checks if alternative module assembly recipes are unlocked.
     * Requires Advanced Assembly technology at level 1 or higher.
     *
     * @return true if alternative module recipes are available
     */
    public boolean hasAdvancedAssembly() {
        return getLevel(TechnologyUpgrade.ADVANCED_ASSEMBLY) > 0;
    }

    /**
     * Returns a summary of the technology tree state.
     *
     * @return map of technology names to their levels
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("empireId", empireId);
        summary.put("researchPoints", researchPoints);

        Map<String, Integer> techLevels = new LinkedHashMap<>();
        for (TechnologyUpgrade tech : TechnologyUpgrade.values()) {
            int level = getLevel(tech);
            if (level > 0) {
                techLevels.put(tech.getDisplayName(), level);
            }
        }
        summary.put("researchedTechnologies", techLevels);

        Map<String, Object> bonuses = new LinkedHashMap<>();
        bonuses.put("miningYieldMultiplier", getMiningYieldMultiplier());
        bonuses.put("processingSpeedBonus", getProcessingSpeedBonus());
        bonuses.put("storageCapacityMultiplier", getStorageCapacityMultiplier());
        bonuses.put("tradeProfitBonusPercent", getTradeProfitBonus());
        bonuses.put("moduleQualityBonus", getModuleQualityBonus());
        summary.put("bonuses", bonuses);

        return summary;
    }
}
