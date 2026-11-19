package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a character to a growing item in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_character_growing_items", indexes = {
        @Index(name = "idx_lotr_char_grow_character", columnList = "characterId")
})
public class LotrCharacterGrowingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private int level = 1;

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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
