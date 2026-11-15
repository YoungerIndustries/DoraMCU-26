package com.yisy.modules.doramcu.unified;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.hex.*;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages unified quests across all game modules.
 * <p>
 * Quests combine objectives from multiple modules into a single
 * trackable quest with rewards.
 * </p>
 */
@Service
public class UnifiedQuestService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedQuestService.class);

    private final Map<String, UnifiedQuest> quests = new HashMap<>();
    private final UnifiedEventBus eventBus;

    public UnifiedQuestService(UnifiedEventBus eventBus) {
        this.eventBus = eventBus;
        // Register for quest completion events
        eventBus.registerListener(UnifiedEvent.EventType.QUEST_COMPLETE, this::handleQuestComplete);
    }

    @PostConstruct
    public void seedInitialQuests() {
        log.info("Seeding initial unified quests...");

        // Quest 1: First Steps
        List<QuestObjective> firstStepsObjectives = new ArrayList<>();
        firstStepsObjectives.add(new QuestObjective(
                "fs-1", "Discover 3 locations on the world map", "unified",
                "discovery", 3.0, Collections.emptyMap()));
        Map<String, Object> firstStepsRewards = new HashMap<>();
        firstStepsRewards.put("xp", 200);
        firstStepsRewards.put("reputation", 25);
        createQuest("First Steps", "Explore the unified world and discover 3 locations.",
                Collections.singletonList("unified"), firstStepsObjectives, firstStepsRewards);

        // Quest 2: Module Explorer
        List<QuestObjective> moduleExplorerObjectives = new ArrayList<>();
        moduleExplorerObjectives.add(new QuestObjective(
                "me-1", "Enable 3 game modules in the unified world", "unified",
                "module_enable", 3.0, Collections.emptyMap()));
        Map<String, Object> moduleExplorerRewards = new HashMap<>();
        moduleExplorerRewards.put("xp", 300);
        moduleExplorerRewards.put("reputation", 50);
        createQuest("Module Explorer", "Enable 3 game modules to unlock their content layers.",
                Collections.singletonList("unified"), moduleExplorerObjectives, moduleExplorerRewards);

        // Quest 3: Cross-Module Adventure
        List<QuestObjective> crossModuleObjectives = new ArrayList<>();
        crossModuleObjectives.add(new QuestObjective(
                "cm-1", "Complete an action in 2 different modules", "unified",
                "cross_module_action", 2.0, Collections.emptyMap()));
        crossModuleObjectives.add(new QuestObjective(
                "cm-2", "Trigger a cross-module event", "unified",
                "cross_module_event", 1.0, Collections.emptyMap()));
        Map<String, Object> crossModuleRewards = new HashMap<>();
        crossModuleRewards.put("xp", 500);
        crossModuleRewards.put("reputation", 75);
        createQuest("Cross-Module Adventure", "Complete actions across different game modules to unlock hidden connections.",
                Arrays.asList("earth2026", "captains-log", "dnd5e", "traveller", "starwars", "babylon5", "startrek4x", "mechwarrior", "zerozeroseven"),
                crossModuleObjectives, crossModuleRewards);

        log.info("Seeded {} initial quests", quests.size());
    }

    /**
     * Creates a new quest.
     */
    public UnifiedQuest createQuest(String title, String description,
                                     List<String> sourceModules, List<QuestObjective> objectives,
                                     Map<String, Object> rewards) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        UnifiedQuest quest = new UnifiedQuest(id, title, description, sourceModules, objectives, rewards);
        quests.put(id, quest);
        log.info("Created quest: {} ({})", title, id);
        return quest;
    }

    /**
     * Returns all quests.
     */
    public List<UnifiedQuest> getAllQuests() {
        return new ArrayList<>(quests.values());
    }

    /**
     * Returns a specific quest by ID.
     */
    public Optional<UnifiedQuest> getQuest(String questId) {
        return Optional.ofNullable(quests.get(questId));
    }

    /**
     * Activates a quest.
     */
    public boolean activateQuest(String questId) {
        UnifiedQuest quest = quests.get(questId);
        if (quest != null && quest.getStatus() == UnifiedQuest.QuestStatus.AVAILABLE) {
            quest.setStatus(UnifiedQuest.QuestStatus.ACTIVE);
            log.info("Activated quest: {}", quest.getTitle());
            return true;
        }
        return false;
    }

    /**
     * Adds progress to a specific objective.
     */
    public boolean addProgress(String questId, String objectiveId, double amount) {
        UnifiedQuest quest = quests.get(questId);
        if (quest == null) return false;

        for (QuestObjective obj : quest.getObjectives()) {
            if (obj.getId().equals(objectiveId)) {
                obj.addProgress(amount);
                log.debug("Added progress to objective {}: {}/{}, quest: {}",
                        objectiveId, obj.getProgress(), obj.getTarget(), quest.getTitle());

                // Check if all objectives are completed
                if (quest.isAllObjectivesCompleted()) {
                    quest.setStatus(UnifiedQuest.QuestStatus.COMPLETED);
                    log.info("Quest completed: {}", quest.getTitle());
                    eventBus.publish(new UnifiedEvent(
                            UnifiedEvent.EventType.QUEST_COMPLETE,
                            "unified-quest",
                            "Quest Completed: " + quest.getTitle(),
                            quest.getDescription(),
                            quest.getRewards(),
                            null
                    ));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Completes an objective directly.
     */
    public boolean completeObjective(String questId, String objectiveId) {
        UnifiedQuest quest = quests.get(questId);
        if (quest == null) return false;

        for (QuestObjective obj : quest.getObjectives()) {
            if (obj.getId().equals(objectiveId)) {
                obj.complete();
                if (quest.isAllObjectivesCompleted()) {
                    quest.setStatus(UnifiedQuest.QuestStatus.COMPLETED);
                    log.info("Quest completed: {}", quest.getTitle());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a quest from an event.
     */
    public UnifiedQuest generateQuestFromEvent(UnifiedEvent event) {
        String title = "Aftermath: " + event.getTitle();
        String description = "Following the event '" + event.getTitle() + "', new opportunities have arisen.";

        List<QuestObjective> objectives = new ArrayList<>();
        objectives.add(new QuestObjective(
                "obj-1",
                "Investigate the consequences of " + event.getTitle(),
                event.getSourceModule(),
                "investigation",
                1.0,
                Collections.emptyMap()
        ));

        Map<String, Object> rewards = new HashMap<>();
        rewards.put("xp", 100);
        rewards.put("reputation", 10);

        return createQuest(title, description, Collections.singletonList(event.getSourceModule()),
                objectives, rewards);
    }

    /**
     * Returns quests by status.
     */
    public List<UnifiedQuest> getQuestsByStatus(UnifiedQuest.QuestStatus status) {
        return quests.values().stream()
                .filter(q -> q.getStatus() == status)
                .collect(Collectors.toList());
    }

    private void handleQuestComplete(UnifiedEvent event) {
        log.info("Quest complete event received: {}", event.getTitle());
    }
}
