package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ModuleHealthMonitor Tests")
class ModuleHealthMonitorTest {

    ModuleLifecycleManager lifecycleManager;
    ModuleHealthMonitor monitor;

    @BeforeEach
    void setUp() {
        lifecycleManager = mock(ModuleLifecycleManager.class);
        monitor = new ModuleHealthMonitor(lifecycleManager);
    }

    @Test
    @DisplayName("should initialize without error")
    void init() {
        assertDoesNotThrow(() -> monitor.init());
    }

    @Test
    @DisplayName("should skip health check when no modules loaded")
    void checkHealthNoModules() {
        when(lifecycleManager.getAllModuleInfo()).thenReturn(java.util.Collections.emptyMap());

        assertDoesNotThrow(() -> monitor.checkHealth());
    }

    @Test
    @DisplayName("should check health of each module")
    void checkHealthHealthy() {
        ModuleInfo info = new ModuleInfo();
        info.setGameType(GameType.BABYLON_5);
        info.setHealth(ModuleHealthStatus.HEALTHY);

        when(lifecycleManager.getAllModuleInfo()).thenReturn(
                java.util.Map.of(GameType.BABYLON_5, info));
        when(lifecycleManager.healthCheck(GameType.BABYLON_5)).thenReturn(info);

        assertDoesNotThrow(() -> monitor.checkHealth());
        verify(lifecycleManager).healthCheck(GameType.BABYLON_5);
    }

    @Test
    @DisplayName("should attempt recovery for crashed module")
    void checkHealthCrashed() {
        ModuleInfo info = new ModuleInfo();
        info.setGameType(GameType.TRAVELLER);
        info.setHealth(ModuleHealthStatus.CRASHED);
        info.setJarPath(java.nio.file.Path.of("/opt/doramcu/modules/traveller.jar"));

        when(lifecycleManager.getAllModuleInfo()).thenReturn(
                java.util.Map.of(GameType.TRAVELLER, info));
        when(lifecycleManager.healthCheck(GameType.TRAVELLER)).thenReturn(info);

        assertDoesNotThrow(() -> monitor.checkHealth());
        verify(lifecycleManager).healthCheck(GameType.TRAVELLER);
    }

    @Test
    @DisplayName("should skip disabled modules")
    void checkHealthDisabled() {
        ModuleInfo info = new ModuleInfo();
        info.setGameType(GameType.DND_5E);
        info.setHealth(ModuleHealthStatus.DISABLED);

        when(lifecycleManager.getAllModuleInfo()).thenReturn(
                java.util.Map.of(GameType.DND_5E, info));
        when(lifecycleManager.healthCheck(GameType.DND_5E)).thenReturn(info);

        assertDoesNotThrow(() -> monitor.checkHealth());
    }

    @Test
    @DisplayName("should handle health check exception")
    void checkHealthException() {
        when(lifecycleManager.getAllModuleInfo()).thenReturn(
                java.util.Map.of(GameType.BABYLON_5, new ModuleInfo()));
        when(lifecycleManager.healthCheck(GameType.BABYLON_5))
                .thenThrow(new RuntimeException("Context error"));

        assertDoesNotThrow(() -> monitor.checkHealth());
    }

    @Test
    @DisplayName("should handle null health check result")
    void checkHealthNullResult() {
        when(lifecycleManager.getAllModuleInfo()).thenReturn(
                java.util.Map.of(GameType.BABYLON_5, new ModuleInfo()));
        when(lifecycleManager.healthCheck(GameType.BABYLON_5)).thenReturn(null);

        assertDoesNotThrow(() -> monitor.checkHealth());
    }
}
