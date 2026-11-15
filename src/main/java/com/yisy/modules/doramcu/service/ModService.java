package com.yisy.modules.doramcu.service;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Modding support service for user-created game modes, rulesets, and content packs.
 * <p>
 * Provides mod discovery, loading, validation, dependency resolution, and lifecycle management.
 * Mods are organized by type (game mode, ruleset, content pack, UI theme) and can have
 * dependencies on other mods or core game modules.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Mod registration with metadata (name, version, type, author, description)</li>
 *   <li>Dependency resolution and validation</li>
 *   <li>Mod lifecycle: registered ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ validated ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ enabled ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ disabled</li>
 *   <li>Conflict detection between mods</li>
 *   <li>Mod statistics and health monitoring</li>
 *   <li>Mod configuration overrides</li>
 * </ul>
 * </p>
 */

@Service
public class ModService {

    private static final Logger log = LoggerFactory.getLogger(ModService.class);
    private static final int MAX_MODS = 100;
    private static final int MAX_DEPENDENCIES = 20;
    private static final int MAX_CONFLICTS = 10;

    /** Mod ID ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ModInfo mapping. */
    private final ConcurrentHashMap<String, ModInfo> mods = new ConcurrentHashMap<>();

    /** Enabled mod IDs for quick lookup. */
    private final Set<String> enabledMods = ConcurrentHashMap.newKeySet();

    /** Mod ID ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ configuration overrides. */
    private final ConcurrentHashMap<String, Map<String, Object>> modConfig = new ConcurrentHashMap<>();

    /** Counter for mod registration order. */
    private final AtomicLong registrationOrder = new AtomicLong(0);

    /**
     * Types of mods that can be registered.
     */
    public enum ModType {
        GAME_MODE("Game Mode", "A complete game mode with rules and mechanics"),
        RULESET("Ruleset", "Alternative rules for an existing game mode"),
        CONTENT_PACK("Content Pack", "Additional content like ships, planets, or characters"),
        UI_THEME("UI Theme", "Visual customization of the game interface"),
        AI_BEHAVIOR("AI Behavior", "Alternative AI strategies and behaviors"),
        GALAXY_TEMPLATE("Galaxy Template", "Custom galaxy generation parameters"),
        CHARACTER_CLASS("Character Class", "New character classes or archetypes"),
        EVENT_SYSTEM("Event System", "Custom event types and triggers");

        private final String name;
        private final String description;

        ModType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * Status of a mod in its lifecycle.
     */
    public enum ModStatus {
        REGISTERED("Registered", "Mod is registered but not yet validated"),
        VALIDATED("Validated", "Mod has passed validation checks"),
        ENABLED("Enabled", "Mod is active and in use"),
        DISABLED("Disabled", "Mod is registered but not active"),
        ERROR("Error", "Mod has encountered an error"),
        CONFLICT("Conflict", "Mod conflicts with another enabled mod");

        private final String name;
        private final String description;

