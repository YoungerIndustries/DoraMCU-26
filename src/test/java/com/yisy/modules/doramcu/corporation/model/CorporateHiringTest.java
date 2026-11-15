package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorporateHiring Entity Tests")
class CorporateHiringTest {

    @Test
    @DisplayName("constructor should set default values correctly")
    void constructorDefaults() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);

        assertEquals(1L, hiring.getCorporationId());
        assertEquals(100L, hiring.getAiCharacterId());
        assertEquals(CorporatePosition.CFO, hiring.getPosition());
        assertEquals(500L, hiring.getDailySalary());
        assertEquals(CorporateHiring.Status.ACTIVE, hiring.getStatus());
        assertNotNull(hiring.getHireDate());
    }

    @Test
    @DisplayName("all statuses should be present")
    void statuses() {
        CorporateHiring.Status[] statuses = CorporateHiring.Status.values();
        assertEquals(3, statuses.length);
    }

    @Test
    @DisplayName("isActive should return true for active hiring with future expiry")
    void isActiveFuture() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setExpiryDate(LocalDateTime.now().plusDays(30));
        assertTrue(hiring.isActive());
    }

    @Test
    @DisplayName("isActive should return true for active hiring with null expiry")
    void isActiveNullExpiry() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setExpiryDate(null);
        assertTrue(hiring.isActive());
    }

    @Test
    @DisplayName("isActive should return false for active hiring with past expiry")
    void isActivePastExpiry() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setExpiryDate(LocalDateTime.now().minusDays(1));
        assertFalse(hiring.isActive());
    }

    @Test
    @DisplayName("isActive should return false for terminated hiring")
    void isActiveTerminated() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setStatus(CorporateHiring.Status.TERMINATED);
        hiring.setExpiryDate(LocalDateTime.now().plusDays(30));
        assertFalse(hiring.isActive());
    }

    @Test
    @DisplayName("isActive should return false for expired hiring")
    void isActiveExpired() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setStatus(CorporateHiring.Status.EXPIRED);
        assertFalse(hiring.isActive());
    }

    @Test
    @DisplayName("setters should update all fields")
    void setters() {
        CorporateHiring hiring = new CorporateHiring();
        hiring.setId(10L);
        hiring.setCorporationId(5L);
        hiring.setAiCharacterId(42L);
        hiring.setPosition(CorporatePosition.SHIP_CAPTAIN);
        hiring.setStatus(CorporateHiring.Status.TERMINATED);
        LocalDateTime now = LocalDateTime.now();
        hiring.setHireDate(now);
        hiring.setExpiryDate(now.plusDays(365));
        hiring.setDailySalary(750L);
        hiring.setPerformanceRating(8.5);

        assertEquals(10L, hiring.getId());
        assertEquals(5L, hiring.getCorporationId());
        assertEquals(42L, hiring.getAiCharacterId());
        assertEquals(CorporatePosition.SHIP_CAPTAIN, hiring.getPosition());
        assertEquals(CorporateHiring.Status.TERMINATED, hiring.getStatus());
        assertEquals(now, hiring.getHireDate());
        assertEquals(now.plusDays(365), hiring.getExpiryDate());
        assertEquals(750L, hiring.getDailySalary());
        assertEquals(8.5, hiring.getPerformanceRating());
    }

    @Test
    @DisplayName("toString should include key fields")
    void toStringContainsFields() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);
        hiring.setId(10L);

        String str = hiring.toString();
        assertTrue(str.contains("id=10"));
        assertTrue(str.contains("corporationId=1"));
        assertTrue(str.contains("aiCharacterId=100"));
        assertTrue(str.contains("position=CFO"));
        assertTrue(str.contains("salary=500"));
    }

    @Test
    @DisplayName("status transitions should work correctly")
    void statusTransitions() {
        CorporateHiring hiring = new CorporateHiring(1L, 100L, CorporatePosition.CFO, 500L);

        // ACTIVE -> TERMINATED
        assertEquals(CorporateHiring.Status.ACTIVE, hiring.getStatus());
        hiring.setStatus(CorporateHiring.Status.TERMINATED);
        assertEquals(CorporateHiring.Status.TERMINATED, hiring.getStatus());

        // Reset and test ACTIVE -> EXPIRED
        hiring.setStatus(CorporateHiring.Status.ACTIVE);
        hiring.setStatus(CorporateHiring.Status.EXPIRED);
        assertEquals(CorporateHiring.Status.EXPIRED, hiring.getStatus());
    }
}
