package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Trade classification system from Classic Traveller B07: Merchant Prince.
 * <p>
 * Combines a world's trade codes into a meaningful trade type classification
 * that determines the world's role in interstellar commerce. Each classification
 * has a primary and secondary trade code, describing the dominant economic
 * characteristics of the world.
 * </p>
 * <p>
 * Trade classifications affect:
 * </p>
 * <ul>
 *   <li>Commodity availability and pricing modifiers</li>
 *   <li>Trade route profitability estimates</li>
 *   <li>World revenue computation</li>
 *   <li>Market size adjustments</li>
 * </ul>
 * <p>
 * Derived from Classic Traveller Supplement 7: Traders & Gunboats (1986).
 * </p>
 */
public class TradeClassification {

    /**
     * The primary trade code for this classification.
     */
    private final TradeCode primary;

    /**
     * The secondary trade code, or null if single-code classification.
     */
    private final TradeCode secondary;

    /**
     * Human-readable description of this trade type.
     */
    private final String description;

    /**
     * Revenue multiplier for this trade classification.
     * Higher values indicate more profitable trade types.
     */
    private final double revenueMultiplier;

    /**
     * Commodity price modifier for this classification.
     * Values > 1.0 indicate higher prices; < 1.0 indicate lower prices.
     */
    private final double priceModifier;

    /**
     * Preferred commodities for this trade classification.
     * These commodities are most profitable on worlds with this classification.
     */
    private final List<Commodity> preferredCommodities;

    /**
     * Creates a trade classification with primary and secondary codes.
     *
     * @param primary the primary trade code
     * @param secondary the secondary trade code, or null
     * @param description human-readable description
     * @param revenueMultiplier revenue multiplier (1.0 = normal)
     * @param priceModifier commodity price modifier (1.0 = normal)
     * @param preferredCommodities commodities most profitable on this world type
     */
    public TradeClassification(TradeCode primary, TradeCode secondary,
                                String description, double revenueMultiplier,
                                double priceModifier, List<Commodity> preferredCommodities) {
        this.primary = primary;
        this.secondary = secondary;
        this.description = description;
        this.revenueMultiplier = revenueMultiplier;
        this.priceModifier = priceModifier;
        this.preferredCommodities = Collections.unmodifiableList(
                preferredCommodities != null ? new ArrayList<>(preferredCommodities) : Collections.emptyList());
    }

    // ---- Accessors ----

    public TradeCode getPrimary() {
        return primary;
    }

    public TradeCode getSecondary() {
        return secondary;
    }

    public String getDescription() {
        return description;
    }

    public double getRevenueMultiplier() {
        return revenueMultiplier;
    }

    public double getPriceModifier() {
        return priceModifier;
    }

    public List<Commodity> getPreferredCommodities() {
        return preferredCommodities;
    }

    /**
     * Returns the trade classification code string.
     * Format: "Ag/Co" for dual-code, "Ag" for single-code.
     *
     * @return the classification code string
     */
    public String getCodeString() {
        if (secondary != null) {
            return primary.getCode() + "/" + secondary.getCode();
        }
        return primary.getCode();
    }

    // ---- Classification Logic ----

    /**
     * Determines the trade classification for a world based on its trade codes.
     * <p>
     * Uses the B07 classification algorithm:
     * </p>
     * <ol>
     *   <li>If the world has no trade codes, returns "None" classification</li>
     *   <li>If the world has one trade code, returns single-code classification</li>
     *   <li>If the world has multiple trade codes, returns the most economically significant pair</li>
     * </ol>
     *
     * @param world the world to classify
     * @return the trade classification
     */
    public static TradeClassification classify(TravellerWorld world) {
        Set<TradeCode> tradeCodes = world.getTradeCodes();
        if (tradeCodes == null || tradeCodes.isEmpty()) {
            return CLASSIFICATIONS.get("None");
        }

        if (tradeCodes.size() == 1) {
            TradeCode code = tradeCodes.iterator().next();
            String key = code.getCode();
            TradeClassification classification = CLASSIFICATIONS.get(key);
            if (classification != null) {
                return classification;
            }
            return createSingleClassification(code);
        }

        // Multiple codes — find the best pair
        List<TradeCode> codes = new ArrayList<>(tradeCodes);
        TradeCode bestPrimary = codes.get(0);
        TradeCode bestSecondary = codes.size() > 1 ? codes.get(1) : null;

        // Try to find a known classification pair
        for (int i = 0; i < codes.size(); i++) {
            for (int j = i + 1; j < codes.size(); j++) {
                String key = codes.get(i).getCode() + "/" + codes.get(j).getCode();
                TradeClassification classification = CLASSIFICATIONS.get(key);
                if (classification != null) {
                    return classification;
                }
            }
        }

        // No known pair — create dynamic classification
        return createDynamicClassification(bestPrimary, bestSecondary);
    }

    /**
     * Creates a classification for a single trade code.
     */
    private static TradeClassification createSingleClassification(TradeCode code) {
        return new TradeClassification(code, null, code.getDescription(),
                1.0, 1.0, defaultPreferredCommodities(code));
    }

