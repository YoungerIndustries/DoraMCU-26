package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.eve.model.AiCommanderHiring;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for AI commander experience tracking and hiring.
 * <p>
 * Provides endpoints for viewing commander experience, hiring available
 * commanders, and managing hiring contracts.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/commanders")
public class AiCommanderController {

    private final AiCommanderExperienceService experienceService;

    public AiCommanderController(AiCommanderExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    /**
     * GET /api/v1/commanders/available
     * List all available (hirable) AI commanders.
     */
    @GetMapping("/available")
    public ResponseEntity<List<AiCharacter>> getAvailableCommanders(
            @RequestParam(required = false) Integer minExperience) {
        List<AiCharacter> commanders;
        if (minExperience != null && minExperience > 0) {
            commanders = experienceService.getAvailableCommanders(minExperience);
        } else {
            commanders = experienceService.getAvailableCommanders();
        }
        return ResponseEntity.ok(commanders);
    }

    /**
     * GET /api/v1/commanders/{id}
     * Get AI commander details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AiCharacter> getCommander(@PathVariable Long id) {
        return experienceService.getAvailableCommanders()
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/commanders/{id}/experience
     * Get battle experience history for an AI commander.
     */
    @GetMapping("/{id}/experience")
    public ResponseEntity<List<AiCommanderExperience>> getExperience(@PathVariable Long id) {
        List<AiCommanderExperience> experiences = experienceService.getExperiences(id);
        return ResponseEntity.ok(experiences);
    }

    /**
     * GET /api/v1/commanders/{id}/stats
     * Get experience stats for an AI commander.
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long id) {
        int totalXp = experienceService.getTotalExperience(id);
        long battleCount = experienceService.getBattleCount(id);
        return ResponseEntity.ok(Map.of(
                "aiCharacterId", id,
                "totalExperience", totalXp,
                "battleCount", battleCount
        ));
    }

    /**
     * POST /api/v1/commanders/{id}/hire
     * Hire an AI commander.
     */
    @PostMapping("/{id}/hire")
    public ResponseEntity<?> hireCommander(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String playerId = (String) request.get("playerId");
            if (playerId == null || playerId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "playerId is required"));
            }
            int durationDays = request.containsKey("durationDays")
                    ? ((Number) request.get("durationDays")).intValue()
                    : 0;
            AiCommanderHiring hiring = experienceService.hireCommander(id, playerId, durationDays);
            return ResponseEntity.ok(hiring);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/commanders/{id}/release
     * Release (fire) an AI commander.
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<?> releaseCommander(@PathVariable Long id) {
        try {
            AiCommanderHiring hiring = experienceService.releaseCommander(id);
            return ResponseEntity.ok(hiring);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/commanders/{id}/hiring
     * Get active hiring contract for an AI commander.
     */
    @GetMapping("/{id}/hiring")
    public ResponseEntity<?> getHiringStatus(@PathVariable Long id) {
        return experienceService.getActiveHiring(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * GET /api/v1/commanders/player/{playerId}
     * Get all hired commanders for a player.
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<AiCommanderHiring>> getPlayerCommanders(@PathVariable String playerId) {
        List<AiCommanderHiring> hired = experienceService.getPlayerHiredCommanders(playerId);
        return ResponseEntity.ok(hired);
    }
}
