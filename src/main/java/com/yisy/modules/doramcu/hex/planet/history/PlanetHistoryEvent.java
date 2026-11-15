package com.yisy.modules.doramcu.hex.planet.history;

import com.yisy.modules.doramcu.hex.planet.CargoType;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a history event in the planet's mining, manufacturing, and transit log.
 * <p>
 * Each event tracks:
 * <ul>
 *   <li>The event type (production, processing, delivery, etc.)</li>
 *   <li>The cargo type and amount involved</li>
 *   <li>The source and destination entities</li>
 *   <li>The timestamp of the event</li>
 * </ul>
 * </p>
 */
public class PlanetHistoryEvent {

    public enum EventType {
        /** Cargo produced at a mine or primary industry */
        PRODUCTION,
        /** Cargo processed at a secondary/tertiary factory */
        PROCESSING,
        /** Cargo loaded onto a vehicle */
        LOADING,
        /** Cargo delivered to a station */
        DELIVERY,
        /** Cargo consumed by an industry */
        CONSUMPTION,
        /** Cargo decayed (perishable) */
        DECAY,
        /** Vehicle breakdown */
        BREAKDOWN,
        /** Vehicle repair */
        REPAIR
    }

    private final String id;
    private final EventType eventType;
    private final CargoType cargoType;
    private final int amount;
    private final String sourceId;
    private final String destinationId;
    private final String vehicleId;
    private final String itemId;
    private final Instant timestamp;
    private final int turn;

    /**
     * Creates a history event.
     *
     * @param eventType the type of event
     * @param cargoType the cargo type involved
     * @param amount the amount of cargo
     * @param sourceId the source entity ID
     * @param destinationId the destination entity ID
     * @param vehicleId the vehicle ID (if applicable)
     * @param itemId the item batch ID for traceability
     * @param turn the turn number
     */
    public PlanetHistoryEvent(EventType eventType, CargoType cargoType, int amount,
                               String sourceId, String destinationId,
                               String vehicleId, String itemId, int turn) {
        this.id = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.cargoType = cargoType;
        this.amount = amount;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.vehicleId = vehicleId;
        this.itemId = itemId;
        this.timestamp = Instant.now();
        this.turn = turn;
    }

    /** @return the unique event ID */
    public String getId() {
        return id;
    }

    /** @return the event type */
    public EventType getEventType() {
        return eventType;
    }

    /** @return the cargo type */
    public CargoType getCargoType() {
        return cargoType;
    }

    /** @return the amount of cargo */
    public int getAmount() {
        return amount;
    }

    /** @return the source entity ID */
    public String getSourceId() {
        return sourceId;
    }

    /** @return the destination entity ID */
    public String getDestinationId() {
        return destinationId;
    }

    /** @return the vehicle ID */
    public String getVehicleId() {
        return vehicleId;
    }

    /** @return the item batch ID for traceability */
    public String getItemId() {
        return itemId;
    }

    /** @return the timestamp */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** @return the turn number */
    public int getTurn() {
        return turn;
    }

    @Override
    public String toString() {
        return String.format("PlanetHistoryEvent{type=%s, cargo=%s, amount=%d, turn=%d}",
                eventType, cargoType, amount, turn);
    }
}
