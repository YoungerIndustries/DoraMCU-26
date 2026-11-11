package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Unified response DTO for galaxy health monitoring.
 * <p>
 * Combines assessment results, workforce data, build status, and a quick
 * health summary into a single view for the admin dashboard.
 * </p>
 */
public class GalaxyHealthDto {

    /**
     * Galaxy name.
     */
    private String galaxyName;

    /**
     * Galaxy ID.
     */
    private Long galaxyId;

    /**
     * Overall health score (0-100). Higher is healthier.
     */
    private int healthScore;

    /**
     * Human-readable health status derived from the score.
     * Values: HEALTHY, WARNING, CRITICAL.
     */
    private String healthStatus;

    /**
     * Whether the galaxy has at least the Earth hub generated and is playable.
     */
    private boolean isPlayable;

    /**
     * Galaxy completion percentage (0-100).
     */
    private int completionPercent;

    /**
     * Number of solar systems currently in the galaxy.
     */
    private int solarSystemCount;

    /**
     * Expected maximum number of solar systems per rules database.
     */
    private int expectedMaxSystems;

    /**
     * Total number of AI agents deployed.
     */
    private int aiAgentCount;

    /**
     * Whether the workforce is overstaffed.
     */
    private boolean isOverstaffed;

    /**
     * Whether a background build is currently running.
     */
    private boolean buildRunning;

    /**
     * Number of critical findings.
     */
    private int criticalFindings;

    /**
     * Number of high severity findings.
     */
    private int highFindings;

    /**
     * Number of medium severity findings.
     */
    private int mediumFindings;

    /**
     * Top recommendations sorted by priority.
     */
    private List<String> topRecommendations;

    /**
     * Default constructor for JSON deserialization.
     */
    public GalaxyHealthDto() {
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

    public int getExpectedMaxSystems() {
        return expectedMaxSystems;
    }

    public void setExpectedMaxSystems(int expectedMaxSystems) {
        this.expectedMaxSystems = expectedMaxSystems;
    }

    public int getAiAgentCount() {
        return aiAgentCount;
    }

    public void setAiAgentCount(int aiAgentCount) {
        this.aiAgentCount = aiAgentCount;
    }

    public boolean isOverstaffed() {
        return isOverstaffed;
    }

    public void setOverstaffed(boolean overstaffed) {
        isOverstaffed = overstaffed;
    }

    public boolean isBuildRunning() {
        return buildRunning;
    }

    public void setBuildRunning(boolean buildRunning) {
        this.buildRunning = buildRunning;
    }

    public int getCriticalFindings() {
        return criticalFindings;
    }

    public void setCriticalFindings(int criticalFindings) {
        this.criticalFindings = criticalFindings;
    }

    public int getHighFindings() {
        return highFindings;
    }

    public void setHighFindings(int highFindings) {
        this.highFindings = highFindings;
    }

    public int getMediumFindings() {
        return mediumFindings;
    }

    public void setMediumFindings(int mediumFindings) {
        this.mediumFindings = mediumFindings;
    }

    public List<String> getTopRecommendations() {
        return topRecommendations;
    }

    public void setTopRecommendations(List<String> topRecommendations) {
        this.topRecommendations = topRecommendations;
    }

    /**
     * Derives health status from the score.
     */
    public static String deriveHealthStatus(int healthScore) {
        if (healthScore >= 80) {
            return "HEALTHY";
        } else if (healthScore >= 50) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }
}
