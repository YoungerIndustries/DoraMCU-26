package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.eve.model.AiCommanderHiring;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.AiCommanderExperienceRepository;
import com.yisy.modules.doramcu.repository.AiCommanderHiringRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AiCommanderExperienceService Tests")
class AiCommanderExperienceServiceTest {

    private AiCharacterRepository aiCharacterRepository;
    private AiCommanderExperienceRepository experienceRepository;
    private AiCommanderHiringRepository hiringRepository;
    private AiCommanderExperienceService service;

    @BeforeEach
    void setUp() {
        aiCharacterRepository = mock(AiCharacterRepository.class);
        experienceRepository = mock(AiCommanderExperienceRepository.class);
        hiringRepository = mock(AiCommanderHiringRepository.class);
        service = new AiCommanderExperienceService(
                aiCharacterRepository, experienceRepository, hiringRepository);
    }

    private AiCharacter createAiCharacter(Long id, String name) {
        AiCharacter character = new AiCharacter(name, "Leader", "Test Empire");
        character.setId(id);
        character.setHirable(true);
        character.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        character.setDailySalary(500L);
        return character;
    }

    // ---- calculateExperiencePoints ----

    @Nested
    @DisplayName("calculateExperiencePoints")
    class CalculateExperiencePointsTests {

        @Test
        @DisplayName("VICTORY should return 50 XP")
        void victoryXp() {
            assertEquals(50, service.calculateExperiencePoints(AiCommanderExperience.Outcome.VICTORY));
        }

        @Test
        @DisplayName("PYRRHIC_VICTORY should return 30 XP")
        void pyrrhicVictoryXp() {
            assertEquals(30, service.calculateExperiencePoints(AiCommanderExperience.Outcome.PYRRHIC_VICTORY));
        }

        @Test
        @DisplayName("STALEMATE should return 15 XP")
        void stalemateXp() {
            assertEquals(15, service.calculateExperiencePoints(AiCommanderExperience.Outcome.STALEMATE));
        }

        @Test
        @DisplayName("DEFEAT should return 10 XP")
        void defeatXp() {
            assertEquals(10, service.calculateExperiencePoints(AiCommanderExperience.Outcome.DEFEAT));
        }

        @Test
        @DisplayName("ROUTE should return 5 XP")
        void routeXp() {
            assertEquals(5, service.calculateExperiencePoints(AiCommanderExperience.Outcome.ROUTE));
        }
    }

    // ---- calculateLeadershipBonus ----

    @Nested
    @DisplayName("calculateLeadershipBonus")
    class CalculateLeadershipBonusTests {

        @Test
        @DisplayName("VICTORY should return +1 leadership")
        void victoryLeadership() {
            assertEquals(1, service.calculateLeadershipBonus(AiCommanderExperience.Outcome.VICTORY));
        }

        @Test
        @DisplayName("PYRRHIC_VICTORY should return 0 leadership")
        void pyrrhicVictoryLeadership() {
            assertEquals(0, service.calculateLeadershipBonus(AiCommanderExperience.Outcome.PYRRHIC_VICTORY));
        }

        @Test
        @DisplayName("STALEMATE should return 0 leadership")
        void stalemateLeadership() {
            assertEquals(0, service.calculateLeadershipBonus(AiCommanderExperience.Outcome.STALEMATE));
        }

        @Test
        @DisplayName("DEFEAT should return -1 leadership")
        void defeatLeadership() {
            assertEquals(-1, service.calculateLeadershipBonus(AiCommanderExperience.Outcome.DEFEAT));
        }

        @Test
        @DisplayName("ROUTE should return -2 leadership")
        void routeLeadership() {
            assertEquals(-2, service.calculateLeadershipBonus(AiCommanderExperience.Outcome.ROUTE));
        }
    }

    // ---- recordBattleExperience (simple) ----

    @Nested
    @DisplayName("recordBattleExperience (simple)")
    class RecordBattleExperienceSimpleTests {

        @Test
        @DisplayName("should record experience and return saved record")
        void recordExperience() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderExperience result = service.recordBattleExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY);

