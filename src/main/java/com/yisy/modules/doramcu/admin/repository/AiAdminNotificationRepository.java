package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AI admin notifications.
 */
@Repository
public interface AiAdminNotificationRepository extends JpaRepository<AiAdminNotification, Long> {

    /**
     * Find all notifications for a specific AI admin, ordered by creation time descending.
     */
    List<AiAdminNotification> findByAiAdminIdOrderByCreatedAtDesc(Long aiAdminId);

    /**
     * Find unread notifications for a specific AI admin.
     */
    List<AiAdminNotification> findByAiAdminIdAndIsReadFalseOrderByCreatedAtDesc(Long aiAdminId);

    /**
     * Count unread notifications for a specific AI admin.
     */
    long countByAiAdminIdAndIsReadFalse(Long aiAdminId);

    /**
     * Find notifications by type for a specific AI admin.
     */
    List<AiAdminNotification> findByAiAdminIdAndNotificationTypeOrderByCreatedAtDesc(
            Long aiAdminId, AiAdminNotificationType notificationType);

    /**
     * Find notifications related to a specific order.
     */
    List<AiAdminNotification> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * Mark all notifications as read for a specific AI admin.
     */
    @Modifying
    @Query("UPDATE AiAdminNotification n SET n.isRead = true, n.readAt = :now " +
           "WHERE n.aiAdminId = :aiAdminId AND n.isRead = false")
    int markAllAsRead(@Param("aiAdminId") Long aiAdminId, @Param("now") LocalDateTime now);

    /**
     * Mark a single notification as read.
     */
    @Modifying
    @Query("UPDATE AiAdminNotification n SET n.isRead = true, n.readAt = :now " +
           "WHERE n.id = :id AND n.aiAdminId = :aiAdminId AND n.isRead = false")
    int markAsRead(@Param("id") Long id, @Param("aiAdminId") Long aiAdminId,
                   @Param("now") LocalDateTime now);

    /**
     * Delete expired notifications (expires_at is in the past).
     */
    @Modifying
    @Query("DELETE FROM AiAdminNotification n WHERE n.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    /**
     * Delete old read notifications to prevent unbounded growth.
     * Keeps only the last 30 days of read notifications.
     */
    @Modifying
    @Query("DELETE FROM AiAdminNotification n WHERE n.isRead = true AND n.createdAt < :cutoff")
    int deleteOldReadNotifications(@Param("cutoff") LocalDateTime cutoff);
}
