package com.yisy.modules.doramcu.hex.planet.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing an RPG quest on a planet.
 */
public class QuestDto {

    private String id;
    private String title;
    private String description;
    private String type;
    private int difficulty;
    private long creditsReward;
    private int xpReward;
    private int reputationReward;
    private String status;
    private String issuerName;
    private String issuerEmpireId;
    private List<String> objectives;
    private List<String> requiredStationIds;
    private String assignedCorporationId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;

    public QuestDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public long getCreditsReward() {
        return creditsReward;
    }

    public void setCreditsReward(long creditsReward) {
        this.creditsReward = creditsReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public void setReputationReward(int reputationReward) {
        this.reputationReward = reputationReward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getIssuerEmpireId() {
        return issuerEmpireId;
    }

    public void setIssuerEmpireId(String issuerEmpireId) {
        this.issuerEmpireId = issuerEmpireId;
    }

    public List<String> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<String> objectives) {
        this.objectives = objectives;
    }

    public List<String> getRequiredStationIds() {
        return requiredStationIds;
    }

    public void setRequiredStationIds(List<String> requiredStationIds) {
        this.requiredStationIds = requiredStationIds;
    }

    public String getAssignedCorporationId() {
        return assignedCorporationId;
    }

    public void setAssignedCorporationId(String assignedCorporationId) {
        this.assignedCorporationId = assignedCorporationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
