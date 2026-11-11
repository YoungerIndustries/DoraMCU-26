package com.yisy.modules.doramcu.sfb.model;

/**
 * Commodities available in SFB trade simulation.
 * <p>
 * Each commodity has a base price, bulk tons, and rarity level.
 * Commodities are themed around the Star Trek universe including
 * Federation, Klingon, Romulan, and other faction goods.
 * </p>
 */
public enum SfbCommodity {
    // Food and beverages
    FOOD("Food", 100, 10, Rarity.COMMON),
    TRILL_TEA("Trill Tea", 500, 1, Rarity.UNCOMMON),
    KLINGON_WINE("Klingon Wine", 800, 2, Rarity.UNCOMMON),
    ROMULAN_ALE("Romulan Ale", 600, 2, Rarity.UNCOMMON),
    BAJORAN_SPICE("Bajoran Spice", 1200, 0.5, Rarity.RARE),
    ANDORIAN_WHISKEY("Andorian Whiskey", 700, 1, Rarity.UNCOMMON),

    // Minerals and resources
    DILITHIUM("Dilithium", 5000, 5, Rarity.RARE),
    TRITANIUM("Tritanium", 3000, 8, Rarity.UNCOMMON),
    PLASMA("Plasma", 2000, 3, Rarity.COMMON),
    DEUTERIUM("Deuterium", 500, 10, Rarity.COMMON),
    GOLD("Gold", 10000, 1, Rarity.RARE),
    PLATINUM("Platinum", 15000, 1, Rarity.EXTREMELY_RARE),

    // Technology
    PHASER_TECH("Phaser Technology", 8000, 2, Rarity.RARE),
    CLOAKING_DEVICE("Cloaking Device", 50000, 5, Rarity.EXTREMELY_RARE),
    WARP_CORE("Warp Core", 100000, 10, Rarity.EXTREMELY_RARE),
    DEFLECTOR_SHIELD("Deflector Shield", 20000, 8, Rarity.RARE),

    // Medical
    MEDICINE("Medicine", 1500, 2, Rarity.COMMON),
    TRICORDER("Tricorder", 3000, 1, Rarity.UNCOMMON),
    HYPODERMIC("Hypodermic", 2000, 1, Rarity.UNCOMMON),

    // Luxury
    FEDERATION_ART("Federation Art", 5000, 1, Rarity.RARE),
    KLINGON_ARTIFACT("Klingon Artifact", 7000, 2, Rarity.RARE),
    ROMULAN_SILK("Romulan Silk", 4000, 1, Rarity.UNCOMMON),
    VULCAN_SCULPTURE("Vulcan Sculpture", 6000, 2, Rarity.RARE),

    // Illegal
    ILLEGAL_WEAPONS("Illegal Weapons", 25000, 5, Rarity.ILLEGAL),
    STOLEN_TECH("Stolen Technology", 30000, 3, Rarity.ILLEGAL);

    private final String name;
    private final int basePrice;
    private final double bulkTons;
    private final Rarity rarity;

    SfbCommodity(String name, int basePrice, double bulkTons, Rarity rarity) {
        this.name = name;
        this.basePrice = basePrice;
        this.bulkTons = bulkTons;
        this.rarity = rarity;
    }

    public String getName() {
        return name;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public double getBulkTons() {
        return bulkTons;
    }

    public Rarity getRarity() {
        return rarity;
    }

    /**
     * Rarity levels for commodities.
     */
    public enum Rarity {
        COMMON("Common"),
        UNCOMMON("Uncommon"),
        RARE("Rare"),
        EXTREMELY_RARE("Extremely Rare"),
        ILLEGAL("Illegal");

        private final String name;

        Rarity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
