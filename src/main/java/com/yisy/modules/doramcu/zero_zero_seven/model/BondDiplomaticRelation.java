package com.yisy.modules.doramcu.zero_zero_seven.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the diplomatic relationship between two empires.
 * <p>
 * Tracks relation score (-100 to 100), active treaties, and war status.
 * </p>
 */
@Entity
@Table(name = "bond_diplomatic_relations")
public class BondDiplomaticRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** First empire in the relationship. */
    @Column(name = "empire1_name", nullable = false, length = 100)
    private String empire1Name;

    /** Second empire in the relationship. */
    @Column(name = "empire2_name", nullable = false, length = 100)
    private String empire2Name;

    /** Relation score from -100 (war) to 100 (allied). */
    @Column(name = "relation_score", nullable = false)
    private int relationScore = 0;

    /** Whether the empires are at war. */
    @Column(name = "at_war", nullable = false)
    private boolean atWar = false;

    /** Active treaties. */
    @ElementCollection
    @CollectionTable(name = "bond_relation_treaties", joinColumns = @JoinColumn(name = "relation_id"))
    @Column(name = "treaty_type")
    private List<String> activeTreaties = new ArrayList<>();

    /** Last diplomatic event timestamp. */
    @Column(name = "last_event_at")
    private LocalDateTime lastEventAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public BondDiplomaticRelation() {
    }

    public BondDiplomaticRelation(String empire1Name, String empire2Name, BondGameSession gameSession) {
        this.empire1Name = empire1Name;
        this.empire2Name = empire2Name;
        this.gameSession = gameSession;
        this.lastEventAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmpire1Name() { return empire1Name; }
    public void setEmpire1Name(String empire1Name) { this.empire1Name = empire1Name; }

    public String getEmpire2Name() { return empire2Name; }
    public void setEmpire2Name(String empire2Name) { this.empire2Name = empire2Name; }

    public int getRelationScore() { return relationScore; }
    public void setRelationScore(int relationScore) {
        this.relationScore = Math.max(-100, Math.min(100, relationScore));
    }

    public boolean isAtWar() { return atWar; }
    public void setAtWar(boolean atWar) { this.atWar = atWar; }

    public List<String> getActiveTreaties() { return activeTreaties; }
    public void setActiveTreaties(List<String> activeTreaties) { this.activeTreaties = activeTreaties; }

    public LocalDateTime getLastEventAt() { return lastEventAt; }
    public void setLastEventAt(LocalDateTime lastEventAt) { this.lastEventAt = lastEventAt; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Modify the relation score.
     */
    public void modifyRelation(int delta) {
        setRelationScore(getRelationScore() + delta);
        this.lastEventAt = LocalDateTime.now();
        // Auto-set war status based on score
        this.atWar = getRelationScore() < -50;
    }

    /**
     * Add a treaty.
     */
    public void addTreaty(String treatyType) {
        activeTreaties.add(treatyType);
        this.lastEventAt = LocalDateTime.now();
    }

    /**
     * Remove a treaty.
     */
    public void removeTreaty(String treatyType) {
        activeTreaties.remove(treatyType);
        this.lastEventAt = LocalDateTime.now();
    }

    /**
     * Check if a treaty exists.
     */
    public boolean hasTreaty(String treatyType) {
        return activeTreaties.contains(treatyType);
    }
}
