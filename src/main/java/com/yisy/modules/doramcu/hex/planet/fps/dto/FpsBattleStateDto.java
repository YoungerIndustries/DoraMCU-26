package com.yisy.modules.doramcu.hex.planet.fps.dto;

import java.util.List;

/**
 * Battle state for the FPS 3D renderer.
 * <p>
 * Represents an active or completed battle with attacker/defender info and outcome.
 * </p>
 */
public class FpsBattleStateDto {

    private final String battleId;
    private final String battleType; // "GROUND", "NAVAL", "SPACE"
    private final String attackerFaction;
    private final String defenderFaction;
    private final int x, y, z; // Battle location hex
    private final String terrain; // Terrain type at battle location
    private final String state; // "INITIATED", "IN_PROGRESS", "COMPLETED", "RETREAT"
    private final int currentRound;
    private final String outcome; // "ATTACKER_VICTORY", "DEFENDER_VICTORY", "DRAW", etc.
    private final int attackerCasualties;
    private final int defenderCasualties;
    private final int attackerUnitsLost;
    private final int defenderUnitsLost;
    private final List<String> events; // Battle event log

    public FpsBattleStateDto(String battleId, String battleType,
                              String attackerFaction, String defenderFaction,
                              int x, int y, int z,
                              String terrain, String state,
                              int currentRound, String outcome,
                              int attackerCasualties, int defenderCasualties,
                              int attackerUnitsLost, int defenderUnitsLost,
                              List<String> events) {
        this.battleId = battleId;
        this.battleType = battleType;
        this.attackerFaction = attackerFaction;
        this.defenderFaction = defenderFaction;
        this.x = x;
        this.y = y;
        this.z = z;
        this.terrain = terrain;
        this.state = state;
        this.currentRound = currentRound;
        this.outcome = outcome;
        this.attackerCasualties = attackerCasualties;
        this.defenderCasualties = defenderCasualties;
        this.attackerUnitsLost = attackerUnitsLost;
        this.defenderUnitsLost = defenderUnitsLost;
        this.events = events;
    }

    public String getBattleId() { return battleId; }
    public String getBattleType() { return battleType; }
    public String getAttackerFaction() { return attackerFaction; }
    public String getDefenderFaction() { return defenderFaction; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getTerrain() { return terrain; }
    public String getState() { return state; }
    public int getCurrentRound() { return currentRound; }
    public String getOutcome() { return outcome; }
    public int getAttackerCasualties() { return attackerCasualties; }
    public int getDefenderCasualties() { return defenderCasualties; }
    public int getAttackerUnitsLost() { return attackerUnitsLost; }
    public int getDefenderUnitsLost() { return defenderUnitsLost; }
    public List<String> getEvents() { return events; }

    /** Returns true if the battle is currently in progress. */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(state);
    }

    /** Returns true if the battle is completed. */
    public boolean isCompleted() {
        return "COMPLETED".equals(state) || "RETREAT".equals(state);
    }
}
