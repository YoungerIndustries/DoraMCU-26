package com.yisy.modules.doramcu.unified.controller;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for cross-system rules queries.
 * <p>
 * Exposes the unified rules infrastructure via HTTP endpoints, enabling
 * frontend clients and external API consumers to search, compare, and
 * analyze rules across ALL game systems in DoraMCU.
 * </p>
 * <p>
 * This controller delegates to {@link UnifiedRulesQueryService} which
 * queries the shared JPA rules database containing entries from all
 * game systems (D&D 5e, Babylon 5, Star Wars, Traveller, etc.).
 * </p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><code>GET /api/v1/rules/search?keyword=</code> — search all systems</li>
 *   <li><code>GET /api/v1/rules/search/systems?keyword=&systems=</code> — search specific systems</li>
 *   <li><code>GET /api/v1/rules/category?category=</code> — rules by category across all systems</li>
 *   <li><code>GET /api/v1/rules/category/grouped?category=</code> — rules by category grouped by system</li>
 *   <li><code>GET /api/v1/rules/compare?mechanic=</code> — compare mechanic across systems</li>
 *   <li><code>GET /api/v1/rules/all</code> — all rules</li>
 *   <li><code>GET /api/v1/rules/all/grouped</code> — all rules grouped by system</li>
 *   <li><code>GET /api/v1/rules/categories</code> — all categories</li>
 *   <li><code>GET /api/v1/rules/categories/bySystem</code> — categories per system</li>
 *   <li><code>GET /api/v1/rules/statistics</code> — entry counts by system</li>
 *   <li><code>GET /api/v1/rules/populatedSystems</code> — systems with rules</li>
 *   <li><code>GET /api/v1/rules/summary</code> — database summary</li>
 *   <li><code>GET /api/v1/rules/source</code> — rules by source across systems</li>
 *   <li><code>GET /api/v1/rules/chapter</code> — rules by chapter across systems</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/rules")
public class UnifiedRulesController {

    private static final Logger log = LoggerFactory.getLogger(UnifiedRulesController.class);

    private final UnifiedRulesQueryService queryService;

    public UnifiedRulesController(UnifiedRulesQueryService queryService) {
        this.queryService = queryService;
    }

    // ==================== Search ====================

    /**
     * Search for rules across ALL game systems by keyword.
     * Searches title, text, and notes fields across every game system.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RulesEntry>> searchAllSystems(@RequestParam String keyword) {
        log.debug("GET /api/v1/rules/search?keyword={}", keyword);
        return ResponseEntity.ok(queryService.searchAllSystems(keyword));
    }

    /**
     * Search for rules across specific game systems by keyword.
     * Systems parameter is a comma-separated list of GameSystem names.
     */
    @GetMapping("/search/systems")
    public ResponseEntity<List<RulesEntry>> searchSystems(
            @RequestParam String keyword,
            @RequestParam(required = false) String systems) {
        log.debug("GET /api/v1/rules/search/systems?keyword={}, systems={}", keyword, systems);
        List<GameSystem> systemList = parseGameSystems(systems);
        return ResponseEntity.ok(queryService.searchSystems(keyword, systemList));
    }

    // ==================== Category Queries ====================

    /**
     * Get all rules for a specific category across ALL game systems.
     */
    @GetMapping("/category")
    public ResponseEntity<List<RulesEntry>> getRulesByCategory(@RequestParam String category) {
        log.debug("GET /api/v1/rules/category?category={}", category);
        return ResponseEntity.ok(queryService.getRulesByCategoryAcrossSystems(category));
    }

    /**
     * Get rules by category, grouped by game system.
     * Returns a map where each key is a GameSystem name and the value is the list of rules.
     */
    @GetMapping("/category/grouped")
    public ResponseEntity<Map<String, List<RulesEntry>>> getRulesByCategoryGrouped(
            @RequestParam String category) {
        log.debug("GET /api/v1/rules/category/grouped?category={}", category);
        Map<GameSystem, List<RulesEntry>> grouped = queryService.getRulesByCategoryGroupedBySystem(category);
        Map<String, List<RulesEntry>> response = new HashMap<>();
        for (Map.Entry<GameSystem, List<RulesEntry>> entry : grouped.entrySet()) {
            response.put(entry.getKey().name(), entry.getValue());
        }
        return ResponseEntity.ok(response);
    }

    // ==================== Rules Comparison ====================

