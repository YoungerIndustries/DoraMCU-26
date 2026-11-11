package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorporationRankTest {

    @Test
    void values_returnsAllRanks() {
        CorporationRank[] ranks = CorporationRank.values();
        assertEquals(5, ranks.length);
    }

    @Test
    void nextRank_progressesCorrectly() {
        assertEquals(CorporationRank.STARTUP, CorporationRank.FREELANCER.nextRank());
        assertEquals(CorporationRank.REGIONAL, CorporationRank.STARTUP.nextRank());
        assertEquals(CorporationRank.INTERPLANETARY, CorporationRank.REGIONAL.nextRank());
        assertEquals(CorporationRank.GALACTIC_MONOPOLY, CorporationRank.INTERPLANETARY.nextRank());
        assertNull(CorporationRank.GALACTIC_MONOPOLY.nextRank());
    }

    @Test
    void previousRank_regressesCorrectly() {
        assertNull(CorporationRank.FREELANCER.previousRank());
        assertEquals(CorporationRank.FREELANCER, CorporationRank.STARTUP.previousRank());
        assertEquals(CorporationRank.STARTUP, CorporationRank.REGIONAL.previousRank());
        assertEquals(CorporationRank.REGIONAL, CorporationRank.INTERPLANETARY.previousRank());
        assertEquals(CorporationRank.INTERPLANETARY, CorporationRank.GALACTIC_MONOPOLY.previousRank());
    }

    @Test
    void getMinCredits_returnsCorrectValues() {
        assertEquals(0, CorporationRank.FREELANCER.getMinCredits());
        assertEquals(5000, CorporationRank.STARTUP.getMinCredits());
        assertEquals(50000, CorporationRank.REGIONAL.getMinCredits());
        assertEquals(500000, CorporationRank.INTERPLANETARY.getMinCredits());
        assertEquals(5000000, CorporationRank.GALACTIC_MONOPOLY.getMinCredits());
    }

    @Test
    void getMaxPlanets_returnsCorrectValues() {
        assertEquals(1, CorporationRank.FREELANCER.getMaxPlanets());
        assertEquals(2, CorporationRank.STARTUP.getMaxPlanets());
        assertEquals(3, CorporationRank.REGIONAL.getMaxPlanets());
        assertEquals(5, CorporationRank.INTERPLANETARY.getMaxPlanets());
        assertEquals(10, CorporationRank.GALACTIC_MONOPOLY.getMaxPlanets());
    }

    @Test
    void ordinal_ordering() {
        assertEquals(0, CorporationRank.FREELANCER.ordinal());
        assertEquals(1, CorporationRank.STARTUP.ordinal());
        assertEquals(2, CorporationRank.REGIONAL.ordinal());
        assertEquals(3, CorporationRank.INTERPLANETARY.ordinal());
        assertEquals(4, CorporationRank.GALACTIC_MONOPOLY.ordinal());
    }
}
