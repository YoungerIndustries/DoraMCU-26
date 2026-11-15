package com.yisy.modules.doramcu.traveller.shipcustomization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ShipCustomizationController Tests")
@ExtendWith(MockitoExtension.class)
class ShipCustomizationControllerTest {

    @Mock
    private ShipCustomizationService service;

    private ShipCustomizationController controller;

    @BeforeEach
    void setUp() {
        controller = new ShipCustomizationController(service);
    }

    // ---- Component Endpoints ----

    @Nested
    @DisplayName("Component Endpoint Tests")
    class ComponentEndpointTests {

        @Test
        @DisplayName("getAllComponents should return 200 OK")
        void getAllComponents_shouldReturnOk() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            when(service.getAllComponents()).thenReturn(List.of(c1));

            ResponseEntity<List<ShipComponent>> response = controller.getAllComponents();

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("getComponentsByType should return filtered list")
        void getComponentsByType_shouldReturnFiltered() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            when(service.getComponentsByType(ComponentType.TURRET_WEAPON)).thenReturn(List.of(c1));

            ResponseEntity<List<ShipComponent>> response =
                    controller.getComponentsByType(ComponentType.TURRET_WEAPON);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(ComponentType.TURRET_WEAPON, response.getBody().get(0).getType());
        }

        @Test
        @DisplayName("getComponentsByTypeAndTech should return filtered list")
        void getComponentsByTypeAndTech_shouldReturnFiltered() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            when(service.getComponentsByTypeAndTech(ComponentType.TURRET_WEAPON, 11)).thenReturn(List.of(c1));

            ResponseEntity<List<ShipComponent>> response =
                    controller.getComponentsByTypeAndTech(ComponentType.TURRET_WEAPON, 11);

            assertEquals(200, response.getStatusCode().value());
        }
    }

    // ---- Design Endpoints ----

    @Nested
    @DisplayName("Design Endpoint Tests")
    class DesignEndpointTests {

        private CustomShipDesign testDesign;

        @BeforeEach
        void createDesign() {
            testDesign = new CustomShipDesign("Test Ship", "player1", 1000, 11);
            testDesign.setId(1L);
        }

        @Test
        @DisplayName("createDesign should return 200 OK")
        void createDesign_shouldReturnOk() {
            when(service.createDesign("Test Ship", "player1", 1000, 11)).thenReturn(testDesign);

            ShipCustomizationController.DesignRequest request = new ShipCustomizationController.DesignRequest();
            request.setName("Test Ship");
            request.setOwnerId("player1");
            request.setTotalTonnage(1000);
            request.setTechLevel(11);

            ResponseEntity<CustomShipDesign> response = controller.createDesign(request);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Test Ship", response.getBody().getName());
        }

        @Test
        @DisplayName("getDesign should return 200 for existing")
        void getDesign_shouldReturnOk() {
            when(service.getDesign(1L)).thenReturn(Optional.of(testDesign));

            ResponseEntity<CustomShipDesign> response = controller.getDesign(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("Test Ship", response.getBody().getName());
        }

        @Test
        @DisplayName("getDesign should return 404 for missing")
        void getDesign_shouldReturnNotFound() {
            when(service.getDesign(999L)).thenReturn(Optional.empty());

            ResponseEntity<CustomShipDesign> response = controller.getDesign(999L);

            assertEquals(404, response.getStatusCode().value());
        }

        @Test
        @DisplayName("getDesignsByOwner should return list")
        void getDesignsByOwner_shouldReturnList() {
            when(service.getDesignsByOwner("player1")).thenReturn(List.of(testDesign));

            ResponseEntity<List<CustomShipDesign>> response = controller.getDesignsByOwner("player1");

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("updateDesign should return updated design")
        void updateDesign_shouldReturnUpdated() {
            when(service.updateDesign(1L, "New Name", "New Desc")).thenReturn(testDesign);

            ShipCustomizationController.DesignUpdateRequest request = new ShipCustomizationController.DesignUpdateRequest();
            request.setName("New Name");
            request.setDescription("New Desc");

            ResponseEntity<CustomShipDesign> response = controller.updateDesign(1L, request);

            assertEquals(200, response.getStatusCode().value());
        }

        @Test
        @DisplayName("deleteDesign should return 204")
        void deleteDesign_shouldReturnNoContent() {
            doNothing().when(service).deleteDesign(1L);

            ResponseEntity<Void> response = controller.deleteDesign(1L);

            assertEquals(204, response.getStatusCode().value());
        }
    }

    // ---- Component Slot Endpoints ----

    @Nested
    @DisplayName("Component Slot Endpoint Tests")
    class ComponentSlotEndpointTests {

        private CustomShipDesign testDesign;

        @BeforeEach
        void createDesign() {
            testDesign = new CustomShipDesign("Test Ship", "player1", 1000, 11);
            testDesign.setId(1L);
        }

        @Test
        @DisplayName("addComponent should return updated design")
        void addComponent_shouldReturnUpdated() {
            when(service.addComponent(1L, "Turret-1", 10L)).thenReturn(testDesign);

            ShipCustomizationController.ComponentSlotRequest request = new ShipCustomizationController.ComponentSlotRequest();
            request.setSlotName("Turret-1");
            request.setComponentId(10L);

            ResponseEntity<CustomShipDesign> response = controller.addComponent(1L, request);

            assertEquals(200, response.getStatusCode().value());
        }

        @Test
        @DisplayName("removeComponent should return updated design")
        void removeComponent_shouldReturnUpdated() {
            when(service.removeComponent(1L, "Turret-1")).thenReturn(testDesign);

            ResponseEntity<CustomShipDesign> response = controller.removeComponent(1L, "Turret-1");

            assertEquals(200, response.getStatusCode().value());
        }

        @Test
        @DisplayName("validateDesign should return validated design")
        void validateDesign_shouldReturnValidated() {
            testDesign.setValidated(true);
            when(service.validateDesign(1L)).thenReturn(testDesign);

            ResponseEntity<CustomShipDesign> response = controller.validateDesign(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isValidated());
        }

        @Test
        @DisplayName("validateDesign should return 400 on error")
        void validateDesign_shouldReturnBadRequest() {
            when(service.validateDesign(1L)).thenThrow(new IllegalArgumentException("Tonnage overflow"));

            ResponseEntity<CustomShipDesign> response = controller.validateDesign(1L);

            assertEquals(400, response.getStatusCode().value());
        }
    }
}
