package com.yisy.modules.doramcu.traveller;

import java.time.Instant;
import java.util.*;

/**
 * An economic event affecting trade in a Traveller sector.
 * <p>
 * Economic events are temporary conditions that modify market conditions,
 * commodity prices, and trade route profitability for a specific duration.
 * Events can affect individual worlds, sub-sectors, or entire sectors.
 * </p>
 * <p>
 * Each event has:
 * </p>
 * <ul>
 *   <li>A type (Piracy, Embargo, Boom, Bust, War, Plague, etc.)</li>
 *   <li>A severity (1-10) determining impact magnitude</li>
 *   <li>A duration in simulation ticks</li>
 *   <li>A scope (world, sub-sector, or sector-wide)</li>
 *   <li>A start tick and expiration tick</li>
 * </ul>
 * <p>
 * Events are applied to {@link MarketConditions} via {@link EconomicEventService}.
 * </p>
 */
public class EconomicEvent {

    private final String id;
    private final EconomicEventType type;
    private final int severity;
    private final int durationTicks;
    private final int startTick;
    private final int endTick;
    private final Scope scope;
    private final List<TravellerWorld> affectedWorlds;
    private final String description;
    private final Instant createdAt;
    private boolean active;

    /**
     * Scope of the economic event.
     */
    public enum Scope {
        /** Affects a single world */
        WORLD,
        /** Affects a sub-sector (group of adjacent worlds) */
        SUB_SECTOR,
        /** Affects the entire sector */
        SECTOR
    }

    /**
     * Builder for creating EconomicEvent instances.
     */
    public static class Builder {
        private EconomicEventType type;
        private int severity;
        private int durationTicks;
        private int startTick;
        private Scope scope = Scope.WORLD;
        private List<TravellerWorld> affectedWorlds = new ArrayList<>();
        private String customDescription;

        public Builder type(EconomicEventType type) {
            this.type = type;
            return this;
        }

        public Builder severity(int severity) {
            this.severity = severity;
            return this;
        }

        public Builder durationTicks(int durationTicks) {
            this.durationTicks = durationTicks;
            return this;
        }

        public Builder startTick(int startTick) {
            this.startTick = startTick;
            return this;
        }

        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder affectedWorlds(List<TravellerWorld> worlds) {
            this.affectedWorlds = new ArrayList<>(worlds);
            return this;
        }

        public Builder affectedWorld(TravellerWorld world) {
            this.affectedWorlds.add(world);
            return this;
        }

        public Builder customDescription(String description) {
            this.customDescription = description;
            return this;
        }

        public EconomicEvent build() {
            return new EconomicEvent(this);
        }
    }

    /**
     * Creates a builder for a new economic event.
     *
     * @param type the event type
     * @return a new builder
     */
    public static Builder builder(EconomicEventType type) {
        return new Builder().type(type);
    }

    /**
     * Creates a builder for a random economic event.
     *
     * @param random the random generator
     * @return a new builder with random type and severity
     */
    public static Builder randomBuilder(Random random) {
        EconomicEventType[] types = EconomicEventType.values();
        EconomicEventType type = types[random.nextInt(types.length)];
        int severity = type.getMinSeverity() + random.nextInt(type.getMaxSeverity() - type.getMinSeverity() + 1);
        int duration = type.getMinDuration() + random.nextInt(type.getMaxDuration() - type.getMinDuration() + 1);
        return new Builder()
                .type(type)
                .severity(severity)
                .durationTicks(duration);
    }

    private EconomicEvent(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.durationTicks = builder.durationTicks;
        this.startTick = builder.startTick;
        this.endTick = builder.startTick + builder.durationTicks;
        this.scope = builder.scope;
        this.affectedWorlds = Collections.unmodifiableList(new ArrayList<>(builder.affectedWorlds));
        this.active = true;
        this.createdAt = Instant.now();

        // Build description
        StringBuilder desc = new StringBuilder();
        if (builder.customDescription != null) {
            desc.append(builder.customDescription);
        } else {
            desc.append(type.getDisplayName())
                    .append(" (severity ").append(severity).append(")")
                    .append(" for ").append(durationTicks).append(" ticks");
            if (scope == Scope.WORLD && !affectedWorlds.isEmpty()) {
                desc.append(" affecting ").append(affectedWorlds.size()).append(" world(s)");
            } else if (scope == Scope.SUB_SECTOR) {
                desc.append(" affecting a sub-sector");
            } else if (scope == Scope.SECTOR) {
                desc.append(" affecting the entire sector");
            }
        }
        this.description = desc.toString();

        // Generate UUID-based ID
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    // ---- Accessors ----

    public String getId() {
        return id;
    }

    public EconomicEventType getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getStartTick() {
        return startTick;
    }

    public int getEndTick() {
        return endTick;
    }

    public Scope getScope() {
        return scope;
    }

    public List<TravellerWorld> getAffectedWorlds() {
        return affectedWorlds;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Deactivates this event.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Checks if this event is active at the given tick.
     *
     * @param tick the current simulation tick
     * @return true if the event is active and the tick is within the event's duration
     */
    public boolean isActiveAt(int tick) {
        return active && tick >= startTick && tick < endTick;
    }

    /**
     * Returns the number of ticks remaining until the event expires.
     *
     * @param currentTick the current simulation tick
     * @return ticks remaining, or 0 if expired
     */
    public int getTicksRemaining(int currentTick) {
        if (!active || currentTick >= endTick) {
            return 0;
        }
        return Math.max(0, endTick - currentTick);
    }

    /**
     * Calculates the combined price modifier for a commodity,
     * considering this event's type and severity.
     *
     * @param commodity the commodity
     * @return the price multiplier (1.0 = no change)
     */
    public double getPriceModifier(Commodity commodity) {
        return type.getPriceModifier(commodity, severity);
    }

    /**
     * Determines the market condition override for a commodity.
     *
     * @param commodity the commodity
     * @return the market condition override, or null if no override
     */
    public MarketConditions.Condition getConditionOverride(Commodity commodity) {
        return type.getConditionOverride(commodity, severity);
    }

    /**
     * Checks if this event affects the given world.
     *
     * @param world the world to check
     * @return true if the world is affected
     */
    public boolean affectsWorld(TravellerWorld world) {
        if (scope == Scope.SECTOR) {
            return true;
        }
        return affectedWorlds.contains(world);
    }

    @Override
    public String toString() {
        return String.format("EconomicEvent{id=%s, type=%s, severity=%d, ticks=%d-%d, scope=%s, active=%s}",
                id, type, severity, startTick, endTick, scope, active);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EconomicEvent that = (EconomicEvent) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
