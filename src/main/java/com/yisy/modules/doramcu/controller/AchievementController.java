package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.service.AchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * AchievementController — REST API for cross-game achievements and leaderboards.
 *
 * Endpoints:
 * - GET  /api/v1/achievements/definitions          — All achievement definitions
 * - GET  /api/v1/achievements/{playerId}           — Player achievements
 * - GET  /api/v1/achievements/{playerId}/progress  — Achievement progress
 * - POST /api/v1/achievements/{playerId}/unlock    — Unlock an achievement
 * - GET  /api/v1/achievements/leaderboard          — Global leaderboard
 * - GET  /api/v1/achievements/{playerId}/stats     — Player stats
 * - GET  /api/v1/achievements/{playerId}/history   — Game history
 * - GET  /api/v1/achievements/{playerId}/inventory — Cross-game inventory
 * - GET  /api/v1/achievements/{playerId}/notifications — Notifications
 * - POST /api/v1/achievements/{playerId}/notifications/clear — Clear notifications
 */
@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping("/definitions")
    public ResponseEntity<Map<String, AchievementService.AchievementDefinition>> getDefinitions() {
        return ResponseEntity.ok(achievementService.getAchievementDefinitions());
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<List<AchievementService.AchievementDto>> getPlayerAchievements(@PathVariable String playerId) {
        return ResponseEntity.ok(achievementService.getPlayerAchievements(playerId));
    }

    @GetMapping("/{playerId}/progress")
    public ResponseEntity<AchievementService.AchievementProgress> getProgress(@PathVariable String playerId) {
        return ResponseEntity.ok(achievementService.getAchievementProgress(playerId));
    }

    @PostMapping("/{playerId}/unlock")
    public ResponseEntity<Map<String, Object>> unlockAchievement(
            @PathVariable String playerId,
            @RequestBody Map<String, String> request) {
        String achievementId = request.get("achievementId");
        boolean unlocked = achievementService.unlockAchievement(playerId, achievementId);
        Map<String, Object> response = new HashMap<>();
        response.put("unlocked", unlocked);
        response.put("achievementId", achievementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "overall") String category,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit) {
        AchievementService.LeaderboardEntry[] entries = achievementService.getLeaderboard(category, offset, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = offset + 1;
        for (AchievementService.LeaderboardEntry entry : entries) {
            Map<String, Object> map = new HashMap<>();
            map.put("rank", rank++);
            map.put("playerId", entry.playerId);
            map.put("score", entry.score);
            map.put("gamesCompleted", entry.stats.gamesCompleted);
            map.put("wins", entry.stats.wins);
            map.put("losses", entry.stats.losses);
            map.put("hoursPlayed", entry.stats.hoursPlayed);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{playerId}/stats")
    public ResponseEntity<AchievementService.PlayerStats> getPlayerStats(@PathVariable String playerId) {
        return ResponseEntity.ok(achievementService.getPlayerStats(playerId));
    }

    @GetMapping("/{playerId}/history")
    public ResponseEntity<List<AchievementService.GameHistoryEntry>> getGameHistory(
            @PathVariable String playerId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(achievementService.getGameHistory(playerId, limit));
    }

    @GetMapping("/{playerId}/inventory")
    public ResponseEntity<List<AchievementService.CrossGameItem>> getInventory(@PathVariable String playerId) {
        return ResponseEntity.ok(achievementService.getInventory(playerId));
    }

    @GetMapping("/{playerId}/notifications")
    public ResponseEntity<List<AchievementService.AchievementNotification>> getNotifications(@PathVariable String playerId) {
        return ResponseEntity.ok(achievementService.getNotifications(playerId));
    }

    @PostMapping("/{playerId}/notifications/clear")
    public ResponseEntity<Map<String, String>> clearNotifications(@PathVariable String playerId) {
        achievementService.clearNotifications(playerId);
        return ResponseEntity.ok(Collections.singletonMap("status", "cleared"));
    }
}
