package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleHealthStatus Tests")
class ModuleHealthStatusTest {

    @Test
    @DisplayName("should have all expected health statuses")
    void allStatuses() {
        ModuleHealthStatus[] statuses = ModuleHealthStatus.values();
        assertEquals(6, statuses.length);

        assertNotNull(ModuleHealthStatus.HEALTHY);
        assertNotNull(ModuleHealthStatus.LOADING);
        assertNotNull(ModuleHealthStatus.RELOADING);
        assertNotNull(ModuleHealthStatus.CRASHED);
        assertNotNull(ModuleHealthStatus.DISABLED);
        assertNotNull(ModuleHealthStatus.UNLOADED);
    }

    @Test
    @DisplayName("should parse health status by name")
    void valueOf() {
        assertEquals(ModuleHealthStatus.HEALTHY, ModuleHealthStatus.valueOf("HEALTHY"));
        assertEquals(ModuleHealthStatus.LOADING, ModuleHealthStatus.valueOf("LOADING"));
        assertEquals(ModuleHealthStatus.RELOADING, ModuleHealthStatus.valueOf("RELOADING"));
        assertEquals(ModuleHealthStatus.CRASHED, ModuleHealthStatus.valueOf("CRASHED"));
        assertEquals(ModuleHealthStatus.DISABLED, ModuleHealthStatus.valueOf("DISABLED"));
        assertEquals(ModuleHealthStatus.UNLOADED, ModuleHealthStatus.valueOf("UNLOADED"));
    }

    @Test
    @DisplayName("should throw for invalid status name")
    void valueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ModuleHealthStatus.valueOf("INVALID"));
    }
}
