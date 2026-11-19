package com.yisy.modules.doramcu.lord_of_the_rings.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a Lord of the Rings campaign session.
 * <p>
 * A session records a single adventure session within a campaign,
 * including the date, notes, XP awarded, and corruption changes.
 * </p>
 */
@Entity
@Table(name = "lotr_sessions", indexes = {
        @Index(name = "idx_lotr_session_campaign", columnList = "campaign_id"),
        @Index(name = "idx_lotr_session_date", columnList = "session_date")
})
public class LotrSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private LotrCampaign campaign;

    @Column(name = "session_name", nullable = false, length = 200)
    private String sessionName;

    @Column(name = "session_number", nullable = false)
    private int sessionNumber;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "xp_awarded", nullable = false)
    private int xpAwarded = 0;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "corruption_change", nullable = false)
    private int corruptionChange = 0;

    @Column(name = "hope_change", nullable = false)
    private int hopeChange = 0;

    @Column(name = "shadow_change", nullable = false)
    private int shadowChange = 0;

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

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getXpAwarded() {
        return xpAwarded;
    }

    public void setXpAwarded(int xpAwarded) {
        this.xpAwarded = xpAwarded;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getCorruptionChange() {
        return corruptionChange;
    }

    public void setCorruptionChange(int corruptionChange) {
        this.corruptionChange = corruptionChange;
    }

    public int getHopeChange() {
        return hopeChange;
    }

    public void setHopeChange(int hopeChange) {
        this.hopeChange = hopeChange;
    }

    public int getShadowChange() {
        return shadowChange;
    }

    public void setShadowChange(int shadowChange) {
        this.shadowChange = shadowChange;
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

    @Override
    public String toString() {
        return "LotrSession{name='" + sessionName + "', number=" + sessionNumber + ", xp=" + xpAwarded + "}";
    }
}
