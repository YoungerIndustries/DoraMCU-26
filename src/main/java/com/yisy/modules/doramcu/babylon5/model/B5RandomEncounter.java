package com.yisy.modules.doramcu.babylon5.model;

import java.util.*;

/**
 * Random encounters in the Babylon 5 universe.
 * <p>
 * Encounters can occur in three contexts:
 * </p>
 * <ul>
 *   <li><b>Space</b> — while traveling between systems or in orbit</li>
 *   <li><b>Urban</b> — while on a planet or station</li>
 *   <li><b>Wildlife</b> — while exploring a planet's surface</li>
 * </ul>
 * <p>
 * Each encounter has a severity (1-10), a description, and possible outcomes.
 * Encounters are themed around the Babylon 5 universe, featuring Earth Force,
 * Narn, Centauri, Minbari, Vorlon, Shadow, and other B5 elements.
 * </p>
 */
public class B5RandomEncounter {

    /**
     * Type of encounter context.
     */
    public enum EncounterType {
        /**
         * Encounter in space — ships, stations, phenomena.
         */
        SPACE,

        /**
         * Encounter in an urban environment — stations, cities, outposts.
         */
        URBAN,

        /**
         * Encounter with wildlife on a planet's surface.
         */
        WILDLIFE
    }

    /**
     * Possible outcomes of an encounter.
     */
    public enum Outcome {
        /**
         * No consequence.
         */
        NONE,

        /**
         * Combat ensued.
         */
        COMBAT,

        /**
         * Trade opportunity.
         */
        TRADE,

        /**
         * Information gained.
         */
        INFORMATION,

        /**
         * Crew affected (injury, morale, etc.).
         */
        CREW_EFFECT,

        /**
         * Ship affected (damage, repair, etc.).
         */
        SHIP_EFFECT,

        /**
         * Mission opportunity.
         */
        MISSION,

        /**
         * Diplomatic event.
         */
        DIPLOMACY,

        /**
         * Psionic event.
         */
        PSIONIC,

        /**
         * Artifact discovered.
         */
        ARTIFACT
    }

    /**
     * A single random encounter entry.
     */
    public static class EncounterEntry {
        private final String title;
        private final String description;
        private final int severity;  // 1-10
        private final Outcome primaryOutcome;
        private final List<String> possibleOutcomes;

        EncounterEntry(String title, String description, int severity,
                        Outcome primaryOutcome, List<String> possibleOutcomes) {
            this.title = title;
            this.description = description;
            this.severity = Math.max(1, Math.min(10, severity));
            this.primaryOutcome = primaryOutcome;
            this.possibleOutcomes = possibleOutcomes != null ?
                    Collections.unmodifiableList(possibleOutcomes) : Collections.emptyList();
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getSeverity() {
            return severity;
        }

        public Outcome getPrimaryOutcome() {
            return primaryOutcome;
        }

        public List<String> getPossibleOutcomes() {
            return possibleOutcomes;
        }

        @Override
        public String toString() {
            return String.format("[%d/10] %s — %s", severity, title, description);
        }
    }

    // ---- Space Encounters ----

