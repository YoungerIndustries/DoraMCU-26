package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Encounter for balancing.
 * <p>
 * Encounters are combat or social challenges that the party faces. This model
 * tracks the encounter's composition, XP value, difficulty relative to the party,
 * and any tension-based adjustments.
 * </p>
 * <p>
 * Key mechanics:
 * - Monster XP: Total XP value of all monsters in the encounter
 * - Party XP Thresholds: Based on party size and average level
 * - Difficulty: Trivial, Easy, Medium, Hard, Deadly based on XP ratio
 * - Multiple Encounter Adjustment: Multiplier based on recent encounters
 * - Tension Bonus: Additional difficulty from tension pool levels
 * - Adjusted XP: Final XP after all adjustments for difficulty determination
 * </p>
 * <p>
 * XP Thresholds per level (DMG p.82):
 * Party Size | Trivial | Easy | Medium | Hard | Deadly
 * 1          | 100     | 200  | 400    | 800  | 1100
 * 2          | 200     | 400  | 800    | 1600 | 2200
 * 3          | 300     | 600  | 1200   | 2400 | 3300
 * 4          | 400     | 800  | 1600   | 3200 | 4400
 * 5          | 550     | 1100 | 2200   | 4400 | 6050
 * 6          | 700     | 1400 | 2800   | 5600 | 7700
 * 7          | 850     | 1700 | 3400   | 6800 | 9350
 * 8          | 1000    | 2000 | 4000   | 8000 | 11000
 * </p>
 */
@Entity
@Table(name = "dnd5e_encounters", indexes = {
        @Index(name = "idx_dnd5e_encounter_name", columnList = "name"),
        @Index(name = "idx_dnd5e_encounter_difficulty", columnList = "difficulty"),
        @Index(name = "idx_dnd5e_encounter_party_level", columnList = "partyLevel")
})
public class Dnd5eEncounter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the encounter (e.g., "Goblin Ambush", "Dragon's Lair").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Description of the encounter.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Average party level this encounter is designed for.
     */
    @Column(name = "party_level", nullable = false)
    private int partyLevel;

    /**
     * Party size this encounter is designed for.
     */
    @Column(name = "party_size", nullable = false)
    private int partySize = 4;

    /**
     * Total XP value of all monsters in the encounter (before adjustments).
     */
    @Column(name = "monster_xp", nullable = false)
    private int monsterXp;

    /**
     * Number of monsters in the encounter.
     */
    @Column(name = "monster_count", nullable = false)
    private int monsterCount;

    /**
     * Monster composition — JSON array of monster names and counts.
     * e.g., [{"name":"Goblin","count":4},{"name":"Hobgoblin","count":1}]
     */
    @Column(name = "monster_composition", columnDefinition = "TEXT")
    private String monsterComposition = "[]";

    /**
     * Average Challenge Rating of monsters in the encounter.
     */
    @Column(name = "average_cr", nullable = false)
    private double averageCr;

    /**
     * Encounter difficulty: Trivial, Easy, Medium, Hard, Deadly.
     */
    @Column(nullable = false, length = 10)
    private String difficulty = "Medium";

    /**
     * Number of recent encounters (for multiple encounter adjustment).
     * 0 = no recent encounters, 1 = 1 recent encounter, etc.
     */
    @Column(name = "recent_encounters", nullable = false)
    private int recentEncounters = 0;

    /**
     * Multiple encounter adjustment multiplier.
     * 0 recent = 1.0x, 1 recent = 1.5x, 2 recent = 1.5x, 3+ recent = 2.0x
     */
    @Column(name = "encounter_multiplier", nullable = false)
    private double encounterMultiplier = 1.0;

    /**
     * Tension level bonus (from tension pools).
     * Each tension level adds a small XP bonus to the encounter.
     */
    @Column(name = "tension_bonus", nullable = false)
    private int tensionBonus = 0;

    /**
     * Adjusted XP value (monster XP × multiplier + tension bonus).
     * Used for final difficulty determination.
     */
    @Column(name = "adjusted_xp", nullable = false)
    private int adjustedXp;

    /**
     * XP threshold for Medium difficulty for this party size and level.
     */
    @Column(name = "medium_threshold", nullable = false)
    private int mediumThreshold;

    /**
     * XP threshold for Deadly difficulty for this party size and level.
     */
    @Column(name = "deadly_threshold", nullable = false)
    private int deadlyThreshold;

    /**
     * Whether this encounter is active (not yet resolved).
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * DM-only notes about this encounter.
     */
    @Column(columnDefinition = "TEXT")
    private String dmNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPartyLevel() {
        return partyLevel;
    }

    public void setPartyLevel(int partyLevel) {
        this.partyLevel = Math.max(1, Math.min(20, partyLevel));
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = Math.max(1, Math.min(8, partySize));
    }

    public int getMonsterXp() {
        return monsterXp;
    }

    public void setMonsterXp(int monsterXp) {
        this.monsterXp = Math.max(0, monsterXp);
    }

    public int getMonsterCount() {
        return monsterCount;
    }

    public void setMonsterCount(int monsterCount) {
        this.monsterCount = Math.max(0, monsterCount);
    }

    public String getMonsterComposition() {
        return monsterComposition;
    }

    public void setMonsterComposition(String monsterComposition) {
        this.monsterComposition = monsterComposition;
    }

    public double getAverageCr() {
        return averageCr;
    }

    public void setAverageCr(double averageCr) {
        this.averageCr = Math.max(0, averageCr);
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getRecentEncounters() {
        return recentEncounters;
    }

    public void setRecentEncounters(int recentEncounters) {
        this.recentEncounters = Math.max(0, recentEncounters);
    }

    public double getEncounterMultiplier() {
        return encounterMultiplier;
    }

    public void setEncounterMultiplier(double encounterMultiplier) {
        this.encounterMultiplier = Math.max(0.5, encounterMultiplier);
    }

    public int getTensionBonus() {
        return tensionBonus;
    }

    public void setTensionBonus(int tensionBonus) {
        this.tensionBonus = Math.max(0, tensionBonus);
    }

    public int getAdjustedXp() {
        return adjustedXp;
    }

    public void setAdjustedXp(int adjustedXp) {
        this.adjustedXp = Math.max(0, adjustedXp);
    }

    public int getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(int mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }

    public int getDeadlyThreshold() {
        return deadlyThreshold;
    }

    public void setDeadlyThreshold(int deadlyThreshold) {
        this.deadlyThreshold = deadlyThreshold;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDmNotes() {
        return dmNotes;
    }

    public void setDmNotes(String dmNotes) {
        this.dmNotes = dmNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eEncounter{name='" + name + "', difficulty='" + difficulty + "', XP=" + monsterXp + "}";
    }
}
