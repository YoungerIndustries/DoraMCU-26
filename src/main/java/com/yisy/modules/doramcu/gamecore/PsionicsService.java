package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for psionic powers across all game modules.
 * <p>
 * D&amp;D provides psionics subclasses, Traveller has psionic disciplines,
 * and Earth2026 can use this for psychic abilities.
 * </p>
 */
public interface PsionicsService {

    /**
     * List all available psionic powers.
     *
     * @return list of psionic powers
     */
    List<PsionicPower> listPowers();

    /**
     * Get a psionic power by ID.
     *
     * @param powerId the power ID
     * @return the power or null if not found
     */
    PsionicPower getPower(Long powerId);

    /**
     * Use a psionic power.
     *
     * @param characterId the character ID
     * @param powerId the power ID
     * @param targetId optional target ID
     * @return the usage result
     */
    PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId);

    /**
     * A psionic power.
     */
    static class PsionicPower {
        private Long id;
        private String name;
        private String discipline;
        private int powerLevel;
        private int powerPointsCost;
        private String range;
        private String duration;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDiscipline() { return discipline; }
        public void setDiscipline(String discipline) { this.discipline = discipline; }
        public int getPowerLevel() { return powerLevel; }
        public void setPowerLevel(int powerLevel) { this.powerLevel = powerLevel; }
        public int getPowerPointsCost() { return powerPointsCost; }
        public void setPowerPointsCost(int powerPointsCost) { this.powerPointsCost = powerPointsCost; }
        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * Result of using a psionic power.
     */
    static class PsionicPowerResult {
        private final Long powerId;
        private final String powerName;
        private final boolean success;
        private final String message;
        private final int powerPointsSpent;

        public PsionicPowerResult(Long powerId, String powerName, boolean success, String message, int powerPointsSpent) {
            this.powerId = powerId;
            this.powerName = powerName;
            this.success = success;
            this.message = message;
            this.powerPointsSpent = powerPointsSpent;
        }

        public Long getPowerId() { return powerId; }
        public String getPowerName() { return powerName; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getPowerPointsSpent() { return powerPointsSpent; }
    }
}
