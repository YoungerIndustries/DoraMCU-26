package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.dto.AiAdminOrderRequest;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto;
import com.yisy.modules.doramcu.admin.dto.AiAdminReportDto.AiAdminTestCaseDto;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestCaseRepository;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.admin.service.TestRegistry;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
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
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final AiAdminTestCaseRepository testCaseRepository;
    private final TestRegistry testRegistry;

    public AiAdminController(AiAdminService aiAdminService,
                             PlayerRepository playerRepository,
                             UserRepository userRepository,
                             AiAdminTestCaseRepository testCaseRepository,
                             TestRegistry testRegistry) {
        this.aiAdminService = aiAdminService;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.testCaseRepository = testCaseRepository;
        this.testRegistry = testRegistry;
    }

    /**
     * Submit a new test order.
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
        AiAdminOrder order = aiAdminService.submitOrder(username, orderText);

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("status", order.getStatus().name());
        result.put("submittedBy", order.getSubmittedBy());
        result.put("createdAt", order.getCreatedAt());

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
}
