package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.dto.AiAdminOrderRequest;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto.AiAdminTestCaseDto;
import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminNotificationType;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder.OrderStatus;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.service.AiAdminDispatchService;
import com.yisy.modules.doramcu.admin.service.AiAdminLlmService;
import com.yisy.modules.doramcu.admin.service.AiAdminNotificationService;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.admin.service.TestRegistry;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiAdminController Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AiAdminControllerTest {

    @Mock
    private AiAdminService aiAdminService;

    @Mock
    private AiAdminDispatchService aiAdminDispatchService;

    @Mock
    private AiAdminNotificationService notificationService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiAdminTestCaseRepository testCaseRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private TestRegistry testRegistry;

    @Mock
    private AiAdminLlmService llmService;

    @Mock
    private Authentication authentication;

    private AiAdminController controller;

    private Player adminPlayer;
    private Player nonAdminPlayer;

    @BeforeEach
    void setUp() {
        controller = new AiAdminController(
                aiAdminService,
                aiAdminDispatchService,
                notificationService,
                playerRepository,
                userRepository,
                testCaseRepository,
                testRegistry,
                aiCharacterRepository,
                llmService
        );

        adminPlayer = new Player();
        adminPlayer.setId(1L);
        adminPlayer.setEmail("admin@doramcu.local");
        adminPlayer.setAdmin(true);

        nonAdminPlayer = new Player();
        nonAdminPlayer.setId(2L);
        nonAdminPlayer.setEmail("user@doramcu.local");
        nonAdminPlayer.setAdmin(false);
    }

    // ---- submitOrder ----

    @Nested
    @DisplayName("submitOrder Tests")
    class SubmitOrderTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void submitOrder_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            AiAdminOrderRequest request = new AiAdminOrderRequest();
            request.setOrderText("Run API tests");

            ResponseEntity<?> response = controller.submitOrder(request, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when order text is blank")
        void submitOrder_shouldReturn400WhenBlank() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminOrderRequest request = new AiAdminOrderRequest();
            request.setOrderText("   ");

            ResponseEntity<?> response = controller.submitOrder(request, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 201 when order is submitted successfully")
        void submitOrder_shouldReturn201() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminOrder order = new AiAdminOrder("admin@doramcu.local", "Run API tests");
            order.setId(1L);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            when(aiAdminService.submitOrder("admin@doramcu.local", "Run API tests"))
                    .thenReturn(order);

            AiAdminOrderRequest request = new AiAdminOrderRequest();
            request.setOrderText("Run API tests");

            ResponseEntity<?> response = controller.submitOrder(request, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("id"));
            assertEquals("PENDING", body.get("status"));
            assertEquals("admin@doramcu.local", body.get("submittedBy"));
        }
    }

    // ---- listOrders ----

    @Nested
    @DisplayName("listOrders Tests")
    class ListOrdersTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void listOrders_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.listOrders(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should return all orders when user is admin")
        void listOrders_shouldReturnAllOrders() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminOrder order = new AiAdminOrder("admin", "Test");
            order.setId(1L);
            order.setStatus(OrderStatus.PENDING);
            order.setAssignedAiAdminId(100L);
            when(aiAdminService.getAllOrders()).thenReturn(List.of(order));

            ResponseEntity<?> response = controller.listOrders(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
            assertEquals(1, body.size());
            assertEquals(1L, body.get(0).get("id"));
            assertEquals(100L, body.get(0).get("assignedAiAdminId"));
        }
    }

    // ---- getOrder ----

    @Nested
    @DisplayName("getOrder Tests")
    class GetOrderTests {

        @Test
        @DisplayName("should return 404 when order not found")
        void getOrder_shouldReturn404() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(aiAdminService.getOrder(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getOrder(999L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("should return order when found")
        void getOrder_shouldReturnOrder() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminOrder order = new AiAdminOrder("admin", "Test");
            order.setId(1L);
            order.setStatus(OrderStatus.COMPLETED);
            order.setAssignedAiAdminId(100L);
            when(aiAdminService.getOrder(1L)).thenReturn(Optional.of(order));

            ResponseEntity<?> response = controller.getOrder(1L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("id"));
            assertEquals("COMPLETED", body.get("status"));
        }
    }

    // ---- listTests ----

    @Nested
    @DisplayName("listTests Tests")
    class ListTestsTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void listTests_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.listTests(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should return test catalog when user is admin")
        void listTests_shouldReturnCatalog() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            TestDefinition test = new TestDefinition();
            test.setId("api-health");
            test.setName("API Health Check");
            test.setCategory("API");
            test.setDescription("Checks API health endpoint");
            test.setEstimatedDurationMs(1000);
            test.setEnabled(true);
            when(testRegistry.getTestCatalog()).thenReturn(List.of(test));

            ResponseEntity<?> response = controller.listTests(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
            assertEquals(1, body.size());
            assertEquals("api-health", body.get(0).get("id"));
        }
    }

    // ---- listTestCategories ----

    @Nested
    @DisplayName("listTestCategories Tests")
    class ListTestCategoriesTests {

        @Test
        @DisplayName("should return categories when user is admin")
        void listTestCategories_shouldReturnCategories() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(testRegistry.getCategories()).thenReturn(List.of("API", "GAME", "AI_CHAT", "AUTH"));

            ResponseEntity<?> response = controller.listTestCategories(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<String> body = (List<String>) response.getBody();
            assertEquals(4, body.size());
        }
    }

    // ---- getReportForOrder ----

    @Nested
    @DisplayName("getReportForOrder Tests")
    class GetReportForOrderTests {

        @Test
        @DisplayName("should return 404 when no report for order")
        void getReportForOrder_shouldReturn404() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(aiAdminService.getReportForOrder(1L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getReportForOrder(1L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("should return report when found")
        void getReportForOrder_shouldReturnReport() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminTestReport report = new AiAdminTestReport(1L);
            report.setId(10L);
            report.setPassed(5);
            report.setFailed(0);
            report.setTotalTests(5);
            report.setSummary("All passed");
            when(aiAdminService.getReportForOrder(1L)).thenReturn(Optional.of(report));
            when(testCaseRepository.findByReportId(10L)).thenReturn(List.of());

            ResponseEntity<?> response = controller.getReportForOrder(1L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            AiAdminReportDto body = (AiAdminReportDto) response.getBody();
            assertEquals(10L, body.getId());
            assertEquals(5, body.getPassed());
        }
    }

    // ---- listReports ----

    @Nested
    @DisplayName("listReports Tests")
    class ListReportsTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void listReports_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.listReports(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should return all reports when user is admin")
        void listReports_shouldReturnAllReports() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(aiAdminService.getAllReports()).thenReturn(List.of());

            ResponseEntity<?> response = controller.listReports(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    // ---- Dispatch endpoints ----

    @Nested
    @DisplayName("getDispatchSummary Tests")
    class GetDispatchSummaryTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void getDispatchSummary_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.getDispatchSummary(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should return dispatch summary when user is admin")
        void getDispatchSummary_shouldReturnSummary() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminDispatchService.DispatchSummary summary = new AiAdminDispatchService.DispatchSummary();
            summary.setTotalAiAdmins(3);
            summary.setAvailableAiAdmins(2);
            summary.setBusyAiAdmins(1);
            summary.setTotalActiveOrders(2);
            summary.setUnassignedPendingOrders(1);
            summary.setAssignedPendingOrders(1);
            summary.setRunningOrders(2);
            summary.setExecutingOrders(2);
            when(aiAdminDispatchService.getDispatchSummary()).thenReturn(summary);

            ResponseEntity<?> response = controller.getDispatchSummary(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(3, body.get("totalAiAdmins"));
            assertEquals(2, body.get("availableAiAdmins"));
            assertEquals(1, body.get("busyAiAdmins"));
            assertEquals(2, body.get("totalActiveOrders"));
            assertEquals(1, body.get("unassignedPendingOrders"));
            assertEquals(1, body.get("assignedPendingOrders"));
            assertEquals(2, body.get("runningOrders"));
            assertEquals(2, body.get("executingOrders"));
        }
    }

    @Nested
    @DisplayName("getAiAdminWorkload Tests")
    class GetAiAdminWorkloadTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void getAiAdminWorkload_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.getAiAdminWorkload(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should return empty list when no AI admins")
        void getAiAdminWorkload_shouldReturnEmptyList() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(aiAdminDispatchService.getAiAdminWorkload()).thenReturn(List.of());

            ResponseEntity<?> response = controller.getAiAdminWorkload(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
            assertTrue(body.isEmpty());
        }

        @Test
        @DisplayName("should return all AI admins with workload info")
        void getAiAdminWorkload_shouldReturnAllAdmins() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiCharacter admin1 = new AiCharacter("Alpha Overseer", "Overseer", "Alpha");
            admin1.setId(100L);
            admin1.setActive(true);
            admin1.setCurrentOrderId(1L);
            admin1.setActiveOrderCount(2);
            admin1.setTotalExperience(50);
            admin1.setLeadershipRating(8);

            AiCharacter admin2 = new AiCharacter("Beta Admin", "Advisor", "Beta");
            admin2.setId(101L);
            admin2.setActive(true);
            admin2.setCurrentOrderId(null);
            admin2.setActiveOrderCount(0);
            admin2.setTotalExperience(10);
            admin2.setLeadershipRating(5);

            when(aiAdminDispatchService.getAiAdminWorkload())
                    .thenReturn(List.of(admin1, admin2));

            ResponseEntity<?> response = controller.getAiAdminWorkload(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
            assertEquals(2, body.size());

            Map<String, Object> first = body.get(0);
            assertEquals(100L, first.get("id"));
            assertEquals("Alpha Overseer", first.get("name"));
            assertEquals("Overseer", first.get("role"));
            assertEquals("Alpha", first.get("empireName"));
            assertEquals(true, first.get("active"));
            assertEquals(1L, first.get("currentOrderId"));
            assertEquals(2, first.get("activeOrderCount"));
            assertEquals(50, first.get("totalExperience"));
            assertEquals(8, first.get("leadershipRating"));
        }
    }

    @Nested
    @DisplayName("triggerDispatch Tests")
    class TriggerDispatchTests {

        @Test
        @DisplayName("should return 403 when user is not admin")
        void triggerDispatch_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.triggerDispatch(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("should trigger dispatch and return success message")
        void triggerDispatch_shouldReturnSuccess() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            doNothing().when(aiAdminDispatchService).dispatchPendingOrders();

            ResponseEntity<?> response = controller.triggerDispatch(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Dispatch triggered successfully", body.get("message"));
            verify(aiAdminDispatchService).dispatchPendingOrders();
        }
    }

    // ---- Admin auth via User fallback ----

    @Nested
    @DisplayName("Admin Authorization Fallback Tests")
    class AdminAuthFallbackTests {

        @Test
        @DisplayName("should find admin via User -> Player lookup fallback")
        void shouldFindAdminViaUserFallback() {
            when(authentication.getName()).thenReturn("admin_user");
            when(playerRepository.findByEmail("admin_user"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByEmail("admin_user@doramcu.local"))
                    .thenReturn(Optional.empty());

            User user = new User();
            user.setUsername("admin_user");
            when(userRepository.findByUsername("admin_user"))
                    .thenReturn(Optional.of(user));
            when(playerRepository.findByUser(user))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminDispatchService.DispatchSummary summary = new AiAdminDispatchService.DispatchSummary();
            summary.setTotalAiAdmins(0);
            when(aiAdminDispatchService.getDispatchSummary()).thenReturn(summary);

            ResponseEntity<?> response = controller.getDispatchSummary(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 403 when player not found via any lookup path")
        void shouldReturn403WhenPlayerNotFound() {
            when(authentication.getName()).thenReturn("unknown_user");
            when(playerRepository.findByEmail("unknown_user"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByEmail("unknown_user@doramcu.local"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsername("unknown_user"))
                    .thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getDispatchSummary(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }

    // ---- Notification endpoints ----

    @Nested
    @DisplayName("Notification Tests")
    class NotificationTests {

        @Test
        @DisplayName("getNotifications should return 403 when user is not admin")
        void getNotifications_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.getNotifications(100L, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("getNotifications should return notifications for admin")
        void getNotifications_shouldReturnNotifications() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminNotification notification = new AiAdminNotification(
                    100L, AiAdminNotificationType.ORDER_ASSIGNED,
                    "New work order assigned", "Order #1 has been assigned to you. Category: API");
            notification.setId(1L);
            notification.setOrderId(1L);

            when(notificationService.getNotificationsForAdmin(100L))
                    .thenReturn(List.of(notification));

            ResponseEntity<?> response = controller.getNotifications(100L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
            assertEquals(1, body.size());
            assertEquals(1L, body.get(0).get("id"));
            assertEquals("ORDER_ASSIGNED", body.get(0).get("type"));
            assertEquals("Order Assigned", body.get(0).get("label"));
            assertEquals("info", body.get(0).get("severity"));
        }

        @Test
        @DisplayName("getUnreadNotifications should return 403 when user is not admin")
        void getUnreadNotifications_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.getUnreadNotifications(100L, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("getUnreadNotifications should return unread notifications")
        void getUnreadNotifications_shouldReturnUnread() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiAdminNotification notification = new AiAdminNotification(
                    100L, AiAdminNotificationType.ORDER_COMPLETED,
                    "Work order completed", "Order #1 completed: 5 passed, 0 failed. Earned 15 XP.");
            notification.setId(2L);
            notification.setOrderId(1L);

            when(notificationService.getUnreadNotificationsForAdmin(100L))
                    .thenReturn(List.of(notification));

            ResponseEntity<?> response = controller.getUnreadNotifications(100L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
            assertEquals(1, body.size());
            assertEquals("ORDER_COMPLETED", body.get(0).get("type"));
        }

        @Test
        @DisplayName("getUnreadCount should return unread count")
        void getUnreadCount_shouldReturnCount() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(notificationService.getUnreadCountForAdmin(100L)).thenReturn(5L);

            ResponseEntity<?> response = controller.getUnreadCount(100L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(100L, body.get("adminId"));
            assertEquals(5L, body.get("unreadCount"));
        }

        @Test
        @DisplayName("markNotificationRead should return 404 when not found")
        void markNotificationRead_shouldReturn404WhenNotFound() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(notificationService.markAsRead(999L, 100L)).thenReturn(false);

            ResponseEntity<?> response = controller.markNotificationRead(999L, 100L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("markNotificationRead should mark notification as read")
        void markNotificationRead_shouldMarkAsRead() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(notificationService.markAsRead(1L, 100L)).thenReturn(true);

            ResponseEntity<?> response = controller.markNotificationRead(1L, 100L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Notification marked as read", body.get("message"));
        }

        @Test
        @DisplayName("markAllNotificationsRead should mark all as read")
        void markAllNotificationsRead_shouldMarkAllAsRead() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));
            when(notificationService.markAllAsRead(100L)).thenReturn(3);

            ResponseEntity<?> response = controller.markAllNotificationsRead(100L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("3 notifications marked as read", body.get("message"));
        }

        @Test
        @DisplayName("getNotificationsSummary should return 403 when user is not admin")
        void getNotificationsSummary_shouldReturn403WhenNotAdmin() {
            when(authentication.getName()).thenReturn("user@doramcu.local");
            when(playerRepository.findByEmail("user@doramcu.local"))
                    .thenReturn(Optional.of(nonAdminPlayer));

            ResponseEntity<?> response = controller.getNotificationsSummary(authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("getNotificationsSummary should return summary for all admins")
        void getNotificationsSummary_shouldReturnSummary() {
            when(authentication.getName()).thenReturn("admin@doramcu.local");
            when(playerRepository.findByEmail("admin@doramcu.local"))
                    .thenReturn(Optional.of(adminPlayer));

            AiCharacter admin = new AiCharacter("Alpha Overseer", "Overseer", "Alpha");
            admin.setId(100L);
            admin.setActive(true);

            when(aiAdminDispatchService.getAiAdminWorkload()).thenReturn(List.of(admin));
            when(notificationService.getUnreadCountForAdmin(100L)).thenReturn(2L);
            when(notificationService.getNotificationsForAdmin(100L)).thenReturn(List.of(
                    new AiAdminNotification(), new AiAdminNotification()
            ));

            ResponseEntity<?> response = controller.getNotificationsSummary(authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1, body.get("totalAdmins"));
            assertEquals(2L, body.get("totalUnread"));
            assertEquals(2L, body.get("totalNotifications"));
        }
    }

    // ------------------------------------------------------------------
    // LLM Endpoint Tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("LLM Response Tests")
    class LlmResponseTests {

        @Test
        @DisplayName("getLlmResponse should return 404 when order not found")
        void getLlmResponse_shouldReturn404WhenNotFound() {
            when(aiAdminService.getOrder(999L)).thenReturn(Optional.empty());

            try {
                controller.getLlmResponse(999L);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("not found"));
            }
        }

        @Test
        @DisplayName("getLlmResponse should return null when no LLM response")
        void getLlmResponse_shouldReturnNull() {
            AiAdminOrder order = new AiAdminOrder("test", "order");
            order.setId(1L);
            when(aiAdminService.getOrder(1L)).thenReturn(Optional.of(order));

            ResponseEntity<?> response = controller.getLlmResponse(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("orderId"));
            assertNull(body.get("llmResponse"));
        }

        @Test
        @DisplayName("getLlmResponse should return stored LLM response")
        void getLlmResponse_shouldReturnStoredResponse() {
            AiAdminOrder order = new AiAdminOrder("test", "order");
            order.setId(1L);
            order.setLlmResponse("{\"action\":\"DEPLOY\"}");
            when(aiAdminService.getOrder(1L)).thenReturn(Optional.of(order));

            ResponseEntity<?> response = controller.getLlmResponse(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("{\"action\":\"DEPLOY\"}", body.get("llmResponse"));
        }
    }

    @Nested
    @DisplayName("LLM Retry Tests")
    class LlmRetryTests {

        @Test
        @DisplayName("llmRetryOrder should return 404 when order not found")
        void llmRetryOrder_shouldReturn404WhenNotFound() {
            when(aiAdminService.getOrder(999L)).thenReturn(Optional.empty());

            try {
                controller.llmRetryOrder(999L);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("not found"));
            }
        }

        @Test
        @DisplayName("llmRetryOrder should return 400 when no assigned admin")
        void llmRetryOrder_shouldReturn400WhenNoAdmin() {
            AiAdminOrder order = new AiAdminOrder("test", "order");
            order.setId(1L);
            order.setAssignedAiAdminId(null);
            when(aiAdminService.getOrder(1L)).thenReturn(Optional.of(order));

            try {
                controller.llmRetryOrder(1L);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("no assigned"));
            }
        }

        @Test
        @DisplayName("llmRetryOrder should return 404 when admin not found")
        void llmRetryOrder_shouldReturn404WhenAdminNotFound() {
            AiAdminOrder order = new AiAdminOrder("test", "order");
            order.setId(1L);
            order.setAssignedAiAdminId(100L);
            when(aiAdminService.getOrder(1L)).thenReturn(Optional.of(order));
            when(aiCharacterRepository.findById(100L)).thenReturn(Optional.empty());

            try {
                controller.llmRetryOrder(1L);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("not found"));
            }
        }

        @Test
        @DisplayName("llmRetryOrder should re-run LLM and store response")
        void llmRetryOrder_shouldRerunLlm() {
            AiAdminOrder order = new AiAdminOrder("test", "Low fleet count");
            order.setId(1L);
            order.setAssignedAiAdminId(100L);
            when(aiAdminService.getOrder(1L)).thenReturn(Optional.of(order));

            AiCharacter admin = new AiCharacter("Alpha Overseer", "Overseer", "Alpha");
            admin.setId(100L);
            when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(admin));

            AiAdminLlmService.LlmAnalysisResult llmResult = new AiAdminLlmService.LlmAnalysisResult(
                    "DEPLOY", "Sending ships", Map.of("ships", "5"),
                    "{\"action\":\"DEPLOY\"}", true, null);
            when(llmService.analyzeOrder(eq(admin), eq(order))).thenReturn(llmResult);

            ResponseEntity<?> response = controller.llmRetryOrder(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("orderId"));
            assertEquals(true, body.get("success"));
            assertEquals("DEPLOY", body.get("action"));
            assertEquals("Sending ships", body.get("reasoning"));
            verify(aiAdminService).saveOrder(order);
        }
    }

    @Nested
    @DisplayName("LLM Test Prompt Tests")
    class LlmTestPromptTests {

        @Test
        @DisplayName("testLlmPrompt should return 400 when adminId missing")
        void testLlmPrompt_shouldReturn400WhenNoAdminId() {
            Map<String, String> body = Map.of("orderText", "test");

            try {
                controller.testLlmPrompt(body);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("adminId"));
            }
        }

        @Test
        @DisplayName("testLlmPrompt should return 400 when orderText missing")
        void testLlmPrompt_shouldReturn400WhenNoOrderText() {
            Map<String, String> body = Map.of("adminId", "100");

            try {
                controller.testLlmPrompt(body);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("orderText"));
            }
        }

        @Test
        @DisplayName("testLlmPrompt should return 404 when admin not found")
        void testLlmPrompt_shouldReturn404WhenAdminNotFound() {
            Map<String, String> body = Map.of("adminId", "100", "orderText", "test");
            when(aiCharacterRepository.findById(100L)).thenReturn(Optional.empty());

            try {
                controller.testLlmPrompt(body);
                fail("Expected ResponseStatusException");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("not found"));
            }
        }

        @Test
        @DisplayName("testLlmPrompt should return LLM result with system prompt")
        void testLlmPrompt_shouldReturnResult() {
            Map<String, String> body = Map.of("adminId", "100", "orderText", "Low fleet");

            AiCharacter admin = new AiCharacter("Alpha Overseer", "Overseer", "Alpha");
            admin.setId(100L);
            when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(admin));

            AiAdminLlmService.LlmAnalysisResult llmResult = new AiAdminLlmService.LlmAnalysisResult(
                    "DEPLOY", "Sending ships", Map.of(), "{\"action\":\"DEPLOY\"}", true, null);
            when(llmService.analyzeOrder(any(), any())).thenReturn(llmResult);

            ResponseEntity<?> response = controller.testLlmPrompt(body);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> respBody = (Map<String, Object>) response.getBody();
            assertEquals(100L, respBody.get("adminId"));
            assertEquals("Alpha Overseer", respBody.get("adminName"));
            assertNotNull(respBody.get("systemPrompt"));
            assertEquals(true, respBody.get("success"));
            assertEquals("DEPLOY", respBody.get("action"));
        }
    }
}
