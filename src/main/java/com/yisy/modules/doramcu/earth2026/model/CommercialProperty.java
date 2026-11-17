package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed commercial property information including lease terms, tenant info,
 * maintenance costs, and property value.
 */
@Entity
@Table(name = "commercial_properties", indexes = {
        @Index(name = "idx_commercial_properties_type", columnList = "property_type"),
        @Index(name = "idx_commercial_properties_owner", columnList = "owner_id"),
        @Index(name = "idx_commercial_properties_status", columnList = "status")
})
public class CommercialProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_name", nullable = false, length = 200)
    private String propertyName;

    @Column(name = "property_type", nullable = false, length = 50)
    private String propertyType; // WAREHOUSE, INDUSTRIAL_FACILITY, RETAIL_SPACE, OFFICE_PARK, MIXED_USE

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "planet_id")
    private Long planetId;

    @Column(name = "square_footage")
    private Long squareFootage;

    @Column(name = "zoning_type", length = 50)
    private String zoningType;

    @Column(name = "property_value", precision = 15, scale = 2)
    private BigDecimal propertyValue;

    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "appreciation_rate", precision = 5, scale = 2)
    private BigDecimal appreciationRate;

    @Column(name = "maintenance_cost", precision = 12, scale = 2)
    private BigDecimal maintenanceCost;

    @Column(name = "lease_term_months")
    private Integer leaseTermMonths;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "tenant_name", length = 100)
    private String tenantName;

    @Column(name = "lease_start_date")
    private LocalDateTime leaseStartDate;

    @Column(name = "lease_end_date")
    private LocalDateTime leaseEndDate;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_type", nullable = false, length = 20)
    private String ownerType; // "PLAYER" or "CORPORATION"

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "AVAILABLE", "LEASED", "SOLD"

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public Long getSquareFootage() {
        return squareFootage;
    }

    public void setSquareFootage(Long squareFootage) {
        this.squareFootage = squareFootage;
    }

    public String getZoningType() {
        return zoningType;
    }

    public void setZoningType(String zoningType) {
        this.zoningType = zoningType;
    }

    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(BigDecimal propertyValue) {
        this.propertyValue = propertyValue;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getAppreciationRate() {
        return appreciationRate;
    }

    public void setAppreciationRate(BigDecimal appreciationRate) {
        this.appreciationRate = appreciationRate;
    }

    public BigDecimal getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(BigDecimal maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public Integer getLeaseTermMonths() {
        return leaseTermMonths;
    }

    public void setLeaseTermMonths(Integer leaseTermMonths) {
        this.leaseTermMonths = leaseTermMonths;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public LocalDateTime getLeaseStartDate() {
        return leaseStartDate;
    }

    public void setLeaseStartDate(LocalDateTime leaseStartDate) {
        this.leaseStartDate = leaseStartDate;
    }

    public LocalDateTime getLeaseEndDate() {
        return leaseEndDate;
    }

    public void setLeaseEndDate(LocalDateTime leaseEndDate) {
        this.leaseEndDate = leaseEndDate;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CommercialProperty{" +
                "id=" + id +
                ", propertyName='" + propertyName + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", propertyValue=" + propertyValue +
                ", status='" + status + '\'' +
                '}';
    }
}
