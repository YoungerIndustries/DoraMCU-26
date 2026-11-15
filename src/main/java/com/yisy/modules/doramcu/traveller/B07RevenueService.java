package com.yisy.modules.doramcu.traveller;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Revenue computation service from Classic Traveller B07: Merchant Prince.
 * <p>
 * Calculates the annual trade revenue for a world based on its trade codes,
 * population, price level, and market conditions. Revenue represents the total
 * value of goods traded through a world's starport in one standard year.
 * </p>
 * <p>
 * The B07 revenue formula considers:
 * </p>
 * <ul>
 *   <li>Base revenue from population and price level</li>
 *   <li>Trade classification multiplier</li>
 *   <li>Market size and port wealth</li>
 *   <li>Trade code modifiers (surplus/shortage effects)</li>
 *   <li>Government and law level effects</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Supplement 7: Traders & Gunboats (1986).
 * </p>
 */

@Service
public class B07RevenueService {

    /**
     * Base revenue per million population in credits.
     */
    private static final long BASE_REVENUE_PER_MILLION = 1_000_000;

    /**
     * Minimum annual revenue for any inhabited world.
     */
    private static final long MINIMUM_REVENUE = 100_000;

    /**
     * Trade code revenue modifiers.
     * Positive values increase revenue, negative values decrease it.
     */
    private static final Map<TradeCode, Double> TRADE_CODE_REVENUE_MODIFIER;
    static {
        Map<TradeCode, Double> map = new EnumMap<>(TradeCode.class);
        map.put(TradeCode.AG, 1.3);
        map.put(TradeCode.CE, 0.8);
        map.put(TradeCode.CO, 0.7);
        map.put(TradeCode.EC, 2.5);
        map.put(TradeCode.FL, 0.6);
        map.put(TradeCode.HI, 1.8);
        map.put(TradeCode.IC, 0.7);
        map.put(TradeCode.LO, 0.5);
        map.put(TradeCode.NC, 1.0);
        map.put(TradeCode.NI, 0.1);
        map.put(TradeCode.PO, 0.4);
        map.put(TradeCode.SE, 0.7);
        map.put(TradeCode.VA, 0.5);
        map.put(TradeCode.WE, 0.6);
        map.put(TradeCode.AN, 1.2);
        map.put(TradeCode.CX, 1.1);
        map.put(TradeCode.LW, 1.1);
        map.put(TradeCode.MO, 1.2);
        map.put(TradeCode.RF, 0.8);
        map.put(TradeCode.RO, 0.9);
        map.put(TradeCode.UR, 2.0);
        TRADE_CODE_REVENUE_MODIFIER = Map.copyOf(map);
    }

    /**
     * Government type revenue modifiers.
     * Higher law levels generally increase revenue through stability.
     */
    private static final Map<String, Double> GOVERNMENT_REVENUE_MODIFIER;
    static {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("None", 0.3);
        map.put("Anarchy", 0.5);
        map.put("Dictatorship", 0.8);
        map.put("Democracy", 1.2);
        map.put("Feudal", 0.9);
        map.put("Theocracy", 0.8);
        map.put("Corporate", 1.5);
        map.put("Confederacy", 1.3);
        map.put("Imperial", 1.4);
        map.put("Feudal Confucian", 1.0);
        map.put("Warlord", 0.4);
        map.put("Criminal", 0.6);
        GOVERNMENT_REVENUE_MODIFIER = Map.copyOf(map);
    }

    /**
     * Computes the annual trade revenue for a world.
     * <p>
     * The revenue formula is:
     * </p>
     * <pre>
     * baseRevenue = populationMillions * BASE_REVENUE_PER_MILLION
     * priceLevelMultiplier = priceLevel.getMultiplier()
     * tradeMultiplier = product of trade code modifiers
     * governmentMultiplier = government type modifier
     * portMultiplier = (portWealth + 1) / 2.0
     * revenue = baseRevenue * priceLevelMultiplier * tradeMultiplier * governmentMultiplier * portMultiplier
     * </pre>
     *
     * @param world the world to compute revenue for
     * @return the annual revenue in credits
     */
    public long computeRevenue(TravellerWorld world) {
        if (world == null) {
            return 0;
        }

        long populationMillions = world.getPopulationMillions();
        if (populationMillions <= 0) {
            return MINIMUM_REVENUE;
        }

        // Base revenue from population
        long baseRevenue = populationMillions * BASE_REVENUE_PER_MILLION;

        // Price level multiplier
        PriceLevel priceLevel = PriceLevel.fromWorld(world);
        double priceMultiplier = priceLevel.getPriceMultiplier();

        // Trade code multiplier (product of all trade code modifiers)
        double tradeMultiplier = computeTradeMultiplier(world);

        // Government modifier
        double governmentMultiplier = computeGovernmentMultiplier(world);

        // Port wealth multiplier
        double portMultiplier = (world.getPortWealth() + 1) / 2.0;

        // Law level modifier (higher law = more stable trade)
        double lawMultiplier = computeLawMultiplier(world);

        // Final revenue calculation
        long revenue = (long) (baseRevenue * priceMultiplier * tradeMultiplier *
                governmentMultiplier * portMultiplier * lawMultiplier);

        return Math.max(MINIMUM_REVENUE, revenue);
    }

