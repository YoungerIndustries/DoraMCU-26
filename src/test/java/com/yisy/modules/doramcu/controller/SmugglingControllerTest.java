package com.yisy.modules.doramcu.controller;
import com.yisy.modules.doramcu.controller.game.SmugglingController;

import com.yisy.modules.doramcu.traveller.ContrabandCommodity;
import com.yisy.modules.doramcu.traveller.CustomsCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SmugglingController Tests")
class SmugglingControllerTest {

    private SmugglingController controller;

    @BeforeEach
    void setUp() {
        controller = new SmugglingController();
    }

    @Nested
    @DisplayName("GET /contraband")
    class GetContraband {

        @Test
        @DisplayName("Should return all contraband types")
        void testAllContraband() {
            var response = controller.getContraband();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            List<Map<String, Object>> body = response.getBody();
            assertNotNull(body);
            assertEquals(ContrabandCommodity.values().length, body.size());
        }

        @Test
        @DisplayName("Should include risk level and price for each type")
        void testContrabandFields() {
            var response = controller.getContraband();
            List<Map<String, Object>> body = response.getBody();
            for (Map<String, Object> item : body) {
                assertTrue(item.containsKey("name"));
                assertTrue(item.containsKey("displayName"));
                assertTrue(item.containsKey("riskLevel"));
                assertTrue(item.containsKey("basePrice"));
                assertTrue(item.containsKey("profitMultiplier"));
            }
        }
    }

    @Nested
    @DisplayName("GET /customs-outcomes")
    class GetCustomsOutcomes {

        @Test
        @DisplayName("Should return all customs outcomes")
        void testAllOutcomes() {
            var response = controller.getCustomsOutcomes();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            List<Map<String, Object>> body = response.getBody();
            assertNotNull(body);
            assertEquals(CustomsCheck.Outcome.values().length, body.size());
        }
    }

    @Nested
    @DisplayName("GET /actions")
    class GetActions {

        @Test
        @DisplayName("Should return all smuggler actions")
        void testAllActions() {
            var response = controller.getActions();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            List<Map<String, Object>> body = response.getBody();
            assertNotNull(body);
            assertEquals(CustomsCheck.Action.values().length, body.size());
        }
    }

    @Nested
    @DisplayName("POST /operations")
    class CreateOperation {

        @Test
        @DisplayName("Should create a smuggling operation")
        void testCreateOperation() {
            SmugglingController.SmugglingRequest request = new SmugglingController.SmugglingRequest();
            request.setContrabandType("DRUGS");
            request.setQuantity(10);
            request.setSourceWorld("Tresckoe");
            request.setDestinationWorld("Ruthe");
            request.setDestinationLawLevel(6);
            request.setDestinationStarport("C");

            var response = controller.createOperation(request);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertNotNull(body);
            assertEquals("Drugs", body.get("contraband"));
            assertEquals(10, body.get("quantity"));
            assertEquals("Tresckoe", body.get("sourceWorld"));
            assertEquals("Ruthe", body.get("destinationWorld"));
            assertEquals('C', body.get("destinationStarport"));
        }

