package com.yisy.modules.doramcu.admin.service;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder.OrderStatus;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Orchestrates order processing, test scheduling, and report generation.
 * Receives orders, schedules test execution via a thread pool, and tracks order status.
 */
@Service
public class AiAdminService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminService.class);

    private final AiAdminOrderRepository orderRepository;
    private final AiAdminTestReportRepository reportRepository;
    private final TestExecutor testExecutor;
    private final TestReportGenerator testReportGenerator;
    private final ThreadPoolTaskExecutor taskExecutor;

    // Track running orders for polling
    private final Set<Long> runningOrders = new HashSet<>();

    public AiAdminService(AiAdminOrderRepository orderRepository,
                          AiAdminTestReportRepository reportRepository,
                          TestExecutor testExecutor,
                          TestReportGenerator testReportGenerator) {
        this.orderRepository = orderRepository;
        this.reportRepository = reportRepository;
        this.testExecutor = testExecutor;
        this.testReportGenerator = testReportGenerator;
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(2);
        this.taskExecutor.setMaxPoolSize(4);
        this.taskExecutor.setQueueCapacity(10);
        this.taskExecutor.setThreadNamePrefix("ai-admin-test-");
        this.taskExecutor.initialize();
    }

    /**
     * Submits a new test order. The order is saved with PENDING status and will be
     * dispatched to an available AI admin by the AiAdminDispatchService.
     * <p>
     * The dispatch service runs periodically (every 30 seconds) and assigns pending
     * orders to AI admin characters based on galaxy affinity and workload balancing.
     * </p>
     *
     * @param submittedBy the admin username
     * @param orderText the natural language order description
     * @return the saved order with PENDING status
     */
    @Transactional
    public AiAdminOrder submitOrder(String submittedBy, String orderText) {
        return submitOrder(submittedBy, orderText, null, null, null, null);
    }

    /**
     * Submits a new test order with a target location.
     * The AI admin assigned to this order will move to the target location.
     *
     * @param submittedBy the admin username
     * @param orderText the natural language order description
     * @param targetGalaxy target galaxy (optional)
     * @param targetPosX target X coordinate (optional)
     * @param targetPosY target Y coordinate (optional)
     * @param targetPosZ target Z coordinate (optional)
     * @return the saved order with PENDING status
     */
    @Transactional
    public AiAdminOrder submitOrder(String submittedBy, String orderText,
                                     String targetGalaxy, Double targetPosX, Double targetPosY, Double targetPosZ) {
        AiAdminOrder order = new AiAdminOrder(submittedBy, orderText);
        order.setTargetGalaxy(targetGalaxy);
        order.setTargetPosX(targetPosX);
        order.setTargetPosY(targetPosY);
        order.setTargetPosZ(targetPosZ);
        order = orderRepository.save(order);
        if (order.hasTargetLocation()) {
            log.info("Submitted order {} by {}: {} -> target ({}, {}, {}) in {}",
                    order.getId(), submittedBy, orderText, targetPosX, targetPosY, targetPosZ, targetGalaxy);
        } else {
            log.info("Submitted order {} by {}: {} (awaiting dispatch to AI admin)", order.getId(), submittedBy, orderText);
        }
        return order;
    }

    /**
     * Submits a test order and immediately executes it without waiting for dispatch.
     * Use this method for urgent orders that should not wait for the dispatch cycle.
     *
     * @param submittedBy the admin username
     * @param orderText the natural language order description
     * @return the saved order
     */
    @Transactional
    public AiAdminOrder submitAndExecuteOrder(String submittedBy, String orderText) {
        AiAdminOrder order = new AiAdminOrder(submittedBy, orderText);
        order = orderRepository.save(order);
        log.info("Submitted and executing order {} by {}: {}", order.getId(), submittedBy, orderText);
        scheduleTestExecution(order, orderText);
        return order;
    }

    /**
     * Parses order text to extract test categories or test IDs.
     * Supports natural language like "test API health", "test game combat", "test all".
     *
     * @param orderText the natural language order
     * @return a list of categories to test, or empty list for all tests
     */
    public List<String> parseOrderCategories(String orderText) {
        if (orderText == null || orderText.isBlank()) {
            return List.of();
        }

        String lower = orderText.toLowerCase();
        Set<String> categories = new HashSet<>();

        // Check for category keywords
        if (lower.contains("api") || lower.contains("endpoint") || lower.contains("health")) {
            categories.add("API");
        }
        if (lower.contains("game") || lower.contains("combat") || lower.contains("galaxy") || lower.contains("travel")) {
            categories.add("GAME");
        }
        if (lower.contains("ai") || lower.contains("chat") || lower.contains("llm")) {
            categories.add("AI_CHAT");
        }
        if (lower.contains("auth") || lower.contains("login") || lower.contains("admin") || lower.contains("security")) {
            categories.add("AUTH");
        }

        // If no specific categories found, run all tests
        if (categories.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(categories);
    }

    /**
     * Checks if an order is currently running.
     */
    public boolean isOrderRunning(Long orderId) {
        return runningOrders.contains(orderId);
    }

    /**
     * Waits for an order to complete, polling at intervals.
     *
     * @param orderId the order to wait for
     * @param timeoutSeconds maximum wait time
     * @return true if the order completed successfully, false if timed out
     */
    @SuppressWarnings("BusyWait")
    public boolean waitForOrder(Long orderId, long timeoutSeconds) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutSeconds * 1000) {
            Optional<AiAdminOrder> orderOpt = getOrder(orderId);
            if (orderOpt.isPresent()) {
                AiAdminOrder.OrderStatus status = orderOpt.get().getStatus();
                if (status == AiAdminOrder.OrderStatus.COMPLETED || status == AiAdminOrder.OrderStatus.FAILED) {
                    return status == AiAdminOrder.OrderStatus.COMPLETED;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Schedules test execution for the given order in a background thread.
     * Parses the order text to determine which tests to run.
     */
    private void scheduleTestExecution(AiAdminOrder order, String orderText) {
        taskExecutor.execute(() -> {
            try {
                // Update status to RUNNING
                order.setStatus(OrderStatus.RUNNING);
                orderRepository.save(order);
                runningOrders.add(order.getId());
                log.info("Executing order {}", order.getId());

                // Parse order to determine which tests to run
                List<String> categories = parseOrderCategories(orderText);
                List<AiAdminTestCase> testCases;

                if (categories.isEmpty()) {
                    // Run all tests
                    testCases = testExecutor.runAllTests();
                } else {
                    // Run tests for selected categories
                    List<AiAdminTestCase> allResults = new ArrayList<>();
                    for (String category : categories) {
                        allResults.addAll(testExecutor.runTestsByCategory(category));
                    }
                    testCases = allResults;
                }

                // Generate report
                AiAdminTestReport report = testReportGenerator.generateReport(order.getId(), testCases);

                // Mark order as completed
                order.setStatus(OrderStatus.COMPLETED);
                order.setCompletedAt(LocalDateTime.now());
                orderRepository.save(order);
                runningOrders.remove(order.getId());

                log.info("Order {} completed. Report {}: {} passed, {} failed",
                        order.getId(), report.getId(), report.getPassed(), report.getFailed());

            } catch (Exception e) {
                log.error("Order {} failed: {}", order.getId(), e.getMessage(), e);
                order.setStatus(OrderStatus.FAILED);
                order.setCompletedAt(LocalDateTime.now());
                orderRepository.save(order);
                runningOrders.remove(order.getId());
            }
        });
    }

    /**
     * Gets an order by ID.
     */
    @Transactional(readOnly = true)
    public Optional<AiAdminOrder> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Lists all orders.
     */
    @Transactional(readOnly = true)
    public List<AiAdminOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Gets the report for an order.
     */
    @Transactional(readOnly = true)
    public Optional<AiAdminTestReport> getReportForOrder(Long orderId) {
        List<AiAdminTestReport> reports = reportRepository.findByOrderId(orderId);
        return reports.stream().findFirst();
    }

    /**
     * Lists all reports.
     */
    @Transactional(readOnly = true)
    public List<AiAdminTestReport> getAllReports() {
        return reportRepository.findAll();
    }

    /**
     * Gets a report by ID.
     */
    @Transactional(readOnly = true)
    public Optional<AiAdminTestReport> getReport(Long reportId) {
        return reportRepository.findById(reportId);
    }

    /**
     * Finds orders by text prefix.
     */
    @Transactional(readOnly = true)
    public List<AiAdminOrder> getOrdersByPrefix(String prefix) {
        return orderRepository.findBySubmittedByContaining(prefix);
    }

    /**
     * Finds orders by text prefix and status.
     */
    @Transactional(readOnly = true)
    public List<AiAdminOrder> getOrdersByPrefixAndStatus(String prefix, AiAdminOrder.OrderStatus status) {
        return orderRepository.findBySubmittedByContainingAndStatus(prefix, status);
    }
}
