package com.yisy.modules.doramcu.hex.planet.dto;

import java.util.Map;

/**
 * DTO representing an industry on a planet surface.
 * <p>
 * Includes the industry's position, type, production rates, inventory,
 * and reference to interior dungeon rooms for 3D exploration.
 * </p>
 */
public class IndustryDto {

    private String id;
    private String type;
    private int x;
    private int y;
    private int z;
    private int productionRate;
    private Map<String, Integer> inventory;
    private boolean active;
    private String ownerEmpireId;
    private String dungeonId;

    public IndustryDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getProductionRate() {
        return productionRate;
    }

    public void setProductionRate(int productionRate) {
        this.productionRate = productionRate;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
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

    public String getDungeonId() {
        return dungeonId;
    }

    public void setDungeonId(String dungeonId) {
        this.dungeonId = dungeonId;
    }
}
