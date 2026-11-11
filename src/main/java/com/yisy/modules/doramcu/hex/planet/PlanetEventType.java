package com.yisy.modules.doramcu.hex.planet;

/**
 * Event types for planet-level game events.
 * <p>
 * Extends the basic {@link com.yisy.modules.doramcu.hex.HexEvent} with
 * planet-specific events inspired by OpenTTD's 20+ event types.
 * These events drive reactive game logic: settlement growth, industry
 * production, vehicle movement, and economy changes.
 */
public enum PlanetEventType {

    // ---- Settlement Events ----

    /** A new settlement was founded on the planet surface */
    SETTLEMENT_FOUND,

    /** A settlement grew in population */
    SETTLEMENT_GROWN,

    /** A settlement was abandoned or destroyed */
    SETTLEMENT_ABANDONED,

    /** A settlement was removed from the planet */
    SETTLEMENT_REMOVED,

    /** A settlement reached a new size tier (village → town → city) */
    SETTLEMENT_UPGRADED,

    // ---- Industry Events ----

    /** A new industry was built */
    INDUSTRY_BUILT,

    /** An industry closed down due to lack of inputs */
    INDUSTRY_CLOSED,

    /** An industry's production level changed significantly */
    INDUSTRY_PRODUCTION_CHANGED,

    /** An industry reached full production capacity */
    INDUSTRY_FULL_PRODUCTION,

    // ---- Transport Events ----

    /** A road was constructed on a hex cell */
    ROAD_CONSTRUCTED,

    /** A rail line was constructed on a hex cell */
    RAIL_CONSTRUCTED,

    /** A new station was built */
    STATION_BUILT,

    /** A station was removed */
    STATION_REMOVED,

    /** A vehicle was purchased and deployed */
    VEHICLE_BUILT,

    /** A vehicle was deployed on a route */
    VEHICLE_DEPLOYED,

    /** A vehicle was removed */
    VEHICLE_REMOVED,

    /** A vehicle completed a delivery */
    CARGO_DELIVERED,

    /** A vehicle broke down or was destroyed */
    VEHICLE_DESTROYED,

    // ---- Economy Events ----

    /** A subsidy was granted for a cargo type or industry */
    SUBSIDY_GRANTED,

    /** A new cargo type became available */
    CARGO_TYPE_AVAILABLE,

    /** The economy reached a profit milestone */
    PROFIT_MILESTONE,

    // ---- Environment Events ----

    /** A natural disaster affected a region (earthquake, flood, etc.) */
    NATURAL_DISASTER,

    /** A resource deposit was discovered */
    RESOURCE_DISCOVERED,

    /** Climate change affected biome distribution */
    CLIMATE_CHANGE,

    /** Biomes were generated for a planet surface */
    BIOME_GENERATED,

    // ---- Player Events ----

    /** A player claimed ownership of a settlement */
    SETTLEMENT_CLAIMED,

    /** A player completed a goal or mission */
    GOAL_COMPLETED,

    /** A new goal or mission became available */
    GOAL_AVAILABLE,

    // ---- Campaign Events ----

    /** A trade route was established */
    ROUTE_ESTABLISHED,

    /** A contract was accepted by a corporation */
    CONTRACT_ACCEPTED,

    /** A campaign objective was completed */
    OBJECTIVE_COMPLETED,

    /** A corporation ranked up */
    RANK_UP,

    /** A turn was completed */
    TURN_COMPLETED,

    /** The campaign ended */
    CAMPAIGN_ENDED;

    /**
     * @return true if this event is related to settlements
     */
    public boolean isSettlementEvent() {
        return this == SETTLEMENT_FOUND || this == SETTLEMENT_GROWN ||
                this == SETTLEMENT_ABANDONED || this == SETTLEMENT_UPGRADED ||
                this == SETTLEMENT_CLAIMED;
    }

    /**
     * @return true if this event is related to industries
     */
    public boolean isIndustryEvent() {
        return this == INDUSTRY_BUILT || this == INDUSTRY_CLOSED ||
                this == INDUSTRY_PRODUCTION_CHANGED || this == INDUSTRY_FULL_PRODUCTION;
    }

    /**
     * @return true if this event is related to transport
     */
    public boolean isTransportEvent() {
        return this == ROAD_CONSTRUCTED || this == RAIL_CONSTRUCTED ||
                this == STATION_BUILT || this == STATION_REMOVED ||
                this == VEHICLE_BUILT || this == VEHICLE_DEPLOYED || this == VEHICLE_REMOVED ||
                this == CARGO_DELIVERED || this == VEHICLE_DESTROYED ||
                this == ROUTE_ESTABLISHED;
    }

    /**
     * @return true if this event is related to the economy
     */
    public boolean isEconomyEvent() {
        return this == SUBSIDY_GRANTED || this == CARGO_TYPE_AVAILABLE ||
                this == PROFIT_MILESTONE;
    }

    /**
     * @return true if this event is related to the campaign
     */
    public boolean isCampaignEvent() {
        return this == ROUTE_ESTABLISHED || this == CONTRACT_ACCEPTED ||
                this == OBJECTIVE_COMPLETED || this == RANK_UP ||
                this == TURN_COMPLETED || this == CAMPAIGN_ENDED;
    }
}
