package com.yisy.modules.doramcu.dnd5e.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA entity linking a D&D 5e character to a skill proficiency.
 * <p>
 * D&D 5e has 18 skills: Acrobatics, Animal Handling, Arcana, Athletics,
 * Deception, History, Insight, Intimidation, Investigation, Medicine,
 * Nature, Perception, Performance, Persuasion, Religion, Sleight of Hand,
 * Stealth, Survival.
 * </p>
 */
@Entity
@Table(name = "dnd5e_character_skills")
public class Dnd5eCharacterSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private Dnd5eCharacter character;

    @Column(name = "skill_name", nullable = false, length = 50)
    private String skillName;

    @Column(name = "proficiency_bonus", nullable = false)
    private int proficiencyBonus = 0;

    @Column(nullable = false)
    private boolean expertise = false;

    public Dnd5eCharacterSkill() {
    }

    public Dnd5eCharacterSkill(Dnd5eCharacter character, String skillName) {
        this.character = character;
        this.skillName = skillName;
        this.proficiencyBonus = character.getProficiencyBonus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dnd5eCharacter getCharacter() {
        return character;
    }

    public void setCharacter(Dnd5eCharacter character) {
        this.character = character;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getProficiencyBonus() {
        return proficiencyBonus;
    }

    public void setProficiencyBonus(int proficiencyBonus) {
        this.proficiencyBonus = proficiencyBonus;
    }

    public boolean isExpertise() {
        return expertise;
    }

    public void setExpertise(boolean expertise) {
        this.expertise = expertise;
    }

    @Override
    public String toString() {
        return "Dnd5eCharacterSkill{skill='" + skillName + "', proficient=" + (proficiencyBonus > 0) +
                ", expertise=" + expertise + "}";
    }
}
