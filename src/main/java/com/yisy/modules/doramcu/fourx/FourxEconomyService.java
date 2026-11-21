package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Interface for 4X economy mechanics.
 * Implemented by game-specific economy services (B5, CaptainsLog, etc.).
 */
public interface FourxEconomyService {

    Map<String, Object> calculateEconomySummary(String factionId);

    void processEconomyTurn();

    FourxColony createColony(String factionId, String colonyName, String starSystemId);

    List<FourxColony> getColonies(String factionId);

    FourxColony getColony(String colonyId);

    FourxColony addBuilding(String colonyId, String building);

    FourxTradeRoute createTradeRoute(String factionId, String originSystemId, String destinationSystemId, FourxTradeRoute.CargoType cargoType);

    List<FourxTradeRoute> getTradeRoutes(String factionId);

    int getTradeProfit(String factionId);

    void setTradeRouteActive(String tradeRouteId, boolean active);
}
