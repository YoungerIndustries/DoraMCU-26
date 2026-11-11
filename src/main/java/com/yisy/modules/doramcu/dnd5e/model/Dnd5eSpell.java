package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a D&D 5e spell.
 * <p>
 * Spells have a level (0-9), school, casting time, range, components,
 * duration, and optional damage or healing effects.
 * </p>
 */
@Entity
@Table(name = "dnd5e_spells", indexes = {
        @Index(name = "idx_dnd5e_spell_name", columnList = "name"),
        @Index(name = "idx_dnd5e_spell_level", columnList = "level"),
        @Index(name = "idx_dnd5e_spell_school", columnList = "school")
})
public class Dnd5eSpell {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int level = 0;

    @Column(nullable = false, length = 50)
    private String school;

    @Column(name = "casting_time", nullable = false, length = 50)
    private String castingTime;

    @Column(nullable = false, length = 50)
    private String range;

    @Column(nullable = false, length = 100)
    private String components;

    @Column(nullable = false, length = 50)
    private String duration;

    @Column(nullable = false)
    private boolean concentration = false;

    @Column(nullable = false)
    private boolean ritual = false;

    @Column(length = 50)
    private String damageType;

    @Column(name = "damage_formula", length = 100)
    private String damageFormula;

    @Column(name = "healing_formula", length = 100)
    private String healingFormula;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSON")
    private String classes;

    @Column(nullable = false)
    private boolean active = true;

    public Dnd5eSpell() {
    }

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
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

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isConcentration() {
        return concentration;
    }

    public void setConcentration(boolean concentration) {
        this.concentration = concentration;
    }

    public boolean isRitual() {
        return ritual;
    }

    public void setRitual(boolean ritual) {
        this.ritual = ritual;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getDamageFormula() {
        return damageFormula;
    }

    public void setDamageFormula(String damageFormula) {
        this.damageFormula = damageFormula;
    }

    public String getHealingFormula() {
        return healingFormula;
    }

    public void setHealingFormula(String healingFormula) {
        this.healingFormula = healingFormula;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Dnd5eSpell{name='" + name + "', level=" + level + ", school='" + school + "'}";
    }
}
