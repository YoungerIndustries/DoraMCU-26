package com.yisy.modules.doramcu.babylon5.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Market conditions for a B5 star system.
 * <p>
 * Each system has supply and demand levels for each commodity, which determine
 * the actual buy/sell prices. Prices fluctuate based on market conditions,
 * events, and trade activity.
 * </p>
 * <p>
 * Supply levels range from 1 (scarce) to 6 (abundant).
 * Demand levels range from 1 (none) to 6 (critical).
 * </p>
 * <p>
 * Price modifiers are calculated as:
 * </p>
 * <ul>
 *   <li>Buy Price = Base Price × (7 - Supply) / 4</li>
 *   <li>Sell Price = Base Price × Demand / 4</li>
 * </ul>
 * <p>
 * Higher supply means cheaper to buy. Higher demand means better sell price.
 * </p>
 */
public class B5MarketConditions {

    private final String systemId;
    private final String systemName;
    private final Map<B5Commodity, MarketEntry> marketEntries;
    private final Random random;
    private int tick;

    /**
     * Market entry for a single commodity in a system.
     */
    public static class MarketEntry {
        private final B5Commodity commodity;
        private int supply;  // 1-6, higher = more available
        private int demand;  // 1-6, higher = more wanted
        private int buyPrice;
        private int sellPrice;
        private boolean available;

        MarketEntry(B5Commodity commodity, int supply, int demand) {
            this.commodity = commodity;
            this.supply = supply;
            this.demand = demand;
            this.available = true;
            recalculatePrices();
        }

        public B5Commodity getCommodity() {
            return commodity;
        }

        public int getSupply() {
            return supply;
        }

        public void setSupply(int supply) {
            this.supply = Math.max(1, Math.min(6, supply));
            recalculatePrices();
        }

        public int getDemand() {
            return demand;
        }

        public void setDemand(int demand) {
            this.demand = Math.max(1, Math.min(6, demand));
            recalculatePrices();
        }

        public int getBuyPrice() {
            return buyPrice;
        }

        public int getSellPrice() {
            return sellPrice;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        /**
         * Recalculates buy and sell prices based on supply and demand.
         */
        private void recalculatePrices() {
            // Buy price: higher supply = cheaper
            this.buyPrice = (int) (commodity.getBasePrice() * (7.0 - supply) / 4.0);
            // Sell price: higher demand = better price
            this.sellPrice = (int) (commodity.getBasePrice() * demand / 4.0);
        }

        /**
         * Returns the profit margin for buying and selling this commodity.
         *
         * @return sell price - buy price
         */
        public int getProfitMargin() {
            return sellPrice - buyPrice;
        }
    }

    public B5MarketConditions(String systemId, String systemName) {
        this.systemId = systemId;
        this.systemName = systemName;
        this.marketEntries = new ConcurrentHashMap<>();
        this.random = new Random();
        this.tick = 0;
        initializeMarkets();
    }

    /**
     * Initializes market conditions for all commodities.
     * Supply and demand are randomized based on commodity rarity.
     */
    private void initializeMarkets() {
        for (B5Commodity commodity : B5Commodity.values()) {
            int supply = rollSupply(commodity);
            int demand = rollDemand(commodity);
            marketEntries.put(commodity, new MarketEntry(commodity, supply, demand));
        }
    }

    /**
     * Rolls supply level based on commodity rarity.
     * Common items have higher supply, rare items have lower supply.
     */
    private int rollSupply(B5Commodity commodity) {
        return switch (commodity.getRarity()) {
            case COMMON -> 4 + random.nextInt(3);  // 4-6
            case UNCOMMON -> 2 + random.nextInt(4); // 2-5
            case RARE -> 1 + random.nextInt(3);     // 1-3
            case EXTREMELY_RARE -> 1;               // 1 only
            case ILLEGAL -> random.nextInt(2);      // 0-1 (may not be available)
        };
    }

    /**
     * Rolls demand level based on commodity rarity.
     * Rare items have higher demand, common items have lower demand.
     */
    private int rollDemand(B5Commodity commodity) {
        return switch (commodity.getRarity()) {
            case COMMON -> 2 + random.nextInt(4);   // 2-5
            case UNCOMMON -> 3 + random.nextInt(4); // 3-6
            case RARE -> 4 + random.nextInt(3);     // 4-6
            case EXTREMELY_RARE -> 5 + random.nextInt(2); // 5-6
            case ILLEGAL -> 1 + random.nextInt(3);  // 1-3 (risky demand)
        };
    }

    // ---- Accessors ----

