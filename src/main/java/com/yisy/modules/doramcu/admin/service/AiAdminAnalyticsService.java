package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for computing AI admin performance analytics.
 * Provides admin performance metrics, leaderboard ranking, order trends,
 * category breakdown, and system health summaries.
 */
@Service
public class AiAdminAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminAnalyticsService.class);

    private final AiAdminOrderRepository orderRepository;
    private final AiAdminTestReportRepository reportRepository;
    private final AiCharacterRepository characterRepository;

    public AiAdminAnalyticsService(AiAdminOrderRepository orderRepository,
                                   AiAdminTestReportRepository reportRepository,
                                   AiCharacterRepository characterRepository) {
        this.orderRepository = orderRepository;
        this.reportRepository = reportRepository;
        this.characterRepository = characterRepository;
    }

    // --- DTOs ---

    /** Performance metrics for a single AI admin. */
    public static class AdminPerformance {
        public Long adminId;
        public String adminName;
        public String role;
        public String empireName;
        public int totalOrders;
        public int completedOrders;
        public int failedOrders;
        public double successRate;
        public long avgCompletionTimeMs;
        public int totalTestsPassed;
        public int totalTestsFailed;
        public int totalTestsSkipped;
        public int totalExperience;
        public int recentOrders;

        public AdminPerformance() {
        }
    }

    /** A single entry in the admin leaderboard. */
    public static class AdminLeaderboardEntry {
        public int rank;
        public Long adminId;
        public String adminName;
        public String role;
        public String empireName;
        public double successRate;
        public int totalOrders;
        public int completedOrders;
        public int totalExperience;

        public AdminLeaderboardEntry() {
        }
    }

    /** Daily order counts for trend analysis. */
    public static class OrderTrend {
        public String date;
        public int pending;
        public int running;
        public int completed;
        public int failed;

        public OrderTrend() {
        }
    }

    /** Pass/fail counts grouped by test category. */
    public static class CategoryBreakdown {
        public String category;
        public int totalOrders;
        public int passed;
        public int failed;
        public int skipped;

        public CategoryBreakdown() {
        }
    }

    /** Overall system health summary. */
    public static class SystemHealth {
        public int totalAdmins;
        public int availableAdmins;
        public int busyAdmins;
        public int pendingOrders;
        public int runningOrders;
        public int completedOrders;
        public int failedOrders;
        public double overallSuccessRate;
        public long avgCompletionTimeMs;

        public SystemHealth() {
        }
    }

    // --- Public methods ---

    /**
     * Get performance metrics for a specific AI admin.
     */
    @Transactional(readOnly = true)
    public AdminPerformance getAdminPerformance(Long adminId) {
        AiCharacter admin = characterRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("AI admin not found: " + adminId));

        AdminPerformance perf = new AdminPerformance();
        perf.adminId = admin.getId();
        perf.adminName = admin.getName();
        perf.role = admin.getRole();
        perf.empireName = admin.getEmpireName();
        perf.totalExperience = admin.getTotalExperience();

        // Count orders by status
        perf.completedOrders = (int) orderRepository.countByAssignedAiAdminIdAndStatus(adminId, AiAdminOrder.OrderStatus.COMPLETED);
        perf.failedOrders = (int) orderRepository.countByAssignedAiAdminIdAndStatus(adminId, AiAdminOrder.OrderStatus.FAILED);
        perf.totalOrders = perf.completedOrders + perf.failedOrders;
        perf.successRate = perf.totalOrders > 0 ? (double) perf.completedOrders / perf.totalOrders * 100.0 : 0.0;

        // Average completion time
        Double avgMs = orderRepository.avgCompletionDurationMsByAdminId(adminId);
        perf.avgCompletionTimeMs = avgMs != null ? avgMs.longValue() : 0L;

        // Get recent orders for test report aggregation
        List<AiAdminOrder> recentOrders = orderRepository.findByAssignedAiAdminIdOrderByCreatedAtDesc(adminId, PageRequest.of(0, 50));
        perf.recentOrders = recentOrders.size();

        // Aggregate test report data
        if (!recentOrders.isEmpty()) {
            List<Long> orderIds = recentOrders.stream()
                    .map(AiAdminOrder::getId)
                    .collect(Collectors.toList());
            List<AiAdminTestReport> reports = reportRepository.findByOrderIdIn(orderIds);
            for (AiAdminTestReport report : reports) {
                perf.totalTestsPassed += report.getPassed();
                perf.totalTestsFailed += report.getFailed();
                perf.totalTestsSkipped += report.getSkipped();
            }
        }

        return perf;
    }

    /**
     * Get leaderboard of all AI admins ranked by success rate, then by total experience.
     */
    @Transactional(readOnly = true)
    public List<AdminLeaderboardEntry> getAdminLeaderboard() {
        List<AiCharacter> admins = characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc();

        List<AdminLeaderboardEntry> entries = new ArrayList<>();
        for (AiCharacter admin : admins) {
            AdminLeaderboardEntry entry = new AdminLeaderboardEntry();
            entry.adminId = admin.getId();
            entry.adminName = admin.getName();
            entry.role = admin.getRole();
            entry.empireName = admin.getEmpireName();
            entry.totalExperience = admin.getTotalExperience();

            int completed = (int) orderRepository.countByAssignedAiAdminIdAndStatus(admin.getId(), AiAdminOrder.OrderStatus.COMPLETED);
            int failed = (int) orderRepository.countByAssignedAiAdminIdAndStatus(admin.getId(), AiAdminOrder.OrderStatus.FAILED);
            entry.completedOrders = completed;
            entry.totalOrders = completed + failed;
            entry.successRate = entry.totalOrders > 0 ? (double) completed / entry.totalOrders * 100.0 : 0.0;

            entries.add(entry);
        }

        // Sort by success rate descending, then by total experience descending
        entries.sort((a, b) -> {
            int cmp = Double.compare(b.successRate, a.successRate);
            if (cmp != 0) return cmp;
            return Integer.compare(b.totalExperience, a.totalExperience);
        });

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).rank = i + 1;
        }

        return entries;
    }

    /**
     * Get order trends over the last N days.
     * Returns daily counts of orders by status.
     */
    @Transactional(readOnly = true)
    public List<OrderTrend> getOrderTrends(int days) {
        if (days <= 0) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        List<OrderTrend> trends = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            OrderTrend trend = new OrderTrend();
            trend.date = dayStart.toLocalDate().toString();

            // Query orders in this day range
            List<Object[]> results = orderRepository.countOrdersByStatusAndDateRange(dayStart, dayEnd);
            Map<String, Long> statusCounts = new LinkedHashMap<>();
            for (Object[] row : results) {
                statusCounts.put(row[0].toString(), (Long) row[1]);
            }

            trend.pending = statusCounts.getOrDefault("PENDING", 0L).intValue();
            trend.running = statusCounts.getOrDefault("RUNNING", 0L).intValue();
            trend.completed = statusCounts.getOrDefault("COMPLETED", 0L).intValue();
            trend.failed = statusCounts.getOrDefault("FAILED", 0L).intValue();

            trends.add(trend);
        }

        return trends;
    }

    /**
     * Get test results broken down by test category.
     * Groups orders by the category keywords found in their order text.
     */
    @Transactional(readOnly = true)
    public List<CategoryBreakdown> getCategoryBreakdown() {
        List<AiAdminOrder> allOrders = orderRepository.findAll();

        // Group orders by category
        Map<String, List<AiAdminOrder>> categoryMap = new LinkedHashMap<>();
        for (AiAdminOrder order : allOrders) {
            String category = extractCategory(order.getOrderText());
            categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(order);
        }

        // Get all reports for batch lookup
        List<Long> allOrderIds = allOrders.stream().map(AiAdminOrder::getId).collect(Collectors.toList());
        List<AiAdminTestReport> allReports = reportRepository.findByOrderIdIn(allOrderIds);
        Map<Long, AiAdminTestReport> reportByOrderId = allReports.stream()
                .collect(Collectors.toMap(AiAdminTestReport::getOrderId, r -> r, (a, b) -> a));

        List<CategoryBreakdown> breakdowns = new ArrayList<>();
        for (Map.Entry<String, List<AiAdminOrder>> entry : categoryMap.entrySet()) {
            CategoryBreakdown cb = new CategoryBreakdown();
            cb.category = entry.getKey();
            cb.totalOrders = entry.getValue().size();

            for (AiAdminOrder order : entry.getValue()) {
                AiAdminTestReport report = reportByOrderId.get(order.getId());
                if (report != null) {
                    cb.passed += report.getPassed();
                    cb.failed += report.getFailed();
                    cb.skipped += report.getSkipped();
                }
            }

            breakdowns.add(cb);
        }

        return breakdowns;
    }

    /**
     * Get overall system health summary.
     */
    @Transactional(readOnly = true)
    public SystemHealth getSystemHealth() {
        SystemHealth health = new SystemHealth();

        // Admin counts
        List<AiCharacter> allAdmins = characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc();
        health.totalAdmins = allAdmins.size();
        health.availableAdmins = characterRepository.findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNull().size();
        health.busyAdmins = health.totalAdmins - health.availableAdmins;

        // Order counts
        health.pendingOrders = (int) orderRepository.countByStatus(AiAdminOrder.OrderStatus.PENDING);
        health.runningOrders = (int) orderRepository.countByStatus(AiAdminOrder.OrderStatus.RUNNING);
        health.completedOrders = (int) orderRepository.countByStatus(AiAdminOrder.OrderStatus.COMPLETED);
        health.failedOrders = (int) orderRepository.countByStatus(AiAdminOrder.OrderStatus.FAILED);

        // Overall success rate
        int totalCompleted = health.completedOrders;
        int totalFailed = health.failedOrders;
        int totalFinished = totalCompleted + totalFailed;
        health.overallSuccessRate = totalFinished > 0 ? (double) totalCompleted / totalFinished * 100.0 : 0.0;

        // Average completion time across all completed orders
        List<AiAdminOrder> completedOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.COMPLETED);
        if (!completedOrders.isEmpty()) {
            long totalMs = 0;
            int count = 0;
            for (AiAdminOrder order : completedOrders) {
                if (order.getCreatedAt() != null && order.getCompletedAt() != null) {
                    totalMs += Duration.between(order.getCreatedAt(), order.getCompletedAt()).toMillis();
                    count++;
                }
            }
            health.avgCompletionTimeMs = count > 0 ? totalMs / count : 0L;
        }

        return health;
    }

    // --- Helper methods ---

    /**
     * Extract the test category from order text.
     * Looks for known category keywords: API, GAME, AI_CHAT, AUTH.
     */
    String extractCategory(String orderText) {
        if (orderText == null || orderText.isBlank()) {
            return "UNKNOWN";
        }
        String upper = orderText.toUpperCase();
        if (upper.contains("AI_CHAT")) return "AI_CHAT";
        if (upper.contains("AUTH")) return "AUTH";
        if (upper.contains("GAME")) return "GAME";
        if (upper.contains("API")) return "API";
        return "OTHER";
    }
}
