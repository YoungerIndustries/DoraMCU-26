package com.yisy.modules.doramcu.mechwarrior.service;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorGameEngine;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorMech;
import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import com.yisy.modules.doramcu.mechwarrior.model.Mission;
import com.yisy.modules.doramcu.mechwarrior.model.MissionObjective;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing MechWarrior missions.
 * <p>
 * Handles mission creation, acceptance, progression, completion,
 * and reward distribution. Integrates with the game engine for
 * combat-based missions.
 * </p>
 * <p>
 * Supports all mission types from MechWarrior FPS games:
 * assault, defense, escort, recon, extraction, capture, sabotage,
 * survival, patrol, ambush, interdiction, search & destroy, and Solaris.
 * </p>
 */
@Service
public class MissionService {

    private static final Logger log = LoggerFactory.getLogger(MissionService.class);

    private final MechWarriorGameEngine gameEngine;

    // All available missions
    private final Map<String, Mission> missions = new ConcurrentHashMap<>();

    // Active missions (accepted or in progress) keyed by pilot ID
    private final Map<String, String> activeMissions = new ConcurrentHashMap<>();

    // Mission statistics per pilot
    private final Map<String, PilotMissionStats> pilotStats = new ConcurrentHashMap<>();

    // Faction reputation per pilot
    private final Map<String, Map<String, Integer>> factionReputation = new ConcurrentHashMap<>();

    // Pilot credits
    private final Map<String, Integer> pilotCredits = new ConcurrentHashMap<>();

    public MissionService(MechWarriorGameEngine gameEngine) {
        this.gameEngine = gameEngine;
        initializeDefaultMissions();
    }

