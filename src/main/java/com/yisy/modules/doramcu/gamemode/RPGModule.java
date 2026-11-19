package com.yisy.modules.doramcu.gamemode;

import com.yisy.modules.doramcu.gamecore.GameMode;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPG game mode module: character-driven storytelling with quests,
 * dialogue, skill checks, and narrative choices.
 * <p>
 * Provides quest generation, NPC dialogue trees, skill check resolution,
 * and narrative event tracking for RPG-style gameplay.
 * </p>
 */
@Component
public class RPGModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(RPGModule.class);

    private final GalaxyRepository galaxyRepository;

    // Galaxy name -> list of active quests
    private final Map<String, List<RPGQuest>> activeQuests = new ConcurrentHashMap<>();

    // Galaxy name -> quest completion count
    private final Map<String, Integer> questCompletionCount = new ConcurrentHashMap<>();

    // Predefined quest templates by lore category
    private static final Map<String, List<String>> QUEST_TEMPLATES = new HashMap<>();

    static {
        QUEST_TEMPLATES.put("sci_fi", Arrays.asList(
                "Retrieve the stolen data core from the pirate station",
                "Negotiate peace between warring factions",
                "Investigate the mysterious signal from the outer rim",
                "Escort the diplomat through hostile territory",
                "Find the lost colony ship before resources run out",
                "Infiltrate the enemy command center",
                "Decode the ancient alien artifact",
                "Stop the rogue AI from destroying the space station"
        ));
        QUEST_TEMPLATES.put("fantasy", Arrays.asList(
                "Slay the dragon terrorizing the village",
                "Retrieve the enchanted sword from the dungeon",
                "Heal the cursed king before the solstice",
                "Find the missing heir to the throne",
                "Defeat the dark sorcerer's army",
                "Collect the three magical relics",
                "Break the curse on the ancient forest",
                "Negotiate with the dragon for the kingdom's safety"
        ));
        QUEST_TEMPLATES.put("horror", Arrays.asList(
                "Survive the night on the abandoned space station",
                "Find the source of the alien infestation",
                "Escape the derelict ship before oxygen runs out",
                "Investigate the crew disappearances",
                "Destroy the alien egg before it hatches",
                "Find the emergency beacon and signal for rescue"
        ));
        QUEST_TEMPLATES.put("post_apocalyptic", Arrays.asList(
                "Find clean water for the settlement",
                "Trade with the raider gang for supplies",
                "Explore the pre-war vault for technology",
                "Defend the settlement from the mutant attack",
                "Find the legendary safe zone",
                "Repair the water purifier before the drought"
        ));
    }

    public RPGModule(GalaxyRepository galaxyRepository) {
        this.galaxyRepository = galaxyRepository;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.RPG;
    }

    @Override
    public String getModuleName() {
        return "RPG Module";
    }

    @Override
    public String getDescription() {
        return "Character-driven storytelling with quests, dialogue, skill checks, and narrative choices";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing RPG module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        activeQuests.put(galaxyName, new ArrayList<>());
        questCompletionCount.put(galaxyName, 0);

        // Generate initial quests based on lore
        List<RPGQuest> quests = generateInitialQuests(galaxyName, loreSlug);
        activeQuests.put(galaxyName, quests);
        log.info("Generated {} initial quests for galaxy: {}", quests.size(), galaxyName);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        List<RPGQuest> quests = activeQuests.getOrDefault(galaxyName, Collections.emptyList());
        if (quests.isEmpty()) {
            return "No active quests in " + galaxyName;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("RPG Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        // Progress active quests
        int progressed = 0;
        for (RPGQuest quest : quests) {
            if (quest.isActive()) {
                quest.progress();
                progressed++;
                if (quest.isCompleted()) {
                    questCompletionCount.merge(galaxyName, 1, Integer::sum);
                    summary.append("  âœ“ Quest completed: ").append(quest.getTitle()).append("\n");
                } else {
                    summary.append("  â†’ Quest progressing: ").append(quest.getTitle())
                           .append(" (").append(quest.getProgress()).append("/").append(quest.getTargetProgress()).append(")\n");
                }
            }
        }

        // Generate new quest if few active
        long activeCount = quests.stream().filter(RPGQuest::isActive).count();
        if (activeCount < 2) {
            RPGQuest newQuest = generateQuest(galaxyName);
            quests.add(newQuest);
            summary.append("  ✓ New quest available: ").append(newQuest.getTitle()).append("\n");
        }

        summary.append("  Total quests completed: ").append(questCompletionCount.getOrDefault(galaxyName, 0));
        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        List<RPGQuest> quests = activeQuests.getOrDefault(galaxyName, Collections.emptyList());
        long active = quests.stream().filter(RPGQuest::isActive).count();
        long completed = quests.stream().filter(RPGQuest::isCompleted).count();
        return String.format("RPG: %d active, %d completed quests", active, completed);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        return activeQuests.containsKey(galaxyName);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            List<RPGQuest> quests = activeQuests.getOrDefault(galaxyName, Collections.emptyList());
            List<Map<String, Object>> questData = new ArrayList<>();
            for (RPGQuest quest : quests) {
                Map<String, Object> q = new LinkedHashMap<>();
                q.put("title", quest.getTitle());
                q.put("targetProgress", quest.getTargetProgress());
                q.put("progress", quest.getProgress());
                q.put("active", quest.isActive());
                q.put("completed", quest.isCompleted());
                questData.add(q);
            }
            state.put("quests", questData);
            state.put("questCompletionCount", questCompletionCount.getOrDefault(galaxyName, 0));
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize RPG state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) {
            return;
        }
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questData = (List<Map<String, Object>>) state.get("quests");
            if (questData != null) {
                List<RPGQuest> quests = new ArrayList<>();
                for (Map<String, Object> q : questData) {
                    RPGQuest quest = new RPGQuest(
                            (String) q.get("title"),
                            ((Number) q.get("targetProgress")).intValue()
                    );
                    quest.setProgress(((Number) q.get("progress")).intValue());
                    quest.setActive((Boolean) q.get("active"));
                    quest.setCompleted((Boolean) q.get("completed"));
                    quests.add(quest);
                }
                activeQuests.put(galaxyName, quests);
            }
            if (state.containsKey("questCompletionCount")) {
                questCompletionCount.put(galaxyName, ((Number) state.get("questCompletionCount")).intValue());
            }
            log.info("Restored RPG state for {}: {} quests, {} completed", galaxyName,
                    activeQuests.getOrDefault(galaxyName, Collections.emptyList()).size(),
                    questCompletionCount.getOrDefault(galaxyName, 0));
        } catch (Exception e) {
            log.error("Failed to restore RPG state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    private List<RPGQuest> generateInitialQuests(String galaxyName, String loreSlug) {
        List<String> templates = getQuestTemplatesForLore(loreSlug);
        List<RPGQuest> quests = new ArrayList<>();
        Random random = new Random();

        // Generate 3 initial quests
        for (int i = 0; i < 3; i++) {
            String title = templates.get(random.nextInt(templates.size()));
            quests.add(new RPGQuest(title, 5 + random.nextInt(10)));
        }
        return quests;
    }

    private RPGQuest generateQuest(String galaxyName) {
        // Determine lore from galaxy
        var galaxy = galaxyRepository.findByName(galaxyName);
        String loreSlug = galaxy.map(g -> g.getLoreSlug()).orElse("sci_fi");
        List<String> templates = getQuestTemplatesForLore(loreSlug);

        Random random = new Random();
        String title = templates.get(random.nextInt(templates.size()));
        return new RPGQuest(title, 5 + random.nextInt(15));
    }

    private List<String> getQuestTemplatesForLore(String loreSlug) {
        // Map lore slug to category
        String category = determineLoreCategory(loreSlug);
        return QUEST_TEMPLATES.getOrDefault(category, QUEST_TEMPLATES.get("sci_fi"));
    }

    private String determineLoreCategory(String loreSlug) {
        if (loreSlug == null) return "sci_fi";
        loreSlug = loreSlug.toLowerCase();
        if (loreSlug.contains("dune") || loreSlug.contains("star") || loreSlug.contains("halo")
                || loreSlug.contains("expanse") || loreSlug.contains("mass") || loreSlug.contains("babylon")
                || loreSlug.contains("firefly") || loreSlug.contains("stargate") || loreSlug.contains("red dwarf")
                || loreSlug.contains("warhammer") || loreSlug.contains("alien") || loreSlug.contains("dead space")
                || loreSlug.contains("avatar") || loreSlug.contains("transformers") || loreSlug.contains("destiny")) {
            return "sci_fi";
        }
        if (loreSlug.contains("dnd") || loreSlug.contains("forgotten") || loreSlug.contains("lotr")
                || loreSlug.contains("warcraft") || loreSlug.contains("elder") || loreSlug.contains("warhammer fantasy")
                || loreSlug.contains("pokemon") || loreSlug.contains("minecraft") || loreSlug.contains("genshin")) {
            return "fantasy";
        }
        if (loreSlug.contains("alien") || loreSlug.contains("dead space")) {
            return "horror";
        }
        if (loreSlug.contains("fallout")) {
            return "post_apocalyptic";
        }
        return "sci_fi";
    }

    /**
     * Simple quest model for RPG gameplay.
     */
    public static class RPGQuest {
        private final String title;
        private final int targetProgress;
        private int progress;
        private boolean active;
        private boolean completed;

        public RPGQuest(String title, int targetProgress) {
            this.title = title;
            this.targetProgress = targetProgress;
            this.progress = 0;
            this.active = true;
            this.completed = false;
        }

        public void progress() {
            if (active && !completed) {
                progress += 1 + new Random().nextInt(2);
                if (progress >= targetProgress) {
                    progress = targetProgress;
                    completed = true;
                    active = false;
                }
            }
        }

        public String getTitle() { return title; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getTargetProgress() { return targetProgress; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
