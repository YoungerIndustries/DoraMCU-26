package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an active dungeon instance in a basement level.
 * <p>
 * Each dungeon instance is generated from a template and linked to a specific
 * basement floor in either the Younger Industries HQ or Apartment Building.
 * </p>
 */
@Entity
@Table(name = "dungeon_instances", indexes = {
        @Index(name = "idx_di_hq", columnList = "hq_id"),
        @Index(name = "idx_di_building", columnList = "building_id"),
        @Index(name = "idx_di_template", columnList = "template_id"),
        @Index(name = "idx_di_status", columnList = "status")
})
public class DungeonInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * HQ building ID (if in Younger Industries HQ).
     */
    @Column(name = "hq_id")
    private Long hqId;

    /**
     * Apartment building ID (if in Apartment Building).
     */
    @Column(name = "building_id")
    private Long buildingId;

    /**
     * Basement level (-1, -2, -3, etc.).
     */
    @Column(name = "basement_level", nullable = false)
    private int basementLevel;

    /**
     * Template ID used to generate this instance.
     */
    @Column(name = "template_id", nullable = false, length = 100)
    private String templateId;

    /**
     * Template name.
     */
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    /**
     * Dungeon theme.
     */
    @Column(nullable = false, length = 100)
    private String theme;

    /**
     * Difficulty level (1-20).
     */
    @Column(nullable = false)
    private int difficulty;

    /**
     * Number of rooms in this instance.
     */
    @Column(name = "room_count", nullable = false)
    private int roomCount;

    /**
     * Current status: ACTIVE, COMPLETED, ABANDONED.
     */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * Whether the instance is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * When the instance was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the instance was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * DM-only notes about this instance.
     */
    @Column(length = 2000)
    private String dmNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hq_id", insertable = false, updatable = false)
    private YoungerIndustriesHQ hq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", insertable = false, updatable = false)
    private ApartmentBuilding apartmentBuilding;

    public DungeonInstance() {
    }

    public DungeonInstance(Long hqId, Long buildingId, int basementLevel, String templateId,
                           String templateName, String theme, int difficulty) {
        this.hqId = hqId;
        this.buildingId = buildingId;
        this.basementLevel = basementLevel;
        this.templateId = templateId;
        this.templateName = templateName;
        this.theme = theme;
        this.difficulty = difficulty;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public int getBasementLevel() {
        return basementLevel;
    }

    public void setBasementLevel(int basementLevel) {
        this.basementLevel = basementLevel;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getDmNotes() {
        return dmNotes;
    }

    public void setDmNotes(String dmNotes) {
        this.dmNotes = dmNotes;
    }

    public YoungerIndustriesHQ getHq() {
        return hq;
    }

    public void setHq(YoungerIndustriesHQ hq) {
        this.hq = hq;
    }

    public ApartmentBuilding getApartmentBuilding() {
        return apartmentBuilding;
    }

    public void setApartmentBuilding(ApartmentBuilding apartmentBuilding) {
        this.apartmentBuilding = apartmentBuilding;
    }

    @Override
    public String toString() {
        return "DungeonInstance{id=" + id + ", template='" + templateName + "', basement=" +
                basementLevel + ", status='" + status + "'}";
    }
}
