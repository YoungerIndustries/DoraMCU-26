package com.yisy.modules.doramcu.babylon5.model;

import java.util.Objects;

/**
 * A single rule entry from the Babylon 5 universe rules reference.
 * <p>
 * Each entry stores a snippet of rule text with metadata for categorization and retrieval.
 * Unlike the Traveller rules database which uses JPA persistence, the B5 rules database
 * is in-memory only, consistent with the B5 module's data loading pattern.
 * </p>
 */
public class B5RulesEntry {

    private final String id;
    private final String title;
    private final String source;
    private final String chapter;
    private final String category;
    private final Integer page;
    private final String text;
    private final String notes;

    /**
     * Creates a new rule entry.
     *
     * @param id unique identifier for the rule
     * @param title short label for the rule
     * @param source the source (e.g., "B5 TV Series", "B5 RPG", "B5 Novels")
     * @param chapter the rulebook chapter (e.g., "Combat", "Psionics")
     * @param category the rule domain (e.g., "DICE_MECHANICS", "MELEE_COMBAT")
     * @param page approximate page number or episode reference
     * @param text the full rule text
     * @param notes additional context or cross-references
     */
    public B5RulesEntry(String id, String title, String source, String chapter,
                         String category, Integer page, String text, String notes) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.source = Objects.requireNonNull(source, "Source must not be null");
        this.chapter = Objects.requireNonNull(chapter, "Chapter must not be null");
        this.category = Objects.requireNonNull(category, "Category must not be null");
        this.page = page;
        this.text = Objects.requireNonNull(text, "Text must not be null");
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getChapter() {
        return chapter;
    }

    public String getCategory() {
        return category;
    }

    public Integer getPage() {
        return page;
    }

    public String getText() {
        return text;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return "B5RulesEntry{id='" + id + "', title='" + title + "', source='" + source +
                "', chapter='" + chapter + "', category='" + category + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        B5RulesEntry that = (B5RulesEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
