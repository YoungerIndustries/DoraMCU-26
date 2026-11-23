package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicRegistry;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ModuleWatcher Tests")
class ModuleWatcherTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("should not start when watch is disabled")
    void disabledWatcher() {
        ModuleLifecycleManager lifecycleManager = mock(ModuleLifecycleManager.class);
        ModuleWatcher watcher = new ModuleWatcher(lifecycleManager);
        watcher.setWatchEnabled(false);

        // Should not throw even if directory doesn't exist
        assertDoesNotThrow(() -> watcher.start());
        assertDoesNotThrow(() -> watcher.stop());
    }

    @Test
    @DisplayName("should not crash when modules directory does not exist")
    void missingDirectory() {
        ModuleLifecycleManager lifecycleManager = mock(ModuleLifecycleManager.class);
        ModuleWatcher watcher = new ModuleWatcher(lifecycleManager);
        watcher.setWatchEnabled(true);
        watcher.setModulesDir("/non/existent/path/");

        // Should log a warning and not crash
        assertDoesNotThrow(() -> watcher.start());
        assertDoesNotThrow(() -> watcher.stop());
    }

    @Test
    @DisplayName("should start and stop without error when directory exists")
    void startAndStop() throws Exception {
        ModuleLifecycleManager lifecycleManager = mock(ModuleLifecycleManager.class);
        ModuleWatcher watcher = new ModuleWatcher(lifecycleManager);
        watcher.setWatchEnabled(true);
        watcher.setModulesDir(tempDir.toString());

        assertDoesNotThrow(() -> watcher.start());
        // Give it a moment to initialize
        Thread.sleep(100);
        assertDoesNotThrow(() -> watcher.stop());
    }

    @Test
    @DisplayName("should not trigger load for non-JAR files")
    void nonJarFile() throws Exception {
        ModuleLifecycleManager lifecycleManager = mock(ModuleLifecycleManager.class);
        ModuleWatcher watcher = new ModuleWatcher(lifecycleManager);
        watcher.setWatchEnabled(true);
        watcher.setModulesDir(tempDir.toString());

        watcher.start();

        // Create a non-JAR file
        Path txtFile = tempDir.resolve("test.txt");
        java.nio.file.Files.writeString(txtFile, "hello");

        // Wait for debounce
        Thread.sleep(1000);

        watcher.stop();

        // No module should be loaded
        verify(lifecycleManager, never()).loadModule(any(Path.class));
    }

    // Note: ModuleWatcher uses @Value annotations, so we need setters for testing
    // We'll add package-private setters to ModuleWatcher for testability
}
