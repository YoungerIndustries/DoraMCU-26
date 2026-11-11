package com.yisy.modules.doramcu.hex.planet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages RPG-style quests issued by AI empires to the player.
 * <p>
 * The RpgQuestSystem is the narrative bridge between the 4X galaxy layer and
 * the tycoon economy layer. It:
 * <ul>
 *   <li>Issues quests from AI empires based on their needs and personality</li>
 *   <li>Tracks quest acceptance, progress, and completion</li>
 *   <li>Distributes rewards (credits, XP, reputation) on completion</li>
 *   <li>Creates quest chains for narrative arcs</li>
 *   <li>Generates dynamic quests based on galaxy events (wars, alliances)</li>
 * </ul>
 * </p>
 * <p>
 * Quest chains create narrative arcs:
 * <pre>
 *   Supply a war effort → Gain alliance → Access new systems → Build infrastructure
 * </pre>
 * </p>
 */
public class RpgQuestSystem {

    /** All available quests */
    private final Map<String, RpgQuest> allQuests;

    /** Active quests (accepted by player) */
    private final Set<String> activeQuestIds;

    /** Completed quests history */
    private final List<RpgQuest> completedQuests;

    /** Quest counter for unique IDs */
    private final AtomicLong questCounter;

    /** Random for quest generation */
    private final Random random;

    /** Maximum concurrent active quests */
    private static final int MAX_ACTIVE_QUESTS = 5;

    /** Quest generation interval (turns) */
    private static final int QUEST_GENERATION_INTERVAL = 3;

    /**
     * Creates a new quest system.
     */
    public RpgQuestSystem() {
        this.allQuests = new ConcurrentHashMap<>();
        this.activeQuestIds = ConcurrentHashMap.newKeySet();
        this.completedQuests = new ArrayList<>();
        this.questCounter = new AtomicLong(0);
        this.random = new Random();
    }

