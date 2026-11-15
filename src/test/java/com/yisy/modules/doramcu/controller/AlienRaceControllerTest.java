package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.AlienRace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AlienRaceController Tests")
class AlienRaceControllerTest {

    private AlienRaceController controller;

    @BeforeEach
    void setUp() {
        controller = new AlienRaceController();
    }

    @Nested
    @DisplayName("GET /alien-races")
    class GetAlienRaces {

        @Test
        @DisplayName("Should return all alien races")
        void testAllRaces() {
            var response = controller.getAlienRaces();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            List<Map<String, Object>> body = response.getBody();
            assertNotNull(body);
            assertEquals(AlienRace.values().length, body.size());
        }

        @Test
        @DisplayName("Should include all modifier fields")
        void testRaceFields() {
            var response = controller.getAlienRaces();
            List<Map<String, Object>> body = response.getBody();
            for (Map<String, Object> race : body) {
                assertTrue(race.containsKey("name"));
                assertTrue(race.containsKey("displayName"));
                assertTrue(race.containsKey("combatDM"));
                assertTrue(race.containsKey("socialDM"));
                assertTrue(race.containsKey("navigationDM"));
                assertTrue(race.containsKey("armorBonus"));
                assertTrue(race.containsKey("tradeBonusPercent"));
            }
        }

        @Test
        @DisplayName("Should include Human as baseline")
        void testHumanBaseline() {
            var response = controller.getAlienRaces();
            List<Map<String, Object>> body = response.getBody();
            Map<String, Object> human = body.stream()
                    .filter(r -> "HUMAN".equals(r.get("name")))
                    .findFirst()
                    .orElseThrow();
            assertEquals(0, human.get("combatDM"));
            assertEquals(0, human.get("socialDM"));
            assertEquals(0, human.get("navigationDM"));
            assertEquals(0, human.get("armorBonus"));
            assertEquals(0, human.get("tradeBonusPercent"));
        }
    }

    @Nested
    @DisplayName("GET /alien-races/{name}")
    class GetAlienRace {

        @Test
        @DisplayName("Should return Aslan details")
        void testAslan() {
            var response = controller.getAlienRace("ASLAN");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertEquals("Aslan", body.get("displayName"));
            assertEquals(1, body.get("combatDM"));
            assertEquals(-1, body.get("socialDM"));
            assertNotNull(body.get("traits"));
        }

        @Test
        @DisplayName("Should return Vargr details")
        void testVargr() {
            var response = controller.getAlienRace("VARGR");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertEquals("Vargr", body.get("displayName"));
            assertEquals(1, body.get("navigationDM"));
        }

        @Test
        @DisplayName("Should return Melniki trade bonus")
        void testMelniki() {
            var response = controller.getAlienRace("MELNIKI");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertEquals(2, body.get("tradeBonusPercent"));
            assertEquals(-1, body.get("combatDM"));
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void testCaseInsensitive() {
            var response = controller.getAlienRace("aslan");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals("Aslan", response.getBody().get("displayName"));
        }

        @Test
        @DisplayName("Should return 404 for unknown race")
        void testUnknownRace() {
            var response = controller.getAlienRace("UNKNOWN");
            assertEquals(404, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should include traits list")
        void testTraits() {
            var response = controller.getAlienRace("INSECTOID");
            Map<String, Object> body = response.getBody();
            List<String> traits = (List<String>) body.get("traits");
            assertNotNull(traits);
            assertTrue(traits.size() > 0, "Insectoid should have at least one trait");
        }
    }

    @Nested
    @DisplayName("GET /alien-races/{name}/combat-dm")
    class GetCombatDM {

        @Test
        @DisplayName("Should return combat DM for Aslan")
        void testAslanCombat() {
            var response = controller.getCombatDM("ASLAN");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertEquals(1, body.get("combatDM"));
        }

        @Test
        @DisplayName("Should return 404 for unknown race")
        void testUnknownRace() {
            var response = controller.getCombatDM("UNKNOWN");
            assertEquals(404, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("GET /alien-races/{name}/trade-dm")
    class GetTradeDM {

        @Test
        @DisplayName("Should return trade bonus for Melniki")
        void testMelnikiTrade() {
            var response = controller.getTradeDM("MELNIKI");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertEquals(2, body.get("tradeBonusPercent"));
        }
    }

    @Nested
    @DisplayName("GET /alien-races/compare")
    class CompareRaces {

        @Test
        @DisplayName("Should compare Aslan vs Vargr")
        void testCompareAslanVargr() {
            var response = controller.compareRaces("ASLAN", "VARGR");
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertNotNull(body.get("race1"));
            assertNotNull(body.get("race2"));
            assertNotNull(body.get("race1Advantages"));
            assertNotNull(body.get("race2Advantages"));
        }

        @Test
        @DisplayName("Should identify Aslan combat advantage over Vargr")
        void testCombatAdvantage() {
            var response = controller.compareRaces("ASLAN", "VARGR");
            Map<String, Object> body = response.getBody();
            List<String> aslanAdv = (List<String>) body.get("race1Advantages");
            assertTrue(aslanAdv.stream().anyMatch(a -> a.contains("combat")),
                    "Aslan should have combat advantage over Vargr");
        }

        @Test
        @DisplayName("Should return 400 for invalid race")
        void testInvalidRace() {
            var response = controller.compareRaces("ASLAN", "UNKNOWN");
            assertEquals(400, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should compare Melniki vs Human for trade advantage")
        void testTradeAdvantage() {
            var response = controller.compareRaces("MELNIKI", "HUMAN");
            Map<String, Object> body = response.getBody();
            List<String> melnikiAdv = (List<String>) body.get("race1Advantages");
            assertTrue(melnikiAdv.stream().anyMatch(a -> a.contains("trade")),
                    "Melniki should have trade advantage over Human");
        }
    }
}
