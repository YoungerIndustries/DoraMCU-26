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
@DisplayName("Galaxy Overseer Persistence Tests")
class GalaxyOverseerPersistenceTest {

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

    // ---- New Galaxy Overseer Initialization ----

    @Nested
    @DisplayName("New Galaxy Overseer Initialization")
    class NewOverseerInitializationTests {

        @Test
        @DisplayName("should set canPromoteToCoServer to true on new Galaxy Overseer")
        void createNew_shouldSetCanPromoteToCoServerTrue() {
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
            assertTrue(overseer.isCanPromoteToCoServer(), "New Galaxy Overseer should be promotable to co-server");
        }

        @Test
        @DisplayName("should set active to true on new Galaxy Overseer")
        void createNew_shouldSetActiveTrue() {
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
            assertTrue(overseer.isActive(), "New Galaxy Overseer should be active");
        }

        @Test
        @DisplayName("should set default leadershipRating to 5 on new Galaxy Overseer")
        void createNew_shouldSetDefaultLeadershipRating() {
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
            assertEquals(5, overseer.getLeadershipRating(), "New Galaxy Overseer should have default leadership rating of 5");
        }

        @Test
        @DisplayName("should set zero experience on new Galaxy Overseer")
        void createNew_shouldSetZeroExperience() {
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
            assertEquals(0, overseer.getTotalExperience(), "New Galaxy Overseer should have zero XP");
            assertEquals(0, overseer.getTotalBattles(), "New Galaxy Overseer should have zero battles");
            assertEquals(0, overseer.getTotalVictories(), "New Galaxy Overseer should have zero victories");
        }

        @Test
        @DisplayName("should set hirable to false on new Galaxy Overseer")
        void createNew_shouldSetHirableFalse() {
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
            assertFalse(overseer.isHirable(), "New Galaxy Overseer should not be hirable");
        }

        @Test
        @DisplayName("should set daily salary to 1000 on new Galaxy Overseer")
        void createNew_shouldSetDailySalary() {
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
            assertEquals(1000, overseer.getDailySalary(), "New Galaxy Overseer should have salary of 1000");
        }

        @Test
        @DisplayName("should set aiAdmin to true on new Galaxy Overseer")
        void createNew_shouldSetAiAdminTrue() {
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
            assertTrue(overseer.isAiAdmin(), "New Galaxy Overseer should be marked as AI admin");
        }

        @Test
        @DisplayName("should set role to Galaxy Overseer on new character")
        void createNew_shouldSetRole() {
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
            assertEquals("Galaxy Overseer", overseer.getRole());
        }
    }

    // ---- Experience Field Preservation on Reuse ----

    @Nested
    @DisplayName("Experience Field Preservation on Reuse")
    class ExperiencePreservationTests {

        @Test
        @DisplayName("should preserve totalExperience when reusing Galaxy Overseer")
        void reuse_shouldPreserveTotalExperience() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setAiAdmin(true);
            experiencedOverseer.setTotalExperience(750);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(750, result.getTotalExperience(), "Total experience should be preserved on reuse");
        }

