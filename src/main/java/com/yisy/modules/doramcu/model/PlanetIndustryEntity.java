package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists an industry on a planet surface.
 * <p>
 * The Galaxy Overlord saves every industry so it can be recreated from the
 * database instead of being randomly placed again.
 * </p>
 */
@Entity
@Table(name = "planet_industries", indexes = {
    @Index(name = "idx_industry_planet", columnList = "planet_id"),
    @Index(name = "idx_industry_empire", columnList = "owner_empire_id"),
    @Index(name = "idx_industry_hex", columnList = "hex_x,hex_y,hex_z")
})
public class PlanetIndustryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique industry ID (matches Industry.id, e.g., "IND-1"). */
    @Column(name = "industry_id", nullable = false, unique = true, length = 100)
    private String industryId;

    /** The planet this industry belongs to. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Industry type (MINE, FARM, STEEL_MILL, etc.). */
    @Column(name = "industry_type", nullable = false, length = 30)
    private String industryType;

    /** Hex X coordinate. */
    @Column(name = "hex_x", nullable = false)
    private int hexX;

    /** Hex Y coordinate. */
    @Column(name = "hex_y", nullable = false)
    private int hexY;

    /** Hex Z coordinate. */
    @Column(name = "hex_z", nullable = false)
    private int hexZ;

    /** JSON-serialized input inventory map. */
    @Column(name = "input_inventory_json", columnDefinition = "TEXT")
    private String inputInventoryJson;

    /** JSON-serialized output inventory map. */
    @Column(name = "output_inventory_json", columnDefinition = "TEXT")
    private String outputInventoryJson;

    /** Current production level (0.0 to 1.0). */
    @Column(name = "production_level", nullable = false)
    private double productionLevel = 1.0;

    /** Number of turns since built. */
    @Column(name = "turns_since_built", nullable = false)
    private int turnsSinceBuilt = 0;

    /** Whether the industry is active. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Owner empire ID (nullable). */
    @Column(name = "owner_empire_id", length = 100)
    private String ownerEmpireId;

    /** Dungeon ID for interior rooms (nullable). */
    @Column(name = "dungeon_id", length = 255)
    private String dungeonId;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlanetIndustryEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public int getHexX() {
        return hexX;
    }

    public void setHexX(int hexX) {
        this.hexX = hexX;
    }

    public int getHexY() {
        return hexY;
    }

    public void setHexY(int hexY) {
        this.hexY = hexY;
    }

    public int getHexZ() {
        return hexZ;
    }

    public void setHexZ(int hexZ) {
        this.hexZ = hexZ;
    }

    public String getInputInventoryJson() {
        return inputInventoryJson;
    }

    public void setInputInventoryJson(String inputInventoryJson) {
        this.inputInventoryJson = inputInventoryJson;
    }

    public String getOutputInventoryJson() {
        return outputInventoryJson;
    }

    public void setOutputInventoryJson(String outputInventoryJson) {
        this.outputInventoryJson = outputInventoryJson;
    }

    public double getProductionLevel() {
        return productionLevel;
    }

    public void setProductionLevel(double productionLevel) {
        this.productionLevel = productionLevel;
        this.updatedAt = LocalDateTime.now();
    }

    public int getTurnsSinceBuilt() {
        return turnsSinceBuilt;
    }

    public void setTurnsSinceBuilt(int turnsSinceBuilt) {
        this.turnsSinceBuilt = turnsSinceBuilt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PlanetIndustryEntity{id=" + industryId + ", type=" + industryType +
                ", planet=" + planetId + ", level=" + productionLevel + "}";
    }
}
