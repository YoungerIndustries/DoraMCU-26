package com.yisy.modules.doramcu.fourx;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared colony model for the 4X framework.
 * <p>
 * A colony represents a settlement on a planet with population, resource production,
 * and buildings. It belongs to a faction and is located in a star system.
 * </p>
 * <p>
 * This is a plain DTO — not a JPA entity.
 * </p>
 */
public class FourxColony {

    private String id;
    private String name;

    /** Population of the colony. */
    private long population;

    /** Mineral resources per turn. */
    private int mineralsPerTurn;

    /** Energy resources per turn. */
    private int energyPerTurn;

    /** Food resources per turn. */
    private int foodPerTurn;

    /** Research points per turn. */
    private int researchPerTurn;

    /** List of building names. */
    private List<String> buildings = new ArrayList<>();

    /** Owner faction ID. */
    private String ownerFactionId;

    /** Star system ID this colony is in. */
    private String starSystemId;

    /** Whether this colony is operational. */
    private boolean operational = true;

    public FourxColony() {
    }

    public FourxColony(String id, String name, String ownerFactionId, String starSystemId) {
        this.id = id;
        this.name = name;
        this.ownerFactionId = ownerFactionId;
        this.starSystemId = starSystemId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(String ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public String getStarSystemId() { return starSystemId; }
    public void setStarSystemId(String starSystemId) { this.starSystemId = starSystemId; }

    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }

    /** Add a building to the colony. */
    public void addBuilding(String building) {
        buildings.add(building);
    }
}
