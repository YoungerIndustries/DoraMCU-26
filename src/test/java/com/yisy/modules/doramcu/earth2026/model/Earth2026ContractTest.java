package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Contract Tests")
class Earth2026ContractTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty contract")
        void noArgConstructor() {
            Earth2026Contract contract = new Earth2026Contract();
            assertNull(contract.getId());
            assertNull(contract.getTitle());
            assertNull(contract.getType());
            assertTrue(contract.isActive());
        }

        @Test
        @DisplayName("full constructor should set title, type, payment, and planet ID")
        void fullConstructor() {
            Earth2026Contract contract = new Earth2026Contract("Security Detail", "Employment", 50000, 1L);
            assertEquals("Security Detail", contract.getTitle());
            assertEquals("Employment", contract.getType());
            assertEquals(50000, contract.getPaymentUsd());
            assertEquals(1L, contract.getPlanetId());
            assertEquals(50, contract.getXpReward());
            assertEquals(1, contract.getMinLevel());
            assertEquals(3, contract.getDifficulty());
            assertEquals(1, contract.getDurationTurns());
            assertEquals("available", contract.getStatus());
            assertTrue(contract.isActive());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all basic fields")
        void basicFields() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setId(1L);
            contract.setTitle("Trade Negotiation");
            contract.setType("Freelance");
            contract.setDescription("Negotiate trade deal");

            assertEquals(1L, contract.getId());
            assertEquals("Trade Negotiation", contract.getTitle());
            assertEquals("Freelance", contract.getType());
            assertEquals("Negotiate trade deal", contract.getDescription());
        }

        @Test
        @DisplayName("should set and get payment and XP")
        void paymentAndXp() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setPaymentUsd(100000);
            contract.setXpReward(200);

            assertEquals(100000, contract.getPaymentUsd());
            assertEquals(200, contract.getXpReward());
        }

        @Test
        @DisplayName("should set and get requirements")
        void requirements() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setMinLevel(5);
            contract.setMinStr(12);
            contract.setMinInt(10);
            contract.setMinCha(14);
            contract.setDifficulty(7);

            assertEquals(5, contract.getMinLevel());
            assertEquals(12, contract.getMinStr());
            assertEquals(10, contract.getMinInt());
            assertEquals(14, contract.getMinCha());
            assertEquals(7, contract.getDifficulty());
        }

        @Test
        @DisplayName("should set and get duration")
        void duration() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setDurationTurns(5);
            assertEquals(5, contract.getDurationTurns());
        }

        @Test
        @DisplayName("should set and get status")
        void status() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("active");
            assertEquals("active", contract.getStatus());
        }

        @Test
        @DisplayName("should set and get assignee")
        void assignee() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setAssigneeId(42L);
            assertEquals(42L, contract.getAssigneeId());
        }

        @Test
        @DisplayName("should set and get location fields")
        void locationFields() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setPlanetId(1L);
            contract.setCountryId(1L);
            contract.setLocationId(5L);

            assertEquals(1L, contract.getPlanetId());
            assertEquals(1L, contract.getCountryId());
            assertEquals(5L, contract.getLocationId());
        }

        @Test
        @DisplayName("should set and get offering org")
        void offeringOrg() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setOfferingOrg("Interstellar Alliance");
            assertEquals("Interstellar Alliance", contract.getOfferingOrg());
        }

        @Test
        @DisplayName("should set and get timestamps")
        void timestamps() {
            Earth2026Contract contract = new Earth2026Contract();
            LocalDateTime now = LocalDateTime.now();
            contract.setCreatedAt(now);
            contract.setCompletedAt(now);
            assertEquals(now, contract.getCreatedAt());
            assertEquals(now, contract.getCompletedAt());
        }

        @Test
        @DisplayName("should set and get active status")
        void activeStatus() {
            Earth2026Contract contract = new Earth2026Contract();
            assertTrue(contract.isActive());
            contract.setActive(false);
            assertFalse(contract.isActive());
        }
    }

    @Nested
    @DisplayName("Status Values")
    class StatusTests {

        @Test
        @DisplayName("should support available status")
        void available() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("available");
            assertEquals("available", contract.getStatus());
        }

        @Test
        @DisplayName("should support active status")
        void active() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("active");
            assertEquals("active", contract.getStatus());
        }

        @Test
        @DisplayName("should support completed status")
        void completed() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("completed");
            assertEquals("completed", contract.getStatus());
        }

        @Test
        @DisplayName("should support failed status")
        void failed() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("failed");
            assertEquals("failed", contract.getStatus());
        }

        @Test
        @DisplayName("should support expired status")
        void expired() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("expired");
            assertEquals("expired", contract.getStatus());
        }
    }

    @Nested
    @DisplayName("Contract Types")
    class TypeTests {

        @Test
        @DisplayName("should support Employment type")
        void employment() {
            Earth2026Contract contract = new Earth2026Contract("Job", "Employment", 50000, 1L);
            assertEquals("Employment", contract.getType());
        }

        @Test
        @DisplayName("should support Freelance type")
        void freelance() {
            Earth2026Contract contract = new Earth2026Contract("Gig", "Freelance", 10000, 1L);
            assertEquals("Freelance", contract.getType());
        }

        @Test
        @DisplayName("should support Government type")
        void government() {
            Earth2026Contract contract = new Earth2026Contract("Mission", "Government", 100000, 1L);
            assertEquals("Government", contract.getType());
        }

        @Test
        @DisplayName("should support Military type")
        void military() {
            Earth2026Contract contract = new Earth2026Contract("Operation", "Military", 75000, 1L);
            assertEquals("Military", contract.getType());
        }

        @Test
        @DisplayName("should support Trade type")
        void trade() {
            Earth2026Contract contract = new Earth2026Contract("Deal", "Trade", 25000, 1L);
            assertEquals("Trade", contract.getType());
        }
    }

    @Nested
    @DisplayName("Expiry Check")
    class ExpiryTests {

        @Test
        @DisplayName("should not be expired when no expiry date set")
        void noExpiryDate() {
            Earth2026Contract contract = new Earth2026Contract();
            assertFalse(contract.isExpired());
        }

        @Test
        @DisplayName("should be expired when expiry date is in the past")
        void pastExpiryDate() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setExpiryDate(LocalDateTime.now().minusDays(1));
            assertTrue(contract.isExpired());
        }

        @Test
        @DisplayName("should not be expired when expiry date is in the future")
        void futureExpiryDate() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setExpiryDate(LocalDateTime.now().plusDays(1));
            assertFalse(contract.isExpired());
        }
    }

    @Nested
    @DisplayName("Availability Check")
    class AvailabilityTests {

        @Test
        @DisplayName("should be available when status is available and not expired")
        void isAvailable() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("available");
            assertTrue(contract.isAvailable());
        }

        @Test
        @DisplayName("should not be available when status is active")
        void notAvailableWhenActive() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("active");
            assertFalse(contract.isAvailable());
        }

        @Test
        @DisplayName("should not be available when expired")
        void notAvailableWhenExpired() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("available");
            contract.setExpiryDate(LocalDateTime.now().minusDays(1));
            assertFalse(contract.isAvailable());
        }

        @Test
        @DisplayName("should not be available when completed")
        void notAvailableWhenCompleted() {
            Earth2026Contract contract = new Earth2026Contract();
            contract.setStatus("completed");
            assertFalse(contract.isAvailable());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Contract contract = new Earth2026Contract("Security Detail", "Employment", 50000, 1L);
            contract.setId(1L);
            contract.setXpReward(100);
            contract.setStatus("active");

            String str = contract.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("title='Security Detail'"));
            assertTrue(str.contains("type='Employment'"));
            assertTrue(str.contains("payment=50000"));
            assertTrue(str.contains("xp=100"));
            assertTrue(str.contains("status='active'"));
        }
    }
}
