package com.yisy.modules.doramcu.fullthrust.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core game engine service for FullThrust.
 * Manages game sessions, movement, combat, and turn resolution.
 * Sessions are persisted to the database and loaded on startup.
 */
@Service
public class GameService {
    private final PersistenceService persistenceService;
    private final AIService aiService;
    private final PlayerAccountService playerAccountService;
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public GameService(PersistenceService persistenceService, AIService aiService, PlayerAccountService playerAccountService) {
        this.persistenceService = persistenceService;
        this.aiService = aiService;
        this.playerAccountService = playerAccountService;
    }

    /**
     * Load all saved game sessions from the database on startup.
     */
    @PostConstruct
    public void loadSessions() {
        List<String> sessionIds = persistenceService.listAllSessionIds();
        for (String sessionId : sessionIds) {
            persistenceService.loadSession(sessionId).ifPresent(session ->
                    sessions.put(session.getId(), session));
        }
    }

    /**
     * Save the current session to the database.
     */
    private void saveSession(GameSession session) {
        persistenceService.saveSession(session);
    }

    /**
     * Create a new game session
     */
    public GameSession createGame(String name) {
        GameSession session = new GameSession();
        session.setName(name);
        session.setStarted(false);

        // Add default ship classes
        session.addShipClass(createDefaultShipClasses("scout", "Scout", 10, 1, 8, 3, 2, 2, 5, 1, 20,
                new String[]{"laser", "laser"}, new int[]{0, 3}));
        session.addShipClass(createDefaultShipClasses("fighter", "Fighter", 20, 2, 6, 2, 2, 3, 10, 1, 30,
                new String[]{"laser", "laser", "missile"}, new int[]{0, 3, 1}));
        session.addShipClass(createDefaultShipClasses("cruiser", "Cruiser", 40, 3, 5, 2, 1, 4, 20, 2, 50,
                new String[]{"laser", "laser", "missile", "missile"}, new int[]{0, 3, 1, 2}));
        session.addShipClass(createDefaultShipClasses("destroyer", "Destroyer", 30, 2, 7, 3, 2, 3, 15, 1, 40,
                new String[]{"laser", "missile", "torpedo"}, new int[]{0, 2, 1}));
        session.addShipClass(createDefaultShipClasses("dreadnought", "Dreadnought", 80, 5, 3, 1, 1, 5, 40, 3, 80,
                new String[]{"laser", "laser", "missile", "missile", "torpedo"}, new int[]{0, 3, 1, 2, 1}));

        sessions.put(session.getId(), session);
        saveSession(session);
        return session;
    }

    private ShipClass createDefaultShipClasses(String id, String name, int hull, int armor,
            int maxSpeed, int accel, int deaccel, int headingChange, int shields, int shieldRegen,
            int fuel, String[] weaponTypes, int[] weaponArcs) {
        ShipClass shipClass = new ShipClass();
        shipClass.setId(id);
        shipClass.setName(name);
        shipClass.setHullPoints(hull);
        shipClass.setArmorValue(armor);
        shipClass.setMaxSpeed(maxSpeed);
        shipClass.setAcceleration(accel);
        shipClass.setDeceleration(deaccel);
        shipClass.setMaxHeadingChange(headingChange);
        shipClass.setShieldPoints(shields);
        shipClass.setShieldRegeneration(shieldRegen);
        shipClass.setFuelCapacity(fuel);
        shipClass.setDescription(name + " class vessel");

        for (int i = 0; i < weaponTypes.length; i++) {
            ShipClass.WeaponSlot slot = new ShipClass.WeaponSlot(
                    weaponTypes[i] + "_" + i, weaponArcs[i], weaponTypes[i]);
            shipClass.addWeaponSlot(slot);
        }

        return shipClass;
    }

