package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a collaboration note on an incident.
 * Provides an audit trail of collaborative investigation activities.
 */
@Entity
@Table(name = "ai_admin_collaboration_notes", indexes = {
        @Index(name = "idx_aacn_incident", columnList = "incident_id"),
        @Index(name = "idx_aacn_created", columnList = "created_at")
})
public class AiAdminCollaborationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The incident this note belongs to.
     */
    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    /**
     * The AI admin character who wrote this note.
     */
    @Column(name = "ai_admin_id", nullable = false)
    private Long aiAdminId;

    /**
     * The note content.
     */
    @Column(nullable = false, length = 2000)
    private String note;

    /**
     * When the note was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
