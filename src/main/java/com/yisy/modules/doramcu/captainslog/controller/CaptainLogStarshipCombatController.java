package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.dto.StartCombatRequest;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log starship combat endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides starship combat encounters, actions, and state management.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogStarshipCombatController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogStarshipCombatController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogStarshipCombatController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Starts an enhanced starship combat encounter.
     * <p>
     * Requires the character to have an assigned starship.
     * </p>
     */
    @PostMapping("/starship-combat/start")
    public ResponseEntity<Map<String, Object>> startStarshipCombat(@RequestBody StartCombatRequest request) {
        log.debug("POST /api/v1/captains-log/starship-combat/start");
        Map<String, Object> result = captainLogService.startStarshipCombat(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Executes a combat action in starship combat.
     * <p>
     * Actions: ATTACK, TORPEDO, MANEUVER, REPAIR, EVASIVE, SHIELD_RECHARGE
     * Optional targetSystem for REPAIR action.
     * </p>
     */
    @PostMapping("/starship-combat/{combatId}/action")
    public ResponseEntity<Map<String, Object>> executeStarshipCombatAction(
            @PathVariable Long combatId,
            @RequestBody Map<String, String> body) {
        log.debug("POST /api/v1/captains-log/starship-combat/{}/action", combatId);
        String actionName = body.get("action");
        String targetSystem = body.get("targetSystem");
        Map<String, Object> result = captainLogService.executeStarshipCombatAction(combatId, actionName, targetSystem);
        return ResponseEntity.ok(result);
    }

    /**
     * Gets the current state of a starship combat.
     */
    @GetMapping("/starship-combat/{combatId}")
    public ResponseEntity<Map<String, Object>> getStarshipCombatState(@PathVariable Long combatId) {
        log.debug("GET /api/v1/captains-log/starship-combat/{}", combatId);
        Map<String, Object> result = captainLogService.getStarshipCombatState(combatId);
        return ResponseEntity.ok(result);
    }

    /**
     * Retreats from starship combat.
     */
    @PostMapping("/starship-combat/{combatId}/retreat")
    public ResponseEntity<Map<String, Object>> retreatFromStarshipCombat(@PathVariable Long combatId) {
        log.debug("POST /api/v1/captains-log/starship-combat/{}/retreat", combatId);
        Map<String, Object> result = captainLogService.retreatFromStarshipCombat(combatId);
        return ResponseEntity.ok(result);
    }

    /**
     * Lists all available starship combat actions with descriptions.
     */
    @GetMapping("/starship-combat/actions")
    public ResponseEntity<List<Map<String, String>>> listStarshipCombatActions() {
        log.debug("GET /api/v1/captains-log/starship-combat/actions");
        return ResponseEntity.ok(captainLogService.listStarshipCombatActions());
    }

    /**
     * Lists all range bands with descriptions.
     */
    @GetMapping("/starship-combat/range-bands")
    public ResponseEntity<List<Map<String, String>>> listStarshipCombatRangeBands() {
        log.debug("GET /api/v1/captains-log/starship-combat/range-bands");
        return ResponseEntity.ok(captainLogService.listStarshipCombatRangeBands());
    }
}
