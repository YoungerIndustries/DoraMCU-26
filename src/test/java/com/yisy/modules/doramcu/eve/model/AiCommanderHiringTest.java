package com.yisy.modules.doramcu.eve.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiCommanderHiring Tests")
class AiCommanderHiringTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty hiring")
        void noArgConstructor() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            assertNull(hiring.getId());
            assertNull(hiring.getAiCharacterId());
            assertNull(hiring.getPlayerId());
            assertNull(hiring.getStatus());
            assertEquals(0, hiring.getDailySalary());
            assertEquals(0, hiring.getContractDurationDays());
            assertEquals(0, hiring.getBattlesFought());
            assertEquals(0, hiring.getVictories());
            assertEquals(5.0, hiring.getPerformanceRating());
        }

        @Test
        @DisplayName("full constructor should set aiCharacterId, playerId, dailySalary, status, and hireDate")
        void fullConstructor() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);

            assertEquals(1L, hiring.getAiCharacterId());
            assertEquals("player123", hiring.getPlayerId());
            assertEquals(500L, hiring.getDailySalary());
            assertEquals(AiCommanderHiring.Status.ACTIVE, hiring.getStatus());
            assertNotNull(hiring.getHireDate());
            assertEquals(0, hiring.getContractDurationDays());
            assertEquals(0, hiring.getBattlesFought());
        }
    }

    @Nested
    @DisplayName("Status Enum")
    class StatusEnumTests {

        @Test
        @DisplayName("should have all 4 status values")
        void allStatusValues() {
            AiCommanderHiring.Status[] statuses = AiCommanderHiring.Status.values();
            assertEquals(4, statuses.length);
        }

        @Test
        @DisplayName("ACTIVE status should be accessible")
        void activeStatus() {
            assertEquals("ACTIVE", AiCommanderHiring.Status.ACTIVE.name());
        }

        @Test
        @DisplayName("EXPIRED status should be accessible")
        void expiredStatus() {
            assertEquals("EXPIRED", AiCommanderHiring.Status.EXPIRED.name());
        }

        @Test
        @DisplayName("TERMINATED status should be accessible")
        void terminatedStatus() {
            assertEquals("TERMINATED", AiCommanderHiring.Status.TERMINATED.name());
        }

        @Test
        @DisplayName("DISCHARGED status should be accessible")
        void dischargedStatus() {
            assertEquals("DISCHARGED", AiCommanderHiring.Status.DISCHARGED.name());
        }

        @Test
        @DisplayName("status should be settable and gettable")
        void statusSetter() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setStatus(AiCommanderHiring.Status.TERMINATED);
            assertEquals(AiCommanderHiring.Status.TERMINATED, hiring.getStatus());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get ID")
        void id() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setId(42L);
            assertEquals(42L, hiring.getId());
        }

        @Test
        @DisplayName("should set and get aiCharacterId")
        void aiCharacterId() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setAiCharacterId(7L);
            assertEquals(7L, hiring.getAiCharacterId());
        }

        @Test
        @DisplayName("should set and get playerId")
        void playerId() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setPlayerId("player456");
            assertEquals("player456", hiring.getPlayerId());
        }

        @Test
        @DisplayName("should set and get dailySalary")
        void dailySalary() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setDailySalary(1000L);
            assertEquals(1000L, hiring.getDailySalary());
        }

        @Test
        @DisplayName("should set and get contractDurationDays")
        void contractDurationDays() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setContractDurationDays(30);
            assertEquals(30, hiring.getContractDurationDays());
        }

        @Test
        @DisplayName("should set and get hireDate")
        void hireDate() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            LocalDateTime date = LocalDateTime.of(2024, 1, 15, 9, 0);
            hiring.setHireDate(date);
            assertEquals(date, hiring.getHireDate());
        }

        @Test
        @DisplayName("should set and get expiryDate")
        void expiryDate() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            LocalDateTime date = LocalDateTime.of(2024, 2, 15, 9, 0);
            hiring.setExpiryDate(date);
            assertEquals(date, hiring.getExpiryDate());
        }

        @Test
        @DisplayName("should set and get battlesFought")
        void battlesFought() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setBattlesFought(25);
            assertEquals(25, hiring.getBattlesFought());
        }

        @Test
        @DisplayName("should set and get victories")
        void victories() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setVictories(20);
            assertEquals(20, hiring.getVictories());
        }

        @Test
        @DisplayName("should set and get performanceNotes")
        void performanceNotes() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setPerformanceNotes("Excellent tactical commander");
            assertEquals("Excellent tactical commander", hiring.getPerformanceNotes());
        }
    }

    @Nested
    @DisplayName("Performance Rating")
    class PerformanceRatingTests {

        @Test
        @DisplayName("performance rating should default to 5.0")
        void defaultPerformanceRating() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            assertEquals(5.0, hiring.getPerformanceRating());
        }

        @Test
        @DisplayName("performance rating should be clamped to minimum 1.0")
        void clampMinimumRating() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setPerformanceRating(0.0);
            assertEquals(1.0, hiring.getPerformanceRating());
        }

        @Test
        @DisplayName("performance rating should be clamped to maximum 10.0")
        void clampMaximumRating() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setPerformanceRating(15.0);
            assertEquals(10.0, hiring.getPerformanceRating());
        }

        @Test
        @DisplayName("performance rating should accept values within range")
        void ratingWithinRange() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setPerformanceRating(7.5);
            assertEquals(7.5, hiring.getPerformanceRating());
        }

        @Test
        @DisplayName("performance rating should clamp negative values to 1.0")
        void clampNegativeRating() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setPerformanceRating(-5.0);
            assertEquals(1.0, hiring.getPerformanceRating());
        }
    }

    @Nested
    @DisplayName("isActive")
    class IsActiveTests {

        @Test
        @DisplayName("should return true when status is ACTIVE")
        void isActiveWhenActive() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            assertTrue(hiring.isActive());
        }

        @Test
        @DisplayName("should return false when status is EXPIRED")
        void isNotActiveWhenExpired() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setStatus(AiCommanderHiring.Status.EXPIRED);
            assertFalse(hiring.isActive());
        }

        @Test
        @DisplayName("should return false when status is TERMINATED")
        void isNotActiveWhenTerminated() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setStatus(AiCommanderHiring.Status.TERMINATED);
            assertFalse(hiring.isActive());
        }

        @Test
        @DisplayName("should return false when status is DISCHARGED")
        void isNotActiveWhenDischarged() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setStatus(AiCommanderHiring.Status.DISCHARGED);
            assertFalse(hiring.isActive());
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpiredTests {

        @Test
        @DisplayName("should return true when status is not ACTIVE")
        void isExpiredWhenNotActive() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            hiring.setStatus(AiCommanderHiring.Status.EXPIRED);
            assertTrue(hiring.isExpired());
        }

        @Test
        @DisplayName("should return false when active and no expiry date")
        void isNotExpiredWhenNoExpiry() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            assertFalse(hiring.isExpired());
        }

        @Test
        @DisplayName("should return false when active and expiry date is in the future")
        void isNotExpiredWhenFutureExpiry() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setExpiryDate(LocalDateTime.now().plusDays(30));
            assertFalse(hiring.isExpired());
        }

        @Test
        @DisplayName("should return true when active and expiry date is in the past")
        void isExpiredWhenPastExpiry() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setExpiryDate(LocalDateTime.now().minusDays(1));
            assertTrue(hiring.isExpired());
        }

        @Test
        @DisplayName("should return false when expiry date equals now")
        void isNotExpiredWhenExpiryEqualsNow() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setExpiryDate(LocalDateTime.now());
            assertFalse(hiring.isExpired());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            AiCommanderHiring hiring = new AiCommanderHiring(1L, "player123", 500L);
            hiring.setId(10L);
            hiring.setBattlesFought(15);
            hiring.setVictories(12);

            String str = hiring.toString();
            assertTrue(str.contains("AiCommanderHiring{"));
            assertTrue(str.contains("id=10"));
            assertTrue(str.contains("aiCharacterId=1"));
            assertTrue(str.contains("playerId='player123'"));
            assertTrue(str.contains("status=ACTIVE"));
            assertTrue(str.contains("salary=500"));
            assertTrue(str.contains("battles=15"));
            assertTrue(str.contains("victories=12"));
        }

        @Test
        @DisplayName("toString should handle null fields gracefully")
        void toStringNullFields() {
            AiCommanderHiring hiring = new AiCommanderHiring();
            String str = hiring.toString();
            assertNotNull(str);
            assertTrue(str.contains("AiCommanderHiring{"));
        }
    }
}
