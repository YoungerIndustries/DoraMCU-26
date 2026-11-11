package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a structure within an Earth 2026 location.
 * <p>
 * Structures are buildings, fortifications, or installations that provide
 * tactical value in battle environments. Each structure has its own tech level
 * (which may differ from the planet's base tech level), defense rating,
 * and capacity.
 * </p>
 * <p>
 * For example, a WWII-era location might have bunkers (TL4), command centers (TL4),
 * and airfields (TL4), while a modern location might have a TL6 research lab
 * alongside TL5 civilian buildings.
 * </p>
 */
@Entity
@Table(name = "earth2026_structures", indexes = {
        @Index(name = "idx_earth2026_structure_location", columnList = "location_id"),
        @Index(name = "idx_earth2026_structure_type", columnList = "structure_type"),
        @Index(name = "idx_earth2026_structure_tl", columnList = "tech_level")
})
public class Earth2026Structure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Structure name (e.g., "Eisenhower Executive Office Building", "Bunker 7").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The location this structure belongs to.
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * Structure type (e.g., "Bunker", "Airfield", "Port", "City", "Bridge",
     * "Command Center", "U-boat Pen", "Factory", "Hospital").
     */
    @Column(name = "structure_type", nullable = false, length = 100)
    private String structureType;

    /**
     * Tech level of this specific structure (may differ from planet base TL).
     */
    @Column(name = "tech_level", nullable = false)
    private int techLevel;

    /**
     * Defense rating (0-100). Higher values mean harder to destroy.
     */
    @Column(name = "defense_rating", nullable = false)
    private int defenseRating;

    /**
     * Maximum number of units this structure can hold.
     */
    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    /**
     * Current number of units occupying this structure.
     */
    @Column(name = "current_capacity", nullable = false)
    private int currentCapacity = 0;

    /**
     * Whether this structure provides cover for defending units.
     */
    @Column(name = "provides_cover", nullable = false)
    private boolean providesCover;

    /**
     * Cover bonus for defending units (added to defense calculation).
     */
    @Column(name = "cover_bonus", nullable = false)
    private int coverBonus = 0;

    /**
     * Whether this structure is a strategic objective.
     */
    @Column(name = "is_strategic", nullable = false)
    private boolean isStrategic;

    /**
     * Description of the structure.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether this structure is currently active/operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    public Earth2026Structure() {
    }

    public Earth2026Structure(String name, Long locationId, String structureType, int techLevel) {
        this.name = name;
        this.locationId = locationId;
        this.structureType = structureType;
        this.techLevel = techLevel;
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

    public String getStructureType() {
        return structureType;
    }

    public void setStructureType(String structureType) {
        this.structureType = structureType;
    }

    public int getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(int techLevel) {
        this.techLevel = techLevel;
    }

    public TechLevel getTechLevelEnum() {
        return TechLevel.fromLevel(techLevel);
    }

    public int getDefenseRating() {
        return defenseRating;
    }

    public void setDefenseRating(int defenseRating) {
        this.defenseRating = defenseRating;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(int currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public boolean isProvidesCover() {
        return providesCover;
    }

    public void setProvidesCover(boolean providesCover) {
        this.providesCover = providesCover;
    }

    public int getCoverBonus() {
        return coverBonus;
    }

    public void setCoverBonus(int coverBonus) {
        this.coverBonus = coverBonus;
    }

    public boolean isStrategic() {
        return isStrategic;
    }

    public void setStrategic(boolean strategic) {
        isStrategic = strategic;
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

    /**
     * Check if this structure can accommodate more units.
     */
    public boolean hasCapacity(int units) {
        return (currentCapacity + units) <= maxCapacity;
    }

    /**
     * Add units to this structure.
     *
     * @param units the number of units to add
     * @return true if units were added, false if capacity exceeded
     */
    public boolean addUnits(int units) {
        if (hasCapacity(units)) {
            currentCapacity += units;
            return true;
        }
        return false;
    }

    /**
     * Remove units from this structure.
     *
     * @param units the number of units to remove
     * @return the actual number of units removed
     */
    public int removeUnits(int units) {
        int removed = Math.min(units, currentCapacity);
        currentCapacity -= removed;
        return removed;
    }

    @Override
    public String toString() {
        return "Earth2026Structure{id=" + id + ", name='" + name + "', type='" +
                structureType + "', TL=" + techLevel + ", defense=" + defenseRating + "}";
    }
}
