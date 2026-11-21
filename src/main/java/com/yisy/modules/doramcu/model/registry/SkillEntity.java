package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for skills across all game modules.
 */
@Entity
@Table(name = "skill", indexes = {
        @Index(name = "idx_sk_game_module", columnList = "game_module")
})
public class SkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "skill_code", nullable = false, unique = true, length = 50)
    private String skillCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "game_module", length = 50)
    private String gameModule;

    @Column(name = "related_stats", columnDefinition = "TEXT")
    private String relatedStats;

    public SkillEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillCode() {
        return skillCode;
    }

    public void setSkillCode(String skillCode) {
        this.skillCode = skillCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameModule() {
        return gameModule;
    }

    public void setGameModule(String gameModule) {
        this.gameModule = gameModule;
    }

    public String getRelatedStats() {
        return relatedStats;
    }

    public void setRelatedStats(String relatedStats) {
        this.relatedStats = relatedStats;
    }
}
