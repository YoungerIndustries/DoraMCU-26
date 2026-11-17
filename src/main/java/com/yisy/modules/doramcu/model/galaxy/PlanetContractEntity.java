package com.yisy.modules.doramcu.model.galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a Planet Contract on a planet surface.
 * <p>
 * Contracts specify cargo delivery tasks between stations. When completed,
 * the assigned corporation earns credits, XP, and reputation.
 * </p>
 */
@Entity
@Table(name = "planet_contracts", indexes = {
    @Index(name = "idx_contract_planet", columnList = "planet_id"),
    @Index(name = "idx_contract_corp", columnList = "assigned_corporation_id"),
    @Index(name = "idx_contract_status", columnList = "status")
})
public class PlanetContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique contract ID (matches PlanetContract.id, UUID string). */
    @Column(name = "contract_id", nullable = false, unique = true, length = 100)
    private String contractId;

    /** The planet this contract is on. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Contract display name. */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Contract type (STANDARD, URGENT, BULK, HAZARDOUS, VIP). */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /** Cargo type to deliver. */
    @Column(name = "cargo_type", nullable = false, length = 30)
    private String cargoType;

    /** Amount of cargo in units. */
    @Column(name = "cargo_amount", nullable = false)
    private int cargoAmount;

    /** Pickup station ID. */
    @Column(name = "from_station_id", nullable = false, length = 100)
    private String fromStationId;

    /** Delivery station ID. */
    @Column(name = "to_station_id", nullable = false, length = 100)
    private String toStationId;

    /** Credits paid on completion. */
    @Column(name = "pay_credits", nullable = false)
    private long payCredits;

    /** XP awarded on completion. */
    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    /** Reputation points awarded on completion. */
    @Column(name = "reputation_reward", nullable = false)
    private int reputationReward;

    /** Contract status (AVAILABLE, ACTIVE, COMPLETED, FAILED, CANCELLED). */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Empire that issued the contract. */
    @Column(name = "issuer_empire_id", nullable = false, length = 100)
    private String issuerEmpireId;

    /** Corporation currently assigned to this contract. */
    @Column(name = "assigned_corporation_id", length = 100)
    private String assignedCorporationId;

    /** When the contract was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When the contract expires. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** When the contract was completed (nullable). */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** When this record was created in DB. */
    @Column(name = "db_created_at", nullable = false)
    private LocalDateTime dbCreatedAt;

    /** When this record was last updated in DB. */
    @Column(name = "db_updated_at", nullable = false)
    private LocalDateTime dbUpdatedAt;

    public PlanetContractEntity() {
        this.dbCreatedAt = LocalDateTime.now();
        this.dbUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public int getCargoAmount() {
        return cargoAmount;
    }

    public void setCargoAmount(int cargoAmount) {
        this.cargoAmount = cargoAmount;
    }

    public String getFromStationId() {
        return fromStationId;
    }

    public void setFromStationId(String fromStationId) {
        this.fromStationId = fromStationId;
    }

    public String getToStationId() {
        return toStationId;
    }

    public void setToStationId(String toStationId) {
        this.toStationId = toStationId;
    }

    public long getPayCredits() {
        return payCredits;
    }

    public void setPayCredits(long payCredits) {
        this.payCredits = payCredits;
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

    public String getIssuerEmpireId() {
        return issuerEmpireId;
    }

    public void setIssuerEmpireId(String issuerEmpireId) {
        this.issuerEmpireId = issuerEmpireId;
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

    public LocalDateTime getDbCreatedAt() {
        return dbCreatedAt;
    }

    public void setDbCreatedAt(LocalDateTime dbCreatedAt) {
        this.dbCreatedAt = dbCreatedAt;
    }

    public LocalDateTime getDbUpdatedAt() {
        return dbUpdatedAt;
    }

    public void setDbUpdatedAt(LocalDateTime dbUpdatedAt) {
        this.dbUpdatedAt = dbUpdatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.dbCreatedAt = LocalDateTime.now();
        this.dbUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dbUpdatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PlanetContractEntity{" +
                "id=" + id +
                ", contractId='" + contractId + '\'' +
                ", planetId=" + planetId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
