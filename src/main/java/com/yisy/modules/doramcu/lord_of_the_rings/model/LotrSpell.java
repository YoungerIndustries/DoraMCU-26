package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Lord of the Rings spell (magic effect).
 * <p>
 * Spells in LOTR are primarily used by Wizards (Maiar) and Elves.
 * They include fire, light, healing, and enchantment effects.
 * </p>
 */
@Entity
@Table(name = "lotr_spells", indexes = {
        @Index(name = "idx_lotr_spell_name", columnList = "name"),
        @Index(name = "idx_lotr_spell_level", columnList = "spellLevel")
})
public class LotrSpell {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "spell_level", nullable = false)
    private int spellLevel;

    @Column(name = "casting_time", length = 50)
    private String castingTime;

    @Column(nullable = false, length = 50)
    private String range;

    @Column(name = "duration", nullable = false, length = 50)
    private String duration;

    @Column(name = "spell_school", length = 50)
    private String spellSchool;

    @Column(name = "requires_focus", nullable = false)
    private boolean requiresFocus = true;

    @Column(name = "verbal_component", nullable = false)
    private boolean verbalComponent = false;

    @Column(name = "somatic_component", nullable = false)
    private boolean somaticComponent = false;

    @Column(name = "material_component", length = 200)
    private String materialComponent;

    @Column(name = "corruption_cost", nullable = false)
    private int corruptionCost = 0;

    @Column(name = "ability_score", length = 10)
    private String abilityScore = "wis";

    @Column(name = "dc", nullable = false)
    private int dc = 10;

    @Column(name = "damage_die", length = 20)
    private String damageDie = "1d6";

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

    public int getSpellLevel() {
        return spellLevel;
    }

    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    public String getCastingTime() {
        return castingTime;
    }

    public void setCastingTime(String castingTime) {
        this.castingTime = castingTime;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSpellSchool() {
        return spellSchool;
    }

    public void setSpellSchool(String spellSchool) {
        this.spellSchool = spellSchool;
    }

    public boolean isRequiresFocus() {
        return requiresFocus;
    }

    public void setRequiresFocus(boolean requiresFocus) {
        this.requiresFocus = requiresFocus;
    }

    public boolean isVerbalComponent() {
        return verbalComponent;
    }

    public void setVerbalComponent(boolean verbalComponent) {
        this.verbalComponent = verbalComponent;
    }

    public boolean isSomaticComponent() {
        return somaticComponent;
    }

    public void setSomaticComponent(boolean somaticComponent) {
        this.somaticComponent = somaticComponent;
    }

    public String getMaterialComponent() {
        return materialComponent;
    }

    public void setMaterialComponent(String materialComponent) {
        this.materialComponent = materialComponent;
    }

    public int getCorruptionCost() {
        return corruptionCost;
    }

    public void setCorruptionCost(int corruptionCost) {
        this.corruptionCost = corruptionCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAbilityScore() {
        return abilityScore;
    }

    public void setAbilityScore(String abilityScore) {
        this.abilityScore = abilityScore;
    }

    public int getDC() {
        return dc;
    }

    public void setDC(int dc) {
        this.dc = dc;
    }

    public String getDamageDie() {
        return damageDie;
    }

    public void setDamageDie(String damageDie) {
        this.damageDie = damageDie;
    }

    /** Alias for getSpellLevel() */
    public int getLevel() {
        return spellLevel;
    }

    @Override
    public String toString() {
        return "LotrSpell{name='" + name + "', level=" + spellLevel + ", school='" + spellSchool + "'}";
    }
}
