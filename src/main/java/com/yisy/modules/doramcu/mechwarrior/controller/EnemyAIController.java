package com.yisy.modules.doramcu.mechwarrior.controller;

import com.yisy.modules.doramcu.mechwarrior.ai.EnemyAI;
import com.yisy.modules.doramcu.mechwarrior.ai.EnemyPilot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for enemy AI management in MechWarrior missions.
 * <p>
 * Provides endpoints for spawning enemies, processing AI ticks,
 * and querying enemy state.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/mechwarrior/enemies")
public class EnemyAIController {

    private final EnemyAI enemyAI;

    public EnemyAIController(EnemyAI enemyAI) {
        this.enemyAI = enemyAI;
    }

    /**
     * Spawn enemies for a mission.
     * POST /api/v1/mechwarrior/enemies/spawn?sessionId={sessionId}&missionId={missionId}
     */
    @PostMapping("/spawn")
    public ResponseEntity<?> spawnEnemies(
            @RequestParam String sessionId,
            @RequestParam String missionId) {
        try {
            List<EnemyPilot> enemies = enemyAI.spawnEnemies(sessionId, missionId);
            enemyAI.registerEnemies(sessionId, enemies);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "enemyCount", enemies.size(),
                    "enemies", enemies.stream().map(e -> Map.of(
                            "id", e.getPilot().getId(),
                            "name", e.getPilot().getName(),
                            "role", e.getRole().name(),
                            "difficulty", e.getDifficultyLevel(),
                            "mech", e.getChassis().getName(),
                            "weapons", e.getWeapons().size()
                    )).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Process AI tick for all enemies in a session.
     * POST /api/v1/mechwarrior/enemies/tick?sessionId={sessionId}
     */
    @PostMapping("/tick")
    public ResponseEntity<?> processAITick(@RequestParam String sessionId) {
        try {
            enemyAI.processAITick(sessionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "sessionId", sessionId,
                    "enemyCount", enemyAI.getEnemyIds(sessionId).size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get enemy IDs in a session.
     * GET /api/v1/mechwarrior/enemies?sessionId={sessionId}
     */
    @GetMapping
    public ResponseEntity<?> getEnemies(@RequestParam String sessionId) {
        List<String> enemyIds = enemyAI.getEnemyIds(sessionId);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "enemyCount", enemyIds.size(),
                "enemyIds", enemyIds
        ));
    }

    /**
     * Remove enemies from a session when it ends.
     * DELETE /api/v1/mechwarrior/enemies?sessionId={sessionId}
     */
    @DeleteMapping
    public ResponseEntity<?> removeEnemies(@RequestParam String sessionId) {
        enemyAI.removeSessionEnemies(sessionId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", sessionId
        ));
    }
}
