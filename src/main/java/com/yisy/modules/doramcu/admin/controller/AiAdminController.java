package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.dto.AiAdminOrderRequest;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto.AiAdminTestCaseDto;
import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminQuest;
import com.yisy.modules.doramcu.admin.model.AiAdminQuestStep;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminCollaborationNote;
import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import com.yisy.modules.doramcu.admin.service.AiAdminAnalyticsService;
import com.yisy.modules.doramcu.admin.service.AiAdminCollaborationService;
import com.yisy.modules.doramcu.admin.service.AiAdminDashboardService;
import com.yisy.modules.doramcu.admin.service.AiAdminIncidentService;
import com.yisy.modules.doramcu.admin.service.AiAdminDispatchService;
import com.yisy.modules.doramcu.admin.service.AiAdminInteractionService;
import com.yisy.modules.doramcu.admin.service.AiAdminLlmService;
import com.yisy.modules.doramcu.admin.service.AiAdminNotificationService;
import com.yisy.modules.doramcu.admin.service.AiAdminOrderProgressService;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.admin.service.TestRegistry;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for AI admin test orders and reports.
 * All endpoints require admin rights.
 */
@RestController
@RequestMapping("/api/v1/admin/ai-orders")
public class AiAdminController {

    private static final Logger log = LoggerFactory.getLogger(AiAdminController.class);

    private final AiAdminService aiAdminService;
    private final AiAdminDispatchService aiAdminDispatchService;
    private final AiAdminNotificationService notificationService;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final AiAdminTestCaseRepository testCaseRepository;
    private final TestRegistry testRegistry;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiAdminLlmService llmService;
    private final AiAdminVisibilityService visibilityService;
    private final AiAdminOrderProgressService progressService;
    private final AiAdminInteractionService interactionService;
    private final AiAdminAnalyticsService analyticsService;
    private final AiAdminDashboardService dashboardService;
    private final AiAdminIncidentService incidentService;
    private final AiAdminCollaborationService collaborationService;

    public AiAdminController(AiAdminService aiAdminService,
                             AiAdminDispatchService aiAdminDispatchService,
                             AiAdminNotificationService notificationService,
                             PlayerRepository playerRepository,
                             UserRepository userRepository,
                             AiAdminTestCaseRepository testCaseRepository,
                             TestRegistry testRegistry,
                             AiCharacterRepository aiCharacterRepository,
                             AiAdminLlmService llmService,
                             AiAdminVisibilityService visibilityService,
                             AiAdminOrderProgressService progressService,
                             AiAdminInteractionService interactionService,
                             AiAdminAnalyticsService analyticsService,
                             AiAdminDashboardService dashboardService,
                             AiAdminIncidentService incidentService,
                             AiAdminCollaborationService collaborationService) {
      this.aiAdminService = aiAdminService;
      this.aiAdminDispatchService = aiAdminDispatchService;
      this.notificationService = notificationService;
      this.playerRepository = playerRepository;
      this.userRepository = userRepository;
      this.testCaseRepository = testCaseRepository;
      this.testRegistry = testRegistry;
      this.aiCharacterRepository = aiCharacterRepository;
      this.llmService = llmService;
      this.visibilityService = visibilityService;
      this.progressService = progressService;
      this.interactionService = interactionService;
      this.analyticsService = analyticsService;
      this.dashboardService = dashboardService;
      this.incidentService = incidentService;
      this.collaborationService = collaborationService;
  }

