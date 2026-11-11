package com.yisy.modules.doramcu.thirdreich.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ground battle in the 3R4 combat engine.
 * <p>
 * A battle consists of two opposing sides (attacker and defender), each with
 * a list of ground units and an optional commander. The battle is resolved
 * round-by-round using CRT (Combat Results Table) mechanics.
 * </p>
 */
public class ThirdReichBattle {

    public enum BattleState {
        /** Battle created but not yet started */
        INITIATED,
        /** Battle in progress */
        IN_PROGRESS,
        /** Battle completed with a result */
        COMPLETED,
        /** Battle ended in retreat */
        RETREAT
    }

    private final String id;
    private final List<ThirdReichGroundUnit> attackerUnits = new ArrayList<>();
    private final List<ThirdReichGroundUnit> defenderUnits = new ArrayList<>();
    private TerrainContext terrainContext;
    private String attackerCommanderId;
    private String defenderCommanderId;
    private BattleState state = BattleState.INITIATED;
    private int currentRound = 0;
    private final List<String> events = new ArrayList<>();
    private ThirdReichBattleResult result;

    /**
     * Creates a new battle with the given ID.
     *
     * @param id the unique battle identifier
     */
    public ThirdReichBattle(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Battle ID must not be null or blank");
        }
        this.id = id;
    }

    // ---- Accessors ----

    /** Returns the battle ID. */
    public String getId() {
        return id;
    }

    /** Returns the attacker's units. */
    public List<ThirdReichGroundUnit> getAttackerUnits() {
        return attackerUnits;
    }

    /** Returns the defender's units. */
    public List<ThirdReichGroundUnit> getDefenderUnits() {
        return defenderUnits;
    }

    /** Returns the terrain context. */
    public TerrainContext getTerrainContext() {
        return terrainContext;
    }

    /** Sets the terrain context. */
    public void setTerrainContext(TerrainContext terrainContext) {
        this.terrainContext = terrainContext;
    }

    /** Returns the attacker commander ID, or null if none. */
    public String getAttackerCommanderId() {
        return attackerCommanderId;
    }

    /** Sets the attacker commander ID. */
    public void setAttackerCommanderId(String commanderId) {
        this.attackerCommanderId = commanderId;
    }

    /** Returns the defender commander ID, or null if none. */
    public String getDefenderCommanderId() {
        return defenderCommanderId;
    }

    /** Sets the defender commander ID. */
    public void setDefenderCommanderId(String commanderId) {
        this.defenderCommanderId = commanderId;
    }

    /** Returns the current battle state. */
    public BattleState getState() {
        return state;
    }

    /** Returns the current round number. */
    public int getCurrentRound() {
        return currentRound;
    }

    /** Returns the list of battle events. */
    public List<String> getEvents() {
        return events;
    }

    /** Returns the battle result, or null if not completed. */
    public ThirdReichBattleResult getResult() {
        return result;
    }

    // ---- Battle Management ----

    /**
     * Starts the battle. Validates that both sides have units.
     */
    public void start() {
        if (attackerUnits.isEmpty() || defenderUnits.isEmpty()) {
            addEvent("Battle cannot start: one side has no units");
            return;
        }
        state = BattleState.IN_PROGRESS;
        addEvent("Battle initiated");
        if (terrainContext != null) {
            addEvent("Terrain: " + terrainContext);
        }
    }

    /**
     * Adds a unit to the attacker's side.
     */
    public void addAttackerUnit(ThirdReichGroundUnit unit) {
        if (unit == null) return;
        attackerUnits.add(unit);
    }

    /**
     * Adds a unit to the defender's side.
     */
    public void addDefenderUnit(ThirdReichGroundUnit unit) {
        if (unit == null) return;
        defenderUnits.add(unit);
    }

    /**
     * Records a battle event.
     */
    public void addEvent(String event) {
        events.add("Round " + currentRound + ": " + event);
    }

    /**
     * Advances to the next round.
     */
    public void nextRound() {
        currentRound++;
        addEvent("Round " + currentRound + " begins");
    }

    /**
     * Ends the battle with the given result.
     */
    public void endBattle(ThirdReichBattleResult battleResult) {
        this.result = battleResult;
        state = BattleState.COMPLETED;
        addEvent("Battle ended: " + battleResult.getOutcome());
    }

    /**
     * Ends the battle in retreat.
     */
    public void retreat() {
        state = BattleState.RETREAT;
        addEvent("Battle ended in retreat");
    }

    /**
     * Returns the total attack power of the attacker's side.
     */
    public int getAttackerTotalAttack() {
        return attackerUnits.stream()
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .mapToInt(ThirdReichGroundUnit::getEffectiveAttack)
                .sum();
    }

    /**
     * Returns the total defense power of the defender's side.
     */
    public int getDefenderTotalDefense() {
        return defenderUnits.stream()
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .mapToInt(ThirdReichGroundUnit::getEffectiveDefense)
                .sum();
    }

    /**
     * Returns the total attack power of the defender's side.
     */
    public int getDefenderTotalAttack() {
        return defenderUnits.stream()
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .mapToInt(ThirdReichGroundUnit::getEffectiveAttack)
                .sum();
    }

    /**
     * Returns the total defense power of the attacker's side.
     */
    public int getAttackerTotalDefense() {
        return attackerUnits.stream()
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .mapToInt(ThirdReichGroundUnit::getEffectiveDefense)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("3R4Battle{id='%s', state=%s, round=%d, attackers=%d, defenders=%d}",
                id, state, currentRound, attackerUnits.size(), defenderUnits.size());
    }
}
