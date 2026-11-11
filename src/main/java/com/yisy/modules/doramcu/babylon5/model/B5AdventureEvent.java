package com.yisy.modules.doramcu.babylon5.model;

import java.util.*;

/**
 * Adventure events in the Babylon 5 universe.
 * <p>
 * Adventure events are narrative-driven occurrences that can happen during
 * exploration, travel, or downtime. They provide mission hooks, character
 * interactions, and storyline progression opportunities.
 * </p>
 * <p>
 * Events are categorized by type and severity, with B5-themed content including
 * Shadow War events, Vorlon mysteries, Earth Force operations, and more.
 * </p>
 */
public class B5AdventureEvent {

    /**
     * Type of adventure event.
     */
    public enum EventType {
        /**
         * A mission or quest opportunity.
         */
        MISSION,

        /**
         * A diplomatic event between factions.
         */
        DIPLOMACY,

        /**
         * A combat encounter.
         */
        COMBAT,

        /**
         * A discovery or exploration event.
         */
        DISCOVERY,

        /**
         * A trade or economy event.
         */
        TRADE,

        /**
         * A psionic event.
         */
        PSIONIC,

        /**
         * A storyline-related event tied to B5 campaigns.
         */
        STORYLINE,

        /**
         * A character interaction event.
         */
        CHARACTER,

        /**
         * A system-wide event affecting the galaxy.
         */
        SYSTEM_WIDE,

        /**
         * A random hazard or environmental event.
         */
        HAZARD
    }

    /**
     * Severity of the event (1-10).
     */
    public enum Severity {
        /**
         * Trivial — minor inconvenience.
         */
        TRIVIAL(1),

        /**
         * Minor — slight impact.
         */
        MINOR(3),

        /**
         * Moderate — noticeable impact.
         */
        MODERATE(5),

        /**
         * Major — significant impact.
         */
        MAJOR(7),

        /**
         * Critical — galaxy-altering impact.
         */
        CRITICAL(10);

        private final int value;

        Severity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * A single adventure event.
     */
    public static class AdventureEventEntry {
        private final String id;
        private final String title;
        private final String description;
        private final EventType type;
        private final Severity severity;
        private final List<String> prerequisites;
        private final List<String> possibleOutcomes;
        private final String storylineRef;
        private final int xpReward;

        public static class Builder {
            private String id;
            private String title;
            private String description;
            private EventType type;
            private Severity severity = Severity.MODERATE;
            private List<String> prerequisites = new ArrayList<>();
            private List<String> possibleOutcomes = new ArrayList<>();
            private String storylineRef;
            private int xpReward = 0;

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder type(EventType type) {
                this.type = type;
                return this;
            }

            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public Builder prerequisite(String prerequisite) {
                this.prerequisites.add(prerequisite);
                return this;
            }

            public Builder outcome(String outcome) {
                this.possibleOutcomes.add(outcome);
                return this;
            }

            public Builder storylineRef(String storylineRef) {
                this.storylineRef = storylineRef;
                return this;
            }

            public Builder xpReward(int xpReward) {
                this.xpReward = xpReward;
                return this;
            }

            public AdventureEventEntry build() {
                return new AdventureEventEntry(
                        id != null ? UUID.randomUUID().toString() : null,
                        title,
                        description,
                        type,
                        severity,
                        Collections.unmodifiableList(prerequisites),
                        Collections.unmodifiableList(possibleOutcomes),
                        storylineRef,
                        xpReward
                );
            }
        }

        private AdventureEventEntry(String id, String title, String description,
                                     EventType type, Severity severity,
                                     List<String> prerequisites,
                                     List<String> possibleOutcomes,
                                     String storylineRef, int xpReward) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
            this.severity = severity;
            this.prerequisites = prerequisites;
            this.possibleOutcomes = possibleOutcomes;
            this.storylineRef = storylineRef;
            this.xpReward = xpReward;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public EventType getType() {
            return type;
        }

        public Severity getSeverity() {
            return severity;
        }

        public List<String> getPrerequisites() {
            return prerequisites;
        }

        public List<String> getPossibleOutcomes() {
            return possibleOutcomes;
        }

        public String getStorylineRef() {
            return storylineRef;
        }

        public int getXpReward() {
            return xpReward;
        }

        public boolean hasPrerequisites() {
            return !prerequisites.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s — %s", severity, title, description);
        }
    }

    // ---- Predefined B5 Adventure Events ----

