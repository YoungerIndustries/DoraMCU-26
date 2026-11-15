package com.yisy.modules.doramcu.traveller;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Types of economic events that can affect trade in a Traveller sector.
 * <p>
 * Economic events are temporary conditions that modify market conditions,
 * commodity prices, and trade route profitability. They simulate the
 * dynamic nature of interstellar commerce in Classic Traveller.
 * </p>
 * <p>
 * Each event type has:
 * </p>
 * <ul>
 *   <li>A severity range (1-10) determining impact magnitude</li>
 *   <li>A duration range in ticks</li>
 *   <li>A price modifier function affecting buy/sell prices</li>
 *   <li>A condition modifier function affecting market conditions</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Supplement 7: Traders & Gunboats (1986).
 * </p>
 */
public enum EconomicEventType {

    /**
     * Pirate activity increases along trade routes, raising insurance costs
     * and reducing trade volume. Affects all commodities except Weapons.
     */
    PIRACY("Piracy", 3, 8, 1, 6,
            (commodity, severity) -> commodity == Commodity.WEAPONS ? 1.2 : (1.0 + severity * 0.05),
            (commodity, severity) -> commodity == Commodity.WEAPONS ? MarketConditions.Condition.SURPLUS :
                    (severity > 6 ? MarketConditions.Condition.SHORT : MarketConditions.Condition.NORMAL)),

    /**
     * Trade embargo restricts certain commodities between worlds or sectors.
     * Embargoed commodities become unavailable or extremely expensive.
     */
    EMBARGO("Embargo", 4, 9, 2, 10,
            (commodity, severity) -> 2.0 + severity * 0.1,
            (commodity, severity) -> severity > 5 ? MarketConditions.Condition.NONE : MarketConditions.Condition.SHORT),

    /**
     * Economic boom increases demand and prices for all commodities.
     * Trade routes become more profitable but competition increases.
     */
    BOOM("Economic Boom", 2, 7, 1, 5,
            (commodity, severity) -> 1.0 + severity * 0.1,
            (commodity, severity) -> MarketConditions.Condition.SHORT),

    /**
     * Economic bust decreases demand and prices for all commodities.
     * Trade routes become less profitable but supply is abundant.
     */
    BUST("Economic Bust", 3, 8, 1, 5,
            (commodity, severity) -> 1.0 - severity * 0.05,
            (commodity, severity) -> MarketConditions.Condition.SURPLUS),

    /**
     * Local war disrupts trade routes and increases demand for military
     * supplies while reducing civilian trade.
     */
    WAR("Local War", 5, 10, 3, 8,
            (commodity, severity) -> commodity == Commodity.WEAPONS || commodity == Commodity.MEDICINE ?
                    1.5 + severity * 0.1 : (1.0 + severity * 0.03),
            (commodity, severity) -> commodity == Commodity.WEAPONS ? MarketConditions.Condition.SURPLUS :
                    (severity > 7 ? MarketConditions.Condition.NONE : MarketConditions.Condition.NORMAL)),

    /**
     * Plague or disease outbreak reduces population and trade activity.
     * Medicine becomes extremely valuable; other trade suffers.
     */
    PLAGUE("Plague", 4, 9, 2, 6,
            (commodity, severity) -> commodity == Commodity.MEDICINE ? 2.0 + severity * 0.15 :
                    (1.0 - severity * 0.05),
            (commodity, severity) -> commodity == Commodity.MEDICINE ? MarketConditions.Condition.SURPLUS :
                    (severity > 6 ? MarketConditions.Condition.NONE : MarketConditions.Condition.SHORT)),

    /**
     * Bumper harvest increases food supply and lowers food prices.
     * Other commodities are unaffected.
     */
    HARVEST("Bumper Harvest", 1, 5, 1, 3,
            (commodity, severity) -> commodity == Commodity.FOOD ? (1.0 - severity * 0.1) : 1.0,
            (commodity, severity) -> commodity == Commodity.FOOD ? MarketConditions.Condition.SURPLUS : null),

