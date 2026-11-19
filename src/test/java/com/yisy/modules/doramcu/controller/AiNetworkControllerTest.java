package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.service.ai.AiNetworkService;
import com.yisy.modules.doramcu.service.ai.AiNetworkService.AiNetworkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiNetworkController Tests")
class AiNetworkControllerTest {

    @Mock
    private AiNetworkService aiNetworkService;

    private AiNetworkController controller;

    @BeforeEach
    void setUp() {
        controller = new AiNetworkController(aiNetworkService);
    }

    // --- Helper methods ---

    private AiCharacter createAiCharacter(AiCharacter.CharacterType type, String empireName, boolean active) {
        AiCharacter character = new AiCharacter();
        character.setId(1L);
        character.setName("Test AI");
        character.setRole("Computer");
        character.setEmpireName(empireName);
        character.setCharacterType(type);
        character.setActive(active);
        return character;
    }

    // --- getAllNetworks Tests ---

    @Test
    @DisplayName("getAllNetworks should return OK with list of network statuses")
    void getAllNetworks_shouldReturnOkWithStatuses() {
        List<AiNetworkStatus> statuses = new ArrayList<>();
        statuses.add(new AiNetworkStatus("Federation", 5, 2, 2, 1, 1, 1, 1, 0, 0, 0));
        statuses.add(new AiNetworkStatus("Empire", 3, 1, 1, 1, 0, 1, 0, 0, 0, 0));

        when(aiNetworkService.getAllNetworksStatus()).thenReturn(statuses);

        ResponseEntity<List<AiNetworkStatus>> response = controller.getAllNetworks();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Federation", response.getBody().get(0).getEmpireName());
        assertEquals("Empire", response.getBody().get(1).getEmpireName());
        verify(aiNetworkService).getAllNetworksStatus();
    }

    @Test
    @DisplayName("getAllNetworks should return OK with empty list when no networks")
    void getAllNetworks_shouldReturnOkWithEmptyList() {
        when(aiNetworkService.getAllNetworksStatus()).thenReturn(new ArrayList<>());

        ResponseEntity<List<AiNetworkStatus>> response = controller.getAllNetworks();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // --- getNetworkStatus Tests ---

    @Test
    @DisplayName("getNetworkStatus should return OK with single empire status")
    void getNetworkStatus_shouldReturnOkWithStatus() {
        String empireName = "Federation";
        AiNetworkStatus status = new AiNetworkStatus(empireName, 10, 3, 4, 3, 2, 1, 1, 1, 0, 0);

        when(aiNetworkService.getNetworkStatus(empireName)).thenReturn(status);

        ResponseEntity<AiNetworkStatus> response = controller.getNetworkStatus(empireName);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(empireName, response.getBody().getEmpireName());
        assertEquals(10, response.getBody().getTotalActiveEntities());
        assertEquals(3, response.getBody().getTotalInactiveEntities());
        verify(aiNetworkService).getNetworkStatus(empireName);
    }

    // --- getActiveEntities Tests ---

    @Test
    @DisplayName("getActiveEntities should return OK with active entities list")
    void getActiveEntities_shouldReturnOkWithEntities() {
        String empireName = "Federation";
        List<AiCharacter> entities = new ArrayList<>();
        entities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        entities.add(createAiCharacter(AiCharacter.CharacterType.STATION, empireName, true));

        when(aiNetworkService.getActiveNetworkEntities(empireName)).thenReturn(entities);

        ResponseEntity<List<AiCharacter>> response = controller.getActiveEntities(empireName);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(aiNetworkService).getActiveNetworkEntities(empireName);
    }

    @Test
    @DisplayName("getActiveEntities should return OK with empty list when no entities")
    void getActiveEntities_shouldReturnOkWithEmptyList() {
        String empireName = "Empty";
        when(aiNetworkService.getActiveNetworkEntities(empireName)).thenReturn(new ArrayList<>());

        ResponseEntity<List<AiCharacter>> response = controller.getActiveEntities(empireName);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // --- getAllEntities Tests ---

    @Test
    @DisplayName("getAllEntities should return OK with all entities (active and inactive)")
    void getAllEntities_shouldReturnOkWithAllEntities() {
        String empireName = "Federation";
        List<AiCharacter> entities = new ArrayList<>();
        entities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        entities.add(createAiCharacter(AiCharacter.CharacterType.STATION, empireName, false));

        when(aiNetworkService.getAllNetworkEntities(empireName)).thenReturn(entities);

        ResponseEntity<List<AiCharacter>> response = controller.getAllEntities(empireName);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(aiNetworkService).getAllNetworkEntities(empireName);
    }

    // --- getTopology Tests ---

    @Test
    @DisplayName("getTopology should return OK with string-keyed topology map")
    void getTopology_shouldReturnOkWithStringKeyedMap() {
        String empireName = "Federation";

        Map<AiCharacter.CharacterType, List<AiCharacter>> topology = new java.util.HashMap<>();
        topology.put(AiCharacter.CharacterType.SHIP, List.of(
                createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true)
        ));
        topology.put(AiCharacter.CharacterType.STATION, List.of(
                createAiCharacter(AiCharacter.CharacterType.STATION, empireName, true)
        ));

        when(aiNetworkService.getNetworkTopology(empireName)).thenReturn(topology);

        ResponseEntity<Map<String, List<AiCharacter>>> response = controller.getTopology(empireName);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().containsKey("SHIP"));
        assertTrue(response.getBody().containsKey("STATION"));
        assertEquals(1, response.getBody().get("SHIP").size());
        assertEquals(1, response.getBody().get("STATION").size());
    }

