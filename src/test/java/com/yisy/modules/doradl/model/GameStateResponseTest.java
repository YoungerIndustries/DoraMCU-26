package com.yisy.modules.doradl.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.yisy.modules.doradl.dto.GameStateResponse;
import com.yisy.modules.doradl.dto.PlayerSummary;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameStateResponse Tests")
class GameStateResponseTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            GameStateResponse response = new GameStateResponse();
            assertNull(response.getSessionId());
            assertEquals(0, response.getCurrentTurn());
            assertNull(response.getCurrentPlayerId());
            assertNull(response.getCurrentPlayerName());
            assertNull(response.getPlayers());
            assertFalse(response.isMyTurn());
            assertNull(response.getSubSectors());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets sessionId, currentTurn, currentPlayerId, currentPlayerName, players, isMyTurn")
        void setsFields() {
            List<PlayerSummary> players = List.of(new PlayerSummary());
            GameStateResponse response = new GameStateResponse("sess-1", 5, 1L, "Player1", players, true);
            assertEquals("sess-1", response.getSessionId());
            assertEquals(5, response.getCurrentTurn());
            assertEquals(1L, response.getCurrentPlayerId());
            assertEquals("Player1", response.getCurrentPlayerName());
            assertSame(players, response.getPlayers());
            assertTrue(response.isMyTurn());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get sessionId")
        void sessionId() {
            GameStateResponse response = new GameStateResponse();
            response.setSessionId("game-123");
            assertEquals("game-123", response.getSessionId());
        }

        @Test
        @DisplayName("Set and get currentTurn")
        void currentTurn() {
            GameStateResponse response = new GameStateResponse();
            response.setCurrentTurn(10);
            assertEquals(10, response.getCurrentTurn());
        }

        @Test
        @DisplayName("Set and get currentPlayerId")
        void currentPlayerId() {
            GameStateResponse response = new GameStateResponse();
            response.setCurrentPlayerId(42L);
            assertEquals(42L, response.getCurrentPlayerId());
        }

        @Test
        @DisplayName("Set and get currentPlayerName")
        void currentPlayerName() {
            GameStateResponse response = new GameStateResponse();
            response.setCurrentPlayerName("Commander");
            assertEquals("Commander", response.getCurrentPlayerName());
        }

        @Test
        @DisplayName("Set and get players")
        void players() {
            GameStateResponse response = new GameStateResponse();
            List<PlayerSummary> players = List.of(new PlayerSummary());
            response.setPlayers(players);
            assertSame(players, response.getPlayers());
        }

        @Test
        @DisplayName("Set and get isMyTurn")
        void isMyTurn() {
            GameStateResponse response = new GameStateResponse();
            response.setMyTurn(true);
            assertTrue(response.isMyTurn());
        }

        @Test
        @DisplayName("Set and get subSectors")
        void subSectors() {
            GameStateResponse response = new GameStateResponse();
            response.setSubSectors(List.of());
            assertNotNull(response.getSubSectors());
        }
    }
}
