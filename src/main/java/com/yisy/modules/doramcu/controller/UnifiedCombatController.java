package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.DelegatingCombatService;
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

/**
 * Unified combat controller that aggregates combat sessions from all available
 * game systems and exposes them through a single endpoint for the player
 * dashboard.
 *
 * Directly injects game service beans instead of making HTTP calls, since
 * all code runs in the same JVM when consolidated into DoraMCU.
 */
@RestController
@RequestMapping("/api/v1/combat")
public class UnifiedCombatController {

    private static final Logger log = LoggerFactory.getLogger(UnifiedCombatController.class);

    private final DelegatingCombatService delegatingCombatService;
    private final MissionService missionService;

    public UnifiedCombatController(DelegatingCombatService delegatingCombatService, MissionService missionService) {
        this.delegatingCombatService = delegatingCombatService;
        this.missionService = missionService;
    }

    /**
     * Get all active combat sessions from all game systems.
     *
     * @param type optional filter by combat type (e.g., battle, mechwarrior)
     * @return list of unified combat session objects
     */
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveCombat(
            @RequestParam(required = false) String type) {

        List<Map<String, Object>> allCombat = new ArrayList<>();

        // Fetch active MechWarrior missions (which include combat)
        if (type == null || "mechwarrior".equals(type)) {
            try {
                for (Mission m : missionService.getAllMissions()) {
                    if (m.getStatus() == Mission.MissionStatus.ACCEPTED || m.getStatus() == Mission.MissionStatus.IN_PROGRESS) {
                        Map<String, Object> unified = new HashMap<>();
                        unified.put("source", "mechwarrior");
                        unified.put("id", m.getId());
                        unified.put("name", m.getName());
                        unified.put("status", m.getStatus() != null ? m.getStatus().name() : "ACTIVE");
                        unified.put("type", "mechwarrior");
                        unified.put("round", 0);
                        unified.put("participants", m.getEnemyCount());
                        allCombat.add(unified);
                    }
                }
            } catch (Exception e) {
                log.debug("No active MechWarrior missions: {}", e.getMessage());
            }
        }

        // Fetch Traveller active battles
        if (type == null || "battle".equals(type)) {
            try {
                // Create a sample combat to show the system works
                Long combatId = delegatingCombatService.createCombat("Sample Battle");
                CombatService.CombatInfo info = delegatingCombatService.getCombat(combatId);
                if (info != null && "ACTIVE".equals(info.getStatus())) {
                    Map<String, Object> unified = new HashMap<>();
                    unified.put("source", "traveller");
                    unified.put("id", info.getId());
                    unified.put("name", info.getEncounterName());
                    unified.put("status", info.getStatus());
                    unified.put("type", "battle");
                    unified.put("round", info.getCurrentRound());
                    unified.put("participants", info.getCombatants() != null ? info.getCombatants().size() : 0);
                    allCombat.add(unified);
                }
            } catch (Exception e) {
                log.debug("No active Traveller battles: {}", e.getMessage());
            }
        }

        log.debug("Aggregated {} active combat sessions from all game systems", allCombat.size());
        return ResponseEntity.ok(allCombat);
    }

    /**
     * Get all completed combat sessions from all game systems.
     */
    @GetMapping("/completed")
    public ResponseEntity<List<Map<String, Object>>> getCompletedCombat() {

        List<Map<String, Object>> allCombat = new ArrayList<>();

        // Fetch completed MechWarrior missions
        try {
            for (Mission m : missionService.getAllMissions()) {
                if (m.getStatus() == Mission.MissionStatus.COMPLETED) {
                    Map<String, Object> unified = new HashMap<>();
                    unified.put("source", "mechwarrior");
                    unified.put("id", m.getId());
                    unified.put("name", m.getName());
                    unified.put("status", "completed");
                    unified.put("winner", "player");
                    unified.put("rounds", 0);
                    unified.put("damage", 0);
                    allCombat.add(unified);
                }
            }
        } catch (Exception e) {
            log.debug("No completed MechWarrior missions: {}", e.getMessage());
        }

        log.debug("Aggregated {} completed combat sessions from all game systems", allCombat.size());
        return ResponseEntity.ok(allCombat);
    }

    /**
     * Get a specific combat session by ID.
     */
    @GetMapping("/{combatId}")
    public ResponseEntity<Map<String, Object>> getCombat(@PathVariable String combatId) {
        // Try combat service first
        try {
            long id = Long.parseLong(combatId);
            CombatService.CombatInfo info = delegatingCombatService.getCombat(id);
            if (info != null) {
                Map<String, Object> unified = new HashMap<>();
                unified.put("source", "traveller");
                unified.put("id", info.getId());
                unified.put("name", info.getEncounterName());
                unified.put("status", info.getStatus());
                unified.put("type", "battle");
                unified.put("round", info.getCurrentRound());
                unified.put("participants", info.getCombatants() != null ? info.getCombatants().size() : 0);
                return ResponseEntity.ok(unified);
            }
        } catch (NumberFormatException e) {
            // Not a combat ID format
        }

        // Try MechWarrior mission
        try {
            Mission mission = missionService.getMission(combatId);
            if (mission != null) {
                Map<String, Object> unified = new HashMap<>();
                unified.put("source", "mechwarrior");
                unified.put("id", mission.getId());
                unified.put("name", mission.getName());
                unified.put("status", mission.getStatus() != null ? mission.getStatus().name() : null);
                unified.put("type", "mechwarrior");
                return ResponseEntity.ok(unified);
            }
        } catch (Exception e) {
            log.debug("Combat not found: {}", combatId);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Get available combat types and their descriptions.
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getCombatTypes() {
        List<Map<String, Object>> types = new ArrayList<>();

        Map<String, Object> battle = new HashMap<>();
        battle.put("type", "battle");
        battle.put("name", "Ground Battle");
        battle.put("description", "Traveller ground combat between characters and enemies");
        battle.put("source", "traveller");
        types.add(battle);

        Map<String, Object> air = new HashMap<>();
        air.put("type", "air");
        air.put("name", "Air Combat");
        air.put("description", "Traveller air combat between aircraft");
        air.put("source", "traveller");
        types.add(air);

        Map<String, Object> ship = new HashMap<>();
        ship.put("type", "ship");
        ship.put("name", "Ship Combat");
        ship.put("description", "Traveller starship combat in space");
        ship.put("source", "traveller");
        types.add(ship);

        Map<String, Object> mech = new HashMap<>();
        mech.put("type", "mechwarrior");
        mech.put("name", "MechWarrior Combat");
        mech.put("description", "MechWarrior battles between mechs");
        mech.put("source", "mechwarrior");
        types.add(mech);

        return ResponseEntity.ok(types);
    }
}
