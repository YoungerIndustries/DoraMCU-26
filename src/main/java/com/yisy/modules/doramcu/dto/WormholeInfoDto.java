package com.yisy.modules.doramcu.dto;

import java.time.LocalDateTime;

/**
 * DTO for wormhole information returned by admin API.
 * <p>
 * Contains all wormhole metadata needed to display wormhole state
 * in admin UI and support wormhole management operations.
 * </p>
 */
public class WormholeInfoDto {

    private Long id;
    private String galaxyName;
    private Long solarSystemId;
    private int sectorRow;
    private int sectorCol;
    private int subSectorRow;
    private int subSectorCol;
    private int posX;
    private int posY;
    private int posZ;
    private String name;
    private boolean isActive;
    private Long connectedWormholeId;
    private String connectedGalaxyName;
    private LocalDateTime createdAt;

    public WormholeInfoDto() {
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

    public String getConnectedGalaxyName() {
        return connectedGalaxyName;
    }

    public void setConnectedGalaxyName(String connectedGalaxyName) {
        this.connectedGalaxyName = connectedGalaxyName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
