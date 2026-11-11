package com.yisy.modules.doramcu.dnd5e.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a character to a campaign.
 * <p>
 * Tracks the character's role in the campaign and when they joined.
 * </p>
 */
@Entity
@Table(name = "dnd5e_campaign_characters", indexes = {
        @Index(name = "idx_dnd5e_campaign_char_campaign", columnList = "campaign_id"),
        @Index(name = "idx_dnd5e_campaign_char_character", columnList = "character_id")
})
public class Dnd5eCampaignCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private Dnd5eCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Dnd5eCharacter character;

    @Column(length = 50)
    private String role; // e.g. "Player Character", "NPC", "DM Character"

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dnd5eCampaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Dnd5eCampaign campaign) {
        this.campaign = campaign;
    }

    public Dnd5eCharacter getCharacter() {
        return character;
    }

    public void setCharacter(Dnd5eCharacter character) {
        this.character = character;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
