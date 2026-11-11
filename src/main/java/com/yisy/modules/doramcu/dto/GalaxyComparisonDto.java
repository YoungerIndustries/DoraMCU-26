package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for side-by-side comparison of two galaxies.
 * <p>
 * Compares health scores, build progress, workforce, work orders, events,
 * and player population between two galaxies, with delta fields showing
 * the difference (Galaxy A minus Galaxy B).
 * </p>
 */
public class GalaxyComparisonDto {

    // ---- Galaxy A ----

    /** Galaxy A name. */
    private String galaxyAName;

    /** Galaxy A ID. */
    private Long galaxyAId;

    /** Galaxy A game type. */
    private String galaxyAGameType;

    /** Galaxy A difficulty level. */
    private String galaxyADifficulty;

    /** Galaxy A health score (0-100). */
    private int galaxyAHealthScore;

    /** Galaxy A health status. */
    private String galaxyAHealthStatus;

    /** Galaxy A completion percent. */
    private int galaxyACompletionPercent;

    /** Galaxy A solar system count. */
    private int galaxyASolarSystemCount;

    /** Galaxy A AI agent count. */
    private int galaxyAAiAgentCount;

    /** Galaxy A player count. */
    private int galaxyAPlayerCount;

    /** Galaxy A pending work orders. */
    private int galaxyAPendingWorkOrders;

    /** Galaxy A critical findings. */
    private int galaxyACriticalFindings;

    /** Galaxy A is playable. */
    private boolean galaxyAIsPlayable;

    /** Galaxy A build running. */
    private boolean galaxyABuildRunning;

    // ---- Galaxy B ----

    /** Galaxy B name. */
    private String galaxyBName;

    /** Galaxy B ID. */
    private Long galaxyBId;

    /** Galaxy B game type. */
    private String galaxyBGameType;

    /** Galaxy B difficulty level. */
    private String galaxyBDifficulty;

    /** Galaxy B health score (0-100). */
    private int galaxyBHealthScore;

    /** Galaxy B health status. */
    private String galaxyBHealthStatus;

    /** Galaxy B completion percent. */
    private int galaxyBCompletionPercent;

    /** Galaxy B solar system count. */
    private int galaxyBSolarSystemCount;

    /** Galaxy B AI agent count. */
    private int galaxyBAiAgentCount;

    /** Galaxy B player count. */
    private int galaxyBPlayerCount;

    /** Galaxy B pending work orders. */
    private int galaxyBPendingWorkOrders;

    /** Galaxy B critical findings. */
    private int galaxyBCriticalFindings;

    /** Galaxy B is playable. */
    private boolean galaxyBIsPlayable;

    /** Galaxy B build running. */
    private boolean galaxyBBuildRunning;

    // ---- Deltas (A - B) ----

    /** Health score difference (positive means A is healthier). */
    private int deltaHealthScore;

    /** Completion percent difference (positive means A is more complete). */
    private int deltaCompletionPercent;

    /** Solar system count difference. */
    private int deltaSolarSystemCount;

    /** AI agent count difference. */
    private int deltaAiAgentCount;

    /** Player count difference. */
    private int deltaPlayerCount;

    /** Pending work order difference. */
    private int deltaPendingWorkOrders;

    /** Critical findings difference. */
    private int deltaCriticalFindings;

    // ---- Comparison Summary ----

    /** Which galaxy is healthier (A, B, or TIE). */
    private String healthierGalaxy;

    /** Which galaxy is more complete (A, B, or TIE). */
    private String moreCompleteGalaxy;

    /** Which galaxy has more players (A, B, or TIE). */
    private String morePopulatedGalaxy;

    /** Which galaxy has more AI agents (A, B, or TIE). */
    private String moreStaffedGalaxy;

    /** Which galaxy has fewer pending work orders (A, B, or TIE). */
    private String fewerWorkOrders;

    /** Overall winner based on combined score (A, B, or TIE). */
    private String overallWinner;

    /** Combined score for Galaxy A. */
    private double galaxyAScore;

    /** Combined score for Galaxy B. */
    private double galaxyBScore;

    /** List of comparison insights — human-readable observations about the differences. */
    private List<String> insights;

    /** Default constructor for JSON deserialization. */
    public GalaxyComparisonDto() {
    }

    // -- Getters and Setters --

    public String getGalaxyAName() {
        return galaxyAName;
    }

    public void setGalaxyAName(String galaxyAName) {
        this.galaxyAName = galaxyAName;
    }

    public Long getGalaxyAId() {
        return galaxyAId;
    }

    public void setGalaxyAId(Long galaxyAId) {
        this.galaxyAId = galaxyAId;
    }

    public String getGalaxyAGameType() {
        return galaxyAGameType;
    }

    public void setGalaxyAGameType(String galaxyAGameType) {
        this.galaxyAGameType = galaxyAGameType;
    }

