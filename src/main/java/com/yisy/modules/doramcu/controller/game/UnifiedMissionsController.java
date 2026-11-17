package com.yisy.modules.doramcu.controller.game;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.mechwarrior.model.Mission;
import com.yisy.modules.doramcu.mechwarrior.service.MissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unified missions controller that aggregates missions from all available
 * game systems and exposes them through a single endpoint for the player
 * dashboard.
 *
 * Directly injects game service beans instead of making HTTP calls, since
 * all code runs in the same JVM when consolidated into DoraMCU.
 */
@RestController
@RequestMapping("/api/v1/missions")
public class UnifiedMissionsController {

    private static final Logger log = LoggerFactory.getLogger(UnifiedMissionsController.class);

    private final MissionService missionService;

    public UnifiedMissionsController(MissionService missionService) {
        this.missionService = missionService;
    }

    /**
     * Get all available missions from all game systems.
     *
     * @param type optional filter by mission type (e.g., ASSAULT, DEFENSE)
     * @param difficulty optional filter by difficulty (e.g., EASY, NORMAL)
     * @return list of unified mission objects
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMissions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {

        List<Map<String, Object>> allMissions = new ArrayList<>();

        // Fetch MechWarrior missions (primary source with pre-populated data)
        List<Mission> mechMissions = missionService.getAllMissions();
        for (Mission m : mechMissions) {
            if (type != null && m.getType() != null && !m.getType().name().equalsIgnoreCase(type)) {
                continue;
            }
            if (difficulty != null && m.getDifficulty() != null && !m.getDifficulty().name().equalsIgnoreCase(difficulty)) {
                continue;
            }
            allMissions.addAll(unifyMission(m));
        }

        log.debug("Aggregated {} missions from all game systems", allMissions.size());
        return ResponseEntity.ok(allMissions);
    }

    /**
     * Get a specific mission by ID.
     */
    @GetMapping("/{missionId}")
    public ResponseEntity<Map<String, Object>> getMission(@PathVariable String missionId) {
        Mission mission = missionService.getMission(missionId);
        if (mission != null) {
            List<Map<String, Object>> unified = unifyMission(mission);
            if (!unified.isEmpty()) {
                return ResponseEntity.ok(unified.get(0));
            }
        }
        return ResponseEntity.notFound().build();
    }

    private List<Map<String, Object>> unifyMission(Mission mission) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> unified = new HashMap<>();
        unified.put("source", "mechwarrior");
        unified.put("id", mission.getId());
        unified.put("name", mission.getName());
        unified.put("description", mission.getDescription());
        unified.put("type", mission.getType() != null ? mission.getType().name() : null);
        unified.put("difficulty", mission.getDifficulty() != null ? mission.getDifficulty().name() : null);
        unified.put("status", mission.getStatus() != null ? mission.getStatus().name() : null);
        unified.put("planet", mission.getPlanetName());
        unified.put("system", mission.getSystemName());
        unified.put("faction", mission.getFaction());
        unified.put("creditsReward", mission.getCreditsReward());
        unified.put("experienceReward", mission.getExperienceReward());
        unified.put("reputationReward", mission.getReputationReward());
        unified.put("enemyCount", mission.getEnemyCount());
        unified.put("isCampaign", mission.isCampaign());
        unified.put("isRepeatable", mission.isRepeatable());
        unified.put("isCoop", mission.isCoop());
        unified.put("minPilotLevel", mission.getMinPilotLevel());
        result.add(unified);
        return result;
    }
}
