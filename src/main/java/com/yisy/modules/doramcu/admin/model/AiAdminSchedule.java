package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA entity representing a scheduled shift for an AI admin.
 * Shifts define when an admin is on duty and which galaxies they cover.
 */
@Entity
@Table(name = "ai_admin_schedules", indexes = {
        @Index(name = "idx_aas_admin", columnList = "ai_admin_id"),
        @Index(name = "idx_aas_galaxy", columnList = "galaxy_name"),
        @Index(name = "idx_aas_active", columnList = "is_active"),
        @Index(name = "idx_aas_type", columnList = "shift_type")
})
public class AiAdminSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * AI admin character assigned to this shift.
     */
    @Column(name = "ai_admin_id", nullable = false)
    private Long aiAdminId;

    /**
     * Type of shift.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 15)
    private ShiftType shiftType;

    /**
     * Galaxy this shift covers. Null for global shifts.
     */
    @Column(name = "galaxy_name", length = 100)
    private String galaxyName;

    /**
     * Start time of the shift (time of day).
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * End time of the shift (time of day).
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Day of week for recurring shifts. Null for ONCE and ON_CALL shifts.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private DayOfWeek dayOfWeek;

    /**
     * Whether this schedule is currently active.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Optional notes about this schedule.
     */
    @Column(length = 500)
    private String notes;

    /**
     * When this schedule was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When this schedule was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ShiftType {
        /**
         * Repeats weekly on the specified day of week.
         */
        RECURRING,
        /**
         * One-time shift for a specific date range.
         */
        ONCE,
        /**
         * Always available as backup/on-call.
         */
        ON_CALL
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAiAdminId() {
        return aiAdminId;
    }

    public void setAiAdminId(Long aiAdminId) {
        this.aiAdminId = aiAdminId;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        return "AiAdminSchedule{" +
                "id=" + id +
                ", aiAdminId=" + aiAdminId +
                ", shiftType=" + shiftType +
                ", galaxyName='" + galaxyName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", dayOfWeek=" + dayOfWeek +
                ", isActive=" + isActive +
                '}';
    }
}
