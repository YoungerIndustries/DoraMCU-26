package com.yisy.modules.doramcu.babylon5.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinator for trade simulation across B5 star systems.
 * <p>
 * The TradeSimulator manages market conditions for multiple systems,
 * finds profitable trade routes, executes trade missions, and tracks
 * trade statistics.
 * </p>
 * <p>
 * It integrates with {@link B5MarketConditions} to provide trade opportunities
 * between all systems, considering market conditions, commodity availability,
 * and ship cargo capacity.
 * </p>
 * <p>
 * Trade missions follow the B5 universe themes: smuggling Shadow crystals,
 * delivering Minbari incense, trading Narn grain, and more.
 * </p>
 */
public class B5TradeSimulator {

    private final Map<String, B5MarketConditions> markets;
    private final List<B5TradeMission> missionHistory;
    private final Random random;
    private int tick;

    /**
     * A trade route between two systems.
     */
    public static class B5TradeRoute {
        private final String originSystemId;
        private final String destinationSystemId;
        private final B5Commodity commodity;
        private final int units;
        private final int buyPrice;
        private final int sellPrice;
        private final int totalCost;
        private final int totalRevenue;
        private final int profit;
        private final double profitMargin;

        B5TradeRoute(String originSystemId, String destinationSystemId,
                     B5Commodity commodity, int units, int buyPrice, int sellPrice) {
            this.originSystemId = originSystemId;
            this.destinationSystemId = destinationSystemId;
            this.commodity = commodity;
            this.units = units;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.totalCost = buyPrice * units;
            this.totalRevenue = sellPrice * units;
            this.profit = totalRevenue - totalCost;
            this.profitMargin = totalCost > 0 ? (double) profit / totalCost * 100.0 : 0.0;
        }

        public String getOriginSystemId() {
            return originSystemId;
        }

        public String getDestinationSystemId() {
            return destinationSystemId;
        }

        public B5Commodity getCommodity() {
            return commodity;
        }

        public int getUnits() {
            return units;
        }

        public int getBuyPrice() {
            return buyPrice;
        }

        public int getSellPrice() {
            return sellPrice;
        }

        public int getTotalCost() {
            return totalCost;
        }

        public int getTotalRevenue() {
            return totalRevenue;
        }

        public int getProfit() {
            return profit;
        }

        public double getProfitMargin() {
            return profitMargin;
        }

        /**
         * Returns a summary of this trade route.
         *
         * @return a map with route details
         */
        public Map<String, Object> toSummary() {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("origin", originSystemId);
            summary.put("destination", destinationSystemId);
            summary.put("commodity", commodity.getName());
            summary.put("units", units);
            summary.put("buyPrice", buyPrice);
            summary.put("sellPrice", sellPrice);
            summary.put("totalCost", totalCost);
            summary.put("totalRevenue", totalRevenue);
            summary.put("profit", profit);
            summary.put("profitMargin", String.format("%.1f%%", profitMargin));
            return summary;
        }
    }

    /**
     * A completed trade mission.
     */
    public static class B5TradeMission {
        private final String id;
        private final String originSystemId;
        private final String destinationSystemId;
        private final B5Commodity commodity;
        private final int units;
        private final int profit;
        private final MissionOutcome outcome;
        private final String description;
        private final int tickCompleted;

        B5TradeMission(String id, String originSystemId, String destinationSystemId,
                       B5Commodity commodity, int units, int profit,
                       MissionOutcome outcome, String description, int tickCompleted) {
            this.id = id;
            this.originSystemId = originSystemId;
            this.destinationSystemId = destinationSystemId;
            this.commodity = commodity;
            this.units = units;
            this.profit = profit;
            this.outcome = outcome;
            this.description = description;
            this.tickCompleted = tickCompleted;
        }

        public String getId() {
            return id;
        }

        public String getOriginSystemId() {
            return originSystemId;
        }

        public String getDestinationSystemId() {
            return destinationSystemId;
        }

        public B5Commodity getCommodity() {
            return commodity;
        }

        public int getUnits() {
            return units;
        }

