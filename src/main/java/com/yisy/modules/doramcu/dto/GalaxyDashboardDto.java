package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive response DTO for a single galaxy dashboard view.
 * <p>
 * Aggregates health assessment, build status, workforce report, work orders,
 * recent events, and player population into a single response so admin dashboards
 * need only one API call to get the full picture of a galaxy's state.
 * </p>
 */
public class GalaxyDashboardDto {

    // ---- Galaxy Identity ----

    /**
     * Galaxy name.
     */
    private String galaxyName;

    /**
     * Galaxy ID.
     */
    private Long galaxyId;

    /**
     * Galaxy description (if set).
     */
    private String description;

    /**
     * Game type (e.g., TRAVELLER, STAR_TREK).
     */
    private String gameType;

    /**
     * Difficulty level.
     */
    private String difficultyLevel;

    /**
     * Theme (if set).
     */
    private String theme;

    // ---- Health Section ----

    /**
     * Overall health score (0-100). Higher is healthier.
     */
    private int healthScore;

    /**
     * Human-readable health status: HEALTHY, WARNING, CRITICAL.
     */
    private String healthStatus;

    /**
     * Whether the galaxy has at least the Earth hub generated and is playable.
     */
    private boolean isPlayable;

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
     * Detailed assessment findings.
     */
    private List<GalaxyAssessmentDto.FindingDto> findings;

    // ---- Build Section ----

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
     * Whether a background build is currently running.
     */
    private boolean buildRunning;

    /**
     * Current build phase description (e.g., "CORE_SYSTEMS", "Complete").
     */
    private String buildPhase;

    /**
     * Build error message if the last build failed.
     */
    private String buildErrorMessage;

    /**
     * Build elapsed time in milliseconds.
     */
    private long buildElapsedMs;

    // ---- Workforce Section ----

    /**
     * Total number of AI agents deployed.
     */
    private int aiAgentCount;

    /**
     * AI agent counts broken down by role.
     */
    private Map<String, Integer> aiAgentsByRole;

    /**
     * Whether the workforce is overstaffed.
     */
    private boolean isOverstaffed;

    /**
     * Workforce recommendation text.
     */
    private String workforceRecommendation;

    // ---- Work Orders Section ----

    /**
     * Total number of work orders for this galaxy.
     */
    private int workOrderTotal;

    /**
     * Number of pending work orders.
     */
    private int workOrderPending;

    /**
     * Number of running work orders.
     */
    private int workOrderRunning;

    /**
     * Number of completed work orders.
     */
    private int workOrderCompleted;

    /**
     * Number of failed work orders.
     */
    private int workOrderFailed;

    /**
     * Most recent work orders (up to 5).
     */
    private List<WorkOrderDto> recentWorkOrders;

    // ---- Events Section ----

    /**
     * Most recent galaxy events (up to 10).
     */
    private List<Map<String, Object>> recentEvents;

    // ---- Player Population Section ----

    /**
     * Total number of players in this galaxy.
     */
    private int playerCount;

    /**
     * Number of AI players in this galaxy.
     */
    private int aiPlayerCount;

    /**
     * Number of human players in this galaxy.
     */
    private int humanPlayerCount;

    /**
     * List of player names currently in the galaxy.
     */
    private List<String> playerNames;

    // ---- Metadata ----

    /**
     * Galaxy creation timestamp.
     */
    private java.time.LocalDateTime createdAt;

    /**
     * Default constructor for JSON deserialization.
     */
    public GalaxyDashboardDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
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

    public List<GalaxyAssessmentDto.FindingDto> getFindings() {
        return findings;
    }

    public void setFindings(List<GalaxyAssessmentDto.FindingDto> findings) {
        this.findings = findings;
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

    public boolean isBuildRunning() {
        return buildRunning;
    }

    public void setBuildRunning(boolean buildRunning) {
        this.buildRunning = buildRunning;
    }

    public String getBuildPhase() {
        return buildPhase;
    }

    public void setBuildPhase(String buildPhase) {
        this.buildPhase = buildPhase;
    }

    public String getBuildErrorMessage() {
        return buildErrorMessage;
    }

    public void setBuildErrorMessage(String buildErrorMessage) {
        this.buildErrorMessage = buildErrorMessage;
    }

    public long getBuildElapsedMs() {
        return buildElapsedMs;
    }

    public void setBuildElapsedMs(long buildElapsedMs) {
        this.buildElapsedMs = buildElapsedMs;
    }

    public int getAiAgentCount() {
        return aiAgentCount;
    }

    public void setAiAgentCount(int aiAgentCount) {
        this.aiAgentCount = aiAgentCount;
    }

    public Map<String, Integer> getAiAgentsByRole() {
        return aiAgentsByRole;
    }

    public void setAiAgentsByRole(Map<String, Integer> aiAgentsByRole) {
        this.aiAgentsByRole = aiAgentsByRole;
    }

    public boolean isOverstaffed() {
        return isOverstaffed;
    }

    public void setOverstaffed(boolean overstaffed) {
        isOverstaffed = overstaffed;
    }

    public String getWorkforceRecommendation() {
        return workforceRecommendation;
    }

    public void setWorkforceRecommendation(String workforceRecommendation) {
        this.workforceRecommendation = workforceRecommendation;
    }

    public int getWorkOrderTotal() {
        return workOrderTotal;
    }

    public void setWorkOrderTotal(int workOrderTotal) {
        this.workOrderTotal = workOrderTotal;
    }

    public int getWorkOrderPending() {
        return workOrderPending;
    }

    public void setWorkOrderPending(int workOrderPending) {
        this.workOrderPending = workOrderPending;
    }

    public int getWorkOrderRunning() {
        return workOrderRunning;
    }

    public void setWorkOrderRunning(int workOrderRunning) {
        this.workOrderRunning = workOrderRunning;
    }

    public int getWorkOrderCompleted() {
        return workOrderCompleted;
    }

    public void setWorkOrderCompleted(int workOrderCompleted) {
        this.workOrderCompleted = workOrderCompleted;
    }

    public int getWorkOrderFailed() {
        return workOrderFailed;
    }

    public void setWorkOrderFailed(int workOrderFailed) {
        this.workOrderFailed = workOrderFailed;
    }

    public List<WorkOrderDto> getRecentWorkOrders() {
        return recentWorkOrders;
    }

    public void setRecentWorkOrders(List<WorkOrderDto> recentWorkOrders) {
        this.recentWorkOrders = recentWorkOrders;
    }

    public List<Map<String, Object>> getRecentEvents() {
        return recentEvents;
    }

    public void setRecentEvents(List<Map<String, Object>> recentEvents) {
        this.recentEvents = recentEvents;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getAiPlayerCount() {
        return aiPlayerCount;
    }

    public void setAiPlayerCount(int aiPlayerCount) {
        this.aiPlayerCount = aiPlayerCount;
    }

    public int getHumanPlayerCount() {
        return humanPlayerCount;
    }

    public void setHumanPlayerCount(int humanPlayerCount) {
        this.humanPlayerCount = humanPlayerCount;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