    private static final List<EncounterEntry> SPACE_ENCOUNTERS = List.of(
            new EncounterEntry(
                    "Earth Force Patrol",
                    "An Earth Force patrol vessel hails you for routine identification.",
                    2, Outcome.DIPLOMACY,
                    List.of("Pass identification check", "Attempt to evade", "Request escort")
            ),
            new EncounterEntry(
                    "Narn Crusader",
                    "A Narn Crusader warship emerges from jump — hostile posture.",
                    7, Outcome.COMBAT,
                    List.of("Engage in combat", "Attempt diplomacy", "Flee to jump")
            ),
            new EncounterEntry(
                    "Centauri Merchant",
                    "A Centauri merchant vessel offers trade — luxury goods available.",
                    3, Outcome.TRADE,
                    List.of("Trade goods", "Inspect cargo", "Decline and pass")
            ),
            new EncounterEntry(
                    "Minbari Warship",
                    "A Minbari warship demands you state your business in this sector.",
                    5, Outcome.DIPLOMACY,
                    List.of("Comply and explain", "Refuse and risk conflict", "Request passage")
            ),
            new EncounterEntry(
                    "Wormhole Fluctuation",
                    "A wormhole opens nearby — unstable energy readings detected.",
                    6, Outcome.INFORMATION,
                    List.of("Investigate carefully", "Collect data from distance", "Avoid immediately")
            ),
            new EncounterEntry(
                    "Pirate Attack",
                    "Pak'ma'ra raiders ambush your vessel — weapons hot.",
                    8, Outcome.COMBAT,
                    List.of("Fight back", "Attempt to negotiate", "Jump to escape")
            ),
            new EncounterEntry(
                    "Derelict Ship",
                    "A drifting vessel — no transponder, hull damaged.",
                    4, Outcome.MISSION,
                    List.of("Board and investigate", "Scan from distance", "Pass by")
            ),
            new EncounterEntry(
                    "Trade Convoy",
                    "A large trade convoy passes through — escort available for hire.",
                    2, Outcome.TRADE,
                    List.of("Join convoy", "Offer escort services", "Observe and pass")
            ),
            new EncounterEntry(
                    "Shadow Ship",
                    "A Shadow vessel appears — massive firepower, no response to hails.",
                    10, Outcome.COMBAT,
                    List.of("Attempt to flee", "Stand ground", "Send distress signal")
            ),
            new EncounterEntry(
                    "Vorlon Probe",
                    "An ancient Vorlon probe scans your vessel — no hostile action.",
                    5, Outcome.INFORMATION,
                    List.of("Allow scanning", "Attempt to jam", "Record data")
            ),
            new EncounterEntry(
                    "Asteroid Field",
                    "Dense asteroid field ahead — navigation challenge.",
                    3, Outcome.SHIP_EFFECT,
                    List.of("Navigate carefully", "Take shortcut", "Reroute around")
            ),
            new EncounterEntry(
                    "Distress Signal",
                    "A distress signal from a nearby vessel — needs assistance.",
                    4, Outcome.MISSION,
                    List.of("Respond and assist", "Investigate first", "Ignore and pass")
            ),
            new EncounterEntry(
                    "Psi Corps Operative",
                    "A Psi Corps operative aboard another vessel attempts mental contact.",
                    6, Outcome.PSIONIC,
                    List.of("Accept contact", "Resist mentally", "Report to authorities")
            ),
            new EncounterEntry(
                    "Space Station",
                    "An unmarked space station — possible refueling opportunity.",
                    2, Outcome.TRADE,
                    List.of("Dock and investigate", "Scan from orbit", "Pass by")
            ),
            new EncounterEntry(
                    "Gravitational Anomaly",
                    "Strange gravitational readings — ship systems affected.",
                    7, Outcome.SHIP_EFFECT,
                    List.of("Investigate source", "Stabilize ship systems", "Emergency jump")
            )
    );

    // ---- Urban Encounters ----

