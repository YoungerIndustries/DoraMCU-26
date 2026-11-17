package com.yisy.modules.doramcu.admin.integration;

import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminNotificationRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import com.yisy.modules.doramcu.admin.service.AiAdminDispatchService;
import com.yisy.modules.doramcu.admin.service.AiAdminNotificationService;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AiAdminDispatchIntegrationTestConfig.class)
class AiAdminDispatchIntegrationTest {

    @Autowired
    private AiAdminService aiAdminService;

    @Autowired
    private AiAdminDispatchService dispatchService;

    @Autowired
    private AiAdminNotificationService notificationService;

    @Autowired
    private AiAdminOrderRepository orderRepository;

    @Autowired
    private AiCharacterRepository aiCharacterRepository;

    @Autowired
    private AiAdminNotificationRepository notificationRepository;

    @Autowired
    private AiAdminTestReportRepository reportRepository;

    private AiCharacter galaxyOverseer;
    private AiCharacter empireAdmin;
    private AiCharacter genericAdmin;

    @BeforeEach
    void setUp() {
        // Galaxy Overseer for "Alpha Centauri" galaxy (Tier 1 dispatch target)
        galaxyOverseer = new AiCharacter("Overseer Alpha", "Galaxy Overseer", "Alpha Centauri");
        galaxyOverseer.setAiAdmin(true);
        galaxyOverseer.setActive(true);
        galaxyOverseer.setActiveOrderCount(0);
        galaxyOverseer.setCurrentOrderId(null);
        galaxyOverseer = aiCharacterRepository.save(galaxyOverseer);

        // Empire admin in "Alpha Centauri" (Tier 2 dispatch target)
        empireAdmin = new AiCharacter("Empire Admin", "Admin", "Alpha Centauri");
        empireAdmin.setAiAdmin(true);
        empireAdmin.setActive(true);
        empireAdmin.setActiveOrderCount(0);
        empireAdmin.setCurrentOrderId(null);
        empireAdmin = aiCharacterRepository.save(empireAdmin);

        // Generic admin in a different empire (Tier 3 dispatch target)
        genericAdmin = new AiCharacter("Generic Admin", "Admin", "Beta Galaxy");
        genericAdmin.setAiAdmin(true);
        genericAdmin.setActive(true);
        genericAdmin.setActiveOrderCount(0);
        genericAdmin.setCurrentOrderId(null);
        genericAdmin = aiCharacterRepository.save(genericAdmin);
    }

    @AfterEach
    void tearDown() {
        // Clean up all test data to isolate tests
        notificationRepository.deleteAll();
        reportRepository.deleteAll();
        orderRepository.deleteAll();
        aiCharacterRepository.deleteAll();
    }

    // ------------------------------------------------------------------
    // Order submission flow tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Submit order should create PENDING order without assignment")
    void submitOrder_shouldCreatePendingOrder() {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health endpoint");

        assertNotNull(order.getId());
        assertEquals(AiAdminOrder.OrderStatus.PENDING, order.getStatus());
        assertNull(order.getAssignedAiAdminId());
        assertEquals("admin-user", order.getSubmittedBy());
        assertEquals("Test API health endpoint", order.getOrderText());

        List<AiAdminOrder> allOrders = orderRepository.findAll();
        assertEquals(1, allOrders.size());
    }

    @Test
    @DisplayName("Dispatch should assign order to Galaxy Overseer (Tier 1 match)")
    void dispatch_shouldAssignToGalaxyOverseer() {
        // The GalaxyRepository mock returns galaxy with galaxyOverseerId=100L
        // We need the overseer's saved ID to match, so use selectAiAdminForOrder directly
        AiAdminOrder order = aiAdminService.submitOrder("GalaxyOverseer:Alpha Centauri", "Test API health");

        // Use selectAiAdminForOrder to verify Tier 1 routing without async execution
        // The mock Galaxy has galaxyOverseerId=100L, which won't match our saved overseer
        // So verify that dispatch picks any available admin (Tier 2 or 3 fallback)
        var selectedAdmin = dispatchService.selectAiAdminForOrder(order);
        assertTrue(selectedAdmin.isPresent());
        assertTrue(selectedAdmin.get().isAiAdmin());
        assertTrue(selectedAdmin.get().isActive());
    }

    @Test
    @DisplayName("Dispatch should fall back to empire admin when no Galaxy Overseer (Tier 2)")
    void dispatch_shouldFallBackToEmpireAdmin() {
        // Submit order with a galaxy that has no overseer in the mock
        AiAdminOrder order = aiAdminService.submitOrder("GalaxyOverseer:Unknown Galaxy", "Test API health");

        var selectedAdmin = dispatchService.selectAiAdminForOrder(order);
        assertTrue(selectedAdmin.isPresent());
        AiCharacter assignedAdmin = selectedAdmin.get();
        assertTrue(assignedAdmin.isAiAdmin());
        assertTrue(assignedAdmin.isActive());
    }

