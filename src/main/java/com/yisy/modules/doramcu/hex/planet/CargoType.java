package com.yisy.modules.doramcu.hex.planet;

/**
 * Represents types of cargo that can be produced, transported, and consumed
 * in the planet-level economy.
 * <p>
 * Inspired by OpenTTD's cargo system, each cargo type has:
 * <ul>
 *   <li>A display name and color for UI rendering</li>
 *   <li>A category (raw, processed, consumer, mail)</li>
 *   <li>Perishability — whether the cargo decays if not delivered quickly</li>
 *   <li>Value per unit — base economic value</li>
 * </ul>
 * <p>
 * Cargo flows through the economy via:
 * <ul>
 *   <li>Primary industries → produce raw cargo (ore, wood, grain)</li>
 *   <li>Secondary industries → consume raw, produce processed (steel, lumber, bread)</li>
 *   <li>Tertiary industries → consume processed, produce advanced goods</li>
 *   <li>Settlements → consume consumer goods, produce mail</li>
 * </ul>
 */
public enum CargoType {

    // ---- Raw Materials (Primary Industries) ----

    /** Iron ore from mines */
    ORE("Ore", "#8B8682", CargoCategory.RAW, false, 10),

    /** Coal from coal mines */
    COAL("Coal", "#2C2C2C", CargoCategory.RAW, false, 12),

    /** Crude oil from oil wells */
    OIL("Crude Oil", "#1A1A2E", CargoCategory.RAW, false, 15),

    /** Wood from logging camps */
    WOOD("Wood", "#8B4513", CargoCategory.RAW, false, 8),

    /** Grain from farms */
    GRAIN("Grain", "#DAA520", CargoCategory.RAW, true, 6),

    /** Livestock from ranches */
    LIVESTOCK("Livestock", "#8FBC8F", CargoCategory.RAW, true, 10),

    /** Fish from fishing ports */
    FISH("Fish", "#4682B4", CargoCategory.RAW, true, 9),

    /** Fruit from orchards */
    FRUIT("Fruit", "#FF6347", CargoCategory.RAW, true, 7),

    /** Mail from settlements */
    MAIL("Mail", "#FFD700", CargoCategory.MAIL, true, 5),

    /** Passengers traveling between settlements */
    PASSENGERS("Passengers", "#FF69B4", CargoCategory.MAIL, true, 20),

    // ---- Processed Materials (Secondary Industries) ----

    /** Steel from steel mills */
    STEEL("Steel", "#708090", CargoCategory.PROCESSED, false, 25),

    /** Concrete from cement plants */
    CONCRETE("Concrete", "#A9A9A9", CargoCategory.PROCESSED, false, 15),

    /** Glass from glass factories */
    GLASS("Glass", "#87CEEB", CargoCategory.PROCESSED, false, 18),

    /** Lumber from sawmills */
    LUMBER("Lumber", "#DEB887", CargoCategory.PROCESSED, false, 12),

    /** Bread from bakeries */
    BREAD("Bread", "#F5DEB3", CargoCategory.PROCESSED, true, 10),

    /** Meat from slaughterhouses */
    MEAT("Meat", "#CD5C5C", CargoCategory.PROCESSED, true, 14),

    /** Refined oil from refineries */
    REFINED_OIL("Refined Oil", "#4A4A6A", CargoCategory.PROCESSED, false, 30),

    /** Plastic from chemical plants */
    PLASTIC("Plastic", "#FF6B6B", CargoCategory.PROCESSED, false, 22),

    // ---- Advanced Goods (Tertiary Industries) ----

    /** Electronics from factories */
    ELECTRONICS("Electronics", "#00CED1", CargoCategory.ADVANCED, false, 45),

    /** Machinery from machine shops */
    MACHINERY("Machinery", "#696969", CargoCategory.ADVANCED, false, 40),

    /** Luxury goods from luxury factories */
    LUXURY("Luxury Goods", "#FFD700", CargoCategory.ADVANCED, false, 55),

    /** Chemicals from chemical plants */
    CHEMICALS("Chemicals", "#9370DB", CargoCategory.ADVANCED, false, 35);

    private final String displayName;
    private final String color;
    private final CargoCategory category;
    private final boolean perishable;
    private final int baseValue;

    CargoType(String displayName, String color, CargoCategory category,
              boolean perishable, int baseValue) {
        this.displayName = displayName;
        this.color = color;
        this.category = category;
        this.perishable = perishable;
        this.baseValue = baseValue;
    }

    /** @return the human-readable display name */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the RGB hex color for UI rendering */
    public String getColor() {
        return color;
    }

    /** @return the cargo category */
    public CargoCategory getCategory() {
        return category;
    }

    /** @return true if this cargo decays over time if not delivered */
    public boolean isPerishable() {
        return perishable;
    }

    /** @return the base economic value per unit */
    public int getBaseValue() {
        return baseValue;
    }

    /**
     * Categories of cargo that determine how they flow through the economy.
     */
    public enum CargoCategory {
        /** Raw materials extracted from primary industries */
        RAW,
        /** Processed materials from secondary industries */
        PROCESSED,
        /** Advanced goods from tertiary industries */
        ADVANCED,
        /** Consumer goods and mail */
        MAIL
    }
}
