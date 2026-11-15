package com.yisy.modules.doramcu.gamemode;

import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adventure game mode module: story-driven exploration with puzzle-solving,
 * narrative events, exploration milestones, and light combat encounters.
 * <p>
 * Provides puzzle system with lore-themed templates, narrative chapters,
 * exploration milestones, light combat, and treasure discovery for
 * adventure-style gameplay.
 * </p>
 */
@Component
public class AdventureModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(AdventureModule.class);

    // Galaxy name -> adventure state
    private final Map<String, AdventureState> states = new ConcurrentHashMap<>();

    // Puzzle types
    private static final List<String> PUZZLE_TYPES = Arrays.asList(
            "Logic Puzzle", "Riddle", "Cipher Decoding", "Pattern Recognition",
            "Navigation Challenge", "Timing Puzzle", "Memory Test", "Strategy Puzzle"
    );

    // Story chapter themes
    private static final List<String> CHAPTER_THEMES = Arrays.asList(
            "The Beginning", "Into the Unknown", "Allies and Enemies",
            "The Dark Secret", "Rising Threat", "The Turning Point",
            "Final Confrontation", "New Horizons"
    );

    // Treasure types
    private static final List<String> TREASURE_TYPES = Arrays.asList(
            "Ancient Artifact", "Hidden Map", "Rare Gemstone", "Lost Technology",
            "Enchanted Weapon", "Mystical Scroll", "Golden Idol", "Crystal Key",
            "Treasure Chest", "Secret Codex"
    );

    // Exploration milestones
    private static final List<String> MILESTONES = Arrays.asList(
            "Discovered hidden cave system",
            "Found ancient ruins",
            "Mapped uncharted territory",
            "Located legendary landmark",
            "Uncovered secret passage",
            "Reached the summit",
            "Crossed the forbidden zone",
            "Found the lost city"
    );

    // Combat encounter types
    private static final List<String> COMBAT_ENCOUNTERS = Arrays.asList(
            "Guardian Beast", "Bandit Ambush", "Wild Creature",
            "Rogue Mercenary", "Monster Attack", "Pirate Raid",
            "Mutant Horde", "Shadow Assassin"
    );

    // Puzzle templates by lore category
    private static final Map<String, List<String>> PUZZLE_TEMPLATES = new HashMap<>();

    // Narrative events by lore category
    private static final Map<String, List<String>> NARRATIVE_EVENTS = new HashMap<>();

    static {
        PUZZLE_TEMPLATES.put("sci_fi", Arrays.asList(
                "Decode the alien transmission signal",
                "Align the holographic star map",
                "Hack the security system firewall",
                "Synchronize the quantum entanglement array",
                "Calculate the hyperspace jump coordinates",
                "Reassemble the damaged AI core",
                "Match the DNA sequences to unlock the vault"
        ));
        PUZZLE_TEMPLATES.put("fantasy", Arrays.asList(
                "Solve the riddle of the ancient guardian",
                "Arrange the enchanted runes in the correct order",
                "Find the hidden combination in the elven inscription",
                "Complete the magical circle pattern",
                "Translate the dragon tongue message",
                "Balance the elemental forces",
                "Follow the constellation map to the treasure"
        ));
        PUZZLE_TEMPLATES.put("horror", Arrays.asList(
                "Identify the pattern in the alien symbols",
                "Sequence the emergency protocols correctly",
                "Match the biometric samples to unlock the door",
                "Decode the distress signal before time runs out",
                "Find the safe path through the infested corridor"
        ));
        PUZZLE_TEMPLATES.put("post_apocalyptic", Arrays.asList(
                "Repair the pre-war lock mechanism",
                "Decode the vault combination from clues",
                "Route power through the damaged grid",
                "Identify the safe water source from chemical tests",
                "Reconstruct the map from fragmented pieces"
        ));

        NARRATIVE_EVENTS.put("sci_fi", Arrays.asList(
                "A mysterious signal leads to a hidden space station",
                "First contact with an unknown alien species",
                "The AI companion reveals a hidden agenda",
                "A wormhole opens to an unexplored dimension",
                "Evidence of ancient space-faring civilization found"
        ));
        NARRATIVE_EVENTS.put("fantasy", Arrays.asList(
                "An ancient prophecy begins to unfold",
                "A magical portal reveals a hidden realm",
                "The legendary sword chooses its wielder",
                "A dragon offers an unexpected alliance",
                "The dark lord's influence begins to spread"
        ));
        NARRATIVE_EVENTS.put("horror", Arrays.asList(
                "Strange sounds echo through the corridors",
                "Crew members report seeing shadows",
                "The alien presence grows stronger",
                "A distress call reveals a terrifying truth",
                "The infestation spreads to new sections"
        ));
        NARRATIVE_EVENTS.put("post_apocalyptic", Arrays.asList(
                "A survivor tells of a legendary safe zone",
                "Pre-war recordings reveal the truth about the war",
                "A hidden vault contains pre-war technology",
                "Mutants show signs of intelligence",
                "A new settlement offers hope for the future"
        ));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.ADVENTURE;
    }

    @Override
    public String getModuleName() {
        return "Adventure Module";
    }

    @Override
    public String getDescription() {
        return "Story-driven exploration with puzzle-solving and light combat";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Adventure module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        AdventureState state = new AdventureState();
        state.active = true;
        state.loreCategory = determineLoreCategory(loreSlug);
        state.currentChapter = 0;
        state.explorationScore = 0;
        state.health = 100;

        // Start with the first chapter
        state.currentChapterTitle = CHAPTER_THEMES.get(0);
        states.put(galaxyName, state);

        log.info("Adventure module initialized for galaxy: {} — Chapter 1: {}",
                galaxyName, state.currentChapterTitle);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        AdventureState state = states.getOrDefault(galaxyName, new AdventureState());

        StringBuilder summary = new StringBuilder();
        summary.append("Adventure Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");
        summary.append("  📖 Chapter ").append(state.currentChapter + 1).append(": ").append(state.currentChapterTitle).append("\n");

        Random random = new Random();

        // Determine event type
        int eventRoll = random.nextInt(100);

        if (eventRoll < 35) {
            // Puzzle event
            processPuzzle(state, random, summary);
        } else if (eventRoll < 60) {
            // Exploration event
            processExploration(state, random, summary);
        } else if (eventRoll < 75) {
            // Combat encounter
            processCombat(state, random, summary);
        } else if (eventRoll < 90) {
            // Treasure discovery
            processTreasure(state, random, summary);
        } else {
            // Narrative event
            processNarrative(state, random, summary);
        }

        // Check for chapter progression
        if (state.puzzlesSolved >= 3 + state.currentChapter && state.explorationScore >= 50 * (state.currentChapter + 1)) {
            if (state.currentChapter < CHAPTER_THEMES.size() - 1) {
                state.currentChapter++;
                state.chaptersCompleted++;
                state.currentChapterTitle = CHAPTER_THEMES.get(state.currentChapter);
                summary.append("\n  📖 Chapter complete! New chapter: ").append(state.currentChapterTitle);
            } else {
                summary.append("\n  🏆 All chapters completed! Adventure story finished!");
            }
        }

        // Summary stats
        summary.append("\n  📊 Puzzles: ").append(state.puzzlesSolved)
               .append(" | Chapters: ").append(state.chaptersCompleted + 1)
               .append(" | Treasures: ").append(state.treasuresFound)
               .append(" | Exploration: ").append(state.explorationScore)
               .append(" | Health: ").append(state.health);

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        AdventureState state = states.getOrDefault(galaxyName, new AdventureState());
        return String.format("Adventure: %d puzzles, %d chapters, %d treasures, exploration %d",
                state.puzzlesSolved, state.chaptersCompleted, state.treasuresFound, state.explorationScore);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        AdventureState state = states.get(galaxyName);
        return state != null && state.active;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            AdventureState state = states.get(galaxyName);
            if (state == null) return null;
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Adventure state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            AdventureState state = objectMapper.readValue(stateJson, AdventureState.class);
            states.put(galaxyName, state);
            log.info("Restored Adventure state for {}: {} puzzles, {} chapters", galaxyName, state.puzzlesSolved, state.chaptersCompleted);
        } catch (Exception e) {
            log.error("Failed to restore Adventure state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    private void processPuzzle(AdventureState state, Random random, StringBuilder summary) {
        List<String> templates = PUZZLE_TEMPLATES.getOrDefault(state.loreCategory, PUZZLE_TEMPLATES.get("sci_fi"));
        String puzzle = templates.get(random.nextInt(templates.size()));
        String puzzleType = PUZZLE_TYPES.get(random.nextInt(PUZZLE_TYPES.size()));

        boolean solved = random.nextDouble() < 0.65;
        if (solved) {
            state.puzzlesSolved++;
            state.explorationScore += 15 + random.nextInt(10);
            summary.append("  🧩 Puzzle (").append(puzzleType).append("): ").append(puzzle).append("\n");
            summary.append("     ✓ Solved! +").append(15 + random.nextInt(10)).append(" exploration points\n");
        } else {
            summary.append("  🧩 Puzzle (").append(puzzleType).append("): ").append(puzzle).append("\n");
            summary.append("     ✗ Not solved yet — try again next turn\n");
        }
    }

    private void processExploration(AdventureState state, Random random, StringBuilder summary) {
        String milestone = MILESTONES.get(random.nextInt(MILESTONES.size()));
        int explorationGain = 10 + random.nextInt(20);
        state.explorationScore += explorationGain;
        state.milestonesReached++;

        summary.append("  🗺️ Exploration: ").append(milestone).append("\n");
        summary.append("     +").append(explorationGain).append(" exploration points\n");
    }

    private void processCombat(AdventureState state, Random random, StringBuilder summary) {
        String encounter = COMBAT_ENCOUNTERS.get(random.nextInt(COMBAT_ENCOUNTERS.size()));
        int enemyStrength = 10 + random.nextInt(30);
        int playerStrength = 15 + random.nextInt(25);

        boolean won = playerStrength >= enemyStrength || random.nextDouble() < 0.4;
        if (won) {
            int reward = 10 + random.nextInt(15);
            state.explorationScore += reward;
            state.combatsWon++;
            summary.append("  ⚔️ Combat: ").append(encounter).append("\n");
            summary.append("     Victory! +").append(reward).append(" exploration points\n");
        } else {
            int damage = 5 + random.nextInt(15);
            state.health = Math.max(0, state.health - damage);
            summary.append("  ⚔️ Combat: ").append(encounter).append("\n");
            summary.append("     Defeated! Took ").append(damage).append(" damage (health: ").append(state.health).append(")\n");
        }
    }

    private void processTreasure(AdventureState state, Random random, StringBuilder summary) {
        String treasure = TREASURE_TYPES.get(random.nextInt(TREASURE_TYPES.size()));
        state.treasuresFound++;
        int explorationGain = 20 + random.nextInt(20);
        state.explorationScore += explorationGain;

        summary.append("  💎 Treasure: ").append(treasure).append("\n");
        summary.append("     +").append(explorationGain).append(" exploration points\n");
    }

    private void processNarrative(AdventureState state, Random random, StringBuilder summary) {
        List<String> events = NARRATIVE_EVENTS.getOrDefault(state.loreCategory, NARRATIVE_EVENTS.get("sci_fi"));
        String event = events.get(random.nextInt(events.size()));
        state.narrativeEvents++;
        state.explorationScore += 5;

        summary.append("  📜 Narrative: ").append(event).append("\n");
    }

    private String determineLoreCategory(String loreSlug) {
        if (loreSlug == null) return "sci_fi";
        loreSlug = loreSlug.toLowerCase();
        if (loreSlug.contains("dune") || loreSlug.contains("star") || loreSlug.contains("halo")
                || loreSlug.contains("expanse") || loreSlug.contains("mass") || loreSlug.contains("babylon")
                || loreSlug.contains("firefly") || loreSlug.contains("stargate") || loreSlug.contains("red dwarf")
                || loreSlug.contains("warhammer") || loreSlug.contains("alien") || loreSlug.contains("dead space")
                || loreSlug.contains("avatar") || loreSlug.contains("transformers") || loreSlug.contains("destiny")
                || loreSlug.contains("earth")) {
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
     * Adventure state for a galaxy.
     */
    public static class AdventureState {
        boolean active = false;
        int puzzlesSolved = 0;
        int chaptersCompleted = 0;
        int currentChapter = 0;
        String currentChapterTitle = "";
        int explorationScore = 0;
        int treasuresFound = 0;
        int milestonesReached = 0;
        int combatsWon = 0;
        int narrativeEvents = 0;
        int health = 100;
        String loreCategory = "sci_fi";
    }
}