    private static final List<AdventureEventEntry> PREDEFINED_EVENTS = List.of(
            // Shadow War Events
            new AdventureEventEntry.Builder()
                    .title("Shadow Fleet Detected")
                    .description("Long-range sensors detect a massive Shadow fleet assembling in a nearby sector. The Shadow War may be resuming.")
                    .type(EventType.STORYLINE)
                    .severity(Severity.CRITICAL)
                    .outcome("Warn Earth Force immediately")
                    .outcome("Investigate the fleet composition")
                    .outcome("Evacuate nearby colonies")
                    .storylineRef("SHADOW_WAR")
                    .xpReward(500)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Wormhole Network Activation")
                    .description("Multiple wormholes activate simultaneously across the sector — a sign of Shadow manipulation.")
                    .type(EventType.SYSTEM_WIDE)
                    .severity(Severity.MAJOR)
                    .outcome("Map the wormhole network")
                    .outcome("Investigate the activation source")
                    .outcome("Report to Vorlon contacts")
                    .storylineRef("SHADOW_WAR")
                    .xpReward(300)
                    .build(),

            // Vorlon Mystery Events
            new AdventureEventEntry.Builder()
                    .title("Vorlon Artifact Discovery")
                    .description("An ancient Vorlon artifact is discovered on a remote planet — it responds to psionic contact.")
                    .type(EventType.DISCOVERY)
                    .severity(Severity.MAJOR)
                    .outcome("Study the artifact carefully")
                    .outcome("Activate the artifact")
                    .outcome("Secure the site for further study")
                    .xpReward(250)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Vorlon Envoy Contact")
                    .description("A mysterious figure claims to be a Vorlon envoy — offers cryptic guidance about the coming conflict.")
                    .type(EventType.CHARACTER)
                    .severity(Severity.MODERATE)
                    .outcome("Listen to the envoy's message")
                    .outcome("Question the envoy's identity")
                    .outcome("Refuse to engage")
                    .xpReward(150)
                    .build(),

            // Earth Force Mission Events
            new AdventureEventEntry.Builder()
                    .title("Earth Force Patrol Duty")
                    .description("Earth Force requests your assistance patrolling a contested border region.")
                    .type(EventType.MISSION)
                    .severity(Severity.MODERATE)
                    .outcome("Accept the patrol mission")
                    .outcome("Negotiate better terms")
                    .outcome("Decline politely")
                    .xpReward(100)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Babylon 5 Emergency")
                    .description("Babylon 5 station is under attack — all available vessels are requested to assist.")
                    .type(EventType.COMBAT)
                    .severity(Severity.CRITICAL)
                    .outcome("Rush to defend the station")
                    .outcome("Evacuate civilians first")
                    .outcome("Coordinate with Earth Force")
                    .storylineRef("EARTH_MIND_WAR")
                    .xpReward(400)
                    .build(),

            // Diplomatic Events
            new AdventureEventEntry.Builder()
                    .title("Narn-Centauri Peace Talks")
                    .description("Peace negotiations between the Narn Regime and Centauri Prime have broken down — mediation needed.")
                    .type(EventType.DIPLOMACY)
                    .severity(Severity.MAJOR)
                    .outcome("Mediate between the factions")
                    .outcome("Support the Narn position")
                    .outcome("Support the Centauri position")
                    .xpReward(200)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Minbari Religious Crisis")
                    .description("A schism in the Minbari religious councils threatens to split the race — your counsel is sought.")
                    .type(EventType.DIPLOMACY)
                    .severity(Severity.MAJOR)
                    .outcome("Advise the Warrior Caste")
                    .outcome("Advise the Religious Caste")
                    .outcome("Remain neutral and observe")
                    .xpReward(200)
                    .build(),

            // Trade Events
            new AdventureEventEntry.Builder()
                    .title("Centauri Wine Monopoly")
                    .description("A Centauri noble offers exclusive rights to distribute fine wines — but at a steep price.")
                    .type(EventType.TRADE)
                    .severity(Severity.MINOR)
                    .outcome("Accept the monopoly deal")
                    .outcome("Negotiate better terms")
                    .outcome("Find alternative suppliers")
                    .xpReward(75)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Black Market Opportunity")
                    .description("A contact offers access to a black market dealing in Shadow technology — high risk, high reward.")
                    .type(EventType.TRADE)
                    .severity(Severity.MAJOR)
                    .outcome("Investigate the market")
                    .outcome("Report to authorities")
                    .outcome("Purchase Shadow tech")
                    .xpReward(250)
                    .build(),

            // Psionic Events
            new AdventureEventEntry.Builder()
                    .title("Psi Corps Recruitment Drive")
                    .description("Psi Corps operatives are conducting a recruitment drive — your crew member shows potential.")
                    .type(EventType.PSIONIC)
                    .severity(Severity.MODERATE)
                    .outcome("Allow the recruitment")
                    .outcome("Protect your crew member")
                    .outcome("Negotiate terms with Psi Corps")
                    .xpReward(150)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Psionic Resonance Storm")
                    .description("A psionic resonance storm sweeps through the sector — psi-sensitive individuals are affected.")
                    .type(EventType.HAZARD)
                    .severity(Severity.MAJOR)
                    .outcome("Shield your crew")
                    .outcome("Investigate the storm source")
                    .outcome("Ride out the storm")
                    .xpReward(200)
                    .build(),

            // Discovery Events
            new AdventureEventEntry.Builder()
                    .title("First One Ruins")
                    .description("Ancient ruins of the First Ones are discovered — technology beyond comprehension.")
                    .type(EventType.DISCOVERY)
                    .severity(Severity.CRITICAL)
                    .outcome("Explore the ruins carefully")
                    .outcome("Secure the site")
                    .outcome("Study the technology")
                    .storylineRef("FIRST_ONES")
                    .xpReward(500)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Unknown Species Contact")
                    .description("A previously unknown species makes first contact — their technology is advanced but unfamiliar.")
                    .type(EventType.DIPLOMACY)
                    .severity(Severity.MAJOR)
                    .outcome("Establish peaceful contact")
                    .outcome("Study their technology")
                    .outcome("Report to Earth Force")
                    .xpReward(300)
                    .build(),

            // Hazard Events
            new AdventureEventEntry.Builder()
                    .title("Asteroid Field Collision")
                    .description("Your vessel is caught in an unexpected asteroid field — collision risk is high.")
                    .type(EventType.HAZARD)
                    .severity(Severity.MODERATE)
                    .outcome("Navigate carefully through")
                    .outcome("Take evasive action")
                    .outcome("Emergency jump if possible")
                    .xpReward(100)
                    .build(),

            new AdventureEventEntry.Builder()
                    .title("Radiation Storm")
                    .description("A sudden radiation storm from the local star — ship systems are degrading.")
                    .type(EventType.HAZARD)
                    .severity(Severity.MAJOR)
                    .outcome("Seek shelter behind a planet")
                    .outcome("Reinforce ship shielding")
                    .outcome("Emergency jump to safety")
                    .xpReward(200)
                    .build()
    );