    /**
     * Return the default ship class definitions for the ship library.
     */
    public List<ShipClass> getDefaultShipClasses() {
        List<ShipClass> classes = new ArrayList<>();
        classes.add(createDefaultShipClasses("scout", "Scout", 10, 1, 8, 3, 2, 2, 5, 1, 20,
                new String[]{"laser", "laser"}, new int[]{0, 3}));
        classes.add(createDefaultShipClasses("fighter", "Fighter", 20, 2, 6, 2, 2, 3, 10, 1, 30,
                new String[]{"laser", "laser", "missile"}, new int[]{0, 3, 1}));
        classes.add(createDefaultShipClasses("cruiser", "Cruiser", 40, 3, 5, 2, 1, 4, 20, 2, 50,
                new String[]{"laser", "laser", "missile", "missile"}, new int[]{0, 3, 1, 2}));
        classes.add(createDefaultShipClasses("destroyer", "Destroyer", 30, 2, 7, 3, 2, 3, 15, 1, 40,
                new String[]{"laser", "missile", "torpedo"}, new int[]{0, 2, 1}));
        classes.add(createDefaultShipClasses("dreadnought", "Dreadnought", 80, 5, 3, 1, 1, 5, 40, 3, 80,
                new String[]{"laser", "laser", "missile", "missile", "torpedo"}, new int[]{0, 3, 1, 2, 1}));
        return classes;
    }

    /**
     * Get a game session by ID
     */
    public Optional<GameSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * List all active game sessions
     */
    public List<GameSession> listSessions() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Add a player to a game
     */
    public GameSession addPlayer(String sessionId, String playerName, String faction) {
        return addPlayer(sessionId, playerName, faction, false);
    }

    /**
     * Add a player to a game, optionally as an AI-controlled player
     */
    public GameSession addPlayer(String sessionId, String playerName, String faction, boolean isAI) {
        return addPlayer(sessionId, playerName, faction, isAI, "medium");
    }

    /**
     * Add a player to a game, optionally as an AI-controlled player with difficulty
     */
    public GameSession addPlayer(String sessionId, String playerName, String faction, boolean isAI, String aiDifficulty) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (session.isStarted()) {
            throw new IllegalStateException("Game already started");
        }

