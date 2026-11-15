package com.yisy.modules.doramcu.traveller.shipcustomization;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * JPA entity representing a ship component that can be installed on a custom ship design.
 * <p>
 * Components include weapons, armor, drives, sensors, and other ship systems.
 * Each component has a type, stats, and tonnage cost.
 * </p>
 */
@Entity
@Table(name = "traveller_ship_component")
public class ShipComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentType type;

    @Column(nullable = false)
    private String description;

    /** Tonnage cost of this component */
    @Column(nullable = false)
    private int tonnageCost;

    /** Construction cost in credits */
    @Column(nullable = false)
    private long cost;

    /** Minimum tech level required */
    @Column(nullable = false)
    private int minTechLevel;

    // Weapon-specific stats
    /** Damage dice (e.g., 2 for 2D6) */
    private int damageDice;

    /** Damage modifier */
    private int damageModifier;

    /** Optimal range band (0=Adjacent, 1=Close, 2=Short, 3=Medium, 4=Long, 5=Very Long, 6=Distant) */
    private int optimalRange;

    // Drive-specific stats
    /** Drive rating (0-6) */
    private int driveRating;

    // Armor-specific stats
    /** Armor protection factor */
    private int armorFactor;

    // Power plant stats
    /** Power plant rating */
    private int powerRating;

    // Computer stats
    /** Computer model (0-9) */
    private int computerModel;

    // Sensor stats
    /** Sensor rating */
    private int sensorRating;

    // Defense stats
    /** Number of nuclear dampers */
    private int nuclearDampers;

    /** Number of meson screens */
    private int mesonScreens;

    // Capacity stats
    /** Cargo capacity in tons */
    private int cargoCapacity;

    /** Fuel capacity in tons */
    private int fuelCapacity;

    /** Crew capacity */
    private int crewCapacity;

    /** Marine capacity */
    private int marineCapacity;

    public ShipComponent() {
    }

    public ShipComponent(String name, ComponentType type, String description, int tonnageCost, long cost, int minTechLevel) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.type = Objects.requireNonNull(type, "Type must not be null");
        this.description = Objects.requireNonNull(description, "Description must not be null");
        this.tonnageCost = tonnageCost;
        this.cost = cost;
        this.minTechLevel = minTechLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    public ComponentType getType() { return type; }
    public void setType(ComponentType type) { this.type = Objects.requireNonNull(type); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = Objects.requireNonNull(description); }

    public int getTonnageCost() { return tonnageCost; }
    public void setTonnageCost(int tonnageCost) { this.tonnageCost = tonnageCost; }

    public long getCost() { return cost; }
    public void setCost(long cost) { this.cost = cost; }

    public int getMinTechLevel() { return minTechLevel; }
    public void setMinTechLevel(int minTechLevel) { this.minTechLevel = minTechLevel; }

    public int getDamageDice() { return damageDice; }
    public void setDamageDice(int damageDice) { this.damageDice = damageDice; }

    public int getDamageModifier() { return damageModifier; }
    public void setDamageModifier(int damageModifier) { this.damageModifier = damageModifier; }

    public int getOptimalRange() { return optimalRange; }
    public void setOptimalRange(int optimalRange) { this.optimalRange = optimalRange; }

    public int getDriveRating() { return driveRating; }
    public void setDriveRating(int driveRating) { this.driveRating = driveRating; }

    public int getArmorFactor() { return armorFactor; }
    public void setArmorFactor(int armorFactor) { this.armorFactor = armorFactor; }

    public int getPowerRating() { return powerRating; }
    public void setPowerRating(int powerRating) { this.powerRating = powerRating; }

    public int getComputerModel() { return computerModel; }
    public void setComputerModel(int computerModel) { this.computerModel = computerModel; }

    public int getSensorRating() { return sensorRating; }
    public void setSensorRating(int sensorRating) { this.sensorRating = sensorRating; }

    public int getNuclearDampers() { return nuclearDampers; }
    public void setNuclearDampers(int nuclearDampers) { this.nuclearDampers = nuclearDampers; }

    public int getMesonScreens() { return mesonScreens; }
    public void setMesonScreens(int mesonScreens) { this.mesonScreens = mesonScreens; }

    public int getCargoCapacity() { return cargoCapacity; }
    public void setCargoCapacity(int cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    public int getFuelCapacity() { return fuelCapacity; }
    public void setFuelCapacity(int fuelCapacity) { this.fuelCapacity = fuelCapacity; }

    public int getCrewCapacity() { return crewCapacity; }
    public void setCrewCapacity(int crewCapacity) { this.crewCapacity = crewCapacity; }

    public int getMarineCapacity() { return marineCapacity; }
    public void setMarineCapacity(int marineCapacity) { this.marineCapacity = marineCapacity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipComponent that = (ShipComponent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ShipComponent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", tonnageCost=" + tonnageCost +
                '}';
    }
}
