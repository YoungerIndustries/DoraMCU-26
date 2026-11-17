package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an event in the Younger Industries HQ.
 * Events are time-limited occurrences that affect all or specific corporations in the HQ.
 */
@Entity
@Table(name = "hq_events", indexes = {
        @Index(name = "idx_he_hq", columnList = "hq_id"),
        @Index(name = "idx_he_type", columnList = "event_type"),
        @Index(name = "idx_he_status", columnList = "status"),
        @Index(name = "idx_he_start", columnList = "start_time"),
        @Index(name = "idx_he_end", columnList = "end_time")
})
public class HqEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The HQ building where this event occurs.
     */
    @Column(name = "hq_id", nullable = false)
    private Long hqId;

    /**
     * Type of event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private HqEventType eventType;

    /**
     * Event status: ACTIVE, COMPLETED, CANCELLED, SCHEDULED.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "SCHEDULED";

    /**
     * Event title.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Detailed description of the event.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * When the event starts.
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * When the event was manually completed (null if expired naturally).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * When the event ends.
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * Which floor/area the event takes place on.
     */
    @Column(name = "location", length = 50)
    private String location;

    /**
     * JSON array of corporation IDs that participated in this event.
     */
    @Column(name = "participating_corps", columnDefinition = "TEXT")
    private String participatingCorps;

    /**
     * JSON object of rewards/penalties applied.
     */
    @Column(name = "rewards_json", columnDefinition = "TEXT")
    private String rewardsJson;

    /**
     * Optional: corporation ID that triggered/organized this event.
     */
    @Column(name = "triggered_by_corp")
    private Long triggeredByCorp;

    /**
     * When the event was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the event was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HqEvent() {
    }

    // Getters and Setters

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

    public HqEventType getEventType() {
        return eventType;
    }

    public void setEventType(HqEventType eventType) {
        this.eventType = eventType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getParticipatingCorps() {
        return participatingCorps;
    }

    public void setParticipatingCorps(String participatingCorps) {
        this.participatingCorps = participatingCorps;
    }

    public String getRewardsJson() {
        return rewardsJson;
    }

    public void setRewardsJson(String rewardsJson) {
        this.rewardsJson = rewardsJson;
    }

    public Long getTriggeredByCorp() {
        return triggeredByCorp;
    }

    public void setTriggeredByCorp(Long triggeredByCorp) {
        this.triggeredByCorp = triggeredByCorp;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "HqEvent{id=" + id + ", type=" + eventType + ", status=" + status +
                ", title='" + title + "', startTime=" + startTime + "}";
    }
}
