package com.yisy.modules.doramcu.shared.service;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesCategory;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Encounter generator that consults the rules database for encounter difficulty,
 * terrain modifiers, and faction-specific patterns.
 * <p>
 * Previously, {@link EncounterService} implementations generated random encounters
 * with no rules awareness. This generator uses:
 * </p>
 * <ul>
 *   <li>RulesTable entries for encounter difficulty tables (roll-based encounter selection)</li>
 *   <li>RulesEntry data for terrain modifiers and faction-specific encounter guidelines</li>
 *   <li>Scenario rules for battle-specific encounter composition</li>
 * </ul>
 * <p>
 * The generator wraps any existing EncounterService implementation and enhances
 * its encounters with rules-based difficulty scaling and terrain modifiers.
 * </p>
 */
@Service("rulesAwareEncounterGenerator")
public class RulesAwareEncounterGenerator {

    private static final Logger log = LoggerFactory.getLogger(RulesAwareEncounterGenerator.class);

    private final UnifiedRulesEntryRepository entryRepository;
    private final UnifiedRulesTableRepository tableRepository;
    private final Random random = new Random();

    public RulesAwareEncounterGenerator(
            UnifiedRulesEntryRepository entryRepository,
            UnifiedRulesTableRepository tableRepository) {
        this.entryRepository = entryRepository;
        this.tableRepository = tableRepository;
    }

    /**
     * Generate a rules-aware random encounter for a given game system and area.
     * <p>
     * The encounter difficulty scales with the character/unit level, and terrain
     * modifiers are applied based on the area type.
     * </p>
     *
     * @param gameSystem the game system (e.g., STAR_WARS, BABYLON_5, DND_5E)
     * @param areaId the area identifier (e.g., "HOTH", "YAVIN", "SECTOR_7G")
     * @param level the character/unit level for difficulty scaling
     * @return a rules-aware encounter
     */
    public Encounter generateEncounter(GameSystem gameSystem, String areaId, int level) {
        // 1. Look up encounter rules for this game system
        List<RulesEntry> encounterRules = findEncounterRules(gameSystem);

        // 2. Look up encounter tables for this game system
        List<RulesTable> encounterTables = findEncounterTables(gameSystem);

        // 3. Determine difficulty based on level and rules
        String difficulty = calculateDifficulty(level, encounterRules);

        // 4. Generate encounter type from rules or tables
        String encounterType = determineEncounterType(encounterRules, encounterTables, gameSystem);

        // 5. Apply terrain modifiers based on area
        int terrainModifier = calculateTerrainModifier(areaId, gameSystem);

        // 6. Calculate XP reward based on difficulty and level
        int xpReward = calculateXpReward(difficulty, level, terrainModifier);

        // 7. Build the encounter
        Encounter encounter = new Encounter();
        encounter.setId(null); // Will be assigned by the underlying service
        encounter.setName(generateEncounterName(encounterType, areaId, gameSystem));
        encounter.setDescription(generateEncounterDescription(encounterType, areaId, difficulty, gameSystem));
        encounter.setDifficulty(difficulty);
        encounter.setXpReward(xpReward);
        encounter.setMinLevel(Math.max(1, level - 2));
        encounter.setMaxLevel(level + 2);
        encounter.setEncounterType(encounterType);
        encounter.setAreaId(areaId);

        log.debug("Generated rules-aware encounter: {} (difficulty={}, xp={}, terrain_mod={})",
                encounter.getName(), difficulty, xpReward, terrainModifier);

        return encounter;
    }

