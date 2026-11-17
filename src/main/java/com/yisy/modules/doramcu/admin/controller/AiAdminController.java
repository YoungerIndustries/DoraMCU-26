package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.dto.AiAdminOrderRequest;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto.AiAdminTestCaseDto;
import com.yisy.modules.doramcu.admin.model.AiAdminNotification;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.service.AiAdminDispatchService;
import com.yisy.modules.doramcu.admin.service.AiAdminLlmService;
import com.yisy.modules.doramcu.admin.service.AiAdminNotificationService;
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

    public AiAdminController(AiAdminService aiAdminService,
                             AiAdminDispatchService aiAdminDispatchService,
                             AiAdminNotificationService notificationService,
                             PlayerRepository playerRepository,
                             UserRepository userRepository,
                             AiAdminTestCaseRepository testCaseRepository,
                             TestRegistry testRegistry,
                             AiCharacterRepository aiCharacterRepository,
                             AiAdminLlmService llmService,
                             AiAdminVisibilityService visibilityService) {
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
                request.getTargetPosY(), request.getTargetPosZ());

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

        log.info("Order {} submitted by {}", order.getId(), username);
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
     * Returns dispatch summary, all AI admins with positions, galaxy list, and notification counts.
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> getDashboardSummary() {
        AiAdminDispatchService.DispatchSummary dispatchSummary = aiAdminDispatchService.getDispatchSummary();
        List<AiCharacter> allAdmins = aiAdminDispatchService.getAiAdminWorkload();

        // Build admin list with position info
        List<Map<String, Object>> admins = allAdmins.stream().map(admin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", admin.getId());
            map.put("name", admin.getName());
            map.put("role", admin.getRole());
            map.put("empireName", admin.getEmpireName());
            map.put("galaxyName", admin.getGalaxyName());
            map.put("characterType", admin.getCharacterType() != null ? admin.getCharacterType().name() : "HUMAN_LEADER");
            map.put("posX", admin.getPosX());
            map.put("posY", admin.getPosY());
            map.put("posZ", admin.getPosZ());
            map.put("homePosX", admin.getHomePosX());
            map.put("homePosY", admin.getHomePosY());
            map.put("homePosZ", admin.getHomePosZ());
            map.put("currentOrderId", admin.getCurrentOrderId());
            map.put("activeOrderCount", admin.getActiveOrderCount());
            map.put("active", admin.isActive());
            map.put("status", admin.getActiveOrderCount() >= AiAdminDispatchService.MAX_ORDERS_PER_ADMIN ? "busy" : "available");
            return map;
        }).collect(Collectors.toList());

        // Collect unique galaxy names
        List<String> galaxies = allAdmins.stream()
                .map(AiCharacter::getGalaxyName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Notification summary: count unread notifications across all admins
        List<AiAdminNotification> unreadNotifications = allAdmins.stream()
                .flatMap(admin -> notificationService.getUnreadNotificationsForAdmin(admin.getId()).stream())
                .collect(Collectors.toList());
        int totalUnread = unreadNotifications.size();
        Map<String, Integer> unreadByAdmin = unreadNotifications.stream()
                .collect(Collectors.groupingBy(
                        n -> String.valueOf(n.getAiAdminId()),
                        Collectors.summingInt(n -> 1)
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("dispatchSummary", toMap(dispatchSummary));
        response.put("admins", admins);
        response.put("galaxies", galaxies);
        response.put("notificationsSummary", Map.of(
                "totalUnread", totalUnread,
                "unreadByAdmin", unreadByAdmin
        ));
        return ResponseEntity.ok(response);
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
}
