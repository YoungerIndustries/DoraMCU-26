package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Synchronous helper for the AI agent development workflow.
 * After implementing new code, the AI agent calls this service to run tests,
 * wait for completion, and receive a report — all in one call.
 */
@Service
public class DevelopmentTestHelper {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentTestHelper.class);

    private final AiAdminService aiAdminService;

    @Value("${dora.admin.test.timeout:60}")
    private long defaultTimeoutSeconds;

    public DevelopmentTestHelper(AiAdminService aiAdminService) {
        this.aiAdminService = aiAdminService;
    }

    /**
     * Submits a test order, waits for completion, and returns the report.
     * This is the main entry point for the AI agent after implementing new code.
     *
     * @param submittedBy the admin username
     * @param orderText natural language description of what to test
     * @return the test report, or null if the order failed or timed out
     */
    public AiAdminTestReport runTestsAndReport(String submittedBy, String orderText) {
        return runTestsAndReport(submittedBy, orderText, defaultTimeoutSeconds);
    }

    /**
     * Submits a test order, waits for completion, and returns the report.
     *
     * @param submittedBy the admin username
     * @param orderText natural language description of what to test
     * @param timeoutSeconds maximum wait time in seconds
     * @return the test report, or null if the order failed or timed out
     */
    public AiAdminTestReport runTestsAndReport(String submittedBy, String orderText, long timeoutSeconds) {
        log.info("DevelopmentTestHelper: submitting order for {}: {}", submittedBy, orderText);

        // Submit the order
        AiAdminOrder order = aiAdminService.submitOrder(submittedBy, orderText);
        Long orderId = order.getId();

        // Wait for completion
        boolean completed = aiAdminService.waitForOrder(orderId, timeoutSeconds);

        if (!completed) {
            Optional<AiAdminOrder> orderOpt = aiAdminService.getOrder(orderId);
            if (orderOpt.isPresent()) {
                AiAdminOrder.OrderStatus status = orderOpt.get().getStatus();
                if (status == AiAdminOrder.OrderStatus.FAILED) {
                    log.error("DevelopmentTestHelper: order {} failed", orderId);
                } else {
                    log.warn("DevelopmentTestHelper: order {} timed out after {} seconds", orderId, timeoutSeconds);
                }
            }
            return null;
        }

        // Get the report
        Optional<AiAdminTestReport> reportOpt = aiAdminService.getReportForOrder(orderId);
        if (reportOpt.isPresent()) {
            AiAdminTestReport report = reportOpt.get();
            log.info("DevelopmentTestHelper: order {} completed. {} passed, {} failed",
                    orderId, report.getPassed(), report.getFailed());
            return report;
        } else {
            log.warn("DevelopmentTestHelper: no report found for order {}", orderId);
            return null;
        }
    }

    /**
     * Runs tests for a specific category and returns the report.
     *
     * @param submittedBy the admin username
     * @param category the test category (API, GAME, AI_CHAT, AUTH)
     * @return the test report, or null if the order failed or timed out
     */
    public AiAdminTestReport runTestsForCategory(String submittedBy, String category) {
        String orderText = "test " + category.toLowerCase();
        return runTestsAndReport(submittedBy, orderText);
    }

    /**
     * Runs all tests and returns the report.
     *
     * @param submittedBy the admin username
     * @return the test report, or null if the order failed or timed out
     */
    public AiAdminTestReport runAllTests(String submittedBy) {
        return runTestsAndReport(submittedBy, "test all");
    }

    /**
     * Gets the default timeout in seconds.
     */
    public long getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }
}
