package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JPA entity representing a Lord of the Rings character.
 * <p>
 * A character has six ability scores (STR, DEX, CON, INT, WIS, CHA),
 * race, calling (class), level, hit points, armor class, skills,
 * corruption track, hope/shadow mechanics, and Ring status.
 * </p>
 * <p>
 * LOTR-specific mechanics:
 * - Corruption: 0-10 track; at 10 the character is lost to the Shadow.
 * - Hope/Shadow: Dual track affecting narrative dice rolls.
 * - Calling: Character class (Scout, Warrior, Ranger, Bard, Mage, etc.).
 * - Race: Elf, Man, Dwarf, Hobbit, Ent, Maiar.
 * - Ring: Whether the character bears a Ring of Power.
 * </p>
 */
@Entity
@Table(name = "lotr_characters", indexes = {
        @Index(name = "idx_lotr_character_name", columnList = "name")
})
public class LotrCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String race;

    @Column(name = "calling", nullable = false, length = 50)
    private String calling;

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

    // --- LOTR-specific fields ---

    /**
     * Corruption level: 0-10. At 10, the character is lost to the Shadow.
     * The One Ring increases corruption over time.
     */
    @Column(nullable = false)
    private int corruption = 0;

    /**
     * Hope level: 0-10. Higher hope makes narrative checks easier.
     * Starting hope is typically 5.
     */
    @Column(nullable = false)
    private int hope = 5;

    /**
     * Shadow level: 0-10. Higher shadow makes the Eye of Sauron die more dangerous.
     * Starting shadow is typically 0.
     */
    @Column(nullable = false)
    private int shadow = 0;

    /**
     * Whether this character bears the One Ring.
     */
    @Column(name = "one_ring_bearer", nullable = false)
    private boolean oneRingBearer = false;

    /**
     * Which Ring of Power this character bears (if any).
     * Values: "None", "One Ring", "Narya", "Nenya", "Vilya", "Seven Rings", "Three Dwarf Rings".
     */
    @Column(name = "ring_bearer", length = 50)
    private String ringBearer = "None";

    /**
     * Faction allegiance: "Free Peoples", "Forces of Darkness", "Neutral".
     */
    @Column(name = "faction", length = 50)
    private String faction = "Neutral";

    /**
     * Home realm: "Shire", "Rivendell", "Gondor", "Rohan", "Moria", "Lothlorien", etc.
     */
    @Column(name = "home_realm", length = 50)
    private String homeRealm;

    @Column(columnDefinition = "TEXT")
    private String equipment;

    @Column(columnDefinition = "TEXT")
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
    private List<LotrCharacterSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LotrCharacterMagicItem> magicItems = new ArrayList<>();

    /** Spells this character knows */
    @ElementCollection
    private List<Long> knownSpellIds = new ArrayList<>();

    /** Skill proficiencies (e.g., "stealth", "perception") */
    @ElementCollection
    private List<String> proficiencies = new ArrayList<>();

    /** Saving throw proficiencies (e.g., "str", "dex") */
    @ElementCollection
    private List<String> savingThrowProficiencies = new ArrayList<>();

    /** Spell slots by level (key = spell level, value = LotrSpellSlot) */
    @ElementCollection
    private Map<Integer, LotrSpellSlot> spellSlots = new java.util.HashMap<>();

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

    // --- LOTR-specific helpers ---

    /**
     * Returns true if the character is corrupted (corruption >= 10).
     */
    public boolean isLostToShadow() {
        return corruption >= 10;
    }

    /**
     * Returns true if the character is at risk of corruption (corruption >= 7).
     */
    public boolean isCorruptionRisk() {
        return corruption >= 7;
    }

    /**
     * Returns true if the character has high hope (hope >= 7).
     */
    public boolean hasHighHope() {
        return hope >= 7;
    }

    /**
     * Returns true if the character is under the Eye's gaze (shadow >= 5).
     */
    public boolean isUnderEyeGaze() {
        return shadow >= 5;
    }

    /**
     * Returns true if this character is an Elf.
     */
    public boolean isElf() {
        return "Elf".equalsIgnoreCase(race);
    }

    /**
     * Returns true if this character is a Man.
     */
    public boolean isMan() {
        return "Man".equalsIgnoreCase(race);
    }

    /**
     * Returns true if this character is a Dwarf.
     */
    public boolean isDwarf() {
        return "Dwarf".equalsIgnoreCase(race);
    }

    /**
     * Returns true if this character is a Hobbit.
     */
    public boolean isHobbit() {
        return "Hobbit".equalsIgnoreCase(race);
    }

    /**
     * Returns true if this character is an Ent.
     */
    public boolean isEnt() {
        return "Ent".equalsIgnoreCase(race);
    }

    /**
     * Returns true if this character is a Maia (Wizard).
     */
    public boolean isMaia() {
        return "Maia".equalsIgnoreCase(race);
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

    public String getCalling() {
        return calling;
    }

    public void setCalling(String calling) {
        this.calling = calling;
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

    public int getCorruption() {
        return corruption;
    }

    public void setCorruption(int corruption) {
        this.corruption = Math.max(0, Math.min(10, corruption));
    }

    public int getHope() {
        return hope;
    }

    public void setHope(int hope) {
        this.hope = Math.max(0, Math.min(10, hope));
    }

    public int getShadow() {
        return shadow;
    }

    public void setShadow(int shadow) {
        this.shadow = Math.max(0, Math.min(10, shadow));
    }

    public boolean isOneRingBearer() {
        return oneRingBearer;
    }

    public void setOneRingBearer(boolean oneRingBearer) {
        this.oneRingBearer = oneRingBearer;
    }

    public String getRingBearer() {
        return ringBearer;
    }

    public void setRingBearer(String ringBearer) {
        this.ringBearer = ringBearer;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getHomeRealm() {
        return homeRealm;
    }

    public void setHomeRealm(String homeRealm) {
        this.homeRealm = homeRealm;
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

    public List<LotrCharacterSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<LotrCharacterSkill> skills) {
        this.skills = skills;
    }

    public List<LotrCharacterMagicItem> getMagicItems() {
        return magicItems;
    }

    public void setMagicItems(List<LotrCharacterMagicItem> magicItems) {
        this.magicItems = magicItems;
    }

    public List<Long> getKnownSpellIds() {
        return knownSpellIds;
    }

    public void setKnownSpellIds(List<Long> knownSpellIds) {
        this.knownSpellIds = knownSpellIds;
    }

    public List<String> getProficiencies() {
        return proficiencies;
    }

    public void setProficiencies(List<String> proficiencies) {
        this.proficiencies = proficiencies;
    }

    public List<String> getSavingThrowProficiencies() {
        return savingThrowProficiencies;
    }

    public void setSavingThrowProficiencies(List<String> savingThrowProficiencies) {
        this.savingThrowProficiencies = savingThrowProficiencies;
    }

    public Map<Integer, LotrSpellSlot> getSpellSlots() {
        return spellSlots;
    }

    public void setSpellSlots(Map<Integer, LotrSpellSlot> spellSlots) {
        this.spellSlots = spellSlots;
    }

    @Override
    public String toString() {
        return "LotrCharacter{id=" + id + ", name='" + name + "', race='" + race +
                "', calling='" + calling + "', level=" + level + ", hp=" + hitPoints + "/" + maxHitPoints +
                ", corruption=" + corruption + "}";
    }
}
