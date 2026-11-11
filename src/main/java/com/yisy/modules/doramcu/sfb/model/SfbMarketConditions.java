package com.yisy.modules.doramcu.sfb.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Market conditions for SFB trade simulation.
 * <p>
 * Tracks supply and demand for each commodity in a star system,
 * with dynamic price calculation and market fluctuation over time.
 * </p>
 */
public class SfbMarketConditions {

    private static final Random RANDOM = new Random();

    private final String systemId;
    private final String systemName;
    private final Map<SfbCommodity, MarketEntry> marketEntries = new HashMap<>();

    public SfbMarketConditions(String systemId, String systemName) {
        this.systemId = systemId;
        this.systemName = systemName;
        initializeMarkets();
    }

    private void initializeMarkets() {
        for (SfbCommodity commodity : SfbCommodity.values()) {
            int supply = RANDOM.nextInt(6) + 1;
            int demand = RANDOM.nextInt(6) + 1;
            marketEntries.put(commodity, new MarketEntry(commodity, supply, demand));
        }
    }

    /**
     * Gets the market entry for a commodity.
     */
    public MarketEntry getMarketEntry(SfbCommodity commodity) {
        return marketEntries.get(commodity);
    }

    /**
     * Advances the market by one tick, causing prices to fluctuate.
     */
    public void advanceTick() {
        for (MarketEntry entry : marketEntries.values()) {
            entry.fluctuate();
        }
    }

    /**
     * Returns a summary of market conditions.
     */
    public String getMarketSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Market conditions for ").append(systemName).append(":\n");
        for (MarketEntry entry : marketEntries.values()) {
            sb.append("  ").append(entry.getCommodity().getName())
              .append(": Buy=").append(entry.getBuyPrice())
              .append(", Sell=").append(entry.getSellPrice())
              .append("\n");
        }
        return sb.toString();
    }

    public String getSystemId() {
        return systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    /**
     * A market entry for a single commodity.
     */
    public static class MarketEntry {
        private final SfbCommodity commodity;
        private int supply;
        private int demand;

        public MarketEntry(SfbCommodity commodity, int supply, int demand) {
            this.commodity = commodity;
            this.supply = supply;
            this.demand = demand;
        }

        public SfbCommodity getCommodity() {
            return commodity;
        }

        public int getSupply() {
            return supply;
        }

        public int getDemand() {
            return demand;
        }

        /**
         * Calculates the buy price based on supply and demand.
         */
        public int getBuyPrice() {
            double multiplier = demand / (double) Math.max(1, supply);
            return (int) (commodity.getBasePrice() * multiplier);
        }

        /**
         * Calculates the sell price (slightly lower than buy price).
         */
        public int getSellPrice() {
            return (int) (getBuyPrice() * 0.8);
        }

        /**
         * Fluctuates supply and demand.
         */
        public void fluctuate() {
            if (RANDOM.nextInt(100) < 30) {
                supply = Math.max(1, Math.min(6, supply + RANDOM.nextInt(3) - 1));
            }
            if (RANDOM.nextInt(100) < 30) {
                demand = Math.max(1, Math.min(6, demand + RANDOM.nextInt(3) - 1));
            }
        }
    }
}