        @Test
        @DisplayName("Should return 400 for invalid contraband type")
        void testInvalidContraband() {
            SmugglingController.SmugglingRequest request = new SmugglingController.SmugglingRequest();
            request.setContrabandType("INVALID");
            request.setQuantity(1);
            request.setSourceWorld("A");
            request.setDestinationWorld("B");
            request.setDestinationLawLevel(5);

            var response = controller.createOperation(request);
            assertEquals(400, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should calculate purchase cost and sell revenue")
        void testCostCalculation() {
            SmugglingController.SmugglingRequest request = new SmugglingController.SmugglingRequest();
            request.setContrabandType("WEAPONS");
            request.setQuantity(5);
            request.setSourceWorld("A");
            request.setDestinationWorld("B");
            request.setDestinationLawLevel(5);

            var response = controller.createOperation(request);
            Map<String, Object> body = response.getBody();
            assertTrue((long) body.get("purchaseCost") > 0, "Purchase cost should be positive");
            assertTrue((long) body.get("sellRevenue") > 0, "Sell revenue should be positive");
        }
    }

    @Nested
    @DisplayName("GET /operations")
    class ListOperations {

        @Test
        @DisplayName("Should return empty list initially")
        void testEmptyList() {
            var response = controller.listOperations();
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return created operations")
        void testListCreatedOperations() {
            SmugglingController.SmugglingRequest request = new SmugglingController.SmugglingRequest();
            request.setContrabandType("DRUGS");
            request.setQuantity(10);
            request.setSourceWorld("A");
            request.setDestinationWorld("B");
            request.setDestinationLawLevel(5);
            controller.createOperation(request);

            var response = controller.listOperations();
            assertEquals(1, response.getBody().size());
        }
    }

    @Nested
    @DisplayName("GET /operations/{id}")
    class GetOperation {

        @Test
        @DisplayName("Should return 404 for non-existent operation")
        void testNotFound() {
            var response = controller.getOperation(999);
            assertEquals(404, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should return the operation by ID")
        void testGetById() {
            SmugglingController.SmugglingRequest request = new SmugglingController.SmugglingRequest();
            request.setContrabandType("DRUGS");
            request.setQuantity(10);
            request.setSourceWorld("A");
            request.setDestinationWorld("B");
            request.setDestinationLawLevel(5);
            var createResponse = controller.createOperation(request);
            int id = (int) createResponse.getBody().get("id");

            var response = controller.getOperation(id);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertEquals(id, response.getBody().get("id"));
        }
    }

    @Nested
    @DisplayName("POST /operations/{id}/customs-check")
    class CustomsCheckTest {

        @Test
        @DisplayName("Should return 404 for non-existent operation")
        void testNotFound() {
            SmugglingController.ResolveRequest request = new SmugglingController.ResolveRequest();
            request.setAction("BRIBE");
            var response = controller.customsCheck(999, request);
            assertEquals(404, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should return 400 for invalid action")
        void testInvalidAction() {
            SmugglingController.SmugglingRequest req = new SmugglingController.SmugglingRequest();
            req.setContrabandType("DRUGS");
            req.setQuantity(10);
            req.setSourceWorld("A");
            req.setDestinationWorld("B");
            req.setDestinationLawLevel(5);
            var createResponse = controller.createOperation(req);
            int id = (int) createResponse.getBody().get("id");

            SmugglingController.ResolveRequest request = new SmugglingController.ResolveRequest();
            request.setAction("INVALID");
            var response = controller.customsCheck(id, request);
            assertEquals(400, response.getStatusCode().value());
        }

        @Test
        @DisplayName("Should execute customs check with bribe action")
        void testBribeAction() {
            SmugglingController.SmugglingRequest req = new SmugglingController.SmugglingRequest();
            req.setContrabandType("DRUGS");
            req.setQuantity(10);
            req.setSourceWorld("A");
            req.setDestinationWorld("B");
            req.setDestinationLawLevel(5);
            req.setDestinationStarport("E");
            var createResponse = controller.createOperation(req);
            int id = (int) createResponse.getBody().get("id");

            SmugglingController.ResolveRequest request = new SmugglingController.ResolveRequest();
            request.setAction("BRIBE");
            request.setPilotSkill(3);
            request.setCredits(1000L);

            var response = controller.customsCheck(id, request);
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("POST /estimate-profit")
    class EstimateProfit {

        @Test
        @DisplayName("Should estimate profit for drugs")
        void testDrugsEstimate() {
            SmugglingController.EstimateRequest request = new SmugglingController.EstimateRequest();
            request.setContrabandType("DRUGS");
            request.setQuantity(10);

            var response = controller.estimateProfit(request);
            assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            assertEquals("Drugs", body.get("contraband"));
            assertEquals(10, body.get("quantity"));
            assertTrue((long) body.get("estimatedProfit") > 0);
        }

        @Test
        @DisplayName("Should return 400 for invalid contraband type")
        void testInvalidType() {
            SmugglingController.EstimateRequest request = new SmugglingController.EstimateRequest();
            request.setContrabandType("INVALID");

            var response = controller.estimateProfit(request);
            assertEquals(400, response.getStatusCode().value());
        }
    }
}
