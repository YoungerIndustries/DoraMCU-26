package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.CharacterTrainingTour;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.repository.CharacterTrainingTourRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the CharacterTrainingService using an H2 in-memory database.
 * <p>
 * Tests training enrollment, instant training simulation, dismissal,
 * training history, and cost calculation.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TrainingAndCrewTestConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CharacterTrainingService Integration Tests")
class CharacterTrainingServiceIntegrationTest {

    private static final Long TEST_CORPORATION_ID = 1L;

    @Autowired
    private CharacterTrainingService characterTrainingService;

    @Autowired
    private CharacterTrainingTourRepository tourRepository;

    @Autowired
    private CorporateHiringRepository hiringRepository;

    @Autowired
    private AiCharacterRepository aiCharacterRepository;

    private Long aiCharacterId;
    private Long hiringId;

    @BeforeEach
    void setUp() {
        // Create an AI character for training
        AiCharacter ai = new AiCharacter("Cadet Smith", "Engineer", "Test Corp");
        ai.setHirable(true);
        ai.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        ai.setLeadershipRating(5);
        ai.setPrimarySkill("COMBAT");
        ai.setDailySalary(300L);
        ai = aiCharacterRepository.save(ai);
        aiCharacterId = ai.getId();

        // Create a hiring record
        CorporateHiring hire = new CorporateHiring();
        hire.setCorporationId(TEST_CORPORATION_ID);
        hire.setAiCharacterId(aiCharacterId);
        hire.setPosition(CorporatePosition.ENGINEER);
        hire.setStatus(CorporateHiring.Status.ACTIVE);
        hire.setHireDate(LocalDateTime.now());
        hire.setDailySalary(300L);
        hire.setPerformanceRating(6.0);
        hire = hiringRepository.save(hire);
        hiringId = hire.getId();
    }

    @Test
    @DisplayName("Enroll in training should create a tour record")
    void enrollInTraining() {
        CharacterTrainingTour tour = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                2
        );

