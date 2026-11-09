package com.yisy.modules.doramcu.sfb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tactical battle scenario in SFB.
 * <p>
 * Tracks participating ships, turn/impulse state, and event log.
 * A scenario is a single tactical engagement on the hex grid.
 * </p>
 */
public class SfbScenario {

    public enum ScenarioPhase {
        SETUP,
        ENERGY_ALLOCATION,
        IMPULSE_PROCEDURE,
        DAMAGE_RESOLUTION,
        TURN_COMPLETE,
        BATTLE_ENDED
    }

    private String id;
    private String name;
    private List<SfbShip> ships;
    private int currentTurn;
    private int currentImpulse; // 1-32
    private ScenarioPhase currentPhase;
    private List<SfbEvent> eventLog;
    private SfbBattleLocation battleLocation;

    public SfbScenario() {
        this.ships = new ArrayList<>();
        this.eventLog = new ArrayList<>();
        this.currentTurn = 0;
        this.currentImpulse = 0;
        this.currentPhase = ScenarioPhase.SETUP;
    }

    public SfbScenario(String id) {
        this.id = id;
        this.ships = new ArrayList<>();
        this.eventLog = new ArrayList<>();
        this.currentTurn = 0;
        this.currentImpulse = 0;
        this.currentPhase = ScenarioPhase.SETUP;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<SfbShip> getShips() { return ships; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public int getCurrentImpulse() { return currentImpulse; }
    public void setCurrentImpulse(int currentImpulse) { this.currentImpulse = currentImpulse; }

    public ScenarioPhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(ScenarioPhase currentPhase) { this.currentPhase = currentPhase; }

    public List<SfbEvent> getEventLog() { return eventLog; }

    public SfbBattleLocation getBattleLocation() { return battleLocation; }
    public void setBattleLocation(SfbBattleLocation battleLocation) { this.battleLocation = battleLocation; }

    /**
     * Add a ship to the scenario.
     */
    public void addShip(SfbShip ship) {
        ships.add(ship);
    }

    /**
     * Remove a ship from the scenario (destroyed or retreated).
     */
    public void removeShip(String shipId) {
        ships.removeIf(s -> s.getId().equals(shipId));
    }

    /**
     * Get all ships for a given side.
     */
    public List<SfbShip> getShipsBySide(int side) {
        List<SfbShip> result = new ArrayList<>();
        for (SfbShip ship : ships) {
            if (ship.getSide() == side) {
                result.add(ship);
            }
        }
        return result;
    }

    /**
     * Get all ships still in combat (not crippled or destroyed).
     */
    public List<SfbShip> getActiveShips() {
        List<SfbShip> result = new ArrayList<>();
        for (SfbShip ship : ships) {
            if (ship.isInCombat()) {
                result.add(ship);
            }
        }
        return result;
    }

    /**
     * Log an event.
     */
    public void logEvent(SfbEvent event) {
        eventLog.add(event);
    }

    /**
     * Start a new turn.
     */
    public void startTurn() {
        currentTurn++;
        currentImpulse = 0;
        currentPhase = ScenarioPhase.ENERGY_ALLOCATION;
        logEvent(new SfbEvent(currentTurn, 0, SfbEvent.EventType.TURN_STARTED,
                "Turn " + currentTurn + " started", null));
    }

    /**
     * Advance to the next impulse.
     */
    public void nextImpulse() {
        currentImpulse++;
        if (currentImpulse > 32) {
            currentPhase = ScenarioPhase.TURN_COMPLETE;
        } else {
            currentPhase = ScenarioPhase.IMPULSE_PROCEDURE;
            logEvent(new SfbEvent(currentTurn, currentImpulse, SfbEvent.EventType.IMPULSE_STARTED,
                "Impulse " + currentImpulse, null));
        }
    }

    /**
     * Check if the battle is over.
     */
    public boolean isBattleOver() {
        // Check if one side has no active ships
        List<SfbShip> side0 = getActiveShipsBySide(0);
        List<SfbShip> side1 = getActiveShipsBySide(1);

        if (side0.isEmpty() || side1.isEmpty()) {
            currentPhase = ScenarioPhase.BATTLE_ENDED;
            return true;
        }
        return false;
    }

    /**
     * Get all ships for a given side that are still active.
     */
    public List<SfbShip> getActiveShipsBySide(int side) {
        List<SfbShip> result = new ArrayList<>();
        for (SfbShip ship : ships) {
            if (ship.getSide() == side && ship.isInCombat()) {
                result.add(ship);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "SfbScenario{id=" + id + ", name=" + name +
                ", turn=" + currentTurn + ", impulse=" + currentImpulse +
                ", phase=" + currentPhase + ", ships=" + ships.size() + "}";
    }
}
