package com.yisy.modules.doramcu.model.galaxy;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a single terrain cell on a planet surface.
 * <p>
 * Each row represents one {@code Hex3D} cell with its biome, infrastructure,
 * and references to any settlement or industry occupying it. The Galaxy Overlord
 * saves all terrain cells when a planet is generated so the map can be recreated
 * without re-running procedural generation.
 * </p>
 */
@Entity
@Table(name = "planet_terrain_cells", indexes = {
    @Index(name = "idx_terrain_planet", columnList = "planet_id"),
    @Index(name = "idx_terrain_hex", columnList = "hex_x,hex_y,hex_z"),
    @Index(name = "idx_terrain_settlement", columnList = "settlement_id"),
    @Index(name = "idx_terrain_industry", columnList = "industry_id")
})
public class PlanetTerrainCell {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The planet this terrain cell belongs to. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Cubic X coordinate of the hex cell. */
    @Column(name = "hex_x", nullable = false)
    private int hexX;

    /** Cubic Y coordinate of the hex cell. */
    @Column(name = "hex_y", nullable = false)
    private int hexY;

    /** Cubic Z coordinate of the hex cell (derived: -x-y). */
    @Column(name = "hex_z", nullable = false)
    private int hexZ;

    /** Terrain type name (e.g., GRASSLAND, DESERT, DEEP_OCEAN). */
    @Column(name = "terrain_type", nullable = false, length = 30)
    private String terrainType;

    /** Biome name (e.g., "Temperate Grassland", "Hot Desert"). */
    @Column(name = "biome_name", length = 60)
    private String biomeName;

    /** Temperature in Celsius (-50 to +50). */
    @Column(name = "temperature")
    private double temperature;

    /** Moisture level (0.0 to 1.0). */
    @Column(name = "moisture")
    private double moisture;

    /** Altitude in meters above sea level. */
    @Column(name = "altitude")
    private int altitude;

    /** Infrastructure type (NONE, ROAD, RAIL, STATION, PORT, AIRPORT). */
    @Column(name = "infrastructure", length = 20)
    private String infrastructure;

    /** Water level (0 = dry, higher = deeper). */
    @Column(name = "water_level")
    private int waterLevel = 0;

    /** Settlement ID occupying this cell (nullable). */
    @Column(name = "settlement_id", length = 100)
    private String settlementId;

    /** Industry ID occupying this cell (nullable). */
    @Column(name = "industry_id", length = 100)
    private String industryId;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlanetTerrainCell() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
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

    public String getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(String terrainType) {
        this.terrainType = terrainType;
    }

    public String getBiomeName() {
        return biomeName;
    }

    public void setBiomeName(String biomeName) {
        this.biomeName = biomeName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getMoisture() {
        return moisture;
    }

    public void setMoisture(double moisture) {
        this.moisture = moisture;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public String getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(String infrastructure) {
        this.infrastructure = infrastructure;
        this.updatedAt = LocalDateTime.now();
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
        this.updatedAt = LocalDateTime.now();
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
        return "PlanetTerrainCell{planet=" + planetId + ", hex=(" + hexX + "," + hexY + "," + hexZ +
                "), terrain=" + terrainType + ", biome=" + biomeName + "}";
    }
}
