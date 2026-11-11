package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameSession Tests")
class GameSessionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            GameSession session = new GameSession();
            assertNull(session.getId());
            assertNull(session.getName());
            assertEquals(0, session.getGalaxyId());
            assertEquals(0, session.getCurrentTurn());
            assertEquals(GameSession.GameMode.FOUR_X, session.getMode());
            assertEquals(0, session.getGalaxySeed());
            assertNotNull(session.getCreatedAt());
            assertNotNull(session.getUpdatedAt());
            assertTrue(session.getFactions().isEmpty());
            assertTrue(session.getColonies().isEmpty());
            assertTrue(session.getTradeRoutes().isEmpty());
            assertTrue(session.getDiplomaticRelations().isEmpty());
        }
    }

    @Nested
    @DisplayName("Turn advancement")
    class TurnAdvancement {

        @Test
        @DisplayName("Advance turn increments counter")
        void advanceTurn() {
            GameSession session = new GameSession();
            session.advanceTurn();
            assertEquals(1, session.getCurrentTurn());
        }

        @Test
        @DisplayName("Multiple advances")
        void multipleAdvances() {
            GameSession session = new GameSession();
            session.advanceTurn();
            session.advanceTurn();
            session.advanceTurn();
            assertEquals(3, session.getCurrentTurn());
        }

        @Test
        @DisplayName("Advance turn updates timestamp")
        void updatesTimestamp() {
            GameSession session = new GameSession();
            LocalDateTime before = LocalDateTime.now();
            session.advanceTurn();
            assertNotNull(session.getUpdatedAt());
            assertTrue(session.getUpdatedAt().isAfter(before) || session.getUpdatedAt().isEqual(before));
        }
    }

    @Nested
    @DisplayName("Mode switching")
    class ModeSwitching {

        @Test
        @DisplayName("Switch to CAPTAINS_LOG mode")
        void switchToCaptainsLog() {
            GameSession session = new GameSession();
            session.switchMode(GameSession.GameMode.CAPTAINS_LOG);
            assertEquals(GameSession.GameMode.CAPTAINS_LOG, session.getMode());
        }

        @Test
        @DisplayName("Switch to FOUR_X mode")
        void switchToFourX() {
            GameSession session = new GameSession();
            session.switchMode(GameSession.GameMode.CAPTAINS_LOG);
            session.switchMode(GameSession.GameMode.FOUR_X);
            assertEquals(GameSession.GameMode.FOUR_X, session.getMode());
        }

        @Test
        @DisplayName("Switch mode updates timestamp")
        void updatesTimestamp() {
            GameSession session = new GameSession();
            LocalDateTime before = LocalDateTime.now();
            session.switchMode(GameSession.GameMode.CAPTAINS_LOG);
            assertNotNull(session.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("GameMode enum")
    class GameModeEnum {

        @Test
        @DisplayName("GameMode values")
        void gameModeValues() {
            assertSame(GameSession.GameMode.CAPTAINS_LOG, GameSession.GameMode.valueOf("CAPTAINS_LOG"));
            assertSame(GameSession.GameMode.FOUR_X, GameSession.GameMode.valueOf("FOUR_X"));
        }
    }

    @Nested
    @DisplayName("Galaxy properties")
    class GalaxyProperties {

        @Test
        @DisplayName("Set galaxy ID and seed")
        void setGalaxyProperties() {
            GameSession session = new GameSession();
            session.setGalaxyId(42);
            session.setGalaxySeed(123456789L);
            assertEquals(42, session.getGalaxyId());
            assertEquals(123456789L, session.getGalaxySeed());
        }
    }
}
