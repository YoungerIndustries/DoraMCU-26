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
 * Roguelike game mode module: procedurally generated dungeons with
 * permadeath, run-based progression, item collection, and random encounters.
 * <p>
 * Provides dungeon generation, encounter system, item drops with rarity,
 * death tracking, and run statistics for roguelike-style gameplay.
 * </p>
 */
@Component
public class RoguelikeModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(RoguelikeModule.class);

    // Galaxy name -> roguelike state
    private final Map<String, RoguelikeState> states = new ConcurrentHashMap<>();

    // Encounter types
    private static final List<String> ENCOUNTER_TYPES = Arrays.asList(
            "Combat", "Trap", "Treasure", "Puzzle", "Merchant", "Rest Area", "Boss"
    );

    // Item rarity levels
    private static final List<String> ITEM_RARITIES = Arrays.asList(
            "Common", "Uncommon", "Rare", "Epic", "Legendary"
    );

    // Item types by category
    private static final Map<String, List<String>> ITEM_TEMPLATES = new HashMap<>();

    // Dungeon room descriptions by lore category
    private static final Map<String, List<String>> ROOM_DESCRIPTIONS = new HashMap<>();

    static {
        ITEM_TEMPLATES.put("weapon", Arrays.asList(
                "Sword", "Axe", "Bow", "Staff", "Dagger", "Mace", "Wand", "Hammer"
        ));
        ITEM_TEMPLATES.put("armor", Arrays.asList(
                "Helmet", "Chestplate", "Shield", "Boots", "Gloves", "Amulet", "Ring"
        ));
        ITEM_TEMPLATES.put("consumable", Arrays.asList(
                "Health Potion", "Mana Elixir", "Antidote", "Scroll of Teleport",
                "Bomb", "Smoke Bomb", "Revival Stone"
        ));
        ITEM_TEMPLATES.put("artifact", Arrays.asList(
                "Ancient Relic", "Cursed Gem", "Dragon Scale", "Phoenix Feather",
                "Void Crystal", "Time Shard", "Soul Orb"
        ));

        ROOM_DESCRIPTIONS.put("sci_fi", Arrays.asList(
                "Abandoned space station corridor", "Alien laboratory", "Cryogenic chamber",
                "Plasma reactor room", "Holographic archive", "Wormhole gateway",
                "Droid maintenance bay", "Quantum computer core"
        ));
        ROOM_DESCRIPTIONS.put("fantasy", Arrays.asList(
                "Ancient dungeon chamber", "Enchanted forest clearing", "Dragon's lair",
                "Wizard's tower", "Underground crypt", "Elven sanctuary",
                "Goblin war camp", "Sacred temple ruins"
        ));
        ROOM_DESCRIPTIONS.put("horror", Arrays.asList(
                "Dark derelict corridor", "Infested medical bay", "Sealed containment cell",
                "Blood-stained operating room", "Shadowy ventilation shaft",
                "Abandoned observation deck", "Alien hive chamber"
        ));
        ROOM_DESCRIPTIONS.put("post_apocalyptic", Arrays.asList(
                "Ruined subway tunnel", "Overgrown bunker", "Raider stronghold",
                "Pre-war vault", "Mutant nest", "Scavenger camp",
                "Collapsed shopping mall", "Radiation zone"
        ));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.ROGUELIKE;
    }

    @Override
    public String getModuleName() {
        return "Roguelike Module";
    }

    @Override
    public String getDescription() {
        return "Procedural generation with permadeath and run-based progression";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Roguelike module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        RoguelikeState state = new RoguelikeState();
        state.active = true;
        state.loreCategory = determineLoreCategory(loreSlug);
        state.startNewRun();
        states.put(galaxyName, state);

        log.info("Roguelike module initialized for galaxy: {} â€” first run started", galaxyName);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        RoguelikeState state = states.getOrDefault(galaxyName, new RoguelikeState());

        StringBuilder summary = new StringBuilder();
        summary.append("Roguelike Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        Random random = new Random();

        // Check if run is active
        if (!state.currentRunActive) {
            state.startNewRun();
            summary.append("  ðŸ’€ New run started after death!\n");
        }

        // Advance dungeon depth
        state.currentDepth++;

        // Room description
        List<String> rooms = ROOM_DESCRIPTIONS.getOrDefault(state.loreCategory, ROOM_DESCRIPTIONS.get("sci_fi"));
        String room = rooms.get(random.nextInt(rooms.size()));
        summary.append("  ðŸšï¸ Depth ").append(state.currentDepth).append(": ").append(room).append("\n");

        // Random encounter
        String encounter = ENCOUNTER_TYPES.get(random.nextInt(ENCOUNTER_TYPES.size()));
        summary.append("  âš¡ Encounter: ").append(encounter).append("\n");

        switch (encounter) {
            case "Combat":
                processCombat(state, random, summary);
                break;
            case "Trap":
                processTrap(state, random, summary);
                break;
            case "Treasure":
                processTreasure(state, random, summary);
                break;
            case "Puzzle":
                processPuzzle(state, random, summary);
                break;
            case "Merchant":
                processMerchant(state, random, summary);
                break;
            case "Rest Area":
                processRest(state, random, summary);
                break;
            case "Boss":
                processBoss(state, random, summary);
                break;
        }

        // Check for death
        if (state.health <= 0 && random.nextDouble() < 0.15) {
            state.totalDeaths++;
            state.currentRunActive = false;

            // Update best score
            int runScore = state.currentScore;
            if (runScore > state.bestScore) {
                state.bestScore = runScore;
            }

            summary.append("\n  ðŸ’€ Run ended! Death at depth ").append(state.currentDepth)
                   .append(". Score: ").append(runScore)
                   .append(" | Best: ").append(state.bestScore);
        } else {
            // Summary stats
            summary.append("\n  ðŸ“Š Depth: ").append(state.currentDepth)
                   .append(" | Items: ").append(state.itemsCollected)
                   .append(" | Health: ").append(state.health).append("/100")
                   .append(" | Deaths: ").append(state.totalDeaths)
                   .append(" | Best: ").append(state.bestScore);
        }

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        RoguelikeState state = states.getOrDefault(galaxyName, new RoguelikeState());
        return String.format("Roguelike: depth %d, %d items, %d deaths, best %d",
                state.currentDepth, state.itemsCollected, state.totalDeaths, state.bestScore);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        return states.containsKey(galaxyName);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            RoguelikeState state = states.get(galaxyName);
            if (state == null) return null;
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Roguelike state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            RoguelikeState state = objectMapper.readValue(stateJson, RoguelikeState.class);
            states.put(galaxyName, state);
            log.info("Restored Roguelike state for {}: depth {}, {} deaths, best {}", galaxyName, state.currentDepth, state.totalDeaths, state.bestScore);
        } catch (Exception e) {
            log.error("Failed to restore Roguelike state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    private void processCombat(RoguelikeState state, Random random, StringBuilder summary) {
        int damage = random.nextInt(20) + 5;
        int enemyDamage = random.nextInt(15);
        state.health -= enemyDamage;
        state.currentScore += 10;

        boolean won = random.nextDouble() < 0.7;
        if (won) {
            summary.append("     Combat won! Dealt ").append(damage).append(" damage, took ").append(enemyDamage).append(" damage\n");
        } else {
            summary.append("     Combat lost! Took ").append(enemyDamage * 2).append(" damage\n");
            state.health -= enemyDamage;
        }
    }

    private void processTrap(RoguelikeState state, Random random, StringBuilder summary) {
        int trapDamage = random.nextInt(15);
        boolean avoided = random.nextDouble() < 0.4;
        if (avoided) {
            summary.append("     Trap avoided! Quick reflexes saved you\n");
            state.currentScore += 5;
        } else {
            state.health -= trapDamage;
            summary.append("     Trap triggered! Took ").append(trapDamage).append(" damage\n");
        }
    }

    private void processTreasure(RoguelikeState state, Random random, StringBuilder summary) {
        String item = generateItem(random);
        state.itemsCollected++;
        state.currentScore += 15;
        summary.append("     Found: ").append(item).append("\n");
    }

    private void processPuzzle(RoguelikeState state, Random random, StringBuilder summary) {
        boolean solved = random.nextDouble() < 0.6;
        if (solved) {
            state.currentScore += 20;
            summary.append("     Puzzle solved! Gained bonus points\n");
        } else {
            state.health -= 5;
            summary.append("     Puzzle failed! Lost health from confusion\n");
        }
    }

    private void processMerchant(RoguelikeState state, Random random, StringBuilder summary) {
        int healAmount = 10 + random.nextInt(20);
        state.health = Math.min(100, state.health + healAmount);
        state.itemsCollected++;
        summary.append("     Merchant: Healed ").append(healAmount).append(" health\n");
    }

    private void processRest(RoguelikeState state, Random random, StringBuilder summary) {
        int healAmount = 15 + random.nextInt(25);
        state.health = Math.min(100, state.health + healAmount);
        summary.append("     Rest area: Recovered ").append(healAmount).append(" health\n");
    }

    private void processBoss(RoguelikeState state, Random random, StringBuilder summary) {
        int bossDamage = 20 + random.nextInt(30);
        boolean won = random.nextDouble() < 0.5;
        if (won) {
            state.currentScore += 50;
            state.itemsCollected += 2;
            summary.append("     Boss defeated! Massive score bonus and loot\n");
        } else {
            state.health -= bossDamage;
            summary.append("     Boss fight lost! Took ").append(bossDamage).append(" damage\n");
        }
    }

    private String generateItem(Random random) {
        String[] categories = {"weapon", "armor", "consumable", "artifact"};
        String category = categories[random.nextInt(categories.length)];
        List<String> items = ITEM_TEMPLATES.get(category);
        String itemName = items.get(random.nextInt(items.size()));

        // Rarity weighted: Common > Uncommon > Rare > Epic > Legendary
        int rarityRoll = random.nextInt(100);
        String rarity;
        if (rarityRoll < 50) rarity = "Common";
        else if (rarityRoll < 75) rarity = "Uncommon";
        else if (rarityRoll < 90) rarity = "Rare";
        else if (rarityRoll < 97) rarity = "Epic";
        else rarity = "Legendary";

        return "[" + rarity + "] " + itemName;
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
     * Roguelike state for a galaxy.
     */
    public static class RoguelikeState {
        boolean active = false;
        int currentDepth = 0;
        int itemsCollected = 0;
        int totalDeaths = 0;
        int bestScore = 0;
        int currentScore = 0;
        boolean currentRunActive = false;
        int health = 100;
        String loreCategory = "sci_fi";

        public void startNewRun() {
            this.currentDepth = 0;
            this.itemsCollected = 0;
            this.currentScore = 0;
            this.currentRunActive = true;
            this.health = 100;
        }
    }
}
