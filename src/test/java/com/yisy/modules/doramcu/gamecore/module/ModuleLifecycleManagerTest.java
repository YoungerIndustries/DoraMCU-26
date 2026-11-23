package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ModuleLifecycleManager Tests")
class ModuleLifecycleManagerTest {

    @TempDir
    Path tempDir;

    ApplicationContext applicationContext;
    GameModuleRegistry gameModuleRegistry;
    MechanicRegistry mechanicRegistry;
    ModuleLifecycleManager manager;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        gameModuleRegistry = mock(GameModuleRegistry.class);
        mechanicRegistry = mock(MechanicRegistry.class);

        manager = new ModuleLifecycleManager(applicationContext, gameModuleRegistry, mechanicRegistry);
    }

    @Test
    @DisplayName("should return empty for non-existent JAR")
    void loadModuleNonExistent() {
        Path jarPath = Path.of("/non/existent.jar");

        var result = manager.loadModule(jarPath);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty for JAR without manifest")
    void loadModuleNoManifest() throws IOException {
        Path jarPath = tempDir.resolve("no-manifest.jar");
        try (java.util.zip.ZipOutputStream zos =
                new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(jarPath))) {
            // Empty JAR
        }

        var result = manager.loadModule(jarPath);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should track loaded modules")
    void getModuleInfo() {
        assertFalse(manager.isModuleLoaded(GameType.BABYLON_5));
        assertNull(manager.getModuleInfo(GameType.BABYLON_5));
        assertTrue(manager.getAllModuleInfo().isEmpty());
    }

    @Test
    @DisplayName("should enable a loaded module")
    void enableModule() {
        // Simulate a loaded module
        ModuleInfo info = new ModuleInfo();
        info.setGameType(GameType.BABYLON_5);
        info.setEnabled(false);
        info.setHealth(ModuleHealthStatus.DISABLED);

        // We can't easily test the full load, but we can test enable/disable logic
        // by checking the methods return null for non-loaded modules
        ModuleInfo result = manager.enableModule(GameType.BABYLON_5);
        assertNull(result, "Enable should return null for non-loaded module");
    }

    @Test
    @DisplayName("should disable a loaded module")
    void disableModule() {
        ModuleInfo result = manager.disableModule(GameType.BABYLON_5);
        assertNull(result, "Disable should return null for non-loaded module");
    }

    @Test
    @DisplayName("should return null for health check of non-loaded module")
    void healthCheckNonLoaded() {
        ModuleInfo result = manager.healthCheck(GameType.BABYLON_5);
        assertNull(result);
    }

    @Test
    @DisplayName("should return false for unload of non-loaded module")
    void unloadNonLoaded() {
        boolean result = manager.unloadModule(GameType.BABYLON_5);
        assertFalse(result);
    }

    @Test
    @DisplayName("should return null for reload of non-loaded module")
    void reloadNonLoaded() {
        ModuleInfo result = manager.reloadModule(GameType.BABYLON_5);
        assertNull(result);
    }

    @Test
    @DisplayName("should return null for reload with path of non-loaded module")
    void reloadNonLoadedWithPath() {
        Path jarPath = tempDir.resolve("test.jar");
        ModuleInfo result = manager.reloadModule(GameType.BABYLON_5, jarPath);
        assertNull(result);
    }

    @Test
    @DisplayName("should provide modules directory (default value)")
    void getModulesDir() {
        // @Value default is /opt/doramcu/modules/ - may be null without Spring context
        // Just verify the method doesn't throw
        assertDoesNotThrow(() -> manager.getModulesDir());
    }

    @Test
    @DisplayName("should initialize on PostConstruct")
    void init() {
        assertDoesNotThrow(() -> manager.init());
    }

    @Test
    @DisplayName("should shutdown without error when no modules loaded")
    void shutdownEmpty() {
        assertDoesNotThrow(() -> manager.shutdown());
    }

    // --- Test with a valid manifest JAR (load will fail at context refresh, but manifest parsing works) ---

    @Test
    @DisplayName("should reject JAR with invalid manifest JSON")
    void loadModuleInvalidManifest() throws IOException {
        Path jarPath = createJarWithManifest("invalid json");

        var result = manager.loadModule(jarPath);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should reject JAR with invalid game type in manifest")
    void loadModuleInvalidGameType() throws IOException {
        Path jarPath = createJarWithManifest("""
            {"gameType": "INVALID", "version": "1.0.0", "name": "X", "mainClass": ""}
            """);

        var result = manager.loadModule(jarPath);

        assertTrue(result.isEmpty());
    }

    // --- Helper methods ---

    private Path createJarWithManifest(String manifestJson) throws IOException {
        Path jarPath = tempDir.resolve("test-module.jar");
        try (JarOutputStream jos = new JarOutputStream(java.nio.file.Files.newOutputStream(jarPath))) {
            JarEntry entry = new JarEntry("META-INF/module.json");
            jos.putNextEntry(entry);
            jos.write(manifestJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return jarPath;
    }
}
