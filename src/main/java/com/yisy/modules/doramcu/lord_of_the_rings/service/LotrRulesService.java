package com.yisy.modules.doramcu.lord_of_the_rings.service;
import com.yisy.modules.doramcu.model.players.Player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for LOTR rules reference and summary.
 * <p>
 * Provides access to the core rules of the Lord of the Rings RPG, including
 * combat mechanics, skill checks, corruption, hope/shadow tracks, and
 * calling abilities. Based on The One Ring RPG, LOTR CODA RPG, and MERP.
 * </p>
 * <p>
 * Key mechanics:
 * - 2d6 narrative task resolution
 * - Hope/Shadow dual tracks
 * - Corruption track (0-10)
 * - Eye of Sauron die (d12)
 * - Calling-based abilities
 * - Race-based traits
 * </p>
 */
@Service
public class LotrRulesService {

    private static final Logger log = LoggerFactory.getLogger(LotrRulesService.class);

    // Core rules summary
    private static final Map<String, String> CORE_RULES;
    static {
        Map<String, String> rules = new HashMap<>();
        rules.put("Task Resolution",
                "Most tasks use 2d6 + modifier vs a difficulty number (DC).\n" +
                "- DC 6: Easy task\n" +
                "- DC 8: Routine task\n" +
                "- DC 10: Challenging task\n" +
                "- DC 12: Difficult task\n" +
                "- DC 14: Very difficult task\n" +
                "- DC 16: Nearly impossible task\n" +
                "On a tie, the task is partially successful with a complication.");

        rules.put("Hope and Shadow",
                "The Hope and Shadow tracks represent the fate of Middle-earth.\n" +
                "- Hope: When characters succeed against great odds, the GM may add a Hope point.\n" +
                "  Players can spend Hope to reroll one die or gain a +1 bonus.\n" +
                "- Shadow: When things go terribly wrong, the GM may add a Shadow point.\n" +
                "  Shadow makes enemies stronger and the world more dangerous.\n" +
                "- The Eye of Sauron (d12) is rolled periodically. High rolls mean Sauron is alert.");

        rules.put("Corruption",
                "The Ring and dark magic corrupt the bearer.\n" +
                "- Corruption 0-3: Minor dark thoughts, easily resisted.\n" +
                "- Corruption 4-6: Dark thoughts linger; disadvantage on Persuasion.\n" +
                "- Corruption 7-9: The Ring's power grows; disadvantage on all social checks.\n" +
                "- Corruption 10: Fully corrupted; the Ring controls the bearer.\n" +
                "Using the Ring or dark magic increases corruption.");

        rules.put("Combat",
                "Combat uses d20 attack rolls vs Armor Class (AC).\n" +
                "- Attack Roll: d20 + attack bonus vs target's AC\n" +
                "- Damage: Weapon die + ability modifier\n" +
                "- Saving Throws: d20 + modifier vs DC\n" +
                "- Critical Hit: Natural 20 deals double damage\n" +
                "- Critical Miss: Natural 1 is a fumble\n" +
                "Hope can grant advantage; Shadow can impose disadvantage.");

        rules.put("Callings",
                "Callings define a character's role:\n" +
                "- Scout (d6 HP, Dex): Swift and stealthy explorer\n" +
                "- Warrior (d10 HP, Str): Trained combatant\n" +
                "- Ranger (d8 HP, Dex): Tracker and hunter\n" +
                "- Bard (d8 HP, Cha): Inspiring performer\n" +
                "- Mage (d6 HP, Int): Arcane spellcaster\n" +
                "- Healer (d8 HP, Wis): Divine healer\n" +
                "Each calling has unique powers and proficiencies.");

        rules.put("Races",
                "Race determines ability score bonuses and traits:\n" +
                "- Elf: +2 Dex, +1 Cha, +1 Int; darkvision, elven sight\n" +
                "- Man: +2 Str or +2 Wis; no special traits\n" +
                "- Dwarf: +2 Con, +1 Str; darkvision, stonecunning\n" +
                "- Hobbit: +2 Dex, +1 Wis, +1 Cha; lucky, nimble\n" +
                "- Ent: +4 Str, +4 Con; slow but incredibly durable\n" +
                "- Maia: +4 Int, +2 Wis; innate magical power");

        rules.put("Resting",
                "Recovery between adventures:\n" +
                "- Short Rest (1 hour): Recover hit dice, some spell slots\n" +
                "- Long Rest (8 hours): Full HP recovery, all spell slots\n" +
                "- During long rests, corruption may decrease by 1 if the Ring is not used");

        rules.put("The Eye of Sauron",
                "The Eye die (d12) represents Sauron's awareness.\n" +
                "- Roll the Eye at key moments: entering Mordor, using the Ring, etc.\n" +
                "- Eye 1-3: Sauron is distracted; advantage on stealth\n" +
                "- Eye 4-6: Sauron is neutral; no effect\n" +
                "- Eye 7-9: Sauron is alert; disadvantage on stealth\n" +
                "- Eye 10-12: Sauron is watching! Enemies appear or reinforce");

        rules.put("Leveling",
                "Characters advance through XP thresholds:\n" +
                "- Level 1: 0 XP\n" +
                "- Level 2: 300 XP\n" +
                "- Level 3: 900 XP\n" +
                "- Level 4: 2,700 XP\n" +
                "- Level 5: 6,500 XP\n" +
                "- Level 6: 14,000 XP\n" +
                "- Level 7: 23,000 XP\n" +
                "- Level 8: 34,000 XP\n" +
                "- Level 9: 48,000 XP\n" +
                "- Level 10: 64,000 XP\n" +
                "At each level, gain HP, abilities, and calling powers.");

        rules.put("Magic Items",
                "Magical items have charges and attunement:\n" +
                "- Common: No attunement, unlimited charges\n" +
                "- Uncommon: May require attunement\n" +
                "- Rare: Requires attunement\n" +
                "- Legendary: Requires attunement, unique effects\n" +
                "The One Ring is a special item that corrupts the bearer.");

        CORE_RULES = Map.copyOf(rules);
    }

