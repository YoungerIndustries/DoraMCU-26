package com.yisy.modules.doramcu.zero_zero_seven.model;

import com.yisy.modules.doramcu.earth2026.model.TechLevel;
import jakarta.persistence.*;

/**
 * Represents a hex region on the Earth map in the 007 game.
 * <p>
 * Territories form the hex-based map of Earth regions. Each territory has resources,
 * an owner empire, and can host spy networks. Empires expand and compete for control
 * of these territories.
 * </p>
 */
@Entity
@Table(name = "bond_territories")
public class BondTerritory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Description of the territory. */
    @Column(length = 500)
    private String description;

    /** Hex X position on the map. */
    @Column(name = "hex_x", nullable = false)
    private int hexX;

    /** Hex Y position on the map. */
    @Column(name = "hex_y", nullable = false)
    private int hexY;

    /** Owner empire ID (nullable if uncontrolled). */
    @Column(name = "owner_empire_id")
    private Long ownerEmpireId;

    /** Tech level of the territory. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tech_level")
    private TechLevel techLevel;

    /** Mineral resources. */
    @Column(nullable = false)
    private int minerals = 0;

    /** Energy resources. */
    @Column(nullable = false)
    private int energy = 0;

    /** Food resources. */
    @Column(nullable = false)
    private int food = 0;

    /** Whether this territory has a spy network. */
    @Column(name = "has_spy_network", nullable = false)
    private boolean hasSpyNetwork = false;

    /** Whether this territory is operational. */
    @Column(nullable = false)
    private boolean operational = true;

    /** Population of the territory. */
    @Column(nullable = false)
    private long population = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public BondTerritory() {
    }

    public BondTerritory(String name, int hexX, int hexY, BondGameSession gameSession) {
        this.name = name;
        this.hexX = hexX;
        this.hexY = hexY;
        this.gameSession = gameSession;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getHexX() { return hexX; }
    public void setHexX(int hexX) { this.hexX = hexX; }

    public int getHexY() { return hexY; }
    public void setHexY(int hexY) { this.hexY = hexY; }

    public Long getOwnerEmpireId() { return ownerEmpireId; }
    public void setOwnerEmpireId(Long ownerEmpireId) { this.ownerEmpireId = ownerEmpireId; }

    public TechLevel getTechLevel() { return techLevel; }
    public void setTechLevel(TechLevel techLevel) { this.techLevel = techLevel; }

    public int getMinerals() { return minerals; }
    public void setMinerals(int minerals) { this.minerals = minerals; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = energy; }

    public int getFood() { return food; }
    public void setFood(int food) { this.food = food; }

    public boolean isHasSpyNetwork() { return hasSpyNetwork; }
    public void setHasSpyNetwork(boolean hasSpyNetwork) { this.hasSpyNetwork = hasSpyNetwork; }

    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }

    public long getPopulation() { return population; }
    public void setPopulation(long population) { this.population = population; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Check if this territory is uncontrolled.
     */
    public boolean isUncontrolled() {
        return ownerEmpireId == null;
    }

    /**
     * Calculate total resource output.
     */
    public int getTotalResources() {
        return minerals + energy + food;
    }
}
