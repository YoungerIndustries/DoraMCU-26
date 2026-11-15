package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.shared.model.RulesEntry;

import java.util.List;

/**
 * Shared interface for rules database access across all game modules.
 * <p>
 * Each module has its own rules database (D&amp;D rules, Traveller rules, Earth2026 rules,
 * Star Wars rules, Babylon 5 rules). This interface provides a unified way to query rules entries.
 * </p>
 * <p>
 * Uses the shared JPA {@link RulesEntry} entity from {@code shared.model} instead of a
 * duplicate inner class, ensuring type consistency across all game modules.
 * </p>
 */
public interface RulesService {

    /**
     * List all rules entries.
     *
     * @return list of rules entries
     */
    List<RulesEntry> listRules();

    /**
     * Search rules by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching rules entries
     */
    List<RulesEntry> searchRules(String keyword);

    /**
     * Get a rules entry by ID.
     *
     * @param id the entry ID
     * @return the rules entry or null if not found
     */
    RulesEntry getRule(Long id);

    /**
     * Get rules entries by category.
     *
     * @param category the category to filter by
     * @return list of rules entries in that category
     */
    default List<RulesEntry> getRulesByCategory(String category) {
        List<RulesEntry> all = listRules();
        List<RulesEntry> result = new java.util.ArrayList<>();
        for (RulesEntry entry : all) {
            if (entry.getCategory() != null && entry.getCategory().equalsIgnoreCase(category)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Get rules entries by source book.
     *
     * @param source the source book to filter by
     * @return list of rules entries from that source
     */
    default List<RulesEntry> getRulesBySource(String source) {
        List<RulesEntry> all = listRules();
        List<RulesEntry> result = new java.util.ArrayList<>();
        for (RulesEntry entry : all) {
            if (entry.getSource() != null && entry.getSource().equalsIgnoreCase(source)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Get distinct categories available in this rules database.
     *
     * @return list of distinct category strings
     */
    default List<String> getCategories() {
        List<RulesEntry> all = listRules();
        return all.stream()
                .map(RulesEntry::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
}
