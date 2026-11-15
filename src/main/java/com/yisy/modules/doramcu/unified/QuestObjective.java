package com.yisy.modules.doramcu.unified;

import java.util.Collections;
import java.util.Map;

/**
 * Represents an objective within a unified quest.
 * <p>
 * Objectives track progress toward quest completion. Each objective
 * belongs to a specific module and has a target value.
 * </p>
 */
public class QuestObjective {

    private final String id;
    private final String description;
    private final String module;
    private final String type;
    private final double target;
    private double progress;
    private boolean completed;
    private final Map<String, Object> metadata;

    public QuestObjective(String id, String description, String module,
                          String type, double target, Map<String, Object> metadata) {
        this.id = id;
        this.description = description;
        this.module = module;
        this.type = type;
        this.target = target;
        this.progress = 0;
        this.completed = false;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getModule() { return module; }
    public String getType() { return type; }
    public double getTarget() { return target; }
    public double getProgress() { return progress; }
    public boolean isCompleted() { return completed; }
    public Map<String, Object> getMetadata() { return metadata; }

    /**
     * Adds progress to this objective.
     */
    public void addProgress(double amount) {
        this.progress = Math.min(this.progress + amount, this.target);
        if (this.progress >= this.target) {
            this.completed = true;
        }
    }

    /**
     * Marks this objective as completed.
     */
    public void complete() {
        this.completed = true;
        this.progress = this.target;
    }
}
