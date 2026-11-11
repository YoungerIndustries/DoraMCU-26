package com.yisy.modules.doramcu.babylon5.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Objects;

/**
 * Exploration activities within a B5 star system.
 * <p>
 * When a ship explores a B5 star system, it can perform various activities:
 * survey celestial bodies, discover resources, detect hazards, map routes,
 * deploy beacons, and conduct full system surveys.
 * </p>
 * <p>
 * Each activity consumes time (weeks) and may require specific ship equipment
 * or crew skills. Activities have success probabilities based on the ship's
 * capabilities and the system's characteristics.
 * </p>
 * <p>
 * B5-themed discoveries include Vorlon artifacts, Shadow facilities, wormholes,
 * First One technology, and other elements from the Babylon 5 universe.
 * </p>
 */
public class B5ExplorationActivity {

    /**
     * Types of exploration activities.
     */
    public enum ActivityType {
        /**
         * Survey a celestial body to determine its characteristics.
         */
        SURVEY_BODY,

        /**
         * Scan for resources (minerals, gases, water).
         */
        RESOURCE_SCAN,

        /**
         * Detect hazards (asteroid fields, radiation, gravitational anomalies).
         */
        HAZARD_DETECTION,

        /**
         * Map safe routes between celestial bodies.
         */
        ROUTE_MAPPING,

        /**
         * Establish a navigation beacon for future reference.
         */
        BEACON_DEPLOYMENT,

        /**
         * Full system survey — comprehensive exploration of the entire system.
         */
        FULL_SYSTEM_SURVEY
    }

    /**
     * Possible discoveries during B5 exploration.
     */
    public enum Discovery {
        /**
         * No significant findings.
         */
        NONE,

        /**
         * Habitable world discovered.
         */
        HABITABLE_WORLD,

        /**
         * Mineral deposits found.
         */
        MINERAL_DEPOSITS,

        /**
         * Gas giant with valuable isotopes.
         */
        GAS_ISOTOPES,

        /**
         * Asteroid belt with mining potential.
         */
        ASTEROID_BELT,

        /**
         * Water ice deposits.
         */
        WATER_ICE,

        /**
         * Ancient Vorlon artifacts or structures.
         */
        VORLON_ARTIFACTS,

        /**
         * Shadow facility or technology.
         */
        SHADOW_FACILITY,

        /**
         * Wormhole detected.
         */
        WORMHOLE,

        /**
         * First One technology fragment.
         */
        FIRST_ONE_TECH,

        /**
         * Hazardous phenomenon (radiation, gravitational anomaly).
         */
        HAZARDOUS_PHENOMENON,

        /**
         * Unknown signal or phenomenon.
         */
        UNKNOWN_SIGNAL,

        /**
         * Another ship or station.
         */
        FOREIGN_PRESENCE,

        /**
         * Derelict vessel from a past conflict.
         */
        DERELICT_VESSEL,

        /**
         * Psi corps training site.
         */
        PSI_SITE;

        /**
         * Returns a human-readable description of this discovery.
         *
         * @return the description
         */
        public String getDescription() {
            switch (this) {
                case HABITABLE_WORLD:
                    return "Potentially habitable world requiring further survey.";
                case MINERAL_DEPOSITS:
                    return "Significant mineral deposits detected — mining recommended.";
                case GAS_ISOTOPES:
                    return "Valuable isotopes detected in atmosphere.";
                case ASTEROID_BELT:
                    return "Asteroid belt with mining potential identified.";
                case WATER_ICE:
                    return "Water ice deposits found — refueling possible.";
                case VORLON_ARTIFACTS:
                    return "Ancient Vorlon structures detected — archaeological significance.";
                case SHADOW_FACILITY:
                    return "Abandoned Shadow facility discovered — dangerous technology.";
                case WORMHOLE:
                    return "Wormhole fluctuation detected — unstable, avoid proximity.";
                case FIRST_ONE_TECH:
                    return "First One technology fragment — priceless artifact.";
                case HAZARDOUS_PHENOMENON:
                    return "Hazardous phenomenon detected — avoid area.";
                case UNKNOWN_SIGNAL:
                    return "Unknown signal source detected — investigation recommended.";
                case FOREIGN_PRESENCE:
                    return "Foreign ship or station detected — caution advised.";
                case DERELICT_VESSEL:
                    return "Derelict vessel from a past conflict — salvage possible.";
                case PSI_SITE:
                    return "Psionic training site detected — Psi Corps interest.";
                default:
                    return "No significant findings.";
            }
        }
    }

