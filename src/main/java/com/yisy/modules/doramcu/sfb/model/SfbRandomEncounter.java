package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Random encounters for SFB.
 * <p>
 * Provides space, urban, and wildlife encounter tables with
 * SFB-themed encounters and outcomes.
 * </p>
 */
public class SfbRandomEncounter {

    private static final Random RANDOM = new Random();

    private final List<EncounterEntry> spaceEncounters = new ArrayList<>();
    private final List<EncounterEntry> urbanEncounters = new ArrayList<>();
    private final List<EncounterEntry> wildlifeEncounters = new ArrayList<>();

    public SfbRandomEncounter() {
        initializeEncounters();
    }

    private void initializeEncounters() {
        // Space encounters
        spaceEncounters.add(new EncounterEntry("Federation Patrol", "Starfleet patrol vessel hails you", 2,
                List.of("Trade offered", "Diplomatic meeting", "Continue on way")));
        spaceEncounters.add(new EncounterEntry("Klingon Warship", "Klingon battle cruiser intercepts", 7,
                List.of("Fight", "Diplomatic parley", "Warp escape")));
        spaceEncounters.add(new EncounterEntry("Romulan Warbird", "Cloaked Romulan ship decloaks", 6,
                List.of("Fight", "Negotiate", "Retreat")));
        spaceEncounters.add(new EncounterEntry("Derelict Ship", "Abandoned vessel drifting", 3,
                List.of("Board and investigate", "Scan from distance", "Ignore")));
        spaceEncounters.add(new EncounterEntry("Asteroid Field", "Dense asteroid field ahead", 2,
                List.of("Navigate through", "Go around", "Scan for minerals")));
        spaceEncounters.add(new EncounterEntry("Nebula", "Colorful nebula in path", 1,
                List.of("Fly through", "Study from outside", "Continue on course")));
        spaceEncounters.add(new EncounterEntry("Warp Anomaly", "Distortion in subspace", 5,
                List.of("Investigate", "Mark hazard", "Avoid")));
        spaceEncounters.add(new EncounterEntry("Merchant Vessel", "Independent trader offers goods", 1,
                List.of("Trade", "Ignore", "Report to authorities")));
        spaceEncounters.add(new EncounterEntry("Pirate Attack", "Hostile ship attacks", 8,
                List.of("Fight", "Flee", "Hail for parley")));
        spaceEncounters.add(new EncounterEntry("Distress Signal", "Emergency beacon detected", 3,
                List.of("Respond", "Ignore", "Report to Starfleet")));
        spaceEncounters.add(new EncounterEntry("Suliban Raider", "Suliban shape-shifters attack", 7,
                List.of("Fight", "Capture for interrogation", "Flee")));
        spaceEncounters.add(new EncounterEntry("Gorn Ship", "Gorn vessel on collision course", 8,
                List.of("Fight", "Evade", "Attempt communication")));
        spaceEncounters.add(new EncounterEntry("Tholian Web", "Tholian interceptor web detected", 6,
                List.of("Cut through", "Go around", "Study the web")));
        spaceEncounters.add(new EncounterEntry("Space Station", "Unmarked station in orbit", 2,
                List.of("Dock", "Scan", "Pass by")));
        spaceEncounters.add(new EncounterEntry("Wormhole", "Stable wormhole detected", 4,
                List.of("Enter", "Study", "Mark for later")));

        // Urban encounters
        urbanEncounters.add(new EncounterEntry("Starfleet Officer", "Officer needs assistance", 2,
                List.of("Help", "Decline politely", "Report to superiors")));
        urbanEncounters.add(new EncounterEntry("Klingon Warrior", "Klingon challenges you to combat", 6,
                List.of("Accept challenge", "Decline", "Diplomatic response")));
        urbanEncounters.add(new EncounterEntry("Black Market", "Underground trading post", 4,
                List.of("Browse goods", "Report to authorities", "Ignore")));
        urbanEncounters.add(new EncounterEntry("Romulan Spy", "Suspected spy approaches", 5,
                List.of("Confront", "Ignore", "Report to security")));
        urbanEncounters.add(new EncounterEntry("Science Conference", "Academic presentation", 1,
                List.of("Attend", "Network", "Skip")));
        urbanEncounters.add(new EncounterEntry("Bar Fight", "Brawl breaks out", 4,
                List.of("Intervene", "Stay out of it", "Call security")));
        urbanEncounters.add(new EncounterEntry("Recruitment Officer", "Career opportunity offered", 1,
                List.of("Consider", "Decline", "Ask for details")));
        urbanEncounters.add(new EncounterEntry("Protest", "Political demonstration", 3,
                List.of("Join", "Observe", "Avoid")));
        urbanEncounters.add(new EncounterEntry("Medical Emergency", "Person needs help", 2,
                List.of("Help", "Call medics", "Continue on way")));
        urbanEncounters.add(new EncounterEntry("Security Checkpoint", "Enhanced security in area", 2,
                List.of("Comply", "Show credentials", "Find alternate route")));
        urbanEncounters.add(new EncounterEntry("Cultural Event", "Festival or celebration", 1,
                List.of("Participate", "Observe", "Skip")));
        urbanEncounters.add(new EncounterEntry("Smuggler Contact", "Illegal goods offered", 5,
                List.of("Refuse", "Report", "Negotiate")));
        urbanEncounters.add(new EncounterEntry("Ambassador", "Diplomatic meeting arranged", 2,
                List.of("Attend", "Send representative", "Decline")));
        urbanEncounters.add(new EncounterEntry("Tribunal", "Legal proceeding", 3,
                List.of("Attend as observer", "Provide testimony", "Skip")));
        urbanEncounters.add(new EncounterEntry("Mysterious Stranger", "Unknown alien approaches", 4,
                List.of("Talk", "Be cautious", "Leave")));

        // Wildlife encounters
        wildlifeEncounters.add(new EncounterEntry("Gorn", "Massive reptilian creature", 9,
                List.of("Fight", "Flee", "Observe from distance")));
        wildlifeEncounters.add(new EncounterEntry("Horta", "Rock-like creature", 4,
                List.of("Communicate", "Study", "Avoid")));
        wildlifeEncounters.add(new EncounterEntry("Denobulan Beast", "Native predator", 6,
                List.of("Fight", "Retreat", "Study behavior")));
        wildlifeEncounters.add(new EncounterEntry("Klingon Bird-of-Prey", "Large predatory bird", 5,
                List.of("Hunt", "Observe", "Ignore")));
        wildlifeEncounters.add(new EncounterEntry("Trill Symbiont Host", "Rare symbiont species", 2,
                List.of("Study", "Report to science", "Leave undisturbed")));
        wildlifeEncounters.add(new EncounterEntry("Andorian Snow Leopard", "Arctic predator", 5,
                List.of("Observe", "Photograph", "Avoid")));
        wildlifeEncounters.add(new EncounterEntry("Vulcan Humpback Wolf", "Endangered species", 3,
                List.of("Study", "Protect", "Leave")));
        wildlifeEncounters.add(new EncounterEntry("Borg Nanobots", "Assimilation threat", 10,
                List.of("Fight", "Flee immediately", "Report to Starfleet")));
        wildlifeEncounters.add(new EncounterEntry("Energy Lifeform", "Being of pure energy", 7,
                List.of("Attempt communication", "Study", "Retreat")));
        wildlifeEncounters.add(new EncounterEntry("Plant Lifeform", "Aggressive flora", 4,
                List.of("Study", "Clear path", "Avoid area")));
        wildlifeEncounters.add(new EncounterEntry("Aquatic Creature", "Large sea lifeform", 3,
                List.of("Observe", "Study", "Continue")));
        wildlifeEncounters.add(new EncounterEntry("Insectoid Swarm", "Swarm of alien insects", 6,
                List.of("Disperse", "Study", "Flee")));
    }

