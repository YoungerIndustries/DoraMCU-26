package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Party Tests")
class Earth2026PartyTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty party")
        void noArgConstructor() {
            Earth2026Party party = new Earth2026Party();
            assertNull(party.getId());
            assertNull(party.getName());
            assertNull(party.getDescription());
            assertNull(party.getLocationId());
            assertNull(party.getLeaderId());
            assertEquals(6, party.getMaxMembers());
            assertEquals("active", party.getStatus());
            assertNull(party.getScenarioId());
            assertNull(party.getCreatedAt());
            assertNull(party.getUpdatedAt());
            assertTrue(party.isRecruiting());
        }

        @Test
        @DisplayName("full constructor should set name, location, leader and timestamp")
        void fullConstructor() {
            Earth2026Party party = new Earth2026Party("The Squad", 1L, 10L);
            assertEquals("The Squad", party.getName());
            assertEquals(1L, party.getLocationId());
            assertEquals(10L, party.getLeaderId());
            assertNotNull(party.getCreatedAt());
            assertEquals("active", party.getStatus());
            assertEquals(6, party.getMaxMembers());
            assertTrue(party.isRecruiting());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get name")
        void name() {
            Earth2026Party party = new Earth2026Party();
            party.setName("Area 51 Investigators");
            assertEquals("Area 51 Investigators", party.getName());
        }

        @Test
        @DisplayName("should set and get description")
        void description() {
            Earth2026Party party = new Earth2026Party();
            party.setDescription("Investigating UFO sightings");
            assertEquals("Investigating UFO sightings", party.getDescription());
        }

        @Test
        @DisplayName("should set and get location ID")
        void locationId() {
            Earth2026Party party = new Earth2026Party();
            party.setLocationId(5L);
            assertEquals(5L, party.getLocationId());
        }

        @Test
        @DisplayName("should set and get leader ID")
        void leaderId() {
            Earth2026Party party = new Earth2026Party();
            party.setLeaderId(42L);
            assertEquals(42L, party.getLeaderId());
        }

        @Test
        @DisplayName("should set and get max members")
        void maxMembers() {
            Earth2026Party party = new Earth2026Party();
            party.setMaxMembers(10);
            assertEquals(10, party.getMaxMembers());
        }

        @Test
        @DisplayName("should set and get status")
        void status() {
            Earth2026Party party = new Earth2026Party();
            party.setStatus("on_scenario");
            assertEquals("on_scenario", party.getStatus());
        }

        @Test
        @DisplayName("should set and get scenario ID")
        void scenarioId() {
            Earth2026Party party = new Earth2026Party();
            party.setScenarioId(7L);
            assertEquals(7L, party.getScenarioId());
        }

        @Test
        @DisplayName("should set and get recruiting flag")
        void recruiting() {
            Earth2026Party party = new Earth2026Party();
            assertTrue(party.isRecruiting());
            party.setRecruiting(false);
            assertFalse(party.isRecruiting());
        }
    }

    @Nested
    @DisplayName("Status Values")
    class StatusTests {

        @Test
        @DisplayName("default status should be active")
        void defaultStatus() {
            Earth2026Party party = new Earth2026Party();
            assertEquals("active", party.getStatus());
        }

        @Test
        @DisplayName("should support disbanded status")
        void disbandedStatus() {
            Earth2026Party party = new Earth2026Party();
            party.setStatus("disbanded");
            assertEquals("disbanded", party.getStatus());
        }

        @Test
        @DisplayName("should support on_scenario status")
        void onScenarioStatus() {
            Earth2026Party party = new Earth2026Party();
            party.setStatus("on_scenario");
            party.setScenarioId(3L);
            assertEquals("on_scenario", party.getStatus());
            assertEquals(3L, party.getScenarioId());
        }
    }

    @Nested
    @DisplayName("Timestamps")
    class TimestampTests {

        @Test
        @DisplayName("should set and get created at")
        void createdAt() {
            Earth2026Party party = new Earth2026Party();
            LocalDateTime now = LocalDateTime.now();
            party.setCreatedAt(now);
            assertEquals(now, party.getCreatedAt());
        }

        @Test
        @DisplayName("should set and get updated at")
        void updatedAt() {
            Earth2026Party party = new Earth2026Party();
            LocalDateTime now = LocalDateTime.now();
            party.setUpdatedAt(now);
            assertEquals(now, party.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Party party = new Earth2026Party("The Squad", 1L, 10L);
            party.setId(5L);
            party.setStatus("active");

            String str = party.toString();
            assertTrue(str.contains("id=5"));
            assertTrue(str.contains("name='The Squad'"));
            assertTrue(str.contains("locationId=1"));
            assertTrue(str.contains("leaderId=10"));
            assertTrue(str.contains("status='active'"));
        }
    }
}
