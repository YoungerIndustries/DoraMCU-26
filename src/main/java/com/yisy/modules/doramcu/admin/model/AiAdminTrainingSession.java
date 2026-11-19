package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity tracking an individual training session for an AI admin.
 * Sessions record progress, score, and LLM feedback for a training scenario.
 */
@Entity
@Table(name = "ai_admin_training_sessions", indexes = {
        @Index(name = "idx_trnsn_admin", columnList = "ai_admin_id"),
        @Index(name = "idx_trnsn_scenario", columnList = "scenario_id"),
        @Index(name = "idx_trnsn_status", columnList = "status"),
        @Index(name = "idx_trnsn_created", columnList = "created_at")
})
public class AiAdminTrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The AI admin undergoing training.
     */
    @Column(name = "ai_admin_id", nullable = false)
    private Long aiAdminId;

    /**
     * The training scenario being practiced.
     */
    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    /**
     * Current status of the training session.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.NOT_STARTED;

    /**
     * Current score earned in the session.
     */
    @Column(name = "score", nullable = false)
    private Integer score = 0;

    /**
     * Maximum possible score for the scenario.
     */
    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    /**
     * LLM-generated feedback after session completion.
     */
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    /**
     * JSON log of actions taken during the session.
     */
    @Column(name = "actions_taken", columnDefinition = "TEXT")
    private String actionsTaken;

    /**
     * When the training session started.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * When the training session ended.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * When the session was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAiAdminId() {
        return aiAdminId;
    }

    public void setAiAdminId(Long aiAdminId) {
        this.aiAdminId = aiAdminId;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