    /**
     * Issues a new quest from an empire.
     *
     * @param empireId the empire issuing the quest
     * @param empireName the empire's name
     * @param personality the empire's personality
     * @param isAtWar whether the empire is at war
     * @return the issued quest
     */
    public RpgQuest issueQuest(int empireId, String empireName,
                                GalaxyOverlordAI.AiPersonality personality,
                                boolean isAtWar) {
        String id = "quest_" + questCounter.incrementAndGet();

        // Choose quest type based on empire state
        RpgQuest.QuestType type;
        if (isAtWar && random.nextDouble() < 0.5) {
            type = RpgQuest.QuestType.MILITARY_LOGISTICS;
        } else {
            RpgQuest.QuestType[] types = RpgQuest.QuestType.values();
            type = types[random.nextInt(types.length)];
        }

        // Generate quest parameters based on type
        String title;
        String description;
        int targetValue;
        int rewardCredits;
        int rewardXp;
        int rewardReputation;
        int maxTurns;
        int minReputation;
        CargoType cargoType = null;
        String destinationPlanetId = null;

        switch (type) {
            case DELIVERY:
                title = "Urgent Delivery for " + empireName;
                description = "The " + empireName + " needs cargo transported to their facilities.";
                targetValue = 100 + random.nextInt(200);
                rewardCredits = 500 + random.nextInt(1000);
                rewardXp = 20 + random.nextInt(30);
                rewardReputation = 5 + random.nextInt(10);
                maxTurns = 5 + random.nextInt(5);
                minReputation = 10;
                CargoType[] cargoTypes = CargoType.values();
                cargoType = cargoTypes[random.nextInt(cargoTypes.length)];
                break;

            case CONSTRUCTION:
                title = "Infrastructure Project for " + empireName;
                description = "The " + empireName + " requests you build infrastructure on their planet.";
                targetValue = 3 + random.nextInt(5);
                rewardCredits = 2000 + random.nextInt(3000);
                rewardXp = 50 + random.nextInt(50);
                rewardReputation = 10 + random.nextInt(15);
                maxTurns = 10 + random.nextInt(10);
                minReputation = 20;
                break;

            case ESPIONAGE:
                title = "Intelligence Gathering for " + empireName;
                description = "The " + empireName + " needs information about rival operations.";
                targetValue = 3 + random.nextInt(5);
                rewardCredits = 1000 + random.nextInt(2000);
                rewardXp = 30 + random.nextInt(40);
                rewardReputation = 8 + random.nextInt(12);
                maxTurns = 5 + random.nextInt(5);
                minReputation = 30;
                break;

            case MILITARY_LOGISTICS:
                title = "War Supply Run for " + empireName;
                description = "The " + empireName + " is at war and needs supplies delivered to the front.";
                targetValue = 200 + random.nextInt(300);
                rewardCredits = 3000 + random.nextInt(5000);
                rewardXp = 60 + random.nextInt(60);
                rewardReputation = 15 + random.nextInt(20);
                maxTurns = 8 + random.nextInt(5);
                minReputation = 25;
                break;

            case ROUTE_ESTABLISHMENT:
                title = "New Trade Route for " + empireName;
                description = "The " + empireName + " wants you to establish a new trade connection.";
                targetValue = 1;
                rewardCredits = 1500 + random.nextInt(2000);
                rewardXp = 40 + random.nextInt(30);
                rewardReputation = 10 + random.nextInt(10);
                maxTurns = 10 + random.nextInt(5);
                minReputation = 15;
                break;

            case RESCUE:
                title = "Rescue Mission for " + empireName;
                description = "Citizens of " + empireName + " are stranded and need rescue.";
                targetValue = 1;
                rewardCredits = 800 + random.nextInt(1200);
                rewardXp = 25 + random.nextInt(25);
                rewardReputation = 12 + random.nextInt(13);
                maxTurns = 3 + random.nextInt(3);
                minReputation = 5;
                break;

            case ESCORT:
                title = "Convoy Escort for " + empireName;
                description = "A valuable convoy from " + empireName + " needs protection.";
                targetValue = 5 + random.nextInt(5);
                rewardCredits = 1200 + random.nextInt(1800);
                rewardXp = 35 + random.nextInt(35);
                rewardReputation = 8 + random.nextInt(12);
                maxTurns = 5 + random.nextInt(5);
                minReputation = 20;
                break;

            case SABOTAGE:
                title = "Covert Operation for " + empireName;
                description = "The " + empireName + " needs a rival operation disrupted.";
                targetValue = 3 + random.nextInt(3);
                rewardCredits = 2000 + random.nextInt(3000);
                rewardXp = 50 + random.nextInt(50);
                rewardReputation = 5 + random.nextInt(10);
                maxTurns = 5 + random.nextInt(5);
                minReputation = 40;
                break;

            default:
                title = "Mission for " + empireName;
                description = "Complete a task for the " + empireName + ".";
                targetValue = 100;
                rewardCredits = 1000;
                rewardXp = 30;
                rewardReputation = 10;
                maxTurns = 10;
                minReputation = 10;
                break;
        }

        // Scale rewards by personality
        if (personality == GalaxyOverlordAI.AiPersonality.ECONOMIC) {
            rewardCredits = (int) (rewardCredits * 1.3);
        } else if (personality == GalaxyOverlordAI.AiPersonality.AGGRESSIVE) {
            rewardReputation = (int) (rewardReputation * 0.7);
        }

        RpgQuest quest = new RpgQuest(
                id, title, description, type,
                empireId, targetValue,
                rewardCredits, rewardXp, rewardReputation,
                maxTurns, minReputation, CorporationRank.FREELANCER,
                cargoType, destinationPlanetId
        );

        allQuests.put(id, quest);
        return quest;
    }

    /**
     * Generates new quests from all empires periodically.
     *
     * @param empires map of empire ID to GalaxyOverlordAI
     * @param turnNumber current turn number
     */
    public void generateQuests(Map<Integer, GalaxyOverlordAI> empires, int turnNumber) {
        if (turnNumber % QUEST_GENERATION_INTERVAL != 0) {
            return;
        }

        for (GalaxyOverlordAI empire : empires.values()) {
            // Limit quests per empire
            int empireQuestCount = (int) allQuests.values().stream()
                    .filter(q -> q.getIssuingEmpireId() == empire.getEmpireId() &&
                            q.getStatus() == RpgQuest.QuestStatus.AVAILABLE)
                    .count();

            if (empireQuestCount < 3) {
                boolean isAtWar = !empire.getEnemies().isEmpty();
                issueQuest(empire.getEmpireId(), empire.getEmpireName(),
                        empire.getPersonality(), isAtWar);
            }
        }
    }

    /**
     * Accepts a quest for the player.
     *
     * @param questId the quest ID
     * @param corp the player's corporation
     * @param reputationTracker the reputation tracker
     * @return true if the quest was accepted
     */
    public boolean acceptQuest(String questId, TransportCorporation corp,
                                EmpireReputationTracker reputationTracker) {
        RpgQuest quest = allQuests.get(questId);
        if (quest == null || quest.getStatus() != RpgQuest.QuestStatus.AVAILABLE) {
            return false;
        }

        // Check prerequisites
        int reputation = reputationTracker.getReputation(quest.getIssuingEmpireId());
        if (!quest.isCompletableBy(corp, reputation)) {
            return false;
        }

        // Check active quest limit
        if (activeQuestIds.size() >= MAX_ACTIVE_QUESTS) {
            return false;
        }

        return quest.accept();
    }

