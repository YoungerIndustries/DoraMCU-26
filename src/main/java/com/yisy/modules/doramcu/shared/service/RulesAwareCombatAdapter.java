package com.yisy.modules.doramcu.shared.service;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.CombatService.AttackRequest;
import com.yisy.modules.doramcu.gamecore.CombatService.AttackResult;
import com.yisy.modules.doramcu.gamecore.CombatService.CombatInfo;
import com.yisy.modules.doramcu.gamecore.CombatService.CombatantInfo;
import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesContext;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decorator that wraps any {@link CombatService} implementation and consults the
 * rules database before resolving attacks.
 * <p>
 * This is the core component that connects the rules database to actual gameplay.
 * Previously, all combat adapters used hard-coded attack/defense logic with no
 * rules lookup. Now, every attack passes through this adapter, which:
 * </p>
 * <ol>
 *   <li>Builds a {@link RulesContext} from the combat situation</li>
 *   <li>Queries the rules database for applicable modifiers (cover, terrain, faction, weapon type)</li>
 *   <li>Aggregates all modifiers into a {@link RulesModifier}</li>
 *   <li>Applies the modifiers to the attack request before delegating to the underlying service</li>
 *   <li>Enriches the attack result with rules-based context information</li>
 * </ol>
 * <p>
 * The adapter uses {@link UnifiedRulesQueryService} for cross-system rule lookups,
 * so it works with any game system that has populated rules entries.
 * </p>
 * <p>
 * Usage: wrap any CombatService implementation:
 * <pre>
 * CombatService rulesAware = new RulesAwareCombatAdapter(delegateCombatService, unifiedRulesQueryService);
 * </pre>
 * </p>
 */
@Service("rulesAwareCombatAdapter")
public class RulesAwareCombatAdapter implements CombatService {

    private static final Logger log = LoggerFactory.getLogger(RulesAwareCombatAdapter.class);

    /**
     * Pattern to extract numeric bonuses from rule text (e.g., "+2 to attack", "-1 defense").
     */
    static final Pattern BONUS_PATTERN = Pattern.compile("([+-]\\d+)\\s*(to\\s*)?(attack|hit|defense|ac|armor|damage)?", Pattern.CASE_INSENSITIVE);

    /** The underlying combat service that handles the actual combat logic. */
    private final CombatService delegate;

    /** Cross-system rules query service for looking up applicable rules. */
    private final UnifiedRulesQueryService rulesQueryService;

    /** Maps combat IDs to their associated game system (for rules lookup). */
    private final Map<Long, GameSystem> combatGameSystems = new ConcurrentHashMap<>();

    /** Maps combat IDs to their associated rules context. */
    private final Map<Long, RulesContext> combatContexts = new ConcurrentHashMap<>();

    /** Counter for internal combat ID mapping when delegate creates its own IDs. */
    private final AtomicLong internalIdCounter = new AtomicLong(1);

    /** Map from our external IDs to delegate's internal IDs. */
    private final Map<Long, Long> idMapping = new ConcurrentHashMap<>();

    /**
     * Creates a new rules-aware combat adapter.
     *
     * @param delegate the underlying combat service to wrap
     * @param rulesQueryService the cross-system rules query service
     */
    public RulesAwareCombatAdapter(@Qualifier("delegatingCombatService") CombatService delegate, UnifiedRulesQueryService rulesQueryService) {
        this.delegate = delegate;
        this.rulesQueryService = rulesQueryService;
    }

    // ==================== Combat Lifecycle ====================

    @Override
    public Long createCombat(String encounterName) {
        Long externalId = internalIdCounter.getAndIncrement();
        Long delegateId = delegate.createCombat(encounterName);
        idMapping.put(externalId, delegateId);
        log.info("Created rules-aware combat {} (delegate {}) for encounter: {}", externalId, delegateId, encounterName);
        return externalId;
    }

    @Override
    public CombatInfo getCombat(Long combatId) {
        Long delegateId = idMapping.get(combatId);
        if (delegateId == null) return null;
        return delegate.getCombat(delegateId);
    }

