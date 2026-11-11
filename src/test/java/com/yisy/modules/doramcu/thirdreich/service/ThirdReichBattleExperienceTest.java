package com.yisy.modules.doramcu.thirdreich.service;

import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainContext;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainType;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ThirdReichBattleExperienceTest {

    @Mock
    private PlanetService planetService;

    @Mock
    private AiCommanderExperienceService experienceService;

    // ------------------------------------------------------------------
    // mapOutcomeForCommander tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("mapOutcomeForCommander")
    class MapOutcomeTests {

        @Test
        @DisplayName("maps ATTACKER_VICTORY to VICTORY")
        void mapsAttackerVictory() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            AICommander commander = new AICommander("c1", "Rommel");

            AiCommanderExperience.Outcome outcome = service.mapOutcomeForCommander(commander, result);

            assertEquals(AiCommanderExperience.Outcome.VICTORY, outcome);
        }

        @Test
        @DisplayName("maps DEFENDER_VICTORY to VICTORY")
        void mapsDefenderVictory() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.DEFENDER_VICTORY);
            AICommander commander = new AICommander("c1", "Rommel");

            AiCommanderExperience.Outcome outcome = service.mapOutcomeForCommander(commander, result);

            assertEquals(AiCommanderExperience.Outcome.VICTORY, outcome);
        }

        @Test
        @DisplayName("maps PYRRHIC_VICTORY to PYRRHIC_VICTORY")
        void mapsPyrrhicVictory() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.PYRRHIC_VICTORY);
            AICommander commander = new AICommander("c1", "Rommel");

            AiCommanderExperience.Outcome outcome = service.mapOutcomeForCommander(commander, result);

            assertEquals(AiCommanderExperience.Outcome.PYRRHIC_VICTORY, outcome);
        }

        @Test
        @DisplayName("maps DEFENDER_HOLDS to STALEMATE")
        void mapsDefenderHolds() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.DEFENDER_HOLDS);
            AICommander commander = new AICommander("c1", "Rommel");

            AiCommanderExperience.Outcome outcome = service.mapOutcomeForCommander(commander, result);

            assertEquals(AiCommanderExperience.Outcome.STALEMATE, outcome);
        }

        @Test
        @DisplayName("maps DRAW to STALEMATE")
        void mapsDraw() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.DRAW);
            AICommander commander = new AICommander("c1", "Rommel");

            AiCommanderExperience.Outcome outcome = service.mapOutcomeForCommander(commander, result);

            assertEquals(AiCommanderExperience.Outcome.STALEMATE, outcome);
        }

        @Test
        @DisplayName("maps RETREAT to ROUTE")
        void mapsRetreat() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.RETREAT);
            AICommander commander = new AICommander("c1", "Rommel");

            AiCommanderExperience.Outcome outcome = service.mapOutcomeForCommander(commander, result);

            assertEquals(AiCommanderExperience.Outcome.ROUTE, outcome);
        }
    }

    // ------------------------------------------------------------------
    // recordCommanderExperience tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("recordCommanderExperience")
    class RecordExperienceTests {

        @Test
        @DisplayName("skips when commander is null")
        void skipsNullCommander() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            TerrainContext terrain = new TerrainContext(TerrainType.PLAINS);

            service.recordCommanderExperience(null, result, "b1", terrain);

            verify(experienceService, never()).recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("skips when experienceService is null")
        void skipsNullExperienceService() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, null);
            AICommander commander = new AICommander("c1", "Rommel");
            commander.setLinkedAiCharacterId(1L);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            TerrainContext terrain = new TerrainContext(TerrainType.PLAINS);

            service.recordCommanderExperience(commander, result, "b1", terrain);

            // No exception, silently skipped
        }

        @Test
        @DisplayName("skips when commander has no linked AI character ID")
        void skipsNoLinkedAiCharacter() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            AICommander commander = new AICommander("c1", "Rommel");
            // linkedAiCharacterId is null by default
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            TerrainContext terrain = new TerrainContext(TerrainType.PLAINS);

            service.recordCommanderExperience(commander, result, "b1", terrain);

            verify(experienceService, never()).recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("records experience for commander with linked AI character")
        void recordsExperience() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            AICommander commander = new AICommander("c1", "Rommel");
            commander.setLinkedAiCharacterId(42L);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setNarrativeSummary("Decisive victory on the plains");
            TerrainContext terrain = new TerrainContext(TerrainType.PLAINS, "Earth");

            service.recordCommanderExperience(commander, result, "b1", terrain);

            verify(experienceService).recordBattleExperience(
                    eq(42L), eq("THIRD_REICH"), eq("Ground Battle"),
                    eq(AiCommanderExperience.Outcome.VICTORY),
                    eq("Decisive victory on the plains"), eq("Earth"));
        }

        @Test
        @DisplayName("records experience with null terrain")
        void recordsExperienceWithNullTerrain() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            AICommander commander = new AICommander("c1", "Rommel");
            commander.setLinkedAiCharacterId(42L);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setNarrativeSummary("Victory");

            service.recordCommanderExperience(commander, result, "b1", null);

            verify(experienceService).recordBattleExperience(
                    eq(42L), eq("THIRD_REICH"), eq("Ground Battle"),
                    eq(AiCommanderExperience.Outcome.VICTORY),
                    eq("Victory"), isNull());
        }

        @Test
        @DisplayName("records experience for all outcome types")
        void recordsAllOutcomeTypes() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);

            // PYRRHIC_VICTORY
            AICommander c1 = new AICommander("c1", "Cmdr1");
            c1.setLinkedAiCharacterId(1L);
            ThirdReichBattleResult r1 = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.PYRRHIC_VICTORY);
            service.recordCommanderExperience(c1, r1, "b1", null);
            verify(experienceService).recordBattleExperience(eq(1L), anyString(), anyString(), eq(AiCommanderExperience.Outcome.PYRRHIC_VICTORY), isNull(), isNull());

            // DEFENDER_HOLDS -> STALEMATE
            AICommander c2 = new AICommander("c2", "Cmdr2");
            c2.setLinkedAiCharacterId(2L);
            ThirdReichBattleResult r2 = new ThirdReichBattleResult("b2", ThirdReichBattleResult.Outcome.DEFENDER_HOLDS);
            service.recordCommanderExperience(c2, r2, "b2", null);
            verify(experienceService).recordBattleExperience(eq(2L), anyString(), anyString(), eq(AiCommanderExperience.Outcome.STALEMATE), isNull(), isNull());

            // RETREAT -> ROUTE
            AICommander c3 = new AICommander("c3", "Cmdr3");
            c3.setLinkedAiCharacterId(3L);
            ThirdReichBattleResult r3 = new ThirdReichBattleResult("b3", ThirdReichBattleResult.Outcome.RETREAT);
            service.recordCommanderExperience(c3, r3, "b3", null);
            verify(experienceService).recordBattleExperience(eq(3L), anyString(), anyString(), eq(AiCommanderExperience.Outcome.ROUTE), isNull(), isNull());
        }

        @Test
        @DisplayName("handles experience service exception gracefully")
        void handlesExperienceServiceException() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            AICommander commander = new AICommander("c1", "Rommel");
            commander.setLinkedAiCharacterId(42L);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            TerrainContext terrain = new TerrainContext(TerrainType.PLAINS);

            doThrow(new RuntimeException("DB error")).when(experienceService)
                    .recordBattleExperience(anyLong(), anyString(), anyString(), any(), isNull(), isNull());

            assertDoesNotThrow(() -> service.recordCommanderExperience(commander, result, "b1", terrain));
        }

        @Test
        @DisplayName("uses location from terrain planet name")
        void usesTerrainPlanetName() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            AICommander commander = new AICommander("c1", "Rommel");
            commander.setLinkedAiCharacterId(42L);
            ThirdReichBattleResult result = new ThirdReichBattleResult("b1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            TerrainContext terrain = new TerrainContext(TerrainType.URBAN, "Stalingrad");

            service.recordCommanderExperience(commander, result, "b1", terrain);

            verify(experienceService).recordBattleExperience(
                    eq(42L), anyString(), anyString(), any(), isNull(), eq("Stalingrad"));
        }
    }

    // ------------------------------------------------------------------
    // Constructor variants
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor variants")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with both services")
        void constructorWithBothServices() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService, experienceService);
            assertNotNull(service);
        }

        @Test
        @DisplayName("constructor with only PlanetService")
        void constructorWithPlanetServiceOnly() {
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService(planetService);
            assertNotNull(service);
        }

        @Test
        @DisplayName("deprecated no-arg constructor")
        void deprecatedNoArgConstructor() {
            @SuppressWarnings("deprecation")
            ThirdReichGroundBattleService service = new ThirdReichGroundBattleService();
            assertNotNull(service);
        }
    }
}
