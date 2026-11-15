package com.yisy.modules.doramcu.hex.planet.dto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DTO representing a settlement on a planet surface.
 * <p>
 * Includes the settlement's position, size, population, cargo production,
 * and references to interior dungeon rooms for 3D exploration.
 * </p>
 */
public class SettlementDto {

    private String id;
    private String name;
    private int centerX;
    private int centerY;
    private int centerZ;
    private String size;
    private int population;
    private int passengerProduction;
    private boolean active;
    private String ownerEmpireId;
    private List<String> dungeonIds;
    private List<String> hexes;

    public SettlementDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getPassengerProduction() {
        return passengerProduction;
    }

    public void setPassengerProduction(int passengerProduction) {
        this.passengerProduction = passengerProduction;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(String ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public List<String> getDungeonIds() {
        return dungeonIds;
    }

    public void setDungeonIds(List<String> dungeonIds) {
        this.dungeonIds = dungeonIds;
    }

    public List<String> getHexes() {
        return hexes;
    }

    public void setHexes(List<String> hexes) {
        this.hexes = hexes;
    }
}
