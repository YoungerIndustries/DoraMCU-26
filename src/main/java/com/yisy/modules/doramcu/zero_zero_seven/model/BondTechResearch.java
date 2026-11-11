package com.yisy.modules.doramcu.zero_zero_seven.model;

import jakarta.persistence.*;

/**
 * Technology research progress for an empire.
 * <p>
 * Tracks research toward Cold War-era technologies: nuclear weapons, jet aircraft,
 * communications, cryptography, and more. Categories include military, intelligence,
 * industrial, and scientific research.
 * </p>
 */
@Entity
@Table(name = "bond_tech_research")
public class BondTechResearch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Description of the technology. */
    @Column(length = 500)
    private String description;

    /** Target tech level. */
    @Column(name = "target_tech_level", nullable = false)
    private int targetTechLevel;

    /** Research category. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResearchCategory category;

    /** Research points spent so far. */
    @Column(name = "research_points_spent", nullable = false)
    private int researchPointsSpent = 0;

    /** Research points needed to complete. */
    @Column(name = "research_points_needed", nullable = false)
    private int researchPointsNeeded = 100;

    /** Whether this research is completed. */
    @Column(nullable = false)
    private boolean completed = false;

    /** Owner empire ID. */
    @Column(name = "owner_empire_id", nullable = false)
    private Long ownerEmpireId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public BondTechResearch() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTargetTechLevel() { return targetTechLevel; }
    public void setTargetTechLevel(int targetTechLevel) { this.targetTechLevel = targetTechLevel; }

    public ResearchCategory getCategory() { return category; }
    public void setCategory(ResearchCategory category) { this.category = category; }

    public int getResearchPointsSpent() { return researchPointsSpent; }
    public void setResearchPointsSpent(int researchPointsSpent) { this.researchPointsSpent = researchPointsSpent; }

    public int getResearchPointsNeeded() { return researchPointsNeeded; }
    public void setResearchPointsNeeded(int researchPointsNeeded) { this.researchPointsNeeded = researchPointsNeeded; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Long getOwnerEmpireId() { return ownerEmpireId; }
    public void setOwnerEmpireId(Long ownerEmpireId) { this.ownerEmpireId = ownerEmpireId; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Add research points.
     */
    public void addResearchPoints(int points) {
        this.researchPointsSpent += points;
        if (researchPointsSpent >= researchPointsNeeded) {
            this.completed = true;
        }
    }

    /**
     * Get progress percentage.
     */
    public double getProgress() {
        return (double) researchPointsSpent / researchPointsNeeded * 100.0;
    }

    public enum ResearchCategory {
        MILITARY,
        INTELLIGENCE,
        INDUSTRIAL,
        SCIENTIFIC,
        NUCLEAR,
        COMMUNICATIONS
    }
}
