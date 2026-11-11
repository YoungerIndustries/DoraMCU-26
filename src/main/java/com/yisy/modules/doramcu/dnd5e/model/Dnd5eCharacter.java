package com.yisy.modules.doramcu.dnd5e.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e character.
 * <p>
 * A character has six ability scores (STR, DEX, CON, INT, WIS, CHA),
 * race, class, level, hit points, armor class, skills, proficiencies,
 * equipment, and spell slots.
 * </p>
 */
@Entity
@Table(name = "dnd5e_characters", indexes = {
        @Index(name = "idx_dnd5e_character_name", columnList = "name")
})
public class Dnd5eCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String race;

    @Column(name = "character_class", nullable = false, length = 50)
    private String characterClass;

    @Column(nullable = false)
    private int level = 1;

    @Column(length = 50)
    private String background;

    @Column(name = "str_score", nullable = false)
    private int strScore = 10;

    @Column(name = "dex_score", nullable = false)
    private int dexScore = 10;

    @Column(name = "con_score", nullable = false)
    private int conScore = 10;

    @Column(name = "int_score", nullable = false)
    private int intScore = 10;

    @Column(name = "wis_score", nullable = false)
    private int wisScore = 10;

    @Column(name = "cha_score", nullable = false)
    private int chaScore = 10;

    @Column(name = "hit_points", nullable = false)
    private int hitPoints;

    @Column(name = "max_hit_points", nullable = false)
    private int maxHitPoints;

    @Column(name = "armor_class", nullable = false)
    private int armorClass = 10;

    @Column(nullable = false)
    private int speed = 30;

    @Column(length = 50)
    private String hitDice;

    @Column(name = "proficiency_bonus", nullable = false)
    private int proficiencyBonus = 2;

    @Column(name = "death_save_successes", nullable = false)
    private int deathSaveSuccesses = 0;

    @Column(name = "death_save_failures", nullable = false)
    private int deathSaveFailures = 0;

    @Column(name = "temp_hit_points", nullable = false)
    private int tempHitPoints = 0;

    @Column(name = "exhaustion_level", nullable = false)
    private int exhaustionLevel = 0;

    @Column(columnDefinition = "TEXT")
    private String equipment;

    @Column(columnDefinition = "JSON")
    private String conditions;

    @Column(name = "experience_points", nullable = false)
    private int experiencePoints = 0;

    @Column(nullable = false)
    private boolean inspiration = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Dnd5eCharacterSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Dnd5eSpellSlot> spellSlots = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Ability Score Modifiers ---

    public int getStrMod() {
        return modifier(strScore);
    }

    public int getDexMod() {
        return modifier(dexScore);
    }

    public int getConMod() {
        return modifier(conScore);
    }

    public int getIntMod() {
        return modifier(intScore);
    }

    public int getWisMod() {
        return modifier(wisScore);
    }

    public int getChaMod() {
        return modifier(chaScore);
    }

    private int modifier(int score) {
        return (score - 10) / 2;
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

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getCharacterClass() {
        return characterClass;
    }

    public void setCharacterClass(String characterClass) {
        this.characterClass = characterClass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public int getStrScore() {
        return strScore;
    }

    public void setStrScore(int strScore) {
        this.strScore = strScore;
    }

    public int getDexScore() {
        return dexScore;
    }

    public void setDexScore(int dexScore) {
        this.dexScore = dexScore;
    }

    public int getConScore() {
        return conScore;
    }

    public void setConScore(int conScore) {
        this.conScore = conScore;
    }

    public int getIntScore() {
        return intScore;
    }

    public void setIntScore(int intScore) {
        this.intScore = intScore;
    }

    public int getWisScore() {
        return wisScore;
    }

    public void setWisScore(int wisScore) {
        this.wisScore = wisScore;
    }

    public int getChaScore() {
        return chaScore;
    }

    public void setChaScore(int chaScore) {
        this.chaScore = chaScore;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getHitDice() {
        return hitDice;
    }

    public void setHitDice(String hitDice) {
        this.hitDice = hitDice;
    }

    public int getProficiencyBonus() {
        return proficiencyBonus;
    }

    public void setProficiencyBonus(int proficiencyBonus) {
        this.proficiencyBonus = proficiencyBonus;
    }

    public int getDeathSaveSuccesses() {
        return deathSaveSuccesses;
    }

    public void setDeathSaveSuccesses(int deathSaveSuccesses) {
        this.deathSaveSuccesses = deathSaveSuccesses;
    }

    public int getDeathSaveFailures() {
        return deathSaveFailures;
    }

    public void setDeathSaveFailures(int deathSaveFailures) {
        this.deathSaveFailures = deathSaveFailures;
    }

    public int getTempHitPoints() {
        return tempHitPoints;
    }

    public void setTempHitPoints(int tempHitPoints) {
        this.tempHitPoints = tempHitPoints;
    }

    public int getExhaustionLevel() {
        return exhaustionLevel;
    }

    public void setExhaustionLevel(int exhaustionLevel) {
        this.exhaustionLevel = exhaustionLevel;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public boolean isInspiration() {
        return inspiration;
    }

    public void setInspiration(boolean inspiration) {
        this.inspiration = inspiration;
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

    public List<Dnd5eCharacterSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<Dnd5eCharacterSkill> skills) {
        this.skills = skills;
    }

    public List<Dnd5eSpellSlot> getSpellSlots() {
        return spellSlots;
    }

    public void setSpellSlots(List<Dnd5eSpellSlot> spellSlots) {
        this.spellSlots = spellSlots;
    }

    @Override
    public String toString() {
        return "Dnd5eCharacter{id=" + id + ", name='" + name + "', race='" + race +
                "', class='" + characterClass + "', level=" + level + ", hp=" + hitPoints + "/" + maxHitPoints + "}";
    }
}
