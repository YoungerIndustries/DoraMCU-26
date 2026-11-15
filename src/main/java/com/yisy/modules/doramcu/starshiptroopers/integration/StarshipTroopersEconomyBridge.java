package com.yisy.modules.doramcu.starshiptroopers.integration;

import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bridges Starship Troopers economy to the core FourxEconomyService system.
 * <p>
 * Starship Troopers uses federal credits as currency. This adapter maps
 * colony production, federal budget, and military spending to the 4X economy system.
 * </p>
 */
@Component
public class StarshipTroopersEconomyBridge implements FourxEconomyService {

    private final Map<String, FourxColony> colonies = new HashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new HashMap<>();

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("factionId", factionId);
        summary.put("module", "starshiptroopers");
        summary.put("currency", "Federal Credits");
        summary.put("credits", 75000);
        summary.put("colonyProduction", 3000);
        summary.put("militarySpending", 20000);
        summary.put("income", 10000);
        summary.put("expenses", 15000);
        summary.put("netIncome", -5000);
        return summary;
    }

    @Override
    public void processEconomyTurn() {
        // Process all Starship Troopers faction economies
    }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        FourxColony colony = new FourxColony("colony-" + colonyName.hashCode(), colonyName, factionId, starSystemId);
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
        route.setProfit(3000);
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
        return 3000;
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) {
    }
}
