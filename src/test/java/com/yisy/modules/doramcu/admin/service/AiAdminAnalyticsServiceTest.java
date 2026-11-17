package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTestReportRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiAdminAnalyticsService Tests")
class AiAdminAnalyticsServiceTest {

    @Mock
    private AiAdminOrderRepository orderRepository;

    @Mock
    private AiAdminTestReportRepository reportRepository;

    @Mock
    private AiCharacterRepository characterRepository;

    private AiAdminAnalyticsService analyticsService;

    private AiCharacter admin1;
    private AiCharacter admin2;

    @BeforeEach
    void setUp() {
        analyticsService = new AiAdminAnalyticsService(orderRepository, reportRepository, characterRepository);

        admin1 = new AiCharacter();
        admin1.setId(1L);
        admin1.setName("Overseer Alpha");
        admin1.setRole("Galaxy Overseer");
        admin1.setEmpireName("Alpha Empire");
        admin1.setTotalExperience(500);
        admin1.setAiAdmin(true);
        admin1.setActive(true);

        admin2 = new AiCharacter();
        admin2.setId(2L);
        admin2.setName("Overseer Beta");
        admin2.setRole("Galaxy Overseer");
        admin2.setEmpireName("Beta Empire");
        admin2.setTotalExperience(300);
        admin2.setAiAdmin(true);
        admin2.setActive(true);
    }

    // --- getAdminPerformance tests ---

