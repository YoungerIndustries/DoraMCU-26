package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a character to a feat in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_character_feats", indexes = {
        @Index(name = "idx_lotr_char_feat_character", columnList = "characterId")
})
public class LotrCharacterFeat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "feat_id", nullable = false)
    private Long featId;

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

    public Long getFeatId() {
        return featId;
    }

    public void setFeatId(Long featId) {
        this.featId = featId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
