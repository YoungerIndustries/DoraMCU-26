package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminHealthReport;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.repository.AiAdminHealthReportRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiAdminXpTrendServiceTest {

    @Mock
    private AiAdminHealthReportRepository reportRepository;

    @Mock
    private AiAdminOrderRepository orderRepository;

    @Mock
    private AiAdminIncidentRepository incidentRepository;

    @Mock
    private AiCharacterRepository characterRepository;

    @InjectMocks
    private AiAdminXpTrendService service;

    private AiCharacter testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new AiCharacter();
        testAdmin.setId(1L);
        testAdmin.setName("Test Admin");
        testAdmin.setEmpireName("Test Empire");
        testAdmin.setRole("Galaxy Overseer");
        testAdmin.setTotalExperience(5000);
        testAdmin.setActive(true);
        testAdmin.setAiAdmin(true);
    }

    // ---------------------------------------------------------------
    // XP Trend tests
    // ---------------------------------------------------------------

    @Test
    void getXpTrend_returnsTrendPoints() {
        AiAdminHealthReport report1 = new AiAdminHealthReport();
        report1.setGeneratedAt(LocalDateTime.now().minusDays(2));
        report1.setXpEarned(50);
        report1.setOrdersCompleted(5);
        report1.setIncidentsHandled(1);

        AiAdminHealthReport report2 = new AiAdminHealthReport();
        report2.setGeneratedAt(LocalDateTime.now().minusDays(1));
        report2.setXpEarned(80);
        report2.setOrdersCompleted(8);
        report2.setIncidentsHandled(2);

        when(reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(eq(1L), any(), any()))
                .thenReturn(List.of(report2, report1));

        List<AiAdminXpTrendService.XpTrendPoint> trend = service.getXpTrend(1L, 7);

        assertEquals(2, trend.size());
        assertEquals(50, trend.get(0).xpEarned);
        assertEquals(80, trend.get(1).xpEarned);
    }

    @Test
    void getXpTrend_returnsEmptyWhenNoReports() {
        when(reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(eq(1L), any(), any()))
                .thenReturn(new ArrayList<>());

        List<AiAdminXpTrendService.XpTrendPoint> trend = service.getXpTrend(1L, 7);

        assertTrue(trend.isEmpty());
    }

    // ---------------------------------------------------------------
    // XP Velocity tests
    // ---------------------------------------------------------------

    @Test
    void getXpVelocity_calculatesVelocity() {
        AiAdminHealthReport report1 = new AiAdminHealthReport();
        report1.setGeneratedAt(LocalDateTime.now().minusDays(7));
        report1.setXpEarned(100);

        AiAdminHealthReport report2 = new AiAdminHealthReport();
        report2.setGeneratedAt(LocalDateTime.now());
        report2.setXpEarned(200);

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report2, report1));

        AiAdminXpTrendService.XpVelocity velocity = service.getXpVelocity(1L);

        assertEquals(300, velocity.totalXp);
        assertEquals(2, velocity.totalReports);
        assertTrue(velocity.xpPerDay > 0);
        assertTrue(velocity.xpPerWeek > 0);
        assertEquals(150.0, velocity.xpPerReport, 0.1);
    }

    @Test
    void getXpVelocity_returnsEmptyWhenNoReports() {
        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(new ArrayList<>());

        AiAdminXpTrendService.XpVelocity velocity = service.getXpVelocity(1L);

        assertEquals(0, velocity.totalXp);
        assertEquals(0, velocity.totalReports);
        assertEquals(0.0, velocity.xpPerDay);
    }

    @Test
    void getXpVelocity_handlesSingleReport() {
        AiAdminHealthReport report = new AiAdminHealthReport();
        report.setGeneratedAt(LocalDateTime.now());
        report.setXpEarned(100);

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report));

        AiAdminXpTrendService.XpVelocity velocity = service.getXpVelocity(1L);

        assertEquals(100, velocity.totalXp);
        assertEquals(1, velocity.totalReports);
        // Single report means daysSpan = 1, so xpPerDay = totalXp
        assertEquals(100.0, velocity.xpPerDay, 0.1);
    }

    // ---------------------------------------------------------------
    // XP Projection tests
    // ---------------------------------------------------------------

    @Test
    void getXpProjection_returnsNextMilestone() {
        AiAdminHealthReport report = new AiAdminHealthReport();
        report.setGeneratedAt(LocalDateTime.now().minusDays(3));
        report.setXpEarned(800);

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report));

        AiAdminXpTrendService.XpProjection projection = service.getXpProjection(1L);

        assertEquals(800, projection.currentXp);
        assertEquals(1000, projection.nextMilestone);
        assertEquals(200, projection.xpRemaining);
        assertFalse(projection.allMilestonesAchieved);
    }

    @Test
    void getXpProjection_allMilestonesAchieved() {
        AiAdminHealthReport report = new AiAdminHealthReport();
        report.setGeneratedAt(LocalDateTime.now().minusDays(3));
        report.setXpEarned(150000);

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report));

        AiAdminXpTrendService.XpProjection projection = service.getXpProjection(1L);

        assertTrue(projection.allMilestonesAchieved);
        assertEquals(-1, projection.nextMilestone);
        assertEquals(0, projection.xpRemaining);
    }

    @Test
    void getXpProjection_noVelocity() {
        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(new ArrayList<>());

        AiAdminXpTrendService.XpProjection projection = service.getXpProjection(1L);

        assertEquals(0, projection.currentXp);
        assertEquals(100, projection.nextMilestone);
        assertEquals(-1, projection.estimatedDaysRemaining);
    }

    // ---------------------------------------------------------------
    // XP Source Breakdown tests
    // ---------------------------------------------------------------

    @Test
    void getXpSourceBreakdown_calculatesBreakdown() {
        AiAdminOrder order1 = new AiAdminOrder();
        order1.setStatus(AiAdminOrder.OrderStatus.COMPLETED);
        order1.setCreatedAt(LocalDateTime.now().minusDays(1));

        AiAdminOrder order2 = new AiAdminOrder();
        order2.setStatus(AiAdminOrder.OrderStatus.FAILED);
        order2.setCreatedAt(LocalDateTime.now().minusDays(2));

        AiAdminIncident incident = new AiAdminIncident();
        incident.setStatus(AiAdminIncident.Status.RESOLVED);
        incident.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(orderRepository.findByAssignedAiAdminIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(List.of(order1, order2));
        when(incidentRepository.findByAssignedAiAdminIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(List.of(incident));

        AiAdminXpTrendService.XpSourceBreakdown breakdown = service.getXpSourceBreakdown(1L, 7);

        assertEquals(10, breakdown.xpFromOrders); // 1 completed * 10 XP
        assertEquals(25, breakdown.xpFromIncidents); // 1 resolved * 25 XP
        assertEquals(35, breakdown.totalXp);
        assertEquals(1, breakdown.ordersCompleted);
        assertEquals(1, breakdown.incidentsResolved);
    }

    @Test
    void getXpSourceBreakdown_emptyData() {
        when(orderRepository.findByAssignedAiAdminIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(new ArrayList<>());
        when(incidentRepository.findByAssignedAiAdminIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(new ArrayList<>());

        AiAdminXpTrendService.XpSourceBreakdown breakdown = service.getXpSourceBreakdown(1L, 7);

        assertEquals(0, breakdown.totalXp);
        assertEquals(0, breakdown.xpFromOrders);
        assertEquals(0, breakdown.xpFromIncidents);
        assertEquals(0.0, breakdown.ordersPercent);
    }

    // ---------------------------------------------------------------
    // Admin XP Comparison tests
    // ---------------------------------------------------------------

    @Test
    void getAdminXpComparison_returnsAllAdmins() {
        AiCharacter admin1 = new AiCharacter();
        admin1.setId(1L);
        admin1.setName("Admin 1");
        admin1.setEmpireName("Empire 1");
        admin1.setRole("Galaxy Overseer");
        admin1.setTotalExperience(5000);

        AiCharacter admin2 = new AiCharacter();
        admin2.setId(2L);
        admin2.setName("Admin 2");
        admin2.setEmpireName("Empire 2");
        admin2.setRole("Fleet Commander");
        admin2.setTotalExperience(3000);

        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(List.of(admin1, admin2));
        when(reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(eq(1L), any(), any()))
                .thenReturn(new ArrayList<>());
        when(reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(eq(2L), any(), any()))
                .thenReturn(new ArrayList<>());

        List<AiAdminXpTrendService.AdminXpComparison> comparisons = service.getAdminXpComparison(7);

        assertEquals(2, comparisons.size());
        assertEquals("Admin 1", comparisons.get(0).adminName);
        assertEquals("Admin 2", comparisons.get(1).adminName);
    }

    @Test
    void getAdminXpComparison_sortedByXpEarned() {
        AiCharacter admin1 = new AiCharacter();
        admin1.setId(1L);
        admin1.setName("Admin 1");
        admin1.setTotalExperience(5000);

        AiCharacter admin2 = new AiCharacter();
        admin2.setId(2L);
        admin2.setName("Admin 2");
        admin2.setTotalExperience(3000);

        when(characterRepository.findByAiAdminTrueAndActiveTrueOrderByTotalExperienceDesc())
                .thenReturn(List.of(admin1, admin2));

        AiAdminHealthReport report2 = new AiAdminHealthReport();
        report2.setXpEarned(200);
        report2.setOrdersCompleted(10);
        report2.setIncidentsHandled(2);

        when(reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(eq(1L), any(), any()))
                .thenReturn(new ArrayList<>());
        when(reportRepository.findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(eq(2L), any(), any()))
                .thenReturn(List.of(report2));

        List<AiAdminXpTrendService.AdminXpComparison> comparisons = service.getAdminXpComparison(7);

        // Admin 2 should be first because it has more XP earned
        assertEquals("Admin 2", comparisons.get(0).adminName);
        assertEquals(200, comparisons.get(0).xpEarned);
    }

    // ---------------------------------------------------------------
    // XP Milestone History tests
    // ---------------------------------------------------------------

    @Test
    void getXpMilestoneHistory_returnsAllMilestones() {
        AiAdminHealthReport report = new AiAdminHealthReport();
        report.setGeneratedAt(LocalDateTime.now());
        report.setXpEarned(1500);

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report));

        List<AiAdminXpTrendService.XpMilestoneAchievement> achievements = service.getXpMilestoneHistory(1L);

        assertEquals(9, achievements.size());
        // 100, 500, 1000 should be achieved
        assertTrue(achievements.get(0).achieved); // 100
        assertTrue(achievements.get(1).achieved); // 500
        assertTrue(achievements.get(2).achieved); // 1000
        assertFalse(achievements.get(3).achieved); // 2500
    }

    @Test
    void getXpMilestoneHistory_noReports() {
        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(new ArrayList<>());

        List<AiAdminXpTrendService.XpMilestoneAchievement> achievements = service.getXpMilestoneHistory(1L);

        assertEquals(9, achievements.size());
        for (AiAdminXpTrendService.XpMilestoneAchievement a : achievements) {
            assertFalse(a.achieved);
            assertEquals(0, a.cumulativeXp);
        }
    }

    @Test
    void getXpMilestoneHistory_estimatesAchievementDate() {
        AiAdminHealthReport report1 = new AiAdminHealthReport();
        report1.setGeneratedAt(LocalDateTime.now().minusDays(5));
        report1.setXpEarned(50);

        AiAdminHealthReport report2 = new AiAdminHealthReport();
        report2.setGeneratedAt(LocalDateTime.now().minusDays(3));
        report2.setXpEarned(100); // Cumulative: 150, crosses 100 threshold

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report2, report1));

        List<AiAdminXpTrendService.XpMilestoneAchievement> achievements = service.getXpMilestoneHistory(1L);

        assertTrue(achievements.get(0).achieved); // 100 threshold
        assertNotNull(achievements.get(0).achievedAt);
    }

    @Test
    void getXpMilestoneHistory_percentToNext() {
        AiAdminHealthReport report = new AiAdminHealthReport();
        report.setGeneratedAt(LocalDateTime.now());
        report.setXpEarned(750);

        when(reportRepository.findByAiAdminIdOrderByGeneratedAtDesc(1L))
                .thenReturn(List.of(report));

        List<AiAdminXpTrendService.XpMilestoneAchievement> achievements = service.getXpMilestoneHistory(1L);

        // 750/1000 = 75% to next milestone (1000)
        assertEquals(75.0, achievements.get(2).percentToNext, 0.1);
        // 100 and 500 are achieved, so 100%
        assertEquals(100.0, achievements.get(0).percentToNext, 0.1);
        assertEquals(100.0, achievements.get(1).percentToNext, 0.1);
    }
}
