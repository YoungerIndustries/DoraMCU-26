package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes test reports and generates recommendations.
 * Identifies patterns: flaky tests, recurring failures, performance issues.
 * Suggests fixes based on error messages and test categories.
 */
@Service
public class TestResultAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TestResultAnalyzer.class);

    private final AiAdminTestCaseRepository testCaseRepository;
    private final AiAdminTestReportRepository reportRepository;

    public TestResultAnalyzer(AiAdminTestCaseRepository testCaseRepository,
                              AiAdminTestReportRepository reportRepository) {
        this.testCaseRepository = testCaseRepository;
        this.reportRepository = reportRepository;
    }

    /**
     * Analyzes a test report and generates recommendations.
     *
     * @param report the test report to analyze
     * @return structured analysis with recommendations
     */
    public TestAnalysis analyzeReport(AiAdminTestReport report) {
        List<AiAdminTestCase> testCases = testCaseRepository.findByReportId(report.getId());

        TestAnalysis analysis = new TestAnalysis();
        analysis.setReportId(report.getId());
        analysis.setTotalTests(report.getTotalTests());
        analysis.setPassed(report.getPassed());
        analysis.setFailed(report.getFailed());
        analysis.setSkipped(report.getSkipped());

        // Calculate pass rate
        analysis.setPassRate(report.getTotalTests() > 0 ? (double) report.getPassed() / report.getTotalTests() : 0.0);

        // Analyze failures by category
        Map<String, List<AiAdminTestCase>> failuresByCategory = testCases.stream()
                .filter(tc -> tc.getStatus() == TestStatus.FAILED)
                .collect(Collectors.groupingBy(AiAdminTestCase::getCategory));

        analysis.setFailuresByCategory(new ArrayList<>(failuresByCategory.keySet()));

        // Analyze slow tests
        List<AiAdminTestCase> slowTests = testCases.stream()
                .filter(tc -> tc.getDurationMs() > 3000)
                .sorted(Comparator.comparingLong(AiAdminTestCase::getDurationMs).reversed())
                .collect(Collectors.toList());
        analysis.setSlowTests(slowTests.size());

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();

        if (report.getFailed() > 0) {
            recommendations.add("Fix " + report.getFailed() + " failed test(s) before proceeding.");

            // Category-specific recommendations
            for (String category : failuresByCategory.keySet()) {
                switch (category) {
                    case "API":
                        recommendations.add("Check API endpoint availability and network connectivity.");
                        break;
                    case "GAME":
                        recommendations.add("Verify game services are running and database connections are healthy.");
                        break;
                    case "AI_CHAT":
                        recommendations.add("Check AI provider configuration and API keys.");
                        break;
                    case "AUTH":
                        recommendations.add("Review authentication configuration and JWT settings.");
                        break;
                    default:
                        recommendations.add("Investigate failures in " + category + " category.");
                }
            }
        }

        if (slowTests.size() > 0) {
            recommendations.add(slowTests.size() + " test(s) took over 3 seconds. Consider optimizing or increasing timeout.");
        }

        if (analysis.getPassRate() >= 0.95) {
            recommendations.add("Test suite is healthy. Pass rate: " + String.format("%.0f%%", analysis.getPassRate() * 100));
        } else if (analysis.getPassRate() >= 0.8) {
            recommendations.add("Some tests are failing. Focus on the most critical failures first.");
        } else {
            recommendations.add("Critical: Many tests are failing. System may be unstable.");
        }

        analysis.setRecommendations(recommendations);

        // Generate summary
        analysis.setSummary(generateSummary(analysis));

        log.info("Analysis for report {}: {} passed, {} failed, {} recommendations",
                report.getId(), analysis.getPassed(), analysis.getFailed(), recommendations.size());

        return analysis;
    }

    /**
     * Analyzes trends across multiple reports.
     *
     * @param reportIds list of report IDs to analyze
     * @return trend analysis
     */
    public TrendAnalysis analyzeTrends(List<Long> reportIds) {
        TrendAnalysis trend = new TrendAnalysis();
        trend.setTotalReports(reportIds.size());

        if (reportIds.isEmpty()) {
            return trend;
        }

        List<AiAdminTestReport> reports = reportRepository.findAllById(reportIds);
        if (reports.isEmpty()) {
            return trend;
        }

        // Calculate average pass rate
        double avgPassRate = reports.stream()
                .mapToDouble(r -> r.getTotalTests() > 0 ? (double) r.getPassed() / r.getTotalTests() : 0.0)
                .average()
                .orElse(0.0);
        trend.setAveragePassRate(avgPassRate);

        // Find recurring failures
        Map<String, Integer> failureCounts = new HashMap<>();
        for (AiAdminTestReport report : reports) {
            List<AiAdminTestCase> testCases = testCaseRepository.findByReportId(report.getId());
            for (AiAdminTestCase tc : testCases) {
                if (tc.getStatus() == TestStatus.FAILED) {
                    failureCounts.merge(tc.getTestName(), 1, Integer::sum);
                }
            }
        }

        // Tests that failed in more than one report are recurring
        List<String> recurringFailures = failureCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        trend.setRecurringFailures(recurringFailures);

        // Identify flaky tests (tests that sometimes pass and sometimes fail)
        Map<String, List<TestStatus>> testHistory = new HashMap<>();
        for (AiAdminTestReport report : reports) {
            List<AiAdminTestCase> testCases = testCaseRepository.findByReportId(report.getId());
            for (AiAdminTestCase tc : testCases) {
                testHistory.computeIfAbsent(tc.getTestName(), k -> new ArrayList<>()).add(tc.getStatus());
            }
        }

        List<String> flakyTests = testHistory.entrySet().stream()
                .filter(entry -> {
                    List<TestStatus> statuses = entry.getValue();
                    boolean hasPassed = statuses.contains(TestStatus.PASSED);
                    boolean hasFailed = statuses.contains(TestStatus.FAILED);
                    return hasPassed && hasFailed;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        trend.setFlakyTests(flakyTests);

        return trend;
    }

    private String generateSummary(TestAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("Test Analysis Summary\n");
        sb.append("=====================\n");
        sb.append("Total: ").append(analysis.getTotalTests()).append("\n");
        sb.append("Passed: ").append(analysis.getPassed()).append("\n");
        sb.append("Failed: ").append(analysis.getFailed()).append("\n");
        sb.append("Skipped: ").append(analysis.getSkipped()).append("\n");
        sb.append("Pass Rate: ").append(String.format("%.1f%%", analysis.getPassRate() * 100)).append("\n");

        if (analysis.getSlowTests() > 0) {
            sb.append("Slow Tests: ").append(analysis.getSlowTests()).append("\n");
        }

        if (!analysis.getRecommendations().isEmpty()) {
            sb.append("\nRecommendations:\n");
            for (String rec : analysis.getRecommendations()) {
                sb.append("- ").append(rec).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Structured analysis result for a single test report.
     */
    public static class TestAnalysis {
        private Long reportId;
        private int totalTests;
        private int passed;
        private int failed;
        private int skipped;
        private double passRate;
        private List<String> failuresByCategory;
        private int slowTests;
        private List<String> recommendations;
        private String summary;

        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        public int getTotalTests() { return totalTests; }
        public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
        public int getPassed() { return passed; }
        public void setPassed(int passed) { this.passed = passed; }
        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }
        public int getSkipped() { return skipped; }
        public void setSkipped(int skipped) { this.skipped = skipped; }
        public double getPassRate() { return passRate; }
        public void setPassRate(double passRate) { this.passRate = passRate; }
        public List<String> getFailuresByCategory() { return failuresByCategory; }
        public void setFailuresByCategory(List<String> failuresByCategory) { this.failuresByCategory = failuresByCategory; }
        public int getSlowTests() { return slowTests; }
        public void setSlowTests(int slowTests) { this.slowTests = slowTests; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    /**
     * Trend analysis across multiple reports.
     */
    public static class TrendAnalysis {
        private int totalReports;
        private double averagePassRate;
        private List<String> recurringFailures;
        private List<String> flakyTests;

        public int getTotalReports() { return totalReports; }
        public void setTotalReports(int totalReports) { this.totalReports = totalReports; }
        public double getAveragePassRate() { return averagePassRate; }
        public void setAveragePassRate(double averagePassRate) { this.averagePassRate = averagePassRate; }
        public List<String> getRecurringFailures() { return recurringFailures; }
        public void setRecurringFailures(List<String> recurringFailures) { this.recurringFailures = recurringFailures; }
        public List<String> getFlakyTests() { return flakyTests; }
        public void setFlakyTests(List<String> flakyTests) { this.flakyTests = flakyTests; }
    }
}
