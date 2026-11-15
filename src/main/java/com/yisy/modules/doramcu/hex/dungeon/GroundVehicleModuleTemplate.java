package com.yisy.modules.doramcu.hex.dungeon;

import jakarta.persistence.*;

/**
 * JPA entity representing a reusable ground combat vehicle module template.
 * <p>
 * A ground vehicle module template defines a pre-built vehicle section (tank hull,
 * turret, engine bay, etc.) that can be instantiated multiple times when building
 * ground combat vehicles. Each template stores its shape, compartment layout, and
 * connection points as serialized JSON data, along with combat-specific stats.
 * </p>
 * <p>
 * Templates are stored in the database and used by {@link com.yisy.modules.doramcu.hex.GroundVehicleBuilder}
 * to assemble {@link com.yisy.modules.doramcu.hex.GroundVehicle} instances with 3D structure
 * instead of flat enum-based types.
 * </p>
 */
@Entity
@Table(name = "ground_vehicle_module_templates", indexes = {
        @Index(name = "idx_ground_vehicle_module_type", columnList = "type"),
        @Index(name = "idx_ground_vehicle_module_name", columnList = "name")
})
public class GroundVehicleModuleTemplate extends ModuleTemplate {

    /**
     * Functional type of a ground combat vehicle module section.
     */
    public enum GroundVehicleModuleType {
        /** Main battle tank — heavy armor, large caliber gun */
        TANK,
        /** Armored personnel carrier — troop transport with light armor */
        APC,
        /** Infantry fighting vehicle — APC with integrated weapon system */
        IFV,
        /** Self-propelled artillery — mobile howitzer or mortar */
        ARTILLERY,
        /** Strategic missile launcher — long-range strike platform */
        MISSILE_LAUNCHER,
        /** Air superiority fighter — fast, maneuverable combat aircraft */
        FIGHTER_JET,
        /** Strategic bomber — heavy payload, long range */
        BOMBER,
        /** Attack or transport helicopter — rotary wing aircraft */
        HELICOPTER,
        /** Amphibious transport ship — water-borne vehicle transport */
        TRANSPORT_SHIP,
        
        // ---- Civilian Transport Vehicles ----

        /** Passenger bus — road transport for people */
        BUS,
        /** Freight or passenger train — rail transport */
        TRAIN,
        /** Personal or commercial car — light road vehicle */
        CAR,
        /** Cargo truck — heavy road freight */
        TRUCK,
        /** Passenger or cargo ferry — water transport */
        FERRY,
        /** Passenger or cargo aircraft — air transport */
        AIRCRAFT,
        
        /** Connecting section for large multi-module vehicles */
        CORRIDOR,
        /** Empty or unassigned module */
        EMPTY;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GroundVehicleModuleType type;

    /**
     * Hull armor value — determines damage resistance.
     */
    @Column
    private int armorRating;

    /**
     * Base weapon damage — firepower contribution.
     */
    @Column
    private int firepower;

    /**
     * Maximum speed in km/h.
     */
    @Column
    private int speedKmh;

    /**
     * Crew required to operate this module.
     */
    @Column
    private int crewCapacity;

    /**
     * Fuel consumption per turn.
     */
    @Column
    private double fuelConsumption;

    /**
     * Sensor detection range in hexes.
     */
    @Column
    private int detectionRange;

    /**
     * Comma-separated terrain types this vehicle can operate on.
     * E.g., "OPEN,URBAN,DESERT,JUNGLE,MOUNTAIN"
     */
    @Column(length = 200)
    private String compatibleTerrain;

    /**
     * Per-turn supply cost in credits.
     */
    @Column
    private int supplyCost;

    // ---- Transport-specific fields (for civilian vehicles) ----

    /**
     * Number of passengers this module can carry.
     */
    @Column
    private int passengerCapacity;

    /**
     * Amount of cargo (in units) this module can carry.
     */
    @Column
    private int cargoCapacity;

    /**
     * Construction cost in credits.
     */
    @Column
    private int constructionCost;

    /**
     * Per-turn maintenance cost in credits.
     */
    @Column
    private int maintenanceCost;

    /**
     * Required infrastructure type for operation.
     * Values: NONE, ROAD, RAIL, WATER, AIRPORT
     */
    @Column(length = 20)
    private String infrastructureRequirement;

    // ---- Getters and Setters ----

    /**
     * @return the ground vehicle module type
     */
    public GroundVehicleModuleType getType() {
        return type;
    }

    /**
     * Sets the ground vehicle module type.
     *
     * @param type the module type
     */
    public void setType(GroundVehicleModuleType type) {
        this.type = type;
    }

    /**
     * @return the hull armor value
     */
    public int getArmorRating() {
        return armorRating;
    }

    /**
     * Sets the hull armor value.
     *
     * @param armorRating the armor rating
     */
    public void setArmorRating(int armorRating) {
        this.armorRating = armorRating;
    }

    /**
     * @return the base weapon damage
     */
    public int getFirepower() {
        return firepower;
    }

