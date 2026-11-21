package com.yisy.modules.doramcu.gamemode;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sandbox game mode module: open-ended creative freedom with building,
 * crafting, terrain modification, and free-form exploration.
 * <p>
 * Provides building system, crafting recipes, terrain modification events,
 * and creativity scoring for sandbox-style gameplay with minimal constraints.
 * </p>
 */
@Component
public class SandboxModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(SandboxModule.class);

    // Galaxy name -> sandbox state
    private final Map<String, SandboxState> states = new ConcurrentHashMap<>();

    // Building types available in sandbox mode
    private static final List<String> BUILDING_TYPES = Arrays.asList(
            "Workshop", "Farm", "Tower", "Wall", "Bridge", "Dock",
            "Lab", "Garden", "Forge", "Observatory", "Barracks", "Marketplace"
    );

    // Crafting recipe categories
    private static final List<String> CRAFTING_CATEGORIES = Arrays.asList(
            "Tools", "Weapons", "Decorations", "Resources", "Vehicles", "Machinery"
    );

    // Terrain modification types
    private static final List<String> TERRAIN_MODS = Arrays.asList(
            "Excavate Quarry", "Plant Forest", "Dig Canal", "Raise Mountain",
            "Create Lake", "Clear Land", "Build Road", "Lay Rail",
            "Drill Well", "Construct Dam"
    );

    // Sandbox event templates by lore category
    private static final Map<String, List<String>> EVENT_TEMPLATES = new HashMap<>();

    static {
        EVENT_TEMPLATES.put("sci_fi", Arrays.asList(
                "Discovered rare minerals while excavating",
                "A space freighter docked at your new spaceport",
                "Automated construction drones completed a sector",
                "Solar flare disrupted power grids temporarily",
                "Alien artifact found during excavation",
                "New trade route established with neighboring system"
        ));
        EVENT_TEMPLATES.put("fantasy", Arrays.asList(
                "A magical spring emerged from the ground",
                "Mystical creatures migrated to your territory",
                "Ancient ruins discovered during construction",
                "Enchanted materials found in the quarry",
                "A wandering merchant offered rare blueprints",
                "The forest grew unusually fast overnight"
        ));
        EVENT_TEMPLATES.put("horror", Arrays.asList(
                "Strange noises from the underground excavation",
                "Unidentified organisms found in the quarry",
                "Power fluctuations in the construction zone",
                "Mysterious symbols appeared on newly built walls",
                "A section of terrain shifted unexpectedly"
        ));
        EVENT_TEMPLATES.put("post_apocalyptic", Arrays.asList(
                "Scavengers found pre-war materials in the rubble",
                "A water source discovered during excavation",
                "Raiders avoided your fortified perimeter",
                "Salvaged machinery boosted construction speed",
                "A wandering trader brought rare supplies"
        ));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.SANDBOX;
    }

    @Override
    public String getModuleName() {
        return "Sandbox Module";
    }

    @Override
    public String getDescription() {
        return "Open-ended creative freedom with building, crafting, and terrain modification";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Sandbox module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        SandboxState state = new SandboxState();
        state.active = true;
        state.buildingTypes = BUILDING_TYPES.size();
        state.craftingCategories = CRAFTING_CATEGORIES.size();
        state.terrainModTypes = TERRAIN_MODS.size();
        state.loreCategory = determineLoreCategory(loreSlug);
        states.put(galaxyName, state);

        log.info("Sandbox module initialized for galaxy: {} with {} building types and {} crafting categories",
                galaxyName, state.buildingTypes, state.craftingCategories);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        SandboxState state = states.getOrDefault(galaxyName, new SandboxState());

        StringBuilder summary = new StringBuilder();
        summary.append("Sandbox Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        Random random = new Random();

        // Random event: building, crafting, or terrain modification
        int eventRoll = random.nextInt(100);

        if (eventRoll < 45) {
            // Building event
            String building = BUILDING_TYPES.get(random.nextInt(BUILDING_TYPES.size()));
            int cost = 10 + random.nextInt(40);
            state.structuresBuilt++;
            state.resourcesSpent += cost;
            state.creativityScore += 5 + random.nextInt(10);
            summary.append("  ðŸ—ï¸ Built: ").append(building).append(" (cost: ").append(cost).append(" resources)\n");
        } else if (eventRoll < 75) {
            // Crafting event
            String category = CRAFTING_CATEGORIES.get(random.nextInt(CRAFTING_CATEGORIES.size()));
            String item = generateCraftedItem(category, random);
            state.itemsCrafted++;
            state.creativityScore += 3 + random.nextInt(7);
            summary.append("  ðŸ”¨ Crafted: ").append(item).append(" (").append(category).append(")\n");
        } else {
            // Terrain modification
            String terrainMod = TERRAIN_MODS.get(random.nextInt(TERRAIN_MODS.size()));
            state.terrainModifications++;
            state.creativityScore += 8 + random.nextInt(12);
            summary.append("  ðŸŒ Terrain: ").append(terrainMod).append("\n");
        }

        // Random sandbox event based on lore
        if (random.nextDouble() < 0.3) {
            List<String> events = EVENT_TEMPLATES.getOrDefault(state.loreCategory, EVENT_TEMPLATES.get("sci_fi"));
            String event = events.get(random.nextInt(events.size()));
            state.eventsCount++;
            summary.append("  âœ¦ Event: ").append(event).append("\n");
        }

        // Resource gathering
        int resourcesGained = 5 + random.nextInt(15);
        state.resourcesGained += resourcesGained;
        summary.append("  ðŸ“¦ Resources gathered: ").append(resourcesGained).append("\n");

        // Summary stats
        summary.append("  Total: ").append(state.structuresBuilt).append(" structures, ")
               .append(state.itemsCrafted).append(" crafted, ")
               .append(state.terrainModifications).append(" terrain mods")
               .append(" | Creativity: ").append(state.creativityScore);

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        SandboxState state = states.getOrDefault(galaxyName, new SandboxState());
        return String.format("Sandbox: %d structures, %d crafted, %d terrain mods, creativity %d",
                state.structuresBuilt, state.itemsCrafted, state.terrainModifications, state.creativityScore);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        SandboxState state = states.get(galaxyName);
        return state != null && state.active;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            SandboxState state = states.get(galaxyName);
            if (state == null) return null;
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Sandbox state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            SandboxState state = objectMapper.readValue(stateJson, SandboxState.class);
            states.put(galaxyName, state);
            log.info("Restored Sandbox state for {}: {} structures, creativity {}", galaxyName, state.structuresBuilt, state.creativityScore);
        } catch (Exception e) {
            log.error("Failed to restore Sandbox state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    private String generateCraftedItem(String category, Random random) {
        Map<String, List<String>> items = new HashMap<>();
        items.put("Tools", Arrays.asList("Hammer", "Wrench", "Drill", "Saw", "Laser Cutter", "Welder"));
        items.put("Weapons", Arrays.asList("Blaster", "Plasma Rifle", "Energy Sword", "Crossbow", "Shotgun"));
        items.put("Decorations", Arrays.asList("Statue", "Fountain", "Garden Wall", "Lantern", "Banner"));
        items.put("Resources", Arrays.asList("Steel Beam", "Crystal Shard", "Fuel Cell", "Circuit Board", "Alloy Plate"));
        items.put("Vehicles", Arrays.asList("Speeder", "Hovercraft", "AT-RT", "Dropship", "Submarine"));
        items.put("Machinery", Arrays.asList("Generator", "Fabricator", "Recycler", "Turbine", "Reactor"));

        List<String> itemList = items.getOrDefault(category, items.get("Tools"));
        return itemList.get(random.nextInt(itemList.size()));
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
     * Sandbox state for a galaxy.
     */
    public static class SandboxState {
        boolean active = false;
        int structuresBuilt = 0;
        int itemsCrafted = 0;
        int terrainModifications = 0;
        int creativityScore = 0;
        int resourcesGained = 0;
        int resourcesSpent = 0;
        int eventsCount = 0;
        int buildingTypes = 0;
        int craftingCategories = 0;
        int terrainModTypes = 0;
        String loreCategory = "sci_fi";
    }
}
