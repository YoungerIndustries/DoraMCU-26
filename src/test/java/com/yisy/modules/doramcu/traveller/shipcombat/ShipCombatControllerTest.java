package com.yisy.modules.doramcu.traveller.shipcombat;

import com.yisy.modules.doramcu.traveller.ShipTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShipCombatController Tests")
class ShipCombatControllerTest {

    private ShipCombatController controller;
    private ShipCombatService service;
    private ShipCombatCampaignService campaignService;

    @BeforeEach
    void setUp() {
        controller = new ShipCombatController();
        // Access via reflection or just test the controller directly
        // Since the controller creates its own services, we test via the controller
    }

    // ---- Templates ----

    @Nested
    @DisplayName("Template Endpoints")
    class TemplateEndpointTests {

        @Test
        @DisplayName("getAllTemplates should return 200 OK")
        void getAllTemplates_shouldReturnOk() {
            ResponseEntity<?> response = controller.getAllTemplates();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertTrue(((List<?>) body.get("templates")).size() >= 8);
        }

        @Test
        @DisplayName("getTemplatesByClass should return templates")
        void getTemplatesByClass_shouldReturnTemplates() {
            ResponseEntity<?> response = controller.getTemplatesByClass("DESTROYER");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("DESTROYER", body.get("shipClass"));
        }