    /**
     * Political instability causes unpredictable market fluctuations.
     * All commodities may shift between surplus and shortage.
     */
    INSTABILITY("Political Instability", 3, 8, 2, 7,
            (commodity, severity) -> 1.0 + (severity % 5) * 0.1 - severity * 0.02,
            (commodity, severity) -> {
                int mod = severity % 3;
                return mod == 0 ? MarketConditions.Condition.SURPLUS :
                        (mod == 1 ? MarketConditions.Condition.SHORT : MarketConditions.Condition.NORMAL);
            }),

    /**
     * Technological breakthrough increases production efficiency.
     * Electronics and luxury goods become more available.
     */
    TECHNOLOGY("Technological Breakthrough", 2, 6, 1, 4,
            (commodity, severity) -> (commodity == Commodity.ELECTRONICS || commodity == Commodity.LUXURY) ?
                    (1.0 - severity * 0.08) : 1.0,
            (commodity, severity) -> (commodity == Commodity.ELECTRONICS || commodity == Commodity.LUXURY) ?
                    MarketConditions.Condition.SURPLUS : null),

    /**
     * Natural disaster (asteroid impact, solar flare, etc.) disrupts
     * local trade and increases demand for reconstruction materials.
     */
    DISASTER("Natural Disaster", 5, 10, 2, 5,
            (commodity, severity) -> (commodity == Commodity.FUEL || commodity == Commodity.METALS) ?
                    1.5 + severity * 0.1 : (1.0 + severity * 0.03),
            (commodity, severity) -> (commodity == Commodity.FUEL || commodity == Commodity.METALS) ?
                    MarketConditions.Condition.SURPLUS : MarketConditions.Condition.SHORT);

    private final String displayName;
    private final int minSeverity;
    private final int maxSeverity;
    private final int minDuration;
    private final int maxDuration;
    private final BiFunction<Commodity, Integer, Double> priceModifier;
    private final BiFunction<Commodity, Integer, MarketConditions.Condition> conditionModifier;

    EconomicEventType(String displayName, int minSeverity, int maxSeverity,
                       int minDuration, int maxDuration,
                       BiFunction<Commodity, Integer, Double> priceModifier,
                       BiFunction<Commodity, Integer, MarketConditions.Condition> conditionModifier) {
        this.displayName = displayName;
        this.minSeverity = minSeverity;
        this.maxSeverity = maxSeverity;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.priceModifier = priceModifier;
        this.conditionModifier = conditionModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinSeverity() {
        return minSeverity;
    }

    public int getMaxSeverity() {
        return maxSeverity;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     * Calculates the price modifier for a commodity given this event's severity.
     *
     * @param commodity the commodity
     * @param severity the event severity (1-10)
     * @return the price multiplier (1.0 = no change)
     */
    public double getPriceModifier(Commodity commodity, int severity) {
        return priceModifier.apply(commodity, severity);
    }

    /**
     * Determines the market condition override for a commodity given this event's severity.
     * Returns null if the event does not override the condition for this commodity.
     *
     * @param commodity the commodity
     * @param severity the event severity (1-10)
     * @return the market condition override, or null if no override
     */
    public MarketConditions.Condition getConditionOverride(Commodity commodity, int severity) {
        return conditionModifier.apply(commodity, severity);
    }

    /**
     * Returns a description of this event type's effects.
     *
     * @return a human-readable description
     */
    public String getDescription() {
        return switch (this) {
            case PIRACY -> "Pirate activity increases insurance costs and reduces trade volume.";
            case EMBARGO -> "Trade restrictions make certain commodities unavailable or expensive.";
            case BOOM -> "Economic boom increases demand and prices for all commodities.";
            case BUST -> "Economic bust decreases demand and prices; supply is abundant.";
            case WAR -> "Local war disrupts trade routes and increases military supply demand.";
            case PLAGUE -> "Disease outbreak reduces population; medicine becomes extremely valuable.";
            case HARVEST -> "Bumper harvest increases food supply and lowers food prices.";
            case INSTABILITY -> "Political instability causes unpredictable market fluctuations.";
            case TECHNOLOGY -> "Technological breakthrough increases production of electronics and luxuries.";
            case DISASTER -> "Natural disaster disrupts trade; reconstruction materials in high demand.";
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
