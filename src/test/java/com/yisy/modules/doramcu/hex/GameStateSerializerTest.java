package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link GameStateSerializer} serialization and deserialization.
 * Verifies that game state can be saved and restored correctly.
 */
class GameStateSerializerTest {

    private final GameStateSerializer serializer = new GameStateSerializer();

    // ---- Serialize Tests ----

    @Nested
    @DisplayName("Serialize")
    class SerializeTests {

        @Test
        @DisplayName("Should serialize empty game")
        void testSerializeEmptyGame() {
            Game game = new Game("Test Game");
            Map<String, Object> state = serializer.serialize(game);

            assertEquals("Test Game", state.get("name"));
            assertEquals(0L, state.get("currentTime"));
            assertEquals(Game.DEFAULT_TURN_DURATION, state.get("turnDuration"));
            assertEquals(0, state.get("currentPlayerIndex"));
            assertEquals(false, state.get("gameEnded"));
            assertEquals(1, state.get("nextPlanetId"));
            assertEquals(1, state.get("nextFleetId"));
            assertEquals(1, state.get("nextShipId"));
            assertEquals(1, state.get("nextBattleId"));
            assertEquals(1, state.get("nextSolarSystemId"));
            @SuppressWarnings("unchecked")
            List<Object> players = (List<Object>) state.get("players");
            @SuppressWarnings("unchecked")
            List<Object> planets = (List<Object>) state.get("planets");
            @SuppressWarnings("unchecked")
            List<Object> fleets = (List<Object>) state.get("fleets");
            assertTrue(players.isEmpty());
            assertTrue(planets.isEmpty());
            assertTrue(fleets.isEmpty());
        }

        @Test
        @DisplayName("Should serialize game with players")
        void testSerializeWithPlayers() {
            Game game = new Game("Test Game");
            Player player = new Player(1, "Commander");
            game.addPlayer(player);

            Map<String, Object> state = serializer.serialize(game);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> players = (List<Map<String, Object>>) state.get("players");
            assertEquals(1, players.size());
            assertEquals(1, players.get(0).get("id"));
            assertEquals("Commander", players.get(0).get("name"));
        }

        @Test
        @DisplayName("Should serialize game with planets")
        void testSerializeWithPlanets() {
            Game game = new Game("Test Game");
            Planet planet = new Planet(1, PlanetSize.LARGE, MineralQuality.RICH);
            game.getPlanets().put(1, planet);

            Map<String, Object> state = serializer.serialize(game);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> planets = (List<Map<String, Object>>) state.get("planets");
            assertEquals(1, planets.size());
            assertEquals(1, planets.get(0).get("id"));
            assertEquals("LARGE", planets.get(0).get("size"));
            assertEquals("RICH", planets.get(0).get("mineralQuality"));
        }

        @Test
        @DisplayName("Should serialize game with fleets")
        void testSerializeWithFleets() {
            Game game = new Game("Test Game");
            Player player = new Player(1, "Commander");
            game.addPlayer(player);
            Fleet fleet = new Fleet(1, "Alpha Fleet");
            fleet.setOwner(player);
            game.getFleets().put(1, fleet);
            player.addFleet(1);

            Map<String, Object> state = serializer.serialize(game);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fleets = (List<Map<String, Object>>) state.get("fleets");
            assertEquals(1, fleets.size());
            assertEquals(1, fleets.get(0).get("id"));
            assertEquals("Alpha Fleet", fleets.get(0).get("name"));
            assertEquals(1, fleets.get(0).get("ownerId"));
        }

        @Test
        @DisplayName("Should serialize fleet position")
        void testSerializeFleetPosition() {
            Game game = new Game("Test Game");
            Player player = new Player(1, "Commander");
            game.addPlayer(player);
            Fleet fleet = new Fleet(1, "Alpha Fleet");
            fleet.setOwner(player);
            fleet.setPosition(new Hex3D(5, 3));
            game.getFleets().put(1, fleet);

            Map<String, Object> state = serializer.serialize(game);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fleets = (List<Map<String, Object>>) state.get("fleets");
            Map<String, Object> fleetState = fleets.get(0);
            assertEquals(5, fleetState.get("positionX"));
            assertEquals(3, fleetState.get("positionY"));
            assertEquals(-8, fleetState.get("positionZ"));
        }
    }

    // ---- Deserialize Tests ----

    @Nested
    @DisplayName("Deserialize")
    class DeserializeTests {