    /**
     * Get all core rules.
     */
    public Map<String, String> getAllRules() {
        return new HashMap<>(CORE_RULES);
    }

    /**
     * Get a specific rule by name.
     */
    public String getRule(String ruleName) {
        return CORE_RULES.getOrDefault(ruleName, "Rule not found: " + ruleName);
    }

    /**
     * Get a summary of all rules.
     */
    public String getRulesSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Lord of the Rings RPG Rules Summary ===\n\n");
        for (Map.Entry<String, String> entry : CORE_RULES.entrySet()) {
            sb.append(entry.getKey()).append(":\n");
            String value = entry.getValue();
            // Add first line only for summary
            int newlineIdx = value.indexOf('\n');
            if (newlineIdx > 0) {
                sb.append(value, 0, newlineIdx);
            } else {
                sb.append(value);
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Get the XP thresholds for leveling.
     */
    public Map<Integer, Integer> getXpThresholds() {
        Map<Integer, Integer> thresholds = new HashMap<>();
        int[] xp = {0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000};
        for (int i = 0; i < xp.length; i++) {
            thresholds.put(i + 1, xp[i]);
        }
        return thresholds;
    }

    /**
     * Get the DC table for task resolution.
     */
    public Map<Integer, String> getDcTable() {
        Map<Integer, String> dcTable = new HashMap<>();
        dcTable.put(6, "Easy");
        dcTable.put(8, "Routine");
        dcTable.put(10, "Challenging");
        dcTable.put(12, "Difficult");
        dcTable.put(14, "Very Difficult");
        dcTable.put(16, "Nearly Impossible");
        return dcTable;
    }

    /**
     * Get the corruption levels and their effects.
     */
    public Map<String, String> getCorruptionLevels() {
        Map<String, String> levels = new HashMap<>();
        levels.put("0-3", "Minor dark thoughts, easily resisted.");
        levels.put("4-6", "Dark thoughts linger; disadvantage on Persuasion.");
        levels.put("7-9", "The Ring's power grows; disadvantage on all social checks.");
        levels.put("10", "Fully corrupted; the Ring controls the bearer.");
        return levels;
    }

    /**
     * Get the Eye of Sauron table.
     */
    public Map<String, String> getEyeOfSauronTable() {
        Map<String, String> table = new HashMap<>();
        table.put("1-3", "Sauron is distracted; advantage on stealth");
        table.put("4-6", "Sauron is neutral; no effect");
        table.put("7-9", "Sauron is alert; disadvantage on stealth");
        table.put("10-12", "Sauron is watching! Enemies appear or reinforce");
        return table;
    }

    /**
     * Get the calling hit dice table.
     */
    public Map<String, String> getCallingHitDice() {
        Map<String, String> table = new HashMap<>();
        table.put("Scout", "d6");
        table.put("Warrior", "d10");
        table.put("Ranger", "d8");
        table.put("Bard", "d8");
        table.put("Mage", "d6");
        table.put("Healer", "d8");
        return table;
    }

    /**
     * Get the race ability score bonuses.
     */
    public Map<String, Map<String, Integer>> getRaceBonuses() {
        Map<String, Map<String, Integer>> bonuses = new HashMap<>();

        Map<String, Integer> elf = new HashMap<>();
        elf.put("Dex", 2);
        elf.put("Cha", 1);
        elf.put("Int", 1);
        bonuses.put("Elf", elf);

        Map<String, Integer> man = new HashMap<>();
        man.put("Str", 2);
        bonuses.put("Man", man);

        Map<String, Integer> dwarf = new HashMap<>();
        dwarf.put("Con", 2);
        dwarf.put("Str", 1);
        bonuses.put("Dwarf", dwarf);

        Map<String, Integer> hobbit = new HashMap<>();
        hobbit.put("Dex", 2);
        hobbit.put("Wis", 1);
        hobbit.put("Cha", 1);
        bonuses.put("Hobbit", hobbit);

        Map<String, Integer> ent = new HashMap<>();
        ent.put("Str", 4);
        ent.put("Con", 4);
        bonuses.put("Ent", ent);

        Map<String, Integer> maia = new HashMap<>();
        maia.put("Int", 4);
        maia.put("Wis", 2);
        bonuses.put("Maia", maia);

        return bonuses;
    }
}
