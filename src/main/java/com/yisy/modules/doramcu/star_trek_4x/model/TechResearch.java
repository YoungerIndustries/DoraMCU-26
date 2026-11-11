package com.yisy.modules.doramcu.star_trek_4x.model;

import com.yisy.modules.doramcu.earth2026.model.TechLevel;
import jakarta.persistence.*;

/**
 * Technology research progress for a faction.
 * <p>
 * Tracks research toward a specific TechLevel target using Earth 2026's TL0-TL20 system.
 * Categories include weapon, transport, comms, computing, medicine, and industry.
 * </p>
 */
@Entity
@Table(name = "st4x_tech_research")
public class TechResearch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Target tech level from Earth 2026 (TL0-TL20). */
    @Column(nullable = false)
    private int targetTechLevel;

    /** Research category: WEAPON, TRANSPORT, COMMS, COMPUTING, MEDICINE, INDUSTRY. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResearchCategory category;

    /** Research points spent so far. */
    @Column(nullable = false)
    private int researchPointsSpent = 0;

    /** Research points needed to complete. */
    @Column(nullable = false)
    private int researchPointsNeeded = 100;

    /** Whether this research is completed. */
    @Column(nullable = false)
    private boolean completed = false;

    /** Owner faction ID. */
    @Column(nullable = false)
    private Long ownerFactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public TechResearch() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public Long getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(Long ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

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
        WEAPON,
        TRANSPORT,
        COMMS,
        COMPUTING,
        MEDICINE,
        INDUSTRY
    }
}