    @Test
    @DisplayName("getTopology should return OK with empty map when no entities")
    void getTopology_shouldReturnOkWithEmptyMap() {
        String empireName = "Empty";
        when(aiNetworkService.getNetworkTopology(empireName)).thenReturn(new java.util.HashMap<>());

        ResponseEntity<Map<String, List<AiCharacter>>> response = controller.getTopology(empireName);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // --- isShipConnected Tests ---

    @Test
    @DisplayName("isShipConnected should return OK with connected=true")
    void isShipConnected_shouldReturnConnected() {
        when(aiNetworkService.isShipConnected(42)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.isShipConnected(42);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(42, response.getBody().get("shipId"));
        assertEquals(true, response.getBody().get("connected"));
        verify(aiNetworkService).isShipConnected(42);
    }

    @Test
    @DisplayName("isShipConnected should return OK with connected=false")
    void isShipConnected_shouldReturnNotConnected() {
        when(aiNetworkService.isShipConnected(99)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.isShipConnected(99);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(false, response.getBody().get("connected"));
    }

    // --- isStationConnected Tests ---

    @Test
    @DisplayName("isStationConnected should return OK with connected=true")
    void isStationConnected_shouldReturnConnected() {
        when(aiNetworkService.isStationConnected(10)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.isStationConnected(10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, response.getBody().get("stationId"));
        assertEquals(true, response.getBody().get("connected"));
    }

    @Test
    @DisplayName("isStationConnected should return OK with connected=false")
    void isStationConnected_shouldReturnNotConnected() {
        when(aiNetworkService.isStationConnected(20)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.isStationConnected(20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(false, response.getBody().get("connected"));
    }

    // --- isVehicleConnected Tests ---

    @Test
    @DisplayName("isVehicleConnected should return OK with connected=true")
    void isVehicleConnected_shouldReturnConnected() {
        when(aiNetworkService.isVehicleConnected("vehicle-1")).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.isVehicleConnected("vehicle-1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("vehicle-1", response.getBody().get("vehicleId"));
        assertEquals(true, response.getBody().get("connected"));
    }

    @Test
    @DisplayName("isVehicleConnected should return OK with connected=false")
    void isVehicleConnected_shouldReturnNotConnected() {
        when(aiNetworkService.isVehicleConnected("vehicle-2")).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.isVehicleConnected("vehicle-2");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(false, response.getBody().get("connected"));
    }

    // --- isFacilityConnected Tests ---

    @Test
    @DisplayName("isFacilityConnected should return OK with connected=true")
    void isFacilityConnected_shouldReturnConnected() {
        when(aiNetworkService.isFacilityConnected(5)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.isFacilityConnected(5);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(5, response.getBody().get("facilityId"));
        assertEquals(true, response.getBody().get("connected"));
    }

    @Test
    @DisplayName("isFacilityConnected should return OK with connected=false")
    void isFacilityConnected_shouldReturnNotConnected() {
        when(aiNetworkService.isFacilityConnected(15)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.isFacilityConnected(15);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(false, response.getBody().get("connected"));
    }

    // --- Panel data structure tests ---

    @Test
    @DisplayName("getAllNetworks response should contain all fields needed by frontend panel")
    void getAllNetworks_shouldContainPanelFields() {
        AiNetworkStatus status = new AiNetworkStatus(
                "Federation",
                10,  // totalActiveEntities
                3,   // totalInactiveEntities
                4,   // activeShips
                3,   // activeStations
                2,   // activeVehicles
                1,   // activeFacilities
                1,   // inactiveShips
                1,   // inactiveStations
                0,   // inactiveVehicles
                0    // inactiveFacilities
        );
        when(aiNetworkService.getAllNetworksStatus()).thenReturn(List.of(status));

        ResponseEntity<List<AiNetworkStatus>> response = controller.getAllNetworks();
        AiNetworkStatus body = response.getBody().get(0);

        // Verify all fields accessed by the frontend panel are present
        assertNotNull(body.getEmpireName());
        assertEquals(10, body.getTotalActiveEntities());
        assertEquals(3, body.getTotalInactiveEntities());
        assertEquals(4, body.getActiveShips());
        assertEquals(3, body.getActiveStations());
        assertEquals(2, body.getActiveVehicles());
        assertEquals(1, body.getActiveFacilities());
        assertEquals(1, body.getInactiveShips());
        assertEquals(1, body.getInactiveStations());
        assertEquals(0, body.getInactiveVehicles());
        assertEquals(0, body.getInactiveFacilities());
    }
}
