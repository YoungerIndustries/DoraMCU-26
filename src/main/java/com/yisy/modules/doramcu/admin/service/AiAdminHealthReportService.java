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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for generating AI admin health reports.
 * Aggregates data from analytics, incidents, and galaxy services,
 * then uses LLM to generate recommendations.
 */
@Service
public class AiAdminHealthReportService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminHealthReportService.class);

    private final AiAdminHealthReportRepository reportRepository;
    private final AiAdminOrderRepository orderRepository;
    private final AiAdminIncidentRepository incidentRepository;
    private final AiCharacterRepository characterRepository;
    private final AiAdminLlmService llmService;

    public AiAdminHealthReportService(AiAdminHealthReportRepository reportRepository,
                                       AiAdminOrderRepository orderRepository,
                                       AiAdminIncidentRepository incidentRepository,
                                       AiCharacterRepository characterRepository,
                                       AiAdminLlmService llmService) {
        this.reportRepository = reportRepository;
        this.orderRepository = orderRepository;
        this.incidentRepository = incidentRepository;
        this.characterRepository = characterRepository;
        this.llmService = llmService;
    }

    /**
     * Generate a health report for a specific AI admin.
     *
     * @param adminId the AI admin character ID
     * @param reportType the report type (DAILY, WEEKLY, MONTHLY)
     * @return the generated report
     */
    @Transactional
    public AiAdminHealthReport generateReport(Long adminId, AiAdminHealthReport.ReportType reportType) {
        AiCharacter admin = characterRepository.findById(adminId).orElse(null);
        if (admin == null) {
            throw new IllegalArgumentException("AI admin not found: " + adminId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = calculatePeriodStart(reportType, now);

        // Aggregate order data
        List<AiAdminOrder> orders = orderRepository.findByAssignedAiAdminIdAndCreatedAtBetween(adminId, periodStart, now);
        int ordersProcessed = orders.size();
        int ordersCompleted = 0;
        int ordersFailed = 0;
        for (AiAdminOrder order : orders) {
            if (order.getStatus() == AiAdminOrder.OrderStatus.COMPLETED) {
                ordersCompleted++;
            } else if (order.getStatus() == AiAdminOrder.OrderStatus.FAILED) {
                ordersFailed++;
            }
        }

        // Aggregate incident data
        List<AiAdminIncident> incidents = incidentRepository.findByAssignedAiAdminIdAndCreatedAtBetween(adminId, periodStart, now);
        int incidentsHandled = incidents.size();

        // Count active incidents
        long activeIncidents = incidentRepository.countByAssignedAiAdminIdAndStatusIn(
                adminId, List.of(AiAdminIncident.Status.OPEN, AiAdminIncident.Status.INVESTIGATING));

        // Calculate XP earned (from completed orders: 10 base + tests passed)
        int xpEarned = ordersCompleted * 10;

        // Build summary
        String summary = buildSummary(admin, ordersProcessed, ordersCompleted, ordersFailed,
                incidentsHandled, activeIncidents, xpEarned, reportType);

        // Create report entity
        AiAdminHealthReport report = new AiAdminHealthReport();
        report.setAiAdminId(adminId);
        report.setReportType(reportType);
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(now);
        report.setGeneratedAt(now);
        report.setOrdersProcessed(ordersProcessed);
        report.setOrdersCompleted(ordersCompleted);
        report.setOrdersFailed(ordersFailed);
        report.setIncidentsHandled(incidentsHandled);
        report.setXpEarned(xpEarned);
        report.setActiveIncidents((int) activeIncidents);
        report.setSummary(summary);

        // Generate LLM recommendations
        try {
            LlmAnalysisResult llmResult = llmService.analyzeReport(admin, Map.of(
                            "ordersCompleted", report.getOrdersCompleted(),
                            "ordersFailed", report.getOrdersFailed(),
                            "incidentsHandled", report.getIncidentsHandled(),
                            "activeIncidents", report.getActiveIncidents(),
                            "galaxyHealthScore", report.getGalaxyHealthScore(),
                            "periodStart", report.getPeriodStart(),
                            "periodEnd", report.getPeriodEnd()
                    ));
            report.setRecommendations(llmResult.getReasoning());
            report.setLlmAnalysis(llmResult.getRawResponse());
            log.info("LLM recommendations generated for admin {} report", adminId);
        } catch (Exception e) {
            log.warn("LLM recommendations failed for admin {} report: {}", adminId, e.getMessage());
            report.setRecommendations("LLM unavailable. Manual review recommended.");
        }

        report = reportRepository.save(report);
        log.info("Generated {} report for admin {}: {} orders, {} incidents",
                reportType, adminId, ordersProcessed, incidentsHandled);
        return report;
    }

    /**
     * Generate reports for all active AI admins.
     *
     * @param reportType the report type
     * @return list of generated reports
     */
    @Transactional
    public List<AiAdminHealthReport> generateAllReports(AiAdminHealthReport.ReportType reportType) {
        List<AiCharacter> admins = characterRepository.findByAiAdminTrueAndActiveTrue();
        List<AiAdminHealthReport> reports = new ArrayList<>();

        for (AiCharacter admin : admins) {
            try {
                AiAdminHealthReport report = generateReport(admin.getId(), reportType);
                reports.add(report);
            } catch (Exception e) {
                log.error("Failed to generate report for admin {}: {}", admin.getId(), e.getMessage());
            }
        }

        log.info("Generated {} reports for {} admins", reports.size(), admins.size());
        return reports;
    }

    /**
     * Get the latest report for an admin.
     *
     * @param adminId the AI admin character ID
     * @return the latest report or null
     */
    @Transactional(readOnly = true)
    public AiAdminHealthReport getLatestReport(Long adminId) {
        return reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId).stream()
                .findFirst().orElse(null);
    }

    /**
     * Get the latest report for an admin and report type.
     *
     * @param adminId the AI admin character ID
     * @param reportType the report type
     * @return the latest report or null
     */
    @Transactional(readOnly = true)
    public AiAdminHealthReport getReport(Long adminId, AiAdminHealthReport.ReportType reportType) {
        return reportRepository.findByAiAdminIdAndReportTypeOrderByGeneratedAtDesc(adminId, reportType);
    }

    /**
     * Get reports for an admin within a date range.
     *
     * @param adminId the AI admin character ID
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of reports
     */
    @Transactional(readOnly = true)
    public List<AiAdminHealthReport> getReports(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(adminId, startDate, endDate);
    }

    /**
     * Get a summary of recent reports for an admin.
     *
     * @param adminId the AI admin character ID
     * @return report summary
     */
    @Transactional(readOnly = true)
    public ReportSummary getReportSummary(Long adminId) {
        List<AiAdminHealthReport> reports = reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId);
        long totalReports = reportRepository.countByAiAdminId(adminId);

        ReportSummary summary = new ReportSummary();
        summary.adminId = adminId;
        summary.totalReports = (int) totalReports;
        summary.recentReports = reports.size();

        if (!reports.isEmpty()) {
            AiAdminHealthReport latest = reports.get(0);
            summary.latestReportType = latest.getReportType();
            summary.latestGeneratedAt = latest.getGeneratedAt();
            summary.totalOrdersProcessed = latest.getOrdersProcessed();
            summary.totalIncidentsHandled = latest.getIncidentsHandled();
            summary.totalXpEarned = latest.getXpEarned();

            // Calculate averages from recent reports
            int totalOrders = 0;
            int totalCompleted = 0;
            int totalFailed = 0;
            for (AiAdminHealthReport report : reports) {
                totalOrders += report.getOrdersProcessed();
                totalCompleted += report.getOrdersCompleted();
                totalFailed += report.getOrdersFailed();
            }
            summary.avgOrdersPerReport = reports.isEmpty() ? 0 : totalOrders / reports.size();
            summary.avgSuccessRate = totalOrders == 0 ? 0.0 : (double) totalCompleted / totalOrders * 100;
        }

        return summary;
    }

    /**
     * Get LLM recommendations from the latest report.
     *
     * @param adminId the AI admin character ID
     * @return recommendations or null
     */
    @Transactional(readOnly = true)
    public String getRecommendations(Long adminId) {
        AiAdminHealthReport report = getLatestReport(adminId);
        return report != null ? report.getRecommendations() : null;
    }

    /**
     * Scheduled task to generate daily reports for all active AI admins.
     * Runs once per day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyReports() {
        log.info("Starting scheduled daily report generation");
        try {
            List<AiAdminHealthReport> reports = generateAllReports(AiAdminHealthReport.ReportType.DAILY);
            log.info("Completed scheduled daily report generation: {} reports", reports.size());
        } catch (Exception e) {
            log.error("Scheduled daily report generation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Calculate the period start based on report type.
     */
    private LocalDateTime calculatePeriodStart(AiAdminHealthReport.ReportType reportType, LocalDateTime now) {
        switch (reportType) {
            case DAILY:
                return now.minusDays(1);
            case WEEKLY:
                return now.minusWeeks(1);
            case MONTHLY:
                return now.minusMonths(1);
            default:
                return now.minusDays(1);
        }
    }

    /**
     * Build a text summary of the report period.
     */
    private String buildSummary(AiCharacter admin, int ordersProcessed, int ordersCompleted,
                                 int ordersFailed, int incidentsHandled, long activeIncidents,
                                 int xpEarned, AiAdminHealthReport.ReportType reportType) {
        StringBuilder sb = new StringBuilder();
        sb.append(admin.getName()).append(" - ").append(reportType).append(" Health Report\n\n");
        sb.append("Orders: ").append(ordersProcessed).append(" processed, ")
                .append(ordersCompleted).append(" completed, ").append(ordersFailed).append(" failed\n");
        sb.append("Incidents: ").append(incidentsHandled).append(" handled, ")
                .append(activeIncidents).append(" active\n");
        sb.append("XP Earned: ").append(xpEarned).append("\n");

        if (ordersProcessed > 0) {
            double successRate = (double) ordersCompleted / ordersProcessed * 100;
            sb.append("Success Rate: ").append(String.format("%.1f%%", successRate)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Compare two reports for an admin (e.g., current period vs previous period).
     *
     * @param adminId the AI admin character ID
     * @param reportType the report type
     * @return comparison result, or null if fewer than 2 reports exist
     */
    @Transactional(readOnly = true)
    public ReportComparison compareReports(Long adminId, AiAdminHealthReport.ReportType reportType) {
        List<AiAdminHealthReport> allReports = reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId);
        List<AiAdminHealthReport> reports = new ArrayList<>();
        for (AiAdminHealthReport r : allReports) {
            if (r.getReportType() == reportType) {
                reports.add(r);
            }
        }
        if (reports.size() < 2) {
            return null;
        }

        AiAdminHealthReport current = reports.get(0);
        AiAdminHealthReport previous = reports.get(1);

        ReportComparison comparison = new ReportComparison();
        comparison.adminId = adminId;
        comparison.reportType = reportType;
        comparison.currentPeriod = current;
        comparison.previousPeriod = previous;

        comparison.ordersProcessedChange = current.getOrdersProcessed() - previous.getOrdersProcessed();
        comparison.ordersCompletedChange = current.getOrdersCompleted() - previous.getOrdersCompleted();
        comparison.ordersFailedChange = current.getOrdersFailed() - previous.getOrdersFailed();
        comparison.incidentsHandledChange = current.getIncidentsHandled() - previous.getIncidentsHandled();
        comparison.xpEarnedChange = current.getXpEarned() - previous.getXpEarned();

        double currentRate = current.getOrdersProcessed() > 0
                ? (double) current.getOrdersCompleted() / current.getOrdersProcessed() * 100
                : 0.0;
        double previousRate = previous.getOrdersProcessed() > 0
                ? (double) previous.getOrdersCompleted() / previous.getOrdersProcessed() * 100
                : 0.0;
        comparison.successRateChange = currentRate - previousRate;

        comparison.xpEarnedPercentChange = previous.getXpEarned() > 0
                ? (double) comparison.xpEarnedChange / previous.getXpEarned() * 100
                : 0.0;

        return comparison;
    }

    /**
     * Get trend data across multiple reports for an admin.
     *
     * @param adminId the AI admin character ID
     * @param days number of days to look back
     * @return list of trend points
     */
    @Transactional(readOnly = true)
    public List<TrendPoint> getReportTrends(Long adminId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<AiAdminHealthReport> reports = reportRepository
                .findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(adminId, startDate, LocalDateTime.now());

        List<TrendPoint> trends = new ArrayList<>();
        for (AiAdminHealthReport report : reports) {
            TrendPoint point = new TrendPoint();
            point.generatedAt = report.getGeneratedAt();
            point.reportType = report.getReportType();
            point.ordersProcessed = report.getOrdersProcessed();
            point.ordersCompleted = report.getOrdersCompleted();
            point.ordersFailed = report.getOrdersFailed();
            point.incidentsHandled = report.getIncidentsHandled();
            point.xpEarned = report.getXpEarned();
            point.successRate = report.getOrdersProcessed() > 0
                    ? (double) report.getOrdersCompleted() / report.getOrdersProcessed() * 100
                    : 0.0;
            trends.add(point);
        }
        return trends;
    }

    /**
     * Get XP milestones achieved by an admin based on cumulative XP from reports.
     *
     * @param adminId the AI admin character ID
     * @return list of XP milestones
     */
    @Transactional(readOnly = true)
    public List<XpMilestone> getXpMilestones(Long adminId) {
        List<AiAdminHealthReport> reports = reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId);

        // Calculate cumulative XP
        int cumulativeXp = 0;
        for (AiAdminHealthReport report : reports) {
            cumulativeXp += report.getXpEarned();
        }

        // Define milestone thresholds
        int[] thresholds = {100, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000};
        List<XpMilestone> milestones = new ArrayList<>();

        for (int threshold : thresholds) {
            XpMilestone milestone = new XpMilestone();
            milestone.threshold = threshold;
            milestone.achieved = cumulativeXp >= threshold;
            milestone.cumulativeXp = cumulativeXp;
            milestone.percentToNext = cumulativeXp >= threshold ? 100.0
                    : (double) cumulativeXp / threshold * 100;
            milestones.add(milestone);
        }

        return milestones;
    }

    /**
     * Get a growth overview for an admin based on all reports.
     *
     * @param adminId the AI admin character ID
     * @return growth overview
     */
    @Transactional(readOnly = true)
    public GrowthOverview getGrowthOverview(Long adminId) {
        List<AiAdminHealthReport> reports = reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(adminId);

        GrowthOverview overview = new GrowthOverview();
        overview.adminId = adminId;
        overview.totalReports = reports.size();

        if (reports.isEmpty()) {
            return overview;
        }

        // Calculate totals across all reports
        int totalOrders = 0;
        int totalCompleted = 0;
        int totalFailed = 0;
        int totalIncidents = 0;
        int totalXp = 0;
        int minHealthScore = Integer.MAX_VALUE;
        int maxHealthScore = Integer.MIN_VALUE;

        for (AiAdminHealthReport report : reports) {
            totalOrders += report.getOrdersProcessed();
            totalCompleted += report.getOrdersCompleted();
            totalFailed += report.getOrdersFailed();
            totalIncidents += report.getIncidentsHandled();
            totalXp += report.getXpEarned();
            if (report.getGalaxyHealthScore() != null) {
                minHealthScore = Math.min(minHealthScore, report.getGalaxyHealthScore());
                maxHealthScore = Math.max(maxHealthScore, report.getGalaxyHealthScore());
            }
        }

        overview.totalOrdersProcessed = totalOrders;
        overview.totalOrdersCompleted = totalCompleted;
        overview.totalOrdersFailed = totalFailed;
        overview.totalIncidentsHandled = totalIncidents;
        overview.totalXpEarned = totalXp;
        overview.overallSuccessRate = totalOrders > 0 ? (double) totalCompleted / totalOrders * 100 : 0.0;
        overview.minGalaxyHealthScore = minHealthScore == Integer.MAX_VALUE ? null : minHealthScore;
        overview.maxGalaxyHealthScore = maxHealthScore == Integer.MIN_VALUE ? null : maxHealthScore;

        // First and last report dates
        overview.firstReportDate = reports.get(reports.size() - 1).getGeneratedAt();
        overview.lastReportDate = reports.get(0).getGeneratedAt();

        // Average XP per report
        overview.avgXpPerReport = totalXp / reports.size();

        return overview;
    }

    /**
     * Summary of recent reports for an admin.
     */
    public static class ReportSummary {
        public Long adminId;
        public int totalReports;
        public int recentReports;
        public AiAdminHealthReport.ReportType latestReportType;
        public LocalDateTime latestGeneratedAt;
        public int totalOrdersProcessed;
        public int totalIncidentsHandled;
        public int totalXpEarned;
        public int avgOrdersPerReport;
        public double avgSuccessRate;

        public ReportSummary() {
        }
    }

    /**
     * Comparison between two consecutive reports.
     */
    public static class ReportComparison {
        public Long adminId;
        public AiAdminHealthReport.ReportType reportType;
        public AiAdminHealthReport currentPeriod;
        public AiAdminHealthReport previousPeriod;
        public int ordersProcessedChange;
        public int ordersCompletedChange;
        public int ordersFailedChange;
        public int incidentsHandledChange;
        public int xpEarnedChange;
        public double successRateChange;
        public double xpEarnedPercentChange;

        public ReportComparison() {
        }
    }

    /**
     * A single trend point from a report.
     */
    public static class TrendPoint {
        public LocalDateTime generatedAt;
        public AiAdminHealthReport.ReportType reportType;
        public int ordersProcessed;
        public int ordersCompleted;
        public int ordersFailed;
        public int incidentsHandled;
        public int xpEarned;
        public double successRate;

        public TrendPoint() {
        }
    }

    /**
     * XP milestone achievement.
     */
    public static class XpMilestone {
        public int threshold;
        public boolean achieved;
        public int cumulativeXp;
        public double percentToNext;

        public XpMilestone() {
        }
    }

    /**
     * Growth overview across all reports.
     */
    public static class GrowthOverview {
        public Long adminId;
        public int totalReports;
        public int totalOrdersProcessed;
        public int totalOrdersCompleted;
        public int totalOrdersFailed;
        public int totalIncidentsHandled;
        public int totalXpEarned;
        public double overallSuccessRate;
        public Integer minGalaxyHealthScore;
        public Integer maxGalaxyHealthScore;
        public LocalDateTime firstReportDate;
        public LocalDateTime lastReportDate;
        public int avgXpPerReport;

        public GrowthOverview() {
        }
    }
}