    /**
     * Computes the trade classification multiplier for a world.
     * <p>
     * Uses the product of all trade code modifiers, capped at 3.0x.
     * </p>
     *
     * @param world the world
     * @return the trade multiplier (0.1 to 3.0)
     */
    public double computeTradeMultiplier(TravellerWorld world) {
        Set<TradeCode> tradeCodes = world.getTradeCodes();
        if (tradeCodes == null || tradeCodes.isEmpty()) {
            return 0.1;
        }

        double multiplier = 1.0;
        for (TradeCode code : tradeCodes) {
            Double modifier = TRADE_CODE_REVENUE_MODIFIER.get(code);
            if (modifier != null) {
                multiplier *= modifier;
            }
        }

        // Cap at 3.0x to prevent extreme values
        return Math.min(3.0, Math.max(0.1, multiplier));
    }

    /**
     * Computes the government type revenue modifier.
     *
     * @param world the world
     * @return the government modifier (0.3 to 1.5)
     */
    public double computeGovernmentMultiplier(TravellerWorld world) {
        String government = world.getGovernment();
        if (government == null || government.isEmpty()) {
            return 1.0;
        }

        Double modifier = GOVERNMENT_REVENUE_MODIFIER.get(government);
        if (modifier != null) {
            return modifier;
        }

        // Default modifier for unknown government types
        return 1.0;
    }

