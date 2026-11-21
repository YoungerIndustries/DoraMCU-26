package com.yisy.modules.doramcu.mechwarrior;
import com.yisy.modules.doramcu.mechwarrior.MechWarriorRulesService.MechWarriorRulesSummary;

import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for browsing and searching the MechWarrior / BattleTech rules database.
 * <p>
 * Provides endpoints for querying rule entries and reference tables covering:
 * <ul>
 *   <li>Inner Sphere lore — Great Houses, Succession Wars, the Star League, the Clans</li>
 *   <li>Core game mechanics — 2D6 system, initiative, movement, combat resolution</li>
 *   <li>Combat rules — To-Hit numbers, hit locations, armor, damage, critical hits</li>
 *   <li>Heat system — heat generation, shutdown thresholds, heat sink capacity</li>
 *   <li>MechWarrior pilot rules — skills, consciousness, experience levels</li>
 *   <li>Weapon systems — energy, ballistic, and missile weapons</li>
 *   <li>Mech classifications — light, medium, heavy, and assault BattleMechs</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/mechwarrior/rules")
public class MechWarriorRulesController {

    private static final Logger log = LoggerFactory.getLogger(MechWarriorRulesController.class);

    private final MechWarriorRulesService mechWarriorRulesService;

    public MechWarriorRulesController(MechWarriorRulesService mechWarriorRulesService) {
        this.mechWarriorRulesService = mechWarriorRulesService;
    }

    // ==================== Summary ====================

    /**
     * Gets a summary of all rules and tables in the MechWarrior rules database.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRulesSummary() {
        log.debug("GET /api/v1/mechwarrior/rules/summary");
        MechWarriorRulesSummary summary = mechWarriorRulesService.getSummary();

        Map<String, Object> response = new HashMap<>();
        response.put("totalEntries", summary.totalEntries);
        response.put("totalTables", summary.totalTables);
        response.put("categories", summary.categories);
        response.put("sources", summary.sources);
        response.put("chapters", summary.chapters);
        response.put("tableCategories", summary.tableCategories);
        return ResponseEntity.ok(response);
    }

    // ==================== Rule Entries ====================

    /**
     * Gets all rule entries for a given category (e.g., "CORE_MECHANICS", "COMBAT", "WEAPONS").
     */
    @GetMapping("/category")
    public ResponseEntity<List<RulesEntry>> getRulesByCategory(@RequestParam String category) {
        log.debug("GET /api/v1/mechwarrior/rules/category?category={}", category);
        List<RulesEntry> rules = mechWarriorRulesService.getRulesByCategory(category);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets all rule entries from a given source (e.g., "BattleTech Rulebook", "MechWarrior Lore").
     */
    @GetMapping("/source")
    public ResponseEntity<List<RulesEntry>> getRulesBySource(@RequestParam String source) {
        log.debug("GET /api/v1/mechwarrior/rules/source?source={}", source);
        return ResponseEntity.ok(mechWarriorRulesService.getRulesBySource(source));
    }

    /**
     * Gets all rule entries from a given chapter (e.g., "Core Rules", "Combat", "Movement").
     */
    @GetMapping("/chapter")
    public ResponseEntity<List<RulesEntry>> getRulesByChapter(@RequestParam String chapter) {
        log.debug("GET /api/v1/mechwarrior/rules/chapter?chapter={}", chapter);
        return ResponseEntity.ok(mechWarriorRulesService.getRulesByChapter(chapter));
    }

    /**
     * Searches rule entries by keyword in title.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RulesEntry>> searchRules(@RequestParam String keyword) {
        log.debug("GET /api/v1/mechwarrior/rules/search?keyword={}", keyword);
        return ResponseEntity.ok(mechWarriorRulesService.searchRules(keyword));
    }

    /**
     * Searches rule entries by keyword in text content.
     */
    @GetMapping("/search/text")
    public ResponseEntity<List<RulesEntry>> searchRulesByText(@RequestParam String keyword) {
        log.debug("GET /api/v1/mechwarrior/rules/search/text?keyword={}", keyword);
        return ResponseEntity.ok(mechWarriorRulesService.searchRulesByText(keyword));
    }

    /**
     * Gets all available rule categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.debug("GET /api/v1/mechwarrior/rules/categories");
        return ResponseEntity.ok(mechWarriorRulesService.getCategories());
    }

    /**
     * Gets all available source books.
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        log.debug("GET /api/v1/mechwarrior/rules/sources");
        return ResponseEntity.ok(mechWarriorRulesService.getSources());
    }

    /**
     * Gets all available chapters.
     */
    @GetMapping("/chapters")
    public ResponseEntity<List<String>> getChapters() {
        log.debug("GET /api/v1/mechwarrior/rules/chapters");
        return ResponseEntity.ok(mechWarriorRulesService.getChapters());
    }

    /**
     * Gets all active rule entries for MechWarrior.
     */
    @GetMapping
    public ResponseEntity<List<RulesEntry>> getAllRules() {
        log.debug("GET /api/v1/mechwarrior/rules");
        return ResponseEntity.ok(mechWarriorRulesService.getAllRules());
    }

    // ==================== Reference Tables ====================

    /**
     * Gets all table entries for a given table name (e.g., "Hit Location Table").
     * If no name is specified, returns all table entries.
     */
    @GetMapping("/tables")
    public ResponseEntity<List<RulesTable>> getTableEntries(@RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            log.debug("GET /api/v1/mechwarrior/rules/tables?name={}", name);
            return ResponseEntity.ok(mechWarriorRulesService.getTableEntries(name));
        } else {
            log.debug("GET /api/v1/mechwarrior/rules/tables (all)");
            return ResponseEntity.ok(mechWarriorRulesService.getAllTableEntries());
        }
    }

    /**
     * Gets all table entries for a given category (e.g., "Combat", "Movement").
     */
    @GetMapping("/tables/category")
    public ResponseEntity<List<RulesTable>> getTableEntriesByCategory(@RequestParam String category) {
        log.debug("GET /api/v1/mechwarrior/rules/tables/category?category={}", category);
        return ResponseEntity.ok(mechWarriorRulesService.getTableEntriesByCategory(category));
    }

    /**
     * Gets all available table categories.
     */
    @GetMapping("/tables/categories")
    public ResponseEntity<List<String>> getTableCategories() {
        log.debug("GET /api/v1/mechwarrior/rules/tables/categories");
        return ResponseEntity.ok(mechWarriorRulesService.getAllTableCategories());
    }
}
