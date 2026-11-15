package com.yisy.modules.doramcu.mechwarrior;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing and querying the MechWarrior / BattleTech rules database.
 * <p>
 * This service provides access to MechWarrior lore, game rules, and reference tables
 * stored in the unified rules database, filtered by the MECH_WARRIOR game system.
 * </p>
 * <p>
 * The rules database covers:
 * <ul>
 *   <li>Inner Sphere lore â€” Great Houses, Succession Wars, the Star League, the Clans</li>
 *   <li>Core game mechanics â€” 2D6 system, initiative, movement, combat resolution</li>
 *   <li>Combat rules â€” To-Hit numbers, hit locations, armor, damage, critical hits</li>
 *   <li>Heat system â€” heat generation, shutdown thresholds, heat sink capacity</li>
 *   <li>MechWarrior pilot rules â€” skills, consciousness, experience levels</li>
 *   <li>Weapon systems â€” energy, ballistic, and missile weapons</li>
 *   <li>Mech classifications â€” light, medium, heavy, and assault BattleMechs</li>
 *   <li>Battle formations â€” lances, companies, and scenario types</li>
 * </ul>
 * </p>
 */
@Service
public class MechWarriorRulesService {

    private static final Logger log = LoggerFactory.getLogger(MechWarriorRulesService.class);

    private final UnifiedRulesEntryRepository entryRepository;
    private final UnifiedRulesTableRepository tableRepository;

    public MechWarriorRulesService(UnifiedRulesEntryRepository entryRepository,
                                    UnifiedRulesTableRepository tableRepository) {
        this.entryRepository = entryRepository;
        this.tableRepository = tableRepository;
    }

    // ==================== Query Methods ====================

    /**
     * Get all rules for a given category (e.g., "CORE_MECHANICS", "COMBAT", "WEAPONS").
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getRulesByCategory(String category) {
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.MECH_WARRIOR, category);
    }

    /**
     * Get all rules from a given source (e.g., "BattleTech Rulebook", "MechWarrior Lore").
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getRulesBySource(String source) {
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(GameSystem.MECH_WARRIOR, source);
    }

    /**
     * Get all rules from a given chapter (e.g., "Core Rules", "Combat", "Movement").
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getRulesByChapter(String chapter) {
        return entryRepository.findByGameSystemAndChapterAndActiveTrue(GameSystem.MECH_WARRIOR, chapter);
    }

    /**
     * Search rules by keyword in title.
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> searchRules(String keyword) {
        return entryRepository.searchByTitle(GameSystem.MECH_WARRIOR, keyword);
    }

    /**
     * Search rules by keyword in text content.
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> searchRulesByText(String keyword) {
        return entryRepository.searchByText(GameSystem.MECH_WARRIOR, keyword);
    }

    /**
     * Get all distinct categories.
     */
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR)
                .stream().map(RulesEntry::getCategory).distinct().sorted().toList();
    }

    /**
     * Get all distinct source books.
     */
    @Transactional(readOnly = true)
    public List<String> getSources() {
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR)
                .stream().map(RulesEntry::getSource).distinct().sorted().toList();
    }

    /**
     * Get all distinct chapters.
     */
    @Transactional(readOnly = true)
    public List<String> getChapters() {
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR)
                .stream().map(RulesEntry::getChapter).distinct().sorted().toList();
    }

    /**
     * Get all active rule entries for MechWarrior.
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getAllRules() {
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR);
    }

    // ==================== Reference Tables ====================

    /**
     * Get all table entries for a given table name.
     */
    @Transactional(readOnly = true)
    public List<RulesTable> getTableEntries(String tableName) {
        return tableRepository.findByGameSystemAndTableNameAndActiveTrue(GameSystem.MECH_WARRIOR, tableName);
    }

    /**
     * Get all table entries for a given category.
     */
    @Transactional(readOnly = true)
    public List<RulesTable> getTableEntriesByCategory(String category) {
        return tableRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.MECH_WARRIOR, category);
    }

    /**
     * Get all distinct table categories from the database.
     */
    @Transactional(readOnly = true)
    public List<String> getAllTableCategories() {
        return tableRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR)
                .stream().map(RulesTable::getCategory).distinct().sorted().toList();
    }

    /**
     * Get all active table entries.
     */
    @Transactional(readOnly = true)
    public List<RulesTable> getAllTableEntries() {
        return tableRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR);
    }

    /**
     * Get a summary of the MechWarrior rules database.
     */
    @Transactional(readOnly = true)
    public MechWarriorRulesSummary getSummary() {
        List<RulesEntry> entries = entryRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR);
        List<RulesTable> tables = tableRepository.findByGameSystemAndActiveTrue(GameSystem.MECH_WARRIOR);

        MechWarriorRulesSummary summary = new MechWarriorRulesSummary();
        summary.totalEntries = entries.size();
        summary.totalTables = tables.size();
        summary.categories = entries.stream().map(RulesEntry::getCategory).distinct().sorted().toList();
        summary.sources = entries.stream().map(RulesEntry::getSource).distinct().sorted().toList();
        summary.chapters = entries.stream().map(RulesEntry::getChapter).distinct().sorted().toList();
        summary.tableCategories = tables.stream().map(RulesTable::getCategory).distinct().sorted().toList();
        return summary;
    }

    /**
     * Summary data for the MechWarrior rules database.
     */
    public static class MechWarriorRulesSummary {
        public int totalEntries;
        public int totalTables;
        public List<String> categories;
        public List<String> sources;
        public List<String> chapters;
        public List<String> tableCategories;
    }
}
