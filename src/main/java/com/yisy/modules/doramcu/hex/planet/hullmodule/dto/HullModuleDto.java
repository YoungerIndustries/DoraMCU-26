package com.yisy.modules.doramcu.hex.planet.hullmodule.dto;

import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for HullModule data exposed via REST API.
 */
public class HullModuleDto {
    private final Long id;
    private final Long planetId;
    private final String name;
    private final String moduleType;
    private final String category;
    private final int hexX, hexY, hexZ;
    private final String ownerId;
    private final Long blueprintId;
    private final int floors;
    private final int basementLevels;
    private final int constructionCost;
    private final int maintenanceCost;
    private final int totalCapacity;
    private final int occupiedCapacity;
    private final double occupancyRate;
    private final Integer productionLevel;
    private final Map<String, Integer> inputCargo;
    private final Map<String, Integer> outputCargo;
    private final String description;
    private final boolean active;
    private final int constructionProgress;
    private final String dungeonId;
    private final LocalDateTime createdAt;

    public HullModuleDto(Long id, Long planetId, String name, HullModuleType moduleType,
                         int hexX, int hexY, int hexZ, String ownerId,
                         Long blueprintId, int floors, int basementLevels,
                         int constructionCost, int maintenanceCost,
                         int totalCapacity, int occupiedCapacity,
                         Integer productionLevel,
                         Map<String, Integer> inputCargo, Map<String, Integer> outputCargo,
                         String description, boolean active, int constructionProgress,
                         String dungeonId, LocalDateTime createdAt) {
        this.id = id;
        this.planetId = planetId;
        this.name = name;
        this.moduleType = moduleType.name();
        this.category = moduleType.getCategory().name();
        this.hexX = hexX;
        this.hexY = hexY;
        this.hexZ = hexZ;
        this.ownerId = ownerId;
        this.blueprintId = blueprintId;
        this.floors = floors;
        this.basementLevels = basementLevels;
        this.constructionCost = constructionCost;
        this.maintenanceCost = maintenanceCost;
        this.totalCapacity = totalCapacity;
        this.occupiedCapacity = occupiedCapacity;
        this.occupancyRate = totalCapacity > 0 ? (double) occupiedCapacity / totalCapacity : 0.0;
        this.productionLevel = productionLevel;
        this.inputCargo = inputCargo;
        this.outputCargo = outputCargo;
        this.description = description;
        this.active = active;
        this.constructionProgress = constructionProgress;
        this.dungeonId = dungeonId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getPlanetId() { return planetId; }
    public String getName() { return name; }
    public String getModuleType() { return moduleType; }
    public String getCategory() { return category; }
    public int getHexX() { return hexX; }
    public int getHexY() { return hexY; }
    public int getHexZ() { return hexZ; }
    public String getOwnerId() { return ownerId; }
    public Long getBlueprintId() { return blueprintId; }
    public int getFloors() { return floors; }
    public int getBasementLevels() { return basementLevels; }
    public int getConstructionCost() { return constructionCost; }
    public int getMaintenanceCost() { return maintenanceCost; }
    public int getTotalCapacity() { return totalCapacity; }
    public int getOccupiedCapacity() { return occupiedCapacity; }
    public double getOccupancyRate() { return occupancyRate; }
    public Integer getProductionLevel() { return productionLevel; }
    public Map<String, Integer> getInputCargo() { return inputCargo; }
    public Map<String, Integer> getOutputCargo() { return outputCargo; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public int getConstructionProgress() { return constructionProgress; }
    public String getDungeonId() { return dungeonId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
