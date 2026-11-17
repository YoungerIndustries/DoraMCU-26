package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an Apartment Building.
 * <p>
 * The Apartment Building has 10 upper-level floors for residential apartments
 * and 10 basement levels for configurable dungeon instances.
 * </p>
 */
@Entity
@Table(name = "apartment_buildings", indexes = {
        @Index(name = "idx_ab_location", columnList = "location_id"),
        @Index(name = "idx_ab_active", columnList = "active")
})
public class ApartmentBuilding {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earth2026_seq")
    @SequenceGenerator(name = "earth2026_seq", sequenceName = "earth2026_sequence", allocationSize = 1)
    private Long id;

    /**
     * Name of the apartment building.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The location this building is situated at.
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * Building level indicating the tier/quality of the building (1-10).
     */
    @Column(name = "building_level", nullable = false)
    private int buildingLevel = 1;

    /**
     * Corporation that owns this building (nullable for public buildings).
     */
    @Column(name = "corporation_id")
    private Long corporationId;

    /**
     * Number of upper-level apartment floors.
     */
    @Column(name = "apartment_floors", nullable = false)
    private int apartmentFloors = 10;

    /**
     * Number of basement dungeon levels.
     */
    @Column(name = "basement_levels", nullable = false)
    private int basementLevels = 10;

    /**
     * Total number of apartment units across all floors.
     */
    @Column(name = "total_apartments", nullable = false)
    private int totalApartments;

    /**
     * Number of currently occupied apartments.
     */
    @Column(name = "occupied_apartments", nullable = false)
    private int occupiedApartments = 0;

    /**
     * Description of the apartment building.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Whether the building is currently operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * When the building was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update time.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "apartmentBuilding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApartmentUnit> apartmentUnits = new ArrayList<>();

    @OneToMany(mappedBy = "apartmentBuilding", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DungeonInstance> dungeonInstances = new ArrayList<>();

    public ApartmentBuilding() {
    }

    public ApartmentBuilding(String name, Long locationId) {
        this.name = name;
        this.locationId = locationId;
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

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public int getBuildingLevel() {
        return buildingLevel;
    }

    public void setBuildingLevel(int buildingLevel) {
        this.buildingLevel = buildingLevel;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public int getApartmentFloors() {
        return apartmentFloors;
    }

    public void setApartmentFloors(int apartmentFloors) {
        this.apartmentFloors = apartmentFloors;
    }

    public int getBasementLevels() {
        return basementLevels;
    }

    public void setBasementLevels(int basementLevels) {
        this.basementLevels = basementLevels;
    }

    public int getTotalApartments() {
        return totalApartments;
    }

    public void setTotalApartments(int totalApartments) {
        this.totalApartments = totalApartments;
    }

    public int getOccupiedApartments() {
        return occupiedApartments;
    }

    public void setOccupiedApartments(int occupiedApartments) {
        this.occupiedApartments = occupiedApartments;
    }

    public double getOccupancyRate() {
        return totalApartments > 0 ? (double) occupiedApartments / totalApartments : 0.0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public List<ApartmentUnit> getApartmentUnits() {
        return apartmentUnits;
    }

    public void setApartmentUnits(List<ApartmentUnit> apartmentUnits) {
        this.apartmentUnits = apartmentUnits;
    }

    public List<DungeonInstance> getDungeonInstances() {
        return dungeonInstances;
    }

    public void setDungeonInstances(List<DungeonInstance> dungeonInstances) {
        this.dungeonInstances = dungeonInstances;
    }

    @Override
    public String toString() {
        return "ApartmentBuilding{id=" + id + ", name='" + name + "', apartmentFloors=" +
                apartmentFloors + ", basementLevels=" + basementLevels + ", occupancy=" +
                String.format("%.1f%%", getOccupancyRate() * 100) + "}";
    }
}
