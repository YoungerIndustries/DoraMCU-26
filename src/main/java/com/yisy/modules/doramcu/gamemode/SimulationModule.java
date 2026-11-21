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
 * Simulation game mode module: realistic systems with population dynamics,
 * economic simulation, environmental events, and social stability.
 * <p>
 * Provides population growth/decline, market simulation, environmental events,
 * social dynamics, and policy decisions for realistic simulation gameplay.
 * </p>
 */
@Component
public class SimulationModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(SimulationModule.class);

    // Galaxy name -> simulation state
    private final Map<String, SimulationState> states = new ConcurrentHashMap<>();

    // Policy types
    private static final List<String> POLICIES = Arrays.asList(
            "Tax Reform", "Education Investment", "Healthcare Expansion",
            "Military Buildup", "Trade Agreement", "Environmental Protection",
            "Infrastructure Development", "Welfare Program", "Immigration Policy",
            "Research Funding", "Housing Initiative", "Energy Transition"
    );

    // Environmental event types
    private static final List<String> ENVIRONMENTAL_EVENTS = Arrays.asList(
        "Solar storm disrupts communications",
        "Asteroid impact warning issued",
        "Climate shift affects agriculture",
        "Volcanic eruption detected",
        "Ocean current changes",
        "Magnetic field fluctuation",
        "Dust storm sweeps the region",
        "Ice shelf collapse detected"
    );

    // Economic event types
    private static final List<String> ECONOMIC_EVENTS = Arrays.asList(
        "Market boom driven by new technology",
        "Trade embargo imposed by rival faction",
        "Resource shortage drives prices up",
        "New mining operation increases supply",
        "Currency devaluation crisis",
        "Investment surge in infrastructure",
        "Black market flourishes",
        "Economic sanctions lifted"
    );

    // Social event types
    private static final List<String> SOCIAL_EVENTS = Arrays.asList(
        "Civil rights movement gains momentum",
        "Religious revival spreads across regions",
        "Youth unemployment sparks protests",
        "Cultural renaissance begins",
        "Political corruption scandal exposed",
        "Labor union strikes paralyze industry",
        "Scientific breakthrough celebrated",
        "Pandemic threat detected"
    );

    // Simulation event templates by lore category
    private static final Map<String, List<String>> EVENT_TEMPLATES = new HashMap<>();

    static {
        EVENT_TEMPLATES.put("sci_fi", Arrays.asList(
            "AI development raises ethical concerns",
            "Faster-than-light travel breakthrough",
            "Alien contact changes diplomatic landscape",
            "Cyber warfare threatens infrastructure",
            "Space colonization program expands"
        ));
        EVENT_TEMPLATES.put("fantasy", Arrays.asList(
            "Magic surge affects all living beings",
            "Dragon migration disrupts trade routes",
            "Ancient prophecy comes to light",
            "Enchanted forest expands territory",
            "Dark cult gains followers"
        ));
        EVENT_TEMPLATES.put("horror", Arrays.asList(
            "Alien parasite infection detected",
            "Unexplained disappearances increase",
            "Biohazard containment breach",
            "Psychological effects from deep space",
            "Mutant population grows"
        ));
        EVENT_TEMPLATES.put("post_apocalyptic", Arrays.asList(
            "Radiation levels fluctuate unpredictably",
            "Water scarcity worsens conflicts",
            "Pre-war technology discovered",
            "Mutant tribes establish territory",
            "Scavenger economy booms"
        ));
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.SIMULATION;
    }

    @Override
    public String getModuleName() {
        return "Simulation Module";
    }

    @Override
    public String getDescription() {
        return "Realistic physics, economics, and social systems simulation";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Simulation module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        SimulationState state = new SimulationState();
        state.active = true;
        state.population = 1000 + new Random().nextInt(9000);
        state.gdp = state.population * 100;
        state.socialStability = 70.0 + new Random().nextInt(20);
        state.environmentalHealth = 60.0 + new Random().nextInt(30);
        state.loreCategory = determineLoreCategory(loreSlug);
        states.put(galaxyName, state);

        log.info("Simulation module initialized for galaxy: {} with population: {}, GDP: {}",
                galaxyName, state.population, state.gdp);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        SimulationState state = states.getOrDefault(galaxyName, new SimulationState());

        StringBuilder summary = new StringBuilder();
        summary.append("Simulation Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        Random random = new Random();

        // Population dynamics
        double popGrowthRate = calculatePopulationGrowth(state, random);
        int popChange = (int) (state.population * popGrowthRate / 100.0);
        state.population = Math.max(100, state.population + popChange);
        state.populationChanges.add(popChange);

        summary.append("  ðŸ‘¥ Population: ").append(String.format("%,d", state.population))
               .append(" (").append(popChange >= 0 ? "+" : "").append(popChange).append(")\n");

        // Economic simulation
        double gdpGrowth = calculateGDPGrowth(state, random);
        long gdpChange = (long) (state.gdp * gdpGrowth / 100.0);
        state.gdp = Math.max(1000, state.gdp + gdpChange);
        state.gdpGrowthHistory.add(gdpGrowth);

        summary.append("  ðŸ’° GDP: ").append(String.format("%,d", state.gdp))
               .append(" (growth: ").append(String.format("%.1f", gdpGrowth)).append("%)\n");

        // Social stability changes
        double stabilityChange = calculateStabilityChange(state, random);
        state.socialStability = Math.max(0, Math.min(100, state.socialStability + stabilityChange));

        summary.append("  âš–ï¸ Stability: ").append(String.format("%.1f", state.socialStability)).append("%")
               .append(" (").append(stabilityChange >= 0 ? "+" : "").append(String.format("%.1f", stabilityChange)).append(")\n");

        // Environmental health
        double envChange = calculateEnvironmentalChange(state, random);
        state.environmentalHealth = Math.max(0, Math.min(100, state.environmentalHealth + envChange));

        summary.append("  ðŸŒ¿ Environment: ").append(String.format("%.1f", state.environmentalHealth)).append("%")
               .append(" (").append(envChange >= 0 ? "+" : "").append(String.format("%.1f", envChange)).append(")\n");

        // Policy decision
        if (random.nextDouble() < 0.4) {
            String policy = POLICIES.get(random.nextInt(POLICIES.size()));
            double policyEffect = (random.nextDouble() - 0.3) * 10.0;
            state.policiesEnacted++;

            // Policy affects different metrics
            if (policy.contains("Tax") || policy.contains("Trade") || policy.contains("Investment")) {
                state.gdp = (long) (state.gdp * (1 + policyEffect / 100.0));
            } else if (policy.contains("Healthcare") || policy.contains("Welfare") || policy.contains("Education")) {
                state.socialStability = Math.min(100, state.socialStability + Math.abs(policyEffect));
            } else if (policy.contains("Environmental")) {
                state.environmentalHealth = Math.min(100, state.environmentalHealth + Math.abs(policyEffect));
            }

            summary.append("  ðŸ“‹ Policy: ").append(policy).append(" enacted (effect: ").append(String.format("%.1f", policyEffect)).append(")\n");
        }

        // Environmental events
        if (random.nextDouble() < 0.25) {
            String event = ENVIRONMENTAL_EVENTS.get(random.nextInt(ENVIRONMENTAL_EVENTS.size()));
            state.eventsCount++;
            double impact = (random.nextDouble() - 0.5) * 10.0;
            state.environmentalHealth = Math.max(0, Math.min(100, state.environmentalHealth + impact));
            summary.append("  ðŸŒ Event: ").append(event).append(" (impact: ").append(String.format("%.1f", impact)).append(")\n");
        }

        // Economic events
        if (random.nextDouble() < 0.2) {
            String event = ECONOMIC_EVENTS.get(random.nextInt(ECONOMIC_EVENTS.size()));
            state.eventsCount++;
            double impact = (random.nextDouble() - 0.4) * 15.0;
            state.gdp = (long) (state.gdp * (1 + impact / 100.0));
            summary.append("  ðŸ“ˆ Economic: ").append(event).append(" (impact: ").append(String.format("%.1f", impact)).append("%)\n");
        }

        // Social events
        if (random.nextDouble() < 0.2) {
            String event = SOCIAL_EVENTS.get(random.nextInt(SOCIAL_EVENTS.size()));
            state.eventsCount++;
            double impact = (random.nextDouble() - 0.4) * 8.0;
            state.socialStability = Math.max(0, Math.min(100, state.socialStability + impact));
            summary.append("  ðŸ›ï¸ Social: ").append(event).append(" (impact: ").append(String.format("%.1f", impact)).append(")\n");
        }

        // Lore-specific events
        if (random.nextDouble() < 0.15) {
            List<String> events = EVENT_TEMPLATES.getOrDefault(state.loreCategory, EVENT_TEMPLATES.get("sci_fi"));
            String event = events.get(random.nextInt(events.size()));
            summary.append("  âœ¦ Lore Event: ").append(event).append("\n");
        }

        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        SimulationState state = states.getOrDefault(galaxyName, new SimulationState());
        return String.format("Simulation: pop %,d, GDP %,d, stability %.1f%%, env %.1f%%",
                state.population, state.gdp, state.socialStability, state.environmentalHealth);
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        SimulationState state = states.get(galaxyName);
        return state != null && state.active;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            SimulationState state = states.get(galaxyName);
            if (state == null) return null;
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Simulation state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            SimulationState state = objectMapper.readValue(stateJson, SimulationState.class);
            states.put(galaxyName, state);
            log.info("Restored Simulation state for {}: pop {}, GDP {}, stability {}%", galaxyName, state.population, state.gdp, state.socialStability);
        } catch (Exception e) {
            log.error("Failed to restore Simulation state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    private double calculatePopulationGrowth(SimulationState state, Random random) {
        // Base growth rate
        double growth = 1.0 + random.nextDouble() * 2.0;

        // Modified by social stability
        if (state.socialStability > 80) growth += 1.0;
        else if (state.socialStability < 30) growth -= 2.0;

        // Modified by environmental health
        if (state.environmentalHealth > 70) growth += 0.5;
        else if (state.environmentalHealth < 30) growth -= 1.5;

        // Random events
        if (random.nextDouble() < 0.1) growth -= 3.0; // Plague/disaster
        if (random.nextDouble() < 0.1) growth += 2.0; // Baby boom

        return growth;
    }

    private double calculateGDPGrowth(SimulationState state, Random random) {
        // Base growth
        double growth = 0.5 + random.nextDouble() * 3.0;

        // Population effect
        if (state.population > 5000) growth += 1.0;
        else if (state.population < 1000) growth -= 0.5;

        // Stability effect
        if (state.socialStability > 70) growth += 1.0;
        else if (state.socialStability < 40) growth -= 2.0;

        // Random economic events
        if (random.nextDouble() < 0.1) growth -= 5.0; // Recession
        if (random.nextDouble() < 0.1) growth += 4.0; // Boom

        return growth;
    }

    private double calculateStabilityChange(SimulationState state, Random random) {
        double change = (random.nextDouble() - 0.4) * 5.0;

        // GDP effect
        if (state.gdp > (long) state.population * 150) change += 1.0;
        else if (state.gdp < (long) state.population * 50) change -= 2.0;

        // Environmental effect
        if (state.environmentalHealth < 30) change -= 2.0;

        return change;
    }

    private double calculateEnvironmentalChange(SimulationState state, Random random) {
        double change = (random.nextDouble() - 0.45) * 4.0;

        // Population pressure
        if (state.population > 8000) change -= 1.0;

        // Policy effect
        if (state.policiesEnacted > 5) change += 0.5;

        return change;
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
     * Simulation state for a galaxy.
     */
    public static class SimulationState {
        boolean active = false;
        int population = 0;
        long gdp = 0;
        double socialStability = 0.0;
        double environmentalHealth = 0.0;
        int eventsCount = 0;
        int policiesEnacted = 0;
        List<Integer> populationChanges = new ArrayList<>();
        List<Double> gdpGrowthHistory = new ArrayList<>();
        String loreCategory = "sci_fi";
    }
}
