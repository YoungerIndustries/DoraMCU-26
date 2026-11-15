package com.yisy.modules.doramcu.mechwarrior.engine;

import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechWarriorSession Engine Tests")
class MechWarriorSessionTest {

    private MechWarriorSession session;

    @BeforeEach
    void setUp() {
        session = new MechWarriorSession("session-1", "Test Map", 4);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with correct defaults")
        void constructor_shouldInitializeDefaults() {
            assertEquals("session-1", session.getSessionId());
            assertEquals("Test Map", session.getMapName());
            assertEquals(4, session.getMaxPlayers());
            assertEquals(MechWarriorSession.SessionState.WAITING, session.getState());
            assertEquals(0, session.getCurrentTick());
            assertTrue(session.getStartTime() > 0);
            assertEquals(0, session.getEndTime());
        }

        @Test
        @DisplayName("should start with empty mechs map")
        void constructor_shouldHaveEmptyMechs() {
            assertTrue(session.getAllMechs().isEmpty());
        }

        @Test
        @DisplayName("should have 0 players initially")
        void constructor_shouldHaveZeroPlayers() {
            assertEquals(0, session.getPlayerCount());
        }
    }

    // ---- Session States ----

    @Nested
    @DisplayName("Session State Tests")
    class SessionStateTests {

        @Test
        @DisplayName("should support all 3 session states")
        void enum_shouldSupportAllStates() {
            MechWarriorSession.SessionState[] values = MechWarriorSession.SessionState.values();
            assertEquals(3, values.length);
            assertNotNull(MechWarriorSession.SessionState.WAITING);
            assertNotNull(MechWarriorSession.SessionState.IN_PROGRESS);
            assertNotNull(MechWarriorSession.SessionState.FINISHED);
        }

        @Test
        @DisplayName("should transition from WAITING to IN_PROGRESS")
        void startMatch_shouldTransitionToInProgress() {
            session.startMatch();
            assertEquals(MechWarriorSession.SessionState.IN_PROGRESS, session.getState());
        }

        @Test
        @DisplayName("should transition from IN_PROGRESS to FINISHED")
        void endMatch_shouldTransitionToFinished() {
            session.startMatch();
            session.endMatch();
            assertEquals(MechWarriorSession.SessionState.FINISHED, session.getState());
        }

        @Test
        @DisplayName("should end match directly from WAITING")
        void endMatch_shouldWorkFromWaiting() {
            session.endMatch();
            assertEquals(MechWarriorSession.SessionState.FINISHED, session.getState());
        }

        @Test
        @DisplayName("should set end time on endMatch")
        void endMatch_shouldSetEndTime() {
            session.endMatch();
            assertTrue(session.getEndTime() > 0);
        }

        @Test
        @DisplayName("should update start time on startMatch")
        void startMatch_shouldUpdateStartTime() {
            long before = System.currentTimeMillis();
            session.startMatch();
            assertTrue(session.getStartTime() >= before);
        }

        @Test
        @DisplayName("should set state directly")
        void setState_shouldSetState() {
            session.setState(MechWarriorSession.SessionState.FINISHED);
            assertEquals(MechWarriorSession.SessionState.FINISHED, session.getState());
        }
    }

    // ---- Mech Management ----

    @Nested
    @DisplayName("Mech Management Tests")
    class MechManagementTests {

        @Test
        @DisplayName("should add mech to session")
        void addMech_shouldAddMech() {
            MechWarriorMech mech = new MechWarriorMech(
                    new MechPilot("Pilot 1"),
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100)
            );
            session.addMech("pilot-1", mech);
            assertEquals(1, session.getPlayerCount());
        }

        @Test
        @DisplayName("should get mech by pilot ID")
        void getMech_shouldReturnMech() {
            MechPilot pilot = new MechPilot("Pilot 1");
            MechWarriorMech mech = new MechWarriorMech(pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            session.addMech("pilot-1", mech);
            assertEquals(mech, session.getMech("pilot-1"));
        }

        @Test
        @DisplayName("should return null for non-existent mech")
        void getMech_shouldReturnNull() {
            assertNull(session.getMech("non-existent"));
        }

        @Test
        @DisplayName("should return all mechs")
        void getAllMechs_shouldReturnAll() {
            MechWarriorMech m1 = new MechWarriorMech(new MechPilot("Pilot 1"),
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            MechWarriorMech m2 = new MechWarriorMech(new MechPilot("Pilot 2"),
                    new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70));
            session.addMech("pilot-1", m1);
            session.addMech("pilot-2", m2);
            assertEquals(2, session.getAllMechs().size());
        }

        @Test
        @DisplayName("should count players correctly")
        void getPlayerCount_shouldCountCorrectly() {
            assertEquals(0, session.getPlayerCount());
            session.addMech("pilot-1", new MechWarriorMech(new MechPilot("Pilot 1"),
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100)));
            assertEquals(1, session.getPlayerCount());
            session.addMech("pilot-2", new MechWarriorMech(new MechPilot("Pilot 2"),
                    new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70)));
            assertEquals(2, session.getPlayerCount());
        }

        @Test
        @DisplayName("should overwrite mech with same pilot ID")
        void addMech_shouldOverwriteSameId() {
            MechWarriorMech m1 = new MechWarriorMech(new MechPilot("Pilot 1"),
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            MechWarriorMech m2 = new MechWarriorMech(new MechPilot("Pilot 1 Updated"),
                    new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70));
            session.addMech("pilot-1", m1);
            session.addMech("pilot-1", m2);
            assertEquals(1, session.getPlayerCount());
            assertEquals(m2, session.getMech("pilot-1"));
        }
    }

    // ---- Tick Management ----

    @Nested
    @DisplayName("Tick Management Tests")
    class TickTests {

        @Test
        @DisplayName("should start with tick 0")
        void constructor_shouldHaveTick0() {
            assertEquals(0, session.getCurrentTick());
        }

        @Test
        @DisplayName("should increment tick")
        void incrementTick_shouldIncrement() {
            session.incrementTick();
            assertEquals(1, session.getCurrentTick());
            session.incrementTick();
            assertEquals(2, session.getCurrentTick());
        }

        @Test
        @DisplayName("should increment tick multiple times")
        void incrementTick_shouldIncrementMultipleTimes() {
            for (int i = 0; i < 10; i++) {
                session.incrementTick();
            }
            assertEquals(10, session.getCurrentTick());
        }
    }

    // ---- Session Properties ----

    @Nested
    @DisplayName("Session Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("should return session ID")
        void getSessionId_shouldReturnId() {
            assertEquals("session-1", session.getSessionId());
        }

        @Test
        @DisplayName("should return map name")
        void getMapName_shouldReturnMapName() {
            assertEquals("Test Map", session.getMapName());
        }

        @Test
        @DisplayName("should return max players")
        void getMaxPlayers_shouldReturnMaxPlayers() {
            assertEquals(4, session.getMaxPlayers());
        }

        @Test
        @DisplayName("should create session with different max players")
        void constructor_shouldSetMaxPlayers() {
            MechWarriorSession s = new MechWarriorSession("s2", "Map 2", 8);
            assertEquals(8, s.getMaxPlayers());
        }
    }
}