    public String getGalaxyADifficulty() {
        return galaxyADifficulty;
    }

    public void setGalaxyADifficulty(String galaxyADifficulty) {
        this.galaxyADifficulty = galaxyADifficulty;
    }

    public int getGalaxyAHealthScore() {
        return galaxyAHealthScore;
    }

    public void setGalaxyAHealthScore(int galaxyAHealthScore) {
        this.galaxyAHealthScore = galaxyAHealthScore;
    }

    public String getGalaxyAHealthStatus() {
        return galaxyAHealthStatus;
    }

    public void setGalaxyAHealthStatus(String galaxyAHealthStatus) {
        this.galaxyAHealthStatus = galaxyAHealthStatus;
    }

    public int getGalaxyACompletionPercent() {
        return galaxyACompletionPercent;
    }

    public void setGalaxyACompletionPercent(int galaxyACompletionPercent) {
        this.galaxyACompletionPercent = galaxyACompletionPercent;
    }

    public int getGalaxyASolarSystemCount() {
        return galaxyASolarSystemCount;
    }

    public void setGalaxyASolarSystemCount(int galaxyASolarSystemCount) {
        this.galaxyASolarSystemCount = galaxyASolarSystemCount;
    }

    public int getGalaxyAAiAgentCount() {
        return galaxyAAiAgentCount;
    }

    public void setGalaxyAAiAgentCount(int galaxyAAiAgentCount) {
        this.galaxyAAiAgentCount = galaxyAAiAgentCount;
    }

    public int getGalaxyAPlayerCount() {
        return galaxyAPlayerCount;
    }

    public void setGalaxyAPlayerCount(int galaxyAPlayerCount) {
        this.galaxyAPlayerCount = galaxyAPlayerCount;
    }

    public int getGalaxyAPendingWorkOrders() {
        return galaxyAPendingWorkOrders;
    }

    public void setGalaxyAPendingWorkOrders(int galaxyAPendingWorkOrders) {
        this.galaxyAPendingWorkOrders = galaxyAPendingWorkOrders;
    }

    public int getGalaxyACriticalFindings() {
        return galaxyACriticalFindings;
    }

    public void setGalaxyACriticalFindings(int galaxyACriticalFindings) {
        this.galaxyACriticalFindings = galaxyACriticalFindings;
    }

    public boolean isGalaxyAIsPlayable() {
        return galaxyAIsPlayable;
    }

    public void setGalaxyAIsPlayable(boolean galaxyAIsPlayable) {
        this.galaxyAIsPlayable = galaxyAIsPlayable;
    }

    public boolean isGalaxyABuildRunning() {
        return galaxyABuildRunning;
    }

    public void setGalaxyABuildRunning(boolean galaxyABuildRunning) {
        this.galaxyABuildRunning = galaxyABuildRunning;
    }

    public String getGalaxyBName() {
        return galaxyBName;
    }

    public void setGalaxyBName(String galaxyBName) {
        this.galaxyBName = galaxyBName;
    }

    public Long getGalaxyBId() {
        return galaxyBId;
    }

    public void setGalaxyBId(Long galaxyBId) {
        this.galaxyBId = galaxyBId;
    }

    public String getGalaxyBGameType() {
        return galaxyBGameType;
    }

    public void setGalaxyBGameType(String galaxyBGameType) {
        this.galaxyBGameType = galaxyBGameType;
    }

    public String getGalaxyBDifficulty() {
        return galaxyBDifficulty;
    }

    public void setGalaxyBDifficulty(String galaxyBDifficulty) {
        this.galaxyBDifficulty = galaxyBDifficulty;
    }

    public int getGalaxyBHealthScore() {
        return galaxyBHealthScore;
    }

    public void setGalaxyBHealthScore(int galaxyBHealthScore) {
        this.galaxyBHealthScore = galaxyBHealthScore;
    }

    public String getGalaxyBHealthStatus() {
        return galaxyBHealthStatus;
    }

    public void setGalaxyBHealthStatus(String galaxyBHealthStatus) {
        this.galaxyBHealthStatus = galaxyBHealthStatus;
    }

    public int getGalaxyBCompletionPercent() {
        return galaxyBCompletionPercent;
    }

    public void setGalaxyBCompletionPercent(int galaxyBCompletionPercent) {
        this.galaxyBCompletionPercent = galaxyBCompletionPercent;
    }

    public int getGalaxyBSolarSystemCount() {
        return galaxyBSolarSystemCount;
    }

    public void setGalaxyBSolarSystemCount(int galaxyBSolarSystemCount) {
        this.galaxyBSolarSystemCount = galaxyBSolarSystemCount;
    }

    public int getGalaxyBAiAgentCount() {
        return galaxyBAiAgentCount;
    }

