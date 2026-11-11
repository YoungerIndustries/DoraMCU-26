package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SFB implementation of the Economy 4X pillar.
 * <p>
 * Handles colony production, trade routes, resource management, and turn-based
 * economy processing for SFB empires.
 * </p>
 */
@Service
public class SfbEconomyService implements FourxEconomyService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(SfbEconomyService.class);

    /** Colonies keyed by colony ID. */
    private final Map<String, FourxColony> colonies = new ConcurrentHashMap<>();

    /** Trade routes keyed by route ID. */
    private final Map<String, FourxTradeRoute> tradeRoutes = new ConcurrentHashMap<>();

    /** Colonies per faction. */
    private final Map<String, List<String>> factionColonies = new ConcurrentHashMap<>();

    /** Trade routes per faction. */
    private final Map<String, List<String>> factionTradeRoutes = new ConcurrentHashMap<>();

    private final AtomicLong colonyIdCounter = new AtomicLong(1);
    private final AtomicLong tradeRouteIdCounter = new AtomicLong(1);
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        log.info("SfbEconomyService initialized");
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.CUSTOM;
    }

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        List<FourxColony> factionColonies = getColonies(factionId);

        int totalMinerals = 0;
        int totalEnergy = 0;
        int totalFood = 0;
        int totalResearch = 0;
        int totalPopulation = 0;

        for (FourxColony colony : factionColonies) {
            totalMinerals += colony.getMineralsPerTurn();
            totalEnergy += colony.getEnergyPerTurn();
            totalFood += colony.getFoodPerTurn();
            totalResearch += colony.getResearchPerTurn();
            totalPopulation += (int) colony.getPopulation();
        }

        int tradeProfit = getTradeProfit(factionId);
        int maintenanceCost = factionColonies.size() * 10; // 10 credits per colony
        int netIncome = totalMinerals + totalEnergy + totalFood + totalResearch + tradeProfit - maintenanceCost;

        summary.put("faction", factionId);
        summary.put("colonies", factionColonies.size());
        summary.put("population", totalPopulation);
        summary.put("minerals", totalMinerals);
        summary.put("energy", totalEnergy);
        summary.put("food", totalFood);
        summary.put("research", totalResearch);
        summary.put("tradeProfit", tradeProfit);
        summary.put("maintenanceCost", maintenanceCost);
        summary.put("netIncome", netIncome);

        return summary;
    }

    @Override
    public void processEconomyTurn() {
        // Process colony production for all colonies
        for (FourxColony colony : colonies.values()) {
            // Base production
            int baseProduction = Math.max(1, (int) (colony.getPopulation() / 100));

            // Building bonuses
            for (String building : colony.getBuildings()) {
                switch (building) {
                    case "MINING_FACILITY":
                        colony.setMineralsPerTurn(colony.getMineralsPerTurn() + baseProduction + 5);
                        break;
                    case "POWER_PLANT":
                        colony.setEnergyPerTurn(colony.getEnergyPerTurn() + baseProduction + 5);
                        break;
                    case "FARM":
                        colony.setFoodPerTurn(colony.getFoodPerTurn() + baseProduction + 5);
                        break;
                    case "RESEARCH_LAB":
                        colony.setResearchPerTurn(colony.getResearchPerTurn() + baseProduction + 3);
                        break;
                    case "HABITAT":
                        colony.setPopulation(colony.getPopulation() + baseProduction / 2);
                        break;
                    case "SHIPYARD":
                        colony.setEnergyPerTurn(colony.getEnergyPerTurn() + 3);
                        break;
                }
            }

            // Population growth
            long pop = colony.getPopulation();
            if (colony.getFoodPerTurn() > pop / 10) {
                colony.setPopulation(pop + Math.max(1, pop / 50));
            }

            // Consumption
            int consumption = (int) (pop / 50);
            colony.setFoodPerTurn(Math.max(0, colony.getFoodPerTurn() - consumption));
            colony.setEnergyPerTurn(Math.max(0, colony.getEnergyPerTurn() - consumption / 2));
        }

        // Process trade routes
        for (FourxTradeRoute route : tradeRoutes.values()) {
            if (route.isActive()) {
                int profit = calculateTradeProfit(route);
                route.setProfit(route.getProfit() + profit);
            }
        }

        log.info("Processed economy turn: {} colonies, {} trade routes",
                colonies.size(), tradeRoutes.size());
    }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        String id = String.valueOf(colonyIdCounter.getAndIncrement());
        FourxColony colony = new FourxColony();
        colony.setId(id);
        colony.setName(colonyName);
        colony.setOwnerFactionId(factionId);
        colony.setStarSystemId(starSystemId);
        colony.setPopulation(100);
        colony.setMineralsPerTurn(10);
        colony.setEnergyPerTurn(10);
        colony.setFoodPerTurn(20);
        colony.setResearchPerTurn(5);
        colony.setBuildings(List.of("HABITAT"));

        colonies.put(id, colony);
        factionColonies.computeIfAbsent(factionId, k -> new ArrayList<>()).add(id);

        log.info("Created colony: {} ({}) for {}", colonyName, id, factionId);
        return colony;
    }

    @Override
    public List<FourxColony> getColonies(String factionId) {
        List<String> colonyIds = factionColonies.get(factionId);
        if (colonyIds == null) return List.of();

        List<FourxColony> result = new ArrayList<>();
        for (String id : colonyIds) {
            FourxColony colony = colonies.get(id);
            if (colony != null) {
                result.add(colony);
            }
        }
        return result;
    }

    @Override
    public FourxColony getColony(String colonyId) {
        return colonies.get(colonyId);
    }

    @Override
    public FourxColony addBuilding(String colonyId, String building) {
        FourxColony colony = colonies.get(colonyId);
        if (colony == null) {
            log.warn("Colony not found: {}", colonyId);
            return null;
        }

        List<String> buildings = colony.getBuildings();
        if (!buildings.contains(building)) {
            List<String> newBuildings = new ArrayList<>(buildings);
            newBuildings.add(building);
            colony.setBuildings(newBuildings);
        }

        // Apply building effects
        switch (building) {
            case "MINING_FACILITY":
                colony.setMineralsPerTurn(colony.getMineralsPerTurn() + 20);
                break;
            case "POWER_PLANT":
                colony.setEnergyPerTurn(colony.getEnergyPerTurn() + 20);
                break;
            case "FARM":
                colony.setFoodPerTurn(colony.getFoodPerTurn() + 20);
                break;
            case "RESEARCH_LAB":
                colony.setResearchPerTurn(colony.getResearchPerTurn() + 10);
                break;
            case "HABITAT":
                colony.setPopulation(colony.getPopulation() + 50);
                break;
            case "SHIPYARD":
                colony.setEnergyPerTurn(colony.getEnergyPerTurn() + 10);
                break;
        }

        log.info("Added building {} to colony {}", building, colonyId);
        return colony;
    }

    @Override
    public FourxTradeRoute createTradeRoute(String factionId, String originSystemId,
                                             String destinationSystemId,
                                             FourxTradeRoute.CargoType cargoType) {
        String id = String.valueOf(tradeRouteIdCounter.getAndIncrement());
        FourxTradeRoute route = new FourxTradeRoute();
        route.setId(id);
        route.setOwnerFactionId(factionId);
        route.setOriginSystemId(originSystemId);
        route.setDestinationSystemId(destinationSystemId);
        route.setCargoType(cargoType);
        route.setProfit(0);
        route.setActive(true);
        route.setSecurityLevel(50);

        tradeRoutes.put(id, route);
        factionTradeRoutes.computeIfAbsent(factionId, k -> new ArrayList<>()).add(id);

        log.info("Created trade route: {} -> {} ({})", originSystemId, destinationSystemId, cargoType);
        return route;
    }

    @Override
    public List<FourxTradeRoute> getTradeRoutes(String factionId) {
        List<String> routeIds = factionTradeRoutes.get(factionId);
        if (routeIds == null) return List.of();

        List<FourxTradeRoute> result = new ArrayList<>();
        for (String id : routeIds) {
            FourxTradeRoute route = tradeRoutes.get(id);
            if (route != null) {
                result.add(route);
            }
        }
        return result;
    }

    @Override
    public int getTradeProfit(String factionId) {
        List<FourxTradeRoute> routes = getTradeRoutes(factionId);
        int totalProfit = 0;
        for (FourxTradeRoute route : routes) {
            if (route.isActive()) {
                totalProfit += route.getProfit();
            }
        }
        return totalProfit;
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) {
        FourxTradeRoute route = tradeRoutes.get(tradeRouteId);
        if (route != null) {
            route.setActive(active);
            log.info("Trade route {} {}", tradeRouteId, active ? "activated" : "deactivated");
        }
    }

    private int calculateTradeProfit(FourxTradeRoute route) {
        int baseProfit = 5;

        // Cargo type multiplier
        switch (route.getCargoType()) {
            case MINERALS:
                baseProfit = 8;
                break;
            case ENERGY:
                baseProfit = 7;
                break;
            case FOOD:
                baseProfit = 5;
                break;
            case RESEARCH:
                baseProfit = 10;
                break;
            case GENERAL:
            default:
                baseProfit = 6;
                break;
        }

        // Security level affects profit
        int securityBonus = route.getSecurityLevel() / 20;
        baseProfit += securityBonus;

        // Random fluctuation
        baseProfit += random.nextInt(3) - 1;

        return Math.max(1, baseProfit);
    }
}
