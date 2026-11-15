package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing an apartment unit within an Apartment Building.
 * <p>
 * Each apartment unit tracks tenancy, rental status, and pricing.
 * </p>
 */
@Entity
@Table(name = "apartment_units", indexes = {
        @Index(name = "idx_au_building", columnList = "building_id"),
        @Index(name = "idx_au_tenant", columnList = "tenant_id"),
        @Index(name = "idx_au_status", columnList = "status")
})
public class ApartmentUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The apartment building this unit belongs to.
     */
    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    /**
     * Floor level (1-10 for upper floors).
     */
    @Column(name = "floor_level", nullable = false)
    private int floorLevel;

    /**
     * Unit number on the floor.
     */
    @Column(name = "unit_number", nullable = false)
    private int unitNumber;

    /**
     * Apartment name/label.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the apartment.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Apartment size in square meters.
     */
    @Column(name = "size_sqm", nullable = false)
    private int sizeSqm;

    /**
     * Number of bedrooms.
     */
    @Column(name = "bedrooms", nullable = false)
    private int bedrooms;

    /**
     * Current status: AVAILABLE, OCCUPIED, RESERVED.
     */
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    /**
     * Character ID that rents this apartment.
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * Monthly rent amount.
     */
    @Column(name = "monthly_rent", nullable = false)
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    /**
     * When the apartment was assigned.
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /**
     * When the apartment was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", insertable = false, updatable = false)
    private ApartmentBuilding apartmentBuilding;

    public ApartmentUnit() {
    }

    public ApartmentUnit(Long buildingId, int floorLevel, int unitNumber, String name) {
        this.buildingId = buildingId;
        this.floorLevel = floorLevel;
        this.unitNumber = unitNumber;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(int unitNumber) {
        this.unitNumber = unitNumber;
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

    public int getSizeSqm() {
        return sizeSqm;
    }

    public void setSizeSqm(int sizeSqm) {
        this.sizeSqm = sizeSqm;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ApartmentBuilding getApartmentBuilding() {
        return apartmentBuilding;
    }

    public void setApartmentBuilding(ApartmentBuilding apartmentBuilding) {
        this.apartmentBuilding = apartmentBuilding;
    }

    @Override
    public String toString() {
        return "ApartmentUnit{id=" + id + ", name='" + name + "', floor=" + floorLevel +
                ", unit=" + unitNumber + ", status='" + status + "'}";
    }
}
