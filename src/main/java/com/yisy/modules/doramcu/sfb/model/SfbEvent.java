package com.yisy.modules.doramcu.sfb.model;

/**
 * Represents a single event during an SFB battle.
 * <p>
 * Events are logged during the 32-impulse turn loop to track
 * what happened in the battle.
 * </p>
 */
public class SfbEvent {

    public enum EventType {
        BATTLE_STARTED,
        TURN_STARTED,
        IMPULSE_STARTED,
        ENERGY_ALLOCATED,
        SHIP_MOVED,
        SHIP_TURNED,
        WEAPON_FIRED,
        WEAPON_HIT,
        WEAPON_MISS,
        DAMAGE_ABSORBED,
        DAMAGE_PENETRATED,
        SYSTEM_DAMAGED,
        SHIELD_DOWN,
        SHIP_CRIPPLED,
        SHIP_DESTROYED,
        SEEKING_WEAPON_LAUNCHED,
        SEEKING_WEAPON_HIT,
        SEEKING_WEAPON_INTERCEPTED,
        SEEKING_WEAPON_INERT,
        CLOAK_ACTIVATED,
        CLOAK_DEACTIVATED,
        TRACTOR_BEAM_USED,
        DAMCON_REPAIR,
        TURN_COMPLETED,
        BATTLE_ENDED
    }

    private final int turn;
    private final int impulse;
    private final EventType type;
    private final String message;
    private final String shipId;

    public SfbEvent(int turn, int impulse, EventType type, String message, String shipId) {
        this.turn = turn;
        this.impulse = impulse;
        this.type = type;
        this.message = message;
        this.shipId = shipId;
    }

    public int getTurn() { return turn; }
    public int getImpulse() { return impulse; }
    public EventType getType() { return type; }
    public String getMessage() { return message; }
    public String getShipId() { return shipId; }

    @Override
    public String toString() {
        return "SfbEvent{turn=" + turn + ", impulse=" + impulse +
                ", type=" + type + ", ship=" + shipId + ", msg=" + message + "}";
    }
}
