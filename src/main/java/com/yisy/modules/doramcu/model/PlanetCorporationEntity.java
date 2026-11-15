package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a Transport Corporation on a planet surface.
 * <p>
 * The Galaxy Overlord saves every corporation so it can be recreated from the
 * database instead of being randomly generated again. Each corporation owns
 * stations, vehicles, and trade routes on one or more planets.
 * </p>
 */
@Entity
@Table(name = "planet_corporations", indexes = {
    @Index(name = "idx_corp_planet", columnList = "planet_id"),
    @Index(name = "idx_corp_owner", columnList = "owner_empire_id"),
    @Index(name = "idx_corp_corp_id", columnList = "corporation_id")
})
public class PlanetCorporationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique corporation ID (matches TransportCorporation.id, UUID string). */
    @Column(name = "corporation_id", nullable = false, unique = true, length = 100)
    private String corporationId;

    /** The planet this corporation operates on. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Corporation name. */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Owner empire ID. */
    @Column(name = "owner_empire_id", nullable = false, length = 100)
    private String ownerEmpireId;

    /** Corporation rank (FREELANCER, STARTUP, REGIONAL, INTERPLANETARY, GALACTIC_MONOPOLY). */
    @Column(name = "rank", nullable = false, length = 30)
    private String rank;

    /** Available credits for spending. */
    @Column(name = "credits", nullable = false)
    private long credits;

    /** Reputation score (0-100). */
    @Column(name = "reputation", nullable = false)
    private int reputation;

    /** Experience points for rank progression. */
    @Column(name = "xp", nullable = false)
    private int xp;

    /** XP needed for next rank. */
    @Column(name = "xp_to_next_rank", nullable = false)
    private int xpToNextRank;

    /** Number of planets where this corporation has assets. */
    @Column(name = "planets_operated", nullable = false)
    private int planetsOperated;

    /** Lifetime total cargo value delivered. */
    @Column(name = "total_cargo_delivered", nullable = false)
    private long totalCargoDelivered;

    /** JSON-serialized set of owned station IDs. */
    @Column(name = "owned_station_ids_json", columnDefinition = "TEXT")
    private String ownedStationIdsJson;

    /** JSON-serialized set of owned vehicle IDs. */
    @Column(name = "owned_vehicle_ids_json", columnDefinition = "TEXT")
    private String ownedVehicleIdsJson;

    /** JSON-serialized set of owned route IDs. */
    @Column(name = "owned_route_ids_json", columnDefinition = "TEXT")
    private String ownedRouteIdsJson;

    /** JSON-serialized set of active contract IDs. */
    @Column(name = "active_contract_ids_json", columnDefinition = "TEXT")
    private String activeContractIdsJson;

    /** JSON-serialized cargo inventory map (CargoType -> amount). */
    @Column(name = "cargo_inventory_json", columnDefinition = "TEXT")
    private String cargoInventoryJson;

    /** JSON-serialized list of active quest IDs. */
    @Column(name = "active_quest_ids_json", columnDefinition = "TEXT")
    private String activeQuestIdsJson;

    /** JSON-serialized list of completed quest IDs. */
    @Column(name = "completed_quest_ids_json", columnDefinition = "TEXT")
    private String completedQuestIdsJson;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlanetCorporationEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(String corporationId) {
        this.corporationId = corporationId;
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

    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    public void setOwnerEmpireId(String ownerEmpireId) {
        this.ownerEmpireId = ownerEmpireId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getXpToNextRank() {
        return xpToNextRank;
    }

    public void setXpToNextRank(int xpToNextRank) {
        this.xpToNextRank = xpToNextRank;
    }

    public int getPlanetsOperated() {
        return planetsOperated;
    }

    public void setPlanetsOperated(int planetsOperated) {
        this.planetsOperated = planetsOperated;
    }

    public long getTotalCargoDelivered() {
        return totalCargoDelivered;
    }

    public void setTotalCargoDelivered(long totalCargoDelivered) {
        this.totalCargoDelivered = totalCargoDelivered;
    }

    public String getOwnedStationIdsJson() {
        return ownedStationIdsJson;
    }

    public void setOwnedStationIdsJson(String ownedStationIdsJson) {
        this.ownedStationIdsJson = ownedStationIdsJson;
    }

    public String getOwnedVehicleIdsJson() {
        return ownedVehicleIdsJson;
    }

    public void setOwnedVehicleIdsJson(String ownedVehicleIdsJson) {
        this.ownedVehicleIdsJson = ownedVehicleIdsJson;
    }

    public String getOwnedRouteIdsJson() {
        return ownedRouteIdsJson;
    }

    public void setOwnedRouteIdsJson(String ownedRouteIdsJson) {
        this.ownedRouteIdsJson = ownedRouteIdsJson;
    }

    public String getActiveContractIdsJson() {
        return activeContractIdsJson;
    }

    public void setActiveContractIdsJson(String activeContractIdsJson) {
        this.activeContractIdsJson = activeContractIdsJson;
    }

    public String getCargoInventoryJson() {
        return cargoInventoryJson;
    }

    public void setCargoInventoryJson(String cargoInventoryJson) {
        this.cargoInventoryJson = cargoInventoryJson;
    }

    public String getActiveQuestIdsJson() {
        return activeQuestIdsJson;
    }

    public void setActiveQuestIdsJson(String activeQuestIdsJson) {
        this.activeQuestIdsJson = activeQuestIdsJson;
    }

    public String getCompletedQuestIdsJson() {
        return completedQuestIdsJson;
    }

    public void setCompletedQuestIdsJson(String completedQuestIdsJson) {
        this.completedQuestIdsJson = completedQuestIdsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PlanetCorporationEntity{" +
                "id=" + id +
                ", corporationId='" + corporationId + '\'' +
                ", planetId=" + planetId +
                ", name='" + name + '\'' +
                ", rank='" + rank + '\'' +
                ", credits=" + credits +
                ", reputation=" + reputation +
                '}';
    }
}
