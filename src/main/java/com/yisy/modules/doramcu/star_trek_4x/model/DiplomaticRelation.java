package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the diplomatic relationship between two factions.
 * <p>
 * Tracks relation score (-100 to 100), active treaties, and war status.
 * </p>
 */
@Entity
@Table(name = "st4x_diplomatic_relations")
public class DiplomaticRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** First faction in the relationship. */
    @Column(nullable = false, length = 100)
    private String faction1Name;

    /** Second faction in the relationship. */
    @Column(nullable = false, length = 100)
    private String faction2Name;

    /** Relation score from -100 (war) to 100 (allied). */
    @Column(nullable = false)
    private int relationScore = 0;

    /** Whether the factions are at war. */
    @Column(nullable = false)
    private boolean atWar = false;

    /** Active treaties. */
    @ElementCollection
    @CollectionTable(name = "st4x_relation_treaties", joinColumns = @JoinColumn(name = "relation_id"))
    @Column(name = "treaty_type")
    private List<String> activeTreaties = new ArrayList<>();

    /** Last diplomatic event timestamp. */
    @Column
    private LocalDateTime lastEventAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public DiplomaticRelation() {
    }

    public DiplomaticRelation(String faction1Name, String faction2Name, GameSession gameSession) {
        this.faction1Name = faction1Name;
        this.faction2Name = faction2Name;
        this.gameSession = gameSession;
        this.lastEventAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFaction1Name() { return faction1Name; }
    public void setFaction1Name(String faction1Name) { this.faction1Name = faction1Name; }

    public String getFaction2Name() { return faction2Name; }
    public void setFaction2Name(String faction2Name) { this.faction2Name = faction2Name; }

    public int getRelationScore() { return relationScore; }
    public void setRelationScore(int relationScore) { this.relationScore = Math.max(-100, Math.min(100, relationScore)); }

    public boolean isAtWar() { return atWar; }
    public void setAtWar(boolean atWar) { this.atWar = atWar; }

    public List<String> getActiveTreaties() { return activeTreaties; }
    public void setActiveTreaties(List<String> activeTreaties) { this.activeTreaties = activeTreaties; }

    public LocalDateTime getLastEventAt() { return lastEventAt; }
    public void setLastEventAt(LocalDateTime lastEventAt) { this.lastEventAt = lastEventAt; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

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
