package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a Lord of the Rings campaign (quest).
 * <p>
 * A campaign groups characters together for a shared adventure,
 * tracks the narrator (DM), and contains a list of sessions.
 * LOTR campaigns are themed around quests like "Journey to Mount Doom",
 * "Battle of the Five Armies", or "Downfall of Númenor".
 * </p>
 */
@Entity
@Table(name = "lotr_campaigns", indexes = {
        @Index(name = "idx_lotr_campaign_name", columnList = "name")
})
public class LotrCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "narrator_name", nullable = false, length = 100)
    private String narratorName;

    @Column(name = "setting_name", length = 100)
    private String settingName;

    @Column(name = "starting_level", nullable = false)
    private int startingLevel = 1;

    @Column(name = "quest_type", length = 50)
    private String questType;

    @Column(name = "hope_level", nullable = false)
    private int hopeLevel = 5;

    @Column(name = "shadow_level", nullable = false)
    private int shadowLevel = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LotrCampaignCharacter> campaignCharacters = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LotrSession> sessions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNarratorName() {
        return narratorName;
    }

    public void setNarratorName(String narratorName) {
        this.narratorName = narratorName;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public String getQuestType() {
        return questType;
    }

    public void setQuestType(String questType) {
        this.questType = questType;
    }

    public int getHopeLevel() {
        return hopeLevel;
    }

    public void setHopeLevel(int hopeLevel) {
        this.hopeLevel = Math.max(0, Math.min(10, hopeLevel));
    }

    public int getShadowLevel() {
        return shadowLevel;
    }

    public void setShadowLevel(int shadowLevel) {
        this.shadowLevel = Math.max(0, Math.min(10, shadowLevel));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<LotrCampaignCharacter> getCampaignCharacters() {
        return campaignCharacters;
    }

    public void setCampaignCharacters(List<LotrCampaignCharacter> campaignCharacters) {
        this.campaignCharacters = campaignCharacters;
    }

    public List<LotrSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<LotrSession> sessions) {
        this.sessions = sessions;
    }

    @Override
    public String toString() {
        return "LotrCampaign{id=" + id + ", name='" + name + "', quest='" + questType + "'}";
    }
}
