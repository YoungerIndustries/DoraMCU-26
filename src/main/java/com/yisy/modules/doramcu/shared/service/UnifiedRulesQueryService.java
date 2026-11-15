package com.yisy.modules.doramcu.shared.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified query service that aggregates rules across ALL game systems.
 * <p>
 * This service solves the cross-module rules aggregation problem: previously,
 * each module's rules database (D&D 5e, Babylon 5, Star Wars, etc.) could only
 * be queried individually. Now, AI commanders, encounter generators, and any
 * cross-system feature can leverage the collective knowledge of all rules
 * databases through a single entry point.
 * </p>
 * <p>
 * Key capabilities:
 * <ul>
 *   <li>Search rules across ALL game systems by keyword</li>
 *   <li>Retrieve rules grouped by game system</li>
 *   <li>Query rules by category across all systems</li>
 *   <li>Compare how different systems handle the same mechanic</li>
 *   <li>Get statistics and metadata about the entire rules database</li>
 * </ul>
 * </p>
 * <p>
 * All rules databases (B5, Star Wars, D&D 5e) now use JPA persistence via
 * {@link UnifiedRulesEntryRepository}, so this service queries the same
 * underlying database that the per-system services use.
 * </p>
 */
@Service
public class UnifiedRulesQueryService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedRulesQueryService.class);

    private final UnifiedRulesEntryRepository entryRepository;

    public UnifiedRulesQueryService(UnifiedRulesEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    // ==================== Cross-System Search ====================

    /**
     * Search for rules across ALL game systems by keyword.
     * <p>
     * Searches title, text, and notes fields across every game system.
     * This is the primary method for AI commanders and encounter generators
     * that need to find relevant rules regardless of which game system they belong to.
     * </p>
     *
     * @param keyword the search keyword (case-insensitive)
     * @return list of matching entries from all game systems
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> searchAllSystems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Searching all game systems for keyword: {}", keyword);
        Set<RulesEntry> uniqueResults = new LinkedHashSet<>();
        String lower = keyword.toLowerCase();

        // Search title and text fields via repository methods
        for (GameSystem system : GameSystem.values()) {
            uniqueResults.addAll(entryRepository.searchByTitle(system, keyword));
            uniqueResults.addAll(entryRepository.searchByText(system, keyword));
        }

        // Also check notes field (not indexed by repository methods)
        List<RulesEntry> allEntries = entryRepository.findByActiveTrue();
        for (RulesEntry entry : allEntries) {
            if (entry.getNotes() != null && entry.getNotes().toLowerCase().contains(lower)) {
                uniqueResults.add(entry);
            }
        }

        List<RulesEntry> results = new ArrayList<>(uniqueResults);
        log.debug("Cross-system search found {} results for keyword: {}", results.size(), keyword);
        return results;
    }

    /**
     * Search for rules across ALL game systems, limited to specific systems.
     *
     * @param keyword    the search keyword
     * @param gameSystems the game systems to search (if null or empty, searches all)
     * @return list of matching entries from the specified game systems
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> searchSystems(String keyword, List<GameSystem> gameSystems) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (gameSystems == null || gameSystems.isEmpty()) {
            return searchAllSystems(keyword);
        }
        log.debug("Searching {} game systems for keyword: {}", gameSystems.size(), keyword);
        Set<RulesEntry> uniqueResults = new LinkedHashSet<>();
        String lower = keyword.toLowerCase();

        for (GameSystem system : gameSystems) {
            uniqueResults.addAll(entryRepository.searchByTitle(system, keyword));
            uniqueResults.addAll(entryRepository.searchByText(system, keyword));
        }

        // Also check notes field
        for (GameSystem system : gameSystems) {
            for (RulesEntry entry : entryRepository.findByGameSystemAndActiveTrue(system)) {
                if (entry.getNotes() != null && entry.getNotes().toLowerCase().contains(lower)) {
                    uniqueResults.add(entry);
                }
            }
        }

        return new ArrayList<>(uniqueResults);
    }

    // ==================== Cross-System Category Queries ====================

    /**
     * Get all rules for a specific category across ALL game systems.
     * <p>
     * Useful for comparing how different game systems handle the same type
     * of mechanic (e.g., "COMBAT" rules across D&D 5e, B5, and Star Wars).
     * </p>
     *
     * @param category the category to query (case-insensitive)
     * @return list of entries in that category from all game systems
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getRulesByCategoryAcrossSystems(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Querying category '{}' across all game systems", category);
        List<RulesEntry> results = new ArrayList<>();
        for (GameSystem system : GameSystem.values()) {
            results.addAll(entryRepository.findByGameSystemAndCategoryAndActiveTrue(system, category));
        }
        log.debug("Category '{}' found {} entries across all systems", category, results.size());
        return results;
    }

    /**
     * Get rules by category, grouped by game system.
     * <p>
     * Returns a map where each key is a GameSystem and the value is the list
     * of rules in that category for that system. Systems with no matching
     * rules are omitted from the map.
     * </p>
     *
     * @param category the category to query
     * @return map of game system to list of rules entries
     */
    @Transactional(readOnly = true)
    public Map<GameSystem, List<RulesEntry>> getRulesByCategoryGroupedBySystem(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<GameSystem, List<RulesEntry>> grouped = new EnumMap<>(GameSystem.class);
        for (GameSystem system : GameSystem.values()) {
            List<RulesEntry> entries = entryRepository.findByGameSystemAndCategoryAndActiveTrue(system, category);
            if (!entries.isEmpty()) {
                grouped.put(system, entries);
            }
        }
        return Collections.unmodifiableMap(grouped);
    }

    // ==================== Rules Comparison ====================

    /**
     * Compare how different game systems handle a similar mechanic.
     * <p>
     * Searches for the given keyword across all systems and returns the results
     * grouped by game system, making it easy to see how each system approaches
     * the same concept (e.g., "cover", "saving throw", "initiative").
     * </p>
     *
     * @param mechanic the mechanic keyword to compare
     * @return map of game system to list of matching rules entries
     */
    @Transactional(readOnly = true)
    public Map<GameSystem, List<RulesEntry>> compareMechanicAcrossSystems(String mechanic) {
        if (mechanic == null || mechanic.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        log.info("Comparing mechanic '{}' across all game systems", mechanic);
        Map<GameSystem, List<RulesEntry>> comparison = new EnumMap<>(GameSystem.class);

        for (GameSystem system : GameSystem.values()) {
            Set<RulesEntry> systemResults = new LinkedHashSet<>();
            systemResults.addAll(entryRepository.searchByTitle(system, mechanic));
            systemResults.addAll(entryRepository.searchByText(system, mechanic));

            // Also check notes
            String lower = mechanic.toLowerCase();
            for (RulesEntry entry : entryRepository.findByGameSystemAndActiveTrue(system)) {
                if (entry.getNotes() != null && entry.getNotes().toLowerCase().contains(lower)) {
                    systemResults.add(entry);
                }
            }

            if (!systemResults.isEmpty()) {
                comparison.put(system, new ArrayList<>(systemResults));
            }
        }

        log.info("Mechanic '{}' found in {} game systems", mechanic, comparison.size());
        return Collections.unmodifiableMap(comparison);
    }

    // ==================== All-System Retrieval ====================

    /**
     * Get ALL active rules entries across all game systems.
     *
     * @return list of all active rules entries
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getAllRules() {
        return entryRepository.findByActiveTrue();
    }

    /**
     * Get all rules entries grouped by game system.
     *
     * @return map of game system to list of all its rules entries
     */
    @Transactional(readOnly = true)
    public Map<GameSystem, List<RulesEntry>> getAllRulesGroupedBySystem() {
        Map<GameSystem, List<RulesEntry>> grouped = new EnumMap<>(GameSystem.class);
        for (GameSystem system : GameSystem.values()) {
            List<RulesEntry> entries = entryRepository.findByGameSystemAndActiveTrue(system);
            if (!entries.isEmpty()) {
                grouped.put(system, entries);
            }
        }
        return Collections.unmodifiableMap(grouped);
    }

    // ==================== Metadata and Statistics ====================

    /**
     * Get all distinct categories across all game systems.
     *
     * @return sorted list of all unique categories
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        List<RulesEntry> allEntries = entryRepository.findByActiveTrue();
        return allEntries.stream()
                .map(RulesEntry::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct categories grouped by game system.
     *
     * @return map of game system to its list of categories
     */
    @Transactional(readOnly = true)
    public Map<GameSystem, List<String>> getCategoriesBySystem() {
        Map<GameSystem, List<String>> grouped = new EnumMap<>(GameSystem.class);
        for (GameSystem system : GameSystem.values()) {
            List<RulesEntry> entries = entryRepository.findByGameSystemAndActiveTrue(system);
            if (!entries.isEmpty()) {
                List<String> categories = entries.stream()
                        .map(RulesEntry::getCategory)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                if (!categories.isEmpty()) {
                    grouped.put(system, categories);
                }
            }
        }
        return Collections.unmodifiableMap(grouped);
    }

    /**
     * Get all distinct sources across all game systems.
     *
     * @return sorted list of all unique source books
     */
    @Transactional(readOnly = true)
    public List<String> getAllSources() {
        List<RulesEntry> allEntries = entryRepository.findByActiveTrue();
        return allEntries.stream()
                .map(RulesEntry::getSource)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct chapters across all game systems.
     *
     * @return sorted list of all unique chapters
     */
    @Transactional(readOnly = true)
    public List<String> getAllChapters() {
        List<RulesEntry> allEntries = entryRepository.findByActiveTrue();
        return allEntries.stream()
                .map(RulesEntry::getChapter)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Get a summary of rules entry counts per game system.
     *
     * @return map of game system to entry count
     */
    @Transactional(readOnly = true)
    public Map<GameSystem, Integer> getEntryCountsBySystem() {
        Map<GameSystem, Integer> counts = new EnumMap<>(GameSystem.class);
        for (GameSystem system : GameSystem.values()) {
            int count = entryRepository.findByGameSystemAndActiveTrue(system).size();
            if (count > 0) {
                counts.put(system, count);
            }
        }
        return Collections.unmodifiableMap(counts);
    }

    /**
     * Get the total number of active rules entries across all game systems.
     *
     * @return total entry count
     */
    @Transactional(readOnly = true)
    public int getTotalEntryCount() {
        return entryRepository.findByActiveTrue().size();
    }

    /**
     * Get the list of game systems that have populated rules entries.
     *
     * @return list of game systems with at least one active rule entry
     */
    @Transactional(readOnly = true)
    public List<GameSystem> getPopulatedSystems() {
        List<GameSystem> populated = new ArrayList<>();
        for (GameSystem system : GameSystem.values()) {
            if (!entryRepository.findByGameSystemAndActiveTrue(system).isEmpty()) {
                populated.add(system);
            }
        }
        return Collections.unmodifiableList(populated);
    }

    /**
     * Get a comprehensive summary of the entire rules database.
     * <p>
     * Returns a map containing:
     * <ul>
     *   <li>totalEntries - total number of active rules entries</li>
     *   <li>populatedSystems - number of game systems with entries</li>
     *   <li>totalCategories - number of distinct categories</li>
     *   <li>totalSources - number of distinct source books</li>
     *   <li>entriesBySystem - map of system to entry count</li>
     * </ul>
     * </p>
     *
     * @return map of summary statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDatabaseSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        List<RulesEntry> allEntries = entryRepository.findByActiveTrue();
        int totalEntries = allEntries.size();

        Set<GameSystem> populatedSystems = EnumSet.noneOf(GameSystem.class);
        Set<String> categories = new HashSet<>();
        Set<String> sources = new HashSet<>();

        for (RulesEntry entry : allEntries) {
            populatedSystems.add(entry.getGameSystem());
            if (entry.getCategory() != null) {
                categories.add(entry.getCategory());
            }
            if (entry.getSource() != null) {
                sources.add(entry.getSource());
            }
        }

        summary.put("totalEntries", totalEntries);
        summary.put("populatedSystems", populatedSystems.size());
        summary.put("populatedSystemNames", new ArrayList<>(populatedSystems));
        summary.put("totalCategories", categories.size());
        summary.put("totalSources", sources.size());
        summary.put("entriesBySystem", getEntryCountsBySystem());

        log.debug("Rules database summary: {} entries across {} systems", totalEntries, populatedSystems.size());
        return Collections.unmodifiableMap(summary);
    }

    // ==================== Cross-System Source Queries ====================

    /**
     * Get all rules from a specific source book across all game systems.
     * <p>
     * Some source books may appear in multiple game systems (e.g., a core rulebook
     * that was adapted for different systems).
     * </p>
     *
     * @param source the source book name (case-insensitive)
     * @return list of entries from that source across all systems
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getRulesBySourceAcrossSystems(String source) {
        if (source == null || source.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<RulesEntry> results = new ArrayList<>();
        for (GameSystem system : GameSystem.values()) {
            results.addAll(entryRepository.findByGameSystemAndSourceAndActiveTrue(system, source));
        }
        return results;
    }

    /**
     * Get all rules from a specific chapter across all game systems.
     *
     * @param chapter the chapter name (case-insensitive)
     * @return list of entries from that chapter across all systems
     */
    @Transactional(readOnly = true)
    public List<RulesEntry> getRulesByChapterAcrossSystems(String chapter) {
        if (chapter == null || chapter.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<RulesEntry> results = new ArrayList<>();
        for (GameSystem system : GameSystem.values()) {
            results.addAll(entryRepository.findByGameSystemAndChapterAndActiveTrue(system, chapter));
        }
        return results;
    }
}
