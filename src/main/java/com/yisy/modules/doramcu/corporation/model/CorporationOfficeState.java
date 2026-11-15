package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Persists corporation office state across server restarts.
 * <p>
 * Replaces the in-memory ConcurrentHashMap for tracking office building levels.
 * Each corporation has a single office state record that is loaded on startup
 * and saved on changes.
 * </p>
 */
@Entity
@Table(name = "corporation_office_state", indexes = {
        @Index(name = "idx_cos_corp", columnList = "corporation_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cos_corp", columnNames = {"corporation_id"})
})
public class CorporationOfficeState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false, unique = true)
    private Long corporationId;

    @Column(name = "office_building_level", nullable = false)
    private Integer officeBuildingLevel = 0;

    @Column(name = "last_saved_at", nullable = false)
    private LocalDateTime lastSavedAt;

    @Version
    private Integer version;

    public CorporationOfficeState() {
        this.lastSavedAt = LocalDateTime.now();
    }

    public CorporationOfficeState(Long corporationId) {
        this();
        this.corporationId = corporationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public Integer getOfficeBuildingLevel() {
        return officeBuildingLevel;
    }

    public void setOfficeBuildingLevel(Integer officeBuildingLevel) {
        this.officeBuildingLevel = Math.max(0, officeBuildingLevel);
        this.lastSavedAt = LocalDateTime.now();
    }

    public void incrementBuildingLevel() {
        this.officeBuildingLevel++;
        this.lastSavedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "CorporationOfficeState{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", officeBuildingLevel=" + officeBuildingLevel +
                ", lastSavedAt=" + lastSavedAt +
                '}';
    }
}