        @Test
        @DisplayName("Should deserialize empty state")
        void testDeserializeEmptyState() {
            Game game = new Game("Test Game");
            Map<String, Object> state = new java.util.HashMap<>();
            state.put("name", "Test Game");
            state.put("currentTime", 0L);
            state.put("turnDuration", Game.DEFAULT_TURN_DURATION);
            state.put("currentPlayerIndex", 0);
            state.put("gameEnded", false);
            state.put("nextPlanetId", 1);
            state.put("nextFleetId", 1);
            state.put("nextShipId", 1);
            state.put("nextBattleId", 1);
            state.put("nextSolarSystemId", 1);
            state.put("players", List.of());
            state.put("planets", List.of());
            state.put("fleets", List.of());
            serializer.deserialize(game, state);

            assertEquals(0, game.getPlayerCount());
            assertTrue(game.getPlanets().isEmpty());
            assertTrue(game.getFleets().isEmpty());
        }

        @Test
        @DisplayName("Should deserialize game time")
        void testDeserializeGameTime() {
            Game game = new Game("Test Game");
            Map<String, Object> state = Map.of(
                    "currentTime", 300L,
                    "turnDuration", 60L,
                    "currentPlayerIndex", 2,
                    "gameEnded", true
            );
            serializer.deserialize(game, state);

            assertEquals(300L, game.getCurrentTime());
            assertEquals(60L, game.getTurnDuration());
            assertEquals(2, game.getCurrentPlayerIndex());
            assertTrue(game.isGameEnded());
        }

        @Test
        @DisplayName("Should deserialize ID counters")
        void testDeserializeIdCounters() {
            Game game = new Game("Test Game");
            Map<String, Object> state = Map.of(
                    "nextPlanetId", 10,
                    "nextFleetId", 20,
                    "nextShipId", 30,
                    "nextBattleId", 40,
                    "nextSolarSystemId", 50
            );
            serializer.deserialize(game, state);

            assertEquals(10, game.getCurrentNextPlanetId());
            assertEquals(20, game.getCurrentNextFleetId());
            assertEquals(30, game.getCurrentNextShipId());
            assertEquals(40, game.getCurrentNextBattleId());
            assertEquals(50, game.getCurrentNextSolarSystemId());
        }

        @Test
        @DisplayName("Should deserialize players")
        void testDeserializePlayers() {
            Game game = new Game("Test Game");
            Map<String, Object> playerState = Map.of(
                    "id", 1,
                    "name", "Commander"
            );
            Map<String, Object> state = Map.of(
                    "players", List.of(playerState)
            );
            serializer.deserialize(game, state);

            assertEquals(1, game.getPlayerCount());
            Player player = game.getPlayer(1);
            assertNotNull(player);
            assertEquals("Commander", player.getEmpireName());
        }

        @Test
        @DisplayName("Should deserialize planets")
        void testDeserializePlanets() {
            Game game = new Game("Test Game");
            Map<String, Object> planetState = Map.of(
                    "id", 1,
                    "name", "Alpha",
                    "size", "LARGE",
                    "mineralQuality", "RICH"
            );
            Map<String, Object> state = Map.of(
                    "planets", List.of(planetState)
            );
            serializer.deserialize(game, state);

            Planet planet = game.getPlanet(1);
            assertNotNull(planet);
            assertEquals(PlanetSize.LARGE, planet.getPlanetSize());
            assertEquals(MineralQuality.RICH, planet.getMineralQuality());
        }

        @Test
        @DisplayName("Should deserialize planets with default values")
        void testDeserializePlanetsDefaults() {
            Game game = new Game("Test Game");
            Map<String, Object> planetState = new java.util.HashMap<>();
            planetState.put("id", 1);
            planetState.put("size", null);
            planetState.put("mineralQuality", null);
            Map<String, Object> state = Map.of(
                    "planets", List.of(planetState)
            );
            serializer.deserialize(game, state);

            Planet planet = game.getPlanet(1);
            assertNotNull(planet);
            assertEquals(PlanetSize.MEDIUM, planet.getPlanetSize());
            assertEquals(MineralQuality.POOR, planet.getMineralQuality());
        }

        @Test
        @DisplayName("Should deserialize fleets")
        void testDeserializeFleets() {
            Game game = new Game("Test Game");
            Map<String, Object> playerState = Map.of("id", 1, "name", "Commander");
            Map<String, Object> fleetState = Map.of(
                    "id", 1,
                    "name", "Alpha Fleet",
                    "ownerId", 1
            );
            Map<String, Object> state = Map.of(
                    "players", List.of(playerState),
                    "fleets", List.of(fleetState)
            );
            serializer.deserialize(game, state);

            Fleet fleet = game.getFleet(1);
            assertNotNull(fleet);
            assertEquals("Alpha Fleet", fleet.getName());
            // Owner is tracked via player.addFleet, not Fleet.setOwner
            Player player = game.getPlayer(1);
            assertNotNull(player);
            assertTrue(player.getOwnedFleetIds().contains(1));
        }