    /**
     * Compare how different game systems handle a similar mechanic.
     * Returns a map of game system to matching rules entries.
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, List<RulesEntry>>> compareMechanic(@RequestParam String mechanic) {
        log.debug("GET /api/v1/rules/compare?mechanic={}", mechanic);
        Map<GameSystem, List<RulesEntry>> comparison = queryService.compareMechanicAcrossSystems(mechanic);
        Map<String, List<RulesEntry>> response = new HashMap<>();
        for (Map.Entry<GameSystem, List<RulesEntry>> entry : comparison.entrySet()) {
            response.put(entry.getKey().name(), entry.getValue());
        }
        return ResponseEntity.ok(response);
    }

    // ==================== All-System Retrieval ====================

    /**
     * Get ALL active rules entries across all game systems.
     */
    @GetMapping("/all")
    public ResponseEntity<List<RulesEntry>> getAllRules() {
        log.debug("GET /api/v1/rules/all");
        return ResponseEntity.ok(queryService.getAllRules());
    }

    /**
     * Get all rules entries grouped by game system.
     * Returns a map where each key is a GameSystem name.
     */
    @GetMapping("/all/grouped")
    public ResponseEntity<Map<String, List<RulesEntry>>> getAllRulesGrouped() {
        log.debug("GET /api/v1/rules/all/grouped");
        Map<GameSystem, List<RulesEntry>> grouped = queryService.getAllRulesGroupedBySystem();
        Map<String, List<RulesEntry>> response = new HashMap<>();
        for (Map.Entry<GameSystem, List<RulesEntry>> entry : grouped.entrySet()) {
            response.put(entry.getKey().name(), entry.getValue());
        }
        return ResponseEntity.ok(response);
    }

    // ==================== Metadata and Statistics ====================

    /**
     * Get all distinct categories across all game systems.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.debug("GET /api/v1/rules/categories");
        return ResponseEntity.ok(queryService.getAllCategories());
    }

    /**
     * Get all distinct categories grouped by game system.
     * Returns a map of game system name to its list of categories.
     */
    @GetMapping("/categories/bySystem")
    public ResponseEntity<Map<String, List<String>>> getCategoriesBySystem() {
        log.debug("GET /api/v1/rules/categories/bySystem");
        Map<GameSystem, List<String>> grouped = queryService.getCategoriesBySystem();
        Map<String, List<String>> response = new HashMap<>();
        for (Map.Entry<GameSystem, List<String>> entry : grouped.entrySet()) {
            response.put(entry.getKey().name(), entry.getValue());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get a summary of rules entry counts per game system.
     * Returns a map of game system name to entry count.
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Integer>> getStatistics() {
        log.debug("GET /api/v1/rules/statistics");
        Map<GameSystem, Integer> counts = queryService.getEntryCountsBySystem();
        Map<String, Integer> response = new HashMap<>();
        for (Map.Entry<GameSystem, Integer> entry : counts.entrySet()) {
            response.put(entry.getKey().name(), entry.getValue());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get the list of game systems that have populated rules entries.
     */
    @GetMapping("/populatedSystems")
    public ResponseEntity<List<String>> getPopulatedSystems() {
        log.debug("GET /api/v1/rules/populatedSystems");
        List<GameSystem> systems = queryService.getPopulatedSystems();
        List<String> names = systems.stream().map(GameSystem::name).toList();
        return ResponseEntity.ok(names);
    }

    /**
     * Get a comprehensive summary of the entire rules database.
     * Returns total entries, populated systems, categories, sources, and entries by system.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDatabaseSummary() {
        log.debug("GET /api/v1/rules/summary");
        return ResponseEntity.ok(queryService.getDatabaseSummary());
    }

    // ==================== Source and Chapter Queries ====================

    /**
     * Get all rules from a specific source book across all game systems.
     */
    @GetMapping("/source")
    public ResponseEntity<List<RulesEntry>> getRulesBySource(@RequestParam String source) {
        log.debug("GET /api/v1/rules/source?source={}", source);
        return ResponseEntity.ok(queryService.getRulesBySourceAcrossSystems(source));
    }

    /**
     * Get all rules from a specific chapter across all game systems.
     */
    @GetMapping("/chapter")
    public ResponseEntity<List<RulesEntry>> getRulesByChapter(@RequestParam String chapter) {
        log.debug("GET /api/v1/rules/chapter?chapter={}", chapter);
        return ResponseEntity.ok(queryService.getRulesByChapterAcrossSystems(chapter));
    }

    // ==================== Utility Methods ====================

    /**
     * Parse comma-separated GameSystem names into a list.
     * Returns null if the string is null or empty (meaning search all systems).
     */
    private List<GameSystem> parseGameSystems(String systemsParam) {
        if (systemsParam == null || systemsParam.trim().isEmpty()) {
            return null;
        }
        return List.of(systemsParam.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return GameSystem.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown GameSystem: {}", s);
                        return null;
                    }
                })
                .filter(s -> s != null)
                .toList();
    }
}
