package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import com.yisy.modules.doramcu.sfb.model.SfbCharacterAssignment;
import com.yisy.modules.doramcu.sfb.model.SfbScenario;
import com.yisy.modules.doramcu.sfb.model.SfbShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SfbCombatEngineExperienceTest {

    @Mock private AiCommanderExperienceService experienceService;
    @Mock private SfbScenario scenario;
    @Mock private ImpulseManager impulseManager;
    @Mock private EnergyAllocationService energyAllocationService;
    @Mock private MovementService movementService;
    @Mock private WeaponsFireService weaponsFireService;
    @Mock private DamageAllocationService damageAllocationService;
    @Mock private SpecialSystemsService specialSystemsService;
    @Mock private SfbAIService aiService;
    @Mock private SfbRoleSystem roleSystem;
    @Mock private SfbCharacterBridge characterBridge;

    private SfbCombatEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SfbCombatEngine(
                impulseManager, energyAllocationService, movementService,
                weaponsFireService, damageAllocationService, specialSystemsService,
                aiService, roleSystem, characterBridge, experienceService);
    }

    // ------------------------------------------------------------------
    // recordAdmiralExperience tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("recordAdmiralExperience")
    class RecordAdmiralExperienceTests {

        @Test
        @DisplayName("skips when experienceService is null")
        void skipsNullExperienceService() {
            SfbCombatEngine engineNoExp = new SfbCombatEngine(
                    impulseManager, energyAllocationService, movementService,
                    weaponsFireService, damageAllocationService, specialSystemsService,
                    aiService, roleSystem, characterBridge, null);
            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Collections.emptyList());

            engineNoExp.recordAdmiralExperience(scenario);
            // No exception, silently skipped
        }

        @Test
        @DisplayName("determines VICTORY when only one side has surviving ships")
        void determinesVictoryOneSideSurviving() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            SfbShip side1Ship = mock(SfbShip.class);
            when(side1Ship.getSide()).thenReturn(1);
            when(side1Ship.isDestroyed()).thenReturn(true);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship, side1Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(5);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Admiral Kirk");
            admiral0.setAiCharacterId(100L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);
            when(scenario.getAdmiralAssignment(1)).thenReturn(null);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.VICTORY), anyString(), eq("Test Battle"));
        }

        @Test
        @DisplayName("determines STALEMATE when both sides have surviving ships")
        void determinesStalemateBothSidesSurviving() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            SfbShip side1Ship = mock(SfbShip.class);
            when(side1Ship.getSide()).thenReturn(1);
            when(side1Ship.isDestroyed()).thenReturn(false);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship, side1Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(3);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Admiral Kirk");
            admiral0.setAiCharacterId(100L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);

            SfbCharacterAssignment admiral1 = new SfbCharacterAssignment();
            admiral1.setCharacterName("Admiral Khan");
            admiral1.setAiCharacterId(200L);
            when(scenario.getAdmiralAssignment(1)).thenReturn(admiral1);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.STALEMATE), anyString(), eq("Test Battle"));
            verify(experienceService).recordBattleExperience(
                    eq(200L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.STALEMATE), anyString(), eq("Test Battle"));
        }

        @Test
        @DisplayName("determines STALEMATE when no ships survive")
        void determinesStalemateNoShipsSurviving() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(true);

            SfbShip side1Ship = mock(SfbShip.class);
            when(side1Ship.getSide()).thenReturn(1);
            when(side1Ship.isDestroyed()).thenReturn(true);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship, side1Ship));
            when(scenario.getName()).thenReturn("Mutual Destruction");
            when(scenario.getCurrentTurn()).thenReturn(10);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Admiral Kirk");
            admiral0.setAiCharacterId(100L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.STALEMATE), anyString(), eq("Mutual Destruction"));
        }

        @Test
        @DisplayName("skips side with no admiral assignment")
        void skipsSideWithNoAdmiral() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(1);

            when(scenario.getAdmiralAssignment(0)).thenReturn(null);
            when(scenario.getAdmiralAssignment(1)).thenReturn(null);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService, never()).recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("skips admiral without aiCharacterId")
        void skipsAdmiralWithoutAiCharacterId() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(1);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Player Admiral");
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);
            when(scenario.getAdmiralAssignment(1)).thenReturn(null);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService, never()).recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("records for both admirals when both have aiCharacterId")
        void recordsBothAdmirals() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            SfbShip side1Ship = mock(SfbShip.class);
            when(side1Ship.getSide()).thenReturn(1);
            when(side1Ship.isDestroyed()).thenReturn(true);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship, side1Ship));
            when(scenario.getName()).thenReturn("Fleet Engagement");
            when(scenario.getCurrentTurn()).thenReturn(7);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Admiral Kirk");
            admiral0.setAiCharacterId(100L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);

            SfbCharacterAssignment admiral1 = new SfbCharacterAssignment();
            admiral1.setCharacterName("Admiral Khan");
            admiral1.setAiCharacterId(200L);
            when(scenario.getAdmiralAssignment(1)).thenReturn(admiral1);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.VICTORY), anyString(), eq("Fleet Engagement"));
            verify(experienceService).recordBattleExperience(
                    eq(200L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.VICTORY), anyString(), eq("Fleet Engagement"));
        }

        @Test
        @DisplayName("handles experience service exception gracefully")
        void handlesExperienceServiceException() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(1);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Admiral Kirk");
            admiral0.setAiCharacterId(100L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);

            doThrow(new RuntimeException("DB error")).when(experienceService)
                    .recordBattleExperience(anyLong(), anyString(), anyString(), any(), anyString(), anyString());

            assertDoesNotThrow(() -> engine.recordAdmiralExperience(scenario));
        }

        @Test
        @DisplayName("includes turn number in description")
        void includesTurnNumberInDescription() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(false);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(42);

            SfbCharacterAssignment admiral0 = new SfbCharacterAssignment();
            admiral0.setCharacterName("Admiral Kirk");
            admiral0.setAiCharacterId(100L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(admiral0);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService).recordBattleExperience(
                    eq(100L), eq("SFB"), eq("Space Battle"), any(),
                    argThat(desc -> desc.contains("turn 42")), eq("Test Battle"));
        }

        @Test
        @DisplayName("VICTORY for side 1 when side 0 destroyed")
        void victoryForSide1() {
            SfbShip side0Ship = mock(SfbShip.class);
            when(side0Ship.getSide()).thenReturn(0);
            when(side0Ship.isDestroyed()).thenReturn(true);

            SfbShip side1Ship = mock(SfbShip.class);
            when(side1Ship.getSide()).thenReturn(1);
            when(side1Ship.isDestroyed()).thenReturn(false);

            when(scenario.isBattleOver()).thenReturn(true);
            when(scenario.getShips()).thenReturn(Arrays.asList(side0Ship, side1Ship));
            when(scenario.getName()).thenReturn("Test Battle");
            when(scenario.getCurrentTurn()).thenReturn(2);

            SfbCharacterAssignment admiral1 = new SfbCharacterAssignment();
            admiral1.setCharacterName("Admiral Khan");
            admiral1.setAiCharacterId(200L);
            when(scenario.getAdmiralAssignment(0)).thenReturn(null);
            when(scenario.getAdmiralAssignment(1)).thenReturn(admiral1);

            engine.recordAdmiralExperience(scenario);

            verify(experienceService).recordBattleExperience(
                    eq(200L), eq("SFB"), eq("Space Battle"),
                    eq(AiCommanderExperience.Outcome.VICTORY), anyString(), eq("Test Battle"));
        }
    }

    // ------------------------------------------------------------------
    // Constructor tests
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor variants")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor creates engine without experience service")
        void noArgConstructor() {
            SfbCombatEngine engine = new SfbCombatEngine();
            assertNotNull(engine);
        }

        @Test
        @DisplayName("constructor with experience service")
        void constructorWithExperienceService() {
            SfbCombatEngine e = new SfbCombatEngine(
                    impulseManager, energyAllocationService, movementService,
                    weaponsFireService, damageAllocationService, specialSystemsService,
                    aiService, roleSystem, characterBridge, experienceService);
            assertNotNull(e);
        }
    }
}
