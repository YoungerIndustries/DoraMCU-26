package com.yisy.modules.doramcu.fps.core;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FpsSessionTest {

    FpsMap createMap() {
        FpsMap map = new FpsMap("Arena", 100, 100, 20);
        map.getSpawnPoints().add(new FpsMap.SpawnPoint(-40, 0, -40, 0));
        map.getSpawnPoints().add(new FpsMap.SpawnPoint(40, 0, 40, 1));
        map.getCapturePoints().add(new FpsMap.CapturePoint("A", 0, 0, 0, 10));
        return map;
    }

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorInitializesNulls() {
            FpsSession session = new FpsSession();
            assertNull(session.getSessionId());
            assertNull(session.getState());
        }

        @Test
        void fullConstructorSetsInitialState() {
            FpsMap map = createMap();
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, map, 8);
            assertEquals("s1", session.getSessionId());
            assertEquals(FpsSession.SessionState.WAITING, session.getState());
            assertEquals(FpsMatch.GameMode.DEATHMATCH, session.getGameMode());
            assertEquals(map, session.getMap());
            assertEquals(8, session.getMaxPlayers());
            assertEquals(3, session.getAvailableWeapons().size());
            assertEquals(0, session.getCurrentTick());
        }

        @Test
        void fullConstructorAddsDefaultWeapons() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            assertEquals("Pistol", session.getAvailableWeapons().get(0).getName());
            assertEquals("Assault Rifle", session.getAvailableWeapons().get(1).getName());
            assertEquals("Shotgun", session.getAvailableWeapons().get(2).getName());
        }

        @Test
        void fullConstructorSetsRespawnDelay() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            assertEquals(3, session.getRespawnDelaySeconds());
        }
    }

    @Nested
    class PlayerManagementTests {
        @Test
        void addPlayerReturnsTrueWhenSpaceAvailable() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            assertTrue(session.addPlayer("p1", "Hero", 0));
        }

        @Test
        void addPlayerIncrementsCount() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.addPlayer("p2", "Villain", 1);
            assertEquals(2, session.getPlayerCount());
        }

        @Test
        void addPlayerReturnsFalseWhenFull() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 1);
            session.addPlayer("p1", "Hero", 0);
            assertFalse(session.addPlayer("p2", "Villain", 1));
        }

        @Test
        void addPlayerReturnsFalseForDuplicateId() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            assertFalse(session.addPlayer("p1", "Another", 1));
        }

        @Test
        void addPlayerSetsSpawnPosition() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            FpsPlayerState player = session.getPlayers().get("p1");
            assertNotNull(player);
            assertEquals(-40.0, player.getPosX());
        }

        @Test
        void addPlayerSetsDefaultWeapon() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            FpsPlayerState player = session.getPlayers().get("p1");
            // The weapon ID is the UUID of the first available weapon (Pistol)
            assertNotNull(player.getCurrentWeaponId());
            assertEquals(session.getAvailableWeapons().get(0).getId(), player.getCurrentWeaponId());
        }

        @Test
        void removePlayerDecrementsCount() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.removePlayer("p1");
            assertEquals(0, session.getPlayerCount());
        }

        @Test
        void isFullReturnsTrueWhenAtMax() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 1);
            session.addPlayer("p1", "Hero", 0);
            assertTrue(session.isFull());
        }

        @Test
        void isFullReturnsFalseWhenNotAtMax() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            assertFalse(session.isFull());
        }
    }

    @Nested
    class MatchLifecycleTests {
        @Test
        void startMatchSetsStateAndTime() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.startMatch();
            assertEquals(FpsSession.SessionState.IN_PROGRESS, session.getState());
            assertNotNull(session.getStartTime());
        }

        @Test
        void endMatchSetsStateAndEndTime() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.startMatch();
            session.endMatch();
            assertEquals(FpsSession.SessionState.FINISHED, session.getState());
            assertNotNull(session.getEndTime());
        }

        @Test
        void startMatchAddsEvent() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.startMatch();
            assertEquals(1, session.getEvents().size());
            assertEquals(FpsSession.SessionEventType.MATCH_STARTED, session.getEvents().get(0).type());
        }

        @Test
        void endMatchAddsEvent() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.startMatch();
            session.endMatch();
            FpsSession.SessionEvent last = session.getEvents().get(session.getEvents().size() - 1);
            assertEquals(FpsSession.SessionEventType.MATCH_ENDED, last.type());
        }
    }

    @Nested
    class EventTests {
        @Test
        void addPlayerAddsEvent() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            assertEquals(1, session.getEvents().size());
            assertEquals(FpsSession.SessionEventType.PLAYER_JOINED, session.getEvents().get(0).type());
        }

        @Test
        void removePlayerAddsEvent() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.removePlayer("p1");
            FpsSession.SessionEvent last = session.getEvents().get(session.getEvents().size() - 1);
            assertEquals(FpsSession.SessionEventType.PLAYER_LEFT, last.type());
        }

        @Test
        void eventsAreCappedAt100() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            for (int i = 0; i < 110; i++) {
                session.addEvent(new FpsSession.SessionEvent(
                        FpsSession.SessionEventType.PLAYER_JOINED, "p" + i, "Player" + i, null));
            }
            assertEquals(100, session.getEvents().size());
        }
    }

    @Nested
    class RespawnTests {
        @Test
        void scheduleRespawnSetsTimer() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.setCurrentTick(0);
            session.scheduleRespawn("p1");
            assertFalse(session.canRespawn("p1"));
        }

        @Test
        void canRespawnReturnsTrueAfterDelay() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.setCurrentTick(0);
            session.scheduleRespawn("p1");
            session.setCurrentTick(60); // 3 seconds * 20 ticks
            assertTrue(session.canRespawn("p1"));
        }

        @Test
        void respawnPlayerRestoresHealth() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            FpsPlayerState player = session.getPlayers().get("p1");
            player.setHealth(0);
            session.setCurrentTick(100);
            session.scheduleRespawn("p1");
            session.setCurrentTick(200);
            session.respawnPlayer("p1");
            assertTrue(player.isAlive());
            assertEquals(100, player.getHealth());
        }

        @Test
        void respawnPlayerDoesNotRespawnAlivePlayer() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.respawnPlayer("p1");
            FpsPlayerState player = session.getPlayers().get("p1");
            assertEquals(100, player.getHealth()); // unchanged
        }
    }

    @Nested
    class CapturePointTests {
        @Test
        void initCapturePointsLoadsFromMap() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.CAPTURE_THE_POINT, createMap(), 8);
            session.initCapturePoints();
            assertEquals(1, session.getCapturePointStates().size());
            assertNotNull(session.getCapturePointStates().get("A"));
        }

        @Test
        void initCapturePointsInitializesTeamScores() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.CAPTURE_THE_POINT, createMap(), 8);
            session.initCapturePoints();
            assertEquals(0, session.getTeamScores().get(0).intValue());
            assertEquals(0, session.getTeamScores().get(1).intValue());
        }

        @Test
        void capturePointConstantsAreAccessible() {
            assertEquals(10, FpsSession.getCaptureRadius());
            assertEquals(1000, FpsSession.getScoreThreshold());
        }
    }

    @Nested
    class ToMatchRecordTests {
        @Test
        void toMatchRecordIncludesPlayers() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.addPlayer("p1", "Hero", 0);
            session.addPlayer("p2", "Villain", 1);
            session.startMatch();
            session.endMatch();
            FpsMatch match = session.toMatchRecord();
            assertEquals(2, match.getPlayerScores().size());
        }

        @Test
        void toMatchRecordSetsMode() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.TEAM_DEATHMATCH, createMap(), 8);
            session.startMatch();
            session.endMatch();
            FpsMatch match = session.toMatchRecord();
            assertEquals(FpsMatch.GameMode.TEAM_DEATHMATCH, match.getMode());
        }

        @Test
        void toMatchRecordSetsDuration() {
            FpsSession session = new FpsSession("s1", FpsMatch.GameMode.DEATHMATCH, createMap(), 8);
            session.startMatch();
            session.endMatch();
            // Set times after match lifecycle so they are not overwritten
            Instant start = Instant.parse("2026-01-01T00:00:00Z");
            Instant end = Instant.parse("2026-01-01T00:05:00Z");
            session.setStartTime(start);
            session.setEndTime(end);
            FpsMatch match = session.toMatchRecord();
            assertEquals(300, match.getDurationSeconds());
        }
    }

    @Nested
    class SessionEventTests {
        @Test
        void sessionEventRecordCanBeCreated() {
            FpsSession.SessionEvent event = new FpsSession.SessionEvent(
                    FpsSession.SessionEventType.PLAYER_JOINED, "p1", "Hero", null);
            assertEquals(FpsSession.SessionEventType.PLAYER_JOINED, event.type());
            assertEquals("p1", event.playerId());
            assertEquals("Hero", event.playerName());
            assertNull(event.data());
        }

        @Test
        void allSessionEventTypesExist() {
            assertEquals(8, FpsSession.SessionEventType.values().length);
        }
    }

    @Nested
    class CapturePointStateTests {
        @Test
        void capturePointStateRecordCanBeCreated() {
            FpsSession.CapturePointState state = new FpsSession.CapturePointState("A", 0, 50.0);
            assertEquals("A", state.pointId());
            assertEquals(0, state.ownerTeam());
            assertEquals(50.0, state.captureProgress());
        }

        @Test
        void unownedPointHasTeamMinusOne() {
            FpsSession.CapturePointState state = new FpsSession.CapturePointState("A", -1, 0.0);
            assertEquals(-1, state.ownerTeam());
        }
    }

    @Nested
    class GetterSetterTests {
        @Test
        void sessionIdCanBeSet() {
            FpsSession session = new FpsSession();
            session.setSessionId("custom");
            assertEquals("custom", session.getSessionId());
        }

        @Test
        void stateCanBeSet() {
            FpsSession session = new FpsSession();
            session.setState(FpsSession.SessionState.READY);
            assertEquals(FpsSession.SessionState.READY, session.getState());
        }

        @Test
        void gameModeCanBeSet() {
            FpsSession session = new FpsSession();
            session.setGameMode(FpsMatch.GameMode.CAPTURE_THE_POINT);
            assertEquals(FpsMatch.GameMode.CAPTURE_THE_POINT, session.getGameMode());
        }

        @Test
        void mapCanBeSet() {
            FpsSession session = new FpsSession();
            FpsMap map = createMap();
            session.setMap(map);
            assertEquals(map, session.getMap());
        }

        @Test
        void maxPlayersCanBeSet() {
            FpsSession session = new FpsSession();
            session.setMaxPlayers(16);
            assertEquals(16, session.getMaxPlayers());
        }

        @Test
        void respawnDelayCanBeSet() {
            FpsSession session = new FpsSession();
            session.setRespawnDelaySeconds(5);
            assertEquals(5, session.getRespawnDelaySeconds());
        }

        @Test
        void themeCanBeSet() {
            FpsSession session = new FpsSession();
            session.setTheme("SciFi");
            assertEquals("SciFi", session.getTheme());
        }

        @Test
        void currentTickCanBeSet() {
            FpsSession session = new FpsSession();
            session.setCurrentTick(100);
            assertEquals(100, session.getCurrentTick());
        }

        @Test
        void availableWeaponsCanBeReplaced() {
            FpsSession session = new FpsSession();
            java.util.List<FpsWeapon> weapons = new java.util.ArrayList<>();
            weapons.add(new FpsWeapon("Custom", FpsWeapon.WeaponType.PISTOL, 10, 1.0, 1.0, 10));
            session.setAvailableWeapons(weapons);
            assertEquals(1, session.getAvailableWeapons().size());
        }
    }
}
