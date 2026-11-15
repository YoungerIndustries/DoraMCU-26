package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.*;
import com.yisy.modules.doramcu.exception.NotYourTurnException;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameController Tests")
class GameControllerTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private PlayerIdExtractor playerIdExtractor;

    @Mock
    private Authentication authentication;

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(gameSessionService, playerIdExtractor);
    }

    // ---- Get Game State ----

    @Test
    @DisplayName("getGameState - should return 200 OK with game state")
    void getGameState_shouldReturnOk() {
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getGameState(eq(1L), eq("sess-1"), eq(false)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.getGameState("sess-1", false, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    @DisplayName("getGameState - should throw NoSuchElementException when session not found")
    void getGameState_shouldThrow404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getGameState(eq(1L), eq("nonexistent"), eq(false)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.getGameState("nonexistent", false, authentication));
    }

    // ---- Move Fleet ----

    @Test
    @DisplayName("moveFleet - should return 200 OK on success")
    void moveFleet_shouldReturnOk() {
        MoveFleetRequest request = new MoveFleetRequest(0, 1, 1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.moveFleet(eq(1L), eq("sess-1"), any(MoveFleetRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.moveFleet("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("moveFleet - should throw NoSuchElementException when session not found")
    void moveFleet_shouldThrow404() {
        MoveFleetRequest request = new MoveFleetRequest(0, 1, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.moveFleet(eq(1L), eq("nonexistent"), any(MoveFleetRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.moveFleet("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("moveFleet - should throw NotYourTurnException when not player's turn")
    void moveFleet_shouldThrow403() {
        MoveFleetRequest request = new MoveFleetRequest(0, 1, 1);

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.moveFleet(eq(2L), eq("sess-1"), any(MoveFleetRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.moveFleet("sess-1", request, authentication));
    }

    @Test
    @DisplayName("moveFleet - should throw IllegalStateException when fleet not found")
    void moveFleet_shouldThrow400() {
        MoveFleetRequest request = new MoveFleetRequest(999, 1, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.moveFleet(eq(1L), eq("sess-1"), any(MoveFleetRequest.class)))
                .thenThrow(new IllegalStateException("Fleet not found or not owned"));

        assertThrows(IllegalStateException.class, () -> controller.moveFleet("sess-1", request, authentication));
    }

    // ---- Build Ship ----

    @Test
    @DisplayName("buildShip - should return 200 OK on success")
    void buildShip_shouldReturnOk() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", null);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShip(eq(1L), eq("sess-1"), any(BuildShipRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.buildShip("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("buildShip - should throw NoSuchElementException when session not found")
    void buildShip_shouldThrow404() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", null);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShip(eq(1L), eq("nonexistent"), any(BuildShipRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.buildShip("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("buildShip - should throw NotYourTurnException when not player's turn")
    void buildShip_shouldThrow403() {
        BuildShipRequest request = new BuildShipRequest("FIGHTER", null);

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.buildShip(eq(2L), eq("sess-1"), any(BuildShipRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.buildShip("sess-1", request, authentication));
    }

    @Test
    @DisplayName("buildShip - should throw IllegalArgumentException on invalid ship type")
    void buildShip_shouldThrow400() {
        BuildShipRequest request = new BuildShipRequest("INVALID", null);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShip(eq(1L), eq("sess-1"), any(BuildShipRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid ship type"));

        assertThrows(IllegalArgumentException.class, () -> controller.buildShip("sess-1", request, authentication));
    }

    // ---- Engage Combat ----

    @Test
    @DisplayName("engageCombat - should return 200 OK on success")
    void engageCombat_shouldReturnOk() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.engageCombat(eq(1L), eq("sess-1"), any(EngageCombatRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.engageCombat("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("engageCombat - should throw NoSuchElementException when session not found")
    void engageCombat_shouldThrow404() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.engageCombat(eq(1L), eq("nonexistent"), any(EngageCombatRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.engageCombat("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("engageCombat - should throw NotYourTurnException when not player's turn")
    void engageCombat_shouldThrow403() {
        EngageCombatRequest request = new EngageCombatRequest(0, 1);

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.engageCombat(eq(2L), eq("sess-1"), any(EngageCombatRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.engageCombat("sess-1", request, authentication));
    }

    @Test
    @DisplayName("engageCombat - should throw IllegalStateException when fleet not found")
    void engageCombat_shouldThrow400() {
        EngageCombatRequest request = new EngageCombatRequest(999, 1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.engageCombat(eq(1L), eq("sess-1"), any(EngageCombatRequest.class)))
                .thenThrow(new IllegalStateException("Attacker fleet not found or not owned"));

        assertThrows(IllegalStateException.class, () -> controller.engageCombat("sess-1", request, authentication));
    }

    // ---- Advance Turn ----

    @Test
    @DisplayName("advanceTurn - should return 200 OK on success")
    void advanceTurn_shouldReturnOk() {
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 1, 2L, "Player2", Collections.emptyList(), false);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.advanceTurn(eq(1L), eq("sess-1")))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.advanceTurn("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getCurrentTurn());
        assertEquals(2L, response.getBody().getCurrentPlayerId());
    }

    @Test
    @DisplayName("advanceTurn - should throw NoSuchElementException when session not found")
    void advanceTurn_shouldThrow404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.advanceTurn(eq(1L), eq("nonexistent")))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.advanceTurn("nonexistent", authentication));
    }

    @Test
    @DisplayName("advanceTurn - should throw NotYourTurnException when not player's turn")
    void advanceTurn_shouldThrow403() {
        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.advanceTurn(eq(2L), eq("sess-1")))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.advanceTurn("sess-1", authentication));
    }

    // ---- Pipeline Status ----

    @Test
    @DisplayName("getPipelineStatus - should return 200 OK with pipeline summary")
    void getPipelineStatus_shouldReturnOk() {
        com.yisy.modules.doramcu.dto.ResourcePipelineSummary expected =
                new com.yisy.modules.doramcu.dto.ResourcePipelineSummary();

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getPipelineSummary(eq(1L), eq("sess-1")))
                .thenReturn(expected);

        ResponseEntity<com.yisy.modules.doramcu.dto.ResourcePipelineSummary> response =
                controller.getPipelineStatus("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    @DisplayName("getPipelineStatus - should return 404 when pipeline not found")
    void getPipelineStatus_shouldReturn404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getPipelineSummary(eq(1L), eq("sess-1")))
                .thenReturn(null);

        ResponseEntity<com.yisy.modules.doramcu.dto.ResourcePipelineSummary> response =
                controller.getPipelineStatus("sess-1", authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---- Pipeline Details ----

    @Test
    @DisplayName("getPipelineDetails - should return 200 OK with pipeline details")
    void getPipelineDetails_shouldReturnOk() {
        com.yisy.modules.doramcu.dto.PipelineDetailsDto expected =
                new com.yisy.modules.doramcu.dto.PipelineDetailsDto();

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getPipelineDetails(eq(1L), eq("sess-1")))
                .thenReturn(expected);

        ResponseEntity<com.yisy.modules.doramcu.dto.PipelineDetailsDto> response =
                controller.getPipelineDetails("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    @DisplayName("getPipelineDetails - should return 404 when pipeline not found")
    void getPipelineDetails_shouldReturn404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getPipelineDetails(eq(1L), eq("sess-1")))
                .thenReturn(null);

        ResponseEntity<com.yisy.modules.doramcu.dto.PipelineDetailsDto> response =
                controller.getPipelineDetails("sess-1", authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---- Build Station ----

    @Test
    @DisplayName("buildStation - should return 200 OK on success")
    void buildStation_shouldReturnOk() {
        BuildStationRequest request = new BuildStationRequest();
        request.setStationType("OUTPOST");
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildStation(eq(1L), eq("sess-1"), any(BuildStationRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.buildStation("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("buildStation - should throw NoSuchElementException when session not found")
    void buildStation_shouldThrow404() {
        BuildStationRequest request = new BuildStationRequest();
        request.setStationType("OUTPOST");

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildStation(eq(1L), eq("nonexistent"), any(BuildStationRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.buildStation("nonexistent", request, authentication));
    }

    @Test
    @DisplayName("buildStation - should throw NotYourTurnException when not player's turn")
    void buildStation_shouldThrow403() {
        BuildStationRequest request = new BuildStationRequest();
        request.setStationType("OUTPOST");

        when(authentication.getName()).thenReturn("2");
        when(playerIdExtractor.extract(authentication)).thenReturn(2L);
        when(gameSessionService.buildStation(eq(2L), eq("sess-1"), any(BuildStationRequest.class)))
                .thenThrow(new NotYourTurnException("Not your turn"));

        assertThrows(NotYourTurnException.class, () -> controller.buildStation("sess-1", request, authentication));
    }

    // ---- Build Facility ----

    @Test
    @DisplayName("buildFacility - should return 200 OK on success")
    void buildFacility_shouldReturnOk() {
        BuildFacilityRequest request = new BuildFacilityRequest();
        request.setPlanetId(1);
        request.setFacilityType("SHIPYARD");
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildFacility(eq(1L), eq("sess-1"), any(BuildFacilityRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.buildFacility("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("buildFacility - should throw NoSuchElementException when session not found")
    void buildFacility_shouldThrow404() {
        BuildFacilityRequest request = new BuildFacilityRequest();
        request.setPlanetId(1);
        request.setFacilityType("SHIPYARD");

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildFacility(eq(1L), eq("nonexistent"), any(BuildFacilityRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.buildFacility("nonexistent", request, authentication));
    }

    // ---- Create Fleet ----

    @Test
    @DisplayName("createFleet - should return 200 OK on success")
    void createFleet_shouldReturnOk() {
        CreateFleetRequest request = new CreateFleetRequest();
        request.setFleetName("Test Fleet");
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.createFleet(eq(1L), eq("sess-1"), any(CreateFleetRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.createFleet("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("createFleet - should throw NoSuchElementException when session not found")
    void createFleet_shouldThrow404() {
        CreateFleetRequest request = new CreateFleetRequest();
        request.setFleetName("Test Fleet");

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.createFleet(eq(1L), eq("nonexistent"), any(CreateFleetRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.createFleet("nonexistent", request, authentication));
    }

    // ---- Colonize Planet ----

    @Test
    @DisplayName("colonizePlanet - should return 200 OK on success")
    void colonizePlanet_shouldReturnOk() {
        ColonizeRequest request = new ColonizeRequest();
        request.setPlanetId(1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.colonizePlanet(eq(1L), eq("sess-1"), any(ColonizeRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.colonizePlanet("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("colonizePlanet - should throw NoSuchElementException when session not found")
    void colonizePlanet_shouldThrow404() {
        ColonizeRequest request = new ColonizeRequest();
        request.setPlanetId(1);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.colonizePlanet(eq(1L), eq("nonexistent"), any(ColonizeRequest.class)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.colonizePlanet("nonexistent", request, authentication));
    }

    // ---- Active Battles ----

    @Test
    @DisplayName("getActiveBattles - should return 200 OK with battles list")
    void getActiveBattles_shouldReturnOk() {
        java.util.List<BattleSummaryDto> expected = Collections.emptyList();

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getActiveBattles(eq(1L), eq("sess-1")))
                .thenReturn(expected);

        ResponseEntity<java.util.List<BattleSummaryDto>> response = controller.getActiveBattles("sess-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- Reinforce Battle ----

    @Test
    @DisplayName("reinforceBattle - should return 200 OK on success")
    void reinforceBattle_shouldReturnOk() {
        ReinforceBattleRequest request = new ReinforceBattleRequest();
        request.setFleetId(1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.reinforceBattle(eq(1L), eq("sess-1"), eq(1), any(ReinforceBattleRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.reinforceBattle("sess-1", 1, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- Retreat Battle ----

    @Test
    @DisplayName("retreatBattle - should return 200 OK on success")
    void retreatBattle_shouldReturnOk() {
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.retreatBattle(eq(1L), eq("sess-1"), eq(1)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.retreatBattle("sess-1", 1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- System Details ----

    @Test
    @DisplayName("getSystemDetails - should return 200 OK with system details")
    void getSystemDetails_shouldReturnOk() {
        java.util.Map<String, Object> expected = new java.util.HashMap<>();
        expected.put("systemId", 1);
        expected.put("name", "Sol");

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getSystemDetails(eq(1L), eq("sess-1"), eq(1)))
                .thenReturn(expected);

        ResponseEntity<java.util.Map<String, Object>> response = controller.getSystemDetails("sess-1", 1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sol", response.getBody().get("name"));
    }

    @Test
    @DisplayName("getSystemDetails - should return 404 when system not found")
    void getSystemDetails_shouldReturn404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getSystemDetails(eq(1L), eq("sess-1"), eq(999)))
                .thenReturn(null);

        ResponseEntity<java.util.Map<String, Object>> response = controller.getSystemDetails("sess-1", 999, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---- Build From Design (Traveller RPG) ----

    @Test
    @DisplayName("buildFromDesign - should return 200 OK on success")
    void buildFromDesign_shouldReturnOk() {
        ShipDesignRequest request = new ShipDesignRequest();
        request.setName("Design-1");
        request.setTotalTonnage(1000);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buildShipFromDesign(eq(1L), eq("sess-1"), any(ShipDesignRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.buildFromDesign("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- Trade: Buy Commodity ----

    @Test
    @DisplayName("buyCommodity - should return 200 OK on success")
    void buyCommodity_shouldReturnOk() {
        TradeRequest request = new TradeRequest();
        request.setCommodity("TIN");
        request.setQuantity(100);
        request.setWorldId(1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.buyCommodity(eq(1L), eq("sess-1"), any(TradeRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.buyCommodity("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- Trade: Sell Commodity ----

    @Test
    @DisplayName("sellCommodity - should return 200 OK on success")
    void sellCommodity_shouldReturnOk() {
        TradeRequest request = new TradeRequest();
        request.setCommodity("TIN");
        request.setQuantity(100);
        request.setWorldId(1);
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.sellCommodity(eq(1L), eq("sess-1"), any(TradeRequest.class)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.sellCommodity("sess-1", request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- Trade Market ----

    @Test
    @DisplayName("getTradeMarket - should return 200 OK with trade opportunities")
    void getTradeMarket_shouldReturnOk() {
        java.util.List<TradeOpportunityDto> expected = Collections.emptyList();

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.getTradeMarket(eq(1L), eq("sess-1"), eq(1)))
                .thenReturn(expected);

        ResponseEntity<java.util.List<TradeOpportunityDto>> response = controller.getTradeMarket("sess-1", 1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ---- Assign Commander ----

    @Test
    @DisplayName("assignCommander - should return 200 OK on success")
    void assignCommander_shouldReturnOk() {
        GameStateResponse expected = new GameStateResponse(
                "sess-1", 0, 1L, "Player1", Collections.emptyList(), true);

        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.assignCommander(eq(1L), eq("sess-1"), eq(1)))
                .thenReturn(expected);

        ResponseEntity<GameStateResponse> response = controller.assignCommander("sess-1", 1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("assignCommander - should throw NoSuchElementException when session not found")
    void assignCommander_shouldThrow404() {
        when(authentication.getName()).thenReturn("1");
        when(playerIdExtractor.extract(authentication)).thenReturn(1L);
        when(gameSessionService.assignCommander(eq(1L), eq("nonexistent"), eq(1)))
                .thenThrow(new NoSuchElementException("Session not found"));

        assertThrows(NoSuchElementException.class, () -> controller.assignCommander("nonexistent", 1, authentication));
    }
}

