package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.earth2026.service.Earth2026RulesDatabaseService;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Galaxy Overseer Service Tests")
class GalaxyOverseerServiceTest {

    @Mock
    private GalaxyRepository galaxyRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private Earth2026RulesDatabaseService rulesDatabase;

    @Mock
    private AiAdminService aiAdminService;

    @Mock
    private GalaxyEventService galaxyEventService;

    @Mock
    private GalaxyMetricsService galaxyMetricsService;

    @InjectMocks
    private GalaxyOverseerService galaxyOverseerService;

    // ---- Galaxy Overseer creation tests ----

    @Nested
    @DisplayName("Galaxy Overseer Creation")
    class GalaxyOverseerCreationTests {

        @Test
        @DisplayName("should create new Galaxy Overseer when none exists")
        void createOrGetGalaxyOverseer_shouldCreateNewWhenNoneExists() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        ai.setId(100L);
                        return ai;
                    });
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter overseer = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertNotNull(overseer);
            assertEquals("Galaxy Overseer", overseer.getRole());
            assertTrue(overseer.isAiAdmin());
            assertTrue(overseer.isActive());
            assertTrue(overseer.isCanPromoteToCoServer());
            assertFalse(overseer.isHirable());
            assertEquals(1000, overseer.getDailySalary());
            assertEquals(100L, galaxy.getGalaxyOverseerId());
            verify(aiCharacterRepository).save(overseer);
            verify(galaxyRepository).save(galaxy);
        }

        @Test
        @DisplayName("should return existing overseer if galaxy already has one assigned")
        void createOrGetGalaxyOverseer_shouldReturnExistingWhenAssigned() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            galaxy.setGalaxyOverseerId(50L);

            AiCharacter existingOverseer = new AiCharacter("Galaxy Overseer Test Galaxy", "Galaxy Overseer", "Test Galaxy");
            existingOverseer.setId(50L);
            existingOverseer.setActive(true);
            existingOverseer.setAiAdmin(true);

            when(aiCharacterRepository.findById(50L)).thenReturn(Optional.of(existingOverseer));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertSame(existingOverseer, result);
            verify(aiCharacterRepository, never()).save(any(AiCharacter.class));
            verify(galaxyRepository, never()).save(any(Galaxy.class));
        }

        @Test
        @DisplayName("should reuse existing Galaxy Overseer from pool with experience retained")
        void createOrGetGalaxyOverseer_shouldReuseFromPoolWithExperience() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setAiAdmin(true);
            experiencedOverseer.setTotalExperience(500);
            experiencedOverseer.setLeadershipRating(9);
            experiencedOverseer.setTotalBattles(50);
            experiencedOverseer.setTotalVictories(40);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertSame(experiencedOverseer, result);
            assertEquals("New Galaxy", result.getEmpireName());
            assertEquals(500, result.getTotalExperience(), "Experience should be retained");
            assertEquals(9, result.getLeadershipRating(), "Leadership should be retained");
            assertEquals(50, result.getTotalBattles(), "Battles should be retained");
            assertEquals(40, result.getTotalVictories(), "Victories should be retained");
            assertEquals(200L, galaxy.getGalaxyOverseerId());
            verify(aiCharacterRepository).save(experiencedOverseer);
            verify(galaxyRepository).save(galaxy);
        }

        @Test
        @DisplayName("should pick highest XP Galaxy Overseer from pool")
        void createOrGetGalaxyOverseer_shouldPickHighestXpFromPool() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter novice = new AiCharacter("Novice Overseer", "Galaxy Overseer", "Galaxy A");
            novice.setId(100L);
            novice.setActive(true);
            novice.setTotalExperience(50);

            AiCharacter veteran = new AiCharacter("Veteran Overseer", "Galaxy Overseer", "Galaxy B");
            veteran.setId(200L);
            veteran.setActive(true);
            veteran.setTotalExperience(500);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(veteran, novice));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertSame(veteran, result);
            assertEquals(500, result.getTotalExperience());
        }

        @Test
        @DisplayName("should update empire name and description on reuse")
        void createOrGetGalaxyOverseer_shouldUpdateEmpireAndDescriptionOnReuse() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Andromeda");

            AiCharacter existingOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            existingOverseer.setId(300L);
            existingOverseer.setActive(true);
            existingOverseer.setDescription("Old description");

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(existingOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository).save(captor.capture());
            AiCharacter saved = captor.getValue();
            assertEquals("Andromeda", saved.getEmpireName());
            assertTrue(saved.getDescription().contains("Andromeda"));
            assertTrue(saved.getDescription().contains("oversees the entire AI workforce"));
        }
    }

    // ---- Get Galaxy Overseer tests ----

    @Nested
    @DisplayName("Get Galaxy Overseer")
    class GetGalaxyOverseerTests {

        @Test
        @DisplayName("should get overseer by galaxy ID")
        void getGalaxyOverseer_byId_shouldReturnOverseer() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            galaxy.setGalaxyOverseerId(100L);

            AiCharacter overseer = new AiCharacter("Galaxy Overseer Test Galaxy", "Galaxy Overseer", "Test Galaxy");
            overseer.setId(100L);

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(overseer));

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer(1L);

            // Assert
            assertTrue(result.isPresent());
            assertSame(overseer, result.get());
        }

        @Test
        @DisplayName("should get overseer by galaxy name")
        void getGalaxyOverseer_byName_shouldReturnOverseer() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Milky Way");
            galaxy.setGalaxyOverseerId(100L);

            AiCharacter overseer = new AiCharacter("Galaxy Overseer Milky Way", "Galaxy Overseer", "Milky Way");
            overseer.setId(100L);

            when(galaxyRepository.findByName("Milky Way")).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(overseer));

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer("Milky Way");

            // Assert
            assertTrue(result.isPresent());
            assertSame(overseer, result.get());
        }

        @Test
        @DisplayName("should return empty if galaxy has no overseer assigned")
        void getGalaxyOverseer_shouldReturnEmptyWhenNoOverseer() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Empty Galaxy");
            // No overseer assigned

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer(1L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty if galaxy not found")
        void getGalaxyOverseer_shouldReturnEmptyWhenGalaxyNotFound() {
            // Arrange
            when(galaxyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- Workforce monitoring tests ----

    @Nested
    @DisplayName("Workforce Monitoring")
    class WorkforceMonitoringTests {

        @Test
        @DisplayName("should generate workforce report with correct counts")
        void monitorWorkforce_shouldGenerateCorrectReport() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");

            AiCharacter galaxyOverseer = new AiCharacter("Galaxy Overseer", "Galaxy Overseer", "Test Galaxy");
            galaxyOverseer.setAiAdmin(true);
            galaxyOverseer.setActive(true);
            galaxyOverseer.setTotalExperience(500);

            AiCharacter overseer1 = new AiCharacter("Planet Overseer 1", "Overseer", "Test Galaxy");
            overseer1.setAiAdmin(true);
            overseer1.setActive(true);
            overseer1.setTotalExperience(200);

            AiCharacter leader1 = new AiCharacter("Country Leader 1", "Leader", "Test Galaxy");
            leader1.setAiAdmin(true);
            leader1.setActive(true);
            leader1.setTotalExperience(100);

            AiCharacter governor1 = new AiCharacter("Region Governor 1", "Governor", "Test Galaxy");
            governor1.setAiAdmin(true);
            governor1.setActive(true);
            governor1.setTotalExperience(50);

            List<AiCharacter> allAdmins = List.of(galaxyOverseer, overseer1, leader1, governor1);

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Test Galaxy"))
                    .thenReturn(allAdmins);

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertEquals(1L, report.getGalaxyId());
            assertEquals("Test Galaxy", report.getGalaxyName());
            assertEquals(4, report.getTotalAiAgents());
            assertEquals(1, report.getGalaxyOverseerCount());
            assertEquals(1, report.getPlanetOverseerCount());
            assertEquals(1, report.getLeaderCount());
            assertEquals(1, report.getGovernorCount());
            assertEquals(0, report.getOtherCount());
            assertEquals(850, report.getTotalWorkforceExperience());
        }

        @Test
        @DisplayName("should detect overstaffed workforce")
        void monitorWorkforce_shouldDetectOverstaffed() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Crowded Galaxy");
            // 1 solar system = 1 estimated planet, max = 15 AI agents
            galaxy.setSolarSystems(new java.util.ArrayList<>());
            galaxy.getSolarSystems().add(new com.yisy.modules.doramcu.model.SolarSystem());

            // Create 20 AI admins — exceeds the 15 max for 1 planet
            List<AiCharacter> allAdmins = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                AiCharacter ai = new AiCharacter("AI " + i, "Leader", "Crowded Galaxy");
                ai.setAiAdmin(true);
                ai.setActive(true);
                allAdmins.add(ai);
            }

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Crowded Galaxy"))
                    .thenReturn(allAdmins);

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertTrue(report.isOverstaffed());
            assertEquals(20, report.getTotalAiAgents());
            assertEquals(15, report.getRecommendedMaxAgents());
            assertTrue(report.getRecommendation().contains("exceeds"));
            assertTrue(report.getRecommendation().contains("5"));
        }

        @Test
        @DisplayName("should report workforce within limits when under threshold")
        void monitorWorkforce_shouldReportWithinLimits() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Sparse Galaxy");
            galaxy.setSolarSystems(new java.util.ArrayList<>());
            galaxy.getSolarSystems().add(new com.yisy.modules.doramcu.model.SolarSystem());

            // 5 AI admins — well under the 15 max for 1 planet
            List<AiCharacter> allAdmins = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                AiCharacter ai = new AiCharacter("AI " + i, "Leader", "Sparse Galaxy");
                ai.setAiAdmin(true);
                ai.setActive(true);
                allAdmins.add(ai);
            }

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Sparse Galaxy"))
                    .thenReturn(allAdmins);

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertFalse(report.isOverstaffed());
            assertEquals(5, report.getTotalAiAgents());
            assertEquals(15, report.getRecommendedMaxAgents());
            assertTrue(report.getRecommendation().contains("within"));
            assertTrue(report.getRecommendation().contains("10"));
        }

        @Test
        @DisplayName("should throw exception when galaxy not found")
        void monitorWorkforce_shouldThrowWhenGalaxyNotFound() {
            // Arrange
            when(galaxyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    galaxyOverseerService.monitorWorkforce(999L));
        }

        @Test
        @DisplayName("should calculate average experience correctly")
        void monitorWorkforce_shouldCalculateAverageExperience() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");

            AiCharacter ai1 = new AiCharacter("AI One", "Leader", "Test Galaxy");
            ai1.setAiAdmin(true);
            ai1.setActive(true);
            ai1.setTotalExperience(300);

            AiCharacter ai2 = new AiCharacter("AI Two", "Governor", "Test Galaxy");
            ai2.setAiAdmin(true);
            ai2.setActive(true);
            ai2.setTotalExperience(100);

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Test Galaxy"))
                    .thenReturn(List.of(ai1, ai2));

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertEquals(400, report.getTotalWorkforceExperience());
            assertEquals(200.0, report.getAverageExperience(), 0.01);
        }

        @Test
        @DisplayName("should handle empty workforce")
        void monitorWorkforce_shouldHandleEmptyWorkforce() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Empty Galaxy");

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Empty Galaxy"))
                    .thenReturn(new ArrayList<>());

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertEquals(0, report.getTotalAiAgents());
            assertEquals(0, report.getGalaxyOverseerCount());
            assertEquals(0, report.getPlanetOverseerCount());
            assertEquals(0, report.getLeaderCount());
            assertEquals(0, report.getGovernorCount());
            assertEquals(0, report.getTotalWorkforceExperience());
            assertEquals(0.0, report.getAverageExperience(), 0.01);
            assertFalse(report.isOverstaffed());
        }

        @Test
        @DisplayName("should count other roles correctly")
        void monitorWorkforce_shouldCountOtherRoles() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");

            AiCharacter custom = new AiCharacter("Custom AI", "Custom Role", "Test Galaxy");
            custom.setAiAdmin(true);
            custom.setActive(true);

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Test Galaxy"))
                    .thenReturn(List.of(custom));

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertEquals(1, report.getTotalAiAgents());
            assertEquals(1, report.getOtherCount());
        }
    }

    // ---- Workforce Report DTO tests ----

    @Nested
    @DisplayName("Workforce Report DTO")
    class WorkforceReportDtoTests {

        @Test
        @DisplayName("should have correct toString output")
        void workforceReport_toString_shouldContainKeyFields() {
            // Arrange
            GalaxyOverseerService.WorkforceReport report = new GalaxyOverseerService.WorkforceReport();
            report.setGalaxyName("Test Galaxy");
            report.setTotalAiAgents(10);
            report.setIsOverstaffed(true);
            report.setRecommendation("Reduce workforce");

            // Act
            String str = report.toString();

            // Assert
            assertTrue(str.contains("Test Galaxy"));
            assertTrue(str.contains("10"));
            assertTrue(str.contains("true"));
            assertTrue(str.contains("Reduce workforce"));
        }

        @Test
        @DisplayName("should support all getter/setter pairs")
        void workforceReport_shouldSupportAllFields() {
            // Arrange & Act
            GalaxyOverseerService.WorkforceReport report = new GalaxyOverseerService.WorkforceReport();
            report.setGalaxyId(1L);
            report.setGalaxyName("Test");
            report.setTotalAiAgents(5);
            report.setGalaxyOverseerCount(1);
            report.setPlanetOverseerCount(2);
            report.setLeaderCount(3);
            report.setGovernorCount(4);
            report.setOtherCount(5);
            report.setEstimatedPlanets(10);
            report.setRecommendedMaxAgents(150);
            report.setIsOverstaffed(false);
            report.setTotalWorkforceExperience(1000);
            report.setAverageExperience(200.5);
            report.setRecommendation("All good");

            // Assert
            assertEquals(1L, report.getGalaxyId());
            assertEquals("Test", report.getGalaxyName());
            assertEquals(5, report.getTotalAiAgents());
            assertEquals(1, report.getGalaxyOverseerCount());
            assertEquals(2, report.getPlanetOverseerCount());
            assertEquals(3, report.getLeaderCount());
            assertEquals(4, report.getGovernorCount());
            assertEquals(5, report.getOtherCount());
            assertEquals(10, report.getEstimatedPlanets());
            assertEquals(150, report.getRecommendedMaxAgents());
            assertFalse(report.isOverstaffed());
            assertEquals(1000, report.getTotalWorkforceExperience());
            assertEquals(200.5, report.getAverageExperience(), 0.01);
            assertEquals("All good", report.getRecommendation());
        }
    }

    // ---- Cross-galaxy overseer reuse tests ----

    @Nested
    @DisplayName("Cross-Galaxy Overseer Reuse")
    class CrossGalaxyOverseerReuseTests {

        @Test
        @DisplayName("should reuse Galaxy Overseer across galaxy creations")
        void createOrGetGalaxyOverseer_shouldReuseAcrossGalaxies() {
            // Arrange: First galaxy creates the overseer
            Galaxy galaxy1 = new Galaxy();
            galaxy1.setId(1L);
            galaxy1.setName("Galaxy Alpha");

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(100L);
                        return ai;
                    });
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Create overseer for first galaxy
            AiCharacter overseer1 = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy1);
            assertEquals(100L, overseer1.getId());

            // Simulate overseer gaining experience
            overseer1.setTotalExperience(300);
            overseer1.setLeadershipRating(8);
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Second galaxy should reuse the experienced overseer
            Galaxy galaxy2 = new Galaxy();
            galaxy2.setId(2L);
            galaxy2.setName("Galaxy Beta");

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(overseer1));

            AiCharacter overseer2 = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy2);

            // Assert
            assertSame(overseer1, overseer2);
            assertEquals("Galaxy Beta", overseer2.getEmpireName());
            assertEquals(300, overseer2.getTotalExperience(), "Experience retained on reuse");
            assertEquals(8, overseer2.getLeadershipRating(), "Leadership retained on reuse");
        }
    }
}
