package com.yisy.modules.doramcu.gamecore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GameModuleRegistry Hot-Swap Tests")
class GameModuleRegistryTest {

    GameModuleRegistry registry;
    MechanicRegistry mechanicRegistry;

    @BeforeEach
    void setUp() {
        mechanicRegistry = mock(MechanicRegistry.class);
        registry = new GameModuleRegistry();
        registry.setMechanicRegistry(mechanicRegistry);
    }

    @Test
    @DisplayName("should register a module")
    void registerModule() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5");

        registry.registerModule(module);

        assertEquals(1, registry.getModulesMap().size());
        assertEquals(module, registry.getModulesMap().get(GameType.BABYLON_5));
    }

    @Test
    @DisplayName("should unregister a module")
    void unregisterModule() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5");
        registry.registerModule(module);

        registry.unregisterModule(GameType.BABYLON_5);

        assertEquals(0, registry.getModulesMap().size());
        verify(mechanicRegistry).rebuildIndex();
    }

    @Test
    @DisplayName("should do nothing when unregistering non-existent module")
    void unregisterNonExistent() {
        registry.unregisterModule(GameType.BABYLON_5);

        assertEquals(0, registry.getModulesMap().size());
        verify(mechanicRegistry, never()).rebuildIndex();
    }

    @Test
    @DisplayName("should reload a module")
    void reloadModule() {
        GameModule oldModule = createMockModule(GameType.BABYLON_5, "Babylon 5 Old");
        registry.registerModule(oldModule);

        GameModule newModule = createMockModule(GameType.BABYLON_5, "Babylon 5 New");
        registry.reloadModule(GameType.BABYLON_5, newModule);

        assertEquals(1, registry.getModulesMap().size());
        assertEquals(newModule, registry.getModulesMap().get(GameType.BABYLON_5));
        verify(mechanicRegistry).rebuildIndex();
    }

    @Test
    @DisplayName("should enable a module")
    void enableModule() {
        GameModule module = createMockModule(GameType.TRAVELLER, "Traveller");
        registry.registerModule(module);

        assertDoesNotThrow(() -> registry.enableModule(GameType.TRAVELLER, true));
    }

    @Test
    @DisplayName("should do nothing when enabling non-existent module")
    void enableNonExistent() {
        assertDoesNotThrow(() -> registry.enableModule(GameType.TRAVELLER, true));
    }

    @Test
    @DisplayName("should return modules map")
    void getModulesMap() {
        GameModule module1 = createMockModule(GameType.BABYLON_5, "Babylon 5");
        GameModule module2 = createMockModule(GameType.TRAVELLER, "Traveller");

        registry.registerModule(module1);
        registry.registerModule(module2);

        assertEquals(2, registry.getModulesMap().size());
    }

    @Test
    @DisplayName("should handle multiple register/unregister cycles")
    void multipleCycles() {
        GameModule module1 = createMockModule(GameType.BABYLON_5, "Babylon 5");
        GameModule module2 = createMockModule(GameType.TRAVELLER, "Traveller");

        registry.registerModule(module1);
        registry.registerModule(module2);
        assertEquals(2, registry.getModulesMap().size());

        registry.unregisterModule(GameType.BABYLON_5);
        assertEquals(1, registry.getModulesMap().size());

        GameModule module3 = createMockModule(GameType.DND_5E, "D&D 5e");
        registry.registerModule(module3);
        assertEquals(2, registry.getModulesMap().size());
    }

    // --- Helper ---

    private GameModule createMockModule(GameType gameType, String name) {
        GameModule module = mock(GameModule.class);
        when(module.getGameType()).thenReturn(gameType);
        when(module.getModuleName()).thenReturn(name);
        when(module.getMechanics()).thenReturn(Set.of(MechanicType.COMBAT, MechanicType.RPG));
        return module;
    }
}
