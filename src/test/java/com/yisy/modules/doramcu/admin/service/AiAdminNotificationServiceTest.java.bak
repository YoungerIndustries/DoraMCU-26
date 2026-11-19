package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminNotificationRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiAdminNotificationService Tests")
class AiAdminNotificationServiceTest {

    @Mock
    private AiAdminNotificationRepository notificationRepository;

    @InjectMocks
    private AiAdminNotificationService notificationService;

    private AiCharacter admin;
    private AiAdminOrder order;
    private AiAdminTestReport report;

    @BeforeEach
    void setUp() {
        admin = new AiCharacter();
        admin.setId(1L);
        admin.setName("Test Admin");

        order = new AiAdminOrder();
        order.setId(100L);
        order.setOrderText("API: Test endpoint /health");
        order.setRetryCount(1);
        order.setMaxRetries(3);

        report = new AiAdminTestReport();
        report.setOrderId(100L);
        report.setPassed(5);
        report.setFailed(2);
    }

    // ------------------------------------------------------------------
    // Notification creation tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("notifyOrderAssigned should create ORDER_ASSIGNED notification")
    void notifyOrderAssigned_shouldCreateNotification() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertNotNull(result);
        assertEquals(1L, result.getAiAdminId());
        assertEquals(AiAdminNotificationType.ORDER_ASSIGNED, result.getNotificationType());
        assertEquals("New work order assigned", result.getTitle());
        assertTrue(result.getMessage().contains("Order #100"));
        assertTrue(result.getMessage().contains("API"));
        assertEquals(100L, result.getOrderId());
        assertNotNull(result.getExpiresAt());
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyOrderCompleted with report should include test results")
    void notifyOrderCompleted_withReport_shouldIncludeResults() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderCompleted(admin, order, report);

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.ORDER_COMPLETED, result.getNotificationType());
        assertEquals("Work order completed", result.getTitle());
        assertTrue(result.getMessage().contains("5 passed"));
        assertTrue(result.getMessage().contains("2 failed"));
        assertTrue(result.getMessage().contains("Earned 15 XP"));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyOrderCompleted without report should create simple notification")
    void notifyOrderCompleted_withoutReport_shouldCreateSimple() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderCompleted(admin, order, null);

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.ORDER_COMPLETED, result.getNotificationType());
        assertTrue(result.getMessage().contains("Order #100 completed successfully."));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyOrderFailed should create ORDER_FAILED notification")
    void notifyOrderFailed_shouldCreateNotification() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderFailed(admin, order, "Connection timeout");

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.ORDER_FAILED, result.getNotificationType());
        assertEquals("Work order failed", result.getTitle());
        assertTrue(result.getMessage().contains("Connection timeout"));
        assertTrue(result.getMessage().contains("Retry 1/3"));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyOrderFailed with null reason should use Unknown error")
    void notifyOrderFailed_nullReason_shouldUseUnknown() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderFailed(admin, order, null);

        assertNotNull(result);
        assertTrue(result.getMessage().contains("Unknown error"));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyOrderRetried should create ORDER_RETRIED notification")
    void notifyOrderRetried_shouldCreateNotification() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderRetried(admin, order);

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.ORDER_RETRIED, result.getNotificationType());
        assertEquals("Work order retry", result.getTitle());
        assertTrue(result.getMessage().contains("attempt 1/3"));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyOrderReclaimed should create ORDER_STALE_RECLAIMED notification")
    void notifyOrderReclaimed_shouldCreateNotification() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderReclaimed(admin, order);

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.ORDER_STALE_RECLAIMED, result.getNotificationType());
        assertEquals("Stale order reclaimed", result.getTitle());
        assertTrue(result.getMessage().contains("stuck in RUNNING"));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyWorkloadWarning should create WORKLOAD_WARNING notification")
    void notifyWorkloadWarning_shouldCreateNotification() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyWorkloadWarning(admin, 2, 3);

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.WORKLOAD_WARNING, result.getNotificationType());
        assertEquals("Workload warning", result.getTitle());
        assertTrue(result.getMessage().contains("2/3 concurrent orders"));
        assertNull(result.getOrderId()); // No order ID for workload warnings
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    @Test
    @DisplayName("notifyXpMilestone should create XP_MILESTONE notification")
    void notifyXpMilestone_shouldCreateNotification() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyXpMilestone(admin, 15, 150);

        assertNotNull(result);
        assertEquals(AiAdminNotificationType.XP_MILESTONE, result.getNotificationType());
        assertEquals("Experience milestone", result.getTitle());
        assertTrue(result.getMessage().contains("15 XP"));
        assertTrue(result.getMessage().contains("Total experience: 150"));
        verify(notificationRepository).save(any(AiAdminNotification.class));
    }

    // ------------------------------------------------------------------
    // Query method tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getNotificationsForAdmin should return all notifications")
    void getNotificationsForAdmin_shouldReturnAll() {
        List<AiAdminNotification> notifications = List.of(
                new AiAdminNotification(1L, AiAdminNotificationType.ORDER_ASSIGNED, "Title1", "Msg1"),
                new AiAdminNotification(1L, AiAdminNotificationType.ORDER_COMPLETED, "Title2", "Msg2")
        );
        when(notificationRepository.findByAiAdminIdOrderByCreatedAtDesc(1L))
                .thenReturn(notifications);

        List<AiAdminNotification> result = notificationService.getNotificationsForAdmin(1L);

        assertEquals(2, result.size());
        verify(notificationRepository).findByAiAdminIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("getNotificationsForAdmin should return empty list when none")
    void getNotificationsForAdmin_empty_shouldReturnEmpty() {
        when(notificationRepository.findByAiAdminIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<AiAdminNotification> result = notificationService.getNotificationsForAdmin(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getUnreadNotificationsForAdmin should return only unread")
    void getUnreadNotificationsForAdmin_shouldReturnUnread() {
        AiAdminNotification unread = new AiAdminNotification(1L, AiAdminNotificationType.ORDER_ASSIGNED, "Unread", "Msg");
        when(notificationRepository.findByAiAdminIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(unread));

        List<AiAdminNotification> result = notificationService.getUnreadNotificationsForAdmin(1L);

        assertEquals(1, result.size());
        assertEquals("Unread", result.get(0).getTitle());
    }

    @Test
    @DisplayName("getUnreadCountForAdmin should return count")
    void getUnreadCountForAdmin_shouldReturnCount() {
        when(notificationRepository.countByAiAdminIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCountForAdmin(1L);

        assertEquals(5, count);
    }

    // ------------------------------------------------------------------
    // Mark as read tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("markAsRead should return true when updated")
    void markAsRead_shouldReturnTrue() {
        when(notificationRepository.markAsRead(eq(10L), eq(1L), any(LocalDateTime.class))).thenReturn(1);

        boolean result = notificationService.markAsRead(10L, 1L);

        assertTrue(result);
        verify(notificationRepository).markAsRead(eq(10L), eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("markAsRead should return false when not updated")
    void markAsRead_shouldReturnFalse() {
        when(notificationRepository.markAsRead(eq(10L), eq(1L), any(LocalDateTime.class))).thenReturn(0);

        boolean result = notificationService.markAsRead(10L, 1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("markAllAsRead should return count of updated")
    void markAllAsRead_shouldReturnCount() {
        when(notificationRepository.markAllAsRead(eq(1L), any(LocalDateTime.class))).thenReturn(3);

        int count = notificationService.markAllAsRead(1L);

        assertEquals(3, count);
        verify(notificationRepository).markAllAsRead(eq(1L), any(LocalDateTime.class));
    }

    // ------------------------------------------------------------------
    // Cleanup tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("cleanupOldNotifications should delete expired and old read")
    void cleanupOldNotifications_shouldDeleteBoth() {
        when(notificationRepository.deleteExpired(any(LocalDateTime.class))).thenReturn(2);
        when(notificationRepository.deleteOldReadNotifications(any(LocalDateTime.class))).thenReturn(5);

        notificationService.cleanupOldNotifications();

        verify(notificationRepository).deleteExpired(any(LocalDateTime.class));
        verify(notificationRepository).deleteOldReadNotifications(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("cleanupOldNotifications should handle zero deletions")
    void cleanupOldNotifications_zeroDeletions_shouldNotLog() {
        when(notificationRepository.deleteExpired(any(LocalDateTime.class))).thenReturn(0);
        when(notificationRepository.deleteOldReadNotifications(any(LocalDateTime.class))).thenReturn(0);

        notificationService.cleanupOldNotifications();

        verify(notificationRepository).deleteExpired(any(LocalDateTime.class));
        verify(notificationRepository).deleteOldReadNotifications(any(LocalDateTime.class));
    }

    // ------------------------------------------------------------------
    // Category extraction tests (via notifyOrderAssigned)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("extractCategory should detect API category")
    void extractCategory_shouldDetectAPI() {
        order.setOrderText("API: Test endpoint");
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("API"));
    }

    @Test
    @DisplayName("extractCategory should detect GAME category")
    void extractCategory_shouldDetectGAME() {
        order.setOrderText("GAME: Battle simulation");
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("GAME"));
    }

    @Test
    @DisplayName("extractCategory should detect AI_CHAT category")
    void extractCategory_shouldDetectAI_CHAT() {
        order.setOrderText("AI_CHAT: Conversation test");
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("AI_CHAT"));
    }

    @Test
    @DisplayName("extractCategory should detect AUTH category")
    void extractCategory_shouldDetectAUTH() {
        order.setOrderText("AUTH: Login test");
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("AUTH"));
    }

    @Test
    @DisplayName("extractCategory should return ALL for unknown category")
    void extractCategory_unknown_shouldReturnALL() {
        order.setOrderText("Unknown category test");
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("ALL"));
    }

    @Test
    @DisplayName("extractCategory should return ALL for null order text")
    void extractCategory_null_shouldReturnALL() {
        order.setOrderText(null);
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("ALL"));
    }

    @Test
    @DisplayName("extractCategory should return ALL for blank order text")
    void extractCategory_blank_shouldReturnALL() {
        order.setOrderText("   ");
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertTrue(result.getMessage().contains("ALL"));
    }

    // ------------------------------------------------------------------
    // Expiry time tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Notifications should expire after 7 days")
    void notification_expiry_shouldBe7Days() {
        when(notificationRepository.save(any(AiAdminNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiAdminNotification result = notificationService.notifyOrderAssigned(admin, order);

        assertNotNull(result.getExpiresAt());
        LocalDateTime expectedExpiry = LocalDateTime.now().plusDays(7);
        // Allow 1-day tolerance for timing
        assertTrue(result.getExpiresAt().isBefore(expectedExpiry.plusDays(1))
                && result.getExpiresAt().isAfter(expectedExpiry.minusDays(1)));
    }
}
