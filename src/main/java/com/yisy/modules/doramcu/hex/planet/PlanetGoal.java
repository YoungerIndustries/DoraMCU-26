package com.yisy.modules.doramcu.hex.planet;

import java.util.*;

/**
 * Represents a goal or mission on the planet surface.
 * <p>
 * Inspired by OpenTTD's {@code GSGoal} API, a goal:
 * <ul>
 *   <li>Has a title and description</li>
 *   <li>Tracks progress toward completion</li>
 *   <li>Can have prerequisites (other goals that must be completed first)</li>
 *   <li>Triggers callbacks when completed</li>
 * </ul>
 * <p>
 * Examples:
 * <ul>
 *   <li>"Found your first settlement" — completed when a settlement is built</li>
 *   <li>"Build a steel mill" — completed when a secondary industry is built</li>
 *   <li>"Deliver 1000 cargo units" — completed when total deliveries reach threshold</li>
 * </ul>
 */
public class PlanetGoal {

    private final String id;
    private final String title;
    private final String description;
    private final Set<String> prerequisiteIds;
    private double progress;
    private final double targetProgress;
    private boolean completed;
    private boolean failed;
    private long completedTurn;
    private final Map<String, Object> rewards;

    /**
     * Creates a new goal.
     *
     * @param id            unique identifier
     * @param title         short display title
     * @param description   detailed description
     * @param targetProgress the progress value needed to complete (e.g., 1.0, 100.0)
     */
    public PlanetGoal(String id, String title, String description, double targetProgress) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Goal ID must not be empty");
        }
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Goal title must not be empty");
        }
        if (description == null) {
            throw new IllegalArgumentException("Goal description must not be null");
        }
        if (targetProgress <= 0) {
            throw new IllegalArgumentException("Target progress must be positive: " + targetProgress);
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.prerequisiteIds = new HashSet<>();
        this.progress = 0.0;
        this.targetProgress = targetProgress;
        this.completed = false;
        this.failed = false;
        this.completedTurn = -1;
        this.rewards = new HashMap<>();
    }

    /** @return the unique goal ID */
    public String getId() {
        return id;
    }

    /** @return the short display title */
    public String getTitle() {
        return title;
    }

    /** @return the detailed description */
    public String getDescription() {
        return description;
    }

    /** @return the prerequisite goal IDs */
    public Set<String> getPrerequisiteIds() {
        return Collections.unmodifiableSet(prerequisiteIds);
    }

    /**
     * Adds a prerequisite goal.
     *
     * @param prerequisiteId the ID of the prerequisite goal
     */
    public void addPrerequisite(String prerequisiteId) {
        if (prerequisiteId == null || prerequisiteId.isEmpty()) {
            throw new IllegalArgumentException("Prerequisite ID must not be empty");
        }
        prerequisiteIds.add(prerequisiteId);
    }

    /** @return the current progress value */
    public double getProgress() {
        return progress;
    }

    /** @return the target progress needed for completion */
    public double getTargetProgress() {
        return targetProgress;
    }

    /**
     * @return the progress ratio (0.0 to 1.0)
     */
    public double getProgressRatio() {
        return Math.min(1.0, progress / targetProgress);
    }

    /** @return true if the goal is completed */
    public boolean isCompleted() {
        return completed;
    }

    /** @return true if the goal has failed */
    public boolean isFailed() {
        return failed;
    }

    /** @return the turn number when completed, or -1 if not completed */
    public long getCompletedTurn() {
        return completedTurn;
    }

    /** @return the reward map */
    public Map<String, Object> getRewards() {
        return Collections.unmodifiableMap(rewards);
    }

    /**
     * Adds a reward for completing this goal.
     *
     * @param key   the reward key (e.g., "credits", "technology")
     * @param value the reward value
     */
    public void addReward(String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Reward key must not be empty");
        }
        rewards.put(key, value);
    }

    /**
     * Increments the progress toward completion.
     *
     * @param amount the amount to add
     * @param currentTurn the current turn number
     * @return true if the goal was completed by this update
     */
    public boolean addProgress(double amount, long currentTurn) {
        if (completed || failed) return false;
        if (amount < 0) {
            throw new IllegalArgumentException("Progress amount must be non-negative: " + amount);
        }
        progress = Math.min(targetProgress, progress + amount);
        if (progress >= targetProgress) {
            completed = true;
            completedTurn = currentTurn;
        }
        return completed;
    }

    /**
     * Sets the progress directly.
     *
     * @param value the new progress value
     * @param currentTurn the current turn number
     * @return true if the goal was completed by this update
     */
    public boolean setProgress(double value, long currentTurn) {
        if (completed || failed) return false;
        progress = Math.min(targetProgress, Math.max(0.0, value));
        if (progress >= targetProgress) {
            completed = true;
            completedTurn = currentTurn;
        }
        return completed;
    }

    /**
     * Marks this goal as failed.
     */
    public void fail() {
        if (completed) return;
        failed = true;
    }

    /**
     * @return true if all prerequisites are satisfied
     * (passes by default if no prerequisites are set)
     */
    public boolean arePrerequisitesMet(Map<String, PlanetGoal> allGoals) {
        for (String prereqId : prerequisiteIds) {
            PlanetGoal prereq = allGoals.get(prereqId);
            if (prereq == null || !prereq.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("PlanetGoal{%s, progress=%.1f/%.1f, completed=%s}",
                title, progress, targetProgress, completed);
    }
}
