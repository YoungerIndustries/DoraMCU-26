package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5Race;
import com.yisy.modules.doramcu.fourx.FourxColony;
import com.yisy.modules.doramcu.fourx.FourxEconomyService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.fourx.FourxTradeRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * B5 implementation of the Economy 4X pillar.
 * <p>
 * Manages colony production, trade routes, and resource management for B5 factions.
 * Uses B5Race tradeBonus to influence colony output and trade profit.
 * </p>
 */
@Service
public class B5EconomyService implements FourxEconomyService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(B5EconomyService.class);

    private final Babylon5DatabaseService databaseService;
    private final Map<String, FourxColony> colonies = new ConcurrentHashMap<>();
    private final Map<String, FourxTradeRoute> tradeRoutes = new ConcurrentHashMap<>();
    private final AtomicLong colonyIdCounter = new AtomicLong(1);
    private final AtomicLong tradeRouteIdCounter = new AtomicLong(1);
    private final Random random = new Random();

    public B5EconomyService(Babylon5DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.BABYLON5;
    }

    @Override
    public Map<String, Object> calculateEconomySummary(String factionId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("factionId", factionId);

        List<FourxColony> factionColonies = getColonies(factionId);
        int totalMinerals = 0;
        int totalEnergy = 0;
        int totalFood = 0;
        int totalResearch = 0;
        long totalPopulation = 0;

        for (FourxColony colony : factionColonies) {
            if (colony.isOperational()) {
                totalMinerals += colony.getMineralsPerTurn();
                totalEnergy += colony.getEnergyPerTurn();
                totalFood += colony.getFoodPerTurn();
                totalResearch += colony.getResearchPerTurn();
                totalPopulation += colony.getPopulation();
            }
        }

        // Add trade income
        List<FourxTradeRoute> factionRoutes = getTradeRoutes(factionId);
        int tradeIncome = 0;
        for (FourxTradeRoute route : factionRoutes) {
            if (route.isActive()) {
                tradeIncome += route.getProfit();
            }
        }

        // Apply trade bonus from race
        B5Race race = databaseService.getRace(factionId).orElse(null);
        if (race != null) {
            tradeIncome += race.getTradeBonus() * factionRoutes.size();
        }

        summary.put("totalMinerals", totalMinerals);
        summary.put("totalEnergy", totalEnergy);
        summary.put("totalFood", totalFood);
        summary.put("totalResearch", totalResearch);
        summary.put("totalPopulation", totalPopulation);
        summary.put("colonyCount", factionColonies.size());
        summary.put("tradeIncome", tradeIncome);
        summary.put("tradeRouteCount", factionRoutes.size());
        summary.put("netIncome", totalMinerals + totalEnergy + tradeIncome);

        return summary;
    }

    @Override
    public void processEconomyTurn() {
        // Process all colonies - grow population
        for (FourxColony colony : colonies.values()) {
            if (colony.isOperational() && colony.getFoodPerTurn() > 0) {
                // Population grows based on food surplus
                long growth = Math.max(1, colony.getFoodPerTurn() / 10);
                colony.setPopulation(colony.getPopulation() + growth);
            }
        }

        // Process trade routes
        for (FourxTradeRoute route : tradeRoutes.values()) {
            if (route.isActive()) {
                // Trade routes can be disrupted
                if (random.nextInt(100) < (100 - route.getSecurityLevel()) / 10) {
                    log.info("Trade route {} disrupted!", route.getId());
                    route.setActive(false);
                }
            }
        }

        log.info("Economy turn processed: {} colonies, {} trade routes", 
                colonies.size(), tradeRoutes.size());
    }

    @Override
    public FourxColony createColony(String factionId, String colonyName, String starSystemId) {
        String id = String.valueOf(colonyIdCounter.getAndIncrement());
        FourxColony colony = new FourxColony(id, colonyName, factionId, starSystemId);

        // Set initial population
        colony.setPopulation(100000 + random.nextInt(900000));

        // Calculate production based on race tradeBonus
        B5Race race = databaseService.getRace(factionId).orElse(null);
        int tradeBonus = race != null ? race.getTradeBonus() : 0;

        colony.setMineralsPerTurn(10 + random.nextInt(20) + tradeBonus);
        colony.setEnergyPerTurn(5 + random.nextInt(15));
        colony.setFoodPerTurn(15 + random.nextInt(25));
        colony.setResearchPerTurn(5 + random.nextInt(10) + (race != null ? race.getNavigationDM() : 0));

        // Add initial buildings
        colony.addBuilding("Command Center");
        colony.addBuilding("Habitat Ring");

        colonies.put(id, colony);
        log.info("Created colony {} for faction {} in system {}", colonyName, factionId, starSystemId);
        return colony;
    }

    @Override
    public List<FourxColony> getColonies(String factionId) {
        return colonies.values().stream()
                .filter(c -> c.getOwnerFactionId().equals(factionId))
                .toList();
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

        colony.addBuilding(building);

        // Apply building effects
        switch (building) {
            case "Mine" -> colony.setMineralsPerTurn(colony.getMineralsPerTurn() + 10);
            case "Power Plant" -> colony.setEnergyPerTurn(colony.getEnergyPerTurn() + 15);
            case "Farm" -> colony.setFoodPerTurn(colony.getFoodPerTurn() + 20);
            case "Research Lab" -> colony.setResearchPerTurn(colony.getResearchPerTurn() + 10);
            case "Shipyard" -> colony.setEnergyPerTurn(colony.getEnergyPerTurn() - 5);
            case "Market" -> colony.setMineralsPerTurn(colony.getMineralsPerTurn() + 5);
        }

        log.info("Added {} to colony {}", building, colony.getName());
        return colony;
    }

    @Override
    public FourxTradeRoute createTradeRoute(String factionId, String originSystemId, 
                                            String destinationSystemId, FourxTradeRoute.CargoType cargoType) {
        String id = String.valueOf(tradeRouteIdCounter.getAndIncrement());
        FourxTradeRoute route = new FourxTradeRoute(originSystemId, destinationSystemId, cargoType, factionId);
        route.setId(id);

        // Calculate profit based on cargo type and race tradeBonus
        B5Race race = databaseService.getRace(factionId).orElse(null);
        int baseProfit = switch (cargoType) {
            case MINERALS -> 20;
            case ENERGY -> 15;
            case FOOD -> 10;
            case RESEARCH -> 25;
            case GENERAL -> 12;
        };
        int tradeBonus = race != null ? race.getTradeBonus() : 0;
        route.setProfit(baseProfit + tradeBonus * 2);

        // Security based on systems
        route.setSecurityLevel(50 + random.nextInt(30));

        tradeRoutes.put(id, route);
        log.info("Created trade route {} for faction {}: {} -> {} ({})", 
                id, factionId, originSystemId, destinationSystemId, cargoType);
        return route;
    }

    @Override
    public List<FourxTradeRoute> getTradeRoutes(String factionId) {
        return tradeRoutes.values().stream()
                .filter(r -> r.getOwnerFactionId().equals(factionId))
                .toList();
    }

    @Override
    public int getTradeProfit(String factionId) {
        return getTradeRoutes(factionId).stream()
                .filter(FourxTradeRoute::isActive)
                .mapToInt(FourxTradeRoute::getProfit)
                .sum();
    }

    @Override
    public void setTradeRouteActive(String tradeRouteId, boolean active) {
        FourxTradeRoute route = tradeRoutes.get(tradeRouteId);
        if (route != null) {
            route.setActive(active);
            log.info("Trade route {} {} by faction {}", 
                    tradeRouteId, active ? "activated" : "deactivated", route.getOwnerFactionId());
        }
    }
}
