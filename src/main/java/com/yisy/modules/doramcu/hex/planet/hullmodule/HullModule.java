package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.planet.CargoType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unified JPA entity representing any buildable structure on the planet surface.
 * <p>
 * This replaces scattered building models (ApartmentBuilding, YoungerIndustriesHQ, etc.)
 * with a single flexible model that supports all structure types through the HullModuleType enum.
 * </p>
 * <p>
 * Each module has:
 * <ul>
 *   <li>A type defining its base characteristics</li>
 *   <li>Position on the hex grid (x, y, z cubic coordinates)</li>
 *   <li>Owner corporation/player</li>
 *   <li>Construction and operational state</li>
 *   <li>Production metrics for industries</li>
 *   <li>Occupancy metrics for residential/commercial</li>
 *   <li>Link to generated dungeon rooms via BuildingInteriorGenerator</li>
 * </ul>
 * </p>
 * <p>
 * Players can create custom blueprints (HullModuleBlueprint) to define new module types
 * beyond the predefined enum values, enabling player-driven expansion of the economy.
 * </p>
 */
@Entity
@Table(name = "hull_modules", indexes = {
        @Index(name = "idx_hm_planet", columnList = "planet_id"),
        @Index(name = "idx_hm_type", columnList = "module_type"),
        @Index(name = "idx_hm_owner", columnList = "owner_id"),
        @Index(name = "idx_hm_active", columnList = "active"),
        @Index(name = "idx_hm_position", columnList = "hex_x,hex_y,hex_z")
})
public class HullModule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hullmodule_seq")
    @SequenceGenerator(name = "hullmodule_seq", sequenceName = "hullmodule_sequence", allocationSize = 1)
    private Long id;

    /**
     * The planet this module is built on.
     */
    @Column(name = "planet_id", nullable = false)
    private Long planetId;

    /**
     * Name of the module instance.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The type of this module (from HullModuleType enum).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false, length = 50)
    private HullModuleType moduleType;

    /**
     * Hex grid X coordinate (cubic).
     */
    @Column(name = "hex_x", nullable = false)
    private int hexX;

    /**
     * Hex grid Y coordinate (cubic).
     */
    @Column(name = "hex_y", nullable = false)
    private int hexY;

    /**
     * Hex grid Z coordinate (cubic).
     */
    @Column(name = "hex_z", nullable = false)
    private int hexZ;

    /**
     * Owner corporation/player ID.
     */
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    /**
     * Blueprint used for construction (null for predefined types).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blueprint_id")
    private HullModuleBlueprint blueprint;

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
     * Total capacity units (apartments, offices, cargo slots, etc.).
     */
    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    /**
     * Currently occupied/used capacity units.
     */
    @Column(name = "occupied_capacity", nullable = false)
    private int occupiedCapacity = 0;

    /**
     * Production level (0-100) for industries.
     */
    @Column(name = "production_level")
    private Integer productionLevel;

    /**
     * Input cargo requirements for production facilities.
     */
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    @CollectionTable(name = "hull_module_input_cargo", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "amount")
    private Map<String, Integer> inputCargo = new java.util.HashMap<>();

    /**
     * Output cargo production for industries.
     */
    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    @CollectionTable(name = "hull_module_output_cargo", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "amount")
    private Map<String, Integer> outputCargo = new java.util.HashMap<>();

    /**
     * Description of the module.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Whether the module is currently operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Construction progress (0-100). 100 = complete.
     */
    @Column(name = "construction_progress", nullable = false)
    private int constructionProgress = 100;

    /**
     * When the module was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update time.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Dungeon ID linking to generated interior rooms.
     */
    @Column(name = "dungeon_id", length = 100)
    private String dungeonId;

    public HullModule() {
    }

    public HullModule(String name, HullModuleType moduleType, Long planetId,
                      int hexX, int hexY, int hexZ, String ownerId) {
        this.name = name;
        this.moduleType = moduleType;
        this.planetId = planetId;
        this.hexX = hexX;
        this.hexY = hexY;
        this.hexZ = hexZ;
        this.ownerId = ownerId;
        this.floors = moduleType.getFloors();
        this.basementLevels = moduleType.getBasementLevels();
        this.constructionCost = moduleType.getConstructionCost();
        this.maintenanceCost = moduleType.getMaintenanceCost();
        this.totalCapacity = moduleType.getApartmentUnits() + moduleType.getOfficeUnits() +
                moduleType.getCargoCapacity() + moduleType.getPassengerCapacity();
        this.inputCargo = new java.util.HashMap<>();
        for (java.util.Map.Entry<CargoType, Integer> entry : moduleType.getInputCargo().entrySet()) {
            this.inputCargo.put(entry.getKey().name(), entry.getValue());
        }
        this.outputCargo = new java.util.HashMap<>();
        for (java.util.Map.Entry<CargoType, Integer> entry : moduleType.getOutputCargo().entrySet()) {
            this.outputCargo.put(entry.getKey().name(), entry.getValue());
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.constructionProgress = 100;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlanetId() { return planetId; }
    public void setPlanetId(Long planetId) { this.planetId = planetId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public HullModuleType getModuleType() { return moduleType; }
    public void setModuleType(HullModuleType moduleType) { this.moduleType = moduleType; }

    public int getHexX() { return hexX; }
    public void setHexX(int hexX) { this.hexX = hexX; }

    public int getHexY() { return hexY; }
    public void setHexY(int hexY) { this.hexY = hexY; }

    public int getHexZ() { return hexZ; }
    public void setHexZ(int hexZ) { this.hexZ = hexZ; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public HullModuleBlueprint getBlueprint() { return blueprint; }
    public void setBlueprint(HullModuleBlueprint blueprint) { this.blueprint = blueprint; }

    public int getFloors() { return floors; }
    public void setFloors(int floors) { this.floors = floors; }

    public int getBasementLevels() { return basementLevels; }
    public void setBasementLevels(int basementLevels) { this.basementLevels = basementLevels; }

    public int getConstructionCost() { return constructionCost; }
    public void setConstructionCost(int constructionCost) { this.constructionCost = constructionCost; }

    public int getMaintenanceCost() { return maintenanceCost; }
    public void setMaintenanceCost(int maintenanceCost) { this.maintenanceCost = maintenanceCost; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public int getOccupiedCapacity() { return occupiedCapacity; }
    public void setOccupiedCapacity(int occupiedCapacity) { this.occupiedCapacity = occupiedCapacity; }

    public double getOccupancyRate() {
        return totalCapacity > 0 ? (double) occupiedCapacity / totalCapacity : 0.0;
    }

    public Integer getProductionLevel() { return productionLevel; }
    public void setProductionLevel(Integer productionLevel) { this.productionLevel = productionLevel; }

    public Map<String, Integer> getInputCargo() { return inputCargo; }
    public void setInputCargo(Map<String, Integer> inputCargo) { this.inputCargo = inputCargo; }

    public Map<String, Integer> getOutputCargo() { return outputCargo; }
    public void setOutputCargo(Map<String, Integer> outputCargo) { this.outputCargo = outputCargo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getConstructionProgress() { return constructionProgress; }
    public void setConstructionProgress(int constructionProgress) { this.constructionProgress = constructionProgress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getDungeonId() { return dungeonId; }
    public void setDungeonId(String dungeonId) { this.dungeonId = dungeonId; }

    @Override
    public String toString() {
        return "HullModule{id=" + id + ", name='" + name + "', type=" + moduleType +
                ", position=(" + hexX + "," + hexY + "," + hexZ + ")," +
                " floors=" + floors + ", basement=" + basementLevels + ", active=" + active + "}";
    }
}
