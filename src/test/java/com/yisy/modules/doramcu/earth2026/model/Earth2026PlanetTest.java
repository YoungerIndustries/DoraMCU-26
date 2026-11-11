package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Planet Tests")
class Earth2026PlanetTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty planet")
        void noArgConstructor() {
            Earth2026Planet planet = new Earth2026Planet();
            assertNull(planet.getId());
            assertNull(planet.getName());
            assertNull(planet.getPlanetType());
            assertTrue(planet.isActive());
            assertFalse(planet.isInitialized());
        }

        @Test
        @DisplayName("full constructor should set name, time period, and default values")
        void fullConstructor() {
            Earth2026Planet planet = new Earth2026Planet("Earth", "MODERN");
            assertEquals("Earth", planet.getName());
            assertEquals("Terrestrial", planet.getPlanetType());
            assertEquals(TechLevel.TL5, planet.getBaseTechLevel());
            assertEquals("MODERN", planet.getTimePeriod());
            assertEquals(8.0, planet.getTotalPopulationBillions());
            assertEquals(0, planet.getCountryCount());
            assertEquals(0, planet.getRegionCount());
            assertEquals(0, planet.getLocationCount());
            assertEquals(1.0, planet.getGravity());
            assertEquals(15.0, planet.getAvgTemperatureC());
            assertEquals("Nitrogen-Oxygen", planet.getAtmosphere());
            assertEquals(6371.0, planet.getRadiusKm());
            assertTrue(planet.isActive());
            assertFalse(planet.isInitialized());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all basic fields")
        void basicFields() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setId(1L);
            planet.setName("Terra");
            planet.setPlanetType("Ocean World");
            planet.setBaseTechLevel(TechLevel.TL6);
            planet.setTimePeriod("NEAR_FUTURE");
            planet.setDescription("A water-rich world");

            assertEquals(1L, planet.getId());
            assertEquals("Terra", planet.getName());
            assertEquals("Ocean World", planet.getPlanetType());
            assertEquals(TechLevel.TL6, planet.getBaseTechLevel());
            assertEquals("NEAR_FUTURE", planet.getTimePeriod());
            assertEquals("A water-rich world", planet.getDescription());
        }

        @Test
        @DisplayName("should set and get population and counts")
        void populationAndCounts() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setTotalPopulationBillions(10.0);
            planet.setCountryCount(20);
            planet.setRegionCount(50);
            planet.setLocationCount(100);

            assertEquals(10.0, planet.getTotalPopulationBillions());
            assertEquals(20, planet.getCountryCount());
            assertEquals(50, planet.getRegionCount());
            assertEquals(100, planet.getLocationCount());
        }

        @Test
        @DisplayName("should set and get AI overseer fields")
        void aiOverseerFields() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setAiOverseerId(42L);
            planet.setOverseerName("Galactic Overseer");

            assertEquals(42L, planet.getAiOverseerId());
            assertEquals("Galactic Overseer", planet.getOverseerName());
        }

        @Test
        @DisplayName("should set and get environmental fields")
        void environmentalFields() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setGravity(0.8);
            planet.setAvgTemperatureC(25.0);
            planet.setAtmosphere("Carbon Dioxide");
            planet.setRadiusKm(5000.0);

            assertEquals(0.8, planet.getGravity());
            assertEquals(25.0, planet.getAvgTemperatureC());
            assertEquals("Carbon Dioxide", planet.getAtmosphere());
            assertEquals(5000.0, planet.getRadiusKm());
        }

        @Test
        @DisplayName("should set and get active status")
        void activeStatus() {
            Earth2026Planet planet = new Earth2026Planet();
            assertTrue(planet.isActive());
            planet.setActive(false);
            assertFalse(planet.isActive());
        }

        @Test
        @DisplayName("should set and get initialized status")
        void initializedStatus() {
            Earth2026Planet planet = new Earth2026Planet();
            assertFalse(planet.isInitialized());
            planet.setInitialized(true);
            assertTrue(planet.isInitialized());
        }

        @Test
        @DisplayName("should set and get timestamps")
        void timestamps() {
            Earth2026Planet planet = new Earth2026Planet();
            LocalDateTime now = LocalDateTime.now();
            planet.setCreatedAt(now);
            planet.setUpdatedAt(now);
            assertEquals(now, planet.getCreatedAt());
            assertEquals(now, planet.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Ready for Game")
    class ReadyForGameTests {

        @Test
        @DisplayName("should not be ready when not initialized")
        void notReadyWhenNotInitialized() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setCountryCount(10);
            planet.setActive(true);
            assertFalse(planet.isReadyForGame());
        }

        @Test
        @DisplayName("should not be ready when not active")
        void notReadyWhenNotActive() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setInitialized(true);
            planet.setCountryCount(10);
            planet.setActive(false);
            assertFalse(planet.isReadyForGame());
        }

        @Test
        @DisplayName("should not be ready when no countries")
        void notReadyWhenNoCountries() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setInitialized(true);
            planet.setActive(true);
            planet.setCountryCount(0);
            assertFalse(planet.isReadyForGame());
        }

        @Test
        @DisplayName("should be ready when initialized, active, and has countries")
        void isReady() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setInitialized(true);
            planet.setActive(true);
            planet.setCountryCount(20);
            assertTrue(planet.isReadyForGame());
        }

        @Test
        @DisplayName("should be ready with one country")
        void readyWithOneCountry() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setInitialized(true);
            planet.setActive(true);
            planet.setCountryCount(1);
            assertTrue(planet.isReadyForGame());
        }
    }

    @Nested
    @DisplayName("Planet Types")
    class PlanetTypeTests {

        @Test
        @DisplayName("should support Terrestrial type")
        void terrestrial() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setPlanetType("Terrestrial");
            assertEquals("Terrestrial", planet.getPlanetType());
        }

        @Test
        @DisplayName("should support Gas Giant type")
        void gasGiant() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setPlanetType("Gas Giant");
            assertEquals("Gas Giant", planet.getPlanetType());
        }

        @Test
        @DisplayName("should support Ocean World type")
        void oceanWorld() {
            Earth2026Planet planet = new Earth2026Planet();
            planet.setPlanetType("Ocean World");
            assertEquals("Ocean World", planet.getPlanetType());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Planet planet = new Earth2026Planet("Earth", "MODERN");
            planet.setId(1L);
            planet.setCountryCount(20);
            planet.setRegionCount(50);
            planet.setLocationCount(100);
            planet.setInitialized(true);
            planet.setActive(true);

            String str = planet.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='Earth'"));
            assertTrue(str.contains("type='Terrestrial'"));
            assertTrue(str.contains("techLevel=TL5"));
            assertTrue(str.contains("period='MODERN'"));
            assertTrue(str.contains("countries=20"));
            assertTrue(str.contains("regions=50"));
            assertTrue(str.contains("locations=100"));
            assertTrue(str.contains("ready=true"));
        }
    }
}
