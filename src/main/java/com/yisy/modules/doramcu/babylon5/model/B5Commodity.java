package com.yisy.modules.doramcu.babylon5.model;

/**
 * Commodities available for trade in the Babylon 5 universe.
 * <p>
 * Each commodity has a base price, bulk factor (tons per unit), and rarity
 * that determines availability on different types of worlds. Trade profits
 * come from buying low in one system and selling high in another.
 * </p>
 * <p>
 * Commodities are themed around the Babylon 5 universe, including Earth Alliance
 * goods, Minbari religious items, Narn agricultural products, Centauri luxuries,
 * and rare alien artifacts.
 * </p>
 */
public enum B5Commodity {

    // ---- Basic Goods ----

    /**
     * Basic foodstuffs — grain, canned goods, hydroponic produce.
     * High bulk, low value, always in demand.
     */
    FOOD("Food", 100, 10, Rarity.COMMON),

    /**
     * Medicines and pharmaceuticals.
     * Low bulk, high value, essential on frontier worlds.
     */
    MEDICINE("Medicine", 500, 1, Rarity.COMMON),

    /**
     * Fuel and energy cells for ships and stations.
     * High bulk, moderate value, always needed.
     */
    FUEL("Fuel", 150, 8, Rarity.COMMON),

    /**
     * Industrial metals and alloys.
     * High bulk, moderate value, needed for manufacturing.
     */
    METALS("Metals", 200, 5, Rarity.COMMON),

    /**
     * Textiles and fabrics.
     * Moderate bulk, moderate value.
     */
    TEXTILES("Textiles", 300, 2, Rarity.COMMON),

    // ---- B5-Themed Goods ----

    /**
     * Narn grain — staple crop from Narn homeworld.
     * High bulk, low value, abundant in Narn space.
     */
    NARN_GRAIN("Narn Grain", 80, 12, Rarity.COMMON),

    /**
     * Centauri wine — fine wines from Centauri Prime vineyards.
     * Low bulk, high value, luxury item.
     */
    CENTAURI_WINE("Centauri Wine", 2000, 0.5, Rarity.UNCOMMON),

    /**
     * Minbari incense — sacred incense used in Minbari religious ceremonies.
     * Very low bulk, high value, rare outside Minbari space.
     */
    MINBARI_INCENSE("Minbari Incense", 3000, 0.2, Rarity.RARE),

    /**
     * Drazi fur — pelts from Drazi warrior beasts.
     * Moderate bulk, moderate value, prized by collectors.
     */
    DRAZI_FUR("Drazi Fur", 800, 3, Rarity.UNCOMMON),

    /**
     * Vree spices — exotic spices from Vree homeworld.
     * Very low bulk, high value, sought after by chefs.
     */
    VREE_SPICES("Vree Spices", 1500, 0.3, Rarity.UNCOMMON),

    /**
     * Hyach timber — rare wood from Hyach forests.
     * Very high bulk, moderate value, only on Hyach worlds.
     */
    HYACH_TIMBER("Hyach Timber", 100, 15, Rarity.UNCOMMON),

    /**
     * Brakiri religious artifacts — sacred items from Brakir.
     * Low bulk, very high value, only on religious worlds.
     */
    BRAKIRI_ARTIFACT("Brakiri Artifact", 5000, 1, Rarity.RARE),

    // ---- High-Tech Goods ----

    /**
     * Electronics and computer components.
     * Low bulk, high value, requires high technology level.
     */
    ELECTRONICS("Electronics", 2000, 0.5, Rarity.UNCOMMON),

    /**
     * Machinery and industrial equipment.
     * Very high bulk, high value.
     */
    MACHINERY("Machinery", 3000, 20, Rarity.UNCOMMON),

    /**
     * Weapons and ammunition.
     * Low bulk, high value, restricted on some worlds.
     */
    WEAPONS("Weapons", 1500, 1, Rarity.UNCOMMON),

    /**
     * Luxury items — art, jewelry, fine goods from Centauri.
     * Low bulk, very high value, only on wealthy worlds.
     */
    LUXURY("Luxury", 10000, 0.2, Rarity.RARE),

