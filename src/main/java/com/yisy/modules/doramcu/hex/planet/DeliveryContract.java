package com.yisy.modules.doramcu.hex.planet;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a cargo delivery contract in the planet-level economy.
 * <p>
 * A delivery contract specifies cargo to be transported from one station to another
 * within a time limit. When completed, the corporation earns credits, XP,
 * and reputation. Delivery contracts are generated based on industry production and
 * settlement demand.
 * </p>
 * <p>
 * Contract types:
 * <ul>
 *   <li>STANDARD — Regular cargo delivery</li>
 *   <li>URGENT — Time-sensitive delivery with higher pay</li>
 *   <li>BULK — Large volume, lower per-unit pay</li>
 *   <li>HAZARDOUS — Dangerous cargo with premium pay</li>
 *   <li>VIP — High-value passenger or mail delivery</li>
 * </ul>
 * </p>
 */
public class DeliveryContract {

    public enum ContractType {
        /** Regular cargo delivery — standard pay and time limit */
        STANDARD("Standard", 1.0, 48),
        /** Time-sensitive delivery — 2x pay, half time limit */
        URGENT("Urgent", 2.0, 24),
        /** Large volume delivery — 0.5x per-unit pay, double time limit */
        BULK("Bulk", 0.5, 96),
        /** Dangerous cargo — 3x pay, standard time limit */
        HAZARDOUS("Hazardous", 3.0, 48),
        /** High-value passenger or mail — 2.5x pay, urgent time */
        VIP("VIP", 2.5, 24);

        private final String displayName;
        private final double payMultiplier;
        private final int timeLimitHours;

        ContractType(String displayName, double payMultiplier, int timeLimitHours) {
            this.displayName = displayName;
            this.payMultiplier = payMultiplier;
            this.timeLimitHours = timeLimitHours;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getPayMultiplier() {
            return payMultiplier;
        }

        public int getTimeLimitHours() {
            return timeLimitHours;
        }
    }

    public enum ContractStatus {
        /** Available for any corporation to accept */
        AVAILABLE,
        /** Accepted by a corporation but not yet completed */
        ACTIVE,
        /** Successfully delivered — credits and XP awarded */
        COMPLETED,
        /** Time limit expired or corporation failed */
        FAILED,
        /** Cancelled by the issuer */
        CANCELLED
    }

    private final String id;
    private final String name;
    private final ContractType type;
    private final CargoType cargoType;
    private final int cargoAmount;
    private final String fromStationId;
    private final String toStationId;
    private final long payCredits;
    private final int xpReward;
    private final int reputationReward;
    private ContractStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final String issuerEmpireId;
    private String assignedCorporationId;
    private LocalDateTime completedAt;

    /**
     * Creates a new delivery contract.
     *
     * @param name the contract display name
     * @param type the contract type
     * @param cargoType the type of cargo to deliver
     * @param cargoAmount the amount of cargo in units
     * @param fromStationId the pickup station ID
     * @param toStationId the delivery station ID
     * @param payCredits credits paid on completion
     * @param xpReward XP awarded on completion
     * @param reputationReward reputation points awarded
     * @param issuerEmpireId the empire that issued the contract
     */
    public DeliveryContract(String name, ContractType type, CargoType cargoType,
                            int cargoAmount, String fromStationId, String toStationId,
                            long payCredits, int xpReward, int reputationReward,
                            String issuerEmpireId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.cargoType = cargoType;
        this.cargoAmount = cargoAmount;
        this.fromStationId = fromStationId;
        this.toStationId = toStationId;
        this.payCredits = payCredits;
        this.xpReward = xpReward;
        this.reputationReward = reputationReward;
        this.status = ContractStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(type.getTimeLimitHours());
        this.issuerEmpireId = issuerEmpireId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ContractType getType() {
        return type;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    public int getCargoAmount() {
        return cargoAmount;
    }

    public String getFromStationId() {
        return fromStationId;
    }

    public String getToStationId() {
        return toStationId;
    }

    public long getPayCredits() {
        return payCredits;
    }

    public int getXpReward() {
        return xpReward;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getIssuerEmpireId() {
        return issuerEmpireId;
    }

    public String getAssignedCorporationId() {
        return assignedCorporationId;
    }

    public void setAssignedCorporationId(String assignedCorporationId) {
        this.assignedCorporationId = assignedCorporationId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Accepts this contract for a corporation.
     *
     * @param corporationId the corporation accepting the contract
     */
    public void accept(String corporationId) {
        if (this.status != ContractStatus.AVAILABLE) {
            throw new IllegalStateException("Contract is not available: " + this.status);
        }
        this.assignedCorporationId = corporationId;
        this.status = ContractStatus.ACTIVE;
    }

    /**
     * Completes this contract successfully.
     */
    public void complete() {
        if (this.status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contract is not active: " + this.status);
        }
        this.status = ContractStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Fails this contract.
     */
    public void fail() {
        if (this.status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contract is not active: " + this.status);
        }
        this.status = ContractStatus.FAILED;
    }

    /**
     * Cancels this contract.
     */
    public void cancel() {
        if (this.status == ContractStatus.COMPLETED || this.status == ContractStatus.CANCELLED) {
            throw new IllegalStateException("Contract cannot be cancelled: " + this.status);
        }
        this.status = ContractStatus.CANCELLED;
    }

    /**
     * Checks if this contract has expired.
     *
     * @return true if the current time is past the expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Calculates the total value of this contract (cargo value * pay multiplier).
     *
     * @return the total contract value in credits
     */
    public long getTotalValue() {
        return (long) (cargoType.getBaseValue() * cargoAmount * type.getPayMultiplier());
    }

    @Override
    public String toString() {
        return "DeliveryContract{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type.getDisplayName() +
                ", cargo=" + cargoType.getDisplayName() +
                ", amount=" + cargoAmount +
                ", pay=" + payCredits +
                ", status=" + status +
                '}';
    }
}