        public int getProfit() {
            return profit;
        }

        public MissionOutcome getOutcome() {
            return outcome;
        }

        public String getDescription() {
            return description;
        }

        public int getTickCompleted() {
            return tickCompleted;
        }

        /**
         * Returns a summary of this mission.
         *
         * @return a map with mission details
         */
        public Map<String, Object> toSummary() {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", id);
            summary.put("origin", originSystemId);
            summary.put("destination", destinationSystemId);
            summary.put("commodity", commodity.getName());
            summary.put("units", units);
            summary.put("profit", profit);
            summary.put("outcome", outcome);
            summary.put("description", description);
            summary.put("tick", tickCompleted);
            return summary;
        }
    }

    /**
     * Possible outcomes of a trade mission.
     */
    public enum MissionOutcome {
        SUCCESS("Success"),
        PARTIAL_SUCCESS("Partial Success"),
        PIRACY("Piracy Attack"),
        CUSTOMS_SEIZURE("Customs Seizure"),
        MARKET_CRASH("Market Crash"),
        FAILED("Failed");

        private final String description;

        MissionOutcome(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public B5TradeSimulator() {
        this.markets = new ConcurrentHashMap<>();
        this.missionHistory = new ArrayList<>();
        this.random = new Random();
        this.tick = 0;
    }

    // ---- Accessors ----

    /**
     * Returns the market conditions for a system.
     *
     * @param systemId the system ID
     * @return the market conditions, or null if not registered
     */
    public B5MarketConditions getMarket(String systemId) {
        return markets.get(systemId);
    }

    /**
     * Returns all markets (immutable).
     *
     * @return map of system IDs to market conditions
     */
    public Map<String, B5MarketConditions> getAllMarkets() {
        return Collections.unmodifiableMap(markets);
    }

    /**
     * Returns the mission history (immutable).
     *
     * @return list of completed missions
     */
    public List<B5TradeMission> getMissionHistory() {
        return Collections.unmodifiableList(missionHistory);
    }

    /**
     * Returns the current simulation tick.
     *
     * @return the tick number
     */
    public int getTick() {
        return tick;
    }

    // ---- Market Management ----

    /**
     * Registers a new system with market conditions.
     *
     * @param systemId the system ID
     * @param systemName the system name
     */
    public void registerSystem(String systemId, String systemName) {
        markets.put(systemId, new B5MarketConditions(systemId, systemName));
    }

    /**
     * Removes a system from the trade simulator.
     *
     * @param systemId the system ID
     */
    public void removeSystem(String systemId) {
        markets.remove(systemId);
    }

    /**
     * Returns the number of registered systems.
     *
     * @return the number of systems
     */
    public int getSystemCount() {
        return markets.size();
    }

    // ---- Route Finding ----

    /**
     * Finds the most profitable trade route in the sector.
     * <p>
     * Searches all pairs of systems to find the route with the highest
     * potential profit for the given cargo capacity.
     * </p>
     *
     * @param cargoTons available cargo capacity in tons
     * @return the best trade route, or null if no profitable route exists
     */
    public B5TradeRoute findBestTradeRoute(int cargoTons) {
        List<String> systemIds = new ArrayList<>(markets.keySet());
        B5TradeRoute bestRoute = null;
        int bestProfit = 0;

        for (int i = 0; i < systemIds.size(); i++) {
            for (int j = i + 1; j < systemIds.size(); j++) {
                String originId = systemIds.get(i);
                String destId = systemIds.get(j);

                // Check origin -> destination
                B5TradeRoute route = findBestRouteBetween(originId, destId, cargoTons);
                if (route != null && route.getProfit() > bestProfit) {
                    bestProfit = route.getProfit();
                    bestRoute = route;
                }

                // Check destination -> origin
                route = findBestRouteBetween(destId, originId, cargoTons);
                if (route != null && route.getProfit() > bestProfit) {
                    bestProfit = route.getProfit();
                    bestRoute = route;
                }
            }
        }

        return bestRoute;
    }

    /**
     * Finds the best trade route between two specific systems.
     *
     * @param originId the origin system ID
     * @param destinationId the destination system ID
     * @param cargoTons available cargo capacity in tons
     * @return the best trade route, or null if no profitable route exists
     */
    public B5TradeRoute findBestRouteBetween(String originId, String destinationId, int cargoTons) {
        B5MarketConditions originMarket = markets.get(originId);
        B5MarketConditions destMarket = markets.get(destinationId);

        if (originMarket == null || destMarket == null) {
            return null;
        }

        B5TradeRoute bestRoute = null;
        int bestProfit = 0;

        for (B5Commodity commodity : B5Commodity.values()) {
            B5MarketConditions.MarketEntry originEntry = originMarket.getMarketEntry(commodity);
            B5MarketConditions.MarketEntry destEntry = destMarket.getMarketEntry(commodity);

            if (originEntry == null || destEntry == null) {
                continue;
            }

            if (!originEntry.isAvailable() || !destEntry.isAvailable()) {
                continue;
            }

            // Calculate max units based on cargo capacity
            int maxUnits = commodity.maxUnitsForCargo(cargoTons);
            if (maxUnits <= 0) {
                continue;
            }

            int buyPrice = originEntry.getBuyPrice();
            int sellPrice = destEntry.getSellPrice();
            int profit = (sellPrice - buyPrice) * maxUnits;

            if (profit > bestProfit) {
                bestProfit = profit;
                bestRoute = new B5TradeRoute(originId, destinationId, commodity, maxUnits, buyPrice, sellPrice);
            }
        }

        return bestRoute;
    }

    /**
     * Finds all profitable trade routes for a given cargo capacity.
     *
     * @param cargoTons available cargo capacity in tons
     * @return list of profitable routes sorted by profit
     */
    public List<B5TradeRoute> findAllProfitableRoutes(int cargoTons) {
        List<B5TradeRoute> routes = new ArrayList<>();
        List<String> systemIds = new ArrayList<>(markets.keySet());

        for (int i = 0; i < systemIds.size(); i++) {
            for (int j = i + 1; j < systemIds.size(); j++) {
                String originId = systemIds.get(i);
                String destId = systemIds.get(j);

                B5TradeRoute route = findBestRouteBetween(originId, destId, cargoTons);
                if (route != null && route.getProfit() > 0) {
                    routes.add(route);
                }

                route = findBestRouteBetween(destId, originId, cargoTons);
                if (route != null && route.getProfit() > 0) {
                    routes.add(route);
                }
            }
        }

        routes.sort(Comparator.comparingInt(B5TradeRoute::getProfit).reversed());
        return routes;
    }

    // ---- Trade Missions ----

    /**
     * Executes a trade mission.
     * <p>
     * The mission may succeed, fail, or encounter events such as piracy
     * or customs seizures. The outcome affects the profit and mission history.
     * </p>
     *
     * @param route the trade route to execute
     * @return the completed trade mission
     */
    public B5TradeMission executeTradeMission(B5TradeRoute route) {
        String id = "TM-" + (missionHistory.size() + 1);
        MissionOutcome outcome = determineMissionOutcome(route);
        int adjustedProfit = adjustProfit(route.getProfit(), outcome);

        String description = generateMissionDescription(route, outcome);

        B5TradeMission mission = new B5TradeMission(
                id,
                route.getOriginSystemId(),
                route.getDestinationSystemId(),
                route.getCommodity(),
                route.getUnits(),
                adjustedProfit,
                outcome,
                description,
                tick
        );

        missionHistory.add(mission);

        // Simulate trade effects on markets
        B5MarketConditions originMarket = markets.get(route.getOriginSystemId());
        B5MarketConditions destMarket = markets.get(route.getDestinationSystemId());

        if (outcome == MissionOutcome.SUCCESS || outcome == MissionOutcome.PARTIAL_SUCCESS) {
            if (originMarket != null) {
                originMarket.simulateTrade(route.getCommodity(), route.getUnits(), true);
            }
            if (destMarket != null) {
                destMarket.simulateTrade(route.getCommodity(), route.getUnits(), false);
            }
        }

        return mission;
    }

    /**
     * Determines the outcome of a trade mission based on risk factors.
     */
    private MissionOutcome determineMissionOutcome(B5TradeRoute route) {
        int risk = 0;

        // Illegal commodities have higher risk
        if (route.getCommodity().getRarity() == B5Commodity.Rarity.ILLEGAL) {
            risk += 40;
        } else if (route.getCommodity().getRarity() == B5Commodity.Rarity.EXTREMELY_RARE) {
            risk += 20;
        }

        // High-value cargo attracts pirates
        if (route.getTotalRevenue() > 50000) {
            risk += 15;
        }

        int roll = random.nextInt(100);
        if (roll < risk / 3) {
            return MissionOutcome.PIRACY;
        } else if (roll < risk / 2 && route.getCommodity().getRarity() == B5Commodity.Rarity.ILLEGAL) {
            return MissionOutcome.CUSTOMS_SEIZURE;
        } else if (roll < risk) {
            return MissionOutcome.MARKET_CRASH;
        } else if (roll < 95) {
            return MissionOutcome.SUCCESS;
        } else {
            return MissionOutcome.PARTIAL_SUCCESS;
        }
    }

    /**
     * Adjusts profit based on mission outcome.
     */
    private int adjustProfit(int baseProfit, MissionOutcome outcome) {
        return switch (outcome) {
            case SUCCESS -> baseProfit;
            case PARTIAL_SUCCESS -> (int) (baseProfit * 0.5);
            case PIRACY -> (int) (baseProfit * -0.3); // Lose 30% of profit
            case CUSTOMS_SEIZURE -> -baseProfit; // Lose everything
            case MARKET_CRASH -> (int) (baseProfit * 0.2);
            case FAILED -> -baseProfit;
        };
    }

    /**
     * Generates a description for a trade mission.
     */
    private String generateMissionDescription(B5TradeRoute route, MissionOutcome outcome) {
        String commodity = route.getCommodity().getName();
        String origin = route.getOriginSystemId();
        String destination = route.getDestinationSystemId();

        return switch (outcome) {
            case SUCCESS ->
                    "Successfully traded " + route.getUnits() + " units of " + commodity + " from " + origin + " to " + destination + ".";
            case PARTIAL_SUCCESS ->
                    "Partial success: only half the cargo was sold at " + destination + ".";
            case PIRACY ->
                    "Pirates attacked while carrying " + commodity + " from " + origin + " to " + destination + "!";
            case CUSTOMS_SEIZURE ->
                    "Customs seized " + commodity + " at " + destination + ". Cargo was illegal!";
            case MARKET_CRASH ->
                    "Market crash at " + destination + " devalued " + commodity + ".";
            case FAILED ->
                    "Trade mission failed: " + commodity + " could not be delivered.";
        };
    }

    // ---- Simulation ----

    /**
     * Advances the simulation by one tick.
     * <p>
     * All markets fluctuate, and new trade opportunities may arise.
     * </p>
     */
    public void advanceTick() {
        tick++;
        for (B5MarketConditions market : markets.values()) {
            market.advanceTick();
        }
    }

    /**
     * Generates a summary of the entire trade simulation.
     *
     * @return a map with simulation statistics
     */
    public Map<String, Object> getSimulationSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("tick", tick);
        summary.put("systems", markets.size());
        summary.put("totalMissions", missionHistory.size());

        int totalProfit = missionHistory.stream().mapToInt(B5TradeMission::getProfit).sum();
        summary.put("totalProfit", totalProfit);

        long successCount = missionHistory.stream()
                .filter(m -> m.getOutcome() == MissionOutcome.SUCCESS)
                .count();
        summary.put("successfulMissions", successCount);

        long failureCount = missionHistory.stream()
                .filter(m -> m.getOutcome() != MissionOutcome.SUCCESS && m.getOutcome() != MissionOutcome.PARTIAL_SUCCESS)
                .count();
        summary.put("failedMissions", failureCount);

        return summary;
    }
}