    /**
     * Precious gems and gemstones.
     * Very low bulk, very high value, rare.
     */
    GEMS("Gems", 5000, 0.1, Rarity.RARE),

    // ---- Rare Alien Goods ----

    /**
     * Vorlon artifacts — ancient technology fragments.
     * Zero bulk, extremely high value, extremely rare.
     */
    VORLON_ARTIFACT("Vorlon Artifact", 50000, 0, Rarity.EXTREMELY_RARE),

    /**
     * Shadow crystals — energy crystals from Shadow worlds.
     * Very low bulk, extremely high value, dangerous to handle.
     */
    SHADOW_CRYSTAL("Shadow Crystal", 25000, 0.1, Rarity.EXTREMELY_RARE),

    /**
     * First One technology — fragments of ancient First One devices.
     * Zero bulk, priceless, nearly impossible to obtain.
     */
    FIRST_ONE_TECH("First One Tech", 100000, 0, Rarity.EXTREMELY_RARE),

    /**
     * Psi corps training materials — psionic enhancement devices.
     * Low bulk, very high value, restricted to authorized personnel.
     */
    PSI_MATERIALS("Psi Materials", 15000, 0.5, Rarity.RARE),

    // ---- Restricted Goods ----

    /**
     * Information and data — intelligence, charts, secrets.
     * Zero bulk, variable value.
     */
    INFORMATION("Information", 1000, 0, Rarity.RARE),

    /**
     * Contraband weapons — illegal weapons from Shadow or Vorlon sources.
     * Low bulk, very high value, illegal on most worlds.
     */
    CONTRABAND_WEAPONS("Contraband Weapons", 20000, 1, Rarity.ILLEGAL),

    /**
     * Slaves and indentured servants.
     * Moderate bulk, high value, illegal on many worlds.
     */
    SLAVES("Slaves", 8000, 2, Rarity.ILLEGAL),

    /**
     * Shadow bio-weapons — biological weapons from Shadow experiments.
     * Very low bulk, extremely high value, extremely dangerous.
     */
    SHADOW_BIO_WEAPON("Shadow Bio-Weapon", 75000, 0.1, Rarity.ILLEGAL);

    private final String name;
    private final int basePrice;
    private final double bulkTons;
    private final Rarity rarity;

    B5Commodity(String name, int basePrice, double bulkTons, Rarity rarity) {
        this.name = name;
        this.basePrice = basePrice;
        this.bulkTons = bulkTons;
        this.rarity = rarity;
    }

    /**
     * Returns the human-readable name of this commodity.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the base price per unit in credits.
     * <p>
     * Actual price varies by system market conditions and supply/demand.
     * </p>
     *
     * @return base price in credits
     */
    public int getBasePrice() {
        return basePrice;
    }

    /**
     * Returns the bulk factor in tons per unit.
     * <p>
     * Determines how many units fit in a given cargo capacity.
     * Zero bulk items (like information) have no weight limit.
     * </p>
     *
     * @return tons per unit
     */
    public double getBulkTons() {
        return bulkTons;
    }

    /**
     * Returns the rarity of this commodity.
     *
     * @return the rarity level
     */
    public Rarity getRarity() {
        return rarity;
    }

    /**
     * Calculates the number of units that fit in the given cargo tons.
     *
     * @param cargoTons available cargo capacity in tons
     * @return maximum units that can be carried
     */
    public int maxUnitsForCargo(int cargoTons) {
        if (bulkTons <= 0) {
            return 1000; // zero-bulk items have arbitrary high limit
        }
        return (int) (cargoTons / bulkTons);
    }

    /**
     * Calculates the tons required for the given number of units.
     *
     * @param units number of units
     * @return tons required
     */
    public double tonsForUnits(int units) {
        return units * bulkTons;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Rarity of a commodity, affecting availability on different worlds.
     */
    public enum Rarity {
        /**
         * Available on most worlds.
         */
        COMMON,

        /**
         * Available on many worlds, but not all.
         */
        UNCOMMON,

        /**
         * Only available on specific types of worlds.
         */
        RARE,

        /**
         * Nearly impossible to find, only in special circumstances.
         */
        EXTREMELY_RARE,

        /**
         * Illegal or restricted on most worlds.
         */
        ILLEGAL;
    }
}
