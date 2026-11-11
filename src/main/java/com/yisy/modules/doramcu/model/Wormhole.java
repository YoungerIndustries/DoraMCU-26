package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a wormhole gate within a galaxy.
 * <p>
 * A wormhole is a portal that connects two galaxies, allowing players and their
 * crew to travel between them. Each galaxy can have at most one active wormhole.
 * Wormholes are created in pairs, with each wormhole referencing its counterpart
 * in the connected galaxy.
 * </p>
 * <p>
 * The wormhole is positioned at a specific solar system within a galaxy sector,
 * using the same coordinate system as solar systems (sector, subsector, x/y/z).
 * </p>
 */
@Entity
@Table(name = "wormholes", indexes = {
    @Index(name = "idx_wormhole_galaxy", columnList = "galaxy_name"),
    @Index(name = "idx_wormhole_connected", columnList = "connected_wormhole_id")
})
public class Wormhole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The galaxy this wormhole resides in. */
    @Column(name = "galaxy_name", nullable = false, unique = true)
    private String galaxyName;

    /** The solar system containing this wormhole. */
    @Column(name = "solar_system_id", nullable = false)
    private Long solarSystemId;

    /** Sector row in the 10x10 grid (0-9). */
    @Column(name = "sector_row", nullable = false)
    private int sectorRow;

    /** Sector column in the 10x10 grid (0-9). */
    @Column(name = "sector_col", nullable = false)
    private int sectorCol;

    /** Subsector row in the 10x10 subsector grid (0-9). */
    @Column(name = "sub_sector_row", nullable = false)
    private int subSectorRow;

    /** Subsector column in the 10x10 subsector grid (0-9). */
    @Column(name = "sub_sector_col", nullable = false)
    private int subSectorCol;

    /** X position within the subsector (0-9). */
    @Column(name = "pos_x", nullable = false)
    private int posX;

    /** Y position within the subsector (0-9). */
    @Column(name = "pos_y", nullable = false)
    private int posY;

    /** Z position within the subsector (0-9). */
    @Column(name = "pos_z", nullable = false)
    private int posZ;

    /** Human-readable name for this wormhole gate. */
    @Column(nullable = false)
    private String name;

    /** Whether the wormhole is currently active and can be used for travel. */
    @Column(nullable = false)
    private boolean isActive = true;

    /** Reference to the paired wormhole in the connected galaxy. */
    @Column(name = "connected_wormhole_id")
    private Long connectedWormholeId;

    /** Timestamp when this wormhole was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Wormhole() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public Long getSolarSystemId() {
        return solarSystemId;
    }

    public void setSolarSystemId(Long solarSystemId) {
        this.solarSystemId = solarSystemId;
    }

    public int getSectorRow() {
        return sectorRow;
    }

    public void setSectorRow(int sectorRow) {
        this.sectorRow = sectorRow;
    }

    public int getSectorCol() {
        return sectorCol;
    }

    public void setSectorCol(int sectorCol) {
        this.sectorCol = sectorCol;
    }

    public int getSubSectorRow() {
        return subSectorRow;
    }

    public void setSubSectorRow(int subSectorRow) {
        this.subSectorRow = subSectorRow;
    }

    public int getSubSectorCol() {
        return subSectorCol;
    }

    public void setSubSectorCol(int subSectorCol) {
        this.subSectorCol = subSectorCol;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getPosZ() {
        return posZ;
    }

    public void setPosZ(int posZ) {
        this.posZ = posZ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getConnectedWormholeId() {
        return connectedWormholeId;
    }

    public void setConnectedWormholeId(Long connectedWormholeId) {
        this.connectedWormholeId = connectedWormholeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Wormhole{" +
                "id=" + id +
                ", galaxyName='" + galaxyName + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", connectedWormholeId=" + connectedWormholeId +
                '}';
    }
}
