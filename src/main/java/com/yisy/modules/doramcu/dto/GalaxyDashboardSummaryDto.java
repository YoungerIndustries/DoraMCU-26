package com.yisy.modules.doramcu.dto;

/**
 * Lightweight summary DTO for the all-galaxies dashboard view.
 * <p>
 * Provides a condensed overview of each galaxy's state without the full
 * detail of {@link GalaxyDashboardDto}. Used by the dashboard list endpoint
 * to show all galaxies at a glance.
 * </p>
 */
public class GalaxyDashboardSummaryDto {

    /**
     * Galaxy name.
     */
    private String galaxyName;

    /**
     * Galaxy ID.
     */
    private Long galaxyId;

    /**
     * Game type (e.g., TRAVELLER, STAR_TREK).
     */
    private String gameType;

    /**
     * Overall health score (0-100).
     */
    private int healthScore;

    /**
     * Health status: HEALTHY, WARNING, CRITICAL.
     */
    private String healthStatus;

    /**
     * Whether the galaxy is playable (Earth hub exists).
     */
    private boolean isPlayable;

    /**
     * Galaxy completion percentage (0-100).
     */
    private int completionPercent;

    /**
     * Whether a background build is currently running.
     */
    private boolean buildRunning;

    /**
     * Total number of AI agents.
     */
    private int aiAgentCount;

    /**
     * Total number of players.
     */
    private int playerCount;

    /**
     * Number of pending work orders.
     */
    private int pendingWorkOrders;

    /**
     * Number of critical findings.
     */
    private int criticalFindings;

    /**
     * Galaxy creation timestamp.
     */
    private java.time.LocalDateTime createdAt;

    /**
     * Default constructor for JSON deserialization.
     */
    public GalaxyDashboardSummaryDto() {
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

    public boolean isBuildRunning() {
        return buildRunning;
    }

    public void setBuildRunning(boolean buildRunning) {
        this.buildRunning = buildRunning;
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

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
