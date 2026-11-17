package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an AI admin collaborator on an incident.
 * Enables multiple AI admins to work together on complex incidents.
 */
@Entity
@Table(name = "ai_admin_incident_collaborators", indexes = {
        @Index(name = "idx_aaic_incident", columnList = "incident_id"),
        @Index(name = "idx_aaic_admin", columnList = "ai_admin_id"),
        @Index(name = "idx_aaic_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_aaic_incident_admin", columnNames = {"incident_id", "ai_admin_id"})
})
public class AiAdminIncidentCollaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The incident this collaborator is working on.
     */
    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    /**
     * The AI admin character ID.
     */
    @Column(name = "ai_admin_id", nullable = false)
    private Long aiAdminId;

    /**
     * Role of the collaborator in the incident.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Role role;

    /**
     * Current status of the collaborator.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    /**
     * Notes from this collaborator.
     */
    @Column(length = 2000)
    private String notes;

    /**
     * When the collaborator joined the incident.
     */
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /**
     * When the collaborator completed their work.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum Role {
        LEAD,
        INVESTIGATOR,
        ANALYST,
        REVIEWER
    }

    public enum Status {
        INVITED,
        ACTIVE,
        COMPLETED,
        REMOVED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public Long getAiAdminId() {
        return aiAdminId;
    }

    public void setAiAdminId(Long aiAdminId) {
        this.aiAdminId = aiAdminId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