        @Test
        @DisplayName("Should deserialize fleet position")
        void testDeserializeFleetPosition() {
            Game game = new Game("Test Game");
            Map<String, Object> fleetState = new java.util.HashMap<>();
            fleetState.put("id", 1);
            fleetState.put("name", "Alpha Fleet");
            fleetState.put("positionX", 5);
            fleetState.put("positionY", 3);
            fleetState.put("positionZ", -8);
            Map<String, Object> state = Map.of(
                    "fleets", List.of(fleetState)
            );
            serializer.deserialize(game, state);

            Fleet fleet = game.getFleet(1);
            assertNotNull(fleet.getPosition());
            assertEquals(5, fleet.getPosition().getX());
            assertEquals(3, fleet.getPosition().getY());
            assertEquals(-8, fleet.getPosition().getZ());
        }

        @Test
        @DisplayName("Should skip missing ships gracefully")
        void testDeserializeFleetMissingShips() {
            Game game = new Game("Test Game");
            Map<String, Object> fleetState = new java.util.HashMap<>();
            fleetState.put("id", 1);
            fleetState.put("name", "Alpha Fleet");
            fleetState.put("shipIds", List.of(999, 1000));
            Map<String, Object> state = Map.of(
                    "fleets", List.of(fleetState)
            );
            serializer.deserialize(game, state);

            Fleet fleet = game.getFleet(1);
            assertNotNull(fleet);
            assertEquals(0, fleet.getShipCount());
        }

        @Test
        @DisplayName("Should handle null fleet position")
        void testDeserializeFleetNullPosition() {
            Game game = new Game("Test Game");
            Map<String, Object> fleetState = new java.util.HashMap<>();
            fleetState.put("id", 1);
            fleetState.put("name", "Alpha Fleet");
            Map<String, Object> state = Map.of(
                    "fleets", List.of(fleetState)
            );
            serializer.deserialize(game, state);

            Fleet fleet = game.getFleet(1);
            assertNotNull(fleet);
            assertNull(fleet.getPosition());
        }

        @Test
        @DisplayName("Should handle null player list")
        void testDeserializeNullPlayers() {
            Game game = new Game("Test Game");
            Map<String, Object> state = new java.util.HashMap<>();
            state.put("players", null);
            serializer.deserialize(game, state);
            assertEquals(0, game.getPlayerCount());
        }

        @Test
        @DisplayName("Should handle null planet list")
        void testDeserializeNullPlanets() {
            Game game = new Game("Test Game");
            Map<String, Object> state = new java.util.HashMap<>();
            state.put("planets", null);
            serializer.deserialize(game, state);
            assertTrue(game.getPlanets().isEmpty());
        }

        @Test
        @DisplayName("Should handle null fleet list")
        void testDeserializeNullFleets() {
            Game game = new Game("Test Game");
            Map<String, Object> state = new java.util.HashMap<>();
            state.put("fleets", null);
            serializer.deserialize(game, state);
            assertTrue(game.getFleets().isEmpty());
        }

        @Test
        @DisplayName("Should handle empty state map")
        void testDeserializeEmptyMap() {
            Game game = new Game("Test Game");
            serializer.deserialize(game, Map.of());
            assertEquals(0, game.getPlayerCount());
            assertTrue(game.getPlanets().isEmpty());
            assertTrue(game.getFleets().isEmpty());
        }
    }

    // ---- Round-trip Tests ----

    @Nested
    @DisplayName("Round-trip Serialize/Deserialize")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip empty game")
        void testRoundTripEmptyGame() {
            Game original = new Game("Test Game");
            Map<String, Object> state = serializer.serialize(original);

            Game restored = new Game("Test Game");
            serializer.deserialize(restored, state);

            assertEquals(original.getCurrentTime(), restored.getCurrentTime());
            assertEquals(original.getTurnDuration(), restored.getTurnDuration());
            assertEquals(original.getCurrentPlayerIndex(), restored.getCurrentPlayerIndex());
            assertEquals(original.isGameEnded(), restored.isGameEnded());
            assertEquals(original.getCurrentNextPlanetId(), restored.getCurrentNextPlanetId());
            assertEquals(original.getCurrentNextFleetId(), restored.getCurrentNextFleetId());
        }