        Player player = new Player(UUID.randomUUID().toString(), playerName, faction, isAI);
        if (isAI) {
            player.setAiDifficulty(aiDifficulty != null ? aiDifficulty : "medium");
        }
        session.addPlayer(player);
        saveSession(session);
        return session;
    }

    /**
     * Deploy a ship for a player
     */
    public GameSession deployShip(String sessionId, String playerId, String shipClassId,
            String shipName, int startQ, int startR) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (session.isStarted()) {
            throw new IllegalStateException("Game already started");
        }

        // Resolve player name to player ID if needed
        String resolvedPlayerId = playerId;
        if (!session.getPlayers().containsKey(playerId)) {
            // Try to find player by name
            for (Player player : session.getPlayers().values()) {
                if (player.getName().equals(playerId)) {
                    resolvedPlayerId = player.getId();
                    break;
                }
            }
        }

        ShipClass shipClass = session.getShipClasses().get(shipClassId);
        if (shipClass == null) {
            throw new IllegalArgumentException("Ship class not found: " + shipClassId);
        }

        Ship ship = new Ship(UUID.randomUUID().toString(), shipName, resolvedPlayerId, shipClass);
        ship.setPosition(new HexPosition(startQ, startR));
        session.addShip(ship);
        saveSession(session);
        return session;
    }

    /**
     * Start the game
     */
    public GameSession startGame(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        if (session.getPlayers().size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start");
        }

        session.setStarted(true);
        session.setCurrentTurn(1);

        // Set first player
        List<String> playerIds = new ArrayList<>(session.getPlayers().keySet());
        session.setCurrentPlayerId(playerIds.get(0));

        // Generate asteroid field
        session.getBoard().generateAsteroidField(50, random);

        session.logEvent("Game started!");
        saveSession(session);
        return session;
    }

    /**
     * Move a ship - FullThrust movement rules
     * Ship can change heading and accelerate/decelerate
     */
    public GameSession moveShip(String sessionId, String shipId, int headingChange, int thrust) {
        GameSession session = sessions.get(sessionId);
        if (session == null || !session.isStarted() || session.isEnded()) {
            throw new IllegalStateException("Game not in progress");
        }

        Ship ship = session.getShips().get(shipId);
        if (ship == null || ship.isDestroyed()) {
            throw new IllegalArgumentException("Ship not found or destroyed");
        }

        if (!ship.getPlayerId().equals(session.getCurrentPlayerId())) {
            throw new IllegalStateException("Not your turn");
        }

        ShipClass shipClass = session.getShipClasses().get(ship.getShipClassId());
        if (shipClass == null) {
            throw new IllegalStateException("Ship class not found");
        }

        // Apply heading change (limited by ship's max heading change)
        int maxHeadingChange = shipClass.getMaxHeadingChange();
        headingChange = Math.max(-maxHeadingChange, Math.min(maxHeadingChange, headingChange));
        int newHeading = (ship.getHeading() + headingChange + 6) % 6;
        ship.setHeading(newHeading);

        // Apply thrust (acceleration/deceleration)
        int maxAccel = shipClass.getAcceleration();
        int maxDeaccel = shipClass.getDeceleration();
        thrust = Math.max(-maxDeaccel, Math.min(maxAccel, thrust));

        int newSpeed = ship.getSpeed() + thrust;
        newSpeed = Math.max(0, Math.min(shipClass.getMaxSpeed(), newSpeed));
        ship.setSpeed(newSpeed);

        // Move the ship
        if (newSpeed > 0 && ship.getCurrentFuel() > 0) {
            // Direction vectors for hex grid (0=East, 1=SE, 2=SW, 3=West, 4=NW, 5=NE)
            int[] dq = {1, 1, 0, -1, -1, 0};
            int[] dr = {0, 1, 1, 0, -1, -1};

            HexPosition currentPos = ship.getPosition();
            int newQ = currentPos.getQ() + dq[newHeading] * newSpeed;
            int newR = currentPos.getR() + dr[newHeading] * newSpeed;

            // Check bounds
            HexPosition newPos = new HexPosition(newQ, newR);
            if (session.getBoard().isValidPosition(newPos)) {
                // Check for asteroid collision
                if (session.getBoard().hasObstacle(newPos)) {
                    int asteroidDamage = calculateCollisionDamage(newSpeed, shipClass.getArmorValue());
                    ship.takeDamage(asteroidDamage);
                    session.logEvent(ship.getName() + " collided with asteroid at (" + newQ + "," + newR +
                            ") for " + asteroidDamage + " damage!");
                    if (ship.isDestroyed()) {
                        session.logEvent(ship.getName() + " was destroyed by asteroid collision!");
                    }
                    newSpeed = 0;
                    ship.setSpeed(0);
                } else {
                    // Check for ship-to-ship collision
                    Ship shipAtPosition = null;
                    for (Ship other : session.getShips().values()) {
                        if (!other.getId().equals(shipId) && !other.isDestroyed() &&
                                other.getPosition().getQ() == newQ && other.getPosition().getR() == newR) {
                            shipAtPosition = other;
                            break;
                        }
                    }
                    if (shipAtPosition != null) {
                        // Ship-to-ship collision
                        ShipClass otherClass = session.getShipClasses().get(shipAtPosition.getShipClassId());
                        int mySpeed = newSpeed;
                        int otherSpeed = shipAtPosition.getSpeed();
                        int myDamage = calculateCollisionDamage(mySpeed, otherClass != null ? otherClass.getArmorValue() : 2);
                        int otherDamage = calculateCollisionDamage(otherSpeed, shipClass.getArmorValue());
                        shipAtPosition.takeDamage(myDamage);
                        ship.takeDamage(otherDamage);
                        session.logEvent(ship.getName() + " collided with " + shipAtPosition.getName() +
                                " at (" + newQ + "," + newR + ")! " +
                                ship.getName() + " took " + otherDamage + " damage, " +
                                shipAtPosition.getName() + " took " + myDamage + " damage!");
                        if (ship.isDestroyed()) {
                            session.logEvent(ship.getName() + " was destroyed in the collision!");
                        }
                        if (shipAtPosition.isDestroyed()) {
                            session.logEvent(shipAtPosition.getName() + " was destroyed in the collision!");
                        }
                        newSpeed = 0;
                        shipAtPosition.setSpeed(0);
                        ship.setSpeed(0);
                    } else {
                        currentPos.setQ(newQ);
                        currentPos.setR(newR);
                        ship.setCurrentFuel(Math.max(0, ship.getCurrentFuel() - 1));
                    }
                }
            } else {
                // Hit boundary - stop
                newSpeed = 0;
                ship.setSpeed(0);
            }
        }

        session.logEvent(ship.getName() + " moved (heading=" + newHeading + ", speed=" + newSpeed + ")");
        saveSession(session);
        return session;
    }

    /**
     * Fire weapons at a target
     */
    public GameSession fireWeapon(String sessionId, String shipId, String targetId, int weaponIndex) {
        GameSession session = sessions.get(sessionId);
        if (session == null || !session.isStarted() || session.isEnded()) {
            throw new IllegalStateException("Game not in progress");
        }

        Ship ship = session.getShips().get(shipId);
        Ship target = session.getShips().get(targetId);
        if (ship == null || target == null) {
            throw new IllegalArgumentException("Ship or target not found");
        }
        if (ship.isDestroyed() || target.isDestroyed()) {
            throw new IllegalArgumentException("Ship or target is destroyed");
        }
        if (!ship.getPlayerId().equals(session.getCurrentPlayerId())) {
            throw new IllegalStateException("Not your turn");
        }
        if (ship.getPlayerId().equals(target.getPlayerId())) {
            throw new IllegalArgumentException("Cannot fire at friendly ship");
        }

        if (weaponIndex < 0 || weaponIndex >= ship.getWeapons().size()) {
            throw new IllegalArgumentException("Invalid weapon index");
        }

        Weapon weapon = ship.getWeapons().get(weaponIndex);
        if (weapon.isDisabled() || weapon.isReloading()) {
            throw new IllegalStateException("Weapon is not ready");
        }

        // Check if target is in range and in the correct arc
        int distance = ship.getPosition().distanceTo(target.getPosition());
        if (distance > weapon.getRange()) {
            throw new IllegalArgumentException("Target out of range");
        }

        int absoluteDirection = ship.getPosition().directionTo(target.getPosition());
        int relativeDirection = (absoluteDirection - ship.getHeading() + 6) % 6;
        // Allow targets within ±1 direction of the weapon's arc (60-degree cone)
        int arcDiff = Math.abs(relativeDirection - weapon.getArc());
        arcDiff = Math.min(arcDiff, 6 - arcDiff); // shortest wrap-around distance
        if (arcDiff > 1) {
            throw new IllegalArgumentException("Target not in weapon's firing arc");
        }

        // Fire the weapon
        weapon.fire();

        if ("laser".equals(weapon.getType())) {
            // Lasers hit immediately
            int damage = calculateDamage(weapon.getDamage(), target.getArmorValue());
            target.takeDamage(damage);
            session.logEvent(ship.getName() + " fired " + weapon.getType() + " at " + target.getName() +
                    " for " + damage + " damage");

            if (target.isDestroyed()) {
                session.logEvent(target.getName() + " was destroyed!");
            }
        } else {
            // Missiles and torpedoes are launched
            String missileId = UUID.randomUUID().toString();
            Missile missile = new Missile(missileId, shipId, ship.getName(),
                    new HexPosition(ship.getPosition().getQ(), ship.getPosition().getR()),
                    weapon.getArc(), weapon.getDamage(), 3); // 3 turns to reach target
            session.getActiveMissiles().add(missile);
            session.logEvent(ship.getName() + " launched " + weapon.getType() + " at " + target.getName());
        }

        saveSession(session);
        return session;
    }

    /**
     * Calculate damage based on weapon power and target armor
     */
    private int calculateDamage(int weaponDamage, int targetArmor) {
        // Simple damage calculation: roll a die and compare to armor
        int roll = random.nextInt(6) + 1; // 1-6
        int damage = Math.max(0, weaponDamage + roll - targetArmor);
        return damage;
    }

    /**
     * Calculate collision damage based on speed and target armor.
     * Higher speed = more damage, higher armor = less damage.
     */
    private int calculateCollisionDamage(int speed, int armor) {
        // Collision damage is based on speed: speed * 1d6, reduced by armor
        if (speed <= 0) return 0;
        int roll = random.nextInt(6) + 1; // 1-6
        int damage = Math.max(1, speed * roll - armor);
        return damage;
    }

    /**
     * End current player's turn
     */
    public GameSession endTurn(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null || !session.isStarted() || session.isEnded()) {
            throw new IllegalStateException("Game not in progress");
        }

        // Advance all weapon reloads for current player's ships
        for (Ship ship : session.getShipsForPlayer(session.getCurrentPlayerId())) {
            for (Weapon weapon : ship.getWeapons()) {
                weapon.advanceReload();
            }
            // Shield regeneration
            ShipClass shipClass = session.getShipClasses().get(ship.getShipClassId());
            if (shipClass != null && shipClass.getShieldRegeneration() > 0) {
                ship.regenerateShields(shipClass.getShieldRegeneration());
            }
        }

        // Move missiles
        List<Missile> expiredMissiles = new ArrayList<>();
        for (Missile missile : session.getActiveMissiles()) {
            missile.move();

            // Check if missile hits any ship
            for (Ship ship : session.getActiveShips()) {
                if (missile.getPosition().equals(ship.getPosition()) &&
                        !missile.getOwnerId().equals(ship.getId())) {
                    int damage = missile.getDamage();
                    ship.takeDamage(damage);
                    missile.setHit(true);
                    session.logEvent(missile.getOwnerName() + "'s missile hit " + ship.getName() +
                            " for " + damage + " damage");
                    if (ship.isDestroyed()) {
                        session.logEvent(ship.getName() + " was destroyed!");
                    }
                    break;
                }
            }

            if (missile.isExpired() || missile.isHit()) {
                expiredMissiles.add(missile);
            }
        }
        session.getActiveMissiles().removeAll(expiredMissiles);

        // Check game end
        session.checkGameEnd();

        // If game ended, record battle results
        if (session.isEnded()) {
            session.logEvent("Game Over! Winner: " + session.getWinnerName());
            playerAccountService.recordBattleResults(session);
            saveSession(session);
            return session;
        }

        // Move to next player
        session.nextPlayer();

        // If we've cycled through all players, advance turn
        List<String> playerIds = new ArrayList<>(session.getPlayers().keySet());
        if (session.getCurrentPlayerId().equals(playerIds.get(0))) {
            session.nextTurn();
        }

        session.logEvent("Turn ended");
        saveSession(session);
        return session;
    }

    /**
     * Process an AI player's turn automatically.
     * The AI will move all its ships and fire weapons, then end the turn.
     */
    public GameSession processAITurn(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null || !session.isStarted() || session.isEnded()) {
            throw new IllegalStateException("Game not in progress");
        }

        Player currentPlayer = session.getPlayers().get(session.getCurrentPlayerId());
        if (currentPlayer == null || !currentPlayer.isAI()) {
            throw new IllegalStateException("Current player is not an AI player");
        }

        // Let the AI make its moves
        aiService.processAITurn(session);

        // End the AI's turn
        return endTurn(sessionId);
    }

    /**
     * Process all consecutive AI players' turns in one call.
     * This speeds up AI-vs-AI battles by eliminating round-trip latency between turns.
     * Stops when it reaches a human player or the game ends.
     * Returns the session after processing all consecutive AI turns.
     */
    public GameSession processAllAITurns(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null || !session.isStarted()) {
            throw new IllegalStateException("Game not in progress");
        }
        // If game already ended, return current state without error
        if (session.isEnded()) {
            return session;
        }

        // Process up to 10 consecutive AI turns (safety limit to prevent infinite loops)
        int maxTurns = 10;
        int turnsProcessed = 0;

        while (turnsProcessed < maxTurns && !session.isEnded()) {
            Player currentPlayer = session.getPlayers().get(session.getCurrentPlayerId());
            if (currentPlayer == null || !currentPlayer.isAI()) {
                // Reached a human player — stop
                break;
            }

            // Let the AI make its moves
            aiService.processAITurn(session);

            // End the AI's turn
            session = endTurn(sessionId);
            turnsProcessed++;
        }

        return session;
    }

    /**
     * Delete a game session
     */
    public void deleteGame(String sessionId) {
        sessions.remove(sessionId);
        persistenceService.deleteSession(sessionId);
    }
}