    @Override
    public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
        Long delegateId = idMapping.get(combatId);
        if (delegateId == null) return null;
        return delegate.addCombatant(delegateId, name, armorClass, maxHp, isCharacter);
    }

    @Override
    public CombatInfo rollInitiative(Long combatId, Map<String, Integer> dexModifiers) {
        Long delegateId = idMapping.get(combatId);
        if (delegateId == null) return null;

        // Look up initiative rules from the rules database
        GameSystem gameSystem = combatGameSystems.get(combatId);
        if (gameSystem != null) {
            List<RulesEntry> initiativeRules = rulesQueryService.searchSystems("initiative", List.of(gameSystem));
            if (!initiativeRules.isEmpty()) {
                log.debug("Found {} initiative rules for {}: {}", initiativeRules.size(), gameSystem,
                        initiativeRules.get(0).getTitle());
            }
        }

        return delegate.rollInitiative(delegateId, dexModifiers);
    }

    // ==================== Core: Rules-Aware Attack Resolution ====================

    @Override
    public AttackResult makeAttack(Long combatId, AttackRequest request) {
        Long delegateId = idMapping.get(combatId);
        if (delegateId == null) {
            AttackResult error = new AttackResult();
            error.setAttackerName(request.getAttackerName());
            error.setTargetName(request.getTargetName());
            error.setHit(false);
            error.setMessage("Combat session not found");
            return error;
        }

        // Step 1: Build the rules context from the attack request and combat state
        RulesContext context = buildRulesContext(combatId, request);

        // Step 2: Query the rules database for applicable modifiers
        RulesModifier modifier = resolveRulesModifiers(context);

        // Step 3: Apply modifiers to the attack request
        AttackRequest modifiedRequest = applyModifiers(request, modifier);

        // Step 4: Delegate to the underlying combat service
        AttackResult result = delegate.makeAttack(delegateId, modifiedRequest);

        // Step 5: Enrich the result with rules-based context
        enrichResult(result, modifier, context);

        log.debug("Rules-aware attack: {} vs {} Ã¢â‚¬â€ modifier: {} Ã¢â‚¬â€ result: {}",
                request.getAttackerName(), request.getTargetName(), modifier, result.isHit() ? "HIT" : "MISS");

        return result;
    }

    @Override
    public CombatantInfo healCombatant(Long combatId, String name, int amount) {
        Long delegateId = idMapping.get(combatId);
        if (delegateId == null) return null;
        return delegate.healCombatant(delegateId, name, amount);
    }

    @Override
    public CombatInfo endCombat(Long combatId, String status) {
        Long delegateId = idMapping.get(combatId);
        if (delegateId == null) return null;
        combatGameSystems.remove(combatId);
        combatContexts.remove(combatId);
        return delegate.endCombat(delegateId, status);
    }

    // ==================== Rules Context Building ====================

    /**
     * Builds a RulesContext from the current combat state and attack request.
     * <p>
     * This method extracts all available context information from the combat session
     * and the attack request to enable precise rules lookups.
     * </p>
     */
    protected RulesContext buildRulesContext(Long combatId, AttackRequest request) {
        RulesContext context = new RulesContext();

        // Retrieve stored game system for this combat
        GameSystem gameSystem = combatGameSystems.get(combatId);
        context.setGameSystem(gameSystem);

        // Retrieve stored context (terrain, factions, etc.)
        RulesContext storedContext = combatContexts.get(combatId);
        if (storedContext != null) {
            context.setTerrainType(storedContext.getTerrainType());
            context.setScenarioName(storedContext.getScenarioName());
            context.setSpecialConditions(new ArrayList<>(storedContext.getSpecialConditions()));
        }

        // Extract faction and type info from combatant names
        // Convention: names may contain faction info (e.g., "Stormtrooper-1" Ã¢â€ â€™ faction=EMPIRE, type=TROOPER)
        String attackerName = request.getAttackerName();
        String targetName = request.getTargetName();

        // Try to infer faction from combatant names
        String attackerFaction = inferFaction(attackerName, gameSystem);
        String defenderFaction = inferFaction(targetName, gameSystem);
        context.setAttackerFaction(attackerFaction);
        context.setDefenderFaction(defenderFaction);

        // Try to infer unit type from combatant names
        String attackerType = inferUnitType(attackerName, gameSystem);
        String defenderType = inferUnitType(targetName, gameSystem);
        context.setAttackerType(attackerType);
        context.setDefenderType(defenderType);

        // Extract weapon type from damage type or formula
        String damageType = request.getDamageType();
        if (damageType != null && !damageType.isEmpty()) {
            context.setDamageType(damageType);
            context.setWeaponType(inferWeaponType(damageType, request.getDamageFormula()));
        }

        // Check for advantage/disadvantage in request
        if (request.isAdvantage()) {
            context.addSpecialCondition("ADVANTAGE");
        }
        if (request.isDisadvantage()) {
            context.addSpecialCondition("DISADVANTAGE");
        }

        return context;
    }

    /**
     * Infers the faction from a combatant name.
     * <p>
     * Uses keyword matching against known faction names for each game system.
     * </p>
     */
    protected String inferFaction(String name, GameSystem gameSystem) {
        if (name == null) return null;
        String lower = name.toLowerCase();

        if (gameSystem == GameSystem.STAR_WARS) {
            if (lower.contains("storm") || lower.contains("tie") || lower.contains("at-st") || lower.contains("imperial")) return "EMPIRE";
            if (lower.contains("rebel") || lower.contains("x-wing") || lower.contains("awing") || lower.contains("resistance")) return "REBELS";
            if (lower.contains("separatist") || lower.contains("battle") || lower.contains("droideka") || lower.contains("b1")) return "SEPARATISTS";
            if (lower.contains("sith") || lower.contains("dark")) return "SITH";
            if (lower.contains("jedi")) return "JEDI_ORDER";
            if (lower.contains("first order") || lower.contains("capital")) return "FIRST_ORDER";
            if (lower.contains("scum") || lower.contains("villain") || lower.contains("mandalorian")) return "SCUM_AND_VILLAINY";
        } else if (gameSystem == GameSystem.BABYLON_5) {
            if (lower.contains("centauri") || lower.contains("cent")) return "CENTAURI";
            if (lower.contains("minbari")) return "MINBARI";
            if (lower.contains("narn")) return "NARN";
            if (lower.contains("shadow")) return "SHADOWS";
            if (lower.contains("earthforce") || lower.contains("ef")) return "EARTHFORCE";
        } else if (gameSystem == GameSystem.DND_5E) {
            // D&D factions are more campaign-dependent; use generic inference
            if (lower.contains("dragon") || lower.contains("red")) return "DRAGONLORDS";
            if (lower.contains("drow") || lower.contains("dark elf")) return "DROW";
        }
        return null;
    }

    /**
     * Infers the unit type from a combatant name.
     */
    protected String inferUnitType(String name, GameSystem gameSystem) {
        if (name == null) return null;
        String lower = name.toLowerCase();

        if (gameSystem == GameSystem.STAR_WARS) {
            if (lower.contains("trooper") || lower.contains("soldier")) return "TROOPER";
            if (lower.contains("jedi")) return "JEDI";
            if (lower.contains("sith")) return "SITH";
            if (lower.contains("officer") || lower.contains("commander")) return "OFFICER";
            if (lower.contains("droid") || lower.contains("b1") || lower.contains("droideka")) return "DROID";
            if (lower.contains("wookie") || lower.contains("chew")) return "WOOKIE";
            if (lower.contains("sith") || lower.contains("vader")) return "DARK_SIDE";
        } else if (gameSystem == GameSystem.DND_5E) {
            if (lower.contains("warrior") || lower.contains("fighter")) return "FIGHTER";
            if (lower.contains("mage") || lower.contains("wizard") || lower.contains("sorcerer")) return "CASTER";
            if (lower.contains("rogue") || lower.contains("thief")) return "ROGUE";
            if (lower.contains("cleric") || lower.contains("paladin")) return "HOLY";
        }
        return null;
    }

    /**
     * Infers the weapon type from damage type or formula.
     */
    protected String inferWeaponType(String damageType, String damageFormula) {
        if (damageType != null) {
            String lower = damageType.toLowerCase();
            if (lower.contains("energy") || lower.contains("blaster") || lower.contains("laser")) return "ENERGY";
            if (lower.contains("kinetic") || lower.contains("bullet") || lower.contains("slug")) return "KINETIC";
            if (lower.contains("piercing")) return "PIERCING";
            if (lower.contains("slashing") || lower.contains("blade") || lower.contains("sword")) return "SLASHING";
            if (lower.contains("bludgeoning") || lower.contains("blunt")) return "BLUDGEONING";
            if (lower.contains("force") || lower.contains("lightsaber")) return "FORCE";
        }
        return null;
    }

    // ==================== Rules Modifier Resolution ====================

    /**
     * Queries the rules database for all applicable modifiers based on the combat context.
     * <p>
     * This is the core method that connects the rules database to gameplay.
     * It searches for rules related to:
     * <ul>
     *   <li>Cover bonuses (based on cover type)</li>
     *   <li>Terrain effects (based on terrain type)</li>
     *   <li>Faction advantages (based on attacker/defender factions)</li>
     *   <li>Weapon type effects (based on weapon type)</li>
     *   <li>Special conditions (flanking, surprise, high ground, etc.)</li>
     *   <li>Unit type abilities (Jedi, officer, etc.)</li>
     * </ul>
     * </p>
     *
     * @param context the combat context
     * @return aggregated rules modifier
     */
    protected RulesModifier resolveRulesModifiers(RulesContext context) {
        RulesModifier combined = RulesModifier.none();

        if (context.getGameSystem() == null) {
            log.debug("No game system in context, skipping rules lookup");
            return combined;
        }

        // 1. Cover bonuses
        RulesModifier coverMod = resolveCoverModifiers(context);
        if (!coverMod.isEmpty()) {
            combined.combineWith(coverMod);
        }

        // 2. Terrain effects
        RulesModifier terrainMod = resolveTerrainModifiers(context);
        if (!terrainMod.isEmpty()) {
            combined.combineWith(terrainMod);
        }

        // 3. Faction-specific modifiers
        RulesModifier factionMod = resolveFactionModifiers(context);
        if (!factionMod.isEmpty()) {
            combined.combineWith(factionMod);
        }

        // 4. Weapon type effects
        RulesModifier weaponMod = resolveWeaponModifiers(context);
        if (!weaponMod.isEmpty()) {
            combined.combineWith(weaponMod);
        }

        // 5. Special conditions
        RulesModifier conditionMod = resolveConditionModifiers(context);
        if (!conditionMod.isEmpty()) {
            combined.combineWith(conditionMod);
        }

        // 6. Unit type abilities
        RulesModifier unitMod = resolveUnitTypeModifiers(context);
        if (!unitMod.isEmpty()) {
            combined.combineWith(unitMod);
        }

        if (!combined.isEmpty()) {
            log.debug("Resolved rules modifiers for {}: {}", context.describe(), combined);
        }

        return combined;
    }

    /**
     * Resolves cover-based modifiers from the rules database.
     */
    protected RulesModifier resolveCoverModifiers(RulesContext context) {
        String coverType = context.getCoverType();
        if (coverType == null || coverType.isEmpty() || "NONE".equals(coverType)) {
            return RulesModifier.none();
        }

        // Search for cover rules in the game system
        List<RulesEntry> coverRules = rulesQueryService.searchSystems("cover", List.of(context.getGameSystem()));
        if (coverRules.isEmpty()) {
            // Fall back to default cover bonuses based on standard wargaming conventions
            return applyDefaultCoverBonuses(coverType);
        }

        RulesModifier mod = RulesModifier.none();
        for (RulesEntry entry : coverRules) {
            String text = entry.getText();
            if (text == null) continue;

            // Look for cover-related keywords in the rule text
            String lower = text.toLowerCase();
            if (lower.contains(coverType.toLowerCase())) {
                // Extract numeric bonuses from the rule text
                RulesModifier entryMod = extractModifiersFromText(entry, "cover");
                if (!entryMod.isEmpty()) {
                    mod.combineWith(entryMod);
                }
            }
        }

        // If no specific rules found, apply defaults
        if (mod.isEmpty()) {
            mod = applyDefaultCoverBonuses(coverType);
        }

        return mod;
    }

    /**
     * Applies default cover bonuses when no specific rules are found.
     * Based on standard wargaming conventions (Legion, 3R4, etc.).
     */
    protected RulesModifier applyDefaultCoverBonuses(String coverType) {
        RulesModifier mod = RulesModifier.none();
        switch (coverType.toUpperCase()) {
            case "HALF":
                mod.setDefenseBonus(2);
                mod.setDescription("Half cover: +2 to defense");
                mod.addRuleSource("Default cover rules");
                break;
            case "TOTAL":
                mod.setDefenseBonus(4);
                mod.setDescription("Total cover: +4 to defense");
                mod.addRuleSource("Default cover rules");
                break;
            case "CONCEALMENT":
                mod.setAttackBonus(-2);
                mod.setDescription("Concealment: -2 to attack");
                mod.addRuleSource("Default cover rules");
                break;
            default:
                break;
        }
        return mod;
    }

    /**
     * Resolves terrain-based modifiers from the rules database.
     */
    protected RulesModifier resolveTerrainModifiers(RulesContext context) {
        String terrainType = context.getTerrainType();
        if (terrainType == null || terrainType.isEmpty()) {
            return RulesModifier.none();
        }

        List<RulesEntry> terrainRules = rulesQueryService.searchSystems(terrainType.toLowerCase(), List.of(context.getGameSystem()));
        if (terrainRules.isEmpty()) {
            return RulesModifier.none();
        }

        RulesModifier mod = RulesModifier.none();
        for (RulesEntry entry : terrainRules) {
            String text = entry.getText();
            if (text == null) continue;
            String lower = text.toLowerCase();
            if (lower.contains("terrain") || lower.contains("ground") || lower.contains("difficult")) {
                RulesModifier entryMod = extractModifiersFromText(entry, "terrain");
                if (!entryMod.isEmpty()) {
                    mod.combineWith(entryMod);
                }
            }
        }
        return mod;
    }

    /**
     * Resolves faction-specific modifiers from the rules database.
     */
    protected RulesModifier resolveFactionModifiers(RulesContext context) {
        String attackerFaction = context.getAttackerFaction();
        if (attackerFaction == null || attackerFaction.isEmpty()) {
            return RulesModifier.none();
        }

        List<RulesEntry> factionRules = rulesQueryService.searchSystems(attackerFaction.toLowerCase(), List.of(context.getGameSystem()));
        if (factionRules.isEmpty()) {
            return RulesModifier.none();
        }

        RulesModifier mod = RulesModifier.none();
        for (RulesEntry entry : factionRules) {
            String text = entry.getText();
            if (text == null) continue;
            String lower = text.toLowerCase();
            if (lower.contains("advantage") || lower.contains("bonus") || lower.contains("special")) {
                RulesModifier entryMod = extractModifiersFromText(entry, "faction:" + attackerFaction);
                if (!entryMod.isEmpty()) {
                    mod.combineWith(entryMod);
                }
            }
        }
        return mod;
    }

    /**
     * Resolves weapon type modifiers from the rules database.
     */
    protected RulesModifier resolveWeaponModifiers(RulesContext context) {
        String weaponType = context.getWeaponType();
        if (weaponType == null || weaponType.isEmpty()) {
            return RulesModifier.none();
        }

        List<RulesEntry> weaponRules = rulesQueryService.searchSystems(weaponType.toLowerCase(), List.of(context.getGameSystem()));
        if (weaponRules.isEmpty()) {
            return RulesModifier.none();
        }

        RulesModifier mod = RulesModifier.none();
        for (RulesEntry entry : weaponRules) {
            String text = entry.getText();
            if (text == null) continue;
            RulesModifier entryMod = extractModifiersFromText(entry, "weapon:" + weaponType);
            if (!entryMod.isEmpty()) {
                mod.combineWith(entryMod);
            }
        }
        return mod;
    }

    /**
     * Resolves special condition modifiers (flanking, surprise, high ground, etc.).
     */
    protected RulesModifier resolveConditionModifiers(RulesContext context) {
        RulesModifier mod = RulesModifier.none();

        for (String condition : context.getSpecialConditions()) {
            // Skip advantage/disadvantage Ã¢â‚¬â€ those are handled directly
            if ("ADVANTAGE".equals(condition) || "DISADVANTAGE".equals(condition)) {
                continue;
            }

            List<RulesEntry> conditionRules = rulesQueryService.searchSystems(
                    condition.toLowerCase(), List.of(context.getGameSystem()));

            for (RulesEntry entry : conditionRules) {
                RulesModifier entryMod = extractModifiersFromText(entry, "condition:" + condition);
                if (!entryMod.isEmpty()) {
                    mod.combineWith(entryMod);
                }
            }

            // Apply default condition modifiers for common conditions
            RulesModifier defaultMod = applyDefaultConditionModifier(condition);
            if (!defaultMod.isEmpty()) {
                mod.combineWith(defaultMod);
            }
        }

        return mod;
    }

    /**
     * Applies default modifiers for common combat conditions.
     */
    protected RulesModifier applyDefaultConditionModifier(String condition) {
        RulesModifier mod = RulesModifier.none();
        switch (condition.toUpperCase()) {
            case "FLANKING":
                mod.setAttackBonus(2);
                mod.setDescription("Flanking: +2 to attack");
                mod.addRuleSource("Default condition rules");
                break;
            case "SURPRISE":
                mod.setAttackBonus(2);
                mod.setAdvantage(true);
                mod.setDescription("Surprise: +2 to attack, advantage");
                mod.addRuleSource("Default condition rules");
                break;
            case "HIGH_GROUND":
                mod.setAttackBonus(1);
                mod.setDescription("High ground: +1 to attack");
                mod.addRuleSource("Default condition rules");
                break;
            case "LOW_GROUND":
                mod.setAttackBonus(-1);
                mod.setDescription("Low ground: -1 to attack");
                mod.addRuleSource("Default condition rules");
                break;
            default:
                break;
        }
        return mod;
    }

    /**
     * Resolves unit type-specific modifiers (Jedi abilities, officer bonuses, etc.).
     */
    protected RulesModifier resolveUnitTypeModifiers(RulesContext context) {
        String attackerType = context.getAttackerType();
        if (attackerType == null || attackerType.isEmpty()) {
            return RulesModifier.none();
        }

        List<RulesEntry> unitRules = rulesQueryService.searchSystems(attackerType.toLowerCase(), List.of(context.getGameSystem()));
        if (unitRules.isEmpty()) {
            return RulesModifier.none();
        }

        RulesModifier mod = RulesModifier.none();
        for (RulesEntry entry : unitRules) {
            String text = entry.getText();
            if (text == null) continue;
            String lower = text.toLowerCase();
            if (lower.contains("ability") || lower.contains("special") || lower.contains("power")) {
                RulesModifier entryMod = extractModifiersFromText(entry, "unit:" + attackerType);
                if (!entryMod.isEmpty()) {
                    mod.combineWith(entryMod);
                }
            }
        }
        return mod;
    }

    // ==================== Modifier Extraction ====================

    /**
     * Extracts numeric modifiers from a rule entry's text.
     * <p>
     * Parses the rule text for patterns like "+2 to attack", "-1 to defense",
     * "advantage on rolls", etc.
     * </p>
     */
    protected RulesModifier extractModifiersFromText(RulesEntry entry, String sourceLabel) {
        RulesModifier mod = RulesModifier.none();
        String text = entry.getText();
        if (text == null || text.isEmpty()) return mod;

        String lower = text.toLowerCase();

        // Look for numeric bonuses
        Matcher matcher = BONUS_PATTERN.matcher(text);
        while (matcher.find()) {
            String value = matcher.group(1);
            String type = matcher.group(3);
            int num = Integer.parseInt(value);

            if (type == null || type.isEmpty() || type.equalsIgnoreCase("attack") || type.equalsIgnoreCase("hit")) {
                mod.setAttackBonus(mod.getAttackBonus() + num);
            } else if (type.equalsIgnoreCase("defense") || type.equalsIgnoreCase("ac") || type.equalsIgnoreCase("armor")) {
                mod.setDefenseBonus(mod.getDefenseBonus() + num);
            } else if (type.equalsIgnoreCase("damage")) {
                mod.setDamageModifier(mod.getDamageModifier() + num);
            }
        }

        // Look for advantage/disadvantage keywords
        if (lower.contains("advantage") && !lower.contains("disadvantage")) {
            mod.setAdvantage(true);
        }
        if (lower.contains("disadvantage")) {
            mod.setDisadvantage(true);
        }

        // Look for critical hit keywords
        if (lower.contains("critical") || lower.contains("crit") || lower.contains("double damage")) {
            if (lower.contains("auto") || lower.contains("always")) {
                mod.setAutoCritical(true);
            } else {
                mod.setDamageMultiplier(2);
            }
        }

        // Look for miss keywords
        if (lower.contains("auto miss") || lower.contains("automatically miss")) {
            mod.setAutoMiss(true);
        }

        if (!mod.isEmpty()) {
            String source = entry.getTitle() != null ? entry.getTitle() : sourceLabel;
            mod.addRuleSource(source);
            if (mod.getDescription() == null) {
                mod.setDescription(source + ": " + (entry.getText() != null ? entry.getText().substring(0, Math.min(80, entry.getText().length())) : ""));
            }
        }

        return mod;
    }

    // ==================== Modifier Application ====================

    /**
     * Applies the resolved rules modifiers to an attack request.
     * <p>
     * Creates a new AttackRequest with the modifiers applied:
     * attack bonus is added, advantage/disadvantage is set,
     * and damage formula is adjusted if needed.
     * </p>
     */
    protected AttackRequest applyModifiers(AttackRequest original, RulesModifier modifier) {
        AttackRequest modified = new AttackRequest();
        modified.setAttackerName(original.getAttackerName());
        modified.setTargetName(original.getTargetName());

        // Apply attack bonus from rules
        int attackBonus = original.getAttackBonus() + modifier.getAttackBonus();
        modified.setAttackBonus(attackBonus);

        // Apply advantage/disadvantage from rules
        boolean advantage = original.isAdvantage() || modifier.isAdvantage();
        boolean disadvantage = original.isDisadvantage() || modifier.isDisadvantage();
        // If both, they cancel out
        if (advantage && disadvantage) {
            advantage = false;
            disadvantage = false;
        }
        modified.setAdvantage(advantage);
        modified.setDisadvantage(disadvantage);

        // Apply damage formula override if specified by rules
        String damageFormula = modifier.getDiceFormula() != null ? modifier.getDiceFormula() : original.getDamageFormula();
        modified.setDamageFormula(damageFormula);

        // Apply damage type override if specified by rules
        String damageType = modifier.getDamageType() != null ? modifier.getDamageType() : original.getDamageType();
        modified.setDamageType(damageType);

        return modified;
    }

    /**
     * Enriches the attack result with rules-based context information.
     * <p>
     * Appends the rules modifier description to the result message
     * so the user can see which rules were applied.
     * </p>
     */
    protected void enrichResult(AttackResult result, RulesModifier modifier, RulesContext context) {
        if (modifier.isEmpty()) {
            return;
        }

        // Apply damage modifier and multiplier to the result
        int originalDamage = result.getDamageDealt();
        int adjustedDamage = originalDamage + modifier.getDamageModifier();
        adjustedDamage = Math.max(0, adjustedDamage * modifier.getDamageMultiplier());
        result.setDamageDealt(adjustedDamage);

        // Apply auto-critical
        if (modifier.isAutoCritical()) {
            result.setCriticalHit(true);
        }

        // Apply auto-miss
        if (modifier.isAutoMiss()) {
            result.setHit(false);
            result.setDamageDealt(0);
        }

        // Build enriched message
        StringBuilder message = new StringBuilder();
        if (result.getMessage() != null) {
            message.append(result.getMessage());
        }
        message.append(" [Rules: ");
        message.append(modifier.getDescription());
        if (!modifier.getRuleSources().isEmpty()) {
            message.append(" | Sources: ").append(String.join(", ", modifier.getRuleSources()));
        }
        message.append("]");
        result.setMessage(message.toString());
    }

    // ==================== Context Management ====================

    /**
     * Sets the game system for a combat session.
     * <p>
     * Call this after createCombat() to enable rules-aware combat for a specific game system.
     * </p>
     *
     * @param combatId the combat session ID
     * @param gameSystem the game system governing this combat
     */
    public void setCombatGameSystem(Long combatId, GameSystem gameSystem) {
        combatGameSystems.put(combatId, gameSystem);
        log.info("Set game system {} for combat {}", gameSystem, combatId);
    }

    /**
     * Sets the rules context for a combat session.
     * <p>
     * Call this to provide terrain, faction, and other context information
     * that will be used for rules lookups during combat.
     * </p>
     *
     * @param combatId the combat session ID
     * @param context the rules context
     */
    public void setCombatContext(Long combatId, RulesContext context) {
        combatContexts.put(combatId, context);
        log.info("Set rules context for combat {}: {}", combatId, context.describe());
    }

    /**
     * Gets the current rules context for a combat session.
     *
     * @param combatId the combat session ID
     * @return the current rules context, or null if not set
     */
    public RulesContext getCombatContext(Long combatId) {
        return combatContexts.get(combatId);
    }

    /**
     * Resolves rules modifiers for a given context without performing an attack.
     * <p>
     * Useful for previewing what modifiers would apply before committing to an attack.
     * </p>
     *
     * @param context the combat context
     * @return the resolved rules modifier
     */
    public RulesModifier previewModifiers(RulesContext context) {
        return resolveRulesModifiers(context);
    }

    /**
     * Returns the underlying delegate combat service.
     *
     * @return the delegate combat service
     */
    public CombatService getDelegate() {
        return delegate;
    }
}
