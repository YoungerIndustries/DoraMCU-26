package com.yisy.modules.doramcu.hex.planet;

/**
 * Career progression ranks for a Transport Corporation in the Business Tycoon campaign.
 * <p>
 * Instead of character levels, the corporation progresses through ranks based on
 * total assets, route count, and market share. Each rank unlocks new capabilities
 * such as larger vehicles, access to more planets, and AI assistant features.
 * </p>
 * <p>
 * Progression path:
 * <pre>
 *   Freelancer → Startup → Regional Carrier → Interplanetary Logistics → Galactic Monopoly
 * </pre>
 * </p>
 */
public enum CorporationRank {

    /**
     * Entry-level operator with minimal assets.
     * Can operate on 1 planet with basic vehicles.
     */
    FREELANCER(0, 5000, "Freelancer", 1, 2, 1,
            "A lone transport operator just starting out. Small vehicles, single planet."),

    /**
     * Small business with a few vehicles and routes.
     * Can operate on 2 planets, access to trucks and small ships.
     */
    STARTUP(5000, 50000, "Startup", 2, 5, 3,
            "A growing transport company with multiple vehicles and routes."),

    /**
     * Established regional carrier with significant infrastructure.
     * Can operate on 3 planets, access to trains and larger ships.
     */
    REGIONAL(50000, 500000, "Regional Carrier", 3, 15, 5,
            "A well-known regional logistics provider with stations and fleets."),

    /**
     * Major interplanetary logistics corporation.
     * Can operate on 5 planets, access to all vehicle types and space ships.
     */
    INTERPLANETARY(500000, 5000000, "Interplanetary Logistics", 5, 50, 10,
            "A dominant force in interplanetary cargo transport."),

    /**
     * Ultimate rank — galactic monopoly on transportation.
     * Can operate on up to 10 planets with unlimited capabilities.
     * This is the win condition for the campaign.
     */
    GALACTIC_MONOPOLY(5000000, Integer.MAX_VALUE, "Galactic Monopoly", 10, Integer.MAX_VALUE, 20,
            "Total control of galactic transportation. The ultimate tycoon achievement.");

    private final int minCredits;
    private final int maxCredits;
    private final String title;
    private final int maxPlanets;
    private final int maxRoutes;
    private final int maxVehicles;
    private final String description;

    CorporationRank(int minCredits, int maxCredits, String title, int maxPlanets,
                     int maxRoutes, int maxVehicles, String description) {
        this.minCredits = minCredits;
        this.maxCredits = maxCredits;
        this.title = title;
        this.maxPlanets = maxPlanets;
        this.maxRoutes = maxRoutes;
        this.maxVehicles = maxVehicles;
        this.description = description;
    }

    /** @return the minimum credits required to reach this rank */
    public int getMinCredits() {
        return minCredits;
    }

    /** @return the maximum credits for this rank (exclusive upper bound) */
    public int getMaxCredits() {
        return maxCredits;
    }

    /** @return the human-readable rank title */
    public String getTitle() {
        return title;
    }

    /** @return the maximum number of planets the corporation can operate on */
    public int getMaxPlanets() {
        return maxPlanets;
    }

    /** @return the maximum number of trade routes allowed */
    public int getMaxRoutes() {
        return maxRoutes;
    }

    /** @return the maximum number of vehicles allowed */
    public int getMaxVehicles() {
        return maxVehicles;
    }

    /** @return a description of this rank */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the next rank in the progression, or null if already at the top.
     *
     * @return the next rank or null
     */
    public CorporationRank nextRank() {
        CorporationRank[] values = values();
        int ordinal = this.ordinal();
        if (ordinal < values.length - 1) {
            return values[ordinal + 1];
        }
        return null;
    }

    /**
     * Returns the previous rank in the progression, or null if already at the bottom.
     *
     * @return the previous rank or null
     */
    public CorporationRank previousRank() {
        if (ordinal() > 0) {
            return values()[ordinal() - 1];
        }
        return null;
    }

    /**
     * Determines the rank based on the current credit amount.
     *
     * @param credits the current credit amount
     * @return the matching rank
     */
    public static CorporationRank fromCredits(int credits) {
        for (CorporationRank rank : values()) {
            if (credits >= rank.minCredits && credits < rank.maxCredits) {
                return rank;
            }
        }
        return GALACTIC_MONOPOLY;
    }
}
