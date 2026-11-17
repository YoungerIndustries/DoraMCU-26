package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity defining a training scenario for AI admin practice.
 * Scenarios simulate incidents with increasing difficulty levels.
 */
@Entity
@Table(name = "ai_admin_training_scenarios", indexes = {
        @Index(name = "idx_trnscn_difficulty", columnList = "difficulty"),
        @Index(name = "idx_trnscn_type", columnList = "incident_type"),
        @Index(name = "idx_trnscn_active", columnList = "is_active")
})
public class AiAdminTrainingScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the training scenario.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Description of the training scenario.
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Difficulty level of the scenario.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 15)
    private Difficulty difficulty;

    /**
     * Type of incident simulated.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 20)
    private AiAdminIncident.IncidentType incidentType;

    /**
     * Severity of the simulated incident.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private AiAdminIncident.Severity severity;

    /**
     * Scenario data as JSON: incident details, expected actions, scoring criteria.
     */
    @Column(name = "scenario_data", nullable = false, columnDefinition = "TEXT")
    private String scenarioData;

    /**
     * Time limit in minutes for completing the scenario.
     */
    @Column(name = "time_limit_minutes", nullable = false)
    private Integer timeLimitMinutes = 30;

    /**
     * XP reward for completing the scenario.
     */
    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward = 50;

    /**
     * Whether the scenario is active and available for training.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * When the scenario was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Difficulty {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public AiAdminIncident.IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(AiAdminIncident.IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public AiAdminIncident.Severity getSeverity() {
        return severity;
    }

    public void setSeverity(AiAdminIncident.Severity severity) {
        this.severity = severity;
    }

    public String getScenarioData() {
        return scenarioData;
    }

    public void setScenarioData(String scenarioData) {
        this.scenarioData = scenarioData;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public Integer getXpReward() {
        return xpReward;
    }

    public void setXpReward(Integer xpReward) {
        this.xpReward = xpReward;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
