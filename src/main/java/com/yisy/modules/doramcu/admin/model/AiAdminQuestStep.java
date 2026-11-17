package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a step within a multi-step quest.
 * Steps are completed in order; a quest is complete when all steps are done.
 */
@Entity
@Table(name = "ai_admin_quest_steps")
@EntityListeners(CdcEntityListener.class)
public class AiAdminQuestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Parent quest this step belongs to.
     */
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * Order of this step (1 = first, 2 = second, etc.).
     */
    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    /**
     * Short title of the step.
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of what this step requires.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Total count required to complete this step.
     */
    @Column(name = "target_count", nullable = false)
    private int targetCount = 1;

    /**
     * Current progress count toward the step target.
     */
    @Column(name = "current_count", nullable = false)
    private int currentCount = 0;

    /**
     * Whether this step is completed.
     */
    @Column(nullable = false)
    private boolean completed = false;

    /**
     * When this step was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Constructors ---

    public AiAdminQuestStep() {
    }

    public AiAdminQuestStep(Long questId, int stepOrder, String title, String description,
                            int targetCount) {
        this.questId = questId;
        this.stepOrder = stepOrder;
        this.title = title;
        this.description = description;
        this.targetCount = targetCount;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestId() {
        return questId;
    }

    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Helper methods ---

    /**
     * Returns the progress percentage (0-100) for this step.
     */
    public int getProgressPercent() {
        if (targetCount <= 0) return 0;
        return Math.min(100, (currentCount * 100) / targetCount);
    }

    /**
     * Increments the current count by the given amount.
     * Auto-completes if the target is reached.
     */
    public void incrementProgress(int amount) {
        this.currentCount = Math.min(targetCount, this.currentCount + amount);
        if (this.currentCount >= this.targetCount) {
            this.completed = true;
        }
    }

    /**
     * Returns true if this step is the next uncompleted step.
     */
    public boolean isNextStep() {
        return !completed;
    }

    @Override
    public String toString() {
        return "AiAdminQuestStep{id=" + id + ", questId=" + questId + ", step=" + stepOrder +
                ", title='" + title + "', progress=" + currentCount + "/" + targetCount +
                ", completed=" + completed + "}";
    }
}
