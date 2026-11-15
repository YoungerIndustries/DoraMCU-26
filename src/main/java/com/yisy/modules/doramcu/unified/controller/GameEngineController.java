package com.yisy.modules.doramcu.unified.controller;

import com.yisy.modules.doramcu.unified.GameEngine;
import com.yisy.modules.doramcu.unified.GameEngineRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for game engine state management.
 * <p>
 * Exposes the {@link GameEngineRegistry} via HTTP endpoints, enabling
 * frontend clients and external API consumers to manage game engine
 * state, turn advancement, and engine lifecycle across all game systems.
 * </p>
 * <p>
 * This controller delegates to {@link GameEngineRegistry} which manages
 * all 13 registered GameEngine implementations.
 * </p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><code>GET /api/v1/rules/engines/summary</code> — all engines summary</li>
 *   <li><code>GET /api/v1/rules/engines/list</code> — list all registered game types</li>
 *   <li><code>GET /api/v1/rules/engines/{gameType}</code> — get specific engine state</li>
 *   <li><code>POST /api/v1/rules/engines/{gameType}/start</code> — start a game engine</li>
 *   <li><code>POST /api/v1/rules/engines/{gameType}/pause</code> — pause a game engine</li>
 *   <li><code>POST /api/v1/rules/engines/{gameType}/end</code> — end a game engine</li>
 *   <li><code>POST /api/v1/rules/engines/{gameType}/nextTurn</code> — advance turn</li>
 *   <li><code>POST /api/v1/rules/engines/advanceAll</code> — advance all active engines</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/rules/engines")
public class GameEngineController {

    private static final Logger log = LoggerFactory.getLogger(GameEngineController.class);

    private final GameEngineRegistry engineRegistry;

    public GameEngineController(GameEngineRegistry engineRegistry) {
        this.engineRegistry = engineRegistry;
    }

    // ==================== Summary ====================

    /**
     * Get a summary of all registered game engines.
     * Returns engine count, game types, and current turn numbers.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        log.debug("GET /api/v1/rules/engines/summary");
        return ResponseEntity.ok(engineRegistry.getSummary());
    }

    // ==================== List ====================

    /**
     * List all registered game types that have a GameEngine.
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listEngines() {
        log.debug("GET /api/v1/rules/engines/list");
        List<String> types = engineRegistry.getRegisteredGameTypes().stream()
                .map(GameType::name)
                .sorted()
                .toList();
        return ResponseEntity.ok(types);
    }

    // ==================== Engine State ====================

    /**
     * Get the state of a specific game engine.
     * Returns name, type, current turn, active status, and turn-based flag.
     *
     * @param gameType the game type (e.g., "DND_5E", "STAR_WARS")
     */
    @GetMapping("/{gameType}")
    public ResponseEntity<Map<String, Object>> getEngineState(@PathVariable String gameType) {
        log.debug("GET /api/v1/rules/engines/{}", gameType);
        GameEngine engine = resolveEngine(gameType);

        Map<String, Object> state = new HashMap<>();
        state.put("gameType", gameType);
        state.put("name", engine.getName());
        state.put("currentTurn", engine.getCurrentTurn());
        state.put("isActive", engine.isActive());
        state.put("turnBased", engine.isTurnBased());
        state.put("playerCount", engine.getPlayers().size());
        return ResponseEntity.ok(state);
    }

    // ==================== Lifecycle Control ====================

    /**
     * Start a game engine. Resets turn to 0 and sets active to true.
     *
     * @param gameType the game type
     */
    @PostMapping("/{gameType}/start")
    public ResponseEntity<Map<String, Object>> startEngine(@PathVariable String gameType) {
        log.debug("POST /api/v1/rules/engines/{}/start", gameType);
        GameEngine engine = resolveEngine(gameType);
        engine.start();

        Map<String, Object> response = new HashMap<>();
        response.put("gameType", gameType);
        response.put("action", "start");
        response.put("status", "started");
        response.put("currentTurn", engine.getCurrentTurn());
        return ResponseEntity.ok(response);
    }

    /**
     * Pause a game engine. Sets active to false without resetting turn.
     *
     * @param gameType the game type
     */
    @PostMapping("/{gameType}/pause")
    public ResponseEntity<Map<String, Object>> pauseEngine(@PathVariable String gameType) {
        log.debug("POST /api/v1/rules/engines/{}/pause", gameType);
        GameEngine engine = resolveEngine(gameType);
        engine.pause();

        Map<String, Object> response = new HashMap<>();
        response.put("gameType", gameType);
        response.put("action", "pause");
        response.put("status", "paused");
        response.put("currentTurn", engine.getCurrentTurn());
        return ResponseEntity.ok(response);
    }

