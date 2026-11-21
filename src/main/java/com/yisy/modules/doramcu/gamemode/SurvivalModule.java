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
 * Survival game mode module: resource scarcity, environmental hazards,
 * and base building under pressure.
 * <p>
 * Provides resource management, base building, hazard events,
 * and survival metrics for survival-style gameplay.
 * </p>
 */
@Component
public class SurvivalModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(SurvivalModule.class);

    // Galaxy name -> survival state
    private final Map<String, SurvivalState> survivals = new ConcurrentHashMap<>();

    // Galaxy name -> base structures
    private final Map<String, List<Structure>> structures = new ConcurrentHashMap<>();

    // Hazard events
    private static final List<String> HAZARDS = Arrays.asList(
            "Solar flare damages solar panels",
            "Asteroid shower hits the base",
            "Alien creatures attack the perimeter",
            "Power grid failure",
            "Water contamination",
            "Food spoilage due to temperature",
            "Radiation storm",
            "Supply drone destroyed",
            "Crew member falls ill",
            "Equipment malfunction"
    );

    // Structure types
    private static final List<String> STRUCTURES = Arrays.asList(
            "Habitat Module", "Solar Panel Array", "Water Purifier",
            "Hydroponics Bay", "Power Generator", "Med Bay",
            "Workshop", "Storage Container", "Defense Turret", "Communications Array"
    );

    @Override
    public GameMode getGameMode() {
        return GameMode.SURVIVAL;
    }

    @Override
    public String getModuleName() {
        return "Survival Module";
    }

    @Override
    public String getDescription() {
        return "Resource scarcity and base building under environmental pressure";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Survival module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        survivals.put(galaxyName, new SurvivalState());
        structures.put(galaxyName, new ArrayList<>());

        // Build initial structures
        Random random = new Random();
        structures.get(galaxyName).add(new Structure("Habitat Module", 100));
        structures.get(galaxyName).add(new Structure("Solar Panel Array", 80));
        structures.get(galaxyName).add(new Structure("Water Purifier", 90));
        log.info("Built initial structures for galaxy: {}", galaxyName);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        SurvivalState state = survivals.getOrDefault(galaxyName, new SurvivalState());
        List<Structure> structList = structures.getOrDefault(galaxyName, Collections.emptyList());

        StringBuilder summary = new StringBuilder();
        summary.append("Survival Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        // Resource consumption
        int crewSize = state.crewAlive;
        int foodConsumed = crewSize * 2;
        int waterConsumed = crewSize * 3;
        int powerConsumed = structList.size() * 5;

        state.food -= foodConsumed;
        state.water -= waterConsumed;
        state.power -= powerConsumed;

        // Resource production from structures
        int foodProduced = 0;
        int waterProduced = 0;
        int powerProduced = 0;

        for (Structure structure : structList) {
            if (structure.isOperational()) {
                switch (structure.getType()) {
                    case "Hydroponics Bay" -> foodProduced += 15 * structure.getLevel();
                    case "Water Purifier" -> waterProduced += 20 * structure.getLevel();
                    case "Solar Panel Array", "Power Generator" -> powerProduced += 25 * structure.getLevel();
                }
            }
        }

        state.food += foodProduced;
        state.water += waterProduced;
        state.power += powerProduced;

        summary.append("  Resources: Food ").append(state.food).append(", Water ").append(state.water)
               .append(", Power ").append(state.power).append("\n");

        // Check resource shortages
        if (state.food < 0) {
            state.food = 0;
            int casualties = Math.min(crewSize, 1 + new Random().nextInt(3));
            state.crewAlive -= casualties;
            state.totalDeaths += casualties;
            summary.append("  âš  Starvation! ").append(casualties).append(" crew members lost\n");
        }
        if (state.water < 0) {
            state.water = 0;
            int casualties = Math.min(crewSize, 1 + new Random().nextInt(3));
            state.crewAlive -= casualties;
            state.totalDeaths += casualties;
            summary.append("  âš  Dehydration! ").append(casualties).append(" crew members lost\n");
        }
        if (state.power < 0) {
            state.power = 0;
            // Disable random structure
            if (!structList.isEmpty()) {
                int idx = new Random().nextInt(structList.size());
                structList.get(idx).setOperational(false);
                summary.append("  âš  Power failure! ").append(structList.get(idx).getType()).append(" offline\n");
            }
        }

        // Random hazard event
        Random random = new Random();
        if (random.nextDouble() < 0.3) {
            String hazard = HAZARDS.get(random.nextInt(HAZARDS.size()));
            summary.append("  âš¡ Hazard: ").append(hazard).append("\n");

            // Apply hazard effects
            int damage = 10 + random.nextInt(30);
            if (hazard.contains("Solar") || hazard.contains("Power")) {
                state.power -= damage;
            } else if (hazard.contains("Water") || hazard.contains("contamination")) {
                state.water -= damage;
            } else if (hazard.contains("Alien") || hazard.contains("attack")) {
                if (random.nextDouble() < 0.3) {
                    int casualties = random.nextInt(2);
                    state.crewAlive -= casualties;
                    state.totalDeaths += casualties;
                    summary.append("  âœ ").append(casualties).append(" crew members lost in attack\n");
                }
            } else {
                state.food -= damage;
            }
        }

        // Build new structure if resources allow
        if (state.scrap > 100 && structList.size() < 12 && turnNumber % 3 == 0) {
            String type = STRUCTURES.get(random.nextInt(STRUCTURES.size()));
            structList.add(new Structure(type, 100));
            state.scrap -= 80;
            summary.append("  âœ¦ Built: ").append(type).append("\n");
        }

        // Scrap collection
        state.scrap += 10 + random.nextInt(20);

        // Survival days counter
        state.daysSurvived++;

        summary.append("  Crew: ").append(state.crewAlive).append(" alive, ")
               .append(state.totalDeaths).append(" lost\n")
               .append("  Structures: ").append(structList.size()).append(" (")
               .append((int) structList.stream().filter(Structure::isOperational).count()).append(" operational)\n")
               .append("  Days survived: ").append(state.daysSurvived)
               .append(", Scrap: ").append(state.scrap);

        // Check game over
        if (state.crewAlive <= 0) {
            summary.append("\n  â˜  GAME OVER â€” All crew members lost");
        }

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        SurvivalState state = survivals.getOrDefault(galaxyName, new SurvivalState());
        List<Structure> structList = structures.getOrDefault(galaxyName, Collections.emptyList());
        return String.format("Survival: Day %d, %d crew alive, %d structures, %d scrap",
                state.daysSurvived, state.crewAlive, structList.size(), state.scrap);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        return survivals.containsKey(galaxyName);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            SurvivalState s = survivals.get(galaxyName);
            if (s == null) return null;
            state.put("crewAlive", s.crewAlive);
            state.put("totalDeaths", s.totalDeaths);
            state.put("daysSurvived", s.daysSurvived);
            state.put("food", s.food);
            state.put("water", s.water);
            state.put("power", s.power);
            state.put("scrap", s.scrap);
            List<Structure> structList = structures.getOrDefault(galaxyName, Collections.emptyList());
            List<Map<String, Object>> structData = new ArrayList<>();
            for (Structure st : structList) {
                Map<String, Object> sd = new LinkedHashMap<>();
                sd.put("type", st.getType());
                sd.put("health", st.getHealth());
                sd.put("maxHealth", st.getMaxHealth());
                sd.put("level", st.getLevel());
                sd.put("operational", st.isOperational());
                structData.add(sd);
            }
            state.put("structures", structData);
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Survival state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, Map.class);
            SurvivalState s = new SurvivalState();
            s.crewAlive = ((Number) state.getOrDefault("crewAlive", 10)).intValue();
            s.totalDeaths = ((Number) state.getOrDefault("totalDeaths", 0)).intValue();
            s.daysSurvived = ((Number) state.getOrDefault("daysSurvived", 0)).intValue();
            s.food = ((Number) state.getOrDefault("food", 200)).intValue();
            s.water = ((Number) state.getOrDefault("water", 300)).intValue();
            s.power = ((Number) state.getOrDefault("power", 200)).intValue();
            s.scrap = ((Number) state.getOrDefault("scrap", 100)).intValue();
            survivals.put(galaxyName, s);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> structData = (List<Map<String, Object>>) state.get("structures");
            if (structData != null) {
                List<Structure> structList = new ArrayList<>();
                for (Map<String, Object> sd : structData) {
                    Structure st = new Structure((String) sd.get("type"), ((Number) sd.get("health")).intValue());
                    st.setMaxHealth(((Number) sd.get("maxHealth")).intValue());
                    st.setLevel(((Number) sd.get("level")).intValue());
                    st.setOperational((Boolean) sd.get("operational"));
                    structList.add(st);
                }
                structures.put(galaxyName, structList);
            }
            log.info("Restored Survival state for {}: Day {}, {} crew alive", galaxyName, s.daysSurvived, s.crewAlive);
        } catch (Exception e) {
            log.error("Failed to restore Survival state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    /**
     * Survival state for survival gameplay.
     */
    public static class SurvivalState {
        int crewAlive = 10;
        int totalDeaths = 0;
        int daysSurvived = 0;
        int food = 200;
        int water = 300;
        int power = 200;
        int scrap = 100;
    }

    /**
     * Structure for survival gameplay.
     */
    public static class Structure {
        private final String type;
        private int health;
        private int maxHealth;
        private int level;
        private boolean operational;

        public Structure(String type, int health) {
            this.type = type;
            this.health = health;
            this.maxHealth = health;
            this.level = 1;
            this.operational = true;
        }

        public String getType() { return type; }
        public int getHealth() { return health; }
        public void setHealth(int health) { this.health = health; }
        public int getMaxHealth() { return maxHealth; }
        public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public boolean isOperational() { return operational && health > 0; }
        public void setOperational(boolean operational) { this.operational = operational; }
    }
}
