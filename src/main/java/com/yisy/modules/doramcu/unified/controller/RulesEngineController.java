package com.yisy.modules.doramcu.unified.controller;

import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the unified RulesEngine: dice rolling, task resolution,
 * and combat for any game system.
 * <p>
 * Instead of creating 13 separate endpoints (one per game system), this controller
 * accepts a {@code gameSystem} parameter and delegates to the correct engine via
 * {@link GameModuleRegistry#getRulesEngine(GameType)}. This is a single endpoint
 * that works for all systems.
 * </p>
 * <p>
 * Request/response DTOs are used for {@code taskAttempt} and {@code combatRound}
 * to avoid exposing internal Java types directly.
 * </p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><code>POST /api/v1/rules/engine/rollDice?gameSystem=&sides=&count=</code> — roll dice</li>
 *   <li><code>POST /api/v1/rules/engine/taskAttempt?gameSystem=</code> with body — task resolution</li>
 *   <li><code>POST /api/v1/rules/engine/combatRound?gameSystem=</code> with body — combat resolution</li>
 *   <li><code>GET /api/v1/rules/engine/queryRules?gameSystem=&category=</code> — query rules by category</li>
 *   <li><code>GET /api/v1/rules/engine/systems</code> — list all available game systems</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/rules/engine")
public class RulesEngineController {

    private static final Logger log = LoggerFactory.getLogger(RulesEngineController.class);

    private final GameModuleRegistry gameModuleRegistry;

    public RulesEngineController(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
    }

    // ==================== Dice Rolling ====================

    /**
     * Roll dice for a specific game system.
     *
     * @param gameSystem the game system name (e.g., "DND_5E", "STAR_WARS")
     * @param sides      the number of sides per die (default: 6)
     * @param count      the number of dice to roll (default: 1)
     */
    @PostMapping("/rollDice")
    public ResponseEntity<Map<String, Object>> rollDice(
            @RequestParam String gameSystem,
            @RequestParam(defaultValue = "6") int sides,
            @RequestParam(defaultValue = "1") int count) {
        log.debug("POST /api/v1/rules/engine/rollDice?gameSystem={}, sides={}, count={}", gameSystem, sides, count);
        RulesEngine engine = resolveEngine(gameSystem);

        int result = engine.rollDice(sides, count);
        Map<String, Object> response = new HashMap<>();
        response.put("gameSystem", gameSystem);
        response.put("sides", sides);
        response.put("count", count);
        response.put("result", result);
        response.put("description", count + "d" + sides + " = " + result);
        return ResponseEntity.ok(response);
    }

    // ==================== Task Resolution ====================

    /**
     * Resolve a task attempt for a specific game system.
     *
     * @param gameSystem the game system name
     * @param request    JSON body with {@code difficulty} and {@code skillLevel}
     */
    @PostMapping("/taskAttempt")
    public ResponseEntity<Map<String, Object>> taskAttempt(
            @RequestParam String gameSystem,
            @RequestBody TaskAttemptRequest request) {
        log.debug("POST /api/v1/rules/engine/taskAttempt?gameSystem={}, difficulty={}, skillLevel={}",
                gameSystem, request.difficulty, request.skillLevel);
        RulesEngine engine = resolveEngine(gameSystem);

        RulesEngine.TaskResult result = engine.taskAttempt(request.difficulty, request.skillLevel);
        Map<String, Object> response = new HashMap<>();
        response.put("gameSystem", gameSystem);
        response.put("difficulty", request.difficulty);
        response.put("skillLevel", request.skillLevel);
        response.put("success", result.isSuccess());
        response.put("roll", result.getRoll());
        response.put("target", result.getTarget());
        response.put("description", result.getDescription());
        return ResponseEntity.ok(response);
    }

    // ==================== Combat Resolution ====================

    /**
     * Resolve a combat round for a specific game system.
     *
     * @param gameSystem the game system name
     * @param request    JSON body with {@code attacker} and {@code defender} stat maps
     */
    @PostMapping("/combatRound")
    public ResponseEntity<Map<String, Object>> combatRound(
            @RequestParam String gameSystem,
            @RequestBody CombatRoundRequest request) {
        log.debug("POST /api/v1/rules/engine/combatRound?gameSystem={}", gameSystem);
        RulesEngine engine = resolveEngine(gameSystem);

        RulesEngine.CombatResult result = engine.combatRound(request.attacker, request.defender);
        Map<String, Object> response = new HashMap<>();
        response.put("gameSystem", gameSystem);
        response.put("hit", result.isHit());
        response.put("damage", result.getDamage());
        response.put("description", result.getDescription());
        return ResponseEntity.ok(response);
    }

    // ==================== Rules Query ====================

    /**
     * Query rules by category for a specific game system.
     *
     * @param gameSystem the game system name
     * @param category   the rule category (e.g., "COMBAT", "EXPLORATION")
     */
    @GetMapping("/queryRules")
    public ResponseEntity<List<RulesEntry>> queryRules(
            @RequestParam String gameSystem,
            @RequestParam String category) {
        log.debug("GET /api/v1/rules/engine/queryRules?gameSystem={}, category={}", gameSystem, category);
        RulesEngine engine = resolveEngine(gameSystem);
        return ResponseEntity.ok(engine.queryRules(category));
    }

    // ==================== Available Systems ====================

    /**
     * List all available game systems that have a RulesEngine implementation.
     */
    @GetMapping("/systems")
    public ResponseEntity<List<String>> getAvailableSystems() {
        log.debug("GET /api/v1/rules/engine/systems");
        List<String> systems = java.util.Arrays.stream(GameType.values())
                .map(gt -> {
                    if (gameModuleRegistry.getRulesEngine(gt).isPresent()) {
                        return gt.name();
                    }
                    return null;
                })
                .filter(s -> s != null)
                .toList();
        return ResponseEntity.ok(systems);
    }

    // ==================== Utility Methods ====================

    /**
     * Resolve a game system name to its RulesEngine.
     * Returns 404 if the game system is not found or has no RulesEngine.
     */
    private RulesEngine resolveEngine(String gameSystemName) {
        try {
            GameType gameType = GameType.valueOf(gameSystemName.toUpperCase());
            return gameModuleRegistry.getRulesEngine(gameType)
                    .orElseThrow(() -> new RuntimeException("No RulesEngine for " + gameSystemName));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown game system: " + gameSystemName);
        }
    }

    // ==================== Request DTOs ====================

    /**
     * Request body for task attempt endpoint.
     */
    public static class TaskAttemptRequest {
        public int difficulty;
        public int skillLevel;
    }

    /**
     * Request body for combat round endpoint.
     */
    public static class CombatRoundRequest {
        public Map<String, Object> attacker;
        public Map<String, Object> defender;
    }
}
