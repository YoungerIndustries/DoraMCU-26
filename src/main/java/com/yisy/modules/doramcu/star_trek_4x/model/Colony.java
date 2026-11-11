package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a colony established on a planet.
 * <p>
 * A colony has population, resources, buildings, and an owner faction.
 * Links to Earth 2026 country data via earth2026CountryId.
 * </p>
 */
@Entity
@Table(name = "st4x_colonies")
public class Colony {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Population of the colony. */
    @Column(nullable = false)
    private long population = 0;

    /** Mineral resources per turn. */
    @Column(nullable = false)
    private int mineralsPerTurn = 0;

    /** Energy resources per turn. */
    @Column(nullable = false)
    private int energyPerTurn = 0;

    /** Food resources per turn. */
    @Column(nullable = false)
    private int foodPerTurn = 0;

    /** Research points per turn. */
    @Column(nullable = false)
    private int researchPerTurn = 0;

    /** List of building names. */
    @ElementCollection
    @CollectionTable(name = "st4x_colony_buildings", joinColumns = @JoinColumn(name = "colony_id"))
    @Column(name = "building_name")
    private List<String> buildings = new ArrayList<>();

    /** Owner faction ID. */
    @Column(nullable = false)
    private Long ownerFactionId;

    /** Star system this colony is in. */
    @Column(nullable = false)
    private Long starSystemId;

    /** Link to Earth 2026 country data. */
    @Column(length = 50)
    private String earth2026CountryId;

    /** Whether this colony is operational. */
    @Column(nullable = false)
    private boolean operational = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public Colony() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPopulation() { return population; }
    public void setPopulation(long population) { this.population = population; }

    public int getMineralsPerTurn() { return mineralsPerTurn; }
    public void setMineralsPerTurn(int mineralsPerTurn) { this.mineralsPerTurn = mineralsPerTurn; }

    public int getEnergyPerTurn() { return energyPerTurn; }
    public void setEnergyPerTurn(int energyPerTurn) { this.energyPerTurn = energyPerTurn; }

    public int getFoodPerTurn() { return foodPerTurn; }
    public void setFoodPerTurn(int foodPerTurn) { this.foodPerTurn = foodPerTurn; }

    public int getResearchPerTurn() { return researchPerTurn; }
    public void setResearchPerTurn(int researchPerTurn) { this.researchPerTurn = researchPerTurn; }

    public List<String> getBuildings() { return buildings; }
    public void setBuildings(List<String> buildings) { this.buildings = buildings; }

    public Long getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(Long ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public Long getStarSystemId() { return starSystemId; }
    public void setStarSystemId(Long starSystemId) { this.starSystemId = starSystemId; }

    public String getEarth2026CountryId() { return earth2026CountryId; }
    public void setEarth2026CountryId(String earth2026CountryId) { this.earth2026CountryId = earth2026CountryId; }

    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Add a building to the colony.
     */
    public void addBuilding(String building) {
        buildings.add(building);
    }
}
