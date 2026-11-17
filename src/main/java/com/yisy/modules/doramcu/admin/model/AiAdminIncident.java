package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an incident managed by AI admins.
 * Incidents are created for severe health findings (CRITICAL/HIGH) and tracked
 * through a lifecycle: OPEN → INVESTIGATING → RESOLVED/ESCALATED/CLOSED.
 */
@Entity
@Table(name = "ai_admin_incidents", indexes = {
        @Index(name = "idx_aai_status", columnList = "status"),
        @Index(name = "idx_aai_severity", columnList = "severity"),
        @Index(name = "idx_aai_galaxy", columnList = "galaxy_name"),
        @Index(name = "idx_aai_admin", columnList = "assigned_ai_admin_id"),
        @Index(name = "idx_aai_created", columnList = "created_at")
})
public class AiAdminIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Type of incident.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 20)
    private IncidentType incidentType;

    /**
     * Severity level of the incident.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private Severity severity;

    /**
     * Priority score (1-10, higher is more urgent).
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 5;

    /**
     * Current status of the incident.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status;

    /**
     * Short title of the incident.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Detailed description of the incident.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Galaxy where the incident occurred.
     */
    @Column(name = "galaxy_name", nullable = false, length = 100)
    private String galaxyName;

    /**
     * AI admin character assigned to investigate this incident.
     */
    @Column(name = "assigned_ai_admin_id")
    private Long assignedAiAdminId;

    /**
     * Notes from the AI admin during investigation.
     */
    @Column(name = "investigator_notes", length = 2000)
    private String investigatorNotes;

    /**
     * Action taken to resolve the incident.
     */
    @Column(name = "resolution_action", length = 2000)
    private String resolutionAction;

    /**
     * Root cause identified during investigation.
     */
    @Column(name = "root_cause", length = 2000)
    private String rootCause;

    /**
     * Related work order ID, if any.
     */
    @Column(name = "related_order_id")
    private Long relatedOrderId;

    /**
     * When the incident was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the incident was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When the incident was resolved.
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * When the incident was escalated.
     */
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    public enum IncidentType {
        HEALTH,
        PERFORMANCE,
        SECURITY,
        SYSTEM
    }

    public enum Severity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum Status {
        OPEN,
        INVESTIGATING,
        RESOLVED,
        ESCALATED,
        CLOSED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public Long getAssignedAiAdminId() {
        return assignedAiAdminId;
    }

    public void setAssignedAiAdminId(Long assignedAiAdminId) {
        this.assignedAiAdminId = assignedAiAdminId;
    }

    public String getInvestigatorNotes() {
        return investigatorNotes;
    }

    public void setInvestigatorNotes(String investigatorNotes) {
        this.investigatorNotes = investigatorNotes;
    }

    public String getResolutionAction() {
        return resolutionAction;
    }

    public void setResolutionAction(String resolutionAction) {
        this.resolutionAction = resolutionAction;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
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

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(LocalDateTime escalatedAt) {
        this.escalatedAt = escalatedAt;
    }
}
