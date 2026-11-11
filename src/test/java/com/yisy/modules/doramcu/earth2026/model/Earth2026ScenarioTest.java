package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Scenario Tests")
class Earth2026ScenarioTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty scenario")
        void noArgConstructor() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            assertNull(scenario.getId());
            assertNull(scenario.getName());
            assertNull(scenario.getDescription());
            assertNull(scenario.getType());
            assertEquals(Earth2026Scenario.ScenarioStatus.AVAILABLE, scenario.getStatus());
            assertEquals(1, scenario.getDifficulty());
            assertEquals(1, scenario.getMinLevel());
            assertEquals(0, scenario.getXpReward());
            assertNull(scenario.getLocation());
            assertFalse(scenario.isSupernatural());
            assertNull(scenario.getSanityLoss());
            assertNull(scenario.getPartySize());
            assertNull(scenario.getDurationMinutes());
            assertNull(scenario.getStartedAt());
            assertNull(scenario.getCompletedAt());
            assertTrue(scenario.isActive());
            assertNull(scenario.getCreatedAt());
        }

        @Test
        @DisplayName("full constructor should set name, description, type")
        void fullConstructor() {
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "A mystery to solve", "Mystery");
            assertEquals("Mystery", scenario.getName());
            assertEquals("A mystery to solve", scenario.getDescription());
            assertEquals("Mystery", scenario.getType());
            assertEquals(Earth2026Scenario.ScenarioStatus.AVAILABLE, scenario.getStatus());
        }
    }

    @Nested
    @DisplayName("ScenarioStatus Enum")
    class ScenarioStatusTests {

        @Test
        @DisplayName("should have all 5 status values")
        void allStatusValues() {
            Earth2026Scenario.ScenarioStatus[] statuses = Earth2026Scenario.ScenarioStatus.values();
            assertEquals(5, statuses.length);
            assertTrue(java.util.Arrays.asList(statuses).contains(Earth2026Scenario.ScenarioStatus.AVAILABLE));
            assertTrue(java.util.Arrays.asList(statuses).contains(Earth2026Scenario.ScenarioStatus.IN_PROGRESS));
            assertTrue(java.util.Arrays.asList(statuses).contains(Earth2026Scenario.ScenarioStatus.COMPLETED));
            assertTrue(java.util.Arrays.asList(statuses).contains(Earth2026Scenario.ScenarioStatus.FAILED));
            assertTrue(java.util.Arrays.asList(statuses).contains(Earth2026Scenario.ScenarioStatus.LOCKED));
        }

        @Test
        @DisplayName("should set and get status")
        void setStatus() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.IN_PROGRESS);
            assertEquals(Earth2026Scenario.ScenarioStatus.IN_PROGRESS, scenario.getStatus());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get name")
        void name() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setName("The Missing Artifact");
            assertEquals("The Missing Artifact", scenario.getName());
        }

        @Test
        @DisplayName("should set and get description")
        void description() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setDescription("Find the lost artifact");
            assertEquals("Find the lost artifact", scenario.getDescription());
        }

        @Test
        @DisplayName("should set and get type")
        void type() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setType("Supernatural");
            assertEquals("Supernatural", scenario.getType());
        }

        @Test
        @DisplayName("should set and get difficulty")
        void difficulty() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setDifficulty(8);
            assertEquals(8, scenario.getDifficulty());
        }

        @Test
        @DisplayName("should set and get min level")
        void minLevel() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setMinLevel(5);
            assertEquals(5, scenario.getMinLevel());
        }

        @Test
        @DisplayName("should set and get XP reward")
        void xpReward() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setXpReward(200);
            assertEquals(200, scenario.getXpReward());
        }

        @Test
        @DisplayName("should set and get location")
        void location() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            Earth2026Location location = new Earth2026Location("San Diego", "San Diego", "USA", "City");
            scenario.setLocation(location);
            assertEquals(location, scenario.getLocation());
        }

        @Test
        @DisplayName("should set and get party size")
        void partySize() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setPartySize(4);
            assertEquals(4, scenario.getPartySize());
        }

        @Test
        @DisplayName("should set and get duration minutes")
        void durationMinutes() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setDurationMinutes(120);
            assertEquals(120, scenario.getDurationMinutes());
        }
    }

    @Nested
    @DisplayName("Supernatural")
    class SupernaturalTests {

        @Test
        @DisplayName("should set and get supernatural flag")
        void supernatural() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            assertFalse(scenario.isSupernatural());
            scenario.setSupernatural(true);
            assertTrue(scenario.isSupernatural());
        }

        @Test
        @DisplayName("should set and get sanity loss")
        void sanityLoss() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            scenario.setSanityLoss(10);
            assertEquals(10, scenario.getSanityLoss());
        }

        @Test
        @DisplayName("sanity loss should be nullable")
        void sanityLossNullable() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            assertNull(scenario.getSanityLoss());
            scenario.setSanityLoss(5);
            assertEquals(5, scenario.getSanityLoss());
            scenario.setSanityLoss(null);
            assertNull(scenario.getSanityLoss());
        }
    }

    @Nested
    @DisplayName("Active and Timestamps")
    class ActiveTimestampTests {

        @Test
        @DisplayName("should set and get active flag")
        void active() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            assertTrue(scenario.isActive());
            scenario.setActive(false);
            assertFalse(scenario.isActive());
        }

        @Test
        @DisplayName("should set and get started at")
        void startedAt() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            LocalDateTime now = LocalDateTime.now();
            scenario.setStartedAt(now);
            assertEquals(now, scenario.getStartedAt());
        }

        @Test
        @DisplayName("should set and get completed at")
        void completedAt() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            LocalDateTime now = LocalDateTime.now();
            scenario.setCompletedAt(now);
            assertEquals(now, scenario.getCompletedAt());
        }

        @Test
        @DisplayName("should set and get created/updated timestamps")
        void timestamps() {
            Earth2026Scenario scenario = new Earth2026Scenario();
            LocalDateTime now = LocalDateTime.now();
            scenario.setCreatedAt(now);
            scenario.setUpdatedAt(now);
            assertEquals(now, scenario.getCreatedAt());
            assertEquals(now, scenario.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Scenario scenario = new Earth2026Scenario("Mystery", "Desc", "Mystery");
            scenario.setId(1L);
            scenario.setStatus(Earth2026Scenario.ScenarioStatus.IN_PROGRESS);
            scenario.setDifficulty(5);

            String str = scenario.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='Mystery'"));
            assertTrue(str.contains("type='Mystery'"));
            assertTrue(str.contains("status=IN_PROGRESS"));
            assertTrue(str.contains("difficulty=5"));
        }
    }
}
