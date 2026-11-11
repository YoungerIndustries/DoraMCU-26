package com.yisy.modules.doramcu.earth2026.dto;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Planet;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Region;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Planet Population DTOs Tests")
class PlanetPopulationDtoTest {

    @Nested
    @DisplayName("PlanetCharacterInfo")
    class PlanetCharacterInfoTests {

        @Test
        @DisplayName("no-arg constructor should create empty character info")
        void noArgConstructor() {
            PlanetCharacterInfo info = new PlanetCharacterInfo();
            assertNull(info.getGameId());
            assertNull(info.getName());
            assertNull(info.getRole());
            assertNull(info.getSpeciesId());
            assertFalse(info.isPlayerControlled());
            assertNull(info.getEmpireName());
            assertNull(info.getCountryOrRegion());
            assertNull(info.getGameAttributes());
            assertNull(info.getDescription());
            assertFalse(info.isActive());
        }

        @Test
        @DisplayName("constructor should set required fields")
        void constructor() {
            PlanetCharacterInfo info = new PlanetCharacterInfo("game-1", "John Doe", "Leader", "Human");
            assertEquals("game-1", info.getGameId());
            assertEquals("John Doe", info.getName());
            assertEquals("Leader", info.getRole());
            assertEquals("Human", info.getSpeciesId());
            assertFalse(info.isPlayerControlled());
            assertTrue(info.isActive());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            PlanetCharacterInfo info = new PlanetCharacterInfo();
            info.setGameId("game-42");
            info.setName("Klingon Warlord");
            info.setRole("General");
            info.setSpeciesId("Klingon");
            info.setPlayerControlled(true);
            info.setEmpireName("Klingon Empire");
            info.setCountryOrRegion("Qo'nos");
            info.setDescription("Fierce warrior");
            info.setActive(true);

            assertEquals("game-42", info.getGameId());
            assertEquals("Klingon Warlord", info.getName());
            assertEquals("General", info.getRole());
            assertEquals("Klingon", info.getSpeciesId());
            assertTrue(info.isPlayerControlled());
            assertEquals("Klingon Empire", info.getEmpireName());
            assertEquals("Qo'nos", info.getCountryOrRegion());
            assertEquals("Fierce warrior", info.getDescription());
            assertTrue(info.isActive());
        }

        @Test
        @DisplayName("toString should include key fields")
        void toStringOutput() {
            PlanetCharacterInfo info = new PlanetCharacterInfo("g1", "Test", "Leader", "Human");
            String s = info.toString();
            assertTrue(s.contains("g1"));
            assertTrue(s.contains("Test"));
            assertTrue(s.contains("Leader"));
            assertTrue(s.contains("Human"));
        }
    }

    @Nested
    @DisplayName("PlanetPopulationRequest")
    class PlanetPopulationRequestTests {

        @Test
        @DisplayName("no-arg constructor should create empty request")
        void noArgConstructor() {
            PlanetPopulationRequest req = new PlanetPopulationRequest();
            assertNull(req.getGameModuleName());
            assertNull(req.getPlanetName());
            assertNull(req.getPlanetType());
            assertNull(req.getTechLevel());
            assertNull(req.getTimePeriod());
            assertNull(req.getExistingCharacters());
            assertNull(req.getSpeciesIdForNewAi());
            assertNull(req.getGamePlanetMetadata());
            assertFalse(req.isForceRegenerate());
        }

        @Test
        @DisplayName("constructor should set required fields with defaults")
        void constructor() {
            PlanetPopulationRequest req = new PlanetPopulationRequest("TRAVELLER", "Terra");
            assertEquals("TRAVELLER", req.getGameModuleName());
            assertEquals("Terra", req.getPlanetName());
            assertEquals("Terrestrial", req.getPlanetType());
            assertEquals(5, req.getTechLevel());
            assertFalse(req.isForceRegenerate());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            PlanetPopulationRequest req = new PlanetPopulationRequest();
            req.setGameModuleName("CAPTAINS_LOG");
            req.setPlanetName("Earth");
            req.setPlanetType("Terrestrial");
            req.setTechLevel(7);
            req.setTimePeriod("Space Age");
            req.setExistingCharacters(List.of());
            req.setSpeciesIdForNewAi("Human");
            req.setForceRegenerate(true);

            assertEquals("CAPTAINS_LOG", req.getGameModuleName());
            assertEquals("Earth", req.getPlanetName());
            assertEquals("Terrestrial", req.getPlanetType());
            assertEquals(7, req.getTechLevel());
            assertEquals("Space Age", req.getTimePeriod());
            assertTrue(req.isForceRegenerate());
        }

