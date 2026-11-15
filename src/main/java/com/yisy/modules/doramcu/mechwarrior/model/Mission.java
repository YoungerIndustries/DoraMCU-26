package com.yisy.modules.doramcu.mechwarrior.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a MechWarrior mission with objectives, rewards, and difficulty.
 * <p>
 * Missions are the core progression mechanic in MechWarrior FPS games.
 * Each mission has a type (assault, defense, escort, recon, extraction, etc.),
 * a set of objectives, and rewards for completion.
 * </p>
 */
public class Mission {

    public enum MissionType {
        ASSAULT,        // Destroy all enemy forces
        DEFENSE,        // Defend a position for a set time
        ESCORT,         // Protect a friendly unit
        RECON,          // Gather intelligence on enemy positions
        EXTRACTION,     // Rescue a target and extract
        CAPTURE,        // Capture and hold a position
        SABOTAGE,       // Destroy a specific target
        SURVIVAL,       // Survive waves of enemies
        PATROL,         // Patrol a route and eliminate threats
        AMBUSH,         // Set up and execute an ambush
        INTERDICTION,   // Block enemy reinforcements
        SEARCH_DESTROY, // Find and destroy a hidden target
        SOLARIS,        // Arena combat for glory and credits
        CUSTOM          // Custom instant action mission
    }

    public enum MissionDifficulty {
        EASY(0.5),
        NORMAL(1.0),
        HARD(1.5),
        EXTREME(2.0),
        LEGENDARY(3.0);

        private final double rewardMultiplier;

        MissionDifficulty(double rewardMultiplier) {
            this.rewardMultiplier = rewardMultiplier;
        }

        public double getRewardMultiplier() {
            return rewardMultiplier;
        }
    }

    public enum MissionStatus {
        AVAILABLE,      // Mission can be accepted
        ACCEPTED,       // Mission accepted, not yet started
        IN_PROGRESS,    // Mission is active
        COMPLETED,      // Mission objectives completed
        FAILED,         // Mission failed
        ABANDONED       // Mission voluntarily abandoned
    }

    private String id;
    private String name;
    private String description;
    private MissionType type;
    private MissionDifficulty difficulty;
    private MissionStatus status;
    private String planetName;        // planet where mission takes place
    private String systemName;        // star system
    private String mapName;           // map/battlefield name
    private String faction;           // faction offering the mission
    private String enemyFaction;      // opposing faction
    private int timeLimit;            // time limit in seconds (0 = no limit)
    private int waveCount;            // number of waves for survival missions
    private int enemyCount;           // expected number of enemy units
    private int playerLanceSize;      // number of mechs in player lance
    private List<MissionObjective> objectives;
    private List<MissionReward> rewards;
    private int creditsReward;        // base credits reward
    private int salvageReward;        // base salvage value
    private int experienceReward;     // base experience reward
    private int reputationReward;     // reputation points with offering faction
    private String briefing;          // mission briefing text
    private String debriefing;        // post-mission debriefing
    private boolean isCampaign;       // part of campaign chain
    private String prerequisiteMissionId; // must complete this mission first
    private String nextMissionId;     // unlocks this mission on completion
    private int minReputation;        // minimum reputation to accept
    private int minPilotLevel;        // minimum pilot experience level
    private boolean isRepeatable;     // can be repeated after completion
    private boolean isCoop;           // supports cooperative play
    private int maxCoopPlayers;       // max players in co-op
    private long createdAt;
    private long acceptedAt;
    private long completedAt;

    public Mission() {
    }

    public Mission(String name, MissionType type, MissionDifficulty difficulty, String faction) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.difficulty = difficulty;
        this.faction = faction;
        this.status = MissionStatus.AVAILABLE;
        this.objectives = new ArrayList<>();
        this.rewards = new ArrayList<>();
        this.timeLimit = 0;
        this.waveCount = 1;
        this.enemyCount = 4;
        this.playerLanceSize = 4;
        this.creditsReward = 10000;
        this.salvageReward = 5000;
        this.experienceReward = 100;
        this.reputationReward = 10;
        this.isCampaign = false;
        this.isRepeatable = false;
        this.isCoop = false;
        this.maxCoopPlayers = 1;
        this.minReputation = 0;
        this.minPilotLevel = 0;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Add an objective to the mission.
     */
    public void addObjective(MissionObjective objective) {
        objectives.add(objective);
    }

    /**
     * Add a reward to the mission.
     */
    public void addReward(MissionReward reward) {
        rewards.add(reward);
    }

    /**
     * Accept the mission.
     */
    public void accept() {
        if (status == MissionStatus.AVAILABLE) {
            this.status = MissionStatus.ACCEPTED;
            this.acceptedAt = System.currentTimeMillis();
        }
    }

