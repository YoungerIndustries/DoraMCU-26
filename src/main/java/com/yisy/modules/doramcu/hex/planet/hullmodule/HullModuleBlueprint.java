package com.yisy.modules.doramcu.hex.planet.hullmodule;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Player-created blueprint for custom HullModule types.
 * <p>
 * This enables the economy to expand through player innovation - players can design
 * new building types, define their characteristics, and construct them using the
 * unified HullModule system.
 * </p>
 * <p>
 * Each blueprint defines:
 * <ul>
 *   <li>Display name and description</li>
 *   <li>Category (residential, commercial, industry, infrastructure)</li>
 *   <li>Construction cost and maintenance cost</li>
 *   <li>Floor count and basement levels</li>
 *   <li>Input/output cargo requirements</li>
 *   <li>Capacity metrics</li>
 *   <li>Required technology level</li>
 * </ul>
 * </p>
 * <p>
 * Blueprints can be shared, traded, and licensed between players, creating a
 * secondary economy around building design.
 * </p>
 */
@Entity
@Table(name = "hull_module_blueprints", indexes = {
        @Index(name = "idx_hmb_creator", columnList = "creator_id"),
        @Index(name = "idx_hmb_category", columnList = "category"),
        @Index(name = "idx_hmb_published", columnList = "published")
})
public class HullModuleBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blueprint_seq")
    @SequenceGenerator(name = "blueprint_seq", sequenceName = "blueprint_sequence", allocationSize = 1)
    private Long id;

    /**
     * Unique identifier for this blueprint.
     */
    @Column(name = "blueprint_code", nullable = false, unique = true, length = 50)
    private String blueprintCode;

    /**
     * Display name of the blueprint.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of what this blueprint builds.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Player/corporation who created this blueprint.
     */
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    /**
     * Category of the structure.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HullModuleType.Category category;

    /**
     * Construction cost in credits.
     */
    @Column(name = "construction_cost", nullable = false)
    private int constructionCost;

    /**
     * Per-turn maintenance cost in credits.
     */
    @Column(name = "maintenance_cost", nullable = false)
    private int maintenanceCost;

    /**
     * Number of upper floors.
     */
    @Column(nullable = false)
    private int floors;

    /**
     * Number of basement dungeon levels.
     */
    @Column(name = "basement_levels", nullable = false)
    private int basementLevels;

    /**
     * Total capacity units.
     */
    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    /**
     * Input cargo requirements (cargo type -> amount per turn).
     */
    @ElementCollection
    @MapKeyColumn(name = "cargo_type")
    @CollectionTable(name = "blueprint_input_cargo", joinColumns = @JoinColumn(name = "blueprint_id"))
    @Column(name = "amount")
    private Map<String, Integer> inputCargo = new HashMap<>();

    /**
     * Output cargo production (cargo type -> amount per turn).
     */
    @ElementCollection
    @MapKeyColumn(name = "cargo_type")
    @CollectionTable(name = "blueprint_output_cargo", joinColumns = @JoinColumn(name = "blueprint_id"))
    @Column(name = "amount")
    private Map<String, Integer> outputCargo = new HashMap<>();

    /**
     * Required technology level to build this module.
     */
    @Column(name = "tech_level_required", nullable = false)
    private int techLevelRequired = 1;

    /**
     * Whether the blueprint is published for public use.
     */
    @Column(nullable = false)
    private boolean published = false;

    /**
     * License fee for other players to use this blueprint.
     */
    @Column(name = "license_fee")
    private Integer licenseFee;

    /**
     * Number of times this blueprint has been used.
     */
    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    /**
     * Room templates for interior generation.
     */
    @ElementCollection
    @CollectionTable(name = "blueprint_room_templates", joinColumns = @JoinColumn(name = "blueprint_id"))
    private List<String> roomTemplates = new ArrayList<>();

    /**
     * Whether the blueprint is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * When the blueprint was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update time.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "blueprint", fetch = FetchType.LAZY)
    private List<HullModule> modules = new ArrayList<>();

    public HullModuleBlueprint() {
    }

    public HullModuleBlueprint(String blueprintCode, String name, String creatorId,
                               HullModuleType.Category category) {
        this.blueprintCode = blueprintCode;
        this.name = name;
        this.creatorId = creatorId;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBlueprintCode() { return blueprintCode; }
    public void setBlueprintCode(String blueprintCode) { this.blueprintCode = blueprintCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public HullModuleType.Category getCategory() { return category; }
    public void setCategory(HullModuleType.Category category) { this.category = category; }

    public int getConstructionCost() { return constructionCost; }
    public void setConstructionCost(int constructionCost) { this.constructionCost = constructionCost; }

    public int getMaintenanceCost() { return maintenanceCost; }
    public void setMaintenanceCost(int maintenanceCost) { this.maintenanceCost = maintenanceCost; }

    public int getFloors() { return floors; }
    public void setFloors(int floors) { this.floors = floors; }

    public int getBasementLevels() { return basementLevels; }
    public void setBasementLevels(int basementLevels) { this.basementLevels = basementLevels; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public Map<String, Integer> getInputCargo() { return inputCargo; }
    public void setInputCargo(Map<String, Integer> inputCargo) { this.inputCargo = inputCargo; }

    public Map<String, Integer> getOutputCargo() { return outputCargo; }
    public void setOutputCargo(Map<String, Integer> outputCargo) { this.outputCargo = outputCargo; }

    public int getTechLevelRequired() { return techLevelRequired; }
    public void setTechLevelRequired(int techLevelRequired) { this.techLevelRequired = techLevelRequired; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public Integer getLicenseFee() { return licenseFee; }
    public void setLicenseFee(Integer licenseFee) { this.licenseFee = licenseFee; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public List<String> getRoomTemplates() { return roomTemplates; }
    public void setRoomTemplates(List<String> roomTemplates) { this.roomTemplates = roomTemplates; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<HullModule> getModules() { return modules; }
    public void setModules(List<HullModule> modules) { this.modules = modules; }

    public void incrementUsageCount() {
        this.usageCount++;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "HullModuleBlueprint{code='" + blueprintCode + "', name='" + name +
                "', category=" + category + ", cost=" + constructionCost + "}";
    }
}
