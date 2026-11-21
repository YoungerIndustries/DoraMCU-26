package com.yisy.modules.doramcu.corporation.model;

import com.yisy.modules.doramcu.model.CareerLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorporateHiringCareerTest {

    @Test
    void testDefaultCareerFields() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        assertEquals(CareerLevel.JUNIOR, hiring.getCareerLevel());
        assertEquals(0, hiring.getXpEarned());
        assertTrue(hiring.getSkills().isEmpty());
    }

    @Test
    void testSetCareerLevel() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setCareerLevel(CareerLevel.SENIOR);
        assertEquals(CareerLevel.SENIOR, hiring.getCareerLevel());
    }

    @Test
    void testSetXpEarned() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setXpEarned(50);
        assertEquals(50, hiring.getXpEarned());
    }

    @Test
    void testLevelThreshold() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        assertEquals(100, hiring.getLevelThreshold()); // JUNIOR -> SENIOR

        hiring.setCareerLevel(CareerLevel.SENIOR);
        assertEquals(500, hiring.getLevelThreshold()); // SENIOR -> LEAD

        hiring.setCareerLevel(CareerLevel.DIRECTOR);
        assertEquals(Integer.MAX_VALUE, hiring.getLevelThreshold()); // MAX
    }

    @Test
    void testIsEligibleForPromotion_notEnoughXp() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setXpEarned(50);
        assertFalse(hiring.isEligibleForPromotion());
    }

    @Test
    void testIsEligibleForPromotion_enoughXp() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setXpEarned(100);
        assertTrue(hiring.isEligibleForPromotion());
    }

    @Test
    void testIsEligibleForPromotion_maxLevel() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setCareerLevel(CareerLevel.DIRECTOR);
        hiring.setXpEarned(5000);
        assertFalse(hiring.isEligibleForPromotion());
    }

    @Test
    void testPromote_success() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setXpEarned(100);
        assertTrue(hiring.promote());
        assertEquals(CareerLevel.SENIOR, hiring.getCareerLevel());
    }

    @Test
    void testPromote_notEnoughXp() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setXpEarned(50);
        assertFalse(hiring.promote());
        assertEquals(CareerLevel.JUNIOR, hiring.getCareerLevel());
    }

    @Test
    void testPromote_maxLevel() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setCareerLevel(CareerLevel.DIRECTOR);
        hiring.setXpEarned(5000);
        assertFalse(hiring.promote());
    }

    @Test
    void testDemote_success() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setCareerLevel(CareerLevel.SENIOR);
        assertTrue(hiring.demote());
        assertEquals(CareerLevel.JUNIOR, hiring.getCareerLevel());
    }

    @Test
    void testDemote_minLevel() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        assertFalse(hiring.demote());
        assertEquals(CareerLevel.JUNIOR, hiring.getCareerLevel());
    }

    @Test
    void testEffectiveSalary() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        assertEquals(100L, hiring.getEffectiveSalary()); // JUNIOR: 100 * 1.0

        hiring.setCareerLevel(CareerLevel.SENIOR);
        assertEquals(120L, hiring.getEffectiveSalary()); // SENIOR: 100 * 1.2

        hiring.setCareerLevel(CareerLevel.LEAD);
        assertEquals(144L, hiring.getEffectiveSalary()); // LEAD: 100 * 1.44
    }

    @Test
    void testSkillLevel_default() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        assertEquals(1, hiring.getSkillLevel("NEGOTIATION"));
    }

    @Test
    void testSkillLevel_setAndGet() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setSkillLevel("NEGOTIATION", 5);
        assertEquals(5, hiring.getSkillLevel("NEGOTIATION"));
    }

    @Test
    void testSkillLevel_clampMin() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setSkillLevel("NEGOTIATION", 0);
        assertEquals(1, hiring.getSkillLevel("NEGOTIATION"));
    }

    @Test
    void testSkillLevel_clampMax() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setSkillLevel("NEGOTIATION", 15);
        assertEquals(10, hiring.getSkillLevel("NEGOTIATION"));
    }

    @Test
    void testToString_includesCareerInfo() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 100L);
        hiring.setXpEarned(50);
        hiring.setSkillLevel("NEGOTIATION", 3);
        String str = hiring.toString();
        assertTrue(str.contains("careerLevel=JUNIOR"));
        assertTrue(str.contains("xpEarned=50"));
        assertTrue(str.contains("skills="));
    }
}