    /**
     * Submit a new test order.
     * Optionally includes target location fields for order-aware positioning.
     */
    @PostMapping("/")
    public ResponseEntity<?> submitOrder(@Valid @RequestBody AiAdminOrderRequest request,
                                         Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        String orderText = request.getOrderText();
        if (orderText == null || orderText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order text is required"));
        }

        String username = authentication.getName();
        AiAdminOrder order = aiAdminService.submitOrder(username, orderText,
                request.getTargetGalaxy(), request.getTargetPosX(),
                request.getTargetPosY(), request.getTargetPosZ(),
                request.getBuildContractId());

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("status", order.getStatus().name());
        result.put("submittedBy", order.getSubmittedBy());
        result.put("createdAt", order.getCreatedAt());
        if (order.hasTargetLocation()) {
            result.put("targetGalaxy", order.getTargetGalaxy());
            result.put("targetPosX", order.getTargetPosX());
            result.put("targetPosY", order.getTargetPosY());
            result.put("targetPosZ", order.getTargetPosZ());
        }
        if (order.isProductionOrder()) {
            result.put("buildContractId", order.getBuildContractId());
            result.put("orderType", "production");
        }

        log.info("Order {} submitted by {} (type: {})", order.getId(), username,
                order.isProductionOrder() ? "production" : "test");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * List all orders.
     */
    @GetMapping("/")
    public ResponseEntity<?> listOrders(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiAdminOrder> orders = aiAdminService.getAllOrders();
        List<Map<String, Object>> result = orders.stream().map(this::orderToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * List all available tests (test catalog).
     */
    @GetMapping("/tests")
    public ResponseEntity<?> listTests(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<TestDefinition> tests = testRegistry.getTestCatalog();
        List<Map<String, Object>> result = tests.stream().map(this::testToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * List available test categories.
     */
    @GetMapping("/tests/categories")
    public ResponseEntity<?> listTestCategories(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<String> categories = testRegistry.getCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific order.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        return aiAdminService.getOrder(orderId)
                .map(order -> ResponseEntity.ok(orderToMap(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get the report for a specific order.
     */
    @GetMapping("/{orderId}/report")
    public ResponseEntity<?> getReportForOrder(@PathVariable Long orderId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        Optional<AiAdminTestReport> reportOpt = aiAdminService.getReportForOrder(orderId);
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AiAdminReportDto dto = reportToDto(reportOpt.get());
        return ResponseEntity.ok(dto);
    }

    /**
     * List all reports.
     */
    @GetMapping("/reports")
    public ResponseEntity<?> listReports(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiAdminTestReport> reports = aiAdminService.getAllReports();
        List<AiAdminReportDto> dtos = reports.stream().map(this::reportToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Download a report as JSON.
     */
    @GetMapping("/reports/{reportId}/download")
    public ResponseEntity<?> downloadReport(@PathVariable Long reportId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        Optional<AiAdminTestReport> reportOpt = aiAdminService.getReport(reportId);
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AiAdminTestReport report = reportOpt.get();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=\"test-report-" + reportId + ".json\"")
                .body(report.getReportJson());
    }

    // ---- Helpers ----

    private boolean isAdmin(Authentication authentication) {
        String username = authentication.getName();
        Optional<Player> playerOpt = playerRepository.findByEmail(username);
        if (playerOpt.isEmpty()) {
            playerOpt = playerRepository.findByEmail(username + "@doramcu.local");
        }
        if (playerOpt.isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                playerOpt = playerRepository.findByUser(userOpt.get());
            }
        }
        return playerOpt.map(Player::isAdmin).orElse(false);
    }

    private Map<String, Object> orderToMap(AiAdminOrder order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("submittedBy", order.getSubmittedBy());
        map.put("orderText", order.getOrderText());
        map.put("status", order.getStatus().name());
        map.put("assignedAiAdminId", order.getAssignedAiAdminId());
        map.put("createdAt", order.getCreatedAt());
        map.put("completedAt", order.getCompletedAt());
        map.put("progressPercent", order.getProgressPercent());
        map.put("currentTestName", order.getCurrentTestName());
        if (order.hasTargetLocation()) {
            map.put("targetGalaxy", order.getTargetGalaxy());
            map.put("targetPosX", order.getTargetPosX());
            map.put("targetPosY", order.getTargetPosY());
            map.put("targetPosZ", order.getTargetPosZ());
        }
        if (order.isProductionOrder()) {
            map.put("buildContractId", order.getBuildContractId());
            map.put("orderType", "production");
        } else {
            map.put("orderType", "test");
        }
        return map;
    }

    private Map<String, Object> testToMap(TestDefinition test) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", test.getId());
        map.put("name", test.getName());
        map.put("category", test.getCategory());
        map.put("description", test.getDescription());
        map.put("estimatedDurationMs", test.getEstimatedDurationMs());
        map.put("enabled", test.isEnabled());
        return map;
    }

    private AiAdminReportDto reportToDto(AiAdminTestReport report) {
        AiAdminReportDto dto = new AiAdminReportDto();
        dto.setId(report.getId());
        dto.setOrderId(report.getOrderId());
        dto.setSummary(report.getSummary());
        dto.setTotalTests(report.getTotalTests());
        dto.setPassed(report.getPassed());
        dto.setFailed(report.getFailed());
        dto.setSkipped(report.getSkipped());
        dto.setCreatedAt(report.getCreatedAt());

        List<AiAdminTestCase> cases = testCaseRepository.findByReportId(report.getId());
        List<AiAdminTestCaseDto> caseDtos = cases.stream().map(tc -> {
            AiAdminTestCaseDto cd = new AiAdminTestCaseDto();
            cd.setId(tc.getId());
            cd.setTestName(tc.getTestName());
            cd.setCategory(tc.getCategory());
            cd.setStatus(tc.getStatus().name());
            cd.setDurationMs(tc.getDurationMs());
            cd.setErrorMessage(tc.getErrorMessage());
            return cd;
        }).collect(Collectors.toList());
        dto.setTestCases(caseDtos);

        return dto;
    }

    // ---- Dispatch endpoints ----

    /**
     * Get the current dispatch summary showing AI admin workload distribution.
     */
    @GetMapping("/dispatch/summary")
    public ResponseEntity<?> getDispatchSummary(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        AiAdminDispatchService.DispatchSummary summary = aiAdminDispatchService.getDispatchSummary();
        Map<String, Object> result = new HashMap<>();
        result.put("totalAiAdmins", summary.getTotalAiAdmins());
        result.put("availableAiAdmins", summary.getAvailableAiAdmins());
        result.put("busyAiAdmins", summary.getBusyAiAdmins());
        result.put("totalActiveOrders", summary.getTotalActiveOrders());
        result.put("unassignedPendingOrders", summary.getUnassignedPendingOrders());
        result.put("assignedPendingOrders", summary.getAssignedPendingOrders());
        result.put("runningOrders", summary.getRunningOrders());
        result.put("executingOrders", summary.getExecutingOrders());
        return ResponseEntity.ok(result);
    }

    /**
     * List all AI admins with their current workload.
     */
    @GetMapping("/dispatch/admins")
    public ResponseEntity<?> getAiAdminWorkload(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiCharacter> admins = aiAdminDispatchService.getAiAdminWorkload();
        List<Map<String, Object>> result = admins.stream().map(admin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", admin.getId());
            map.put("name", admin.getName());
            map.put("role", admin.getRole());
            map.put("empireName", admin.getEmpireName());
            map.put("active", admin.isActive());
            map.put("currentOrderId", admin.getCurrentOrderId());
            map.put("activeOrderCount", admin.getActiveOrderCount());
            map.put("totalExperience", admin.getTotalExperience());
            map.put("leadershipRating", admin.getLeadershipRating());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Manually trigger dispatch of all pending unassigned orders.
     */
    @PostMapping("/dispatch/trigger")
    public ResponseEntity<?> triggerDispatch(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        aiAdminDispatchService.dispatchPendingOrders();
        return ResponseEntity.ok(Map.of("message", "Dispatch triggered successfully"));
    }

    // ---- 3D Position endpoints ----

    /**
     * Set the 3D position of an AI admin character.
     * Request body: {"x": double, "y": double, "z": double, "galaxyName": string}
     */
    @PatchMapping("/admins/{id}/position")
    public ResponseEntity<?> setAiAdminPosition(@PathVariable Long id,
                                                @RequestBody Map<String, Object> body,
                                                Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        Optional<AiCharacter> adminOpt = aiCharacterRepository.findById(id);
        if (adminOpt.isEmpty() || !adminOpt.get().isAiAdmin()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "AI admin not found"));
        }

        AiCharacter admin = adminOpt.get();
        Double x = body.containsKey("x") ? ((Number) body.get("x")).doubleValue() : null;
        Double y = body.containsKey("y") ? ((Number) body.get("y")).doubleValue() : null;
        Double z = body.containsKey("z") ? ((Number) body.get("z")).doubleValue() : null;
        
        if (x == null || y == null || z == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "x, y, and z coordinates are required"));
        }

        admin.setPosX(x);
        admin.setPosY(y);
        admin.setPosZ(z);
        
        if (body.containsKey("galaxyName") && body.get("galaxyName") != null) {
            admin.setGalaxyName((String) body.get("galaxyName"));
        } else if (admin.getGalaxyName() == null) {
            admin.setGalaxyName(admin.getEmpireName());
        }

        aiCharacterRepository.save(admin);
        log.info("AI admin {} position updated to ({}, {}, {})", admin.getName(), x, y, z);

        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("name", admin.getName());
        result.put("posX", admin.getPosX());
        result.put("posY", admin.getPosY());
        result.put("posZ", admin.getPosZ());
        result.put("galaxyName", admin.getGalaxyName());
        return ResponseEntity.ok(result);
    }

    /**
     * List all AI admin positions in a specific galaxy.
     */
    @GetMapping("/admins/{galaxyName}/positions")
    public ResponseEntity<?> getAiAdminPositions(@PathVariable String galaxyName,
                                                 Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiCharacter> admins = aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndGalaxyName(galaxyName);
        List<Map<String, Object>> result = admins.stream().map(admin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", admin.getId());
            map.put("name", admin.getName());
            map.put("role", admin.getRole());
            map.put("posX", admin.getPosX());
            map.put("posY", admin.getPosY());
            map.put("posZ", admin.getPosZ());
            map.put("galaxyName", admin.getGalaxyName());
            map.put("hasPosition", admin.hasPosition());
            map.put("activeOrderCount", admin.getActiveOrderCount());
            map.put("currentOrderId", admin.getCurrentOrderId());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Notification endpoints
    // ------------------------------------------------------------------

    /**
     * Get all notifications for a specific AI admin.
     */
    @GetMapping("/admins/{adminId}/notifications")
    public ResponseEntity<?> getNotifications(@PathVariable Long adminId,
                                              Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiAdminNotification> notifications = notificationService.getNotificationsForAdmin(adminId);
        List<Map<String, Object>> result = notifications.stream().map(this::notificationToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get unread notifications for a specific AI admin.
     */
    @GetMapping("/admins/{adminId}/notifications/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable Long adminId,
                                                    Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiAdminNotification> notifications = notificationService.getUnreadNotificationsForAdmin(adminId);
        List<Map<String, Object>> result = notifications.stream().map(this::notificationToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get unread notification count for a specific AI admin.
     */
    @GetMapping("/admins/{adminId}/notifications/unread/count")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long adminId,
                                            Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        long count = notificationService.getUnreadCountForAdmin(adminId);
        return ResponseEntity.ok(Map.of("adminId", adminId, "unreadCount", count));
    }

    /**
     * Mark a notification as read.
     */
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long notificationId,
                                                  @RequestParam Long adminId,
                                                  Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        boolean marked = notificationService.markAsRead(notificationId, adminId);
        if (!marked) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notification not found"));
        }
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    /**
     * Mark all notifications for an AI admin as read.
     */
    @PatchMapping("/admins/{adminId}/notifications/read-all")
    public ResponseEntity<?> markAllNotificationsRead(@PathVariable Long adminId,
                                                      Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        int count = notificationService.markAllAsRead(adminId);
        return ResponseEntity.ok(Map.of("message", count + " notifications marked as read"));
    }

    /**
     * Get notifications summary across all AI admins.
     */
    @GetMapping("/notifications/summary")
    public ResponseEntity<?> getNotificationsSummary(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin rights required"));
        }

        List<AiCharacter> admins = aiAdminDispatchService.getAiAdminWorkload();
        Map<String, Object> summary = new HashMap<>();

        // Use arrays to allow mutation from lambda
        final long[] totalUnread = {0};
        final long[] totalNotifications = {0};

        List<Map<String, Object>> adminSummary = admins.stream().map(admin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("adminId", admin.getId());
            map.put("adminName", admin.getName());
            long unread = notificationService.getUnreadCountForAdmin(admin.getId());
            List<AiAdminNotification> all = notificationService.getNotificationsForAdmin(admin.getId());
            map.put("unreadCount", unread);
            map.put("totalCount", all.size());
            totalUnread[0] += unread;
            totalNotifications[0] += all.size();
            return map;
        }).collect(Collectors.toList());

        summary.put("totalAdmins", admins.size());
        summary.put("totalUnread", totalUnread[0]);
        summary.put("totalNotifications", totalNotifications[0]);
        summary.put("admins", adminSummary);
        return ResponseEntity.ok(summary);
    }

    // ------------------------------------------------------------------
    // LLM Endpoints
    // ------------------------------------------------------------------

    /**
     * Get the LLM response stored for an order.
     */
    @GetMapping("/orders/{id}/llm-response")
    public ResponseEntity<Map<String, Object>> getLlmResponse(@PathVariable Long id) {
        AiAdminOrder order = aiAdminService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("llmResponse", order.getLlmResponse());
        return ResponseEntity.ok(result);
    }

    /**
     * Re-run an order through the LLM with the assigned AI admin.
     */
    @PostMapping("/orders/{id}/llm-retry")
    public ResponseEntity<Map<String, Object>> llmRetryOrder(@PathVariable Long id) {
        AiAdminOrder order = aiAdminService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getAssignedAiAdminId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has no assigned AI admin");
        }

        AiCharacter admin = aiCharacterRepository.findById(order.getAssignedAiAdminId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI admin not found"));

        AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, order);

        // Store the new LLM response
        order.setLlmResponse(result.getRawResponse());
        aiAdminService.saveOrder(order);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("success", result.isSuccess());
        response.put("action", result.getAction());
        response.put("reasoning", result.getReasoning());
        response.put("parameters", result.getParameters());
        response.put("rawResponse", result.getRawResponse());
        return ResponseEntity.ok(response);
    }

    /**
     * Test the LLM with a sample order and AI admin.
     */
    @PostMapping("/llm/test-prompt")
    public ResponseEntity<Map<String, Object>> testLlmPrompt(@RequestBody Map<String, String> body) {
        String adminIdStr = body.get("adminId");
        String orderText = body.get("orderText");

        if (adminIdStr == null || adminIdStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "adminId is required");
        }
        if (orderText == null || orderText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderText is required");
        }

        Long adminId = Long.parseLong(adminIdStr);
        AiCharacter admin = aiCharacterRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI admin not found"));

        // Create a temporary order for testing
        AiAdminOrder tempOrder = new AiAdminOrder("ManualTest", orderText);

        AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, tempOrder);

        Map<String, Object> response = new HashMap<>();
        response.put("adminId", admin.getId());
        response.put("adminName", admin.getName());
        response.put("systemPrompt", llmService.buildSystemPrompt(admin));
        response.put("userMessage", llmService.buildUserMessage(tempOrder));
        response.put("success", result.isSuccess());
        response.put("action", result.getAction());
        response.put("reasoning", result.getReasoning());
        response.put("parameters", result.getParameters());
        response.put("rawResponse", result.getRawResponse());
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    // Dashboard
    // ------------------------------------------------------------------

    /**
     * Combined dashboard summary endpoint for the admin activity dashboard.
     * Returns dispatch overview, system health, admins with positions, galaxy list,
     * notification counts, recent trends, category breakdown, top performers, and running orders.
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> getDashboardSummary() {
        AiAdminDashboardService.DashboardSummary summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Lightweight health check for the dashboard.
     */
    @GetMapping("/dashboard/health")
    public ResponseEntity<?> getDashboardHealth() {
        Map<String, Object> health = dashboardService.getHealthCheck();
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> toMap(AiAdminDispatchService.DispatchSummary summary) {
        Map<String, Object> map = new HashMap<>();
        map.put("totalAiAdmins", summary.getTotalAiAdmins());
        map.put("availableAiAdmins", summary.getAvailableAiAdmins());
        map.put("busyAiAdmins", summary.getBusyAiAdmins());
        map.put("totalActiveOrders", summary.getTotalActiveOrders());
        map.put("unassignedPendingOrders", summary.getUnassignedPendingOrders());
        map.put("assignedPendingOrders", summary.getAssignedPendingOrders());
        map.put("runningOrders", summary.getRunningOrders());
        map.put("executingOrders", summary.getExecutingOrders());
        return map;
    }

    // ------------------------------------------------------------------
    // Progress Endpoints
    // ------------------------------------------------------------------

    /**
     * Get the current progress for a specific order.
     */
    @GetMapping("/orders/{id}/progress")
    public ResponseEntity<?> getOrderProgress(@PathVariable Long id) {
        Optional<AiAdminOrderProgressService.OrderProgress> progress = progressService.getOrderProgress(id);
        if (progress.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return ResponseEntity.ok(progress.get());
    }

    /**
     * Get progress for all running orders.
     */
    @GetMapping("/orders/progress")
    public ResponseEntity<List<AiAdminOrderProgressService.OrderProgress>> getRunningOrdersProgress() {
        List<AiAdminOrderProgressService.OrderProgress> progressList = progressService.getRunningOrdersProgress();
        return ResponseEntity.ok(progressList);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Map<String, Object> notificationToMap(AiAdminNotification notification) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", notification.getId());
        map.put("aiAdminId", notification.getAiAdminId());
        map.put("type", notification.getNotificationType().name());
        map.put("label", notification.getNotificationType().getLabel());
        map.put("severity", notification.getNotificationType().getSeverity());
        map.put("title", notification.getTitle());
        map.put("message", notification.getMessage());
        map.put("orderId", notification.getOrderId());
        map.put("isRead", notification.isRead());
        map.put("createdAt", notification.getCreatedAt());
        map.put("readAt", notification.getReadAt());
        map.put("expiresAt", notification.getExpiresAt());
        return map;
    }

    // ------------------------------------------------------------------
    // Quest endpoints
    // ------------------------------------------------------------------

    /**
     * Assign a quest from an AI admin to a player.
     */
    @PostMapping("/admins/{aiAdminId}/quest")
    public ResponseEntity<Map<String, Object>> assignQuest(
            @PathVariable Long aiAdminId,
            @RequestBody AiAdminQuestRequest request,
            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        AiAdminQuest.QuestType questType = AiAdminQuest.QuestType.OTHER;
        try {
            questType = AiAdminQuest.QuestType.valueOf(request.getQuestType());
        } catch (IllegalArgumentException e) {
            // default to OTHER
        }

        AiAdminQuest quest = interactionService.assignQuest(
                aiAdminId, player.getId(),
                request.getTitle(), request.getDescription(),
                questType,
                request.getXpReward() != null ? request.getXpReward() : 10,
                request.getDispositionReward() != null ? request.getDispositionReward() : 1,
                request.getTargetDescription(),
                request.getTargetCount() != null ? request.getTargetCount() : 1);

        Map<String, Object> result = questToMap(quest);
        log.info("Quest '{}' assigned by AI admin {} to player {}", quest.getTitle(), aiAdminId, player.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * List all quests given by a specific AI admin.
     */
    @GetMapping("/admins/{aiAdminId}/quests")
    public ResponseEntity<List<Map<String, Object>>> getQuestsByAdmin(@PathVariable Long aiAdminId) {
        List<AiAdminQuest> quests = interactionService.getQuestsByGiver(aiAdminId);
        return ResponseEntity.ok(quests.stream().map(this::questToMap).collect(Collectors.toList()));
    }

    /**
     * Get all active quests for the current player.
     */
    @GetMapping("/quests/player/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveQuests(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        List<AiAdminQuest> quests = interactionService.getActiveQuestsForPlayer(player.getId());
        return ResponseEntity.ok(quests.stream().map(this::questToMap).collect(Collectors.toList()));
    }

    /**
     * Update quest progress.
     */
    @PostMapping("/quests/{questId}/progress")
    public ResponseEntity<Map<String, Object>> updateQuestProgress(
            @PathVariable Long questId,
            @RequestBody QuestProgressRequest request) {
        AiAdminQuest quest = interactionService.updateQuestProgress(questId, request.getAmount());
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Complete a quest.
     */
    @PostMapping("/quests/{questId}/complete")
    public ResponseEntity<Map<String, Object>> completeQuest(@PathVariable Long questId) {
        AiAdminQuest quest = interactionService.completeQuest(questId);
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Fail a quest.
     */
    @PostMapping("/quests/{questId}/fail")
    public ResponseEntity<Map<String, Object>> failQuest(
            @PathVariable Long questId,
            @RequestBody QuestFailRequest request) {
        AiAdminQuest quest = interactionService.failQuest(questId, request.getReason());
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Abandon a quest.
     */
    @PostMapping("/quests/{questId}/abandon")
    public ResponseEntity<Map<String, Object>> abandonQuest(@PathVariable Long questId) {
        AiAdminQuest quest = interactionService.abandonQuest(questId);
        return ResponseEntity.ok(questToMap(quest));
    }

    /**
     * Get disposition toward an AI admin.
     */
    @GetMapping("/admins/{aiAdminId}/disposition")
    public ResponseEntity<Map<String, Object>> getDisposition(@PathVariable Long aiAdminId) {
        return ResponseEntity.ok(interactionService.getDisposition(aiAdminId));
    }

    // ------------------------------------------------------------------
    // Analytics endpoints
    // ------------------------------------------------------------------

    /**
     * Get performance metrics for a specific AI admin.
     */
    @GetMapping("/analytics/admin/{adminId}")
    public ResponseEntity<?> getAdminPerformance(@PathVariable Long adminId) {
        try {
            AiAdminAnalyticsService.AdminPerformance perf = analyticsService.getAdminPerformance(adminId);
            return ResponseEntity.ok(perf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get leaderboard of all AI admins ranked by performance.
     */
    @GetMapping("/analytics/leaderboard")
    public ResponseEntity<?> getAdminLeaderboard() {
        List<AiAdminAnalyticsService.AdminLeaderboardEntry> leaderboard = analyticsService.getAdminLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Get order trends over the last N days (default 7).
     */
    @GetMapping("/analytics/trends")
    public ResponseEntity<?> getOrderTrends(@RequestParam(defaultValue = "7") int days) {
        List<AiAdminAnalyticsService.OrderTrend> trends = analyticsService.getOrderTrends(days);
        return ResponseEntity.ok(trends);
    }

    /**
     * Get test results broken down by category.
     */
    @GetMapping("/analytics/categories")
    public ResponseEntity<?> getCategoryBreakdown() {
        List<AiAdminAnalyticsService.CategoryBreakdown> breakdown = analyticsService.getCategoryBreakdown();
        return ResponseEntity.ok(breakdown);
    }

    /**
     * Get overall system health summary.
     */
    @GetMapping("/analytics/health")
    public ResponseEntity<?> getSystemHealth() {
        AiAdminAnalyticsService.SystemHealth health = analyticsService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    // ------------------------------------------------------------------
    // Advanced Quest endpoints (chains, steps, rewards)
    // ------------------------------------------------------------------

    /**
     * Assign a quest chain to a player (admin-only).
     */
    @PostMapping("/admins/{adminId}/quest-chain")
    public ResponseEntity<?> assignQuestChain(@PathVariable Long adminId,
                                               @RequestBody AiAdminQuestChainRequest request,
                                               Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required"));
        }
        try {
            List<AiAdminQuest> quests = new ArrayList<>();
            for (AiAdminQuestRequest qReq : request.getQuests()) {
                AiAdminQuest quest = new AiAdminQuest(
                        adminId, request.getPlayerId(),
                        qReq.getTitle(), qReq.getDescription(),
                        AiAdminQuest.QuestType.valueOf(qReq.getQuestType()),
                        qReq.getXpReward() != null ? qReq.getXpReward() : 10,
                        qReq.getDispositionReward() != null ? qReq.getDispositionReward() : 1,
                        qReq.getTargetDescription(),
                        qReq.getTargetCount() != null ? qReq.getTargetCount() : 1);
                quests.add(quest);
            }
            List<AiAdminQuest> saved = interactionService.assignQuestChain(
                    adminId, request.getPlayerId(), request.getChainId(), quests);
            List<Map<String, Object>> result = new ArrayList<>();
            for (AiAdminQuest q : saved) {
                result.add(questToMap(q));
            }
            return ResponseEntity.ok(Map.of(
                    "chainId", request.getChainId(),
                    "quests", result,
                    "message", "Quest chain assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get quest steps for a quest.
     */
    @GetMapping("/quests/{questId}/steps")
    public ResponseEntity<?> getQuestSteps(@PathVariable Long questId) {
        try {
            List<AiAdminQuestStep> steps = interactionService.getQuestSteps(questId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (AiAdminQuestStep step : steps) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", step.getId());
                map.put("questId", step.getQuestId());
                map.put("stepOrder", step.getStepOrder());
                map.put("title", step.getTitle());
                map.put("description", step.getDescription());
                map.put("targetCount", step.getTargetCount());
                map.put("currentCount", step.getCurrentCount());
                map.put("completed", step.isCompleted());
                map.put("progressPercent", step.getProgressPercent());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Complete a specific step.
     */
    @PostMapping("/quests/{questId}/steps/{stepOrder}/complete")
    public ResponseEntity<?> completeStep(@PathVariable Long questId,
                                           @PathVariable int stepOrder) {
        try {
            AiAdminQuestStep step = interactionService.completeStep(questId, stepOrder);
            Map<String, Object> map = new HashMap<>();
            map.put("id", step.getId());
            map.put("stepOrder", step.getStepOrder());
            map.put("title", step.getTitle());
            map.put("completed", step.isCompleted());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update progress on a specific step.
     */
    @PostMapping("/quests/{questId}/steps/{stepOrder}/progress")
    public ResponseEntity<?> updateStepProgress(@PathVariable Long questId,
                                                  @PathVariable int stepOrder,
                                                  @RequestBody QuestProgressRequest request) {
        try {
            AiAdminQuestStep step = interactionService.updateStepProgress(
                    questId, stepOrder, request.getAmount());
            Map<String, Object> map = new HashMap<>();
            map.put("id", step.getId());
            map.put("stepOrder", step.getStepOrder());
            map.put("currentCount", step.getCurrentCount());
            map.put("targetCount", step.getTargetCount());
            map.put("completed", step.isCompleted());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all rewards for a quest.
     */
    @GetMapping("/quests/{questId}/rewards")
    public ResponseEntity<?> getQuestRewards(@PathVariable Long questId) {
        try {
            Map<String, Object> summary = interactionService.getQuestRewardSummary(questId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get chain progress for a player.
     */
    @GetMapping("/chains/{chainId}/progress")
    public ResponseEntity<?> getChainProgress(@PathVariable String chainId,
                                               @RequestParam Long playerId) {
        try {
            Map<String, Object> progress = interactionService.getChainProgress(chainId, playerId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all active chains for a player.
     */
    @GetMapping("/player/{playerId}/chains")
    public ResponseEntity<?> getPlayerChains(@PathVariable Long playerId) {
        try {
            List<String> chains = interactionService.getAllChainsForPlayer(playerId);
            return ResponseEntity.ok(Map.of("chains", chains, "count", chains.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Quest helpers
    // ------------------------------------------------------------------

    private Map<String, Object> questToMap(AiAdminQuest quest) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", quest.getId());
        map.put("questGiverId", quest.getQuestGiverId());
        map.put("playerId", quest.getPlayerId());
        map.put("title", quest.getTitle());
        map.put("description", quest.getDescription());
        map.put("questType", quest.getQuestType().name());
        map.put("status", quest.getStatus().name());
        map.put("xpReward", quest.getXpReward());
        map.put("dispositionReward", quest.getDispositionReward());
        map.put("targetDescription", quest.getTargetDescription());
        map.put("targetCount", quest.getTargetCount());
        map.put("currentCount", quest.getCurrentCount());
        map.put("progressPercent", quest.getProgressPercent());
        map.put("assignedAt", quest.getAssignedAt());
        map.put("completedAt", quest.getCompletedAt());
        map.put("isActive", quest.isActive());
        // Chain fields
        map.put("chainId", quest.getChainId());
        map.put("chainOrder", quest.getChainOrder());
        map.put("parentQuestId", quest.getParentQuestId());
        map.put("isPartOfChain", quest.isPartOfChain());
        // Reward fields
        map.put("rewardCredits", quest.getRewardCredits());
        map.put("rewardReputation", quest.getRewardReputation());
        map.put("rewardItemId", quest.getRewardItemId());
        map.put("rewardTitle", quest.getRewardTitle());
        return map;
    }

    /**
     * Request body for quest assignment.
     */
    public static class AiAdminQuestRequest {
        private String title;
        private String description;
        private String questType = "OTHER";
        private Integer xpReward;
        private Integer dispositionReward;
        private String targetDescription;
        private Integer targetCount;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getQuestType() { return questType; }
        public void setQuestType(String questType) { this.questType = questType; }
        public Integer getXpReward() { return xpReward; }
        public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
        public Integer getDispositionReward() { return dispositionReward; }
        public void setDispositionReward(Integer dispositionReward) { this.dispositionReward = dispositionReward; }
        public String getTargetDescription() { return targetDescription; }
        public void setTargetDescription(String targetDescription) { this.targetDescription = targetDescription; }
        public Integer getTargetCount() { return targetCount; }
        public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }
    }

    /**
     * Request body for quest progress update.
     */
    public static class QuestProgressRequest {
        private int amount = 1;

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    /**
     * Request body for quest failure.
     */
    public static class QuestFailRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Request body for quest chain assignment.
     */
    public static class AiAdminQuestChainRequest {
        private String chainId;
        private Long playerId;
        private List<AiAdminQuestRequest> quests;

        public String getChainId() { return chainId; }
        public void setChainId(String chainId) { this.chainId = chainId; }
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public List<AiAdminQuestRequest> getQuests() { return quests; }
        public void setQuests(List<AiAdminQuestRequest> quests) { this.quests = quests; }
    }

    // ==================== INCIDENT MANAGEMENT ENDPOINTS ====================

    /**
     * Create a new incident manually.
     */
    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(@RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        String type = (String) body.get("type");
        String severity = (String) body.get("severity");
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String galaxyName = (String) body.get("galaxyName");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
        }
        if (galaxyName == null || galaxyName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Galaxy name is required"));
        }
        AiAdminIncident.IncidentType incidentType = parseIncidentType(type);
        AiAdminIncident.Severity incidentSeverity = parseSeverity(severity);
        AiAdminIncident incident = incidentService.createIncident(
                incidentType, incidentSeverity, title, description, galaxyName);
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentToMap(incident));
    }

    /**
     * List incidents with optional filters.
     */
    @GetMapping("/incidents")
    public ResponseEntity<?> listIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String galaxyName,
            Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminIncident> incidents;
        if (galaxyName != null && !galaxyName.isBlank()) {
            incidents = incidentService.getIncidentsForGalaxy(galaxyName);
        } else if (status != null && !status.isBlank()) {
            AiAdminIncident.Status s = parseIncidentStatus(status);
            incidents = incidentService.getIncidentsByStatus(s);
        } else {
            incidents = incidentService.getOpenIncidents();
        }
        List<Map<String, Object>> result = incidents.stream().map(this::incidentToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get incident details by ID.
     */
    @GetMapping("/incidents/{id}")
    public ResponseEntity<?> getIncident(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        AiAdminIncident incident = incidentService.getIncidentsByStatus(AiAdminIncident.Status.OPEN).stream()
                .filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        if (incident == null) {
            // Try all statuses
            for (AiAdminIncident.Status s : AiAdminIncident.Status.values()) {
                incident = incidentService.getIncidentsByStatus(s).stream()
                        .filter(i -> i.getId().equals(id)).findFirst().orElse(null);
                if (incident != null) break;
            }
        }
        if (incident == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Incident not found"));
        }
        return ResponseEntity.ok(incidentToMap(incident));
    }

    /**
     * Assign an incident to an AI admin.
     */
    @PostMapping("/incidents/{id}/assign")
    public ResponseEntity<?> assignIncident(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        try {
            AiAdminIncident incident = incidentService.assignIncident(id, adminId);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Investigate an incident with optional LLM analysis.
     */
    @PostMapping("/incidents/{id}/investigate")
    public ResponseEntity<?> investigateIncident(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String notes = (String) body.get("notes");
        boolean useLlm = body.getOrDefault("useLlm", true).equals(true);
        try {
            AiAdminIncident incident = incidentService.investigate(id, adminId, notes, useLlm);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resolve an incident.
     */
    @PostMapping("/incidents/{id}/resolve")
    public ResponseEntity<?> resolveIncident(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String action = (String) body.get("action");
        String rootCause = (String) body.get("rootCause");
        try {
            AiAdminIncident incident = incidentService.resolve(id, adminId, action, rootCause);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Escalate an incident.
     */
    @PostMapping("/incidents/{id}/escalate")
    public ResponseEntity<?> escalateIncident(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            AiAdminIncident incident = incidentService.escalate(id);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Close an incident.
     */
    @PostMapping("/incidents/{id}/close")
    public ResponseEntity<?> closeIncident(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            AiAdminIncident incident = incidentService.close(id);
            return ResponseEntity.ok(incidentToMap(incident));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get incidents for a galaxy.
     */
    @GetMapping("/incidents/galaxy/{galaxyName}")
    public ResponseEntity<?> getIncidentsForGalaxy(@PathVariable String galaxyName, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminIncident> incidents = incidentService.getIncidentsForGalaxy(galaxyName);
        List<Map<String, Object>> result = incidents.stream().map(this::incidentToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get incident dashboard summary.
     */
    @GetMapping("/incidents/dashboard")
    public ResponseEntity<?> getIncidentDashboard(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Map<String, Object> summary = incidentService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Helper: convert incident to map for JSON response.
     */
    private Map<String, Object> incidentToMap(AiAdminIncident incident) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", incident.getId());
        map.put("type", incident.getIncidentType() != null ? incident.getIncidentType().name() : null);
        map.put("severity", incident.getSeverity() != null ? incident.getSeverity().name() : null);
        map.put("priority", incident.getPriority());
        map.put("status", incident.getStatus() != null ? incident.getStatus().name() : null);
        map.put("title", incident.getTitle());
        map.put("description", incident.getDescription());
        map.put("galaxyName", incident.getGalaxyName());
        map.put("assignedAiAdminId", incident.getAssignedAiAdminId());
        map.put("investigatorNotes", incident.getInvestigatorNotes());
        map.put("resolutionAction", incident.getResolutionAction());
        map.put("rootCause", incident.getRootCause());
        map.put("relatedOrderId", incident.getRelatedOrderId());
        map.put("createdAt", incident.getCreatedAt());
        map.put("updatedAt", incident.getUpdatedAt());
        map.put("resolvedAt", incident.getResolvedAt());
        map.put("escalatedAt", incident.getEscalatedAt());
        return map;
    }

    /**
     * Helper: parse incident type from string.
     */
    private AiAdminIncident.IncidentType parseIncidentType(String type) {
        if (type == null || type.isBlank()) return AiAdminIncident.IncidentType.HEALTH;
        try {
            return AiAdminIncident.IncidentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiAdminIncident.IncidentType.HEALTH;
        }
    }

    /**
     * Helper: parse severity from string.
     */
    private AiAdminIncident.Severity parseSeverity(String severity) {
        if (severity == null || severity.isBlank()) return AiAdminIncident.Severity.MEDIUM;
        try {
            return AiAdminIncident.Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiAdminIncident.Severity.MEDIUM;
        }
    }

    /**
     * Helper: parse incident status from string.
     */
    private AiAdminIncident.Status parseIncidentStatus(String status) {
        if (status == null || status.isBlank()) return AiAdminIncident.Status.OPEN;
        try {
            return AiAdminIncident.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiAdminIncident.Status.OPEN;
        }
    }

    // ==================== COLLABORATION ENDPOINTS ====================

    /**
     * Add a collaborator to an incident.
     */
    @PostMapping("/incidents/{id}/collaborators")
    public ResponseEntity<?> addCollaborator(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String role = (String) body.get("role");
        AiAdminIncidentCollaborator.Role collaboratorRole;
        if (role == null || role.isBlank()) {
            collaboratorRole = AiAdminIncidentCollaborator.Role.INVESTIGATOR;
        } else {
            try {
                collaboratorRole = AiAdminIncidentCollaborator.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                collaboratorRole = AiAdminIncidentCollaborator.Role.INVESTIGATOR;
            }
        }
        try {
            AiAdminIncidentCollaborator collab = collaborationService.addCollaborator(id, adminId, collaboratorRole);
            return ResponseEntity.ok(collaboratorToMap(collab));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove a collaborator from an incident.
     */
    @DeleteMapping("/incidents/{id}/collaborators/{adminId}")
    public ResponseEntity<?> removeCollaborator(@PathVariable Long id, @PathVariable Long adminId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            collaborationService.removeCollaborator(id, adminId);
            return ResponseEntity.ok(Map.of("message", "Collaborator removed"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all collaborators for an incident.
     */
    @GetMapping("/incidents/{id}/collaborators")
    public ResponseEntity<?> listCollaborators(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminIncidentCollaborator> collaborators = collaborationService.getCollaborators(id);
        List<Map<String, Object>> result = collaborators.stream().map(this::collaboratorToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Add a collaboration note to an incident.
     */
    @PostMapping("/incidents/{id}/notes")
    public ResponseEntity<?> addNote(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Long adminId = ((Number) body.get("adminId")).longValue();
        String note = (String) body.get("note");
        try {
            AiAdminCollaborationNote collaborationNote = collaborationService.addNote(id, adminId, note);
            return ResponseEntity.ok(noteToMap(collaborationNote));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all notes for an incident.
     */
    @GetMapping("/incidents/{id}/notes")
    public ResponseEntity<?> getNotes(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        List<AiAdminCollaborationNote> notes = collaborationService.getNotes(id);
        List<Map<String, Object>> result = notes.stream().map(this::noteToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Auto-invite collaborators based on expertise.
     */
    @PostMapping("/incidents/{id}/auto-invite")
    public ResponseEntity<?> autoInvite(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        try {
            List<AiAdminIncidentCollaborator> invited = collaborationService.autoInviteCollaborators(id);
            List<Map<String, Object>> result = invited.stream().map(this::collaboratorToMap).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get collaboration summary for an incident.
     */
    @GetMapping("/incidents/{id}/collaboration-summary")
    public ResponseEntity<?> getCollaborationSummary(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        Map<String, Object> summary = collaborationService.getCollaborationSummary(id);
        return ResponseEntity.ok(summary);
    }

    /**
     * Helper: convert collaborator to map for JSON response.
     */
    private Map<String, Object> collaboratorToMap(AiAdminIncidentCollaborator c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("incidentId", c.getIncidentId());
        map.put("aiAdminId", c.getAiAdminId());
        map.put("role", c.getRole() != null ? c.getRole().name() : null);
        map.put("status", c.getStatus() != null ? c.getStatus().name() : null);
        map.put("notes", c.getNotes());
        map.put("joinedAt", c.getJoinedAt());
        map.put("completedAt", c.getCompletedAt());
        return map;
    }

    /**
     * Helper: convert note to map for JSON response.
     */
    private Map<String, Object> noteToMap(AiAdminCollaborationNote n) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", n.getId());
        map.put("incidentId", n.getIncidentId());
        map.put("aiAdminId", n.getAiAdminId());
        map.put("note", n.getNote());
        map.put("createdAt", n.getCreatedAt());
        return map;
    }
}