    /**
     * Initialize default missions based on MechWarrior lore.
     */
    private void initializeDefaultMissions() {
        // Assault mission
        Mission assault = new Mission(
                "Operation: Iron Hammer",
                Mission.MissionType.ASSAULT,
                Mission.MissionDifficulty.NORMAL,
                "Federated Suns"
        );
        assault.setDescription("Destroy the enemy mech force threatening the colony.");
        assault.setEnemyFaction("Draconis Combine");
        assault.setPlanetName("Tharkad");
        assault.setSystemName("Tharkad");
        assault.setMapName("Urban Ruins");
        assault.setEnemyCount(6);
        assault.setCreditsReward(25000);
        assault.setExperienceReward(200);
        assault.setReputationReward(15);
        assault.setBriefing("Enemy forces have been spotted advancing on the colony. " +
                "Your lance is to intercept and destroy them before they reach civilian areas.");
        assault.addObjective(new MissionObjective(
                "Destroy 6 enemy mechs", MissionObjective.ObjectiveType.DESTROY_ENEMIES
        ));
        missions.put(assault.getId(), assault);

        // Defense mission
        Mission defense = new Mission(
                "Hold the Line",
                Mission.MissionType.DEFENSE,
                Mission.MissionDifficulty.HARD,
                "Comstar"
        );
        defense.setDescription("Defend the Comstar communications array from enemy attack.");
        defense.setEnemyFaction("Word of Blake");
        defense.setPlanetName("Tukayyid");
        defense.setSystemName("Tukayyid");
        defense.setMapName("Desert Outpost");
        defense.setTimeLimit(300); // 5 minutes
        defense.setEnemyCount(8);
        defense.setCreditsReward(40000);
        defense.setExperienceReward(350);
        defense.setReputationReward(20);
        defense.setBriefing("The communications array is our only link to command. " +
                "Hold the position until reinforcements arrive.");
        defense.addObjective(new MissionObjective(
                "Survive for 5 minutes", MissionObjective.ObjectiveType.SURVIVE
        ));
        defense.getObjectives().get(0).setDurationSeconds(300);
        missions.put(defense.getId(), defense);

        // Escort mission
        Mission escort = new Mission(
                "Safe Passage",
                Mission.MissionType.ESCORT,
                Mission.MissionDifficulty.NORMAL,
                "Clan Wolf"
        );
        escort.setDescription("Escort a Clan Wolf dropship through hostile territory.");
        escort.setEnemyFaction("Clan Jade Falcon");
        escort.setPlanetName("Basilisk");
        escort.setSystemName("Basilisk");
        escort.setMapName("Mountain Pass");
        escort.setEnemyCount(4);
        escort.setCreditsReward(20000);
        escort.setExperienceReward(150);
        escort.setReputationReward(10);
        escort.setBriefing("The dropship carries vital supplies. Protect it at all costs.");
        escort.addObjective(new MissionObjective(
                "Protect the dropship", MissionObjective.ObjectiveType.ESCORT
        ));
        missions.put(escort.getId(), escort);

        // Recon mission
        Mission recon = new Mission(
                "Shadow Run",
                Mission.MissionType.RECON,
                Mission.MissionDifficulty.EASY,
                "Federated Suns"
        );
        recon.setDescription("Gather intelligence on enemy positions without engaging.");
        recon.setEnemyFaction("Draconis Combine");
        recon.setPlanetName("New Avalon");
        recon.setSystemName("Avalon");
        recon.setMapName("Forest Ridge");
        recon.setEnemyCount(3);
        recon.setCreditsReward(15000);
        recon.setExperienceReward(100);
        recon.setReputationReward(8);
        recon.setBriefing("Intel reports enemy movement in the sector. " +
                "Locate and report their positions without engaging.");
        recon.addObjective(new MissionObjective(
                "Locate 3 enemy positions", MissionObjective.ObjectiveType.RECON
        ));
        recon.getObjectives().get(0).setTargetCount(3);
        missions.put(recon.getId(), recon);

        // Survival mission
        Mission survival = new Mission(
                "Last Stand",
                Mission.MissionType.SURVIVAL,
                Mission.MissionDifficulty.EXTREME,
                "Lyran Commonwealth"
        );
        survival.setDescription("Survive waves of enemy attacks on an isolated outpost.");
        survival.setEnemyFaction("Free Worlds League");
        survival.setPlanetName("New London");
        survival.setSystemName("New London");
        survival.setMapName("Frozen Plains");
        survival.setWaveCount(5);
        survival.setEnemyCount(20);
        survival.setCreditsReward(60000);
        survival.setExperienceReward(500);
        survival.setReputationReward(30);
        survival.setBriefing("Reinforcements are hours away. Hold the line against all odds.");
        survival.addObjective(new MissionObjective(
                "Survive 5 waves of attacks", MissionObjective.ObjectiveType.SURVIVE
        ));
        survival.getObjectives().get(0).setTargetCount(5);
        missions.put(survival.getId(), survival);

        // Solaris mission
        Mission solaris = new Mission(
                "Solaris Arena: Duel",
                Mission.MissionType.SOLARIS,
                Mission.MissionDifficulty.NORMAL,
                "Solaris Syndicate"
        );
        solaris.setDescription("Enter the Solaris arena and prove your worth in combat.");
        solaris.setEnemyFaction("Solaris Syndicate");
        solaris.setPlanetName("Solaris VII");
        solaris.setSystemName("Solaris");
        solaris.setMapName("Arena");
        solaris.setEnemyCount(1);
        solaris.setCreditsReward(50000);
        solaris.setExperienceReward(250);
        solaris.setReputationReward(25);
        solaris.setBriefing("Welcome to Solaris. Defeat your opponent and claim your prize.");
        solaris.addObjective(new MissionObjective(
                "Defeat the opponent", MissionObjective.ObjectiveType.DESTROY_ENEMIES
        ));
        missions.put(solaris.getId(), solaris);

        // Extraction mission
        Mission extraction = new Mission(
                "Rescue Operation",
                Mission.MissionType.EXTRACTION,
                Mission.MissionDifficulty.HARD,
                "Capellan Confederation"
        );
        extraction.setDescription("Extract a captured pilot from enemy territory.");
        extraction.setEnemyFaction("Federated Suns");
        extraction.setPlanetName("Sian");
        extraction.setSystemName("Sian");
        extraction.setMapName("Jungle Valley");
        extraction.setEnemyCount(5);
        extraction.setCreditsReward(35000);
        extraction.setExperienceReward(300);
        extraction.setReputationReward(18);
        extraction.setBriefing("A captured pilot is being held in the enemy compound. " +
                "Extract them before they are executed.");
        extraction.addObjective(new MissionObjective(
                "Reach the extraction point", MissionObjective.ObjectiveType.EXTRACTION
        ));
        missions.put(extraction.getId(), extraction);

        // Sabotage mission
        Mission sabotage = new Mission(
                "Silent Strike",
                Mission.MissionType.SABOTAGE,
                Mission.MissionDifficulty.HARD,
                "Clan Jade Falcon"
        );
        sabotage.setDescription("Destroy the enemy weapon factory before it can be used.");
        sabotage.setEnemyFaction("Clan Wolf");
        sabotage.setPlanetName("Essex");
        sabotage.setSystemName("Essex");
        sabotage.setMapName("Industrial Complex");
        sabotage.setEnemyCount(4);
        sabotage.setCreditsReward(45000);
        sabotage.setExperienceReward(400);
        sabotage.setReputationReward(22);
        sabotage.setBriefing("Intel confirms a weapon factory is operational. " +
                "Destroy it before it can arm enemy forces.");
        sabotage.addObjective(new MissionObjective(
                "Destroy the factory", MissionObjective.ObjectiveType.SABOTAGE
        ));
        missions.put(sabotage.getId(), sabotage);

        // Custom Instant Action mission
        Mission custom = new Mission(
                "Instant Action: Test Run",
                Mission.MissionType.CUSTOM,
                Mission.MissionDifficulty.EASY,
                "Independent"
        );
        custom.setDescription("Test your mech loadout in a quick combat scenario.");
        custom.setEnemyFaction("Independent");
        custom.setPlanetName("Unknown");
        custom.setSystemName("Unknown");
        custom.setMapName("Random");
        custom.setEnemyCount(3);
        custom.setCreditsReward(5000);
        custom.setExperienceReward(50);
        custom.setReputationReward(0);
        custom.setRepeatable(true);
        custom.setBriefing("Quick combat scenario for testing loadouts and practice.");
        custom.addObjective(new MissionObjective(
                "Destroy 3 enemy mechs", MissionObjective.ObjectiveType.DESTROY_ENEMIES
        ));
        custom.getObjectives().get(0).setTargetCount(3);
        missions.put(custom.getId(), custom);

        log.info("Initialized {} default missions", missions.size());
    }

