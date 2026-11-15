package com.yisy.modules.doramcu.earth2026.tycoon;

import java.time.LocalDateTime;

/**
 * Represents a research project in the Corporation Tycoon game.
 * Research projects unlock new technologies and bonuses.
 */
public class TycoonResearchProject {

    public enum ResearchType {
        WEAPONS_TECH("Weapons Technology", "Improved weapons for FPS missions", 10000),
        AI_SYSTEMS("AI Systems", "Better AI assistants and automation", 15000),
        MEDICAL_BAY("Medical Bay", "Healing items and health bonuses", 8000),
        CYBER_SECURITY("Cyber Security", "Protect against corporate espionage", 12000),
        ENERGY_GRID("Energy Grid", "Reduce operational costs", 20000),
        TRANSPORT("Transport Systems", "Faster travel and logistics", 18000),
        SURVEILLANCE("Surveillance Network", "Intelligence gathering bonuses", 14000),
        NANOTECH("Nanotechnology", "Advanced materials and repairs", 25000);

        private final String displayName;
        private final String description;
        private final long baseCost;

        ResearchType(String displayName, String description, long baseCost) {
            this.displayName = displayName;
            this.description = description;
            this.baseCost = baseCost;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public long getBaseCost() {
            return baseCost;
        }
    }

    private String name;
    private long cost;
    private double progress; // 0-100
    private boolean completed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public TycoonResearchProject() {
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        if (this.progress >= 100 && !completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "TycoonResearchProject{" +
                "name='" + name + '\'' +
                ", progress=" + progress +
                ", completed=" + completed +
                '}';
    }
}
