package com.yisy.modules.doramcu.sfb.model;

import java.util.*;

/**
 * Exploration activities for SFB.
 * <p>
 * Provides 6 activity types with SFB-themed discoveries including
 * ancient ruins, alien artifacts, and scientific findings.
 * </p>
 */
public class SfbExplorationActivity {

    private static final Random RANDOM = new Random();

    private final ActivityType type;
    private final String targetName;
    private final String systemName;
    private final int crewRequired;
    private final int successChance;
    private Discovery discovery;

    public SfbExplorationActivity(ActivityType type, String targetName, String systemName,
                                   int crewRequired, int successChance) {
        this.type = type;
        this.targetName = targetName;
        this.systemName = systemName;
        this.crewRequired = crewRequired;
        this.successChance = successChance;
    }

    /**
     * Executes the exploration activity.
     */
    public boolean execute() {
        int roll = RANDOM.nextInt(100);
        boolean success = roll < successChance;

        if (success) {
            discovery = rollDiscovery();
        }

        return success;
    }

    private Discovery rollDiscovery() {
        List<Discovery> possible = new ArrayList<>();

        switch (type) {
            case SURVEY_BODY:
                possible.add(new Discovery("Habitable planet discovered", "New colony site identified", 100));
                possible.add(new Discovery("Dilithium deposits found", "Valuable mineral resources", 150));
                possible.add(new Discovery("Ancient ruins detected", "Pre-warp civilization remains", 200));
                possible.add(new Discovery("Alien artifact found", "Unknown technology recovered", 250));
                break;
            case RESOURCE_SCAN:
                possible.add(new Discovery("Deuterium reserves", "Fuel for warp drive", 80));
                possible.add(new Discovery("Tritanium deposits", "Shipbuilding materials", 120));
                possible.add(new Discovery("Plasma vents", "Energy source identified", 100));
                break;
            case HAZARD_DETECTION:
                possible.add(new Discovery("Nebula hazard mapped", "Safe passage identified", 60));
                possible.add(new Discovery("Asteroid field charted", "Navigation route secured", 50));
                possible.add(new Discovery("Gravitational anomaly", "Scientific research value", 150));
                break;
            case ROUTE_MAPPING:
                possible.add(new Discovery("Warp lane discovered", "Faster travel route", 200));
                possible.add(new Discovery("Subspace corridor", "Communication boost", 180));
                break;
            case BEACON_DEPLOYMENT:
                possible.add(new Discovery("Navigation beacon placed", "Sector chart updated", 50));
                possible.add(new Discovery("Communication relay established", "Long-range comms enabled", 80));
                break;
            case FULL_SYSTEM_SURVEY:
                possible.add(new Discovery("Complete system map", "All bodies catalogued", 300));
                possible.add(new Discovery("Ancient alien facility", "Pre-Federation technology", 500));
                possible.add(new Discovery("Wormhole detected", "Instant travel possible", 1000));
                break;
        }

        return possible.get(RANDOM.nextInt(possible.size()));
    }

    public ActivityType getType() { return type; }
    public String getTargetName() { return targetName; }
    public String getSystemName() { return systemName; }
    public int getCrewRequired() { return crewRequired; }
    public int getSuccessChance() { return successChance; }
    public Discovery getDiscovery() { return discovery; }

    /**
     * Types of exploration activities.
     */
    public enum ActivityType {
        SURVEY_BODY("Survey Body"),
        RESOURCE_SCAN("Resource Scan"),
        HAZARD_DETECTION("Hazard Detection"),
        ROUTE_MAPPING("Route Mapping"),
        BEACON_DEPLOYMENT("Beacon Deployment"),
        FULL_SYSTEM_SURVEY("Full System Survey");

        private final String name;

        ActivityType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * A discovery from exploration.
     */
    public static class Discovery {
        private final String name;
        private final String description;
        private final int xpReward;

        public Discovery(String name, String description, int xpReward) {
            this.name = name;
            this.description = description;
            this.xpReward = xpReward;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getXpReward() { return xpReward; }
    }
}
