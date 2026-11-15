package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.GameActionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GameReplayServiceTest {

    private GameReplayService service;

    @BeforeEach
    void setUp() {
        service = new GameReplayService();
    }

    // ---- Record Action ----

    @Nested
    class RecordAction {
        @Test
        void recordsAction() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .data("fleetId", 10)
                    .now()
                    .build();
            service.recordAction(log);
            assertEquals(1, service.getLogCount("session1"));
        }

        @Test
        void ignoresNullAction() {
            service.recordAction(null);
            assertEquals(0, service.getLogCount("nonexistent"));
        }

        @Test
        void recordsMultipleActions() {
            for (int i = 0; i < 5; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            assertEquals(5, service.getLogCount("session1"));
        }

        @Test
        void recordsActionsForDifferentSessions() {
            GameActionLog log1 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .now()
                    .build();
            GameActionLog log2 = GameActionLog.builder(GameActionLog.ActionType.BUILD_SHIP, "session2")
                    .playerId(2L)
                    .turnNumber(2)
                    .now()
                    .build();
            service.recordAction(log1);
            service.recordAction(log2);
            assertEquals(1, service.getLogCount("session1"));
            assertEquals(1, service.getLogCount("session2"));
        }

        @Test
        void trimsOldEntriesWhenExceedingMaxSize() {
            // Record MAX_LOG_SIZE_PER_SESSION + 5 entries
            for (int i = 0; i < 10005; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            assertEquals(10000, service.getLogCount("session1"));
            // First entry should be trimmed
            List<GameActionLog> logs = service.getSessionLogs("session1");
            assertEquals(5, logs.get(0).getTurnNumber());
        }
    }

    // ---- Record System Event ----

    @Nested
    class RecordSystemEvent {
        @Test
        void recordsSystemEvent() {
            service.recordSystemEvent("session1", GameActionLog.ActionType.GAME_STARTED, 0,
                    Map.of("reason", "test"));
            assertEquals(1, service.getLogCount("session1"));
            List<GameActionLog> logs = service.getSessionLogs("session1");
            assertEquals(GameActionLog.ActionType.GAME_STARTED, logs.get(0).getActionType());
            assertNull(logs.get(0).getPlayerId());
        }
    }

    // ---- Get Session Logs ----

    @Nested
    class GetSessionLogs {
        @Test
        void returnsEmptyListForUnknownSession() {
            assertTrue(service.getSessionLogs("unknown").isEmpty());
        }

        @Test
        void returnsAllLogsForSession() {
            for (int i = 0; i < 3; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            assertEquals(3, service.getSessionLogs("session1").size());
        }

        @Test
        void returnsImmutableList() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .now()
                    .build();
            service.recordAction(log);
            List<GameActionLog> logs = service.getSessionLogs("session1");
            assertThrows(UnsupportedOperationException.class, () -> logs.add(log));
        }
    }

    // ---- Get Player Logs ----

    @Nested
    class GetPlayerLogs {
        @Test
        void returnsOnlyPlayerActions() {
            GameActionLog log1 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .now()
                    .build();
            GameActionLog log2 = GameActionLog.builder(GameActionLog.ActionType.BUILD_SHIP, "session1")
                    .playerId(2L)
                    .turnNumber(2)
                    .now()
                    .build();
            GameActionLog log3 = GameActionLog.builder(GameActionLog.ActionType.ENGAGE_COMBAT, "session1")
                    .playerId(1L)
                    .turnNumber(3)
                    .now()
                    .build();
            service.recordAction(log1);
            service.recordAction(log2);
            service.recordAction(log3);
            assertEquals(2, service.getPlayerLogs("session1", 1L).size());
            assertEquals(1, service.getPlayerLogs("session1", 2L).size());
        }

        @Test
        void returnsEmptyForUnknownPlayer() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .now()
                    .build();
            service.recordAction(log);
            assertTrue(service.getPlayerLogs("session1", 99L).isEmpty());
        }
    }

    // ---- Get Action Type Logs ----

    @Nested
    class GetActionTypeLogs {
        @Test
        void returnsOnlyMatchingActionType() {
            GameActionLog log1 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .now()
                    .build();
            GameActionLog log2 = GameActionLog.builder(GameActionLog.ActionType.BUILD_SHIP, "session1")
                    .playerId(1L)
                    .turnNumber(2)
                    .now()
                    .build();
            GameActionLog log3 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(3)
                    .now()
                    .build();
            service.recordAction(log1);
            service.recordAction(log2);
            service.recordAction(log3);
            assertEquals(2, service.getActionTypeLogs("session1", GameActionLog.ActionType.MOVE_FLEET).size());
            assertEquals(1, service.getActionTypeLogs("session1", GameActionLog.ActionType.BUILD_SHIP).size());
        }
    }

    // ---- Get Turn Range Logs ----

    @Nested
    class GetTurnRangeLogs {
        @Test
        void returnsLogsInTurnRange() {
            for (int i = 0; i < 10; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            assertEquals(5, service.getTurnRangeLogs("session1", 3, 7).size());
        }

        @Test
        void returnsEmptyForOutOfRange() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .now()
                    .build();
            service.recordAction(log);
            assertTrue(service.getTurnRangeLogs("session1", 10, 20).isEmpty());
        }
    }

    // ---- Get Time Range Logs ----

    @Nested
    class GetTimeRangeLogs {
        @Test
        void returnsLogsInTimeRange() {
            Instant start = Instant.now();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            GameActionLog log1 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .timestamp(start)
                    .build();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            Instant middle = Instant.now();
            GameActionLog log2 = GameActionLog.builder(GameActionLog.ActionType.BUILD_SHIP, "session1")
                    .playerId(1L)
                    .turnNumber(2)
                    .timestamp(middle)
                    .build();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            Instant end = Instant.now();
            GameActionLog log3 = GameActionLog.builder(GameActionLog.ActionType.ENGAGE_COMBAT, "session1")
                    .playerId(1L)
                    .turnNumber(3)
                    .timestamp(end)
                    .build();
            service.recordAction(log1);
            service.recordAction(log2);
            service.recordAction(log3);
            List<GameActionLog> inRange = service.getTimeRangeLogs("session1", middle, end);
            assertTrue(inRange.size() >= 1);
        }
    }

    // ---- Get Recent Logs ----

    @Nested
    class GetRecentLogs {
        @Test
        void returnsLastNLogs() {
            for (int i = 0; i < 10; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            List<GameActionLog> recent = service.getRecentLogs("session1", 3);
            assertEquals(3, recent.size());
            assertEquals(9, recent.get(2).getTurnNumber());
        }

        @Test
        void returnsAllWhenCountExceedsTotal() {
            for (int i = 0; i < 3; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            assertEquals(3, service.getRecentLogs("session1", 100).size());
        }
    }

    // ---- Session Statistics ----

    @Nested
    class SessionStatistics {
        @Test
        void returnsEmptyStatsForUnknownSession() {
            Map<String, Object> stats = service.getSessionStatistics("unknown");
            assertEquals("unknown", stats.get("sessionId"));
            assertEquals(0, stats.get("totalActions"));
        }

        @Test
        void returnsCorrectStats() {
            for (int i = 0; i < 5; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            for (int i = 5; i < 8; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.BUILD_SHIP, "session1")
                        .playerId(2L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            Map<String, Object> stats = service.getSessionStatistics("session1");
            assertEquals(8, stats.get("totalActions"));
            Map<String, Long> byType = (Map<String, Long>) stats.get("actionsByType");
            assertEquals(5L, byType.get("MOVE_FLEET"));
            assertEquals(3L, byType.get("BUILD_SHIP"));
            Map<String, Long> byPlayer = (Map<String, Long>) stats.get("actionsByPlayer");
            assertEquals(5L, byPlayer.get("1"));
            assertEquals(3L, byPlayer.get("2"));
            assertEquals(0, stats.get("minTurn"));
            assertEquals(7, stats.get("maxTurn"));
            assertEquals(8, stats.get("turnCount"));
        }
    }

    // ---- Export/Import ----

    @Nested
    class ExportImport {
        @Test
        void exportsLogsAsMaps() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .data("fleetId", 10)
                    .now()
                    .build();
            service.recordAction(log);
            List<Map<String, Object>> exported = service.exportSessionLogs("session1");
            assertEquals(1, exported.size());
            assertEquals("MOVE_FLEET", exported.get(0).get("actionType"));
            assertEquals(1L, exported.get(0).get("playerId"));
            assertEquals(5, exported.get(0).get("turnNumber"));
        }

        @Test
        void exportsEmptyListForUnknownSession() {
            assertTrue(service.exportSessionLogs("unknown").isEmpty());
        }

        @Test
        void importsLogsFromMaps() {
            List<Map<String, Object>> logMaps = new ArrayList<>();
            Map<String, Object> map1 = new LinkedHashMap<>();
            map1.put("actionType", "MOVE_FLEET");
            map1.put("playerId", 1L);
            map1.put("turnNumber", 5);
            map1.put("timestamp", Instant.now().toString());
            map1.put("data", Map.of("fleetId", 10));
            logMaps.add(map1);

            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("actionType", "BUILD_SHIP");
            map2.put("playerId", 2L);
            map2.put("turnNumber", 6);
            map2.put("timestamp", Instant.now().toString());
            logMaps.add(map2);

            int count = service.importSessionLogs("session1", logMaps);
            assertEquals(2, count);
            assertEquals(2, service.getLogCount("session1"));
        }

        @Test
        void importReturnsZeroForEmptyList() {
            assertEquals(0, service.importSessionLogs("session1", Collections.emptyList()));
        }

        @Test
        void importReturnsZeroForNull() {
            assertEquals(0, service.importSessionLogs("session1", null));
        }

        @Test
        void roundTripExportImport() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .data("fleetId", 10)
                    .now()
                    .build();
            service.recordAction(log);
            List<Map<String, Object>> exported = service.exportSessionLogs("session1");
            GameReplayService newService = new GameReplayService();
            int count = newService.importSessionLogs("session2", exported);
            assertEquals(1, count);
            // The imported log retains the original sessionId from the export ("session1")
            assertEquals(1, newService.getLogCount("session1"));
            List<GameActionLog> imported = newService.getSessionLogs("session1");
            assertEquals(GameActionLog.ActionType.MOVE_FLEET, imported.get(0).getActionType());
            assertEquals(1L, imported.get(0).getPlayerId());
            assertEquals(5, imported.get(0).getTurnNumber());
        }

        @Test
        void importSkipsInvalidEntries() {
            List<Map<String, Object>> logMaps = new ArrayList<>();
            Map<String, Object> invalid = new LinkedHashMap<>();
            invalid.put("actionType", "INVALID_TYPE");
            logMaps.add(invalid);

            Map<String, Object> valid = new LinkedHashMap<>();
            valid.put("actionType", "MOVE_FLEET");
            valid.put("playerId", 1L);
            valid.put("turnNumber", 5);
            valid.put("timestamp", Instant.now().toString());
            logMaps.add(valid);

            int count = service.importSessionLogs("session1", logMaps);
            assertEquals(1, count);
        }
    }

    // ---- Clear Session Logs ----

    @Nested
    class ClearSessionLogs {
        @Test
        void clearsLogsForSession() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(1)
                    .now()
                    .build();
            service.recordAction(log);
            service.clearSessionLogs("session1");
            assertEquals(0, service.getLogCount("session1"));
        }

        @Test
        void clearUnknownSessionDoesNothing() {
            service.clearSessionLogs("unknown");
            assertEquals(0, service.getLogCount("unknown"));
        }
    }

    // ---- List Sessions With Logs ----

    @Nested
    class ListSessionsWithLogs {
        @Test
        void returnsEmptyWhenNoLogs() {
            assertTrue(service.listSessionsWithLogs().isEmpty());
        }

        @Test
        void returnsSessionsWithLogCounts() {
            for (int i = 0; i < 3; i++) {
                GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                        .playerId(1L)
                        .turnNumber(i)
                        .now()
                        .build();
                service.recordAction(log);
            }
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.BUILD_SHIP, "session2")
                    .playerId(2L)
                    .turnNumber(1)
                    .now()
                    .build();
            service.recordAction(log);
            List<Map<String, Object>> sessions = service.listSessionsWithLogs();
            assertEquals(2, sessions.size());
        }
    }

    // ---- GameActionLog Model ----

    @Nested
    class GameActionLogModel {
        @Test
        void builderCreatesLog() {
            Instant now = Instant.now();
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .data("fleetId", 10)
                    .timestamp(now)
                    .build();
            assertEquals(GameActionLog.ActionType.MOVE_FLEET, log.getActionType());
            assertEquals("session1", log.getSessionId());
            assertEquals(1L, log.getPlayerId());
            assertEquals(5, log.getTurnNumber());
            assertEquals(now, log.getTimestamp());
            assertEquals(10, log.getData().get("fleetId"));
        }

        @Test
        void builderDefaultsTimestampToNow() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .build();
            assertNotNull(log.getTimestamp());
        }

        @Test
        void getDataStringReturnsValue() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .data("name", "test")
                    .now()
                    .build();
            assertEquals("test", log.getDataString("name"));
        }

        @Test
        void getDataStringReturnsNullForMissingKey() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .now()
                    .build();
            assertNull(log.getDataString("missing"));
        }

        @Test
        void getDataIntReturnsValue() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .data("count", 42)
                    .now()
                    .build();
            assertEquals(42, log.getDataInt("count", 0));
        }

        @Test
        void getDataIntReturnsDefaultForMissingKey() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .now()
                    .build();
            assertEquals(-1, log.getDataInt("missing", -1));
        }

        @Test
        void dataMapIsImmutable() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .data("key", "value")
                    .now()
                    .build();
            assertThrows(UnsupportedOperationException.class, () -> log.getData().put("new", "val"));
        }

        @Test
        void equalsAndHashCode() {
            GameActionLog log1 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .timestamp(Instant.now())
                    .build();
            GameActionLog log2 = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .timestamp(log1.getTimestamp())
                    .build();
            assertEquals(log1, log2);
            assertEquals(log1.hashCode(), log2.hashCode());
        }

        @Test
        void toStringContainsInfo() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .playerId(1L)
                    .turnNumber(5)
                    .now()
                    .build();
            String str = log.toString();
            assertTrue(str.contains("MOVE_FLEET"));
            assertTrue(str.contains("session1"));
        }

        @Test
        void allActionTypesExist() {
            assertSame(18, GameActionLog.ActionType.values().length);
        }

        @Test
        void builderWithGenericBuilder() {
            GameActionLog log = GameActionLog.builder()
                    .actionType(GameActionLog.ActionType.GAME_STARTED)
                    .sessionId("session1")
                    .turnNumber(0)
                    .now()
                    .build();
            assertEquals(GameActionLog.ActionType.GAME_STARTED, log.getActionType());
            assertEquals("session1", log.getSessionId());
        }

        @Test
        void builderWithDataMap() {
            Map<String, Object> data = new HashMap<>();
            data.put("key1", "value1");
            data.put("key2", 42);
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .data(data)
                    .now()
                    .build();
            assertEquals("value1", log.getData().get("key1"));
            assertEquals(42, log.getData().get("key2"));
        }

        @Test
        void builderWithNullData() {
            GameActionLog log = GameActionLog.builder(GameActionLog.ActionType.MOVE_FLEET, "session1")
                    .data((Map<String, Object>) null)
                    .now()
                    .build();
            assertTrue(log.getData().isEmpty());
        }
    }
}
