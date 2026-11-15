package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for encounters across all game modules.
 * <p>
 * Used for random encounters, NPC interactions, and event generation.
 * </p>
 */
public interface EncounterService {

    /**
     * List all available encounters.
     *
     * @return list of encounters
     */
    List<Encounter> listEncounters();

    /**
     * Get an encounter by ID.
     *
     * @param encounterId the encounter ID
     * @return the encounter or null if not found
     */
    Encounter getEncounter(Long encounterId);

    /**
     * Generate a random encounter for a location/area.
     *
     * @param areaId the area identifier
     * @param level the character level
     * @return a random encounter
     */
    Encounter generateRandomEncounter(String areaId, int level);

    /**
     * Resolve an encounter.
     *
     * @param encounterId the encounter ID
     * @param characterIds list of character IDs involved
     * @return the encounter result
     */
    EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds);

    /**
     * An encounter.
     */
    class Encounter {
        private Long id;
        private String name;
        private String description;
        private String difficulty;
        private int xpReward;
        private int minLevel;
        private int maxLevel;
        private String encounterType;
        private String areaId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getXpReward() { return xpReward; }
        public void setXpReward(int xpReward) { this.xpReward = xpReward; }
        public int getMinLevel() { return minLevel; }
        public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
        public int getMaxLevel() { return maxLevel; }
        public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
        public String getEncounterType() { return encounterType; }
        public void setEncounterType(String encounterType) { this.encounterType = encounterType; }
        public String getAreaId() { return areaId; }
        public void setAreaId(String areaId) { this.areaId = areaId; }
    }

    /**
     * Result of resolving an encounter.
     */
    class EncounterResult {
        private final Long encounterId;
        private final String encounterName;
        private final boolean success;
        private final String outcome;
        private final int xpAwarded;

        public EncounterResult(Long encounterId, String encounterName, boolean success, String outcome, int xpAwarded) {
            this.encounterId = encounterId;
            this.encounterName = encounterName;
            this.success = success;
            this.outcome = outcome;
            this.xpAwarded = xpAwarded;
        }

        public Long getEncounterId() { return encounterId; }
        public String getEncounterName() { return encounterName; }
        public boolean isSuccess() { return success; }
        public String getOutcome() { return outcome; }
        public int getXpAwarded() { return xpAwarded; }
    }
}
