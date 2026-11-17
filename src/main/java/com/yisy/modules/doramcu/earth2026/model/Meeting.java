package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a business meeting between two corporations
 * in the Younger Industries HQ.
 */
@Entity
@Table(name = "corporate_meetings", indexes = {
        @Index(name = "idx_cm_requester", columnList = "requester_corporation_id"),
        @Index(name = "idx_cm_target", columnList = "target_corporation_id"),
        @Index(name = "idx_cm_status", columnList = "status")
})
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "requester_corporation_id", nullable = false)
    private Long requesterCorporationId;

    @Column(name = "target_corporation_id", nullable = false)
    private Long targetCorporationId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetingStatus status;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "meeting_notes", length = 4000)
    private String meetingNotes;

    public Meeting() {
        this.status = MeetingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Meeting(Long requesterCorporationId, Long targetCorporationId, String title, String description) {
        this();
        this.requesterCorporationId = requesterCorporationId;
        this.targetCorporationId = targetCorporationId;
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterCorporationId() {
        return requesterCorporationId;
    }

    public void setRequesterCorporationId(Long requesterCorporationId) {
        this.requesterCorporationId = requesterCorporationId;
    }

    public Long getTargetCorporationId() {
        return targetCorporationId;
    }

    public void setTargetCorporationId(Long targetCorporationId) {
        this.targetCorporationId = targetCorporationId;
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

    public MeetingStatus getStatus() {
        return status;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
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

    public String getMeetingNotes() {
        return meetingNotes;
    }

    public void setMeetingNotes(String meetingNotes) {
        this.meetingNotes = meetingNotes;
    }

    @Override
    public String toString() {
        return "Meeting{id=" + id + ", requester=" + requesterCorporationId +
                ", target=" + targetCorporationId + ", title='" + title +
                "', status=" + status + "}";
    }
}
