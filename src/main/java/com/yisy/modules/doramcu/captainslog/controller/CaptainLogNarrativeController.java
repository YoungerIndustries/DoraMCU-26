package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.MissionDevelopmentService;
import com.yisy.modules.doramcu.captainslog.service.NarrativeBeatService;
import com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log narrative beat guidance endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides narrative beat guidance, act descriptions, and task suggestions.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogNarrativeController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogNarrativeController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogNarrativeController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Gets the narrative beat guidance for the current mission.
     * <p>
     * Returns the title, narrative prompt, suggested action, oracle questions,
     * and whether a task attempt is needed for the current beat.
     * </p>
     *
     * @param missionId the mission ID
     */
    @GetMapping("/missions/{missionId}/beat/guidance")
    public ResponseEntity<NarrativeBeatService.BeatGuidance> getBeatGuidance(@PathVariable Long missionId) {
        log.debug("GET /api/v1/captains-log/missions/{}/beat/guidance", missionId);
        NarrativeBeatService.BeatGuidance guidance = captainLogService.getBeatGuidance(missionId);
        if (guidance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(guidance);
    }

    /**
     * Gets all beats for a specific act.
     * <p>
     * Returns the 5 beats for the given act (1-3), each with title, prompt, and guidance.
     * </p>
     *
     * @param act the act number (1-3)
     */
    @GetMapping("/acts/{act}/beats")
    public ResponseEntity<NarrativeBeatService.BeatGuidance[]> getActBeats(@PathVariable int act) {
        log.debug("GET /api/v1/captains-log/acts/{}/beats", act);
        NarrativeBeatService.BeatGuidance[] beats = captainLogService.getActBeats(act);
        return ResponseEntity.ok(beats);
    }

    /**
     * Gets a description of an act's purpose.
     *
     * @param act the act number (1-3)
     */
    @GetMapping("/acts/{act}/description")
    public ResponseEntity<String> getActDescription(@PathVariable int act) {
        log.debug("GET /api/v1/captains-log/acts/{}/description", act);
        return ResponseEntity.ok(captainLogService.getActDescription(act));
    }

    /**
     * Suggests a task attempt based on the current beat and mission type.
     * <p>
     * Returns a suggested attribute + discipline combination for the current beat.
     * </p>
     *
     * @param missionId the mission ID
     */
    @GetMapping("/missions/{missionId}/beat/task-suggestion")
    public ResponseEntity<String> suggestTaskAttempt(@PathVariable Long missionId) {
        log.debug("GET /api/v1/captains-log/missions/{}/beat/task-suggestion", missionId);
        String suggestion = captainLogService.suggestTaskAttempt(missionId);
        if (suggestion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(suggestion);
    }

    /**
     * Checks if the current beat is an act transition.
     * <p>
     * Returns true if the player should write a log entry and advance the act.
     * </p>
     *
     * @param missionId the mission ID
     */
    @GetMapping("/missions/{missionId}/beat/is-transition")
    public ResponseEntity<Boolean> isActTransition(@PathVariable Long missionId) {
        log.debug("GET /api/v1/captains-log/missions/{}/beat/is-transition", missionId);
        return ResponseEntity.ok(captainLogService.isActTransition(missionId));
    }

    /**
     * Gets the log entry prompt for the current act transition.
     * <p>
     * Returns a prompt for writing the log entry at the end of an act.
     * Returns null if the current beat is not an act transition.
     * </p>
     *
     * @param missionId the mission ID
     */
    @GetMapping("/missions/{missionId}/beat/log-prompt")
    public ResponseEntity<String> getLogEntryPrompt(@PathVariable Long missionId) {
        log.debug("GET /api/v1/captains-log/missions/{}/beat/log-prompt", missionId);
        String prompt = captainLogService.getLogEntryPrompt(missionId);
        if (prompt == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(prompt);
    }
}
