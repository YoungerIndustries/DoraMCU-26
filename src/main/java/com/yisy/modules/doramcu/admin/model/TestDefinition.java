package com.yisy.modules.doramcu.admin.model;

import java.util.List;

/**
 * Metadata for a single test that can be registered with the TestRegistry.
 * Each test has an ID, name, category, description, and an estimated duration.
 */
public class TestDefinition {

    private String id;
    private String name;
    private String category;
    private String description;
    private long estimatedDurationMs;
    private List<String> dependencies;
    private boolean enabled;

    public TestDefinition() {
    }

    public TestDefinition(String id, String name, String category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.estimatedDurationMs = 5000;
        this.enabled = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEstimatedDurationMs() {
        return estimatedDurationMs;
    }

    public void setEstimatedDurationMs(long estimatedDurationMs) {
        this.estimatedDurationMs = estimatedDurationMs;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
