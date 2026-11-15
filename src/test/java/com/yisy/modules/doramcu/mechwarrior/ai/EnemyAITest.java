package com.yisy.modules.doramcu.mechwarrior.ai;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorGameEngine;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorMech;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorSession;
import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.Mission;
import com.yisy.modules.doramcu.mechwarrior.service.MissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("EnemyAI Service Tests")
@ExtendWith(MockitoExtension.class)
class EnemyAITest {

    @Mock
    private MechWarriorGameEngine gameEngine;

    @Mock
    private MissionService missionService;

    private EnemyAI enemyAI;

    @BeforeEach
    void setUp() {
        enemyAI = new EnemyAI(gameEngine, missionService);
    }

    // ---- Enemy Spawning ----

    @Nested
    @DisplayName("Enemy Spawning Tests")
    class EnemySpawningTests {

        @Test
        @DisplayName("should spawn enemies based on mission enemy count")
        void spawnEnemies_shouldUseMissionEnemyCount() {
            Mission mission = new Mission("Test Mission", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.NORMAL, "Test Faction");
            mission.setEnemyCount(5);

            List<Mission> missions = new ArrayList<>();
            missions.add(mission);
            when(missionService.getAllMissions()).thenReturn(missions);

            List<EnemyPilot> enemies = enemyAI.spawnEnemies("session-1", mission.getId());

            assertEquals(5, enemies.size());
        }

        @Test
        @DisplayName("should cap enemy count at 10")
        void spawnEnemies_shouldCapAt10() {
            Mission mission = new Mission("Test Mission", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.NORMAL, "Test Faction");
            mission.setEnemyCount(20);

            List<Mission> missions = new ArrayList<>();
            missions.add(mission);
            when(missionService.getAllMissions()).thenReturn(missions);

            List<EnemyPilot> enemies = enemyAI.spawnEnemies("session-1", mission.getId());

            assertEquals(10, enemies.size());
        }

        @Test
        @DisplayName("should use default enemy count if mission not found")
        void spawnEnemies_shouldUseDefaultCount() {
            when(missionService.getAllMissions()).thenReturn(new ArrayList<>());

            List<EnemyPilot> enemies = enemyAI.spawnEnemies("session-1", "non-existent");

            assertEquals(3, enemies.size());
        }

        @Test
        @DisplayName("should scale difficulty by mission difficulty")
        void spawnEnemies_shouldScaleDifficulty() {
            Mission easyMission = new Mission("Easy", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.EASY, "Test Faction");
            Mission hardMission = new Mission("Hard", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.HARD, "Test Faction");

            List<Mission> missions = new ArrayList<>();
            missions.add(easyMission);
            missions.add(hardMission);
            when(missionService.getAllMissions()).thenReturn(missions);

            List<EnemyPilot> easyEnemies = enemyAI.spawnEnemies("session-1", easyMission.getId());
            enemyAI.removeSessionEnemies("session-1");

            List<EnemyPilot> hardEnemies = enemyAI.spawnEnemies("session-2", hardMission.getId());

            // Hard enemies should have higher difficulty
            assertTrue(hardEnemies.get(0).getDifficultyLevel() > easyEnemies.get(0).getDifficultyLevel());
        }

        @Test
        @DisplayName("should create enemies with unique IDs")
        void spawnEnemies_shouldCreateUniqueIds() {
            Mission mission = new Mission("Test Mission", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.NORMAL, "Test Faction");
            mission.setEnemyCount(3);

            List<Mission> missions = new ArrayList<>();
            missions.add(mission);
            when(missionService.getAllMissions()).thenReturn(missions);

            List<EnemyPilot> enemies = enemyAI.spawnEnemies("session-1", mission.getId());

            assertNotNull(enemies.get(0).getPilot().getId());
            assertNotEquals(enemies.get(0).getPilot().getId(), enemies.get(1).getPilot().getId());
        }

