package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.model.CareerLevel;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareerProgressionServiceTest {

    @Mock
    private CorporateHiringRepository hiringRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    private CareerProgressionService service;

    @BeforeEach
    void setUp() {
        service = new CareerProgressionService(hiringRepository, aiCharacterRepository);
    }

    private CorporateHiring createHire(Long id, CareerLevel level, int xp) {
        CorporateHiring hire = new CorporateHiring(1L, id, CorporatePosition.SECURITY_CHIEF, 100L);
        hire.setId(id);
        hire.setCareerLevel(level);
        hire.setXpEarned(xp);
        hire.setPerformanceRating(7.0);
        return hire;
    }

    // ---- awardXp Tests ----

    @Test
    void awardXp_notFound() {
        when(hiringRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<CorporateHiring> result = service.awardXp(999L, 50, CorporateContract.ContractType.SECURITY);
        assertTrue(result.isEmpty());
    }

    @Test
    void awardXp_inactiveHire() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        hire.setStatus(CorporateHiring.Status.TERMINATED);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));
        Optional<CorporateHiring> result = service.awardXp(1L, 50, CorporateContract.ContractType.SECURITY);
        assertTrue(result.isEmpty());
    }

    @Test
    void awardXp_matchingSkill_givesDoubleXp() {
        // SECURITY_CHIEF -> COMBAT -> SECURITY contracts
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));

        service.awardXp(1L, 50, CorporateContract.ContractType.SECURITY);

        assertEquals(50, hire.getXpEarned());
        // 2x XP for matching skill = 100, 100/50 = 2 levels, so skill goes from 1 to 3
        assertEquals(3, hire.getSkillLevel("COMBAT"));
    }

    @Test
    void awardXp_nonMatchingSkill_normalXp() {
        // SECURITY_CHIEF -> COMBAT -> TRADE is not matching
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));

        service.awardXp(1L, 30, CorporateContract.ContractType.TRADE);

        assertEquals(30, hire.getXpEarned());
        // 30/50 = 0, no skill increase
        assertEquals(1, hire.getSkillLevel("COMBAT"));
    }

    @Test
    void awardXp_skillLevelCappedAt10() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        hire.setSkillLevel("COMBAT", 9);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));

        service.awardXp(1L, 100, CorporateContract.ContractType.SECURITY);

        // 2x XP = 200, 200/50 = 4 levels, 9 + 4 = 13, capped at 10
        assertEquals(10, hire.getSkillLevel("COMBAT"));
    }

    @Test
    void awardXp_triggersAutoLevelUp() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 80);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));
        when(hiringRepository.save(any(CorporateHiring.class))).thenReturn(hire);

        service.awardXp(1L, 30, CorporateContract.ContractType.SECURITY);

        // Total XP = 110 >= 100 threshold for SENIOR
        assertEquals(110, hire.getXpEarned());
        assertEquals(CareerLevel.SENIOR, hire.getCareerLevel());
    }

    // ---- promote Tests ----

    @Test
    void promote_notFound() {
        when(hiringRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<CorporateHiring> result = service.promote(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void promote_notEligible() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 50);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));
        Optional<CorporateHiring> result = service.promote(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void promote_success() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 100);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));
        when(hiringRepository.save(any(CorporateHiring.class))).thenReturn(hire);
        Optional<CorporateHiring> result = service.promote(1L);
        assertTrue(result.isPresent());
        assertEquals(CareerLevel.SENIOR, hire.getCareerLevel());
    }

    // ---- demote Tests ----

    @Test
    void demote_notFound() {
        when(hiringRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<CorporateHiring> result = service.demote(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void demote_alreadyJunior() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));
        Optional<CorporateHiring> result = service.demote(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void demote_success() {
        CorporateHiring hire = createHire(1L, CareerLevel.SENIOR, 100);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));
        when(hiringRepository.save(any(CorporateHiring.class))).thenReturn(hire);
        Optional<CorporateHiring> result = service.demote(1L);
        assertTrue(result.isPresent());
        assertEquals(CareerLevel.JUNIOR, hire.getCareerLevel());
    }

    // ---- getCareerDetails Tests ----

    @Test
    void getCareerDetails_notFound() {
        when(hiringRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Map<String, Object>> result = service.getCareerDetails(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCareerDetails_success() {
        CorporateHiring hire = createHire(1L, CareerLevel.SENIOR, 200);
        hire.setSkillLevel("COMBAT", 5);
        when(hiringRepository.findById(1L)).thenReturn(Optional.of(hire));

        AiCharacter aiChar = new AiCharacter();
        aiChar.setId(1L);
        aiChar.setName("TestAI");
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(aiChar));

        Optional<Map<String, Object>> result = service.getCareerDetails(1L);

        assertTrue(result.isPresent());
        Map<String, Object> details = result.get();
        assertEquals(1L, details.get("hiringId"));
        assertEquals("SECURITY_CHIEF", details.get("position"));
        assertEquals("SENIOR", details.get("careerLevel"));
        assertEquals(200, details.get("xpEarned"));
        assertEquals(500, details.get("nextLevelThreshold"));
        assertFalse((Boolean) details.get("eligibleForPromotion"));
        assertEquals("COMBAT", details.get("primarySkill"));
        assertEquals(5, details.get("primarySkillLevel"));
        assertEquals(1.2, details.get("salaryMultiplier"));
    }

    // ---- calculateEffectivePerformance Tests ----

    @Test
    void calculateEffectivePerformance_matchingSkill_above5() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        hire.setPerformanceRating(7.0);
        hire.setSkillLevel("COMBAT", 8);

        // SECURITY_CHIEF -> COMBAT -> SECURITY is matching
        double perf = service.calculateEffectivePerformance(hire, CorporateContract.ContractType.SECURITY);

        // 7.0 + (8 - 5) * 0.5 = 7.0 + 1.5 = 8.5
        assertEquals(8.5, perf, 0.01);
    }

    @Test
    void calculateEffectivePerformance_matchingSkill_at5() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        hire.setPerformanceRating(7.0);
        hire.setSkillLevel("COMBAT", 5);

        double perf = service.calculateEffectivePerformance(hire, CorporateContract.ContractType.SECURITY);

        // 7.0 + (5 - 5) * 0.5 = 7.0
        assertEquals(7.0, perf, 0.01);
    }

    @Test
    void calculateEffectivePerformance_nonMatchingSkill() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        hire.setPerformanceRating(7.0);
        hire.setSkillLevel("COMBAT", 8);

        // SECURITY_CHIEF -> COMBAT -> TRADE is not matching
        double perf = service.calculateEffectivePerformance(hire, CorporateContract.ContractType.TRADE);

        // No bonus for non-matching skill
        assertEquals(7.0, perf, 0.01);
    }

    @Test
    void calculateEffectivePerformance_nullPerformanceRating() {
        CorporateHiring hire = createHire(1L, CareerLevel.JUNIOR, 0);
        hire.setPerformanceRating(null);
        hire.setSkillLevel("COMBAT", 8);

        double perf = service.calculateEffectivePerformance(hire, CorporateContract.ContractType.SECURITY);

        // Default 5.0 + (8 - 5) * 0.5 = 5.0 + 1.5 = 6.5
        assertEquals(6.5, perf, 0.01);
    }

    // ---- Constants ----

    @Test
    void testConstants() {
        assertEquals(50, CareerProgressionService.XP_PER_SKILL_LEVEL);
        assertEquals(10, CareerProgressionService.MAX_SKILL_LEVEL);
        assertEquals(1, CareerProgressionService.MIN_SKILL_LEVEL);
        assertEquals(0.5, CareerProgressionService.PERFORMANCE_BONUS_PER_SKILL_ABOVE_5);
    }
}
