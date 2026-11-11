package com.yisy.modules.doramcu.zero_zero_seven.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a covert operation in the 007 game.
 * <p>
 * Covert operations are the core espionage mechanic: sabotage, assassination, intelligence
 * gathering, propaganda, and infiltration missions. Each operation has a difficulty,
 * reward, and can succeed, fail, or be detected.
 * </p>
 */
@Entity
@Table(name = "bond_covert_operations")
public class CovertOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Description of the operation. */
    @Column(length = 500)
    private String description;

    /** Operation type. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationType type;

    /** Owner empire ID (the empire launching the operation). */
    @Column(name = "owner_empire_id", nullable = false)
    private Long ownerEmpireId;

    /** Target empire ID (the empire being targeted). */
    @Column(name = "target_empire_id", nullable = false)
    private Long targetEmpireId;

    /** Assigned agent ID. */
    @Column(name = "assigned_agent_id", nullable = false)
    private Long assignedAgentId;

    /** Current status of the operation. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationStatus status = OperationStatus.PLANNED;

    /** Difficulty level (1-10). */
    @Column(nullable = false)
    private int difficulty = 5;

    /** Reward points on completion. */
    @Column(nullable = false)
    private int reward = 0;

    /** Success probability (0-100). */
    @Column(name = "success_probability", nullable = false)
    private int successProbability = 50;

    /** Turns remaining to complete. */
    @Column(name = "turns_remaining", nullable = false)
    private int turnsRemaining = 0;

    /** Timestamp when the operation was created. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the operation was completed. */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public CovertOperation() {
    }

    public CovertOperation(String name, OperationType type, Long ownerEmpireId, Long targetEmpireId,
                           Long assignedAgentId, BondGameSession gameSession) {
        this.name = name;
        this.type = type;
        this.ownerEmpireId = ownerEmpireId;
        this.targetEmpireId = targetEmpireId;
        this.assignedAgentId = assignedAgentId;
        this.gameSession = gameSession;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OperationType getType() { return type; }
    public void setType(OperationType type) { this.type = type; }

    public Long getOwnerEmpireId() { return ownerEmpireId; }
    public void setOwnerEmpireId(Long ownerEmpireId) { this.ownerEmpireId = ownerEmpireId; }

    public Long getTargetEmpireId() { return targetEmpireId; }
    public void setTargetEmpireId(Long targetEmpireId) { this.targetEmpireId = targetEmpireId; }

    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public OperationStatus getStatus() { return status; }
    public void setStatus(OperationStatus status) { this.status = status; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = Math.max(1, Math.min(10, difficulty)); }

    public int getReward() { return reward; }
    public void setReward(int reward) { this.reward = reward; }

    public int getSuccessProbability() { return successProbability; }
    public void setSuccessProbability(int successProbability) {
        this.successProbability = Math.max(0, Math.min(100, successProbability));
    }

    public int getTurnsRemaining() { return turnsRemaining; }
    public void setTurnsRemaining(int turnsRemaining) { this.turnsRemaining = turnsRemaining; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Advance the operation by one turn.
     */
    public void advanceTurn() {
        if (status == OperationStatus.IN_PROGRESS && turnsRemaining > 0) {
            this.turnsRemaining--;
            if (turnsRemaining <= 0) {
                this.completedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Check if the operation is active (planned or in progress).
     */
    public boolean isActive() {
        return status == OperationStatus.PLANNED || status == OperationStatus.IN_PROGRESS;
    }

    public enum OperationType {
        SABOTAGE,
        ASSASSINATION,
        INTEL_GATHERING,
        PROPAGANDA,
        INFILTRATION
    }

    public enum OperationStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        DETECTED
    }
}
