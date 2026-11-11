package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.repository.*;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GalaxyMetricsService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026Service Tests")
class Earth2026ServiceTest {

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

    // ---- createCharacter ----

    @Nested
    @DisplayName("createCharacter Tests")
    class CreateCharacterTests {

        @Test
        @DisplayName("should create character with name, age, and occupation")
        void createCharacter_shouldCreateWithAllFields() {
            // Arrange
            when(characterRepository.save(any(Earth2026Character.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Character character = earth2026Service.createCharacter("John Doe", 30, "Detective");

            // Assert
            assertNotNull(character);
            assertEquals("John Doe", character.getName());
            assertEquals(30, character.getAge());
            assertEquals("Detective", character.getOccupation());
            assertEquals("San Diego, CA", character.getEarthLocation());
            assertNotNull(character.getArrivalDateTime());
            assertNotNull(character.getCreatedAt());
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should create character with default ability scores")
        void createCharacter_shouldHaveDefaultAbilityScores() {
            // Arrange
            when(characterRepository.save(any(Earth2026Character.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Character character = earth2026Service.createCharacter("Jane Doe", 25, "Student");

            // Assert
            assertEquals(10, character.getStrScore());
            assertEquals(10, character.getDexScore());
            assertEquals(10, character.getConScore());
            assertEquals(10, character.getIntScore());
            assertEquals(10, character.getWisScore());
            assertEquals(10, character.getChaScore());
        }

        @Test
        @DisplayName("should create character with default level and XP")
        void createCharacter_shouldHaveDefaultLevelAndXp() {
            // Arrange
            when(characterRepository.save(any(Earth2026Character.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Character character = earth2026Service.createCharacter("Test", 20, null);

            // Assert
            assertEquals(1, character.getLevel());
            assertEquals(0, character.getExperiencePoints());
            assertFalse(character.isHasTransitioned());
            assertNull(character.getTargetGalaxy());
        }

        @Test
        @DisplayName("should create character with hit points based on CON score")
        void createCharacter_shouldCalculateHitPoints() {
            // Arrange
            when(characterRepository.save(any(Earth2026Character.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Character character = earth2026Service.createCharacter("Warrior", 25, "Soldier");

            // Assert
            // Default CON=10, so maxHitPoints = 10 + (10-10)/2 = 10
            assertEquals(10, character.getMaxHitPoints());
            assertEquals(10, character.getHitPoints());
        }

        @Test
        @DisplayName("should create character without supernatural mode")
        void createCharacter_shouldNotHaveSupernaturalMode() {
            // Arrange
            when(characterRepository.save(any(Earth2026Character.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Character character = earth2026Service.createCharacter("Normal", 25, "Engineer");

            // Assert
            assertFalse(character.isSupernaturalEnabled());
            assertNull(character.getSanityScore());
        }
    }

    // ---- getCharacter ----

    @Nested
    @DisplayName("getCharacter Tests")
    class GetCharacterTests {

        @Test
        @DisplayName("should return character when found")
        void getCharacter_shouldReturnWhenFound() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Found", 25, "Test");
            character.setId(1L);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            Optional<Earth2026Character> result = earth2026Service.getCharacter(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Found", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void getCharacter_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.getCharacter(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getCharactersOnEarth ----

    @Nested
    @DisplayName("getCharactersOnEarth Tests")
    class GetCharactersOnEarthTests {

        @Test
        @DisplayName("should return characters that have not transitioned")
        void getCharactersOnEarth_shouldReturnNonTransitioned() {
            // Arrange
            Earth2026Character char1 = new Earth2026Character("On Earth", 25, "Test");
            char1.setId(1L);
            char1.setHasTransitioned(false);
            when(characterRepository.findByHasTransitionedFalse()).thenReturn(List.of(char1));

            // Act
            List<Earth2026Character> result = earth2026Service.getCharactersOnEarth();

            // Assert
            assertEquals(1, result.size());
            assertEquals("On Earth", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no characters on Earth")
        void getCharactersOnEarth_shouldReturnEmptyList() {
            // Arrange
            when(characterRepository.findByHasTransitionedFalse()).thenReturn(List.of());

            // Act
            List<Earth2026Character> result = earth2026Service.getCharactersOnEarth();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- moveCharacter ----

    @Nested
    @DisplayName("moveCharacter Tests")
    class MoveCharacterTests {

        @Test
        @DisplayName("should update character location")
        void moveCharacter_shouldUpdateLocation() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Mover", 25, "Traveler");
            character.setId(1L);
            character.setEarthLocation("San Diego, CA");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.moveCharacter(1L, "New York, NY");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("New York, NY", result.get().getEarthLocation());
            assertNotNull(result.get().getUpdatedAt());
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should return empty when character not found")
        void moveCharacter_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.moveCharacter(999L, "London, UK");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- setTargetGalaxy ----

    @Nested
    @DisplayName("setTargetGalaxy Tests")
    class SetTargetGalaxyTests {

        @Test
        @DisplayName("should set target galaxy for character")
        void setTargetGalaxy_shouldSetTarget() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setHasTransitioned(false);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.setTargetGalaxy(1L, "Milky Way");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Milky Way", result.get().getTargetGalaxy());
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should throw when character already transitioned")
        void setTargetGalaxy_shouldThrowWhenAlreadyTransitioned() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Gone", 25, "Test");
            character.setId(1L);
            character.setHasTransitioned(true);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> earth2026Service.setTargetGalaxy(1L, "Milky Way"));
        }

        @Test
        @DisplayName("should return empty when character not found")
        void setTargetGalaxy_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.setTargetGalaxy(999L, "Milky Way");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- completeTransition ----

    @Nested
    @DisplayName("completeTransition Tests")
    class CompleteTransitionTests {

        @Test
        @DisplayName("should complete transition when target galaxy is set")
        void completeTransition_shouldCompleteSuccessfully() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("Milky Way");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.completeTransition(1L);

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get().isHasTransitioned());
            assertEquals("Milky Way", result.get().getTargetGalaxy());
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should throw when character already transitioned")
        void completeTransition_shouldThrowWhenAlreadyTransitioned() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Gone", 25, "Test");
            character.setId(1L);
            character.setHasTransitioned(true);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> earth2026Service.completeTransition(1L));
        }

        @Test
        @DisplayName("should throw when no target galaxy selected")
        void completeTransition_shouldThrowWhenNoTargetGalaxy() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Stuck", 25, "Test");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy(null);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> earth2026Service.completeTransition(1L));
        }

        @Test
        @DisplayName("should return empty when character not found")
        void completeTransition_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.completeTransition(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getAvailableScenarios ----

    @Nested
    @DisplayName("getAvailableScenarios Tests")
    class GetAvailableScenariosTests {

        @Test
        @DisplayName("should return scenarios for character level")
        void getAvailableScenarios_shouldReturnForLevel() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Test Scenario", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setMinLevel(1);
            scenario.setActive(true);
            when(scenarioRepository.findByMinLevelLessThanEqualAndActiveTrue(1))
                    .thenReturn(List.of(scenario));

            // Act
            List<Earth2026Scenario> result = earth2026Service.getAvailableScenarios(1);

            // Assert
            assertEquals(1, result.size());
            assertEquals("Test Scenario", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no scenarios available")
        void getAvailableScenarios_shouldReturnEmpty() {
            // Arrange
            when(scenarioRepository.findByMinLevelLessThanEqualAndActiveTrue(5))
                    .thenReturn(List.of());

            // Act
            List<Earth2026Scenario> result = earth2026Service.getAvailableScenarios(5);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- startScenario ----

    @Nested
    @DisplayName("startScenario Tests")
    class StartScenarioTests {

        @Test
        @DisplayName("should start available scenario")
        void startScenario_shouldStartAvailable() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.AVAILABLE);
            when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
            when(scenarioRepository.save(scenario)).thenReturn(scenario);

            // Act
            Optional<Earth2026Scenario> result = earth2026Service.startScenario(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(Earth2026Scenario.ScenarioStatus.IN_PROGRESS, result.get().getStatus());
            assertNotNull(result.get().getStartedAt());
            verify(scenarioRepository).save(scenario);
        }

        @Test
        @DisplayName("should throw when scenario not available")
        void startScenario_shouldThrowWhenNotAvailable() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Done", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.COMPLETED);
            when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> earth2026Service.startScenario(1L));
        }

        @Test
        @DisplayName("should return empty when scenario not found")
        void startScenario_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Scenario> result = earth2026Service.startScenario(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- completeScenario ----

    @Nested
    @DisplayName("completeScenario Tests")
    class CompleteScenarioTests {

        @Test
        @DisplayName("should complete scenario successfully")
        void completeScenario_shouldCompleteSuccessfully() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.IN_PROGRESS);
            when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
            when(scenarioRepository.save(scenario)).thenReturn(scenario);

            // Act
            Optional<Earth2026Scenario> result = earth2026Service.completeScenario(1L, true);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(Earth2026Scenario.ScenarioStatus.COMPLETED, result.get().getStatus());
            assertNotNull(result.get().getCompletedAt());
        }

        @Test
        @DisplayName("should mark scenario as failed")
        void completeScenario_shouldMarkAsFailed() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.IN_PROGRESS);
            when(scenarioRepository.findById(1L)).thenReturn(Optional.of(scenario));
            when(scenarioRepository.save(scenario)).thenReturn(scenario);

            // Act
            Optional<Earth2026Scenario> result = earth2026Service.completeScenario(1L, false);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(Earth2026Scenario.ScenarioStatus.FAILED, result.get().getStatus());
        }

        @Test
        @DisplayName("should return empty when scenario not found")
        void completeScenario_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(scenarioRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Scenario> result = earth2026Service.completeScenario(999L, true);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- awardXp ----

    @Nested
    @DisplayName("awardXp Tests")
    class AwardXpTests {

        @Test
        @DisplayName("should award XP to character")
        void awardXp_shouldAwardXp() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Hero", 25, "Adventurer");
            character.setId(1L);
            character.setExperiencePoints(0);
            character.setLevel(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.awardXp(1L, 50);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(50, result.get().getExperiencePoints());
            assertEquals(1, result.get().getLevel()); // 1 + 50/100 = 1
        }

        @Test
        @DisplayName("should level up when XP threshold reached")
        void awardXp_shouldLevelUp() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Hero", 25, "Adventurer");
            character.setId(1L);
            character.setExperiencePoints(80);
            character.setLevel(1);
            character.setConScore(10);
            character.setMaxHitPoints(10);
            character.setHitPoints(10);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.awardXp(1L, 30);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(110, result.get().getExperiencePoints());
            assertEquals(2, result.get().getLevel()); // 1 + 110/100 = 2
            // Level up: maxHitPoints += (10-10)/2 + 1 = 1, so 10 + 1 = 11
            assertEquals(11, result.get().getMaxHitPoints());
            assertEquals(11, result.get().getHitPoints()); // HP restored to max
        }

        @Test
        @DisplayName("should level up multiple times")
        void awardXp_shouldLevelUpMultipleTimes() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Hero", 25, "Adventurer");
            character.setId(1L);
            character.setExperiencePoints(0);
            character.setLevel(1);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.awardXp(1L, 350);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(350, result.get().getExperiencePoints());
            assertEquals(4, result.get().getLevel()); // 1 + 350/100 = 4
        }

        @Test
        @DisplayName("should return empty when character not found")
        void awardXp_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.awardXp(999L, 50);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getAllLocations ----

    @Nested
    @DisplayName("getAllLocations Tests")
    class GetAllLocationsTests {

        @Test
        @DisplayName("should return all active locations")
        void getAllLocations_shouldReturnActiveLocations() {
            // Arrange
            Earth2026Location location = new Earth2026Location("San Diego", "San Diego", "USA", "City");
            location.setId(1L);
            location.setActive(true);
            when(locationRepository.findByActiveTrue()).thenReturn(List.of(location));

            // Act
            List<Earth2026Location> result = earth2026Service.getAllLocations();

            // Assert
            assertEquals(1, result.size());
            assertEquals("San Diego", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no locations")
        void getAllLocations_shouldReturnEmpty() {
            // Arrange
            when(locationRepository.findByActiveTrue()).thenReturn(List.of());

            // Act
            List<Earth2026Location> result = earth2026Service.getAllLocations();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getPortalLocations ----

    @Nested
    @DisplayName("getPortalLocations Tests")
    class GetPortalLocationsTests {

        @Test
        @DisplayName("should return portal locations")
        void getPortalLocations_shouldReturnPortals() {
            // Arrange
            Earth2026Location portal = new Earth2026Location("Portal Room", "San Diego", "USA", "Portal");
            portal.setId(1L);
            portal.setHasPortal(true);
            portal.setPortalGalaxy("Milky Way");
            when(locationRepository.findByHasPortalTrue()).thenReturn(List.of(portal));

            // Act
            List<Earth2026Location> result = earth2026Service.getPortalLocations();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isHasPortal());
            assertEquals("Milky Way", result.get(0).getPortalGalaxy());
        }
    }

    // ---- getLocation ----

    @Nested
    @DisplayName("getLocation Tests")
    class GetLocationTests {

        @Test
        @DisplayName("should return location when found")
        void getLocation_shouldReturnWhenFound() {
            // Arrange
            Earth2026Location location = new Earth2026Location("NYC", "New York", "USA", "City");
            location.setId(1L);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

            // Act
            Optional<Earth2026Location> result = earth2026Service.getLocation(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("NYC", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void getLocation_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(locationRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Location> result = earth2026Service.getLocation(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- enableSupernatural ----

    @Nested
    @DisplayName("enableSupernatural Tests")
    class EnableSupernaturalTests {

        @Test
        @DisplayName("should enable supernatural mode and set sanity to 100")
        void enableSupernatural_shouldEnableAndSetSanity() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Investigator", 30, "Detective");
            character.setId(1L);
            character.setSupernaturalEnabled(false);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.enableSupernatural(1L);

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get().isSupernaturalEnabled());
            assertEquals(100, result.get().getSanityScore());
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should return empty when character not found")
        void enableSupernatural_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.enableSupernatural(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- reduceSanity ----

    @Nested
    @DisplayName("reduceSanity Tests")
    class ReduceSanityTests {

        @Test
        @DisplayName("should reduce sanity by amount")
        void reduceSanity_shouldReduceByAmount() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Investigator", 30, "Detective");
            character.setId(1L);
            character.setSupernaturalEnabled(true);
            character.setSanityScore(100);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.reduceSanity(1L, 20);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(80, result.get().getSanityScore());
        }

        @Test
        @DisplayName("should not go below 0 sanity")
        void reduceSanity_shouldNotGoBelowZero() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Crazy", 30, "Detective");
            character.setId(1L);
            character.setSupernaturalEnabled(true);
            character.setSanityScore(10);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.reduceSanity(1L, 50);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(0, result.get().getSanityScore());
        }

        @Test
        @DisplayName("should throw when supernatural mode not enabled")
        void reduceSanity_shouldThrowWhenNotSupernatural() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Normal", 30, "Test");
            character.setId(1L);
            character.setSupernaturalEnabled(false);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> earth2026Service.reduceSanity(1L, 10));
        }

        @Test
        @DisplayName("should default sanity to 100 when null")
        void reduceSanity_shouldDefaultSanityTo100() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Newbie", 30, "Detective");
            character.setId(1L);
            character.setSupernaturalEnabled(true);
            character.setSanityScore(null);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(character)).thenReturn(character);

            // Act
            Optional<Earth2026Character> result = earth2026Service.reduceSanity(1L, 15);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(85, result.get().getSanityScore());
        }

        @Test
        @DisplayName("should return empty when character not found")
        void reduceSanity_shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Earth2026Character> result = earth2026Service.reduceSanity(999L, 10);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getAllTechLevels ----

    @Nested
    @DisplayName("getAllTechLevels Tests")
    class GetAllTechLevelsTests {

        @Test
        @DisplayName("should return all 21 tech levels")
        void getAllTechLevels_shouldReturnAll21() {
            List<TechLevel> levels = earth2026Service.getAllTechLevels();
            assertEquals(21, levels.size());
        }

        @Test
        @DisplayName("should include TL0 and TL20")
        void getAllTechLevels_shouldIncludeExtremes() {
            List<TechLevel> levels = earth2026Service.getAllTechLevels();
            assertTrue(levels.contains(TechLevel.TL0));
            assertTrue(levels.contains(TechLevel.TL20));
        }
    }

    // ---- getTechLevel ----

    @Nested
    @DisplayName("getTechLevel Tests")
    class GetTechLevelTests {

        @Test
        @DisplayName("should find TL4")
        void getTechLevel_TL4() {
            Optional<TechLevel> result = earth2026Service.getTechLevel(4);
            assertTrue(result.isPresent());
            assertEquals(TechLevel.TL4, result.get());
        }

        @Test
        @DisplayName("should find TL0")
        void getTechLevel_TL0() {
            Optional<TechLevel> result = earth2026Service.getTechLevel(0);
            assertTrue(result.isPresent());
            assertEquals(TechLevel.TL0, result.get());
        }

        @Test
        @DisplayName("should return empty for invalid level")
        void getTechLevel_invalid() {
            Optional<TechLevel> result = earth2026Service.getTechLevel(99);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty for negative level")
        void getTechLevel_negative() {
            Optional<TechLevel> result = earth2026Service.getTechLevel(-1);
            assertTrue(result.isEmpty());
        }
    }

    // ---- getAllTimePeriodTemplates ----

    @Nested
    @DisplayName("getAllTimePeriodTemplates Tests")
    class GetAllTimePeriodTemplatesTests {

        @Test
        @DisplayName("should return all 12 time period templates")
        void getAllTimePeriodTemplates_shouldReturnAll12() {
            List<TimePeriodTemplate> periods = earth2026Service.getAllTimePeriodTemplates();
            assertEquals(12, periods.size());
        }

        @Test
        @DisplayName("should include WWII and MODERN")
        void getAllTimePeriodTemplates_shouldIncludeWWII() {
            List<TimePeriodTemplate> periods = earth2026Service.getAllTimePeriodTemplates();
            assertTrue(periods.contains(TimePeriodTemplate.WWII));
            assertTrue(periods.contains(TimePeriodTemplate.MODERN));
        }
    }

    // ---- getTimePeriodTemplate ----

    @Nested
    @DisplayName("getTimePeriodTemplate Tests")
    class GetTimePeriodTemplateTests {

        @Test
        @DisplayName("should find WWII by name")
        void getTimePeriodTemplate_WWII() {
            Optional<TimePeriodTemplate> result = earth2026Service.getTimePeriodTemplate("World War II");
            assertTrue(result.isPresent());
            assertEquals(TimePeriodTemplate.WWII, result.get());
        }

        @Test
        @DisplayName("should find by case-insensitive name")
        void getTimePeriodTemplate_caseInsensitive() {
            Optional<TimePeriodTemplate> result = earth2026Service.getTimePeriodTemplate("modern");
            assertTrue(result.isPresent());
            assertEquals(TimePeriodTemplate.MODERN, result.get());
        }

        @Test
        @DisplayName("should return empty for unknown name")
        void getTimePeriodTemplate_unknown() {
            Optional<TimePeriodTemplate> result = earth2026Service.getTimePeriodTemplate("Unknown Era");
            assertTrue(result.isEmpty());
        }
    }

    // ---- getStructuresAtLocation ----

    @Nested
    @DisplayName("getStructuresAtLocation Tests")
    class GetStructuresAtLocationTests {

        @Test
        @DisplayName("should return structures for location")
        void getStructuresAtLocation_success() {
            // Arrange
            Earth2026Structure s1 = new Earth2026Structure("Bunker 1", 1L, "Bunker", 4);
            s1.setId(10L);
            Earth2026Structure s2 = new Earth2026Structure("Airfield", 1L, "Airfield", 4);
            s2.setId(11L);
            when(structureRepository.findByLocationIdAndActiveTrue(1L))
                    .thenReturn(List.of(s1, s2));

            // Act
            List<Earth2026Structure> result = earth2026Service.getStructuresAtLocation(1L);

            // Assert
            assertEquals(2, result.size());
            assertEquals("Bunker 1", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no structures")
        void getStructuresAtLocation_empty() {
            when(structureRepository.findByLocationIdAndActiveTrue(99L))
                    .thenReturn(List.of());

            List<Earth2026Structure> result = earth2026Service.getStructuresAtLocation(99L);
            assertTrue(result.isEmpty());
        }
    }

    // ---- getStructure ----

    @Nested
    @DisplayName("getStructure Tests")
    class GetStructureTests {

        @Test
        @DisplayName("should find structure by ID")
        void getStructure_found() {
            // Arrange
            Earth2026Structure s = new Earth2026Structure("Bunker 7", 1L, "Bunker", 4);
            s.setId(10L);
            when(structureRepository.findById(10L)).thenReturn(Optional.of(s));

            // Act
            Optional<Earth2026Structure> result = earth2026Service.getStructure(10L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Bunker 7", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void getStructure_notFound() {
            when(structureRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Structure> result = earth2026Service.getStructure(999L);
            assertTrue(result.isEmpty());
        }
    }

    // ---- buildBattleEnvironment ----

    @Nested
    @DisplayName("buildBattleEnvironment Tests")
    class BuildBattleEnvironmentTests {

        @Test
        @DisplayName("should build environment with WWII template")
        void buildBattleEnvironment_WWII() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "CLEAR");

            // Assert
            assertEquals(1L, env.getLocationId());
            assertEquals("Berlin", env.getLocationName());
            assertEquals(4, env.getBaseTechLevel());
            assertEquals("World War II", env.getTimePeriod());
            assertEquals("URBAN", env.getTerrainType());
            assertNotNull(env.getGeneratedAt());
        }

        @Test
        @DisplayName("should include structures in environment")
        void buildBattleEnvironment_withStructures() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

            Earth2026Structure s = new Earth2026Structure("Bunker 7", 1L, "Bunker", 4);
            s.setId(10L);
            s.setDefenseRating(80);
            s.setMaxCapacity(50);
            s.setProvidesCover(true);
            s.setCoverBonus(3);
            s.setStrategic(true);
            s.setActive(true);
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of(s));

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "CLEAR");

            // Assert
            assertEquals(1, env.getStructures().size());
            assertEquals("Bunker 7", env.getStructures().get(0).getName());
        }

        @Test
        @DisplayName("should include units based on tech level")
        void buildBattleEnvironment_withUnits() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "CLEAR");

            // Assert
            // WWII is TL4, so should have Tanks, Riflemen, etc.
            assertTrue(env.getAvailableUnits().size() > 0);
            assertTrue(env.getAvailableUnits().stream()
                    .anyMatch(u -> u.getUnitType().equals("Tanks")));
        }

        @Test
        @DisplayName("should apply weather modifiers")
        void buildBattleEnvironment_weatherModifiers() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "FOG");

            // Assert
            assertEquals(-3, env.getVisibilityModifier());
            assertEquals(-1, env.getMovementModifier());
        }

        @Test
        @DisplayName("should throw when location not found")
        void buildBattleEnvironment_locationNotFound() {
            when(locationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> earth2026Service.buildBattleEnvironment(999L, "WWII", "URBAN", "CLEAR"));
        }

        @Test
        @DisplayName("should throw when time period not found")
        void buildBattleEnvironment_timePeriodNotFound() {
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

            assertThrows(IllegalArgumentException.class,
                    () -> earth2026Service.buildBattleEnvironment(1L, "Unknown Era", "URBAN", "CLEAR"));
        }

        @Test
        @DisplayName("should use default terrain and weather")
        void buildBattleEnvironment_defaults() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II");

            // Assert
            assertEquals("URBAN", env.getTerrainType());
            assertEquals(0, env.getVisibilityModifier()); // CLEAR weather
        }

        @Test
        @DisplayName("should handle case-insensitive weather")
        void buildBattleEnvironment_weatherCaseInsensitive() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "rain");

            // Assert
            assertEquals(-1, env.getVisibilityModifier()); // RAIN
        }

        @Test
        @DisplayName("should default to CLEAR for invalid weather")
        void buildBattleEnvironment_invalidWeather() {
            // Arrange
            Earth2026Location location = new Earth2026Location("Berlin", "Berlin", "Germany", "City");
            location.setId(1L);
            location.setActive(true);
            location.setLatitude(52.52);
            location.setLongitude(13.405);
            when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "INVALID");

            // Assert
            assertEquals(0, env.getVisibilityModifier()); // defaults to CLEAR
        }

        @Test
        @DisplayName("should use MODERN template")
        void buildBattleEnvironment_MODERN() {
            // Arrange
            Earth2026Location location = new Earth2026Location("New York", "New York", "USA", "City");
            location.setId(3L);
            location.setActive(true);
            location.setLatitude(40.7128);
            location.setLongitude(-74.006);
            when(locationRepository.findById(3L)).thenReturn(Optional.of(location));
            when(structureRepository.findByLocationIdAndActiveTrue(3L)).thenReturn(List.of());

            // Act
            var env = earth2026Service.buildBattleEnvironment(3L, "MODERN", "URBAN", "CLEAR");

            // Assert
            assertEquals(5, env.getBaseTechLevel());
            assertEquals("Modern", env.getTimePeriod());
        }
    }

    // ---- getAvailableUnitsForTechLevel ----

    @Nested
    @DisplayName("getAvailableUnitsForTechLevel Tests")
    class GetAvailableUnitsForTechLevelTests {

        @Test
        @DisplayName("TL0 should have only Infantry")
        void getAvailableUnitsForTechLevel_TL0() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(0);
            assertEquals(1, units.size());
            assertEquals("Infantry", units.get(0).getUnitType());
        }

        @Test
        @DisplayName("TL1 should have Infantry and Cavalry")
        void getAvailableUnitsForTechLevel_TL1() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(1);
            assertEquals(2, units.size());
        }

        @Test
        @DisplayName("TL4 should have WWII units")
        void getAvailableUnitsForTechLevel_TL4() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(4);
            // Infantry, Cavalry, Knights, Archers, Musketeers, Artillery, Riflemen, Machine Gun, Tanks, Artillery, Anti-Tank, Fighters, Bombers
            assertTrue(units.size() >= 7);
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Tanks")));
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Fighters")));
        }

        @Test
        @DisplayName("TL5 should have modern units")
        void getAvailableUnitsForTechLevel_TL5() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(5);
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Helicopters")));
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Missiles")));
        }

        @Test
        @DisplayName("TL6 should have advanced units")
        void getAvailableUnitsForTechLevel_TL6() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(6);
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Drones")));
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Nuclear")));
        }

        @Test
        @DisplayName("TL10 should have Jump Ships")
        void getAvailableUnitsForTechLevel_TL10() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(10);
            assertTrue(units.stream().anyMatch(u -> u.getUnitType().equals("Jump Ship")));
        }

        @Test
        @DisplayName("TL20 should have all units")
        void getAvailableUnitsForTechLevel_TL20() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units =
                    earth2026Service.getAvailableUnitsForTechLevel(20);
            assertTrue(units.size() > 10);
        }
    }

    // ---- getCharactersAtLocation ----

    @Nested
    @DisplayName("getCharactersAtLocation Tests")
    class GetCharactersAtLocationTests {

        @Test
        @DisplayName("should return characters at a specific location")
        void getCharactersAtLocation_success() {
            Earth2026Character char1 = new Earth2026Character("Alice", 30, "Detective");
            char1.setId(1L);
            char1.setEarthLocation("San Diego, CA");
            Earth2026Character char2 = new Earth2026Character("Bob", 25, "Engineer");
            char2.setId(2L);
            char2.setEarthLocation("San Diego, CA");
            when(characterRepository.findByEarthLocation("San Diego, CA"))
                    .thenReturn(List.of(char1, char2));

            List<Earth2026Character> result = earth2026Service.getCharactersAtLocation("San Diego, CA");

            assertEquals(2, result.size());
            assertEquals("Alice", result.get(0).getName());
            assertEquals("Bob", result.get(1).getName());
        }

        @Test
        @DisplayName("should return empty list when no characters at location")
        void getCharactersAtLocation_empty() {
            when(characterRepository.findByEarthLocation("Unknown City"))
                    .thenReturn(List.of());

            List<Earth2026Character> result = earth2026Service.getCharactersAtLocation("Unknown City");
            assertTrue(result.isEmpty());
        }
    }

    // ---- getLocationsNearby ----

    @Nested
    @DisplayName("getLocationsNearby Tests")
    class GetLocationsNearbyTests {

        @Test
        @DisplayName("should return locations within radius using Haversine formula")
        void getLocationsNearby_withinRadius() {
            // San Diego: 32.7157, -117.1611
            Earth2026Location sd = new Earth2026Location("San Diego", "San Diego", "USA", "City");
            sd.setId(1L);
            sd.setActive(true);
            sd.setLatitude(32.7157);
            sd.setLongitude(-117.1611);

            // Los Angeles: 34.0522, -118.2437 (~120km from San Diego)
            Earth2026Location la = new Earth2026Location("Los Angeles", "Los Angeles", "USA", "City");
            la.setId(2L);
            la.setActive(true);
            la.setLatitude(34.0522);
            la.setLongitude(-118.2437);

            // Tokyo: 35.6762, 139.6503 (~8500km from San Diego)
            Earth2026Location tokyo = new Earth2026Location("Tokyo", "Tokyo", "Japan", "City");
            tokyo.setId(6L);
            tokyo.setActive(true);
            tokyo.setLatitude(35.6762);
            tokyo.setLongitude(139.6503);

            when(locationRepository.findByActiveTrue())
                    .thenReturn(List.of(sd, la, tokyo));

            // 200km radius from San Diego should include San Diego and LA, but not Tokyo
            List<Earth2026Location> result = earth2026Service.getLocationsNearby(32.7157, -117.1611, 200.0);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(l -> l.getName().equals("San Diego")));
            assertTrue(result.stream().anyMatch(l -> l.getName().equals("Los Angeles")));
        }

        @Test
        @DisplayName("should exclude locations without coordinates")
        void getLocationsNearby_excludeNullCoordinates() {
            Earth2026Location withCoords = new Earth2026Location("NYC", "New York", "USA", "City");
            withCoords.setId(3L);
            withCoords.setActive(true);
            withCoords.setLatitude(40.7128);
            withCoords.setLongitude(-74.006);

            Earth2026Location noCoords = new Earth2026Location("Secret", "Unknown", "Unknown", "Secret");
            noCoords.setId(99L);
            noCoords.setActive(true);
            // No latitude/longitude set

            when(locationRepository.findByActiveTrue())
                    .thenReturn(List.of(withCoords, noCoords));

            List<Earth2026Location> result = earth2026Service.getLocationsNearby(40.7128, -74.006, 100.0);

            assertEquals(1, result.size());
            assertEquals("NYC", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no locations in radius")
        void getLocationsNearby_empty() {
            Earth2026Location tokyo = new Earth2026Location("Tokyo", "Tokyo", "Japan", "City");
            tokyo.setId(6L);
            tokyo.setActive(true);
            tokyo.setLatitude(35.6762);
            tokyo.setLongitude(139.6503);

            when(locationRepository.findByActiveTrue()).thenReturn(List.of(tokyo));

            // Search near San Diego with small radius
            List<Earth2026Location> result = earth2026Service.getLocationsNearby(32.7157, -117.1611, 10.0);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no active locations")
        void getLocationsNearby_noActiveLocations() {
            when(locationRepository.findByActiveTrue()).thenReturn(List.of());

            List<Earth2026Location> result = earth2026Service.getLocationsNearby(0.0, 0.0, 1000.0);
            assertTrue(result.isEmpty());
        }
    }

    // ---- mapStatsToGame ----

    @Nested
    @DisplayName("mapStatsToGame Tests")
    class MapStatsToGameTests {

        private Earth2026Character createCharacter() {
            Earth2026Character character = new Earth2026Character("Test", 30, "Adventurer");
            character.setStrScore(15);
            character.setDexScore(12);
            character.setConScore(14);
            character.setIntScore(16);
            character.setWisScore(10);
            character.setChaScore(8);
            return character;
        }

        @Test
        @DisplayName("DND_5E should map directly 1:1")
        void mapStatsToGame_DND_5E() {
            Earth2026Character character = createCharacter();
            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.DND_5E);

            assertEquals(6, stats.size());
            assertEquals(15, stats.get("STR"));
            assertEquals(12, stats.get("DEX"));
            assertEquals(14, stats.get("CON"));
            assertEquals(16, stats.get("INT"));
            assertEquals(10, stats.get("WIS"));
            assertEquals(8, stats.get("CHA"));
        }

        @Test
        @DisplayName("EARTH_2026 should map directly 1:1")
        void mapStatsToGame_EARTH_2026() {
            Earth2026Character character = createCharacter();
            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.EARTH_2026);

            assertEquals(6, stats.size());
            assertEquals(15, stats.get("STR"));
            assertEquals(12, stats.get("DEX"));
        }

        @Test
        @DisplayName("CAPTAINS_LOG should map to Star Trek stats")
        void mapStatsToGame_CAPTAINS_LOG() {
            Earth2026Character character = createCharacter();
            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.CAPTAINS_LOG);

            assertEquals(6, stats.size());
            assertEquals(15, stats.get("Strength"));   // STR
            assertEquals(12, stats.get("Fitness"));    // DEX
            assertEquals(16, stats.get("Reason"));     // INT
            assertEquals(10, stats.get("Insight"));    // WIS
            assertEquals(8, stats.get("Presence"));    // CHA
            assertEquals(14, stats.get("Constitution"));// CON
        }

        @Test
        @DisplayName("TRAVELLER should map to D6 stats (2-18 range)")
        void mapStatsToGame_TRAVELLER() {
            Earth2026Character character = createCharacter();
            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.TRAVELLER);

            assertEquals(6, stats.size());
            assertTrue(stats.containsKey("STR"));
            assertTrue(stats.containsKey("DEX"));
            assertTrue(stats.containsKey("END"));
            assertTrue(stats.containsKey("INT"));
            assertTrue(stats.containsKey("EDU"));
            assertTrue(stats.containsKey("SOC"));
            // All values should be in 2-18 range
            for (int value : stats.values()) {
                assertTrue(value >= 2 && value <= 18, "Traveller stat out of range: " + value);
            }
        }

        @Test
        @DisplayName("DORAMCU_4X should map to leadership/diplomacy/industry/science")
        void mapStatsToGame_DORAMCU_4X() {
            Earth2026Character character = createCharacter();
            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.DORAMCU_4X);

            assertEquals(4, stats.size());
            // Leadership = (STR + CHA) / 2 = (15 + 8) / 2 = 11
            assertEquals(11, stats.get("Leadership"));
            // Diplomacy = (CHA + WIS) / 2 = (8 + 10) / 2 = 9
            assertEquals(9, stats.get("Diplomacy"));
            // Industry = (STR + DEX) / 2 = (15 + 12) / 2 = 13
            assertEquals(13, stats.get("Industry"));
            // Science = (INT + WIS) / 2 = (16 + 10) / 2 = 13
            assertEquals(13, stats.get("Science"));
        }

        @Test
        @DisplayName("THIRD_REICH should map to command/tactics/logistics")
        void mapStatsToGame_THIRD_REICH() {
            Earth2026Character character = createCharacter();
            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.THIRD_REICH);

            assertEquals(3, stats.size());
            // Command = CHA
            assertEquals(8, stats.get("Command"));
            // Tactics = INT
            assertEquals(16, stats.get("Tactics"));
            // Logistics = WIS
            assertEquals(10, stats.get("Logistics"));
        }

        @Test
        @DisplayName("should return non-empty map for all game types")
        void mapStatsToGame_allGameTypes() {
            Earth2026Character character = createCharacter();
            for (GameType gt : GameType.values()) {
                Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, gt);
                assertFalse(stats.isEmpty(), "Stats map empty for " + gt);
            }
        }

        @Test
        @DisplayName("TRAVELLER stat scaling should clamp to 2-18")
        void mapStatsToGame_TravellerClamping() {
            Earth2026Character character = new Earth2026Character("Test", 30, "Test");
            character.setStrScore(1);    // Minimum
            character.setDexScore(20);   // Maximum

            Map<String, Integer> stats = earth2026Service.mapStatsToGame(character, GameType.TRAVELLER);

            // STR from score 1 should be clamped to 2 (minimum)
            assertEquals(2, stats.get("STR"));
            // DEX from score 20 should be clamped to 18 (maximum)
            assertEquals(18, stats.get("DEX"));
        }
    }

    // ---- getCountryAiLeader ----

    @Nested
    @DisplayName("getCountryAiLeader Tests")
    class GetCountryAiLeaderTests {

        @Test
        @DisplayName("should return AI leader for country with assigned leader")
        void getCountryAiLeader_success() {
            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country.setAiLeaderId(2L);
            AiCharacter ai = new AiCharacter("Joe Biden", "President", "United States");
            ai.setId(2L);

            when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
            when(aiCharacterRepository.findById(2L)).thenReturn(Optional.of(ai));

            Optional<AiCharacter> result = earth2026Service.getCountryAiLeader(1L);

            assertTrue(result.isPresent());
            assertEquals("Joe Biden", result.get().getName());
            assertEquals("President", result.get().getRole());
        }

        @Test
        @DisplayName("should return empty for country without AI leader")
        void getCountryAiLeader_noLeader() {
            Earth2026Country country = new Earth2026Country("Test", "TT", "Republic", 1L);
            country.setAiLeaderId(null);

            when(countryRepository.findById(1L)).thenReturn(Optional.of(country));

            Optional<AiCharacter> result = earth2026Service.getCountryAiLeader(1L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty for non-existent country")
        void getCountryAiLeader_notFound() {
            when(countryRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<AiCharacter> result = earth2026Service.getCountryAiLeader(999L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- getRegionAiGovernor ----

    @Nested
    @DisplayName("getRegionAiGovernor Tests")
    class GetRegionAiGovernorTests {

        @Test
        @DisplayName("should return AI governor for region with assigned governor")
        void getRegionAiGovernor_success() {
            Earth2026Region region = new Earth2026Region("California", "State", 1L);
            region.setAiGovernorId(22L);
            AiCharacter ai = new AiCharacter("Gavin Newsom", "Governor", "United States");
            ai.setId(22L);

            when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
            when(aiCharacterRepository.findById(22L)).thenReturn(Optional.of(ai));

            Optional<AiCharacter> result = earth2026Service.getRegionAiGovernor(1L);

            assertTrue(result.isPresent());
            assertEquals("Gavin Newsom", result.get().getName());
            assertEquals("Governor", result.get().getRole());
        }

        @Test
        @DisplayName("should return empty for region without AI governor")
        void getRegionAiGovernor_noGovernor() {
            Earth2026Region region = new Earth2026Region("Test", "State", 1L);
            region.setAiGovernorId(null);

            when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

            Optional<AiCharacter> result = earth2026Service.getRegionAiGovernor(1L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty for non-existent region")
        void getRegionAiGovernor_notFound() {
            when(regionRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<AiCharacter> result = earth2026Service.getRegionAiGovernor(999L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- getAllCountryAiLeaders ----

    @Nested
    @DisplayName("getAllCountryAiLeaders Tests")
    class GetAllCountryAiLeadersTests {

        @Test
        @DisplayName("should return all AI leaders for a planet")
        void getAllCountryAiLeaders_success() {
            Earth2026Country country1 = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country1.setAiLeaderId(2L);
            Earth2026Country country2 = new Earth2026Country("China", "CN", "Republic", 1L);
            country2.setAiLeaderId(3L);

            AiCharacter ai1 = new AiCharacter("Joe Biden", "President", "United States");
            ai1.setId(2L);
            AiCharacter ai2 = new AiCharacter("Xi Jinping", "President", "China");
            ai2.setId(3L);

            when(countryRepository.findByPlanetIdAndActiveTrue(1L))
                    .thenReturn(List.of(country1, country2));
            when(aiCharacterRepository.findById(2L)).thenReturn(Optional.of(ai1));
            when(aiCharacterRepository.findById(3L)).thenReturn(Optional.of(ai2));

            List<AiCharacter> result = earth2026Service.getAllCountryAiLeaders(1L);

            assertEquals(2, result.size());
            assertEquals("Joe Biden", result.get(0).getName());
            assertEquals("Xi Jinping", result.get(1).getName());
        }

        @Test
        @DisplayName("should skip countries without AI leaders")
        void getAllCountryAiLeaders_skipNullLeaders() {
            Earth2026Country country = new Earth2026Country("Test", "TT", "Republic", 1L);
            country.setAiLeaderId(null);

            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of(country));

            List<AiCharacter> result = earth2026Service.getAllCountryAiLeaders(1L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for planet with no countries")
        void getAllCountryAiLeaders_empty() {
            when(countryRepository.findByPlanetIdAndActiveTrue(1L)).thenReturn(List.of());

            List<AiCharacter> result = earth2026Service.getAllCountryAiLeaders(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- getAllRegionAiGovernors ----

    @Nested
    @DisplayName("getAllRegionAiGovernors Tests")
    class GetAllRegionAiGovernorsTests {

        @Test
        @DisplayName("should return all AI governors for a country")
        void getAllRegionAiGovernors_success() {
            Earth2026Region region1 = new Earth2026Region("California", "State", 1L);
            region1.setAiGovernorId(22L);
            Earth2026Region region2 = new Earth2026Region("Texas", "State", 1L);
            region2.setAiGovernorId(23L);

            AiCharacter ai1 = new AiCharacter("Gavin Newsom", "Governor", "United States");
            ai1.setId(22L);
            AiCharacter ai2 = new AiCharacter("Greg Abbott", "Governor", "United States");
            ai2.setId(23L);

            when(regionRepository.findByCountryIdAndActiveTrue(1L))
                    .thenReturn(List.of(region1, region2));
            when(aiCharacterRepository.findById(22L)).thenReturn(Optional.of(ai1));
            when(aiCharacterRepository.findById(23L)).thenReturn(Optional.of(ai2));

            List<AiCharacter> result = earth2026Service.getAllRegionAiGovernors(1L);

            assertEquals(2, result.size());
            assertEquals("Gavin Newsom", result.get(0).getName());
            assertEquals("Greg Abbott", result.get(1).getName());
        }

        @Test
        @DisplayName("should return empty list for country with no regions")
        void getAllRegionAiGovernors_empty() {
            when(regionRepository.findByCountryIdAndActiveTrue(1L)).thenReturn(List.of());

            List<AiCharacter> result = earth2026Service.getAllRegionAiGovernors(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- assignCountryAiLeader ----

    @Nested
    @DisplayName("assignCountryAiLeader Tests")
    class AssignCountryAiLeaderTests {

        @Test
        @DisplayName("should assign AI leader to country")
        void assignCountryAiLeader_success() {
            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            AiCharacter ai = new AiCharacter("Joe Biden", "President", "United States");
            ai.setId(2L);

            when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
            when(aiCharacterRepository.findById(2L)).thenReturn(Optional.of(ai));
            when(countryRepository.save(country)).thenReturn(country);

            Optional<Earth2026Country> result = earth2026Service.assignCountryAiLeader(1L, 2L);

            assertTrue(result.isPresent());
            assertEquals(2L, result.get().getAiLeaderId());
            assertEquals("Joe Biden", result.get().getLeaderName());
            assertEquals("President", result.get().getLeaderTitle());
            verify(countryRepository).save(country);
        }

        @Test
        @DisplayName("should return empty for non-existent country")
        void assignCountryAiLeader_countryNotFound() {
            when(countryRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Country> result = earth2026Service.assignCountryAiLeader(999L, 2L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw for non-existent AI character")
        void assignCountryAiLeader_aiNotFound() {
            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);

            when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
            when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> earth2026Service.assignCountryAiLeader(1L, 999L));
        }
    }

    // ---- assignRegionAiGovernor ----

    @Nested
    @DisplayName("assignRegionAiGovernor Tests")
    class AssignRegionAiGovernorTests {

        @Test
        @DisplayName("should assign AI governor to region")
        void assignRegionAiGovernor_success() {
            Earth2026Region region = new Earth2026Region("California", "State", 1L);
            AiCharacter ai = new AiCharacter("Gavin Newsom", "Governor", "United States");
            ai.setId(22L);

            when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
            when(aiCharacterRepository.findById(22L)).thenReturn(Optional.of(ai));
            when(regionRepository.save(region)).thenReturn(region);

            Optional<Earth2026Region> result = earth2026Service.assignRegionAiGovernor(1L, 22L);

            assertTrue(result.isPresent());
            assertEquals(22L, result.get().getAiGovernorId());
            assertEquals("Gavin Newsom", result.get().getGovernorName());
            assertEquals("Governor", result.get().getGovernorTitle());
            verify(regionRepository).save(region);
        }

        @Test
        @DisplayName("should return empty for non-existent region")
        void assignRegionAiGovernor_regionNotFound() {
            when(regionRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Region> result = earth2026Service.assignRegionAiGovernor(999L, 22L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw for non-existent AI character")
        void assignRegionAiGovernor_aiNotFound() {
            Earth2026Region region = new Earth2026Region("California", "State", 1L);

            when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
            when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> earth2026Service.assignRegionAiGovernor(1L, 999L));
        }
    }
}
