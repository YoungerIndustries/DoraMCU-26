package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.ShipType;
import jakarta.persistence.*;

/**
 * JPA entity representing a reusable ship module template.
 * <p>
 * A ship module template defines a pre-built ship section (bridge, engine room,
 * weapon bay, cargo hold, etc.) that can be instantiated multiple times when
 * building ships. Each template stores its shape, compartment layout, and
 * connection points (docking ports) as serialized JSON data.
 * </p>
 * <p>
 * Templates are stored in the database and loaded by the {@link ShipBuilder}
 * to assemble ships from reusable sections.
 * </p>
 */
@Entity
@Table(name = "ship_module_templates", indexes = {
        @Index(name = "idx_ship_module_type", columnList = "type"),
        @Index(name = "idx_ship_module_name", columnList = "name")
})
public class ShipModuleTemplate extends ModuleTemplate {

    /**
     * Functional type of a ship module section.
     */
    public enum ShipModuleType {
        /** Command and control center — navigation, communications, tactical operations */
        BRIDGE,
        /** Power generation and distribution — reactors, engines, power conduits */
        ENGINE_ROOM,
        /** Weapon systems — missile bays, turret mounts, weapon control */
        WEAPON_BAY,
        /** Storage area for cargo, supplies, or raw materials */
        CARGO_HOLD,
        /** Living quarters, mess halls, medical facilities for crew */
        CREW_QUARTERS,
        /** Shield generators, armor control, defensive systems */
        SHIELD_GENERATOR,
        /** Docking bays, airlocks, ship maintenance facilities */
        DOCKING_BAY,
        /** Scientific instruments, laboratories, research equipment */
        RESEARCH_LAB,
        /** Corridors and access tunnels connecting other modules */
        CORRIDOR,
        /** Reinforced storage for hazardous or valuable materials */
        VAULT,
        /** Medical bay and repair facilities */
        MEDICAL_BAY,
        /** Hangar for shuttles and fighters */
        HANGAR_BAY,
        /** Empty or unassigned module */
        EMPTY;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShipModuleType type;

    /**
     * Suggested ship types that can use this module (comma-separated).
     * If null, the module can be used by any ship type.
     */
    @Column(length = 200)
    private String compatibleShipTypes;

    /**
     * Mass in tons contributed by this module.
     */
    @Column
    private double massTons;

    /**
     * Crew capacity required by this module.
     */
    @Column
    private int crewCapacity;

    /**
     * Power consumption in MW.
     */
    @Column
    private double powerConsumption;

    // ---- Getters and Setters ----

    /**
     * @return the ship module type
     */
    public ShipModuleType getType() {
        return type;
    }

    /**
     * Sets the ship module type.
     *
     * @param type the module type
     */
    public void setType(ShipModuleType type) {
        this.type = type;
    }

    /**
     * @return the compatible ship types, or null if compatible with all
     */
    public String getCompatibleShipTypes() {
        return compatibleShipTypes;
    }

    /**
     * Sets the compatible ship types (comma-separated ShipType names).
     *
     * @param compatibleShipTypes the compatible ship types
     */
    public void setCompatibleShipTypes(String compatibleShipTypes) {
        this.compatibleShipTypes = compatibleShipTypes;
    }

    /**
     * @return the mass in tons
     */
    public double getMassTons() {
        return massTons;
    }

    /**
     * Sets the mass in tons.
     *
     * @param massTons the mass
     */
    public void setMassTons(double massTons) {
        this.massTons = massTons;
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
     * @return the power consumption in MW
     */
    public double getPowerConsumption() {
        return powerConsumption;
    }

    /**
     * Sets the power consumption in MW.
     *
     * @param powerConsumption the power consumption
     */
    public void setPowerConsumption(double powerConsumption) {
        this.powerConsumption = powerConsumption;
    }

    /**
     * Checks if this module is compatible with the given ship type.
     *
     * @param shipType the ship type to check
     * @return true if compatible or if no compatibility restriction is set
     */
    public boolean isCompatibleWith(ShipType shipType) {
        if (compatibleShipTypes == null || compatibleShipTypes.isBlank()) {
            return true;
        }
        return compatibleShipTypes.contains(shipType.name());
    }

    @Override
    public String toString() {
        return String.format("ShipModuleTemplate{id=%d, name='%s', type=%s, dimensions=%dx%dx%d, mass=%.1ft}",
                getId(), getName(), type, getWidth(), getHeight(), getDepth(), massTons);
    }
}
