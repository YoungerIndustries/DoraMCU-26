package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.EncounterAnalysis;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eEncounter;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eEncounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eEncounterService Tests")
class Dnd5eEncounterServiceTest {

    @Mock
    private Dnd5eEncounterRepository encounterRepository;

    private Dnd5eEncounterService encounterService;

    @BeforeEach
    void setUp() {
        lenient().when(encounterRepository.countByActiveTrue()).thenReturn(0L);
        lenient().when(encounterRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        encounterService = new Dnd5eEncounterService(encounterRepository);
    }

    // ==================== XP Thresholds ====================

    @Nested
    @DisplayName("getXpThresholds Tests")
    class GetXpThresholdsTests {

        @Test
        @DisplayName("should return correct thresholds for level 1, party of 4")
        void shouldReturnLevel1Party4Thresholds() {
            int[] thresholds = encounterService.getXpThresholds(1, 4);
            assertEquals(25, thresholds[0]); // Trivial
            assertEquals(50, thresholds[1]); // Easy
            assertEquals(100, thresholds[2]); // Medium
            assertEquals(200, thresholds[3]); // Hard
            assertEquals(300, thresholds[4]); // Deadly
        }

        @Test
        @DisplayName("should return correct thresholds for level 5, party of 4")
        void shouldReturnLevel5Party4Thresholds() {
            int[] thresholds = encounterService.getXpThresholds(5, 4);
            assertEquals(250, thresholds[0]); // Trivial
            assertEquals(500, thresholds[1]); // Easy
            assertEquals(1000, thresholds[2]); // Medium
            assertEquals(2000, thresholds[3]); // Hard
            assertEquals(3000, thresholds[4]); // Deadly
        }

        @Test
        @DisplayName("should scale down for party of 2")
        void shouldScaleDownForPartyOf2() {
            int[] thresholds = encounterService.getXpThresholds(1, 2);
            assertEquals(13, thresholds[0]);  // Math.round(25 * 2/4) = Math.round(12.5) = 13
            assertEquals(25, thresholds[1]);  // Math.round(50 * 2/4) = 25
            assertEquals(50, thresholds[2]);  // Math.round(100 * 2/4) = 50
        }

        @Test
        @DisplayName("should scale down for party of 1")
        void shouldScaleDownForPartyOf1() {
            int[] thresholds = encounterService.getXpThresholds(1, 1);
            assertEquals(6, thresholds[0]);   // Math.round(25 * 1/4) = Math.round(6.25) = 6
            assertEquals(13, thresholds[1]);  // Math.round(50 * 1/4) = Math.round(12.5) = 13
            assertEquals(25, thresholds[2]);  // Math.round(100 * 1/4) = 25
        }

        @Test
        @DisplayName("should scale up for party of 6")
        void shouldScaleUpForPartyOf6() {
            int[] thresholds = encounterService.getXpThresholds(1, 6);
            // Party of 4 = 25, party of 5 = 25*1.5=37.5, party of 6 = 37.5*1.5=56.25
            assertTrue(thresholds[0] > 25); // Should be higher than party of 4
        }

        @Test
        @DisplayName("should clamp level to 1-20")
        void shouldClampLevel() {
            int[] low = encounterService.getXpThresholds(0, 4);
            int[] high = encounterService.getXpThresholds(25, 4);
            int[] valid = encounterService.getXpThresholds(20, 4);

            assertArrayEquals(low, encounterService.getXpThresholds(1, 4));
            assertArrayEquals(high, valid);
        }

        @Test
        @DisplayName("should clamp party size to 1-8")
        void shouldClampPartySize() {
            int[] low = encounterService.getXpThresholds(1, 0);
            int[] high = encounterService.getXpThresholds(1, 10);
            int[] validLow = encounterService.getXpThresholds(1, 1);
            int[] validHigh = encounterService.getXpThresholds(1, 8);

            assertArrayEquals(low, validLow);
            assertArrayEquals(high, validHigh);
        }
    }

    // ==================== Difficulty Determination ====================

    @Nested
    @DisplayName("determineDifficulty Tests")
    class DetermineDifficultyTests {

        @Test
        @DisplayName("should return Trivial for XP below trivial threshold")
        void shouldReturnTrivial() {
            int[] thresholds = {25, 50, 100, 200, 300};
            assertEquals("Trivial", encounterService.determineDifficulty(10, thresholds));
        }

        @Test
        @DisplayName("should return Easy for XP between trivial and easy")
        void shouldReturnEasy() {
            int[] thresholds = {25, 50, 100, 200, 300};
            assertEquals("Easy", encounterService.determineDifficulty(35, thresholds));
        }

        @Test
        @DisplayName("should return Medium for XP between easy and medium")
        void shouldReturnMedium() {
            int[] thresholds = {25, 50, 100, 200, 300};
            assertEquals("Medium", encounterService.determineDifficulty(75, thresholds));
        }

        @Test
        @DisplayName("should return Hard for XP between medium and hard")
        void shouldReturnHard() {
            int[] thresholds = {25, 50, 100, 200, 300};
            assertEquals("Hard", encounterService.determineDifficulty(150, thresholds));
        }

        @Test
        @DisplayName("should return Deadly for XP at or above hard threshold")
        void shouldReturnDeadly() {
            int[] thresholds = {25, 50, 100, 200, 300};
            assertEquals("Deadly", encounterService.determineDifficulty(250, thresholds));
            assertEquals("Deadly", encounterService.determineDifficulty(500, thresholds));
        }
    }

    // ==================== Encounter Multiplier ====================

    @Nested
    @DisplayName("getEncounterMultiplier Tests")
    class GetEncounterMultiplierTests {

        @Test
        @DisplayName("should return 1.0 for 0 recent encounters")
        void shouldReturn1xForZeroRecent() {
            assertEquals(1.0, encounterService.getEncounterMultiplier(0));
        }

        @Test
        @DisplayName("should return 1.5 for 1 recent encounter")
        void shouldReturn1_5xForOneRecent() {
            assertEquals(1.5, encounterService.getEncounterMultiplier(1));
        }

        @Test
        @DisplayName("should return 1.5 for 2 recent encounters")
        void shouldReturn1_5xForTwoRecent() {
            assertEquals(1.5, encounterService.getEncounterMultiplier(2));
        }

        @Test
        @DisplayName("should return 2.0 for 3+ recent encounters")
        void shouldReturn2xForThreePlusRecent() {
            assertEquals(2.0, encounterService.getEncounterMultiplier(3));
            assertEquals(2.0, encounterService.getEncounterMultiplier(5));
            assertEquals(2.0, encounterService.getEncounterMultiplier(10));
        }
    }

    // ==================== CR to XP ====================

    @Nested
    @DisplayName("getCrXp Tests")
    class GetCrXpTests {

        @Test
        @DisplayName("should return 0 for CR below 0")
        void shouldReturnZeroForNegativeCr() {
            assertEquals(0, encounterService.getCrXp(-1));
            assertEquals(0, encounterService.getCrXp(0));
        }

        @Test
        @DisplayName("should return 25 for CR 1/8")
        void shouldReturn25ForCr1_8() {
            assertEquals(25, encounterService.getCrXp(0.2)); // < 0.25 -> CR 1/8
        }

        @Test
        @DisplayName("should return 50 for CR 1/4")
        void shouldReturn50ForCr1_4() {
            assertEquals(50, encounterService.getCrXp(0.3)); // >= 0.25, < 0.5 -> CR 1/4
        }

        @Test
        @DisplayName("should return 100 for CR 1/2")
        void shouldReturn100ForCr1_2() {
            assertEquals(100, encounterService.getCrXp(0.75)); // >= 0.5, < 1.0 -> CR 1/2
        }

        @Test
        @DisplayName("should return 200 for CR 1")
        void shouldReturn200ForCr1() {
            assertEquals(200, encounterService.getCrXp(1));
        }

        @Test
        @DisplayName("should return correct XP for CR 5")
        void shouldReturnCorrectXpForCr5() {
            assertEquals(1300, encounterService.getCrXp(5));
        }

        @Test
        @DisplayName("should return correct XP for CR 10")
        void shouldReturnCorrectXpForCr10() {
            assertEquals(2800, encounterService.getCrXp(10));
        }

        @Test
        @DisplayName("should return correct XP for CR 20")
        void shouldReturnCorrectXpForCr20() {
            assertEquals(15000, encounterService.getCrXp(20));
        }

        @Test
        @DisplayName("should clamp CR above 30 to 30")
        void shouldClampCrAbove30() {
            int xp30 = encounterService.getCrXp(30);
            int xp35 = encounterService.getCrXp(35);
            assertEquals(xp30, xp35);
        }
    }

    // ==================== Encounter Analysis ====================

    @Nested
    @DisplayName("analyzeEncounter Tests")
    class AnalyzeEncounterTests {

        @Test
        @DisplayName("should analyze a medium difficulty encounter")
        void shouldAnalyzeMediumEncounter() {
            // Level 1, party 4: thresholds = {25, 50, 100, 200, 300}
            // 75 XP = Medium (>= 50 easy, < 100 medium threshold)
            EncounterAnalysis analysis = encounterService.analyzeEncounter(1, 4, 75, 0, 0);
            assertEquals("Medium", analysis.getDifficulty());
            assertEquals(75, analysis.getMonsterXp());
            assertEquals(1.0, analysis.getEncounterMultiplier());
            assertEquals(75, analysis.getAdjustedXp());
        }

        @Test
        @DisplayName("should analyze with encounter multiplier")
        void shouldAnalyzeWithMultiplier() {
            // Level 1, party 4: thresholds = {25, 50, 100, 200, 300}
            // 100 * 1.5 = 150, which is Hard (>= 100 medium, < 200 hard)
            EncounterAnalysis analysis = encounterService.analyzeEncounter(1, 4, 100, 2, 0);
            assertEquals(150, analysis.getAdjustedXp());
            assertEquals(1.5, analysis.getEncounterMultiplier());
        }

        @Test
        @DisplayName("should analyze with tension bonus")
        void shouldAnalyzeWithTensionBonus() {
            // Level 1, party 4: thresholds = {25, 50, 100, 200, 300}
            // 100 * 1.0 + 150 = 250, which is Deadly (>= 200 hard)
            EncounterAnalysis analysis = encounterService.analyzeEncounter(1, 4, 100, 0, 150);
            assertEquals(250, analysis.getAdjustedXp());
            assertEquals(150, analysis.getTensionBonus());
        }

        @Test
        @DisplayName("should include recommendation")
        void shouldIncludeRecommendation() {
            EncounterAnalysis analysis = encounterService.analyzeEncounter(1, 4, 400, 0, 0);
            assertNotNull(analysis.getRecommendation());
            assertTrue(analysis.getRecommendation().length() > 0);
        }

        @Test
        @DisplayName("should include all thresholds")
        void shouldIncludeAllThresholds() {
            EncounterAnalysis analysis = encounterService.analyzeEncounter(1, 4, 100, 0, 0);
            assertEquals(25, analysis.getTrivialThreshold());
            assertEquals(50, analysis.getEasyThreshold());
            assertEquals(100, analysis.getMediumThreshold());
            assertEquals(200, analysis.getHardThreshold());
            assertEquals(300, analysis.getDeadlyThreshold());
        }

        @Test
        @DisplayName("should clamp party level and size")
        void shouldClampLevelAndSize() {
            EncounterAnalysis analysis = encounterService.analyzeEncounter(25, 0, 400, 0, 0);
            assertEquals(20, analysis.getPartyLevel());
            assertEquals(1, analysis.getPartySize());
        }
    }

    // ==================== Tension Bonus ====================

    @Nested
    @DisplayName("calculateTensionBonus Tests")
    class CalculateTensionBonusTests {

        @Test
        @DisplayName("should return 0 for tension level 0")
        void shouldReturnZeroForZeroTension() {
            assertEquals(0, encounterService.calculateTensionBonus(1000, 0));
        }

        @Test
        @DisplayName("should return 0 for negative tension level")
        void shouldReturnZeroForNegativeTension() {
            assertEquals(0, encounterService.calculateTensionBonus(1000, -5));
        }

        @Test
        @DisplayName("should return 10% per tension level")
        void shouldReturn10PercentPerLevel() {
            assertEquals(100, encounterService.calculateTensionBonus(1000, 1));
            assertEquals(200, encounterService.calculateTensionBonus(1000, 2));
            assertEquals(500, encounterService.calculateTensionBonus(1000, 5));
        }
    }

    // ==================== CRUD Operations ====================

    @Nested
    @DisplayName("createEncounter Tests")
    class CreateEncounterTests {

        @Test
        @DisplayName("should create an encounter with auto-calculated fields")
        void shouldCreateEncounter() {
            Dnd5eEncounter encounter = new Dnd5eEncounter();
            encounter.setName("Test Encounter");
            encounter.setDescription("Test");
            encounter.setPartyLevel(1);
            encounter.setPartySize(4);
            encounter.setMonsterXp(400);
            encounter.setMonsterCount(4);
            encounter.setAverageCr(0.25);

            when(encounterRepository.save(any())).thenReturn(encounter);

            Dnd5eEncounter saved = encounterService.createEncounter(encounter);

            assertNotNull(saved);
            // Level 1, party 4: thresholds = {25, 50, 100, 200, 300}
            // 400 XP >= 200 (hard threshold) -> Deadly
            assertEquals("Deadly", saved.getDifficulty());
            assertEquals(1.0, saved.getEncounterMultiplier());
            assertEquals(400, saved.getAdjustedXp());
            verify(encounterRepository).save(encounter);
        }

        @Test
        @DisplayName("should calculate difficulty with tension bonus")
        void shouldCalculateDifficultyWithTension() {
            Dnd5eEncounter encounter = new Dnd5eEncounter();
            encounter.setName("Tension Encounter");
            encounter.setDescription("Test");
            encounter.setPartyLevel(1);
            encounter.setPartySize(4);
            encounter.setMonsterXp(100);
            encounter.setMonsterCount(4);
            encounter.setAverageCr(0.25);
            encounter.setTensionBonus(150);

            when(encounterRepository.save(any())).thenReturn(encounter);

            Dnd5eEncounter saved = encounterService.createEncounter(encounter);
            // 100 * 1.0 + 150 = 250, thresholds = {25, 50, 100, 200, 300}
            // 250 >= 200 (hard threshold) -> Deadly
            assertEquals("Deadly", saved.getDifficulty());
            assertEquals(250, saved.getAdjustedXp());
        }
    }

    @Nested
    @DisplayName("getEncounter Tests")
    class GetEncounterTests {

        @Test
        @DisplayName("should return encounter when found")
        void shouldReturnEncounter() {
            Dnd5eEncounter encounter = new Dnd5eEncounter();
            encounter.setId(1L);
            encounter.setName("Test");
            when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

            Optional<Dnd5eEncounter> result = encounterService.getEncounter(1L);

            assertTrue(result.isPresent());
            assertEquals("Test", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty() {
            when(encounterRepository.findById(1L)).thenReturn(Optional.empty());
            Optional<Dnd5eEncounter> result = encounterService.getEncounter(1L);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("listEncounters Tests")
    class ListEncountersTests {

        @Test
        @DisplayName("should return all active encounters")
        void shouldReturnAllActive() {
            when(encounterRepository.findByActiveTrue()).thenReturn(List.of(
                    new Dnd5eEncounter(), new Dnd5eEncounter()
            ));
            assertEquals(2, encounterService.listEncounters().size());
        }

        @Test
        @DisplayName("should return empty list when no encounters")
        void shouldReturnEmpty() {
            when(encounterRepository.findByActiveTrue()).thenReturn(List.of());
            assertTrue(encounterService.listEncounters().isEmpty());
        }
    }

    @Nested
    @DisplayName("listEncountersByDifficulty Tests")
    class ListByDifficultyTests {

        @Test
        @DisplayName("should filter by difficulty")
        void shouldFilterByDifficulty() {
            when(encounterRepository.findByDifficultyAndActiveTrue("Deadly"))
                    .thenReturn(List.of(new Dnd5eEncounter()));
            assertEquals(1, encounterService.listEncountersByDifficulty("Deadly").size());
        }
    }

    @Nested
    @DisplayName("deleteEncounter Tests")
    class DeleteEncounterTests {

        @Test
        @DisplayName("should deactivate an encounter")
        void shouldDeactivate() {
            Dnd5eEncounter encounter = new Dnd5eEncounter();
            encounter.setId(1L);
            encounter.setActive(true);
            when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

            encounterService.deleteEncounter(1L);

            assertFalse(encounter.isActive());
            verify(encounterRepository).save(encounter);
        }

        @Test
        @DisplayName("should do nothing when encounter not found")
        void shouldDoNothingWhenNotFound() {
            when(encounterRepository.findById(1L)).thenReturn(Optional.empty());
            encounterService.deleteEncounter(1L);
            verify(encounterRepository, never()).save(any());
        }
    }

    // ==================== Reference Data ====================

    @Nested
    @DisplayName("listDifficulties Tests")
    class ListDifficultiesTests {

        @Test
        @DisplayName("should return all 5 difficulty levels")
        void shouldReturnAllDifficulties() {
            List<String> difficulties = encounterService.listDifficulties();
            assertEquals(5, difficulties.size());
            assertTrue(difficulties.contains("Trivial"));
            assertTrue(difficulties.contains("Easy"));
            assertTrue(difficulties.contains("Medium"));
            assertTrue(difficulties.contains("Hard"));
            assertTrue(difficulties.contains("Deadly"));
        }
    }

    @Nested
    @DisplayName("getXpThresholdsByLevel Tests")
    class GetXpThresholdsByLevelTests {

        @Test
        @DisplayName("should return thresholds for all 20 levels")
        void shouldReturnAllLevels() {
            Map<Integer, int[]> thresholds = encounterService.getXpThresholdsByLevel(4);
            assertEquals(20, thresholds.size());
            assertNotNull(thresholds.get(1));
            assertNotNull(thresholds.get(20));
        }
    }

    @Nested
    @DisplayName("getCrXpTable Tests")
    class GetCrXpTableTests {

        @Test
        @DisplayName("should return CR table with fractional and integer CRs")
        void shouldReturnFullTable() {
            Map<String, Integer> table = encounterService.getCrXpTable();
            assertTrue(table.containsKey("0"));
            assertTrue(table.containsKey("1/8"));
            assertTrue(table.containsKey("1/4"));
            assertTrue(table.containsKey("1/2"));
            assertTrue(table.containsKey("1"));
            assertTrue(table.containsKey("30"));
        }
    }

    @Nested
    @DisplayName("getMultiplierTable Tests")
    class GetMultiplierTableTests {

        @Test
        @DisplayName("should return multiplier table")
        void shouldReturnMultiplierTable() {
            Map<Integer, Double> table = encounterService.getMultiplierTable();
            assertEquals(1.0, table.get(0));
            assertEquals(1.5, table.get(1));
            assertEquals(1.5, table.get(2));
            assertEquals(2.0, table.get(3));
        }
    }

    // ==================== Database Initialization ====================

    @Nested
    @DisplayName("initializeEncounterDatabase Tests")
    class InitializeDatabaseTests {

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfPopulated() {
            // Create a fresh mock to test initialization logic
            Dnd5eEncounterRepository freshRepo = mock(Dnd5eEncounterRepository.class);
            when(freshRepo.countByActiveTrue()).thenReturn(10L);
            new Dnd5eEncounterService(freshRepo);
            verify(freshRepo, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("should populate if empty")
        void shouldPopulateIfEmpty() {
            Dnd5eEncounterRepository freshRepo = mock(Dnd5eEncounterRepository.class);
            when(freshRepo.countByActiveTrue()).thenReturn(0L);
            when(freshRepo.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            new Dnd5eEncounterService(freshRepo);
            verify(freshRepo).saveAll(anyList());
        }
    }

    // ==================== Search ====================

    @Nested
    @DisplayName("searchEncounters Tests")
    class SearchEncountersTests {

        @Test
        @DisplayName("should search by keyword")
        void shouldSearchByKeyword() {
            when(encounterRepository.searchByKeyword("dragon"))
                    .thenReturn(List.of(new Dnd5eEncounter()));
            assertEquals(1, encounterService.searchEncounters("dragon").size());
        }

        @Test
        @DisplayName("should return empty when no matches")
        void shouldReturnEmpty() {
            when(encounterRepository.searchByKeyword("nonexistent"))
                    .thenReturn(List.of());
            assertTrue(encounterService.searchEncounters("nonexistent").isEmpty());
        }
    }

    // ==================== Tension Encounter Creation ====================

    @Nested
    @DisplayName("createTensionEncounter Tests")
    class CreateTensionEncounterTests {

        @Test
        @DisplayName("should create encounter with tension bonus")
        void shouldCreateWithTensionBonus() {
            when(encounterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eEncounter encounter = encounterService.createTensionEncounter(
                    "Tension Test", "Test", 1, 4, 1000, 5, 1.0, 3);

            // Tension bonus = 1000 * 3 * 0.1 = 300
            assertEquals(300, encounter.getTensionBonus());
            assertEquals(1300, encounter.getAdjustedXp()); // 1000 * 1.0 + 300
            // Level 1, party 4: thresholds = {25, 50, 100, 200, 300}
            // 1300 >= 200 (hard) -> Deadly
            assertEquals("Deadly", encounter.getDifficulty());
        }
    }

    // ==================== Analyze by ID ====================

    @Nested
    @DisplayName("analyzeEncounter (by ID) Tests")
    class AnalyzeByIdTests {

        @Test
        @DisplayName("should analyze existing encounter")
        void shouldAnalyzeExisting() {
            Dnd5eEncounter encounter = new Dnd5eEncounter();
            encounter.setId(1L);
            encounter.setPartyLevel(1);
            encounter.setPartySize(4);
            encounter.setMonsterXp(400);
            encounter.setRecentEncounters(0);
            encounter.setTensionBonus(0);
            when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

            EncounterAnalysis analysis = encounterService.analyzeEncounter(1L);

            assertNotNull(analysis);
            // Level 1, party 4: thresholds = {25, 50, 100, 200, 300}
            // 400 XP >= 200 (hard) -> Deadly
            assertEquals("Deadly", analysis.getDifficulty());
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNull() {
            when(encounterRepository.findById(1L)).thenReturn(Optional.empty());
            assertNull(encounterService.analyzeEncounter(1L));
        }
    }
}
