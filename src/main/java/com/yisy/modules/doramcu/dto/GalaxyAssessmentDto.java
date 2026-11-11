package com.yisy.modules.doramcu.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for galaxy assessment results from the Galaxy Overseer.
 * <p>
 * Contains the health score, findings with severity levels, and recommendations
 * based on the Earth 2026 rules database evaluation.
 * </p>
 */
public class GalaxyAssessmentDto {

    /**
     * Galaxy ID.
     */
    private Long galaxyId;

    /**
     * Galaxy name.
     */
    private String galaxyName;

    /**
     * Overall health score (0-100). Higher is healthier.
     */
    private int healthScore;

    /**
     * Current number of solar systems in the galaxy.
     */
    private int solarSystemCount;

    /**
     * Expected maximum number of solar systems per rules database.
     */
    private int expectedMaxSystems;

    /**
     * Current number of AI agents deployed.
     */
    private int aiAgentCount;

    /**
     * Recommended number of AI agents per rules database.
     */
    private int recommendedAiAgents;

    /**
     * Whether galaxy metadata (game type, difficulty, theme, description) is complete.
     */
    private boolean metadataComplete;

    /**
     * Current hyperlane density value.
     */
    private double hyperlaneDensity;

    /**
     * List of assessment findings with severity and descriptions.
     */
    private List<FindingDto> findings = new ArrayList<>();

    /**
     * List of recommended actions.
     */
    private List<String> recommendations = new ArrayList<>();

    /**
     * Default constructor for JSON deserialization.
     */
    public GalaxyAssessmentDto() {
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

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
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

    public int getRecommendedAiAgents() {
        return recommendedAiAgents;
    }

    public void setRecommendedAiAgents(int recommendedAiAgents) {
        this.recommendedAiAgents = recommendedAiAgents;
    }

    public boolean isMetadataComplete() {
        return metadataComplete;
    }

    public void setMetadataComplete(boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
    }

    public double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    public void setHyperlaneDensity(double hyperlaneDensity) {
        this.hyperlaneDensity = hyperlaneDensity;
    }

    public List<FindingDto> getFindings() {
        return findings;
    }

    public void setFindings(List<FindingDto> findings) {
        this.findings = findings;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    /**
     * A single finding from the galaxy assessment.
     */
    public static class FindingDto {

        /**
         * Severity level: CRITICAL, HIGH, MEDIUM, LOW, INFO.
         */
        private String severity;

        /**
         * Human-readable description of the finding.
         */
        private String description;

        /**
         * Recommended action to address this finding.
         */
        private String recommendedAction;

        FindingDto() {
        }

        public FindingDto(String severity, String description, String recommendedAction) {
            this.severity = severity;
            this.description = description;
            this.recommendedAction = recommendedAction;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public void setRecommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
        }
    }
}