        @Test
        @DisplayName("should preserve leadershipRating when reusing Galaxy Overseer")
        void reuse_shouldPreserveLeadershipRating() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setLeadershipRating(9);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(9, result.getLeadershipRating(), "Leadership rating should be preserved on reuse");
        }

        @Test
        @DisplayName("should preserve totalBattles when reusing Galaxy Overseer")
        void reuse_shouldPreserveTotalBattles() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setTotalBattles(85);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(85, result.getTotalBattles(), "Total battles should be preserved on reuse");
        }

        @Test
        @DisplayName("should preserve totalVictories when reusing Galaxy Overseer")
        void reuse_shouldPreserveTotalVictories() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setTotalVictories(70);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(70, result.getTotalVictories(), "Total victories should be preserved on reuse");
        }

        @Test
        @DisplayName("should preserve hirable flag when reusing Galaxy Overseer")
        void reuse_shouldPreserveHirable() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setHirable(false);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertFalse(result.isHirable(), "Hirable flag should be preserved on reuse");
        }

        @Test
        @DisplayName("should preserve dailySalary when reusing Galaxy Overseer")
        void reuse_shouldPreserveDailySalary() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setDailySalary(1500);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(1500, result.getDailySalary(), "Daily salary should be preserved on reuse");
        }

        @Test
        @DisplayName("should preserve all experience fields together when reusing Galaxy Overseer")
        void reuse_shouldPreserveAllExperienceFields() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter experiencedOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            experiencedOverseer.setId(200L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setAiAdmin(true);
            experiencedOverseer.setTotalExperience(1200);
            experiencedOverseer.setLeadershipRating(10);
            experiencedOverseer.setTotalBattles(120);
            experiencedOverseer.setTotalVictories(100);
            experiencedOverseer.setHirable(false);
            experiencedOverseer.setDailySalary(2000);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(1200, result.getTotalExperience());
            assertEquals(10, result.getLeadershipRating());
            assertEquals(120, result.getTotalBattles());
            assertEquals(100, result.getTotalVictories());
            assertFalse(result.isHirable());
            assertEquals(2000, result.getDailySalary());
        }
    }

    // ---- Assigned Overseer Guard Tests ----

    @Nested
    @DisplayName("Assigned Overseer Guard")
    class AssignedOverseerGuardTests {

        @Test
        @DisplayName("should not reassign if galaxy already has overseer ID and character exists")
        void createOrGet_shouldNotReassignWhenAlreadyAssigned() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            galaxy.setGalaxyOverseerId(50L);

            AiCharacter existingOverseer = new AiCharacter("Galaxy Overseer Test Galaxy", "Galaxy Overseer", "Test Galaxy");
            existingOverseer.setId(50L);
            existingOverseer.setActive(true);
            existingOverseer.setAiAdmin(true);
            existingOverseer.setTotalExperience(500);

            when(aiCharacterRepository.findById(50L)).thenReturn(Optional.of(existingOverseer));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertSame(existingOverseer, result);
            verify(aiCharacterRepository, never()).save(any(AiCharacter.class));
            verify(galaxyRepository, never()).save(any(Galaxy.class));
        }

        @Test
        @DisplayName("should return existing overseer without modifying experience")
        void createOrGet_shouldReturnExistingWithoutModifyingExperience() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            galaxy.setGalaxyOverseerId(50L);

            AiCharacter existingOverseer = new AiCharacter("Galaxy Overseer Test Galaxy", "Galaxy Overseer", "Test Galaxy");
            existingOverseer.setId(50L);
            existingOverseer.setTotalExperience(999);
            existingOverseer.setLeadershipRating(10);

            when(aiCharacterRepository.findById(50L)).thenReturn(Optional.of(existingOverseer));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertEquals(999, result.getTotalExperience(), "Experience should not be modified");
            assertEquals(10, result.getLeadershipRating(), "Leadership should not be modified");
        }

        @Test
        @DisplayName("should fall through to pool if assigned overseer ID points to missing character")
        void createOrGet_shouldFallThroughWhenAssignedOverseerMissing() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            galaxy.setGalaxyOverseerId(999L); // Points to non-existent character

            AiCharacter poolOverseer = new AiCharacter("Galaxy Overseer Other Galaxy", "Galaxy Overseer", "Other Galaxy");
            poolOverseer.setId(100L);
            poolOverseer.setActive(true);
            poolOverseer.setAiAdmin(true);
            poolOverseer.setTotalExperience(300);

            when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(poolOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertSame(poolOverseer, result);
            assertEquals(300, result.getTotalExperience(), "Should reuse pool overseer with experience");
            assertEquals(100L, galaxy.getGalaxyOverseerId(), "Galaxy should be updated to point to pool overseer");
        }
    }

    // ---- Cross-Galaxy Overseer Reuse ----

    @Nested
    @DisplayName("Cross-Galaxy Overseer Reuse")
    class CrossGalaxyOverseerReuseTests {

        @Test
        @DisplayName("should reuse same Galaxy Overseer across multiple galaxy creations")
        void createOrGet_shouldReuseAcrossMultipleGalaxies() {
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
            assertEquals(0, overseer1.getTotalExperience());

            // Simulate overseer gaining experience after managing Galaxy Alpha
            overseer1.setTotalExperience(500);
            overseer1.setLeadershipRating(9);
            overseer1.setTotalBattles(30);
            overseer1.setTotalVictories(25);

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
            assertEquals(500, overseer2.getTotalExperience());
            assertEquals(9, overseer2.getLeadershipRating());
            assertEquals(30, overseer2.getTotalBattles());
            assertEquals(25, overseer2.getTotalVictories());

            // Third galaxy should also reuse the same overseer
            Galaxy galaxy3 = new Galaxy();
            galaxy3.setId(3L);
            galaxy3.setName("Galaxy Gamma");

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(overseer1));

            AiCharacter overseer3 = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy3);

            assertSame(overseer1, overseer3);
            assertEquals("Galaxy Gamma", overseer3.getEmpireName());
            assertEquals(500, overseer3.getTotalExperience(), "Experience should still be retained after third reuse");
        }

        @Test
        @DisplayName("should select highest XP overseer when multiple exist across galaxies")
        void createOrGet_shouldSelectHighestXpOverseer() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter novice = new AiCharacter("Novice Overseer", "Galaxy Overseer", "Galaxy A");
            novice.setId(100L);
            novice.setActive(true);
            novice.setTotalExperience(50);

            AiCharacter expert = new AiCharacter("Expert Overseer", "Galaxy Overseer", "Galaxy B");
            expert.setId(200L);
            expert.setActive(true);
            expert.setTotalExperience(300);

            AiCharacter veteran = new AiCharacter("Veteran Overseer", "Galaxy Overseer", "Galaxy C");
            veteran.setId(300L);
            veteran.setActive(true);
            veteran.setTotalExperience(800);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(veteran, expert, novice));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            AiCharacter result = galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            assertSame(veteran, result);
            assertEquals(800, result.getTotalExperience());
        }

        @Test
        @DisplayName("should update empire name on each reuse to reflect new galaxy")
        void createOrGet_shouldUpdateEmpireNameOnReuse() {
            // Arrange
            AiCharacter overseer = new AiCharacter("Galaxy Overseer Alpha", "Galaxy Overseer", "Galaxy Alpha");
            overseer.setId(100L);
            overseer.setActive(true);
            overseer.setAiAdmin(true);
            overseer.setTotalExperience(400);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(overseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Reuse for Galaxy Beta
            Galaxy galaxyBeta = new Galaxy();
            galaxyBeta.setId(2L);
            galaxyBeta.setName("Galaxy Beta");
            galaxyOverseerService.createOrGetGalaxyOverseer(galaxyBeta);
            assertEquals("Galaxy Beta", overseer.getEmpireName());

            // Reuse for Galaxy Gamma
            Galaxy galaxyGamma = new Galaxy();
            galaxyGamma.setId(3L);
            galaxyGamma.setName("Galaxy Gamma");
            galaxyOverseerService.createOrGetGalaxyOverseer(galaxyGamma);
            assertEquals("Galaxy Gamma", overseer.getEmpireName());
        }
    }

    // ---- Description Update on Reuse ----

    @Nested
    @DisplayName("Description Update on Reuse")
    class DescriptionUpdateTests {

        @Test
        @DisplayName("should update description to reflect new galaxy on reuse")
        void reuse_shouldUpdateDescription() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Andromeda");

            AiCharacter existingOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            existingOverseer.setId(300L);
            existingOverseer.setActive(true);
            existingOverseer.setDescription("Old description for Old Galaxy");

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

        @Test
        @DisplayName("should set aiAdmin flag on reused overseer")
        void reuse_shouldSetAiAdminFlag() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("New Galaxy");

            AiCharacter existingOverseer = new AiCharacter("Galaxy Overseer Old Galaxy", "Galaxy Overseer", "Old Galaxy");
            existingOverseer.setId(300L);
            existingOverseer.setActive(true);
            existingOverseer.setAiAdmin(false); // Not set as admin

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Galaxy Overseer"))
                    .thenReturn(List.of(existingOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(galaxyRepository.save(any(Galaxy.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            galaxyOverseerService.createOrGetGalaxyOverseer(galaxy);

            // Assert
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository).save(captor.capture());
            assertTrue(captor.getValue().isAiAdmin(), "aiAdmin should be set to true on reuse");
        }
    }

    // ---- Get Galaxy Overseer Edge Cases ----

    @Nested
    @DisplayName("Get Galaxy Overseer Edge Cases")
    class GetOverseerEdgeCaseTests {

        @Test
        @DisplayName("should return empty when galaxy exists but overseer ID is null")
        void getOverseer_shouldReturnEmptyWhenOverseerIdNull() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Empty Galaxy");
            // galaxyOverseerId is null

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer(1L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty when galaxy not found by ID")
        void getOverseer_shouldReturnEmptyWhenGalaxyNotFoundById() {
            // Arrange
            when(galaxyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer(999L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty when galaxy not found by name")
        void getOverseer_shouldReturnEmptyWhenGalaxyNotFoundByName() {
            // Arrange
            when(galaxyRepository.findByName("Nonexistent Galaxy")).thenReturn(Optional.empty());

            // Act
            Optional<AiCharacter> result = galaxyOverseerService.getGalaxyOverseer("Nonexistent Galaxy");

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return overseer when found by galaxy name")
        void getOverseer_shouldReturnOverseerByName() {
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
    }

    // ---- Workforce Monitoring Edge Cases ----

    @Nested
    @DisplayName("Workforce Monitoring Edge Cases")
    class WorkforceMonitoringEdgeCaseTests {

        @Test
        @DisplayName("should return 0 estimated planets when solar systems list is empty")
        void monitorWorkforce_shouldReturnZeroPlanetsWhenEmpty() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            // solarSystems initialized as empty ArrayList by default in Galaxy entity (never null)

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Test Galaxy"))
                    .thenReturn(new ArrayList<>());

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            // Galaxy.solarSystems is new ArrayList<>() so size is 0; null check passes, returns 0
            assertEquals(0, report.getEstimatedPlanets());
            assertEquals(0, report.getRecommendedMaxAgents()); // 0 * 15
        }

        @Test
        @DisplayName("should calculate recommended max based on solar system count")
        void monitorWorkforce_shouldCalculateMaxBasedOnSolarSystems() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");
            galaxy.setSolarSystems(new java.util.ArrayList<>());
            for (int i = 0; i < 3; i++) {
                galaxy.getSolarSystems().add(new com.yisy.modules.doramcu.model.SolarSystem());
            }

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Test Galaxy"))
                    .thenReturn(new ArrayList<>());

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertEquals(3, report.getEstimatedPlanets());
            assertEquals(45, report.getRecommendedMaxAgents()); // 3 * 15
        }

        @Test
        @DisplayName("should detect overstaffing when AI count exceeds threshold")
        void monitorWorkforce_shouldDetectOverstaffing() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Crowded Galaxy");
            galaxy.setSolarSystems(new java.util.ArrayList<>());
            galaxy.getSolarSystems().add(new com.yisy.modules.doramcu.model.SolarSystem());

            // 20 AI admins for 1 planet (max 15)
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
            assertTrue(report.getRecommendation().contains("5")); // excess of 5
        }

        @Test
        @DisplayName("should report within limits when under threshold")
        void monitorWorkforce_shouldReportWithinLimits() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Sparse Galaxy");
            galaxy.setSolarSystems(new java.util.ArrayList<>());
            galaxy.getSolarSystems().add(new com.yisy.modules.doramcu.model.SolarSystem());

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
            assertTrue(report.getRecommendation().contains("10")); // 10 remaining slots
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when galaxy not found")
        void monitorWorkforce_shouldThrowWhenGalaxyNotFound() {
            // Arrange
            when(galaxyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    galaxyOverseerService.monitorWorkforce(999L));
        }

        @Test
        @DisplayName("should handle empty workforce gracefully")
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
        @DisplayName("should count all role types correctly")
        void monitorWorkforce_shouldCountAllRoleTypes() {
            // Arrange
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName("Test Galaxy");

            AiCharacter galaxyOverseer = new AiCharacter("Galaxy Overseer", "Galaxy Overseer", "Test Galaxy");
            galaxyOverseer.setAiAdmin(true);
            galaxyOverseer.setActive(true);

            AiCharacter overseer1 = new AiCharacter("Planet Overseer 1", "Overseer", "Test Galaxy");
            overseer1.setAiAdmin(true);
            overseer1.setActive(true);

            AiCharacter overseer2 = new AiCharacter("Planet Overseer 2", "Overseer", "Test Galaxy");
            overseer2.setAiAdmin(true);
            overseer2.setActive(true);

            AiCharacter leader1 = new AiCharacter("Country Leader 1", "Leader", "Test Galaxy");
            leader1.setAiAdmin(true);
            leader1.setActive(true);

            AiCharacter leader2 = new AiCharacter("Country Leader 2", "Leader", "Test Galaxy");
            leader2.setAiAdmin(true);
            leader2.setActive(true);

            AiCharacter leader3 = new AiCharacter("Country Leader 3", "Leader", "Test Galaxy");
            leader3.setAiAdmin(true);
            leader3.setActive(true);

            AiCharacter governor1 = new AiCharacter("Region Governor 1", "Governor", "Test Galaxy");
            governor1.setAiAdmin(true);
            governor1.setActive(true);

            AiCharacter custom = new AiCharacter("Custom AI", "Custom Role", "Test Galaxy");
            custom.setAiAdmin(true);
            custom.setActive(true);

            List<AiCharacter> allAdmins = List.of(
                    galaxyOverseer, overseer1, overseer2,
                    leader1, leader2, leader3,
                    governor1, custom
            );

            when(galaxyRepository.findById(1L)).thenReturn(Optional.of(galaxy));
            when(aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName("Test Galaxy"))
                    .thenReturn(allAdmins);

            // Act
            GalaxyOverseerService.WorkforceReport report = galaxyOverseerService.monitorWorkforce(1L);

            // Assert
            assertEquals(8, report.getTotalAiAgents());
            assertEquals(1, report.getGalaxyOverseerCount());
            assertEquals(2, report.getPlanetOverseerCount());
            assertEquals(3, report.getLeaderCount());
            assertEquals(1, report.getGovernorCount());
            assertEquals(1, report.getOtherCount());
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
    }

    // ---- Workforce Report DTO Tests ----

    @Nested
    @DisplayName("Workforce Report DTO")
    class WorkforceReportDtoTests {

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
    }

    // ---- Constants Verification ----

    @Nested
    @DisplayName("Constants Verification")
    class ConstantsTests {

        @Test
        @DisplayName("should have correct recommended AI agents per planet constant")
        void constants_shouldHaveCorrectThreshold() {
            assertEquals(15, GalaxyOverseerService.RECOMMENDED_AI_AGENTS_PER_PLANET);
        }

        @Test
        @DisplayName("should have correct Galaxy Overseer role name constant")
        void constants_shouldHaveCorrectRoleName() {
            assertEquals("Galaxy Overseer", GalaxyOverseerService.ROLE_GALAXY_OVERSEER);
        }
    }
}
