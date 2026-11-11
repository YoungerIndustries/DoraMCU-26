package com.yisy.modules.doramcu.dto;

import java.util.Map;

/**
 * Response DTO for galaxy creation cost estimates.
 * <p>
 * Returned when a player requests a cost estimate before creating a galaxy.
 * Includes the total cost and a detailed breakdown of all cost components.
 * </p>
 */
public class GalaxyCreationCostDto {

    /** The total cost in credits to create the galaxy. */
    private long totalCost;

    /** The game module type (DoraT, CaptainsLog, Hybrid). */
    private String gameType;

    /** The difficulty level (easy, medium, hard). */
    private String difficultyLevel;

    /** The difficulty multiplier applied (as a percentage, e.g., 100 = 1.0x). */
    private long difficultyMultiplierPercent;

    /** Detailed breakdown of cost components. */
    private Map<String, Long> breakdown;

    /** Whether the corporation has sufficient credits to create this galaxy. */
    private boolean sufficientFunds;

    /** The corporation's current credit balance. */
    private long corporationBalance;

    public GalaxyCreationCostDto() {
    }

    public GalaxyCreationCostDto(long totalCost, String gameType, String difficultyLevel,
                                  long difficultyMultiplierPercent, Map<String, Long> breakdown) {
        this.totalCost = totalCost;
        this.gameType = gameType;
        this.difficultyLevel = difficultyLevel;
        this.difficultyMultiplierPercent = difficultyMultiplierPercent;
        this.breakdown = breakdown;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public long getDifficultyMultiplierPercent() {
        return difficultyMultiplierPercent;
    }

    public void setDifficultyMultiplierPercent(long difficultyMultiplierPercent) {
        this.difficultyMultiplierPercent = difficultyMultiplierPercent;
    }

    public Map<String, Long> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(Map<String, Long> breakdown) {
        this.breakdown = breakdown;
    }

    public boolean isSufficientFunds() {
        return sufficientFunds;
    }

    public void setSufficientFunds(boolean sufficientFunds) {
        this.sufficientFunds = sufficientFunds;
    }

    public long getCorporationBalance() {
        return corporationBalance;
    }

    public void setCorporationBalance(long corporationBalance) {
        this.corporationBalance = corporationBalance;
    }
}
