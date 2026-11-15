package com.yisy.modules.doramcu.hex.planet.dto;

import java.time.LocalDateTime;

/**
 * DTO representing a delivery contract on a planet.
 */
public class ContractDto {

    private String id;
    private String name;
    private String type;
    private String cargoType;
    private int cargoAmount;
    private String fromStationId;
    private String toStationId;
    private long payCredits;
    private int xpReward;
    private int reputationReward;
    private String status;
    private String issuerEmpireId;
    private String assignedCorporationId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
    private long totalValue;

    public ContractDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(long totalValue) {
        this.totalValue = totalValue;
    }
}