    /**
     * Get all available missions.
     */
    public List<Mission> getAvailableMissions() {
        return missions.values().stream()
                .filter(m -> m.getStatus() == Mission.MissionStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * Get missions by type.
     */
    public List<Mission> getMissionsByType(Mission.MissionType type) {
        return missions.values().stream()
                .filter(m -> m.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get missions by difficulty.
     */
    public List<Mission> getMissionsByDifficulty(Mission.MissionDifficulty difficulty) {
        return missions.values().stream()
                .filter(m -> m.getDifficulty() == difficulty)
                .collect(Collectors.toList());
    }

    /**
     * Get missions by faction.
     */
    public List<Mission> getMissionsByFaction(String faction) {
        return missions.values().stream()
                .filter(m -> faction.equals(m.getFaction()))
                .collect(Collectors.toList());
    }

    /**
     * Get a mission by ID.
     */
    public Mission getMission(String missionId) {
        return missions.get(missionId);
    }

    /**
     * Get all missions.
     */
    public List<Mission> getAllMissions() {
        return new ArrayList<>(missions.values());
    }

    /**
     * Accept a mission for a pilot.
     */
    public boolean acceptMission(String pilotId, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            log.warn("Mission not found: {}", missionId);
            return false;
        }
        if (mission.getStatus() != Mission.MissionStatus.AVAILABLE) {
            log.warn("Mission not available: {}", missionId);
            return false;
        }

        // Check prerequisites
        if (mission.getPrerequisiteMissionId() != null) {
            PilotMissionStats stats = pilotStats.get(pilotId);
            if (stats == null || !stats.getCompletedMissionIds().contains(mission.getPrerequisiteMissionId())) {
                log.warn("Prerequisite not met for mission: {}", missionId);
                return false;
            }
        }

        // Check reputation requirement
        if (mission.getMinReputation() > 0) {
            int reputation = getReputation(pilotId, mission.getFaction());
            if (reputation < mission.getMinReputation()) {
                log.warn("Insufficient reputation for mission: {}", missionId);
                return false;
            }
        }

        // Check pilot level requirement
        if (mission.getMinPilotLevel() > 0) {
            PilotMissionStats stats = pilotStats.get(pilotId);
            if (stats == null || stats.getLevel() < mission.getMinPilotLevel()) {
                log.warn("Insufficient pilot level for mission: {}", missionId);
                return false;
            }
        }

        // Check if pilot already has an active mission (unless repeatable)
        if (!mission.isRepeatable() && activeMissions.containsKey(pilotId)) {
            log.warn("Pilot already has an active mission: {}", pilotId);
            return false;
        }

        mission.accept();
        activeMissions.put(pilotId, missionId);
        log.info("Pilot {} accepted mission {}", pilotId, mission.getName());
        return true;
    }

    /**
     * Start a mission, creating a game session.
     */
    public MechWarriorSession startMission(String pilotId, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null || mission.getStatus() != Mission.MissionStatus.ACCEPTED) {
            throw new IllegalArgumentException("Mission cannot be started: " + missionId);
        }

        mission.start();

        // Create game session for the mission
        String sessionId = UUID.randomUUID().toString();
        MechWarriorSession session = gameEngine.createSession(
                sessionId,
                mission.getMapName(),
                mission.getPlayerLanceSize()
        );
        session.startMatch();

        log.info("Started mission {} with session {}", mission.getName(), sessionId);
        return session;
    }

    /**
     * Record a kill during a mission, progressing objectives.
     */
    public void recordKill(String pilotId, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            return;
        }

        // Progress destroy objectives
        for (MissionObjective obj : mission.getObjectives()) {
            if (obj.getType() == MissionObjective.ObjectiveType.DESTROY_ENEMIES && !obj.isCompleted()) {
                obj.progress(1);
            }
        }

        // Check if mission is complete
        checkMissionCompletion(pilotId, mission);
    }

    /**
     * Record objective progress.
     */
    public void recordObjectiveProgress(String pilotId, String missionId, String objectiveId, int amount) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            return;
        }