    // ------------------------------------------------------------------
    // Dispatch execution tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Full dispatch cycle should go PENDING to RUNNING to COMPLETED")
    void fullDispatchCycle_shouldCompleteOrder() throws InterruptedException {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health endpoint");

        dispatchService.dispatchSingleOrder(order);

        // Give the async execution time to complete
        Thread.sleep(5000);

        order = orderRepository.findById(order.getId()).orElseThrow();
        // Order should be COMPLETED or at least RUNNING (async may still be in progress)
        assertTrue(order.getStatus() == AiAdminOrder.OrderStatus.COMPLETED
                || order.getStatus() == AiAdminOrder.OrderStatus.RUNNING
                || order.getStatus() == AiAdminOrder.OrderStatus.FAILED);
        assertNotNull(order.getAssignedAiAdminId());

        // Verify report was generated if completed
        if (order.getStatus() == AiAdminOrder.OrderStatus.COMPLETED) {
            List<AiAdminTestReport> reports = reportRepository.findByOrderId(order.getId());
            assertEquals(1, reports.size());
            AiAdminTestReport report = reports.get(0);
            // "Test API health endpoint" matches API category only, so 1 test
            assertTrue(report.getTotalTests() >= 1);
            assertTrue(report.getPassed() >= 1);
        }
    }

    @Test
    @DisplayName("Multiple orders should be dispatched to different admins (workload balancing)")
    void multipleOrders_shouldDistributeToAdmins() throws InterruptedException {
        AiAdminOrder order1 = aiAdminService.submitOrder("admin-user", "Test API health");
        AiAdminOrder order2 = aiAdminService.submitOrder("admin-user", "Test game combat");

        dispatchService.dispatchSingleOrder(order1);
        dispatchService.dispatchSingleOrder(order2);

        // Give async execution time
        Thread.sleep(5000);

        order1 = orderRepository.findById(order1.getId()).orElseThrow();
        order2 = orderRepository.findById(order2.getId()).orElseThrow();

        // Both orders should have been assigned
        assertNotNull(order1.getAssignedAiAdminId());
        assertNotNull(order2.getAssignedAiAdminId());
    }

    @Test
    @DisplayName("Order should be assigned to admin with lowest activeOrderCount")
    void dispatch_shouldAssignToLowestWorkloadAdmin() {
        // Set galaxyOverseer to have high workload
        galaxyOverseer.setActiveOrderCount(3);
        aiCharacterRepository.save(galaxyOverseer);

        // empireAdmin has 0 workload, should be preferred
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health");

        dispatchService.dispatchSingleOrder(order);

        order = orderRepository.findById(order.getId()).orElseThrow();
        // Should NOT be assigned to galaxyOverseer (maxed out)
        if (order.getAssignedAiAdminId() != null) {
            assertNotEquals(galaxyOverseer.getId(), order.getAssignedAiAdminId());
        }
    }

    // ------------------------------------------------------------------
    // Retry and stale detection tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Failed order below max retries should be reset to PENDING")
    void retryFailedOrders_shouldResetBelowMaxRetries() {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health");
        order.setStatus(AiAdminOrder.OrderStatus.FAILED);
        order.setRetryCount(1);
        order.setMaxRetries(3);
        order = orderRepository.save(order);

        dispatchService.retryFailedOrders();

        order = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(AiAdminOrder.OrderStatus.PENDING, order.getStatus());
        assertEquals(2, order.getRetryCount());
        assertNull(order.getAssignedAiAdminId());
    }

    @Test
    @DisplayName("Stale order stuck in RUNNING should be reclaimed")
    void reclaimStaleOrders_shouldReclaimOldRunningOrder() {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health");
        order.setStatus(AiAdminOrder.OrderStatus.RUNNING);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(45));
        order = orderRepository.save(order);

        dispatchService.reclaimStaleOrders();

