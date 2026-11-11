package com.yisy.modules.doradl.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import com.yisy.modules.doradl.dto.PlayerSummary;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerSummary Tests")
class PlayerSummaryTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            PlayerSummary summary = new PlayerSummary();
            assertNull(summary.getId());
            assertNull(summary.getName());
            assertNull(summary.getResources());
            assertEquals(0, summary.getPlanetCount());
            assertEquals(0, summary.getFleetCount());
            assertFalse(summary.isAi());
            assertNull(summary.getDifficulty());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets id, name, resources, planetCount, fleetCount")
        void setsFields() {
            Map<String, Integer> resources = Map.of("minerals", 100, "energy", 50);
            PlayerSummary summary = new PlayerSummary(1L, "Federation", resources, 5, 3);
            assertEquals(1L, summary.getId());
            assertEquals("Federation", summary.getName());
            assertSame(resources, summary.getResources());
            assertEquals(5, summary.getPlanetCount());
            assertEquals(3, summary.getFleetCount());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get id")
        void id() {
            PlayerSummary summary = new PlayerSummary();
            summary.setId(42L);
            assertEquals(42L, summary.getId());
        }

        @Test
        @DisplayName("Set and get name")
        void name() {
            PlayerSummary summary = new PlayerSummary();
            summary.setName("Empire");
            assertEquals("Empire", summary.getName());
        }

        @Test
        @DisplayName("Set and get resources")
        void resources() {
            PlayerSummary summary = new PlayerSummary();
            Map<String, Integer> resources = Map.of("food", 200);
            summary.setResources(resources);
            assertSame(resources, summary.getResources());
        }

        @Test
        @DisplayName("Set and get planetCount")
        void planetCount() {
            PlayerSummary summary = new PlayerSummary();
            summary.setPlanetCount(10);
            assertEquals(10, summary.getPlanetCount());
        }

        @Test
        @DisplayName("Set and get fleetCount")
        void fleetCount() {
            PlayerSummary summary = new PlayerSummary();
            summary.setFleetCount(7);
            assertEquals(7, summary.getFleetCount());
        }

        @Test
        @DisplayName("Set and get isAi")
        void isAi() {
            PlayerSummary summary = new PlayerSummary();
            summary.setAi(true);
            assertTrue(summary.isAi());
        }

        @Test
        @DisplayName("Set and get difficulty")
        void difficulty() {
            PlayerSummary summary = new PlayerSummary();
            summary.setDifficulty("hard");
            assertEquals("hard", summary.getDifficulty());
        }
    }
}
