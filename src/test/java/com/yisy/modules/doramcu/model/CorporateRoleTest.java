package com.yisy.modules.doramcu.model;

import com.yisy.modules.doramcu.model.CorporateRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporateRole Enum Tests")
class CorporateRoleTest {

    @Test
    @DisplayName("should have exactly two roles: CEO and VP")
    void roleCount() {
        assertEquals(2, CorporateRole.values().length);
    }

    @Test
    @DisplayName("CEO should identify as CEO")
    void isCEO() {
        assertTrue(CorporateRole.CEO.isCEO());
        assertFalse(CorporateRole.VP.isCEO());
    }

    @Test
    @DisplayName("VP should identify as VP")
    void isVP() {
        assertTrue(CorporateRole.VP.isVP());
        assertFalse(CorporateRole.CEO.isVP());
    }

    @Test
    @DisplayName("switchTo should toggle between CEO and VP")
    void switchTo() {
        assertEquals(CorporateRole.VP, CorporateRole.CEO.switchTo());
        assertEquals(CorporateRole.CEO, CorporateRole.VP.switchTo());
    }

    @Test
    @DisplayName("double switch should return to original role")
    void doubleSwitch() {
        CorporateRole original = CorporateRole.CEO;
        assertEquals(original, original.switchTo().switchTo());

        original = CorporateRole.VP;
        assertEquals(original, original.switchTo().switchTo());
    }

    @Test
    @DisplayName("getDisplayName should return human-readable names")
    void getDisplayName() {
        assertEquals("Chief Executive Officer", CorporateRole.CEO.getDisplayName());
        assertEquals("Vice President", CorporateRole.VP.getDisplayName());
    }

    @Test
    @DisplayName("enum values should be non-null")
    void valuesNotNull() {
        for (CorporateRole role : CorporateRole.values()) {
            assertNotNull(role);
            assertNotNull(role.getDisplayName());
        }
    }
}
