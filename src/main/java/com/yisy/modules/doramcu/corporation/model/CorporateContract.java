package com.yisy.modules.doramcu.corporation.model;

import com.yisy.modules.doramcu.gamecore.GameType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a business contract in the corporate office kiosk.
 * <p>
 * Contracts are agreements that earn credits, XP, and reputation for the corporation.
 * Types include trade deals, security contracts, logistics operations, and corporate missions.
 * </p>
 */
@Entity
@Table(name = "corporate_contracts", indexes = {
        @Index(name = "idx_corporate_contract_status", columnList = "status"),
        @Index(name = "idx_corporate_contract_corporation", columnList = "corporationId"),
        @Index(name = "idx_corporate_contract_type", columnList = "type")
})
public class CorporateContract {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "payment_credits", nullable = false)
    private Integer paymentCredits;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(name = "duration_turns", nullable = false)
    private Integer durationTurns;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "turns_remaining")
    private Integer turnsRemaining;

    @ElementCollection
    @CollectionTable(name = "corporate_contract_crew", joinColumns = @JoinColumn(name = "contract_id"))
    @Column(name = "crew_id")
    private List<Long> assignedCrewIds = new ArrayList<>();

    @Column(name = "progress_percent")
    private Double progressPercent = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", length = 20)
    private GameType gameType = GameType.TRAVELLER; // Default for backward compatibility

    @Column(name = "universe_display_name", length = 50)
    private String universeDisplayName = "Traveller";

    /**
     * Types of business contracts available in the corporate office.
     */
    public enum ContractType {
        /** Trade deals and commercial agreements */
        TRADE,
        /** Security and protection contracts */
        SECURITY,
        /** Logistics and supply chain operations */
        LOGISTICS,
        /** Special corporate missions */
        MISSION
    }

    /**
     * Contract status lifecycle.
     */
    public enum Status {
        /** Available for acceptance */
        AVAILABLE,
        /** Currently active and being worked on */
        ACTIVE,
        /** Successfully completed */
        COMPLETED,
        /** Failed to complete */
        FAILED,
        /** Expired before completion */
        EXPIRED
    }

    public CorporateContract() {
    }

    public CorporateContract(String title, ContractType type, Integer paymentCredits, Long corporationId) {
        this.title = title;
        this.type = type;
        this.paymentCredits = paymentCredits;
        this.corporationId = corporationId;
        this.xpReward = 50;
        this.difficulty = 3;
        this.durationTurns = 1;
        this.status = Status.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContractType getType() {
        return type;
    }

    public void setType(ContractType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPaymentCredits() {
        return paymentCredits;
    }

    public void setPaymentCredits(Integer paymentCredits) {
        this.paymentCredits = paymentCredits;
    }

    public Integer getXpReward() {
        return xpReward;
    }

    public void setXpReward(Integer xpReward) {
        this.xpReward = xpReward;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDurationTurns() {
        return durationTurns;
    }

    public void setDurationTurns(Integer durationTurns) {
        this.durationTurns = durationTurns;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getTurnsRemaining() {
        return turnsRemaining;
    }

    public void setTurnsRemaining(Integer turnsRemaining) {
        this.turnsRemaining = turnsRemaining;
    }

    public List<Long> getAssignedCrewIds() {
        return assignedCrewIds;
    }

    public void setAssignedCrewIds(List<Long> assignedCrewIds) {
        this.assignedCrewIds = assignedCrewIds;
    }

    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public String getUniverseDisplayName() {
        return universeDisplayName;
    }

    public void setUniverseDisplayName(String universeDisplayName) {
        this.universeDisplayName = universeDisplayName;
    }

    /**
     * Check if this contract has expired.
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if this contract is available for acceptance.
     */
    public boolean isAvailable() {
        return status == Status.AVAILABLE && !isExpired();
    }

    /**
     * Process one turn of contract execution.
     * Decrements turnsRemaining and updates progressPercent.
     * @return true if the contract is complete (turnsRemaining <= 0) and should be finalized
     */
    public boolean processTurn() {
        if (status != Status.ACTIVE) return false;
        if (turnsRemaining == null || turnsRemaining <= 0) return false;
        turnsRemaining--;
        progressPercent = ((double) (durationTurns - turnsRemaining) / durationTurns) * 100.0;
        if (progressPercent > 100.0) progressPercent = 100.0;
        return turnsRemaining <= 0;
    }

    /**
     * Check if the contract is complete (all turns processed).
     */
    public boolean isComplete() {
        return turnsRemaining != null && turnsRemaining <= 0;
    }

    @Override
    public String toString() {
        return "CorporateContract{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", payment=" + paymentCredits +
                ", xp=" + xpReward +
                ", difficulty=" + difficulty +
                ", durationTurns=" + durationTurns +
                ", turnsRemaining=" + turnsRemaining +
                ", progress=" + progressPercent +
                ", status=" + status +
                ", gameType=" + gameType +
                ", universe='" + universeDisplayName + '\'' +
                '}';
    }
}