    /**
     * End a game engine. Sets active to false and resets turn to 0.
     *
     * @param gameType the game type
     */
    @PostMapping("/{gameType}/end")
    public ResponseEntity<Map<String, Object>> endEngine(@PathVariable String gameType) {
        log.debug("POST /api/v1/rules/engines/{}/end", gameType);
        GameEngine engine = resolveEngine(gameType);
        engine.end();

        Map<String, Object> response = new HashMap<>();
        response.put("gameType", gameType);
        response.put("action", "end");
        response.put("status", "ended");
        response.put("currentTurn", engine.getCurrentTurn());
        return ResponseEntity.ok(response);
    }

    // ==================== Turn Management ====================

    /**
     * Advance a game engine by one turn.
     * No-op for non-turn-based engines.
     *
     * @param gameType the game type
     */
    @PostMapping("/{gameType}/nextTurn")
    public ResponseEntity<Map<String, Object>> nextTurn(@PathVariable String gameType) {
        log.debug("POST /api/v1/rules/engines/{}/nextTurn", gameType);
        GameEngine engine = resolveEngine(gameType);
        engine.nextTurn();

        Map<String, Object> response = new HashMap<>();
        response.put("gameType", gameType);
        response.put("action", "nextTurn");
        response.put("currentTurn", engine.getCurrentTurn());
        response.put("turnBased", engine.isTurnBased());
        return ResponseEntity.ok(response);
    }

    /**
     * Advance all active game engines by one turn.
     * Only active, turn-based engines are advanced.
     */
    @PostMapping("/advanceAll")
    public ResponseEntity<Map<String, Object>> advanceAllTurns() {
        log.debug("POST /api/v1/rules/engines/advanceAll");
        engineRegistry.advanceAllTurns();

        Map<String, Object> response = new HashMap<>();
        response.put("action", "advanceAll");
        response.put("turnNumbers", new HashMap<>());
        for (GameEngine engine : engineRegistry.getAllEngines()) {
            ((Map<String, Integer>) response.get("turnNumbers"))
                    .put(engine.getGameType().name(), engine.getCurrentTurn());
        }
        return ResponseEntity.ok(response);
    }

    // ==================== Action Processing ====================

    /**
     * Process an action for a specific game engine.
     * Accepts a JSON body with the action type and parameters, e.g.:
     * {@code {"action": "rollDice", "sides": 6, "count": 2}}
     *
     * @param gameType the game type (e.g., "DND_5E", "STAR_WARS")
     * @param action   the action map with "action" key and action-specific parameters
     */
    @PostMapping("/{gameType}/action")
    public ResponseEntity<Map<String, Object>> processAction(
            @PathVariable String gameType,
            @RequestBody Map<String, Object> action) {
        log.debug("POST /api/v1/rules/engines/{}/action?gameType={}", gameType, action);
        if (action == null || !action.containsKey("action")) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Missing 'action' key in request body");
            return ResponseEntity.badRequest().body(error);
        }
        GameEngine engine = resolveEngine(gameType);
        Map<String, Object> result = engine.processAction(action);
        return ResponseEntity.ok(result);
    }

    /**
     * Get the set of available actions for a specific game engine.
     * Returns the action names that the engine supports based on its available services.
     *
     * @param gameType the game type
     */
    @GetMapping("/{gameType}/actions")
    public ResponseEntity<Map<String, Object>> getAvailableActions(@PathVariable String gameType) {
        log.debug("GET /api/v1/rules/engines/{}/actions", gameType);
        GameEngine engine = resolveEngine(gameType);

        Map<String, Object> response = new HashMap<>();
        response.put("gameType", gameType);
        response.put("name", engine.getName());
        response.put("availableActions", engine.getAvailableActions());
        return ResponseEntity.ok(response);
    }

    /**
     * Generic action processing endpoint with gameType as query parameter.
     * Accepts a JSON body with the action type and parameters.
     *
     * @param gameType the game type (query param)
     * @param action   the action map with "action" key and action-specific parameters
     */
    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> processActionGeneric(
            @RequestParam String gameType,
            @RequestBody Map<String, Object> action) {
        log.debug("POST /api/v1/rules/engines/action?gameType={}", gameType);
        if (action == null || !action.containsKey("action")) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Missing 'action' key in request body");
            return ResponseEntity.badRequest().body(error);
        }
        GameEngine engine = resolveEngine(gameType);
        Map<String, Object> result = engine.processAction(action);
        return ResponseEntity.ok(result);
    }

    // ==================== Utility Methods ====================

    /**
     * Resolve a game type name to its GameEngine.
     * Returns 404 if the game type is not found.
     */
    private GameEngine resolveEngine(String gameTypeName) {
        try {
            GameType gameType = GameType.valueOf(gameTypeName.toUpperCase());
            GameEngine engine = engineRegistry.getEngine(gameType);
            if (engine == null) {
                throw new RuntimeException("No GameEngine registered for " + gameTypeName);
            }
            return engine;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown game type: " + gameTypeName);
        }
    }
}
