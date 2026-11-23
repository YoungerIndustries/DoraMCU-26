package com.yisy.modules.doramcu.gamecore.module;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Set;

/**
 * Parses {@code META-INF/module.json} from a module JAR to extract metadata
 * without loading the module's classes.
 * <p>
 * The manifest contains the module's game type, version, name, mechanics,
 * dependencies, and main class. This enables version comparison and dependency
 * resolution before the module is actually loaded.
 * </p>
 */
public class ModuleManifest {

    private static final Logger log = LoggerFactory.getLogger(ModuleManifest.class);
    private static final String MANIFEST_PATH = "META-INF/module.json";

    private final GameType gameType;
    private final String version;
    private final String name;
    private final List<String> mechanics;
    private final List<String> dependencies;
    private final String mainClass;

    public ModuleManifest(GameType gameType, String version, String name,
                          List<String> mechanics, List<String> dependencies, String mainClass) {
        this.gameType = gameType;
        this.version = version;
        this.name = name;
        this.mechanics = mechanics != null ? mechanics : new ArrayList<>();
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
        this.mainClass = mainClass;
    }

    /**
     * Parse the module manifest from a JAR file.
     *
     * @param jarPath path to the module JAR
     * @return the parsed manifest, or empty if the JAR has no valid manifest
     */
    public static java.util.Optional<ModuleManifest> fromJar(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(MANIFEST_PATH);
            if (entry == null) {
                log.warn("No module manifest found in {}: {}", jarPath, MANIFEST_PATH);
                return java.util.Optional.empty();
            }

            try (InputStream is = jarFile.getInputStream(entry)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);
                return parse(root);
            }
        } catch (IOException e) {
            log.error("Failed to read module manifest from {}: {}", jarPath, e.getMessage());
            return java.util.Optional.empty();
        }
    }

    /**
     * Parse a JSON node into a ModuleManifest.
     */
    private static java.util.Optional<ModuleManifest> parse(JsonNode root) {
        try {
            String gameTypeStr = root.get("gameType").asText();
            GameType gameType = GameType.valueOf(gameTypeStr);

            String version = root.get("version").asText("");
            String name = root.get("name").asText("");
            String mainClass = root.get("mainClass").asText("");

            List<String> mechanics = new ArrayList<>();
            if (root.has("mechanics") && root.get("mechanics").isArray()) {
                for (JsonNode node : root.get("mechanics")) {
                    mechanics.add(node.asText());
                }
            }

            List<String> dependencies = new ArrayList<>();
            if (root.has("dependencies") && root.get("dependencies").isArray()) {
                for (JsonNode node : root.get("dependencies")) {
                    dependencies.add(node.asText());
                }
            }

            return java.util.Optional.of(new ModuleManifest(gameType, version, name, mechanics, dependencies, mainClass));
        } catch (Exception e) {
            log.error("Failed to parse module manifest: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    /**
     * Convert the mechanics list strings to MechanicType enum values.
     */
    public Set<MechanicType> getMechanicTypes() {
        Set<MechanicType> result = new java.util.HashSet<>();
        for (String mech : mechanics) {
            try {
                result.add(MechanicType.valueOf(mech));
            } catch (IllegalArgumentException ignored) {
                log.warn("Unknown mechanic type: {}", mech);
            }
        }
        return result;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public List<String> getMechanics() {
        return mechanics;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public String getMainClass() {
        return mainClass;
    }

    /**
     * Compare two versions using simple semantic versioning.
     *
     * @return negative if this version is older, positive if newer, 0 if equal
     */
    public int compareVersion(String otherVersion) {
        return compareVersions(this.version, otherVersion);
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int len = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < len; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }
}
