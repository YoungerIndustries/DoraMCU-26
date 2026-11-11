package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Rules entry model for SFB.
 */
public class SfbRulesEntry {
    private final String id;
    private final String title;
    private final String source;
    private final String chapter;
    private final String category;
    private final String text;
    private final String notes;

    public SfbRulesEntry(String id, String title, String source, String chapter,
                         String category, String text, String notes) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.chapter = chapter;
        this.category = category;
        this.text = text;
        this.notes = notes;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSource() { return source; }
    public String getChapter() { return chapter; }
    public String getCategory() { return category; }
    public String getText() { return text; }
    public String getNotes() { return notes; }
}
