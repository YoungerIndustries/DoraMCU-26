package com.yisy.modules.doramcu.gamecore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MechanicRegistry Hot-Swap Tests")
class MechanicRegistryTest {

    MechanicRegistry registry;
    GameModuleRegistry gameModuleRegistry;

    @BeforeEach
    void setUp() {
        gameModuleRegistry = mock(GameModuleRegistry.class);
        registry = new MechanicRegistry();
        registry.setGameModuleRegistry(gameModuleRegistry);
    }

    @Test
    @DisplayName("should index a module")
    void indexModule() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT, MechanicType.DIPLOMACY));

        registry.indexModule(module);

        List<GameModule> combatModules = registry.getModulesForMechanic(MechanicType.COMBAT);
        List<GameModule> diplomacyModules = registry.getModulesForMechanic(MechanicType.DIPLOMACY);

        assertEquals(1, combatModules.size());
        assertEquals(module, combatModules.get(0));
        assertEquals(1, diplomacyModules.size());
        assertEquals(module, diplomacyModules.get(0));
    }

    @Test
    @DisplayName("should unindex a module")
    void unindexModule() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT, MechanicType.DIPLOMACY));

        registry.indexModule(module);
        registry.unindexModule(module);

        assertTrue(registry.getModulesForMechanic(MechanicType.COMBAT).isEmpty());
        assertTrue(registry.getModulesForMechanic(MechanicType.DIPLOMACY).isEmpty());
    }

    @Test
    @DisplayName("should handle null module in unindex")
    void unindexNull() {
        assertDoesNotThrow(() -> registry.unindexModule(null));
    }

    @Test
    @DisplayName("should handle module with null mechanics")
    void indexModuleNullMechanics() {
        GameModule module = mock(GameModule.class);
        when(module.getGameType()).thenReturn(GameType.TRAVELLER);
        when(module.getModuleName()).thenReturn("Traveller");
        when(module.getMechanics()).thenReturn(null);

        assertDoesNotThrow(() -> registry.indexModule(module));
        assertTrue(registry.getAllMechanics().isEmpty());
    }

    @Test
    @DisplayName("should index multiple modules for same mechanic")
    void indexMultipleModules() {
        GameModule module1 = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT));
        GameModule module2 = createMockModule(GameType.DND_5E, "D&D 5e",
                Set.of(MechanicType.COMBAT, MechanicType.SUPERNATURAL));

        registry.indexModule(module1);
        registry.indexModule(module2);

        List<GameModule> combatModules = registry.getModulesForMechanic(MechanicType.COMBAT);
        assertEquals(2, combatModules.size());

        List<GameModule> supernaturalModules = registry.getModulesForMechanic(MechanicType.SUPERNATURAL);
        assertEquals(1, supernaturalModules.size());
    }

    @Test
    @DisplayName("should remove only the specified module on unindex")
    void unindexOneModule() {
        GameModule module1 = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT));
        GameModule module2 = createMockModule(GameType.DND_5E, "D&D 5e",
                Set.of(MechanicType.COMBAT));

        registry.indexModule(module1);
        registry.indexModule(module2);
        registry.unindexModule(module1);

        List<GameModule> combatModules = registry.getModulesForMechanic(MechanicType.COMBAT);
        assertEquals(1, combatModules.size());
        assertEquals(module2, combatModules.get(0));
    }

    @Test
    @DisplayName("should clean up empty mechanic entries on unindex")
    void unindexCleanup() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.SUPERNATURAL));

        registry.indexModule(module);
        assertTrue(registry.hasMechanic(MechanicType.SUPERNATURAL));

        registry.unindexModule(module);
        assertFalse(registry.hasMechanic(MechanicType.SUPERNATURAL));
    }

    @Test
    @DisplayName("should return empty list for unknown mechanic")
    void getModulesForUnknownMechanic() {
        List<GameModule> modules = registry.getModulesForMechanic(MechanicType.ECONOMY);
        assertNotNull(modules);
        assertTrue(modules.isEmpty());
    }

    @Test
    @DisplayName("should return empty optional for unknown mechanic")
    void getModuleForUnknownMechanic() {
        Optional<GameModule> module = registry.getModuleForMechanic(MechanicType.ECONOMY);
        assertTrue(module.isEmpty());
    }

    @Test
    @DisplayName("should return first module for mechanic")
    void getModuleForMechanic() {
        GameModule module1 = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT));
        GameModule module2 = createMockModule(GameType.DND_5E, "D&D 5e",
                Set.of(MechanicType.COMBAT));

        registry.indexModule(module1);
        registry.indexModule(module2);

        Optional<GameModule> module = registry.getModuleForMechanic(MechanicType.COMBAT);
        assertTrue(module.isPresent());
        assertEquals(module1, module.get());
    }

    @Test
    @DisplayName("should track all mechanics")
    void getAllMechanics() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT, MechanicType.DIPLOMACY, MechanicType.EXPLORATION));

        registry.indexModule(module);

        Set<MechanicType> allMechanics = registry.getAllMechanics();
        assertEquals(3, allMechanics.size());
        assertTrue(allMechanics.contains(MechanicType.COMBAT));
        assertTrue(allMechanics.contains(MechanicType.DIPLOMACY));
        assertTrue(allMechanics.contains(MechanicType.EXPLORATION));
    }

    @Test
    @DisplayName("should provide mechanic summary")
    void getMechanicSummary() {
        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT, MechanicType.DIPLOMACY));

        registry.indexModule(module);

        var summary = registry.getMechanicSummary();
        assertEquals(2, summary.size());
        assertTrue(summary.containsKey(MechanicType.COMBAT));
        assertTrue(summary.containsKey(MechanicType.DIPLOMACY));
        assertEquals(List.of("Babylon 5"), summary.get(MechanicType.COMBAT));
    }

    @Test
    @DisplayName("should check hasMechanic")
    void hasMechanic() {
        assertFalse(registry.hasMechanic(MechanicType.COMBAT));

        GameModule module = createMockModule(GameType.BABYLON_5, "Babylon 5",
                Set.of(MechanicType.COMBAT));
        registry.indexModule(module);

        assertTrue(registry.hasMechanic(MechanicType.COMBAT));
        assertFalse(registry.hasMechanic(MechanicType.ECONOMY));
    }

    // --- Helper ---

    private GameModule createMockModule(GameType gameType, String name, Set<MechanicType> mechanics) {
        GameModule module = mock(GameModule.class);
        when(module.getGameType()).thenReturn(gameType);
        when(module.getModuleName()).thenReturn(name);
        when(module.getMechanics()).thenReturn(mechanics);
        return module;
    }
}
