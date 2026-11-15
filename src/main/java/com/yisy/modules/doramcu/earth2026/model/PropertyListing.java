package com.yisy.modules.doramcu.earth2026.model;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a property listing for buy or rent.
 * <p>
 * System-managed listings with dynamic pricing based on market conditions.
 * </p>
 */
@Entity
@Table(name = "property_listings", indexes = {
        @Index(name = "idx_pl_type", columnList = "property_type"),
        @Index(name = "idx_pl_status", columnList = "status"),
        @Index(name = "idx_pl_building", columnList = "building_id")
})
@Component
public class PropertyListing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Property name.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the property.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Property type: Office, Apartment.
     */
    @Column(name = "property_type", nullable = false, length = 50)
    private String propertyType;

    /**
     * Building name.
     */
    @Column(name = "building_name", nullable = false, length = 200)
    private String buildingName;

    /**
     * Building ID this property belongs to.
     */
    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    /**
     * Floor level.
     */
    @Column(name = "floor_level", nullable = false)
    private int floorLevel;

    /**
     * Unit ID (OfficeUnit or ApartmentUnit ID).
     */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    /**
     * Purchase price (system-managed).
     */
    @Column(name = "buy_price", nullable = false)
    private BigDecimal buyPrice = BigDecimal.ZERO;

    /**
     * Monthly rent price (system-managed).
     */
    @Column(name = "rent_price", nullable = false)
    private BigDecimal rentPrice = BigDecimal.ZERO;

    /**
     * Current status: AVAILABLE, SOLD, RENTED, EXPIRED.
     */
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    /**
     * Character ID that owns/rents this property.
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * When the listing was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last price update time.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PropertyListing() {
    }

    public PropertyListing(String name, String propertyType, String buildingName, Long buildingId,
                           int floorLevel, Long unitId) {
        this.name = name;
        this.propertyType = propertyType;
        this.buildingName = buildingName;
        this.buildingId = buildingId;
        this.floorLevel = floorLevel;
        this.unitId = unitId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
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

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(BigDecimal rentPrice) {
        this.rentPrice = rentPrice;
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
        return "PropertyListing{id=" + id + ", name='" + name + "', type='" + propertyType +
                "', status='" + status + "', buyPrice=" + buyPrice + "}";
    }
}