        @Test
        @DisplayName("should track enemy IDs in session")
        void spawnEnemies_shouldTrackEnemyIds() {
            Mission mission = new Mission("Test Mission", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.NORMAL, "Test Faction");
            mission.setEnemyCount(4);

            List<Mission> missions = new ArrayList<>();
            missions.add(mission);
            when(missionService.getAllMissions()).thenReturn(missions);

            enemyAI.spawnEnemies("session-1", mission.getId());

            List<String> enemyIds = enemyAI.getEnemyIds("session-1");
            assertEquals(4, enemyIds.size());
        }

        @Test
        @DisplayName("should return empty list for non-existent session")
        void getEnemyIds_shouldReturnEmpty() {
            List<String> enemyIds = enemyAI.getEnemyIds("non-existent");
            assertTrue(enemyIds.isEmpty());
        }
    }

    // ---- Enemy Registration ----

    @Nested
    @DisplayName("Enemy Registration Tests")
    class EnemyRegistrationTests {

        @Test
        @DisplayName("should register enemies with game engine")
        void registerEnemies_shouldAddToSession() {
            List<EnemyPilot> enemies = List.of(
                    new EnemyPilot("Enemy 1", EnemyPilot.EnemyRole.ASSAULT, 3),
                    new EnemyPilot("Enemy 2", EnemyPilot.EnemyRole.SCOUT, 2)
            );

            MechWarriorMech mech1 = new MechWarriorMech(
                    new MechPilot("P1"), new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            MechWarriorMech mech2 = new MechWarriorMech(
                    new MechPilot("P2"), new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));

            when(gameEngine.addPilotToSession(eq("session-1"), any(MechPilot.class), any(MechChassis.class)))
                    .thenReturn(mech1, mech2);

            enemyAI.registerEnemies("session-1", enemies);

            verify(gameEngine, times(2)).addPilotToSession(eq("session-1"), any(MechPilot.class), any(MechChassis.class));
        }

        @Test
        @DisplayName("should mount weapons on enemy mechs")
        void registerEnemies_shouldMountWeapons() {
            EnemyPilot enemy = new EnemyPilot("Enemy 1", EnemyPilot.EnemyRole.ASSAULT, 3);
            List<EnemyPilot> enemies = List.of(enemy);

            MechWarriorMech mech = new MechWarriorMech(
                    new MechPilot("P1"), new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));

            when(gameEngine.addPilotToSession(eq("session-1"), any(MechPilot.class), any(MechChassis.class)))
                    .thenReturn(mech);

            enemyAI.registerEnemies("session-1", enemies);

            // Weapons should be mounted on the mech
            assertTrue(mech.getAllWeapons().size() >= enemy.getWeapons().size());
        }

        @Test
        @DisplayName("should set initial positions")
        void registerEnemies_shouldSetPositions() {
            EnemyPilot enemy = new EnemyPilot("Enemy 1", EnemyPilot.EnemyRole.ASSAULT, 3);
            List<EnemyPilot> enemies = List.of(enemy);

            MechWarriorMech mech = new MechWarriorMech(
                    new MechPilot("P1"), new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));

            when(gameEngine.addPilotToSession(eq("session-1"), any(MechPilot.class), any(MechChassis.class)))
                    .thenReturn(mech);

            enemyAI.registerEnemies("session-1", enemies);

