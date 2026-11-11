package com.yisy.modules.doraneuron.controller.multigame;

import com.yisy.modules.doraneuron.model.multigame.GameProfile;
import com.yisy.modules.doraneuron.protocol.multigame.RegisterGameRequest;
import com.yisy.modules.doraneuron.protocol.multigame.UpdateGameRequest;
import com.yisy.modules.doraneuron.service.multigame.GameRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for managing multiple games on this Neuron.
 *
 * Endpoints allow registering, updating, removing, and querying game profiles.
 * Each game can have its own ONNX model, game server URL, and AI configuration.
 */
@RestController
@RequestMapping("/api/v1/game")
public class GameManagementController {

    private static final Logger log = LoggerFactory.getLogger(GameManagementController.class);

    private final GameRegistryService gameRegistry;

    public GameManagementController(GameRegistryService gameRegistry) {
        this.gameRegistry = gameRegistry;
    }

    /**
     * Register a new game profile.
     *
     * POST /api/v1/game/register
     */
    @PostMapping("/register")
    public ResponseEntity<GameProfile> registerGame(@RequestBody RegisterGameRequest request) {
        try {
            GameProfile profile = gameRegistry.registerGame(request);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to register game: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error registering game: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing game profile.
     *
     * PUT /api/v1/game/{gameId}
     */
    @PutMapping("/{gameId}")
    public ResponseEntity<GameProfile> updateGame(@PathVariable String gameId,
                                                   @RequestBody UpdateGameRequest request) {
        return gameRegistry.updateGame(gameId, request)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Remove a game profile.
     *
     * DELETE /api/v1/game/{gameId}
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeGame(@PathVariable String gameId) {
        if (gameRegistry.removeGame(gameId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get a specific game profile.
     *
     * GET /api/v1/game/{gameId}
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameProfile> getGame(@PathVariable String gameId) {
        return gameRegistry.getGame(gameId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List all registered games.
     *
     * GET /api/v1/game/list
     */
    @GetMapping("/list")
    public ResponseEntity<List<GameProfile>> listGames() {
        return ResponseEntity.ok(gameRegistry.getAllGames());
    }

    /**
     * List all active games.
     *
     * GET /api/v1/game/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<GameProfile>> listActiveGames() {
        return ResponseEntity.ok(gameRegistry.getActiveGames());
    }

    /**
     * Get the default game profile.
     *
     * GET /api/v1/game/default
     */
    @GetMapping("/default")
    public ResponseEntity<GameProfile> getDefaultGame() {
        return gameRegistry.getDefaultGame()
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get summary statistics for all games.
     *
     * GET /api/v1/game/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGameStatus() {
        List<GameProfile> allGames = gameRegistry.getAllGames();
        long activeCount = allGames.stream()
                .filter(p -> p.getStatus() == com.yisy.modules.doraneuron.model.multigame.GameStatus.ACTIVE)
                .count();

        long totalPredictions = allGames.stream()
                .mapToLong(GameProfile::getTotalPredictions)
                .sum();

        long totalExperience = allGames.stream()
                .mapToLong(GameProfile::getTotalExperienceCollected)
                .sum();

        return ResponseEntity.ok(Map.of(
                "totalGames", gameRegistry.getGameCount(),
                "activeGames", activeCount,
                "totalPredictions", totalPredictions,
                "totalExperience", totalExperience,
                "games", allGames.stream().map(p -> Map.of(
                        "gameId", p.getGameId(),
                        "gameName", p.getGameName(),
                        "status", p.getStatus(),
                        "modelVersion", p.getModelVersion(),
                        "predictions", p.getTotalPredictions(),
                        "experience", p.getTotalExperienceCollected()
                )).toList()
        ));
    }
}
