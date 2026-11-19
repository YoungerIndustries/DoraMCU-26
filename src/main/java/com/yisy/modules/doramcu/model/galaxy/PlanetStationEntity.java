package com.yisy.modules.doramcu.model.galaxy;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.hex.dungeon.Dungeon;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a station on a planet surface.
 * <p>
 * The Galaxy Overlord saves every station (bus stop, rail station, boat dock,
 * airport, etc.) so it can be recreated from the database.
 * </p>
 */
public class PlanetStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique station ID (matches Station.id, e.g., "STAT-1"). */
    @Column(name = "station_id", nullable = false, unique = true, length = 100)
    private String stationId;

    /** The planet this station belongs to. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Station name. */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Hex X coordinate. */
    @Column(name = "hex_x", nullable = false)
    private int hexX;

    /** Hex Y coordinate. */
    @Column(name = "hex_y", nullable = false)
    private int hexY;

    /** Hex Z coordinate. */
    @Column(name = "hex_z", nullable = false)
    private int hexZ;

    /** Infrastructure type (STATION, PORT, AIRPORT). */
    @Column(name = "infrastructure_type", nullable = false, length = 20)
    private String infrastructureType;

    /** JSON-serialized cargo waiting map. */
    @Column(name = "cargo_waiting_json", columnDefinition = "TEXT")
    private String cargoWaitingJson;

    /** Number of passengers waiting. */
    @Column(name = "passengers_waiting", nullable = false)
    private int passengersWaiting = 0;

    /** Total income from cargo delivery. */
    @Column(name = "total_income", nullable = false)
    private int totalIncome = 0;

    /** Connected settlement ID (nullable). */
    @Column(name = "connected_settlement_id", length = 100)
    private String connectedSettlementId;

    /** Connected industry ID (nullable). */
    @Column(name = "connected_industry_id", length = 100)
    private String connectedIndustryId;

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

    public PlanetStationEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(String infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    public String getCargoWaitingJson() {
        return cargoWaitingJson;
    }

    public void setCargoWaitingJson(String cargoWaitingJson) {
        this.cargoWaitingJson = cargoWaitingJson;
    }

    public int getPassengersWaiting() {
        return passengersWaiting;
    }

    public void setPassengersWaiting(int passengersWaiting) {
        this.passengersWaiting = passengersWaiting;
        this.updatedAt = LocalDateTime.now();
    }

    public int getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(int totalIncome) {
        this.totalIncome = totalIncome;
        this.updatedAt = LocalDateTime.now();
    }

    public String getConnectedSettlementId() {
        return connectedSettlementId;
    }

    public void setConnectedSettlementId(String connectedSettlementId) {
        this.connectedSettlementId = connectedSettlementId;
    }

    public String getConnectedIndustryId() {
        return connectedIndustryId;
    }

    public void setConnectedIndustryId(String connectedIndustryId) {
        this.connectedIndustryId = connectedIndustryId;
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
        return "PlanetStationEntity{id=" + stationId + ", name=" + name +
                ", planet=" + planetId + ", infra=" + infrastructureType + "}";
    }
}