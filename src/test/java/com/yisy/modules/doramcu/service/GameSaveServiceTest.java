package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PersistedGameSessionRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for GameSaveService.
 * Covers export, import, validation, and listing functionality.
 */
@ExtendWith(MockitoExtension.class)
class GameSaveServiceTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private GalaxyRepository galaxyRepository;

    @Mock
    private SolarSystemRepository solarSystemRepository;

    @Mock
    private PersistedGameSessionRepository persistedGameSessionRepository;

    @Mock
    private GameSession gameSession;

    @Mock
    private Game game;

    private GameSaveService gameSaveService;

    @BeforeEach
    void setUp() {
        gameSaveService = new GameSaveService(
                gameSessionService,
                galaxyRepository,
                solarSystemRepository,
                persistedGameSessionRepository
        );
    }

    // ---- Export Tests ----

    @Nested
    @DisplayName("Export Session")
    class ExportTests {

        @Test
        @DisplayName("Exports session with all metadata")
        void exportSessionReturnsFullSaveFile() {
            String sessionId = "test-session-123";
            String sessionName = "Test Galaxy";

            when(gameSessionService.getSession(sessionId)).thenReturn(gameSession);
            when(gameSession.getName()).thenReturn(sessionName);
            when(gameSession.getGame()).thenReturn(game);
            when(game.serializeGameState()).thenReturn(Map.of(
                    "players", List.of(),
                    "planets", List.of(),
                    "fleets", List.of()
            ));
            when(game.getPlayers()).thenReturn(List.of());
            when(game.getCurrentTime()).thenReturn(5L);
            when(game.getCurrentPlayerIndex()).thenReturn(1);

            Map<String, Object> saveFile = gameSaveService.exportSession(sessionId);

            assertNotNull(saveFile);
            assertEquals("1.0", saveFile.get("version"));
            assertEquals(sessionName, saveFile.get("sessionName"));
            assertEquals(sessionId, saveFile.get("sessionId"));
            assertEquals(0, saveFile.get("playerCount"));
            assertEquals(5L, saveFile.get("currentTurn"));
            assertEquals(1, saveFile.get("currentPlayerIndex"));
            assertNotNull(saveFile.get("timestamp"));
            assertNotNull(saveFile.get("gameState"));
        }

        @Test
        @DisplayName("Throws when session not found")
        void exportSessionThrowsWhenNotFound() {
            when(gameSessionService.getSession("nonexistent")).thenReturn(null);

            assertThrows(NoSuchElementException.class, () ->
                    gameSaveService.exportSession("nonexistent"));
        }

        @Test
        @DisplayName("Throws when session has no game")
        void exportSessionThrowsWhenNoGame() {
            when(gameSessionService.getSession("no-game")).thenReturn(gameSession);
            when(gameSession.getGame()).thenReturn(null);

            assertThrows(IllegalStateException.class, () ->
                    gameSaveService.exportSession("no-game"));
        }

        @Test
        @DisplayName("Includes galaxy name when available")
        void exportSessionIncludesGalaxyName() {
            String sessionId = "galaxy-session";
            String sessionName = "Traveller Galaxy";

            when(gameSessionService.getSession(sessionId)).thenReturn(gameSession);
            when(gameSession.getName()).thenReturn(sessionName);
            when(gameSession.getGame()).thenReturn(game);
            when(game.serializeGameState()).thenReturn(Map.of(
                    "players", List.of(),
                    "planets", List.of(),
                    "fleets", List.of()
            ));
            when(game.getPlayers()).thenReturn(List.of());
            when(game.getCurrentTime()).thenReturn(0L);
            when(game.getCurrentPlayerIndex()).thenReturn(0);

            // Mock galaxy
            com.yisy.modules.doramcu.hex.Galaxy hexGalaxy = mock(com.yisy.modules.doramcu.hex.Galaxy.class);
            when(hexGalaxy.getName()).thenReturn("Core World");
            when(game.getGalaxy(0)).thenReturn(hexGalaxy);

            Map<String, Object> saveFile = gameSaveService.exportSession(sessionId);

            assertEquals("Core World", saveFile.get("galaxyName"));
        }

        @Test
        @DisplayName("Handles missing galaxy gracefully")
        void exportSessionHandlesMissingGalaxy() {
            String sessionId = "no-galaxy-session";

            when(gameSessionService.getSession(sessionId)).thenReturn(gameSession);
            when(gameSession.getName()).thenReturn("No Galaxy");
            when(gameSession.getGame()).thenReturn(game);
            when(game.serializeGameState()).thenReturn(Map.of(
                    "players", List.of(),
                    "planets", List.of(),
                    "fleets", List.of()
            ));
            when(game.getPlayers()).thenReturn(List.of());
            when(game.getCurrentTime()).thenReturn(0L);
            when(game.getCurrentPlayerIndex()).thenReturn(0);
            when(game.getGalaxy(0)).thenReturn(null);

            Map<String, Object> saveFile = gameSaveService.exportSession(sessionId);

            assertNotNull(saveFile);
            assertNull(saveFile.get("galaxyName"));
        }
    }

    // ---- Import Tests ----

    @Nested
    @DisplayName("Import Session")
    class ImportTests {

        private Map<String, Object> createValidSaveFile() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Imported Game");
            saveFile.put("timestamp", "2026-01-01T00:00:00Z");
            saveFile.put("playerCount", 2);
            saveFile.put("currentTurn", 10L);
            saveFile.put("currentPlayerIndex", 0);

            Map<String, Object> gameState = new LinkedHashMap<>();
            gameState.put("players", List.of(
                    Map.of("id", 1L, "name", "Player1", "resources", Map.of()),
                    Map.of("id", 2L, "name", "Player2", "resources", Map.of())
            ));
            gameState.put("planets", List.of());
            gameState.put("fleets", List.of());
            gameState.put("name", "Imported Game");
            gameState.put("currentTime", 10L);
            gameState.put("turnDuration", 60L);
            gameState.put("currentPlayerIndex", 0);
            gameState.put("gameEnded", false);
            gameState.put("nextPlanetId", 1);
            gameState.put("nextFleetId", 1);
            gameState.put("nextShipId", 1);
            gameState.put("nextBattleId", 1);
            gameState.put("nextSolarSystemId", 1);

            saveFile.put("gameState", gameState);
            return saveFile;
        }

        @Test
        @DisplayName("Imports valid save file")
        void importSessionReturnsNewSessionId() {
            Map<String, Object> saveFile = createValidSaveFile();

            String newSessionId = gameSaveService.importSession(saveFile);

            assertNotNull(newSessionId);
            assertFalse(newSessionId.isBlank());
            // UUID format check
            assertEquals(36, newSessionId.length());
        }

        @Test
        @DisplayName("Throws on empty save file")
        void importSessionThrowsOnEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    gameSaveService.importSession(Map.of()));
        }

        @Test
        @DisplayName("Throws on null save file")
        void importSessionThrowsOnNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    gameSaveService.importSession(null));
        }

        @Test
        @DisplayName("Throws on missing version")
        void importSessionThrowsOnMissingVersion() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("gameState", Map.of("players", List.of(), "fleets", List.of()));

            assertThrows(IllegalArgumentException.class, () ->
                    gameSaveService.importSession(saveFile));
        }

        @Test
        @DisplayName("Throws on missing game state")
        void importSessionThrowsOnMissingGameState() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");

            assertThrows(IllegalArgumentException.class, () ->
                    gameSaveService.importSession(saveFile));
        }

        @Test
        @DisplayName("Uses default name when session name is missing")
        void importSessionUsesDefaultName() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");

            Map<String, Object> gameState = new LinkedHashMap<>();
            gameState.put("players", List.of());
            gameState.put("planets", List.of());
            gameState.put("fleets", List.of());
            gameState.put("name", "Default Game");
            gameState.put("currentTime", 0L);
            gameState.put("turnDuration", 60L);
            gameState.put("currentPlayerIndex", 0);
            gameState.put("gameEnded", false);
            gameState.put("nextPlanetId", 1);
            gameState.put("nextFleetId", 1);
            gameState.put("nextShipId", 1);
            gameState.put("nextBattleId", 1);
            gameState.put("nextSolarSystemId", 1);

            saveFile.put("gameState", gameState);

            String newSessionId = gameSaveService.importSession(saveFile);
            assertNotNull(newSessionId);
        }

        @Test
        @DisplayName("Warns on version mismatch but still imports")
        void importSessionWarnsOnVersionMismatch() {
            Map<String, Object> saveFile = createValidSaveFile();
            saveFile.put("version", "0.9");

            String newSessionId = gameSaveService.importSession(saveFile);
            assertNotNull(newSessionId);
        }
    }

    // ---- Validation Tests ----

    @Nested
    @DisplayName("Validate Save File")
    class ValidateTests {

        @Test
        @DisplayName("Validates correct save file")
        void validateSaveFileReturnsValid() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Test");
            saveFile.put("gameState", Map.of(
                    "players", List.of(),
                    "fleets", List.of()
            ));

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            assertTrue((Boolean) result.get("valid"));
            assertTrue(((List<?>) result.get("errors")).isEmpty());
            assertNotNull(result.get("summary"));
        }

        @Test
        @DisplayName("Detects missing version")
        void validateSaveFileDetectsMissingVersion() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("gameState", Map.of("players", List.of(), "fleets", List.of()));

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            assertFalse((Boolean) result.get("valid"));
            List<String> errors = (List<String>) result.get("errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("version")));
        }

        @Test
        @DisplayName("Detects missing game state")
        void validateSaveFileDetectsMissingGameState() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Test");

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            assertFalse((Boolean) result.get("valid"));
            List<String> errors = (List<String>) result.get("errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("game state")));
        }

        @Test
        @DisplayName("Detects missing players in game state")
        void validateSaveFileDetectsMissingPlayers() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Test");
            saveFile.put("gameState", Map.of("fleets", List.of()));

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            assertFalse((Boolean) result.get("valid"));
            List<String> errors = (List<String>) result.get("errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("players")));
        }

        @Test
        @DisplayName("Detects missing fleets in game state")
        void validateSaveFileDetectsMissingFleets() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Test");
            saveFile.put("gameState", Map.of("players", List.of()));

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            assertFalse((Boolean) result.get("valid"));
            List<String> errors = (List<String>) result.get("errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("fleets")));
        }

        @Test
        @DisplayName("Returns invalid for null save file")
        void validateSaveFileReturnsInvalidForNull() {
            Map<String, Object> result = gameSaveService.validateSaveFile(null);

            assertFalse((Boolean) result.get("valid"));
        }

        @Test
        @DisplayName("Returns invalid for empty save file")
        void validateSaveFileReturnsInvalidForEmpty() {
            Map<String, Object> result = gameSaveService.validateSaveFile(Map.of());

            assertFalse((Boolean) result.get("valid"));
        }

        @Test
        @DisplayName("Detects unsupported version")
        void validateSaveFileDetectsUnsupportedVersion() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "99.0");
            saveFile.put("sessionName", "Test");
            saveFile.put("gameState", Map.of("players", List.of(), "fleets", List.of()));

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            assertFalse((Boolean) result.get("valid"));
            List<String> errors = (List<String>) result.get("errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("Unsupported version")));
        }

        @Test
        @DisplayName("Includes summary for valid save file")
        void validateSaveFileIncludesSummary() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Test");
            saveFile.put("timestamp", "2026-01-01T00:00:00Z");
            saveFile.put("galaxyName", "Core World");
            saveFile.put("playerCount", 3);
            saveFile.put("currentTurn", 10L);
            saveFile.put("gameState", Map.of("players", List.of(), "fleets", List.of()));

            Map<String, Object> result = gameSaveService.validateSaveFile(saveFile);

            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) result.get("summary");
            assertNotNull(summary);
            assertEquals("1.0", summary.get("version"));
            assertEquals("Test", summary.get("sessionName"));
            assertEquals("Core World", summary.get("galaxyName"));
            assertEquals(3, summary.get("playerCount"));
        }
    }

    // ---- List Saveable Sessions Tests ----

    @Nested
    @DisplayName("List Saveable Sessions")
    class ListSaveableSessionsTests {

        @Test
        @DisplayName("Returns only sessions with game state")
        void listSaveableSessionsFiltersByGameState() {
            List<Map<String, Object>> allSessions = List.of(
                    Map.of("sessionId", "1", "hasGameState", true),
                    Map.of("sessionId", "2", "hasGameState", false),
                    Map.of("sessionId", "3", "hasGameState", true)
            );
            when(gameSessionService.listPersistedSessions()).thenReturn(allSessions);

            List<Map<String, Object>> saveable = gameSaveService.listSaveableSessions();

            assertEquals(2, saveable.size());
            assertEquals("1", saveable.get(0).get("sessionId"));
            assertEquals("3", saveable.get(1).get("sessionId"));
        }

        @Test
        @DisplayName("Returns empty list when no sessions")
        void listSaveableSessionsReturnsEmpty() {
            when(gameSessionService.listPersistedSessions()).thenReturn(List.of());

            List<Map<String, Object>> saveable = gameSaveService.listSaveableSessions();

            assertTrue(saveable.isEmpty());
        }

        @Test
        @DisplayName("Handles missing hasGameState gracefully")
        void listSaveableSessionsHandlesNullHasGameState() {
            Map<String, Object> session1 = new HashMap<>();
            session1.put("sessionId", "1");
            // hasGameState key not present — simulates null

            Map<String, Object> session2 = Map.of("sessionId", "2", "hasGameState", true);
            List<Map<String, Object>> allSessions = List.of(session1, session2);
            when(gameSessionService.listPersistedSessions()).thenReturn(allSessions);

            List<Map<String, Object>> saveable = gameSaveService.listSaveableSessions();

            assertEquals(1, saveable.size());
            assertEquals("2", saveable.get(0).get("sessionId"));
        }
    }

    // ---- Integration Scenario Tests ----

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Export then validate round-trip")
        void exportThenValidate() {
            String sessionId = "round-trip-session";

            when(gameSessionService.getSession(sessionId)).thenReturn(gameSession);
            when(gameSession.getName()).thenReturn("Round Trip");
            when(gameSession.getGame()).thenReturn(game);
            when(game.serializeGameState()).thenReturn(Map.of(
                    "players", List.of(Map.of("id", 1L, "name", "Player1", "resources", Map.of())),
                    "planets", List.of(),
                    "fleets", List.of()
            ));
            when(game.getPlayers()).thenReturn(List.of(new Player(1, "Player1")));
            when(game.getCurrentTime()).thenReturn(5L);
            when(game.getCurrentPlayerIndex()).thenReturn(0);

            // Export
            Map<String, Object> saveFile = gameSaveService.exportSession(sessionId);

            // Validate the exported file
            Map<String, Object> validation = gameSaveService.validateSaveFile(saveFile);
            assertTrue((Boolean) validation.get("valid"));
        }

        @Test
        @DisplayName("Validate before import")
        void validateBeforeImport() {
            Map<String, Object> saveFile = new LinkedHashMap<>();
            saveFile.put("version", "1.0");
            saveFile.put("sessionName", "Pre-validated");

            Map<String, Object> gameState = new LinkedHashMap<>();
            gameState.put("players", List.of());
            gameState.put("planets", List.of());
            gameState.put("fleets", List.of());
            gameState.put("name", "Pre-validated");
            gameState.put("currentTime", 0L);
            gameState.put("turnDuration", 60L);
            gameState.put("currentPlayerIndex", 0);
            gameState.put("gameEnded", false);
            gameState.put("nextPlanetId", 1);
            gameState.put("nextFleetId", 1);
            gameState.put("nextShipId", 1);
            gameState.put("nextBattleId", 1);
            gameState.put("nextSolarSystemId", 1);

            saveFile.put("gameState", gameState);

            // Validate first
            Map<String, Object> validation = gameSaveService.validateSaveFile(saveFile);
            assertTrue((Boolean) validation.get("valid"));

            // Then import
            String newSessionId = gameSaveService.importSession(saveFile);
            assertNotNull(newSessionId);
        }
    }
}
