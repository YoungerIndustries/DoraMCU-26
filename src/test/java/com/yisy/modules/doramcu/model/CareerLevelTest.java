package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CareerLevelTest {

    @Test
    void testEnumValues() {
        CareerLevel[] levels = CareerLevel.values();
        assertEquals(4, levels.length);
        assertEquals(CareerLevel.JUNIOR, levels[0]);
        assertEquals(CareerLevel.SENIOR, levels[1]);
        assertEquals(CareerLevel.LEAD, levels[2]);
        assertEquals(CareerLevel.DIRECTOR, levels[3]);
    }

    @Test
    void testXpThresholds() {
        assertEquals(0, CareerLevel.JUNIOR.getXpThreshold());
        assertEquals(100, CareerLevel.SENIOR.getXpThreshold());
        assertEquals(500, CareerLevel.LEAD.getXpThreshold());
        assertEquals(2000, CareerLevel.DIRECTOR.getXpThreshold());
    }

    @Test
    void testSalaryMultipliers() {
        assertEquals(1.0, CareerLevel.JUNIOR.getSalaryMultiplier());
        assertEquals(1.2, CareerLevel.SENIOR.getSalaryMultiplier());
        assertEquals(1.44, CareerLevel.LEAD.getSalaryMultiplier());
        assertEquals(1.728, CareerLevel.DIRECTOR.getSalaryMultiplier());
    }

    @Test
    void testIndex() {
        assertEquals(0, CareerLevel.JUNIOR.getIndex());
        assertEquals(1, CareerLevel.SENIOR.getIndex());
        assertEquals(2, CareerLevel.LEAD.getIndex());
        assertEquals(3, CareerLevel.DIRECTOR.getIndex());
    }

    @Test
    void testIsMaxLevel() {
        assertFalse(CareerLevel.JUNIOR.isMaxLevel());
        assertFalse(CareerLevel.SENIOR.isMaxLevel());
        assertFalse(CareerLevel.LEAD.isMaxLevel());
        assertTrue(CareerLevel.DIRECTOR.isMaxLevel());
    }

    @Test
    void testNextLevel() {
        assertEquals(CareerLevel.SENIOR, CareerLevel.JUNIOR.nextLevel());
        assertEquals(CareerLevel.LEAD, CareerLevel.SENIOR.nextLevel());
        assertEquals(CareerLevel.DIRECTOR, CareerLevel.LEAD.nextLevel());
        assertNull(CareerLevel.DIRECTOR.nextLevel());
    }

    @Test
    void testPreviousLevel() {
        assertNull(CareerLevel.JUNIOR.previousLevel());
        assertEquals(CareerLevel.JUNIOR, CareerLevel.SENIOR.previousLevel());
        assertEquals(CareerLevel.SENIOR, CareerLevel.LEAD.previousLevel());
        assertEquals(CareerLevel.LEAD, CareerLevel.DIRECTOR.previousLevel());
    }

    @Test
    void testNextLevelThreshold() {
        assertEquals(100, CareerLevel.JUNIOR.getNextLevelThreshold());
        assertEquals(500, CareerLevel.SENIOR.getNextLevelThreshold());
        assertEquals(2000, CareerLevel.LEAD.getNextLevelThreshold());
        assertEquals(Integer.MAX_VALUE, CareerLevel.DIRECTOR.getNextLevelThreshold());
    }
}
