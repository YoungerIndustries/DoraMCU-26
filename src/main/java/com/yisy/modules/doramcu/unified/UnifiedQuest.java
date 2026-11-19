package com.yisy.modules.doramcu.unified;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a quest in the unified world.
 * <p>
 * Quests combine objectives from multiple modules into a single
 * trackable quest with rewards.
 * </p>
 */
public class UnifiedQuest {

    public enum QuestStatus {
        AVAILABLE,
        ACTIVE,
        COMPLETED,
        FAILED
    }

    private final String id;
    private final String title;
    private final String description;
    private final List<String> sourceModules;
    private QuestStatus status;
    private final List<QuestObjective> objectives;
    private final Map<String, Object> rewards;
    private final LocalDateTime createdAt;

    public UnifiedQuest(String id, String title, String description,
                        List<String> sourceModules, List<QuestObjective> objectives,
                        Map<String, Object> rewards) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sourceModules = sourceModules != null ? Collections.unmodifiableList(sourceModules) : Collections.emptyList();
        this.status = QuestStatus.AVAILABLE;
        this.objectives = objectives != null ? new ArrayList<>(objectives) : new ArrayList<>();
        this.rewards = rewards != null ? new HashMap<>(rewards) : new HashMap<>();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getSourceModules() { return sourceModules; }
    public QuestStatus getStatus() { return status; }
    public void setStatus(QuestStatus status) { this.status = status; }
    public List<QuestObjective> getObjectives() { return Collections.unmodifiableList(objectives); }
    public Map<String, Object> getRewards() { return Collections.unmodifiableMap(rewards); }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Returns the number of completed objectives.
     */
    public long getCompletedObjectives() {
        return objectives.stream().filter(QuestObjective::isCompleted).count();
    }

    /**
     * Returns the total number of objectives.
     */
    public long getTotalObjectives() {
        return objectives.size();
    }

    /**
     * Checks if all objectives are completed.
     */
    public boolean isAllObjectivesCompleted() {
        return !objectives.isEmpty() && objectives.stream().allMatch(QuestObjective::isCompleted);
    }
}
