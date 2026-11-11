package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a contract for planet surface operations.
 * <p>
 * A contract defines a specific building or development task on a planet surface,
 * including the type of work, target location, required resources, payment,
 * and completion criteria. Contracts are issued by an Empire and can be
 * assigned to characters, AI agents, or automated systems.
 * </p>
 * <p>
 * Contract lifecycle:
 * <pre>
 *   AVAILABLE → ACTIVE → COMPLETED
 *                ↘ FAILED
 *                ↘ EXPIRED
 * </pre>
 * </p>
 */
public class PlanetContract {

    private final String id;
    private final PlanetContractType type;
    private final String title;
    private final String description;
    private final int planetId;
    private final String issuerEmpireId;

    /** Target hex cell for the contract (e.g., settlement location) */
    private final Hex3D targetHex;

    /** Secondary target hex (e.g., end point of a road) */
    private final Hex3D secondaryHex;

    /** Payment in credits for completing this contract */
    private final int paymentCredits;

    /** XP reward for completing this contract */
    private final int xpReward;

    /** Difficulty rating (1-10) */
    private final int difficulty;

    /** Number of turns to complete the contract */
    private final int durationTurns;

    /** Resources required to complete the contract */
    private final Map<CargoType, Integer> requiredResources;

    /** Current status of the contract */
    private ContractStatus status;

    /** Empire ID assigned to execute this contract */
    private String assigneeEmpireId;

    /** ID of the entity created by this contract (settlement, industry, etc.) */
    private String resultEntityId;

    /** Number of turns remaining */
    private int turnsRemaining;

    /** When the contract was created */
    private final LocalDateTime createdAt;

    /** When the contract was completed or failed */
    private LocalDateTime completedAt;

    /** Expiry time for the contract */
    private LocalDateTime expiryDate;

    /** Minimum reputation required to accept this contract */
    private int minReputation;

    /** Minimum corporation rank required to accept this contract */
    private CorporationRank minRank;

    /**
     * Contract status during its lifecycle.
     */
    public enum ContractStatus {
        /** Contract is available for acceptance */
        AVAILABLE,
        /** Contract is being executed */
        ACTIVE,
        /** Contract was completed successfully */
        COMPLETED,
        /** Contract failed */
        FAILED,
        /** Contract expired before completion */
        EXPIRED
    }

