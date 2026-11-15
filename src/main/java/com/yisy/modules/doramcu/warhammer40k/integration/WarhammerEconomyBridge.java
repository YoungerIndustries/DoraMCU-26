package com.yisy.modules.doramcu.warhammer40k.integration;

import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Warhammer 40K economy bridge — connects Warhammer 40K to the FourX economy system.
 * <p>
 * Uses Thrones as currency, with industrial output from forge worlds and war economy.
 * </p>
 */
@Component
public class WarhammerEconomyBridge implements FourxEconomyService {

    private final Map<String, FourxColony> colonies = new HashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new HashMap<>();

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("factionId", factionId);
        summary.put("module", "warhammer40k");
        summary.put("currency", "Thrones");
        summary.put("thrones", 100000);
        summary.put("industrialOutput", 5000);
        summary.put("warEconomy", 10000);
        summary.put("income", 15000);
        summary.put("expenses", 8000);
        summary.put("netIncome", 7000);
        return summary;
    }

    @Override
    public void processEconomyTurn() {
        // Process all Warhammer 40K faction economies
    }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        FourxColony colony = new FourxColony("system-" + colonyName.hashCode(), colonyName, factionId, starSystemId);
        colonies.put(colony.getId(), colony);
        return colony;
    }

    @Override
    public List<FourxColony> getColonies(String factionId) {
        return new ArrayList<>(colonies.values());
    }

    @Override
    public FourxColony getColony(String colonyId) {
        return colonies.get(colonyId);
    }

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
        route.setId(id);
        route.setProfit(5000);
        route.setActive(true);
        tradeRoutes.put(id, route);
        return route;
    }

    @Override
    public List<FourxTradeRoute> getTradeRoutes(String factionId) {
        return new ArrayList<>(tradeRoutes.values());
    }

    @Override
    public int getTradeProfit(String factionId) {
        return 5000;
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) {
    }
}