        @Test
        @DisplayName("Should round-trip game with players and fleets")
        void testRoundTripWithPlayersAndFleets() {
            Game original = new Game("Test Game");
            Player player = new Player(1, "Commander");
            original.addPlayer(player);

            Fleet fleet = new Fleet(1, "Alpha Fleet");
            fleet.setOwner(player);
            fleet.setPosition(new Hex3D(5, 3));
            original.getFleets().put(1, fleet);

            Map<String, Object> state = serializer.serialize(original);

            Game restored = new Game("Test Game");
            serializer.deserialize(restored, state);

            assertEquals(1, restored.getPlayerCount());
            assertNotNull(restored.getPlayer(1));
            assertEquals(1, restored.getFleets().size());
            Fleet restoredFleet = restored.getFleet(1);
            assertEquals("Alpha Fleet", restoredFleet.getName());
            assertNotNull(restoredFleet.getPosition());
            assertEquals(5, restoredFleet.getPosition().getX());
            assertEquals(3, restoredFleet.getPosition().getY());
            assertEquals(-8, restoredFleet.getPosition().getZ());
        }

        @Test
        @DisplayName("Should round-trip game with planets")
        void testRoundTripWithPlanets() {
            Game original = new Game("Test Game");
            Planet planet = new Planet(1, PlanetSize.LARGE, MineralQuality.RICH);
            original.getPlanets().put(1, planet);

            Map<String, Object> state = serializer.serialize(original);

            Game restored = new Game("Test Game");
            serializer.deserialize(restored, state);

            Planet restoredPlanet = restored.getPlanet(1);
            assertNotNull(restoredPlanet);
            assertEquals(PlanetSize.LARGE, restoredPlanet.getPlanetSize());
            assertEquals(MineralQuality.RICH, restoredPlanet.getMineralQuality());
        }

        @Test
        @DisplayName("Should round-trip game with modified time")
        void testRoundTripWithModifiedTime() {
            Game original = new Game("Test Game");
            original.setCurrentTime(600L);
            original.setTurnDuration(120L);
            original.setCurrentPlayerIndex(3);

            Map<String, Object> state = serializer.serialize(original);

            Game restored = new Game("Test Game");
            serializer.deserialize(restored, state);

            assertEquals(600L, restored.getCurrentTime());
            assertEquals(120L, restored.getTurnDuration());
            assertEquals(3, restored.getCurrentPlayerIndex());
        }

        @Test
        @DisplayName("Should round-trip game with multiple players")
        void testRoundTripMultiplePlayers() {
            Game original = new Game("Test Game");
            original.addPlayer(new Player(1, "Alpha"));
            original.addPlayer(new Player(2, "Beta"));
            original.addPlayer(new Player(3, "Gamma"));

            Map<String, Object> state = serializer.serialize(original);

            Game restored = new Game("Test Game");
            serializer.deserialize(restored, state);

            assertEquals(3, restored.getPlayerCount());
            assertNotNull(restored.getPlayer(1));
            assertNotNull(restored.getPlayer(2));
            assertNotNull(restored.getPlayer(3));
        }
    }

    // ---- Game Integration Tests ----

    @Nested
    @DisplayName("Game Integration")
    class GameIntegrationTests {

        @Test
        @DisplayName("Game.serializeGameState should delegate to serializer")
        void testGameSerializeDelegation() {
            Game game = new Game("Test Game");
            game.addPlayer(new Player(1, "Commander"));

            Map<String, Object> state = game.serializeGameState();
            assertNotNull(state);
            assertEquals("Test Game", state.get("name"));
        }

        @Test
        @DisplayName("Game.deserializeGameState should delegate to serializer")
        void testGameDeserializeDelegation() {
            Game game = new Game("Test Game");
            Map<String, Object> state = Map.of(
                    "players", List.of(Map.of("id", 1, "name", "Commander"))
            );
            game.deserializeGameState(state);

            assertEquals(1, game.getPlayerCount());
        }

        @Test
        @DisplayName("Full round-trip via Game methods")
        void testGameFullRoundTrip() {
            Game original = new Game("Test Game");
            original.addPlayer(new Player(1, "Commander"));
            original.setCurrentTime(300L);
            original.setNextPlanetId(5);

            Map<String, Object> state = original.serializeGameState();

            Game restored = new Game("Test Game");
            restored.deserializeGameState(state);

            assertEquals(300L, restored.getCurrentTime());
            assertEquals(5, restored.getCurrentNextPlanetId());
            assertEquals(1, restored.getPlayerCount());
        }
    }
}