    private static final List<EncounterEntry> URBAN_ENCOUNTERS = List.of(
            new EncounterEntry(
                    "Street Fight",
                    "A bar fight breaks out — multiple factions involved.",
                    5, Outcome.COMBAT,
                    List.of("Join the fight", "Stay neutral", "Call security")
            ),
            new EncounterEntry(
                    "Black Market Dealer",
                    "A shady dealer offers rare goods — possibly contraband.",
                    4, Outcome.TRADE,
                    List.of("Purchase goods", "Ask for information", "Report to authorities")
            ),
            new EncounterEntry(
                    "Political Rally",
                    "A political rally — tensions high between factions.",
                    3, Outcome.DIPLOMACY,
                    List.of("Attend and listen", "Speak out", "Avoid the area")
            ),
            new EncounterEntry(
                    "Temple Ceremony",
                    "A Minbari temple ceremony — spiritual significance.",
                    2, Outcome.INFORMATION,
                    List.of("Observe respectfully", "Participate", "Ask about traditions")
            ),
            new EncounterEntry(
                    "Centauri Noble",
                    "A Centauri noble offers patronage — with strings attached.",
                    4, Outcome.DIPLOMACY,
                    List.of("Accept patronage", "Negotiate terms", "Politely decline")
            ),
            new EncounterEntry(
                    "Narn Resistance Cell",
                    "Narn resistance fighters seek your help — dangerous mission.",
                    6, Outcome.MISSION,
                    List.of("Help the resistance", "Gather information", "Refuse and leave")
            ),
            new EncounterEntry(
                    "Psi Corps Recruitment",
                    "Psi Corps operatives identify you as a potential recruit.",
                    7, Outcome.PSIONIC,
                    List.of("Accept recruitment", "Resist and flee", "Negotiate terms")
            ),
            new EncounterEntry(
                    "Information Broker",
                    "A local information broker has intel you might need.",
                    3, Outcome.INFORMATION,
                    List.of("Purchase intel", "Barter for info", "Threaten for free info")
            ),
            new EncounterEntry(
                    "Customs Inspection",
                    "Customs officials demand to inspect your cargo.",
                    4, Outcome.NONE,
                    List.of("Comply fully", "Attempt to bribe", "Hide contraband")
            ),
            new EncounterEntry(
                    "Vorlon Envoy",
                    "A mysterious figure — possibly Vorlon — offers cryptic advice.",
                    8, Outcome.INFORMATION,
                    List.of("Listen carefully", "Ask direct questions", "Refuse to engage")
            ),
            new EncounterEntry(
                    "Shadow Agent",
                    "A Shadow agent attempts to manipulate local politics.",
                    9, Outcome.DIPLOMACY,
                    List.of("Expose the agent", "Play along", "Warn others")
            ),
            new EncounterEntry(
                    "Medical Emergency",
                    "A medical emergency — your skills might be needed.",
                    3, Outcome.CREW_EFFECT,
                    List.of("Help immediately", "Assess situation", "Call professionals")
            ),
            new EncounterEntry(
                    "Gambling Den",
                    "An underground gambling den — high stakes.",
                    4, Outcome.CREW_EFFECT,
                    List.of("Play games", "Observe only", "Investigate operations")
            ),
            new EncounterEntry(
                    "Artifact Market",
                    "Ancient artifacts for sale — some may be First One technology.",
                    5, Outcome.ARTIFACT,
                    List.of("Purchase artifact", "Authenticate first", "Report to scholars")
            ),
            new EncounterEntry(
                    "Local Crime Boss",
                    "A crime boss demands protection money.",
                    6, Outcome.COMBAT,
                    List.of("Pay up", "Refuse and fight", "Seek allies")
            )
    );

    // ---- Wildlife Encounters ----