    /**
     * Creates a dynamic classification for an unknown code pair.
     */
    private static TradeClassification createDynamicClassification(TradeCode primary, TradeCode secondary) {
        String desc = primary.getDescription() + (secondary != null ? " / " + secondary.getDescription() : "");
        return new TradeClassification(primary, secondary, desc,
                1.0, 1.0, defaultPreferredCommodities(primary));
    }

    /**
     * Returns the default preferred commodities for a trade code.
     */
    private static List<Commodity> defaultPreferredCommodities(TradeCode code) {
        return switch (code) {
            case AG -> List.of(Commodity.FOOD, Commodity.SPICES, Commodity.ANIMALS);
            case CE -> List.of(Commodity.FURS, Commodity.METALS);
            case CO -> List.of(Commodity.TIMBER, Commodity.FURS, Commodity.METALS);
            case EC -> List.of(Commodity.LUXURY, Commodity.ELECTRONICS, Commodity.TEXTILES);
            case FL, SE, WE -> List.of(Commodity.FUEL, Commodity.METALS);
            case HI, UR -> List.of(Commodity.LUXURY, Commodity.ELECTRONICS, Commodity.TEXTILES);
            case IC -> List.of(Commodity.FURS, Commodity.METALS);
            case LO -> List.of(Commodity.TIMBER, Commodity.FOOD);
            case NC -> List.of(Commodity.WEAPONS, Commodity.INFORMATION);
            case NI -> Collections.emptyList();
            case PO -> List.of(Commodity.FOOD, Commodity.METALS);
            case VA -> List.of(Commodity.METALS, Commodity.FUEL);
            case AN, LW -> List.of(Commodity.WEAPONS, Commodity.SLAVES, Commodity.INFORMATION);
            case CX -> List.of(Commodity.INFORMATION, Commodity.LUXURY);
            case MO -> List.of(Commodity.METALS, Commodity.GEMS, Commodity.TIMBER);
            case RF -> List.of(Commodity.FOOD, Commodity.MEDICINE);
            case RO -> List.of(Commodity.WEAPONS, Commodity.FUEL);
        };
    }

