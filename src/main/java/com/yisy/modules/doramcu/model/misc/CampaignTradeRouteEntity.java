package com.yisy.modules.doramcu.model.misc;
import com.yisy.modules.doramcu.hex.Hex;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a Campaign Trade Route on a planet surface.
 * <p>
 * Trade routes connect multiple stations and define paths that vehicles follow
 * to transport cargo. Routes are owned by corporations and generate income.
 * </p>
 */
public class CampaignTradeRouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique route ID (matches CampaignTradeRoute.id, UUID string). */
    @Column(name = "route_id", nullable = false, unique = true, length = 100)
    private String routeId;

    /** The planet this route is on. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Route display name. */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Corporation that owns this route. */
    @Column(name = "owner_corporation_id", nullable = false, length = 100)
    private String ownerCorporationId;

    /** JSON-serialized ordered list of station IDs defining the route path. */
    @Column(name = "station_ids_json", nullable = false, columnDefinition = "TEXT")
    private String stationIdsJson;

    /** JSON-serialized list of vehicle IDs operating on this route. */
    @Column(name = "vehicle_ids_json", columnDefinition = "TEXT")
    private String vehicleIdsJson;

    /** JSON-serialized list of cargo types transported. */
    @Column(name = "cargo_types_json", columnDefinition = "TEXT")
    private String cargoTypesJson;

    /** Route status (ACTIVE, SUSPENDED, CLOSED). */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Total distance of the route in hex units. */
    @Column(name = "total_distance", nullable = false)
    private int totalDistance;

    /** Income earned today. */
    @Column(name = "daily_income", nullable = false)
    private long dailyIncome;

    /** Total income earned since creation. */
    @Column(name = "total_income", nullable = false)
    private long totalIncome;

    /** Cargo delivered today. */
    @Column(name = "cargo_delivered_today", nullable = false)
    private int cargoDeliveredToday;

    /** When this record was created in DB. */
    @Column(name = "db_created_at", nullable = false)
    private LocalDateTime dbCreatedAt;

    /** When this record was last updated in DB. */
    @Column(name = "db_updated_at", nullable = false)
    private LocalDateTime dbUpdatedAt;

    public CampaignTradeRouteEntity() {
        this.dbCreatedAt = LocalDateTime.now();
        this.dbUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
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

    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    public void setOwnerCorporationId(String ownerCorporationId) {
        this.ownerCorporationId = ownerCorporationId;
    }

    public String getStationIdsJson() {
        return stationIdsJson;
    }

    public void setStationIdsJson(String stationIdsJson) {
        this.stationIdsJson = stationIdsJson;
    }

    public String getVehicleIdsJson() {
        return vehicleIdsJson;
    }

    public void setVehicleIdsJson(String vehicleIdsJson) {
        this.vehicleIdsJson = vehicleIdsJson;
    }

    public String getCargoTypesJson() {
        return cargoTypesJson;
    }

    public void setCargoTypesJson(String cargoTypesJson) {
        this.cargoTypesJson = cargoTypesJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getDailyIncome() {
        return dailyIncome;
    }

    public void setDailyIncome(long dailyIncome) {
        this.dailyIncome = dailyIncome;
    }

    public long getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(long totalIncome) {
        this.totalIncome = totalIncome;
    }

    public int getCargoDeliveredToday() {
        return cargoDeliveredToday;
    }

    public void setCargoDeliveredToday(int cargoDeliveredToday) {
        this.cargoDeliveredToday = cargoDeliveredToday;
    }

    public LocalDateTime getDbCreatedAt() {
        return dbCreatedAt;
    }

    public void setDbCreatedAt(LocalDateTime dbCreatedAt) {
        this.dbCreatedAt = dbCreatedAt;
    }

    public LocalDateTime getDbUpdatedAt() {
        return dbUpdatedAt;
    }

    public void setDbUpdatedAt(LocalDateTime dbUpdatedAt) {
        this.dbUpdatedAt = dbUpdatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.dbCreatedAt = LocalDateTime.now();
        this.dbUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dbUpdatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "CampaignTradeRouteEntity{" +
                "id=" + id +
                ", routeId='" + routeId + '\'' +
                ", planetId=" + planetId +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}