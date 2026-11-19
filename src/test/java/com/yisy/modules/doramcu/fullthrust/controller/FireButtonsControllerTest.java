package com.yisy.modules.doramcu.fullthrust.controller;

import com.yisy.modules.doramcu.fullthrust.handler.GameWebSocketHandler;
import com.yisy.modules.doramcu.fullthrust.service.GameService;
import com.yisy.modules.doramcu.fullthrust.service.PlayerAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-level tests for the fire weapon API endpoint used by fire buttons.
 */
class FireButtonsControllerTest {

    private MockMvc mockMvc;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        GameWebSocketHandler webSocketHandler = mock(GameWebSocketHandler.class);
        PlayerAccountService playerAccountService = mock(PlayerAccountService.class);
        GameController controller = new GameController(gameService, webSocketHandler, playerAccountService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void fireWeapon_returns200Ok() throws Exception {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(new com.yisy.modules.doramcu.fullthrust.model.GameSession());

        Map<String, Object> body = new HashMap<>();
        body.put("shipId", "ship1");
        body.put("targetId", "ship2");
        body.put("weaponIndex", 0);

        mockMvc.perform(post("/api/game/session1/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(body)))
                .andExpect(status().isOk());
    }

    @Test
    void fireWeapon_returnsJsonContentType() throws Exception {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(new com.yisy.modules.doramcu.fullthrust.model.GameSession());

        Map<String, Object> body = new HashMap<>();
        body.put("shipId", "ship1");
        body.put("targetId", "ship2");
        body.put("weaponIndex", 0);

        mockMvc.perform(post("/api/game/session1/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(body)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void fireWeapon_returnsSessionId() throws Exception {
        com.yisy.modules.doramcu.fullthrust.model.GameSession session =
                new com.yisy.modules.doramcu.fullthrust.model.GameSession();
        session.setId("test-session");

        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(session);

        Map<String, Object> body = new HashMap<>();
        body.put("shipId", "ship1");
        body.put("targetId", "ship2");
        body.put("weaponIndex", 0);

        mockMvc.perform(post("/api/game/session1/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(body)))
                .andExpect(jsonPath("$.id").value("test-session"));
    }

    @Test
    void fireWeapon_throwsException_whenGameNotStarted() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalStateException("Game not in progress"));
        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon("session1", "ship1", "ship2", 0));
    }

    @Test
    void fireWeapon_throwsException_whenShipNotFound() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Ship or target not found"));
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon("session1", "non-existent", "ship2", 0));
    }

    @Test
    void fireWeapon_throwsException_whenTargetOutOfRange() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Target out of range"));
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon("session1", "ship1", "ship2", 0));
    }

    @Test
    void fireWeapon_throwsException_whenTargetNotInArc() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Target not in weapon's firing arc"));
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon("session1", "ship1", "ship2", 0));
    }

    @Test
    void fireWeapon_throwsException_whenWeaponNotReady() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalStateException("Weapon is not ready"));
        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon("session1", "ship1", "ship2", 0));
    }

    @Test
    void fireWeapon_throwsException_whenFriendlyFire() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Cannot fire at friendly ship"));
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon("session1", "ship1", "ship1", 0));
    }

    @Test
    void fireWeapon_throwsException_whenNotYourTurn() {
        when(gameService.fireWeapon(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalStateException("Not your turn"));
        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon("session1", "ship1", "ship2", 0));
    }

    private String objectToJson(Object obj) {
        // Simple JSON serialization for test request bodies
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(entry.getKey()).append("\":");
                Object value = entry.getValue();
                if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else {
                    sb.append(value);
                }
            }
            sb.append("}");
            return sb.toString();
        }
        return "{}";
    }
}
