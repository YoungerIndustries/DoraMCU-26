package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log Downtime Phase endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides downtime activities, value reflection, and downtime phase management.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogDowntimeController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogDowntimeController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogDowntimeController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Starts a downtime phase for a character after completing a mission.
     * <p>
     * Resolves threat, recovers injuries, repairs starship, and resets value flags.
     * </p>
     */
    @PostMapping("/downtime/start")
    public ResponseEntity<Map<String, Object>> startDowntime(@RequestBody Map<String, Object> body) {
        log.debug("POST /api/v1/captains-log/downtime/start");
        Long characterId = Long.parseLong(String.valueOf(body.get("characterId")));
        int missionThreat = Integer.parseInt(String.valueOf(body.getOrDefault("missionThreat", 0)));
        Map<String, Object> result = captainLogService.startDowntime(characterId, missionThreat);
        return ResponseEntity.ok(result);
    }

    /**
     * Performs a specific downtime activity.
     * <p>
     * The player provides the activity name and whether the task attempt succeeded.
     * </p>
     */
    @PostMapping("/downtime/activity")
    public ResponseEntity<Map<String, Object>> performDowntimeActivity(@RequestBody Map<String, Object> body) {
        log.debug("POST /api/v1/captains-log/downtime/activity");
        Long characterId = Long.parseLong(String.valueOf(body.get("characterId")));
        String activityName = String.valueOf(body.get("activity"));
        boolean taskSuccess = Boolean.parseBoolean(String.valueOf(body.getOrDefault("taskSuccess", true)));
        Map<String, Object> result = captainLogService.performDowntimeActivity(characterId, activityName, taskSuccess);
        return ResponseEntity.ok(result);
    }

    /**
     * Guides value reflection after a mission.
     * <p>
     * The player indicates which values were used, challenged, retired, or added.
     * </p>
     */
    @PostMapping("/downtime/values/reflect")
    public ResponseEntity<Map<String, Object>> reflectOnValues(@RequestBody Map<String, Object> body) {
        log.debug("POST /api/v1/captains-log/downtime/values/reflect");
        Long characterId = Long.parseLong(String.valueOf(body.get("characterId")));
        @SuppressWarnings("unchecked")
        List<Long> usedValueIds = (List<Long>) body.getOrDefault("usedValueIds", List.of());
        @SuppressWarnings("unchecked")
        List<Long> challengedValueIds = (List<Long>) body.getOrDefault("challengedValueIds", List.of());
        Long retiredValueId = body.get("retiredValueId") != null ? Long.parseLong(String.valueOf(body.get("retiredValueId"))) : null;
        String newValueDescription = (String) body.get("newValueDescription");
        Map<String, Object> result = captainLogService.reflectOnValues(characterId, usedValueIds,
                challengedValueIds, retiredValueId, newValueDescription);
        return ResponseEntity.ok(result);
    }

    /**
     * Lists all available downtime activities with descriptions.
     */
    @GetMapping("/downtime/activities")
    public ResponseEntity<List<Map<String, String>>> listDowntimeActivities() {
        log.debug("GET /api/v1/captains-log/downtime/activities");
        return ResponseEntity.ok(captainLogService.getAllDowntimeActivities());
    }
}