    private final Random random = new Random();

    /**
     * Returns a random adventure event.
     *
     * @return a random adventure event
     */
    public AdventureEventEntry rollEvent() {
        return rollEvent(random);
    }

    /**
     * Returns a random adventure event with a given Random.
     *
     * @param random the random number generator
     * @return a random adventure event
     */
    public AdventureEventEntry rollEvent(Random random) {
        return PREDEFINED_EVENTS.get(random.nextInt(PREDEFINED_EVENTS.size()));
    }

    /**
     * Returns a random adventure event of the specified type.
     *
     * @param type the event type
     * @return a random event of that type, or null if none available
     */
    public AdventureEventEntry rollEvent(EventType type) {
        return rollEvent(type, random);
    }

    /**
     * Returns a random adventure event of the specified type with a given Random.
     *
     * @param type the event type
     * @param random the random number generator
     * @return a random event of that type, or null if none available
     */
    public AdventureEventEntry rollEvent(EventType type, Random random) {
        List<AdventureEventEntry> matching = new ArrayList<>();
        for (AdventureEventEntry event : PREDEFINED_EVENTS) {
            if (event.getType() == type) {
                matching.add(event);
            }
        }
        if (matching.isEmpty()) {
            return null;
        }
        return matching.get(random.nextInt(matching.size()));
    }

    /**
     * Returns all predefined adventure events.
     *
     * @return list of all predefined events
     */
    public List<AdventureEventEntry> getAllEvents() {
        return PREDEFINED_EVENTS;
    }

    /**
     * Returns all events of a specified type.
     *
     * @param type the event type
     * @return list of events of that type
     */
    public List<AdventureEventEntry> getEventsByType(EventType type) {
        List<AdventureEventEntry> matching = new ArrayList<>();
        for (AdventureEventEntry event : PREDEFINED_EVENTS) {
            if (event.getType() == type) {
                matching.add(event);
            }
        }
        return matching;
    }

    /**
     * Returns all events related to a storyline.
     *
     * @param storylineRef the storyline reference ID
     * @return list of events related to that storyline
     */
    public List<AdventureEventEntry> getEventsByStoryline(String storylineRef) {
        List<AdventureEventEntry> matching = new ArrayList<>();
        for (AdventureEventEntry event : PREDEFINED_EVENTS) {
            if (storylineRef.equals(event.getStorylineRef())) {
                matching.add(event);
            }
        }
        return matching;
    }

    /**
     * Creates a custom adventure event using the builder pattern.
     *
     * @return a new Builder instance
     */
    public static AdventureEventEntry.Builder builder() {
        return new AdventureEventEntry.Builder();
    }
}
