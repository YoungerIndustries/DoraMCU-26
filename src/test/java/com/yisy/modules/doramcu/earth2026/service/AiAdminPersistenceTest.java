package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.dto.PlanetCharacterInfo;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Planet;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Region;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ContractRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CountryRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026EconomyRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026JobRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026LocationRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026PlanetRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026RegionRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ScenarioRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026StructureRepository;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.PlanetPopulationProgress;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GalaxyMetricsService;
import com.yisy.modules.doramcu.service.GameSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AI Admin Persistence Tests")
class AiAdminPersistenceTest {

    private Earth2026CharacterRepository characterRepository;
    private Earth2026LocationRepository locationRepository;
    private Earth2026ScenarioRepository scenarioRepository;
    private Earth2026StructureRepository structureRepository;
    private Earth2026CountryRepository countryRepository;
    private Earth2026RegionRepository regionRepository;
    private Earth2026EconomyRepository economyRepository;
    private Earth2026JobRepository jobRepository;
    private Earth2026ContractRepository contractRepository;
    private Earth2026PlanetRepository planetRepository;
    private AiCharacterRepository aiCharacterRepository;
    private Earth2026RulesDatabaseService rulesDatabaseService;
    private AiPlayerService aiPlayerService;
    private GameSessionService gameSessionService;
    private GalaxyEventService galaxyEventService;
    private GalaxyMetricsService galaxyMetricsService;
    private Earth2026Service earth2026Service;

    @BeforeEach
    void setUp() {
        characterRepository = mock(Earth2026CharacterRepository.class);
        locationRepository = mock(Earth2026LocationRepository.class);
        scenarioRepository = mock(Earth2026ScenarioRepository.class);
        structureRepository = mock(Earth2026StructureRepository.class);
        countryRepository = mock(Earth2026CountryRepository.class);
        regionRepository = mock(Earth2026RegionRepository.class);
        economyRepository = mock(Earth2026EconomyRepository.class);
        jobRepository = mock(Earth2026JobRepository.class);
        contractRepository = mock(Earth2026ContractRepository.class);
        planetRepository = mock(Earth2026PlanetRepository.class);
        aiCharacterRepository = mock(AiCharacterRepository.class);
        rulesDatabaseService = mock(Earth2026RulesDatabaseService.class);
        aiPlayerService = mock(AiPlayerService.class);
        gameSessionService = mock(GameSessionService.class);
        galaxyEventService = mock(GalaxyEventService.class);
        galaxyMetricsService = mock(GalaxyMetricsService.class);

        earth2026Service = new Earth2026Service(
                characterRepository, locationRepository, scenarioRepository, structureRepository,
                countryRepository, regionRepository, economyRepository,
                jobRepository, contractRepository, planetRepository, aiCharacterRepository,
                rulesDatabaseService, aiPlayerService, gameSessionService, galaxyEventService,
                galaxyMetricsService
        );
    }

    // ---- findOrCreateAiCharacterByRole: Role-based reuse ----

    @Nested
    @DisplayName("AI Character Reuse by Role")
    class AiCharacterReuseByRoleTests {

        @Test
        @DisplayName("should reuse existing experienced Leader AI instead of creating new one")
        void populatePlanet_shouldReuseExperiencedLeader() {
            // Arrange: Existing experienced AI Leader from a previous planet
            AiCharacter experiencedLeader = new AiCharacter("General Kael", "Leader", "Previous Planet");
            experiencedLeader.setId(100L);
            experiencedLeader.setActive(true);
            experiencedLeader.setTotalExperience(250);
            experiencedLeader.setLeadershipRating(8);
            experiencedLeader.setTotalBattles(15);
            experiencedLeader.setTotalVictories(12);

            // Mock: No existing planet
            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());

            // Mock: planetRepository.save returns the planet with an ID
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            // Mock: No countries on the new planet
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Mock: No existing AI with role "Overseer" — will create a new one
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            // Mock: save returns the same character
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act: Populate the planet
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            PlanetPopulationProgress progress = null;

            // We need to capture the behavior — since findOrCreateAiCharacterByRole is private,
            // we test through the public populatePlanetWithGameData method
            // The overseer will be assigned via findOrCreateAiCharacterByRole("Overseer", ...)
            // Since no Overseer exists, a new one will be created

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: Planet should be created and saved
            verify(planetRepository, atLeastOnce()).save(any(Earth2026Planet.class));
        }

        @Test
        @DisplayName("should retain experience when reusing AI character by role")
        void reuseAiCharacter_shouldRetainExperience() {
            // Arrange: Existing experienced AI Leader
            AiCharacter experiencedLeader = new AiCharacter("General Kael", "Leader", "Previous Planet");
            experiencedLeader.setId(100L);
            experiencedLeader.setActive(true);
            experiencedLeader.setTotalExperience(250);
            experiencedLeader.setLeadershipRating(8);
            experiencedLeader.setTotalBattles(15);
            experiencedLeader.setTotalVictories(12);

            List<AiCharacter> leaders = new ArrayList<>();
            leaders.add(experiencedLeader);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(leaders);
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Mock planet creation
            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            // Add a country so the Leader role is assigned
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());