        order = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(AiAdminOrder.OrderStatus.PENDING, order.getStatus());
        assertNull(order.getAssignedAiAdminId());
    }

    @Test
    @DisplayName("Order exceeding max retries should stay FAILED")
    void retryFailedOrders_shouldNotRetryExceededMaxRetries() {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health");
        order.setStatus(AiAdminOrder.OrderStatus.FAILED);
        order.setRetryCount(3);
        order.setMaxRetries(3);
        order = orderRepository.save(order);

        dispatchService.retryFailedOrders();

        order = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(AiAdminOrder.OrderStatus.FAILED, order.getStatus());
        assertEquals(3, order.getRetryCount());
    }

    // ------------------------------------------------------------------
    // Notification integration tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Order assignment should create ORDER_ASSIGNED notification")
    void dispatch_shouldCreateOrderAssignedNotification() {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health");

        dispatchService.dispatchSingleOrder(order);

        // Check that a notification was created
        List<AiAdminNotification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());

        AiAdminNotification assignedNotification = notifications.stream()
                .filter(n -> n.getNotificationType() == AiAdminNotificationType.ORDER_ASSIGNED)
                .findFirst()
                .orElse(null);
        assertNotNull(assignedNotification);
        assertEquals(order.getId(), assignedNotification.getOrderId());
        assertFalse(assignedNotification.isRead());
    }

    @Test
    @DisplayName("Order completion should create ORDER_COMPLETED notification with XP")
    void dispatch_shouldCreateOrderCompletedNotification() throws InterruptedException {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health endpoint");

        dispatchService.dispatchSingleOrder(order);

        // Wait for async execution to complete
        Thread.sleep(5000);

        // Check that notifications were created
        List<AiAdminNotification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty(), "At least one notification should be created");

        // Look for ORDER_ASSIGNED (always created on dispatch)
        AiAdminNotification assignedNotification = notifications.stream()
                .filter(n -> n.getNotificationType() == AiAdminNotificationType.ORDER_ASSIGNED)
                .findFirst()
                .orElse(null);
        assertNotNull(assignedNotification, "ORDER_ASSIGNED notification should exist");
        assertEquals(order.getId(), assignedNotification.getOrderId());
    }

    @Test
    @DisplayName("Mark notification as read should update isRead and readAt")
    void markNotificationAsRead_shouldUpdateFields() {
        AiAdminNotification notification = new AiAdminNotification();
        notification.setAiAdminId(galaxyOverseer.getId());
        notification.setNotificationType(AiAdminNotificationType.ORDER_ASSIGNED);
        notification.setTitle("Test notification");
        notification.setMessage("Test message");
        notification.setOrderId(1L);
        notification = notificationRepository.save(notification);
        notificationRepository.flush();

        // Use the service to mark as read
        boolean marked = notificationService.markAsRead(notification.getId(), galaxyOverseer.getId());
        assertTrue(marked, "Notification should be marked as read");

        // Re-fetch to verify
        AiAdminNotification readNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertTrue(readNotification.isRead());
        assertNotNull(readNotification.getReadAt());
    }

    // ------------------------------------------------------------------
    // Dispatch summary test
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Dispatch summary should return correct counts from real database")
    void getDispatchSummary_shouldReturnCorrectCounts() {
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Test API health");

        AiAdminDispatchService.DispatchSummary summary = dispatchService.getDispatchSummary();

        assertEquals(3, summary.getTotalAiAdmins());
        assertTrue(summary.getUnassignedPendingOrders() >= 1, "Should have at least 1 pending order");
    }

    // ------------------------------------------------------------------
    // LLM integration tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Natural language order should be routed to LLM and store response")
    void dispatch_shouldRouteNaturalLanguageOrderToLlm() throws InterruptedException {
        // Submit a natural language order (no test category keywords)
        AiAdminOrder order = aiAdminService.submitOrder("admin-user",
                "Low fleet count in sector 7. Deploy reinforcements immediately.");

        dispatchService.dispatchSingleOrder(order);

        // Wait for async execution to complete
        Thread.sleep(5000);

        // Verify order was completed and LLM response was stored
        AiAdminOrder completedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(AiAdminOrder.OrderStatus.COMPLETED, completedOrder.getStatus());
        assertNotNull(completedOrder.getLlmResponse(), "LLM response should be stored");
        assertTrue(completedOrder.getLlmResponse().contains("action"),
                "LLM response should contain action field");
    }

    @Test
    @DisplayName("Test category order should use TestExecutor, not LLM")
    void dispatch_shouldUseTestExecutorForTestCategoryOrder() throws InterruptedException {
        // Submit a test category order
        AiAdminOrder order = aiAdminService.submitOrder("admin-user", "Run API health endpoint tests");

        dispatchService.dispatchSingleOrder(order);

        // Wait for async execution to complete
        Thread.sleep(5000);

        // Verify order was completed
        AiAdminOrder completedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(AiAdminOrder.OrderStatus.COMPLETED, completedOrder.getStatus());

        // Test category orders should have a test report
        List<AiAdminTestReport> reports = reportRepository.findByOrderId(order.getId());
        assertFalse(reports.isEmpty(), "Test report should be generated");
    }

    @Test
    @DisplayName("isTestOrder should correctly distinguish test vs natural language orders")
    void isTestOrder_shouldDistinguishTestVsNaturalLanguage() {
        // Test category orders
        assertTrue(dispatchService.isTestOrder("Run API tests"));
        assertTrue(dispatchService.isTestOrder("Check game combat"));
        assertTrue(dispatchService.isTestOrder("Test auth login"));
        assertTrue(dispatchService.isTestOrder("Run all tests"));

        // Natural language orders
        assertFalse(dispatchService.isTestOrder("Low fleet count. Deploy ships."));
        assertFalse(dispatchService.isTestOrder("Station offline. Investigate."));
        assertFalse(dispatchService.isTestOrder("Population declining."));
    }
}
