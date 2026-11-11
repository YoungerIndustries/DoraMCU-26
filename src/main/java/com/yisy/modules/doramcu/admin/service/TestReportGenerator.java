package com.yisy.modules.doramcu.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates test results from TestExecutor into AiAdminTestReport and AiAdminTestCase entities,
 * generates JSON report and AI summary.
 */
@Service
public class TestReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestReportGenerator.class);

    private final AiAdminTestReportRepository reportRepository;
    private final AiAdminTestCaseRepository testCaseRepository;
    private final ObjectMapper objectMapper;

    public TestReportGenerator(AiAdminTestReportRepository reportRepository,
                               AiAdminTestCaseRepository testCaseRepository,
                               ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.testCaseRepository = testCaseRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a test report from the list of test case results.
     *
     * @param orderId the order this report belongs to
     * @param testCases the list of test case results from TestExecutor
     * @return the saved AiAdminTestReport
     */
    @Transactional
    public AiAdminTestReport generateReport(Long orderId, List<AiAdminTestCase> testCases) {
        // Count pass/fail/skipped
        int passed = 0, failed = 0, skipped = 0;
        for (AiAdminTestCase tc : testCases) {
            switch (tc.getStatus()) {
                case PASSED -> passed++;
                case FAILED -> failed++;
                case SKIPPED -> skipped++;
            }
        }

        // Build JSON report
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("orderId", orderId);
        reportData.put("totalTests", testCases.size());
        reportData.put("passed", passed);
        reportData.put("failed", failed);
        reportData.put("skipped", skipped);

        List<Map<String, Object>> caseData = new java.util.ArrayList<>();
        for (AiAdminTestCase tc : testCases) {
            Map<String, Object> map = new HashMap<>();
            map.put("testName", tc.getTestName());
            map.put("category", tc.getCategory());
            map.put("status", tc.getStatus().name());
            map.put("durationMs", tc.getDurationMs());
            if (tc.getErrorMessage() != null) {
                map.put("errorMessage", tc.getErrorMessage());
            }
            caseData.add(map);
        }
        reportData.put("testCases", caseData);

        String reportJson;
        try {
            reportJson = objectMapper.writeValueAsString(reportData);
        } catch (Exception e) {
            log.error("Failed to serialize report JSON", e);
            reportJson = "{}";
        }

        // Create and save report
        AiAdminTestReport report = new AiAdminTestReport(orderId);
        report.setTotalTests(testCases.size());
        report.setPassed(passed);
        report.setFailed(failed);
        report.setSkipped(skipped);
        report.setReportJson(reportJson);
        report.setSummary(generateSummary(testCases, passed, failed, skipped));
        report = reportRepository.save(report);

        // Save test cases with report ID
        for (AiAdminTestCase tc : testCases) {
            tc.setReportId(report.getId());
            testCaseRepository.save(tc);
        }

        log.info("Generated report {} for order {}: {} passed, {} failed, {} skipped",
                report.getId(), orderId, passed, failed, skipped);
        return report;
    }

    /**
     * Generates a human-readable summary of the test results.
     */
    private String generateSummary(List<AiAdminTestCase> testCases, int passed, int failed, int skipped) {
        StringBuilder sb = new StringBuilder();
        sb.append("Test Report: ").append(testCases.size()).append(" tests executed. ");
        sb.append(passed).append(" passed, ").append(failed).append(" failed, ").append(skipped).append(" skipped. ");

        if (failed > 0) {
            sb.append("Failed tests: ");
            int count = 0;
            for (AiAdminTestCase tc : testCases) {
                if (tc.getStatus() == TestStatus.FAILED && count < 5) {
                    sb.append(tc.getTestName());
                    if (tc.getErrorMessage() != null) {
                        sb.append(" (").append(tc.getErrorMessage()).append(")");
                    }
                    sb.append("; ");
                    count++;
                }
            }
            if (failed > 5) {
                sb.append("... and ").append(failed - 5).append(" more.");
            }
        } else {
            sb.append("All tests passed successfully.");
        }

        return sb.toString();
    }
}
