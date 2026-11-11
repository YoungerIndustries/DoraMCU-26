package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.FourxUnlockState;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.model.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxProgressiveUnlockService Tests")
class FourxProgressiveUnlockServiceTest {

    private FourxProgressiveUnlockService service;
    private GameSession session;

    @BeforeEach
    void setUp() {
        service = new FourxProgressiveUnlockService();
        session = new GameSession("test-session", "Test Game", new Game("Test"));
    }

    @Nested
    @DisplayName("Initial state")
    class InitialState {

        @Test
        @DisplayName("All layers start locked")
        void allLayersLocked() {
            assertFalse(service.isExplorationUnlocked(session));
            assertFalse(service.isDiplomacyUnlocked(session));
            assertFalse(service.isEconomyUnlocked(session));
            assertFalse(service.isCombatUnlocked(session));
        }

        @Test
        @DisplayName("Counters start at zero")
        void countersZero() {
            FourxUnlockState state = service.getUnlockState(session);
            assertEquals(0, state.getSystemsDiscovered());
            assertEquals(0, state.getNpcContacts());
            assertEquals(0, state.getColoniesEstablished());
            assertEquals(0, state.getBattlesFought());
        }

        @Test
        @DisplayName("No layers unlocked initially")
        void noLayersUnlocked() {
            assertFalse(service.isAnyUnlocked(session));
            assertFalse(service.isAllUnlocked(session));
        }
    }

    @Nested
    @DisplayName("Exploration unlock")
    class ExplorationUnlock {

        @Test
        @DisplayName("Unlocks after 5 systems discovered")
        void unlocksAtThreshold() {
            for (int i = 0; i < 4; i++) {
                assertFalse(service.recordSystemDiscovered(session));
            }
            assertFalse(service.isExplorationUnlocked(session));

            assertTrue(service.recordSystemDiscovered(session));
            assertTrue(service.isExplorationUnlocked(session));
        }

        @Test
        @DisplayName("Idempotent — multiple calls after unlock return false")
        void idempotent() {
            for (int i = 0; i < 5; i++) {
                service.recordSystemDiscovered(session);
            }
            assertTrue(service.isExplorationUnlocked(session));

            assertFalse(service.recordSystemDiscovered(session));
            assertFalse(service.recordSystemDiscovered(session));
        }

        @Test
        @DisplayName("Counter increments correctly")
        void counterIncrements() {
            for (int i = 0; i < 7; i++) {
                service.recordSystemDiscovered(session);
            }
            assertEquals(7, service.getUnlockState(session).getSystemsDiscovered());
        }
    }

    @Nested
    @DisplayName("Diplomacy unlock")
    class DiplomacyUnlock {

        @Test
        @DisplayName("Unlocks after 1 NPC contact")
        void unlocksAtThreshold() {
            assertFalse(service.isDiplomacyUnlocked(session));
            assertTrue(service.recordNpcContact(session));
            assertTrue(service.isDiplomacyUnlocked(session));
        }

        @Test
        @DisplayName("Idempotent")
        void idempotent() {
            service.recordNpcContact(session);
            assertTrue(service.isDiplomacyUnlocked(session));
            assertFalse(service.recordNpcContact(session));
        }
    }

    @Nested
    @DisplayName("Economy unlock")
    class EconomyUnlock {

        @Test
        @DisplayName("Unlocks after 3 colonies")
        void unlocksAtThreshold() {
            service.recordColonyEstablished(session);
            service.recordColonyEstablished(session);
            assertFalse(service.isEconomyUnlocked(session));

            assertTrue(service.recordColonyEstablished(session));
            assertTrue(service.isEconomyUnlocked(session));
        }

        @Test
        @DisplayName("Idempotent")
        void idempotent() {
            for (int i = 0; i < 3; i++) {
                service.recordColonyEstablished(session);
            }
            assertFalse(service.recordColonyEstablished(session));
        }
    }

    @Nested
    @DisplayName("Combat unlock")
    class CombatUnlock {

        @Test
        @DisplayName("Unlocks after 1 battle")
        void unlocksAtThreshold() {
            assertFalse(service.isCombatUnlocked(session));
            assertTrue(service.recordBattleFought(session));
            assertTrue(service.isCombatUnlocked(session));
        }

        @Test
        @DisplayName("Idempotent")
        void idempotent() {
            service.recordBattleFought(session);
            assertTrue(service.isCombatUnlocked(session));
            assertFalse(service.recordBattleFought(session));
        }
    }

    @Nested
    @DisplayName("Combined milestones")
    class CombinedMilestones {

        @Test
        @DisplayName("All layers unlock independently")
        void allLayersIndependently() {
            // Unlock combat first
            service.recordBattleFought(session);
            assertTrue(service.isCombatUnlocked(session));
            assertFalse(service.isExplorationUnlocked(session));

            // Unlock diplomacy
            service.recordNpcContact(session);
            assertTrue(service.isDiplomacyUnlocked(session));

            // Unlock economy
            for (int i = 0; i < 3; i++) {
                service.recordColonyEstablished(session);
            }
            assertTrue(service.isEconomyUnlocked(session));

            // Unlock exploration
            for (int i = 0; i < 5; i++) {
                service.recordSystemDiscovered(session);
            }
            assertTrue(service.isExplorationUnlocked(session));

            assertTrue(service.isAllUnlocked(session));
        }

        @Test
        @DisplayName("isAnyUnlocked returns true after first unlock")
        void anyUnlocked() {
            assertFalse(service.isAnyUnlocked(session));
            service.recordBattleFought(session);
            assertTrue(service.isAnyUnlocked(session));
        }
    }
}
