package com.yisy.modules.doramcu.thirdreich.commander;

import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.thirdreich.service.ThirdReichRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing AI commanders in the 3R4 ground combat engine.
 * <p>
 * Provides methods to create commanders from historical profiles, generate
 * commanders from game characters, and retrieve existing commanders.
 * </p>
 * <p>
 * This service is rules-aware: it consults the {@link ThirdReichRulesEngine}
 * for tactical decisions based on commander traits, terrain, and battle state.
 * </p>
 */
@Service
public class AICommanderService {

    private static final Logger log = LoggerFactory.getLogger(AICommanderService.class);

    private final Map<String, AICommander> commanders = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final ThirdReichRulesEngine rulesEngine;

    /**
     * Creates the service with an optional rules engine.
     *
     * @param rulesEngine the Third Reich rules engine, or null for standalone mode
     */
    public AICommanderService(ThirdReichRulesEngine rulesEngine) {
        this.rulesEngine = rulesEngine; // may be null in standalone mode
    }

    /**
     * Creates a standalone service without rules engine integration.
     * @deprecated Use {@link #AICommanderService(ThirdReichRulesEngine)} instead.
     */
    @Deprecated
    public AICommanderService() {
        this.rulesEngine = null;
    }

    /**
     * Tactical advice returned by {@link #getTacticalAdvice(AICommander, Map)}.
     */
    public record TacticalAdvice(
            int attackModifier,
            int defenseModifier,
            int moraleModifier,
            AICommander.TacticalPreference recommendedPreference,
            List<String> reasoning
    ) {
    }

