package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing an office unit within a Younger Industries HQ.
 * <p>
 * Each office unit tracks ownership, rental status, and pricing.
 * </p>
 */
@Entity
@Table(name = "office_units", indexes = {
        @Index(name = "idx_ou_hq", columnList = "hq_id"),
        @Index(name = "idx_ou_owner", columnList = "owner_id"),
        @Index(name = "idx_ou_status", columnList = "status")
})
public class OfficeUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The HQ this office belongs to.
     */
    @Column(name = "hq_id", nullable = false)
    private Long hqId;

    /**
     * Floor level (1-3 for upper floors).
     */
    @Column(name = "floor_level", nullable = false)
    private int floorLevel;

    /**
     * Unit number on the floor.
     */
    @Column(name = "unit_number", nullable = false)
    private int unitNumber;

    /**
     * Office name/label.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the office.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Office size in square meters.
     */
    @Column(name = "size_sqm", nullable = false)
    private int sizeSqm;

    /**
     * Current status: AVAILABLE, OCCUPIED, RESERVED.
     */
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    /**
     * Character ID that owns/rents this office.
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * Whether this is owned (true) or rented (false).
     */
    @Column(name = "is_owned", nullable = false)
    private boolean isOwned = false;

    /**
     * Monthly rent amount.
     */
    @Column(name = "monthly_rent", nullable = false)
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    /**
     * Purchase price.
     */
    @Column(name = "purchase_price", nullable = false)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    /**
     * When the office was assigned.
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /**
     * When the office was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hq_id", insertable = false, updatable = false)
    private YoungerIndustriesHQ hq;

    public OfficeUnit() {
    }

    public OfficeUnit(Long hqId, int floorLevel, int unitNumber, String name) {
        this.hqId = hqId;
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

    public Long getHqId() {
        return hqId;
    }

    public void setHqId(Long hqId) {
        this.hqId = hqId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwned() {
        return isOwned;
    }

    public void setOwned(boolean owned) {
        isOwned = owned;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
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

    public YoungerIndustriesHQ getHq() {
        return hq;
    }

    public void setHq(YoungerIndustriesHQ hq) {
        this.hq = hq;
    }

    @Override
    public String toString() {
        return "OfficeUnit{id=" + id + ", name='" + name + "', floor=" + floorLevel +
                ", unit=" + unitNumber + ", status='" + status + "'}";
    }
}
