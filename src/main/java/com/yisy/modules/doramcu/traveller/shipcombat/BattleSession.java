package com.yisy.modules.doramcu.traveller.shipcombat;

import com.yisy.modules.doramcu.traveller.HighGuardRangeBand;
import com.yisy.modules.doramcu.traveller.HighGuardShipCombat;
import com.yisy.modules.doramcu.traveller.ShipToShipBattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Wraps a ship-to-ship battle with metadata for REST API exposure.
 * <p>
 * Tracks the battle ID, rules type, fleets, current state, and all events.
 * Supports both Classic Traveller (ShipToShipBattle) and High Guard (HighGuardShipCombat) rules.
 * </p>
 */
public class BattleSession {

    public enum RulesType {
        CLASSIC_TRAVELLER,
        HIGH_GUARD
    }

    public enum BattleState {
        CREATED,
        IN_PROGRESS,
        COMPLETED,
        STALEMATE
    }

    private final String id;
    private final RulesType rulesType;
    private final Fleet fleet0;
    private final Fleet fleet1;
    private final String sector;
    private BattleState state;
    private final List<Map<String, Object>> events;
    private int currentRound;
    private ShipToShipBattle classicBattle;
    private HighGuardShipCombat highGuardCombat;
    private String outcome;

    private BattleSession(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Battle ID must not be null");
        this.rulesType = Objects.requireNonNull(builder.rulesType, "Rules type must not be null");
        this.fleet0 = Objects.requireNonNull(builder.fleet0, "Fleet 0 must not be null");
        this.fleet1 = Objects.requireNonNull(builder.fleet1, "Fleet 1 must not be null");
        this.sector = builder.sector != null ? builder.sector : "Unknown Sector";
        this.state = BattleState.CREATED;
        this.events = new ArrayList<>();
        this.currentRound = 0;
    }

    public String getId() { return id; }
    public RulesType getRulesType() { return rulesType; }
    public Fleet getFleet0() { return fleet0; }
    public Fleet getFleet1() { return fleet1; }
    public String getSector() { return sector; }
    public BattleState getState() { return state; }
    public List<Map<String, Object>> getEvents() { return Collections.unmodifiableList(events); }
    public int getCurrentRound() { return currentRound; }
    public String getOutcome() { return outcome; }

    /**
     * Sets the underlying Classic Traveller battle engine.
     */
    void setClassicBattle(ShipToShipBattle battle) {
        this.classicBattle = battle;
    }

    /**
     * Returns the Classic Traveller battle engine.
     */
    ShipToShipBattle getClassicBattle() {
        return classicBattle;
    }

    /**
     * Sets the underlying High Guard combat engine.
     */
    void setHighGuardCombat(HighGuardShipCombat combat) {
        this.highGuardCombat = combat;
    }

    /**
     * Returns the High Guard combat engine.
     */
    HighGuardShipCombat getHighGuardCombat() {
        return highGuardCombat;
    }

    /**
     * Updates the battle state.
     */
    void setState(BattleState state) {
        this.state = state;
    }

    /**
     * Sets the current round number.
     */
    void setCurrentRound(int round) {
        this.currentRound = round;
    }

    /**
     * Sets the final outcome description.
     */
    void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    /**
     * Adds an event to the battle log.
     */
    void addEvent(Map<String, Object> event) {
        this.events.add(Objects.requireNonNull(event));
    }

    /**
     * Creates a new builder for a BattleSession.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private RulesType rulesType;
        private Fleet fleet0;
        private Fleet fleet1;
        private String sector;

        public Builder id(String id) { this.id = id; return this; }
        public Builder rulesType(RulesType rulesType) { this.rulesType = rulesType; return this; }
        public Builder fleet0(Fleet fleet0) { this.fleet0 = fleet0; return this; }
        public Builder fleet1(Fleet fleet1) { this.fleet1 = fleet1; return this; }
        public Builder sector(String sector) { this.sector = sector; return this; }

        public BattleSession build() {
            return new BattleSession(this);
        }
    }
}
