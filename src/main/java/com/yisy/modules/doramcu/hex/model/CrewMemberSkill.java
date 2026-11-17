package com.yisy.modules.doramcu.hex.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a skill for an AI character in the production system.
 * <p>
 * Each AI character can have multiple skills across different categories.
 * Skills determine which contracts a character can perform and how efficiently.
 * Skill levels range from 1-10, and experience points track progress toward
 * the next level.
 * </p>
 * <p>
 * Key mechanics:
 * - Skill Categories: MINING, ENGINEERING, SCIENCE, COMBAT, TRADE
 * - Skill Level: 1-10, determines contract eligibility and efficiency
 * - Experience Points: Accumulated through contract completion
 * - Multiple skills per character: One character can have skills in multiple categories
 * </p>
 */
@Entity
@Table(name = "crew_member_skills", indexes = {
        @Index(name = "idx_crew_skills_character", columnList = "ai_character_id"),
        @Index(name = "idx_crew_skills_category", columnList = "skill_category"),
        @Index(name = "idx_crew_skills_level", columnList = "skill_level")
})
public class CrewMemberSkill {

    public enum SkillCategory {
        MINING("Mining", "Resource extraction and mining operations"),
        ENGINEERING("Engineering", "Construction, repair, and infrastructure"),
        SCIENCE("Science", "Research, discovery, and advanced processing"),
        COMBAT("Combat", "Military operations and defense"),
        TRADE("Trade", "Commerce, negotiation, and logistics");

        private final String displayName;
        private final String description;

        SkillCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the AI character who has this skill.
     */
    @Column(name = "ai_character_id", nullable = false)
    private Long aiCharacterId;

    /**
     * Category of the skill.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_category", nullable = false, length = 50)
    private SkillCategory skillCategory;

    /**
     * Current skill level (1-10).
     */
    @Column(name = "skill_level", nullable = false)
    private Integer skillLevel;

    /**
     * Experience points accumulated for this skill.
     */
    @Column(name = "experience_points")
    private Integer experiencePoints;

    /**
     * When the skill record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (skillLevel == null) {
            skillLevel = 1;
        }
        if (experiencePoints == null) {
            experiencePoints = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public SkillCategory getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(SkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    public Integer getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Integer getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(Integer experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "CrewMemberSkill{characterId=" + aiCharacterId + ", category=" + skillCategory +
                ", level=" + skillLevel + "}";
    }
}
