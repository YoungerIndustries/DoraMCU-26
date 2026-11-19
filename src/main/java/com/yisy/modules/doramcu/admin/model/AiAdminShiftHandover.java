package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a shift handover between AI admins.
 * When an outgoing admin's shift ends, a handover record is created
 * briefing the incoming admin on pending work (incidents, orders, notes).
 */
@Entity
@Table(name = "ai_admin_shift_handovers", indexes = {
        @Index(name = "idx_aash_outgoing", columnList = "outgoing_admin_id"),
        @Index(name = "idx_aash_incoming", columnList = "incoming_admin_id"),
        @Index(name = "idx_aash_galaxy", columnList = "galaxy_name"),
        @Index(name = "idx_aash_status", columnList = "status"),
        @Index(name = "idx_aash_created", columnList = "created_at")
})
public class AiAdminShiftHandover {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Type of handover.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "handover_type", nullable = false, length = 10)
    private HandoverType handoverType;

    /**
     * Current status of the handover.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status;

    /**
     * AI admin character whose shift is ending.
     */
    @Column(name = "outgoing_admin_id", nullable = false)
    private Long outgoingAdminId;

    /**
     * AI admin character whose shift is beginning.
     */
    @Column(name = "incoming_admin_id", nullable = false)
    private Long incomingAdminId;

    /**
     * Galaxy where the handover takes place.
     */
    @Column(name = "galaxy_name", nullable = false, length = 100)
    private String galaxyName;

    /**
     * LLM-generated summary of pending work.
     */
    @Column(length = 4000)
    private String summary;

    /**
     * JSON list of pending incident IDs.
     */
    @Column(name = "pending_incidents", length = 2000)
    private String pendingIncidents;

    /**
     * JSON list of pending order IDs.
     */
    @Column(name = "pending_orders", length = 2000)
    private String pendingOrders;

    /**
     * Notes from the incoming admin acknowledging the handover.
     */
    @Column(length = 2000)
    private String notes;

    /**
     * When the handover was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the incoming admin acknowledged the handover.
     */
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    /**
     * When the handover was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public AiAdminShiftHandover() {
    }

    public AiAdminShiftHandover(Long outgoingAdminId, Long incomingAdminId, String galaxyName,
                                HandoverType handoverType) {
        this.outgoingAdminId = outgoingAdminId;
        this.incomingAdminId = incomingAdminId;
        this.galaxyName = galaxyName;
        this.handoverType = handoverType;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HandoverType getHandoverType() {
        return handoverType;
    }

    public void setHandoverType(HandoverType handoverType) {
        this.handoverType = handoverType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getOutgoingAdminId() {
        return outgoingAdminId;
    }

    public void setOutgoingAdminId(Long outgoingAdminId) {
        this.outgoingAdminId = outgoingAdminId;
    }

    public Long getIncomingAdminId() {
        return incomingAdminId;
    }

    public void setIncomingAdminId(Long incomingAdminId) {
        this.incomingAdminId = incomingAdminId;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPendingIncidents() {
        return pendingIncidents;
    }

    public void setPendingIncidents(String pendingIncidents) {
        this.pendingIncidents = pendingIncidents;
    }

    public String getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(String pendingOrders) {
        this.pendingOrders = pendingOrders;
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

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Type of handover.
     */
    public enum HandoverType {
        AUTO,
        MANUAL
    }

    /**
     * Status of the handover.
     */
    public enum Status {
        PENDING,
        ACKNOWLEDGED,
        COMPLETED
    }
}
