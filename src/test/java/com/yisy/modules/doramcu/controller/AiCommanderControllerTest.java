package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.eve.model.AiCommanderHiring;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AiCommanderController Tests")
class AiCommanderControllerTest {

    private AiCommanderExperienceService experienceService;
    private AiCommanderController controller;

    @BeforeEach
    void setUp() {
        experienceService = mock(AiCommanderExperienceService.class);
        controller = new AiCommanderController(experienceService);
    }

    private AiCharacter createAiCharacter(Long id, String name) {
        AiCharacter character = new AiCharacter(name, "Leader", "Test Empire");
        character.setId(id);
        character.setHirable(true);
        character.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        character.setDailySalary(500L);
        return character;
    }

    // ---- getAvailableCommanders ----

    @Nested
    @DisplayName("GET /available")
    class GetAvailableCommandersTests {

        @Test
        @DisplayName("should return all available commanders")
        void returnAllAvailable() {
            List<AiCharacter> commanders = List.of(
                    createAiCharacter(1L, "Cmdr 1"),
                    createAiCharacter(2L, "Cmdr 2"));
            when(experienceService.getAvailableCommanders()).thenReturn(commanders);

            ResponseEntity<List<AiCharacter>> response = controller.getAvailableCommanders(null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
            verify(experienceService).getAvailableCommanders();
        }

        @Test
        @DisplayName("should filter by minimum experience")
        void filterByMinExperience() {
            List<AiCharacter> commanders = List.of(createAiCharacter(1L, "Experienced Cmdr"));
            when(experienceService.getAvailableCommanders(100)).thenReturn(commanders);

            ResponseEntity<List<AiCharacter>> response = controller.getAvailableCommanders(100);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            verify(experienceService).getAvailableCommanders(100);
        }

        @Test
        @DisplayName("should return empty list when no commanders")
        void returnEmptyList() {
            when(experienceService.getAvailableCommanders()).thenReturn(List.of());

            ResponseEntity<List<AiCharacter>> response = controller.getAvailableCommanders(null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ---- getCommander ----

    @Nested
    @DisplayName("GET /{id}")
    class GetCommanderTests {

        @Test
        @DisplayName("should return commander when found")
        void returnCommander() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(experienceService.getAvailableCommanders()).thenReturn(List.of(character));

            ResponseEntity<AiCharacter> response = controller.getCommander(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Test Commander", response.getBody().getName());
        }

        @Test
        @DisplayName("should return 404 when commander not found")
        void returnNotFound() {
            when(experienceService.getAvailableCommanders()).thenReturn(List.of());

            ResponseEntity<AiCharacter> response = controller.getCommander(999L);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- getExperience ----

    @Nested
    @DisplayName("GET /{id}/experience")
    class GetExperienceTests {

        @Test
        @DisplayName("should return experience history")
        void returnExperience() {
            List<AiCommanderExperience> experiences = List.of(
                    new AiCommanderExperience(1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY));
            when(experienceService.getExperiences(1L)).thenReturn(experiences);

            ResponseEntity<List<AiCommanderExperience>> response = controller.getExperience(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("should return empty list when no experiences")
        void returnEmptyExperiences() {
            when(experienceService.getExperiences(1L)).thenReturn(List.of());

            ResponseEntity<List<AiCommanderExperience>> response = controller.getExperience(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ---- getStats ----

    @Nested
    @DisplayName("GET /{id}/stats")
    class GetStatsTests {

        @Test
        @DisplayName("should return experience stats")
        void returnStats() {
            when(experienceService.getTotalExperience(1L)).thenReturn(250);
            when(experienceService.getBattleCount(1L)).thenReturn(15L);

            ResponseEntity<Map<String, Object>> response = controller.getStats(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = response.getBody();
            assertEquals(1L, body.get("aiCharacterId"));
            assertEquals(250, body.get("totalExperience"));
            assertEquals(15L, body.get("battleCount"));
        }
    }

    // ---- hireCommander ----

    @Nested
    @DisplayName("POST /{id}/hire")
    class HireCommanderTests {

        @Test
        @DisplayName("should hire commander successfully")
        void hireSuccessfully() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            when(experienceService.hireCommander(1L, "player123", 30)).thenReturn(hiring);

            Map<String, Object> request = Map.of("playerId", "player123", "durationDays", 30);
            ResponseEntity<?> response = controller.hireCommander(1L, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(hiring, response.getBody());
        }

        @Test
        @DisplayName("should hire with default duration")
        void hireWithDefaultDuration() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            when(experienceService.hireCommander(1L, "player123", 0)).thenReturn(hiring);

            Map<String, Object> request = Map.of("playerId", "player123");
            ResponseEntity<?> response = controller.hireCommander(1L, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(experienceService).hireCommander(1L, "player123", 0);
        }

        @Test
        @DisplayName("should return 400 when playerId is missing")
        void missingPlayerId() {
            Map<String, Object> request = Map.of();
            ResponseEntity<?> response = controller.hireCommander(1L, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, String> body = (Map<String, String>) response.getBody();
            assertEquals("playerId is required", body.get("error"));
        }

        @Test
        @DisplayName("should return 400 when playerId is blank")
        void blankPlayerId() {
            Map<String, Object> request = Map.of("playerId", "  ");
            ResponseEntity<?> response = controller.hireCommander(1L, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when character not found")
        void characterNotFound() {
            when(experienceService.hireCommander(anyLong(), anyString(), anyInt()))
                    .thenThrow(new IllegalArgumentException("AI character not found: 999"));

            Map<String, Object> request = Map.of("playerId", "player123");
            ResponseEntity<?> response = controller.hireCommander(999L, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, String> body = (Map<String, String>) response.getBody();
            assertTrue(body.get("error").contains("AI character not found"));
        }

        @Test
        @DisplayName("should return 400 when character not hirable")
        void notHirable() {
            when(experienceService.hireCommander(anyLong(), anyString(), anyInt()))
                    .thenThrow(new IllegalStateException("AI character is not hirable: Test Commander"));

            Map<String, Object> request = Map.of("playerId", "player123");
            ResponseEntity<?> response = controller.hireCommander(1L, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    // ---- releaseCommander ----

    @Nested
    @DisplayName("POST /{id}/release")
    class ReleaseCommanderTests {

        @Test
        @DisplayName("should release commander successfully")
        void releaseSuccessfully() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setStatus(AiCommanderHiring.Status.EXPIRED);
            when(experienceService.releaseCommander(1L)).thenReturn(hiring);

            ResponseEntity<?> response = controller.releaseCommander(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(AiCommanderHiring.Status.EXPIRED, ((AiCommanderHiring) response.getBody()).getStatus());
        }

        @Test
        @DisplayName("should return 400 when commander not hired")
        void notHired() {
            when(experienceService.releaseCommander(1L))
                    .thenThrow(new IllegalStateException("AI commander is not currently hired: 1"));

            ResponseEntity<?> response = controller.releaseCommander(1L);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, String> body = (Map<String, String>) response.getBody();
            assertTrue(body.get("error").contains("not currently hired"));
        }
    }

    // ---- getHiringStatus ----

    @Nested
    @DisplayName("GET /{id}/hiring")
    class GetHiringStatusTests {

        @Test
        @DisplayName("should return hiring contract when active")
        void returnActiveHiring() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            when(experienceService.getActiveHiring(1L)).thenReturn(Optional.of(hiring));

            ResponseEntity<?> response = controller.getHiringStatus(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(hiring, response.getBody());
        }

        @Test
        @DisplayName("should return 204 when no active hiring")
        void returnNoContent() {
            when(experienceService.getActiveHiring(1L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getHiringStatus(1L);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

    // ---- getPlayerCommanders ----

    @Nested
    @DisplayName("GET /player/{playerId}")
    class GetPlayerCommandersTests {

        @Test
        @DisplayName("should return all hired commanders for a player")
        void returnPlayerCommanders() {
            List<AiCommanderHiring> hired = List.of(
                    new AiCommanderHiring(1L, "player123", 500L),
                    new AiCommanderHiring(2L, "player123", 300L));
            when(experienceService.getPlayerHiredCommanders("player123")).thenReturn(hired);

            ResponseEntity<List<AiCommanderHiring>> response = controller.getPlayerCommanders("player123");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("should return empty list when no hired commanders")
        void returnEmptyList() {
            when(experienceService.getPlayerHiredCommanders("player123")).thenReturn(List.of());

            ResponseEntity<List<AiCommanderHiring>> response = controller.getPlayerCommanders("player123");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }
}
