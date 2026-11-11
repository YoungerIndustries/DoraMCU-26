package com.yisy.modules.doramcu.star_trek_4x.expansion;

import com.yisy.modules.doramcu.star_trek_4x.model.Colony;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service for managing colony growth, population, and resource production.
 * <p>
 * Handles population increase, resource generation, and building effects
 * for each colony per turn.
 * </p>
 */
@Service
public class ColonyManager {

    private static final Logger log = LoggerFactory.getLogger(ColonyManager.class);
    private static final Random random = new Random();

    /**
     * Process colony growth for one turn.
     *
     * @param colony the colony to process
     */
    public void processGrowth(Colony colony) {
        // Population growth (1-5% per turn)
        long populationGrowth = (long) (colony.getPopulation() * (0.01 + random.nextDouble() * 0.04));
        colony.setPopulation(colony.getPopulation() + populationGrowth);

        // Resource production based on buildings
        int buildingBonus = colony.getBuildings().size();

        // Minerals increase with mining facilities
        if (colony.getBuildings().contains("Mining Facility")) {
            colony.setMineralsPerTurn(colony.getMineralsPerTurn() + 5);
        }

        // Energy increase with power plants
        if (colony.getBuildings().contains("Power Plant")) {
            colony.setEnergyPerTurn(colony.getEnergyPerTurn() + 3);
        }

        // Food increase with farms
        if (colony.getBuildings().contains("Agricultural Center")) {
            colony.setFoodPerTurn(colony.getFoodPerTurn() + 4);
        }

        // Research increase with labs
        if (colony.getBuildings().contains("Research Lab")) {
            colony.setResearchPerTurn(colony.getResearchPerTurn() + 2);
        }

        // Random events
        processRandomEvents(colony);
    }

    /**
     * Process random events that can affect colony growth.
     */
    private void processRandomEvents(Colony colony) {
        int eventRoll = random.nextInt(100);

        if (eventRoll < 5) {
            // Plague reduces population
            long populationLoss = (long) (colony.getPopulation() * 0.1);
            colony.setPopulation(colony.getPopulation() - populationLoss);
            log.warn("Plague hit colony '{}': population reduced by {}", colony.getName(), populationLoss);
        } else if (eventRoll < 10) {
            // Resource discovery increases minerals
            colony.setMineralsPerTurn(colony.getMineralsPerTurn() + 2);
            log.info("Resource discovery at colony '{}': minerals increased", colony.getName());
        } else if (eventRoll < 15) {
            // Trade boom increases energy
            colony.setEnergyPerTurn(colony.getEnergyPerTurn() + 1);
            log.info("Trade boom at colony '{}': energy increased", colony.getName());
        }
    }

    /**
     * Calculate total resources for a colony.
     *
     * @param colony the colony
     * @return total resources map
     */
    public java.util.Map<String, Object> calculateResources(Colony colony) {
        return java.util.Map.of(
                "population", colony.getPopulation(),
                "mineralsPerTurn", colony.getMineralsPerTurn(),
                "energyPerTurn", colony.getEnergyPerTurn(),
                "foodPerTurn", colony.getFoodPerTurn(),
                "researchPerTurn", colony.getResearchPerTurn(),
                "buildings", colony.getBuildings()
        );
    }

    /**
     * Check if a colony can sustain its population.
     *
     * @param colony the colony
     * @return true if food production meets population needs
     */
    public boolean canSustainPopulation(Colony colony) {
        // Each 1000 population needs 1 food per turn
        long foodNeeded = colony.getPopulation() / 1000;
        return colony.getFoodPerTurn() >= foodNeeded;
    }

    /**
     * Add a building to a colony.
     *
     * @param colony   the colony
     * @param building the building name
     */
    public void addBuilding(Colony colony, String building) {
        colony.addBuilding(building);
        log.info("Building '{}' added to colony '{}'", building, colony.getName());
    }
}
