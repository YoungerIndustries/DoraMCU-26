package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Country Tests")
class Earth2026CountryTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty country")
        void noArgConstructor() {
            Earth2026Country country = new Earth2026Country();
            assertNull(country.getId());
            assertNull(country.getName());
            assertNull(country.getIsoCode());
            assertNull(country.getGovernmentType());
            assertNull(country.getTechLevel());
            assertNull(country.getPopulationMillions());
            assertNull(country.getGdpTrillions());
            assertNull(country.getGdpPerCapita());
            assertNull(country.getMilitaryStrength());
            assertNull(country.getEconomicPower());
            assertNull(country.getDiplomaticInfluence());
            assertTrue(country.isActive());
        }

        @Test
        @DisplayName("full constructor should set name, ISO code, government type, and planet ID")
        void fullConstructor() {
            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            assertEquals("United States", country.getName());
            assertEquals("US", country.getIsoCode());
            assertEquals("Federal Republic", country.getGovernmentType());
            assertEquals(1L, country.getPlanetId());
            assertEquals(TechLevel.TL5, country.getTechLevel());
            assertEquals(1.0, country.getPopulationMillions());
            assertEquals(0.01, country.getGdpTrillions());
            assertEquals(10000, country.getGdpPerCapita());
            assertEquals(3, country.getMilitaryStrength());
            assertEquals(3, country.getEconomicPower());
            assertEquals(3, country.getDiplomaticInfluence());
            assertTrue(country.isActive());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all basic fields")
        void basicFields() {
            Earth2026Country country = new Earth2026Country();
            country.setId(1L);
            country.setName("Germany");
            country.setIsoCode("DE");
            country.setIsoCode3("DEU");
            country.setCapitalCity("Berlin");
            country.setGovernmentType("Parliamentary Republic");
            country.setPoliticalIdeology("Social Democracy");
            country.setContinent("Europe");
            country.setOfficialLanguages("German");
            country.setCurrency("EUR - Euro");
            country.setDescription("Central European nation");

            assertEquals(1L, country.getId());
            assertEquals("Germany", country.getName());
            assertEquals("DE", country.getIsoCode());
            assertEquals("DEU", country.getIsoCode3());
            assertEquals("Berlin", country.getCapitalCity());
            assertEquals("Parliamentary Republic", country.getGovernmentType());
            assertEquals("Social Democracy", country.getPoliticalIdeology());
            assertEquals("Europe", country.getContinent());
            assertEquals("German", country.getOfficialLanguages());
            assertEquals("EUR - Euro", country.getCurrency());
            assertEquals("Central European nation", country.getDescription());
        }

        @Test
        @DisplayName("should set and get economic fields")
        void economicFields() {
            Earth2026Country country = new Earth2026Country();
            country.setPopulationMillions(83.0);
            country.setGdpTrillions(4.2);
            country.setGdpPerCapita(50000);
            country.setTechLevel(TechLevel.TL5);

            assertEquals(83.0, country.getPopulationMillions());
            assertEquals(4.2, country.getGdpTrillions());
            assertEquals(50000, country.getGdpPerCapita());
            assertEquals(TechLevel.TL5, country.getTechLevel());
        }

        @Test
        @DisplayName("should set and get power ratings")
        void powerRatings() {
            Earth2026Country country = new Earth2026Country();
            country.setMilitaryStrength(8);
            country.setEconomicPower(7);
            country.setDiplomaticInfluence(9);

            assertEquals(8, country.getMilitaryStrength());
            assertEquals(7, country.getEconomicPower());
            assertEquals(9, country.getDiplomaticInfluence());
        }

        @Test
        @DisplayName("should set and get AI leader fields")
        void aiLeaderFields() {
            Earth2026Country country = new Earth2026Country();
            country.setAiLeaderId(42L);
            country.setLeaderName("Sif Younger");
            country.setLeaderTitle("President");

            assertEquals(42L, country.getAiLeaderId());
            assertEquals("Sif Younger", country.getLeaderName());
            assertEquals("President", country.getLeaderTitle());
        }

        @Test
        @DisplayName("should set and get planet ID")
        void planetId() {
            Earth2026Country country = new Earth2026Country();
            country.setPlanetId(5L);
            assertEquals(5L, country.getPlanetId());
        }

        @Test
        @DisplayName("should set and get active status")
        void activeStatus() {
            Earth2026Country country = new Earth2026Country();
            assertTrue(country.isActive());
            country.setActive(false);
            assertFalse(country.isActive());
        }

        @Test
        @DisplayName("should set and get timestamps")
        void timestamps() {
            Earth2026Country country = new Earth2026Country();
            LocalDateTime now = LocalDateTime.now();
            country.setCreatedAt(now);
            country.setUpdatedAt(now);
            assertEquals(now, country.getCreatedAt());
            assertEquals(now, country.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Power Score")
    class PowerScoreTests {

        @Test
        @DisplayName("should calculate weighted power score")
        void weightedPowerScore() {
            Earth2026Country country = new Earth2026Country();
            country.setMilitaryStrength(10);
            country.setEconomicPower(8);
            country.setDiplomaticInfluence(6);
            // 10*0.4 + 8*0.35 + 6*0.25 = 4.0 + 2.8 + 1.5 = 8.3
            assertEquals(8.3, country.getPowerScore(), 0.01);
        }

        @Test
        @DisplayName("should calculate equal power score")
        void equalPowerScore() {
            Earth2026Country country = new Earth2026Country();
            country.setMilitaryStrength(5);
            country.setEconomicPower(5);
            country.setDiplomaticInfluence(5);
            assertEquals(5.0, country.getPowerScore(), 0.01);
        }

        @Test
        @DisplayName("should calculate minimum power score")
        void minimumPowerScore() {
            Earth2026Country country = new Earth2026Country();
            country.setMilitaryStrength(1);
            country.setEconomicPower(1);
            country.setDiplomaticInfluence(1);
            assertEquals(1.0, country.getPowerScore(), 0.01);
        }

        @Test
        @DisplayName("should calculate maximum power score")
        void maximumPowerScore() {
            Earth2026Country country = new Earth2026Country();
            country.setMilitaryStrength(10);
            country.setEconomicPower(10);
            country.setDiplomaticInfluence(10);
            assertEquals(10.0, country.getPowerScore(), 0.01);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Country country = new Earth2026Country("United States", "US", "Federal Republic", 1L);
            country.setId(1L);
            country.setPopulationMillions(331.0);
            country.setGdpTrillions(25.0);
            country.setMilitaryStrength(10);
            country.setEconomicPower(10);
            country.setDiplomaticInfluence(10);

            String str = country.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='United States'"));
            assertTrue(str.contains("isoCode='US'"));
            assertTrue(str.contains("government='Federal Republic'"));
            assertTrue(str.contains("population=331.0M"));
            assertTrue(str.contains("gdp=25.0T"));
            assertTrue(str.contains("techLevel=TL5"));
            assertTrue(str.contains("power="));
        }
    }
}
