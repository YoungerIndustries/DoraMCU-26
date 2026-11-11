package com.yisy.modules.doramcu.fourx;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared diplomatic relation model for the 4X framework.
 * <p>
 * Tracks the relationship between two factions including relation score,
 * war status, and active treaties.
 * </p>
 * <p>
 * This is a plain DTO — not a JPA entity.
 * </p>
 */
public class FourxDiplomaticRelation {

    private String id;

    /** First faction name in the relationship. */
    private String faction1Name;

    /** Second faction name in the relationship. */
    private String faction2Name;

    /** Relation score from -100 (war) to 100 (allied). */
    private int relationScore;

    /** Whether the factions are at war. */
    private boolean atWar;

    /** Active treaties. */
    private List<String> activeTreaties = new ArrayList<>();

    /** Last diplomatic event timestamp. */
    private LocalDateTime lastEventAt;

    public FourxDiplomaticRelation() {
    }

    public FourxDiplomaticRelation(String faction1Name, String faction2Name) {
        this.faction1Name = faction1Name;
        this.faction2Name = faction2Name;
        this.relationScore = 0;
        this.atWar = false;
        this.lastEventAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFaction1Name() { return faction1Name; }
    public void setFaction1Name(String faction1Name) { this.faction1Name = faction1Name; }

    public String getFaction2Name() { return faction2Name; }
    public void setFaction2Name(String faction2Name) { this.faction2Name = faction2Name; }

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

    /** Modify the relation score. */
    public void modifyRelation(int delta) {
        setRelationScore(getRelationScore() + delta);
        this.lastEventAt = LocalDateTime.now();
        // Auto-set war status based on score
        this.atWar = getRelationScore() < -50;
    }

    /** Add a treaty. */
    public void addTreaty(String treatyType) {
        activeTreaties.add(treatyType);
        this.lastEventAt = LocalDateTime.now();
    }

    /** Remove a treaty. */
    public void removeTreaty(String treatyType) {
        activeTreaties.remove(treatyType);
        this.lastEventAt = LocalDateTime.now();
    }

    /** Check if a treaty exists. */
    public boolean hasTreaty(String treatyType) {
        return activeTreaties.contains(treatyType);
    }
}
