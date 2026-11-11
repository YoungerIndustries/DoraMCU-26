package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxSessionEntity Tests")
class FourxSessionEntityTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Sets initial fields correctly")
        void setsInitialFields() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test Galaxy", 1L);
            assertEquals("sess1", session.getSessionId());
            assertEquals("Test Galaxy", session.getName());
            assertEquals(1L, session.getCreatedByPlayerId());
            assertEquals(0, session.getCurrentTurn());
            assertEquals("CREATED", session.getStatus());
            assertTrue(session.isExplorationUnlocked());
            assertFalse(session.isExpansionUnlocked());
            assertFalse(session.isExploitationUnlocked());
            assertFalse(session.isExterminationUnlocked());
            assertFalse(session.isDiplomacyUnlocked());
            assertNotNull(session.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Lifecycle methods")
    class Lifecycle {

        @Test
        @DisplayName("Start sets status to ACTIVE")
        void start() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test", 1L);
            session.start();
            assertEquals("ACTIVE", session.getStatus());
            assertNotNull(session.getStartedAt());
        }

        @Test
        @DisplayName("End sets status to ENDED")
        void end() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test", 1L);
            session.end();
            assertEquals("ENDED", session.getStatus());
            assertNotNull(session.getEndedAt());
        }

        @Test
        @DisplayName("Advance turn increments counter")
        void advanceTurn() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test", 1L);
            assertEquals(0, session.getCurrentTurn());
            session.advanceTurn();
            assertEquals(1, session.getCurrentTurn());
            session.advanceTurn();
            session.advanceTurn();
            assertEquals(3, session.getCurrentTurn());
        }
    }

    @Nested
    @DisplayName("Unlock state")
    class UnlockState {

        @Test
        @DisplayName("Get unlock state returns DTO with correct values")
        void getUnlockState() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test", 1L);
            session.setExpansionUnlocked(true);
            session.setDiplomacyUnlocked(true);

            FourxUnlockState state = session.getUnlockState();
            assertTrue(state.isExplorationUnlocked());
            assertTrue(state.isExpansionUnlocked());
            assertFalse(state.isExploitationUnlocked());
            assertFalse(state.isExterminationUnlocked());
            assertTrue(state.isDiplomacyUnlocked());
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Game type and difficulty")
        void gameSettings() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test", 1L);
            session.setGameType("STAR_TREK_4X");
            session.setDifficultyLevel("HARD");
            assertEquals("STAR_TREK_4X", session.getGameType());
            assertEquals("HARD", session.getDifficultyLevel());
        }

        @Test
        @DisplayName("Unlock flags")
        void unlockFlags() {
            FourxSessionEntity session = new FourxSessionEntity("sess1", "Test", 1L);
            session.setExpansionUnlocked(true);
            session.setExploitationUnlocked(true);
            session.setExterminationUnlocked(true);
            session.setDiplomacyUnlocked(true);
            assertTrue(session.isExpansionUnlocked());
            assertTrue(session.isExploitationUnlocked());
            assertTrue(session.isExterminationUnlocked());
            assertTrue(session.isDiplomacyUnlocked());
        }
    }
}
