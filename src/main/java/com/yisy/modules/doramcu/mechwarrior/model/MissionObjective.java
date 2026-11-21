package com.yisy.modules.doramcu.mechwarrior.model;
import com.yisy.modules.doramcu.model.players.Player;

import java.util.UUID;

/**
 * Represents a specific objective within a MechWarrior mission.
 * <p>
 * Objectives define what the player must accomplish to complete a mission.
 * Types include destroying enemies, capturing positions, surviving for a duration,
 * escorting units, and gathering intelligence.
 * </p>
 */
public class MissionObjective {

    public enum ObjectiveType {
        DESTROY_ENEMIES,     // Destroy a specified number of enemy units
        CAPTURE_POSITION,    // Capture and hold a position
        SURVIVE,             // Survive for a specified duration
        ESCORT,              // Protect a friendly unit to a destination
        RECON,               // Gather intelligence on enemy positions
        EXTRACTION,          // Rescue a target and extract
        SABOTAGE,            // Destroy a specific target
        HOLD_POSITION,       // Defend a position for a set time
        REACH_LOCATION,      // Move to a specific location
        KILL_LEADER,         // Eliminate a specific enemy commander
        DISABLE_TARGET,      // Disable a specific target (not destroy)
        COLLECT_INTEL,       // Collect data from a location
        PROTECT_ASSET,       // Protect an asset from destruction
        AMBUSH,              // Set up and execute an ambush
        BLOCK_REINFORCEMENT  // Prevent enemy reinforcements from arriving
    }

    private String id;
    private String description;
    private ObjectiveType type;
    private boolean completed;
    private int targetCount;      // target count (e.g., enemies to destroy)
    private int currentCount;     // current progress toward target
    private String targetName;    // name of specific target (for kill leader, etc.)
    private String locationName;  // name of target location
    private int durationSeconds;  // duration for survive/hold objectives
    private int elapsedSeconds;   // time elapsed for duration-based objectives
    private boolean isOptional;   // optional bonus objective
    private int bonusCredits;     // bonus credits for optional objectives
    private int bonusExperience;  // bonus experience for optional objectives

    public MissionObjective() {
    }

    public MissionObjective(String description, ObjectiveType type) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.type = type;
        this.completed = false;
        this.targetCount = 1;
        this.currentCount = 0;
        this.isOptional = false;
        this.bonusCredits = 0;
        this.bonusExperience = 0;
        this.durationSeconds = 0;
        this.elapsedSeconds = 0;
    }

    /**
     * Progress toward the objective.
     * @param amount amount to add to current count
     * @return true if objective is now completed
     */
    public boolean progress(int amount) {
        if (completed) {
            return true;
        }
        currentCount += amount;
        if (currentCount >= targetCount) {
            completed = true;
            currentCount = targetCount;
        }
        return completed;
    }

    /**
     * Add elapsed time for duration-based objectives.
     * @param seconds seconds to add
     * @return true if objective is now completed
     */
    public boolean addElapsedSeconds(int seconds) {
        if (completed) {
            return true;
        }
        elapsedSeconds += seconds;
        if (durationSeconds > 0 && elapsedSeconds >= durationSeconds) {
            completed = true;
        }
        return completed;
    }

    /**
     * Complete the objective immediately.
     */
    public void forceComplete() {
        this.completed = true;
        this.currentCount = this.targetCount;
    }

    /**
     * Get the completion percentage.
     */
    public double getCompletionPercentage() {
        if (targetCount == 0) {
            return completed ? 100.0 : 0.0;
        }
        return (double) currentCount / targetCount * 100.0;
    }

    /**
     * Get the time remaining for duration-based objectives.
     */
    public int getTimeRemaining() {
        if (durationSeconds == 0) {
            return 0;
        }
        return Math.max(0, durationSeconds - elapsedSeconds);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ObjectiveType getType() {
        return type;
    }

    public void setType(ObjectiveType type) {
        this.type = type;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(int elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public int getBonusCredits() {
        return bonusCredits;
    }

    public void setBonusCredits(int bonusCredits) {
        this.bonusCredits = bonusCredits;
    }

    public int getBonusExperience() {
        return bonusExperience;
    }

    public void setBonusExperience(int bonusExperience) {
        this.bonusExperience = bonusExperience;
    }
}
