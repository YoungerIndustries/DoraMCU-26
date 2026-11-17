package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminHealthReport;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminHealthReportRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Service for XP (experience points) trend analytics for AI admins.
 * Provides XP time series, velocity calculations, milestone projections,
 * source breakdowns, admin comparisons, and milestone history.
 */
@Service
public class AiAdminXpTrendService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminXpTrendService.class);

    /** XP milestone thresholds. */
    private static final int[] XP_THRESHOLDS = {100, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000};

    /** XP awarded per completed order. */
    private static final int XP_PER_ORDER = 10;

    /** XP awarded per resolved incident. */
    private static final int XP_PER_INCIDENT = 25;

    private final AiAdminHealthReportRepository reportRepository;
    private final AiAdminOrderRepository orderRepository;
    private final AiAdminIncidentRepository incidentRepository;
    private final AiCharacterRepository characterRepository;

    public AiAdminXpTrendService(AiAdminHealthReportRepository reportRepository,
                                  AiAdminOrderRepository orderRepository,
                                  AiAdminIncidentRepository incidentRepository,
                                  AiCharacterRepository characterRepository) {
        this.reportRepository = reportRepository;
        this.orderRepository = orderRepository;
        this.incidentRepository = incidentRepository;
        this.characterRepository = characterRepository;
    }

    // --- Public methods ---

    /**
     * Get XP time series for an admin over N days.
     *
     * @param adminId the AI admin character ID
     * @param days number of days to look back
     * @return list of XP trend points ordered by date ascending
     */
    @Transactional(readOnly = true)
    public List<XpTrendPoint> getXpTrend(Long adminId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<AiAdminHealthReport> reports = reportRepository
                .findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(adminId, startDate, LocalDateTime.now());

        // Reverse to get ascending order
        List<XpTrendPoint> trend = new ArrayList<>();
        for (int i = reports.size() - 1; i >= 0; i--) {
            AiAdminHealthReport report = reports.get(i);
            XpTrendPoint point = new XpTrendPoint();
            point.date = report.getGeneratedAt().toLocalDate();
            point.xpEarned = report.getXpEarned();
            point.ordersCompleted = report.getOrdersCompleted();
            point.incidentsHandled = report.getIncidentsHandled();
            trend.add(point);
        }
        return trend;
    }

    /**
     * Get XP velocity (average XP per day/week) for an admin.
     *
     * @param adminId the AI admin character ID
     * @return XP velocity metrics
     */
    @Transactional(readOnly = true)
    public XpVelocity getXpVelocity(Long adminId) {
        List<AiAdminHealthReport> reports = reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId);

        XpVelocity velocity = new XpVelocity();
        velocity.adminId = adminId;

        if (reports.isEmpty()) {
            return velocity;
        }

        // Calculate total XP and time span
        int totalXp = 0;
        LocalDateTime firstReport = reports.get(reports.size() - 1).getGeneratedAt();
        LocalDateTime lastReport = reports.get(0).getGeneratedAt();

        for (AiAdminHealthReport report : reports) {
            totalXp += report.getXpEarned();
        }

        velocity.totalXp = totalXp;
        velocity.totalReports = reports.size();
        velocity.firstReportDate = firstReport;
        velocity.lastReportDate = lastReport;

        // Calculate days span
        long daysSpan = Duration.between(firstReport, lastReport).toDays();
        if (daysSpan <= 0) {
            daysSpan = 1; // At least 1 day to avoid division by zero
        }

        velocity.xpPerDay = (double) totalXp / daysSpan;
        velocity.xpPerWeek = velocity.xpPerDay * 7;
        velocity.xpPerReport = reports.size() > 0 ? (double) totalXp / reports.size() : 0.0;

        return velocity;
    }

    /**
     * Get XP milestone projection for an admin.
     * Estimates when the next XP milestone will be reached based on current velocity.
     *
     * @param adminId the AI admin character ID
     * @return XP projection
     */
    @Transactional(readOnly = true)
    public XpProjection getXpProjection(Long adminId) {
        XpVelocity velocity = getXpVelocity(adminId);

        XpProjection projection = new XpProjection();
        projection.adminId = adminId;
        projection.currentXp = velocity.totalXp;
        projection.xpPerDay = velocity.xpPerDay;

        // Find the next unachieved milestone
        int nextMilestone = -1;
        for (int threshold : XP_THRESHOLDS) {
            if (velocity.totalXp < threshold) {
                nextMilestone = threshold;
                break;
            }
        }

        if (nextMilestone == -1) {
            // All milestones achieved
            projection.nextMilestone = -1;
            projection.xpRemaining = 0;
            projection.estimatedDaysRemaining = 0;
            projection.allMilestonesAchieved = true;
        } else {
            projection.nextMilestone = nextMilestone;
            projection.xpRemaining = nextMilestone - velocity.totalXp;
            projection.allMilestonesAchieved = false;

            if (velocity.xpPerDay > 0) {
                projection.estimatedDaysRemaining = (int) Math.ceil((double) projection.xpRemaining / velocity.xpPerDay);
            } else {
                projection.estimatedDaysRemaining = -1; // Cannot estimate
            }
        }

        return projection;
    }

    /**
     * Get XP source breakdown for an admin over N days.
     * Breaks down XP by source: orders completed vs incidents resolved.
     *
     * @param adminId the AI admin character ID
     * @param days number of days to look back
     * @return XP source breakdown
     */
    @Transactional(readOnly = true)
    public XpSourceBreakdown getXpSourceBreakdown(Long adminId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        XpSourceBreakdown breakdown = new XpSourceBreakdown();
        breakdown.adminId = adminId;
        breakdown.days = days;

        // Count completed orders
        List<AiAdminOrder> orders = orderRepository
                .findByAssignedAiAdminIdAndCreatedAtBetween(adminId, startDate, LocalDateTime.now());
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus() == AiAdminOrder.OrderStatus.COMPLETED)
                .count();
        breakdown.xpFromOrders = (int) (completedOrders * XP_PER_ORDER);
        breakdown.ordersCompleted = (int) completedOrders;

        // Count resolved incidents
        List<AiAdminIncident> incidents = incidentRepository
                .findByAssignedAiAdminIdAndCreatedAtBetween(adminId, startDate, LocalDateTime.now());
        long resolvedIncidents = incidents.stream()
                .filter(i -> i.getStatus() == AiAdminIncident.Status.RESOLVED || i.getStatus() == AiAdminIncident.Status.CLOSED)
                .count();
        breakdown.xpFromIncidents = (int) (resolvedIncidents * XP_PER_INCIDENT);
        breakdown.incidentsResolved = (int) resolvedIncidents;

        breakdown.totalXp = breakdown.xpFromOrders + breakdown.xpFromIncidents;

        // Calculate percentages
        if (breakdown.totalXp > 0) {
            breakdown.ordersPercent = (double) breakdown.xpFromOrders / breakdown.totalXp * 100;
            breakdown.incidentsPercent = (double) breakdown.xpFromIncidents / breakdown.totalXp * 100;
        }

        return breakdown;
    }

    /**
     * Get XP comparison across all AI admins over N days.
     *
     * @param days number of days to look back
     * @return list of admin XP comparisons, sorted by XP earned descending
     */
    @Transactional(readOnly = true)
    public List<AdminXpComparison> getAdminXpComparison(int days) {
        List<AiCharacter> admins = characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc();

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<AdminXpComparison> comparisons = new ArrayList<>();

        for (AiCharacter admin : admins) {
            AdminXpComparison comparison = new AdminXpComparison();
            comparison.adminId = admin.getId();
            comparison.adminName = admin.getName();
            comparison.empire = admin.getEmpireName();
            comparison.role = admin.getRole();
            comparison.totalExperience = admin.getTotalExperience();

            // Sum XP from health reports
            List<AiAdminHealthReport> reports = reportRepository
                    .findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(admin.getId(), startDate, LocalDateTime.now());
            int xpEarned = 0;
            int ordersCompleted = 0;
            int incidentsHandled = 0;

            for (AiAdminHealthReport report : reports) {
                xpEarned += report.getXpEarned();
                ordersCompleted += report.getOrdersCompleted();
                incidentsHandled += report.getIncidentsHandled();
            }

            comparison.xpEarned = xpEarned;
            comparison.ordersCompleted = ordersCompleted;
            comparison.incidentsHandled = incidentsHandled;
            comparison.reportsGenerated = reports.size();

            comparisons.add(comparison);
        }

        // Sort by XP earned descending
        comparisons.sort((a, b) -> Integer.compare(b.xpEarned, a.xpEarned));
        return comparisons;
    }

    /**
     * Get XP milestone history for an admin.
     * Shows which milestones have been achieved and when.
     *
     * @param adminId the AI admin character ID
     * @return list of XP milestone achievements
     */
    @Transactional(readOnly = true)
    public List<XpMilestoneAchievement> getXpMilestoneHistory(Long adminId) {
        List<AiAdminHealthReport> reports = reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId);

        // Calculate cumulative XP from all reports
        int cumulativeXp = 0;
        for (AiAdminHealthReport report : reports) {
            cumulativeXp += report.getXpEarned();
        }

        List<XpMilestoneAchievement> achievements = new ArrayList<>();

        for (int threshold : XP_THRESHOLDS) {
            XpMilestoneAchievement achievement = new XpMilestoneAchievement();
            achievement.threshold = threshold;
            achievement.achieved = cumulativeXp >= threshold;
            achievement.cumulativeXp = cumulativeXp;
            achievement.percentToNext = cumulativeXp >= threshold ? 100.0
                    : (double) cumulativeXp / threshold * 100;

            // Estimate when milestone was achieved by walking through reports
            if (achievement.achieved) {
                achievement.achievedAt = estimateMilestoneDate(reports, threshold);
            }

            achievements.add(achievement);
        }

        return achievements;
    }

    // --- Helper methods ---

    /**
     * Estimate when a milestone was achieved by walking through reports in chronological order.
     */
    private LocalDateTime estimateMilestoneDate(List<AiAdminHealthReport> reports, int threshold) {
        int cumulativeXp = 0;
        // Reports are ordered desc, so walk backwards
        for (int i = reports.size() - 1; i >= 0; i--) {
            AiAdminHealthReport report = reports.get(i);
            cumulativeXp += report.getXpEarned();
            if (cumulativeXp >= threshold) {
                return report.getGeneratedAt();
            }
        }
        return null;
    }

    // --- DTOs ---

    /** A single point in the XP time series. */
    public static class XpTrendPoint {
        public java.time.LocalDate date;
        public int xpEarned;
        public int ordersCompleted;
        public int incidentsHandled;

        public XpTrendPoint() {
        }
    }

    /** XP velocity metrics. */
    public static class XpVelocity {
        public Long adminId;
        public int totalXp;
        public int totalReports;
        public LocalDateTime firstReportDate;
        public LocalDateTime lastReportDate;
        public double xpPerDay;
        public double xpPerWeek;
        public double xpPerReport;

        public XpVelocity() {
        }
    }

    /** XP milestone projection. */
    public static class XpProjection {
        public Long adminId;
        public int currentXp;
        public double xpPerDay;
        public int nextMilestone;
        public int xpRemaining;
        public int estimatedDaysRemaining;
        public boolean allMilestonesAchieved;

        public XpProjection() {
        }
    }

    /** XP source breakdown. */
    public static class XpSourceBreakdown {
        public Long adminId;
        public int days;
        public int totalXp;
        public int xpFromOrders;
        public int xpFromIncidents;
        public int ordersCompleted;
        public int incidentsResolved;
        public double ordersPercent;
        public double incidentsPercent;

        public XpSourceBreakdown() {
        }
    }

    /** XP comparison for a single admin. */
    public static class AdminXpComparison {
        public Long adminId;
        public String adminName;
        public String empire;
        public String role;
        public int totalExperience;
        public int xpEarned;
        public int ordersCompleted;
        public int incidentsHandled;
        public int reportsGenerated;

        public AdminXpComparison() {
        }
    }

    /** XP milestone achievement. */
    public static class XpMilestoneAchievement {
        public int threshold;
        public boolean achieved;
        public int cumulativeXp;
        public double percentToNext;
        public LocalDateTime achievedAt;

        public XpMilestoneAchievement() {
        }
    }
}
