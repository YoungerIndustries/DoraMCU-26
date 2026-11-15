package com.yisy.modules.doramcu.traveller;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing economic events in a Traveller sector.
 * <p>
 * The EconomicEventService generates, tracks, and applies economic events
 * that modify market conditions, commodity prices, and trade route profitability.
 * Events are tick-based and automatically expire after their duration.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Random event generation based on sector characteristics</li>
 *   <li>Event application to MarketConditions for affected worlds</li>
 *   <li>Price modifier calculation combining multiple active events</li>
 *   <li>Event lifecycle management (activation, expiration, deactivation)</li>
 *   <li>Event history tracking for analysis</li>
 * </ul>
 * <p>
 * Integration with {@link TradeSimulator}:
 * </p>
 * <ul>
 *   <li>Call {@link #advanceTick(int)} at each simulation tick</li>
 *   <li>Use {@link #getPriceModifier(TravellerWorld, Commodity)} for modified prices</li>
 *   <li>Use {@link #applyToMarketConditions(TravellerWorld)} to update market conditions</li>
 * </ul>
 */

public class EconomicEventService {

    private final TravellerSector sector;
    private final Random random;
    private final Map<String, EconomicEvent> activeEvents;
    private final List<EconomicEvent> eventHistory;
    private final int eventChancePerTick;
    private final int maxActiveEvents;

    /**
     * Creates an economic event service for the given sector.
     *
     * @param sector the sector to manage events for
     */
    public EconomicEventService(TravellerSector sector) {
        this(sector, new Random());
    }

    /**
     * Creates an economic event service with a specific random seed.
     *
     * @param sector the sector
     * @param random the random generator for event generation
     */
    public EconomicEventService(TravellerSector sector, Random random) {
        this(sector, random, 15, 10);
    }

    /**
     * Creates an economic event service with custom parameters.
     *
     * @param sector the sector
     * @param random the random generator
     * @param eventChancePerTick percentage chance of new event per tick (1-100)
     * @param maxActiveEvents maximum concurrent active events
     */
    public EconomicEventService(TravellerSector sector, Random random,
                                 int eventChancePerTick, int maxActiveEvents) {
        this.sector = sector;
        this.random = random;
        this.activeEvents = new LinkedHashMap<>();
        this.eventHistory = new ArrayList<>();
        this.eventChancePerTick = Math.max(1, Math.min(100, eventChancePerTick));
        this.maxActiveEvents = Math.max(1, maxActiveEvents);
    }

    // ---- Accessors ----

    /**
     * Returns the sector being managed.
     *
     * @return the sector
     */
    public TravellerSector getSector() {
        return sector;
    }

    /**
     * Returns all currently active events (immutable).
     *
     * @return map of event ID to event
     */
    public Map<String, EconomicEvent> getActiveEvents() {
        return Collections.unmodifiableMap(activeEvents);
    }

    /**
     * Returns the event history (immutable).
     *
     * @return list of expired/deactivated events
     */
    public List<EconomicEvent> getEventHistory() {
        return Collections.unmodifiableList(eventHistory);
    }

    /**
     * Returns the number of active events.
     *
     * @return active event count
     */
    public int getActiveEventCount() {
        return activeEvents.size();
    }

    /**
     * Returns the event chance per tick.
     *
     * @return percentage chance (1-100)
     */
    public int getEventChancePerTick() {
        return eventChancePerTick;
    }

    // ---- Event Generation ----

    /**
     * Generates a random economic event and adds it to active events.
     * <p>
     * The event's scope and affected worlds are determined by the event type
     * and sector characteristics.
     * </p>
     *
     * @param currentTick the current simulation tick
     * @return the generated event, or null if max active events reached
     */
    public EconomicEvent generateRandomEvent(int currentTick) {
        if (activeEvents.size() >= maxActiveEvents) {
            return null;
        }

        EconomicEvent.Builder builder = EconomicEvent.randomBuilder(random);
        builder.startTick(currentTick);

        // Determine scope based on event type

        EconomicEvent.Scope scope;
        double sectorChance = random.nextDouble();
        if (sectorChance < 0.1) {
            scope = EconomicEvent.Scope.SECTOR;
        } else if (sectorChance < 0.4) {
            scope = EconomicEvent.Scope.SUB_SECTOR;
        } else {
            scope = EconomicEvent.Scope.WORLD;
        }

        builder.scope(scope);

        // Determine affected worlds
        List<TravellerWorld> worlds = sector.getAllWorlds();
        if (worlds.isEmpty()) {
            return null;
        }

        if (scope == EconomicEvent.Scope.SECTOR) {
            builder.affectedWorlds(worlds);
        } else if (scope == EconomicEvent.Scope.SUB_SECTOR) {
            // Select a random cluster of adjacent worlds
            int clusterSize = Math.min(5, worlds.size());
            List<TravellerWorld> cluster = new ArrayList<>();
            TravellerWorld startWorld = worlds.get(random.nextInt(worlds.size()));
            cluster.add(startWorld);

            // Add adjacent worlds
            for (TravellerWorld world : worlds) {
                if (cluster.size() >= clusterSize) break;
                if (!cluster.contains(world) && isAdjacent(startWorld, world)) {
                    cluster.add(world);
                }
            }
            builder.affectedWorlds(cluster);
        } else {
            // Single world
            TravellerWorld world = worlds.get(random.nextInt(worlds.size()));
            builder.affectedWorld(world);
        }

        EconomicEvent event = builder.build();
        activeEvents.put(event.getId(), event);
        return event;
    }

    /**
     * Creates a specific economic event.
     *
     * @param type the event type
     * @param severity the severity (1-10)
     * @param durationTicks the duration in ticks
     * @param scope the event scope
     * @param affectedWorlds the affected worlds (ignored for SECTOR scope)
     * @param startTick the start tick
     * @return the created event, or null if max active events reached
     */
    public EconomicEvent createEvent(EconomicEventType type, int severity, int durationTicks,
                                      EconomicEvent.Scope scope, List<TravellerWorld> affectedWorlds,
                                      int startTick) {
        if (activeEvents.size() >= maxActiveEvents) {
            return null;
        }

        EconomicEvent event = EconomicEvent.builder(type)
                .severity(severity)
                .durationTicks(durationTicks)
                .scope(scope)
                .affectedWorlds(affectedWorlds != null ? affectedWorlds : new ArrayList<>())
                .startTick(startTick)
                .build();

        activeEvents.put(event.getId(), event);
        return event;
    }

    /**
     * Checks if a new event should be generated and generates it if so.
     *
     * @param currentTick the current simulation tick
     * @return the generated event, or null
     */
    public EconomicEvent maybeGenerateEvent(int currentTick) {
        if (random.nextInt(100) < eventChancePerTick) {
            return generateRandomEvent(currentTick);
        }
        return null;
    }

    // ---- Event Lifecycle ----

    /**
     * Advances the simulation by one tick, expiring expired events.
     *
     * @param currentTick the current simulation tick
     * @return list of events that expired this tick
     */
    public List<EconomicEvent> advanceTick(int currentTick) {
        List<EconomicEvent> expired = new ArrayList<>();

        Iterator<Map.Entry<String, EconomicEvent>> iterator = activeEvents.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, EconomicEvent> entry = iterator.next();
            EconomicEvent event = entry.getValue();

            if (!event.isActiveAt(currentTick)) {
                event.deactivate();
                iterator.remove();
                expired.add(event);
                eventHistory.add(event);
            }
        }

        return expired;
    }

    /**
     * Manually deactivates an event by ID.
     *
     * @param eventId the event ID
     * @return the deactivated event, or null if not found
     */
    public EconomicEvent deactivateEvent(String eventId) {
        EconomicEvent event = activeEvents.remove(eventId);
        if (event != null) {
            event.deactivate();
            eventHistory.add(event);
        }
        return event;
    }

    /**
     * Clears all active events.
     *
     * @return the number of cleared events
     */
    public int clearAllEvents() {
        int count = activeEvents.size();
        for (EconomicEvent event : activeEvents.values()) {
            event.deactivate();
            eventHistory.add(event);
        }
        activeEvents.clear();
        return count;
    }

    // ---- Price Modifiers ----

    /**
     * Calculates the combined price modifier for a commodity on a world,
     * considering all active events affecting that world.
     * <p>
     * Multiple events are combined multiplicatively.
     * </p>
     *
     * @param world the world
     * @param commodity the commodity
     * @return the combined price multiplier (1.0 = no change)
     */
    public double getPriceModifier(TravellerWorld world, Commodity commodity) {
        double modifier = 1.0;

        for (EconomicEvent event : activeEvents.values()) {
            if (event.affectsWorld(world)) {
                modifier *= event.getPriceModifier(commodity);
            }
        }

        // Clamp to reasonable range
        return Math.max(0.1, Math.min(5.0, modifier));
    }

    /**
     * Returns all active events affecting a specific world.
     *
     * @param world the world
     * @return list of active events affecting the world
     */
    public List<EconomicEvent> getEventsForWorld(TravellerWorld world) {
        return activeEvents.values().stream()
                .filter(event -> event.affectsWorld(world))
                .collect(Collectors.toList());
    }

    /**
     * Returns all active events of a specific type.
     *
     * @param type the event type
     * @return list of active events of the given type
     */
    public List<EconomicEvent> getEventsByType(EconomicEventType type) {
        return activeEvents.values().stream()
                .filter(event -> event.getType() == type)
                .collect(Collectors.toList());
    }

    // ---- Market Conditions Integration ----

    /**
     * Applies all active events to a world's market conditions.
     * <p>
     * This method updates the MarketConditions for affected commodities
     * based on the active events' condition overrides.
     * </p>
     *
     * @param world the world
     * @param marketConditions the market conditions to update
     */
    public void applyToMarketConditions(TravellerWorld world, MarketConditions marketConditions) {
        List<EconomicEvent> events = getEventsForWorld(world);
        if (events.isEmpty()) {
            return;
        }

        for (Commodity commodity : Commodity.values()) {
            // Find the strongest condition override from active events
            MarketConditions.Condition strongestOverride = null;

            for (EconomicEvent event : events) {
                MarketConditions.Condition override = event.getConditionOverride(commodity);
                if (override != null) {
                    // NONE > SHORT > NORMAL > SURPLUS (in terms of restriction)
                    if (strongestOverride == null || isStronger(override, strongestOverride)) {
                        strongestOverride = override;
                    }
                }
            }

            if (strongestOverride != null) {
                marketConditions.setCondition(commodity, strongestOverride);
            }
        }
    }

    /**
     * Determines if one condition is stronger (more restrictive) than another.
     */
    private boolean isStronger(MarketConditions.Condition newCondition, MarketConditions.Condition current) {
        int newStrength = getConditionStrength(newCondition);
        int currentStrength = getConditionStrength(current);
        return newStrength > currentStrength;
    }

    private int getConditionStrength(MarketConditions.Condition condition) {
        return switch (condition) {
            case NONE -> 4;
            case SHORT -> 3;
            case NORMAL -> 2;
            case SURPLUS -> 1;
        };
    }

    // ---- Utility ----

    /**
     * Checks if two worlds are adjacent (within 2 parsecs).
     */
    private boolean isAdjacent(TravellerWorld w1, TravellerWorld w2) {
        int dx = Math.abs(w1.getSectorX() - w2.getSectorX());
        int dy = Math.abs(w1.getSectorY() - w2.getSectorY());
        return (dx + dy) <= 2;
    }

    /**
     * Returns a summary of the current economic event state.
     *
     * @return a formatted summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Economic Events Summary\n");
        sb.append("=======================\n");
        sb.append("Sector: ").append(sector.getName()).append("\n");
        sb.append("Active Events: ").append(activeEvents.size()).append("\n");
        sb.append("Event History: ").append(eventHistory.size()).append("\n");

        if (!activeEvents.isEmpty()) {
            sb.append("\nActive Events:\n");
            for (EconomicEvent event : activeEvents.values()) {
                sb.append("  - ").append(event.getDescription()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