    /**
     * Creates a new planet contract.
     *
     * @param id                unique contract ID
     * @param type              type of contract
     * @param title             human-readable title
     * @param description       detailed description
     * @param planetId          target planet ID
     * @param issuerEmpireId    empire issuing the contract
     * @param targetHex         target hex cell, or null for planet-wide contracts
     * @param secondaryHex      secondary target hex, or null
     * @param paymentCredits    payment for completion
     * @param xpReward          XP reward
     * @param difficulty        difficulty (1-10)
     * @param durationTurns     turns to complete
     */
    public PlanetContract(String id, PlanetContractType type, String title, String description,
                           int planetId, String issuerEmpireId, Hex3D targetHex, Hex3D secondaryHex,
                           int paymentCredits, int xpReward, int difficulty, int durationTurns) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Contract ID must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Contract type must not be null");
        }
        if (paymentCredits < 0) {
            throw new IllegalArgumentException("Payment must be non-negative: " + paymentCredits);
        }
        if (difficulty < 1 || difficulty > 10) {
            throw new IllegalArgumentException("Difficulty must be between 1 and 10: " + difficulty);
        }
        if (durationTurns < 1) {
            throw new IllegalArgumentException("Duration must be positive: " + durationTurns);
        }
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.planetId = planetId;
        this.issuerEmpireId = issuerEmpireId;
        this.targetHex = targetHex;
        this.secondaryHex = secondaryHex;
        this.paymentCredits = paymentCredits;
        this.xpReward = xpReward;
        this.difficulty = difficulty;
        this.durationTurns = durationTurns;
        this.requiredResources = new HashMap<>();
        this.status = ContractStatus.AVAILABLE;
        this.turnsRemaining = durationTurns;
        this.createdAt = LocalDateTime.now();
        this.minReputation = 0;
        this.minRank = CorporationRank.FREELANCER;
    }

    /**
     * Creates a simplified contract for GalaxyOverlordAI use.
     *
     * @param id unique contract ID
     * @param type contract type
     * @param issuerEmpireId empire ID that issues the contract
     * @param paymentCredits payment for completion
     * @param xpReward XP reward
     * @param durationTurns turns to complete
     */
    public PlanetContract(String id, PlanetContractType type, int issuerEmpireId,
                           int paymentCredits, int xpReward, int durationTurns) {
        this.id = Objects.requireNonNull(id, "Contract ID must not be null");
        this.type = Objects.requireNonNull(type, "Contract type must not be null");
        this.title = type.getLabel();
        this.description = type.getDescription();
        this.planetId = 0;
        this.issuerEmpireId = String.valueOf(issuerEmpireId);
        this.targetHex = null;
        this.secondaryHex = null;
        this.paymentCredits = paymentCredits;
        this.xpReward = xpReward;
        this.difficulty = Math.max(1, Math.min(10, durationTurns));
        this.durationTurns = Math.max(1, durationTurns);
        this.requiredResources = new HashMap<>();
        this.status = ContractStatus.AVAILABLE;
        this.turnsRemaining = this.durationTurns;
        this.createdAt = LocalDateTime.now();
        this.minReputation = 0;
        this.minRank = CorporationRank.FREELANCER;
    }

    /** @return the unique contract ID */
    public String getId() {
        return id;
    }

    /** @return the contract type */
    public PlanetContractType getType() {
        return type;
    }

    /** @return the contract title */
    public String getTitle() {
        return title;
    }

    /** @return the contract description */
    public String getDescription() {
        return description;
    }

    /** @return the target planet ID */
    public int getPlanetId() {
        return planetId;
    }

    /** @return the empire that issued this contract */
    public String getIssuerEmpireId() {
        return issuerEmpireId;
    }

    /** @return the target hex cell, or null */
    public Hex3D getTargetHex() {
        return targetHex;
    }

    /** @return the secondary target hex, or null */
    public Hex3D getSecondaryHex() {
        return secondaryHex;
    }

    /** @return the payment in credits */
    public int getPaymentCredits() {
        return paymentCredits;
    }

    /** @return the XP reward */
    public int getXpReward() {
        return xpReward;
    }

    /** @return the difficulty (1-10) */
    public int getDifficulty() {
        return difficulty;
    }

    /** @return the duration in turns */
    public int getDurationTurns() {
        return durationTurns;
    }

    /** @return the minimum reputation required to accept this contract */
    public int getMinReputation() {
        return minReputation;
    }

    /**
     * Sets the minimum reputation required to accept this contract.
     *
     * @param minReputation the minimum reputation (0-100)
     */
    public void setMinReputation(int minReputation) {
        this.minReputation = Math.max(0, Math.min(100, minReputation));
    }

    /** @return the minimum corporation rank required to accept this contract */
    public CorporationRank getMinRank() {
        return minRank != null ? minRank : CorporationRank.FREELANCER;
    }

    /**
     * Sets the minimum corporation rank required to accept this contract.
     *
     * @param minRank the minimum rank
     */
    public void setMinRank(CorporationRank minRank) {
        this.minRank = minRank;
    }

    /** @return the required resources */
    public Map<CargoType, Integer> getRequiredResources() {
        return Collections.unmodifiableMap(requiredResources);
    }

    /**
     * Adds a required resource to this contract.
     *
     * @param cargoType the cargo type
     * @param amount    the amount required
     */
    public void addRequiredResource(CargoType cargoType, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative: " + amount);
        }
        requiredResources.merge(cargoType, amount, Integer::sum);
    }

    /** @return the current status */
    public ContractStatus getStatus() {
        return status;
    }

    /** @return the assignee empire ID, or null */
    public String getAssigneeEmpireId() {
        return assigneeEmpireId;
    }

    /**
     * Accepts the contract for execution.
     *
     * @param assigneeEmpireId the empire accepting the contract
     * @return true if the contract was accepted
     */
    public boolean accept(String assigneeEmpireId) {
        if (status != ContractStatus.AVAILABLE) {
            return false;
        }
        this.assigneeEmpireId = assigneeEmpireId;
        this.status = ContractStatus.ACTIVE;
        return true;
    }

    /**
     * Completes the contract successfully.
     *
     * @param resultEntityId the ID of the entity created by this contract
     */
    public void complete(String resultEntityId) {
        if (status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contract is not active: " + status);
        }
        this.status = ContractStatus.COMPLETED;
        this.resultEntityId = resultEntityId;
        this.completedAt = LocalDateTime.now();
        this.turnsRemaining = 0;
    }

    /** Marks the contract as failed. */
    public void fail() {
        if (status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contract is not active: " + status);
        }
        this.status = ContractStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.turnsRemaining = 0;
    }

    /**
     * Processes one turn for this contract.
     *
     * @return true if the contract is complete
     */
    public boolean processTurn() {
        if (status != ContractStatus.ACTIVE) {
            return false;
        }
        turnsRemaining--;
        if (turnsRemaining <= 0) {
            // Contract duration expired without completion
            this.status = ContractStatus.EXPIRED;
            this.completedAt = LocalDateTime.now();
            return true;
        }
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) {
            this.status = ContractStatus.EXPIRED;
            this.completedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /** @return the result entity ID, or null */
    public String getResultEntityId() {
        return resultEntityId;
    }

    /** @return the turns remaining */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return the completion timestamp, or null */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /** @return the expiry date, or null */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date for this contract.
     *
     * @param expiryDate the expiry date
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /** @return true if the contract is available for acceptance */
    public boolean isAvailable() {
        return status == ContractStatus.AVAILABLE;
    }

    /** @return true if the contract is active */
    public boolean isActive() {
        return status == ContractStatus.ACTIVE;
    }

    /** @return true if the contract is completed */
    public boolean isCompleted() {
        return status == ContractStatus.COMPLETED;
    }

    /** @return true if the contract has expired */
    public boolean isExpired() {
        return status == ContractStatus.EXPIRED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanetContract that = (PlanetContract) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("PlanetContract{%s, type=%s, status=%s, planet=%d, payment=%d}",
                id, type, status, planetId, paymentCredits);
    }
}
