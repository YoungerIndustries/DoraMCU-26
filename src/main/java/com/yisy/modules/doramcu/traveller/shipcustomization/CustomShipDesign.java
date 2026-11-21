package com.yisy.modules.doramcu.traveller.shipcustomization;
import jakarta.persistence.*;

import java.util.*;

/**
 * JPA entity representing a player-created custom ship design.
 * <p>
 * A custom ship design consists of a base hull size and installed components
 * in various slots. The service validates that the design is within tonnage
 * limits and calculates derived stats like hull points, agility, and thrust.
 * </p>
 */
@Entity
@Table(name = "traveller_custom_ship_design")
public class CustomShipDesign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Owner identifier (player ID or character ID) */
    @Column(nullable = false)
    private String ownerId;

    /** Description of the ship design */
    private String description;

    /** Base hull size in tons (100 to 1,000,000) */
    @Column(nullable = false)
    private int totalTonnage;

    /** Tech level of the design */
    @Column(nullable = false)
    private int techLevel;

    /** Installed components as JSON */
    @ElementCollection
    @CollectionTable(name = "traveller_custom_ship_components", joinColumns = @JoinColumn(name = "design_id"))
    @MapKeyColumn(name = "slot_name")
    @Column(name = "component_id")
    private Map<String, Long> componentSlots = new HashMap<>();

    // Derived stats (calculated by service)
    private int hullPoints;
    private int thrust;
    private int agility;
    private int energyPoints;
    private int cargoCapacity;
    private int crew;
    private int marines;

    @Column(nullable = false)
    private long constructionCost;

    /** Whether the design has been validated */
    @Column(nullable = false)
    private boolean validated;

    public CustomShipDesign() {
    }

    public CustomShipDesign(String name, String ownerId, int totalTonnage, int techLevel) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID must not be null");
        this.totalTonnage = totalTonnage;
        this.techLevel = techLevel;
        this.validated = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = Objects.requireNonNull(ownerId); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalTonnage() { return totalTonnage; }
    public void setTotalTonnage(int totalTonnage) {
        if (totalTonnage < 100 || totalTonnage > 1000000) {
            throw new IllegalArgumentException("Tonnage must be between 100 and 1,000,000");
        }
        this.totalTonnage = totalTonnage;
    }

    public int getTechLevel() { return techLevel; }
    public void setTechLevel(int techLevel) {
        this.techLevel = Math.max(6, Math.min(15, techLevel));
    }

    public Map<String, Long> getComponentSlots() { return componentSlots; }
    public void setComponentSlots(Map<String, Long> componentSlots) { this.componentSlots = componentSlots; }

    /**
     * Add a component to a slot.
     */
    public void addComponent(String slotName, Long componentId) {
        componentSlots.put(slotName, componentId);
    }

    /**
     * Remove a component from a slot.
     */
    public Long removeComponent(String slotName) {
        return componentSlots.remove(slotName);
    }

    /**
     * Get the component ID in a specific slot.
     */
    public Long getComponent(String slotName) {
        return componentSlots.get(slotName);
    }

    public int getHullPoints() { return hullPoints; }
    public void setHullPoints(int hullPoints) { this.hullPoints = hullPoints; }

    public int getThrust() { return thrust; }
    public void setThrust(int thrust) { this.thrust = thrust; }

    public int getAgility() { return agility; }
    public void setAgility(int agility) { this.agility = agility; }

    public int getEnergyPoints() { return energyPoints; }
    public void setEnergyPoints(int energyPoints) { this.energyPoints = energyPoints; }

    public int getCargoCapacity() { return cargoCapacity; }
    public void setCargoCapacity(int cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }

    public int getMarines() { return marines; }
    public void setMarines(int marines) { this.marines = marines; }

    public long getConstructionCost() { return constructionCost; }
    public void setConstructionCost(long constructionCost) { this.constructionCost = constructionCost; }

    public boolean isValidated() { return validated; }
    public void setValidated(boolean validated) { this.validated = validated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomShipDesign that = (CustomShipDesign) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CustomShipDesign{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", totalTonnage=" + totalTonnage +
                ", hullPoints=" + hullPoints +
                '}';
    }
}
