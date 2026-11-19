package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a character to a tool proficiency in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_character_tool_proficiencies", indexes = {
        @Index(name = "idx_lotr_char_tool_character", columnList = "characterId")
})
public class LotrCharacterToolProficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
