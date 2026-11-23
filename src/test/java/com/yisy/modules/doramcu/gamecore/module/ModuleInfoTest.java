package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleInfo Tests")
class ModuleInfoTest {

    @Test
    @DisplayName("should set and get all fields")
    void settersAndGetters() {
        ModuleInfo info = new ModuleInfo();

        info.setGameType(GameType.BABYLON_5);
        info.setVersion("1.2.3");
        info.setName("Babylon 5 RPG");
        info.setJarPath(Path.of("/opt/doramcu/modules/babylon5.jar"));
        info.setEnabled(true);
        info.setHealth(ModuleHealthStatus.HEALTHY);
        info.setLastReloadTime(1000L);

        assertEquals(GameType.BABYLON_5, info.getGameType());
        assertEquals("1.2.3", info.getVersion());
        assertEquals("Babylon 5 RPG", info.getName());
        assertEquals(Path.of("/opt/doramcu/modules/babylon5.jar"), info.getJarPath());
        assertTrue(info.isEnabled());
        assertEquals(ModuleHealthStatus.HEALTHY, info.getHealth());
        assertEquals(1000L, info.getLastReloadTime());
    }

    @Test
    @DisplayName("should default to null and false")
    void defaults() {
        ModuleInfo info = new ModuleInfo();

        assertNull(info.getGameType());
        assertNull(info.getVersion());
        assertNull(info.getName());
        assertNull(info.getJarPath());
        assertFalse(info.isEnabled());
        assertNull(info.getHealth());
        assertEquals(0L, info.getLastReloadTime());
        assertNull(info.getClassLoader());
        assertNull(info.getSpringContext());
        assertNull(info.getGameModule());
    }

    @Test
    @DisplayName("should include key fields in toString")
    void toStringOutput() {
        ModuleInfo info = new ModuleInfo();
        info.setGameType(GameType.TRAVELLER);
        info.setVersion("0.6.2");
        info.setName("Traveller");
        info.setEnabled(false);
        info.setHealth(ModuleHealthStatus.DISABLED);

        String str = info.toString();

        assertTrue(str.contains("TRAVELLER"));
        assertTrue(str.contains("0.6.2"));
        assertTrue(str.contains("Traveller"));
        assertTrue(str.contains("false"));
        assertTrue(str.contains("DISABLED"));
    }

    @Test
    @DisplayName("should support all health statuses")
    void healthStatuses() {
        ModuleInfo info = new ModuleInfo();

        info.setHealth(ModuleHealthStatus.HEALTHY);
        assertEquals(ModuleHealthStatus.HEALTHY, info.getHealth());

        info.setHealth(ModuleHealthStatus.LOADING);
        assertEquals(ModuleHealthStatus.LOADING, info.getHealth());

        info.setHealth(ModuleHealthStatus.RELOADING);
        assertEquals(ModuleHealthStatus.RELOADING, info.getHealth());

        info.setHealth(ModuleHealthStatus.CRASHED);
        assertEquals(ModuleHealthStatus.CRASHED, info.getHealth());

        info.setHealth(ModuleHealthStatus.DISABLED);
        assertEquals(ModuleHealthStatus.DISABLED, info.getHealth());

        info.setHealth(ModuleHealthStatus.UNLOADED);
        assertEquals(ModuleHealthStatus.UNLOADED, info.getHealth());
    }
}