    public void setGalaxyBAiAgentCount(int galaxyBAiAgentCount) {
        this.galaxyBAiAgentCount = galaxyBAiAgentCount;
    }

    public int getGalaxyBPlayerCount() {
        return galaxyBPlayerCount;
    }

    public void setGalaxyBPlayerCount(int galaxyBPlayerCount) {
        this.galaxyBPlayerCount = galaxyBPlayerCount;
    }

    public int getGalaxyBPendingWorkOrders() {
        return galaxyBPendingWorkOrders;
    }

    public void setGalaxyBPendingWorkOrders(int galaxyBPendingWorkOrders) {
        this.galaxyBPendingWorkOrders = galaxyBPendingWorkOrders;
    }

    public int getGalaxyBCriticalFindings() {
        return galaxyBCriticalFindings;
    }

    public void setGalaxyBCriticalFindings(int galaxyBCriticalFindings) {
        this.galaxyBCriticalFindings = galaxyBCriticalFindings;
    }

    public boolean isGalaxyBIsPlayable() {
        return galaxyBIsPlayable;
    }

    public void setGalaxyBIsPlayable(boolean galaxyBIsPlayable) {
        this.galaxyBIsPlayable = galaxyBIsPlayable;
    }

    public boolean isGalaxyBBuildRunning() {
        return galaxyBBuildRunning;
    }

    public void setGalaxyBBuildRunning(boolean galaxyBBuildRunning) {
        this.galaxyBBuildRunning = galaxyBBuildRunning;
    }

    public int getDeltaHealthScore() {
        return deltaHealthScore;
    }

    public void setDeltaHealthScore(int deltaHealthScore) {
        this.deltaHealthScore = deltaHealthScore;
    }

    public int getDeltaCompletionPercent() {
        return deltaCompletionPercent;
    }

    public void setDeltaCompletionPercent(int deltaCompletionPercent) {
        this.deltaCompletionPercent = deltaCompletionPercent;
    }

    public int getDeltaSolarSystemCount() {
        return deltaSolarSystemCount;
    }

    public void setDeltaSolarSystemCount(int deltaSolarSystemCount) {
        this.deltaSolarSystemCount = deltaSolarSystemCount;
    }

    public int getDeltaAiAgentCount() {
        return deltaAiAgentCount;
    }

    public void setDeltaAiAgentCount(int deltaAiAgentCount) {
        this.deltaAiAgentCount = deltaAiAgentCount;
    }

    public int getDeltaPlayerCount() {
        return deltaPlayerCount;
    }

    public void setDeltaPlayerCount(int deltaPlayerCount) {
        this.deltaPlayerCount = deltaPlayerCount;
    }

    public int getDeltaPendingWorkOrders() {
        return deltaPendingWorkOrders;
    }

    public void setDeltaPendingWorkOrders(int deltaPendingWorkOrders) {
        this.deltaPendingWorkOrders = deltaPendingWorkOrders;
    }

    public int getDeltaCriticalFindings() {
        return deltaCriticalFindings;
    }

    public void setDeltaCriticalFindings(int deltaCriticalFindings) {
        this.deltaCriticalFindings = deltaCriticalFindings;
    }

    public String getHealthierGalaxy() {
        return healthierGalaxy;
    }

    public void setHealthierGalaxy(String healthierGalaxy) {
        this.healthierGalaxy = healthierGalaxy;
    }

    public String getMoreCompleteGalaxy() {
        return moreCompleteGalaxy;
    }

    public void setMoreCompleteGalaxy(String moreCompleteGalaxy) {
        this.moreCompleteGalaxy = moreCompleteGalaxy;
    }

    public String getMorePopulatedGalaxy() {
        return morePopulatedGalaxy;
    }

    public void setMorePopulatedGalaxy(String morePopulatedGalaxy) {
        this.morePopulatedGalaxy = morePopulatedGalaxy;
    }

    public String getMoreStaffedGalaxy() {
        return moreStaffedGalaxy;
    }

    public void setMoreStaffedGalaxy(String moreStaffedGalaxy) {
        this.moreStaffedGalaxy = moreStaffedGalaxy;
    }

    public String getFewerWorkOrders() {
        return fewerWorkOrders;
    }

    public void setFewerWorkOrders(String fewerWorkOrders) {
        this.fewerWorkOrders = fewerWorkOrders;
    }

    public String getOverallWinner() {
        return overallWinner;
    }

    public void setOverallWinner(String overallWinner) {
        this.overallWinner = overallWinner;
    }

    public double getGalaxyAScore() {
        return galaxyAScore;
    }

    public void setGalaxyAScore(double galaxyAScore) {
        this.galaxyAScore = galaxyAScore;
    }

    public double getGalaxyBScore() {
        return galaxyBScore;
    }

    public void setGalaxyBScore(double galaxyBScore) {
        this.galaxyBScore = galaxyBScore;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }
}
