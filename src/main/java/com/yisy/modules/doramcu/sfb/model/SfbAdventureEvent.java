package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Adventure events for SFB.
 * <p>
 * Predefined events with storyline references, XP rewards,
 * and severity levels for SFB campaigns.
 * </p>
 */
public class SfbAdventureEvent {

    private static final Random RANDOM = new Random();

    private final List<AdventureEventEntry> events = new ArrayList<>();

    public SfbAdventureEvent() {
        initializeEvents();
    }

    private void initializeEvents() {
        events.add(new AdventureEventEntry.Builder()
                .name("Klingon Border Skirmish")
                .description("Klingon forces test Federation border defenses")
                .eventType(EventType.COMBAT)
                .severity(Severity.HIGH)
                .xpReward(200)
                .storyline("Federation-Klingon Alliance")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Romulan Cloaking Device Discovery")
                .description("Romulan engineer offers cloaking technology")
                .eventType(EventType.DIPLOMACY)
                .severity(Severity.CRITICAL)
                .xpReward(500)
                .storyline("Romulan Star Empire")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Borg Encounter")
                .description("Borg cube detected in nearby sector")
                .eventType(EventType.COMBAT)
                .severity(Severity.CRITICAL)
                .xpReward(1000)
                .storyline("Borg Threat")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("First Contact")
                .description("Unknown species detected")
                .eventType(EventType.EXPLORATION)
                .severity(Severity.MEDIUM)
                .xpReward(300)
                .storyline("Exploration")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Dilithium Mine Collapse")
                .description("Mine accident traps crew underground")
                .eventType(EventType.SURVIVAL)
                .severity(Severity.HIGH)
                .xpReward(150)
                .storyline("Resource Management")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Warp Core Breach")
                .description("Critical warp core malfunction")
                .eventType(EventType.SURVIVAL)
                .severity(Severity.CRITICAL)
                .xpReward(400)
                .storyline("Ship Emergency")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Suliban Conspiracy")
                .description("Suliban shape-shifters infiltrate command")
                .eventType(EventType.STEALTH)
                .severity(Severity.HIGH)
                .xpReward(350)
                .storyline("Dominion War")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Ancient Artifact")
                .description("Pre-Federation technology discovered")
                .eventType(EventType.EXPLORATION)
                .severity(Severity.MEDIUM)
                .xpReward(250)
                .storyline("Ancient History")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Trade Dispute")
                .description("Commercial conflict between factions")
                .eventType(EventType.DIPLOMACY)
                .severity(Severity.LOW)
                .xpReward(100)
                .storyline("Economy")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Pirate Raid")
                .description("Merchant convoy under attack")
                .eventType(EventType.COMBAT)
                .severity(Severity.MEDIUM)
                .xpReward(175)
                .storyline("Security")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Scientific Breakthrough")
                .description("Major discovery in warp theory")
                .eventType(EventType.EXPLORATION)
                .severity(Severity.LOW)
                .xpReward(200)
                .storyline("Science")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Political Assassination")
                .description("Ambassador killed, war threatens")
                .eventType(EventType.DIPLOMACY)
                .severity(Severity.CRITICAL)
                .xpReward(600)
                .storyline("Politics")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Wormhole Discovery")
                .description("Stable wormhole opens new trade route")
                .eventType(EventType.EXPLORATION)
                .severity(Severity.HIGH)
                .xpReward(450)
                .storyline("Exploration")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Mutiny")
                .description("Crew challenges command authority")
                .eventType(EventType.SOCIAL)
                .severity(Severity.HIGH)
                .xpReward(300)
                .storyline("Internal Conflict")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Natural Disaster")
                .description("Planetary catastrophe requires aid")
                .eventType(EventType.SURVIVAL)
                .severity(Severity.HIGH)
                .xpReward(250)
                .storyline("Humanitarian")
                .build());

        events.add(new AdventureEventEntry.Builder()
                .name("Tholian Intercept")
                .description("Tholian web blocks passage")
                .eventType(EventType.COMBAT)
                .severity(Severity.MEDIUM)
                .xpReward(200)
                .storyline("Tholian Assembly")
                .build());
    }

    /**
     * Rolls a random adventure event.
     */
    public AdventureEventEntry rollEvent() {
        return events.get(RANDOM.nextInt(events.size()));
    }

    /**
     * Gets events by severity.
     */
    public List<AdventureEventEntry> getEventsBySeverity(Severity severity) {
        List<AdventureEventEntry> result = new ArrayList<>();
        for (AdventureEventEntry event : events) {
            if (event.getSeverity() == severity) {
                result.add(event);
            }
        }
        return result;
    }

    public List<AdventureEventEntry> getAllEvents() {
        return events;
    }

    /**
     * Types of adventure events.
     */
    public enum EventType {
        COMBAT, DIPLOMACY, EXPLORATION, SURVIVAL, STEALTH, SOCIAL
    }

    /**
     * Severity levels.
     */
    public enum Severity {
        LOW("Low"), MEDIUM("Medium"), HIGH("High"), CRITICAL("Critical");

        private final String name;

        Severity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * A single adventure event entry.
     */
    public static class AdventureEventEntry {
        private final String name;
        private final String description;
        private final EventType eventType;
        private final Severity severity;
        private final int xpReward;
        private final String storyline;

        private AdventureEventEntry(Builder builder) {
            this.name = builder.name;
            this.description = builder.description;
            this.eventType = builder.eventType;
            this.severity = builder.severity;
            this.xpReward = builder.xpReward;
            this.storyline = builder.storyline;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public EventType getEventType() { return eventType; }
        public Severity getSeverity() { return severity; }
        public int getXpReward() { return xpReward; }
        public String getStoryline() { return storyline; }

        public static class Builder {
            private String name;
            private String description;
            private EventType eventType;
            private Severity severity;
            private int xpReward;
            private String storyline;

            public Builder name(String name) { this.name = name; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder eventType(EventType eventType) { this.eventType = eventType; return this; }
            public Builder severity(Severity severity) { this.severity = severity; return this; }
            public Builder xpReward(int xpReward) { this.xpReward = xpReward; return this; }
            public Builder storyline(String storyline) { this.storyline = storyline; return this; }

            public AdventureEventEntry build() {
                return new AdventureEventEntry(this);
            }
        }
    }
}
