package com.yisy.modules.doramcu.sfb.dto;

import com.yisy.modules.doramcu.sfb.model.SfbEvent;

import java.util.List;

/**
 * Server -> Client game state snapshot.
 */
public class SfbGameState {
    public enum Phase {
        ENERGY_ALLOCATION, IMPULSE_PROCEDURE, TURN_COMPLETE
    }

    private String scenarioId;
    private int turn;
    private int impulse;
    private List<SfbShipSnapshot> ships;
    private List<SfbEvent> events;
    private Phase currentPhase;

    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }
    public int getTurn() { return turn; }
    public void setTurn(int turn) { this.turn = turn; }
    public int getImpulse() { return impulse; }
    public void setImpulse(int impulse) { this.impulse = impulse; }
    public List<SfbShipSnapshot> getShips() { return ships; }
    public void setShips(List<SfbShipSnapshot> ships) { this.ships = ships; }
    public List<SfbEvent> getEvents() { return events; }
    public void setEvents(List<SfbEvent> events) { this.events = events; }
    public Phase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(Phase currentPhase) { this.currentPhase = currentPhase; }
}
