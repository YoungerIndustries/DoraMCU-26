package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAdminDashboardServiceTest {

    @Mock
    private AiAdminDispatchService dispatchService;

    @Mock
    private AiAdminAnalyticsService analyticsService;

    @Mock
    private AiAdminNotificationService notificationService;

    @Mock
    private AiAdminOrderProgressService progressService;

    @InjectMocks
    private AiAdminDashboardService dashboardService;

    private AiCharacter admin1;
    private AiCharacter admin2;
    private List<AiCharacter> admins;

    @BeforeEach
    void setUp() {
        admin1 = createAdmin(1L, "Alpha Overseer", "Galaxy Overseer", "Alpha Empire", "Alpha Centauri");
        admin2 = createAdmin(2L, "Beta Overseer", "Galaxy Overseer", "Beta Empire", "Beta Pictoris");
        admins = List.of(admin1, admin2);
    }

    @Test
    void getDashboardSummary_returnsCompleteDashboard() {
        // Given
        AiAdminDispatchService.DispatchSummary dispatchSummary = createDispatchSummary();
        when(dispatchService.getDispatchSummary()).thenReturn(dispatchSummary);
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);

        AiAdminAnalyticsService.SystemHealth health = createSystemHealth();
        when(analyticsService.getSystemHealth()).thenReturn(health);
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());

        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertNotNull(result);
        assertNotNull(result.dispatchOverview);
        assertNotNull(result.systemHealth);
        assertNotNull(result.admins);
        assertNotNull(result.galaxies);
        assertNotNull(result.notifications);
        assertNotNull(result.recentTrends);
        assertNotNull(result.categoryBreakdown);
        assertNotNull(result.topPerformers);
        assertNotNull(result.runningOrders);
    }

    @Test
    void getDashboardSummary_dispatchOverview_populatedCorrectly() {
        // Given
        AiAdminDispatchService.DispatchSummary dispatchSummary = createDispatchSummary();
        when(dispatchService.getDispatchSummary()).thenReturn(dispatchSummary);
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(10, result.dispatchOverview.totalAiAdmins);
        assertEquals(7, result.dispatchOverview.availableAiAdmins);
        assertEquals(3, result.dispatchOverview.busyAiAdmins);
        assertEquals(5, result.dispatchOverview.runningOrders);
    }

    @Test
    void getDashboardSummary_systemHealth_populatedCorrectly() {
        // Given
        AiAdminAnalyticsService.SystemHealth health = createSystemHealth();
        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(health);
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(10, result.systemHealth.totalAdmins);
        assertEquals(85.5, result.systemHealth.overallSuccessRate, 0.01);
        assertEquals(120000L, result.systemHealth.avgCompletionTimeMs);
    }

    @Test
    void getDashboardSummary_admins_mappedCorrectly() {
        // Given
        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(2, result.admins.size());
        assertEquals("Alpha Overseer", result.admins.get(0).name);
        assertEquals("Galaxy Overseer", result.admins.get(0).role);
        assertEquals("Alpha Empire", result.admins.get(0).empireName);
        assertEquals("available", result.admins.get(0).status);
    }

    @Test
    void getDashboardSummary_galaxies_sortedAndUnique() {
        // Given
        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(2, result.galaxies.size());
        assertEquals("Alpha Centauri", result.galaxies.get(0));
        assertEquals("Beta Pictoris", result.galaxies.get(1));
    }

    @Test
    void getDashboardSummary_notifications_withUnreadCount() {
        // Given
        AiAdminNotification notification = new AiAdminNotification();
        notification.setId(100L);
        notification.setAiAdminId(1L);
        notification.setNotificationType(AiAdminNotificationType.ORDER_ASSIGNED);
        notification.setTitle("Order Assigned");
        notification.setMessage("Order #5 assigned");
        notification.setOrderId(5L);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(1L)).thenReturn(List.of(notification));
        when(notificationService.getUnreadNotificationsForAdmin(2L)).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(1L)).thenReturn(List.of(notification));
        when(notificationService.getNotificationsForAdmin(2L)).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(1, result.notifications.totalUnread);
        assertEquals(1, result.notifications.unreadByAdmin.size());
        assertEquals(1, result.notifications.unreadByAdmin.get("1"));
        assertEquals(1, result.notifications.recentNotifications.size());
        assertEquals("ORDER_ASSIGNED", result.notifications.recentNotifications.get(0).notificationType);
    }

    @Test
    void getDashboardSummary_trends_fromAnalyticsService() {
        // Given
        List<AiAdminAnalyticsService.OrderTrend> trends = new ArrayList<>();
        AiAdminAnalyticsService.OrderTrend trend = new AiAdminAnalyticsService.OrderTrend();
        trend.date = "2026-11-17";
        trend.pending = 2;
        trend.running = 1;
        trend.completed = 5;
        trend.failed = 0;
        trends.add(trend);

        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(trends);
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(1, result.recentTrends.size());
        assertEquals("2026-11-17", result.recentTrends.get(0).date);
        assertEquals(5, result.recentTrends.get(0).completed);
    }

    @Test
    void getDashboardSummary_topPerformers_limitedToFive() {
        // Given
        List<AiAdminAnalyticsService.AdminLeaderboardEntry> leaderboard = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            AiAdminAnalyticsService.AdminLeaderboardEntry entry = new AiAdminAnalyticsService.AdminLeaderboardEntry();
            entry.adminId = (long) i;
            entry.adminName = "Admin " + i;
            entry.successRate = 100.0 - i;
            entry.totalExperience = 1000 - i * 10;
            leaderboard.add(entry);
        }

        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(leaderboard);
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(5, result.topPerformers.size());
        assertEquals(1L, result.topPerformers.get(0).adminId);
    }

    @Test
    void getDashboardSummary_runningOrders_fromProgressService() {
        // Given
        AiAdminOrderProgressService.OrderProgress progress = new AiAdminOrderProgressService.OrderProgress();
        progress.setOrderId(5L);
        progress.setStatus("RUNNING");
        progress.setProgressPercent(50);
        progress.setCurrentTestName("test_api_health");
        progress.setTestsCompleted(5);
        progress.setTestsTotal(10);
        progress.setOrderText("API health check");

        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(admins);
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(List.of(progress));

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(1, result.runningOrders.size());
        assertEquals(5L, result.runningOrders.get(0).orderId);
        assertEquals("RUNNING", result.runningOrders.get(0).status);
        assertEquals(50, result.runningOrders.get(0).progressPercent);
        assertEquals("test_api_health", result.runningOrders.get(0).currentTestName);
    }

    @Test
    void getDashboardSummary_emptyData_returnsEmptyCollections() {
        // Given
        AiAdminDispatchService.DispatchSummary dispatchSummary = new AiAdminDispatchService.DispatchSummary();
        when(dispatchService.getDispatchSummary()).thenReturn(dispatchSummary);
        when(dispatchService.getAiAdminWorkload()).thenReturn(new ArrayList<>());

        AiAdminAnalyticsService.SystemHealth health = new AiAdminAnalyticsService.SystemHealth();
        when(analyticsService.getSystemHealth()).thenReturn(health);
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertNotNull(result);
        assertTrue(result.admins.isEmpty());
        assertTrue(result.galaxies.isEmpty());
        assertEquals(0, result.notifications.totalUnread);
        assertTrue(result.recentTrends.isEmpty());
        assertTrue(result.categoryBreakdown.isEmpty());
        assertTrue(result.topPerformers.isEmpty());
        assertTrue(result.runningOrders.isEmpty());
    }

    @Test
    void getHealthCheck_returnsSummaryData() {
        // Given
        AiAdminAnalyticsService.SystemHealth health = createSystemHealth();
        when(analyticsService.getSystemHealth()).thenReturn(health);

        // When
        var result = dashboardService.getHealthCheck();

        // Then
        assertEquals(10, result.get("totalAdmins"));
        assertEquals(7, result.get("availableAdmins"));
        assertEquals(3, result.get("pendingOrders"));
        assertEquals(5, result.get("runningOrders"));
        assertEquals(85.5, result.get("overallSuccessRate"));
        assertEquals(120000L, result.get("avgCompletionTimeMs"));
    }

    @Test
    void adminOverview_busyStatus_whenMaxOrders() {
        // Given
        AiCharacter busyAdmin = createAdmin(1L, "Busy Admin", "Galaxy Overseer", "Alpha Empire", "Alpha Centauri");
        busyAdmin.setActiveOrderCount(AiAdminDispatchService.MAX_ORDERS_PER_ADMIN);
        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(List.of(busyAdmin));
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals("busy", result.admins.get(0).status);
    }

    @Test
    void adminOverview_availableStatus_whenBelowMaxOrders() {
        // Given
        AiCharacter availableAdmin = createAdmin(1L, "Available Admin", "Galaxy Overseer", "Alpha Empire", "Alpha Centauri");
        availableAdmin.setActiveOrderCount(1);
        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(List.of(availableAdmin));
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals("available", result.admins.get(0).status);
    }

    @Test
    void adminOverview_positionFields_mappedCorrectly() {
        // Given
        AiCharacter admin = createAdmin(1L, "Positioned Admin", "Galaxy Overseer", "Alpha Empire", "Alpha Centauri");
        admin.setPosX(100.5);
        admin.setPosY(200.5);
        admin.setPosZ(300.5);
        admin.setHomePosX(100.0);
        admin.setHomePosY(200.0);
        admin.setHomePosZ(300.0);
        when(dispatchService.getDispatchSummary()).thenReturn(createDispatchSummary());
        when(dispatchService.getAiAdminWorkload()).thenReturn(List.of(admin));
        when(analyticsService.getSystemHealth()).thenReturn(createSystemHealth());
        when(analyticsService.getOrderTrends(7)).thenReturn(new ArrayList<>());
        when(analyticsService.getCategoryBreakdown()).thenReturn(new ArrayList<>());
        when(analyticsService.getAdminLeaderboard()).thenReturn(new ArrayList<>());
        when(notificationService.getUnreadNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(notificationService.getNotificationsForAdmin(anyLong())).thenReturn(new ArrayList<>());
        when(progressService.getRunningOrdersProgress()).thenReturn(new ArrayList<>());

        // When
        AiAdminDashboardService.DashboardSummary result = dashboardService.getDashboardSummary();

        // Then
        assertEquals(100.5, result.admins.get(0).posX);
        assertEquals(200.5, result.admins.get(0).posY);
        assertEquals(300.5, result.admins.get(0).posZ);
        assertEquals(100.0, result.admins.get(0).homePosX);
    }

    // --- Helper methods ---

    private AiCharacter createAdmin(Long id, String name, String role, String empireName, String galaxyName) {
        AiCharacter admin = new AiCharacter();
        admin.setId(id);
        admin.setName(name);
        admin.setRole(role);
        admin.setEmpireName(empireName);
        admin.setGalaxyName(galaxyName);
        admin.setCharacterType(AiCharacter.CharacterType.HUMAN_LEADER);
        admin.setActive(true);
        admin.setAiAdmin(true);
        admin.setActiveOrderCount(0);
        admin.setTotalExperience(500);
        return admin;
    }

    private AiAdminDispatchService.DispatchSummary createDispatchSummary() {
        AiAdminDispatchService.DispatchSummary summary = new AiAdminDispatchService.DispatchSummary();
        summary.setTotalAiAdmins(10);
        summary.setAvailableAiAdmins(7);
        summary.setBusyAiAdmins(3);
        summary.setTotalActiveOrders(5);
        summary.setUnassignedPendingOrders(2);
        summary.setAssignedPendingOrders(1);
        summary.setRunningOrders(5);
        summary.setExecutingOrders(3);
        return summary;
    }

    private AiAdminAnalyticsService.SystemHealth createSystemHealth() {
        AiAdminAnalyticsService.SystemHealth health = new AiAdminAnalyticsService.SystemHealth();
        health.totalAdmins = 10;
        health.availableAdmins = 7;
        health.busyAdmins = 3;
        health.pendingOrders = 3;
        health.runningOrders = 5;
        health.completedOrders = 100;
        health.failedOrders = 15;
        health.overallSuccessRate = 85.5;
        health.avgCompletionTimeMs = 120000L;
        return health;
    }
}
