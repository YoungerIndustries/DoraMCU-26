package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a notification for a corporation in the Younger Industries HQ.
 */
@Entity
@Table(name = "hq_notifications", indexes = {
        @Index(name = "idx_hn_corporation", columnList = "corporation_id"),
        @Index(name = "idx_hn_type", columnList = "notification_type"),
        @Index(name = "idx_hn_read", columnList = "is_read"),
        @Index(name = "idx_hn_created", columnList = "created_at")
})
public class HqNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Corporation that receives this notification.
     */
    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    /**
     * Type of notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private HqNotificationType notificationType;

    /**
     * Short title of the notification.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Detailed message body.
     */
    @Column(length = 2000)
    private String message;

    /**
     * Whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /**
     * Related office unit ID, if applicable.
     */
    @Column(name = "office_unit_id")
    private Long officeUnitId;

    /**
     * Related meeting ID, if applicable.
     */
    @Column(name = "meeting_id")
    private Long meetingId;

    /**
     * Related rent payment ID, if applicable.
     */
    @Column(name = "rent_payment_id")
    private Long rentPaymentId;

    /**
     * JSON payload for additional data.
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * When the notification was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the notification was read.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * When the notification expires and can be auto-deleted.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public HqNotification() {
        this.createdAt = LocalDateTime.now();
    }

    public HqNotification(Long corporationId, HqNotificationType notificationType, String title, String message) {
        this();
        this.corporationId = corporationId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
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

    public HqNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(HqNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        if (read) {
            this.readAt = LocalDateTime.now();
        }
    }

    public Long getOfficeUnitId() {
        return officeUnitId;
    }

    public void setOfficeUnitId(Long officeUnitId) {
        this.officeUnitId = officeUnitId;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public Long getRentPaymentId() {
        return rentPaymentId;
    }

    public void setRentPaymentId(Long rentPaymentId) {
        this.rentPaymentId = rentPaymentId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "HqNotification{id=" + id + ", corpId=" + corporationId +
                ", type=" + notificationType + ", title='" + title +
                "', read=" + isRead + "}";
    }
}
