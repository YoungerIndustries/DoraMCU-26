package com.yisy.modules.doramcu.unified;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.DiceService;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.gamecore.RulesService;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for all thin GameEngine implementations.
 * <p>
 * Instead of duplicating {@code processAction} logic in 13 classes, this base class
 * handles action routing (switch on action type) and delegates to the injected
 * {@link GameModule}'s services. Each concrete engine only needs to extend this class
 * and provide its {@code GameModule} via the constructor.
 * </p>
 * <p>
 * Supported actions:
 * <ul>
 *   <li>{@code rollDice} — Roll dice via DiceService or RulesEngine</li>
 *   <li>{@code taskAttempt} — Resolve a task/check via RulesEngine</li>
 *   <li>{@code combatRound} — Resolve a single combat round via RulesEngine</li>
 *   <li>{@code createCombat} — Create a new combat encounter via CombatService</li>
 *   <li>{@code addCombatant} — Add a combatant to combat via CombatService</li>
 *   <li>{@code makeAttack} — Make an attack in combat via CombatService</li>
 *   <li>{@code healCombatant} — Heal a combatant via CombatService</li>
 *   <li>{@code endCombat} — End a combat session via CombatService</li>
 *   <li>{@code generateEncounter} — Generate a random encounter via EncounterService</li>
 *   <li>{@code resolveEncounter} — Resolve an encounter via EncounterService</li>
 *   <li>{@code queryRules} — Query rules by category via RulesEngine</li>
 *   <li>{@code getGameState} — Get current game state</li>
 * </ul>
 * </p>
 * <p>
 * Graceful degradation: if a GameModule returns {@code null} for a service,
 * {@code processAction} returns {@code {"error": "Service not available"}} instead
 * of throwing an exception.
 * </p>
 */
