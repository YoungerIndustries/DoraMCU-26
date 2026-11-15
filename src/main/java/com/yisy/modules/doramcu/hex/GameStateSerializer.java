package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes Game state to JSON-compatible Maps.
 * <p>
 * Extracted from Game to separate persistence logic from the core game controller.
 * Galaxy/solar system data is NOT serialized here as it is stored separately
 * in the database by GalaxyService.
 * </p>
 */
public class GameStateSerializer {

    /**
     * Serializes the essential game state to a JSON-compatible Map.
     *
     * @param game the game to serialize
     * @return a map representing the serializable game state
     */
    public Map<String, Object> serialize(Game game) {
        Map<String, Object> state = new HashMap<>();
        state.put("name", game.getName());
        state.put("currentTime", game.getCurrentTime());
        state.put("turnDuration", game.getTurnDuration());
        state.put("currentPlayerIndex", game.getCurrentPlayerIndex());
        state.put("gameEnded", game.isGameEnded());
        state.put("nextPlanetId", game.getCurrentNextPlanetId());
        state.put("nextFleetId", game.getCurrentNextFleetId());
        state.put("nextShipId", game.getCurrentNextShipId());
        state.put("nextBattleId", game.getCurrentNextBattleId());
        state.put("nextSolarSystemId", game.getCurrentNextSolarSystemId());

        state.put("players", serializePlayers(game));
        state.put("planets", serializePlanets(game));
        state.put("fleets", serializeFleets(game));

        return state;
    }

