package com.yisy.modules.doramcu.warcraft.integration;

import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Warcraft economy bridge — connects Warcraft to the FourX economy system.
 * <p>
 * Uses Gold and Lumber as currency, with trade routes between settlements.
 * </p>
 */
@Component
public class WarcraftEconomyBridge implements FourxEconomyService {

    private final Map<String, FourxColony> colonies = new HashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new HashMap<>();

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("factionId", factionId);
        summary.put("module", "warcraft");
        summary.put("currency", "Gold/Lumber");
        summary.put("gold", 10000);
        summary.put("lumber", 5000);
        summary.put("income", 8000);
        summary.put("expenses", 4000);
        summary.put("netIncome", 4000);
        return summary;
    }

    @Override
    public void processEconomyTurn() { }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        FourxColony colony = new FourxColony("system-" + colonyName.hashCode(), colonyName, factionId, starSystemId);
        colonies.put(colony.getId(), colony);
        return colony;
    }

    @Override
    public List<FourxColony> getColonies(String factionId) { return new ArrayList<>(colonies.values()); }

    @Override
    public FourxColony getColony(String colonyId) { return colonies.get(colonyId); }

    @Override
    public FourxColony addBuilding(String colonyId, String building) {
        FourxColony colony = colonies.get(colonyId);
        if (colony != null) colony.addBuilding(building);
        return colony;
    }

    @Override
    public FourxTradeRoute createTradeRoute(String factionId, String originSystemId, String destinationSystemId, FourxTradeRoute.CargoType cargoType) {
        String id = "route-" + factionId + "-" + originSystemId + "-" + destinationSystemId;
        FourxTradeRoute route = new FourxTradeRoute(originSystemId, destinationSystemId, cargoType, factionId);
        route.setId(id); route.setProfit(3000); route.setActive(true);
        tradeRoutes.put(id, route);
        return route;
    }

    @Override
    public List<FourxTradeRoute> getTradeRoutes(String factionId) { return new ArrayList<>(tradeRoutes.values()); }

    @Override
    public int getTradeProfit(String factionId) { return 3000; }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) { }
}
