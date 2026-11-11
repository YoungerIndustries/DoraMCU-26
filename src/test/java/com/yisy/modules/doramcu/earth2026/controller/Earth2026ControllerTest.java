package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Scenario;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026Controller Tests")
class Earth2026ControllerTest {

    private Earth2026Service earth2026Service;
    private Earth2026Controller controller;

    @BeforeEach
    void setUp() {
        earth2026Service = mock(Earth2026Service.class);
        controller = new Earth2026Controller(earth2026Service);
    }

    // ---- createCharacter ----

    @Nested
    @DisplayName("createCharacter Tests")
    class CreateCharacterTests {

        @Test
        @DisplayName("should create character with name, age, and occupation")
        void createCharacter_shouldCreateWithAllFields() {
            // Arrange
            Earth2026Character character = new Earth2026Character("John Doe", 30, "Detective");
            character.setId(1L);
            when(earth2026Service.createCharacter("John Doe", 30, "Detective", null)).thenReturn(character);

            // Act
            ResponseEntity<?> response = controller.createCharacter(
                    Map.of("name", "John Doe", "age", 30, "occupation", "Detective"));

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.getBody() instanceof Earth2026Character);
            assertEquals("John Doe", ((Earth2026Character) response.getBody()).getName());
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void createCharacter_shouldReturn400WhenNameMissing() {
            // Act
            ResponseEntity<?> response = controller.createCharacter(Map.of("age", 25));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void createCharacter_shouldReturn400WhenNameBlank() {
            // Act
            ResponseEntity<?> response = controller.createCharacter(Map.of("name", "  "));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should use default age when not provided")
        void createCharacter_shouldUseDefaultAge() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Jane", 25, null);
            character.setId(1L);
            when(earth2026Service.createCharacter("Jane", 25, null, null)).thenReturn(character);

            // Act
            ResponseEntity<?> response = controller.createCharacter(Map.of("name", "Jane"));

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(earth2026Service).createCharacter("Jane", 25, null, null);
        }
    }

    // ---- getCharactersOnEarth ----

    @Nested
    @DisplayName("getCharactersOnEarth Tests")
    class GetCharactersOnEarthTests {