    private final ActivityType type;
    private final String target;
    private final String systemName;
    private final int weeksRequired;
    private final int successProbability;
    private final boolean successful;
    private final Discovery discovery;
    private final String notes;

    /**
     * Creates a new exploration activity.
     *
     * @param type the activity type
     * @param target the target description
     * @param systemName the star system name
     * @param weeksRequired time required in weeks
     * @param successProbability success probability (0-100)
     */
    public B5ExplorationActivity(ActivityType type, String target, String systemName,
                                  int weeksRequired, int successProbability) {
        this.type = Objects.requireNonNull(type);
        this.target = Objects.requireNonNull(target);
        this.systemName = Objects.requireNonNull(systemName);
        this.weeksRequired = weeksRequired;
        this.successProbability = Math.max(0, Math.min(100, successProbability));
        this.successful = false;
        this.discovery = Discovery.NONE;
        this.notes = "";
    }

    /**
     * Creates a completed exploration activity.
     *
     * @param type the activity type
     * @param target the target description
     * @param systemName the star system name
     * @param weeksRequired time required
     * @param successful whether the activity succeeded
     * @param discovery the discovery made
     * @param notes additional notes
     */
    public B5ExplorationActivity(ActivityType type, String target, String systemName,
                                  int weeksRequired, boolean successful, Discovery discovery,
                                  String notes) {
        this.type = Objects.requireNonNull(type);
        this.target = Objects.requireNonNull(target);
        this.systemName = Objects.requireNonNull(systemName);
        this.weeksRequired = weeksRequired;
        this.successProbability = 0;
        this.successful = successful;
        this.discovery = discovery;
        this.notes = notes != null ? notes : "";
    }

    /**
     * Executes the exploration activity with the given random seed.
     *
     * @param random the random number generator
     * @return a new completed exploration activity
     */
    public B5ExplorationActivity execute(Random random) {
        boolean success = random.nextInt(100) < successProbability;
        Discovery disc = success ? rollDiscovery(random, type) : Discovery.NONE;
        String note = generateNotes(success, disc);
        return new B5ExplorationActivity(type, target, systemName, weeksRequired, success, disc, note);
    }

    /**
     * Rolls for a discovery based on the activity type.
     * B5-themed discoveries are weighted toward Vorlon, Shadow, and wormhole finds.
     */
    private Discovery rollDiscovery(Random random, ActivityType activityType) {
        int roll = random.nextInt(100);

        if (activityType == ActivityType.RESOURCE_SCAN) {
            if (roll < 35) return Discovery.MINERAL_DEPOSITS;
            if (roll < 55) return Discovery.WATER_ICE;
            if (roll < 70) return Discovery.GAS_ISOTOPES;
            if (roll < 80) return Discovery.ASTEROID_BELT;
            if (roll < 90) return Discovery.VORLON_ARTIFACTS;
            return Discovery.NONE;
        }

        if (activityType == ActivityType.SURVEY_BODY) {
            if (roll < 25) return Discovery.HABITABLE_WORLD;
            if (roll < 45) return Discovery.MINERAL_DEPOSITS;
            if (roll < 60) return Discovery.WATER_ICE;
            if (roll < 70) return Discovery.VORLON_ARTIFACTS;
            if (roll < 78) return Discovery.SHADOW_FACILITY;
            if (roll < 85) return Discovery.HAZARDOUS_PHENOMENON;
            if (roll < 90) return Discovery.PSI_SITE;
            return Discovery.NONE;
        }

        if (activityType == ActivityType.HAZARD_DETECTION) {
            if (roll < 15) return Discovery.HAZARDOUS_PHENOMENON;
            if (roll < 25) return Discovery.ASTEROID_BELT;
            if (roll < 35) return Discovery.FOREIGN_PRESENCE;
            if (roll < 45) return Discovery.WORMHOLE;
            if (roll < 55) return Discovery.DERELICT_VESSEL;
            return Discovery.NONE;
        }

        if (activityType == ActivityType.FULL_SYSTEM_SURVEY) {
            if (roll < 10) return Discovery.FIRST_ONE_TECH;
            if (roll < 20) return Discovery.WORMHOLE;
            if (roll < 30) return Discovery.VORLON_ARTIFACTS;
            if (roll < 40) return Discovery.SHADOW_FACILITY;
            if (roll < 55) return Discovery.HABITABLE_WORLD;
            if (roll < 70) return Discovery.MINERAL_DEPOSITS;
            if (roll < 80) return Discovery.FOREIGN_PRESENCE;
            if (roll < 88) return Discovery.DERELICT_VESSEL;
            if (roll < 93) return Discovery.PSI_SITE;
            return Discovery.NONE;
        }

        // Default: low chance of anything
        if (roll < 8) return Discovery.UNKNOWN_SIGNAL;
        if (roll < 15) return Discovery.FOREIGN_PRESENCE;
        if (roll < 20) return Discovery.VORLON_ARTIFACTS;
        if (roll < 25) return Discovery.DERELICT_VESSEL;
        return Discovery.NONE;
    }

