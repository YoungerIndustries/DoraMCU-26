package com.yisy.modules.doramcu.controller.admin;

import com.yisy.modules.doramcu.service.admin.ModService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for mod management.
 * <p>
 * Provides endpoints for mod registration, validation, enabling/disabling,
 * configuration, and statistics.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/mods")
public class ModController {

    private final ModService modService;

    public ModController(ModService modService) {
        this.modService = modService;
    }

    /**
     * Register a new mod.
     */
    @PostMapping
    public ResponseEntity<?> registerMod(@RequestBody Map<String, Object> request) {
        String id = (String) request.get("id");
        String name = (String) request.get("name");
        String type = (String) request.get("type");
        String author = (String) request.get("author");
        String description = (String) request.get("description");

        if (id == null || name == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "id and name are required"));
        }

        ModType modType = ModType.CONTENT_PACK;
        if (type != null) {
            try {
                modType = ModType.valueOf(type);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid type: " + type));
            }
        }

        ModInfo mod = ModInfo.builder(id, name)
                .type(modType)
                .author(author != null ? author : "Unknown")
                .description(description != null ? description : "")
                .build();

        return modService.registerMod(mod)
                .map(m -> ResponseEntity.ok(Map.of("mod", modToMap(m), "message", "Mod registered")))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Failed to register mod")));
    }

    /**
     * Get all mods.
     */
    @GetMapping
    public List<ModSummary> getAllMods() {
        return modService.getAllMods();
    }

    /**
     * Get a mod by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMod(@PathVariable String id) {
        return modService.getMod(id)
                .map(mod -> ResponseEntity.ok(modToMap(mod)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get mods by type.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getModsByType(@PathVariable String type) {
        try {
            ModType modType = ModType.valueOf(type);
            return ResponseEntity.ok(modService.getModsByType(modType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid type: " + type));
        }
    }

    /**
     * Get mods by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getModsByStatus(@PathVariable String status) {
        try {
            ModStatus modStatus = ModStatus.valueOf(status);
            return ResponseEntity.ok(modService.getModsByStatus(modStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        }
    }

    /**
     * Get enabled mods.
     */
    @GetMapping("/enabled")
    public List<ModSummary> getEnabledMods() {
        return modService.getEnabledMods();
    }

    /**
     * Search mods by tag.
     */
    @GetMapping("/search/tag/{tag}")
    public List<ModSummary> searchModsByTag(@PathVariable String tag) {
        return modService.searchModsByTag(tag);
    }

    /**
     * Search mods by name.
     */
    @GetMapping("/search/name/{query}")
    public List<ModSummary> searchModsByName(@PathVariable String query) {
        return modService.searchModsByName(query);
    }

    /**
     * Validate a mod.
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateMod(@PathVariable String id) {
        ValidationResult result = modService.validateMod(id);
        Map<String, Object> response = Map.of(
                "valid", result.isValid(),
                "errors", result.getErrors(),
                "warnings", result.getWarnings(),
                "errorCount", result.getErrorCount(),
                "warningCount", result.getWarningCount()
        );
        return result.isValid()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Enable a mod.
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<?> enableMod(@PathVariable String id) {
        if (modService.enableMod(id)) {
            return ResponseEntity.ok(Map.of("message", "Mod enabled: " + id));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to enable mod: " + id));
    }

    /**
     * Disable a mod.
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<?> disableMod(@PathVariable String id) {
        if (modService.disableMod(id)) {
            return ResponseEntity.ok(Map.of("message", "Mod disabled: " + id));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to disable mod: " + id));
    }

    /**
     * Set mod configuration.
     */
    @PostMapping("/{id}/config/{key}")
    public ResponseEntity<?> setModConfig(@PathVariable String id, @PathVariable String key,
                                          @RequestBody Object value) {
        modService.setModConfig(id, key, value);
        return ResponseEntity.ok(Map.of("message", "Config set: " + id + " " + key));
    }

    /**
     * Get mod configuration.
     */
    @GetMapping("/{id}/config")
    public Map<String, Object> getModConfig(@PathVariable String id) {
        return modService.getModConfig(id);
    }

    /**
     * Get mod configuration by key.
     */
    @GetMapping("/{id}/config/{key}")
    public ResponseEntity<?> getModConfig(@PathVariable String id, @PathVariable String key) {
        return modService.getModConfig(id, key)
                .map(value -> ResponseEntity.ok(Map.of("key", key, "value", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get mod statistics.
     */
    @GetMapping("/{id}/stats")
    public Map<String, Object> getModStatistics(@PathVariable String id) {
        return modService.getModStatistics(id);
    }

    /**
     * Get global mod statistics.
     */
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        return modService.getStatistics();
    }

    /**
     * Get all mod types.
     */
    @GetMapping("/types")
    public List<Map<String, String>> getAllModTypes() {
        return modService.getAllModTypes();
    }

    /**
     * Get all mod statuses.
     */
    @GetMapping("/statuses")
    public List<Map<String, String>> getAllModStatuses() {
        return modService.getAllModStatuses();
    }

    /**
     * Remove a mod.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeMod(@PathVariable String id) {
        if (modService.removeMod(id)) {
            return ResponseEntity.ok(Map.of("message", "Mod removed: " + id));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to remove mod: " + id));
    }

    /**
     * Clear all mods.
     */
    @DeleteMapping
    public ResponseEntity<?> clearAllMods() {
        modService.clearAll();
        return ResponseEntity.ok(Map.of("message", "All mods cleared"));
    }

    private Map<String, Object> modToMap(ModInfo mod) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", mod.getId());
        map.put("name", mod.getName());
        map.put("version", mod.getVersion());
        map.put("type", mod.getType().getName());
        map.put("status", mod.getStatus().getName());
        map.put("author", mod.getAuthor());
        map.put("description", mod.getDescription());
        map.put("dependencies", mod.getDependencies());
        map.put("conflicts", mod.getConflicts());
        map.put("tags", mod.getTags());
        map.put("registeredAt", mod.getRegisteredAt().toString());
        map.put("enabledAt", mod.getEnabledAt() != null ? mod.getEnabledAt().toString() : null);
        map.put("lastModified", mod.getLastModified().toString());
        map.put("metadata", mod.getMetadata());
        return map;
    }
}
