package com.yisy.modules.doramcu.hex.planet.hullmodule.dto;

import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModuleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for HullModuleBlueprint data exposed via REST API.
 */
public class HullModuleBlueprintDto {
    private final Long id;
    private final String blueprintCode;
    private final String name;
    private final String description;
    private final String creatorId;
    private final String category;
    private final int constructionCost;
    private final int maintenanceCost;
    private final int floors;
    private final int basementLevels;
    private final int totalCapacity;
    private final Map<String, Integer> inputCargo;
    private final Map<String, Integer> outputCargo;
    private final int techLevelRequired;
    private final boolean published;
    private final Integer licenseFee;
    private final int usageCount;
    private final List<String> roomTemplates;
    private final boolean active;
    private final LocalDateTime createdAt;

    public HullModuleBlueprintDto(Long id, String blueprintCode, String name, String description,
                                   String creatorId, HullModuleType.Category category,
                                   int constructionCost, int maintenanceCost,
                                   int floors, int basementLevels, int totalCapacity,
                                   Map<String, Integer> inputCargo, Map<String, Integer> outputCargo,
                                   int techLevelRequired, boolean published, Integer licenseFee,
                                   int usageCount, List<String> roomTemplates,
                                   boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.blueprintCode = blueprintCode;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.category = category.name();
        this.constructionCost = constructionCost;
        this.maintenanceCost = maintenanceCost;
        this.floors = floors;
        this.basementLevels = basementLevels;
        this.totalCapacity = totalCapacity;
        this.inputCargo = inputCargo;
        this.outputCargo = outputCargo;
        this.techLevelRequired = techLevelRequired;
        this.published = published;
        this.licenseFee = licenseFee;
        this.usageCount = usageCount;
        this.roomTemplates = roomTemplates;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getBlueprintCode() { return blueprintCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatorId() { return creatorId; }
    public String getCategory() { return category; }
    public int getConstructionCost() { return constructionCost; }
    public int getMaintenanceCost() { return maintenanceCost; }
    public int getFloors() { return floors; }
    public int getBasementLevels() { return basementLevels; }
    public int getTotalCapacity() { return totalCapacity; }
    public Map<String, Integer> getInputCargo() { return inputCargo; }
    public Map<String, Integer> getOutputCargo() { return outputCargo; }
    public int getTechLevelRequired() { return techLevelRequired; }
    public boolean isPublished() { return published; }
    public Integer getLicenseFee() { return licenseFee; }
    public int getUsageCount() { return usageCount; }
    public List<String> getRoomTemplates() { return roomTemplates; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