        @Test
        @DisplayName("toString should include character count")
        void toStringOutput() {
            PlanetPopulationRequest req = new PlanetPopulationRequest("TRAVELLER", "Terra");
            req.setExistingCharacters(List.of(
                    new PlanetCharacterInfo("1", "A", "Leader", "Human"),
                    new PlanetCharacterInfo("2", "B", "Player", "Human")
            ));
            String s = req.toString();
            assertTrue(s.contains("TRAVELLER"));
            assertTrue(s.contains("Terra"));
            assertTrue(s.contains("characters=2"));
        }
    }

    @Nested
    @DisplayName("PlanetPopulationResponse")
    class PlanetPopulationResponseTests {

        @Test
        @DisplayName("no-arg constructor should create empty response")
        void noArgConstructor() {
            PlanetPopulationResponse resp = new PlanetPopulationResponse();
            assertNull(resp.getPlanet());
            assertNull(resp.getCountries());
            assertNull(resp.getRegions());
            assertNull(resp.getReusedAiCharacterIds());
            assertNull(resp.getNewAiCharacters());
            assertNull(resp.getRegisteredPlayerCharacters());
            assertNull(resp.getSummary());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            PlanetPopulationResponse resp = new PlanetPopulationResponse();
            Earth2026Planet planet = new Earth2026Planet("Test Planet", "Modern");
            resp.setPlanet(planet);
            resp.setCountries(List.of());
            resp.setRegions(List.of());
            resp.setReusedAiCharacterIds(Map.of("g1", 1L));
            resp.setNewAiCharacters(List.of());
            resp.setRegisteredPlayerCharacters(Map.of("p1", "Player1"));
            resp.setSummary(new PlanetPopulationResponse.PopulationSummary());

            assertEquals(planet, resp.getPlanet());
            assertEquals(1, resp.getReusedAiCharacterIds().size());
            assertEquals(1L, resp.getReusedAiCharacterIds().get("g1"));
            assertEquals("Player1", resp.getRegisteredPlayerCharacters().get("p1"));
        }

        @Test
        @DisplayName("toString should include counts")
        void toStringOutput() {
            PlanetPopulationResponse resp = new PlanetPopulationResponse();
            Earth2026Planet planet = new Earth2026Planet("Test", "Modern");
            resp.setPlanet(planet);
            resp.setReusedAiCharacterIds(Map.of("g1", 1L));
            String s = resp.toString();
            assertTrue(s.contains("Test"));
            assertTrue(s.contains("reusedAi=1"));
        }

        @Test
        @DisplayName("toString should handle null planet")
        void toStringNullPlanet() {
            PlanetPopulationResponse resp = new PlanetPopulationResponse();
            String s = resp.toString();
            assertTrue(s.contains("null"));
        }
    }

    @Nested
    @DisplayName("PopulationSummary")
    class PopulationSummaryTests {

        @Test
        @DisplayName("no-arg constructor should create empty summary")
        void noArgConstructor() {
            PlanetPopulationResponse.PopulationSummary summary =
                    new PlanetPopulationResponse.PopulationSummary();
            assertEquals(0, summary.getTotalGameCharacters());
            assertEquals(0, summary.getPlayerCharactersRegistered());
            assertEquals(0, summary.getAiCharactersReused());
            assertEquals(0, summary.getNewAiCharactersGenerated());
            assertEquals(0, summary.getCountriesWithLeaders());
            assertEquals(0, summary.getRegionsWithGovernors());
            assertFalse(summary.isPlanetFound());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            PlanetPopulationResponse.PopulationSummary summary =
                    new PlanetPopulationResponse.PopulationSummary();
            summary.setTotalGameCharacters(10);
            summary.setPlayerCharactersRegistered(3);
            summary.setAiCharactersReused(5);
            summary.setNewAiCharactersGenerated(2);
            summary.setCountriesWithLeaders(5);
            summary.setRegionsWithGovernors(2);
            summary.setPlanetFound(true);

            assertEquals(10, summary.getTotalGameCharacters());
            assertEquals(3, summary.getPlayerCharactersRegistered());
            assertEquals(5, summary.getAiCharactersReused());
            assertEquals(2, summary.getNewAiCharactersGenerated());
            assertEquals(5, summary.getCountriesWithLeaders());
            assertEquals(2, summary.getRegionsWithGovernors());
            assertTrue(summary.isPlanetFound());
        }

        @Test
        @DisplayName("toString should include all counts")
        void toStringOutput() {
            PlanetPopulationResponse.PopulationSummary summary =
                    new PlanetPopulationResponse.PopulationSummary();
            summary.setTotalGameCharacters(10);
            summary.setPlayerCharactersRegistered(3);
            summary.setAiCharactersReused(5);
            summary.setNewAiCharactersGenerated(2);
            String s = summary.toString();
            assertTrue(s.contains("totalGameChars=10"));
            assertTrue(s.contains("players=3"));
            assertTrue(s.contains("aiReused=5"));
            assertTrue(s.contains("aiNew=2"));
        }
    }
}