        @Test
        @DisplayName("getTemplatesByClass should return 400 for unknown class")
        void getTemplatesByClass_shouldReturn400() {
            ResponseEntity<?> response = controller.getTemplatesByClass("NONEXISTENT");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    // ---- Battle Creation ----

    @Nested
    @DisplayName("Battle Creation")
    class BattleCreationTests {

        @Test
        @DisplayName("createBattle should return 201 Created")
        void createBattle_shouldReturnCreated() {
            Map<String, Object> request = Map.of(
                    "id", "test-battle-1",
                    "fleet0", Map.of("name", "Fleet A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "Fleet B", "ships", List.of("Scout/Courier")),
                    "rulesType", "HIGH_GUARD",
                    "sector", "Core"
            );

            ResponseEntity<?> response = controller.createBattle(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("test-battle-1", body.get("id"));
            assertEquals("IN_PROGRESS", body.get("state"));
        }

        @Test
        @DisplayName("createBattle should auto-generate ID")
        void createBattle_shouldAutoGenerateId() {
            Map<String, Object> request = Map.of(
                    "fleet0", Map.of("name", "Fleet A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "Fleet B", "ships", List.of("Scout/Courier"))
            );

            ResponseEntity<?> response = controller.createBattle(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body.get("id"));
        }

        @Test
        @DisplayName("createBattle should return 400 for missing fleets")
        void createBattle_shouldReturn400() {
            Map<String, Object> request = Map.of("id", "test");

            ResponseEntity<?> response = controller.createBattle(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("createBattle should return 400 for unknown template")
        void createBattle_shouldReturn400UnknownTemplate() {
            Map<String, Object> request = Map.of(
                    "fleet0", Map.of("name", "Fleet A", "ships", List.of("Nonexistent")),
                    "fleet1", Map.of("name", "Fleet B", "ships", List.of("Destroyer"))
            );

            ResponseEntity<?> response = controller.createBattle(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    // ---- Battle Status ----

    @Nested
    @DisplayName("Battle Status")
    class BattleStatusTests {

        @Test
        @DisplayName("getBattle should return 200 for existing battle")
        void getBattle_shouldReturn200() {
            // Create battle first
            Map<String, Object> request = Map.of(
                    "id", "status-test",
                    "fleet0", Map.of("name", "A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "B", "ships", List.of("Scout/Courier"))
            );
            controller.createBattle(request);

            ResponseEntity<?> response = controller.getBattle("status-test");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("status-test", body.get("id"));
        }

        @Test
        @DisplayName("getBattle should return 404 for unknown battle")
        void getBattle_shouldReturn404() {
            ResponseEntity<?> response = controller.getBattle("nonexistent");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("listBattles should return battle IDs")
        void listBattles_shouldReturnIds() {
            Map<String, Object> request = Map.of(
                    "id", "list-test",
                    "fleet0", Map.of("name", "A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "B", "ships", List.of("Scout/Courier"))
            );
            controller.createBattle(request);

            ResponseEntity<?> response = controller.listBattles();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(((List<?>) body.get("battles")).contains("list-test"));
        }
    }

    // ---- Battle Resolution ----

    @Nested
    @DisplayName("Battle Resolution")
    class BattleResolutionTests {

        @Test
        @DisplayName("resolveRound should return 200")
        void resolveRound_shouldReturn200() {
            Map<String, Object> request = Map.of(
                    "id", "resolve-test",
                    "fleet0", Map.of("name", "A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "B", "ships", List.of("Scout/Courier"))
            );
            controller.createBattle(request);

            ResponseEntity<?> response = controller.resolveRound("resolve-test");

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("resolveRound should return 404 for unknown battle")
        void resolveRound_shouldReturn404() {
            ResponseEntity<?> response = controller.resolveRound("nonexistent");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("resolveToCompletion should return outcome")
        void resolveToCompletion_shouldReturnOutcome() {
            Map<String, Object> request = Map.of(
                    "id", "complete-test",
                    "fleet0", Map.of("name", "A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "B", "ships", List.of("Scout/Courier"))
            );
            controller.createBattle(request);

            ResponseEntity<?> response = controller.resolveToCompletion("complete-test");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body.get("outcome"));
        }

        @Test
        @DisplayName("getBattleEvents should return events")
        void getBattleEvents_shouldReturnEvents() {
            Map<String, Object> request = Map.of(
                    "id", "events-test",
                    "fleet0", Map.of("name", "A", "ships", List.of("Destroyer")),
                    "fleet1", Map.of("name", "B", "ships", List.of("Scout/Courier"))
            );
            controller.createBattle(request);

            ResponseEntity<?> response = controller.getBattleEvents("events-test");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("events-test", body.get("battleId"));
        }
    }

    // ---- Campaign Endpoints ----

    @Nested
    @DisplayName("Campaign Endpoints")
    class CampaignEndpointTests {

        @Test
        @DisplayName("createCampaign should return 201")
        void createCampaign_shouldReturn201() {
            Map<String, Object> request = Map.of(
                    "id", "camp-api-1",
                    "name", "Test Campaign",
                    "sector", "Core",
                    "faction0", "Imperial",
                    "faction1", "Rebels"
            );

            ResponseEntity<?> response = controller.createCampaign(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("camp-api-1", body.get("id"));
        }

        @Test
        @DisplayName("getCampaign should return 200")
        void getCampaign_shouldReturn200() {
            Map<String, Object> request = Map.of(
                    "id", "camp-api-2",
                    "name", "Test",
                    "sector", "S",
                    "faction0", "F0",
                    "faction1", "F1"
            );
            controller.createCampaign(request);

            ResponseEntity<?> response = controller.getCampaign("camp-api-2");

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("getCampaign should return 404")
        void getCampaign_shouldReturn404() {
            ResponseEntity<?> response = controller.getCampaign("nonexistent");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("addBattleToCampaign should return 200")
        void addBattleToCampaign_shouldReturn200() {
            Map<String, Object> createReq = Map.of(
                    "id", "camp-api-3",
                    "name", "Test",
                    "sector", "S",
                    "faction0", "F0",
                    "faction1", "F1"
            );
            controller.createCampaign(createReq);

            Map<String, Object> battleReq = Map.of(
                    "battleId", "b1",
                    "outcome", "SIDE_0_VICTORY",
                    "rounds", 5
            );

            ResponseEntity<?> response = controller.addBattleToCampaign("camp-api-3", battleReq);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("getCampaignSummary should return summary")
        void getCampaignSummary_shouldReturnSummary() {
            Map<String, Object> createReq = Map.of(
                    "id", "camp-api-4",
                    "name", "Test",
                    "sector", "S",
                    "faction0", "F0",
                    "faction1", "F1"
            );
            controller.createCampaign(createReq);

            ResponseEntity<?> response = controller.getCampaignSummary("camp-api-4");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("camp-api-4", body.get("id"));
        }
    }
}
