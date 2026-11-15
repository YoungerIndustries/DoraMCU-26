package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanetEventTypeTest {

    // ---- isSettlementEvent ----

    @Test
    void isSettlementEvent_returnsTrueForSettlementEvents() {
        assertTrue(PlanetEventType.SETTLEMENT_FOUND.isSettlementEvent());
        assertTrue(PlanetEventType.SETTLEMENT_GROWN.isSettlementEvent());
        assertTrue(PlanetEventType.SETTLEMENT_ABANDONED.isSettlementEvent());
        assertTrue(PlanetEventType.SETTLEMENT_UPGRADED.isSettlementEvent());
        assertTrue(PlanetEventType.SETTLEMENT_CLAIMED.isSettlementEvent());
    }

    @Test
    void isSettlementEvent_returnsFalseForNonSettlementEvents() {
        assertFalse(PlanetEventType.INDUSTRY_BUILT.isSettlementEvent());
        assertFalse(PlanetEventType.ROAD_CONSTRUCTED.isSettlementEvent());
        assertFalse(PlanetEventType.SUBSIDY_GRANTED.isSettlementEvent());
        assertFalse(PlanetEventType.BIOME_GENERATED.isSettlementEvent());
        assertFalse(PlanetEventType.ROUTE_ESTABLISHED.isSettlementEvent());
    }

    // ---- isIndustryEvent ----

    @Test
    void isIndustryEvent_returnsTrueForIndustryEvents() {
        assertTrue(PlanetEventType.INDUSTRY_BUILT.isIndustryEvent());
        assertTrue(PlanetEventType.INDUSTRY_CLOSED.isIndustryEvent());
        assertTrue(PlanetEventType.INDUSTRY_PRODUCTION_CHANGED.isIndustryEvent());
        assertTrue(PlanetEventType.INDUSTRY_FULL_PRODUCTION.isIndustryEvent());
    }

    @Test
    void isIndustryEvent_returnsFalseForNonIndustryEvents() {
        assertFalse(PlanetEventType.SETTLEMENT_FOUND.isIndustryEvent());
        assertFalse(PlanetEventType.STATION_BUILT.isIndustryEvent());
        assertFalse(PlanetEventType.NATURAL_DISASTER.isIndustryEvent());
    }

    // ---- isTransportEvent ----

    @Test
    void isTransportEvent_returnsTrueForTransportEvents() {
        assertTrue(PlanetEventType.ROAD_CONSTRUCTED.isTransportEvent());
        assertTrue(PlanetEventType.RAIL_CONSTRUCTED.isTransportEvent());
        assertTrue(PlanetEventType.STATION_BUILT.isTransportEvent());
        assertTrue(PlanetEventType.STATION_REMOVED.isTransportEvent());
        assertTrue(PlanetEventType.VEHICLE_BUILT.isTransportEvent());
        assertTrue(PlanetEventType.VEHICLE_DEPLOYED.isTransportEvent());
        assertTrue(PlanetEventType.VEHICLE_REMOVED.isTransportEvent());
        assertTrue(PlanetEventType.CARGO_DELIVERED.isTransportEvent());
        assertTrue(PlanetEventType.VEHICLE_DESTROYED.isTransportEvent());
        assertTrue(PlanetEventType.ROUTE_ESTABLISHED.isTransportEvent());
    }

    @Test
    void isTransportEvent_returnsFalseForNonTransportEvents() {
        assertFalse(PlanetEventType.SETTLEMENT_FOUND.isTransportEvent());
        assertFalse(PlanetEventType.INDUSTRY_BUILT.isTransportEvent());
        assertFalse(PlanetEventType.SUBSIDY_GRANTED.isTransportEvent());
        assertFalse(PlanetEventType.CONTRACT_ACCEPTED.isTransportEvent());
    }

    // ---- isEconomyEvent ----

    @Test
    void isEconomyEvent_returnsTrueForEconomyEvents() {
        assertTrue(PlanetEventType.SUBSIDY_GRANTED.isEconomyEvent());
        assertTrue(PlanetEventType.CARGO_TYPE_AVAILABLE.isEconomyEvent());
        assertTrue(PlanetEventType.PROFIT_MILESTONE.isEconomyEvent());
    }

    @Test
    void isEconomyEvent_returnsFalseForNonEconomyEvents() {
        assertFalse(PlanetEventType.SETTLEMENT_FOUND.isEconomyEvent());
        assertFalse(PlanetEventType.VEHICLE_BUILT.isEconomyEvent());
        assertFalse(PlanetEventType.NATURAL_DISASTER.isEconomyEvent());
    }

    // ---- isCampaignEvent ----

    @Test
    void isCampaignEvent_returnsTrueForCampaignEvents() {
        assertTrue(PlanetEventType.ROUTE_ESTABLISHED.isCampaignEvent());
        assertTrue(PlanetEventType.CONTRACT_ACCEPTED.isCampaignEvent());
        assertTrue(PlanetEventType.OBJECTIVE_COMPLETED.isCampaignEvent());
        assertTrue(PlanetEventType.RANK_UP.isCampaignEvent());
        assertTrue(PlanetEventType.TURN_COMPLETED.isCampaignEvent());
        assertTrue(PlanetEventType.CAMPAIGN_ENDED.isCampaignEvent());
    }

    @Test
    void isCampaignEvent_returnsFalseForNonCampaignEvents() {
        assertFalse(PlanetEventType.SETTLEMENT_FOUND.isCampaignEvent());
        assertFalse(PlanetEventType.INDUSTRY_BUILT.isCampaignEvent());
        assertFalse(PlanetEventType.NATURAL_DISASTER.isCampaignEvent());
        assertFalse(PlanetEventType.GOAL_COMPLETED.isCampaignEvent());
    }

    // ---- Cross-category checks ----

    @Test
    void ROUTE_ESTABLISHED_isBothTransportAndCampaign() {
        assertTrue(PlanetEventType.ROUTE_ESTABLISHED.isTransportEvent());
        assertTrue(PlanetEventType.ROUTE_ESTABLISHED.isCampaignEvent());
    }

    @Test
    void enumHasExpectedNumberOfValues() {
        // Count all event types defined in the enum
        int count = PlanetEventType.values().length;
        // 34 event types: 5 settlement + 4 industry + 9 transport + 3 economy + 4 environment + 3 player + 6 campaign
        assertEquals(34, count);
    }
}
