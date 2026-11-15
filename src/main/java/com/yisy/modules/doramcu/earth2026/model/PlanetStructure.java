package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a planet-level structure in the Earth 2026 module.
 * <p>
 * Planet structures are infrastructure that exists independently of any corporation:
 * mines, equipment stores, transport stations, spaceports, factories, and residences.
 * These structures form the backbone of the planetary economy and transport network.
 * </p>
 * <p>
 * Key mechanics:
 * - Structure Type: MINE, EQUIPMENT_STORE, TRANSPORT_STATION, SPACEPORT, FACTORY, RESIDENCE
 * - Geographic Location: latitude, longitude, elevation for 3D placement
 * - Resources: JSON string storing resource quantities (ore, water, materials, etc.)
 * - Capacity: Maximum throughput or storage capacity
 * - Active State: Whether the structure is operational
 * </p>
 */
@Entity
@Table(name = "planet_structures", indexes = {
        @Index(name = "idx_planet_structure_type", columnList = "structureType"),
        @Index(name = "idx_planet_structure_planet", columnList = "planetId"),
        @Index(name = "idx_planet_structure_active", columnList = "active"),
        @Index(name = "idx_planet_structure_location", columnList = "latitude,longitude")
})
public class PlanetStructure {

    public enum StructureType {
        MINE("Mine", "Extracts raw materials from the planet"),
        EQUIPMENT_STORE("Equipment Store", "Sells equipment and supplies to players"),
        TRANSPORT_STATION("Transport Station", "Hub for buses, trolleys, and trains"),
        SPACEPORT("Spaceport", "Launch and landing facility for space travel"),
        FACTORY("Factory", "Manufactures items from raw materials"),
        RESIDENCE("Residence", "Living quarters for NPCs and players");

        private final String displayName;
        private final String description;

        StructureType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the structure.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of structure: MINE, EQUIPMENT_STORE, TRANSPORT_STATION, SPACEPORT, FACTORY, RESIDENCE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "structure_type", nullable = false, length = 30)
    private StructureType structureType;

    /**
     * Planet ID this structure belongs to.
     */
    @Column(nullable = false)
    private Long planetId;

    /**
     * Latitude coordinate for 3D placement.
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * Longitude coordinate for 3D placement.
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * Elevation above sea level in meters.
     */
    private Double elevation;

    /**
     * JSON string storing resource quantities.
     * Example: {"ore": 1000, "water": 500, "materials": 200}
     */
    @Column(columnDefinition = "TEXT")
    private String resourcesJson;

    /**
     * Maximum throughput or storage capacity.
     */
    private Integer capacity;

    /**
     * Whether the structure is currently operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Description of the structure.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * When the structure was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the structure was last updated.
     */
    @Column(name = "updated_at")
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StructureType getStructureType() {
        return structureType;
    }

    public void setStructureType(StructureType structureType) {
        this.structureType = structureType;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
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

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public String getResourcesJson() {
        return resourcesJson;
    }

    public void setResourcesJson(String resourcesJson) {
        this.resourcesJson = resourcesJson;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "PlanetStructure{name='" + name + "', type=" + structureType +
                ", planetId=" + planetId + ", active=" + active + "}";
    }
}