            // Position should be set (enemies spawn at x >= 500)
            assertTrue(mech.getPosX() >= 500);
            assertTrue(mech.getPosY() >= 0);
        }
    }

    // ---- AI Tick Processing ----

    @Nested
    @DisplayName("AI Tick Processing Tests")
    class AITickProcessingTests {

        @Test
        @DisplayName("should handle empty session")
        void processAITick_shouldHandleEmptySession() {
            // No stubbing needed - just verify no exception
            enemyAI.processAITick("session-1");
        }

        @Test
        @DisplayName("should handle no enemies")
        void processAITick_shouldHandleNoEnemies() {
            // No stubbing needed - session has no enemies
            enemyAI.processAITick("session-1");
        }

        @Test
        @DisplayName("should process tick for registered enemies")
        void processAITick_shouldProcessEnemies() {
            // Create a player mech
            MechPilot playerPilot = new MechPilot("Player");
            MechChassis playerChassis = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            MechWarriorMech playerMech = new MechWarriorMech(playerPilot, playerChassis);
            playerMech.setPosX(0);
            playerMech.setPosY(0);

            // Create session with player
            MechWarriorSession session = new MechWarriorSession("session-1", "Test Map", 4);
            session.addMech(playerPilot.getId(), playerMech);

            // Spawn enemies (no game engine interaction needed for spawn)
            List<EnemyPilot> enemies = enemyAI.spawnEnemies("session-1", "any-mission-id");

            // Create enemy mechs
            for (EnemyPilot enemy : enemies) {
                MechWarriorMech enemyMech = new MechWarriorMech(enemy.getPilot(), enemy.getChassis());
                enemyMech.setPosX(500);
                enemyMech.setPosY(200);
                for (var weapon : enemy.getWeapons()) {
                    enemyMech.mountWeapon(weapon, com.yisy.modules.doramcu.mechwarrior.model.MechComponent.ComponentLocation.CENTER_TORSO);
                }
                session.addMech(enemy.getPilot().getId(), enemyMech);
            }

            // Stub session retrieval
            when(gameEngine.getSession("session-1")).thenReturn(session);

            // Process tick - should not throw
            enemyAI.processAITick("session-1");
        }
    }

    // ---- Enemy Removal ----

    @Nested
    @DisplayName("Enemy Removal Tests")
    class EnemyRemovalTests {

        @Test
        @DisplayName("should remove enemies from session")
        void removeSessionEnemies_shouldRemoveEnemies() {
            Mission mission = new Mission("Test Mission", Mission.MissionType.ASSAULT,
                    Mission.MissionDifficulty.NORMAL, "Test Faction");
            mission.setEnemyCount(3);

            List<Mission> missions = new ArrayList<>();
            missions.add(mission);
            when(missionService.getAllMissions()).thenReturn(missions);

            enemyAI.spawnEnemies("session-1", mission.getId());
            assertEquals(3, enemyAI.getEnemyIds("session-1").size());

            enemyAI.removeSessionEnemies("session-1");
            assertTrue(enemyAI.getEnemyIds("session-1").isEmpty());
        }

        @Test
        @DisplayName("should handle removing non-existent session")
        void removeSessionEnemies_shouldHandleNonExistent() {
            // Should not throw
            enemyAI.removeSessionEnemies("non-existent");
        }
    }

    // ---- EnemyState ----

    @Nested
    @DisplayName("EnemyState Tests")
    class EnemyStateTests {

        @Test
        @DisplayName("should initialize with null target")
        void constructor_shouldInitializeDefaults() {
            EnemyAI.EnemyState state = new EnemyAI.EnemyState();
            assertNull(state.getTargetPilotId());
            assertEquals(0, state.getBehaviorTick());
        }

        @Test
        @DisplayName("should set and get target pilot ID")
        void setter_shouldSetTargetPilotId() {
            EnemyAI.EnemyState state = new EnemyAI.EnemyState();
            state.setTargetPilotId("player-1");
            assertEquals("player-1", state.getTargetPilotId());
        }

        @Test
        @DisplayName("should set and get behavior tick")
        void setter_shouldSetBehaviorTick() {
            EnemyAI.EnemyState state = new EnemyAI.EnemyState();
            state.setBehaviorTick(5);
            assertEquals(5, state.getBehaviorTick());
        }

        @Test
        @DisplayName("should set and get move direction")
        void setter_shouldSetMoveDirection() {
            EnemyAI.EnemyState state = new EnemyAI.EnemyState();
            state.setMoveDirection(Math.PI / 2);
            assertEquals(Math.PI / 2, state.getMoveDirection(), 0.001);
        }

        @Test
        @DisplayName("should set and get last fire time")
        void setter_shouldSetLastFireTime() {
            EnemyAI.EnemyState state = new EnemyAI.EnemyState();
            long time = System.currentTimeMillis();
            state.setLastFireTime(time);
            assertEquals(time, state.getLastFireTime());
        }
    }
}
