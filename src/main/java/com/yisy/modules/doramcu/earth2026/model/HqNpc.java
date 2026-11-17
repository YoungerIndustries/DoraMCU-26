package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an NPC (Non-Player Character) in the Younger Industries HQ.
 * NPCs provide information, services, and dialogue to corporations in the lobby.
 */
@Entity
@Table(name = "hq_npcs", indexes = {
        @Index(name = "idx_hnpc_hq", columnList = "hq_id"),
        @Index(name = "idx_hnpc_type", columnList = "npc_type"),
        @Index(name = "idx_hnpc_active", columnList = "is_active")
})
public class HqNpc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The HQ this NPC resides in.
     */
    @Column(name = "hq_id", nullable = false)
    private Long hqId;

    /**
     * Type of NPC determines their role and dialogue.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "npc_type", nullable = false, length = 30)
    private HqNpcType npcType;

    /**
     * Display name of the NPC.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Short title or role description.
     */
    @Column(length = 100)
    private String title;

    /**
     * Detailed description of the NPC.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether the NPC is currently active and available for interaction.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Floor where the NPC is located.
     */
    @Column(name = "floor_location", length = 15)
    private String floorLocation;

    /**
     * JSON array of dialogue options and responses.
     */
    @Column(name = "dialogue_json", columnDefinition = "TEXT")
    private String dialogueJson;

    /**
     * Services this NPC provides (e.g., "RENT_PAYMENT", "OFFICE_INFO", "SECURITY_REPORT").
     */
    @Column(name = "services_json", columnDefinition = "TEXT")
    private String servicesJson;

    /**
     * When the NPC was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public HqNpc() {
        this.createdAt = LocalDateTime.now();
    }

    public HqNpc(Long hqId, HqNpcType npcType, String name, String title, String description) {
        this();
        this.hqId = hqId;
        this.npcType = npcType;
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHqId() {
        return hqId;
    }

    public void setHqId(Long hqId) {
        this.hqId = hqId;
    }

    public HqNpcType getNpcType() {
        return npcType;
    }

    public void setNpcType(HqNpcType npcType) {
        this.npcType = npcType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getFloorLocation() {
        return floorLocation;
    }

    public void setFloorLocation(String floorLocation) {
        this.floorLocation = floorLocation;
    }

    public String getDialogueJson() {
        return dialogueJson;
    }

    public void setDialogueJson(String dialogueJson) {
        this.dialogueJson = dialogueJson;
    }

    public String getServicesJson() {
        return servicesJson;
    }

    public void setServicesJson(String servicesJson) {
        this.servicesJson = servicesJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "HqNpc{id=" + id + ", name=" + name + ", type=" + npcType + ", floor=" + floorLocation + "}";
    }
}
