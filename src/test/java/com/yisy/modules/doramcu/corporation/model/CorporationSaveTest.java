package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorporationSaveTest {

    @Test
    void constructor_defaults() {
        CorporationSave save = new CorporationSave();
        assertNull(save.getId());
        assertNull(save.getCorporationId());
        assertNull(save.getSaveName());
        assertEquals(0L, save.getCreditBalance());
        assertNotNull(save.getTimestamp());
    }

    @Test
    void constructor_withParams() {
        CorporationSave save = new CorporationSave(1L, "Test Save");
        assertEquals(1L, save.getCorporationId());
        assertEquals("Test Save", save.getSaveName());
        assertEquals(0L, save.getCreditBalance());
    }

    @Test
    void setters() {
        CorporationSave save = new CorporationSave();
        save.setCorporationId(5L);
        save.setSaveName("My Save");
        save.setCreditBalance(10000L);
        save.setActiveContracts("[{\"id\":1}]");
        save.setCrewState("[{\"id\":2}]");
        save.setAiAssistantState("{\"active\":true}");
        save.setOfficeState("{\"buildingLevel\":3}");

        assertEquals(5L, save.getCorporationId());
        assertEquals("My Save", save.getSaveName());
        assertEquals(10000L, save.getCreditBalance());
        assertEquals("[{\"id\":1}]", save.getActiveContracts());
        assertEquals("[{\"id\":2}]", save.getCrewState());
        assertEquals("{\"active\":true}", save.getAiAssistantState());
        assertEquals("{\"buildingLevel\":3}", save.getOfficeState());
    }

    @Test
    void toString_containsFields() {
        CorporationSave save = new CorporationSave(1L, "Test Save");
        save.setCreditBalance(5000L);
        String str = save.toString();
        assertTrue(str.contains("corporationId=1"));
        assertTrue(str.contains("saveName='Test Save'"));
        assertTrue(str.contains("creditBalance=5000"));
    }
}