public abstract class AbstractGameEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(AbstractGameEngine.class);

    private final GameModule gameModule;
    private int currentTurn = 0;
    private boolean active = false;

    protected AbstractGameEngine(GameModule gameModule) {
        this.gameModule = gameModule;
    }

    protected GameModule getModule() {
        return gameModule;
    }

    // ==================== GameEngine Interface ====================

    @Override
    public String getName() {
        if (gameModule != null) {
            return gameModule.getModuleName();
        }
        return getGameType().name();
    }

    @Override
    public List<?> getPlayers() {
        return List.of();
    }

    @Override
    public void nextTurn() {
        currentTurn++;
    }

    @Override
    public int getCurrentTurn() {
        return currentTurn;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void start() {
        active = true;
        currentTurn = 0;
    }

    @Override
    public void pause() {
        active = false;
    }

    @Override
    public void end() {
        active = false;
        currentTurn = 0;
    }

    @Override
    public Map<String, Object> getGameState() {
        Map<String, Object> state = new HashMap<>();
        state.put("name", getName());
        state.put("gameType", getGameType().name());
        state.put("currentTurn", currentTurn);
        state.put("active", active);
        state.put("playerCount", getPlayers().size());
        state.put("availableActions", getAvailableActions());
        return state;
    }

    // ==================== Action Processing ====================

    @Override
    public Map<String, Object> processAction(Map<String, Object> action) {
        if (action == null || !action.containsKey("action")) {
            return error("Missing 'action' key in request");
        }

        String actionType = (String) action.get("action");
        log.debug("Processing action '{}' for engine '{}'", actionType, getName());

        return switch (actionType) {
            case "rollDice" -> handleRollDice(action);
            case "taskAttempt" -> handleTaskAttempt(action);
            case "combatRound" -> handleCombatRound(action);
            case "createCombat" -> handleCreateCombat(action);
            case "addCombatant" -> handleAddCombatant(action);
            case "makeAttack" -> handleMakeAttack(action);
            case "healCombatant" -> handleHealCombatant(action);
            case "endCombat" -> handleEndCombat(action);
            case "generateEncounter" -> handleGenerateEncounter(action);
            case "resolveEncounter" -> handleResolveEncounter(action);
            case "queryRules" -> handleQueryRules(action);
            case "getGameState" -> getGameState();
            default -> error("Unknown action: " + actionType);
        };
    }

    @Override
    public Set<String> getAvailableActions() {
        Set<String> actions = new HashSet<>();
        // Always available
        actions.add("getGameState");

        // Dice-related actions
        if (getModule() != null && (getModule().getDiceService() != null || getModule().getRulesEngine() != null)) {
            actions.add("rollDice");
        }

        // RulesEngine actions
        if (getModule() != null && getModule().getRulesEngine() != null) {
            actions.add("taskAttempt");
            actions.add("combatRound");
            actions.add("queryRules");
        }

        // CombatService actions
        if (getModule() != null && getModule().getCombatService() != null) {
            actions.add("createCombat");
            actions.add("addCombatant");
            actions.add("makeAttack");
            actions.add("healCombatant");
            actions.add("endCombat");
        }

        // EncounterService actions
        if (getModule() != null && getModule().getEncounterService() != null) {
            actions.add("generateEncounter");
            actions.add("resolveEncounter");
        }

        return actions;
    }

    // ==================== Action Handlers ====================

    private Map<String, Object> handleRollDice(Map<String, Object> action) {
        int sides = getInt(action, "sides", 6);
        int count = getInt(action, "count", 1);

        // Try RulesEngine first, then DiceService
        if (getModule() != null && getModule().getRulesEngine() != null) {
            int result = getModule().getRulesEngine().rollDice(sides, count);
            return success(Map.of(
                    "sides", sides,
                    "count", count,
                    "result", result,
                    "description", count + "d" + sides + " = " + result
            ));
        }

        if (getModule() != null && getModule().getDiceService() != null) {
            DiceService diceService = getModule().getDiceService();
            DiceService.DiceRollResult rollResult = diceService.rollFormula(count + "d" + sides);
            return success(Map.of(
                    "sides", sides,
                    "count", count,
                    "result", rollResult.getTotal(),
                    "description", rollResult.getDescription()
            ));
        }

        return error("Dice rolling not available for this game system");
    }

    private Map<String, Object> handleTaskAttempt(Map<String, Object> action) {
        if (getModule() == null || getModule().getRulesEngine() == null) {
            return error("Task resolution not available for this game system");
        }

        int difficulty = getInt(action, "difficulty", 10);
        int skillLevel = getInt(action, "skillLevel", 0);

        RulesEngine.TaskResult result = getModule().getRulesEngine().taskAttempt(difficulty, skillLevel);
        Map<String, Object> response = new HashMap<>();
        response.put("difficulty", difficulty);
        response.put("skillLevel", skillLevel);
        response.put("success", result.isSuccess());
        response.put("roll", result.getRoll());
        response.put("target", result.getTarget());
        response.put("description", result.getDescription());
        return success(response);
    }

    private Map<String, Object> handleCombatRound(Map<String, Object> action) {
        if (getModule() == null || getModule().getRulesEngine() == null) {
            return error("Combat resolution not available for this game system");
        }

        Map<String, Object> attacker = (Map<String, Object>) action.get("attacker");
        Map<String, Object> defender = (Map<String, Object>) action.get("defender");

        if (attacker == null || defender == null) {
            return error("Missing 'attacker' or 'defender' in combatRound action");
        }

        RulesEngine.CombatResult result = getModule().getRulesEngine().combatRound(attacker, defender);
        Map<String, Object> response = new HashMap<>();
        response.put("hit", result.isHit());
        response.put("damage", result.getDamage());
        response.put("description", result.getDescription());
        return success(response);
    }

    private Map<String, Object> handleCreateCombat(Map<String, Object> action) {
        CombatService combatService = getCombatServiceOrNull();
        if (combatService == null) {
            return error("Combat not available for this game system");
        }

        String encounterName = (String) action.getOrDefault("encounterName", "Unnamed Encounter");
        Long combatId = combatService.createCombat(encounterName);
        return success(Map.of(
                "combatId", combatId,
                "encounterName", encounterName
        ));
    }

    private Map<String, Object> handleAddCombatant(Map<String, Object> action) {
        CombatService combatService = getCombatServiceOrNull();
        if (combatService == null) {
            return error("Combat not available for this game system");
        }

        Long combatId = getLong(action, "combatId");
        if (combatId == null) {
            return error("Missing 'combatId' in addCombatant action");
        }

        String name = (String) action.get("name");
        int armorClass = getInt(action, "armorClass", 10);
        int maxHp = getInt(action, "maxHp", 10);
        boolean isCharacter = getBoolean(action, "isCharacter", false);

        CombatService.CombatantInfo info = combatService.addCombatant(combatId, name, armorClass, maxHp, isCharacter);
        return success(Map.of(
                "combatId", combatId,
                "name", info.getName(),
                "armorClass", info.getArmorClass(),
                "currentHp", info.getCurrentHp(),
                "maxHp", info.getMaxHp()
        ));
    }

    private Map<String, Object> handleMakeAttack(Map<String, Object> action) {
        CombatService combatService = getCombatServiceOrNull();
        if (combatService == null) {
            return error("Combat not available for this game system");
        }

        Long combatId = getLong(action, "combatId");
        if (combatId == null) {
            return error("Missing 'combatId' in makeAttack action");
        }

        CombatService.AttackRequest request = new CombatService.AttackRequest();
        request.setAttackerName((String) action.get("attackerName"));
        request.setTargetName((String) action.get("targetName"));
        request.setAttackBonus(getInt(action, "attackBonus", 0));
        request.setDamageFormula((String) action.getOrDefault("damageFormula", "1d6"));
        request.setDamageType((String) action.getOrDefault("damageType", "slashing"));
        request.setAdvantage(getBoolean(action, "advantage", false));
        request.setDisadvantage(getBoolean(action, "disadvantage", false));

        CombatService.AttackResult result = combatService.makeAttack(combatId, request);
        Map<String, Object> response = new HashMap<>();
        response.put("combatId", combatId);
        response.put("hit", result.isHit());
        response.put("damage", result.getDamageDealt());
        response.put("attacker", result.getAttackerName());
        response.put("target", result.getTargetName());
        response.put("description", result.getMessage());
        return success(response);
    }

    private Map<String, Object> handleHealCombatant(Map<String, Object> action) {
        CombatService combatService = getCombatServiceOrNull();
        if (combatService == null) {
            return error("Combat not available for this game system");
        }

        Long combatId = getLong(action, "combatId");
        if (combatId == null) {
            return error("Missing 'combatId' in healCombatant action");
        }

        String name = (String) action.get("name");
        int amount = getInt(action, "amount", 1);

        CombatService.CombatantInfo info = combatService.healCombatant(combatId, name, amount);
        return success(Map.of(
                "combatId", combatId,
                "name", info.getName(),
                "currentHp", info.getCurrentHp(),
                "maxHp", info.getMaxHp()
        ));
    }

    private Map<String, Object> handleEndCombat(Map<String, Object> action) {
        CombatService combatService = getCombatServiceOrNull();
        if (combatService == null) {
            return error("Combat not available for this game system");
        }

        Long combatId = getLong(action, "combatId");
        if (combatId == null) {
            return error("Missing 'combatId' in endCombat action");
        }

        String status = (String) action.getOrDefault("status", "ENDED");
        CombatService.CombatInfo info = combatService.endCombat(combatId, status);
        return success(Map.of(
                "combatId", combatId,
                "status", info.getStatus(),
                "encounterName", info.getEncounterName()
        ));
    }

    private Map<String, Object> handleGenerateEncounter(Map<String, Object> action) {
        EncounterService encounterService = getEncounterServiceOrNull();
        if (encounterService == null) {
            return error("Encounters not available for this game system");
        }

        String areaId = (String) action.getOrDefault("areaId", "default");
        int level = getInt(action, "level", 1);

        EncounterService.Encounter encounter = encounterService.generateRandomEncounter(areaId, level);
        Map<String, Object> response = new HashMap<>();
        response.put("id", encounter.getId());
        response.put("name", encounter.getName());
        response.put("description", encounter.getDescription());
        response.put("difficulty", encounter.getDifficulty());
        response.put("xpReward", encounter.getXpReward());
        response.put("areaId", encounter.getAreaId());
        return success(response);
    }

    private Map<String, Object> handleResolveEncounter(Map<String, Object> action) {
        EncounterService encounterService = getEncounterServiceOrNull();
        if (encounterService == null) {
            return error("Encounters not available for this game system");
        }

        Long encounterId = getLong(action, "encounterId");
        if (encounterId == null) {
            return error("Missing 'encounterId' in resolveEncounter action");
        }

        @SuppressWarnings("unchecked")
        List<Long> characterIds = (List<Long>) action.getOrDefault("characterIds", List.of());

        EncounterService.EncounterResult result = encounterService.resolveEncounter(encounterId, characterIds);
        return success(Map.of(
                "encounterId", result.getEncounterId(),
                "encounterName", result.getEncounterName(),
                "success", result.isSuccess(),
                "outcome", result.getOutcome(),
                "xpAwarded", result.getXpAwarded()
        ));
    }

    private Map<String, Object> handleQueryRules(Map<String, Object> action) {
        String category = (String) action.get("category");
        if (category == null) {
            return error("Missing 'category' in queryRules action");
        }

        // Try RulesEngine first
        if (getModule() != null && getModule().getRulesEngine() != null) {
            List<RulesEntry> entries = getModule().getRulesEngine().queryRules(category);
            return success(Map.of(
                    "category", category,
                    "count", entries.size(),
                    "rules", entries
            ));
        }

        // Fall back to RulesService
        if (getModule() != null && getModule().getRulesService() != null) {
            List<RulesEntry> entries = getModule().getRulesService().getRulesByCategory(category);
            return success(Map.of(
                    "category", category,
                    "count", entries.size(),
                    "rules", entries
            ));
        }

        return error("Rules query not available for this game system");
    }

    // ==================== Utility Methods ====================

    private CombatService getCombatServiceOrNull() {
        if (getModule() != null) {
            return getModule().getCombatService();
        }
        return null;
    }

    private EncounterService getEncounterServiceOrNull() {
        if (getModule() != null) {
            return getModule().getEncounterService();
        }
        return null;
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