        for (MissionObjective obj : mission.getObjectives()) {
            if (objectiveId.equals(obj.getId()) && !obj.isCompleted()) {
                obj.progress(amount);
            }
        }

        checkMissionCompletion(pilotId, mission);
    }

    /**
     * Record time elapsed for duration-based objectives.
     */
    public void recordTimeElapsed(String pilotId, String missionId, int seconds) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            return;
        }

        for (MissionObjective obj : mission.getObjectives()) {
            if ((obj.getType() == MissionObjective.ObjectiveType.SURVIVE ||
                 obj.getType() == MissionObjective.ObjectiveType.HOLD_POSITION) &&
                !obj.isCompleted()) {
                obj.addElapsedSeconds(seconds);
            }
        }

        checkMissionCompletion(pilotId, mission);
    }

    /**
     * Check if all objectives are complete and complete the mission.
     */
    private void checkMissionCompletion(String pilotId, Mission mission) {
        if (mission.areAllObjectivesCompleted()) {
            completeMission(pilotId, mission.getId());
        }
    }

    /**
     * Complete a mission and distribute rewards.
     */
    public MissionResult completeMission(String pilotId, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            throw new IllegalArgumentException("Mission not found: " + missionId);
        }

        mission.complete();
        activeMissions.remove(pilotId);

        // Calculate rewards
        MissionResult result = new MissionResult();
        result.setMissionId(missionId);
        result.setMissionName(mission.getName());
        result.setCompleted(true);

        int credits = mission.calculateFinalCredits();
        int experience = mission.calculateFinalExperience();
        int reputation = mission.getReputationReward();

        result.setCreditsEarned(credits);
        result.setExperienceEarned(experience);
        result.setReputationEarned(reputation);

        // Update pilot stats
        PilotMissionStats stats = getOrCreatePilotStats(pilotId);
        stats.addCompletedMission(missionId);
        stats.addCredits(credits);
        stats.addExperience(experience);
        stats.addWins();

        // Update reputation
        addReputation(pilotId, mission.getFaction(), reputation);

        // Update credits
        addCredits(pilotId, credits);

        // Reset mission if repeatable
        if (mission.isRepeatable()) {
            resetMission(mission);
        }

        // Unlock next mission in campaign chain
        if (mission.isCampaign() && mission.getNextMissionId() != null) {
            Mission nextMission = missions.get(mission.getNextMissionId());
            if (nextMission != null) {
                nextMission.setStatus(Mission.MissionStatus.AVAILABLE);
                log.info("Unlocked next campaign mission: {}", nextMission.getName());
            }
        }

        log.info("Pilot {} completed mission {} with rewards: {} credits, {} XP, {} rep",
                pilotId, mission.getName(), credits, experience, reputation);
        return result;
    }

    /**
     * Fail a mission.
     */
    public MissionResult failMission(String pilotId, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            throw new IllegalArgumentException("Mission not found: " + missionId);
        }

        mission.fail();
        activeMissions.remove(pilotId);

        MissionResult result = new MissionResult();
        result.setMissionId(missionId);
        result.setMissionName(mission.getName());
        result.setCompleted(false);

        // Update pilot stats
        PilotMissionStats stats = getOrCreatePilotStats(pilotId);
        stats.addLosses();

        // Reduce reputation
        addReputation(pilotId, mission.getFaction(), -mission.getReputationReward() / 2);

        // Reset mission if repeatable
        if (mission.isRepeatable()) {
            resetMission(mission);
        }

        log.info("Pilot {} failed mission {}", pilotId, mission.getName());
        return result;
    }

    /**
     * Abandon a mission.
     */
    public void abandonMission(String pilotId, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null) {
            return;
        }

        mission.abandon();
        activeMissions.remove(pilotId);

        // Small reputation penalty
        addReputation(pilotId, mission.getFaction(), -5);

        if (mission.isRepeatable()) {
            resetMission(mission);
        }

        log.info("Pilot {} abandoned mission {}", pilotId, mission.getName());
    }

    /**
     * Reset a mission to available state.
     */
    private void resetMission(Mission mission) {
        mission.setStatus(Mission.MissionStatus.AVAILABLE);
        for (MissionObjective obj : mission.getObjectives()) {
            obj.setCompleted(false);
            obj.setCurrentCount(0);
            obj.setElapsedSeconds(0);
        }
    }

    /**
     * Get or create pilot mission stats.
     */
    private PilotMissionStats getOrCreatePilotStats(String pilotId) {
        return pilotStats.computeIfAbsent(pilotId, id -> new PilotMissionStats());
    }

    /**
     * Get pilot mission stats.
     */
    public PilotMissionStats getPilotStats(String pilotId) {
        return pilotStats.get(pilotId);
    }

    /**
     * Get reputation with a faction.
     */
    public int getReputation(String pilotId, String faction) {
        Map<String, Integer> repMap = factionReputation.get(pilotId);
        if (repMap == null) {
            return 0;
        }
        return repMap.getOrDefault(faction, 0);
    }

    /**
     * Add reputation with a faction.
     */
    public void addReputation(String pilotId, String faction, int amount) {
        Map<String, Integer> repMap = factionReputation.computeIfAbsent(pilotId, id -> new HashMap<>());
        int current = repMap.getOrDefault(faction, 0);
        repMap.put(faction, Math.max(0, current + amount));
    }

    /**
     * Get all faction reputations for a pilot.
     */
    public Map<String, Integer> getAllReputations(String pilotId) {
        Map<String, Integer> repMap = factionReputation.get(pilotId);
        if (repMap == null) {
            return new HashMap<>();
        }
        return new HashMap<>(repMap);
    }

    /**
     * Add credits to a pilot.
     */
    public void addCredits(String pilotId, int amount) {
        pilotCredits.merge(pilotId, amount, Integer::sum);
    }

    /**
     * Get pilot credits.
     */
    public int getCredits(String pilotId) {
        return pilotCredits.getOrDefault(pilotId, 0);
    }

    /**
     * Spend credits.
     */
    public boolean spendCredits(String pilotId, int amount) {
        int current = getCredits(pilotId);
        if (current >= amount) {
            pilotCredits.put(pilotId, current - amount);
            return true;
        }
        return false;
    }

    /**
     * Create a custom mission.
     */
    public Mission createCustomMission(String name, Mission.MissionType type,
                                        Mission.MissionDifficulty difficulty,
                                        String faction, String description,
                                        int enemyCount, int creditsReward) {
        Mission mission = new Mission(name, type, difficulty, faction);
        mission.setDescription(description);
        mission.setEnemyCount(enemyCount);
        mission.setCreditsReward(creditsReward);
        mission.setRepeatable(true);
        mission.setBriefing(description);

        MissionObjective obj = new MissionObjective(
                "Destroy " + enemyCount + " enemy mechs",
                MissionObjective.ObjectiveType.DESTROY_ENEMIES
        );
        obj.setTargetCount(enemyCount);
        mission.addObjective(obj);

        missions.put(mission.getId(), mission);
        log.info("Created custom mission: {}", name);
        return mission;
    }

    /**
     * Remove a mission.
     */
    public void removeMission(String missionId) {
        missions.remove(missionId);
        log.info("Removed mission: {}", missionId);
    }

    /**
     * Get the active mission for a pilot.
     */
    public Mission getActiveMission(String pilotId) {
        String missionId = activeMissions.get(pilotId);
        if (missionId == null) {
            return null;
        }
        return missions.get(missionId);
    }

    /**
     * Result of completing or failing a mission.
     */
    public static class MissionResult {
        private String missionId;
        private String missionName;
        private boolean completed;
        private int creditsEarned;
        private int experienceEarned;
        private int reputationEarned;
        private long timestamp;

        public MissionResult() {
            this.timestamp = System.currentTimeMillis();
        }

        public String getMissionId() {
            return missionId;
        }

        public void setMissionId(String missionId) {
            this.missionId = missionId;
        }

        public String getMissionName() {
            return missionName;
        }

        public void setMissionName(String missionName) {
            this.missionName = missionName;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public int getCreditsEarned() {
            return creditsEarned;
        }

        public void setCreditsEarned(int creditsEarned) {
            this.creditsEarned = creditsEarned;
        }

        public int getExperienceEarned() {
            return experienceEarned;
        }

        public void setExperienceEarned(int experienceEarned) {
            this.experienceEarned = experienceEarned;
        }

        public int getReputationEarned() {
            return reputationEarned;
        }

        public void setReputationEarned(int reputationEarned) {
            this.reputationEarned = reputationEarned;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Tracks a pilot's mission statistics.
     */
    public static class PilotMissionStats {
        private int wins;
        private int losses;
        private int totalCredits;
        private int totalExperience;
        private int level;
        private Set<String> completedMissionIds;

        public PilotMissionStats() {
            this.wins = 0;
            this.losses = 0;
            this.totalCredits = 0;
            this.totalExperience = 0;
            this.level = 1;
            this.completedMissionIds = new HashSet<>();
        }

        public void addCompletedMission(String missionId) {
            completedMissionIds.add(missionId);
        }

        public void addCredits(int amount) {
            totalCredits += amount;
        }

        public void addExperience(int amount) {
            totalExperience += amount;
            // Level up every 500 XP
            level = Math.max(level, totalExperience / 500 + 1);
        }

        public void addWins() {
            wins++;
        }

        public void addLosses() {
            losses++;
        }

        public int getWins() {
            return wins;
        }

        public int getLosses() {
            return losses;
        }

        public int getTotalCredits() {
            return totalCredits;
        }

        public int getTotalExperience() {
            return totalExperience;
        }

        public int getLevel() {
            return level;
        }

        public Set<String> getCompletedMissionIds() {
            return completedMissionIds;
        }

        public double getWinRate() {
            int total = wins + losses;
            if (total == 0) {
                return 0.0;
            }
            return (double) wins / total * 100.0;
        }
    }
}
