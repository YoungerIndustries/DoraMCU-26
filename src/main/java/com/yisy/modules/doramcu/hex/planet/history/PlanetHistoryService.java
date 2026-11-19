package com.yisy.modules.doramcu.hex.planet.history;

import com.yisy.modules.doramcu.hex.planet.CargoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Logs all mining, manufacturing, and transit events with full traceability.
 * <p>
 * The service:
 * <ul>
 *   <li>Logs cargo creation at mines</li>
 *   <li>Logs processing at factories</li>
 *   <li>Logs delivery at stations</li>
 *   <li>Tracks item lifecycle: origin mine → factories → destination</li>
 *   <li>Provides history queries by item ID</li>
 * </ul>
 * </p>
 */
@Service
public class PlanetHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PlanetHistoryService.class);

    /** Maximum events to keep in memory */
    private static final int MAX_EVENTS = 10000;

    /** All history events */
    private final List<PlanetHistoryEvent> events;

    /** Item ID -> list of events for that item */
    private final Map<String, List<PlanetHistoryEvent>> itemHistory;

    public PlanetHistoryService() {
        this.events = new CopyOnWriteArrayList<>();
        this.itemHistory = new HashMap<>();
    }

    /**
     * Logs a production event (cargo created at a mine/industry).
     *
     * @param cargoType the cargo type
     * @param amount the amount produced
     * @param sourceId the industry ID
     * @param itemId the item batch ID
     * @param turn the turn number
     */
    public void logProduction(CargoType cargoType, int amount, String sourceId,
                               String itemId, int turn) {
        PlanetHistoryEvent event = new PlanetHistoryEvent(
                EventType.PRODUCTION, cargoType, amount,
                sourceId, null, null, itemId, turn);
        addEvent(event, itemId);
    }

    /**
     * Logs a processing event (cargo processed at a factory).
     *
     * @param inputType the input cargo type
     * @param outputType the output cargo type
     * @param amount the amount processed
     * @param sourceId the factory ID
     * @param inputItemId the input item ID
     * @param outputItemId the output item ID
     * @param turn the turn number
     */
    public void logProcessing(CargoType inputType, CargoType outputType, int amount,
                               String sourceId, String inputItemId, String outputItemId,
                               int turn) {
        // Log consumption of input
        PlanetHistoryEvent consumeEvent = new PlanetHistoryEvent(
                EventType.CONSUMPTION, inputType, amount,
                null, sourceId, null, inputItemId, turn);
        addEvent(consumeEvent, inputItemId);

        // Log production of output
        PlanetHistoryEvent produceEvent = new PlanetHistoryEvent(
                EventType.PROCESSING, outputType, amount,
                sourceId, null, null, outputItemId, turn);
        addEvent(produceEvent, outputItemId);
    }

    /**
     * Logs a loading event (cargo loaded onto a vehicle).
     *
     * @param cargoType the cargo type
     * @param amount the amount loaded
     * @param sourceId the station ID
     * @param vehicleId the vehicle ID
     * @param itemId the item batch ID
     * @param turn the turn number
     */
    public void logLoading(CargoType cargoType, int amount, String sourceId,
                            String vehicleId, String itemId, int turn) {
        PlanetHistoryEvent event = new PlanetHistoryEvent(
                EventType.LOADING, cargoType, amount,
                sourceId, null, vehicleId, itemId, turn);
        addEvent(event, itemId);
    }

    /**
     * Logs a delivery event (cargo delivered to a station).
     *
     * @param cargoType the cargo type
     * @param amount the amount delivered
     * @param destinationId the station ID
     * @param vehicleId the vehicle ID
     * @param itemId the item batch ID
     * @param turn the turn number
     */
    public void logDelivery(CargoType cargoType, int amount, String destinationId,
                             String vehicleId, String itemId, int turn) {
        PlanetHistoryEvent event = new PlanetHistoryEvent(
                EventType.DELIVERY, cargoType, amount,
                null, destinationId, vehicleId, itemId, turn);
        addEvent(event, itemId);
    }

    /**
     * Logs a decay event (perishable cargo expired).
     *
     * @param cargoType the cargo type
     * @param amount the amount decayed
     * @param sourceId the station ID
     * @param itemId the item batch ID
     * @param turn the turn number
     */
    public void logDecay(CargoType cargoType, int amount, String sourceId,
                          String itemId, int turn) {
        PlanetHistoryEvent event = new PlanetHistoryEvent(
                EventType.DECAY, cargoType, amount,
                sourceId, null, null, itemId, turn);
        addEvent(event, itemId);
    }

    /**
     * Gets the full history for an item.
     *
     * @param itemId the item batch ID
     * @return list of events for this item
     */
    public List<PlanetHistoryEvent> getItemHistory(String itemId) {
        return itemHistory.getOrDefault(itemId, Collections.emptyList());
    }

    /**
     * Gets recent events.
     *
     * @param limit maximum number of events
     * @return list of recent events
     */
    public List<PlanetHistoryEvent> getRecentEvents(int limit) {
        int size = Math.min(limit, events.size());
        return new ArrayList<>(events.subList(Math.max(0, events.size() - size), events.size()));
    }

    /**
     * Gets events by type.
     *
     * @param eventType the event type
     * @param limit maximum number of events
     * @return list of events
     */
    public List<PlanetHistoryEvent> getEventsByType(EventType eventType, int limit) {
        return events.stream()
                .filter(e -> e.getEventType() == eventType)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets events for a source entity.
     *
     * @param sourceId the source entity ID
     * @param limit maximum number of events
     * @return list of events
     */
    public List<PlanetHistoryEvent> getEventsBySource(String sourceId, int limit) {
        return events.stream()
                .filter(e -> sourceId.equals(e.getSourceId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Adds an event to the history.
     */
    private void addEvent(PlanetHistoryEvent event, String itemId) {
        events.add(event);

        // Trim old events
        while (events.size() > MAX_EVENTS) {
            events.remove(0);
        }

        // Add to item history
        if (itemId != null) {
            itemHistory.computeIfAbsent(itemId, k -> new ArrayList<>()).add(event);
        }

        log.debug("Logged history event: {}", event);
    }

    /**
     * Generates a unique item batch ID.
     *
     * @param cargoType the cargo type
     * @param sourceId the source entity ID
     * @return the item batch ID
     */
    public String generateItemId(CargoType cargoType, String sourceId) {
        return cargoType.name().toLowerCase() + "-" +
                (sourceId != null ? sourceId : "unknown") + "-" +
                System.nanoTime();
    }
}
