package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Trade simulator for SFB.
 * <p>
 * Manages trade routes between star systems, executes trade missions,
 * and calculates profits based on market conditions.
 * </p>
 */
public class SfbTradeSimulator {

    private static final Random RANDOM = new Random();

    private final Map<String, SfbMarketConditions> systems = new HashMap<>();
    private final List<TradeMission> missionHistory = new ArrayList<>();

    /**
     * Registers a star system with market conditions.
     */
    public void registerSystem(String systemId, String systemName) {
        systems.put(systemId, new SfbMarketConditions(systemId, systemName));
    }

    /**
     * Gets market conditions for a system.
     */
    public SfbMarketConditions getMarketConditions(String systemId) {
        return systems.get(systemId);
    }

    /**
     * Finds the best trade route between two systems.
     */
    public TradeRoute findBestRouteBetween(String originId, String destinationId, int cargoCapacity) {
        SfbMarketConditions origin = systems.get(originId);
        SfbMarketConditions destination = systems.get(destinationId);

        if (origin == null || destination == null) {
            return null;
        }

        double bestProfit = 0;
        SfbCommodity bestCommodity = null;
        int bestQuantity = 0;

        for (SfbCommodity commodity : SfbCommodity.values()) {
            SfbMarketConditions.MarketEntry originEntry = origin.getMarketEntry(commodity);
            SfbMarketConditions.MarketEntry destEntry = destination.getMarketEntry(commodity);

            if (originEntry == null || destEntry == null) {
                continue;
            }

            int buyPrice = originEntry.getBuyPrice();
            int sellPrice = destEntry.getSellPrice();
            if (buyPrice <= 0 || sellPrice <= 0) {
                continue;
            }

            int quantity = (int) Math.min(cargoCapacity / Math.max(0.1, commodity.getBulkTons()), 100);
            double profit = (sellPrice - buyPrice) * quantity;

            if (profit > bestProfit) {
                bestProfit = profit;
                bestCommodity = commodity;
                bestQuantity = quantity;
            }
        }

        if (bestCommodity == null) {
            return null;
        }

        return new TradeRoute(originId, destinationId, bestCommodity, bestQuantity, (int) bestProfit);
    }

    /**
     * Executes a trade mission.
     */
    public TradeMission executeTradeMission(TradeRoute route) {
        MissionOutcome outcome = rollMissionOutcome();
        int profit = (int) (route.getEstimatedProfit() * outcome.getProfitMultiplier());

        TradeMission mission = new TradeMission(route, outcome, profit);
        missionHistory.add(mission);
        return mission;
    }

    /**
     * Advances all markets by one tick.
     */
    public void advanceMarkets() {
        for (SfbMarketConditions market : systems.values()) {
            market.advanceTick();
        }
    }

    private MissionOutcome rollMissionOutcome() {
        int roll = RANDOM.nextInt(100);
        if (roll < 50) return MissionOutcome.SUCCESS;
        else if (roll < 70) return MissionOutcome.PARTIAL_SUCCESS;
        else if (roll < 80) return MissionOutcome.PIRACY;
        else if (roll < 90) return MissionOutcome.CUSTOMS_SEIZURE;
        else if (roll < 95) return MissionOutcome.MARKET_CRASH;
        else return MissionOutcome.FAILED;
    }

    public List<TradeMission> getMissionHistory() {
        return missionHistory;
    }

    /**
     * A trade route between two systems.
     */
    public static class TradeRoute {
        private final String originSystemId;
        private final String destinationSystemId;
        private final SfbCommodity commodity;
        private final int quantity;
        private final int estimatedProfit;

        public TradeRoute(String originSystemId, String destinationSystemId,
                          SfbCommodity commodity, int quantity, int estimatedProfit) {
            this.originSystemId = originSystemId;
            this.destinationSystemId = destinationSystemId;
            this.commodity = commodity;
            this.quantity = quantity;
            this.estimatedProfit = estimatedProfit;
        }

        public String getOriginSystemId() { return originSystemId; }
        public String getDestinationSystemId() { return destinationSystemId; }
        public SfbCommodity getCommodity() { return commodity; }
        public int getQuantity() { return quantity; }
        public int getEstimatedProfit() { return estimatedProfit; }
    }

    /**
     * Outcome of a trade mission.
     */
    public enum MissionOutcome {
        SUCCESS("Success", 1.0),
        PARTIAL_SUCCESS("Partial Success", 0.5),
        PIRACY("Piracy Attack", 0.3),
        CUSTOMS_SEIZURE("Customs Seizure", 0.0),
        MARKET_CRASH("Market Crash", 0.2),
        FAILED("Failed", 0.0);

        private final String description;
        private final double profitMultiplier;

        MissionOutcome(String description, double profitMultiplier) {
            this.description = description;
            this.profitMultiplier = profitMultiplier;
        }

        public String getDescription() { return description; }
        public double getProfitMultiplier() { return profitMultiplier; }
    }

    /**
     * A completed trade mission.
     */
    public static class TradeMission {
        private final TradeRoute route;
        private final MissionOutcome outcome;
        private final int profit;

        public TradeMission(TradeRoute route, MissionOutcome outcome, int profit) {
            this.route = route;
            this.outcome = outcome;
            this.profit = profit;
        }

        public TradeRoute getRoute() { return route; }
        public MissionOutcome getOutcome() { return outcome; }
        public int getProfit() { return profit; }
    }
}