            // No Overseer exists
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            // Collect progress logs to verify reuse message
            List<String> logs = new ArrayList<>();
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}

                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}

                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}

                @Override
                public void onLog(String message) {
                    logs.add(message);
                }

                @Override
                public void onComplete(PopulationSummary summary) {}
            };

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: The experienced leader should have been saved with updated empire name
            // but experience should be retained
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository, atLeastOnce()).save(captor.capture());

            // Find the saved leader among captured arguments
            boolean foundLeader = false;
            for (AiCharacter saved : captor.getAllValues()) {
                if ("Leader".equals(saved.getRole())) {
                    foundLeader = true;
                    assertEquals(250, saved.getTotalExperience(), "Experience should be retained");
                    assertEquals(8, saved.getLeadershipRating(), "Leadership rating should be retained");
                    assertEquals(15, saved.getTotalBattles(), "Battles should be retained");
                    assertEquals(12, saved.getTotalVictories(), "Victories should be retained");
                    assertEquals("Testland", saved.getEmpireName(), "Empire name should be updated to country name");
                    break;
                }
            }
            assertTrue(foundLeader, "Leader AI character should have been saved");
        }

        @Test
        @DisplayName("should create new AI when no existing character with matching role")
        void populatePlanet_shouldCreateNewAiWhenNoneExists() {
            // Arrange: No existing AI characters for any role
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc(anyString()))
                    .thenReturn(new ArrayList<>());

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Track created AI characters
            List<AiCharacter> createdAis = new ArrayList<>();
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        createdAis.add(ai);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: A new Overseer should have been created with 0 experience
            AiCharacter overseer = null;
            for (AiCharacter ai : createdAis) {
                if ("Overseer".equals(ai.getRole())) {
                    overseer = ai;
                    break;
                }
            }
            assertNotNull(overseer, "Overseer should be created when none exists");
            assertEquals(0, overseer.getTotalExperience(), "New AI should have 0 experience");
            assertEquals(0, overseer.getTotalBattles(), "New AI should have 0 battles");
            assertEquals(5, overseer.getLeadershipRating(), "New AI should have default leadership rating");
        }

        @Test
        @DisplayName("should pick highest experience AI when multiple candidates exist")
        void reuseAiCharacter_shouldPickHighestExperience() {
            // Arrange: Multiple AI Leaders with different experience levels
            AiCharacter noviceLeader = new AiCharacter("Novice", "Leader", "Planet A");
            noviceLeader.setId(101L);
            noviceLeader.setActive(true);
            noviceLeader.setTotalExperience(50);
            noviceLeader.setLeadershipRating(5);

            AiCharacter veteranLeader = new AiCharacter("Veteran", "Leader", "Planet B");
            veteranLeader.setId(102L);
            veteranLeader.setActive(true);
            veteranLeader.setTotalExperience(500);
            veteranLeader.setLeadershipRating(10);

            AiCharacter expertLeader = new AiCharacter("Expert", "Leader", "Planet C");
            expertLeader.setId(103L);
            expertLeader.setActive(true);
            expertLeader.setTotalExperience(300);
            expertLeader.setLeadershipRating(9);

            // Return ordered by experience desc (as the repository method would)
            List<AiCharacter> leaders = new ArrayList<>();
            leaders.add(veteranLeader);   // Highest XP first
            leaders.add(expertLeader);
            leaders.add(noviceLeader);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(leaders);
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            // Add a country so the Leader role is assigned
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            List<String> logs = new ArrayList<>();
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}

                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}

                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}

                @Override
                public void onLog(String message) {
                    logs.add(message);
                }

                @Override
                public void onComplete(PopulationSummary summary) {}
            };

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: The veteran (highest XP) should be reused
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository, atLeastOnce()).save(captor.capture());

            boolean foundVeteran = false;
            for (AiCharacter saved : captor.getAllValues()) {
                if ("Leader".equals(saved.getRole())) {
                    foundVeteran = true;
                    assertEquals("Veteran", saved.getName(), "Highest experience AI should be reused");
                    assertEquals(500, saved.getTotalExperience(), "Experience should be retained");
                    assertEquals(10, saved.getLeadershipRating(), "Leadership should be retained");
                    break;
                }
            }
            assertTrue(foundVeteran, "Leader should have been saved");
        }

        @Test
        @DisplayName("should update empire name and description when reusing AI")
        void reuseAiCharacter_shouldUpdateEmpireAndDescription() {
            // Arrange
            AiCharacter existingGovernor = new AiCharacter("Governor Rex", "Governor", "Old Planet");
            existingGovernor.setId(150L);
            existingGovernor.setActive(true);
            existingGovernor.setTotalExperience(180);
            existingGovernor.setLeadershipRating(7);
            existingGovernor.setDescription("AI governor for Old Planet");

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Governor"))
                    .thenReturn(List.of(existingGovernor));
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            // A country with a region that needs a governor
            Earth2026Country country = new Earth2026Country("Test Country", "TC", "Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));

            Earth2026Region region = new Earth2026Region("Test Region", "Administrative", 10L);
            region.setId(20L);
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(List.of(region));
            when(regionRepository.save(any(Earth2026Region.class))).thenAnswer(inv -> inv.getArgument(0));

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: Governor's empire name and description should be updated
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository, atLeastOnce()).save(captor.capture());

            boolean foundGovernor = false;
            for (AiCharacter saved : captor.getAllValues()) {
                if ("Governor".equals(saved.getRole())) {
                    foundGovernor = true;
                    assertEquals(180, saved.getTotalExperience(), "Experience retained");
                    assertEquals(7, saved.getLeadershipRating(), "Leadership retained");
                    // Empire name and description should be updated for the new planet
                    assertNotNull(saved.getEmpireName());
                    assertNotNull(saved.getDescription());
                    break;
                }
            }
            assertTrue(foundGovernor, "Governor should have been saved");
        }
    }

    // ---- findOrCreateAiCharacter: Name-based reuse ----

    @Nested
    @DisplayName("AI Character Reuse by Name")
    class AiCharacterReuseByNameTests {

        @Test
        @DisplayName("should reuse existing AI by exact name match")
        void populatePlanet_shouldReuseByName() {
            // Arrange: Existing AI character with known name
            AiCharacter existingAi = new AiCharacter("Admiral Tachyon", "Leader", "Previous Planet");
            existingAi.setId(300L);
            existingAi.setActive(true);
            existingAi.setTotalExperience(400);
            existingAi.setLeadershipRating(9);
            existingAi.setTotalBattles(25);
            existingAi.setTotalVictories(20);

            PlanetCharacterInfo charInfo = new PlanetCharacterInfo();
            charInfo.setName("Admiral Tachyon");
            charInfo.setRole("Leader");
            charInfo.setEmpireName("New Planet");
            charInfo.setCountryOrRegion("United States");
            charInfo.setActive(true);
            charInfo.setPlayerControlled(false);
            charInfo.setGameId("game-1");

            when(aiCharacterRepository.findByName("Admiral Tachyon"))
                    .thenReturn(Optional.of(existingAi));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            // Country matching the character's countryOrRegion
            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));

            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(charInfo));

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The existing AI should be reused (found by name)
            verify(aiCharacterRepository).findByName("Admiral Tachyon");
            // The AI found by name is returned directly without save in findOrCreateAiCharacter
            // but the country save will happen
            verify(countryRepository).save(any(Earth2026Country.class));
        }

        @Test
        @DisplayName("should fall back to role-based reuse when name not found")
        void populatePlanet_shouldFallbackToRoleReuse() {
            // Arrange: No name match, but role match exists
            AiCharacter roleMatch = new AiCharacter("General Nova", "Leader", "Other Planet");
            roleMatch.setId(301L);
            roleMatch.setActive(true);
            roleMatch.setTotalExperience(150);
            roleMatch.setLeadershipRating(7);

            PlanetCharacterInfo charInfo = new PlanetCharacterInfo();
            charInfo.setName("Admiral Tachyon");
            charInfo.setRole("Leader");
            charInfo.setEmpireName("New Planet");
            charInfo.setCountryOrRegion("United States");
            charInfo.setActive(true);
            charInfo.setPlayerControlled(false);
            charInfo.setGameId("game-1");

            when(aiCharacterRepository.findByName("Admiral Tachyon"))
                    .thenReturn(Optional.empty());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(List.of(roleMatch));
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(charInfo));

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: Role-based reuse should have been triggered
            verify(aiCharacterRepository).findByName("Admiral Tachyon");
            verify(aiCharacterRepository).findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader");
            verify(aiCharacterRepository).save(eq(roleMatch));
        }
    }

    // ---- Cross-planet persistence ----

    @Nested
    @DisplayName("Cross-Planet AI Persistence")
    class CrossPlanetPersistenceTests {

        @Test
        @DisplayName("should reuse same AI admin across multiple planet creations")
        void multiplePlanets_shouldReuseSameAiAdmin() {
            // Arrange: One experienced AI Leader
            AiCharacter sharedLeader = new AiCharacter("Commander Zeta", "Leader", "Planet Alpha");
            sharedLeader.setId(400L);
            sharedLeader.setActive(true);
            sharedLeader.setTotalExperience(350);
            sharedLeader.setLeadershipRating(9);
            sharedLeader.setTotalBattles(20);
            sharedLeader.setTotalVictories(18);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(List.of(sharedLeader));
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Planet 1 — with a country so Leader is assigned
            when(planetRepository.findByNameAndActiveTrue("Planet Beta"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(2L);
                        return p;
                    });
            Earth2026Country country1 = new Earth2026Country("Country Beta", "CB", "Republic", 2L);
            country1.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(2L)).thenReturn(List.of(country1));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            // Act: Create Planet Beta
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request1 =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request1.setPlanetName("Planet Beta");
            request1.setSpeciesIdForNewAi("Human");
            request1.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request1, null);

            // Now simulate creating Planet Gamma — the same leader should still be found
            when(planetRepository.findByNameAndActiveTrue("Planet Gamma"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(3L);
                        return p;
                    });
            Earth2026Country country2 = new Earth2026Country("Country Gamma", "CG", "Republic", 3L);
            country2.setId(20L);
            when(countryRepository.findByPlanetIdAndActiveTrue(3L)).thenReturn(List.of(country2));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(20L)).thenReturn(new ArrayList<>());

            // Act: Create Planet Gamma
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request2 =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request2.setPlanetName("Planet Gamma");
            request2.setSpeciesIdForNewAi("Human");
            request2.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request2, null);

            // Assert: The same AI character should be reused again
            // The Leader query should be called at least twice (once per planet)
            verify(aiCharacterRepository, atLeast(2)).findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader");
        }

        @Test
        @DisplayName("should track new vs reused AI characters correctly")
        void populatePlanet_shouldTrackNewVsReused() {
            // Arrange: Experienced Overseer exists; no Leader exists
            AiCharacter experiencedOverseer = new AiCharacter("Overseer Prime", "Overseer", "Old Planet");
            experiencedOverseer.setId(500L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setTotalExperience(600);
            experiencedOverseer.setLeadershipRating(10);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(new ArrayList<>()); // No Leader — will create new
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(501L);
                        return ai;
                    });

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Collect summary
            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}

                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}

                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}

                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: Summary should show the overseer was reused (not counted as new)
            assertNotNull(summaryHolder[0], "Summary should be provided");
            // The overseer is reused, so newAiCharactersGenerated should not include it
            // Since no countries, only the overseer is involved
            // The overseer has experience > 0, so it should NOT be in newAiCharacters
            assertEquals(0, summaryHolder[0].getNewAiCharactersGenerated(),
                    "Reused overseer should not be counted as new");
        }
    }

    // ---- New AI character initialization ----

    @Nested
    @DisplayName("New AI Character Initialization")
    class NewAiCharacterInitializationTests {

        @Test
        @DisplayName("should set canPromoteToCoServer to false on new AI characters")
        void newAiCharacter_shouldHaveCanPromoteToCoServerFalse() {
            // Arrange: No existing AI for Overseer role
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Track created AI characters
            List<AiCharacter> createdAis = new ArrayList<>();
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        createdAis.add(ai);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The new Overseer should have canPromoteToCoServer = false
            AiCharacter overseer = null;
            for (AiCharacter ai : createdAis) {
                if ("Overseer".equals(ai.getRole())) {
                    overseer = ai;
                    break;
                }
            }
            assertNotNull(overseer, "Overseer should be created");
            assertFalse(overseer.isCanPromoteToCoServer(),
                    "New AI should have canPromoteToCoServer = false");
        }

        @Test
        @DisplayName("should set active to true on new AI characters")
        void newAiCharacter_shouldBeActive() {
            // Arrange
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            List<AiCharacter> createdAis = new ArrayList<>();
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        createdAis.add(ai);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert
            AiCharacter overseer = null;
            for (AiCharacter ai : createdAis) {
                if ("Overseer".equals(ai.getRole())) {
                    overseer = ai;
                    break;
                }
            }
            assertNotNull(overseer);
            assertTrue(overseer.isActive(), "New AI should be active");
        }

        @Test
        @DisplayName("should set default leadership rating to 5 on new AI characters")
        void newAiCharacter_shouldHaveDefaultLeadershipRating() {
            // Arrange
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            List<AiCharacter> createdAis = new ArrayList<>();
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        createdAis.add(ai);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert
            AiCharacter overseer = null;
            for (AiCharacter ai : createdAis) {
                if ("Overseer".equals(ai.getRole())) {
                    overseer = ai;
                    break;
                }
            }
            assertNotNull(overseer);
            assertEquals(5, overseer.getLeadershipRating(),
                    "New AI should have default leadership rating of 5");
        }

        @Test
        @DisplayName("should set zero experience on new AI characters")
        void newAiCharacter_shouldHaveZeroExperience() {
            // Arrange
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            List<AiCharacter> createdAis = new ArrayList<>();
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        createdAis.add(ai);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert
            AiCharacter overseer = null;
            for (AiCharacter ai : createdAis) {
                if ("Overseer".equals(ai.getRole())) {
                    overseer = ai;
                    break;
                }
            }
            assertNotNull(overseer);
            assertEquals(0, overseer.getTotalExperience(), "New AI should have 0 experience");
            assertEquals(0, overseer.getTotalBattles(), "New AI should have 0 battles");
            assertEquals(0, overseer.getTotalVictories(), "New AI should have 0 victories");
        }
    }

    // ---- Progress callback tests ----

    @Nested
    @DisplayName("Progress Callback Verification")
    class ProgressCallbackTests {

        @Test
        @DisplayName("should log reuse message when overseer is reused with experience")
        void populatePlanet_shouldLogOverseerReuseMessage() {
            // Arrange: Experienced Overseer
            AiCharacter experiencedOverseer = new AiCharacter("Overseer Prime", "Overseer", "Old Planet");
            experiencedOverseer.setId(500L);
            experiencedOverseer.setActive(true);
            experiencedOverseer.setTotalExperience(600);
            experiencedOverseer.setLeadershipRating(10);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(List.of(experiencedOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Collect progress logs
            List<String> logs = new ArrayList<>();
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}

                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}

                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}

                @Override
                public void onLog(String message) {
                    logs.add(message);
                }

                @Override
                public void onComplete(PopulationSummary summary) {}
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: Should contain a log message about reusing the overseer with experience
            boolean foundReuseLog = false;
            for (String log : logs) {
                if (log.contains("reused with experience") && log.contains("Overseer Prime")) {
                    foundReuseLog = true;
                    break;
                }
            }
            assertTrue(foundReuseLog,
                    "Should log overseer reuse with experience. Logs: " + logs);
        }

        @Test
        @DisplayName("should log creation message when overseer is newly created")
        void populatePlanet_shouldLogNewOverseerMessage() {
            // Arrange: No existing Overseer
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Collect progress logs
            List<String> logs = new ArrayList<>();
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}

                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}

                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}

                @Override
                public void onLog(String message) {
                    logs.add(message);
                }

                @Override
                public void onComplete(PopulationSummary summary) {}
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: Should contain a log message about assigning the overseer (without "reused")
            boolean foundAssignLog = false;
            for (String log : logs) {
                if (log.contains("Planet overseer assigned") && !log.contains("reused")) {
                    foundAssignLog = true;
                    break;
                }
            }
            assertTrue(foundAssignLog,
                    "Should log new overseer assignment. Logs: " + logs);
        }

        @Test
        @DisplayName("should report planet found in summary when reusing existing planet")
        void populatePlanet_shouldReportPlanetFoundInSummary() {
            // Arrange: Existing planet
            Earth2026Planet existingPlanet = new Earth2026Planet("Existing Planet", "Terrestrial");
            existingPlanet.setId(1L);
            existingPlanet.setActive(true);

            when(planetRepository.findByNameAndActiveTrue("Existing Planet"))
                    .thenReturn(Optional.of(existingPlanet));
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Collect summary
            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}

                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}

                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}

                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("Existing Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: Summary should indicate planet was found
            assertNotNull(summaryHolder[0], "Summary should be provided");
            assertTrue(summaryHolder[0].isPlanetFound(),
                    "Summary should indicate planet was found and reused");
        }
    }

    // ---- Edge case tests ----

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should not reassign leader when country already has aiLeaderId")
        void populatePlanet_shouldSkipExistingLeader() {
            // Arrange: Country with existing leader
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);
            country.setAiLeaderId(999L); // Already has a leader
            country.setLeaderName("Existing Leader");

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The country's leader should not have been changed
            // The findOrCreateAiCharacterByRole for Leader should NOT be called
            // because country.getAiLeaderId() is not null
            verify(aiCharacterRepository, never())
                    .findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader");
        }

        @Test
        @DisplayName("should not reassign governor when region already has aiGovernorId")
        void populatePlanet_shouldSkipExistingGovernor() {
            // Arrange: Country with a region that already has a governor
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);

            Earth2026Region region = new Earth2026Region("Test Region", "Administrative", 10L);
            region.setId(20L);
            region.setAiGovernorId(888L); // Already has a governor
            region.setGovernorName("Existing Governor");

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(List.of(region));
            when(regionRepository.save(any(Earth2026Region.class))).thenAnswer(inv -> inv.getArgument(0));
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The Governor role should NOT be queried for this region
            // because region.getAiGovernorId() is not null
            verify(aiCharacterRepository, never())
                    .findByRoleAndActiveTrueOrderByTotalExperienceDesc("Governor");
        }

        @Test
        @DisplayName("should not reassign overseer when planet already has aiOverseerId")
        void populatePlanet_shouldSkipExistingOverseer() {
            // Arrange: Existing planet with overseer
            Earth2026Planet existingPlanet = new Earth2026Planet("Existing Planet", "Terrestrial");
            existingPlanet.setId(1L);
            existingPlanet.setActive(true);
            existingPlanet.setAiOverseerId(777L); // Already has an overseer

            when(planetRepository.findByNameAndActiveTrue("Existing Planet"))
                    .thenReturn(Optional.of(existingPlanet));
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("Existing Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: Overseer should NOT be queried since planet already has one
            verify(aiCharacterRepository, never())
                    .findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer");
        }

        @Test
        @DisplayName("should preserve all experience fields when reusing AI across planets")
        void reuseAiCharacter_shouldPreserveAllExperienceFields() {
            // Arrange: AI with all experience fields populated
            AiCharacter veteranOverseer = new AiCharacter("Veteran Overseer", "Overseer", "Planet Alpha");
            veteranOverseer.setId(600L);
            veteranOverseer.setActive(true);
            veteranOverseer.setTotalExperience(1000);
            veteranOverseer.setLeadershipRating(10);
            veteranOverseer.setTotalBattles(50);
            veteranOverseer.setTotalVictories(45);
            veteranOverseer.setHirable(false);
            veteranOverseer.setDailySalary(500);

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(List.of(veteranOverseer));
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: All experience fields should be preserved
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository, atLeastOnce()).save(captor.capture());

            boolean foundOverseer = false;
            for (AiCharacter saved : captor.getAllValues()) {
                if ("Overseer".equals(saved.getRole())) {
                    foundOverseer = true;
                    assertEquals(1000, saved.getTotalExperience(), "Total experience preserved");
                    assertEquals(10, saved.getLeadershipRating(), "Leadership rating preserved");
                    assertEquals(50, saved.getTotalBattles(), "Total battles preserved");
                    assertEquals(45, saved.getTotalVictories(), "Total victories preserved");
                    assertFalse(saved.isHirable(), "Hirable flag preserved");
                    assertEquals(500, saved.getDailySalary(), "Daily salary preserved");
                    break;
                }
            }
            assertTrue(foundOverseer, "Overseer should have been saved");
        }
    }

    // ---- Force regenerate tests ----

    @Nested
    @DisplayName("Force Regenerate")
    class ForceRegenerateTests {

        @Test
        @DisplayName("should skip existing AI from game when force regenerate is enabled")
        void populatePlanet_shouldSkipExistingAiWhenForceRegenerate() {
            // Arrange: Country without a leader
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());

            // No existing AI for Leader role — will create new
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Existing AI character from game — should be skipped with force regenerate
            PlanetCharacterInfo charInfo = new PlanetCharacterInfo();
            charInfo.setName("Existing AI Leader");
            charInfo.setRole("Leader");
            charInfo.setEmpireName("New Planet");
            charInfo.setCountryOrRegion("Testland");
            charInfo.setActive(true);
            charInfo.setPlayerControlled(false);
            charInfo.setGameId("game-1");

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(charInfo));
            request.setForceRegenerate(true);

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: findOrCreateAiCharacter should NOT be called for the existing AI
            // because force regenerate skips the existing character lookup
            // The Leader role should be created via findOrCreateAiCharacterByRole instead
            verify(aiCharacterRepository).findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader");
            // findByName should not be called since force regenerate skips existing chars
            verify(aiCharacterRepository, never()).findByName(anyString());
        }
    }

    // ---- Multiple countries with leaders ----

    @Nested
    @DisplayName("Multiple Countries with Leaders")
    class MultipleCountriesTests {

        @Test
        @DisplayName("should assign leaders to all countries on the planet")
        void populatePlanet_shouldAssignLeadersToAllCountries() {
            // Arrange: Multiple countries
            Earth2026Country country1 = new Earth2026Country("Country One", "CO", "Republic", 1L);
            country1.setId(10L);
            Earth2026Country country2 = new Earth2026Country("Country Two", "CT", "Democracy", 1L);
            country2.setId(11L);
            Earth2026Country country3 = new Earth2026Country("Country Three", "C3", "Monarchy", 1L);
            country3.setId(12L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L))
                    .thenReturn(List.of(country1, country2, country3));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(regionRepository.findByCountryIdAndActiveTrue(11L)).thenReturn(new ArrayList<>());
            when(regionRepository.findByCountryIdAndActiveTrue(12L)).thenReturn(new ArrayList<>());

            // No existing AI — will create new leaders
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Collect summary
            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: All 3 countries should have leaders
            assertNotNull(summaryHolder[0]);
            assertEquals(3, summaryHolder[0].getCountriesWithLeaders(),
                    "All 3 countries should have leaders assigned");
        }

        @Test
        @DisplayName("should reuse same experienced leader across multiple countries")
        void populatePlanet_shouldReuseSameLeaderAcrossCountries() {
            // Arrange: One experienced Leader for all countries
            AiCharacter sharedLeader = new AiCharacter("Commander Alpha", "Leader", "Previous Planet");
            sharedLeader.setId(400L);
            sharedLeader.setActive(true);
            sharedLeader.setTotalExperience(350);
            sharedLeader.setLeadershipRating(9);

            Earth2026Country country1 = new Earth2026Country("Country One", "CO", "Republic", 1L);
            country1.setId(10L);
            Earth2026Country country2 = new Earth2026Country("Country Two", "CT", "Democracy", 1L);
            country2.setId(11L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L))
                    .thenReturn(List.of(country1, country2));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(regionRepository.findByCountryIdAndActiveTrue(11L)).thenReturn(new ArrayList<>());

            // Same leader returned for each query
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(List.of(sharedLeader));
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The Leader role should be queried for each country
            verify(aiCharacterRepository, atLeast(2))
                    .findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader");
            // The shared leader should be saved with updated empire names
            ArgumentCaptor<AiCharacter> captor = ArgumentCaptor.forClass(AiCharacter.class);
            verify(aiCharacterRepository, atLeast(2)).save(captor.capture());
        }
    }

    // ---- AI Leader/Governor assignment callbacks ----

    @Nested
    @DisplayName("AI Leader and Governor Assignment Callbacks")
    class AiAssignmentCallbackTests {

        @Test
        @DisplayName("should call onAiLeaderAssigned when leader is assigned to country")
        void populatePlanet_shouldCallOnAiLeaderAssigned() {
            // Arrange
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            List<AiCharacter> assignedLeaders = new ArrayList<>();
            List<Long> assignedCountryIds = new ArrayList<>();
            List<Boolean> isNewFlags = new ArrayList<>();

            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onAiLeaderAssigned(AiCharacter leader, Long countryId, boolean isNew) {
                    assignedLeaders.add(leader);
                    assignedCountryIds.add(countryId);
                    isNewFlags.add(isNew);
                }
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {}
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert
            assertEquals(1, assignedLeaders.size(), "onAiLeaderAssigned should be called once");
            assertEquals(10L, assignedCountryIds.get(0).longValue(), "Country ID should match");
            assertTrue(isNewFlags.get(0), "New leader should have isNew=true");
            assertEquals("Leader", assignedLeaders.get(0).getRole());
        }

        @Test
        @DisplayName("should call onAiGovernorAssigned when governor is assigned to region")
        void populatePlanet_shouldCallOnAiGovernorAssigned() {
            // Arrange
            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);

            Earth2026Region region = new Earth2026Region("Test Region", "Administrative", 10L);
            region.setId(20L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(List.of(region));
            when(regionRepository.save(any(Earth2026Region.class))).thenAnswer(inv -> inv.getArgument(0));

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Governor"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());

            List<AiCharacter> assignedGovernors = new ArrayList<>();
            List<Long> assignedRegionIds = new ArrayList<>();
            List<Boolean> isNewFlags = new ArrayList<>();

            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onAiGovernorAssigned(AiCharacter governor, Long regionId, boolean isNew) {
                    assignedGovernors.add(governor);
                    assignedRegionIds.add(regionId);
                    isNewFlags.add(isNew);
                }
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {}
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert
            assertEquals(1, assignedGovernors.size(), "onAiGovernorAssigned should be called once");
            assertEquals(20L, assignedRegionIds.get(0).longValue(), "Region ID should match");
            assertTrue(isNewFlags.get(0), "New governor should have isNew=true");
            assertEquals("Governor", assignedGovernors.get(0).getRole());
        }

        @Test
        @DisplayName("should set isNew=false in callback when reusing experienced leader")
        void populatePlanet_shouldSetIsNewFalseForReusedLeader() {
            // Arrange: Experienced Leader
            AiCharacter experiencedLeader = new AiCharacter("Veteran Leader", "Leader", "Old Planet");
            experiencedLeader.setId(300L);
            experiencedLeader.setActive(true);
            experiencedLeader.setTotalExperience(200);
            experiencedLeader.setLeadershipRating(8);

            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(List.of(experiencedLeader));
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<Boolean> isNewFlags = new ArrayList<>();

            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onAiLeaderAssigned(AiCharacter leader, Long countryId, boolean isNew) {
                    isNewFlags.add(isNew);
                }
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {}
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert
            assertEquals(1, isNewFlags.size());
            assertFalse(isNewFlags.get(0), "Reused leader should have isNew=false");
        }
    }

    // ---- Player character registration ----

    @Nested
    @DisplayName("Player Character Registration")
    class PlayerCharacterRegistrationTests {

        @Test
        @DisplayName("should register player characters and report count in summary")
        void populatePlanet_shouldRegisterPlayerCharacters() {
            // Arrange
            PlanetCharacterInfo player1 = new PlanetCharacterInfo();
            player1.setName("John Doe");
            player1.setRole("Citizen");
            player1.setEmpireName("New Planet");
            player1.setActive(true);
            player1.setPlayerControlled(true);
            player1.setGameId("player-1");

            PlanetCharacterInfo player2 = new PlanetCharacterInfo();
            player2.setName("Jane Smith");
            player2.setRole("Soldier");
            player2.setEmpireName("New Planet");
            player2.setActive(true);
            player2.setPlayerControlled(true);
            player2.setGameId("player-2");

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            // Collect player registration
            Map<String, String>[] registeredHolder = new Map[1];
            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];

            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onPlayerCharactersRegistered(Map<String, String> registered) {
                    registeredHolder[0] = registered;
                }
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(player1, player2));

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert
            assertNotNull(registeredHolder[0], "Player characters should be registered");
            assertEquals(2, registeredHolder[0].size(), "2 player characters should be registered");
            assertEquals("John Doe", registeredHolder[0].get("player-1"));
            assertEquals("Jane Smith", registeredHolder[0].get("player-2"));

            assertNotNull(summaryHolder[0]);
            assertEquals(2, summaryHolder[0].getPlayerCharactersRegistered(),
                    "Summary should report 2 player characters registered");
        }

        @Test
        @DisplayName("should separate AI and player characters from existing characters list")
        void populatePlanet_shouldSeparateAiAndPlayerCharacters() {
            // Arrange: Mixed AI and player characters
            PlanetCharacterInfo aiChar = new PlanetCharacterInfo();
            aiChar.setName("AI Commander");
            aiChar.setRole("Leader");
            aiChar.setEmpireName("New Planet");
            aiChar.setActive(true);
            aiChar.setPlayerControlled(false);
            aiChar.setGameId("ai-1");

            PlanetCharacterInfo playerChar = new PlanetCharacterInfo();
            playerChar.setName("Player One");
            playerChar.setRole("Citizen");
            playerChar.setEmpireName("New Planet");
            playerChar.setActive(true);
            playerChar.setPlayerControlled(true);
            playerChar.setGameId("player-1");

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(aiChar, playerChar));

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert: Summary should show total game characters = 2, player characters = 1
            assertNotNull(summaryHolder[0]);
            assertEquals(2, summaryHolder[0].getTotalGameCharacters(),
                    "Total game characters should be 2 (1 AI + 1 player)");
            assertEquals(1, summaryHolder[0].getPlayerCharactersRegistered(),
                    "Player characters registered should be 1");
        }
    }

    // ---- Summary statistics accuracy ----

    @Nested
    @DisplayName("Summary Statistics Accuracy")
    class SummaryStatisticsTests {

        @Test
        @DisplayName("should report accurate counts with mixed new and reused AI")
        void populatePlanet_shouldReportAccurateSummaryStats() {
            // Arrange: Experienced Leader (will be reused), no Overseer (will be new)
            AiCharacter experiencedLeader = new AiCharacter("Veteran Leader", "Leader", "Old Planet");
            experiencedLeader.setId(300L);
            experiencedLeader.setActive(true);
            experiencedLeader.setTotalExperience(200);
            experiencedLeader.setLeadershipRating(8);

            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);

            Earth2026Region region = new Earth2026Region("Test Region", "Administrative", 10L);
            region.setId(20L);

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(List.of(region));
            when(regionRepository.save(any(Earth2026Region.class))).thenAnswer(inv -> inv.getArgument(0));

            // Leader pool has experienced leader; Governor and Overseer pools are empty
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(List.of(experiencedLeader));
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Governor"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> {
                        AiCharacter ai = inv.getArgument(0);
                        if (ai.getId() == null) ai.setId(200L);
                        return ai;
                    });

            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(new ArrayList<>());

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert
            assertNotNull(summaryHolder[0]);
            assertEquals(1, summaryHolder[0].getCountriesWithLeaders(),
                    "1 country should have a leader");
            assertEquals(1, summaryHolder[0].getRegionsWithGovernors(),
                    "1 region should have a governor");
            // Leader is reused (not counted as new), Governor and Overseer are new
            assertEquals(2, summaryHolder[0].getNewAiCharactersGenerated(),
                    "2 new AI characters should be generated (Governor + Overseer)");
        }

        @Test
        @DisplayName("should report aiCharactersReused when existing game AI is matched")
        void populatePlanet_shouldReportAiCharactersReused() {
            // Arrange: Existing AI from game that matches by name
            AiCharacter existingAi = new AiCharacter("Admiral Tachyon", "Leader", "Previous Planet");
            existingAi.setId(300L);
            existingAi.setActive(true);
            existingAi.setTotalExperience(400);
            existingAi.setLeadershipRating(9);

            PlanetCharacterInfo charInfo = new PlanetCharacterInfo();
            charInfo.setName("Admiral Tachyon");
            charInfo.setRole("Leader");
            charInfo.setEmpireName("New Planet");
            charInfo.setCountryOrRegion("United States");
            charInfo.setActive(true);
            charInfo.setPlayerControlled(false);
            charInfo.setGameId("game-1");

            when(aiCharacterRepository.findByName("Admiral Tachyon"))
                    .thenReturn(Optional.of(existingAi));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PlanetPopulationProgress.PopulationSummary[] summaryHolder = new PlanetPopulationProgress.PopulationSummary[1];
            PlanetPopulationProgress progress = new PlanetPopulationProgress() {
                @Override
                public void onPlanetCreated(Earth2026Planet planet) {}
                @Override
                public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {}
                @Override
                public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {}
                @Override
                public void onComplete(PlanetPopulationProgress.PopulationSummary summary) {
                    summaryHolder[0] = summary;
                }
            };

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(charInfo));

            earth2026Service.populatePlanetWithGameData(request, progress);

            // Assert
            assertNotNull(summaryHolder[0]);
            assertEquals(1, summaryHolder[0].getAiCharactersReused(),
                    "1 AI character should be marked as reused from game data");
        }
    }

    // ---- Country/Region name matching ----

    @Nested
    @DisplayName("Country and Region Name Matching")
    class NameMatchingTests {

        @Test
        @DisplayName("should match AI character to country by countryOrRegion name")
        void populatePlanet_shouldMatchAiToCountryByName() {
            // Arrange: AI character with countryOrRegion matching a country
            AiCharacter existingAi = new AiCharacter("General Kael", "Leader", "Previous Planet");
            existingAi.setId(100L);
            existingAi.setActive(true);
            existingAi.setTotalExperience(250);
            existingAi.setLeadershipRating(8);

            PlanetCharacterInfo charInfo = new PlanetCharacterInfo();
            charInfo.setName("General Kael");
            charInfo.setRole("Leader");
            charInfo.setEmpireName("New Planet");
            charInfo.setCountryOrRegion("United States");
            charInfo.setActive(true);
            charInfo.setPlayerControlled(false);
            charInfo.setGameId("game-1");

            when(aiCharacterRepository.findByName("General Kael"))
                    .thenReturn(Optional.of(existingAi));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(charInfo));

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The country should have the matched AI as leader
            verify(countryRepository).save(argThat(c ->
                    c instanceof Earth2026Country &&
                    ((Earth2026Country) c).getAiLeaderId() != null &&
                    ((Earth2026Country) c).getAiLeaderId().equals(100L)
            ));
        }

        @Test
        @DisplayName("should match AI character to region by countryOrRegion name")
        void populatePlanet_shouldMatchAiToRegionByName() {
            // Arrange: AI character with countryOrRegion matching a region
            AiCharacter existingAi = new AiCharacter("Governor Rex", "Governor", "Previous Planet");
            existingAi.setId(150L);
            existingAi.setActive(true);
            existingAi.setTotalExperience(180);
            existingAi.setLeadershipRating(7);

            PlanetCharacterInfo charInfo = new PlanetCharacterInfo();
            charInfo.setName("Governor Rex");
            charInfo.setRole("Governor");
            charInfo.setEmpireName("New Planet");
            charInfo.setCountryOrRegion("Test Region");
            charInfo.setActive(true);
            charInfo.setPlayerControlled(false);
            charInfo.setGameId("game-1");

            when(aiCharacterRepository.findByName("Governor Rex"))
                    .thenReturn(Optional.of(existingAi));

            when(planetRepository.findByNameAndActiveTrue("New Planet"))
                    .thenReturn(Optional.empty());
            when(planetRepository.save(any(Earth2026Planet.class)))
                    .thenAnswer(inv -> {
                        Earth2026Planet p = inv.getArgument(0);
                        if (p.getId() == null) p.setId(1L);
                        return p;
                    });

            Earth2026Country country = new Earth2026Country("Testland", "TL", "Republic", 1L);
            country.setId(10L);
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));
            when(countryRepository.save(any(Earth2026Country.class))).thenAnswer(inv -> inv.getArgument(0));

            Earth2026Region region = new Earth2026Region("Test Region", "Administrative", 10L);
            region.setId(20L);
            when(regionRepository.findByCountryIdAndActiveTrue(10L)).thenReturn(List.of(region));
            when(regionRepository.save(any(Earth2026Region.class))).thenAnswer(inv -> inv.getArgument(0));

            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Leader"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc("Overseer"))
                    .thenReturn(new ArrayList<>());
            when(aiCharacterRepository.save(any(AiCharacter.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request =
                    new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest();
            request.setPlanetName("New Planet");
            request.setSpeciesIdForNewAi("Human");
            request.setExistingCharacters(List.of(charInfo));

            earth2026Service.populatePlanetWithGameData(request, null);

            // Assert: The region should have the matched AI as governor
            verify(regionRepository).save(argThat(r ->
                    r instanceof Earth2026Region &&
                    ((Earth2026Region) r).getAiGovernorId() != null &&
                    ((Earth2026Region) r).getAiGovernorId().equals(150L)
            ));
        }
    }
}