    @Test
    @DisplayName("getAdminPerformance should return correct metrics")
    void getAdminPerformance_shouldReturnCorrectMetrics() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(admin1));
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.COMPLETED))).thenReturn(8L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.FAILED))).thenReturn(2L);
        when(orderRepository.avgCompletionDurationMsByAdminId(1L)).thenReturn(5000.0);
        when(orderRepository.findByAssignedAiAdminIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        AiAdminAnalyticsService.AdminPerformance perf = analyticsService.getAdminPerformance(1L);

        assertEquals(1L, perf.adminId);
        assertEquals("Overseer Alpha", perf.adminName);
        assertEquals(10, perf.totalOrders);
        assertEquals(8, perf.completedOrders);
        assertEquals(2, perf.failedOrders);
        assertEquals(80.0, perf.successRate, 0.001);
        assertEquals(5000L, perf.avgCompletionTimeMs);
        assertEquals(500, perf.totalExperience);
    }

    @Test
    @DisplayName("getAdminPerformance should handle zero orders")
    void getAdminPerformance_shouldHandleZeroOrders() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(admin1));
        when(orderRepository.countByAssignedAiAdminIdAndStatus(anyLong(), any())).thenReturn(0L);
        when(orderRepository.avgCompletionDurationMsByAdminId(1L)).thenReturn(null);
        when(orderRepository.findByAssignedAiAdminIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        AiAdminAnalyticsService.AdminPerformance perf = analyticsService.getAdminPerformance(1L);

        assertEquals(0, perf.totalOrders);
        assertEquals(0.0, perf.successRate, 0.001);
        assertEquals(0L, perf.avgCompletionTimeMs);
    }

    @Test
    @DisplayName("getAdminPerformance should throw when admin not found")
    void getAdminPerformance_shouldThrowWhenNotFound() {
        when(characterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> analyticsService.getAdminPerformance(999L));
    }

    @Test
    @DisplayName("getAdminPerformance should aggregate test reports")
    void getAdminPerformance_shouldAggregateTestReports() {
        AiAdminOrder order = new AiAdminOrder("admin", "Test API");
        order.setId(100L);

        AiAdminTestReport report = new AiAdminTestReport(100L);
        report.setPassed(10);
        report.setFailed(2);
        report.setSkipped(1);

        when(characterRepository.findById(1L)).thenReturn(Optional.of(admin1));
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.COMPLETED))).thenReturn(1L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.FAILED))).thenReturn(0L);
        when(orderRepository.avgCompletionDurationMsByAdminId(1L)).thenReturn(1000.0);
        when(orderRepository.findByAssignedAiAdminIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(order));
        when(reportRepository.findByOrderIdIn(List.of(100L))).thenReturn(List.of(report));

        AiAdminAnalyticsService.AdminPerformance perf = analyticsService.getAdminPerformance(1L);

        assertEquals(10, perf.totalTestsPassed);
        assertEquals(2, perf.totalTestsFailed);
        assertEquals(1, perf.totalTestsSkipped);
    }

    // --- getAdminLeaderboard tests ---

    @Test
    @DisplayName("getAdminLeaderboard should rank by success rate then XP")
    void getAdminLeaderboard_shouldRankBySuccessRateThenXP() {
        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(Arrays.asList(admin1, admin2));

        // admin1: 10/10 = 100% success
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.COMPLETED))).thenReturn(10L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.FAILED))).thenReturn(0L);

        // admin2: 5/10 = 50% success
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(2L), eq(AiAdminOrder.OrderStatus.COMPLETED))).thenReturn(5L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(2L), eq(AiAdminOrder.OrderStatus.FAILED))).thenReturn(5L);

        List<AiAdminAnalyticsService.AdminLeaderboardEntry> leaderboard = analyticsService.getAdminLeaderboard();

        assertEquals(2, leaderboard.size());
        assertEquals(1, leaderboard.get(0).rank);
        assertEquals(1L, leaderboard.get(0).adminId);
        assertEquals(100.0, leaderboard.get(0).successRate, 0.001);
        assertEquals(2, leaderboard.get(1).rank);
        assertEquals(50.0, leaderboard.get(1).successRate, 0.001);
    }

    @Test
    @DisplayName("getAdminLeaderboard should return empty list when no admins")
    void getAdminLeaderboard_shouldReturnEmptyWhenNoAdmins() {
        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(Collections.emptyList());

        List<AiAdminAnalyticsService.AdminLeaderboardEntry> leaderboard = analyticsService.getAdminLeaderboard();

        assertTrue(leaderboard.isEmpty());
    }

    @Test
    @DisplayName("getAdminLeaderboard should tie-break by XP")
    void getAdminLeaderboard_shouldTieBreakByXP() {
        // Both have 100% success rate; admin1 has higher XP
        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(Arrays.asList(admin1, admin2));

        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.COMPLETED))).thenReturn(5L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(1L), eq(AiAdminOrder.OrderStatus.FAILED))).thenReturn(0L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(2L), eq(AiAdminOrder.OrderStatus.COMPLETED))).thenReturn(5L);
        when(orderRepository.countByAssignedAiAdminIdAndStatus(eq(2L), eq(AiAdminOrder.OrderStatus.FAILED))).thenReturn(0L);

        List<AiAdminAnalyticsService.AdminLeaderboardEntry> leaderboard = analyticsService.getAdminLeaderboard();

        assertEquals(1, leaderboard.get(0).rank);
        assertEquals(1L, leaderboard.get(0).adminId); // admin1 has higher XP
    }

    // --- getOrderTrends tests ---

    @Test
    @DisplayName("getOrderTrends should return daily counts")
    void getOrderTrends_shouldReturnDailyCounts() {
        when(orderRepository.countOrdersByStatusAndDateRange(any(), any()))
                .thenReturn(Collections.emptyList());

        List<AiAdminAnalyticsService.OrderTrend> trends = analyticsService.getOrderTrends(3);

        assertEquals(3, trends.size());
        assertEquals(0, trends.get(0).pending);
        assertEquals(0, trends.get(0).completed);
    }

    @Test
    @DisplayName("getOrderTrends should return empty for zero days")
    void getOrderTrends_shouldReturnEmptyForZeroDays() {
        List<AiAdminAnalyticsService.OrderTrend> trends = analyticsService.getOrderTrends(0);
        assertTrue(trends.isEmpty());
    }

    @Test
    @DisplayName("getOrderTrends should return empty for negative days")
    void getOrderTrends_shouldReturnEmptyForNegativeDays() {
        List<AiAdminAnalyticsService.OrderTrend> trends = analyticsService.getOrderTrends(-1);
        assertTrue(trends.isEmpty());
    }

    @Test
    @DisplayName("getOrderTrends should parse status counts correctly")
    void getOrderTrends_shouldParseStatusCounts() {
        Object[] completedRow = new Object[]{"COMPLETED", 5L};
        Object[] failedRow = new Object[]{"FAILED", 2L};
        when(orderRepository.countOrdersByStatusAndDateRange(any(), any()))
                .thenReturn(Arrays.asList(completedRow, failedRow));

        List<AiAdminAnalyticsService.OrderTrend> trends = analyticsService.getOrderTrends(1);

        assertEquals(1, trends.size());
        assertEquals(5, trends.get(0).completed);
        assertEquals(2, trends.get(0).failed);
        assertEquals(0, trends.get(0).pending);
    }

    // --- getCategoryBreakdown tests ---

    @Test
    @DisplayName("getCategoryBreakdown should group by category")
    void getCategoryBreakdown_shouldGroupByCategory() {
        AiAdminOrder apiOrder = new AiAdminOrder("admin", "Test API endpoint");
        apiOrder.setId(1L);

        AiAdminOrder gameOrder = new AiAdminOrder("admin", "Test GAME mechanics");
        gameOrder.setId(2L);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(apiOrder, gameOrder));
        when(reportRepository.findByOrderIdIn(anyList())).thenReturn(Collections.emptyList());

        List<AiAdminAnalyticsService.CategoryBreakdown> breakdown = analyticsService.getCategoryBreakdown();

        assertEquals(2, breakdown.size());
        assertEquals("API", breakdown.get(0).category);
        assertEquals("GAME", breakdown.get(1).category);
    }

    @Test
    @DisplayName("getCategoryBreakdown should handle empty orders")
    void getCategoryBreakdown_shouldHandleEmptyOrders() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<AiAdminAnalyticsService.CategoryBreakdown> breakdown = analyticsService.getCategoryBreakdown();

        assertTrue(breakdown.isEmpty());
    }

    @Test
    @DisplayName("getCategoryBreakdown should categorize unknown as OTHER")
    void getCategoryBreakdown_shouldCategorizeUnknownAsOther() {
        AiAdminOrder order = new AiAdminOrder("admin", "Deploy more ships");
        order.setId(1L);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(reportRepository.findByOrderIdIn(anyList())).thenReturn(Collections.emptyList());

        List<AiAdminAnalyticsService.CategoryBreakdown> breakdown = analyticsService.getCategoryBreakdown();

        assertEquals(1, breakdown.size());
        assertEquals("OTHER", breakdown.get(0).category);
    }

    // --- getSystemHealth tests ---

    @Test
    @DisplayName("getSystemHealth should return correct counts")
    void getSystemHealth_shouldReturnCorrectCounts() {
        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(Arrays.asList(admin1, admin2));
        when(characterRepository.findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNull())
                .thenReturn(List.of(admin2));
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.PENDING)).thenReturn(5L);
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.RUNNING)).thenReturn(2L);
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.COMPLETED)).thenReturn(10L);
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.FAILED)).thenReturn(3L);
        when(orderRepository.findByStatus(AiAdminOrder.OrderStatus.COMPLETED)).thenReturn(Collections.emptyList());

        AiAdminAnalyticsService.SystemHealth health = analyticsService.getSystemHealth();

        assertEquals(2, health.totalAdmins);
        assertEquals(1, health.availableAdmins);
        assertEquals(1, health.busyAdmins);
        assertEquals(5, health.pendingOrders);
        assertEquals(2, health.runningOrders);
        assertEquals(10, health.completedOrders);
        assertEquals(3, health.failedOrders);
    }

    @Test
    @DisplayName("getSystemHealth should calculate success rate")
    void getSystemHealth_shouldCalculateSuccessRate() {
        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(Collections.emptyList());
        when(characterRepository.findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNull())
                .thenReturn(Collections.emptyList());
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.PENDING)).thenReturn(0L);
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.RUNNING)).thenReturn(0L);
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.COMPLETED)).thenReturn(8L);
        when(orderRepository.countByStatus(AiAdminOrder.OrderStatus.FAILED)).thenReturn(2L);
        when(orderRepository.findByStatus(AiAdminOrder.OrderStatus.COMPLETED)).thenReturn(Collections.emptyList());

        AiAdminAnalyticsService.SystemHealth health = analyticsService.getSystemHealth();

        assertEquals(80.0, health.overallSuccessRate, 0.001);
    }

    @Test
    @DisplayName("getSystemHealth should handle no orders")
    void getSystemHealth_shouldHandleNoOrders() {
        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(Collections.emptyList());
        when(characterRepository.findByAiAdminTrueAndActiveTrueAndCurrentOrderIdIsNull())
                .thenReturn(Collections.emptyList());
        when(orderRepository.countByStatus(any())).thenReturn(0L);
        when(orderRepository.findByStatus(AiAdminOrder.OrderStatus.COMPLETED)).thenReturn(Collections.emptyList());

        AiAdminAnalyticsService.SystemHealth health = analyticsService.getSystemHealth();

        assertEquals(0.0, health.overallSuccessRate, 0.001);
        assertEquals(0L, health.avgCompletionTimeMs);
    }

    // --- extractCategory tests ---

    @Test
    @DisplayName("extractCategory should detect API")
    void extractCategory_shouldDetectApi() {
        assertEquals("API", analyticsService.extractCategory("Test API endpoint"));
    }

    @Test
    @DisplayName("extractCategory should detect GAME")
    void extractCategory_shouldDetectGame() {
        assertEquals("GAME", analyticsService.extractCategory("Test GAME mechanics"));
    }

    @Test
    @DisplayName("extractCategory should detect AI_CHAT")
    void extractCategory_shouldDetectAiChat() {
        assertEquals("AI_CHAT", analyticsService.extractCategory("Test AI_CHAT flow"));
    }

    @Test
    @DisplayName("extractCategory should detect AUTH")
    void extractCategory_shouldDetectAuth() {
        assertEquals("AUTH", analyticsService.extractCategory("Test AUTH login"));
    }

    @Test
    @DisplayName("extractCategory should return OTHER for unknown")
    void extractCategory_shouldReturnOther() {
        assertEquals("OTHER", analyticsService.extractCategory("Deploy more ships"));
    }

    @Test
    @DisplayName("extractCategory should return UNKNOWN for null")
    void extractCategory_shouldReturnUnknownForNull() {
        assertEquals("UNKNOWN", analyticsService.extractCategory(null));
    }

    @Test
    @DisplayName("extractCategory should return UNKNOWN for blank")
    void extractCategory_shouldReturnUnknownForBlank() {
        assertEquals("UNKNOWN", analyticsService.extractCategory("   "));
    }
}