    @Override
    public String toString() {
        return getCodeString() + " — " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeClassification that = (TradeClassification) o;
        return primary == that.primary && secondary == that.secondary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, secondary);
    }

    // ---- Predefined B07 Classifications ----

    /**
     * Map of all predefined B07 trade classifications, keyed by code string.
     */
    public static final Map<String, TradeClassification> CLASSIFICATIONS;

    static {
        Map<String, TradeClassification> map = new LinkedHashMap<>();

        // Agricultural classifications
        map.put("Ag", new TradeClassification(TradeCode.AG, null, "Agricultural world",
                1.2, 0.8, List.of(Commodity.FOOD, Commodity.SPICES, Commodity.ANIMALS)));
        map.put("Ag/Co", new TradeClassification(TradeCode.AG, TradeCode.CO, "Agricultural Colonial",
                1.1, 0.9, List.of(Commodity.FOOD, Commodity.TIMBER, Commodity.SPICES)));
        map.put("Ag/Mo", new TradeClassification(TradeCode.AG, TradeCode.MO, "Agricultural Mountainous",
                1.3, 0.85, List.of(Commodity.FOOD, Commodity.METALS, Commodity.GEMS)));
        map.put("Ag/Ur", new TradeClassification(TradeCode.AG, TradeCode.UR, "Agricultural Urbanized",
                1.4, 0.75, List.of(Commodity.FOOD, Commodity.TEXTILES, Commodity.LUXURY)));

        // Cold classifications
        map.put("Ce", new TradeClassification(TradeCode.CE, null, "Cold world",
                1.0, 1.1, List.of(Commodity.FURS, Commodity.METALS)));
        map.put("Ce/Lo", new TradeClassification(TradeCode.CE, TradeCode.LO, "Cold Low Population",
                0.8, 1.2, List.of(Commodity.FURS, Commodity.METALS, Commodity.GEMS)));
        map.put("Ce/Co", new TradeClassification(TradeCode.CE, TradeCode.CO, "Cold Colonial",
                0.9, 1.1, List.of(Commodity.FURS, Commodity.METALS, Commodity.TIMBER)));
        map.put("Ce/Ag", new TradeClassification(TradeCode.CE, TradeCode.AG, "Cold Agricultural",
                1.1, 1.0, List.of(Commodity.FOOD, Commodity.FURS)));

        // Colonial classifications
        map.put("Co", new TradeClassification(TradeCode.CO, null, "Colonial world",
                0.9, 1.0, List.of(Commodity.TIMBER, Commodity.FURS, Commodity.METALS)));
        map.put("Co/Lo", new TradeClassification(TradeCode.CO, TradeCode.LO, "Colonial Low Population",
                0.7, 1.1, List.of(Commodity.TIMBER, Commodity.FURS)));
        map.put("Co/Po", new TradeClassification(TradeCode.CO, TradeCode.PO, "Colonial Poor",
                0.6, 1.2, List.of(Commodity.FOOD, Commodity.TIMBER)));

        // Ecumenopolis
        map.put("Ec", new TradeClassification(TradeCode.EC, null, "Ecumenopolis — world-city",
                2.0, 0.6, List.of(Commodity.LUXURY, Commodity.ELECTRONICS, Commodity.TEXTILES)));

        // Flooded/Sea/Water classifications
        map.put("Fl", new TradeClassification(TradeCode.FL, null, "Flooded world",
                0.8, 1.2, List.of(Commodity.FUEL, Commodity.METALS)));
        map.put("Se", new TradeClassification(TradeCode.SE, null, "Sea world",
                0.9, 1.1, List.of(Commodity.FUEL, Commodity.FOOD)));
        map.put("We", new TradeClassification(TradeCode.WE, null, "Water world",
                0.8, 1.1, List.of(Commodity.FUEL, Commodity.FOOD)));

        // High Population / Urbanized
        map.put("Hi", new TradeClassification(TradeCode.HI, null, "High Population world",
                1.5, 0.8, List.of(Commodity.LUXURY, Commodity.ELECTRONICS, Commodity.TEXTILES)));
        map.put("Ur", new TradeClassification(TradeCode.UR, null, "Urbanized world",
                1.6, 0.75, List.of(Commodity.LUXURY, Commodity.ELECTRONICS, Commodity.CHEMICALS)));
        map.put("Hi/Ur", new TradeClassification(TradeCode.HI, TradeCode.UR, "High Population Urbanized",
                1.8, 0.7, List.of(Commodity.LUXURY, Commodity.ELECTRONICS, Commodity.TEXTILES, Commodity.CHEMICALS)));

        // Ice Cap
        map.put("IC", new TradeClassification(TradeCode.IC, null, "Ice Cap world",
                0.9, 1.1, List.of(Commodity.FURS, Commodity.METALS)));

        // Low Population
        map.put("Lo", new TradeClassification(TradeCode.LO, null, "Low Population world",
                0.7, 1.1, List.of(Commodity.TIMBER, Commodity.FOOD)));

        // Non-Aligned
        map.put("NC", new TradeClassification(TradeCode.NC, null, "Non-Aligned world",
                1.0, 1.0, List.of(Commodity.WEAPONS, Commodity.INFORMATION)));

        // None
        map.put("None", new TradeClassification(null, null, "No trade",
                0.0, 2.0, Collections.emptyList()));
        map.put("Ni", new TradeClassification(TradeCode.NI, null, "No trade",
                0.0, 2.0, Collections.emptyList()));

        // Poor
        map.put("Po", new TradeClassification(TradeCode.PO, null, "Poor world",
                0.5, 1.3, List.of(Commodity.FOOD, Commodity.METALS)));
        map.put("Po/Co", new TradeClassification(TradeCode.PO, TradeCode.CO, "Poor Colonial",
                0.4, 1.4, List.of(Commodity.FOOD, Commodity.TIMBER)));

        // Vacuum
        map.put("Va", new TradeClassification(TradeCode.VA, null, "Vacuum world",
                0.6, 1.2, List.of(Commodity.METALS, Commodity.FUEL)));

        // Anarchy / Lawless
        map.put("An", new TradeClassification(TradeCode.AN, null, "Anarchy world",
                1.3, 0.9, List.of(Commodity.WEAPONS, Commodity.SLAVES, Commodity.INFORMATION)));
        map.put("Lw", new TradeClassification(TradeCode.LW, null, "Lawless world",
                1.2, 0.95, List.of(Commodity.WEAPONS, Commodity.SLAVES)));
        map.put("An/Lw", new TradeClassification(TradeCode.AN, TradeCode.LW, "Anarchy Lawless",
                1.5, 0.85, List.of(Commodity.WEAPONS, Commodity.SLAVES, Commodity.INFORMATION)));

        // Complex
        map.put("Cx", new TradeClassification(TradeCode.CX, null, "Complex world",
                1.1, 1.0, List.of(Commodity.INFORMATION, Commodity.LUXURY)));

        // Mountainous
        map.put("Mo", new TradeClassification(TradeCode.MO, null, "Mountainous world",
                1.2, 0.9, List.of(Commodity.METALS, Commodity.GEMS, Commodity.TIMBER)));

        // Refugee
        map.put("Rf", new TradeClassification(TradeCode.RF, null, "Refugee world",
                0.8, 1.1, List.of(Commodity.FOOD, Commodity.MEDICINE)));

        // Rough
        map.put("Ro", new TradeClassification(TradeCode.RO, null, "Rough world",
                1.0, 1.0, List.of(Commodity.WEAPONS, Commodity.FUEL)));

        CLASSIFICATIONS = Collections.unmodifiableMap(map);
    }

    /**
     * Returns all predefined trade classifications.
     *
     * @return unmodifiable map of code string to classification
     */
    public static Map<String, TradeClassification> getAllClassifications() {
        return CLASSIFICATIONS;
    }

    /**
     * Finds a predefined classification by code string.
     *
     * @param code the code string (e.g., "Ag/Co", "Ce/Lo")
     * @return the classification, or null if not found
     */
    public static TradeClassification findByCode(String code) {
        return CLASSIFICATIONS.get(code);
    }
}
