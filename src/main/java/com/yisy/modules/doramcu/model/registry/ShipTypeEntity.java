package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for ship types in space combat.
 * <p>
 * Covers FRIGATE, DESTROYER, CRUISER, BATTLESHIP, CARRIER, and MOTHERSHIP.
 * </p>
 */
@Entity
@Table(name = "ship_type", indexes = {
        @Index(name = "idx_st_ship_type_code", columnList = "ship_type_code")
})
public class ShipTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "ship_type_code", nullable = false, unique = true, length = 50)
    private String shipTypeCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "base_hull_points", nullable = false)
    private int baseHullPoints;

    @Column(name = "component_slots", nullable = false)
    private int componentSlots;

    @Column(name = "build_time_turns", nullable = false)
    private int buildTimeTurns;

    @Column(name = "m_drive_speed", nullable = false)
    private int mDriveSpeed;

    @Column(name = "j_drive_speed", nullable = false)
    private int jDriveSpeed;

    @Column(name = "base_evasion", nullable = false)
    private Double baseEvasion;

    @Column(name = "cubic_meters", nullable = false)
    private int cubicMeters;

    @Column(name = "hull_module_count", nullable = false)
    private int hullModuleCount;

    @Column(name = "module_grid_dimensions", columnDefinition = "JSON")
    private String moduleGridDimensions;

    public ShipTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShipTypeCode() {
        return shipTypeCode;
    }

    public void setShipTypeCode(String shipTypeCode) {
        this.shipTypeCode = shipTypeCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getBaseHullPoints() {
        return baseHullPoints;
    }

    public void setBaseHullPoints(int baseHullPoints) {
        this.baseHullPoints = baseHullPoints;
    }

    public int getComponentSlots() {
        return componentSlots;
    }

    public void setComponentSlots(int componentSlots) {
        this.componentSlots = componentSlots;
    }

    public int getBuildTimeTurns() {
        return buildTimeTurns;
    }

    public void setBuildTimeTurns(int buildTimeTurns) {
        this.buildTimeTurns = buildTimeTurns;
    }

    public int getMDriveSpeed() {
        return mDriveSpeed;
    }

    public void setMDriveSpeed(int mDriveSpeed) {
        this.mDriveSpeed = mDriveSpeed;
    }

    public int getJDriveSpeed() {
        return jDriveSpeed;
    }

    public void setJDriveSpeed(int jDriveSpeed) {
        this.jDriveSpeed = jDriveSpeed;
    }

    public Double getBaseEvasion() {
        return baseEvasion;
    }

    public void setBaseEvasion(Double baseEvasion) {
        this.baseEvasion = baseEvasion;
    }

    public int getCubicMeters() {
        return cubicMeters;
    }

    public void setCubicMeters(int cubicMeters) {
        this.cubicMeters = cubicMeters;
    }

    public int getHullModuleCount() {
        return hullModuleCount;
    }

    public void setHullModuleCount(int hullModuleCount) {
        this.hullModuleCount = hullModuleCount;
    }

    public String getModuleGridDimensions() {
        return moduleGridDimensions;
    }

    public void setModuleGridDimensions(String moduleGridDimensions) {
        this.moduleGridDimensions = moduleGridDimensions;
    }
}