    /**
     * Sets the base weapon damage.
     *
     * @param firepower the firepower
     */
    public void setFirepower(int firepower) {
        this.firepower = firepower;
    }

    /**
     * @return the maximum speed in km/h
     */
    public int getSpeedKmh() {
        return speedKmh;
    }

    /**
     * Sets the maximum speed in km/h.
     *
     * @param speedKmh the speed
     */
    public void setSpeedKmh(int speedKmh) {
        this.speedKmh = speedKmh;
    }

    /**
     * @return the crew capacity required
     */
    public int getCrewCapacity() {
        return crewCapacity;
    }

    /**
     * Sets the crew capacity.
     *
     * @param crewCapacity the crew capacity
     */
    public void setCrewCapacity(int crewCapacity) {
        this.crewCapacity = crewCapacity;
    }

    /**
     * @return the fuel consumption per turn
     */
    public double getFuelConsumption() {
        return fuelConsumption;
    }

    /**
     * Sets the fuel consumption per turn.
     *
     * @param fuelConsumption the fuel consumption
     */
    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    /**
     * @return the sensor detection range in hexes
     */
    public int getDetectionRange() {
        return detectionRange;
    }

    /**
     * Sets the sensor detection range.
     *
     * @param detectionRange the detection range
     */
    public void setDetectionRange(int detectionRange) {
        this.detectionRange = detectionRange;
    }

    /**
     * @return the compatible terrain types, or null if compatible with all
     */
    public String getCompatibleTerrain() {
        return compatibleTerrain;
    }

    /**
     * Sets the compatible terrain types (comma-separated terrain type names).
     *
     * @param compatibleTerrain the compatible terrain types
     */
    public void setCompatibleTerrain(String compatibleTerrain) {
        this.compatibleTerrain = compatibleTerrain;
    }

    /**
     * @return the per-turn supply cost
     */
    public int getSupplyCost() {
        return supplyCost;
    }

    /**
     * Sets the per-turn supply cost.
     *
     * @param supplyCost the supply cost
     */
    public void setSupplyCost(int supplyCost) {
        this.supplyCost = supplyCost;
    }

    /**
     * Checks if this module is compatible with the given terrain type.
     *
     * @param terrain the terrain type name
     * @return true if compatible or if no terrain restriction is set
     */
    public boolean isCompatibleWithTerrain(String terrain) {
        if (compatibleTerrain == null || compatibleTerrain.isBlank()) {
            return true;
        }
        return compatibleTerrain.contains(terrain);
    }

    // ---- Transport-specific Getters and Setters ----

    /**
     * @return the passenger capacity
     */
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    /**
     * Sets the passenger capacity.
     *
     * @param passengerCapacity the passenger capacity
     */
    public void setPassengerCapacity(int passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    /**
     * @return the cargo capacity in units
     */
    public int getCargoCapacity() {
        return cargoCapacity;
    }

    /**
     * Sets the cargo capacity.
     *
     * @param cargoCapacity the cargo capacity
     */
    public void setCargoCapacity(int cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    /**
     * @return the construction cost in credits
     */
    public int getConstructionCost() {
        return constructionCost;
    }

    /**
     * Sets the construction cost.
     *
     * @param constructionCost the construction cost
     */
    public void setConstructionCost(int constructionCost) {
        this.constructionCost = constructionCost;
    }

    /**
     * @return the per-turn maintenance cost in credits
     */
    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    /**
     * Sets the per-turn maintenance cost.
     *
     * @param maintenanceCost the maintenance cost
     */
    public void setMaintenanceCost(int maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    /**
     * @return the required infrastructure type (NONE, ROAD, RAIL, WATER, AIRPORT)
     */
    public String getInfrastructureRequirement() {
        return infrastructureRequirement;
    }

    /**
     * Sets the required infrastructure type.
     *
     * @param infrastructureRequirement the infrastructure requirement
     */
    public void setInfrastructureRequirement(String infrastructureRequirement) {
        this.infrastructureRequirement = infrastructureRequirement;
    }

    /**
     * Checks if this vehicle type is a civilian transport vehicle.
     *
     * @return true if the type is BUS, TRAIN, CAR, TRUCK, FERRY, or AIRCRAFT
     */
    public boolean isTransportVehicle() {
        return type == GroundVehicleModuleType.BUS ||
                type == GroundVehicleModuleType.TRAIN ||
                type == GroundVehicleModuleType.CAR ||
                type == GroundVehicleModuleType.TRUCK ||
                type == GroundVehicleModuleType.FERRY ||
                type == GroundVehicleModuleType.AIRCRAFT;
    }

    @Override
    public String toString() {
        return String.format(
                "GroundVehicleModuleTemplate{id=%d, name='%s', type=%s, dimensions=%dx%dx%d, armor=%d, firepower=%d}",
                getId(), getName(), type, getWidth(), getHeight(), getDepth(), armorRating, firepower);
    }
}
