package com.yisy.modules.doramcu.traveller.aircombat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AirCombatController Tests")
@ExtendWith(MockitoExtension.class)
class AirCombatControllerTest {

    @Mock
    private AirCombatService service;

    private AirCombatController controller;

    @BeforeEach
    void setUp() {
        controller = new AirCombatController(service);
    }

    // ---- Aircraft Endpoints ----

    @Nested
    @DisplayName("Aircraft Endpoint Tests")
    class AircraftEndpointTests {

        @Test
        @DisplayName("getAllAircraft should return 200 OK")
        void getAllAircraft_shouldReturnOk() {
            AircraftDesign f1 = new AircraftDesign();
            f1.setId(1L);
            f1.setName("Star Kraken");
            when(service.getAllAircraft()).thenReturn(List.of(f1));

            ResponseEntity<List<AircraftDesign>> response = controller.getAllAircraft();

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("Star Kraken", response.getBody().get(0).getName());
        }

        @Test
        @DisplayName("getAircraftByType should return filtered list")
        void getAircraftByType_shouldReturnFiltered() {
            AircraftDesign f1 = new AircraftDesign();
            f1.setId(1L);
            f1.setType("FIGHTER");
            when(service.getAircraftByType("FIGHTER")).thenReturn(List.of(f1));

            ResponseEntity<List<AircraftDesign>> response = controller.getAircraftByType("FIGHTER");

            assertEquals(200, response.getStatusCode().value());
            assertEquals("FIGHTER", response.getBody().get(0).getType());
        }
    }

    // ---- Mission Endpoints ----

    @Nested
    @DisplayName("Mission Endpoint Tests")
    class MissionEndpointTests {

        @Test
        @DisplayName("getMissions should return list")
        void getMissions_shouldReturnList() {
            AirCombatMission m1 = new AirCombatMission();
            m1.setId(1L);
            m1.setCharacterId("player1");
            m1.setStatus("ACTIVE");
            when(service.getMissions("player1")).thenReturn(List.of(m1));

            ResponseEntity<List<AirCombatMission>> response = controller.getMissions("player1");

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("player1", response.getBody().get(0).getCharacterId());
        }

        @Test
        @DisplayName("getMissions should return empty list")
        void getMissions_shouldReturnEmpty() {
            when(service.getMissions("player1")).thenReturn(List.of());

            ResponseEntity<List<AirCombatMission>> response = controller.getMissions("player1");

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("createMission should return 200 OK")
        void createMission_shouldReturnOk() {
            AirCombatMission m1 = new AirCombatMission();
            m1.setId(1L);
            m1.setStatus("ACTIVE");
            when(service.createMission("player1", "AIR_TO_AIR")).thenReturn(m1);

            ResponseEntity<AirCombatMission> response = controller.createMission("player1", "AIR_TO_AIR");

            assertEquals(200, response.getStatusCode().value());
            assertEquals("ACTIVE", response.getBody().getStatus());
        }

        @Test
        @DisplayName("createMission with null type")
        void createMission_withNullType() {
            AirCombatMission m1 = new AirCombatMission();
            m1.setId(1L);
            m1.setMissionType("AIR_TO_AIR");
            when(service.createMission("player1", null)).thenReturn(m1);

            ResponseEntity<AirCombatMission> response = controller.createMission("player1", null);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("AIR_TO_AIR", response.getBody().getMissionType());
        }

        @Test
        @DisplayName("resolveMission should return 200 OK")
        void resolveMission_shouldReturnOk() {
            AirCombatService.MissionResult result = new AirCombatService.MissionResult();
            result.setSuccess(true);
            result.setMessage("Complete!");
            result.setXpEarned(100);
            result.setCreditsEarned(500);
            when(service.resolveMission(1L)).thenReturn(result);

            ResponseEntity<AirCombatService.MissionResult> response = controller.resolveMission(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals(100, response.getBody().getXpEarned());
        }

        @Test
        @DisplayName("resolveMission failure should return 200 OK")
        void resolveMission_failure_shouldReturnOk() {
            AirCombatService.MissionResult result = new AirCombatService.MissionResult();
            result.setSuccess(false);
            result.setMessage("Failed!");
            result.setXpEarned(25);
            result.setCreditsEarned(0);
            when(service.resolveMission(1L)).thenReturn(result);

            ResponseEntity<AirCombatService.MissionResult> response = controller.resolveMission(1L);

            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().isSuccess());
        }
    }
}
