package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dark Age of Camelot economy bridge — connects DaOC to the FourX economy system.
 * <p>
 * Uses Gold, Silver, and Copper as currency with trade routes between realms.
 * </p>
 */
@Component
public class DaocEconomyBridge implements FourxEconomyService {

    private final Map<String, FourxColony> colonies = new HashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new HashMap<>();

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("factionId", factionId);
        summary.put("module", "daoc");
        summary.put("currency", "Gold");
        summary.put("expenses", 3500);

        String realm = factionId != null ? factionId.toLowerCase().trim() : null;
        if (realm != null && realm.isEmpty()) realm = null;
        String resource = getRealmResource(realm);
        double multiplier = getRealmMultiplier(realm);
        summary.put("primaryResource", resource);
        summary.put("realmMultiplier", multiplier);

        // Base values per realm
        int baseGold, baseSilver, baseIncome;
        switch (realm) {
            case "albion":
                baseGold = 10000; baseSilver = 18000; baseIncome = 8500;
                break;
            case "midgard":
                baseGold = 9000; baseSilver = 16000; baseIncome = 7500;
                break;
            case "hibernia":
                baseGold = 7000; baseSilver = 14000; baseIncome = 6500;
                break;
            default:
                baseGold = 8000; baseSilver = 15000; baseIncome = 7000;
                break;
        }

        int gold = (int) (baseGold * multiplier);
        int silver = (int) (baseSilver * multiplier);
        int copper = (int) (baseSilver * 10 * multiplier);
        int income = (int) (baseIncome * multiplier);
        int netIncome = income - 3500;

        summary.put("gold", gold);
        summary.put("silver", silver);
        summary.put("copper", copper);
        summary.put("income", income);
        summary.put("netIncome", netIncome);
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
        route.setId(id);
        double multiplier = getRealmMultiplier(factionId);
        route.setProfit((int) (2500 * multiplier));
        route.setActive(true);
        tradeRoutes.put(id, route);
        return route;
    }

    @Override
    public List<FourxTradeRoute> getTradeRoutes(String factionId) { return new ArrayList<>(tradeRoutes.values()); }

    @Override
    public int getTradeProfit(String factionId) {
        double multiplier = getRealmMultiplier(factionId);
        return (int) (2500 * multiplier);
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) { }

    public String getRealmResource(String realm) {
        if (realm == null) return "General Goods";
        switch (realm.toLowerCase()) {
            case "albion": return "Arcane Reagents";
            case "hibernia": return "Enchanted Herbs";
            case "midgard": return "Runic Stones";
            default: return "General Goods";
        }
    }

    public double getRealmMultiplier(String realm) {
        if (realm == null) return 1.0;
        switch (realm.toLowerCase()) {
            case "albion": return 1.2;
            case "hibernia": return 1.0;
            case "midgard": return 1.1;
            default: return 1.0;
        }
    }
}
