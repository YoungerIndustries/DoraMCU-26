package com.yisy.modules.doramcu.fps.integration;

import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bridges FPS economy to the core FourxEconomyService system.
 * <p>
 * FPS uses credits for weapon/ammo trading and base construction.
 * </p>
 */
@Component
public class FpsEconomyBridge implements FourxEconomyService {

    private final Map<String, FourxColony> colonies = new HashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new HashMap<>();

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("factionId", factionId);
        summary.put("module", "fps");
        summary.put("currency", "Credits");
        summary.put("credits", 5000);
        summary.put("income", 500);
        summary.put("expenses", 250);
        summary.put("netIncome", 250);
        return summary;
    }

    @Override
    public void processEconomyTurn() {
    }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        FourxColony colony = new FourxColony("base-" + colonyName.hashCode(), colonyName, factionId, starSystemId);
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
        route.setProfit(200);
        tradeRoutes.put(id, route);
        return route;
    }

    @Override
    public List<FourxTradeRoute> getTradeRoutes(String factionId) {
        return new ArrayList<>(tradeRoutes.values());
    }

    @Override
    public int getTradeProfit(String factionId) {
        return 200;
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) {
    }
}