    /**
     * Adds progress to a quest.
     *
     * @param questId the quest ID
     * @param amount progress amount
     * @return true if the quest is now completed
     */
    public boolean addProgress(String questId, int amount) {
        RpgQuest quest = allQuests.get(questId);
        if (quest == null) {
            return false;
        }
        boolean completed = quest.addProgress(amount);
        if (completed) {
            activeQuestIds.remove(questId);
            completedQuests.add(quest);
        }
        return completed;
    }

    /**
     * Completes a quest and distributes rewards.
     *
     * @param questId the quest ID
     * @param corp the player's corporation
     * @param reputationTracker the reputation tracker
     */
    public void completeQuest(String questId, TransportCorporation corp,
                               EmpireReputationTracker reputationTracker) {
        RpgQuest quest = allQuests.get(questId);
        if (quest == null || quest.getStatus() != RpgQuest.QuestStatus.COMPLETED) {
            return;
        }

        // Distribute rewards
        corp.addCredits(quest.getRewardCredits());
        corp.addXp(quest.getRewardXp());
        reputationTracker.modifyReputation(quest.getIssuingEmpireId(),
                quest.getRewardReputation(), "Quest completed: " + quest.getTitle());

        activeQuestIds.remove(questId);
        completedQuests.add(quest);
    }

    /**
     * Processes a turn for all active quests.
     *
     * @param corp the player's corporation
     * @param reputationTracker the reputation tracker
     */
    public void processTurn(TransportCorporation corp, EmpireReputationTracker reputationTracker) {
        List<String> expiredQuests = new ArrayList<>();

        for (String questId : activeQuestIds) {
            RpgQuest quest = allQuests.get(questId);
            if (quest != null) {
                if (quest.processTurn()) {
                    expiredQuests.add(questId);
                    // Penalty for expired quests
                    reputationTracker.modifyReputation(quest.getIssuingEmpireId(),
                            -5, "Quest expired: " + quest.getTitle());
                }
            }
        }

        for (String questId : expiredQuests) {
            activeQuestIds.remove(questId);
        }
    }

    /**
     * Gets all available quests for the player.
     *
     * @param corp the player's corporation
     * @param reputationTracker the reputation tracker
     * @return list of available quests
     */
    public List<RpgQuest> getAvailableQuests(TransportCorporation corp,
                                              EmpireReputationTracker reputationTracker) {
        List<RpgQuest> available = new ArrayList<>();
        for (RpgQuest quest : allQuests.values()) {
            if (quest.getStatus() == RpgQuest.QuestStatus.AVAILABLE) {
                int reputation = reputationTracker.getReputation(quest.getIssuingEmpireId());
                if (quest.isCompletableBy(corp, reputation)) {
                    available.add(quest);
                }
            }
        }
        return available;
    }

    /**
     * Gets all active quests.
     *
     * @return list of active quests
     */
    public List<RpgQuest> getActiveQuests() {
        List<RpgQuest> active = new ArrayList<>();
        for (String questId : activeQuestIds) {
            RpgQuest quest = allQuests.get(questId);
            if (quest != null) {
                active.add(quest);
            }
        }
        return active;
    }

    /**
     * Gets completed quests history.
     *
     * @return list of completed quests
     */
    public List<RpgQuest> getCompletedQuests() {
        return Collections.unmodifiableList(completedQuests);
    }

    /**
     * Gets a quest by ID.
     *
     * @param questId the quest ID
     * @return the quest or null
     */
    public RpgQuest getQuest(String questId) {
        return allQuests.get(questId);
    }

    /**
     * Gets the total number of completed quests.
     *
     * @return completed quest count
     */
    public int getCompletedQuestCount() {
        return completedQuests.size();
    }

    /**
     * Gets quests by type.
     *
     * @param type the quest type
     * @return list of quests of this type
     */
    public List<RpgQuest> getQuestsByType(RpgQuest.QuestType type) {
        List<RpgQuest> filtered = new ArrayList<>();
        for (RpgQuest quest : allQuests.values()) {
            if (quest.getType() == type) {
                filtered.add(quest);
            }
        }
        return filtered;
    }

    /**
     * Gets quests from a specific empire.
     *
     * @param empireId the empire ID
     * @return list of quests from this empire
     */
    public List<RpgQuest> getQuestsByEmpire(int empireId) {
        List<RpgQuest> filtered = new ArrayList<>();
        for (RpgQuest quest : allQuests.values()) {
            if (quest.getIssuingEmpireId() == empireId) {
                filtered.add(quest);
            }
        }
        return filtered;
    }
}
