package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing the Younger Industries HQ building.
 * <p>
 * The HQ has 3 upper-level floors for corporate offices and 3 basement levels
 * for configurable dungeon instances. This is the primary landing point for
 * new characters in the Earth 2026 game.
 * </p>
 */
@Entity
@Table(name = "younger_industries_hq", indexes = {
        @Index(name = "idx_yihq_location", columnList = "location_id"),
        @Index(name = "idx_yihq_active", columnList = "active")
})
public class YoungerIndustriesHQ {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "earth2026_seq")
    @SequenceGenerator(name = "earth2026_seq", sequenceName = "earth2026_sequence", allocationSize = 1)
    private Long id;

    /**
     * Name of the HQ building.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The location this HQ is situated at.
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * Number of upper-level office floors.
     */
    @Column(name = "office_floors", nullable = false)
    private int officeFloors = 3;

    /**
     * Number of basement dungeon levels.
     */
    @Column(name = "basement_levels", nullable = false)
    private int basementLevels = 3;

    /**
     * Total number of office units across all floors.
     */
    @Column(name = "total_offices", nullable = false)
    private int totalOffices;

    /**
     * Number of currently occupied offices.
     */
    @Column(name = "occupied_offices", nullable = false)
    private int occupiedOffices = 0;

    /**
     * Description of the HQ building.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Whether the HQ is currently operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * When the HQ was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update time.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hq", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OfficeUnit> officeUnits = new ArrayList<>();

    @OneToMany(mappedBy = "hq", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DungeonInstance> dungeonInstances = new ArrayList<>();

    public YoungerIndustriesHQ() {
    }

    public YoungerIndustriesHQ(String name, Long locationId) {
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

    public int getOfficeFloors() {
        return officeFloors;
    }

    public void setOfficeFloors(int officeFloors) {
        this.officeFloors = officeFloors;
    }

    public int getBasementLevels() {
        return basementLevels;
    }

    public void setBasementLevels(int basementLevels) {
        this.basementLevels = basementLevels;
    }

    public int getTotalOffices() {
        return totalOffices;
    }

    public void setTotalOffices(int totalOffices) {
        this.totalOffices = totalOffices;
    }

    public int getOccupiedOffices() {
        return occupiedOffices;
    }

    public void setOccupiedOffices(int occupiedOffices) {
        this.occupiedOffices = occupiedOffices;
    }

    public double getOccupancyRate() {
        return totalOffices > 0 ? (double) occupiedOffices / totalOffices : 0.0;
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

    public List<OfficeUnit> getOfficeUnits() {
        return officeUnits;
    }

    public void setOfficeUnits(List<OfficeUnit> officeUnits) {
        this.officeUnits = officeUnits;
    }

    public List<DungeonInstance> getDungeonInstances() {
        return dungeonInstances;
    }

    public void setDungeonInstances(List<DungeonInstance> dungeonInstances) {
        this.dungeonInstances = dungeonInstances;
    }

    @Override
    public String toString() {
        return "YoungerIndustriesHQ{id=" + id + ", name='" + name + "', officeFloors=" +
                officeFloors + ", basementLevels=" + basementLevels + ", occupancy=" +
                String.format("%.1f%%", getOccupancyRate() * 100) + "}";
    }
}
