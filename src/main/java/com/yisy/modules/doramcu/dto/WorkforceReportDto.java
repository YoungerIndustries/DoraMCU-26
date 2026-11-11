package com.yisy.modules.doramcu.dto;

/**
 * Response DTO for workforce monitoring data from the Galaxy Overseer.
 * <p>
 * Contains the AI agent counts broken down by role, staffing status,
 * and experience metrics for a galaxy's workforce.
 * </p>
 */
public class WorkforceReportDto {

    /**
     * Galaxy ID.
     */
    private Long galaxyId;

    /**
     * Galaxy name.
     */
    private String galaxyName;

    /**
     * Total number of AI agents in the galaxy.
     */
    private int totalAiAgents;

    /**
     * Number of Galaxy Overseer agents.
     */
    private int galaxyOverseerCount;

    /**
     * Number of Planet Overseer agents.
     */
    private int planetOverseerCount;

    /**
     * Number of Leader agents.
     */
    private int leaderCount;

    /**
     * Number of Governor agents.
     */
    private int governorCount;

    /**
     * Number of agents with other roles.
     */
    private int otherCount;

    /**
     * Estimated number of planets (derived from solar system count).
     */
    private int estimatedPlanets;

    /**
     * Recommended maximum number of AI agents based on planet count.
     */
    private int recommendedMaxAgents;

    /**
     * Whether the workforce exceeds the recommended maximum.
     */
    private boolean isOverstaffed;

    /**
     * Total experience points across all AI agents.
     */
    private int totalWorkforceExperience;

    /**
     * Average experience per AI agent.
     */
    private double averageExperience;

    /**
     * Text recommendation based on staffing analysis.
     */
    private String recommendation;

    /**
     * Default constructor for JSON deserialization.
     */
    public WorkforceReportDto() {
    }

    // -- Getters and Setters --

    public Long getGalaxyId() {
        return galaxyId;
    }

    public void setGalaxyId(Long galaxyId) {
        this.galaxyId = galaxyId;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public int getTotalAiAgents() {
        return totalAiAgents;
    }

    public void setTotalAiAgents(int totalAiAgents) {
        this.totalAiAgents = totalAiAgents;
    }

    public int getGalaxyOverseerCount() {
        return galaxyOverseerCount;
    }

    public void setGalaxyOverseerCount(int galaxyOverseerCount) {
        this.galaxyOverseerCount = galaxyOverseerCount;
    }

    public int getPlanetOverseerCount() {
        return planetOverseerCount;
    }

    public void setPlanetOverseerCount(int planetOverseerCount) {
        this.planetOverseerCount = planetOverseerCount;
    }

    public int getLeaderCount() {
        return leaderCount;
    }

    public void setLeaderCount(int leaderCount) {
        this.leaderCount = leaderCount;
    }

    public int getGovernorCount() {
        return governorCount;
    }

    public void setGovernorCount(int governorCount) {
        this.governorCount = governorCount;
    }

    public int getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(int otherCount) {
        this.otherCount = otherCount;
    }

    public int getEstimatedPlanets() {
        return estimatedPlanets;
    }

    public void setEstimatedPlanets(int estimatedPlanets) {
        this.estimatedPlanets = estimatedPlanets;
    }

    public int getRecommendedMaxAgents() {
        return recommendedMaxAgents;
    }

    public void setRecommendedMaxAgents(int recommendedMaxAgents) {
        this.recommendedMaxAgents = recommendedMaxAgents;
    }

    public boolean isOverstaffed() {
        return isOverstaffed;
    }

    public void setIsOverstaffed(boolean isOverstaffed) {
        this.isOverstaffed = isOverstaffed;
    }

    public int getTotalWorkforceExperience() {
        return totalWorkforceExperience;
    }

    public void setTotalWorkforceExperience(int totalWorkforceExperience) {
        this.totalWorkforceExperience = totalWorkforceExperience;
    }

    public double getAverageExperience() {
        return averageExperience;
    }

    public void setAverageExperience(double averageExperience) {
        this.averageExperience = averageExperience;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