    private List<Map<String, Object>> serializePlayers(Game game) {
        List<Map<String, Object>> playerStates = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            Map<String, Object> ps = new HashMap<>();
            ps.put("id", p.getId());
            ps.put("name", p.getName());
            ps.put("resources", new HashMap<String, Integer>()); // Flat resources removed
            ps.put("ownedPlanetIds", java.util.Set.copyOf(p.getOwnedPlanetIds()));
            ps.put("ownedFleetIds", java.util.Set.copyOf(p.getOwnedFleetIds()));
            ps.put("eliminated", p.isEliminated());
            ps.put("homePlanetId", p.getHomePlanetId());
            ps.put("homeSystemId", p.getHomeSystemId());
            ps.put("homeSectorRow", p.getHomeSectorRow());
            ps.put("homeSectorCol", p.getHomeSectorCol());
            ps.put("homeSubSectorRow", p.getHomeSubSectorRow());
            ps.put("homeSubSectorCol", p.getHomeSubSectorCol());
            playerStates.add(ps);
        }
        return playerStates;
    }

    private List<Map<String, Object>> serializePlanets(Game game) {
        List<Map<String, Object>> planetStates = new ArrayList<>();
        for (Planet planet : game.getPlanets().values()) {
            Map<String, Object> pl = new HashMap<>();
            pl.put("id", planet.getId());
            pl.put("name", planet.getName());
            pl.put("size", planet.getPlanetSize().name());
            pl.put("mineralQuality", planet.getMineralQuality().name());
            planetStates.add(pl);
        }
        return planetStates;
    }

    private List<Map<String, Object>> serializeFleets(Game game) {
        List<Map<String, Object>> fleetStates = new ArrayList<>();
        for (Fleet fleet : game.getFleets().values()) {
            Map<String, Object> fl = new HashMap<>();
            fl.put("id", fleet.getId());
            fl.put("name", fleet.getName());
            if (fleet.getOwner() != null) {
                fl.put("ownerId", fleet.getOwner().getId());
            }
            if (fleet.getPosition() != null) {
                fl.put("positionX", fleet.getPosition().getX());
                fl.put("positionY", fleet.getPosition().getY());
                fl.put("positionZ", fleet.getPosition().getZ());
            }
            List<Integer> shipIds = new ArrayList<>();
            for (Ship ship : fleet.getShips()) {
                shipIds.add(ship.getId());
            }
            fl.put("shipIds", shipIds);
            fleetStates.add(fl);
        }
        return fleetStates;
    }

    /**
     * Deserializes a previously serialized game state map and applies it to a new Game instance.
     * <p>
     * This method reconstructs the essential game state: players, planets, and fleets.
     * Galaxy/solar system data is NOT restored here — it must be loaded separately from
     * the database by the calling service (e.g., {@code loadGalaxyToGame}).
     * </p>
     * <p>
     * Ships are referenced by ID only during serialization; during deserialization,
     * ships that no longer exist in the game are skipped gracefully.
     * </p>
     *
     * @param game the new game instance to populate (must have the same name as the serialized state)
     * @param state the serialized game state map
     */
    public void deserialize(Game game, Map<String, Object> state) {
        // Restore top-level game state
        if (state.containsKey("currentTime")) {
            game.setCurrentTime(toLong(state.get("currentTime")));
        }
        if (state.containsKey("turnDuration")) {
            game.setTurnDuration(toLong(state.get("turnDuration")));
        }
        if (state.containsKey("currentPlayerIndex")) {
            game.setCurrentPlayerIndex(toInt(state.get("currentPlayerIndex")));
        }
        if (state.containsKey("gameEnded")) {
            game.setGameEnded(toBoolean(state.get("gameEnded")));
        }
        if (state.containsKey("nextPlanetId")) {
            game.setNextPlanetId(toInt(state.get("nextPlanetId")));
        }
        if (state.containsKey("nextFleetId")) {
            game.setNextFleetId(toInt(state.get("nextFleetId")));
        }
        if (state.containsKey("nextShipId")) {
            game.setNextShipId(toInt(state.get("nextShipId")));
        }
        if (state.containsKey("nextBattleId")) {
            game.setNextBattleId(toInt(state.get("nextBattleId")));
        }
        if (state.containsKey("nextSolarSystemId")) {
            game.setNextSolarSystemId(toInt(state.get("nextSolarSystemId")));
        }

        // Restore players
        Object playersObj = state.get("players");
        if (playersObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> playerStates = (List<Map<String, Object>>) playersObj;
            for (Map<String, Object> ps : playerStates) {
                deserializePlayer(game, ps);
            }
        }

        // Restore planets
        Object planetsObj = state.get("planets");
        if (planetsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> planetStates = (List<Map<String, Object>>) planetsObj;
            for (Map<String, Object> pl : planetStates) {
                deserializePlanet(game, pl);
            }
        }

        // Restore fleets (after players and planets so references are valid)
        Object fleetsObj = state.get("fleets");
        if (fleetsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fleetStates = (List<Map<String, Object>>) fleetsObj;
            for (Map<String, Object> fl : fleetStates) {
                deserializeFleet(game, fl);
            }
        }
    }

    private void deserializePlayer(Game game, Map<String, Object> ps) {
        int id = toInt(ps.get("id"));
        String name = (String) ps.get("name");

        // Find existing player or create a new one
        Player player = game.getPlayer(id);
        if (player == null) {
            // Create player with the serialized name
            player = new Player(id, name != null ? name : "Player" + id);
            game.addPlayer(player);
        }
        // Note: eliminated state and home location are derived from game state
        // (planets, fleets) rather than stored separately
    }

    private void deserializePlanet(Game game, Map<String, Object> pl) {
        int id = toInt(pl.get("id"));
        String name = (String) pl.get("name");
        String sizeStr = (String) pl.get("size");
        String mineralStr = (String) pl.get("mineralQuality");

        // Find existing planet or create a new one directly in the map
        Planet planet = game.getPlanet(id);
        if (planet == null) {
            PlanetSize size = PlanetSize.valueOf(sizeStr != null ? sizeStr : "MEDIUM");
            MineralQuality mineral = MineralQuality.valueOf(mineralStr != null ? mineralStr : "POOR");
            planet = new Planet(id, size, mineral);
            game.getPlanets().put(id, planet);
        }
    }

    private void deserializeFleet(Game game, Map<String, Object> fl) {
        int id = toInt(fl.get("id"));
        String name = (String) fl.get("name");
        Integer ownerId = fl.containsKey("ownerId") ? toInt(fl.get("ownerId")) : null;
        Double positionX = fl.containsKey("positionX") ? toDouble(fl.get("positionX")) : null;
        Double positionY = fl.containsKey("positionY") ? toDouble(fl.get("positionY")) : null;
        Double positionZ = fl.containsKey("positionZ") ? toDouble(fl.get("positionZ")) : null;

        // Find existing fleet or create a new one
        Fleet fleet = game.getFleet(id);
        if (fleet == null) {
            fleet = new Fleet(id, name != null ? name : "Fleet" + id);
            // Add directly to map; owner tracking is handled by the player object
            game.getFleets().put(id, fleet);
            Player owner = ownerId != null ? game.getPlayer(ownerId) : null;
            if (owner != null) {
                owner.addFleet(id);
            }
        }

        // Set position if available (Fleet uses Hex3D for position)
        if (positionX != null && positionY != null && positionZ != null) {
            fleet.setPosition(new Hex3D(positionX.intValue(), positionY.intValue(), positionZ.intValue()));
        }

        // Restore ships (only ships that still exist in the game)
        @SuppressWarnings("unchecked")
        List<Integer> shipIds = (List<Integer>) fl.get("shipIds");
        if (shipIds != null) {
            for (Integer shipId : shipIds) {
                Ship ship = game.getShip(shipId);
                if (ship != null && !fleet.getShips().contains(ship)) {
                    fleet.addShip(ship);
                }
            }
        }
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }

    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
