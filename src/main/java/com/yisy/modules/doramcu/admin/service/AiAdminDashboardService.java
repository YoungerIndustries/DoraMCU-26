package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for the AI admin dispatch monitoring dashboard.
 * Aggregates data from dispatch, analytics, notifications, and progress services
 * into a unified dashboard view.
 */
@Service
public class AiAdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminDashboardService.class);

    private final AiAdminDispatchService dispatchService;
    private final AiAdminAnalyticsService analyticsService;
    private final AiAdminNotificationService notificationService;
    private final AiAdminOrderProgressService progressService;

    public AiAdminDashboardService(AiAdminDispatchService dispatchService,
                                   AiAdminAnalyticsService analyticsService,
                                   AiAdminNotificationService notificationService,
                                   AiAdminOrderProgressService progressService) {
        this.dispatchService = dispatchService;
        this.analyticsService = analyticsService;
        this.notificationService = notificationService;
        this.progressService = progressService;
    }

    // --- DTOs ---

    /** Unified dashboard response containing all monitoring data. */
    public static class DashboardSummary {
        public DispatchOverview dispatchOverview;
        public SystemHealthOverview systemHealth;
        public List<AdminOverview> admins;
        public List<String> galaxies;
        public NotificationOverview notifications;
        public List<AiAdminAnalyticsService.OrderTrend> recentTrends;
        public List<AiAdminAnalyticsService.CategoryBreakdown> categoryBreakdown;
        public List<AiAdminAnalyticsService.AdminLeaderboardEntry> topPerformers;
        public List<OrderProgressOverview> runningOrders;

        public DashboardSummary() {
        }
    }

    /** Dispatch queue overview. */
    public static class DispatchOverview {
        public int totalAiAdmins;
        public int availableAiAdmins;
        public int busyAiAdmins;
        public int totalActiveOrders;
        public int unassignedPendingOrders;
        public int assignedPendingOrders;
        public int runningOrders;
        public int executingOrders;

        public DispatchOverview() {
        }

        public static DispatchOverview from(AiAdminDispatchService.DispatchSummary summary) {
            DispatchOverview overview = new DispatchOverview();
            overview.totalAiAdmins = summary.getTotalAiAdmins();
            overview.availableAiAdmins = summary.getAvailableAiAdmins();
            overview.busyAiAdmins = summary.getBusyAiAdmins();
            overview.totalActiveOrders = summary.getTotalActiveOrders();
            overview.unassignedPendingOrders = summary.getUnassignedPendingOrders();
            overview.assignedPendingOrders = summary.getAssignedPendingOrders();
            overview.runningOrders = summary.getRunningOrders();
            overview.executingOrders = summary.getExecutingOrders();
            return overview;
        }
    }

    /** System health overview from analytics. */
    public static class SystemHealthOverview {
        public int totalAdmins;
        public int availableAdmins;
        public int busyAdmins;
        public int pendingOrders;
        public int runningOrders;
        public int completedOrders;
        public int failedOrders;
        public double overallSuccessRate;
        public long avgCompletionTimeMs;

        public SystemHealthOverview() {
        }

        public static SystemHealthOverview from(AiAdminAnalyticsService.SystemHealth health) {
            SystemHealthOverview overview = new SystemHealthOverview();
            overview.totalAdmins = health.totalAdmins;
            overview.availableAdmins = health.availableAdmins;
            overview.busyAdmins = health.busyAdmins;
            overview.pendingOrders = health.pendingOrders;
            overview.runningOrders = health.runningOrders;
            overview.completedOrders = health.completedOrders;
            overview.failedOrders = health.failedOrders;
            overview.overallSuccessRate = health.overallSuccessRate;
            overview.avgCompletionTimeMs = health.avgCompletionTimeMs;
            return overview;
        }
    }

    /** Admin overview with position and workload info. */
    public static class AdminOverview {
        public Long id;
        public String name;
        public String role;
        public String empireName;
        public String galaxyName;
        public String characterType;
        public Double posX;
        public Double posY;
        public Double posZ;
        public Double homePosX;
        public Double homePosY;
        public Double homePosZ;
        public Long currentOrderId;
        public int activeOrderCount;
        public boolean active;
        public String status;
        public int totalExperience;

        public AdminOverview() {
        }

        public static AdminOverview from(AiCharacter admin) {
            AdminOverview overview = new AdminOverview();
            overview.id = admin.getId();
            overview.name = admin.getName();
            overview.role = admin.getRole();
            overview.empireName = admin.getEmpireName();
            overview.galaxyName = admin.getGalaxyName();
            overview.characterType = admin.getCharacterType() != null ? admin.getCharacterType().name() : "HUMAN_LEADER";
            overview.posX = admin.getPosX();
            overview.posY = admin.getPosY();
            overview.posZ = admin.getPosZ();
            overview.homePosX = admin.getHomePosX();
            overview.homePosY = admin.getHomePosY();
            overview.homePosZ = admin.getHomePosZ();
            overview.currentOrderId = admin.getCurrentOrderId();
            overview.activeOrderCount = admin.getActiveOrderCount();
            overview.active = admin.isActive();
            overview.status = admin.getActiveOrderCount() >= AiAdminDispatchService.MAX_ORDERS_PER_ADMIN ? "busy" : "available";
            overview.totalExperience = admin.getTotalExperience();
            return overview;
        }
    }

    /** Notification summary across all admins. */
    public static class NotificationOverview {
        public int totalUnread;
        public Map<String, Integer> unreadByAdmin;
        public List<RecentNotification> recentNotifications;

        public NotificationOverview() {
        }
    }

    /** A single recent notification. */
    public static class RecentNotification {
        public Long id;
        public Long aiAdminId;
        public String notificationType;
        public String title;
        public String message;
        public Long orderId;
        public boolean isRead;
        public String createdAt;

        public RecentNotification() {
        }

        public static RecentNotification from(AiAdminNotification notification) {
            RecentNotification recent = new RecentNotification();
            recent.id = notification.getId();
            recent.aiAdminId = notification.getAiAdminId();
            recent.notificationType = notification.getNotificationType().name();
            recent.title = notification.getTitle();
            recent.message = notification.getMessage();
            recent.orderId = notification.getOrderId();
            recent.isRead = notification.isRead();
            recent.createdAt = notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null;
            return recent;
        }
    }

    /** Overview of a running order with progress info. */
    public static class OrderProgressOverview {
        public Long orderId;
        public String status;
        public int progressPercent;
        public String currentTestName;
        public int testsCompleted;
        public int testsTotal;
        public String orderText;
        public Long assignedAiAdminId;

        public OrderProgressOverview() {
        }
    }

    // --- Public methods ---

    /**
     * Get the full dashboard summary combining all monitoring data.
     */
    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary() {
        DashboardSummary dashboard = new DashboardSummary();

        // 1. Dispatch overview
        AiAdminDispatchService.DispatchSummary dispatchSummary = dispatchService.getDispatchSummary();
        dashboard.dispatchOverview = DispatchOverview.from(dispatchSummary);

        // 2. System health from analytics
        AiAdminAnalyticsService.SystemHealth health = analyticsService.getSystemHealth();
        dashboard.systemHealth = SystemHealthOverview.from(health);

        // 3. Admin list with positions
        List<AiCharacter> allAdmins = dispatchService.getAiAdminWorkload();
        dashboard.admins = allAdmins.stream()
                .map(AdminOverview::from)
                .collect(Collectors.toList());

        // 4. Unique galaxy names
        dashboard.galaxies = allAdmins.stream()
                .map(AiCharacter::getGalaxyName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 5. Notification summary
        dashboard.notifications = buildNotificationOverview(allAdmins);

        // 6. Recent trends (last 7 days)
        dashboard.recentTrends = analyticsService.getOrderTrends(7);

        // 7. Category breakdown
        dashboard.categoryBreakdown = analyticsService.getCategoryBreakdown();

        // 8. Top performers (top 5 from leaderboard)
        List<AiAdminAnalyticsService.AdminLeaderboardEntry> leaderboard = analyticsService.getAdminLeaderboard();
        dashboard.topPerformers = leaderboard.stream().limit(5).collect(Collectors.toList());

        // 9. Running orders with progress
        dashboard.runningOrders = buildRunningOrdersOverview();

        return dashboard;
    }

    /**
     * Get a lightweight health check for the dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        AiAdminAnalyticsService.SystemHealth systemHealth = analyticsService.getSystemHealth();
        health.put("totalAdmins", systemHealth.totalAdmins);
        health.put("availableAdmins", systemHealth.availableAdmins);
        health.put("pendingOrders", systemHealth.pendingOrders);
        health.put("runningOrders", systemHealth.runningOrders);
        health.put("overallSuccessRate", systemHealth.overallSuccessRate);
        health.put("avgCompletionTimeMs", systemHealth.avgCompletionTimeMs);
        return health;
    }

    // --- Helper methods ---

    private NotificationOverview buildNotificationOverview(List<AiCharacter> allAdmins) {
        NotificationOverview overview = new NotificationOverview();

        List<AiAdminNotification> unreadNotifications = allAdmins.stream()
                .flatMap(admin -> notificationService.getUnreadNotificationsForAdmin(admin.getId()).stream())
                .collect(Collectors.toList());

        overview.totalUnread = unreadNotifications.size();
        overview.unreadByAdmin = unreadNotifications.stream()
                .collect(Collectors.groupingBy(
                        n -> String.valueOf(n.getAiAdminId()),
                        Collectors.summingInt(n -> 1)
                ));

        // Get recent notifications (last 20 across all admins)
        List<AiAdminNotification> recentNotifications = allAdmins.stream()
                .flatMap(admin -> notificationService.getNotificationsForAdmin(admin.getId()).stream())
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(20)
                .collect(Collectors.toList());

        overview.recentNotifications = recentNotifications.stream()
                .map(RecentNotification::from)
                .collect(Collectors.toList());

        return overview;
    }

    private List<OrderProgressOverview> buildRunningOrdersOverview() {
        List<AiAdminOrderProgressService.OrderProgress> runningProgress = progressService.getRunningOrdersProgress();
        return runningProgress.stream().map(progress -> {
            OrderProgressOverview overview = new OrderProgressOverview();
            overview.orderId = progress.orderId;
            overview.status = progress.status;
            overview.progressPercent = progress.progressPercent;
            overview.currentTestName = progress.currentTestName;
            overview.testsCompleted = progress.testsCompleted;
            overview.testsTotal = progress.testsTotal;
            overview.orderText = progress.orderText;
            overview.assignedAiAdminId = progress.assignedAiAdminId;
            return overview;
        }).collect(Collectors.toList());
    }
}
