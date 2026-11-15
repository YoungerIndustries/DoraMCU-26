package com.yisy.modules.doramcu.shared.service;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Unified service for managing rules entries and tables across all game systems.
 * <p>
 * Replaces the duplicate {@code TravellerRulesService} and {@code RulesService}
 * with a single shared service that filters by {@link GameSystem}.
 * </p>
 * <p>
 * Existing services should delegate to this service rather than maintaining
 * separate implementations. The old services are retained for backward compatibility
 * and will be deprecated once all callers migrate.
 * </p>
 */
@Service("unifiedRulesService")
public class RulesService {

    private static final Logger log = LoggerFactory.getLogger(RulesService.class);

    private final UnifiedRulesEntryRepository entryRepository;
    private final UnifiedRulesTableRepository tableRepository;

    public RulesService(UnifiedRulesEntryRepository entryRepository, UnifiedRulesTableRepository tableRepository) {
        this.entryRepository = entryRepository;
        this.tableRepository = tableRepository;
    }

    // ==================== Entry Operations ====================

    /**
     * Find all active rule entries for a specific game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesEntries", key = "'system:' + #gameSystem")
    public List<RulesEntry> getEntriesByGameSystem(GameSystem gameSystem) {
        return entryRepository.findByGameSystemAndActiveTrue(gameSystem);
    }

    /**
     * Find entries by category within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesEntries", key = "'category:' + #gameSystem + ':' + #category")
    public List<RulesEntry> getEntriesByCategory(GameSystem gameSystem, String category) {
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(gameSystem, category);
    }

    /**
     * Find entries by chapter within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesEntries", key = "'chapter:' + #gameSystem + ':' + #chapter")
    public List<RulesEntry> getEntriesByChapter(GameSystem gameSystem, String chapter) {
        return entryRepository.findByGameSystemAndChapterAndActiveTrue(gameSystem, chapter);
    }

    /**
     * Find entries by source book within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesEntries", key = "'source:' + #gameSystem + ':' + #source")
    public List<RulesEntry> getEntriesBySource(GameSystem gameSystem, String source) {
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(gameSystem, source);
    }

    /**
     * Search entries by title keyword within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesEntries", key = "'search_title:' + #gameSystem + ':' + #keyword")
    public List<RulesEntry> searchEntriesByTitle(GameSystem gameSystem, String keyword) {
        return entryRepository.searchByTitle(gameSystem, keyword);
    }

    /**
     * Search entries by text content within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesEntries", key = "'search_text:' + #gameSystem + ':' + #keyword")
    public List<RulesEntry> searchEntriesByText(GameSystem gameSystem, String keyword) {
        return entryRepository.searchByText(gameSystem, keyword);
    }

    /**
     * Save or update a rule entry.
     */
    @Transactional
    @CacheEvict(value = "rulesEntries", allEntries = true)
    public RulesEntry saveEntry(RulesEntry entry) {
        log.debug("Saving rule entry: {} ({})", entry.getTitle(), entry.getGameSystem());
        return entryRepository.save(entry);
    }

    /**
     * Delete a rule entry by ID.
     */
    @Transactional
    @CacheEvict(value = "rulesEntries", allEntries = true)
    public void deleteEntry(Long id) {
        log.debug("Deleting rule entry: {}", id);
        entryRepository.deleteById(id);
    }

    // ==================== Table Operations ====================

    /**
     * Find all active table entries for a specific game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesTables", key = "'system:' + #gameSystem")
    public List<RulesTable> getTablesByGameSystem(GameSystem gameSystem) {
        return tableRepository.findByGameSystemAndActiveTrue(gameSystem);
    }

    /**
     * Find table entries by name within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesTables", key = "'table:' + #gameSystem + ':' + #tableName")
    public List<RulesTable> getTableEntries(GameSystem gameSystem, String tableName) {
        return tableRepository.findByGameSystemAndTableNameAndActiveTrue(gameSystem, tableName);
    }

    /**
     * Find table entries by category within a game system.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesTables", key = "'category:' + #gameSystem + ':' + #category")
    public List<RulesTable> getTablesByCategory(GameSystem gameSystem, String category) {
        return tableRepository.findByGameSystemAndCategoryAndActiveTrue(gameSystem, category);
    }

    /**
     * Find table entry by roll value within a game system and table.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesTables", key = "'roll:' + #gameSystem + ':' + #tableName + ':' + #rollValue")
    public List<RulesTable> getTableEntryByRoll(GameSystem gameSystem, String tableName, int rollValue) {
        return tableRepository.findByGameSystemAndTableNameAndRollValueAndActiveTrue(
                gameSystem, tableName, rollValue);
    }

    /**
     * Save or update a table entry.
     */
    @Transactional
    @CacheEvict(value = "rulesTables", allEntries = true)
    public RulesTable saveTableEntry(RulesTable table) {
        log.debug("Saving table entry: {} ({})", table.getTableName(), table.getGameSystem());
        return tableRepository.save(table);
    }

    /**
     * Delete a table entry by ID.
     */
    @Transactional
    @CacheEvict(value = "rulesTables", allEntries = true)
    public void deleteTableEntry(Long id) {
        log.debug("Deleting table entry: {}", id);
        tableRepository.deleteById(id);
    }
}