    /**
     * Get encounter difficulty modifiers from the rules database.
     *
     * @param gameSystem the game system
     * @param areaId the area identifier
     * @return map of modifier name to value
     */
    public Map<String, Integer> getEncounterModifiers(GameSystem gameSystem, String areaId) {
        Map<String, Integer> modifiers = new HashMap<>();

        // Look up terrain rules
        List<RulesEntry> terrainRules = entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "GROUND_COMBAT");
        terrainRules.addAll(entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "COMBAT"));

        for (RulesEntry rule : terrainRules) {
            if (rule.getText() != null && rule.getText().toLowerCase().contains(areaId.toLowerCase())) {
                int bonus = extractBonusFromText(rule.getText());
                modifiers.put(rule.getTitle(), bonus);
            }
        }

        // Look up terrain modifiers from RulesCategory mapping
        RulesCategory[] terrainCategories = RulesCategory.getByGroup("COMBAT");
        for (RulesCategory cat : terrainCategories) {
            String moduleCat = cat.getModuleCategory(gameSystem);
            List<RulesEntry> entries = entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                    gameSystem, moduleCat);
            for (RulesEntry entry : entries) {
                if (entry.getText() != null && (
                        entry.getText().toLowerCase().contains("cover") ||
                        entry.getText().toLowerCase().contains("terrain") ||
                        entry.getText().toLowerCase().contains("ground"))) {
                    int bonus = extractBonusFromText(entry.getText());
                    if (bonus != 0) {
                        modifiers.put(entry.getTitle(), bonus);
                    }
                }
            }
        }

        return modifiers;
    }

    /**
     * Get scenario-based encounter composition from the rules database.
     * <p>
     * For Star Wars, this returns scenario-specific unit compositions from the
     * scenarios category. For other systems, it returns generic encounter guidelines.
     * </p>
     *
     * @param gameSystem the game system
     * @param scenarioName the scenario name (e.g., "HOTH", "YAVIN", "ENDOR")
     * @return list of rules entries describing the encounter composition
     */
    public List<RulesEntry> getScenarioEncounterRules(GameSystem gameSystem, String scenarioName) {
        List<RulesEntry> rules = new ArrayList<>();

        // Look up scenario rules
        List<RulesEntry> scenarioRules = entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "SCENARIOS");

        for (RulesEntry rule : scenarioRules) {
            if (rule.getTitle() != null && rule.getTitle().toLowerCase().contains(scenarioName.toLowerCase())) {
                rules.add(rule);
            } else if (rule.getText() != null && rule.getText().toLowerCase().contains(scenarioName.toLowerCase())) {
                rules.add(rule);
            }
        }

        // Also check by chapter
        List<RulesEntry> chapterRules = entryRepository.findByGameSystemAndChapterAndActiveTrue(
                gameSystem, scenarioName);
        rules.addAll(chapterRules);

        return rules;
    }

    /**
     * Preview what encounters would be generated for a given area without committing.
     *
     * @param gameSystem the game system
     * @param areaId the area identifier
     * @param level the character level
     * @param count number of preview encounters to generate
     * @return list of preview encounters
     */
    public List<Encounter> previewEncounters(GameSystem gameSystem, String areaId, int level, int count) {
        List<Encounter> previews = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            previews.add(generateEncounter(gameSystem, areaId, level));
        }
        return previews;
    }

    // ========================================================================
    // Internal Helpers
    // ========================================================================

    private List<RulesEntry> findEncounterRules(GameSystem gameSystem) {
        List<RulesEntry> rules = new ArrayList<>();

        // Try standard encounter category
        rules.addAll(entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "ENCOUNTERS"));

        // Try scenario category
        rules.addAll(entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "SCENARIOS"));

        // Try using RulesCategory mapping
        String encounterCat = RulesCategory.ENCOUNTERS.getModuleCategory(gameSystem);
        if (!"ENCOUNTERS".equals(encounterCat)) {
            rules.addAll(entryRepository.findByGameSystemAndCategoryAndActiveTrue(
                    gameSystem, encounterCat));
        }

        return rules;
    }

    private List<RulesTable> findEncounterTables(GameSystem gameSystem) {
        List<RulesTable> tables = new ArrayList<>();

        // Try encounter-related table categories
        tables.addAll(tableRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "ENCOUNTERS"));
        tables.addAll(tableRepository.findByGameSystemAndCategoryAndActiveTrue(
                gameSystem, "SCENARIOS"));

        return tables;
    }

    private String calculateDifficulty(int level, List<RulesEntry> encounterRules) {
        // If encounter rules provide difficulty guidelines, use them
        for (RulesEntry rule : encounterRules) {
            if (rule.getText() != null) {
                String text = rule.getText().toLowerCase();
                if (text.contains("easy") && level <= 3) return "EASY";
                if (text.contains("moderate") && level <= 6) return "MODERATE";
                if (text.contains("hard") && level <= 10) return "HARD";
                if (text.contains("deadly") && level > 10) return "DEADLY";
            }
        }

        // Base difficulty from level
        if (level <= 2) return "EASY";
        if (level <= 5) return "MODERATE";
        if (level <= 8) return "HARD";
        if (level <= 12) return "DEADLY";
        return "LEGENDARY";
    }

    private String determineEncounterType(
            List<RulesEntry> encounterRules,
            List<RulesTable> encounterTables,
            GameSystem gameSystem) {

        // If encounter tables exist, roll on them
        if (!encounterTables.isEmpty()) {
            int roll = random.nextInt(12) + 2; // 2-12 (2D6)
            for (RulesTable table : encounterTables) {
                if (table.getRollValue() == roll) {
                    return table.getResultLabel();
                }
            }
        }

        // Fallback: use encounter rules to determine type
        if (!encounterRules.isEmpty()) {
            RulesEntry rule = encounterRules.get(random.nextInt(encounterRules.size()));
            return rule.getTitle();
        }

        // Ultimate fallback: generic encounter types by game system
        return getGenericEncounterType(gameSystem);
    }

    private String getGenericEncounterType(GameSystem gameSystem) {
        String[] types;
        switch (gameSystem) {
            case STAR_WARS:
                types = new String[]{"Imperial Patrol", "Rebel Ambush", "Sand People Raid",
                        "Moff Inspection", "Smuggler Encounter", "Force Disturbance",
                        "Separatist Attack", "Jedi Mission", "Hutt Territory", "Asteroid Field"};
                break;
            case BABYLON_5:
                types = new String[]{"Minbari Patrol", "Centauri Merchant", "Narn Guerrillas",
                        "Shadow Probe", "Ripper Attack", "EarthForce Inspection",
                        "Vor Ship", "Drakons Encounter", "Wormhole Event", "Border World Trade"};
                break;
            case DND_5E:
                types = new String[]{"Wilderness Ambush", "Dungeon Trap", "Social Encounter",
                        "Puzzle Challenge", "Monster Lair", "Ruined Temple",
                        "Bandit Camp", "Dragon Sighting", "Undead Rising", "Planar Rift"};
                break;
            default:
                types = new String[]{"Combat Encounter", "Exploration Event", "Social Interaction",
                        "Trap Hazard", "Boss Battle", "Random Event"};
        }
        return types[random.nextInt(types.length)];
    }

    private int calculateTerrainModifier(String areaId, GameSystem gameSystem) {
        if (areaId == null) return 0;

        String area = areaId.toLowerCase();

        // Star Wars terrain modifiers
        if (gameSystem == GameSystem.STAR_WARS) {
            if (area.contains("hoth") || area.contains("snow")) return -1; // Cold terrain penalty
            if (area.contains("tatooine") || area.contains("desert")) return 0;
            if (area.contains("yavin") || area.contains("jungle")) return -1; // Jungle penalty
            if (area.contains("endor") || area.contains("forest")) return +1; // Cover bonus
            if (area.contains("mustafar") || area.contains("lava")) return -2; // Extreme terrain
            if (area.contains("naboo") || area.contains("swamp")) return -1;
            if (area.contains("space") || area.contains("orbit")) return 0;
        }

        // B5 terrain modifiers
        if (gameSystem == GameSystem.BABYLON_5) {
            if (area.contains("space") || area.contains("orbit")) return 0;
            if (area.contains("station") || area.contains("ship")) return +1; // Structure bonus
            if (area.contains("planet") || area.contains("surface")) return 0;
        }

        return 0;
    }

    private int calculateXpReward(String difficulty, int level, int terrainModifier) {
        int baseXp;
        switch (difficulty.toUpperCase()) {
            case "EASY": baseXp = level * 25; break;
            case "MODERATE": baseXp = level * 50; break;
            case "HARD": baseXp = level * 100; break;
            case "DEADLY": baseXp = level * 150; break;
            case "LEGENDARY": baseXp = level * 250; break;
            default: baseXp = level * 50; break;
        }

        // Apply terrain modifier (positive = harder, more XP)
        int adjustedXp = baseXp + (terrainModifier * level * 10);
        return Math.max(10, adjustedXp);
    }

    private String generateEncounterName(String type, String areaId, GameSystem gameSystem) {
        return type + " at " + areaId;
    }

    private String generateEncounterDescription(
            String type, String areaId, String difficulty, GameSystem gameSystem) {

        StringBuilder desc = new StringBuilder();
        desc.append("A ").append(difficulty.toLowerCase()).append(" encounter: ").append(type);

        if (areaId != null) {
            desc.append(" in the ").append(areaId).append(" area");
        }

        // Add rules-based context if available
        List<RulesEntry> contextRules = findEncounterRules(gameSystem);
        if (!contextRules.isEmpty()) {
            RulesEntry rule = contextRules.get(random.nextInt(contextRules.size()));
            if (rule.getText() != null && !rule.getText().isEmpty()) {
                desc.append(". ").append(rule.getText().substring(0,
                        Math.min(100, rule.getText().length())));
            }
        }

        return desc.toString();
    }

    private int extractBonusFromText(String text) {
        if (text == null) return 0;

        // Look for patterns like "+2", "-1", "bonus of 3", "penalty of 2"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[+-]?\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        int bonus = 0;
        while (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group());
                // Check context for positive/negative
                int start = Math.max(0, matcher.start() - 10);
                String context = text.substring(start, matcher.end()).toLowerCase();
                if (context.contains("penalty") || context.contains("reduce") ||
                    context.contains("harder") || context.contains("disadvantage")) {
                    bonus -= Math.abs(value);
                } else if (context.contains("bonus") || context.contains("increase") ||
                           context.contains("easier") || context.contains("advantage") ||
                           context.contains("cover")) {
                    bonus += Math.abs(value);
                } else {
                    bonus += value;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return bonus;
    }
}
