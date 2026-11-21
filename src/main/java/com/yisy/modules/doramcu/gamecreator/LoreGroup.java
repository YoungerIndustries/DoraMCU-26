package com.yisy.modules.doramcu.gamecreator;

import java.util.Arrays;
import java.util.List;

/**
 * A group of related lore entries for game creation.
 */
public class LoreGroup {

    private String name;
    private String type;
    private List<String> entries;
    private String description;
    private String category;

    public LoreGroup() {}

    public LoreGroup(String name, String type, List<String> entries) {
        this.name = name;
        this.type = type;
        this.entries = entries;
    }

    public LoreGroup(String name, String type, String[] entries, String description, String category) {
        this.name = name;
        this.type = type;
        this.entries = Arrays.asList(entries);
        this.description = description;
        this.category = category;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getEntries() { return entries; }
    public void setEntries(List<String> entries) { this.entries = entries; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