    /**
     * Start the mission.
     */
    public void start() {
        if (status == MissionStatus.ACCEPTED) {
            this.status = MissionStatus.IN_PROGRESS;
        }
    }

    /**
     * Complete the mission.
     */
    public void complete() {
        if (status == MissionStatus.IN_PROGRESS) {
            this.status = MissionStatus.COMPLETED;
            this.completedAt = System.currentTimeMillis();
        }
    }

    /**
     * Fail the mission.
     */
    public void fail() {
        if (status == MissionStatus.IN_PROGRESS) {
            this.status = MissionStatus.FAILED;
            this.completedAt = System.currentTimeMillis();
        }
    }

    /**
     * Abandon the mission.
     */
    public void abandon() {
        if (status == MissionStatus.ACCEPTED || status == MissionStatus.IN_PROGRESS) {
            this.status = MissionStatus.ABANDONED;
            this.completedAt = System.currentTimeMillis();
        }
    }

    /**
     * Check if all objectives are completed.
     */
    public boolean areAllObjectivesCompleted() {
        if (objectives.isEmpty()) {
            return false;
        }
        for (MissionObjective obj : objectives) {
            if (!obj.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the number of completed objectives.
     */
    public int getCompletedObjectiveCount() {
        int count = 0;
        for (MissionObjective obj : objectives) {
            if (obj.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate the final credits reward based on difficulty and objectives.
     */
    public int calculateFinalCredits() {
        double multiplier = difficulty.getRewardMultiplier();
        int objectiveBonus = getCompletedObjectiveCount() * 1000;
        return (int) ((creditsReward + objectiveBonus) * multiplier);
    }

    /**
     * Calculate the final experience reward based on difficulty.
     */
    public int calculateFinalExperience() {
        double multiplier = difficulty.getRewardMultiplier();
        return (int) (experienceReward * multiplier);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MissionType getType() {
        return type;
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public MissionDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(MissionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getEnemyFaction() {
        return enemyFaction;
    }

    public void setEnemyFaction(String enemyFaction) {
        this.enemyFaction = enemyFaction;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public void setWaveCount(int waveCount) {
        this.waveCount = waveCount;
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public void setEnemyCount(int enemyCount) {
        this.enemyCount = enemyCount;
    }

    public int getPlayerLanceSize() {
        return playerLanceSize;
    }

    public void setPlayerLanceSize(int playerLanceSize) {
        this.playerLanceSize = playerLanceSize;
    }

    public List<MissionObjective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<MissionObjective> objectives) {
        this.objectives = objectives;
    }

    public List<MissionReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<MissionReward> rewards) {
        this.rewards = rewards;
    }

    public int getCreditsReward() {
        return creditsReward;
    }

    public void setCreditsReward(int creditsReward) {
        this.creditsReward = creditsReward;
    }

    public int getSalvageReward() {
        return salvageReward;
    }

    public void setSalvageReward(int salvageReward) {
        this.salvageReward = salvageReward;
    }

    public int getExperienceReward() {
        return experienceReward;
    }

    public void setExperienceReward(int experienceReward) {
        this.experienceReward = experienceReward;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public void setReputationReward(int reputationReward) {
        this.reputationReward = reputationReward;
    }

    public String getBriefing() {
        return briefing;
    }

    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    public String getDebriefing() {
        return debriefing;
    }

    public void setDebriefing(String debriefing) {
        this.debriefing = debriefing;
    }

    public boolean isCampaign() {
        return isCampaign;
    }

    public void setCampaign(boolean campaign) {
        isCampaign = campaign;
    }

    public String getPrerequisiteMissionId() {
        return prerequisiteMissionId;
    }

    public void setPrerequisiteMissionId(String prerequisiteMissionId) {
        this.prerequisiteMissionId = prerequisiteMissionId;
    }

    public String getNextMissionId() {
        return nextMissionId;
    }

    public void setNextMissionId(String nextMissionId) {
        this.nextMissionId = nextMissionId;
    }

    public int getMinReputation() {
        return minReputation;
    }

    public void setMinReputation(int minReputation) {
        this.minReputation = minReputation;
    }

    public int getMinPilotLevel() {
        return minPilotLevel;
    }

    public void setMinPilotLevel(int minPilotLevel) {
        this.minPilotLevel = minPilotLevel;
    }

    public boolean isRepeatable() {
        return isRepeatable;
    }

    public void setRepeatable(boolean repeatable) {
        isRepeatable = repeatable;
    }

    public boolean isCoop() {
        return isCoop;
    }

    public void setCoop(boolean coop) {
        isCoop = coop;
    }

    public int getMaxCoopPlayers() {
        return maxCoopPlayers;
    }

    public void setMaxCoopPlayers(int maxCoopPlayers) {
        this.maxCoopPlayers = maxCoopPlayers;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(long acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}