    /**
     * Generates notes for the activity result.
     */
    private String generateNotes(boolean success, Discovery discovery) {
        if (!success) {
            return "Activity failed — insufficient data collected.";
        }

        if (discovery == Discovery.NONE) {
            return "Activity completed — no significant findings.";
        }

        return discovery.getDescription();
    }

    // ---- Accessors ----

    public ActivityType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public String getSystemName() {
        return systemName;
    }

    public int getWeeksRequired() {
        return weeksRequired;
    }

    public int getSuccessProbability() {
        return successProbability;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Discovery getDiscovery() {
        return discovery;
    }

    public String getNotes() {
        return notes;
    }

    // ---- Factory Methods ----

    /**
     * Creates a survey activity for a celestial body.
     *
     * @param bodyName the name of the celestial body
     * @param systemName the star system name
     * @param hasComputer true if the ship has a computer (increases success)
     * @return a new exploration activity
     */
    public static B5ExplorationActivity createSurvey(String bodyName, String systemName,
                                                      boolean hasComputer) {
        int probability = hasComputer ? 80 : 60;
        return new B5ExplorationActivity(ActivityType.SURVEY_BODY, bodyName, systemName, 2, probability);
    }

    /**
     * Creates a resource scan activity.
     *
     * @param targetArea the area to scan
     * @param systemName the star system name
     * @param hasComputer true if the ship has a computer
     * @return a new exploration activity
     */
    public static B5ExplorationActivity createResourceScan(String targetArea, String systemName,
                                                            boolean hasComputer) {
        int probability = hasComputer ? 75 : 55;
        return new B5ExplorationActivity(ActivityType.RESOURCE_SCAN, targetArea, systemName, 1, probability);
    }

    /**
     * Creates a hazard detection activity.
     *
     * @param targetArea the area to scan
     * @param systemName the star system name
     * @param hasComputer true if the ship has a computer
     * @return a new exploration activity
     */
    public static B5ExplorationActivity createHazardDetection(String targetArea, String systemName,
                                                               boolean hasComputer) {
        int probability = hasComputer ? 85 : 65;
        return new B5ExplorationActivity(ActivityType.HAZARD_DETECTION, targetArea, systemName, 1, probability);
    }

    /**
     * Creates a full system survey activity.
     *
     * @param systemName the star system to survey
     * @param hasComputer true if the ship has a computer
     * @return a new exploration activity
     */
    public static B5ExplorationActivity createFullSystemSurvey(String systemName,
                                                                boolean hasComputer) {
        int probability = hasComputer ? 70 : 45;
        return new B5ExplorationActivity(
                ActivityType.FULL_SYSTEM_SURVEY,
                systemName + " System",
                systemName,
                4,
                probability
        );
    }

    /**
     * Creates a route mapping activity between two points.
     *
     * @param from the starting point
     * @param to the destination
     * @param systemName the star system name
     * @return a new exploration activity
     */
    public static B5ExplorationActivity createRouteMapping(String from, String to, String systemName) {
        return new B5ExplorationActivity(ActivityType.ROUTE_MAPPING, from + " → " + to, systemName, 1, 90);
    }

    /**
     * Creates a beacon deployment activity.
     *
     * @param location the beacon location
     * @param systemName the star system name
     * @return a new exploration activity
     */
    public static B5ExplorationActivity createBeaconDeployment(String location, String systemName) {
        return new B5ExplorationActivity(ActivityType.BEACON_DEPLOYMENT, location, systemName, 1, 95);
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(": ").append(target)
                .append(" (").append(weeksRequired).append(" weeks)");

        if (successful) {
            sb.append(" ✓");
            if (discovery != Discovery.NONE) {
                sb.append(" [").append(discovery).append("]");
            }
        } else if (successful || discovery != Discovery.NONE) {
            sb.append(" ✗");
        }

        if (!notes.isEmpty()) {
            sb.append(" — ").append(notes);
        }

        return sb.toString();
    }
}
