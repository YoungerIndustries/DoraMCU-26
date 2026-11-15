package com.yisy.modules.doramcu.shared.repository;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Shared repository for unified rules entries across all game systems.
 * Renamed from RulesEntryRepository to avoid bean name conflict with Captain's Log.
 */
public interface UnifiedRulesEntryRepository extends JpaRepository<RulesEntry, Long> {

    /** Find all active entries for a specific game system. */
    List<RulesEntry> findByGameSystemAndActiveTrue(GameSystem gameSystem);

    /** Find entries by category within a game system. */
    List<RulesEntry> findByGameSystemAndCategoryAndActiveTrue(GameSystem gameSystem, String category);

    /** Find entries by chapter within a game system. */
    List<RulesEntry> findByGameSystemAndChapterAndActiveTrue(GameSystem gameSystem, String chapter);

    /** Find entries by source book within a game system. */
    List<RulesEntry> findByGameSystemAndSourceAndActiveTrue(GameSystem gameSystem, String source);

    /** Search entries by title keyword within a game system. */
    @Query("SELECT r FROM UnifiedRulesEntry r WHERE r.gameSystem = :gameSystem AND r.active = true AND LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RulesEntry> searchByTitle(@Param("gameSystem") GameSystem gameSystem, @Param("keyword") String keyword);

    /** Search entries by text content within a game system. */
    @Query("SELECT r FROM UnifiedRulesEntry r WHERE r.gameSystem = :gameSystem AND r.active = true AND LOWER(r.text) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RulesEntry> searchByText(@Param("gameSystem") GameSystem gameSystem, @Param("keyword") String keyword);

    /** Find all active entries across all game systems. */
    List<RulesEntry> findByActiveTrue();

    // --- Convenience methods for backward compatibility (default to CAPTAINS_LOG) ---

    /** Find entries by category (defaults to CAPTAINS_LOG game system). */
    default List<RulesEntry> findByCategoryAndActiveTrue(String category) {
        return findByGameSystemAndCategoryAndActiveTrue(GameSystem.CAPTAINS_LOG, category);
    }

    /** Find entries by chapter (defaults to CAPTAINS_LOG game system). */
    default List<RulesEntry> findByChapterAndActiveTrue(String chapter) {
        return findByGameSystemAndChapterAndActiveTrue(GameSystem.CAPTAINS_LOG, chapter);
    }

    /** Search entries by keyword in title or text (defaults to CAPTAINS_LOG game system). */
    default List<RulesEntry> searchByKeyword(String keyword) {
        List<RulesEntry> results = searchByTitle(GameSystem.CAPTAINS_LOG, keyword);
        results.addAll(searchByText(GameSystem.CAPTAINS_LOG, keyword));
        return results;
    }

    /** Search entries by keyword within a category (defaults to CAPTAINS_LOG game system). */
    default List<RulesEntry> searchByKeywordInCategory(String keyword, String category) {
        List<RulesEntry> byCategory = findByGameSystemAndCategoryAndActiveTrue(GameSystem.CAPTAINS_LOG, category);
        List<RulesEntry> matched = new java.util.ArrayList<>();
        String lower = keyword.toLowerCase();
        for (RulesEntry entry : byCategory) {
            if (entry.getTitle().toLowerCase().contains(lower) ||
                (entry.getText() != null && entry.getText().toLowerCase().contains(lower))) {
                matched.add(entry);
            }
        }
        return matched;
    }

    /** Find distinct categories (defaults to CAPTAINS_LOG game system). */
    default List<String> findDistinctCategories() {
        List<RulesEntry> entries = findByGameSystemAndActiveTrue(GameSystem.CAPTAINS_LOG);
        return entries.stream()
                .map(RulesEntry::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    /** Find distinct chapters (defaults to CAPTAINS_LOG game system). */
    default List<String> findDistinctChapters() {
        List<RulesEntry> entries = findByGameSystemAndActiveTrue(GameSystem.CAPTAINS_LOG);
        return entries.stream()
                .map(RulesEntry::getChapter)
                .distinct()
                .sorted()
                .toList();
    }
}