    /**
     * Rolls a random encounter of the given type.
     */
    public EncounterEntry rollEncounter(EncounterType type) {
        return switch (type) {
            case SPACE -> spaceEncounters.get(RANDOM.nextInt(spaceEncounters.size()));
            case URBAN -> urbanEncounters.get(RANDOM.nextInt(urbanEncounters.size()));
            case WILDLIFE -> wildlifeEncounters.get(RANDOM.nextInt(wildlifeEncounters.size()));
        };
    }

    public List<EncounterEntry> getSpaceEncounters() { return spaceEncounters; }
    public List<EncounterEntry> getUrbanEncounters() { return urbanEncounters; }
    public List<EncounterEntry> getWildlifeEncounters() { return wildlifeEncounters; }

    /**
     * Types of encounters.
     */
    public enum EncounterType {
        SPACE, URBAN, WILDLIFE
    }

    /**
     * A single encounter entry.
     */
    public static class EncounterEntry {
        private final String name;
        private final String description;
        private final int severity;
        private final List<String> outcomes;

        public EncounterEntry(String name, String description, int severity, List<String> outcomes) {
            this.name = name;
            this.description = description;
            this.severity = severity;
            this.outcomes = outcomes;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getSeverity() { return severity; }
        public List<String> getOutcomes() { return outcomes; }
    }
}
