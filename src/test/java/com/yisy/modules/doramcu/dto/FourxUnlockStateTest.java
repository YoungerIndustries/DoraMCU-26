package com.yisy.modules.doramcu.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxUnlockState Tests")
class FourxUnlockStateTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("All fields initialized to defaults")
        void defaults() {
            FourxUnlockState state = new FourxUnlockState();
            assertFalse(state.isExplorationUnlocked());
            assertFalse(state.isDiplomacyUnlocked());
            assertFalse(state.isEconomyUnlocked());
            assertFalse(state.isCombatUnlocked());
            assertEquals(0, state.getSystemsDiscovered());
            assertEquals(0, state.getNpcContacts());
            assertEquals(0, state.getColoniesEstablished());
            assertEquals(0, state.getBattlesFought());
        }
    }

    @Nested
    @DisplayName("Thresholds")
    class Thresholds {

        @Test
        @DisplayName("Exploration threshold is 5")
        void explorationThreshold() {
            assertEquals(5, FourxUnlockState.EXPLORATION_THRESHOLD);
        }

        @Test
        @DisplayName("Diplomacy threshold is 1")
        void diplomacyThreshold() {
            assertEquals(1, FourxUnlockState.DIPLOMACY_THRESHOLD);
        }

        @Test
        @DisplayName("Economy threshold is 3")
        void economyThreshold() {
            assertEquals(3, FourxUnlockState.ECONOMY_THRESHOLD);
        }

        @Test
        @DisplayName("Combat threshold is 1")
        void combatThreshold() {
            assertEquals(1, FourxUnlockState.COMBAT_THRESHOLD);
        }
    }

    @Nested
    @DisplayName("Helper methods")
    class HelperMethods {

        @Test
        @DisplayName("isAnyUnlocked returns false when all locked")
        void noneUnlocked() {
            FourxUnlockState state = new FourxUnlockState();
            assertFalse(state.isAnyUnlocked());
        }

        @Test
        @DisplayName("isAnyUnlocked returns true when one unlocked")
        void oneUnlocked() {
            FourxUnlockState state = new FourxUnlockState();
            state.setCombatUnlocked(true);
            assertTrue(state.isAnyUnlocked());
        }

        @Test
        @DisplayName("isAllUnlocked returns false when not all unlocked")
        void notAllUnlocked() {
            FourxUnlockState state = new FourxUnlockState();
            state.setCombatUnlocked(true);
            assertFalse(state.isAllUnlocked());
        }

        @Test
        @DisplayName("isAllUnlocked returns true when all unlocked")
        void allUnlocked() {
            FourxUnlockState state = new FourxUnlockState();
            state.setExplorationUnlocked(true);
            state.setDiplomacyUnlocked(true);
            state.setEconomyUnlocked(true);
            state.setCombatUnlocked(true);
            assertTrue(state.isAllUnlocked());
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Systems discovered")
        void systemsDiscovered() {
            FourxUnlockState state = new FourxUnlockState();
            state.setSystemsDiscovered(10);
            assertEquals(10, state.getSystemsDiscovered());
        }

        @Test
        @DisplayName("NPC contacts")
        void npcContacts() {
            FourxUnlockState state = new FourxUnlockState();
            state.setNpcContacts(5);
            assertEquals(5, state.getNpcContacts());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("Contains field names")
        void containsFields() {
            FourxUnlockState state = new FourxUnlockState();
            String str = state.toString();
            assertTrue(str.contains("exploration="));
            assertTrue(str.contains("diplomacy="));
            assertTrue(str.contains("economy="));
            assertTrue(str.contains("combat="));
        }
    }
}
