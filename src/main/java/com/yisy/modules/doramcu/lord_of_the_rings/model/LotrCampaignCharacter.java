package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a character to a LOTR campaign.
 */
@Entity
@Table(name = "lotr_campaign_characters", indexes = {
        @Index(name = "idx_lotr_cc_campaign", columnList = "campaign_id"),
        @Index(name = "idx_lotr_cc_character", columnList = "character_id")
})
public class LotrCampaignCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private LotrCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @JsonIgnore
    private LotrCharacter character;

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

    public LotrCampaign getCampaign() {
        return campaign;
    }

    public void setCampaign(LotrCampaign campaign) {
        this.campaign = campaign;
    }

    public LotrCharacter getCharacter() {
        return character;
    }

    public void setCharacter(LotrCharacter character) {
        this.character = character;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
