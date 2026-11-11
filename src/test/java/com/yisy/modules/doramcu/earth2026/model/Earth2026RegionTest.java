package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Region Tests")
class Earth2026RegionTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty region")
        void noArgConstructor() {
            Earth2026Region region = new Earth2026Region();
            assertNull(region.getId());
            assertNull(region.getName());
            assertNull(region.getType());
            assertNull(region.getCountryId());
            assertTrue(region.isActive());
        }

        @Test
        @DisplayName("full constructor should set name, type, and country ID")
        void fullConstructor() {
            Earth2026Region region = new Earth2026Region("California", "State", 1L);
            assertEquals("California", region.getName());
            assertEquals("State", region.getType());
            assertEquals(1L, region.getCountryId());
            assertTrue(region.isActive());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all basic fields")
        void basicFields() {
            Earth2026Region region = new Earth2026Region();
            region.setId(1L);
            region.setName("Bavaria");
            region.setType("State");
            region.setCountryId(2L);
            region.setCapitalCity("Munich");
            region.setDescription("Southern German state");

            assertEquals(1L, region.getId());
            assertEquals("Bavaria", region.getName());
            assertEquals("State", region.getType());
            assertEquals(2L, region.getCountryId());
            assertEquals("Munich", region.getCapitalCity());
            assertEquals("Southern German state", region.getDescription());
        }

        @Test
        @DisplayName("should set and get economic fields")
        void economicFields() {
            Earth2026Region region = new Earth2026Region();
            region.setPopulationMillions(12.0);
            region.setGdpBillions(500.0);
            region.setTechLevel(TechLevel.TL5);

            assertEquals(12.0, region.getPopulationMillions());
            assertEquals(500.0, region.getGdpBillions());
            assertEquals(TechLevel.TL5, region.getTechLevel());
        }

        @Test
        @DisplayName("should set and get AI governor fields")
        void aiGovernorFields() {
            Earth2026Region region = new Earth2026Region();
            region.setAiGovernorId(42L);
            region.setGovernorName("Gavin Newsom");
            region.setGovernorTitle("Governor");

            assertEquals(42L, region.getAiGovernorId());
            assertEquals("Gavin Newsom", region.getGovernorName());
            assertEquals("Governor", region.getGovernorTitle());
        }

        @Test
        @DisplayName("should set and get active status")
        void activeStatus() {
            Earth2026Region region = new Earth2026Region();
            assertTrue(region.isActive());
            region.setActive(false);
            assertFalse(region.isActive());
        }

        @Test
        @DisplayName("should set and get timestamps")
        void timestamps() {
            Earth2026Region region = new Earth2026Region();
            LocalDateTime now = LocalDateTime.now();
            region.setCreatedAt(now);
            region.setUpdatedAt(now);
            assertEquals(now, region.getCreatedAt());
            assertEquals(now, region.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Region Types")
    class RegionTypeTests {

        @Test
        @DisplayName("should support State type")
        void stateType() {
            Earth2026Region region = new Earth2026Region("Texas", "State", 1L);
            assertEquals("State", region.getType());
        }

        @Test
        @DisplayName("should support Province type")
        void provinceType() {
            Earth2026Region region = new Earth2026Region("Ontario", "Province", 2L);
            assertEquals("Province", region.getType());
        }

        @Test
        @DisplayName("should support Prefecture type")
        void prefectureType() {
            Earth2026Region region = new Earth2026Region("Tokyo", "Prefecture", 3L);
            assertEquals("Prefecture", region.getType());
        }

        @Test
        @DisplayName("should support Territory type")
        void territoryType() {
            Earth2026Region region = new Earth2026Region("Guam", "Territory", 1L);
            assertEquals("Territory", region.getType());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Region region = new Earth2026Region("California", "State", 1L);
            region.setId(1L);
            region.setPopulationMillions(39.0);

            String str = region.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='California'"));
            assertTrue(str.contains("type='State'"));
            assertTrue(str.contains("countryId=1"));
            assertTrue(str.contains("population=39.0M"));
        }
    }
}
