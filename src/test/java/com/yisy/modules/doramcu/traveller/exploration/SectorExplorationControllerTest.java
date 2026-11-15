package com.yisy.modules.doramcu.traveller.exploration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SectorExplorationController Tests")
@ExtendWith(MockitoExtension.class)
class SectorExplorationControllerTest {

    @Mock
    SectorExplorationService service;

    SectorExplorationController controller;

    @BeforeEach
    void setUp() {
        assertNotNull(service, "Mock service should be initialized");
        controller = new SectorExplorationController(service);
    }

    @Test
    @DisplayName("getCurrentSystem should return 200 OK")
    void getCurrentSystem_shouldReturnOk() {
        StarSystem sol = new StarSystem("Sol", "1717");
        sol.setId(1L);
        when(service.getCurrentSystem("player1")).thenReturn(Optional.of(sol));

        ResponseEntity<StarSystem> response = controller.getCurrentSystem("player1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Sol", response.getBody().getName());
    }

    @Test
    @DisplayName("getCurrentSystem should return 404 for missing")
    void getCurrentSystem_shouldReturnNotFound() {
        when(service.getCurrentSystem("unknown")).thenReturn(Optional.empty());

        ResponseEntity<StarSystem> response = controller.getCurrentSystem("unknown");

        assertEquals(404, response.getStatusCode().value());
    }

    // Navigation tests are covered by SectorExplorationServiceTest

    @Test
    @DisplayName("land should return 200 OK")
    void land_shouldReturnOk() {
        PlanetLanding landing = new PlanetLanding();
        landing.setId(1L);
        landing.setOutcome("SUCCESS");
        when(service.attemptLanding("player1")).thenReturn(landing);

        ResponseEntity<PlanetLanding> response = controller.land("player1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("SUCCESS", response.getBody().getOutcome());
    }

    @Test
    @DisplayName("getLandings should return list")
    void getLandings_shouldReturnList() {
        PlanetLanding landing = new PlanetLanding();
        landing.setId(1L);
        landing.setOutcome("SUCCESS");
        when(service.getLandingHistory("player1")).thenReturn(List.of(landing));

        ResponseEntity<List<PlanetLanding>> response = controller.getLandings("player1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("getTradeGoods should return list")
    void getTradeGoods_shouldReturnList() {
        when(service.getTradeGoods("player1")).thenReturn(List.of("Food", "Electronics"));

        ResponseEntity<List<String>> response = controller.getTradeGoods("player1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getAllSystems should return list")
    void getAllSystems_shouldReturnList() {
        StarSystem sol = new StarSystem("Sol", "1717");
        sol.setId(1L);
        when(service.getAllSystems()).thenReturn(List.of(sol));

        ResponseEntity<List<StarSystem>> response = controller.getAllSystems();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }
}
