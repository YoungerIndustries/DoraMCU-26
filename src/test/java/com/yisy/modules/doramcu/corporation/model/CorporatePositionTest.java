package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporatePosition Enum Tests")
class CorporatePositionTest {

    @Test
    @DisplayName("should have exactly 9 positions")
    void positionCount() {
        assertEquals(9, CorporatePosition.values().length);
    }

    @Test
    @DisplayName("leadership positions should be classified correctly")
    void leadershipPositions() {
        assertTrue(CorporatePosition.CFO.isLeadership());
        assertTrue(CorporatePosition.COO.isLeadership());
        assertTrue(CorporatePosition.CTO.isLeadership());
        assertTrue(CorporatePosition.SECTOR_GOVERNOR_DELEGATE.isLeadership());
        assertTrue(CorporatePosition.TRADE_MINISTER.isLeadership());
    }

    @Test
    @DisplayName("crew positions should be classified correctly")
    void crewPositions() {
        assertTrue(CorporatePosition.SHIP_CAPTAIN.isCrew());
        assertTrue(CorporatePosition.SECURITY_CHIEF.isCrew());
        assertTrue(CorporatePosition.SCIENCE_OFFICER.isCrew());
        assertTrue(CorporatePosition.ENGINEER.isCrew());
    }

    @Test
    @DisplayName("leadership positions should not be crew")
    void leadershipNotCrew() {
        assertFalse(CorporatePosition.CFO.isCrew());
        assertFalse(CorporatePosition.COO.isCrew());
        assertFalse(CorporatePosition.CTO.isCrew());
        assertFalse(CorporatePosition.SECTOR_GOVERNOR_DELEGATE.isCrew());
        assertFalse(CorporatePosition.TRADE_MINISTER.isCrew());
    }

    @Test
    @DisplayName("crew positions should not be leadership")
    void crewNotLeadership() {
        assertFalse(CorporatePosition.SHIP_CAPTAIN.isLeadership());
        assertFalse(CorporatePosition.SECURITY_CHIEF.isLeadership());
        assertFalse(CorporatePosition.SCIENCE_OFFICER.isLeadership());
        assertFalse(CorporatePosition.ENGINEER.isLeadership());
    }

    @Test
    @DisplayName("getDisplayName should return non-empty names")
    void getDisplayName() {
        for (CorporatePosition pos : CorporatePosition.values()) {
            assertNotNull(pos.getDisplayName());
            assertFalse(pos.getDisplayName().isBlank());
        }
    }

    @Test
    @DisplayName("getDescription should return non-empty descriptions")
    void getDescription() {
        for (CorporatePosition pos : CorporatePosition.values()) {
            assertNotNull(pos.getDescription());
            assertFalse(pos.getDescription().isBlank());
        }
    }

    @Test
    @DisplayName("CFO should have correct display name")
    void cfoDisplayName() {
        assertEquals("Chief Financial Officer", CorporatePosition.CFO.getDisplayName());
    }

    @Test
    @DisplayName("SHIP_CAPTAIN should have correct display name")
    void shipCaptainDisplayName() {
        assertEquals("Ship Captain", CorporatePosition.SHIP_CAPTAIN.getDisplayName());
    }
}
