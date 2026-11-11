package com.yisy.modules.doramcu.hex.dungeon;

import jakarta.persistence.*;

/**
 * JPA entity representing a reusable space station module template.
 * <p>
 * A station module template defines a pre-built station section (command module,
 * docking ring, weapon platform, etc.) that can be instantiated multiple times
 * when building space stations. Each template stores its shape, compartment layout,
 * and connection points (docking ports) as serialized JSON data.
 * </p>
 * <p>
 * Templates are stored in the database and used to assemble multi-module space stations
 * with complex internal layouts.
 * </p>
 */
@Entity
@Table(name = "station_module_templates", indexes = {
        @Index(name = "idx_station_module_type", columnList = "type"),
        @Index(name = "idx_station_module_name", columnList = "name")
})
public class StationModuleTemplate extends ModuleTemplate {

    /**
     * Functional type of a station module section.
     */
    public enum StationModuleType {
        /** Command and control center for station operations */
        COMMAND_MODULE,
        /** Docking ring for ship berthing */
        DOCKING_RING,
        /** Weapon platform with turret mounts */
        WEAPON_PLATFORM,
        /** Shield generator array */
        SHIELD_ARRAY,
        /** Sensor and communications array */
        SENSOR_ARRAY,
        /** Cargo storage and transfer */
        CARGO_MODULE,
        /** Crew quarters and amenities */
        CREW_QUARTERS,
        /** Repair and maintenance facilities */
        REPAIR_BAY,
        /** Research and science laboratory */
        RESEARCH_LAB,
        /** Corridor connecting station sections */
        CORRIDOR,
        /** Power generation module */
        POWER_MODULE,
        /** Life support systems */
        LIFE_SUPPORT,
        /** Empty or unassigned module */
        EMPTY;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StationModuleType type;

    /**
     * Number of docking ports provided by this module.
     */
    @Column
    private int dockingPorts;

    /**
     * Shield contribution from this module.
     */
    @Column
    private int shieldContribution;

    /**
     * Hull contribution from this module.
     */
    @Column
    private int hullContribution;

    // ---- Getters and Setters ----

    /**
     * @return the station module type
     */
    public StationModuleType getType() {
        return type;
    }

    /**
     * Sets the station module type.
     *
     * @param type the module type
     */
    public void setType(StationModuleType type) {
        this.type = type;
    }

    /**
     * @return the number of docking ports
     */
    public int getDockingPorts() {
        return dockingPorts;
    }

    /**
     * Sets the number of docking ports.
     *
     * @param dockingPorts the number of ports
     */
    public void setDockingPorts(int dockingPorts) {
        this.dockingPorts = dockingPorts;
    }

    /**
     * @return the shield contribution
     */
    public int getShieldContribution() {
        return shieldContribution;
    }

    /**
     * Sets the shield contribution.
     *
     * @param shieldContribution the shield value
     */
    public void setShieldContribution(int shieldContribution) {
        this.shieldContribution = shieldContribution;
    }

    /**
     * @return the hull contribution
     */
    public int getHullContribution() {
        return hullContribution;
    }

    /**
     * Sets the hull contribution.
     *
     * @param hullContribution the hull value
     */
    public void setHullContribution(int hullContribution) {
        this.hullContribution = hullContribution;
    }

    @Override
    public String toString() {
        return String.format("StationModuleTemplate{id=%d, name='%s', type=%s, dimensions=%dx%dx%d, docking=%d}",
                getId(), getName(), type, getWidth(), getHeight(), getDepth(), dockingPorts);
    }
}