        @Test
        @DisplayName("should return list of characters on Earth")
        void getCharactersOnEarth_shouldReturnList() {
            // Arrange
            Earth2026Character char1 = new Earth2026Character("On Earth", 25, "Test");
            char1.setId(1L);
            when(earth2026Service.getCharactersOnEarth()).thenReturn(List.of(char1));

            // Act
            ResponseEntity<List<Earth2026Character>> response = controller.getCharactersOnEarth();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("should return empty list when no characters")
        void getCharactersOnEarth_shouldReturnEmptyList() {
            // Arrange
            when(earth2026Service.getCharactersOnEarth()).thenReturn(List.of());

            // Act
            ResponseEntity<List<Earth2026Character>> response = controller.getCharactersOnEarth();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
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
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.getCharacter(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody() instanceof Earth2026Character);
        }

        @Test
        @DisplayName("should return 404 when not found")
        void getCharacter_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.getCharacter(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.getCharacter(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- moveCharacter ----

    @Nested
    @DisplayName("moveCharacter Tests")
    class MoveCharacterTests {

        @Test
        @DisplayName("should move character to new location")
        void moveCharacter_shouldMoveToNewLocation() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Mover", 25, "Traveler");
            character.setId(1L);
            character.setEarthLocation("New York, NY");
            when(earth2026Service.moveCharacter(1L, "New York, NY")).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.moveCharacter(1L, Map.of("location", "New York, NY"));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("New York, NY", ((Earth2026Character) response.getBody()).getEarthLocation());
        }

        @Test
        @DisplayName("should return 400 when location is missing")
        void moveCharacter_shouldReturn400WhenLocationMissing() {
            // Act
            ResponseEntity<?> response = controller.moveCharacter(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void moveCharacter_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.moveCharacter(999L, "London, UK")).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.moveCharacter(999L, Map.of("location", "London, UK"));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- setTargetGalaxy ----

    @Nested
    @DisplayName("setTargetGalaxy Tests")
    class SetTargetGalaxyTests {

        @Test
        @DisplayName("should set target galaxy")
        void setTargetGalaxy_shouldSetTarget() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setTargetGalaxy("Milky Way");
            when(earth2026Service.setTargetGalaxy(1L, "Milky Way")).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.setTargetGalaxy(1L, Map.of("targetGalaxy", "Milky Way"));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Milky Way", ((Earth2026Character) response.getBody()).getTargetGalaxy());
        }

        @Test
        @DisplayName("should return 400 when target galaxy is missing")
        void setTargetGalaxy_shouldReturn400WhenMissing() {
            // Act
            ResponseEntity<?> response = controller.setTargetGalaxy(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when character already transitioned")
        void setTargetGalaxy_shouldReturn400WhenAlreadyTransitioned() {
            // Arrange
            doAnswer(inv -> {
                throw new IllegalStateException("Character has already transitioned to another galaxy");
            }).when(earth2026Service).setTargetGalaxy(1L, "Milky Way");

            // Act
            ResponseEntity<?> response = controller.setTargetGalaxy(1L, Map.of("targetGalaxy", "Milky Way"));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void setTargetGalaxy_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.setTargetGalaxy(999L, "Milky Way")).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.setTargetGalaxy(999L, Map.of("targetGalaxy", "Milky Way"));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- completeTransition ----

    @Nested
    @DisplayName("completeTransition Tests")
    class CompleteTransitionTests {

        @Test
        @DisplayName("should complete transition")
        void completeTransition_shouldComplete() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setHasTransitioned(true);
            character.setTargetGalaxy("Milky Way");
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.completeTransition(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(((Earth2026Character) response.getBody()).isHasTransitioned());
        }

        @Test
        @DisplayName("should return 400 when no target galaxy")
        void completeTransition_shouldReturn400WhenNoTarget() {
            // Arrange
            doAnswer(inv -> {
                throw new IllegalStateException("No target galaxy selected");
            }).when(earth2026Service).completeTransition(1L);

            // Act
            ResponseEntity<?> response = controller.completeTransition(1L);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void completeTransition_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.completeTransition(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.completeTransition(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- getAvailableScenarios ----

    @Nested
    @DisplayName("getAvailableScenarios Tests")
    class GetAvailableScenariosTests {

        @Test
        @DisplayName("should return scenarios for level")
        void getAvailableScenarios_shouldReturnForLevel() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            when(earth2026Service.getAvailableScenarios(1)).thenReturn(List.of(scenario));

            // Act
            ResponseEntity<List<Earth2026Scenario>> response = controller.getAvailableScenarios(1);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("should use default level 1 when not specified")
        void getAvailableScenarios_shouldUseDefaultLevel() {
            // Arrange
            when(earth2026Service.getAvailableScenarios(1)).thenReturn(List.of());

            // Act
            ResponseEntity<List<Earth2026Scenario>> response = controller.getAvailableScenarios(1);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(earth2026Service).getAvailableScenarios(1);
        }
    }

    // ---- startScenario ----

    @Nested
    @DisplayName("startScenario Tests")
    class StartScenarioTests {

        @Test
        @DisplayName("should start scenario")
        void startScenario_shouldStart() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.IN_PROGRESS);
            when(earth2026Service.startScenario(1L)).thenReturn(Optional.of(scenario));

            // Act
            ResponseEntity<?> response = controller.startScenario(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(Earth2026Scenario.ScenarioStatus.IN_PROGRESS,
                    ((Earth2026Scenario) response.getBody()).getStatus());
        }

        @Test
        @DisplayName("should return 400 when scenario not available")
        void startScenario_shouldReturn400WhenNotAvailable() {
            // Arrange
            doAnswer(inv -> {
                throw new IllegalStateException("Scenario is not available");
            }).when(earth2026Service).startScenario(1L);

            // Act
            ResponseEntity<?> response = controller.startScenario(1L);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when scenario not found")
        void startScenario_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.startScenario(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.startScenario(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.COMPLETED);
            when(earth2026Service.completeScenario(1L, true)).thenReturn(Optional.of(scenario));

            // Act
            ResponseEntity<?> response = controller.completeScenario(1L, Map.of("success", true));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(Earth2026Scenario.ScenarioStatus.COMPLETED,
                    ((Earth2026Scenario) response.getBody()).getStatus());
        }

        @Test
        @DisplayName("should mark scenario as failed")
        void completeScenario_shouldMarkAsFailed() {
            // Arrange
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.FAILED);
            when(earth2026Service.completeScenario(1L, false)).thenReturn(Optional.of(scenario));

            // Act
            ResponseEntity<?> response = controller.completeScenario(1L, Map.of("success", false));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(Earth2026Scenario.ScenarioStatus.FAILED,
                    ((Earth2026Scenario) response.getBody()).getStatus());
        }

        @Test
        @DisplayName("should return 400 when success flag is missing")
        void completeScenario_shouldReturn400WhenSuccessMissing() {
            // Act
            ResponseEntity<?> response = controller.completeScenario(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when scenario not found")
        void completeScenario_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.completeScenario(999L, true)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.completeScenario(999L, Map.of("success", true));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
            character.setExperiencePoints(50);
            when(earth2026Service.awardXp(1L, 50)).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.awardXp(1L, Map.of("xp", 50));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(50, ((Earth2026Character) response.getBody()).getExperiencePoints());
        }

        @Test
        @DisplayName("should return 400 when XP is missing")
        void awardXp_shouldReturn400WhenXpMissing() {
            // Act
            ResponseEntity<?> response = controller.awardXp(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when XP is zero")
        void awardXp_shouldReturn400WhenXpZero() {
            // Act
            ResponseEntity<?> response = controller.awardXp(1L, Map.of("xp", 0));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void awardXp_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.awardXp(999L, 50)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.awardXp(999L, Map.of("xp", 50));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- getAllLocations ----

    @Nested
    @DisplayName("getAllLocations Tests")
    class GetAllLocationsTests {

        @Test
        @DisplayName("should return all active locations")
        void getAllLocations_shouldReturnAll() {
            // Arrange
            Earth2026Location location = new Earth2026Location("San Diego", "San Diego", "USA", "City");
            location.setId(1L);
            when(earth2026Service.getAllLocations()).thenReturn(List.of(location));

            // Act
            ResponseEntity<List<Earth2026Location>> response = controller.getAllLocations();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
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
            when(earth2026Service.getPortalLocations()).thenReturn(List.of(portal));

            // Act
            ResponseEntity<List<Earth2026Location>> response = controller.getPortalLocations();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertTrue(response.getBody().get(0).isHasPortal());
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
            when(earth2026Service.getLocation(1L)).thenReturn(Optional.of(location));

            // Act
            ResponseEntity<?> response = controller.getLocation(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("NYC", ((Earth2026Location) response.getBody()).getName());
        }

        @Test
        @DisplayName("should return 404 when not found")
        void getLocation_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.getLocation(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.getLocation(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- enableSupernatural ----

    @Nested
    @DisplayName("enableSupernatural Tests")
    class EnableSupernaturalTests {

        @Test
        @DisplayName("should enable supernatural mode")
        void enableSupernatural_shouldEnable() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Investigator", 30, "Detective");
            character.setId(1L);
            character.setSupernaturalEnabled(true);
            character.setSanityScore(100);
            when(earth2026Service.enableSupernatural(1L)).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.enableSupernatural(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(((Earth2026Character) response.getBody()).isSupernaturalEnabled());
            assertEquals(100, ((Earth2026Character) response.getBody()).getSanityScore());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void enableSupernatural_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.enableSupernatural(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.enableSupernatural(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- reduceSanity ----

    @Nested
    @DisplayName("reduceSanity Tests")
    class ReduceSanityTests {

        @Test
        @DisplayName("should reduce sanity")
        void reduceSanity_shouldReduce() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Investigator", 30, "Detective");
            character.setId(1L);
            character.setSanityScore(80);
            when(earth2026Service.reduceSanity(1L, 20)).thenReturn(Optional.of(character));

            // Act
            ResponseEntity<?> response = controller.reduceSanity(1L, Map.of("amount", 20));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(80, ((Earth2026Character) response.getBody()).getSanityScore());
        }

        @Test
        @DisplayName("should return 400 when amount is missing")
        void reduceSanity_shouldReturn400WhenAmountMissing() {
            // Act
            ResponseEntity<?> response = controller.reduceSanity(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when amount is zero")
        void reduceSanity_shouldReturn400WhenAmountZero() {
            // Act
            ResponseEntity<?> response = controller.reduceSanity(1L, Map.of("amount", 0));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when supernatural not enabled")
        void reduceSanity_shouldReturn400WhenNotSupernatural() {
            // Arrange
            doAnswer(inv -> {
                throw new IllegalStateException("Supernatural mode is not enabled");
            }).when(earth2026Service).reduceSanity(1L, 10);

            // Act
            ResponseEntity<?> response = controller.reduceSanity(1L, Map.of("amount", 10));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void reduceSanity_shouldReturn404WhenNotFound() {
            // Arrange
            when(earth2026Service.reduceSanity(999L, 10)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.reduceSanity(999L, Map.of("amount", 10));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}
