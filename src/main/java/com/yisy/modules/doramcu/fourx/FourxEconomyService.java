package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Shared interface for the Economy 4X pillar.
 * <p>
 * Implementing services handle colony production, trade routes, resource management,
 * and turn-based economy processing. Each game module provides its own implementation.
 * </p>
 */
public interface FourxEconomyService {

    /**
     * Calculate economy summary for a faction.
     *
     * @param factionId the faction ID
     * @return economy summary with income, expenses, and net income
     */
    Map<String, Object> calculateEconomySummary(String factionId);

    /**
     * Process economy for a turn across all factions.
     */
    void processEconomyTurn();

    /**
     * Create a new colony on a planet.
     *
     * @param factionId    the owning faction ID
     * @param colonyName   the colony name
     * @param starSystemId the star system ID where the colony is located
     * @return the created colony
     */
    FourxColony createColony(String factionId, String colonyName, String starSystemId);

    /**
     * Get all colonies for a faction.
     *
     * @param factionId the faction ID
     * @return list of colonies
     */
    List<FourxColony> getColonies(String factionId);

    /**
     * Get a colony by ID.
     *
     * @param colonyId the colony ID
     * @return the colony, or null if not found
     */
    FourxColony getColony(String colonyId);

    /**
     * Add a building to a colony.
     *
     * @param colonyId  the colony ID
     * @param building  the building name
     * @return the updated colony
     */
    FourxColony addBuilding(String colonyId, String building);

    /**
     * Create a trade route between two star systems.
     *
     * @param factionId          the owning faction ID
     * @param originSystemId     the origin system ID
     * @param destinationSystemId the destination system ID
     * @param cargoType          the cargo type
     * @return the created trade route
     */
    FourxTradeRoute createTradeRoute(String factionId, String originSystemId, String destinationSystemId, FourxTradeRoute.CargoType cargoType);

    /**
     * Get all trade routes for a faction.
     *
     * @param factionId the faction ID
     * @return list of trade routes
     */
    List<FourxTradeRoute> getTradeRoutes(String factionId);

    /**
     * Get trade profit for a faction.
     *
     * @param factionId the faction ID
     * @return total trade profit
     */
    int getTradeProfit(String factionId);

    /**
     * Activate or deactivate a trade route.
     *
     * @param tradeRouteId the trade route ID
     * @param active       whether to activate or deactivate
     */
    void setTradeRouteActive(String tradeRouteId, boolean active);
}
