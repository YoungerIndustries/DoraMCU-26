package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.FpsSession;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsMatchmakingServiceTest {

    FpsMatchmakingService createService() {
        FpsMapService mapService = new FpsMapService();
        FpsGameServerWrapper gameServer = new FpsGameServerWrapper();
        return new FpsMatchmakingService(gameServer, mapService);
    }

    @Nested
    class RoomCreationTests {
        @Test
        void createRoomReturnsRoom() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertNotNull(room);
        }

        @Test
        void createRoomGeneratesId() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertTrue(room.getRoomId().startsWith("fps-"));
        }

        @Test
        void createRoomAddsCreatorAsPlayer() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertEquals(1, room.getPlayerCount());
        }

        @Test
        void createRoomSetsMode() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.TEAM_DEATHMATCH, null, 8);
            assertEquals(FpsMatch.GameMode.TEAM_DEATHMATCH, room.getMode());
        }

        @Test
        void createRoomSetsMaxPlayers() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 16);
            assertEquals(16, room.getMaxPlayers());
        }

        @Test
        void createRoomUsesDefaultMap() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertNotNull(room.getMap());
        }

        @Test
        void createRoomSetsWaitingState() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertEquals(FpsMatchmakingService.RoomState.WAITING, room.getState());
        }

        @Test
        void createRoomSetsServerUrl() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertNotNull(room.getServerUrl());
        }
    }

    @Nested
    class JoinRoomTests {
        @Test
        void joinRoomReturnsRoom() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertTrue(service.joinRoom(room.getRoomId(), "p2", "Villain").isPresent());
        }

        @Test
        void joinRoomIncrementsPlayerCount() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.joinRoom(room.getRoomId(), "p2", "Villain");
            assertEquals(2, room.getPlayerCount());
        }

        @Test
        void joinRoomReturnsEmptyForUnknownRoom() {
            FpsMatchmakingService service = createService();
            assertFalse(service.joinRoom("unknown", "p1", "Hero").isPresent());
        }

        @Test
        void joinRoomReturnsEmptyWhenFull() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 1);
            assertFalse(service.joinRoom(room.getRoomId(), "p2", "Villain").isPresent());
        }

        @Test
        void joinRoomAssignsAlternatingTeams() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.TEAM_DEATHMATCH, null, 8);
            service.joinRoom(room.getRoomId(), "p2", "Villain");
            assertEquals(0, room.getPlayers().get(0).team());
            assertEquals(1, room.getPlayers().get(1).team());
        }
    }

    @Nested
    class LeaveRoomTests {
        @Test
        void leaveRoomDecrementsPlayerCount() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.joinRoom(room.getRoomId(), "p2", "Villain");
            service.leaveRoom(room.getRoomId(), "p2");
            assertEquals(1, room.getPlayerCount());
        }

        @Test
        void leaveRoomRemovesEmptyRoom() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.leaveRoom(room.getRoomId(), "p1");
            assertTrue(service.getActiveRooms().isEmpty());
        }

        @Test
        void leaveNonExistentRoomDoesNotThrow() {
            FpsMatchmakingService service = createService();
            service.leaveRoom("unknown", "p1");
        }
    }

    @Nested
    class ReadyUpTests {
        @Test
        void readyUpSetsPlayerReady() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.joinRoom(room.getRoomId(), "p2", "Villain");
            service.readyUp(room.getRoomId(), "p1");
            assertTrue(room.getPlayers().get(0).ready());
        }

        @Test
        void readyUpStartsMatchWhenAllReady() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.joinRoom(room.getRoomId(), "p2", "Villain");
            service.readyUp(room.getRoomId(), "p1");
            service.readyUp(room.getRoomId(), "p2");
            // Match should start, room removed from matchmaking
            assertFalse(service.getActiveRooms().contains(room));
        }

        @Test
        void readyUpDoesNotStartWithOnePlayer() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.readyUp(room.getRoomId(), "p1");
            // Room should still be active (need at least 2 players)
            assertTrue(service.getActiveRooms().contains(room));
        }
    }

    @Nested
    class RoomQueryTests {
        @Test
        void getRoomReturnsExistingRoom() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertTrue(service.getRoom(room.getRoomId()).isPresent());
        }

        @Test
        void getRoomReturnsEmptyForUnknown() {
            FpsMatchmakingService service = createService();
            assertFalse(service.getRoom("unknown").isPresent());
        }

        @Test
        void getActiveRoomsReturnsAllRooms() {
            FpsMatchmakingService service = createService();
            service.createRoom("p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.createRoom("p2", "Villain", FpsMatch.GameMode.TEAM_DEATHMATCH, null, 8);
            assertEquals(2, service.getActiveRooms().size());
        }

        @Test
        void findOpenRoomReturnsMatchingRoom() {
            FpsMatchmakingService service = createService();
            service.createRoom("p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertTrue(service.findOpenRoom(FpsMatch.GameMode.DEATHMATCH).isPresent());
        }

        @Test
        void findOpenRoomReturnsEmptyForNoMatch() {
            FpsMatchmakingService service = createService();
            service.createRoom("p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            assertTrue(service.findOpenRoom(FpsMatch.GameMode.CAPTURE_THE_POINT).isEmpty());
        }

        @Test
        void findOpenRoomSkipsFullRooms() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 1);
            assertTrue(service.findOpenRoom(FpsMatch.GameMode.DEATHMATCH).isEmpty());
        }
    }

    @Nested
    class MatchRecordingTests {
        @Test
        void recordMatchAddsToCompleted() {
            FpsMatchmakingService service = createService();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            service.recordMatch(match);
            assertEquals(1, service.getCompletedMatches().size());
        }

        @Test
        void getCompletedMatchesReturnsNewList() {
            FpsMatchmakingService service = createService();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            service.recordMatch(match);
            java.util.List<FpsMatch> list1 = service.getCompletedMatches();
            java.util.List<FpsMatch> list2 = service.getCompletedMatches();
            assertNotSame(list1, list2);
        }
    }

    @Nested
    class PlayerStatsTests {
        @Test
        void getPlayerStatsReturnsZeroForUnknownPlayer() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.PlayerStats stats = service.getPlayerStats("unknown");
            assertEquals(0, stats.matches());
            assertEquals(0, stats.kills());
            assertEquals(0, stats.deaths());
            assertEquals(0.0, stats.kdRatio());
        }

        @Test
        void getPlayerStatsAggregatesKills() {
            FpsMatchmakingService service = createService();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 3, 5));
            service.recordMatch(match);
            FpsMatchmakingService.PlayerStats stats = service.getPlayerStats("p1");
            assertEquals(1, stats.matches());
            assertEquals(10, stats.kills());
            assertEquals(3, stats.deaths());
            assertEquals(5, stats.assists());
        }

        @Test
        void getPlayerStatsCountsWins() {
            FpsMatchmakingService service = createService();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 3, 5));
            match.setWinner("p1");
            service.recordMatch(match);
            FpsMatchmakingService.PlayerStats stats = service.getPlayerStats("p1");
            assertEquals(1, stats.wins());
        }

        @Test
        void getPlayerStatsCalculatesKDRatio() {
            FpsMatchmakingService service = createService();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 5, 0));
            service.recordMatch(match);
            FpsMatchmakingService.PlayerStats stats = service.getPlayerStats("p1");
            assertEquals(2.0, stats.kdRatio(), 0.01);
        }

        @Test
        void getPlayerStatsKDRatioWithNoDeaths() {
            FpsMatchmakingService service = createService();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 0, 0));
            service.recordMatch(match);
            FpsMatchmakingService.PlayerStats stats = service.getPlayerStats("p1");
            assertEquals(10.0, stats.kdRatio(), 0.01);
        }
    }

    @Nested
    class MatchmakingRoomTests {
        @Test
        void roomPlayerRecordCanBeCreated() {
            FpsMatchmakingService.MatchmakingRoom.RoomPlayer player =
                    new FpsMatchmakingService.MatchmakingRoom.RoomPlayer("p1", "Hero", 0, false);
            assertEquals("p1", player.playerId());
            assertEquals("Hero", player.playerName());
            assertEquals(0, player.team());
            assertFalse(player.ready());
        }

        @Test
        void allReadyReturnsFalseWhenNotAllReady() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            service.joinRoom(room.getRoomId(), "p2", "Villain");
            service.readyUp(room.getRoomId(), "p1");
            assertFalse(room.allReady());
        }

        @Test
        void isFullReturnsTrueWhenAtMax() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 1);
            assertTrue(room.isFull());
        }

        @Test
        void setServerUrlChangesUrl() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            room.setServerUrl("ws://custom:9000/ws/fps");
            assertEquals("ws://custom:9000/ws/fps", room.getServerUrl());
        }

        @Test
        void setStateChangesState() {
            FpsMatchmakingService service = createService();
            FpsMatchmakingService.MatchmakingRoom room = service.createRoom(
                    "p1", "Hero", FpsMatch.GameMode.DEATHMATCH, null, 8);
            room.setState(FpsMatchmakingService.RoomState.FINISHED);
            assertEquals(FpsMatchmakingService.RoomState.FINISHED, room.getState());
        }
    }

    @Nested
    class PlayerStatsRecordTests {
        @Test
        void playerStatsRecordCanBeCreated() {
            FpsMatchmakingService.PlayerStats stats = new FpsMatchmakingService.PlayerStats(
                    10, 100, 50, 20, 7, 2.0);
            assertEquals(10, stats.matches());
            assertEquals(100, stats.kills());
            assertEquals(50, stats.deaths());
            assertEquals(20, stats.assists());
            assertEquals(7, stats.wins());
            assertEquals(2.0, stats.kdRatio());
        }
    }

    @Nested
    class RoomStateTests {
        @Test
        void allRoomStatesExist() {
            assertEquals(3, FpsMatchmakingService.RoomState.values().length);
        }
    }
}
