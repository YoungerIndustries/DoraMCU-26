package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AirBattleExperienceTest {

    @Mock
    private AiCommanderExperienceService experienceService;

    // ------------------------------------------------------------------
    // mapTravellerOutcome tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("mapTravellerOutcome")
    class MapOutcomeTests {

        @Test
        @DisplayName("maps ATTACKER_VICTORY to VICTORY")
        void mapsAttackerVictory() {
            AirBattleController controller = new AirBattleController(experienceService);

            AiCommanderExperience.Outcome outcome = controller.mapTravellerOutcome(AirBattleResult.Outcome.ATTACKER_VICTORY);

            assertEquals(AiCommanderExperience.Outcome.VICTORY, outcome);
        }

        @Test
        @DisplayName("maps DEFENDER_VICTORY to VICTORY")
        void mapsDefenderVictory() {
            AirBattleController controller = new AirBattleController(experienceService);

            AiCommanderExperience.Outcome outcome = controller.mapTravellerOutcome(AirBattleResult.Outcome.DEFENDER_VICTORY);

            assertEquals(AiCommanderExperience.Outcome.VICTORY, outcome);
        }

        @Test
        @DisplayName("maps PYRRHIC_VICTORY to PYRRHIC_VICTORY")
        void mapsPyrrhicVictory() {
            AirBattleController controller = new AirBattleController(experienceService);

            AiCommanderExperience.Outcome outcome = controller.mapTravellerOutcome(AirBattleResult.Outcome.PYRRHIC_VICTORY);

            assertEquals(AiCommanderExperience.Outcome.PYRRHIC_VICTORY, outcome);
        }

        @Test
        @DisplayName("maps DRAW to STALEMATE")
        void mapsDraw() {
            AirBattleController controller = new AirBattleController(experienceService);

            AiCommanderExperience.Outcome outcome = controller.mapTravellerOutcome(AirBattleResult.Outcome.DRAW);

            assertEquals(AiCommanderExperience.Outcome.STALEMATE, outcome);
        }

        @Test
        @DisplayName("maps RETREAT to ROUTE")
        void mapsRetreat() {
            AirBattleController controller = new AirBattleController(experienceService);

            AiCommanderExperience.Outcome outcome = controller.mapTravellerOutcome(AirBattleResult.Outcome.RETREAT);

            assertEquals(AiCommanderExperience.Outcome.ROUTE, outcome);
        }
    }

    // ------------------------------------------------------------------
    // resolveBattle experience recording tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("resolveBattle experience recording")
    class ResolveBattleExperienceTests {

        @Test
        @DisplayName("records experience for attacker commander when ID provided")
        void recordsAttackerCommanderExperience() {
            AirBattleController controller = new AirBattleController(experienceService);

            // Create a battle first
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-1");
            controller.createBattle(createRequest);

            // Resolve with attacker commander ID
            Map<String, Object> resolveRequest = new HashMap<>();
            resolveRequest.put("attackerCommanderId", 100L);

            controller.resolveBattle("test-battle-1", resolveRequest);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("TRAVELLER"), eq("Air Battle"), any(), anyString(), isNull());
        }

        @Test
        @DisplayName("records experience for defender commander when ID provided")
        void recordsDefenderCommanderExperience() {
            AirBattleController controller = new AirBattleController(experienceService);

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-2");
            controller.createBattle(createRequest);

            Map<String, Object> resolveRequest = new HashMap<>();
            resolveRequest.put("defenderCommanderId", 200L);

            controller.resolveBattle("test-battle-2", resolveRequest);

            verify(experienceService).recordBattleExperience(
                    eq(200L), eq("TRAVELLER"), eq("Air Battle"), any(), anyString(), isNull());
        }

        @Test
        @DisplayName("records experience for both commanders when both IDs provided")
        void recordsBothCommandersExperience() {
            AirBattleController controller = new AirBattleController(experienceService);

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-3");
            controller.createBattle(createRequest);

            Map<String, Object> resolveRequest = new HashMap<>();
            resolveRequest.put("attackerCommanderId", 100L);
            resolveRequest.put("defenderCommanderId", 200L);

            controller.resolveBattle("test-battle-3", resolveRequest);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("TRAVELLER"), eq("Air Battle"), any(), anyString(), isNull());
            verify(experienceService).recordBattleExperience(
                    eq(200L), eq("TRAVELLER"), eq("Air Battle"), any(), anyString(), isNull());
        }

        @Test
        @DisplayName("skips experience recording when no commander IDs provided")
        void skipsExperienceWhenNoCommanderIds() {
            AirBattleController controller = new AirBattleController(experienceService);

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-4");
            controller.createBattle(createRequest);

            controller.resolveBattle("test-battle-4", null);

            verify(experienceService, never()).recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("skips experience recording when experienceService is null")
        void skipsExperienceWhenServiceNull() {
            AirBattleController controller = new AirBattleController(null);

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-5");
            controller.createBattle(createRequest);

            Map<String, Object> resolveRequest = new HashMap<>();
            resolveRequest.put("attackerCommanderId", 100L);

            ResponseEntity<?> response = controller.resolveBattle("test-battle-5", resolveRequest);

            assertNotNull(response);
            // No exception thrown
        }

        @Test
        @DisplayName("handles experience service exception gracefully")
        void handlesExperienceServiceException() {
            AirBattleController controller = new AirBattleController(experienceService);

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-6");
            controller.createBattle(createRequest);

            doThrow(new RuntimeException("DB error")).when(experienceService)
                    .recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());

            Map<String, Object> resolveRequest = new HashMap<>();
            resolveRequest.put("attackerCommanderId", 100L);

            ResponseEntity<?> response = controller.resolveBattle("test-battle-6", resolveRequest);

            assertNotNull(response);
            // Battle still resolves despite experience recording failure
        }

        @Test
        @DisplayName("resolve returns battle result with outcome")
        void resolveReturnsResult() {
            AirBattleController controller = new AirBattleController(experienceService);

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("id", "test-battle-7");
            controller.createBattle(createRequest);

            ResponseEntity<?> response = controller.resolveBattle("test-battle-7", null);

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertTrue(body.containsKey("outcome"));
        }
    }

    // ------------------------------------------------------------------
    // Constructor tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with experience service")
        void constructorWithService() {
            AirBattleController controller = new AirBattleController(experienceService);
            assertNotNull(controller);
        }

        @Test
        @DisplayName("constructor with null experience service")
        void constructorWithNullService() {
            AirBattleController controller = new AirBattleController(null);
            assertNotNull(controller);
        }
    }
}
