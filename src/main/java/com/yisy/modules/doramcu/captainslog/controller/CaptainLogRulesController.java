package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log rules database endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides access to rules entries, categories, chapters, and oracle tables.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogRulesController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogRulesController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogRulesController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Gets a summary of all rules and tables in the rules database.
     * <p>
     * Returns the list of categories, chapters, table names, and total counts.
     * Useful for AI context to understand what rules are available.
     * </p>
     */
    @GetMapping("/rules/summary")
    public ResponseEntity<Map<String, Object>> getRulesSummary() {
        log.debug("GET /api/v1/captains-log/rules/summary");
        Map<String, Object> summary = captainLogService.getRulesSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Gets all rule entries for a specific category.
     * <p>
     * Categories include: DICE_MECHANICS, MOMENTUM, THREAT, COMBAT, MISSION_STRUCTURE,
     * STARSHIP, ORACLE, CHARACTER_CREATION.
     * </p>
     *
     * @param category the rule category to query
     */
    @GetMapping("/rules/category")
    public ResponseEntity<List<com.yisy.modules.doramcu.shared.model.RulesEntry>> getRulesByCategory(
            @RequestParam String category) {
        log.debug("GET /api/v1/captains-log/rules/category?category={}", category);
        List<com.yisy.modules.doramcu.shared.model.RulesEntry> rules =
                captainLogService.getRulesService().getRulesByCategory(category);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets all rule entries for a specific chapter.
     * <p>
     * Chapters include: "05.00 Rules of Play", "06.00 Playing Captain's Log",
     * "03.00 Reporting for Duty", "04.00 Your Home Among the Stars".
     * </p>
     *
     * @param chapter the rulebook chapter to query
     */
    @GetMapping("/rules/chapter")
    public ResponseEntity<List<com.yisy.modules.doramcu.shared.model.RulesEntry>> getRulesByChapter(
            @RequestParam String chapter) {
        log.debug("GET /api/v1/captains-log/rules/chapter?chapter={}", chapter);
        List<com.yisy.modules.doramcu.shared.model.RulesEntry> rules =
                captainLogService.getRulesService().getRulesByChapter(chapter);
        return ResponseEntity.ok(rules);
    }

    /**
     * Searches rules by keyword across all categories.
     * <p>
     * Searches both the title and text of rule entries.
     * </p>
     *
     * @param keyword the search keyword
     */
    @GetMapping("/rules/search")
    public ResponseEntity<List<com.yisy.modules.doramcu.shared.model.RulesEntry>> searchRules(
            @RequestParam String keyword) {
        log.debug("GET /api/v1/captains-log/rules/search?keyword={}", keyword);
        List<com.yisy.modules.doramcu.shared.model.RulesEntry> rules =
                captainLogService.getRulesService().searchRules(keyword);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets rules relevant to a specific game action.
     * <p>
     * Returns the most relevant rules based on keywords in the action description.
     * For example, "roll a combat check" returns dice mechanics and combat rules.
     * </p>
     *
     * @param action a description of the game action (e.g., "roll combat check", "spend momentum")
     */
    @GetMapping("/rules/action")
    public ResponseEntity<List<com.yisy.modules.doramcu.shared.model.RulesEntry>> getRulesForAction(
            @RequestParam String action) {
        log.debug("GET /api/v1/captains-log/rules/action?action={}", action);
        List<com.yisy.modules.doramcu.shared.model.RulesEntry> rules =
                captainLogService.getRulesService().getRulesForAction(action);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets all available rule categories.
     */
    @GetMapping("/rules/categories")
    public ResponseEntity<List<String>> getRuleCategories() {
        log.debug("GET /api/v1/captains-log/rules/categories");
        return ResponseEntity.ok(captainLogService.getRuleCategories());
    }

    /**
     * Gets all available rulebook chapters.
     */
    @GetMapping("/rules/chapters")
    public ResponseEntity<List<String>> getRuleChapters() {
        log.debug("GET /api/v1/captains-log/rules/chapters");
        return ResponseEntity.ok(captainLogService.getRuleChapters());
    }

    /**
     * Gets all entries for a specific oracle/probability matrix table.
     * <p>
     * Table names include: "Mission Type", "Complication", "Advantage", "Encounter",
     * "NPC Role", "Alien Species", "Location Type", "Career Event".
     * </p>
     *
     * @param tableName the name of the oracle table
     */
    @GetMapping("/rules/tables")
    public ResponseEntity<List<com.yisy.modules.doramcu.captainslog.model.RulesTable>> getTableEntries(
            @RequestParam String tableName) {
        log.debug("GET /api/v1/captains-log/rules/tables?tableName={}", tableName);
        List<com.yisy.modules.doramcu.captainslog.model.RulesTable> entries =
                captainLogService.getRulesService().getTableEntries(tableName);
        return ResponseEntity.ok(entries);
    }

    /**
     * Gets all available oracle table names.
     */
    @GetMapping("/rules/tables/names")
    public ResponseEntity<List<String>> getTableNames() {
        log.debug("GET /api/v1/captains-log/rules/tables/names");
        return ResponseEntity.ok(captainLogService.getTableNames());
    }

    /**
     * Searches oracle table entries by keyword.
     * <p>
     * Searches both the result label and description of table entries.
     * </p>
     *
     * @param keyword the search keyword
     */
    @GetMapping("/rules/tables/search")
    public ResponseEntity<List<com.yisy.modules.doramcu.captainslog.model.RulesTable>> searchTables(
            @RequestParam String keyword) {
        log.debug("GET /api/v1/captains-log/rules/tables/search?keyword={}", keyword);
        List<com.yisy.modules.doramcu.captainslog.model.RulesTable> entries =
                captainLogService.getRulesService().searchTables(keyword);
        return ResponseEntity.ok(entries);
    }
}
