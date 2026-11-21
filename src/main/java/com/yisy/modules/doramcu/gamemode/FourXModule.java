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
 * 4X strategy game mode module: eXplore, eXpand, eXploit, eXterminate.
 * <p>
 * Provides empire management, diplomacy, research trees, fleet building,
 * and galactic conquest mechanics for 4X-style gameplay.
 * </p>
 */
@Component
public class FourXModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(FourXModule.class);

    // Galaxy name -> empire state
    private final Map<String, EmpireState> empires = new ConcurrentHashMap<>();

    // Galaxy name -> research progress
    private final Map<String, List<ResearchProject>> researchProjects = new ConcurrentHashMap<>();

    // Research tree
    private static final List<String> RESEARCH_TREE = Arrays.asList(
            "Warp Drive", "Plasma Weapons", "Shield Generators", "Astrogation",
            "Dyson Swarm", "Quantum Computing", "Bio-Engineering", "AI Cores",
            "Gravity Manipulation", "Dark Energy", "FTL Communication", "Nanotechnology",
            "Planetary Shields", "Weapon Mass Drivers", "Cloaking Devices"
    );

    @Override
    public GameMode getGameMode() {
        return GameMode.FOUR_X;
    }

    @Override
    public String getModuleName() {
        return "4X Strategy Module";
    }

    @Override
    public String getDescription() {
        return "eXplore, eXpand, eXploit, eXterminate â€” empire management and galactic conquest";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing 4X module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        empires.put(galaxyName, new EmpireState());
        researchProjects.put(galaxyName, new ArrayList<>());

        // Start initial research
        Random random = new Random();
        for (int i = 0; i < 2; i++) {
            String tech = RESEARCH_TREE.get(random.nextInt(RESEARCH_TREE.size()));
            researchProjects.get(galaxyName).add(new ResearchProject(tech, 10 + random.nextInt(10)));
        }
        log.info("Started initial research projects for galaxy: {}", galaxyName);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        EmpireState empire = empires.getOrDefault(galaxyName, new EmpireState());
        List<ResearchProject> projects = researchProjects.getOrDefault(galaxyName, Collections.emptyList());

        StringBuilder summary = new StringBuilder();
        summary.append("4X Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        // Resource income
        int mineralIncome = 100 + (empire.systemsControlled * 50);
        int energyIncome = 50 + (empire.systemsControlled * 30);
        empire.minerals += mineralIncome;
        empire.energy += energyIncome;
        summary.append("  Resources: +").append(mineralIncome).append(" minerals, +").append(energyIncome).append(" energy\n");

        // Research progress
        for (ResearchProject project : projects) {
            if (project.isActive()) {
                project.progress();
                if (project.isCompleted()) {
                    summary.append("  âœ“ Research completed: ").append(project.getName()).append("\n");
                    empire.techLevel++;
                } else {
                    summary.append("  â†’ Research: ").append(project.getName())
                           .append(" (").append(project.getProgress()).append("/").append(project.getTargetProgress()).append(")\n");
                }
            }
        }

        // Start new research if slots available
        long activeResearch = projects.stream().filter(ResearchProject::isActive).count();
        if (activeResearch < 2 && empire.techLevel > 0) {
            Random random = new Random();
            String tech = RESEARCH_TREE.get(random.nextInt(RESEARCH_TREE.size()));
            projects.add(new ResearchProject(tech, 10 + random.nextInt(15)));
            summary.append("  âœ¦ New research started: ").append(tech).append("\n");
        }

        // Expansion: gain systems over time
        if (turnNumber % 5 == 0 && empire.systemsControlled < 100) {
            empire.systemsControlled++;
            summary.append("  â˜… Explored new system! Total: ").append(empire.systemsControlled).append("\n");
        }

        summary.append("  Empire: ").append(empire.systemsControlled).append(" systems, ")
               .append(empire.techLevel).append(" tech level, ")
               .append(empire.minerals).append(" minerals, ").append(empire.energy).append(" energy");
        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        EmpireState empire = empires.getOrDefault(galaxyName, new EmpireState());
        return String.format("4X: %d systems, tech level %d, %d minerals, %d energy",
                empire.systemsControlled, empire.techLevel, empire.minerals, empire.energy);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        return empires.containsKey(galaxyName);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            EmpireState empire = empires.get(galaxyName);
            if (empire == null) return null;
            state.put("systemsControlled", empire.systemsControlled);
            state.put("techLevel", empire.techLevel);
            state.put("minerals", empire.minerals);
            state.put("energy", empire.energy);
            state.put("fleetSize", empire.fleetSize);
            state.put("diplomacyPoints", empire.diplomacyPoints);
            List<ResearchProject> projects = researchProjects.getOrDefault(galaxyName, Collections.emptyList());
            List<Map<String, Object>> projectData = new ArrayList<>();
            for (ResearchProject p : projects) {
                Map<String, Object> pd = new LinkedHashMap<>();
                pd.put("name", p.getName());
                pd.put("targetProgress", p.getTargetProgress());
                pd.put("progress", p.getProgress());
                pd.put("active", p.isActive());
                pd.put("completed", p.isCompleted());
                projectData.add(pd);
            }
            state.put("researchProjects", projectData);
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize 4X state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, Map.class);
            EmpireState empire = new EmpireState();
            empire.systemsControlled = ((Number) state.getOrDefault("systemsControlled", 1)).intValue();
            empire.techLevel = ((Number) state.getOrDefault("techLevel", 0)).intValue();
            empire.minerals = ((Number) state.getOrDefault("minerals", 500)).intValue();
            empire.energy = ((Number) state.getOrDefault("energy", 200)).intValue();
            empire.fleetSize = ((Number) state.getOrDefault("fleetSize", 0)).intValue();
            empire.diplomacyPoints = ((Number) state.getOrDefault("diplomacyPoints", 0)).intValue();
            empires.put(galaxyName, empire);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> projectData = (List<Map<String, Object>>) state.get("researchProjects");
            if (projectData != null) {
                List<ResearchProject> projects = new ArrayList<>();
                for (Map<String, Object> pd : projectData) {
                    ResearchProject p = new ResearchProject((String) pd.get("name"), ((Number) pd.get("targetProgress")).intValue());
                    p.setProgress(((Number) pd.get("progress")).intValue());
                    p.setActive((Boolean) pd.get("active"));
                    p.setCompleted((Boolean) pd.get("completed"));
                    projects.add(p);
                }
                researchProjects.put(galaxyName, projects);
            }
            log.info("Restored 4X state for {}: {} systems, tech level {}", galaxyName, empire.systemsControlled, empire.techLevel);
        } catch (Exception e) {
            log.error("Failed to restore 4X state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    /**
     * Empire state for 4X gameplay.
     */
    public static class EmpireState {
        int systemsControlled = 1;
        int techLevel = 0;
        int minerals = 500;
        int energy = 200;
        int fleetSize = 0;
        int diplomacyPoints = 0;
    }

    /**
     * Research project for 4X gameplay.
     */
    public static class ResearchProject {
        private final String name;
        private final int targetProgress;
        private int progress;
        private boolean active;
        private boolean completed;

        public ResearchProject(String name, int targetProgress) {
            this.name = name;
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

        public String getName() { return name; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getTargetProgress() { return targetProgress; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
