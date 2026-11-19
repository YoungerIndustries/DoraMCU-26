package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a skill for a Lord of the Rings character.
 * <p>
 * Skills in LOTR include: Alertness, Athletics, Awareness, Craft, Deception,
 * Insight, Intimidation, Lore, Persuasion, Stealth, Survival, etc.
 * </p>
 */
@Entity
@Table(name = "lotr_character_skills", indexes = {
        @Index(name = "idx_lotr_skill_character", columnList = "character_id")
})
public class LotrCharacterSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private LotrCharacter character;

    @Column(nullable = false, length = 50)
    private String skill;

    @Column(nullable = false)
    private int rank = 0;

    @Column(name = "ability_score", nullable = false, length = 10)
    private String abilityScore;

    @Column(name = "is_proficient", nullable = false)
    private boolean isProficient = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LotrCharacter getCharacter() {
        return character;
    }

    public void setCharacter(LotrCharacter character) {
        this.character = character;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getAbilityScore() {
        return abilityScore;
    }

    public void setAbilityScore(String abilityScore) {
        this.abilityScore = abilityScore;
    }

    public boolean isProficient() {
        return isProficient;
    }

    public void setProficient(boolean proficient) {
        isProficient = proficient;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LotrCharacterSkill{skill='" + skill + "', rank=" + rank + ", proficient=" + isProficient + "}";
    }
}
