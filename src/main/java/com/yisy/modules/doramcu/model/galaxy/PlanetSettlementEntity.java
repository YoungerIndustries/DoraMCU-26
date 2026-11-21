package com.yisy.modules.doramcu.model.galaxy;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity that persists a settlement on a planet surface.
 */
@Entity
@Table(name = "planet_settlements")
public class PlanetSettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Unique settlement ID (matches Settlement.id, e.g., "SETTLE-1"). */
    @Column(name = "settlement_id", nullable = false, unique = true, length = 100)
    private String settlementId;

    /** The planet this settlement belongs to. */
    @Column(name = "planet_id", nullable = false)
    private int planetId;

    /** Settlement name. */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Center hex X coordinate. */
    @Column(name = "center_hex_x", nullable = false)
    private int centerHexX;

    /** Center hex Y coordinate. */
    @Column(name = "center_hex_y", nullable = false)
    private int centerHexY;

    /** Center hex Z coordinate. */
    @Column(name = "center_hex_z", nullable = false)
    private int centerHexZ;

    /** Settlement size (VILLAGE, TOWN, CITY, METROPOLIS). */
    @Column(name = "size", nullable = false, length = 20)
    private String size;

    /** Current population. */
    @Column(name = "population", nullable = false)
    private int population;

    /** Maximum population for current size. */
    @Column(name = "max_population", nullable = false)
    private int maxPopulation;

    /** Growth rate (turns between growth checks). */
    @Column(name = "growth_rate", nullable = false)
    private int growthRate;

    /** Turns since last growth check. */
    @Column(name = "turns_since_last_growth", nullable = false)
    private int turnsSinceLastGrowth = 0;

    /** JSON-serialized cargo goals map. */
    @Column(name = "cargo_goals_json", columnDefinition = "TEXT")
    private String cargoGoalsJson;

    /** Mail produced per turn. */
    @Column(name = "mail_production", nullable = false)
    private int mailProduction = 0;

    /** Passengers produced per turn. */
    @Column(name = "passenger_production", nullable = false)
    private int passengerProduction = 0;

    /** Whether the settlement is active. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Owner empire ID (nullable). */
    @Column(name = "owner_empire_id", length = 100)
    private String ownerEmpireId;

    /** JSON-serialized list of dungeon IDs for interior rooms. */
    @Column(name = "dungeon_ids_json", columnDefinition = "TEXT")
    private String dungeonIdsJson;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlanetSettlementEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
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

    public int getCenterHexX() {
        return centerHexX;
    }

    public void setCenterHexX(int centerHexX) {
        this.centerHexX = centerHexX;
    }

    public int getCenterHexY() {
        return centerHexY;
    }

    public void setCenterHexY(int centerHexY) {
        this.centerHexY = centerHexY;
    }

    public int getCenterHexZ() {
        return centerHexZ;
    }

    public void setCenterHexZ(int centerHexZ) {
        this.centerHexZ = centerHexZ;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
        this.updatedAt = LocalDateTime.now();
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
        this.updatedAt = LocalDateTime.now();
    }

    public int getMaxPopulation() {
        return maxPopulation;
    }

    public void setMaxPopulation(int maxPopulation) {
        this.maxPopulation = maxPopulation;
    }

    public int getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(int growthRate) {
        this.growthRate = growthRate;
    }

    public int getTurnsSinceLastGrowth() {
        return turnsSinceLastGrowth;
    }

    public void setTurnsSinceLastGrowth(int turnsSinceLastGrowth) {
        this.turnsSinceLastGrowth = turnsSinceLastGrowth;
    }

    public String getCargoGoalsJson() {
        return cargoGoalsJson;
    }

    public void setCargoGoalsJson(String cargoGoalsJson) {
        this.cargoGoalsJson = cargoGoalsJson;
    }

    public int getMailProduction() {
        return mailProduction;
    }

    public void setMailProduction(int mailProduction) {
        this.mailProduction = mailProduction;
    }

    public int getPassengerProduction() {
        return passengerProduction;
    }

    public void setPassengerProduction(int passengerProduction) {
        this.passengerProduction = passengerProduction;
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

    public String getDungeonIdsJson() {
        return dungeonIdsJson;
    }

    public void setDungeonIdsJson(String dungeonIdsJson) {
        this.dungeonIdsJson = dungeonIdsJson;
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
        return "PlanetSettlementEntity{id=" + settlementId + ", name=" + name +
                ", planet=" + planetId + ", pop=" + population + ", size=" + size + "}";
    }
}