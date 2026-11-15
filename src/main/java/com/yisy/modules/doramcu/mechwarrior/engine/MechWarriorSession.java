package com.yisy.modules.doramcu.mechwarrior.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a MechWarrior game session with multiple mechs.
 * <p>
 * Tracks all mechs in the session, game state, and match events.
 * </p>
 */
public class MechWarriorSession {

    public enum SessionState {
        WAITING, IN_PROGRESS, FINISHED
    }

    private final String sessionId;
    private final String mapName;
    private final int maxPlayers;
    private final Map<String, MechWarriorMech> mechs = new ConcurrentHashMap<>();
    private SessionState state;
    private long currentTick;
    private long startTime;
    private long endTime;

    public MechWarriorSession(String sessionId, String mapName, int maxPlayers) {
        this.sessionId = sessionId;
        this.mapName = mapName;
        this.maxPlayers = maxPlayers;
        this.state = SessionState.WAITING;
        this.currentTick = 0;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Add a mech to the session.
     */
    public void addMech(String pilotId, MechWarriorMech mech) {
        mechs.put(pilotId, mech);
    }

    /**
     * Get a mech by pilot ID.
     */
    public MechWarriorMech getMech(String pilotId) {
        return mechs.get(pilotId);
    }

    /**
     * Get all mechs in the session.
     */
    public Map<String, MechWarriorMech> getAllMechs() {
        return mechs;
    }

    /**
     * Get the number of players in the session.
     */
    public int getPlayerCount() {
        return mechs.size();
    }

    /**
     * Start the match.
     */
    public void startMatch() {
        this.state = SessionState.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * End the match.
     */
    public void endMatch() {
        this.state = SessionState.FINISHED;
        this.endTime = System.currentTimeMillis();
    }

    /**
     * Increment the tick counter.
     */
    public void incrementTick() {
        this.currentTick++;
    }

    // Getters and Setters

    public String getSessionId() {
        return sessionId;
    }

    public String getMapName() {
        return mapName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
