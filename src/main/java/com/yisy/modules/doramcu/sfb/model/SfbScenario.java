package com.yisy.modules.doramcu.sfb.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<String> aiShipIds; // Ships controlled by AI
    private List<SfbCharacterAssignment> characterAssignments; // Captain's Log character assignments

    public SfbScenario() {
        this.ships = new ArrayList<>();
        this.eventLog = new ArrayList<>();
        this.currentTurn = 0;
        this.currentImpulse = 0;
        this.currentPhase = ScenarioPhase.SETUP;
        this.aiShipIds = new HashSet<>();
        this.characterAssignments = new ArrayList<>();
    }

    public SfbScenario(String id) {
        this.id = id;
        this.ships = new ArrayList<>();
        this.eventLog = new ArrayList<>();
        this.currentTurn = 0;
        this.currentImpulse = 0;
        this.currentPhase = ScenarioPhase.SETUP;
        this.aiShipIds = new HashSet<>();
        this.characterAssignments = new ArrayList<>();
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

    public Set<String> getAiShipIds() { return aiShipIds; }
    public void setAiShipIds(Set<String> aiShipIds) { this.aiShipIds = aiShipIds; }

    public List<SfbCharacterAssignment> getCharacterAssignments() { return characterAssignments; }
    public void setCharacterAssignments(List<SfbCharacterAssignment> characterAssignments) { 
        this.characterAssignments = characterAssignments; 
    }

    /**
     * Mark a ship as AI-controlled.
     */
    public void addAiShip(String shipId) {
        aiShipIds.add(shipId);
    }

    /**
     * Check if a ship is AI-controlled.
     */
    public boolean isAiShip(String shipId) {
        return aiShipIds.contains(shipId);
    }

    /**
     * Add a character assignment to this scenario.
     */
    public void addCharacterAssignment(SfbCharacterAssignment assignment) {
        characterAssignments.add(assignment);
    }

    /**
     * Get character assignments for a specific ship.
     */
    public List<SfbCharacterAssignment> getShipAssignments(String shipId) {
        List<SfbCharacterAssignment> result = new ArrayList<>();
        for (SfbCharacterAssignment assignment : characterAssignments) {
            if (shipId.equals(assignment.getShipId())) {
                result.add(assignment);
            }
        }
        return result;
    }

    /**
     * Get the Admiral assignment for a side.
     */
    public SfbCharacterAssignment getAdmiralAssignment(int side) {
        for (SfbCharacterAssignment assignment : characterAssignments) {
            if (assignment.getRole() == SfbRole.ADMIRAL && assignment.getSide() == side) {
                return assignment;
            }
        }
        return null;
    }

    /**
     * Get the Captain assignment for a ship.
     */
    public SfbCharacterAssignment getCaptainAssignment(String shipId) {
        for (SfbCharacterAssignment assignment : characterAssignments) {
            if (assignment.getRole() == SfbRole.CAPTAIN && shipId.equals(assignment.getShipId())) {
                return assignment;
            }
        }
        return null;
    }

    /**
     * Get crew assignments for a ship.
     */
    public List<SfbCharacterAssignment> getCrewAssignments(String shipId) {
        List<SfbCharacterAssignment> result = new ArrayList<>();
        for (SfbCharacterAssignment assignment : characterAssignments) {
            if (assignment.getRole() == SfbRole.CREW && shipId.equals(assignment.getShipId())) {
                result.add(assignment);
            }
        }
        return result;
    }

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
