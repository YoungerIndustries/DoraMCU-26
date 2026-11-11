package com.yisy.modules.doramcu.dto;

import java.time.LocalDateTime;

/**
 * Lightweight summary DTO for comparing all galaxies in a grid view.
 * <p>
 * Used by the compare-all endpoint to provide a compact overview of all galaxies
 * sorted by various metrics, so admin dashboards can display a comparison table.
 * </p>
 */
public class GalaxyComparisonSummaryDto {

    /** Galaxy name. */
    private String galaxyName;

    /** Galaxy ID. */
    private Long galaxyId;

    /** Game type (e.g., TRAVELLER, STAR_TREK). */
    private String gameType;

    /** Difficulty level. */
    private String difficultyLevel;

    /** Health score (0-100). */
    private int healthScore;

    /** Health status: HEALTHY, WARNING, CRITICAL. */
    private String healthStatus;

    /** Whether the galaxy is playable. */
    private boolean isPlayable;

    /** Completion percentage (0-100). */
    private int completionPercent;

    /** Number of solar systems. */
    private int solarSystemCount;

    /** Number of AI agents. */
    private int aiAgentCount;

    /** Number of players. */
    private int playerCount;

    /** Number of pending work orders. */
    private int pendingWorkOrders;

    /** Number of critical findings. */
    private int criticalFindings;

    /** Whether a build is currently running. */
    private boolean buildRunning;

    /** Combined score for ranking (weighted average of health, completion, staffing). */
    private double combinedScore;

    /** Rank position when sorted by combined score (1 = best). */
    private int rank;

    /** Galaxy creation timestamp. */
    private LocalDateTime createdAt;

    /** Default constructor for JSON deserialization. */
    public GalaxyComparisonSummaryDto() {
    }

    // -- Getters and Setters --

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public Long getGalaxyId() {
        return galaxyId;
    }

    public void setGalaxyId(Long galaxyId) {
        this.galaxyId = galaxyId;
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

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public boolean isPlayable() {
        return isPlayable;
    }

    public void setPlayable(boolean playable) {
        isPlayable = playable;
    }

    public int getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(int completionPercent) {
        this.completionPercent = completionPercent;
    }

    public int getSolarSystemCount() {
        return solarSystemCount;
    }

    public void setSolarSystemCount(int solarSystemCount) {
        this.solarSystemCount = solarSystemCount;
    }

    public int getAiAgentCount() {
        return aiAgentCount;
    }

    public void setAiAgentCount(int aiAgentCount) {
        this.aiAgentCount = aiAgentCount;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getPendingWorkOrders() {
        return pendingWorkOrders;
    }

    public void setPendingWorkOrders(int pendingWorkOrders) {
        this.pendingWorkOrders = pendingWorkOrders;
    }

    public int getCriticalFindings() {
        return criticalFindings;
    }

    public void setCriticalFindings(int criticalFindings) {
        this.criticalFindings = criticalFindings;
    }

    public boolean isBuildRunning() {
        return buildRunning;
    }

    public void setBuildRunning(boolean buildRunning) {
        this.buildRunning = buildRunning;
    }

    public double getCombinedScore() {
        return combinedScore;
    }

    public void setCombinedScore(double combinedScore) {
        this.combinedScore = combinedScore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
