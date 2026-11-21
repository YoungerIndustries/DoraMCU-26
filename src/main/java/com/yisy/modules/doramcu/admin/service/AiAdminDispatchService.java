package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.controller.misc.PresenceWebSocketHandler;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatches pending work orders to available AI admin characters.
 * <p>
 * This service bridges the gap between work order creation and execution:
 * <ul>
 *   <li>Periodically scans for unassigned PENDING orders</li>
 *   <li>Finds the Galaxy Overseer AI admin for the order's galaxy</li>
 *   <li>Falls back to any available AI admin if no galaxy-specific overseer is free</li>
 *   <li>Assigns the order to the selected AI admin character</li>
 *   <li>Executes the order tests in a background thread</li>
 *   <li>Updates the AI admin's experience and workload on completion</li>
 * </ul>
 * </p>
 * <p>
 * Dispatch strategy (priority order):
 * <ol>
 *   <li>Galaxy Overseer for the matching galaxy (if idle)</li>
 *   <li>Any AI admin in the same empire (lowest active order count first)</li>
 *   <li>Any available AI admin across all empires (lowest active order count first)</li>
 * </ol>
 * </p>
 */
@Service
public class AiAdminDispatchService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminDispatchService.class);

    /** Maximum concurrent orders per AI admin before considering it busy. */
    public static final int MAX_ORDERS_PER_ADMIN = 3;

    /** XP awarded for completing a work order successfully. */
    public static final int XP_PER_ORDER_COMPLETED = 10;

    /** XP awarded for each passing test within an order. */
    public static final int XP_PER_TEST_PASSED = 1;

    /** Maximum number of retries for a failed order before permanent failure. */
    public static final int MAX_RETRIES = 3;

    /** Minutes after which a RUNNING order is considered stale and reclaimed. */
    public static final int STALE_ORDER_MINUTES = 30;

    private final AiAdminOrderRepository orderRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final GalaxyRepository galaxyRepository;
    private final TestExecutor testExecutor;
    private final TestRegistry testRegistry;
    private final TestReportGenerator testReportGenerator;
    private final AiAdminNotificationService notificationService;
    private final AiAdminLlmService llmService;
    private final AiAdminOrderProgressService progressService;
    private final AiAdminProductionService productionService;
    private final PresenceWebSocketHandler presenceWebSocketHandler;
    private final ThreadPoolTaskExecutor dispatchExecutor;

    /** Tracks which orders are currently executing, keyed by order ID. */
    private final Map<Long, Long> executingOrderToAdminId = new ConcurrentHashMap<>();

    public AiAdminDispatchService(AiAdminOrderRepository orderRepository,
                                     AiCharacterRepository aiCharacterRepository,
                                     GalaxyRepository galaxyRepository,
                                     TestExecutor testExecutor,
                                     TestRegistry testRegistry,
                                     TestReportGenerator testReportGenerator,
                                     AiAdminNotificationService notificationService,
                                     AiAdminLlmService llmService,
                                     AiAdminOrderProgressService progressService,
                                     AiAdminProductionService productionService,
                                     PresenceWebSocketHandler presenceWebSocketHandler) {
        this.orderRepository = orderRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.galaxyRepository = galaxyRepository;
        this.testExecutor = testExecutor;
        this.testRegistry = testRegistry;
        this.testReportGenerator = testReportGenerator;
        this.notificationService = notificationService;
        this.llmService = llmService;
        this.progressService = progressService;
        this.productionService = productionService;
        this.presenceWebSocketHandler = presenceWebSocketHandler;

        this.dispatchExecutor = new ThreadPoolTaskExecutor();
        this.dispatchExecutor.setCorePoolSize(2);
        this.dispatchExecutor.setMaxPoolSize(8);
        this.dispatchExecutor.setQueueCapacity(20);
        this.dispatchExecutor.setThreadNamePrefix("ai-admin-dispatch-");
        this.dispatchExecutor.initialize();
    }

    // ------------------------------------------------------------------
    // Scheduled dispatch loop
    // ------------------------------------------------------------------

    /**
     * Runs every 30 seconds to find and dispatch unassigned pending orders.
     * Also handles retrying failed orders and reclaiming stale orders.
     */
    @Scheduled(fixedRate = 30000)
    public void dispatchPendingOrders() {
        try {
            List<AiAdminOrder> unassignedPending = orderRepository
                    .findByStatusAndAssignedAiAdminIdIsNull(AiAdminOrder.OrderStatus.PENDING);

            if (!unassignedPending.isEmpty()) {
                log.info("Dispatch scan: found {} unassigned pending orders", unassignedPending.size());
                for (AiAdminOrder order : unassignedPending) {
                    dispatchSingleOrder(order);
                }
            }

            // Retry failed orders that haven't exceeded max retries
            retryFailedOrders();

            // Reclaim stale orders stuck in RUNNING status
            reclaimStaleOrders();

        } catch (Exception e) {
            log.error("Dispatch scan failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Retries failed orders that haven't exceeded their maximum retry count.
     * Resets the order to PENDING status so it can be dispatched again.
     */
    @Transactional
    public void retryFailedOrders() {
        List<AiAdminOrder> failedOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.FAILED);

        for (AiAdminOrder order : failedOrders) {
            if (order.getRetryCount() >= order.getMaxRetries()) {
                log.debug("Order {} exceeded max retries ({}/{}) - permanently failed",
                        order.getId(), order.getRetryCount(), order.getMaxRetries());
                continue;
            }

            order.setRetryCount(order.getRetryCount() + 1);
            order.setStatus(AiAdminOrder.OrderStatus.PENDING);
            order.setAssignedAiAdminId(null);
            order.setCompletedAt(null);
            orderRepository.save(order);

            // Notify the previously assigned admin if known
            if (order.getAssignedAiAdminId() != null) {
                aiCharacterRepository.findById(order.getAssignedAiAdminId()).ifPresent(admin -> {
                    try {
                        notificationService.notifyOrderRetried(admin, order);
                    } catch (Exception e) {
                        log.warn("Failed to send retry notification for admin {}: {}",
                                admin.getId(), e.getMessage());
                    }
                });
            }

            log.info("Retrying order {} (attempt {}/{})",
                    order.getId(), order.getRetryCount(), order.getMaxRetries());
        }
    }

    /**
     * Reclaims orders stuck in RUNNING status for longer than STALE_ORDER_MINUTES.
     * Resets them to PENDING so they can be dispatched to a different AI admin.
     */
    @Transactional
    public void reclaimStaleOrders() {
        List<AiAdminOrder> runningOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.RUNNING);
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(STALE_ORDER_MINUTES);

        for (AiAdminOrder order : runningOrders) {
            // A running order is stale if it was created more than STALE_ORDER_MINUTES ago
            // and hasn't completed yet
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(staleThreshold)) {
                log.warn("Reclaiming stale order {} (running for >{} minutes, assigned to admin {})",
                        order.getId(), STALE_ORDER_MINUTES, order.getAssignedAiAdminId());

                // Release the assigned admin if still tracked
                if (order.getAssignedAiAdminId() != null) {
                    aiCharacterRepository.findById(order.getAssignedAiAdminId()).ifPresent(admin -> {
                        releaseAdminFromOrder(admin, order.getId());
                        // Notify the admin about the reclaimed order
                        try {
                            notificationService.notifyOrderReclaimed(admin, order);
                        } catch (Exception e) {
                            log.warn("Failed to send reclaim notification for admin {}: {}",
                                    admin.getId(), e.getMessage());
                        }
                    });
                }

                order.setStatus(AiAdminOrder.OrderStatus.PENDING);
                order.setAssignedAiAdminId(null);
                order.setCompletedAt(null);
                orderRepository.save(order);

                executingOrderToAdminId.remove(order.getId());
            }
        }
    }

    // ------------------------------------------------------------------
    // Core dispatch logic
    // ------------------------------------------------------------------

    /**
     * Attempts to dispatch a single unassigned order to an available AI admin.
     *
     * @param order the pending order to dispatch
     */
    @Transactional
    public void dispatchSingleOrder(AiAdminOrder order) {
        Optional<AiCharacter> assignedAdmin = selectAiAdminForOrder(order);

        if (assignedAdmin.isEmpty()) {
            log.debug("No available AI admin for order {} (submittedBy: {})",
                    order.getId(), order.getSubmittedBy());
            return;
        }

        AiCharacter admin = assignedAdmin.get();

        // Assign the order to this AI admin
        order.setAssignedAiAdminId(admin.getId());
        order.setStatus(AiAdminOrder.OrderStatus.RUNNING);
        orderRepository.save(order);

        // Save home position before moving to order location
        if (admin.hasPosition() && !admin.hasHomePosition()) {
            admin.setHomePosX(admin.getPosX());
            admin.setHomePosY(admin.getPosY());
            admin.setHomePosZ(admin.getPosZ());
        }

        // Move AI admin to the order's target location if specified
        if (order.hasTargetLocation()) {
            admin.setPosX(order.getTargetPosX());
            admin.setPosY(order.getTargetPosY());
            admin.setPosZ(order.getTargetPosZ());
            if (order.getTargetGalaxy() != null) {
                admin.setGalaxyName(order.getTargetGalaxy());
            }
            log.info("AI admin '{}' moving to order location ({}, {}, {}) in {}",
                    admin.getName(), order.getTargetPosX(), order.getTargetPosY(),
                    order.getTargetPosZ(), order.getTargetGalaxy());
        }

        // Update AI admin's workload
        admin.setCurrentOrderId(order.getId());
        admin.setActiveOrderCount(admin.getActiveOrderCount() + 1);
        aiCharacterRepository.save(admin);

        executingOrderToAdminId.put(order.getId(), admin.getId());

        log.info("Dispatched order {} to AI admin '{}' (id={}), activeOrders={}",
                order.getId(), admin.getName(), admin.getId(), admin.getActiveOrderCount());

        // Notify the AI admin about the new assignment
        try {
            notificationService.notifyOrderAssigned(admin, order);
        } catch (Exception e) {
            log.warn("Failed to send assignment notification for order {}: {}",
                    order.getId(), e.getMessage());
        }

        // Warn if approaching max capacity
        if (admin.getActiveOrderCount() >= MAX_ORDERS_PER_ADMIN - 1) {
            try {
                notificationService.notifyWorkloadWarning(admin,
                        admin.getActiveOrderCount(), MAX_ORDERS_PER_ADMIN);
            } catch (Exception e) {
                log.warn("Failed to send workload warning for admin {}: {}",
                        admin.getId(), e.getMessage());
            }
        }

        // Execute the order in a background thread
        executeOrderAsync(order, admin);
    }

    /**
     * Selects the best available AI admin for a given order using the dispatch strategy.
     *
     * @param order the order to find an AI admin for
     * @return the selected AI admin, or empty if none available
     */
    @Transactional(readOnly = true)
    public Optional<AiCharacter> selectAiAdminForOrder(AiAdminOrder order) {
        String submittedBy = order.getSubmittedBy();

        // Strategy 1: Extract galaxy name from "GalaxyOverseer:{galaxyName}" pattern
        // and try to find the Galaxy Overseer for that galaxy
        Optional<AiCharacter> galaxyOverseer = findGalaxyOverseerForOrder(submittedBy);
        if (galaxyOverseer.isPresent() && isAvailable(galaxyOverseer.get())) {
            return galaxyOverseer;
        }

        // Strategy 2: Find any AI admin in the same empire
        String empireName = extractEmpireName(submittedBy);
        if (empireName != null) {
            List<AiCharacter> empireAdmins = aiCharacterRepository
                    .findByAiAdminTrueAndActiveTrueAndEmpireNameAndCurrentOrderIdIsNull(empireName);
            Optional<AiCharacter> best = empireAdmins.stream()
                    .filter(this::isAvailable)
                    .min(Comparator.comparingInt(AiCharacter::getActiveOrderCount));
            if (best.isPresent()) {
                return best;
            }
        }

        // Strategy 3: Any available AI admin across all empires
        List<AiCharacter> allAvailable = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNull();
        return allAvailable.stream()
                .filter(this::isAvailable)
                .min(Comparator.comparingInt(AiCharacter::getActiveOrderCount));
    }

    /**
     * Checks if an AI admin is available to take on a new order.
     */
    private boolean isAvailable(AiCharacter admin) {
        return admin.getActiveOrderCount() < MAX_ORDERS_PER_ADMIN;
    }

    /**
     * Extracts the galaxy name from the submittedBy field and finds the Galaxy Overseer.
     */
    private Optional<AiCharacter> findGalaxyOverseerForOrder(String submittedBy) {
        if (submittedBy == null || !submittedBy.startsWith("GalaxyOverseer:")) {
            return Optional.empty();
        }

        String galaxyName = submittedBy.substring("GalaxyOverseer:".length());
        Optional<Galaxy> galaxy = galaxyRepository.findByName(galaxyName);

        if (galaxy.isPresent() && galaxy.get().getGalaxyOverseerId() != null) {
            return aiCharacterRepository.findById(galaxy.get().getGalaxyOverseerId());
        }

        return Optional.empty();
    }

    /**
     * Extracts the empire/galaxy name from the submittedBy field.
     */
    private String extractEmpireName(String submittedBy) {
        if (submittedBy == null) {
            return null;
        }
        if (submittedBy.startsWith("GalaxyOverseer:")) {
            return submittedBy.substring("GalaxyOverseer:".length());
        }
        return submittedBy;
    }

    // ------------------------------------------------------------------
    // Order execution
    // ------------------------------------------------------------------

    /**
     * Executes an order's tests in a background thread using the assigned AI admin.
     */
    private void executeOrderAsync(AiAdminOrder order, AiCharacter admin) {
        dispatchExecutor.execute(() -> {
            try {
                log.info("AI admin '{}' executing order {}...", admin.getName(), order.getId());

                // Determine execution path: production order, test categories, or LLM analysis
                List<AiAdminTestCase> testCases;
                AiAdminTestReport report;

                // Path 1: Production order — execute build contract
                if (order.isProductionOrder()) {
                    log.info("AI admin '{}' executing production order {} (build contract {})",
                            admin.getName(), order.getId(), order.getBuildContractId());

                    // Assign and start the build contract
                    productionService.assignAndStartContract(admin, order);

                    // Simulate contract execution progress
                    int estimatedTurns = order.getTestsTotal() != null ? order.getTestsTotal() : 1;
                    for (int turn = 1; turn <= estimatedTurns; turn++) {
                        int progress = (int) Math.round((turn / (double) estimatedTurns) * 100);
                        productionService.updateContractAndOrderProgress(order, progress);
                        progressService.updateProgress(order.getId(),
                                "Turn " + turn + "/" + estimatedTurns + ": " + progress + "%",
                                turn, estimatedTurns);
                    }

                    // Complete the build contract
                    productionService.completeContract(admin, order);

                    // Mark order as completed
                    order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
                    order.setCompletedAt(LocalDateTime.now());
                    orderRepository.save(order);

                    // Update AI admin's workload
                    completeOrderForAdmin(admin, order, null);

                    // Notify the AI admin about the completion
                    try {
                        notificationService.notifyOrderCompleted(admin, order, null);
                    } catch (Exception e) {
                        log.warn("Failed to send completion notification for order {}: {}",
                                order.getId(), e.getMessage());
                    }

                    log.info("Production order {} completed by AI admin '{}': build contract {} finished",
                            order.getId(), admin.getName(), order.getBuildContractId());

                    executingOrderToAdminId.remove(order.getId());
                    return;
                }

                // Path 2: Test category order — run predefined tests
                if (isTestOrder(order.getOrderText())) {
                    // Test category order: run predefined tests
                    List<String> categories = parseOrderCategories(order.getOrderText());
                    
                    // Create progress callback for this order
                    Long orderId = order.getId();
                    OrderProgressCallback progressCallback = new OrderProgressCallback() {
                        @Override
                        public void onTestStarted(String testName) {
                            progressService.setCurrentTest(orderId, testName);
                        }

                        @Override
                        public void onProgressUpdate(int testsCompleted, int testsTotal) {
                            progressService.updateProgress(orderId, null, testsCompleted, testsTotal);
                        }
                    };
                    
                    if (categories.isEmpty()) {
                        testCases = testExecutor.runAllTests(progressCallback);
                    } else {
                        List<AiAdminTestCase> allResults = new ArrayList<>();
                        int totalCount = 0;
                        for (String category : categories) {
                            totalCount += testRegistry.getTestsByCategory(category).size();
                        }
                        order.setTestsTotal(totalCount);
                        orderRepository.save(order);
                        
                        for (String category : categories) {
                            List<AiAdminTestCase> categoryResults = testExecutor.runTestsByCategory(category, progressCallback);
                            allResults.addAll(categoryResults);
                        }
                        testCases = allResults;
                    }
                    report = testReportGenerator.generateReport(order.getId(), testCases);
                } else {
                    // Natural language order: use LLM for analysis
                    log.info("AI admin '{}' using LLM to analyze order {}", admin.getName(), order.getId());

                    // Set progress for LLM orders
                    order.setTestsTotal(1);
                    order.setTestsCompleted(0);
                    order.setProgressPercent(0);
                    order.setCurrentTestName("LLM Analysis");
                    orderRepository.save(order);

                    AiAdminLlmService.LlmAnalysisResult llmResult = llmService.analyzeOrder(admin, order);

                    // Store LLM response for auditability
                    order.setLlmResponse(llmResult.getRawResponse());
                    order.setTestsCompleted(1);
                    order.setProgressPercent(100);
                    orderRepository.save(order);

                    // Generate a test report from LLM results
                    testCases = createTestCaseFromLlmResult(llmResult);
                    report = testReportGenerator.generateReport(order.getId(), testCases);
                }

                // Mark order as completed
                order.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
                order.setCompletedAt(LocalDateTime.now());
                orderRepository.save(order);

                // Update AI admin's experience and workload
                completeOrderForAdmin(admin, order, report);

                // Notify the AI admin about the completion
                try {
                    notificationService.notifyOrderCompleted(admin, order, report);
                } catch (Exception e) {
                    log.warn("Failed to send completion notification for order {}: {}",
                            order.getId(), e.getMessage());
                }

                log.info("Order {} completed by AI admin '{}': {} passed, {} failed",
                        order.getId(), admin.getName(), report.getPassed(), report.getFailed());

            } catch (Exception e) {
                log.error("Order {} failed during execution by AI admin '{}': {}",
                        order.getId(), admin.getName(), e.getMessage(), e);

                // Notify the AI admin about the failure
                try {
                    notificationService.notifyOrderFailed(admin, order, e.getMessage());
                } catch (Exception ne) {
                    log.warn("Failed to send failure notification for order {}: {}",
                            order.getId(), ne.getMessage());
                }

                // Check if we should retry this order
                if (order.getRetryCount() >= order.getMaxRetries()) {
                    // Permanently failed - no more retries
                    order.setStatus(AiAdminOrder.OrderStatus.FAILED);
                    order.setCompletedAt(LocalDateTime.now());
                    orderRepository.save(order);
                    log.warn("Order {} permanently failed after {} retries",
                            order.getId(), order.getRetryCount());
                } else {
                    // Mark as failed - the scheduled retry mechanism will pick it up
                    order.setStatus(AiAdminOrder.OrderStatus.FAILED);
                    order.setCompletedAt(LocalDateTime.now());
                    orderRepository.save(order);
                    log.info("Order {} failed - will be retried by dispatch scheduler (attempt {}/{})",
                            order.getId(), order.getRetryCount(), order.getMaxRetries());
                }

                // Release the admin even on failure
                releaseAdminFromOrder(admin, order.getId());
            }

            executingOrderToAdminId.remove(order.getId());
        });
    }

    /**
     * Updates the AI admin's stats after successfully completing an order.
     * Returns the admin to their home position if they have one.
     * Called from within executeOrderAsync (non-transactional context),
     * so the transaction is managed here directly.
     */
    @Transactional
    public void completeOrderForAdmin(AiCharacter admin, AiAdminOrder order, AiAdminTestReport report) {
        int xpEarned = XP_PER_ORDER_COMPLETED;
        if (report != null) {
            xpEarned += report.getPassed() * XP_PER_TEST_PASSED;
        }

        admin.addExperience(xpEarned);
        int newCount = Math.max(0, admin.getActiveOrderCount() - 1);
        admin.setActiveOrderCount(newCount);
        if (admin.getCurrentOrderId() != null && admin.getCurrentOrderId().equals(order.getId())) {
            admin.setCurrentOrderId(null);
        }

        // Return AI admin to home position after completing the order
        if (admin.hasHomePosition()) {
            admin.setPosX(admin.getHomePosX());
            admin.setPosY(admin.getHomePosY());
            admin.setPosZ(admin.getHomePosZ());
            log.info("AI admin '{}' returned to home position ({}, {}, {})",
                    admin.getName(), admin.getHomePosX(), admin.getHomePosY(), admin.getHomePosZ());
        }

        aiCharacterRepository.save(admin);

        log.info("AI admin '{}' earned {} XP for completing order {}",
                admin.getName(), xpEarned, order.getId());
    }

    /**
     * Releases an AI admin from an order (decrements workload, clears current order).
     * Returns the admin to their home position if they have one.
     * Called from within executeOrderAsync (non-transactional context).
     */
    @Transactional
    public void releaseAdminFromOrder(AiCharacter admin, Long orderId) {
        int newCount = Math.max(0, admin.getActiveOrderCount() - 1);
        admin.setActiveOrderCount(newCount);
        if (admin.getCurrentOrderId() != null && admin.getCurrentOrderId().equals(orderId)) {
            admin.setCurrentOrderId(null);
        }

        // Return AI admin to home position when released from an order
        if (admin.hasHomePosition()) {
            admin.setPosX(admin.getHomePosX());
            admin.setPosY(admin.getHomePosY());
            admin.setPosZ(admin.getHomePosZ());
            log.info("AI admin '{}' released and returned to home position ({}, {}, {})",
                    admin.getName(), admin.getHomePosX(), admin.getHomePosY(), admin.getHomePosZ());
        }

        aiCharacterRepository.save(admin);
        // Notify dashboard of workload change
        try {
            presenceWebSocketHandler.broadcastDashboardUpdate("admin_workload_changed", admin.getId());
        } catch (Exception e) {
            log.warn("Failed to broadcast dashboard update: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Order parsing (mirrors AiAdminService logic)
    // ------------------------------------------------------------------

    /**
     * Parses order text to extract test categories.
     */
    public List<String> parseOrderCategories(String orderText) {
        if (orderText == null || orderText.isBlank()) {
            return List.of();
        }

        String lower = orderText.toLowerCase();
        Map<String, Boolean> categories = new HashMap<>();

        if (lower.contains("api") || lower.contains("endpoint") || lower.contains("health")) {
            categories.put("API", true);
        }
        if (lower.contains("game") || lower.contains("combat") || lower.contains("galaxy") || lower.contains("travel")) {
            categories.put("GAME", true);
        }
        if (lower.contains("ai") || lower.contains("chat") || lower.contains("llm")) {
            categories.put("AI_CHAT", true);
        }
        if (lower.contains("auth") || lower.contains("login") || lower.contains("admin") || lower.contains("security")) {
            categories.put("AUTH", true);
        }

        if (categories.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(categories.keySet());
    }

    /**
     * Checks if an order text contains test category keywords.
     * If true, the order is executed via TestExecutor.
     * If false, the order is sent to the LLM for analysis.
     */
    public boolean isTestOrder(String orderText) {
        if (orderText == null || orderText.isBlank()) {
            return false;
        }
        String lower = orderText.toLowerCase();
        return lower.contains("api") || lower.contains("endpoint") || lower.contains("health")
                || lower.contains("game") || lower.contains("combat") || lower.contains("travel")
                || lower.contains("auth") || lower.contains("login") || lower.contains("security")
                || lower.contains("test");
    }

    /**
     * Creates a test case from an LLM analysis result so it can be included
     * in the standard test report format.
     */
    private List<AiAdminTestCase> createTestCaseFromLlmResult(AiAdminLlmService.LlmAnalysisResult result) {
        AiAdminTestCase testCase = new AiAdminTestCase(null, "LLM Analysis", "LLM");
        testCase.setStatus(result.isSuccess() ? AiAdminTestCase.TestStatus.PASSED : AiAdminTestCase.TestStatus.FAILED);
        testCase.setErrorMessage(result.getAction() + ": " + result.getReasoning());
        return List.of(testCase);
    }

    // ------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------

    /**
     * Gets a dispatch summary showing current AI admin workload distribution.
     */
    @Transactional(readOnly = true)
    public DispatchSummary getDispatchSummary() {
        List<AiCharacter> allAiAdmins = aiCharacterRepository.findByAiAdminTrue();
        List<AiAdminOrder> pendingOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.PENDING);
        List<AiAdminOrder> runningOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.RUNNING);

        int totalAdmins = allAiAdmins.size();
        int availableAdmins = 0;
        int busyAdmins = 0;
        int totalActiveOrders = 0;

        for (AiCharacter admin : allAiAdmins) {
            if (admin.isActive()) {
                totalActiveOrders += admin.getActiveOrderCount();
                if (isAvailable(admin)) {
                    availableAdmins++;
                } else {
                    busyAdmins++;
                }
            }
        }

        int unassignedPending = 0;
        int assignedPending = 0;
        for (AiAdminOrder order : pendingOrders) {
            if (order.getAssignedAiAdminId() == null) {
                unassignedPending++;
            } else {
                assignedPending++;
            }
        }

        DispatchSummary summary = new DispatchSummary();
        summary.setTotalAiAdmins(totalAdmins);
        summary.setAvailableAiAdmins(availableAdmins);
        summary.setBusyAiAdmins(busyAdmins);
        summary.setTotalActiveOrders(totalActiveOrders);
        summary.setUnassignedPendingOrders(unassignedPending);
        summary.setAssignedPendingOrders(assignedPending);
        summary.setRunningOrders(runningOrders.size());
        summary.setExecutingOrders(executingOrderToAdminId.size());
        return summary;
    }

    /**
     * Gets all AI admins with their current workload.
     */
    @Transactional(readOnly = true)
    public List<AiCharacter> getAiAdminWorkload() {
        return aiCharacterRepository.findByAiAdminTrue();
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    /**
     * Summary of the current dispatch state.
     */
    public static class DispatchSummary {
        private int totalAiAdmins;
        private int availableAiAdmins;
        private int busyAiAdmins;
        private int totalActiveOrders;
        private int unassignedPendingOrders;
        private int assignedPendingOrders;
        private int runningOrders;
        private int executingOrders;

        public int getTotalAiAdmins() { return totalAiAdmins; }
        public void setTotalAiAdmins(int totalAiAdmins) { this.totalAiAdmins = totalAiAdmins; }
        public int getAvailableAiAdmins() { return availableAiAdmins; }
        public void setAvailableAiAdmins(int availableAiAdmins) { this.availableAiAdmins = availableAiAdmins; }
        public int getBusyAiAdmins() { return busyAiAdmins; }
        public void setBusyAiAdmins(int busyAiAdmins) { this.busyAiAdmins = busyAiAdmins; }
        public int getTotalActiveOrders() { return totalActiveOrders; }
        public void setTotalActiveOrders(int totalActiveOrders) { this.totalActiveOrders = totalActiveOrders; }
        public int getUnassignedPendingOrders() { return unassignedPendingOrders; }
        public void setUnassignedPendingOrders(int unassignedPendingOrders) { this.unassignedPendingOrders = unassignedPendingOrders; }
        public int getAssignedPendingOrders() { return assignedPendingOrders; }
        public void setAssignedPendingOrders(int assignedPendingOrders) { this.assignedPendingOrders = assignedPendingOrders; }
        public int getRunningOrders() { return runningOrders; }
        public void setRunningOrders(int runningOrders) { this.runningOrders = runningOrders; }
        public int getExecutingOrders() { return executingOrders; }
        public void setExecutingOrders(int executingOrders) { this.executingOrders = executingOrders; }
    }
}
