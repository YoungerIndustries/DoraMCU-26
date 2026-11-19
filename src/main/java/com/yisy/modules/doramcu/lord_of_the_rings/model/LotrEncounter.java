package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Lord of the Rings encounter for balancing.
 * <p>
 * Encounters are combat or social challenges that the party faces.
 * LOTR encounters use corruption and hope mechanics alongside traditional XP balancing.
 * </p>
 */
@Entity
@Table(name = "lotr_encounters", indexes = {
        @Index(name = "idx_lotr_encounter_name", columnList = "name"),
        @Index(name = "idx_lotr_encounter_difficulty", columnList = "difficulty"),
        @Index(name = "idx_lotr_encounter_party_level", columnList = "partyLevel")
})
public class LotrEncounter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "party_level", nullable = false)
    private int partyLevel;

    @Column(name = "party_size", nullable = false)
    private int partySize = 4;

    @Column(name = "monster_xp", nullable = false)
    private int monsterXp;

    @Column(name = "monster_count", nullable = false)
    private int monsterCount;

    @Column(name = "monster_composition", columnDefinition = "TEXT")
    private String monsterComposition = "[]";

    @Column(name = "average_cr", nullable = false)
    private double averageCr;

    @Column(nullable = false, length = 10)
    private String difficulty = "Medium";

    @Column(name = "corruption_risk", nullable = false)
    private int corruptionRisk = 0;

    @Column(name = "hope_test", nullable = false)
    private boolean hopeTest = false;

    @Column(name = "eye_of_sauron", nullable = false)
    private boolean eyeOfSauron = false;

    @Column(nullable = false)
    private boolean active = true;

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

    public int getCorruptionRisk() {
        return corruptionRisk;
    }

    public void setCorruptionRisk(int corruptionRisk) {
        this.corruptionRisk = Math.max(0, corruptionRisk);
    }

    public boolean isHopeTest() {
        return hopeTest;
    }

    public void setHopeTest(boolean hopeTest) {
        this.hopeTest = hopeTest;
    }

    public boolean isEyeOfSauron() {
        return eyeOfSauron;
    }

    public void setEyeOfSauron(boolean eyeOfSauron) {
        this.eyeOfSauron = eyeOfSauron;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "LotrEncounter{name='" + name + "', difficulty='" + difficulty + "', XP=" + monsterXp + "}";
    }
}
