package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.service.AiAdminXpTrendService;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiAdminControllerXpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AiAdminXpTrendService xpTrendService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AiAdminController controller;

    private User testUser;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("admin");
        testUser.setRole("ADMIN");

        testPlayer = new Player();
        testPlayer.setUsername("admin");
        testPlayer.setAdmin(true);

        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(playerRepository.findByUsername("admin")).thenReturn(Optional.of(testPlayer));
    }

    // ---------------------------------------------------------------
    // XP Trend endpoint tests
    // ---------------------------------------------------------------

    @Test
    void getXpTrend_returnsTrendData() {
        AiAdminXpTrendService.XpTrendPoint point = new AiAdminXpTrendService.XpTrendPoint();
        point.date = LocalDate.now();
        point.xpEarned = 100;
        point.ordersCompleted = 10;
        point.incidentsHandled = 2;

        when(xpTrendService.getXpTrend(1L, 30)).thenReturn(List.of(point));

        ResponseEntity<?> response = controller.getXpTrend(1L, 30, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, list.size());
        assertEquals(100, list.get(0).get("xpEarned"));
    }

    @Test
    void getXpTrend_forbiddenForNonAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getXpTrend(1L, 30, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // XP Velocity endpoint tests
    // ---------------------------------------------------------------

    @Test
    void getXpVelocity_returnsVelocityData() {
        AiAdminXpTrendService.XpVelocity velocity = new AiAdminXpTrendService.XpVelocity();
        velocity.adminId = 1L;
        velocity.totalXp = 500;
        velocity.totalReports = 5;
        velocity.xpPerDay = 100.0;
        velocity.xpPerWeek = 700.0;
        velocity.xpPerReport = 100.0;
        velocity.firstReportDate = LocalDateTime.now().minusDays(7);
        velocity.lastReportDate = LocalDateTime.now();

        when(xpTrendService.getXpVelocity(1L)).thenReturn(velocity);

        ResponseEntity<?> response = controller.getXpVelocity(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertEquals(500, map.get("totalXp"));
        assertEquals(100.0, map.get("xpPerDay"));
        assertEquals(700.0, map.get("xpPerWeek"));
    }

    @Test
    void getXpVelocity_forbiddenForNonAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getXpVelocity(1L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // XP Projection endpoint tests
    // ---------------------------------------------------------------

    @Test
    void getXpProjection_returnsProjectionData() {
        AiAdminXpTrendService.XpProjection projection = new AiAdminXpTrendService.XpProjection();
        projection.adminId = 1L;
        projection.currentXp = 800;
        projection.xpPerDay = 100.0;
        projection.nextMilestone = 1000;
        projection.xpRemaining = 200;
        projection.estimatedDaysRemaining = 2;
        projection.allMilestonesAchieved = false;

        when(xpTrendService.getXpProjection(1L)).thenReturn(projection);

        ResponseEntity<?> response = controller.getXpProjection(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertEquals(1000, map.get("nextMilestone"));
        assertEquals(200, map.get("xpRemaining"));
        assertEquals(2, map.get("estimatedDaysRemaining"));
    }

    @Test
    void getXpProjection_allMilestonesAchieved() {
        AiAdminXpTrendService.XpProjection projection = new AiAdminXpTrendService.XpProjection();
        projection.adminId = 1L;
        projection.currentXp = 150000;
        projection.nextMilestone = -1;
        projection.allMilestonesAchieved = true;

        when(xpTrendService.getXpProjection(1L)).thenReturn(projection);

        ResponseEntity<?> response = controller.getXpProjection(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) map.get("allMilestonesAchieved"));
    }

    // ---------------------------------------------------------------
    // XP Source Breakdown endpoint tests
    // ---------------------------------------------------------------

    @Test
    void getXpSourceBreakdown_returnsBreakdown() {
        AiAdminXpTrendService.XpSourceBreakdown breakdown = new AiAdminXpTrendService.XpSourceBreakdown();
        breakdown.adminId = 1L;
        breakdown.days = 30;
        breakdown.totalXp = 350;
        breakdown.xpFromOrders = 100;
        breakdown.xpFromIncidents = 250;
        breakdown.ordersCompleted = 10;
        breakdown.incidentsResolved = 10;
        breakdown.ordersPercent = 28.57;
        breakdown.incidentsPercent = 71.43;

        when(xpTrendService.getXpSourceBreakdown(1L, 30)).thenReturn(breakdown);

        ResponseEntity<?> response = controller.getXpSourceBreakdown(1L, 30, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertEquals(350, map.get("totalXp"));
        assertEquals(100, map.get("xpFromOrders"));
        assertEquals(250, map.get("xpFromIncidents"));
    }

    @Test
    void getXpSourceBreakdown_forbiddenForNonAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getXpSourceBreakdown(1L, 30, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // Admin XP Comparison endpoint tests
    // ---------------------------------------------------------------

    @Test
    void getAdminXpComparison_returnsComparison() {
        AiAdminXpTrendService.AdminXpComparison comparison = new AiAdminXpTrendService.AdminXpComparison();
        comparison.adminId = 1L;
        comparison.adminName = "Admin 1";
        comparison.empire = "Empire 1";
        comparison.role = "Galaxy Overseer";
        comparison.totalExperience = 5000;
        comparison.xpEarned = 500;
        comparison.ordersCompleted = 50;
        comparison.incidentsHandled = 10;
        comparison.reportsGenerated = 5;

        when(xpTrendService.getAdminXpComparison(30)).thenReturn(List.of(comparison));

        ResponseEntity<?> response = controller.getAdminXpComparison(30, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, list.size());
        assertEquals("Admin 1", list.get(0).get("adminName"));
        assertEquals(500, list.get(0).get("xpEarned"));
    }

    @Test
    void getAdminXpComparison_forbiddenForNonAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getAdminXpComparison(30, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // XP Milestone History endpoint tests
    // ---------------------------------------------------------------

    @Test
    void getXpMilestoneHistory_returnsMilestones() {
        AiAdminXpTrendService.XpMilestoneAchievement achievement = new AiAdminXpTrendService.XpMilestoneAchievement();
        achievement.threshold = 100;
        achievement.achieved = true;
        achievement.cumulativeXp = 500;
        achievement.percentToNext = 100.0;
        achievement.achievedAt = LocalDateTime.now().minusDays(5);

        when(xpTrendService.getXpMilestoneHistory(1L)).thenReturn(List.of(achievement));

        ResponseEntity<?> response = controller.getXpMilestoneHistory(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, list.size());
        assertEquals(100, list.get(0).get("threshold"));
        assertTrue((Boolean) list.get(0).get("achieved"));
    }

    @Test
    void getXpMilestoneHistory_forbiddenForNonAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getXpMilestoneHistory(1L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
