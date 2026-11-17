package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a notification for an AI admin character.
 * Notifications are created when work orders are assigned, completed, failed,
 * or when other significant events occur in the dispatch system.
 */
@Entity
@Table(name = "ai_admin_notifications", indexes = {
        @Index(name = "idx_aan_admin", columnList = "ai_admin_id"),
        @Index(name = "idx_aan_type", columnList = "notification_type"),
        @Index(name = "idx_aan_read", columnList = "is_read"),
        @Index(name = "idx_aan_created", columnList = "created_at"),
        @Index(name = "idx_aan_order", columnList = "order_id")
})
public class AiAdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The AI admin character that receives this notification.
     */
    @Column(name = "ai_admin_id", nullable = false)
    private Long aiAdminId;

    /**
     * Type of notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private AiAdminNotificationType notificationType;

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
     * Related work order ID, if applicable.
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * Whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

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

    public AiAdminNotification() {
        this.createdAt = LocalDateTime.now();
    }

    public AiAdminNotification(Long aiAdminId, AiAdminNotificationType notificationType,
                                String title, String message) {
        this();
        this.aiAdminId = aiAdminId;
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

    public Long getAiAdminId() {
        return aiAdminId;
    }

    public void setAiAdminId(Long aiAdminId) {
        this.aiAdminId = aiAdminId;
    }

    public AiAdminNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(AiAdminNotificationType notificationType) {
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
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
}
