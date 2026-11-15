package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorporationOfficeStateTest {

    @Test
    void constructor_defaults() {
        CorporationOfficeState state = new CorporationOfficeState();
        assertNull(state.getId());
        assertNull(state.getCorporationId());
        assertEquals(0, state.getOfficeBuildingLevel());
        assertNotNull(state.getLastSavedAt());
    }

    @Test
    void constructor_withCorporationId() {
        CorporationOfficeState state = new CorporationOfficeState(1L);
        assertEquals(1L, state.getCorporationId());
        assertEquals(0, state.getOfficeBuildingLevel());
    }

    @Test
    void setOfficeBuildingLevel_clampsToZero() {
        CorporationOfficeState state = new CorporationOfficeState();
        state.setOfficeBuildingLevel(-5);
        assertEquals(0, state.getOfficeBuildingLevel());
    }

    @Test
    void setOfficeBuildingLevel_updatesTimestamp() {
        CorporationOfficeState state = new CorporationOfficeState();
        state.setOfficeBuildingLevel(3);
        assertEquals(3, state.getOfficeBuildingLevel());
        assertNotNull(state.getLastSavedAt());
    }

    @Test
    void incrementBuildingLevel() {
        CorporationOfficeState state = new CorporationOfficeState();
        state.setOfficeBuildingLevel(2);
        state.incrementBuildingLevel();
        assertEquals(3, state.getOfficeBuildingLevel());
    }

    @Test
    void toString_containsFields() {
        CorporationOfficeState state = new CorporationOfficeState(1L);
        state.setOfficeBuildingLevel(5);
        String str = state.toString();
        assertTrue(str.contains("corporationId=1"));
        assertTrue(str.contains("officeBuildingLevel=5"));
    }
}