    /**
     * Creates a commander from a historical profile.
     *
     * @param profile the historical profile to use
     * @return the created AICommander
     */
    public AICommander createFromProfile(AICommanderProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile must not be null");
        }
        AICommander commander = profile.createCommander();
        commanders.put(commander.getId(), commander);
        return commander;
    }

    /**
     * Creates a commander from a game character by mapping stats to leadership and traits.
     * <p>
     * This method accepts generic character stats and derives appropriate commander
     * properties. The expected stats map should contain:
     * <ul>
     *   <li>"name" - Character name (String)</li>
     *   <li>"leadership" - Leadership rating 1-10 (Integer, optional)</li>
     *   <li>"aggression" - Aggression level 1-10 (Integer, optional)</li>
     *   <li>"tactics" - Tactical skill 1-10 (Integer, optional)</li>
     *   <li>"morale" - Morale influence 1-10 (Integer, optional)</li>
     * </ul>
     *
     * @param stats the character stats map
     * @param sourceGame the source game system name
     * @return the generated AICommander
     */
    public AICommander generateFromCharacter(Map<String, Object> stats, String sourceGame) {
        if (stats == null || stats.isEmpty()) {
            throw new IllegalArgumentException("Character stats must not be null or empty");
        }

        String name = (String) stats.getOrDefault("name", "Unknown Commander");
        String id = "cmd-" + sourceGame + "-" + idCounter.getAndIncrement();

        AICommander commander = new AICommander(id, name);
        commander.setSourceGame(sourceGame);

        // Map leadership stat (default 5)
        int leadership = 5;
        if (stats.containsKey("leadership")) {
            leadership = ((Number) stats.get("leadership")).intValue();
        }
        commander.setLeadership(leadership);

        // Determine tactical preference based on aggression
        int aggression = 5;
        if (stats.containsKey("aggression")) {
            aggression = ((Number) stats.get("aggression")).intValue();
        }
        if (aggression >= 7) {
            commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
        } else if (aggression <= 3) {
            commander.setTacticalPreference(AICommander.TacticalPreference.DEFENSIVE);
        } else {
            commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
        }

        // Assign traits based on stats
        int tactics = 5;
        if (stats.containsKey("tactics")) {
            tactics = ((Number) stats.get("tactics")).intValue();
        }
        int morale = 5;
        if (stats.containsKey("morale")) {
            morale = ((Number) stats.get("morale")).intValue();
        }

        // Assign up to 3 traits based on highest stats
        if (aggression >= 7) {
            commander.addTrait(CommanderTrait.AGGRESSIVE);
        }
        if (tactics >= 7) {
            commander.addTrait(CommanderTrait.FLANKER);
        }
        if (morale >= 7) {
            commander.addTrait(CommanderTrait.INSPIRATIONAL);
        }
        if (leadership >= 8) {
            commander.addTrait(CommanderTrait.LOGISTICIAN);
        }

        // If no traits assigned, give a random one
        if (commander.getTraits().isEmpty()) {
            CommanderTrait[] allTraits = CommanderTrait.values();
            commander.addTrait(allTraits[random.nextInt(allTraits.length)]);
        }

        commanders.put(id, commander);
        return commander;
    }

    /**
     * Creates a random commander with generated traits.
     *
     * @param name the commander's name
     * @return a randomly generated AICommander
     */
    public AICommander createRandomCommander(String name) {
        String id = "cmd-random-" + idCounter.getAndIncrement();
        AICommander commander = new AICommander(id, name);
        commander.setLeadership(3 + random.nextInt(8));
        commander.setTacticalPreference(
                AICommander.TacticalPreference.values()[random.nextInt(3)]);

        // Assign 1-3 random traits
        CommanderTrait[] allTraits = CommanderTrait.values();
        int traitCount = 1 + random.nextInt(3);
        List<CommanderTrait> available = new ArrayList<>(List.of(allTraits));
        for (int i = 0; i < traitCount && !available.isEmpty(); i++) {
            int idx = random.nextInt(available.size());
            commander.addTrait(available.remove(idx));
        }

        commanders.put(id, commander);
        return commander;
    }

    /**
     * Retrieves a commander by ID.
     *
     * @param id the commander ID
     * @return the commander, or null if not found
     */
    public AICommander getCommander(String id) {
        return commanders.get(id);
    }

    /**
     * Returns all registered commanders.
     *
     * @return list of all commanders
     */
    public List<AICommander> getAllCommanders() {
        return new ArrayList<>(commanders.values());
    }

    /**
     * Returns all available historical profiles.
     *
     * @return list of all historical profiles
     */
    public List<AICommanderProfile> getAllProfiles() {
        return AICommanderProfile.getAllProfiles();
    }

    // ==================== Rules-Aware Tactical Advice ====================

    /**
     * Consults the rules engine for tactical advice based on the commander's
     * traits, the current battle state, and terrain conditions.
     * <p>
     * The battle state map may contain:
     * <ul>
     *   <li>"terrain" — terrain type (e.g., "forest", "urban", "hill")</li>
     *   <li>"attacking" — boolean, whether the commander is currently attacking</li>
     *   <li>"unitMorale" — current average unit morale (1-10)</li>
     *   <li>"casualties" — percentage of casualties (0-100)</li>
     *   <li>"enemyStrength" — relative enemy strength (0-10, 5 = equal)</li>
     * </ul>
     *
     * @param commander the commander to advise
     * @param battleState the current battle state, or null for defaults
     * @return tactical advice with modifiers and recommended preference
     */
    public TacticalAdvice getTacticalAdvice(AICommander commander, Map<String, Object> battleState) {
        if (commander == null) {
            throw new IllegalArgumentException("Commander must not be null");
        }

        int attackModifier = 0;
        int defenseModifier = 0;
        int moraleModifier = 0;
        List<String> reasoning = new ArrayList<>();

        // Default battle state
        String terrain = "open_ground";
        boolean attacking = true;
        int unitMorale = 5;
        int casualties = 0;
        int enemyStrength = 5;

        if (battleState != null) {
            terrain = (String) battleState.getOrDefault("terrain", "open_ground");
            attacking = (boolean) battleState.getOrDefault("attacking", true);
            unitMorale = ((Number) battleState.getOrDefault("unitMorale", 5)).intValue();
            casualties = ((Number) battleState.getOrDefault("casualties", 0)).intValue();
            enemyStrength = ((Number) battleState.getOrDefault("enemyStrength", 5)).intValue();
        }

        // 1. Terrain modifiers from rules database
        if (rulesEngine != null) {
            TerrainModifiers terrainMods = getTerrainModifiersFromRules(terrain);
            attackModifier += terrainMods.attackModifier;
            defenseModifier += terrainMods.defenseModifier;
            if (terrainMods.attackModifier != 0 || terrainMods.defenseModifier != 0) {
                reasoning.add("Terrain (" + terrain + "): attack " + signed(terrainMods.attackModifier) + ", defense " + signed(terrainMods.defenseModifier));
            }
        } else {
            // Fallback: hardcoded terrain modifiers
            TerrainModsFallback fallback = getFallbackTerrainModifiers(terrain);
            attackModifier += fallback.attackModifier;
            defenseModifier += fallback.defenseModifier;
            if (fallback.attackModifier != 0 || fallback.defenseModifier != 0) {
                reasoning.add("Terrain (" + terrain + "): attack " + signed(fallback.attackModifier) + ", defense " + signed(fallback.defenseModifier));
            }
        }

        // 2. Commander trait bonuses from rules database
        if (rulesEngine != null && !commander.getTraits().isEmpty()) {
            for (CommanderTrait trait : commander.getTraits()) {
                TraitModifiers traitMods = getTraitModifiersFromRules(trait);
                attackModifier += traitMods.attackModifier;
                defenseModifier += traitMods.defenseModifier;
                moraleModifier += traitMods.moraleModifier;
                if (traitMods.attackModifier != 0 || traitMods.defenseModifier != 0 || traitMods.moraleModifier != 0) {
                    reasoning.add("Trait (" + trait.name() + "): attack " + signed(traitMods.attackModifier) + ", defense " + signed(traitMods.defenseModifier) + ", morale " + signed(traitMods.moraleModifier));
                }
            }
        }

        // 3. Leadership modifier
        int leadershipMod = commander.getLeadershipModifier();
        if (attacking) {
            attackModifier += leadershipMod;
        } else {
            defenseModifier += leadershipMod;
        }
        moraleModifier += leadershipMod;
        if (leadershipMod != 0) {
            reasoning.add("Leadership (" + commander.getLeadership() + "): " + signed(leadershipMod) + " to " + (attacking ? "attack" : "defense") + " and morale");
        }

        // 4. Morale assessment based on battle state
        if (casualties > 50) {
            moraleModifier -= 2;
            reasoning.add("High casualties (" + casualties + "%): morale -2");
        } else if (casualties > 25) {
            moraleModifier -= 1;
            reasoning.add("Moderate casualties (" + casualties + "%): morale -1");
        }
        if (unitMorale <= 2) {
            moraleModifier -= 1;
            reasoning.add("Low unit morale (" + unitMorale + "): morale -1");
        } else if (unitMorale >= 8) {
            moraleModifier += 1;
            reasoning.add("High unit morale (" + unitMorale + "): morale +1");
        }

        // 5. Recommended tactical preference based on situation
        AICommander.TacticalPreference recommended;
        if (enemyStrength >= 7 && casualties > 25) {
            recommended = AICommander.TacticalPreference.DEFENSIVE;
            reasoning.add("Recommendation: Defensive — enemy is strong and casualties are high");
        } else if (enemyStrength <= 3) {
            recommended = AICommander.TacticalPreference.AGGRESSIVE;
            reasoning.add("Recommendation: Aggressive — enemy is weak");
        } else if (commander.getLeadership() >= 8) {
            recommended = AICommander.TacticalPreference.AGGRESSIVE;
            reasoning.add("Recommendation: Aggressive — high leadership favors offense");
        } else if (commander.getLeadership() <= 4) {
            recommended = AICommander.TacticalPreference.DEFENSIVE;
            reasoning.add("Recommendation: Defensive — low leadership favors holding positions");
        } else {
            recommended = commander.getTacticalPreference();
            reasoning.add("Recommendation: " + recommended.name() + " — current preference is appropriate");
        }

        log.debug("Tactical advice for {}: attack {}, defense {}, morale {}, preference {} — {}",
                commander.getName(), signed(attackModifier), signed(defenseModifier), signed(moraleModifier),
                recommended.name(), reasoning);

        return new TacticalAdvice(attackModifier, defenseModifier, moraleModifier, recommended, reasoning);
    }

    // ==================== Rules Lookup Helpers ====================

    private static final Pattern MODIFIER_PATTERN = Pattern.compile("([+-]\\d+)\\s+(?:to\\s+)?(attack|defense|morale)");

    private TerrainModifiers getTerrainModifiersFromRules(String terrain) {
        List<RulesEntry> entries = rulesEngine.queryRules("TERRAIN");
        for (RulesEntry entry : entries) {
            String title = entry.getTitle().toLowerCase();
            if (title.contains(terrain) || title.replace(" ", "_").contains(terrain.replace(" ", "_"))) {
                return parseModifiersFromEntry(entry);
            }
        }
        return new TerrainModifiers(0, 0);
    }

    private TerrainModsFallback getFallbackTerrainModifiers(String terrain) {
        return switch (terrain.toLowerCase()) {
            case "forest" -> new TerrainModsFallback(-1, 2);
            case "urban" -> new TerrainModsFallback(-2, 4);
            case "hill" -> new TerrainModsFallback(1, 1);
            case "river" -> new TerrainModsFallback(-2, -2);
            case "swamp" -> new TerrainModsFallback(-2, 1);
            default -> new TerrainModsFallback(0, 0);
        };
    }

    private TraitModifiers getTraitModifiersFromRules(CommanderTrait trait) {
        List<RulesEntry> entries = rulesEngine.queryRules("COMMANDER_TRAITS");
        for (RulesEntry entry : entries) {
            String title = entry.getTitle().toLowerCase();
            if (title.contains(trait.name().toLowerCase())) {
                return parseTraitModifiersFromEntry(entry);
            }
        }
        return new TraitModifiers(0, 0, 0);
    }

    private TerrainModifiers parseModifiersFromEntry(RulesEntry entry) {
        int attackMod = 0;
        int defenseMod = 0;
        String text = (entry.getText() != null) ? entry.getText() + " " + entry.getNotes() : "";
        Matcher matcher = MODIFIER_PATTERN.matcher(text);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String type = matcher.group(2).toLowerCase();
            if (type.equals("attack")) {
                attackMod += value;
            } else if (type.equals("defense")) {
                defenseMod += value;
            }
        }
        return new TerrainModifiers(attackMod, defenseMod);
    }

    private TraitModifiers parseTraitModifiersFromEntry(RulesEntry entry) {
        int attackMod = 0;
        int defenseMod = 0;
        int moraleMod = 0;
        String text = (entry.getText() != null) ? entry.getText() + " " + entry.getNotes() : "";
        Matcher matcher = MODIFIER_PATTERN.matcher(text);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String type = matcher.group(2).toLowerCase();
            if (type.equals("attack")) {
                attackMod += value;
            } else if (type.equals("defense")) {
                defenseMod += value;
            } else if (type.equals("morale")) {
                moraleMod += value;
            }
        }
        return new TraitModifiers(attackMod, defenseMod, moraleMod);
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private record TerrainModifiers(int attackModifier, int defenseModifier) {}
    private record TerrainModsFallback(int attackModifier, int defenseModifier) {}
    private record TraitModifiers(int attackModifier, int defenseModifier, int moraleModifier) {}
}
