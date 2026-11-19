package com.yisy.modules.doramcu.mechwarrior.controller;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorSession;
import com.yisy.modules.doramcu.mechwarrior.model.Mission;
import com.yisy.modules.doramcu.mechwarrior.model.MissionObjective;
import com.yisy.modules.doramcu.mechwarrior.service.MissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for MechWarrior mission management.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Browsing available missions by type, difficulty, and faction</li>
 *   <li>Accepting, starting, completing, and abandoning missions</li>
 *   <li>Tracking objective progress</li>
 *   <li>Viewing pilot mission stats and reputation</li>
 *   <li>Managing credits and economy</li>
 *   <li>Creating custom missions</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/mechwarrior/missions")
public class MissionController {

    private static final Logger log = LoggerFactory.getLogger(MissionController.class);

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    // ==================== Mission Browsing ====================

    /**
     * Get all available missions.
     */
    @GetMapping
    public ResponseEntity<List<Mission>> listAvailableMissions() {
        return ResponseEntity.ok(missionService.getAvailableMissions());
    }

    /**
     * Get all missions including active and completed.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Mission>> listAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    /**
     * Get a specific mission by ID.
     */
    @GetMapping("/{missionId}")
    public ResponseEntity<Mission> getMission(@PathVariable String missionId) {
        Mission mission = missionService.getMission(missionId);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mission);
    }

    /**
     * Get missions by type.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Mission>> getMissionsByType(@PathVariable String type) {
        try {
            Mission.MissionType missionType = Mission.MissionType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(missionService.getMissionsByType(missionType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get missions by difficulty.
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Mission>> getMissionsByDifficulty(@PathVariable String difficulty) {
        try {
            Mission.MissionDifficulty missionDifficulty = Mission.MissionDifficulty.valueOf(difficulty.toUpperCase());
            return ResponseEntity.ok(missionService.getMissionsByDifficulty(missionDifficulty));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get missions by faction.
     */
    @GetMapping("/faction/{faction}")
    public ResponseEntity<List<Mission>> getMissionsByFaction(@PathVariable String faction) {
        return ResponseEntity.ok(missionService.getMissionsByFaction(faction));
    }

    // ==================== Mission Lifecycle ====================

    /**
     * Accept a mission.
     */
    @PostMapping("/{missionId}/accept")
    public ResponseEntity<Map<String, Object>> acceptMission(
            @PathVariable String missionId,
            @RequestParam String pilotId) {
        Map<String, Object> response = new HashMap<>();

        boolean accepted = missionService.acceptMission(pilotId, missionId);
        response.put("success", accepted);

        if (accepted) {
            Mission mission = missionService.getMission(missionId);
            response.put("mission", mission);
            response.put("message", "Mission accepted: " + mission.getName());
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to accept mission");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Start a mission, creating a game session.
     */
    @PostMapping("/{missionId}/start")
    public ResponseEntity<MechWarriorSession> startMission(
            @PathVariable String missionId,
            @RequestParam String pilotId) {
        try {
            MechWarriorSession session = missionService.startMission(pilotId, missionId);
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete a mission.
     */
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<MissionResult> completeMission(
            @PathVariable String missionId,
            @RequestParam String pilotId) {
        try {
            MissionResult result = missionService.completeMission(pilotId, missionId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Fail a mission.
     */
    @PostMapping("/{missionId}/fail")
    public ResponseEntity<MissionResult> failMission(
            @PathVariable String missionId,
            @RequestParam String pilotId) {
        try {
            MissionResult result = missionService.failMission(pilotId, missionId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Abandon a mission.
     */
    @PostMapping("/{missionId}/abandon")
    public ResponseEntity<Map<String, Object>> abandonMission(
            @PathVariable String missionId,
            @RequestParam String pilotId) {
        missionService.abandonMission(pilotId, missionId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mission abandoned");
        return ResponseEntity.ok(response);
    }

    // ==================== Objective Progress ====================

    /**
     * Record a kill during a mission.
     */
    @PostMapping("/{missionId}/kill")
    public ResponseEntity<Map<String, Object>> recordKill(
            @PathVariable String missionId,
            @RequestParam String pilotId) {
        missionService.recordKill(pilotId, missionId);

        Mission mission = missionService.getMission(missionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("objectives", mission.getObjectives());
        response.put("allCompleted", mission.areAllObjectivesCompleted());
        return ResponseEntity.ok(response);
    }

    /**
     * Record progress on a specific objective.
     */
    @PostMapping("/{missionId}/objective/{objectiveId}/progress")
    public ResponseEntity<Map<String, Object>> recordObjectiveProgress(
            @PathVariable String missionId,
            @PathVariable String objectiveId,
            @RequestParam String pilotId,
            @RequestParam(defaultValue = "1") int amount) {
        missionService.recordObjectiveProgress(pilotId, missionId, objectiveId, amount);

        Mission mission = missionService.getMission(missionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("objectives", mission.getObjectives());
        response.put("allCompleted", mission.areAllObjectivesCompleted());
        return ResponseEntity.ok(response);
    }

    /**
     * Record time elapsed for duration-based objectives.
     */
    @PostMapping("/{missionId}/time")
    public ResponseEntity<Map<String, Object>> recordTimeElapsed(
            @PathVariable String missionId,
            @RequestParam String pilotId,
            @RequestParam(defaultValue = "1") int seconds) {
        missionService.recordTimeElapsed(pilotId, missionId, seconds);

        Mission mission = missionService.getMission(missionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("objectives", mission.getObjectives());
        response.put("allCompleted", mission.areAllObjectivesCompleted());
        return ResponseEntity.ok(response);
    }

    /**
     * Get the current mission status and objectives.
     */
    @GetMapping("/{missionId}/status")
    public ResponseEntity<Map<String, Object>> getMissionStatus(@PathVariable String missionId) {
        Mission mission = missionService.getMission(missionId);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mission", mission);
        response.put("objectives", mission.getObjectives());
        response.put("completedObjectives", mission.getCompletedObjectiveCount());
        response.put("totalObjectives", mission.getObjectives().size());
        response.put("allCompleted", mission.areAllObjectivesCompleted());
        return ResponseEntity.ok(response);
    }

    // ==================== Pilot Stats ====================

    /**
     * Get pilot mission statistics.
     */
    @GetMapping("/pilot/{pilotId}/stats")
    public ResponseEntity<PilotMissionStats> getPilotStats(@PathVariable String pilotId) {
        PilotMissionStats stats = missionService.getPilotStats(pilotId);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    /**
     * Get pilot's active mission.
     */
    @GetMapping("/pilot/{pilotId}/active")
    public ResponseEntity<Mission> getActiveMission(@PathVariable String pilotId) {
        Mission mission = missionService.getActiveMission(pilotId);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mission);
    }

    // ==================== Reputation ====================

    /**
     * Get reputation with a specific faction.
     */
    @GetMapping("/pilot/{pilotId}/reputation/{faction}")
    public ResponseEntity<Map<String, Object>> getReputation(
            @PathVariable String pilotId,
            @PathVariable String faction) {
        int reputation = missionService.getReputation(pilotId, faction);

        Map<String, Object> response = new HashMap<>();
        response.put("faction", faction);
        response.put("reputation", reputation);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all faction reputations for a pilot.
     */
    @GetMapping("/pilot/{pilotId}/reputation")
    public ResponseEntity<Map<String, Integer>> getAllReputations(@PathVariable String pilotId) {
        return ResponseEntity.ok(missionService.getAllReputations(pilotId));
    }

    // ==================== Credits ====================

    /**
     * Get pilot's current credits.
     */
    @GetMapping("/pilot/{pilotId}/credits")
    public ResponseEntity<Map<String, Object>> getCredits(@PathVariable String pilotId) {
        int credits = missionService.getCredits(pilotId);

        Map<String, Object> response = new HashMap<>();
        response.put("credits", credits);
        return ResponseEntity.ok(response);
    }

    /**
     * Spend credits.
     */
    @PostMapping("/pilot/{pilotId}/credits/spend")
    public ResponseEntity<Map<String, Object>> spendCredits(
            @PathVariable String pilotId,
            @RequestParam int amount) {
        boolean success = missionService.spendCredits(pilotId, amount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("credits", missionService.getCredits(pilotId));

        if (success) {
            response.put("message", "Credits spent successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Insufficient credits");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== Custom Missions ====================

    /**
     * Create a custom mission.
     */
    @PostMapping("/custom")
    public ResponseEntity<Mission> createCustomMission(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String type = (String) request.get("type");
        String difficulty = (String) request.get("difficulty");
        String faction = (String) request.get("faction");
        String description = (String) request.get("description");
        int enemyCount = request.get("enemyCount") != null ? (int) request.get("enemyCount") : 4;
        int creditsReward = request.get("creditsReward") != null ? (int) request.get("creditsReward") : 10000;

        Mission.MissionType missionType;
        try {
            missionType = Mission.MissionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            missionType = Mission.MissionType.CUSTOM;
        }

        Mission.MissionDifficulty missionDifficulty;
        try {
            missionDifficulty = Mission.MissionDifficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            missionDifficulty = Mission.MissionDifficulty.NORMAL;
        }

        Mission mission = missionService.createCustomMission(
                name, missionType, missionDifficulty, faction, description, enemyCount, creditsReward
        );
        return ResponseEntity.ok(mission);
    }

    /**
     * Remove a mission.
     */
    @DeleteMapping("/{missionId}")
    public ResponseEntity<Map<String, Object>> removeMission(@PathVariable String missionId) {
        missionService.removeMission(missionId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mission removed");
        return ResponseEntity.ok(response);
    }

    // ==================== Summary ====================

    /**
     * Get a summary of the mission system.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Mission> allMissions = missionService.getAllMissions();

        Map<String, Object> response = new HashMap<>();
        response.put("totalMissions", allMissions.size());
        response.put("availableMissions", missionService.getAvailableMissions().size());

        // Count by type
        Map<String, Long> byType = new HashMap<>();
        for (Mission.MissionType type : Mission.MissionType.values()) {
            long count = allMissions.stream().filter(m -> m.getType() == type).count();
            if (count > 0) {
                byType.put(type.name(), count);
            }
        }
        response.put("missionsByType", byType);

        // Count by difficulty
        Map<String, Long> byDifficulty = new HashMap<>();
        for (Mission.MissionDifficulty difficulty : Mission.MissionDifficulty.values()) {
            long count = allMissions.stream().filter(m -> m.getDifficulty() == difficulty).count();
            if (count > 0) {
                byDifficulty.put(difficulty.name(), count);
            }
        }
        response.put("missionsByDifficulty", byDifficulty);

        return ResponseEntity.ok(response);
    }
}
