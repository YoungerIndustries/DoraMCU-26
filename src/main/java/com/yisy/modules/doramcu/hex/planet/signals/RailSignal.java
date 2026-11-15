package com.yisy.modules.doramcu.hex.planet.signals;

import com.yisy.modules.doramcu.hex.Hex3D;

import java.util.UUID;

/**
 * Represents a rail signal that manages traffic on rail tracks.
 * <p>
 * Signals prevent head-on collisions and manage block sections.
 * </p>
 */
public class RailSignal {

    public enum SignalState {
        /** Green - track is clear, vehicles may proceed */
        GREEN,
        /** Red - track is occupied, vehicles must stop */
        RED
    }

    private final String id;
    private final Hex3D position;
    private SignalState state;
    private String blockSectionId;
    private String ownerCorporationId;

    /**
     * Creates a rail signal.
     *
     * @param position the hex position
     * @param blockSectionId the block section this signal controls
     * @param ownerCorporationId the corporation that owns this signal
     */
    public RailSignal(Hex3D position, String blockSectionId, String ownerCorporationId) {
        this.id = UUID.randomUUID().toString();
        this.position = position;
        this.state = SignalState.GREEN;
        this.blockSectionId = blockSectionId;
        this.ownerCorporationId = ownerCorporationId;
    }

    /** @return the unique signal ID */
    public String getId() {
        return id;
    }

    /** @return the hex position */
    public Hex3D getPosition() {
        return position;
    }

    /** @return the current signal state */
    public SignalState getState() {
        return state;
    }

    /**
     * Sets the signal state.
     *
     * @param state the new state
     */
    public void setState(SignalState state) {
        this.state = state;
    }

    /** @return true if the signal is green (clear) */
    public boolean isClear() {
        return state == SignalState.GREEN;
    }

    /** @return the block section ID */
    public String getBlockSectionId() {
        return blockSectionId;
    }

    /** @return the owner corporation ID */
    public String getOwnerCorporationId() {
        return ownerCorporationId;
    }

    @Override
    public String toString() {
        return String.format("RailSignal{id=%s, pos=%s, state=%s}", id, position, state);
    }
}
