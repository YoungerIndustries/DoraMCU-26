package com.yisy.modules.doramcu.fps.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FpsMatchTest {

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorInitializesEmptyScores() {
            FpsMatch match = new FpsMatch();
            assertNotNull(match.getPlayerScores());
            assertTrue(match.getPlayerScores().isEmpty());
            assertNull(match.getId());
            assertNull(match.getMode());
        }

        @Test
        void fullConstructorGeneratesUuid() {
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            assertNotNull(match.getId());
            assertEquals(FpsMatch.GameMode.DEATHMATCH, match.getMode());
            assertEquals("map1", match.getMapId());
            assertEquals("Arena", match.getMapName());
        }

        @Test
        void fullConstructorSetsStartTime() {
            Instant before = Instant.now();
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            Instant after = Instant.now();
            assertNotNull(match.getStartTime());
            assertTrue(match.getStartTime().isAfter(before) || match.getStartTime().equals(before));
            assertTrue(match.getStartTime().isBefore(after) || match.getStartTime().equals(after));
        }
    }

    @Nested
    class GameModeTests {
        @Test
        void allGameModesExist() {
            assertSame(FpsMatch.GameMode.DEATHMATCH, FpsMatch.GameMode.valueOf("DEATHMATCH"));
            assertSame(FpsMatch.GameMode.TEAM_DEATHMATCH, FpsMatch.GameMode.valueOf("TEAM_DEATHMATCH"));
            assertSame(FpsMatch.GameMode.CAPTURE_THE_POINT, FpsMatch.GameMode.valueOf("CAPTURE_THE_POINT"));
        }

        @Test
        void modeCanBeChanged() {
            FpsMatch match = new FpsMatch();
            match.setMode(FpsMatch.GameMode.TEAM_DEATHMATCH);
            assertEquals(FpsMatch.GameMode.TEAM_DEATHMATCH, match.getMode());
        }
    }

    @Nested
    class PlayerScoreTests {
        @Test
        void playerScoreRecordCanBeCreated() {
            FpsMatch.PlayerScore score = new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 3, 5);
            assertEquals("p1", score.playerId());
            assertEquals("Hero", score.playerName());
            assertEquals(0, score.team());
            assertEquals(10, score.kills());
            assertEquals(3, score.deaths());
            assertEquals(5, score.assists());
        }

        @Test
        void playerScoresCanBeAdded() {
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 3, 5));
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p2", "Villain", 1, 8, 5, 2));
            assertEquals(2, match.getPlayerScores().size());
        }

        @Test
        void playerScoresCanBeReplaced() {
            FpsMatch match = new FpsMatch();
            java.util.List<FpsMatch.PlayerScore> scores = new java.util.ArrayList<>();
            scores.add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 3, 5));
            match.setPlayerScores(scores);
            assertEquals(1, match.getPlayerScores().size());
        }
    }

    @Nested
    class MatchInfoTests {
        @Test
        void durationCanBeSet() {
            FpsMatch match = new FpsMatch();
            match.setDurationSeconds(300);
            assertEquals(300, match.getDurationSeconds());
        }

        @Test
        void winnerCanBeSet() {
            FpsMatch match = new FpsMatch();
            match.setWinner("p1");
            assertEquals("p1", match.getWinner());
        }

        @Test
        void startTimeCanBeSet() {
            Instant time = Instant.parse("2026-01-01T00:00:00Z");
            FpsMatch match = new FpsMatch();
            match.setStartTime(time);
            assertEquals(time, match.getStartTime());
        }

        @Test
        void endTimeCanBeSet() {
            Instant time = Instant.parse("2026-01-01T00:05:00Z");
            FpsMatch match = new FpsMatch();
            match.setEndTime(time);
            assertEquals(time, match.getEndTime());
        }

        @Test
        void themeCanBeSet() {
            FpsMatch match = new FpsMatch();
            match.setTheme("SciFi");
            assertEquals("SciFi", match.getTheme());
        }

        @Test
        void mapIdCanBeSet() {
            FpsMatch match = new FpsMatch();
            match.setMapId("custom-map");
            assertEquals("custom-map", match.getMapId());
        }

        @Test
        void mapNameCanBeSet() {
            FpsMatch match = new FpsMatch();
            match.setMapName("Custom Arena");
            assertEquals("Custom Arena", match.getMapName());
        }

        @Test
        void idCanBeSet() {
            FpsMatch match = new FpsMatch();
            match.setId("custom-id");
            assertEquals("custom-id", match.getId());
        }
    }

    @Nested
    class IntegrationTests {
        @Test
        void fullMatchRecordCanBeBuilt() {
            FpsMatch match = new FpsMatch(FpsMatch.GameMode.TEAM_DEATHMATCH, "map1", "Arena");
            match.setTheme("SciFi");
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p1", "Hero", 0, 10, 3, 5));
            match.getPlayerScores().add(new FpsMatch.PlayerScore("p2", "Villain", 1, 8, 5, 2));
            match.setWinner("team:0");
            match.setDurationSeconds(300);

            assertEquals(FpsMatch.GameMode.TEAM_DEATHMATCH, match.getMode());
            assertEquals("SciFi", match.getTheme());
            assertEquals(2, match.getPlayerScores().size());
            assertEquals("team:0", match.getWinner());
            assertEquals(300, match.getDurationSeconds());
        }

        @Test
        void twoMatchesHaveDifferentIds() {
            FpsMatch m1 = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            FpsMatch m2 = new FpsMatch(FpsMatch.GameMode.DEATHMATCH, "map1", "Arena");
            assertNotEquals(m1.getId(), m2.getId());
        }
    }
}