    private static final List<EncounterEntry> WILDLIFE_ENCOUNTERS = List.of(
            new EncounterEntry(
                    "Drazi Beast",
                    "A massive Drazi warrior beast — territorial and aggressive.",
                    7, Outcome.COMBAT,
                    List.of("Fight the beast", "Attempt to calm it", "Retreat slowly")
            ),
            new EncounterEntry(
                    "Exotic Flora",
                    "Rare plant species — potential medicinal value.",
                    2, Outcome.INFORMATION,
                    List.of("Collect samples", "Study in place", "Mark for return")
            ),
            new EncounterEntry(
                    "Predatory Swarm",
                    "A swarm of predatory insects — fast and numerous.",
                    6, Outcome.COMBAT,
                    List.of("Use fire weapons", "Find shelter", "Fight through")
            ),
            new EncounterEntry(
                    "Ancient Ruins",
                    "Overgrown ruins — possibly Vorlon or First One origin.",
                    5, Outcome.ARTIFACT,
                    List.of("Explore carefully", "Scan for traps", "Document and leave")
            ),
            new EncounterEntry(
                    "Water Source",
                    "A clean water source — good for resupply.",
                    1, Outcome.NONE,
                    List.of("Collect water", "Test quality", "Mark on maps")
            ),
            new EncounterEntry(
                    "Shadow Experiment",
                    "A failed Shadow biological experiment — dangerous mutation.",
                    9, Outcome.COMBAT,
                    List.of("Destroy the creature", "Study from distance", "Flee immediately")
            ),
            new EncounterEntry(
                    "Peaceful Wildlife",
                    "Gentle native creatures — no threat.",
                    1, Outcome.INFORMATION,
                    List.of("Observe behavior", "Take photos", "Continue journey")
            ),
            new EncounterEntry(
                    "Toxic Atmosphere Zone",
                    "A pocket of toxic gas — environmental hazard.",
                    5, Outcome.CREW_EFFECT,
                    List.of("Don environmental suits", "Find alternate route", "Vent the area")
            ),
            new EncounterEntry(
                    "Mineral Vein",
                    "Exposed mineral deposits — valuable ore.",
                    3, Outcome.INFORMATION,
                    List.of("Collect samples", "Mark for mining", "Assess quantity")
            ),
            new EncounterEntry(
                    "Psionic Resonance",
                    "A natural psionic resonance point — psi-sensitive individuals affected.",
                    6, Outcome.PSIONIC,
                    List.of("Investigate the source", "Shield your mind", "Record readings")
            ),
            new EncounterEntry(
                    "Cave System",
                    "An extensive cave system — possible shelter or hidden treasure.",
                    4, Outcome.MISSION,
                    List.of("Explore the caves", "Map the entrance", "Avoid the darkness")
            ),
            new EncounterEntry(
                    "Native Tribe",
                    "A native tribe — their reaction to outsiders is uncertain.",
                    5, Outcome.DIPLOMACY,
                    List.of("Approach peacefully", "Observe from distance", "Leave quietly")
            )
    );

    private final Random random = new Random();

    /**
     * Generates a random encounter of the specified type.
     *
     * @param type the encounter type (SPACE, URBAN, WILDLIFE)
     * @return a random encounter entry
     */
    public EncounterEntry rollEncounter(EncounterType type) {
        return rollEncounter(type, random);
    }

    /**
     * Generates a random encounter of the specified type with a given Random.
     *
     * @param type the encounter type
     * @param random the random number generator
     * @return a random encounter entry
     */
    public EncounterEntry rollEncounter(EncounterType type, Random random) {
        List<EncounterEntry> encounters = switch (type) {
            case SPACE -> SPACE_ENCOUNTERS;
            case URBAN -> URBAN_ENCOUNTERS;
            case WILDLIFE -> WILDLIFE_ENCOUNTERS;
        };
        return encounters.get(random.nextInt(encounters.size()));
    }

    /**
     * Returns all space encounters.
     *
     * @return list of space encounter entries
     */
    public List<EncounterEntry> getSpaceEncounters() {
        return SPACE_ENCOUNTERS;
    }

    /**
     * Returns all urban encounters.
     *
     * @return list of urban encounter entries
     */
    public List<EncounterEntry> getUrbanEncounters() {
        return URBAN_ENCOUNTERS;
    }

    /**
     * Returns all wildlife encounters.
     *
     * @return list of wildlife encounter entries
     */
    public List<EncounterEntry> getWildlifeEncounters() {
        return WILDLIFE_ENCOUNTERS;
    }

    /**
     * Returns all encounters for a given type.
     *
     * @param type the encounter type
     * @return list of encounter entries
     */
    public List<EncounterEntry> getEncounters(EncounterType type) {
        return switch (type) {
            case SPACE -> SPACE_ENCOUNTERS;
            case URBAN -> URBAN_ENCOUNTERS;
            case WILDLIFE -> WILDLIFE_ENCOUNTERS;
        };
    }
}
