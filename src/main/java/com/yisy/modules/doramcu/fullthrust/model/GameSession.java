package com.yisy.modules.doramcu.fullthrust.model;

import java.util.*;

/**
 * Represents a complete game session with all its state.
 */
public class GameSession {
    private String id;
    private String name;
    private int currentTurn;
    private String currentPlayerId; // Whose turn it is
    private boolean started;
    private boolean ended;
    private String winnerId;
    private GameBoard board;
    private Map<String, Player> players;
    private Map<String, Ship> ships;
    private Map<String, ShipClass> shipClasses;
    private List<Missile> activeMissiles;
    private List<String> eventLog;

    public GameSession() {
        this.id = UUID.randomUUID().toString();
        this.currentTurn = 0;
        this.started = false;
        this.ended = false;
        this.board = new GameBoard();
        this.players = new LinkedHashMap<>();
        this.ships = new LinkedHashMap<>();
        this.shipClasses = new LinkedHashMap<>();
        this.activeMissiles = new ArrayList<>();
        this.eventLog = new ArrayList<>();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }
    public String getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(String currentPlayerId) { this.currentPlayerId = currentPlayerId; }
    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }
    public boolean isEnded() { return ended; }
    public void setEnded(boolean ended) { this.ended = ended; }
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    public GameBoard getBoard() { return board; }
    public void setBoard(GameBoard board) { this.board = board; }
    public Map<String, Player> getPlayers() { return players; }
    public void setPlayers(Map<String, Player> players) { this.players = players; }
    public Map<String, Ship> getShips() { return ships; }
    public void setShips(Map<String, Ship> ships) { this.ships = ships; }
    public Map<String, ShipClass> getShipClasses() { return shipClasses; }
    public void setShipClasses(Map<String, ShipClass> shipClasses) { this.shipClasses = shipClasses; }
    public List<Missile> getActiveMissiles() { return activeMissiles; }
    public void setActiveMissiles(List<Missile> activeMissiles) { this.activeMissiles = activeMissiles; }
    public List<String> getEventLog() { return eventLog; }
    public void setEventLog(List<String> eventLog) { this.eventLog = eventLog; }

    /**
     * Add a player to the game
     */
    public void addPlayer(Player player) {
        players.put(player.getId(), player);
        if (!started && players.size() >= 2) {
            // Set first player
            this.currentPlayerId = player.getId();
        }
    }

    /**
     * Add a ship to the game
     */
    public void addShip(Ship ship) {
        ships.put(ship.getId(), ship);
        Player player = players.get(ship.getPlayerId());
        if (player != null) {
            player.addShip(ship.getId());
        }
    }

    /**
     * Add a ship class definition
     */
    public void addShipClass(ShipClass shipClass) {
        shipClasses.put(shipClass.getId(), shipClass);
    }

    /**
     * Log an event
     */
    public void logEvent(String event) {
        eventLog.add("Turn " + currentTurn + ": " + event);
    }

    /**
     * Get all active (non-destroyed) ships
     */
    public List<Ship> getActiveShips() {
        List<Ship> active = new ArrayList<>();
        for (Ship ship : ships.values()) {
            if (!ship.isDestroyed()) {
                active.add(ship);
            }
        }
        return active;
    }

    /**
     * Get ships for a specific player
     */
    public List<Ship> getShipsForPlayer(String playerId) {
        List<Ship> result = new ArrayList<>();
        for (Ship ship : ships.values()) {
            if (ship.getPlayerId().equals(playerId)) {
                result.add(ship);
            }
        }
        return result;
    }

    /**
     * Advance to next player's turn
     */
    public void nextPlayer() {
        List<String> playerIds = new ArrayList<>(players.keySet());
        if (playerIds.isEmpty()) return;

        int currentIndex = playerIds.indexOf(currentPlayerId);
        int nextIndex = (currentIndex + 1) % playerIds.size();
        this.currentPlayerId = playerIds.get(nextIndex);
    }

    /**
     * Advance to next turn
     */
    public void nextTurn() {
        this.currentTurn++;
        List<String> playerIds = new ArrayList<>(players.keySet());
        if (!playerIds.isEmpty()) {
            this.currentPlayerId = playerIds.get(0); // Start new turn with first player
        }
    }

    /**
     * Check if game is over (all but one player eliminated)
     */
    public void checkGameEnd() {
        if (started && !ended) {
            Set<String> activePlayers = new HashSet<>();
            for (Ship ship : ships.values()) {
                if (!ship.isDestroyed()) {
                    activePlayers.add(ship.getPlayerId());
                }
            }

            if (activePlayers.size() <= 1) {
                this.ended = true;
                if (activePlayers.size() == 1) {
                    this.winnerId = activePlayers.iterator().next();
                }
            }
        }
    }

    /**
     * Get the name of the winning player.
     */
    public String getWinnerName() {
        if (winnerId == null) return "";
        Player winner = players.get(winnerId);
        return winner != null ? winner.getName() : "";
    }
}
