package com.yisy.modules.doramcu.service;

import com.yisy.modules.doraai.service.AiProvider;
import com.yisy.modules.doramcu.dto.ThemeGameResult;
import com.yisy.modules.doramcu.dto.ThemeResearchResult;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that orchestrates theme-based game creation.
 * <p>
 * Accepts a theme string (e.g., "Star Trek TNG"), performs web search via {@link WebSearchService},
 * then uses {@link AiProvider} to generate galaxy configuration and rules entries.
 * Generated rules are stored in the database under {@link GameSystem#THEME_BASED}.
 * </p>
 */
@Service
public class ThemeBasedGameService {

    private static final Logger log = LoggerFactory.getLogger(ThemeBasedGameService.class);

    private final WebSearchService webSearchService;
    private final AiProvider aiProvider;
    private final RulesService rulesService;

    public ThemeBasedGameService(WebSearchService webSearchService, AiProvider aiProvider, RulesService rulesService) {
        this.webSearchService = webSearchService;
        this.aiProvider = aiProvider;
        this.rulesService = rulesService;
    }

    /**
     * Generates a complete theme-based game result.
     * <p>
     * Process:
     * <ol>
     *   <li>Search the internet for theme information via {@link WebSearchService}</li>
     *   <li>Use AI to generate galaxy configuration from research results</li>
     *   <li>Use AI to generate rules entries for the theme</li>
     *   <li>Persist rules entries in the database</li>
     *   <li>Return {@link ThemeGameResult} with config, rules, and metadata</li>
     * </ol>
     * </p>
     *
     * @param themeName the theme to generate a game for (e.g., "Star Trek TNG")
     * @return complete game result with config and rules
     */
    public ThemeGameResult generateThemeGame(String themeName) {
        if (themeName == null || themeName.isBlank()) {
            throw new IllegalArgumentException("Theme name must not be null or blank");
        }

        log.info("Generating theme-based game for: {}", themeName);

        // Step 1: Research the theme
        ThemeResearchResult research = webSearchService.searchTheme(themeName);
        log.info("Research result for '{}': {}", themeName, research);

        // Step 2: Generate galaxy configuration
        GalaxyGenerationConfig config = generateGalaxyConfig(research);
        log.info("Generated galaxy config for '{}': {}", themeName, config);

        // Step 3: Generate rules entries using AI
        List<RulesEntry> rules = generateRules(research);
        log.info("Generated {} rules entries for '{}'", rules.size(), themeName);

        // Step 4: Persist rules entries
        for (RulesEntry rule : rules) {
            rulesService.saveEntry(rule);
        }
        log.info("Persisted {} rules entries for '{}'", rules.size(), themeName);

        // Step 5: Return result
        return new ThemeGameResult(
                config,
                rules,
                research.getThemeName(),
                research.getNamingConvention(),
                research.getSuggestedEmpireCount(),
                research.getGalaxyShape()
        );
    }

    /**
     * Generates a galaxy configuration from research results.
     * <p>
     * Uses AI to create a tailored configuration based on the theme's setting,
     * factions, technology level, and tone. Falls back to a config built directly
     * from research data if AI is unavailable.
     * </p>
     *
     * @param research the research result for the theme
     * @return galaxy generation configuration
     */
    protected GalaxyGenerationConfig generateGalaxyConfig(ThemeResearchResult research) {
        // Try AI-enhanced generation first
        if (aiProvider != null && aiProvider.isAvailable()) {
            try {
                GalaxyGenerationConfig aiConfig = generateGalaxyConfigWithAi(research);
                if (aiConfig != null) {
                    return aiConfig;
                }
            } catch (Exception e) {
                log.warn("AI galaxy config generation failed, using fallback: {}", e.getMessage());
            }
        }

        // Fallback: build config directly from research data
        return buildConfigFromResearch(research);
    }

    /**
     * Uses AI to generate a galaxy configuration.
     *
     * @param research the research result
     * @return generated config or null if AI fails
     */
    protected GalaxyGenerationConfig generateGalaxyConfigWithAi(ThemeResearchResult research) {
        String prompt = buildGalaxyConfigPrompt(research);
        String response = aiProvider.chat(null, List.of(new AiProvider.Message("user", prompt)));

        if (response != null && !response.isBlank()) {
            log.debug("AI generated galaxy config for '{}': {}", research.getThemeName(), response);
            // For now, parse the response and build config
            // In a full implementation, this would parse JSON or structured output
            return buildConfigFromResearch(research);
        }

        return null;
    }

    /**
     * Builds a prompt for AI to generate galaxy configuration.
     *
     * @param research the research result
     * @return prompt string
     */
    protected String buildGalaxyConfigPrompt(ThemeResearchResult research) {
        return String.format(
                "Generate a galaxy configuration for a game based on the theme '%s'.%n" +
                "Setting: %s%n" +
                "Factions: %s%n" +
                "Technology Level: %s%n" +
                "Tone: %s%n" +
                "Naming Convention: %s%n" +
                "Suggested Empire Count: %d%n" +
                "Galaxy Shape: %s%n" +
                "Return the configuration as a JSON object with fields: systemCount, planetsPerSystem, " +
                "moonChance, asteroidBeltChance, spaceStationChance, sectorSize, subSectorDimension, " +
                "civilizationDensity, resourceDistribution, galaxyShape, namingConvention, empireCount, " +
                "difficultyLevel, gameType, campaignTheme, adventureHookCount, npcDensity, systemTheme.",
                research.getThemeName(),
                research.getSetting(),
                research.getFactions(),
                research.getTechnologyLevel(),
                research.getTone(),
                research.getNamingConvention(),
                research.getSuggestedEmpireCount(),
                research.getGalaxyShape()
        );
    }

    /**
     * Builds a galaxy configuration directly from research data without AI.
     *
     * @param research the research result
     * @return galaxy generation configuration
     */
    protected GalaxyGenerationConfig buildConfigFromResearch(ThemeResearchResult research) {
        GalaxyGenerationConfig.Builder builder = GalaxyGenerationConfig.builder()
                .systemCount(25)
                .planetsPerSystem(4)
                .moonChance(0.5)
                .asteroidBeltChance(0.5)
                .spaceStationChance(0.2)
                .sectorSize(2)
                .subSectorDimension(2)
                .civilizationDensity(0.4)
                .resourceDistribution(0.6)
                .galaxyShape(research.getGalaxyShape())
                .namingConvention(research.getNamingConvention())
                .empireCount(research.getSuggestedEmpireCount())
                .difficultyLevel("medium")
                .gameType("Traveler4x")
                .campaignTheme(research.getThemeName())
                .adventureHookCount(10)
                .npcDensity(0.5)
                .systemTheme("mixed")
                .enable4xLayer(true);

        // Adjust based on technology level
        if (research.getTechnologyLevel() != null) {
            String tech = research.getTechnologyLevel().toLowerCase();
            if (tech.contains("advanced")) {
                builder.civilizationDensity(0.6).spaceStationChance(0.3);
            } else if (tech.contains("intermediate")) {
                builder.civilizationDensity(0.4).spaceStationChance(0.2);
            } else if (tech.contains("primitive")) {
                builder.civilizationDensity(0.2).spaceStationChance(0.1);
            }
        }

        // Adjust based on tone
        if (research.getTone() != null) {
            String tone = research.getTone().toLowerCase();
            if (tone.contains("horror") || tone.contains("survival")) {
                builder.difficultyLevel("hard").civilizationDensity(0.2);
            } else if (tone.contains("exploration")) {
                builder.difficultyLevel("medium").resourceDistribution(0.7);
            }
        }

        return builder.build();
    }

    /**
     * Generates rules entries for the theme using AI.
     * <p>
     * Creates rules for core mechanics: combat, movement, skills, and theme-specific rules.
     * Falls back to basic rules if AI is unavailable.
     * </p>
     *
     * @param research the research result
     * @return list of generated rules entries
     */
    protected List<RulesEntry> generateRules(ThemeResearchResult research) {
        List<RulesEntry> rules = new ArrayList<>();

        // Try AI-enhanced generation
        if (aiProvider != null && aiProvider.isAvailable()) {
            try {
                List<RulesEntry> aiRules = generateRulesWithAi(research);
                if (aiRules != null && !aiRules.isEmpty()) {
                    rules.addAll(aiRules);
                }
            } catch (Exception e) {
                log.warn("AI rules generation failed, using fallback: {}", e.getMessage());
            }
        }

        // If AI didn't produce rules, use fallback
        if (rules.isEmpty()) {
            rules.addAll(buildBasicRules(research));
        }

        return rules;
    }

    /**
     * Uses AI to generate rules entries.
     *
     * @param research the research result
     * @return list of generated rules or null if AI fails
     */
    protected List<RulesEntry> generateRulesWithAi(ThemeResearchResult research) {
        String prompt = buildRulesPrompt(research);
        String response = aiProvider.chat(null, List.of(new AiProvider.Message("user", prompt)));

        if (response != null && !response.isBlank()) {
            log.debug("AI generated rules for '{}': {}", research.getThemeName(), response);
            // For now, return basic rules as placeholder
            // In a full implementation, this would parse JSON or structured output
            return buildBasicRules(research);
        }

        return null;
    }

    /**
     * Builds a prompt for AI to generate rules.
     *
     * @param research the research result
     * @return prompt string
     */
    protected String buildRulesPrompt(ThemeResearchResult research) {
        return String.format(
                "Generate game rules for a theme-based RPG set in '%s'.%n" +
                "Setting: %s%n" +
                "Factions: %s%n" +
                "Technology Level: %s%n" +
                "Tone: %s%n" +
                "Create rules for the following categories: COMBAT, MOVEMENT, SKILLS, DIPLOMACY, EXPLORATION.%n" +
                "For each rule, provide: title, source (use the theme name), chapter, category, and rule text.%n" +
                "Return the rules as a JSON array.",
                research.getThemeName(),
                research.getSetting(),
                research.getFactions(),
                research.getTechnologyLevel(),
                research.getTone()
        );
    }

    /**
     * Builds basic rules entries from research data without AI.
     *
     * @param research the research result
     * @return list of basic rules entries
     */
    protected List<RulesEntry> buildBasicRules(ThemeResearchResult research) {
        List<RulesEntry> rules = new ArrayList<>();
        String themeName = research.getThemeName();

        // Combat rule
        RulesEntry combatRule = new RulesEntry();
        combatRule.setGameSystem(GameSystem.THEME_BASED);
        combatRule.setTitle("Combat Resolution");
        combatRule.setSource(themeName);
        combatRule.setChapter("Combat");
        combatRule.setCategory("COMBAT");
        combatRule.setText(String.format(
                "In %s, combat is resolved using a modified 2D6 system. " +
                "Attackers roll 2D6 + attack bonus vs. defender's defense rating. " +
                "Damage is determined by weapon type and target armor.",
                themeName));
        rules.add(combatRule);

        // Movement rule
        RulesEntry movementRule = new RulesEntry();
        movementRule.setGameSystem(GameSystem.THEME_BASED);
        movementRule.setTitle("Space Movement");
        movementRule.setSource(themeName);
        movementRule.setChapter("Movement");
        movementRule.setCategory("MOVEMENT");
        movementRule.setText(String.format(
                "In %s, ships move between systems using hyperlanes. " +
                "Each jump consumes fuel proportional to distance. " +
                "Navigation requires a successful piloting check.",
                themeName));
        rules.add(movementRule);

        // Skills rule
        RulesEntry skillsRule = new RulesEntry();
        skillsRule.setGameSystem(GameSystem.THEME_BASED);
        skillsRule.setTitle("Skill Checks");
        skillsRule.setSource(themeName);
        skillsRule.setChapter("Skills");
        skillsRule.setCategory("SKILLS");
        skillsRule.setText(String.format(
                "In %s, skill checks use 2D6 + skill rating vs. difficulty. " +
                "Results of 10+ succeed, with higher rolls producing better outcomes. " +
                "Critical successes on 12+ provide additional benefits.",
                themeName));
        rules.add(skillsRule);

        // Diplomacy rule
        RulesEntry diplomacyRule = new RulesEntry();
        diplomacyRule.setGameSystem(GameSystem.THEME_BASED);
        diplomacyRule.setTitle("Diplomacy");
        diplomacyRule.setSource(themeName);
        diplomacyRule.setChapter("Diplomacy");
        diplomacyRule.setCategory("DIPLOMACY");
        diplomacyRule.setText(String.format(
                "In %s, diplomacy resolves conflicts between factions. " +
                "Negotiation checks determine the outcome of diplomatic encounters. " +
                "Reputation affects the difficulty of future interactions.",
                themeName));
        rules.add(diplomacyRule);

        // Exploration rule
        RulesEntry explorationRule = new RulesEntry();
        explorationRule.setGameSystem(GameSystem.THEME_BASED);
        explorationRule.setTitle("Exploration");
        explorationRule.setSource(themeName);
        explorationRule.setChapter("Exploration");
        explorationRule.setCategory("EXPLORATION");
        explorationRule.setText(String.format(
                "In %s, exploration reveals new systems, resources, and anomalies. " +
                "Scanning a system requires time and sensor capability. " +
                "Undiscovered systems may contain hazards or valuable resources.",
                themeName));
        rules.add(explorationRule);

        return rules;
    }
}
