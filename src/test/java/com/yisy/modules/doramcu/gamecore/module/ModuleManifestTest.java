package com.yisy.modules.doramcu.gamecore.module;

import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleManifest Tests")
class ModuleManifestTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("should parse valid manifest from JAR")
    void parseValidManifest() throws IOException {
        Path jarPath = createJarWithManifest("""
            {
              "gameType": "BABYLON_5",
              "version": "1.2.3",
              "name": "Babylon 5 RPG",
              "mechanics": ["COMBAT", "DIPLOMACY", "PSIONICS"],
              "dependencies": [],
              "mainClass": "com.yisy.modules.doramcu.babylon5.config.Babylon5GameModule"
            }
            """);

        java.util.Optional<ModuleManifest> opt = ModuleManifest.fromJar(jarPath);

        assertTrue(opt.isPresent());
        ModuleManifest manifest = opt.get();

        assertEquals(GameType.BABYLON_5, manifest.getGameType());
        assertEquals("1.2.3", manifest.getVersion());
        assertEquals("Babylon 5 RPG", manifest.getName());
        assertEquals(List.of("COMBAT", "DIPLOMACY", "PSIONICS"), manifest.getMechanics());
        assertTrue(manifest.getDependencies().isEmpty());
        assertEquals("com.yisy.modules.doramcu.babylon5.config.Babylon5GameModule", manifest.getMainClass());
    }

    @Test
    @DisplayName("should return empty for JAR without manifest")
    void missingManifest() throws IOException {
        Path jarPath = createEmptyJar();

        java.util.Optional<ModuleManifest> opt = ModuleManifest.fromJar(jarPath);

        assertTrue(opt.isEmpty());
    }

    @Test
    @DisplayName("should return empty for invalid JSON")
    void invalidJson() throws IOException {
        Path jarPath = createJarWithManifest("{ invalid json }");

        java.util.Optional<ModuleManifest> opt = ModuleManifest.fromJar(jarPath);

        assertTrue(opt.isEmpty());
    }

    @Test
    @DisplayName("should return empty for invalid game type")
    void invalidGameType() throws IOException {
        Path jarPath = createJarWithManifest("""
            {
              "gameType": "INVALID_TYPE",
              "version": "1.0.0",
              "name": "Invalid",
              "mainClass": ""
            }
            """);

        java.util.Optional<ModuleManifest> opt = ModuleManifest.fromJar(jarPath);

        assertTrue(opt.isEmpty());
    }

    @Test
    @DisplayName("should handle missing optional fields")
    void missingOptionalFields() throws IOException {
        Path jarPath = createJarWithManifest("""
            {
              "gameType": "TRAVELLER",
              "version": "0.1.0",
              "name": "Traveller",
              "mainClass": ""
            }
            """);

        java.util.Optional<ModuleManifest> opt = ModuleManifest.fromJar(jarPath);

        assertTrue(opt.isPresent());
        ModuleManifest manifest = opt.get();
        assertEquals(GameType.TRAVELLER, manifest.getGameType());
        assertEquals("0.1.0", manifest.getVersion());
        assertTrue(manifest.getMechanics().isEmpty());
        assertTrue(manifest.getDependencies().isEmpty());
    }

    @Test
    @DisplayName("should convert mechanics to MechanicType enum values")
    void getMechanicTypes() throws IOException {
        Path jarPath = createJarWithManifest("""
            {
              "gameType": "DND_5E",
              "version": "1.0.0",
              "name": "D&D 5e",
              "mechanics": ["COMBAT", "RPG", "SUPERNATURAL"],
              "mainClass": ""
            }
            """);

        ModuleManifest manifest = ModuleManifest.fromJar(jarPath).get();
        Set<MechanicType> types = manifest.getMechanicTypes();

        assertEquals(3, types.size());
        assertTrue(types.contains(MechanicType.COMBAT));
        assertTrue(types.contains(MechanicType.RPG));
        assertTrue(types.contains(MechanicType.SUPERNATURAL));
    }

    @Test
    @DisplayName("should ignore unknown mechanic types")
    void unknownMechanicTypes() throws IOException {
        Path jarPath = createJarWithManifest("""
            {
              "gameType": "DND_5E",
              "version": "1.0.0",
              "name": "D&D 5e",
              "mechanics": ["COMBAT", "UNKNOWN_MECHANIC"],
              "mainClass": ""
            }
            """);

        ModuleManifest manifest = ModuleManifest.fromJar(jarPath).get();
        Set<MechanicType> types = manifest.getMechanicTypes();

        assertEquals(1, types.size());
        assertTrue(types.contains(MechanicType.COMBAT));
    }

    @Test
    @DisplayName("should compare versions correctly")
    void compareVersions() throws IOException {
        Path jarPath = createJarWithManifest("""
            {
              "gameType": "BABYLON_5",
              "version": "1.2.3",
              "name": "Babylon 5",
              "mainClass": ""
            }
            """);

        ModuleManifest manifest = ModuleManifest.fromJar(jarPath).get();

        assertTrue(manifest.compareVersion("1.2.2") > 0, "1.2.3 should be newer than 1.2.2");
        assertTrue(manifest.compareVersion("1.2.3") == 0, "1.2.3 should equal 1.2.3");
        assertTrue(manifest.compareVersion("1.3.0") < 0, "1.2.3 should be older than 1.3.0");
        assertTrue(manifest.compareVersion("2.0.0") < 0, "1.2.3 should be older than 2.0.0");
        assertTrue(manifest.compareVersion("1.2") > 0, "1.2.3 should be newer than 1.2");
        assertTrue(manifest.compareVersion("1") > 0, "1.2.3 should be newer than 1");
    }

    @Test
    @DisplayName("should return empty for non-existent JAR")
    void nonExistentJar() {
        java.util.Optional<ModuleManifest> opt = ModuleManifest.fromJar(Path.of("/non/existent/path.jar"));
        assertTrue(opt.isEmpty());
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

    private Path createEmptyJar() throws IOException {
        Path jarPath = tempDir.resolve("empty.jar");
        try (ZipOutputStream zos = new ZipOutputStream(java.nio.file.Files.newOutputStream(jarPath))) {
            // Empty JAR
        }
        return jarPath;
    }
}
