package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminNotificationRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing AI admin notifications.
 * Provides convenience methods for creating notifications for common dispatch events
 * and handles notification cleanup to prevent unbounded growth.
 */
@Service
public class AiAdminNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminNotificationService.class);

    /** Notifications expire after 7 days by default. */
    public static final int NOTIFICATION_EXPIRY_DAYS = 7;

    /** Delete read notifications older than 30 days. */
    public static final int READ_NOTIFICATION_RETENTION_DAYS = 30;

    private final AiAdminNotificationRepository notificationRepository;

    public AiAdminNotificationService(AiAdminNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ------------------------------------------------------------------
    // Convenience methods for creating notifications
    // ------------------------------------------------------------------

    /**
     * Creates a notification when an order is assigned to an AI admin.
     */
    @Transactional
    public AiAdminNotification notifyOrderAssigned(AiCharacter admin, AiAdminOrder order) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.ORDER_ASSIGNED);
        notification.setTitle("New work order assigned");
        notification.setMessage(String.format(
                "Order #%d has been assigned to you. Category: %s",
                order.getId(), extractCategory(order.getOrderText())));
        notification.setOrderId(order.getId());
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    /**
     * Creates a notification when an order is completed successfully.
     */
    @Transactional
    public AiAdminNotification notifyOrderCompleted(AiCharacter admin, AiAdminOrder order,
                                                     AiAdminTestReport report) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.ORDER_COMPLETED);
        notification.setTitle("Work order completed");
        String message;
        if (report != null) {
            message = String.format(
                    "Order #%d completed: %d passed, %d failed. Earned %d XP.",
                    order.getId(), report.getPassed(), report.getFailed(),
                    10 + report.getPassed());
        } else {
            message = String.format("Order #%d completed successfully.", order.getId());
        }
        notification.setMessage(message);
        notification.setOrderId(order.getId());
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    /**
     * Creates a notification when an order fails.
     */
    @Transactional
    public AiAdminNotification notifyOrderFailed(AiCharacter admin, AiAdminOrder order,
                                                  String reason) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.ORDER_FAILED);
        notification.setTitle("Work order failed");
        notification.setMessage(String.format(
                "Order #%d failed: %s (Retry %d/%d)",
                order.getId(), reason != null ? reason : "Unknown error",
                order.getRetryCount(), order.getMaxRetries()));
        notification.setOrderId(order.getId());
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    /**
     * Creates a notification when a failed order is being retried.
     */
    @Transactional
    public AiAdminNotification notifyOrderRetried(AiCharacter admin, AiAdminOrder order) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.ORDER_RETRIED);
        notification.setTitle("Work order retry");
        notification.setMessage(String.format(
                "Order #%d is being retried (attempt %d/%d).",
                order.getId(), order.getRetryCount(), order.getMaxRetries()));
        notification.setOrderId(order.getId());
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    /**
     * Creates a notification when a stale order is reclaimed.
     */
    @Transactional
    public AiAdminNotification notifyOrderReclaimed(AiCharacter admin, AiAdminOrder order) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.ORDER_STALE_RECLAIMED);
        notification.setTitle("Stale order reclaimed");
        notification.setMessage(String.format(
                "Order #%d was stuck in RUNNING and has been reclaimed for reassignment.",
                order.getId()));
        notification.setOrderId(order.getId());
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    /**
     * Creates a notification when an AI admin approaches the max order limit.
     */
    @Transactional
    public AiAdminNotification notifyWorkloadWarning(AiCharacter admin, int currentLoad,
                                                      int maxLoad) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.WORKLOAD_WARNING);
        notification.setTitle("Workload warning");
        notification.setMessage(String.format(
                "You are currently handling %d/%d concurrent orders. " +
                "New assignments may be delayed until you free up capacity.",
                currentLoad, maxLoad));
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    /**
     * Creates a notification when an AI admin reaches an XP milestone.
     */
    @Transactional
    public AiAdminNotification notifyXpMilestone(AiCharacter admin, int xpEarned,
                                                   int totalXp) {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(admin.getId());
        notification.setNotificationType(AiAdminNotificationType.XP_MILESTONE);
        notification.setTitle("Experience milestone");
        notification.setMessage(String.format(
                "You earned %d XP! Total experience: %d", xpEarned, totalXp));
        notification.setExpiresAt(LocalDateTime.now().plusDays(NOTIFICATION_EXPIRY_DAYS));
        return notificationRepository.save(notification);
    }

    // ------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------

    /**
     * Returns all notifications for an AI admin, ordered by creation time descending.
     */
    @Transactional(readOnly = true)
    public List<AiAdminNotification> getNotificationsForAdmin(Long aiAdminId) {
        return notificationRepository.findByAiAdminIdOrderByCreatedAtDesc(aiAdminId);
    }

    /**
     * Returns unread notifications for an AI admin.
     */
    @Transactional(readOnly = true)
    public List<AiAdminNotification> getUnreadNotificationsForAdmin(Long aiAdminId) {
        return notificationRepository.findByAiAdminIdAndIsReadFalseOrderByCreatedAtDesc(aiAdminId);
    }

    /**
     * Returns the count of unread notifications for an AI admin.
     */
    @Transactional(readOnly = true)
    public long getUnreadCountForAdmin(Long aiAdminId) {
        return notificationRepository.countByAiAdminIdAndIsReadFalse(aiAdminId);
    }

    /**
     * Marks a single notification as read.
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long aiAdminId) {
        int updated = notificationRepository.markAsRead(notificationId, aiAdminId, LocalDateTime.now());
        return updated > 0;
    }

    /**
     * Marks all notifications for an AI admin as read.
     */
    @Transactional
    public int markAllAsRead(Long aiAdminId) {
        return notificationRepository.markAllAsRead(aiAdminId, LocalDateTime.now());
    }

    // ------------------------------------------------------------------
    // Scheduled cleanup
    // ------------------------------------------------------------------

    /**
     * Runs daily to clean up expired and old read notifications.
     */
    @Scheduled(fixedRate = 86400000) // 24 hours
    @Transactional
    public void cleanupOldNotifications() {
        int expired = notificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        int oldRead = notificationRepository.deleteByIsReadTrueAndCreatedAtBefore(
                LocalDateTime.now().minusDays(READ_NOTIFICATION_RETENTION_DAYS));
        if (expired > 0 || oldRead > 0) {
            log.info("Notification cleanup: deleted {} expired and {} old read notifications",
                    expired, oldRead);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String extractCategory(String orderText) {
        if (orderText == null || orderText.isBlank()) {
            return "ALL";
        }
        // Return the first category found, or "ALL" if none
        String[] categories = {"API", "GAME", "AI_CHAT", "AUTH"};
        for (String cat : categories) {
            if (orderText.toUpperCase().contains(cat)) {
                return cat;
            }
        }
        return "ALL";
    }
}