    /**
     * Returns the system ID.
     *
     * @return the system ID
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Returns the system name.
     *
     * @return the system name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Returns the current tick.
     *
     * @return the tick number
     */
    public int getTick() {
        return tick;
    }

    /**
     * Returns the market entry for a commodity.
     *
     * @param commodity the commodity
     * @return the market entry, or null if not available
     */
    public MarketEntry getMarketEntry(B5Commodity commodity) {
        return marketEntries.get(commodity);
    }

    /**
     * Returns all market entries (immutable).
     *
     * @return map of commodities to market entries
     */
    public Map<B5Commodity, MarketEntry> getAllMarketEntries() {
        return Collections.unmodifiableMap(marketEntries);
    }

    /**
     * Returns available commodities sorted by profit margin (highest first).
     *
     * @return list of profitable market entries
     */
    public List<MarketEntry> getProfitableCommodities() {
        return marketEntries.values().stream()
                .filter(MarketEntry::isAvailable)
                .filter(e -> e.getProfitMargin() > 0)
                .sorted(Comparator.comparingInt(MarketEntry::getProfitMargin).reversed())
                .toList();
    }

    /**
     * Returns the best commodity to buy in this system (lowest buy price relative to base).
     *
     * @return the best buy commodity, or null if none available
     */
    public MarketEntry getBestBuyCommodity() {
        return marketEntries.values().stream()
                .filter(MarketEntry::isAvailable)
                .filter(e -> e.getSupply() >= 4)
                .min(Comparator.comparingInt(MarketEntry::getBuyPrice))
                .orElse(null);
    }

    /**
     * Returns the best commodity to sell in this system (highest sell price relative to base).
     *
     * @return the best sell commodity, or null if none available
     */
    public MarketEntry getBestSellCommodity() {
        return marketEntries.values().stream()
                .filter(MarketEntry::isAvailable)
                .filter(e -> e.getDemand() >= 4)
                .max(Comparator.comparingInt(MarketEntry::getSellPrice))
                .orElse(null);
    }

    // ---- Market Simulation ----

    /**
     * Advances the market by one tick, fluctuating supply and demand.
     * <p>
     * Each tick, supply and demand can change by -1, 0, or +1.
     * Rare events can cause larger fluctuations.
     * </p>
     */
    public void advanceTick() {
        tick++;

        for (MarketEntry entry : marketEntries.values()) {
            // Normal fluctuation: -1, 0, or +1
            int supplyChange = random.nextInt(3) - 1;
            int demandChange = random.nextInt(3) - 1;

            // Rare event: 10% chance of larger fluctuation
            if (random.nextInt(100) < 10) {
                supplyChange = random.nextInt(5) - 2;
                demandChange = random.nextInt(5) - 2;
            }

            entry.setSupply(entry.getSupply() + supplyChange);
            entry.setDemand(entry.getDemand() + demandChange);

            // Illegal commodities may become unavailable
            if (entry.getCommodity().getRarity() == B5Commodity.Rarity.ILLEGAL) {
                entry.setAvailable(random.nextInt(100) < 70); // 70% chance available
            }
        }
    }

    /**
     * Simulates a trade event that affects market conditions.
     *
     * @param commodity the commodity traded
     * @param units the number of units traded
     * @param isBuy true if buying, false if selling
     */
    public void simulateTrade(B5Commodity commodity, int units, boolean isBuy) {
        MarketEntry entry = marketEntries.get(commodity);
        if (entry == null) {
            return;
        }

        if (isBuy) {
            // Buying reduces supply, increases demand
            entry.setSupply(Math.max(1, entry.getSupply() - 1));
            entry.setDemand(Math.min(6, entry.getDemand() + 1));
        } else {
            // Selling increases supply, reduces demand
            entry.setSupply(Math.min(6, entry.getSupply() + 1));
            entry.setDemand(Math.max(1, entry.getDemand() - 1));
        }
    }

    /**
     * Generates a summary of market conditions for this system.
     *
     * @return a map with system info and top commodities
     */
    public Map<String, Object> getMarketSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("systemId", systemId);
        summary.put("systemName", systemName);
        summary.put("tick", tick);

        List<MarketEntry> profitable = getProfitableCommodities();
        summary.put("profitableCommodities", profitable.size());

        if (!profitable.isEmpty()) {
            summary.put("bestProfit", profitable.get(0).getProfitMargin());
            summary.put("bestCommodity", profitable.get(0).getCommodity().getName());
        }

        summary.put("totalCommodities", marketEntries.size());
        summary.put("availableCommodities",
                (int) marketEntries.values().stream().filter(MarketEntry::isAvailable).count());

        return summary;
    }
}