    /**
     * Computes the law level revenue modifier.
     * <p>
     * Higher law levels provide more stable trade environments.
     * Law 0 (no law) has 0.5x modifier, Law A (total control) has 1.5x.
     * </p>
     *
     * @param world the world
     * @return the law modifier (0.5 to 1.5)
     */
    public double computeLawMultiplier(TravellerWorld world) {
        String lawLevel = world.getLawLevel();
        if (lawLevel == null || lawLevel.isEmpty()) {
            return 1.0;
        }

        // Parse law level: 0-9 or A
        try {
            int lawValue;
            if ("A".equalsIgnoreCase(lawLevel)) {
                lawValue = 10;
            } else {
                lawValue = Integer.parseInt(lawLevel);
            }
            // Scale from 0.5 (law 0) to 1.5 (law A)
            return 0.5 + (lawValue / 10.0) * 1.0;
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    /**
     * Computes a detailed revenue breakdown for a world.
     *
     * @param world the world
     * @return a revenue breakdown with all components
     */
    public RevenueBreakdown computeRevenueBreakdown(TravellerWorld world) {
        long populationMillions = world.getPopulationMillions();
        long baseRevenue = populationMillions * BASE_REVENUE_PER_MILLION;

        PriceLevel priceLevel = PriceLevel.fromWorld(world);
        double priceMultiplier = priceLevel.getPriceMultiplier();
        double tradeMultiplier = computeTradeMultiplier(world);
        double governmentMultiplier = computeGovernmentMultiplier(world);
        double portMultiplier = (world.getPortWealth() + 1) / 2.0;
        double lawMultiplier = computeLawMultiplier(world);

        long revenue = (long) (baseRevenue * priceMultiplier * tradeMultiplier *
                governmentMultiplier * portMultiplier * lawMultiplier);
        revenue = Math.max(MINIMUM_REVENUE, revenue);

        TradeClassification classification = TradeClassification.classify(world);

        return new RevenueBreakdown(
                world.getName(),
                baseRevenue,
                priceMultiplier,
                tradeMultiplier,
                governmentMultiplier,
                portMultiplier,
                lawMultiplier,
                revenue,
                classification
        );
    }

    /**
     * Computes the revenue for all worlds in a sector.
     *
     * @param sector the sector
     * @return map of world name to revenue
     */
    public Map<String, Long> computeSectorRevenue(TravellerSector sector) {
        Map<String, Long> revenues = new LinkedHashMap<>();
        for (TravellerWorld world : sector.getAllWorlds()) {
            revenues.put(world.getName(), computeRevenue(world));
        }
        return Collections.unmodifiableMap(revenues);
    }

    /**
     * Finds the top N revenue-generating worlds in a sector.
     *
     * @param sector the sector
     * @param n the number of top worlds to return
     * @return list of revenue breakdowns, sorted by revenue descending
     */
    public List<RevenueBreakdown> topRevenueWorlds(TravellerSector sector, int n) {
        List<RevenueBreakdown> breakdowns = new ArrayList<>();
        for (TravellerWorld world : sector.getAllWorlds()) {
            breakdowns.add(computeRevenueBreakdown(world));
        }
        breakdowns.sort(Comparator.comparingLong(RevenueBreakdown::getRevenue).reversed());
        return breakdowns.subList(0, Math.min(n, breakdowns.size()));
    }

    /**
     * Computes the total sector revenue.
     *
     * @param sector the sector
     * @return total revenue in credits
     */
    public long computeTotalSectorRevenue(TravellerSector sector) {
        return sector.getAllWorlds().stream()
                .mapToLong(this::computeRevenue)
                .sum();
    }

    /**
     * Computes the average world revenue in a sector.
     *
     * @param sector the sector
     * @return average revenue in credits
     */
    public double computeAverageWorldRevenue(TravellerSector sector) {
        List<TravellerWorld> worlds = sector.getAllWorlds();
        if (worlds.isEmpty()) return 0.0;
        long total = computeTotalSectorRevenue(sector);
        return (double) total / worlds.size();
    }

    /**
     * Detailed revenue breakdown for a single world.
     */
    public static class RevenueBreakdown {
        private final String worldName;
        private final long baseRevenue;
        private final double priceMultiplier;
        private final double tradeMultiplier;
        private final double governmentMultiplier;
        private final double portMultiplier;
        private final double lawMultiplier;
        private final long revenue;
        private final TradeClassification classification;

        public RevenueBreakdown(String worldName, long baseRevenue,
                                 double priceMultiplier, double tradeMultiplier,
                                 double governmentMultiplier, double portMultiplier,
                                 double lawMultiplier, long revenue,
                                 TradeClassification classification) {
            this.worldName = worldName;
            this.baseRevenue = baseRevenue;
            this.priceMultiplier = priceMultiplier;
            this.tradeMultiplier = tradeMultiplier;
            this.governmentMultiplier = governmentMultiplier;
            this.portMultiplier = portMultiplier;
            this.lawMultiplier = lawMultiplier;
            this.revenue = revenue;
            this.classification = classification;
        }

        public String getWorldName() {
            return worldName;
        }

        public long getBaseRevenue() {
            return baseRevenue;
        }

        public double getPriceMultiplier() {
            return priceMultiplier;
        }

        public double getTradeMultiplier() {
            return tradeMultiplier;
        }

        public double getGovernmentMultiplier() {
            return governmentMultiplier;
        }

        public double getPortMultiplier() {
            return portMultiplier;
        }

        public double getLawMultiplier() {
            return lawMultiplier;
        }

        public long getRevenue() {
            return revenue;
        }

        public TradeClassification getClassification() {
            return classification;
        }

        /**
         * Returns a formatted summary of this revenue breakdown.
         */
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append(worldName).append("\n");
            sb.append("  Classification: ").append(classification.getCodeString())
              .append(" (").append(classification.getDescription()).append(")\n");
            sb.append("  Base Revenue: ").append(String.format("%,d", baseRevenue)).append(" cr\n");
            sb.append("  Multipliers:\n");
            sb.append("    Price Level: ").append(String.format("%.2f", priceMultiplier)).append("\n");
            sb.append("    Trade Codes: ").append(String.format("%.2f", tradeMultiplier)).append("\n");
            sb.append("    Government: ").append(String.format("%.2f", governmentMultiplier)).append("\n");
            sb.append("    Port Wealth: ").append(String.format("%.2f", portMultiplier)).append("\n");
            sb.append("    Law Level: ").append(String.format("%.2f", lawMultiplier)).append("\n");
            sb.append("  Annual Revenue: ").append(String.format("%,d", revenue)).append(" cr\n");
            return sb.toString();
        }

        @Override
        public String toString() {
            return worldName + ": " + String.format("%,d", revenue) + " cr";
        }
    }
}