        assertNotNull(tour.getId());
        assertEquals(TEST_CORPORATION_ID, tour.getCorporationId());
        assertEquals(CharacterTrainingTour.CharacterType.AI_CHARACTER, tour.getCharacterType());
        assertEquals("COMBAT", tour.getSkill());
        assertEquals("Army", tour.getProfession());
        assertEquals(2, tour.getDurationYears());
        assertEquals(CharacterTrainingTour.Status.ENROLLED, tour.getStatus());
        assertEquals(aiCharacterId, tour.getAiCharacterId());
        assertEquals(hiringId, tour.getHiringId());
    }

    @Test
    @DisplayName("Enroll in training should validate duration")
    void enrollInTraining_invalidDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                characterTrainingService.enrollInTraining(
                        TEST_CORPORATION_ID,
                        CharacterTrainingTour.CharacterType.AI_CHARACTER,
                        aiCharacterId.intValue(),
                        hiringId,
                        "COMBAT",
                        "Army",
                        5
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                characterTrainingService.enrollInTraining(
                        TEST_CORPORATION_ID,
                        CharacterTrainingTour.CharacterType.AI_CHARACTER,
                        aiCharacterId.intValue(),
                        hiringId,
                        "COMBAT",
                        "Army",
                        0
                )
        );
    }

    @Test
    @DisplayName("Simulate instant training should complete the tour")
    void simulateInstantTraining() {
        // Enroll first
        CharacterTrainingTour tour = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                2
        );

        // Simulate instant training
        CharacterTrainingTour completed = characterTrainingService.simulateInstantTraining(tour.getId());

        assertEquals(CharacterTrainingTour.Status.COMPLETED, completed.getStatus());
        assertNotNull(completed.getNarrative());
        assertTrue(completed.getNarrative().length() > 50, "Narrative should be comprehensive");
        assertNotNull(completed.getEventsSummary());
        assertNotNull(completed.getXpAwarded());
        assertTrue(completed.getXpAwarded() > 0, "XP should be awarded");
        assertNotNull(completed.getSkillLevelAfter());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    @DisplayName("Simulate instant training should generate skill-specific narrative")
    void simulateInstantTraining_narrativeContent() {
        CharacterTrainingTour tour = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                4
        );

        CharacterTrainingTour completed = characterTrainingService.simulateInstantTraining(tour.getId());

        String narrative = completed.getNarrative().toLowerCase();
        assertTrue(narrative.contains("combat") || narrative.contains("battle") || narrative.contains("fight"),
                "COMBAT narrative should contain combat-related terms");
    }

    @Test
    @DisplayName("Dismiss from training should set status to DISMISSED")
    void dismissFromTraining() {
        CharacterTrainingTour tour = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "LOGISTICS",
                "Merchant Service",
                3
        );

        characterTrainingService.dismissFromTraining(tour.getId());

        CharacterTrainingTour dismissed = tourRepository.findById(tour.getId()).orElseThrow();
        assertEquals(CharacterTrainingTour.Status.DISMISSED, dismissed.getStatus());
    }

    @Test
    @DisplayName("Get active training tours should return ENROLLED tours")
    void getActiveTrainingTours() {
        assertEquals(0, characterTrainingService.getActiveTrainingTours(TEST_CORPORATION_ID).size());

        characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                2
        );

        List<CharacterTrainingTour> active = characterTrainingService.getActiveTrainingTours(TEST_CORPORATION_ID);
        assertEquals(1, active.size());
        assertEquals(CharacterTrainingTour.Status.ENROLLED, active.get(0).getStatus());
    }

    @Test
    @DisplayName("Get training history should return completed tours")
    void getTrainingHistory() {
        CharacterTrainingTour tour = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "RESEARCH",
                "Science",
                2
        );

        // Initially no completed tours
        List<CharacterTrainingTour> history = characterTrainingService.getTrainingHistory(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue()
        );
        assertEquals(0, history.size());

        // Complete the tour
        characterTrainingService.simulateInstantTraining(tour.getId());

        // Now should have 1 completed tour
        history = characterTrainingService.getTrainingHistory(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue()
        );
        assertEquals(1, history.size());
        assertEquals(CharacterTrainingTour.Status.COMPLETED, history.get(0).getStatus());
    }

    @Test
    @DisplayName("Get available training skills should return all skills")
    void getAvailableTrainingSkills() {
        List<String> skills = characterTrainingService.getAvailableTrainingSkills();
        assertEquals(5, skills.size());
        assertTrue(skills.contains("COMBAT"));
        assertTrue(skills.contains("NEGOTIATION"));
        assertTrue(skills.contains("RESEARCH"));
        assertTrue(skills.contains("LOGISTICS"));
        assertTrue(skills.contains("GOVERNANCE"));
    }

    @Test
    @DisplayName("Calculate training cost should use skill multiplier")
    void calculateTrainingCost() {
        // COMBAT has 1.5x multiplier
        long combatCost = characterTrainingService.calculateTrainingCost("COMBAT", 2);
        assertEquals(15000L, combatCost); // 5000 * 2 * 1.5

        // LOGISTICS has 1.0x multiplier
        long logisticsCost = characterTrainingService.calculateTrainingCost("LOGISTICS", 2);
        assertEquals(10000L, logisticsCost); // 5000 * 2 * 1.0

        // Unknown skill defaults to 1.0x
        long unknownCost = characterTrainingService.calculateTrainingCost("UNKNOWN", 2);
        assertEquals(10000L, unknownCost);
    }

    @Test
    @DisplayName("Get all tours should return all tours for a corporation")
    void getAllTours() {
        characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                2
        );

        characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "LOGISTICS",
                "Merchant Service",
                1
        );

        List<CharacterTrainingTour> allTours = characterTrainingService.getAllTours(TEST_CORPORATION_ID);
        assertEquals(2, allTours.size());
    }

    @Test
    @DisplayName("Simulate instant training should update skill level")
    void simulateInstantTraining_skillLevelUpdate() {
        CharacterTrainingTour tour = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                4
        );

        CharacterTrainingTour completed = characterTrainingService.simulateInstantTraining(tour.getId());

        // Skill level should improve
        assertTrue(completed.getSkillLevelAfter() > completed.getSkillLevelBefore(),
                "Skill level should improve after training");
    }

    @Test
    @DisplayName("Multiple training tours should be tracked separately")
    void multipleTrainingTours() {
        CharacterTrainingTour tour1 = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "COMBAT",
                "Army",
                2
        );

        CharacterTrainingTour tour2 = characterTrainingService.enrollInTraining(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue(),
                hiringId,
                "NEGOTIATION",
                "Diplomacy",
                1
        );

        // Complete first tour
        characterTrainingService.simulateInstantTraining(tour1.getId());

        // Complete second tour
        characterTrainingService.simulateInstantTraining(tour2.getId());

        List<CharacterTrainingTour> history = characterTrainingService.getTrainingHistory(
                TEST_CORPORATION_ID,
                CharacterTrainingTour.CharacterType.AI_CHARACTER,
                aiCharacterId.intValue()
        );
        assertEquals(2, history.size());
    }
}
