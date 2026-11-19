package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Psion (magic user) in the Lord of the Rings galaxy.
 */
@Entity
@Table(name = "lotr_psions", indexes = {
        @Index(name = "idx_lotr_psion_character", columnList = "characterId")
})
public class LotrPsion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(nullable = false)
    private int psiPoints = 0;

    @Column(name = "max_psi_points", nullable = false)
    private int maxPsiPoints = 10;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public int getPsiPoints() {
        return psiPoints;
    }

    public void setPsiPoints(int psiPoints) {
        this.psiPoints = psiPoints;
    }

    public int getMaxPsiPoints() {
        return maxPsiPoints;
    }

    public void setMaxPsiPoints(int maxPsiPoints) {
        this.maxPsiPoints = maxPsiPoints;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
