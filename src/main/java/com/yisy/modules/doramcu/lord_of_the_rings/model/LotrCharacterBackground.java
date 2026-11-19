package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a character to a background in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_character_backgrounds", indexes = {
        @Index(name = "idx_lotr_char_bg_character", columnList = "characterId")
})
public class LotrCharacterBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "background_id", nullable = false)
    private Long backgroundId;

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

    public Long getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(Long backgroundId) {
        this.backgroundId = backgroundId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