            assertNotNull(result);
            assertEquals(1L, result.getAiCharacterId());
            assertEquals("THIRD_REICH", result.getGameModuleName());
            assertEquals("Ground Battle", result.getBattleType());
            assertEquals(AiCommanderExperience.Outcome.VICTORY, result.getOutcome());
            assertEquals(50, result.getExperiencePoints());
            assertEquals(1, result.getLeadershipBonus());
            verify(experienceRepository).save(result);
            verify(aiCharacterRepository).save(character);
        }

        @Test
        @DisplayName("should throw when character not found")
        void characterNotFound() {
            when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    service.recordBattleExperience(999L, "THIRD_REICH", "Ground Battle",
                            AiCommanderExperience.Outcome.VICTORY));
        }

        @Test
        @DisplayName("should record victory for VICTORY outcome")
        void recordVictoryOutcome() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.recordBattleExperience(1L, "THIRD_REICH", "Ground Battle",
                    AiCommanderExperience.Outcome.VICTORY);

            assertEquals(1, character.getTotalBattles());
            assertEquals(1, character.getTotalVictories());
        }

        @Test
        @DisplayName("should record defeat for DEFEAT outcome")
        void recordDefeatOutcome() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.recordBattleExperience(1L, "THIRD_REICH", "Ground Battle",
                    AiCommanderExperience.Outcome.DEFEAT);

            assertEquals(1, character.getTotalBattles());
            assertEquals(0, character.getTotalVictories());
        }

        @Test
        @DisplayName("should record victory for PYRRHIC_VICTORY outcome")
        void recordPyrrhicVictoryOutcome() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.recordBattleExperience(1L, "THIRD_REICH", "Ground Battle",
                    AiCommanderExperience.Outcome.PYRRHIC_VICTORY);

            assertEquals(1, character.getTotalBattles());
            assertEquals(1, character.getTotalVictories());
        }
    }

    // ---- recordBattleExperience (detailed) ----

    @Nested
    @DisplayName("recordBattleExperience (detailed)")
    class RecordBattleExperienceDetailedTests {

        @Test
        @DisplayName("should record experience with description and location")
        void recordWithDetails() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderExperience result = service.recordBattleExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY,
                    "Defended Berlin", "Berlin, Germany");

            assertEquals("Defended Berlin", result.getBattleDescription());
            assertEquals("Berlin, Germany", result.getBattleLocation());
        }

        @Test
        @DisplayName("should handle null description and location")
        void recordWithNullDetails() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderExperience result = service.recordBattleExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY,
                    null, null);

            assertNull(result.getBattleDescription());
            assertNull(result.getBattleLocation());
        }
    }

    // ---- recordDetailedBattleExperience ----

    @Nested
    @DisplayName("recordDetailedBattleExperience")
    class RecordDetailedBattleExperienceTests {

        @Test
        @DisplayName("should record all detailed fields")
        void recordAllFields() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderExperience result = service.recordDetailedBattleExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY,
                    "Battle of Berlin", "Berlin", 15, 3, "Armor Tactics", false);

            assertEquals(15, result.getEnemiesDefeated());
            assertEquals(3, result.getFriendlyLosses());
            assertEquals("Armor Tactics", result.getSkillImproved());
            assertFalse(result.isExceptionalPerformance());
        }

        @Test
        @DisplayName("should add bonus XP for exceptional performance")
        void exceptionalPerformanceBonusXp() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderExperience result = service.recordDetailedBattleExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY,
                    "Epic battle", "Berlin", 20, 2, "Armor Tactics", true);

            // VICTORY = 50 XP, exceptional bonus = 50/2 = 25, total = 75
            assertEquals(75, result.getExperiencePoints());
            assertTrue(result.isExceptionalPerformance());
        }

        @Test
        @DisplayName("should not add bonus XP when not exceptional")
        void noBonusXpWhenNotExceptional() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(experienceRepository.save(any(AiCommanderExperience.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderExperience result = service.recordDetailedBattleExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY,
                    "Normal battle", "Berlin", 10, 5, null, false);

            assertEquals(50, result.getExperiencePoints());
        }
    }

    // ---- getExperiences ----

    @Nested
    @DisplayName("getExperiences")
    class GetExperiencesTests {

        @Test
        @DisplayName("should return experiences ordered by date desc")
        void getExperiences() {
            List<AiCommanderExperience> experiences = List.of(
                    new AiCommanderExperience(1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY));
            when(experienceRepository.findByAiCharacterIdOrderByBattleDateDesc(1L)).thenReturn(experiences);

            List<AiCommanderExperience> result = service.getExperiences(1L);

            assertEquals(1, result.size());
            verify(experienceRepository).findByAiCharacterIdOrderByBattleDateDesc(1L);
        }

        @Test
        @DisplayName("should return empty list when no experiences")
        void getExperiencesEmpty() {
            when(experienceRepository.findByAiCharacterIdOrderByBattleDateDesc(1L)).thenReturn(List.of());

            List<AiCommanderExperience> result = service.getExperiences(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- getTotalExperience ----

    @Nested
    @DisplayName("getTotalExperience")
    class GetTotalExperienceTests {

        @Test
        @DisplayName("should return sum of experience points")
        void getTotalExperience() {
            when(experienceRepository.sumExperiencePointsByAiCharacterId(1L)).thenReturn(250);

            int result = service.getTotalExperience(1L);

            assertEquals(250, result);
        }

        @Test
        @DisplayName("should return 0 when no experiences")
        void getTotalExperienceZero() {
            when(experienceRepository.sumExperiencePointsByAiCharacterId(1L)).thenReturn(0);

            int result = service.getTotalExperience(1L);

            assertEquals(0, result);
        }
    }

    // ---- getBattleCount ----

    @Nested
    @DisplayName("getBattleCount")
    class GetBattleCountTests {

        @Test
        @DisplayName("should return battle count")
        void getBattleCount() {
            when(experienceRepository.countByAiCharacterId(1L)).thenReturn(15L);

            long result = service.getBattleCount(1L);

            assertEquals(15, result);
        }
    }

    // ---- getAvailableCommanders ----

    @Nested
    @DisplayName("getAvailableCommanders")
    class GetAvailableCommandersTests {

        @Test
        @DisplayName("should return all available commanders")
        void getAllAvailable() {
            List<AiCharacter> commanders = List.of(createAiCharacter(1L, "Cmdr 1"), createAiCharacter(2L, "Cmdr 2"));
            when(aiCharacterRepository.findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE))
                    .thenReturn(commanders);

            List<AiCharacter> result = service.getAvailableCommanders();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return empty list when no available commanders")
        void noAvailableCommanders() {
            when(aiCharacterRepository.findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE))
                    .thenReturn(List.of());

            List<AiCharacter> result = service.getAvailableCommanders();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should filter by minimum experience")
        void filterByMinExperience() {
            List<AiCharacter> commanders = List.of(createAiCharacter(1L, "Experienced Cmdr"));
            when(aiCharacterRepository.findByTotalExperienceGreaterThanEqualAndHirableTrue(100))
                    .thenReturn(commanders);

            List<AiCharacter> result = service.getAvailableCommanders(100);

            assertEquals(1, result.size());
            verify(aiCharacterRepository).findByTotalExperienceGreaterThanEqualAndHirableTrue(100);
        }
    }

    // ---- hireCommander ----

    @Nested
    @DisplayName("hireCommander")
    class HireCommanderTests {

        @Test
        @DisplayName("should hire commander successfully")
        void hireSuccessfully() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(hiringRepository.existsByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(false);
            when(hiringRepository.save(any(AiCommanderHiring.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderHiring result = service.hireCommander(1L, "player123", 30);

            assertNotNull(result);
            assertEquals(1L, result.getAiCharacterId());
            assertEquals("player123", result.getPlayerId());
            assertEquals(500L, result.getDailySalary());
            assertEquals(AiCommanderHiring.Status.ACTIVE, result.getStatus());
            assertEquals(30, result.getContractDurationDays());
            assertNotNull(result.getExpiryDate());
            assertEquals(AiCharacter.HiringStatus.HIRED, character.getHiringStatus());
            verify(hiringRepository).save(result);
        }

        @Test
        @DisplayName("should hire with indefinite contract")
        void hireIndefinite() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);
            when(hiringRepository.existsByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(false);
            when(hiringRepository.save(any(AiCommanderHiring.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AiCommanderHiring result = service.hireCommander(1L, "player123", 0);

            assertNull(result.getExpiryDate());
            assertEquals(0, result.getContractDurationDays());
        }

        @Test
        @DisplayName("should throw when character not found")
        void characterNotFound() {
            when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    service.hireCommander(999L, "player123", 30));
        }

        @Test
        @DisplayName("should throw when character is not hirable")
        void notHirable() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            character.setHirable(false);
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));

            assertThrows(IllegalStateException.class, () ->
                    service.hireCommander(1L, "player123", 30));
        }

        @Test
        @DisplayName("should throw when character is not available")
        void notAvailable() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            character.setHiringStatus(AiCharacter.HiringStatus.HIRED);
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));

            assertThrows(IllegalStateException.class, () ->
                    service.hireCommander(1L, "player123", 30));
        }

        @Test
        @DisplayName("should throw when character is already hired")
        void alreadyHired() {
            AiCharacter character = createAiCharacter(1L, "Test Commander");
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(hiringRepository.existsByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(true);

            assertThrows(IllegalStateException.class, () ->
                    service.hireCommander(1L, "player123", 30));
        }
    }

    // ---- releaseCommander ----

    @Nested
    @DisplayName("releaseCommander")
    class ReleaseCommanderTests {

        @Test
        @DisplayName("should release commander successfully")
        void releaseSuccessfully() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setId(10L);
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.of(hiring));
            when(hiringRepository.save(hiring)).thenReturn(hiring);

            AiCharacter character = createAiCharacter(1L, "Test Commander");
            character.setHiringStatus(AiCharacter.HiringStatus.HIRED);
            when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(aiCharacterRepository.save(character)).thenReturn(character);

            AiCommanderHiring result = service.releaseCommander(1L);

            assertEquals(AiCommanderHiring.Status.EXPIRED, result.getStatus());
            assertEquals(AiCharacter.HiringStatus.AVAILABLE, character.getHiringStatus());
            verify(hiringRepository).save(result);
            verify(aiCharacterRepository).save(character);
        }

        @Test
        @DisplayName("should throw when commander is not hired")
        void notHired() {
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () ->
                    service.releaseCommander(1L));
        }
    }

    // ---- getActiveHiring ----

    @Nested
    @DisplayName("getActiveHiring")
    class GetActiveHiringTests {

        @Test
        @DisplayName("should return active hiring contract")
        void returnActiveHiring() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.of(hiring));

            Optional<AiCommanderHiring> result = service.getActiveHiring(1L);

            assertTrue(result.isPresent());
            assertEquals("player123", result.get().getPlayerId());
        }

        @Test
        @DisplayName("should return empty when no active hiring")
        void noActiveHiring() {
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.empty());

            Optional<AiCommanderHiring> result = service.getActiveHiring(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- getPlayerHiredCommanders ----

    @Nested
    @DisplayName("getPlayerHiredCommanders")
    class GetPlayerHiredCommandersTests {

        @Test
        @DisplayName("should return all hired commanders for a player")
        void getPlayerCommanders() {
            AiCommanderHiring hiring1 = new AiCommanderHiring(1L, "player123", 500L);
            AiCommanderHiring hiring2 = new AiCommanderHiring(2L, "player123", 300L);
            when(hiringRepository.findByPlayerIdAndStatus("player123", AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(List.of(hiring1, hiring2));

            List<AiCommanderHiring> result = service.getPlayerHiredCommanders("player123");

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return empty list when no hired commanders")
        void noHiredCommanders() {
            when(hiringRepository.findByPlayerIdAndStatus("player123", AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(List.of());

            List<AiCommanderHiring> result = service.getPlayerHiredCommanders("player123");

            assertTrue(result.isEmpty());
        }
    }

    // ---- recordHiredCommanderBattle ----

    @Nested
    @DisplayName("recordHiredCommanderBattle")
    class RecordHiredCommanderBattleTests {

        @Test
        @DisplayName("should record victory for hired commander")
        void recordVictory() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.of(hiring));
            when(hiringRepository.save(hiring)).thenReturn(hiring);

            service.recordHiredCommanderBattle(1L, true);

            assertEquals(1, hiring.getBattlesFought());
            assertEquals(1, hiring.getVictories());
            assertEquals(10.0, hiring.getPerformanceRating());
            verify(hiringRepository).save(hiring);
        }

        @Test
        @DisplayName("should record defeat for hired commander")
        void recordDefeat() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.of(hiring));
            when(hiringRepository.save(hiring)).thenReturn(hiring);

            service.recordHiredCommanderBattle(1L, false);

            assertEquals(1, hiring.getBattlesFought());
            assertEquals(0, hiring.getVictories());
            // 0 victories / 1 battle * 10 = 0.0, clamped to minimum 1.0 by setPerformanceRating
            assertEquals(1.0, hiring.getPerformanceRating());
        }

        @Test
        @DisplayName("should update performance rating after multiple battles")
        void updatePerformanceRating() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setBattlesFought(2);
            hiring.setVictories(2);
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.of(hiring));
            when(hiringRepository.save(hiring)).thenReturn(hiring);

            service.recordHiredCommanderBattle(1L, false);

            assertEquals(3, hiring.getBattlesFought());
            assertEquals(2, hiring.getVictories());
            assertEquals(6.666666666666667, hiring.getPerformanceRating(), 0.001);
        }

        @Test
        @DisplayName("should do nothing when no active hiring")
        void noActiveHiring() {
            when(hiringRepository.findByAiCharacterIdAndStatus(1L, AiCommanderHiring.Status.ACTIVE))
                    .thenReturn(Optional.empty());

            service.recordHiredCommanderBattle(1L, true);

            verify(hiringRepository, never()).save(any());
        }
    }
}
