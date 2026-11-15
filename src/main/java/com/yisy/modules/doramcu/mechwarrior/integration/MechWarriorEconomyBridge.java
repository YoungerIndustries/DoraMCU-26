package com.yisy.modules.doramcu.mechwarrior.integration;

import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bridges MechWarrior economy to the core FourxEconomyService system.
 * <p>
 * MechWarrior uses C-Bills (CB) as currency. This adapter maps industrial output,
 * mercenary contracts, and commodity trading to the 4X economy system.
 * </p>
 */
@Component
public class MechWarriorEconomyBridge implements FourxEconomyService {

    private final Map<String, FourxColony> colonies = new HashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new HashMap<>();

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("factionId", factionId);
        summary.put("module", "mechwarrior");
        summary.put("currency", "C-Bills");
        summary.put("cBills", 50000);
        summary.put("industrialOutput", 1000);
        summary.put("mercenaryContracts", 5);
        summary.put("income", 5000);
        summary.put("expenses", 2500);
        summary.put("netIncome", 2500);
        return summary;
    }

    @Override
    public void processEconomyTurn() {
        // Process all MechWarrior faction economies
    }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        FourxColony colony = new FourxColony("world-" + colonyName.hashCode(), colonyName, factionId, starSystemId);
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
        route.setProfit(1000);
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
        return 1000;
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) {
    }
}