        ModStatus(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * Validation result for a mod.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        private ValidationResult(Builder builder) {
            this.valid = builder.valid;
            this.errors = Collections.unmodifiableList(builder.errors);
            this.warnings = Collections.unmodifiableList(builder.warnings);
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public int getErrorCount() { return errors.size(); }
        public int getWarningCount() { return warnings.size(); }

        public static Builder builder() {
            return new Builder();
        }

        public static ValidationResult valid() {
            return new Builder().build();
        }

        public static ValidationResult invalid(String error) {
            return new Builder().addError(error).build();
        }

        public static class Builder {
            private boolean valid = true;
            private final List<String> errors = new ArrayList<>();
            private final List<String> warnings = new ArrayList<>();

            public Builder addError(String error) {
                errors.add(error);
                valid = false;
                return this;
            }

            public Builder addWarning(String warning) {
                warnings.add(warning);
                return this;
            }

            public Builder require(boolean condition, String error) {
                if (!condition) {
                    addError(error);
                }
                return this;
            }

            public ValidationResult build() {
                return new ValidationResult(this);
            }
        }
    }

    /**
     * Mod information with metadata and lifecycle state.
     */
    public static class ModInfo {
        private final String id;
        private final String name;
        private final String version;
        private final ModType type;
        private final String author;
        private final String description;
        private final Set<String> dependencies;
        private final Set<String> conflicts;
        private final Set<String> tags;
        private final Instant registeredAt;
        private final long registrationOrder;
        private ModStatus status;
        private Instant enabledAt;
        private Instant lastModified;
        private Map<String, Object> metadata;

        private ModInfo(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.version = builder.version;
            this.type = builder.type;
            this.author = builder.author;
            this.description = builder.description;
            this.dependencies = Collections.unmodifiableSet(new HashSet<>(builder.dependencies));
            this.conflicts = Collections.unmodifiableSet(new HashSet<>(builder.conflicts));
            this.tags = Collections.unmodifiableSet(new HashSet<>(builder.tags));
            this.registeredAt = builder.registeredAt;
            this.registrationOrder = builder.registrationOrder;
            this.status = builder.status;
            this.enabledAt = builder.enabledAt;
            this.lastModified = Instant.now();
            this.metadata = builder.metadata != null ? new HashMap<>(builder.metadata) : new HashMap<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public ModType getType() { return type; }
        public String getAuthor() { return author; }
        public String getDescription() { return description; }
        public Set<String> getDependencies() { return dependencies; }
        public Set<String> getConflicts() { return conflicts; }
        public Set<String> getTags() { return tags; }
        public Instant getRegisteredAt() { return registeredAt; }
        public long getRegistrationOrder() { return registrationOrder; }
        public ModStatus getStatus() { return status; }
        public Instant getEnabledAt() { return enabledAt; }
        public Instant getLastModified() { return lastModified; }
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }

        public void setStatus(ModStatus status) {
            this.status = status;
            this.lastModified = Instant.now();
        }

        public void setEnabledAt(Instant enabledAt) {
            this.enabledAt = enabledAt;
            this.lastModified = Instant.now();
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.lastModified = Instant.now();
        }

        public boolean dependsOn(String modId) {
            return dependencies.contains(modId);
        }

        public boolean conflictsWith(String modId) {
            return conflicts.contains(modId);
        }

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }

        public static Builder builder(String id, String name) {
            return new Builder(id, name);
        }

        public static class Builder {
            private final String id;
            private final String name;
            private String version = "1.0.0";
            private ModType type = ModType.CONTENT_PACK;
            private String author = "Unknown";
            private String description = "";
            private final Set<String> dependencies = new HashSet<>();
            private final Set<String> conflicts = new HashSet<>();
            private final Set<String> tags = new HashSet<>();
            private Instant registeredAt = Instant.now();
            private long registrationOrder = 0;
            private ModStatus status = ModStatus.REGISTERED;
            private Instant enabledAt;
            private Map<String, Object> metadata;

            public Builder(String id, String name) {
                this.id = id;
                this.name = name;
            }

            public Builder version(String version) {
                this.version = version;
                return this;
            }

            public Builder type(ModType type) {
                this.type = type;
                return this;
            }

            public Builder author(String author) {
                this.author = author;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder dependencies(Set<String> dependencies) {
                this.dependencies.addAll(dependencies);
                return this;
            }

            public Builder dependency(String dependency) {
                this.dependencies.add(dependency);
                return this;
            }

            public Builder conflicts(Set<String> conflicts) {
                this.conflicts.addAll(conflicts);
                return this;
            }

            public Builder conflict(String conflict) {
                this.conflicts.add(conflict);
                return this;
            }

            public Builder tags(Set<String> tags) {
                this.tags.addAll(tags);
                return this;
            }

            public Builder tag(String tag) {
                this.tags.add(tag);
                return this;
            }

            public Builder registeredAt(Instant registeredAt) {
                this.registeredAt = registeredAt;
                return this;
            }

            public Builder registrationOrder(long order) {
                this.registrationOrder = order;
                return this;
            }

            public Builder status(ModStatus status) {
                this.status = status;
                return this;
            }

            public Builder enabledAt(Instant enabledAt) {
                this.enabledAt = enabledAt;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public ModInfo build() {
                return new ModInfo(this);
            }
        }
    }

    /**
     * Mod summary for API responses.
     */
    public static class ModSummary {
        private final String id;
        private final String name;
        private final String version;
        private final ModType type;
        private final ModStatus status;
        private final String author;
        private final int dependencyCount;
        private final int conflictCount;

        public ModSummary(ModInfo mod) {
            this.id = mod.getId();
            this.name = mod.getName();
            this.version = mod.getVersion();
            this.type = mod.getType();
            this.status = mod.getStatus();
            this.author = mod.getAuthor();
            this.dependencyCount = mod.getDependencies().size();
            this.conflictCount = mod.getConflicts().size();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public ModType getType() { return type; }
        public ModStatus getStatus() { return status; }
        public String getAuthor() { return author; }
        public int getDependencyCount() { return dependencyCount; }
        public int getConflictCount() { return conflictCount; }
    }

    /**
     * Register a new mod.
     */
    public Optional<ModInfo> registerMod(String id, String name, ModType type, String author, String description) {
        if (mods.size() >= MAX_MODS) {
            log.warn("Maximum number of mods ({}) reached", MAX_MODS);
            return Optional.empty();
        }
        if (mods.containsKey(id)) {
            log.warn("Mod already registered: {}", id);
            return Optional.of(mods.get(id));
        }
        ModInfo mod = ModInfo.builder(id, name)
                .type(type)
                .author(author)
                .description(description)
                .registrationOrder(registrationOrder.incrementAndGet())
                .build();
        mods.put(id, mod);
        log.info("Mod registered: {} ({}) by {}", name, id, author);
        return Optional.of(mod);
    }

    /**
     * Register a mod with full metadata.
     */
    public Optional<ModInfo> registerMod(ModInfo mod) {
        if (mods.size() >= MAX_MODS) {
            log.warn("Maximum number of mods ({}) reached", MAX_MODS);
            return Optional.empty();
        }
        if (mods.containsKey(mod.getId())) {
            log.warn("Mod already registered: {}", mod.getId());
            return Optional.of(mods.get(mod.getId()));
        }
        mods.put(mod.getId(), mod);
        log.info("Mod registered: {} ({}) by {}", mod.getName(), mod.getId(), mod.getAuthor());
        return Optional.of(mod);
    }

    /**
     * Get a mod by ID.
     */
    public Optional<ModInfo> getMod(String modId) {
        return Optional.ofNullable(mods.get(modId));
    }

    /**
     * Get all mods.
     */
    public List<ModSummary> getAllMods() {
        return mods.values().stream()
                .sorted(Comparator.comparingLong(ModInfo::getRegistrationOrder))
                .map(ModSummary::new)
                .collect(Collectors.toList());
    }

    /**
     * Get mods by type.
     */
    public List<ModSummary> getModsByType(ModType type) {
        return mods.values().stream()
                .filter(mod -> mod.getType() == type)
                .sorted(Comparator.comparingLong(ModInfo::getRegistrationOrder))
                .map(ModSummary::new)
                .collect(Collectors.toList());
    }

    /**
     * Get mods by status.
     */
    public List<ModSummary> getModsByStatus(ModStatus status) {
        return mods.values().stream()
                .filter(mod -> mod.getStatus() == status)
                .sorted(Comparator.comparingLong(ModInfo::getRegistrationOrder))
                .map(ModSummary::new)
                .collect(Collectors.toList());
    }

    /**
     * Get enabled mods.
     */
    public List<ModSummary> getEnabledMods() {
        return mods.values().stream()
                .filter(mod -> mod.getStatus() == ModStatus.ENABLED)
                .sorted(Comparator.comparingLong(ModInfo::getRegistrationOrder))
                .map(ModSummary::new)
                .collect(Collectors.toList());
    }

    /**
     * Search mods by tag.
     */
    public List<ModSummary> searchModsByTag(String tag) {
        return mods.values().stream()
                .filter(mod -> mod.hasTag(tag))
                .sorted(Comparator.comparingLong(ModInfo::getRegistrationOrder))
                .map(ModSummary::new)
                .collect(Collectors.toList());
    }

    /**
     * Search mods by name (case-insensitive).
     */
    public List<ModSummary> searchModsByName(String query) {
        String lowerQuery = query.toLowerCase();
        return mods.values().stream()
                .filter(mod -> mod.getName().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparingLong(ModInfo::getRegistrationOrder))
                .map(ModSummary::new)
                .collect(Collectors.toList());
    }

    /**
     * Validate a mod's dependencies and conflicts.
     */
    public ValidationResult validateMod(String modId) {
        ValidationResult.Builder result = ValidationResult.builder();
        ModInfo mod = mods.get(modId);

        if (mod == null) {
            return ValidationResult.invalid("Mod not found: " + modId);
        }

        // Check dependencies exist
        for (String depId : mod.getDependencies()) {
            if (!mods.containsKey(depId)) {
                result.addWarning("Missing dependency: " + depId);
            }
        }

        // Check dependencies are enabled
        for (String depId : mod.getDependencies()) {
            ModInfo dep = mods.get(depId);
            if (dep != null && dep.getStatus() != ModStatus.ENABLED) {
                result.addError("Dependency not enabled: " + depId + " (status: " + dep.getStatus().getName() + ")");
            }
        }

        // Check conflicts with enabled mods
        for (String conflictId : mod.getConflicts()) {
            ModInfo conflict = mods.get(conflictId);
            if (conflict != null && conflict.getStatus() == ModStatus.ENABLED) {
                result.addError("Conflicts with enabled mod: " + conflictId);
            }
        }

        // Check dependency count
        if (mod.getDependencies().size() > MAX_DEPENDENCIES) {
            result.addWarning("Too many dependencies: " + mod.getDependencies().size());
        }

        // Check for circular dependencies
        if (hasCircularDependency(modId, new HashSet<>())) {
            result.addError("Circular dependency detected");
        }

        if (result.build().isValid()) {
            mod.setStatus(ModStatus.VALIDATED);
            log.info("Mod validated: {}", modId);
        } else {
            mod.setStatus(ModStatus.ERROR);
            log.warn("Mod validation failed: {} - {}", modId, result.build().getErrors());
        }

        return result.build();
    }

    /**
     * Enable a mod.
     */
    public boolean enableMod(String modId) {
        ModInfo mod = mods.get(modId);
        if (mod == null) {
            log.warn("Mod not found: {}", modId);
            return false;
        }

        // Validate first
        ValidationResult validation = validateMod(modId);
        if (!validation.isValid()) {
            log.warn("Cannot enable mod {}: {}", modId, validation.getErrors());
            return false;
        }

        // Check dependencies are enabled
        for (String depId : mod.getDependencies()) {
            ModInfo dep = mods.get(depId);
            if (dep == null || dep.getStatus() != ModStatus.ENABLED) {
                log.warn("Cannot enable mod {}: dependency {} not enabled", modId, depId);
                return false;
            }
        }

        // Enable the mod
        mod.setStatus(ModStatus.ENABLED);
        mod.setEnabledAt(Instant.now());
        enabledMods.add(modId);
        log.info("Mod enabled: {}", modId);
        return true;
    }

    /**
     * Disable a mod.
     */
    public boolean disableMod(String modId) {
        ModInfo mod = mods.get(modId);
        if (mod == null) {
            log.warn("Mod not found: {}", modId);
            return false;
        }

        // Check if other enabled mods depend on this one
        for (Map.Entry<String, ModInfo> entry : mods.entrySet()) {
            if (entry.getKey().equals(modId)) continue;
            ModInfo other = entry.getValue();
            if (other.getStatus() == ModStatus.ENABLED && other.dependsOn(modId)) {
                log.warn("Cannot disable mod {}: depended by {}", modId, other.getId());
                return false;
            }
        }

        mod.setStatus(ModStatus.DISABLED);
        mod.setEnabledAt(null);
        enabledMods.remove(modId);
        log.info("Mod disabled: {}", modId);
        return true;
    }

    /**
     * Set configuration override for a mod.
     */
    public void setModConfig(String modId, String key, Object value) {
        modConfig.computeIfAbsent(modId, k -> new HashMap<>());
        modConfig.get(modId).put(key, value);
        log.debug("Mod config set: {} {} = {}", modId, key, value);
    }

    /**
     * Get configuration override for a mod.
     */
    public Optional<Object> getModConfig(String modId, String key) {
        Map<String, Object> config = modConfig.get(modId);
        if (config == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.get(key));
    }

    /**
     * Get all configuration for a mod.
     */
    public Map<String, Object> getModConfig(String modId) {
        return modConfig.getOrDefault(modId, Collections.emptyMap());
    }

    /**
     * Get mod statistics.
     */
    public Map<String, Object> getModStatistics(String modId) {
        ModInfo mod = mods.get(modId);
        if (mod == null) {
            return Map.of("modId", modId, "exists", false);
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("modId", mod.getId());
        stats.put("name", mod.getName());
        stats.put("version", mod.getVersion());
        stats.put("type", mod.getType().getName());
        stats.put("status", mod.getStatus().getName());
        stats.put("author", mod.getAuthor());
        stats.put("dependencies", mod.getDependencies().size());
        stats.put("conflicts", mod.getConflicts().size());
        stats.put("tags", mod.getTags().size());
        stats.put("registeredAt", mod.getRegisteredAt().toString());
        stats.put("enabledAt", mod.getEnabledAt() != null ? mod.getEnabledAt().toString() : "null");
        stats.put("lastModified", mod.getLastModified().toString());
        stats.put("configKeys", modConfig.getOrDefault(mod.getId(), Collections.emptyMap()).size());
        return stats;
    }

    /**
     * Get global mod statistics.
     */
    public Map<String, Object> getStatistics() {
        int totalMods = mods.size();
        int enabledCount = (int) mods.values().stream()
                .filter(mod -> mod.getStatus() == ModStatus.ENABLED)
                .count();
        int disabledCount = (int) mods.values().stream()
                .filter(mod -> mod.getStatus() == ModStatus.DISABLED)
                .count();
        int errorCount = (int) mods.values().stream()
                .filter(mod -> mod.getStatus() == ModStatus.ERROR)
                .count();

        // Count by type
        Map<String, Long> byType = mods.values().stream()
                .collect(Collectors.groupingBy(m -> m.getType().getName(), Collectors.counting()));

        return Map.of(
                "totalMods", totalMods,
                "enabledMods", enabledCount,
                "disabledMods", disabledCount,
                "errorMods", errorCount,
                "byType", byType,
                "maxMods", MAX_MODS
        );
    }

    /**
     * Get all mod types.
     */
    public List<Map<String, String>> getAllModTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        for (ModType type : ModType.values()) {
            types.add(Map.of("name", type.name(), "label", type.getName(), "description", type.getDescription()));
        }
        return types;
    }

    /**
     * Get all mod statuses.
     */
    public List<Map<String, String>> getAllModStatuses() {
        List<Map<String, String>> statuses = new ArrayList<>();
        for (ModStatus status : ModStatus.values()) {
            statuses.add(Map.of("name", status.name(), "label", status.getName(), "description", status.getDescription()));
        }
        return statuses;
    }

    /**
     * Remove a mod.
     */
    public boolean removeMod(String modId) {
        ModInfo mod = mods.get(modId);
        if (mod == null) {
            return false;
        }
        // Cannot remove enabled mods
        if (mod.getStatus() == ModStatus.ENABLED) {
            log.warn("Cannot remove enabled mod: {}", modId);
            return false;
        }
        // Check if other mods depend on this one
        for (Map.Entry<String, ModInfo> entry : mods.entrySet()) {
            if (entry.getKey().equals(modId)) continue;
            if (entry.getValue().dependsOn(modId)) {
                log.warn("Cannot remove mod {}: depended by {}", modId, entry.getKey());
                return false;
            }
        }
        mods.remove(modId);
        enabledMods.remove(modId);
        modConfig.remove(modId);
        log.info("Mod removed: {}", modId);
        return true;
    }

    /**
     * Clear all mods.
     */
    public void clearAll() {
        mods.clear();
        enabledMods.clear();
        modConfig.clear();
        registrationOrder.set(0);
        log.info("All mods cleared");
    }

    /**
     * Check for circular dependencies.
     */
    private boolean hasCircularDependency(String modId, Set<String> visited) {
        if (visited.contains(modId)) {
            return true;
        }
        visited.add(modId);
        ModInfo mod = mods.get(modId);
        if (mod != null) {
            for (String depId : mod.getDependencies()) {
                if (hasCircularDependency(depId, new HashSet<>(visited))) {
                    return true;
                }
            }
        }
        return false;
    }
}
